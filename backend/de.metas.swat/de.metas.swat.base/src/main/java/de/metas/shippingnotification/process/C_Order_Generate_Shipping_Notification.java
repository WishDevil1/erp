/*
 * #%L
 * de.metas.swat.base
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

package de.metas.shippingnotification.process;

import de.metas.document.engine.DocStatus;
import de.metas.i18n.AdMessageKey;
import de.metas.inoutcandidate.api.IShipmentSchedulePA;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.order.IOrderDAO;
import de.metas.order.OrderId;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.Param;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.shippingnotification.ShippingNotificationService;
import de.metas.util.Services;
import lombok.NonNull;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_C_Order;
import org.compiere.util.TimeUtil;

import java.sql.Timestamp;
import java.util.Collection;

public class C_Order_Generate_Shipping_Notification extends JavaProcess implements IProcessPrecondition
{

	private static final AdMessageKey MSG_M_Shipment_Notification_NoHarvestingYear = AdMessageKey.of("de.metas.shippingnotification.NoHarvestingYear");
	private static final AdMessageKey MSG_M_Shipment_Notification_NoShipmentSchedule = AdMessageKey.of("de.metas.shippingnotification.NoShipmentSchedule");
	private static final String PARAM_PhysicalClearanceDate = "PhysicalClearanceDate";
	final IOrderDAO orderDAO = Services.get(IOrderDAO.class);
	private final ShippingNotificationService shippingNotificationService = SpringContextHolder.instance.getBean(ShippingNotificationService.class);
	private final IShipmentSchedulePA shipmentSchedulePA = Services.get(IShipmentSchedulePA.class);

	@Param(parameterName = PARAM_PhysicalClearanceDate, mandatory = true)
	private Timestamp p_physicalClearanceDate;

	public ProcessPreconditionsResolution checkPreconditionsApplicable(@NonNull final IProcessPreconditionsContext context)
	{
		if (context.isNoSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}

		if (context.isMoreThanOneSelected())
		{
			return ProcessPreconditionsResolution.rejectBecauseNotSingleSelection();
		}

		final OrderId orderId = context.getSingleSelectedRecordId(OrderId.class);
		final I_C_Order order = orderDAO.getById(orderId);
		if (!DocStatus.ofNullableCodeOrUnknown(order.getDocStatus()).isCompleted())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("only completed orders");
		}

		if (order.getHarvesting_Year_ID() <= 0)
		{
			return ProcessPreconditionsResolution.reject(msgBL.getTranslatableMsgText(MSG_M_Shipment_Notification_NoHarvestingYear));
		}

		if (shipmentSchedulePA.getByIds(shipmentSchedulePA.retrieveScheduleIdsByOrderId(orderId), I_M_ShipmentSchedule.class).isEmpty())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason(msgBL.getTranslatableMsgText(MSG_M_Shipment_Notification_NoShipmentSchedule));
		}

		return ProcessPreconditionsResolution.accept();
	}

	@Override
	protected String doIt() throws Exception
	{
		final OrderId orderId = OrderId.ofRepoId(getRecord_ID());
		// reverse notifications if exists
		shippingNotificationService.reverseIfExistsShippingNotifications(orderId);

		// generate new one
		shippingNotificationService.generateShippingNotificationAndPropagatePhysicalClearanceDate(orderId, TimeUtil.asInstant(p_physicalClearanceDate));

		return MSG_OK;
	}
}
