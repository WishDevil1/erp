package de.metas.contracts.commission.commissioninstance.services.repos;

import de.metas.contracts.commission.commissioninstance.businesslogic.CommissionPoints;
import de.metas.currency.CurrencyPrecision;
import de.metas.invoicecandidate.api.IInvoiceCandBL;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.logging.LogManager;
import de.metas.money.Money;
import de.metas.money.MoneyService;
import de.metas.product.ProductPrice;
import de.metas.quantity.Quantitys;
import de.metas.tax.api.ITaxBL;
import de.metas.tax.api.ITaxDAO;
import de.metas.uom.UomId;
import de.metas.util.Services;
import de.metas.util.lang.Percent;
import lombok.NonNull;

import org.compiere.model.I_C_Tax;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static de.metas.util.lang.CoalesceUtil.firstGreaterThanZero;

import java.math.BigDecimal;

/*
 * #%L
 * de.metas.contracts
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

@Service
public class InvoiceCandidateRecordHelper
{
	private static final Logger logger = LogManager.getLogger(InvoiceCandidateRecordHelper.class);

	private final MoneyService moneyService;

	public InvoiceCandidateRecordHelper(@NonNull final MoneyService moneyService)
	{
		this.moneyService = moneyService;
	}

	CommissionPoints extractForecastCommissionPoints(@NonNull final I_C_Invoice_Candidate icRecord)
	{
		final CommissionPoints forecastCommissionPoints;

		final ProductPrice priceActual = Services.get(IInvoiceCandBL.class).getPriceActual(icRecord);

		final BigDecimal forecastQtyInPriceUOM = icRecord.getQtyEntered()
				.subtract(icRecord.getQtyToInvoiceInUOM())
				.subtract(icRecord.getQtyInvoicedInUOM());

		if (useActualPriceForComputingCommissionPoints(icRecord))
		{
			final Money forecastNetAmt = moneyService.multiply(
					Quantitys.create(forecastQtyInPriceUOM, UomId.ofRepoId(icRecord.getC_UOM_ID())),
					priceActual);

			forecastCommissionPoints = CommissionPoints.of(forecastNetAmt.toBigDecimal());
		}
		else
		{
			final BigDecimal baseCommissionPointsPerPriceUOM = icRecord.getBase_Commission_Points_Per_Price_UOM();

			final BigDecimal forecastCommissionPointsAmount = baseCommissionPointsPerPriceUOM.multiply(forecastQtyInPriceUOM);

			forecastCommissionPoints = CommissionPoints.of(forecastCommissionPointsAmount);
		}
		return deductTaxAmount(forecastCommissionPoints, icRecord);
	}

	CommissionPoints extractCommissionPointsToInvoice(@NonNull final I_C_Invoice_Candidate icRecord)
	{
		final CommissionPoints commissionPointsToInvoice;

		if (useActualPriceForComputingCommissionPoints(icRecord))
		{
			commissionPointsToInvoice = CommissionPoints.of(icRecord.getNetAmtToInvoice());
		}
		else
		{
			final BigDecimal baseCommissionPointsPerPriceUOM = icRecord.getBase_Commission_Points_Per_Price_UOM();
			commissionPointsToInvoice = CommissionPoints.of(baseCommissionPointsPerPriceUOM.multiply(icRecord.getQtyToInvoiceInUOM()));
		}
		return deductTaxAmount(commissionPointsToInvoice, icRecord);
	}

	CommissionPoints extractInvoicedCommissionPoints(@NonNull final I_C_Invoice_Candidate icRecord)
	{
		final CommissionPoints commissionPointsToInvoice;

		if (useActualPriceForComputingCommissionPoints(icRecord))
		{
			commissionPointsToInvoice = CommissionPoints.of(icRecord.getNetAmtInvoiced());
		}
		else
		{
			final BigDecimal baseCommissionPointsPerPriceUOM = icRecord.getBase_Commission_Points_Per_Price_UOM();
			commissionPointsToInvoice = CommissionPoints.of(baseCommissionPointsPerPriceUOM.multiply(icRecord.getQtyInvoicedInUOM()));
		}
		return deductTaxAmount(commissionPointsToInvoice, icRecord);
	}

	private CommissionPoints deductTaxAmount(
			@NonNull final CommissionPoints commissionPoints,
			@NonNull final I_C_Invoice_Candidate icRecord)
	{
		if (commissionPoints.isZero())
		{
			return commissionPoints; // don't bother going to the DAO layer
		}
		final ITaxDAO taxDAO = Services.get(ITaxDAO.class);
		final ITaxBL taxBL = Services.get(ITaxBL.class);
		final IInvoiceCandBL invoiceCandBL = Services.get(IInvoiceCandBL.class);

		final int effectiveTaxRepoId = firstGreaterThanZero(icRecord.getC_Tax_Override_ID(), icRecord.getC_Tax_ID());
		if (effectiveTaxRepoId <= 0)
		{
			logger.debug("Invoice candidate has effective C_Tax_ID={}; -> return undedacted commissionPoints={}", commissionPoints);
			return commissionPoints;
		}

		final I_C_Tax taxRecord = taxDAO.getTaxById(effectiveTaxRepoId);

		final CurrencyPrecision precision = invoiceCandBL.extractPricePrecision(icRecord);

		final BigDecimal taxAdjustedAmount = taxBL.calculateBaseAmt(
				taxRecord,
				commissionPoints.toBigDecimal(),
				icRecord.isTaxIncluded(),
				precision.toInt());
		return CommissionPoints.of(taxAdjustedAmount);
	}

	Percent extractTradedCommissionPercent(@NonNull final I_C_Invoice_Candidate icRecord)
	{
		return useActualPriceForComputingCommissionPoints(icRecord) ? Percent.ZERO : Percent.of(icRecord.getTraded_Commission_Percent());
	}

	/**
	 * Use actual price when computing the base commission points sum if {@link I_C_Invoice_Candidate#COLUMN_Base_Commission_Points_Per_Price_UOM}
	 * was not calculated or the price was overwritten.
	 *
	 * @param icRecord Invoice Candidate record
	 * @return true, if the actual price should be used for computing commission points, false otherwise
	 */
	private boolean useActualPriceForComputingCommissionPoints(@NonNull final I_C_Invoice_Candidate icRecord)
	{
		return (icRecord.getBase_Commission_Points_Per_Price_UOM().signum() == 0)
				|| (icRecord.getPriceEntered_Override().signum() > 0
						&& !icRecord.getPriceEntered_Override().equals(icRecord.getPriceEntered()));
	}
}
