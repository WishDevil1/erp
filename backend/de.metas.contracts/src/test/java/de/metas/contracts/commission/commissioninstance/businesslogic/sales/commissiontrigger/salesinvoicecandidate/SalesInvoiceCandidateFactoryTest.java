package de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.salesinvoicecandidate;

import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static java.math.BigDecimal.TEN;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.adempiere.test.AdempiereTestHelper;
import org.compiere.model.I_C_Currency;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_Product;
import org.compiere.util.TimeUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.metas.business.BusinessTestHelper;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.salesinvoicecandidate.SalesInvoiceCandidate;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.salesinvoicecandidate.SalesInvoiceCandidateFactory;
import de.metas.contracts.commission.commissioninstance.services.CommissionProductService;
import de.metas.currency.CurrencyRepository;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.money.MoneyService;
import de.metas.util.time.SystemTime;
import io.github.jsonSnapshot.SnapshotMatcher;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2020 metas GmbH
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

class SalesInvoiceCandidateFactoryTest
{
	private SalesInvoiceCandidateFactory salesInvoiceCandidateFactory;

	@BeforeEach
	void beforeEach()
	{
		AdempiereTestHelper.get().init();
		final MoneyService moneyService = new MoneyService(new CurrencyRepository());
		salesInvoiceCandidateFactory = new SalesInvoiceCandidateFactory(moneyService, new CommissionProductService());
	}

	@BeforeAll
	static void beforeAll()
	{
		SnapshotMatcher.start(
				AdempiereTestHelper.SNAPSHOT_CONFIG,
				AdempiereTestHelper.createSnapshotJsonFunction());
	}

	@AfterAll
	static void afterAll()
	{
		validateSnapshots();
	}

	@Test
	void forRecord()
	{
		SystemTime.setTimeSource(() -> 1583223780929L); // approximately 2020-03-03 09:23CET

		final I_C_UOM uomRecord = BusinessTestHelper.createUOM("uom");
		final I_C_Currency currencyRecord = BusinessTestHelper.createCurrency("TobiDollar");

		final I_M_Product product = BusinessTestHelper.createProduct("product", uomRecord);
		product.setIsCommissioned(true);
		saveRecord(product);

		final I_C_Invoice_Candidate icRecord = newInstance(I_C_Invoice_Candidate.class);
		icRecord.setAD_Org_ID(10);
		icRecord.setC_Currency_ID(currencyRecord.getC_Currency_ID());
		icRecord.setC_UOM_ID(uomRecord.getC_UOM_ID());
		icRecord.setM_Product_ID(product.getM_Product_ID());
		icRecord.setC_BPartner_SalesRep_ID(20);
		icRecord.setBill_BPartner_ID(30);
		icRecord.setPriceActual(TEN);
		icRecord.setDateOrdered(TimeUtil.parseTimestamp("2020-03-21"));

		icRecord.setQtyEntered(new BigDecimal("50"));

		icRecord.setNetAmtToInvoice(new BigDecimal("300"));
		icRecord.setQtyToInvoiceInUOM(new BigDecimal("30"));

		icRecord.setNetAmtInvoiced(new BigDecimal("100"));
		icRecord.setQtyInvoicedInUOM(new BigDecimal("10"));
		saveRecord(icRecord);

		// invoke the method under test
		final Optional<SalesInvoiceCandidate> result = salesInvoiceCandidateFactory.forRecord(icRecord);
		assertThat(result).isPresent();

		assertThat(result.get().getForecastCommissionPoints().toBigDecimal()).isEqualByComparingTo("100"); // (Entered - ToInvoiceInUOM - InvoicedInUOM) * PriceActual
		assertThat(result.get().getCommissionPointsToInvoice().toBigDecimal()).isEqualByComparingTo("300"); // toInvoiceInUOM * priceActual
		assertThat(result.get().getInvoicedCommissionPoints().toBigDecimal()).isEqualByComparingTo("100"); // invoicedInUOM * priceActual

		SnapshotMatcher.expect(result.get()).toMatchSnapshot();
	}
}
