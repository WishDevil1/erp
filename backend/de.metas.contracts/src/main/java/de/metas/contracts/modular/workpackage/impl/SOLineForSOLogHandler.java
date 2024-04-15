/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2023 metas GmbH
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

package de.metas.contracts.modular.workpackage.impl;

import de.metas.bpartner.BPartnerId;
import de.metas.contracts.modular.IModularContractTypeHandler;
import de.metas.contracts.modular.ModularContract_Constants;
import de.metas.contracts.modular.impl.SalesOrderLineModularContractHandler;
import de.metas.contracts.modular.invgroup.InvoicingGroupId;
import de.metas.contracts.modular.invgroup.interceptor.ModCntrInvoicingGroupRepository;
import de.metas.contracts.modular.log.LogEntryContractType;
import de.metas.contracts.modular.log.LogEntryCreateRequest;
import de.metas.contracts.modular.log.LogEntryDocumentType;
import de.metas.contracts.modular.log.LogEntryReverseRequest;
import de.metas.contracts.modular.workpackage.IModularContractLogHandler;
import de.metas.document.engine.DocStatus;
import de.metas.i18n.AdMessageKey;
import de.metas.i18n.BooleanWithReason;
import de.metas.i18n.ExplainedOptional;
import de.metas.i18n.IMsgBL;
import de.metas.lang.SOTrx;
import de.metas.money.CurrencyId;
import de.metas.money.Money;
import de.metas.order.IOrderBL;
import de.metas.order.IOrderLineBL;
import de.metas.order.OrderId;
import de.metas.organization.IOrgDAO;
import de.metas.organization.LocalDateAndOrgId;
import de.metas.organization.OrgId;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMDAO;
import de.metas.uom.UomId;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_Warehouse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SOLineForSOLogHandler implements IModularContractLogHandler<I_C_OrderLine>
{
	private static final AdMessageKey MSG_ON_COMPLETE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.workpackage.impl.SOLineForSOLogHandler.OnComplete.Description");

	private final IOrderBL orderBL = Services.get(IOrderBL.class);
	private final IUOMDAO uomDAO = Services.get(IUOMDAO.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IWarehouseBL warehouseBL = Services.get(IWarehouseBL.class);
	private final IOrderLineBL orderLineBL = Services.get(IOrderLineBL.class);
	private final IProductBL productBL = Services.get(IProductBL.class);
	private final IMsgBL msgBL = Services.get(IMsgBL.class);

	@NonNull
	private final SalesOrderLineModularContractHandler contractHandler;
	@NonNull
	private final ModCntrInvoicingGroupRepository modCntrInvoicingGroupRepository;

	@Override
	public LogAction getLogAction(@NonNull final HandleLogsRequest<I_C_OrderLine> request)
	{
		return switch (request.getModelAction())
				{
					case COMPLETED -> LogAction.CREATE;
					case RECREATE_LOGS -> LogAction.RECOMPUTE;
					default -> throw new AdempiereException(ModularContract_Constants.MSG_ERROR_DOC_ACTION_UNSUPPORTED);
				};
	}

	@Override
	public BooleanWithReason doesRecordStateRequireLogCreation(@NonNull final I_C_OrderLine model)
	{
		final DocStatus orderDocStatus = orderBL.getDocStatus(OrderId.ofRepoId(model.getC_Order_ID()));

		if (!orderDocStatus.isCompleted())
		{
			return BooleanWithReason.falseBecause("The C_Order.DocStatus is " + orderDocStatus);
		}

		return BooleanWithReason.TRUE;
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryCreateRequest> createLogEntryCreateRequest(@NonNull final CreateLogRequest<I_C_OrderLine> createLogRequest)
	{
		final I_C_OrderLine orderLine = createLogRequest.getHandleLogsRequest().getModel();

		final I_C_Order order = orderBL.getById(OrderId.ofRepoId(orderLine.getC_Order_ID()));
		final WarehouseId warehouseId = WarehouseId.ofRepoId(order.getM_Warehouse_ID());
		final I_M_Warehouse warehouseRecord = warehouseBL.getById(warehouseId);

		final I_C_UOM uom = uomDAO.getById(UomId.ofRepoId(orderLine.getC_UOM_ID()));
		final Quantity quantity = Quantity.of(orderLine.getQtyEntered(), uom);
		final Money amount = Money.of(orderLine.getLineNetAmt(), CurrencyId.ofRepoId(orderLine.getC_Currency_ID()));

		final ProductId productId = ProductId.ofRepoId(orderLine.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = msgBL.getBaseLanguageMsg(MSG_ON_COMPLETE_DESCRIPTION, productName, quantity);

		final LocalDateAndOrgId transactionDate = LocalDateAndOrgId.ofTimestamp(order.getDateOrdered(),
																				OrgId.ofRepoId(orderLine.getAD_Org_ID()),
																				orgDAO::getTimeZone);

		final InvoicingGroupId invoicingGroupId = modCntrInvoicingGroupRepository.getInvoicingGroupIdFor(productId, transactionDate.toInstant(orgDAO::getTimeZone))
				.orElse(null);

		return ExplainedOptional.of(LogEntryCreateRequest.builder()
											.contractId(createLogRequest.getContractId())
											.productId(productId)
											.productName(createLogRequest.getProductName())
											.referencedRecord(TableRecordReference.of(I_C_OrderLine.Table_Name, orderLine.getC_OrderLine_ID()))
											.producerBPartnerId(BPartnerId.ofRepoId(order.getC_BPartner_ID()))
											.invoicingBPartnerId(BPartnerId.ofRepoId(order.getBill_BPartner_ID()))
											.collectionPointBPartnerId(BPartnerId.ofRepoId(warehouseRecord.getC_BPartner_ID()))
											.warehouseId(warehouseId)
											.documentType(LogEntryDocumentType.SALES_ORDER)
											.contractType(LogEntryContractType.MODULAR_CONTRACT)
											.soTrx(SOTrx.SALES)
											.processed(false)
											.quantity(quantity)
											.amount(amount)
											.transactionDate(transactionDate)
											.year(createLogRequest.getModularContractSettings().getYearAndCalendarId().yearId())
											.description(description)
											.modularContractTypeId(createLogRequest.getTypeId())
											.configId(createLogRequest.getConfigId())
											.priceActual(orderLineBL.getPriceActual(orderLine))
											.invoicingGroupId(invoicingGroupId)
											.build());
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryReverseRequest> createLogEntryReverseRequest(@NonNull final HandleLogsRequest<I_C_OrderLine> handleLogsRequest)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public @NonNull IModularContractTypeHandler<I_C_OrderLine> getModularContractTypeHandler()
	{
		return contractHandler;
	}
}
