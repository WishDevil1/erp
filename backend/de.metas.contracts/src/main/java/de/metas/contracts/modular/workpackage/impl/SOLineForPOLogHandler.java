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
import de.metas.contracts.IFlatrateBL;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.modular.IModularContractTypeHandler;
import de.metas.contracts.modular.ModularContract_Constants;
import de.metas.contracts.modular.impl.SOLineForPOModularContractHandler;
import de.metas.contracts.modular.invgroup.InvoicingGroupId;
import de.metas.contracts.modular.invgroup.interceptor.ModCntrInvoicingGroupRepository;
import de.metas.contracts.modular.log.LogEntryContractType;
import de.metas.contracts.modular.log.LogEntryCreateRequest;
import de.metas.contracts.modular.log.LogEntryDocumentType;
import de.metas.contracts.modular.log.LogEntryReverseRequest;
import de.metas.contracts.modular.log.ModularContractLogDAO;
import de.metas.contracts.modular.log.ModularContractLogQuery;
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
import de.metas.quantity.Quantitys;
import de.metas.uom.UomId;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.util.lang.impl.TableRecordReferenceSet;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SOLineForPOLogHandler implements IModularContractLogHandler<I_C_OrderLine>
{
	private static final AdMessageKey MSG_ON_COMPLETE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.impl.SalesOrderLineModularContractHandler.OnComplete.Description");
	private static final AdMessageKey MSG_ON_REVERSE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.impl.SalesOrderLineModularContractHandler.OnReverse.Description");

	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IOrderBL orderBL = Services.get(IOrderBL.class);
	private final IFlatrateBL flatrateBL = Services.get(IFlatrateBL.class);
	private final IOrderLineBL orderLineBL = Services.get(IOrderLineBL.class);
	private final IProductBL productBL = Services.get(IProductBL.class);
	private final IMsgBL msgBL = Services.get(IMsgBL.class);

	@NonNull
	private final ModularContractLogDAO contractLogDAO;
	@NonNull
	private final SOLineForPOModularContractHandler contractHandler;
	@NonNull
	private final ModCntrInvoicingGroupRepository modCntrInvoicingGroupRepository;

	@Override
	public LogAction getLogAction(@NonNull final HandleLogsRequest<I_C_OrderLine> request)
	{
		return switch (request.getModelAction())
				{
					case COMPLETED -> LogAction.CREATE;
					case REVERSED, REACTIVATED, VOIDED -> LogAction.REVERSE;
					case RECREATE_LOGS -> LogAction.RECOMPUTE;
					default -> throw new AdempiereException(ModularContract_Constants.MSG_ERROR_DOC_ACTION_UNSUPPORTED);
				};
	}

	@Override
	public BooleanWithReason doesRecordStateRequireLogCreation(@NonNull final I_C_OrderLine model)
	{
		final DocStatus orderDocStatus = orderBL.getDocStatus(OrderId.ofRepoId(model.getC_Order_ID()));

		if (!orderDocStatus.isCompletedOrClosed())
		{
			return BooleanWithReason.falseBecause("The C_Order.DocStatus is " + orderDocStatus);
		}

		return BooleanWithReason.TRUE;
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryCreateRequest> createLogEntryCreateRequest(@NonNull final CreateLogRequest<I_C_OrderLine> createLogRequest)
	{
		final I_C_OrderLine orderLine = createLogRequest.getHandleLogsRequest().getModel();

		final I_C_Flatrate_Term contract = flatrateBL.getById(createLogRequest.getContractId());
		final BPartnerId bPartnerId = BPartnerId.ofRepoId(contract.getBill_BPartner_ID());

		final UomId uomId = UomId.ofRepoId(orderLine.getC_UOM_ID());
		final Quantity quantity = Quantitys.create(orderLine.getQtyEntered(), uomId);

		final I_C_Order order = orderBL.getById(OrderId.ofRepoId(orderLine.getC_Order_ID()));

		final ProductId productId = ProductId.ofRepoId(orderLine.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = msgBL.getBaseLanguageMsg(MSG_ON_COMPLETE_DESCRIPTION, productName, quantity.toString());

		final Money amount = Money.of(orderLine.getLineNetAmt(), CurrencyId.ofRepoId(orderLine.getC_Currency_ID()));

		final LocalDateAndOrgId transactionDate = LocalDateAndOrgId.ofTimestamp(order.getDateOrdered(),
																				OrgId.ofRepoId(orderLine.getAD_Org_ID()),
																				orgDAO::getTimeZone);

		final InvoicingGroupId invoicingGroupId = modCntrInvoicingGroupRepository.getInvoicingGroupIdFor(productId, transactionDate.toInstant(orgDAO::getTimeZone))
				.orElse(null);
		
		return ExplainedOptional.of(LogEntryCreateRequest.builder()
											.referencedRecord(TableRecordReference.of(I_C_OrderLine.Table_Name, orderLine.getC_OrderLine_ID()))
											.contractId(createLogRequest.getContractId())
											.collectionPointBPartnerId(bPartnerId)
											.producerBPartnerId(bPartnerId)
											.invoicingBPartnerId(bPartnerId)
											.warehouseId(WarehouseId.ofRepoId(order.getM_Warehouse_ID()))
											.productId(productId)
											.documentType(LogEntryDocumentType.SALES_ORDER)
											.contractType(LogEntryContractType.MODULAR_CONTRACT)
											.soTrx(SOTrx.PURCHASE)
											.processed(false)
											.quantity(quantity)
											.transactionDate(transactionDate)
											.year(createLogRequest.getModularContractSettings().getYearAndCalendarId().yearId())
											.description(description)
											.modularContractTypeId(createLogRequest.getTypeId())
											.amount(amount)
											.configId(createLogRequest.getConfigId())
											.priceActual(orderLineBL.getPriceActual(orderLine))
											.invoicingGroupId(invoicingGroupId)
											.build());
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryReverseRequest> createLogEntryReverseRequest(@NonNull final HandleLogsRequest<I_C_OrderLine> handleLogsRequest)
	{
		final I_C_OrderLine orderLine = handleLogsRequest.getModel();

		final TableRecordReference orderLineRef = TableRecordReference.of(I_C_OrderLine.Table_Name, orderLine.getC_OrderLine_ID());

		final Quantity quantity = contractLogDAO.retrieveQuantityFromExistingLog(ModularContractLogQuery.builder()
																						 .flatrateTermId(handleLogsRequest.getContractId())
																						 .referenceSet(TableRecordReferenceSet.of(orderLineRef))
																						 .build());
		final ProductId productId = ProductId.ofRepoId(orderLine.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = msgBL.getBaseLanguageMsg(MSG_ON_REVERSE_DESCRIPTION, productName, quantity.toString());

		return ExplainedOptional.of(
				LogEntryReverseRequest.builder()
						.referencedModel(orderLineRef)
						.flatrateTermId(handleLogsRequest.getContractId())
						.description(description)
						.logEntryContractType(LogEntryContractType.MODULAR_CONTRACT)
						.build());
	}

	@Override
	public @NonNull IModularContractTypeHandler<I_C_OrderLine> getModularContractTypeHandler()
	{
		return contractHandler;
	}
}
