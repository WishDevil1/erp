/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2024 metas GmbH
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

package de.metas.contracts.modular.computing.purchasecontract.interim;

import de.metas.bpartner.BPartnerId;
import de.metas.contracts.FlatrateTermId;
import de.metas.contracts.IFlatrateBL;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.modular.invgroup.InvoicingGroupId;
import de.metas.contracts.modular.invgroup.interceptor.ModCntrInvoicingGroupRepository;
import de.metas.contracts.modular.log.LogEntryContractType;
import de.metas.contracts.modular.log.LogEntryCreateRequest;
import de.metas.contracts.modular.log.LogEntryDocumentType;
import de.metas.contracts.modular.log.LogEntryReverseRequest;
import de.metas.contracts.modular.log.ModularContractLogDAO;
import de.metas.contracts.modular.log.ModularContractLogEntry;
import de.metas.contracts.modular.log.ModularContractLogQuery;
import de.metas.contracts.modular.log.ModularContractLogService;
import de.metas.contracts.modular.workpackage.IModularContractLogHandler;
import de.metas.i18n.AdMessageKey;
import de.metas.i18n.ExplainedOptional;
import de.metas.i18n.IMsgBL;
import de.metas.invoice.InvoiceId;
import de.metas.invoice.InvoiceLineId;
import de.metas.invoice.service.IInvoiceBL;
import de.metas.invoicecandidate.InvoiceCandidateId;
import de.metas.invoicecandidate.api.IInvoiceCandDAO;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.lang.SOTrx;
import de.metas.money.CurrencyId;
import de.metas.money.Money;
import de.metas.order.OrderLineId;
import de.metas.organization.IOrgDAO;
import de.metas.organization.LocalDateAndOrgId;
import de.metas.organization.OrgId;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.product.ProductPrice;
import de.metas.quantity.Quantity;
import de.metas.quantity.Quantitys;
import de.metas.uom.UomId;
import de.metas.util.Services;
import lombok.Getter;
import lombok.NonNull;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.util.lang.impl.TableRecordReferenceSet;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_InvoiceLine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InterimInvoiceLineLog implements IModularContractLogHandler
{
	private final IInvoiceBL invoiceBL = Services.get(IInvoiceBL.class);
	private final IProductBL productBL = Services.get(IProductBL.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IFlatrateBL flatrateBL = Services.get(IFlatrateBL.class);
	private final IInvoiceCandDAO invoiceCandDAO = Services.get(IInvoiceCandDAO.class);
	private final IMsgBL msgBL = Services.get(IMsgBL.class);
	private final ModularContractLogDAO contractLogDAO;
	private final ModularContractLogService modularContractLogService;
	private final ModCntrInvoicingGroupRepository modCntrInvoicingGroupRepository;

	private static final AdMessageKey MSG_ON_COMPLETE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.interimInvoiceCompleteLogDescription");
	private static final AdMessageKey MSG_ON_REVERSE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.interimInvoiceReverseLogDescription");

	@Getter @NonNull private final InterimComputingMethod computingMethod;
	@Getter @NonNull private final String supportedTableName = I_C_InvoiceLine.Table_Name;
	@Getter @NonNull private final LogEntryDocumentType logEntryDocumentType = LogEntryDocumentType.INTERIM_INVOICE;
	@Getter @NonNull private final LogEntryContractType logEntryContractType = LogEntryContractType.INTERIM;

	public InterimInvoiceLineLog(
			@NonNull final InterimComputingMethod computingMethod,
			@NonNull final ModularContractLogDAO contractLogDAO,
			@NonNull final ModularContractLogService modularContractLogService,
			@NonNull final ModCntrInvoicingGroupRepository modCntrInvoicingGroupRepository)
	{
		this.computingMethod = computingMethod;
		this.contractLogDAO = contractLogDAO;
		this.modularContractLogService = modularContractLogService;
		this.modCntrInvoicingGroupRepository = modCntrInvoicingGroupRepository;
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryCreateRequest> createLogEntryCreateRequest(@NonNull final CreateLogRequest createLogRequest)
	{
		final TableRecordReference tableRecordReference = createLogRequest.getRecordRef();
		final I_C_InvoiceLine invoiceLineRecord = invoiceBL.getLineById(InvoiceLineId.ofRepoId(tableRecordReference.getRecordIdAssumingTableName(I_C_InvoiceLine.Table_Name)));
		final I_C_Invoice invoiceRecord = invoiceBL.getById(InvoiceId.ofRepoId(invoiceLineRecord.getC_Invoice_ID()));

		final BPartnerId invoiceBpartnerId = BPartnerId.ofRepoId(invoiceRecord.getC_BPartner_ID());

		final UomId uomId = UomId.ofRepoId(invoiceLineRecord.getC_UOM_ID());
		final Quantity qtyEntered = Quantitys.of(invoiceLineRecord.getQtyEntered(), uomId);

		final CurrencyId currencyId = CurrencyId.ofRepoId(invoiceRecord.getC_Currency_ID());
		final Money amount = Money.of(invoiceLineRecord.getLineNetAmt(), currencyId);
		final Money priceActual = Money.of(invoiceLineRecord.getPriceActual(), currencyId);
		final ProductId productId = createLogRequest.getProductId();
		final ProductPrice productPrice = ProductPrice.builder()
				.productId(productId)
				.money(priceActual)
				.uomId(uomId)
				.build();

		final FlatrateTermId modularContractId = createLogRequest.getContractId();
		final I_C_Flatrate_Term modularContractRecord = flatrateBL.getById(modularContractId);
		final Optional<ModularContractLogEntry> modularContractLogEntryOptional = modularContractLogService.getLastModularContractLog(
				modularContractId,
				OrderLineId.ofRepoId(modularContractRecord.getC_OrderLine_Term_ID()));

		if (modularContractLogEntryOptional.isEmpty())
		{
			return ExplainedOptional.emptyBecause("No ModularContractLogEntry found for"
														  + " modularContractId: " + modularContractId + ","
														  + " orderLineTermId: " + modularContractRecord.getC_OrderLine_Term_ID()
														  + "! No logs will be created");
		}

		final ModularContractLogEntry modularContractLogEntry = modularContractLogEntryOptional.get();

		final List<I_C_Invoice_Candidate> invoiceCandidates = invoiceCandDAO.retrieveIcForIl(invoiceLineRecord);
		final InvoiceCandidateId invoiceCandidateId = invoiceCandidates.size() != 1 ? null
				: InvoiceCandidateId.ofRepoId(invoiceCandidates.get(0).getC_Invoice_Candidate_ID());

		final String productName = productBL.getProductValueAndName(productId);
		final String description = msgBL.getBaseLanguageMsg(MSG_ON_COMPLETE_DESCRIPTION, productName, qtyEntered);

		final LocalDateAndOrgId transactionDate = LocalDateAndOrgId.ofTimestamp(invoiceRecord.getDateInvoiced(),
																				OrgId.ofRepoId(invoiceLineRecord.getAD_Org_ID()),
																				orgDAO::getTimeZone);

		final InvoicingGroupId invoicingGroupId = modCntrInvoicingGroupRepository.getInvoicingGroupIdFor(productId, transactionDate.toInstant(orgDAO::getTimeZone))
				.orElse(null);

		return ExplainedOptional.of(
				LogEntryCreateRequest.builder()
						.referencedRecord(tableRecordReference)
						.contractId(modularContractId)
						.collectionPointBPartnerId(invoiceBpartnerId)
						.producerBPartnerId(invoiceBpartnerId)
						.invoicingBPartnerId(invoiceBpartnerId)
						.warehouseId(modularContractLogEntry.getWarehouseId())
						.productId(productId)
						.productName(createLogRequest.getProductName())
						.documentType(getLogEntryDocumentType())
						.contractType(getLogEntryContractType())
						.soTrx(SOTrx.PURCHASE)
						.processed(false)
						.quantity(qtyEntered)
						.amount(amount)
						.transactionDate(transactionDate)
						.year(createLogRequest.getModularContractSettings().getYearAndCalendarId().yearId())
						.description(description)
						.modularContractTypeId(createLogRequest.getTypeId())
						.configId(createLogRequest.getConfigId())
						.priceActual(productPrice)
						.invoiceCandidateId(invoiceCandidateId)
						.invoicingGroupId(invoicingGroupId)
						.build()
		);
	}

	@Override
	public @NonNull ExplainedOptional<LogEntryReverseRequest> createLogEntryReverseRequest(@NonNull final CreateLogRequest createLogRequest)
	{
		final TableRecordReference invoiceLineRef = createLogRequest.getRecordRef();
		final I_C_InvoiceLine invoiceLineRecord = invoiceBL.getLineById(InvoiceLineId.ofRepoId(invoiceLineRef.getRecordIdAssumingTableName(I_C_InvoiceLine.Table_Name)));

		final Quantity quantity = contractLogDAO.retrieveQuantityFromExistingLog(
				ModularContractLogQuery.builder()
						.flatrateTermId(createLogRequest.getContractId())
						.referenceSet(TableRecordReferenceSet.of(invoiceLineRef))
						.contractType(getLogEntryContractType())
						.build());

		final ProductId productId = ProductId.ofRepoId(invoiceLineRecord.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = msgBL.getBaseLanguageMsg(MSG_ON_REVERSE_DESCRIPTION, productName, quantity);

		return ExplainedOptional.of(
				LogEntryReverseRequest.builder()
						.referencedModel(invoiceLineRef)
						.flatrateTermId(createLogRequest.getContractId())
						.description(description)
						.logEntryContractType(getLogEntryContractType())
						.contractModuleId(createLogRequest.getModularContractModuleId())
						.build()
		);
	}
}
