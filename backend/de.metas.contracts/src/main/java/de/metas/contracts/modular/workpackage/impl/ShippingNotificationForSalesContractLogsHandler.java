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

import de.metas.contracts.modular.IModularContractTypeHandler;
import de.metas.contracts.modular.impl.ShippingNotificationForSalesModularContractHandler;
import de.metas.contracts.modular.invgroup.interceptor.ModCntrInvoicingGroupRepository;
import de.metas.contracts.modular.log.ModularContractLogDAO;
import de.metas.lang.SOTrx;
import de.metas.shippingnotification.ShippingNotificationService;
import de.metas.shippingnotification.model.I_M_Shipping_NotificationLine;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ShippingNotificationForSalesContractLogsHandler extends AbstractShippingNotificationLogHandler
{
	@NonNull
	private final ShippingNotificationForSalesModularContractHandler contractHandler;

	public ShippingNotificationForSalesContractLogsHandler(
			@NonNull final ShippingNotificationForSalesModularContractHandler contractHandler,
			@NonNull final ShippingNotificationService notificationService,
			@NonNull final ModCntrInvoicingGroupRepository modCntrInvoicingGroupRepository,
			@NonNull final ModularContractLogDAO contractLogDAO)
	{
		super(notificationService, modCntrInvoicingGroupRepository, contractLogDAO);
		this.contractHandler = contractHandler;
	}

	@Override
	public @NonNull IModularContractTypeHandler<I_M_Shipping_NotificationLine> getModularContractTypeHandler()
	{
		return contractHandler;
	}

	@Override
	public SOTrx getSOTrx()
	{
		return SOTrx.SALES;
	}
}
