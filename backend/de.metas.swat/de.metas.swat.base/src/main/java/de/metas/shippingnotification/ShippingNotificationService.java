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

package de.metas.shippingnotification;

import de.metas.calendar.standard.YearAndCalendarId;
import de.metas.document.DocBaseType;
import de.metas.document.DocTypeId;
import de.metas.document.engine.DocStatus;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.inout.ShipmentScheduleId;
import de.metas.inoutcandidate.api.IShipmentScheduleEffectiveBL;
import de.metas.inoutcandidate.api.IShipmentSchedulePA;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.order.IOrderBL;
import de.metas.order.OrderAndLineId;
import de.metas.order.OrderId;
import de.metas.order.impl.DocTypeService;
import de.metas.organization.OrgId;
import de.metas.product.ProductId;
import de.metas.shippingnotification.model.I_M_Shipping_Notification;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.warehouse.LocatorId;
import org.compiere.model.I_C_Order;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingNotificationService
{
	private static final String REVERSE_INDICATOR = "^";
	private final ShippingNotificationRepository shippingNotificationRepository;
	private final DocTypeService docTypeService;
	private final IShipmentSchedulePA shipmentSchedulePA = Services.get(IShipmentSchedulePA.class);
	private final IOrderBL orderBL = Services.get(IOrderBL.class);
	private final IShipmentScheduleEffectiveBL shipmentScheduleEffectiveBL = Services.get(IShipmentScheduleEffectiveBL.class);
	private final IDocumentBL documentBL = Services.get(IDocumentBL.class);

	public void generateShippingNotificationAndPropagatePhysicalClearanceDate(
			@NonNull final OrderId orderId,
			@NonNull final Instant physicalClearanceDate)
	{
		final I_C_Order orderRecord = orderBL.getById(orderId);
		final OrgId orgId = OrgId.ofRepoId(orderRecord.getAD_Org_ID());
		final DocTypeId docTypeId = docTypeService.getDocTypeId(DocBaseType.ShippingNotification, null, orgId);

		final Collection<I_M_ShipmentSchedule> shipmentSchedules = shipmentSchedulePA.getByIds(shipmentSchedulePA.retrieveScheduleIdsByOrderId(orderId), I_M_ShipmentSchedule.class)
				.values();

		final ShippingNotification shippingNotification = ShippingNotification.builder()
				.orgId(orgId)
				.docTypeId(docTypeId)
				.bpartnerAndLocationId(orderBL.getShipToLocationId(orderRecord).getBpartnerLocationId())
				.contactId(orderBL.getShipToContactId(orderRecord).orElse(null))
				.orderId(OrderId.ofRepoId(orderRecord.getC_Order_ID()))
				.auctionId(orderRecord.getC_Auction_ID())
				.physicalClearanceDate(physicalClearanceDate)
				.locatorId(LocatorId.ofRepoId(orderRecord.getM_Warehouse_ID(), orderRecord.getM_Locator_ID()))
				.harvestingYearId(YearAndCalendarId.ofRepoId(orderRecord.getHarvesting_Year_ID(), orderRecord.getC_Harvesting_Calendar_ID()))
				.poReference(orderRecord.getPOReference())
				.description(orderRecord.getDescription())
				.docStatus(DocStatus.Drafted)
				.lines(shipmentSchedules.stream()
						.map(this::toShippingNotificationLine)
						.collect(Collectors.toList()))
				.build();

		shippingNotificationRepository.save(shippingNotification);

		completeIt(shippingNotification);

		shipmentSchedules.forEach(shipmentSchedule -> {
			shipmentSchedule.setPhysicalClearanceDate(Timestamp.from(shippingNotification.getPhysicalClearanceDate()));
			shipmentSchedulePA.save(shipmentSchedule);
		});

		orderRecord.setPhysicalClearanceDate(Timestamp.from(shippingNotification.getPhysicalClearanceDate()));
		orderBL.save(orderRecord);
	}

	public ShippingNotificationLine toShippingNotificationLine(@NonNull final I_M_ShipmentSchedule shipmentSchedule)
	{
		return ShippingNotificationLine.builder()
				.productId(ProductId.ofRepoId(shipmentSchedule.getM_Product_ID()))
				.asiId(AttributeSetInstanceId.ofRepoIdOrNone(shipmentSchedule.getM_AttributeSetInstance_ID()))
				.qty(shipmentScheduleEffectiveBL.getQtyToDeliver(shipmentSchedule))
				.shipmentScheduleId(ShipmentScheduleId.ofRepoId(shipmentSchedule.getM_ShipmentSchedule_ID()))
				.orderAndLineId(OrderAndLineId.ofRepoIds(shipmentSchedule.getC_Order_ID(), shipmentSchedule.getC_OrderLine_ID()))
				.build();
	}

	public void updateWhileSaving(
			@NonNull final I_M_Shipping_Notification record,
			@NonNull final Consumer<ShippingNotification> consumer)
	{
		shippingNotificationRepository.updateWhileSaving(record, consumer);
	}

	public void save(final ShippingNotification shippingNotification)
	{
		shippingNotificationRepository.save(shippingNotification);
	}

	public void completeIt(final ShippingNotification shippingNotification)
	{
		final I_M_Shipping_Notification shippingNotificationRecord = shippingNotificationRepository.saveAndGetRecord(shippingNotification);
		documentBL.processEx(shippingNotificationRecord, IDocument.ACTION_Complete, IDocument.STATUS_Completed);
	}

	public void reverseItNoSave(final ShippingNotification shippingNotification)
	{
		final I_M_Shipping_Notification reversalRecord = shippingNotificationRepository.saveAndGetRecord(shippingNotification.createReversal());
		reversalRecord.setReversal_ID(shippingNotification.getIdNotNull().getRepoId());
		reversalRecord.setDocumentNo(reversalRecord.getDocumentNo() + REVERSE_INDICATOR);
		reversalRecord.setDocStatus(DocStatus.Reversed.getCode());
		reversalRecord.setDocAction(IDocument.ACTION_None);
		shippingNotificationRepository.saveRecord(reversalRecord);

		shippingNotification.setReversalId(ShippingNotificationLoaderAndSaver.extractId(reversalRecord));
		shippingNotification.setDocStatus(DocStatus.Reversed);
	}

	public void reverseIfExistsShippingNotifications(@NonNull final OrderId orderId)
	{
		shippingNotificationRepository.updateByQuery(
				ShippingNotificationQuery.completedOrClosedByOrderId(orderId),
				this::reverseItNoSave);
	}

	public boolean hasCompletedOrClosedShippingNotifications(@NonNull final OrderId orderId)
	{
		return shippingNotificationRepository.anyMatch(ShippingNotificationQuery.completedOrClosedByOrderId(orderId));
	}

}
