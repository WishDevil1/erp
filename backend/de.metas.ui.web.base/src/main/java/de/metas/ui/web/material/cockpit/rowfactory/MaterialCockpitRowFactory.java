package de.metas.ui.web.material.cockpit.rowfactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import de.metas.ad_reference.ADRefListItem;
import de.metas.ad_reference.ADReferenceService;
import de.metas.ad_reference.ReferenceId;
import de.metas.currency.CurrencyRepository;
import de.metas.dimension.DimensionSpec;
import de.metas.dimension.DimensionSpecGroup;
import de.metas.material.cockpit.model.I_MD_Cockpit;
import de.metas.material.cockpit.model.I_MD_Stock;
import de.metas.material.cockpit.model.I_QtyDemand_QtySupply_V;
import de.metas.material.planning.IResourceDAO;
import de.metas.money.Money;
import de.metas.money.MoneyService;
import de.metas.order.stats.purchase_max_price.PurchaseLastMaxPriceProvider;
import de.metas.order.stats.purchase_max_price.PurchaseLastMaxPriceRequest;
import de.metas.order.stats.purchase_max_price.PurchaseLastMaxPriceService;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.product.ResourceId;
import de.metas.ui.web.material.cockpit.MaterialCockpitRow;
import de.metas.ui.web.material.cockpit.MaterialCockpitUtil;
import de.metas.util.IColorRepository;
import de.metas.util.MFColor;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import org.adempiere.warehouse.api.IWarehouseDAO;
import org.compiere.Adempiere;
import org.compiere.model.I_M_Product;
import org.compiere.model.X_M_Product;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
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

@Service
@RequiredArgsConstructor
public class MaterialCockpitRowFactory
{
	@NonNull private final IWarehouseDAO warehouseDAO = Services.get(IWarehouseDAO.class);
	@NonNull private final IProductBL productBL = Services.get(IProductBL.class);
	@NonNull private final IResourceDAO resourceDAO = Services.get(IResourceDAO.class);
	@NonNull private final IColorRepository colorRepository = Services.get(IColorRepository.class);
	@NonNull private final ADReferenceService adReferenceService;
	@NonNull private final PurchaseLastMaxPriceService purchaseLastMaxPriceService;

	@VisibleForTesting
	public static MaterialCockpitRowFactory newInstanceForUnitTesting()
	{
		Adempiere.assertUnitTestMode();
		return new MaterialCockpitRowFactory(
				ADReferenceService.get(),
				new PurchaseLastMaxPriceService(new MoneyService(new CurrencyRepository()))
		);
	}

	@Value
	@lombok.Builder
	public static class CreateRowsRequest
	{
		@NonNull LocalDate date;
		@NonNull @Singular("productIdToListEvenIfEmpty") ImmutableSet<ProductId> productIdsToListEvenIfEmpty;
		@NonNull @Singular List<I_MD_Cockpit> cockpitRecords;
		@NonNull @Singular List<I_MD_Stock> stockRecords;
		@NonNull @Singular List<I_QtyDemand_QtySupply_V> quantitiesRecords;
		boolean includePerPlantDetailRows;
	}

	public List<MaterialCockpitRow> createRows(@NonNull final CreateRowsRequest request)
	{
		return newCreateRowsCommand(request).execute();
	}

	@VisibleForTesting
	CreateRowsCommand newCreateRowsCommand(final @NonNull CreateRowsRequest request)
	{
		return CreateRowsCommand.builder()
				.warehouseDAO(warehouseDAO)
				.productBL(productBL)
				.resourceDAO(resourceDAO)
				.colorRepository(colorRepository)
				.adReferenceService(adReferenceService)
				.purchaseLastMaxPriceProvider(purchaseLastMaxPriceService.newProvider())
				//
				.request(request)
				//
				.build();
	}

	@VisibleForTesting
	@lombok.Builder
	static class CreateRowsCommand
	{
		@NonNull private final IWarehouseDAO warehouseDAO;
		@NonNull private final IProductBL productBL;
		@NonNull private final IResourceDAO resourceDAO;
		@NonNull private final IColorRepository colorRepository;
		@NonNull private final ADReferenceService adReferenceService;
		@NonNull private final PurchaseLastMaxPriceProvider purchaseLastMaxPriceProvider;

		@NonNull private final CreateRowsRequest request;

		private static final ReferenceId PROCUREMENTSTATUS_Reference_ID = ReferenceId.ofRepoId(X_M_Product.PROCUREMENTSTATUS_AD_Reference_ID);

		public List<MaterialCockpitRow> execute()
		{
			purchaseLastMaxPriceProvider.warmUp(request.getProductIdsToListEvenIfEmpty(), request.getDate());

			final Map<MainRowBucketId, MainRowWithSubRows> emptyRowBuckets = createEmptyRowBuckets(
					request.getProductIdsToListEvenIfEmpty(),
					request.getDate(),
					request.isIncludePerPlantDetailRows());

			final DimensionSpec dimensionSpec = MaterialCockpitUtil.retrieveDimensionSpec();

			final Map<MainRowBucketId, MainRowWithSubRows> result = new HashMap<>(emptyRowBuckets);

			addCockpitRowsToResult(dimensionSpec, result);
			addStockRowsToResult(dimensionSpec, result);
			addQuantitiesRowsToResult(dimensionSpec, result);

			return result.values()
					.stream()
					.map(MainRowWithSubRows::createMainRowWithSubRows)
					.collect(ImmutableList.toImmutableList());
		}

		@VisibleForTesting
		Map<MainRowBucketId, MainRowWithSubRows> createEmptyRowBuckets(
				@NonNull final ImmutableSet<ProductId> productIds,
				@NonNull final LocalDate timestamp,
				final boolean includePerPlantDetailRows)
		{
			final DimensionSpec dimensionSpec = MaterialCockpitUtil.retrieveDimensionSpec();

			final List<DimensionSpecGroup> groups = dimensionSpec.retrieveGroups();
			final Set<ResourceId> plantIds = retrieveCountingPlants(includePerPlantDetailRows);

			final Builder<MainRowBucketId, MainRowWithSubRows> result = ImmutableMap.builder();
			for (final ProductId productId : productIds)
			{
				final MainRowBucketId key = MainRowBucketId.createPlainInstance(productId, timestamp);
				final MainRowWithSubRows mainRowBucket = newMainRowWithSubRows(key);

				for (final ResourceId plantId : plantIds)
				{
					mainRowBucket.addEmptyCountingSubrowBucket(plantId);
				}

				for (final DimensionSpecGroup group : groups)
				{
					mainRowBucket.addEmptyAttributesSubrowBucket(group);
				}
				result.put(key, mainRowBucket);

			}
			return result.build();
		}

		private ImmutableSet<ResourceId> retrieveCountingPlants(final boolean includePerPlantDetailRows)
		{
			return includePerPlantDetailRows ? resourceDAO.getActivePlantIds() : ImmutableSet.of();
		}

		private void addCockpitRowsToResult(
				@NonNull final DimensionSpec dimensionSpec,
				@NonNull final Map<MainRowBucketId, MainRowWithSubRows> result)
		{
			for (final I_MD_Cockpit cockpitRecord : request.getCockpitRecords())
			{
				final MainRowBucketId mainRowBucketId = MainRowBucketId.createInstanceForCockpitRecord(cockpitRecord);

				final MainRowWithSubRows mainRowBucket = result.computeIfAbsent(mainRowBucketId, this::newMainRowWithSubRows);
				mainRowBucket.addCockpitRecord(cockpitRecord, dimensionSpec, request.isIncludePerPlantDetailRows());
			}
		}

		private void addStockRowsToResult(
				@NonNull final DimensionSpec dimensionSpec,
				@NonNull final Map<MainRowBucketId, MainRowWithSubRows> result)
		{
			for (final I_MD_Stock stockRecord : request.getStockRecords())
			{
				final MainRowBucketId mainRowBucketId = MainRowBucketId.createInstanceForStockRecord(stockRecord, request.getDate());

				final MainRowWithSubRows mainRowBucket = result.computeIfAbsent(mainRowBucketId, this::newMainRowWithSubRows);
				mainRowBucket.addStockRecord(stockRecord, dimensionSpec, request.isIncludePerPlantDetailRows());
			}
		}

		private void addQuantitiesRowsToResult(
				@NonNull final DimensionSpec dimensionSpec,
				@NonNull final Map<MainRowBucketId, MainRowWithSubRows> result)
		{
			for (final I_QtyDemand_QtySupply_V qtyRecord : request.getQuantitiesRecords())
			{
				final MainRowBucketId mainRowBucketId = MainRowBucketId.createInstanceForQuantitiesRecord(qtyRecord, request.getDate());

				final MainRowWithSubRows mainRowBucket = result.computeIfAbsent(mainRowBucketId, this::newMainRowWithSubRows);
				mainRowBucket.addQuantitiesRecord(qtyRecord, dimensionSpec, request.isIncludePerPlantDetailRows());
			}
		}

		private MainRowWithSubRows newMainRowWithSubRows(@NonNull final MainRowBucketId mainRowBucketId)
		{
			final Money maxPurchasePrice = purchaseLastMaxPriceProvider.getPrice(
							PurchaseLastMaxPriceRequest.builder()
									.productId(mainRowBucketId.getProductId())
									.evalDate(mainRowBucketId.getDate())
									.build())
					.getMaxPurchasePrice();

			final MFColor procurementStatusColor = getProcurementStatusColor(mainRowBucketId.getProductId()).orElse(null);

			return MainRowWithSubRows.builder()
					.warehouseDAO(warehouseDAO)
					.productIdAndDate(mainRowBucketId)
					.procurementStatusColor(procurementStatusColor)
					.maxPurchasePrice(maxPurchasePrice)
					.build();
		}

		private Optional<MFColor> getProcurementStatusColor(@NonNull final ProductId productId)
		{
			final I_M_Product product = productBL.getById(productId);

			return adReferenceService.getRefListById(PROCUREMENTSTATUS_Reference_ID)
					.getItemByValue(product.getProcurementStatus())
					.map(ADRefListItem::getColorId)
					.map(colorRepository::getColorById);
		}
	}
}
