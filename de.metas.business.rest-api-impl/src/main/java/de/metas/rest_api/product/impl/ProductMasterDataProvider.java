package de.metas.rest_api.product.impl;

import static de.metas.util.lang.CoalesceUtil.coalesceSuppliers;
import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;

import java.util.Properties;

import javax.annotation.Nullable;

import org.compiere.model.I_M_Product;
import org.compiere.model.X_M_Product;

import de.metas.cache.CCache;
import de.metas.organization.OrgId;
import de.metas.product.IProductBL;
import de.metas.product.IProductDAO;
import de.metas.product.IProductDAO.ProductQuery;
import de.metas.product.ProductCategoryId;
import de.metas.product.ProductId;
import de.metas.rest_api.SyncAdvise;
import de.metas.rest_api.SyncAdvise.IfExists;
import de.metas.rest_api.bpartner.impl.BPartnerMasterDataContext;
import de.metas.rest_api.product.JsonProductInfo;
import de.metas.rest_api.utils.PermissionService;
import de.metas.uom.IUOMDAO;
import de.metas.uom.UomId;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.StringUtils;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.ordercandidate.rest-api-impl
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class ProductMasterDataProvider
{
	public static ProductMasterDataProvider of(
			@Nullable final Properties ctx,
			@Nullable final PermissionService permissionService)
	{
		return new ProductMasterDataProvider(
				coalesceSuppliers(
						() -> permissionService,
						() -> PermissionService.of(ctx)));
	}

	private final IProductDAO productsRepo = Services.get(IProductDAO.class);
	private final IProductBL productsBL = Services.get(IProductBL.class);
	private final IUOMDAO uomsRepo = Services.get(IUOMDAO.class);

	private final ProductCategoryId defaultProductCategoryId = ProductCategoryId.ofRepoId(1000000); // TODO
	private PermissionService permissionService;

	public ProductMasterDataProvider(@NonNull final PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}

	@Value
	private static class CachingKey
	{
		OrgId orgId;
		JsonProductInfo jsonProductInfo;
	}

	@Value
	public static class ProductInfo
	{
		ProductId productId;
		UomId uomId;
	}

	private final CCache<CachingKey, ProductInfo> productInfoCache = CCache
			.<CachingKey, ProductInfo> builder()
			.cacheName(this.getClass().getSimpleName() + "-productInfoCache")
			.tableName(I_M_Product.Table_Name)
			.build();

	public ProductInfo getCreateProductInfo(
			@NonNull final JsonProductInfo jsonProductInfo,
			@NonNull final OrgId orgId)
	{
		final CachingKey key = new CachingKey(orgId, jsonProductInfo);
		return productInfoCache
				.getOrLoad(key, () -> getCreateProductInfo0(jsonProductInfo, orgId));
	}

	private ProductInfo getCreateProductInfo0(
			@NonNull final JsonProductInfo jsonProductInfo,
			@NonNull final OrgId orgId)
	{
		final BPartnerMasterDataContext context = BPartnerMasterDataContext.ofOrg(orgId);
		final ProductId existingProductId = lookupProductIdOrNull(jsonProductInfo, context);

		final IfExists ifExists = jsonProductInfo.getSyncAdvise().getIfExists();
		if (existingProductId != null && !ifExists.isUpdate())
		{
			return new ProductInfo(
					existingProductId,
					getProductUOMId(existingProductId, jsonProductInfo.getUomCode()));
		}

		final I_M_Product productRecord;
		if (existingProductId != null)
		{
			productRecord = load(existingProductId, I_M_Product.class);
		}
		else
		{
			// if the product doesn't exist and we got here, then ifNotExsits equals "create"
			productRecord = newInstance(I_M_Product.class);
			productRecord.setAD_Org_ID(context.getOrgId().getRepoId());
			productRecord.setValue(jsonProductInfo.getCode());
		}

		productRecord.setName(jsonProductInfo.getName());
		final String productType;
		switch (jsonProductInfo.getType())
		{
			case SERVICE:
				productType = X_M_Product.PRODUCTTYPE_Service;
				break;
			case ITEM:
				productType = X_M_Product.PRODUCTTYPE_Item;
				break;
			default:
				Check.fail("Unexpected type={}; jsonProductInfo={}", jsonProductInfo.getType(), jsonProductInfo);
				productType = null;
				break;
		}
		productRecord.setProductType(productType);

		productRecord.setM_Product_Category_ID(defaultProductCategoryId.getRepoId());

		final UomId uomId = uomsRepo.getUomIdByX12DE355(jsonProductInfo.getUomCode());
		productRecord.setC_UOM_ID(UomId.toRepoId(uomId));

		permissionService.assertCanCreateOrUpdate(productRecord);
		saveRecord(productRecord);

		return new ProductInfo(
				ProductId.ofRepoId(productRecord.getM_Product_ID()),
				uomId);
	}

	private ProductId lookupProductIdOrNull(
			@NonNull final JsonProductInfo json,
			@NonNull final BPartnerMasterDataContext context)
	{
		final SyncAdvise syncAdvise = json.getSyncAdvise();

		final ProductId existingProductId;
		if (Check.isEmpty(json.getCode(), true))
		{
			existingProductId = null;
		}
		else
		{
			final ProductQuery query = ProductQuery.builder()
					.value(json.getCode())
					.orgId(context.getOrgId())
					.outOfTrx(json.getSyncAdvise().isLoadReadOnly())
					.includeAnyOrg(true)
					.outOfTrx(syncAdvise.isLoadReadOnly())
					.build();
			existingProductId = productsRepo.retrieveProductIdBy(query);
		}
		if (existingProductId == null && syncAdvise.getIfNotExists().isFail())
		{
			final String msg = StringUtils.formatMessage("Found no existing product; Searched via value={} and orgId in ({}, 0)", json.getClass(), context.getOrgId());
			throw new ProductNotFoundException(msg);
		}
		return existingProductId;
	}

	private UomId getProductUOMId(
			@Nullable final ProductId productId,
			@Nullable final String uomCode)
	{
		if (!Check.isEmpty(uomCode, true))
		{
			return uomsRepo.getUomIdByX12DE355(uomCode);
		}
		else
		{
			return productsBL.getStockingUOMId(productId);
		}
	}
}
