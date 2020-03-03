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

package de.metas.contracts.commission.commissioninstance.services;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import de.metas.contracts.commission.commissioninstance.businesslogic.CommissionInstance;
import de.metas.contracts.commission.commissioninstance.businesslogic.CreateInstanceRequest;
import lombok.NonNull;

@Service
public class CommissionInstanceService
{
	private final CommissionInstanceRequestFactory commissionInstanceRequestFactory;

	private final CommissionAlgorithmInvoker commissionAlgorithmInvoker;

	public CommissionInstanceService(
			@NonNull final CommissionInstanceRequestFactory commissionInstanceRequestFactory,
			@NonNull final CommissionAlgorithmInvoker commissionAlgorithmInvoker)
	{
		this.commissionInstanceRequestFactory = commissionInstanceRequestFactory;
		this.commissionAlgorithmInvoker = commissionAlgorithmInvoker;
	}

	public ImmutableList<CommissionInstance> getCommissionInstanceFor(
			@NonNull final CreateForecastCommissionInstanceRequest createForecastCommissionInstanceRequest)
	{
		final ImmutableList<CreateInstanceRequest> requests = commissionInstanceRequestFactory.createRequestFor(createForecastCommissionInstanceRequest);

		final ImmutableList.Builder<CommissionInstance> result = ImmutableList.builder();
		for (final CreateInstanceRequest request : requests)
		{
			result.add(commissionAlgorithmInvoker.applyCreateRequest(request));
		}
		return result.build();
	}
}
