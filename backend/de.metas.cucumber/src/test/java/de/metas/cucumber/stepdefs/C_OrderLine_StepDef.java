/*
 * #%L
 * de.metas.cucumber
 * %%
 * Copyright (C) 2021 metas GmbH
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

package de.metas.cucumber.stepdefs;

import de.metas.cucumber.stepdefs.attribute.M_AttributeSetInstance_StepDefData;
import de.metas.cucumber.stepdefs.attribute.M_Attribute_StepDefData;
import de.metas.cucumber.stepdefs.hu.M_HU_PI_Item_Product_StepDefData;
import de.metas.currency.Currency;
import de.metas.currency.CurrencyCode;
import de.metas.currency.ICurrencyDAO;
import de.metas.handlingunits.model.I_M_HU_PI_Item_Product;
import de.metas.material.event.commons.AttributesKey;
import de.metas.ordercandidate.model.I_C_OLCand;
import de.metas.util.Check;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.mm.attributes.api.AttributesKeys;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_Attribute;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.I_M_Product;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.save;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.*;
import static org.eevolution.model.I_PP_Product_Planning.COLUMNNAME_M_AttributeSetInstance_ID;

public class C_OrderLine_StepDef
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final ICurrencyDAO currencyDAO = Services.get(ICurrencyDAO.class);

	private final M_Product_StepDefData productTable;
	private final C_Order_StepDefData orderTable;
	private final C_OrderLine_StepDefData orderLineTable;
	private final M_AttributeSetInstance_StepDefData attributeSetInstanceTable;
	private final M_HU_PI_Item_Product_StepDefData huPiItemProductTable;
	private final M_Attribute_StepDefData attributeTable;

	public C_OrderLine_StepDef(
			@NonNull final M_Product_StepDefData productTable,
			@NonNull final C_Order_StepDefData orderTable,
			@NonNull final C_OrderLine_StepDefData orderLineTable,
			@NonNull final M_AttributeSetInstance_StepDefData attributeSetInstanceTable,
			@NonNull final M_HU_PI_Item_Product_StepDefData huPiItemProductTable,
			@NonNull final M_Attribute_StepDefData attributeTable)
	{
		this.productTable = productTable;
		this.orderTable = orderTable;
		this.orderLineTable = orderLineTable;
		this.attributeSetInstanceTable = attributeSetInstanceTable;
		this.huPiItemProductTable = huPiItemProductTable;
		this.attributeTable = attributeTable;
	}

	@Given("metasfresh contains C_OrderLines:")
	public void metasfresh_contains_c_invoice_candidates(@NonNull final DataTable dataTable)
	{
		final List<Map<String, String>> tableRows = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> tableRow : tableRows)
		{
			final de.metas.handlingunits.model.I_C_OrderLine orderLine = newInstance(de.metas.handlingunits.model.I_C_OrderLine.class);
			orderLine.setAD_Org_ID(StepDefConstants.ORG_ID.getRepoId());

			final String productIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_M_Product_ID + ".Identifier");
			final I_M_Product product = productTable.get(productIdentifier);
			orderLine.setM_Product_ID(product.getM_Product_ID());
			orderLine.setQtyEntered(DataTableUtil.extractBigDecimalForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_QtyEntered));

			final String attributeSetInstanceIdentifier = DataTableUtil.extractStringOrNullForColumnName(tableRow, "OPT." + COLUMNNAME_M_AttributeSetInstance_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(attributeSetInstanceIdentifier))
			{
				final I_M_AttributeSetInstance attributeSetInstance = attributeSetInstanceTable.get(attributeSetInstanceIdentifier);
				assertThat(attributeSetInstance).isNotNull();

				orderLine.setM_AttributeSetInstance_ID(attributeSetInstance.getM_AttributeSetInstance_ID());
			}

			final String itemProductIdentifier = DataTableUtil.extractNullableStringForColumnName(tableRow, "OPT." + de.metas.handlingunits.model.I_C_OrderLine.COLUMNNAME_M_HU_PI_Item_Product_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (de.metas.util.Check.isNotBlank(itemProductIdentifier))
			{
				final String itemProductIdentifierValue = DataTableUtil.nullToken2Null(itemProductIdentifier);
				if (itemProductIdentifierValue == null)
				{
					orderLine.setM_HU_PI_Item_Product_ID(-1);
				}
				else
				{
					final Integer huPiItemProductRecordID = huPiItemProductTable.getOptional(itemProductIdentifier)
							.map(I_M_HU_PI_Item_Product::getM_HU_PI_Item_Product_ID)
							.orElseGet(() -> Integer.parseInt(itemProductIdentifier));

					orderLine.setM_HU_PI_Item_Product_ID(huPiItemProductRecordID);
				}
			}

			final String orderIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_C_Order_ID + ".Identifier");
			final I_C_Order order = orderTable.get(orderIdentifier);
			orderLine.setC_Order_ID(order.getC_Order_ID());

			saveRecord(orderLine);

			orderLineTable.putOrReplace(DataTableUtil.extractRecordIdentifier(tableRow, I_C_OrderLine.COLUMNNAME_C_OrderLine_ID), orderLine);
		}
	}

	@Then("the purchase order with document subtype {string} linked to order {string} has lines:")
	public void thePurchaseOrderLinkedToOrderO_HasLines(@Nullable final String docSubType, @NonNull final String linkedOrderIdentifier, @NonNull final DataTable dataTable)
	{
		final I_C_Order purchaseOrder = queryBL
				.createQueryBuilder(I_C_Order.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_Order.COLUMNNAME_Link_Order_ID, orderTable.get(linkedOrderIdentifier).getC_Order_ID())
				.create().firstOnly(I_C_Order.class);

		assertThat(purchaseOrder).isNotNull();

		final I_C_DocType docType = queryBL
				.createQueryBuilder(I_C_DocType.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_DocType.COLUMN_C_DocType_ID, purchaseOrder.getC_DocTypeTarget_ID())
				.create().firstOnly(I_C_DocType.class);

		assertThat(docType).isNotNull();
		if (Check.isNotBlank(docSubType))
		{
			assertThat(docType.getDocSubType()).isEqualTo(docSubType);
		}

		final List<I_C_OrderLine> purchaseOrderLines = queryBL
				.createQueryBuilder(I_C_OrderLine.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_OrderLine.COLUMNNAME_C_Order_ID, purchaseOrder.getC_Order_ID())
				.create()
				.list(I_C_OrderLine.class);

		final List<Map<String, String>> tableRows = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> tableRow : tableRows)
		{
			final BigDecimal qtyOrdered = DataTableUtil.extractBigDecimalForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_QtyOrdered);
			final BigDecimal netAmt = DataTableUtil.extractBigDecimalForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_LineNetAmt);
			final String productIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_C_OrderLine.COLUMNNAME_M_Product_ID + ".Identifier");

			boolean linePresent = false;

			for (final I_C_OrderLine orderLine : purchaseOrderLines)
			{
				linePresent = orderLine.getLineNetAmt().compareTo(netAmt) == 0
						&& orderLine.getQtyOrdered().compareTo(qtyOrdered) == 0
						&& orderLine.getM_Product_ID() == productTable.get(productIdentifier).getM_Product_ID();

				if (linePresent)
				{
					break;
				}
			}

			assertThat(linePresent).isTrue();
		}
	}

	@And("validate the created order lines")
	public void validate_created_order_lines(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			final String orderIdentifier = DataTableUtil.extractStringForColumnName(row, "C_Order_ID.Identifier");

			final I_C_Order orderRecord = orderTable.get(orderIdentifier);

			final String productIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_OLCand.COLUMNNAME_M_Product_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);
			final Integer expectedProductId = productTable.getOptional(productIdentifier)
					.map(I_M_Product::getM_Product_ID)
					.orElseGet(() -> Integer.parseInt(productIdentifier));

			//dev-note: we assume the tests are not using the same product on different lines
			final I_C_OrderLine orderLineRecord = queryBL.createQueryBuilder(I_C_OrderLine.class)
					.addEqualsFilter(I_C_OrderLine.COLUMNNAME_C_Order_ID, orderRecord.getC_Order_ID())
					.addEqualsFilter(I_C_OrderLine.COLUMNNAME_M_Product_ID, expectedProductId)
					.create()
					.firstOnlyNotNull(I_C_OrderLine.class);

			validateOrderLine(orderLineRecord, row);
		}
	}

	@And("validate C_OrderLine:")
	public void validate_C_OrderLine(@NonNull final DataTable dataTable)
	{
		final List<Map<String, String>> table = dataTable.asMaps();
		for (final Map<String, String> row : table)
		{
			final String orderLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_OrderLine.COLUMNNAME_C_OrderLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
			assertThat(orderLine).isNotNull();

			InterfaceWrapperHelper.refresh(orderLine);

			validateOrderLine(orderLine, row);
		}
	}

	@And("update C_OrderLine:")
	public void updateC_OrderLine(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String orderLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_OrderLine.COLUMNNAME_C_OrderLine_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);
			final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
			assertThat(orderLine).isNotNull();

			final String attributeSetInstanceIdentifier = DataTableUtil.extractNullableStringForColumnName(row, "OPT." + COLUMNNAME_M_AttributeSetInstance_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(attributeSetInstanceIdentifier))
			{
				final String asiIdentifierValue = DataTableUtil.nullToken2Null(attributeSetInstanceIdentifier);
				if (asiIdentifierValue == null)
				{
					orderLine.setM_AttributeSetInstance_ID(-1);
				}
				else
				{
					final I_M_AttributeSetInstance attributeSetInstance = attributeSetInstanceTable.get(attributeSetInstanceIdentifier);
					assertThat(attributeSetInstance).isNotNull();

					orderLine.setM_AttributeSetInstance_ID(attributeSetInstance.getM_AttributeSetInstance_ID());
				}
			}

			save(orderLine);
			orderLineTable.putOrReplace(orderLineIdentifier, orderLine);
		}
	}

	private void validateOrderLine(@NonNull final I_C_OrderLine orderLine, @NonNull final Map<String, String> row)
	{
		final String orderIdentifier = DataTableUtil.extractStringForColumnName(row, "C_Order_ID.Identifier");
		final Timestamp dateOrdered = DataTableUtil.extractDateTimestampForColumnNameOrNull(row, "OPT." + I_C_OrderLine.COLUMNNAME_DateOrdered);
		final String productIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_OrderLine.COLUMNNAME_M_Product_ID + ".Identifier");
		final BigDecimal qtyDelivered = DataTableUtil.extractBigDecimalForColumnName(row, "qtydelivered");
		final BigDecimal qtyordered = DataTableUtil.extractBigDecimalForColumnName(row, I_C_OrderLine.COLUMNNAME_QtyOrdered);
		final BigDecimal qtyinvoiced = DataTableUtil.extractBigDecimalForColumnName(row, "qtyinvoiced");
		final BigDecimal price = DataTableUtil.extractBigDecimalWithScaleForColumnName(row, "price");
		final BigDecimal discount = DataTableUtil.extractBigDecimalForColumnName(row, "discount");
		final String currencyCode = DataTableUtil.extractStringForColumnName(row, "currencyCode");
		final boolean processed = DataTableUtil.extractBooleanForColumnName(row, "processed");

		final Integer expectedProductId = productTable.getOptional(productIdentifier)
				.map(I_M_Product::getM_Product_ID)
				.orElseGet(() -> Integer.parseInt(productIdentifier));

		assertThat(orderLine.getC_Order_ID()).isEqualTo(orderTable.get(orderIdentifier).getC_Order_ID());

		if (dateOrdered != null)
		{
			assertThat(orderLine.getDateOrdered()).isEqualTo(dateOrdered);
		}
		assertThat(orderLine.getQtyDelivered()).isEqualTo(qtyDelivered);
		assertThat(orderLine.getPriceEntered()).isEqualTo(price);
		assertThat(orderLine.getDiscount()).isEqualTo(discount);
		assertThat(orderLine.isProcessed()).isEqualTo(processed);
		assertThat(orderLine.getM_Product_ID()).isEqualTo(expectedProductId);
		assertThat(orderLine.getQtyOrdered()).isEqualTo(qtyordered);
		assertThat(orderLine.getQtyInvoiced()).isEqualTo(qtyinvoiced);

		final Currency currency = currencyDAO.getByCurrencyCode(CurrencyCode.ofThreeLetterCode(currencyCode));
		assertThat(orderLine.getC_Currency_ID()).isEqualTo(currency.getId().getRepoId());

		final String attributeSetInstanceIdentifier = DataTableUtil.extractNullableStringForColumnName(row, "OPT." + COLUMNNAME_M_AttributeSetInstance_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);
		if (Check.isNotBlank(attributeSetInstanceIdentifier))
		{
			final I_M_AttributeSetInstance expectedASI = attributeSetInstanceTable.get(attributeSetInstanceIdentifier);
			assertThat(expectedASI).isNotNull();

			final AttributesKey expectedASIKey = AttributesKeys
					.createAttributesKeyFromASIStorageAttributes(AttributeSetInstanceId.ofRepoId(expectedASI.getM_AttributeSetInstance_ID()))
					.orElse(AttributesKey.NONE);

			final AttributesKey orderLineAttributesKeys = AttributesKeys
					.createAttributesKeyFromASIStorageAttributes(AttributeSetInstanceId.ofRepoId(orderLine.getM_AttributeSetInstance_ID()))
					.orElse(AttributesKey.NONE);

			assertThat(orderLineAttributesKeys).isEqualTo(expectedASIKey);
		}

		final String huPiItemProductIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + de.metas.handlingunits.model.I_C_OrderLine.COLUMNNAME_M_HU_PI_Item_Product_ID + "." + TABLECOLUMN_IDENTIFIER);
		if (Check.isNotBlank(huPiItemProductIdentifier))
		{
			final I_M_HU_PI_Item_Product huPiItemProduct = huPiItemProductTable.get(huPiItemProductIdentifier);
			final de.metas.handlingunits.model.I_C_OrderLine orderLineHU = InterfaceWrapperHelper.load(orderLine.getC_OrderLine_ID(), de.metas.handlingunits.model.I_C_OrderLine.class);
			assertThat(huPiItemProduct.getM_HU_PI_Item_Product_ID()).isEqualTo(orderLineHU.getM_HU_PI_Item_Product_ID());
		}

		final String asiValues = DataTableUtil.extractNullableStringForColumnName(row, "OPT." + I_M_AttributeInstance.COLUMNNAME_M_Attribute_ID + ":" + I_M_AttributeInstance.Table_Name + "." + I_M_AttributeInstance.COLUMNNAME_Value);
		if (Check.isNotBlank(asiValues))
		{
			StepDefUtil.splitIdentifiers(asiValues)
					.forEach(value -> validateAttributeValue(orderLine, value));
		}

		final String orderLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_OrderLine.COLUMNNAME_C_OrderLine_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);

		orderLineTable.putOrReplace(orderLineIdentifier, orderLine);
	}

	private void validateAttributeValue(@NonNull final I_C_OrderLine orderLine, @NonNull final String value)
	{
		final List<String> expectedAttrValuePair = StepDefUtil.splitByColon(value);
		if (expectedAttrValuePair.size() != 2)
		{
			throw new RuntimeException("AttributeValue argument in wrong format! value=" + value);
		}

		final String attributeIdentifier = expectedAttrValuePair.get(0);
		final String expectedAttrValue = DataTableUtil.nullToken2Null(expectedAttrValuePair.get(1));

		final I_M_Attribute attribute = attributeTable.get(attributeIdentifier);
		assertThat(attribute).isNotNull();

		final int attributeSetInstanceId = orderLine.getM_AttributeSetInstance_ID();
		if (attributeSetInstanceId <= 0)
		{
			throw new AdempiereException("No ASI set on C_OrderLine")
					.appendParametersToMessage()
					.setParameter("C_OrderLine_ID", orderLine.getC_OrderLine_ID());
		}

		final I_M_AttributeInstance attributeInstance = queryBL.createQueryBuilder(I_M_AttributeInstance.class)
				.addEqualsFilter(I_M_AttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID, attributeSetInstanceId)
				.addEqualsFilter(I_M_AttributeInstance.COLUMNNAME_M_Attribute_ID, attribute.getM_Attribute_ID())
				.create()
				.firstOnlyOptional(I_M_AttributeInstance.class)
				.orElseThrow(() -> new AdempiereException("No M_AttributeInstance found for M_Attribute_ID and ASI")
						.appendParametersToMessage()
						.setParameter("M_Attribute_ID", attribute.getM_Attribute_ID())
						.setParameter("M_AttributeSetInstance_ID", attributeSetInstanceId));

		if (expectedAttrValue == null)
		{
			assertThat(attributeInstance.getValue()).isNull();
		}
		else
		{
			assertThat(attributeInstance.getValue()).isEqualTo(expectedAttrValue);
		}
	}
}
