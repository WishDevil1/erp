/*
 * #%L
 * de.metas.banking.base
 * %%
 * Copyright (C) 2019 metas GmbH
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

package de.metas.banking.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.ad.modelvalidator.IModelInterceptorRegistry;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.I_C_BP_BankAccount;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BankStatement;
import org.compiere.model.I_C_BankStatementLine;
import org.compiere.model.I_C_Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.metas.banking.BankStatementId;
import de.metas.banking.BankStatementLineId;
import de.metas.banking.BankStatementLineReferenceList;
import de.metas.banking.api.BankAccountId;
import de.metas.banking.payment.PaymentLinkResult;
import de.metas.banking.payment.impl.BankStatementPaymentBL;
import de.metas.banking.service.BankStatementCreateRequest;
import de.metas.banking.service.BankStatementLineCreateRequest;
import de.metas.banking.service.IBankStatementBL;
import de.metas.banking.service.IBankStatementDAO;
import de.metas.banking.service.IBankStatementListener;
import de.metas.banking.service.IBankStatementListenerService;
import de.metas.bpartner.BPartnerId;
import de.metas.business.BusinessTestHelper;
import de.metas.currency.CurrencyCode;
import de.metas.currency.CurrencyRepository;
import de.metas.currency.impl.PlainCurrencyDAO;
import de.metas.document.engine.DocStatus;
import de.metas.money.CurrencyId;
import de.metas.money.Money;
import de.metas.money.MoneyService;
import de.metas.organization.OrgId;
import de.metas.payment.PaymentId;
import de.metas.payment.TenderType;
import de.metas.payment.api.IPaymentBL;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;
import lombok.NonNull;

class BankStatementPaymentBLTest
{
	private final IPaymentBL paymentBL = Services.get(IPaymentBL.class);
	private final IBankStatementDAO bankStatementDAO = Services.get(IBankStatementDAO.class);
	private IBankStatementListenerService bankStatementListenerService;
	private BankStatementPaymentBL bankStatementPaymentBL;

	private final String metasfreshIban = "123456";
	private final LocalDate statementDate = SystemTime.asLocalDate();
	private final LocalDate valutaDate = SystemTime.asLocalDate();

	private CurrencyId euroCurrencyId;
	private BankAccountId euroOrgBankAccountId;

	@BeforeEach
	void beforeEach()
	{
		AdempiereTestHelper.get().init();
		bankStatementListenerService = Services.get(IBankStatementListenerService.class);
		bankStatementPaymentBL = new BankStatementPaymentBL(new MoneyService(new CurrencyRepository()));

		final IBankStatementBL bankStatementBL = Services.get(IBankStatementBL.class);

		final IModelInterceptorRegistry modelInterceptorRegistry = Services.get(IModelInterceptorRegistry.class);
		modelInterceptorRegistry.addModelInterceptor(new C_BankStatementLine_MockedInterceptor(bankStatementBL));

		createMasterData();
	}

	private void createMasterData()
	{
		euroCurrencyId = PlainCurrencyDAO.createCurrencyId(CurrencyCode.EUR);
		euroOrgBankAccountId = createOrgBankAccount(euroCurrencyId);
	}

	private BPartnerId createCustomer()
	{
		I_C_BPartner customer = BusinessTestHelper.createBPartner("le customer");
		return BPartnerId.ofRepoId(customer.getC_BPartner_ID());
	}

	private BankAccountId createOrgBankAccount(final CurrencyId euroCurrencyId)
	{
		final I_C_BPartner metasfreshBPartner = BusinessTestHelper.createBPartner("metasfresh");
		final I_C_BP_BankAccount metasfreshBankAccount = BusinessTestHelper.createBpBankAccount(BPartnerId.ofRepoId(metasfreshBPartner.getC_BPartner_ID()), euroCurrencyId, metasfreshIban);
		return BankAccountId.ofRepoId(metasfreshBankAccount.getC_BP_BankAccount_ID());
	}

	private I_C_BankStatement createBankStatement(final BankAccountId orgBankAccountId)
	{
		final BankStatementId bankStatementId = bankStatementDAO.createBankStatement(BankStatementCreateRequest.builder()
				.orgId(OrgId.ANY)
				.orgBankAccountId(orgBankAccountId)
				.name("Bank Statement 1")
				.statementDate(statementDate)
				.build());

		return bankStatementDAO.getById(bankStatementId);
	}

	private I_C_BankStatementLine createBankStatementLine(
			final BankStatementId bankStatementId,
			final BPartnerId bpartnerId,
			final Money stmtAmt)
	{
		final BankStatementLineId bankStatementLineId = bankStatementDAO.createBankStatementLine(BankStatementLineCreateRequest.builder()
				.bankStatementId(bankStatementId)
				.orgId(OrgId.ANY)
				.bpartnerId(bpartnerId)
				.lineNo(10)
				.statementLineDate(statementDate)
				.valutaDate(valutaDate)
				.statementAmt(stmtAmt)
				.trxAmt(stmtAmt)
				.build());

		return bankStatementDAO.getLineById(bankStatementLineId);
	}

	@Nested
	public class linkSinglePayment
	{
		@Test
		public void checkListenerFired()
		{
			final BPartnerId customerId = createCustomer();

			final I_C_BankStatement bankStatement = createBankStatement(euroOrgBankAccountId);
			final I_C_BankStatementLine bankStatementLine = createBankStatementLine(
					BankStatementId.ofRepoId(bankStatement.getC_BankStatement_ID()),
					customerId,
					Money.of(-123, euroCurrencyId));

			final I_C_Payment payment = paymentBL.newOutboundPaymentBuilder()
					.adOrgId(OrgId.ANY)
					.bpartnerId(customerId)
					.orgBankAccountId(euroOrgBankAccountId)
					.currencyId(euroCurrencyId)
					.payAmt(new BigDecimal("123"))
					.dateAcct(statementDate)
					.dateTrx(statementDate)
					.description("test")
					.tenderType(TenderType.DirectDeposit)
					.createAndProcess();

			final ArrayList<PaymentLinkResult> paymentsLinked = new ArrayList<>();
			bankStatementListenerService.addListener(new IBankStatementListener()
			{
				@Override
				public void onPaymentsLinked(List<PaymentLinkResult> payments)
				{
					paymentsLinked.addAll(payments);
				}

				@Override
				public void onPaymentsUnlinkedFromBankStatementLineReferences(@NonNull BankStatementLineReferenceList lineRefs)
				{
					throw new UnsupportedOperationException();
				}
			});

			//
			// call tested method
			//
			bankStatement.setDocStatus(DocStatus.Completed.getCode());
			bankStatementPaymentBL.linkSinglePayment(bankStatement, bankStatementLine, payment);

			assertThat(paymentsLinked).hasSize(1);
			assertThat(paymentsLinked.get(0)).isEqualTo(PaymentLinkResult.builder()
					.bankStatementId(BankStatementId.ofRepoId(bankStatementLine.getC_BankStatement_ID()))
					.bankStatementLineId(BankStatementLineId.ofRepoId(bankStatementLine.getC_BankStatementLine_ID()))
					.bankStatementLineRefId(null)
					.paymentId(PaymentId.ofRepoId(payment.getC_Payment_ID()))
					.statementTrxAmt(Money.of(-123, euroCurrencyId))
					.paymentMarkedAsReconciled(payment.isReconciled())
					.build());
		}
	}

	@Nested
	public class findOrCreateSinglePaymentAndLinkIfPossible
	{
		private void paymentChecks(
				final String expectedPayAmt,
				final int expectedC_payment_id,
				final boolean expectedIsReceipt,
				final BankAccountId expectedBankAccountId)
		{
			final I_C_Payment payment = InterfaceWrapperHelper.load(expectedC_payment_id, I_C_Payment.class);
			assertThat(payment).isNotNull();
			assertThat(payment.getPayAmt()).isEqualTo(expectedPayAmt);
			assertThat(payment.isReconciled()).isTrue();
			assertThat(payment.isReceipt()).isEqualTo(expectedIsReceipt);
			assertThat(DocStatus.ofCode(payment.getDocStatus())).isEqualTo(DocStatus.Completed);
			assertThat(payment.getC_BP_BankAccount_ID()).isEqualTo(expectedBankAccountId.getRepoId());

			// can't test `payment.getC_DocType_ID()` as it is set by `PaymentsForInvoicesCreator`, and during test there's no DocTypes
		}

		@Nested
		class ExistingPayments
		{
			@Test
			@Disabled("not implemented")
			void vendorOneMatchingPaymentExists_DifferentInvoiceOnBSL()
			{
				// TODO tbp: check with mark in a followup task about this usecase.
				// the followup task is: https://github.com/metasfresh/metasfresh/issues/6128
				// here is a draft of the data required to test
				// outgoing payment:
				// there is invoice for vendor: no. 1111
				// payment is allocated completely against invoice
				// amount 169.09 (same as BSL)
				// curency ok
				// bpartner ok
				// NOT reconciled!
				// ====================
				// BankStatementLine:
				// same bpartner as Invoice 1111
				// amount = 169.09 (same as payment)
				// reference = 2222 (this invoice number is different from the one on the payment!!!!!!!!)
				// ====================
				// => outcome: should not auto-link the payment, since the invoices are wrong
			}

			@Test
			void OneMatchingPaymentExists_SoItIsLinked()
			{
				//
				// create test data
				final I_C_BankStatement bankStatement = createBankStatement(euroOrgBankAccountId);

				final BPartnerId customerId = createCustomer();

				final I_C_BankStatementLine bsl = createBankStatementLine(
						BankStatementId.ofRepoId(bankStatement.getC_BankStatement_ID()),
						customerId,
						Money.of(-123, euroCurrencyId));

				final I_C_Payment payment = paymentBL.newOutboundPaymentBuilder()
						.adOrgId(OrgId.ANY)
						.bpartnerId(customerId)
						.orgBankAccountId(euroOrgBankAccountId)
						.currencyId(euroCurrencyId)
						.payAmt(new BigDecimal("123"))
						.dateAcct(statementDate)
						.dateTrx(statementDate)
						.description("test")
						.tenderType(TenderType.DirectDeposit)
						.createAndProcess();

				//
				// call tested method
				//
				bankStatement.setDocStatus(DocStatus.Completed.getCode());
				bankStatementPaymentBL.findOrCreateSinglePaymentAndLinkIfPossible(bankStatement, bsl);

				//
				// Checks
				final boolean isReceipt = false;
				assertEquals(payment.getC_Payment_ID(), bsl.getC_Payment_ID());
				paymentChecks("123", bsl.getC_Payment_ID(), isReceipt, euroOrgBankAccountId);
				assertFalse(bsl.isMultiplePayment());
				assertFalse(bsl.isMultiplePaymentOrInvoice());
			}

			@Test
			void TwoIdenticalPaymentsExist_SoLineHasNoPaymentLinked()
			{
				//
				// create test data
				final I_C_BankStatement bankStatement = createBankStatement(euroOrgBankAccountId);

				final BPartnerId customerId = createCustomer();

				final I_C_BankStatementLine bsl = createBankStatementLine(
						BankStatementId.ofRepoId(bankStatement.getC_BankStatement_ID()),
						customerId,
						Money.of(-123, euroCurrencyId));

				//
				// create 2 identical payments
				final I_C_Payment payment1 = paymentBL.newOutboundPaymentBuilder()
						.adOrgId(OrgId.ANY)
						.bpartnerId(customerId)
						.orgBankAccountId(euroOrgBankAccountId)
						.currencyId(euroCurrencyId)
						.payAmt(new BigDecimal("123"))
						.dateAcct(statementDate)
						.dateTrx(statementDate)
						.description("test")
						.tenderType(TenderType.DirectDeposit)
						.createAndProcess();

				final I_C_Payment payment2 = paymentBL.newOutboundPaymentBuilder()
						.adOrgId(OrgId.ANY)
						.bpartnerId(customerId)
						.orgBankAccountId(euroOrgBankAccountId)
						.currencyId(euroCurrencyId)
						.payAmt(new BigDecimal("123"))
						.dateAcct(statementDate)
						.dateTrx(statementDate)
						.description("test")
						.tenderType(TenderType.DirectDeposit)
						.createAndProcess();

				//
				// call tested method
				//
				bankStatement.setDocStatus(DocStatus.Completed.getCode());
				bankStatementPaymentBL.findOrCreateSinglePaymentAndLinkIfPossible(bankStatement, bsl);

				//
				// Checks
				assertEquals(0, bsl.getC_Payment_ID());
				assertFalse(bsl.isMultiplePayment());
				assertFalse(bsl.isMultiplePaymentOrInvoice());

				InterfaceWrapperHelper.refresh(payment1);
				InterfaceWrapperHelper.refresh(payment2);
				assertFalse(payment1.isReconciled());
				assertFalse(payment2.isReconciled());
			}
		}

		@Nested
		class NoExistingPayment
		{

			@Test
			void OneInboundBankStatementLine_PaymentIsCreated()
			{
				//
				// create test data
				final I_C_BankStatement bankStatement = createBankStatement(euroOrgBankAccountId);

				final BPartnerId customerId = createCustomer();

				final I_C_BankStatementLine bsl = createBankStatementLine(
						BankStatementId.ofRepoId(bankStatement.getC_BankStatement_ID()),
						customerId,
						Money.of(123, euroCurrencyId));

				//
				// call tested method
				//
				bankStatement.setDocStatus(DocStatus.Completed.getCode());
				bankStatementPaymentBL.findOrCreateSinglePaymentAndLinkIfPossible(bankStatement, bsl);

				//
				// Checks
				final boolean isReceipt = true;
				paymentChecks("123", bsl.getC_Payment_ID(), isReceipt, euroOrgBankAccountId);
				assertFalse(bsl.isMultiplePayment());
				assertFalse(bsl.isMultiplePaymentOrInvoice());
			}

			@Test
			void OneOutboundBankStatementLine_PaymentIsCreated()
			{
				//
				// create test data
				final I_C_BankStatement bankStatement = createBankStatement(euroOrgBankAccountId);

				final BPartnerId customerId = createCustomer();

				final I_C_BankStatementLine bsl = createBankStatementLine(
						BankStatementId.ofRepoId(bankStatement.getC_BankStatement_ID()),
						customerId,
						Money.of(-123, euroCurrencyId));

				//
				// call tested method
				//
				bankStatement.setDocStatus(DocStatus.Completed.getCode());
				bankStatementPaymentBL.findOrCreateSinglePaymentAndLinkIfPossible(bankStatement, bsl);

				//
				// Checks
				final boolean isReceipt = false;
				paymentChecks("123", bsl.getC_Payment_ID(), isReceipt, euroOrgBankAccountId);
				assertFalse(bsl.isMultiplePayment());
				assertFalse(bsl.isMultiplePaymentOrInvoice());
			}
		}
	}
}
