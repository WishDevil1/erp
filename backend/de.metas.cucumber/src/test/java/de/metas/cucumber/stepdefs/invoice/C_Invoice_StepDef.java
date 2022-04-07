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

package de.metas.cucumber.stepdefs.invoice;

import com.google.common.collect.ImmutableSet;
import de.metas.cucumber.stepdefs.C_BPartner_Location_StepDefData;
import de.metas.cucumber.stepdefs.C_BPartner_StepDefData;
import de.metas.cucumber.stepdefs.C_OrderLine_StepDefData;
import de.metas.cucumber.stepdefs.C_Order_StepDefData;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.cucumber.stepdefs.StepDefConstants;
import de.metas.cucumber.stepdefs.StepDefData;
import de.metas.cucumber.stepdefs.StepDefDocAction;
import de.metas.cucumber.stepdefs.StepDefUtil;
import de.metas.cucumber.stepdefs.invoicecandidate.C_Invoice_Candidate_StepDefData;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.inout.model.I_M_InOutLine;
import de.metas.invoice.InvoiceId;
import de.metas.invoice.service.IInvoiceDAO;
import de.metas.invoicecandidate.InvoiceCandidateId;
import de.metas.invoicecandidate.api.IInvoiceCandDAO;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.payment.paymentterm.IPaymentTermRepository;
import de.metas.payment.paymentterm.PaymentTermId;
import de.metas.payment.paymentterm.impl.PaymentTermQuery;
import de.metas.util.Check;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_InvoiceLine;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.X_C_Invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static de.metas.invoicecandidate.model.I_C_Invoice_Candidate.COLUMNNAME_C_Invoice_Candidate_ID;
import static org.assertj.core.api.Assertions.*;
import static org.compiere.model.I_C_Invoice.COLUMNNAME_C_Invoice_ID;

public class C_Invoice_StepDef
{
	private final IPaymentTermRepository paymentTermRepo = Services.get(IPaymentTermRepository.class);
	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final IInvoiceCandDAO invoiceCandDAO = Services.get(IInvoiceCandDAO.class);
	private final IInvoiceDAO invoiceDAO = Services.get(IInvoiceDAO.class);
	private final IDocumentBL documentBL = Services.get(IDocumentBL.class);

	private final C_Invoice_StepDefData invoiceTable;
	private final C_InvoiceLine_StepDefData invoiceLineTable;
	private final C_Order_StepDefData orderTable;
	private final C_BPartner_StepDefData bPartnerTable;
	private final C_BPartner_Location_StepDefData bPartnerLocationTable;
	private final C_OrderLine_StepDefData orderLineTable;
	private final C_Invoice_Candidate_StepDefData invoiceCandidateTable;

	public C_Invoice_StepDef(
			@NonNull final C_Invoice_StepDefData invoiceTable,
			@NonNull final C_InvoiceLine_StepDefData invoiceLineTable,
			@NonNull final C_Order_StepDefData orderTable,
			@NonNull final C_BPartner_StepDefData bPartnerTable,
			@NonNull final C_BPartner_Location_StepDefData bPartnerLocationTable,
			@NonNull final C_Invoice_Candidate_StepDefData invoiceCandidateTable,
			@NonNull final C_OrderLine_StepDefData orderLineTable)
	{
		this.invoiceTable = invoiceTable;
		this.invoiceLineTable = invoiceLineTable;
		this.orderTable = orderTable;
		this.bPartnerTable = bPartnerTable;
		this.bPartnerLocationTable = bPartnerLocationTable;
		this.invoiceCandidateTable = invoiceCandidateTable;
		this.orderLineTable = orderLineTable;
	}

	@And("validate created invoices")
	public void validate_created_invoices(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			final String identifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_Invoice_ID + "." + TABLECOLUMN_IDENTIFIER);

			final I_C_Invoice invoice = invoiceTable.get(identifier);

			validateInvoice(invoice, row);
		}
	}

	@And("^the invoice identified by (.*) is (reversed)$")
	public void invoice_action(@NonNull final String invoiceIdentifier, @NonNull final String action)
	{
		final I_C_Invoice invoice = invoiceTable.get(invoiceIdentifier);

		switch (StepDefDocAction.valueOf(action))
		{
			case reversed:
				invoice.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInvoice.completeIt() won't complete it
				documentBL.processEx(invoice, IDocument.ACTION_Reverse_Correct, IDocument.STATUS_Reversed);
				break;
			default:
				throw new AdempiereException("Unhandled C_Invoice action")
						.appendParametersToMessage()
						.setParameter("action:", action);
		}
	}

	@And("load C_Invoice:")
	public void loadC_Invoice(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final BigDecimal qtyInvoiced = DataTableUtil.extractBigDecimalForColumnName(row, I_C_InvoiceLine.COLUMNNAME_QtyInvoiced);

			final IQueryBuilder<I_C_InvoiceLine> invoiceLineBuilder = queryBL.createQueryBuilder(I_C_InvoiceLine.class)
					.addEqualsFilter(I_C_InvoiceLine.COLUMNNAME_QtyInvoiced, qtyInvoiced);

			final String orderLineIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_C_InvoiceLine.COLUMNNAME_C_OrderLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderLineIdentifier))
			{
				final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
				invoiceLineBuilder.addEqualsFilter(I_M_InOutLine.COLUMNNAME_C_OrderLine_ID, orderLine.getC_OrderLine_ID());
			}

			final I_C_InvoiceLine invoiceLine = invoiceLineBuilder.create()
					.firstOnlyNotNull(I_C_InvoiceLine.class);

			final String invoiceLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_InvoiceLine.COLUMNNAME_C_InvoiceLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			invoiceLineTable.putOrReplace(invoiceLineIdentifier, invoiceLine);

			final I_C_Invoice invoice = InterfaceWrapperHelper.load(invoiceLine.getC_Invoice_ID(), I_C_Invoice.class);
			assertThat(invoice).isNotNull();

			final String docStatus = DataTableUtil.extractStringForColumnName(row, I_C_Invoice.COLUMNNAME_DocStatus);
			assertThat(invoice.getDocStatus()).isEqualTo(docStatus);

			final String orderIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_C_Invoice.COLUMNNAME_C_Order_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderIdentifier))
			{
				final I_C_Order order = orderTable.get(orderIdentifier);
				assertThat(invoice.getC_Order_ID()).isEqualTo(order.getC_Order_ID());
			}

			final String invoiceIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_Invoice_ID + "." + TABLECOLUMN_IDENTIFIER);
			invoiceTable.putOrReplace(invoiceIdentifier, invoice);
		}
	}

	@And("^after not more than (.*)s, C_Invoice are found:$")
	public void wait_until_there_are_invoices(final int timeoutSec, @NonNull final DataTable dataTable) throws InterruptedException
	{
		for (final Map<String, String> tableRow : dataTable.asMaps())
		{
			StepDefUtil.tryAndWait(timeoutSec, 500, () -> loadInvoice(tableRow));
		}
	}

	private void validateInvoice(@NonNull final I_C_Invoice invoice, @NonNull final Map<String, String> row)
	{
		final String bpartnerIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_BPartner.COLUMNNAME_C_BPartner_ID + "." + TABLECOLUMN_IDENTIFIER);
		final String bpartnerLocationIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_BPartner_Location.COLUMNNAME_C_BPartner_Location_ID + "." + TABLECOLUMN_IDENTIFIER);
		final String poReference = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_C_Invoice.COLUMNNAME_POReference);
		final String paymentTerm = DataTableUtil.extractStringForColumnName(row, "paymentTerm");
		final boolean processed = DataTableUtil.extractBooleanForColumnName(row, "processed");
		final String docStatus = DataTableUtil.extractStringForColumnName(row, "docStatus");

		final Integer expectedBPartnerId = bPartnerTable.getOptional(bpartnerIdentifier)
				.map(I_C_BPartner::getC_BPartner_ID)
				.orElseGet(() -> Integer.parseInt(bpartnerIdentifier));

		final Integer expectedBPartnerLocationId = bPartnerLocationTable.getOptional(bpartnerLocationIdentifier)
				.map(I_C_BPartner_Location::getC_BPartner_Location_ID)
				.orElseGet(() -> Integer.parseInt(bpartnerLocationIdentifier));

		assertThat(invoice.getC_BPartner_ID()).isEqualTo(expectedBPartnerId);
		assertThat(invoice.getC_BPartner_Location_ID()).isEqualTo(expectedBPartnerLocationId);
		assertThat(invoice.getPOReference()).isEqualTo(poReference);
		assertThat(invoice.isProcessed()).isEqualTo(processed);
		assertThat(invoice.getDocStatus()).isEqualTo(docStatus);

		final PaymentTermQuery query = PaymentTermQuery.builder()
				.orgId(StepDefConstants.ORG_ID)
				.value(paymentTerm)
				.build();
		final PaymentTermId paymentTermId = paymentTermRepo.retrievePaymentTermId(query)
				.orElse(null);

		assertThat(paymentTermId).isNotNull();
		assertThat(invoice.getC_PaymentTerm_ID()).isEqualTo(paymentTermId.getRepoId());
	}

	public Boolean loadInvoice(@NonNull final Map<String, String> row)
	{
		final String invoiceCandIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_Invoice_Candidate_ID + "." + TABLECOLUMN_IDENTIFIER);
		final I_C_Invoice_Candidate invoiceCandidate = invoiceCandidateTable.get(invoiceCandIdentifier);

		final InvoiceCandidateId invoiceCandidateId = InvoiceCandidateId.ofRepoId(invoiceCandidate.getC_Invoice_Candidate_ID());

		final Set<InvoiceId> invoiceIds = invoiceCandDAO.retrieveIlForIc(invoiceCandidateId)
				.stream()
				.map(I_C_InvoiceLine::getC_Invoice_ID)
				.map(InvoiceId::ofRepoId)
				.collect(ImmutableSet.toImmutableSet());

		if (invoiceIds.isEmpty())
		{
			return false;
		}

		final List<I_C_Invoice> invoices = invoiceDAO.getByIdsOutOfTrx(invoiceIds);

		final String invoiceStatus = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_C_Invoice.COLUMNNAME_DocStatus);
		final String docStatus = Optional.ofNullable(invoiceStatus)
				.orElse(X_C_Invoice.DOCACTION_Complete);

		final Optional<I_C_Invoice> invoice = invoices.stream()
				.filter(i -> i.getDocStatus().equals(docStatus))
				.findFirst();

		if (!invoice.isPresent())
		{
			return false;
		}

		final String invoiceIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_Invoice_ID + "." + TABLECOLUMN_IDENTIFIER);
		invoiceTable.putOrReplace(invoiceIdentifier, invoice.get());

		return true;
	}
}
