package de.metas.contracts.commission.commissioninstance.services;

import org.springframework.stereotype.Service;

import de.metas.contracts.commission.Beneficiary;
import de.metas.contracts.commission.Customer;
import de.metas.contracts.commission.commissioninstance.businesslogic.CommissionPoints;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.CommissionTrigger;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.CommissionTriggerData;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.CommissionTriggerDocument;
import de.metas.contracts.commission.commissioninstance.businesslogic.sales.commissiontrigger.CommissionTriggerData.CommissionTriggerDataBuilder;
import de.metas.util.lang.Percent;
import lombok.NonNull;

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
public class CommissionTriggerFactory
{
	/**
	 * @param documentDeleted might be true for invoice candidates
	 */
	public CommissionTrigger createForDocument(
			@NonNull final CommissionTriggerDocument commissionTriggerDocument,
			final boolean documentDeleted)
	{
		final CommissionTriggerData triggerData = createForRequest(commissionTriggerDocument, documentDeleted);

		final CommissionTrigger trigger = CommissionTrigger.builder()
				.customer(Customer.of(commissionTriggerDocument.getCustomerBPartnerId()))
				.beneficiary(Beneficiary.of(commissionTriggerDocument.getSalesRepBPartnerId()))
				.commissionTriggerData(triggerData)
				.build();

		return trigger;
	}

	private CommissionTriggerData createForRequest(
			@NonNull final CommissionTriggerDocument commissionTriggerDocument,
			final boolean documentDeleted)
	{
		final CommissionTriggerDataBuilder builder = CommissionTriggerData
				.builder()
				.orgId(commissionTriggerDocument.getOrgId())
				.invoiceCandidateWasDeleted(documentDeleted)
				.triggerType(commissionTriggerDocument.getTriggerType())
				.triggerDocumentId(commissionTriggerDocument.getId())
				.triggerDocumentDate(commissionTriggerDocument.getCommissionDate())
				.timestamp(commissionTriggerDocument.getUpdated());

		if (documentDeleted)
		{
			builder
					.forecastedBasePoints(CommissionPoints.ZERO)
					.invoiceableBasePoints(CommissionPoints.ZERO)
					.invoicedBasePoints(CommissionPoints.ZERO)
					.tradedCommissionPercent(Percent.ZERO);
		}
		else
		{
			builder
					.forecastedBasePoints(commissionTriggerDocument.getForecastCommissionPoints())
					.invoiceableBasePoints(commissionTriggerDocument.getCommissionPointsToInvoice())
					.invoicedBasePoints(commissionTriggerDocument.getInvoicedCommissionPoints())
					.tradedCommissionPercent(commissionTriggerDocument.getTradedCommissionPercent());
		}
		return builder.build();
	}

}
