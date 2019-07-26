
package org.eevolution.mrp.spi.impl.pporder;

import static org.adempiere.model.InterfaceWrapperHelper.load;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ClientId;
import org.compiere.model.I_M_Product;
import org.compiere.model.X_C_DocType;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.eevolution.api.IPPOrderBL;
import org.eevolution.api.IPPOrderDAO;
import org.eevolution.model.I_PP_Order;
import org.eevolution.model.I_PP_Product_Planning;
import org.eevolution.model.X_PP_MRP;
import org.eevolution.model.X_PP_Order;
import org.springframework.stereotype.Service;

import de.metas.document.IDocTypeDAO;
import de.metas.material.event.commons.ProductDescriptor;
import de.metas.material.event.pporder.PPOrder;
import de.metas.material.planning.pporder.PPOrderPojoConverter;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.adempiere.libero.libero
 * %%
 * Copyright (C) 2017 metas GmbH
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
public class PPOrderProducer
{
	public I_PP_Order createPPOrder(
			@NonNull final PPOrder ppOrderPojo,
			@NonNull final Date dateOrdered)
	{
		final IPPOrderBL ppOrderBL = Services.get(IPPOrderBL.class);
		final IPPOrderDAO ppOrdersRepo = Services.get(IPPOrderDAO.class);

		final I_PP_Product_Planning productPlanning = InterfaceWrapperHelper
				.create(Env.getCtx(), ppOrderPojo.getProductPlanningId(), I_PP_Product_Planning.class, ITrx.TRXNAME_None);

		//
		// Create PP Order
		final I_PP_Order ppOrderRecord = InterfaceWrapperHelper.newInstance(I_PP_Order.class);
		PPOrderPojoConverter.ATTR_PPORDER_REQUESTED_EVENT_GROUP_ID.setValue(ppOrderRecord, ppOrderPojo.getMaterialDispoGroupId());

		ppOrderRecord.setPP_Product_Planning(productPlanning);

		ppOrderRecord.setMRP_Generated(true);
		ppOrderRecord.setMRP_AllowCleanup(true);
		ppOrderRecord.setLine(10);

		//
		// Planning dimension
		ppOrderRecord.setAD_Org_ID(ppOrderPojo.getOrgId());
		ppOrderRecord.setS_Resource_ID(ppOrderPojo.getPlantId());
		ppOrderRecord.setM_Warehouse_ID(ppOrderPojo.getWarehouseId());
		ppOrderRecord.setPlanner_ID(productPlanning.getPlanner_ID());

		//
		// Document Type & Status
		final int docTypeId = getC_DocType_ID(OrgId.ofRepoId(ppOrderPojo.getOrgId()));

		ppOrderRecord.setC_DocTypeTarget_ID(docTypeId);
		ppOrderRecord.setC_DocType_ID(docTypeId);
		ppOrderRecord.setDocStatus(X_PP_Order.DOCSTATUS_Drafted);
		ppOrderRecord.setDocAction(X_PP_Order.DOCACTION_Complete);

		//
		// Product, ASI, UOM
		final ProductDescriptor productDescriptor = ppOrderPojo.getProductDescriptor();
		ppOrderRecord.setM_Product_ID(productDescriptor.getProductId());
		ppOrderRecord.setM_AttributeSetInstance_ID(productDescriptor.getAttributeSetInstanceId());

		//
		// BOM & Workflow
		ppOrderRecord.setPP_Product_BOM_ID(productPlanning.getPP_Product_BOM_ID());
		ppOrderRecord.setAD_Workflow_ID(productPlanning.getAD_Workflow_ID());

		//
		// Dates
		ppOrderRecord.setDateOrdered(new Timestamp(dateOrdered.getTime()));

		final Timestamp dateFinishSchedule = TimeUtil.asTimestamp(ppOrderPojo.getDatePromised());
		ppOrderRecord.setDatePromised(dateFinishSchedule);
		ppOrderRecord.setDateFinishSchedule(dateFinishSchedule);
		ppOrderRecord.setPreparationDate(dateFinishSchedule);

		final Timestamp dateStartSchedule = TimeUtil.asTimestamp(ppOrderPojo.getDateStartSchedule());
		ppOrderRecord.setDateStartSchedule(dateStartSchedule);

		// Qtys
		ppOrderBL.setQtyOrdered(ppOrderRecord, ppOrderPojo.getQtyRequired());

		ppOrderBL.setQtyEntered(ppOrderRecord, ppOrderPojo.getQtyRequired());
		ppOrderRecord.setC_UOM_ID(load(productDescriptor.getProductId(), I_M_Product.class).getC_UOM_ID());

		// QtyBatchSize : do not set it, let the MO to take it from workflow
		ppOrderRecord.setYield(BigDecimal.ZERO);

		ppOrderRecord.setScheduleType(X_PP_MRP.TYPEMRP_Demand);
		ppOrderRecord.setPriorityRule(X_PP_Order.PRIORITYRULE_Medium);

		//
		// Inherit values from MRP demand
		ppOrderRecord.setC_OrderLine_ID(ppOrderPojo.getOrderLineId());
		if (ppOrderPojo.getBPartnerId() > 0)
		{
			ppOrderRecord.setC_BPartner_ID(ppOrderPojo.getBPartnerId());
		}
		else if (ppOrderPojo.getOrderLineId() > 0)
		{
			ppOrderRecord.setC_BPartner_ID(ppOrderRecord.getC_OrderLine().getC_BPartner_ID());
		}

		//
		// Save the manufacturing order
		// I_PP_Order_BOM and I_PP_Order_BOMLines are created via a model interceptor
		ppOrdersRepo.save(ppOrderRecord);

		return ppOrderRecord;
	}

	private int getC_DocType_ID(final OrgId orgId)
	{
		final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
		final ClientId clientId = orgDAO.getClientIdByOrgId(orgId);

		final IDocTypeDAO docTypeDAO = Services.get(IDocTypeDAO.class);
		return docTypeDAO.getDocTypeId(Env.getCtx(), X_C_DocType.DOCBASETYPE_ManufacturingOrder, clientId.getRepoId(), orgId.getRepoId(), ITrx.TRXNAME_None);
	}
}
