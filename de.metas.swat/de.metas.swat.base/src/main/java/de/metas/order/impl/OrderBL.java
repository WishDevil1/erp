package de.metas.order.impl;

import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2015 metas GmbH
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.IQueryAggregateBuilder;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.persistence.ModelDynAttributeAccessor;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.MFreightCost;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_C_BP_Relation;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_Tax;
import org.compiere.model.I_M_PriceList;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.I_M_PricingSystem;
import org.compiere.model.X_C_DocType;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.compiere.util.Util;
import org.slf4j.Logger;

import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.IBPartnerBL;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.currency.CurrencyPrecision;
import de.metas.document.DocTypeId;
import de.metas.document.DocTypeQuery;
import de.metas.document.IDocTypeDAO;
import de.metas.freighcost.api.IFreightCostBL;
import de.metas.i18n.IModelTranslationMap;
import de.metas.i18n.ITranslatableString;
import de.metas.interfaces.I_C_BPartner;
import de.metas.interfaces.I_C_OrderLine;
import de.metas.lang.SOTrx;
import de.metas.logging.LogManager;
import de.metas.order.IOrderBL;
import de.metas.order.IOrderDAO;
import de.metas.order.IOrderLineBL;
import de.metas.order.IOrderPA;
import de.metas.pricing.PriceListId;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.exceptions.PriceListNotFoundException;
import de.metas.pricing.service.IPriceListBL;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMConversionBL;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.collections.CollectionUtils;
import lombok.NonNull;

public class OrderBL implements IOrderBL
{
	public static final String MSG_NO_FREIGHT_COST_DETAIL = "freightCost.Order.noFreightCostDetail";

	private static final transient Logger logger = LogManager.getLogger(OrderBL.class);

	@Override
	public void checkFreightCost(final I_C_Order order)
	{
		if (!order.isSOTrx())
		{
			logger.debug("{} is no SO", order);
			return;
		}

		final int bPartnerId = order.getC_BPartner_ID();
		final int bPartnerLocationId = order.getC_BPartner_Location_ID();
		final int shipperId = order.getM_Shipper_ID();

		if (bPartnerId <= 0 || bPartnerLocationId <= 0 || shipperId <= 0)
		{
			logger.debug("Can't check cause freight cost info is not yet complete for {}", order);
			return;
		}

		final IFreightCostBL freightCostBL = Services.get(IFreightCostBL.class);
		final de.metas.adempiere.model.I_C_Order o = InterfaceWrapperHelper.create(order, de.metas.adempiere.model.I_C_Order.class);
		if (freightCostBL.checkIfFree(o))
		{
			logger.debug("No freight cost for {}", order);
			return;
		}

		final Properties ctx = InterfaceWrapperHelper.getCtx(order);
		final String trxName = InterfaceWrapperHelper.getTrxName(order);
		final MFreightCost freightCost = MFreightCost.retrieveFor(ctx,
				bPartnerId,
				bPartnerLocationId,
				shipperId,
				order.getAD_Org_ID(),
				order.getDateOrdered(),
				trxName);
		if (freightCost == null)
		{
			throw new AdempiereException("@" + MSG_NO_FREIGHT_COST_DETAIL + "@");
		}
	}

	@Override
	public void setM_PricingSystem_ID(final I_C_Order order, final boolean overridePricingSystemAndDontThrowExIfNotFound)
	{
		final int previousPricingSystemId = order.getM_PricingSystem_ID();

		final boolean overridePricingSystem = overridePricingSystemAndDontThrowExIfNotFound;
		if (overridePricingSystem || previousPricingSystemId <= 0)
		{
			final BillBPartnerAndShipToLocation bpartnerAndLocation = extractPriceListBPartnerAndLocation(order);
			final BPartnerId bpartnerId = BPartnerId.ofRepoIdOrNull(bpartnerAndLocation.getBill_BPartner_ID());
			if (bpartnerId == null)
			{
				logger.debug("Order {} has no C_BPartner_ID. Doing nothing", order);
				return;
			}

			final IBPartnerDAO bpartnersRepo = Services.get(IBPartnerDAO.class);
			final SOTrx soTrx = SOTrx.ofBoolean(order.isSOTrx());
			final PricingSystemId pricingSysId = bpartnersRepo.retrievePricingSystemId(bpartnerId, soTrx);

			final boolean throwExIfNotFound = !overridePricingSystemAndDontThrowExIfNotFound;
			if (pricingSysId == null && throwExIfNotFound)
			{
				final String bpartnerName = Services.get(IBPartnerBL.class).getBPartnerValueAndName(bpartnerId);
				Check.errorIf(true, "Unable to find pricing system for BPartner {}_{}; SOTrx={}", bpartnerName, soTrx);
			}

			order.setM_PricingSystem_ID(PricingSystemId.getRepoId(pricingSysId));
		}

		//
		// Update the M_PriceList_ID only if:
		// * overridePricingSystem is true => this is also covering the case when pricing system was not changed but for some reason the price list could be a different one (Date changed etc)
		// * pricing system really changed => we need to set to correct price list
		// Cases we want to avoid:
		// * overridePriceSystem is false and M_PricingSystem_ID was not changed: in this case we shall NOT update the price list because it might be that we were called for a completed Order and we don't want to change the data.
		if (overridePricingSystemAndDontThrowExIfNotFound || previousPricingSystemId != order.getM_PricingSystem_ID()
				|| order.getM_PriceList_ID() <= 0 // gh #936: attempt to set the pricelist, if we don't have it yet (i don't understand the error, but this might solve it. going to try it out)
		)
		{
			setPriceList(order);
		}
	}

	@Override
	public void setPriceList(final I_C_Order order)
	{
		final PricingSystemId pricingSystemId = PricingSystemId.ofRepoIdOrNull(order.getM_PricingSystem_ID());
		if (pricingSystemId == null)
		{
			logger.debug("order {} has no M_PricingSystem_ID. Doing nothing", order);
			return;
		}

		final BillBPartnerAndShipToLocation bpartnerAndLocation = extractPriceListBPartnerAndLocation(order);
		if (bpartnerAndLocation.getShip_BPartner_Location_ID() <= 0)
		{
			logger.debug("order {} has no C_BPartner_Location_ID. Doing nothing", order);
			return;
		}

		final SOTrx soTrx = SOTrx.ofBoolean(order.isSOTrx());
		final I_M_PriceList priceList = retrievePriceListOrNull(pricingSystemId, bpartnerAndLocation, soTrx);
		if (priceList == null)
		{
			// Fail if no price list found
			final String pricingSystemName = Services.get(IPriceListDAO.class).getPricingSystemName(pricingSystemId);
			throw new PriceListNotFoundException(pricingSystemName, soTrx);
		}

		order.setM_PriceList(priceList);
	}

	@Override
	public void checkForPriceList(final I_C_Order order)
	{
		final PricingSystemId pricingSystemId = PricingSystemId.ofRepoIdOrNull(order.getM_PricingSystem_ID());
		if (pricingSystemId == null)
		{
			return;
		}

		final BillBPartnerAndShipToLocation bpartnerAndLocation = extractPriceListBPartnerAndLocation(order);
		if (bpartnerAndLocation.getShip_BPartner_Location_ID() <= 0)
		{
			return;
		}

		final SOTrx soTrx = SOTrx.ofBoolean(order.isSOTrx());
		final I_M_PriceList pl = retrievePriceListOrNull(pricingSystemId, bpartnerAndLocation, soTrx);
		if (pl == null)
		{
			final I_M_PricingSystem pricingSystem = order.getM_PricingSystem();
			final String pricingSystemName = pricingSystem == null ? "-" : pricingSystem.getName();
			throw new PriceListNotFoundException(pricingSystemName, soTrx);
		}
	}

	@Override
	public PriceListId retrievePriceListId(final I_C_Order order, final PricingSystemId pricingSystemIdOverride)
	{
		final PriceListId orderPriceListId = PriceListId.ofRepoIdOrNull(order.getM_PriceList_ID());
		if (orderPriceListId != null)
		{
			if (pricingSystemIdOverride != null)
			{
				final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);
				final I_M_PriceList priceList = priceListDAO.getById(orderPriceListId);
				if (priceList.getM_PricingSystem_ID() == pricingSystemIdOverride.getRepoId())
				{
					return orderPriceListId;
				}
			}
			else
			{
				return orderPriceListId;
			}
		}

		final PricingSystemId pricingSystemId = pricingSystemIdOverride != null ? pricingSystemIdOverride : PricingSystemId.ofRepoIdOrNull(order.getM_PricingSystem_ID());
		final BillBPartnerAndShipToLocation bpartnerAndLocation = extractPriceListBPartnerAndLocation(order);
		final SOTrx soTrx = SOTrx.ofBoolean(order.isSOTrx());
		final I_M_PriceList priceList = retrievePriceListOrNull(pricingSystemId, bpartnerAndLocation, soTrx);
		return priceList != null ? PriceListId.ofRepoId(priceList.getM_PriceList_ID()) : null;
	}

	private I_M_PriceList retrievePriceListOrNull(final PricingSystemId pricingSystemId, final BillBPartnerAndShipToLocation bpartnerAndLocation, @NonNull final SOTrx soTrx)
	{
		final int shipBPLocationId = bpartnerAndLocation.getShip_BPartner_Location_ID();
		if (shipBPLocationId <= 0)
		{
			return null;
		}

		final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);
		final I_C_BPartner_Location shipBPLocation = loadOutOfTrx(shipBPLocationId, I_C_BPartner_Location.class);
		final I_M_PriceList priceList = priceListDAO.retrievePriceListByPricingSyst(pricingSystemId, shipBPLocation, soTrx);
		return priceList;
	}

	private BillBPartnerAndShipToLocation extractPriceListBPartnerAndLocation(final I_C_Order order)
	{
		final org.compiere.model.I_C_BPartner_Location shipToLocation = getShipToLocation(order);
		final int shipBPLocationId = shipToLocation != null ? shipToLocation.getC_BPartner_Location_ID() : -1;

		final int bpartnerId = shipToLocation != null ? shipToLocation.getC_BPartner_ID() : order.getC_BPartner_ID();

		return new BillBPartnerAndShipToLocation(bpartnerId, shipBPLocationId);
	}

	@lombok.Value
	private static class BillBPartnerAndShipToLocation
	{
		int Bill_BPartner_ID;
		int Ship_BPartner_Location_ID;
	}

	@Override
	public boolean updateFreightAmt(final Properties ctx, final I_C_Order order, final String trxName)
	{
		final IFreightCostBL freightCostBL = Services.get(IFreightCostBL.class);
		final de.metas.adempiere.model.I_C_Order o = InterfaceWrapperHelper.create(order, de.metas.adempiere.model.I_C_Order.class);
		final BigDecimal freightCostAmt = freightCostBL.computeFreightCostForOrder(ctx, o, trxName);

		if (order.getFreightAmt().compareTo(freightCostAmt) != 0)
		{
			order.setFreightAmt(freightCostAmt);
			return true;
		}
		return false;
	}

	@Override
	public boolean setBill_User_ID(final org.compiere.model.I_C_Order order)
	{
		// First try: if order and bill partner and location are the same, and the contact is set
		// we can use the same contact
		if (order.getC_BPartner_ID() == order.getBill_BPartner_ID()
				&& order.getC_BPartner_Location_ID() == order.getBill_Location_ID()
				&& order.getAD_User_ID() > 0)
		{
			order.setBill_User_ID(order.getAD_User_ID());
			return true;
		}

		final IBPartnerBL bpartnerService = Services.get(IBPartnerBL.class);
		final I_AD_User billContact;
		// Case: Bill Location is set, we can use it to retrieve the contact for that location
		if (order.getBill_Location_ID() > 0)
		{
			final I_C_BPartner_Location billLocation = InterfaceWrapperHelper.create(order.getBill_Location(), I_C_BPartner_Location.class);
			billContact = bpartnerService.retrieveUserForLoc(billLocation);
		}
		// Case: Bill Location is NOT set, we search for default bill contact
		else
		{
			final Properties ctx = InterfaceWrapperHelper.getCtx(order);
			final String trxName = InterfaceWrapperHelper.getTrxName(order);
			final int bPartnerId = order.getBill_BPartner_ID();
			billContact = bpartnerService.retrieveBillContact(ctx, bPartnerId, trxName);
		}

		if (billContact == null)
		{
			return false;
		}

		order.setBill_User_ID(billContact.getAD_User_ID());
		return true;
	}

	@Override
	public void setDocTypeTargetId(final I_C_Order order)
	{
		if (order.isSOTrx())
		{
			setDocTypeTargetId(order, X_C_DocType.DOCSUBTYPE_StandardOrder);
			return;
		}
		else
		{
			final DocTypeQuery docTypeQuery = DocTypeQuery.builder()
					.docBaseType(X_C_DocType.DOCBASETYPE_PurchaseOrder)
					.docSubType(DocTypeQuery.DOCSUBTYPE_Any)
					.adClientId(order.getAD_Client_ID())
					.adOrgId(order.getAD_Org_ID())
					.build();
			final IDocTypeDAO docTypeDAO = Services.get(IDocTypeDAO.class);

			final int docTypeId = DocTypeId.toRepoId(docTypeDAO.getDocTypeIdOrNull(docTypeQuery));
			if (docTypeId <= 0)
			{
				logger.error("No POO found for {}", docTypeQuery);
			}
			else
			{
				logger.debug("(PO) - {}", docTypeId);
				setDocTypeTargetIdAndUpdateDescription(order, docTypeId);
			}
		}
	}

	@Override
	public void setDocTypeTargetId(final I_C_Order order, final String soDocSubType)
	{
		final DocTypeQuery docTypeQuery = DocTypeQuery.builder()
				.docBaseType(X_C_DocType.DOCBASETYPE_SalesOrder)
				.docSubType(soDocSubType)
				.adClientId(order.getAD_Client_ID())
				.adOrgId(order.getAD_Org_ID())
				.build();
		final IDocTypeDAO docTypeDAO = Services.get(IDocTypeDAO.class);

		final int docTypeId = DocTypeId.toRepoId(docTypeDAO.getDocTypeIdOrNull(docTypeQuery));
		if (docTypeId <= 0)
		{
			logger.error("Not found for {}", docTypeQuery);
		}
		else
		{
			logger.debug("(SO) - {}", soDocSubType);
			setDocTypeTargetIdAndUpdateDescription(order, docTypeId);
			order.setIsSOTrx(true);
		}
	}

	@Override
	public void setDocTypeTargetIdAndUpdateDescription(final I_C_Order order, final int docTypeId)
	{
		order.setC_DocTypeTarget_ID(docTypeId);
		updateDescriptionFromDocTypeTargetId(order);
	}

	@Override
	public void updateDescriptionFromDocTypeTargetId(final I_C_Order order)
	{
		final int docTypeId = order.getC_DocTypeTarget_ID();
		if (docTypeId <= 0)
		{
			return;
		}

		final int bpartnerId = order.getC_BPartner_ID();

		if (bpartnerId <= 0)
		{
			return;
		}

		final org.compiere.model.I_C_DocType docType = Services.get(IDocTypeDAO.class).getById(docTypeId);

		if (docType == null)
		{
			return;
		}

		if (!docType.isCopyDescriptionToDocument())
		{
			return;
		}

		final String adLanguage = Util.coalesce(order.getC_BPartner().getAD_Language(), Env.getAD_Language());

		final IModelTranslationMap docTypeTrl = InterfaceWrapperHelper.getModelTranslationMap(docType);
		final ITranslatableString description = docTypeTrl.getColumnTrl(I_C_DocType.COLUMNNAME_Description, docType.getDescription());
		final ITranslatableString documentNote = docTypeTrl.getColumnTrl(I_C_DocType.COLUMNNAME_DocumentNote, docType.getDocumentNote());

		order.setDescription(description.translate(adLanguage));
		order.setDescriptionBottom(documentNote.translate(adLanguage));
	}

	@Override
	public void updateAddresses(final org.compiere.model.I_C_Order order)
	{
		final de.metas.adempiere.model.I_C_Order orderEx = InterfaceWrapperHelper.create(order, de.metas.adempiere.model.I_C_Order.class);

		for (final I_C_OrderLine line : Services.get(IOrderDAO.class).retrieveOrderLines(orderEx))
		{
			if (orderEx.isDropShip() && orderEx.getDropShip_BPartner_ID() > 0)
			{
				line.setC_BPartner_ID(orderEx.getDropShip_BPartner_ID());
			}
			else
			{
				line.setC_BPartner_ID(orderEx.getC_BPartner_ID());
			}

			if (orderEx.isDropShip() && orderEx.getDropShip_Location_ID() > 0)
			{
				line.setC_BPartner_Location_ID(orderEx.getDropShip_Location_ID());
				line.setBPartnerAddress(orderEx.getDeliveryToAddress());

			}
			else
			{
				line.setC_BPartner_Location_ID(orderEx.getC_BPartner_Location_ID());
				line.setBPartnerAddress(orderEx.getBPartnerAddress());
			}

			if (orderEx.isDropShip() && orderEx.getDropShip_User_ID() > 0)
			{
				line.setAD_User_ID(orderEx.getDropShip_User_ID());
			}
			else
			{
				line.setAD_User_ID(orderEx.getAD_User_ID());
			}

			InterfaceWrapperHelper.save(line);
		}
	}

	@Override
	public String evaluateOrderDeliveryViaRule(I_C_Order order)
	{
		if (Check.isEmpty(order.getDeliveryViaRule(), true))
		{
			return findDeliveryViaRule(order);
		}

		return order.getDeliveryViaRule();
	}

	/**
	 * Retrieve the deliveryViaRule based on partner form order
	 *
	 * @param order
	 * @return
	 */
	private String findDeliveryViaRule(final I_C_Order order)
	{

		if (order.getC_BPartner_ID() <= 0)
		{
			return null;
		}

		final I_C_BPartner bp = InterfaceWrapperHelper.create(order.getC_BPartner(), I_C_BPartner.class);

		final String deliveryViaRule;

		if (order.isSOTrx())
		{
			deliveryViaRule = bp.getDeliveryViaRule();
		}
		else
		{
			deliveryViaRule = bp.getPO_DeliveryViaRule();
		}

		if (Check.isEmpty(deliveryViaRule, true))
		{
			return null;
		}

		return deliveryViaRule;
	}

	@Override
	public I_M_PriceList_Version getPriceListVersion(final I_C_Order order)
	{
		if (order == null)
		{
			return null;
		}
		final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);

		final LocalDate orderDate;
		if (order.getDatePromised() != null)
		{
			orderDate = TimeUtil.asLocalDate(order.getDatePromised());
		}
		else
		{
			orderDate = TimeUtil.asLocalDate(order.getDateOrdered());
		}

		final Boolean processedPLVFiltering = null; // task 09533: the user doesn't know about PLV's processed flag, so we can't filter by it
		final I_M_PriceList_Version plv = priceListDAO.retrievePriceListVersionOrNull(order.getM_PriceList(), orderDate, processedPLVFiltering);
		return plv;
	}

	@Override
	public void setBPartner(final org.compiere.model.I_C_Order order, final org.compiere.model.I_C_BPartner bp)
	{
		// FIXME: keep in sync / merge with org.compiere.model.MOrder.setBPartner(MBPartner)
		if (bp == null)
		{
			return;
		}

		order.setC_BPartner_ID(bp.getC_BPartner_ID());

		final boolean isSOTrx = order.isSOTrx();
		//
		// Defaults Payment Term
		final int paymentTermId;
		if (isSOTrx)
		{
			paymentTermId = bp.getC_PaymentTerm_ID();
		}
		else
		{
			paymentTermId = bp.getPO_PaymentTerm_ID();
		}
		if (paymentTermId > 0)
		{
			order.setC_PaymentTerm_ID(paymentTermId);
		}

		//
		// Default Price List
		final int priceListId;
		if (isSOTrx)
		{
			priceListId = bp.getM_PriceList_ID();
		}
		else
		{
			priceListId = bp.getPO_PriceList_ID();
		}
		if (priceListId > 0)
		{
			order.setM_PriceList_ID(priceListId);
		}

		//
		// Default Delivery
		final String deliveryRule = bp.getDeliveryRule();
		if (deliveryRule != null)
		{
			order.setDeliveryRule(deliveryRule);
		}

		//
		// Default Delivery Via Rule
		final String deliveryViaRule;
		if (isSOTrx)
		{
			deliveryViaRule = bp.getDeliveryViaRule();
		}
		else
		{
			deliveryViaRule = bp.getPO_DeliveryViaRule();
		}
		if (deliveryViaRule != null)
		{
			order.setDeliveryViaRule(deliveryViaRule);
		}

		//
		// Default Invoice/Payment Rule
		final String invoiceRule = bp.getInvoiceRule();
		if (invoiceRule != null)
		{
			order.setInvoiceRule(invoiceRule);
		}
		final String paymentRule = bp.getPaymentRule();
		if (paymentRule != null)
		{
			order.setPaymentRule(paymentRule);
		}

		//
		// Sales Rep
		final int salesRepId = bp.getSalesRep_ID();
		if (salesRepId > 0)
		{
			order.setSalesRep_ID(salesRepId);
		}

		setBPLocation(order, bp);

		// #1056
		// find if the partner doesn't have a bill relation with another partner. In such a case, that partner will have priority.
		setBillLocation(order);

		final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);

		// Set Contact
		// final List<I_AD_User> contacts = bPartnerDAO.retrieveContacts(bp.getC_BPartner_ID(), false, null);
		// if (contacts != null && contacts.size() == 1)
		// {
		// order.setAD_User_ID(contacts.get(0).getAD_User_ID());
		// }

		// 08812
		// set the fit contact

		final Properties ctx = InterfaceWrapperHelper.getCtx(order);
		final int bpartnerId = bp.getC_BPartner_ID();

		// keep the trxName null, as it was before
		final String trxName = ITrx.TRXNAME_None;
		final I_AD_User contact = bPartnerDAO.retrieveContact(ctx, bpartnerId, isSOTrx, trxName);

		// keep the functionality as it was. Do not set null user
		if (contact != null)
		{
			order.setAD_User(contact);
		}
	}

	@Override
	public void setBPLocation(final org.compiere.model.I_C_Order order, final org.compiere.model.I_C_BPartner bp)
	{
		final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);

		final List<I_C_BPartner_Location> locations = bPartnerDAO.retrieveBPartnerLocations(bp);

		// Set Locations
		final List<I_C_BPartner_Location> shipLocations = new ArrayList<>();
		boolean foundLoc = false;
		for (final I_C_BPartner_Location loc : locations)
		{
			if (loc.isShipTo() && loc.isActive())
			{
				shipLocations.add(loc);
			}

			final org.compiere.model.I_C_BPartner_Location bpLoc = InterfaceWrapperHelper.create(loc, org.compiere.model.I_C_BPartner_Location.class);
			if (bpLoc.isShipToDefault())
			{
				order.setC_BPartner_Location_ID(bpLoc.getC_BPartner_Location_ID());
				foundLoc = true;
			}
		}

		// set first ship location if is not set
		if (!foundLoc)
		{
			if (!shipLocations.isEmpty())
			{
				order.setC_BPartner_Location_ID(shipLocations.get(0).getC_BPartner_Location_ID());
			}
			else if (!locations.isEmpty())
			{
				// set to first
				if (order.getC_BPartner_Location_ID() == 0)
				{
					order.setC_BPartner_Location_ID(locations.get(0)
							.getC_BPartner_Location_ID());
				}
			}
		}

		if (!foundLoc)
		{
			logger.error("MOrder.setBPartner - Has no Ship To Address: {}", bp);
		}
	}

	/**
	 *
	 *
	 * @param order
	 */
	@Override
	public boolean setBillLocation(final I_C_Order order)
	{
		if (order.getC_BPartner() == null)
		{
			return false; // nothing to be done
		}

		//
		// First, try to set the bill location from the C_BPartner
		setBillLocation(order, order.getC_BPartner(), null);
		if (order.getBill_Location_ID() > 0)
		{
			return true; // found it
		}

		final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);

		//
		// Search in relation and try to find an adequate Bill Partner if the bill location could not be found
		final I_C_BP_Relation billPartnerRelation = bPartnerDAO.retrieveBillBPartnerRelationFirstEncountered(order,
				order.getC_BPartner(),
				InterfaceWrapperHelper.create(order.getC_BPartner_Location(), org.compiere.model.I_C_BPartner_Location.class));

		if (billPartnerRelation == null)
		{
			return false; // didn't find it
		}

		final I_C_BPartner partnerToUse = InterfaceWrapperHelper.create(billPartnerRelation.getC_BPartnerRelation(), I_C_BPartner.class);
		final I_C_BPartner_Location defaultLocation = InterfaceWrapperHelper.create(billPartnerRelation.getC_BPartnerRelation_Location(), I_C_BPartner_Location.class);
		setBillLocation(order, partnerToUse, defaultLocation);
		return true; // found it
	}

	private boolean setBillLocation(
			final org.compiere.model.I_C_Order order,
			final org.compiere.model.I_C_BPartner billBPartner,
			final org.compiere.model.I_C_BPartner_Location defaultBillLocation)
	{
		if (billBPartner == null)
		{
			return false;
		}

		int billLocationIdToUse = 0;
		boolean foundLoc = false;

		if (defaultBillLocation != null && defaultBillLocation.getC_BPartner_Location_ID() > 0)
		{
			billLocationIdToUse = defaultBillLocation.getC_BPartner_Location_ID();
			foundLoc = true;
		}

		if (!foundLoc)
		{
			final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);
			final List<I_C_BPartner_Location> locations = bPartnerDAO.retrieveBPartnerLocations(billBPartner);

			// Set Locations
			final List<I_C_BPartner_Location> invLocations = new ArrayList<>();
			for (final I_C_BPartner_Location loc : locations)
			{
				if (foundLoc)
				{
					break;
				}

				if (loc.isBillToDefault())
				{
					billLocationIdToUse = loc.getC_BPartner_Location_ID();
					foundLoc = true;
				}

				if (loc.isBillTo())
				{
					invLocations.add(loc);
				}
			}

			// set first invoice location if is not set
			if (!foundLoc)
			{
				if (!invLocations.isEmpty())
				{
					final I_C_BPartner_Location firstInvLocation = invLocations.get(0);

					billLocationIdToUse = firstInvLocation.getC_BPartner_Location_ID();
				}
				else if (!locations.isEmpty())
				{
					// set to first
					if (order.getBill_Location_ID() == 0)
					{
						final I_C_BPartner_Location firstRetrievedLocation = locations.get(0);
						billLocationIdToUse = firstRetrievedLocation.getC_BPartner_Location_ID();
					}
				}
			}
		}

		order.setBill_BPartner_ID(billBPartner.getC_BPartner_ID());
		order.setBill_Location_ID(billLocationIdToUse);

		if (billLocationIdToUse > 0)
		{
			foundLoc = true;
		}

		// 07138
		// We don't need a SEVERE log for this. even though the partner doesn't have a bill to address
		// there are still fallbacks on the relation, etc
		// In case no address is found, the caller is responsible for deciding what to to (e.g. show a user error).

		if (!foundLoc)
		{
			logger.debug("MOrder.setBPartner - Has no Bill To Address: " + billBPartner);
		}
		return foundLoc;
	}

	@Override
	public CurrencyPrecision getPricePrecision(final I_C_Order order)
	{
		final PriceListId priceListId = PriceListId.ofRepoIdOrNull(order.getM_PriceList_ID());
		return priceListId != null
				? Services.get(IPriceListBL.class).getPricePrecision(priceListId)
				: CurrencyPrecision.TWO;
	}
	
	@Override
	public CurrencyPrecision getAmountPrecision(final I_C_Order order)
	{
		final PriceListId priceListId = PriceListId.ofRepoIdOrNull(order.getM_PriceList_ID());
		return priceListId != null
				? Services.get(IPriceListBL.class).getAmountPrecision(priceListId)
				: CurrencyPrecision.TWO;
	}

	@Override
	public CurrencyPrecision getTaxPrecision(final I_C_Order order)
	{
		final PriceListId priceListId = PriceListId.ofRepoIdOrNull(order.getM_PriceList_ID());
		return priceListId != null
				? Services.get(IPriceListBL.class).getTaxPrecision(priceListId)
				: CurrencyPrecision.TWO;
	}

	@Override
	public boolean isTaxIncluded(final org.compiere.model.I_C_Order order, I_C_Tax tax)
	{
		Check.assumeNotNull(order, "order not null");

		if (tax != null && tax.isWholeTax())
		{
			return true;
		}

		return order.isTaxIncluded();
	}

	@Override
	public void closeLine(final org.compiere.model.I_C_OrderLine orderLine)
	{
		Check.assumeNotNull(orderLine, "orderLine not null");

		if (orderLine.getQtyDelivered().compareTo(orderLine.getQtyOrdered()) >= 0) // they delivered at least the ordered qty => nothing to do
		{
			return; // Do nothing
		}

		orderLine.setQtyOrdered(orderLine.getQtyDelivered());
		InterfaceWrapperHelper.save(orderLine); // saving, just to be on the save side in case reserveStock() does a refresh or sth

		final I_C_Order order = orderLine.getC_Order();
		Services.get(IOrderPA.class).reserveStock(order, orderLine); // FIXME: move reserveStock method to an orderBL service
	}

	@Override
	public void reopenLine(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
		
		//
		// Calculate QtyOrdered as QtyEntered converted to stocking UOM
		final ProductId productId = ProductId.ofRepoId(orderLine.getM_Product_ID());
		final Quantity qtyEntered = Services.get(IOrderLineBL.class).getQtyEntered(orderLine);
		final Quantity qtyOrdered = uomConversionBL.convertToProductUOM(qtyEntered, productId);

		//
		// Set QtyOrdered
		orderLine.setQtyOrdered(qtyOrdered.getAsBigDecimal());
		InterfaceWrapperHelper.save(orderLine); // saving, just to be on the save side in case reserveStock() does a refresh or sth

		//
		// Update qty reservation
		final I_C_Order order = orderLine.getC_Order();
		final IOrderPA orderPA = Services.get(IOrderPA.class);
		orderPA.reserveStock(order, orderLine);
	}

	@Override
	public org.compiere.model.I_C_BPartner getShipToPartner(final I_C_Order order)
	{
		if (order.isDropShip())
		{
			// check for isDropShip to avoid returning a "stale" dropship-partner
			return order.getDropShip_BPartner_ID() > 0 ? order.getDropShip_BPartner() : order.getC_BPartner();
		}
		return order.getC_BPartner();
	}

	@Override
	public org.compiere.model.I_C_BPartner_Location getShipToLocation(final I_C_Order order)
	{
		if (order.isDropShip())
		{
			// check for isDropShip to avoid returning a "stale" dropship-partner
			return order.getDropShip_Location_ID() > 0 ? order.getDropShip_Location() : order.getC_BPartner_Location();
		}
		return order.getC_BPartner_Location();
	}

	@Override
	public BPartnerLocationId getShipToLocationId(final I_C_Order order)
	{
		if (order.isDropShip() && order.getDropShip_BPartner_ID() > 0 && order.getDropShip_Location_ID() > 0)
		{
			return BPartnerLocationId.ofRepoId(order.getDropShip_BPartner_ID(), order.getDropShip_Location_ID());
		}

		return BPartnerLocationId.ofRepoId(order.getC_BPartner_ID(), order.getC_BPartner_Location_ID());
	}

	@Override
	public org.compiere.model.I_AD_User getShipToUser(final I_C_Order order)
	{
		if (order.isDropShip())
		{
			// check for isDropShip to avoid returning a "stale" dropship-partner
			return order.getDropShip_User_ID() > 0 ? order.getDropShip_User() : order.getAD_User();
		}
		return order.getAD_User();
	}

	@Override
	public org.compiere.model.I_C_BPartner_Location getBillToLocation(I_C_Order order)
	{
		return order.getBill_Location_ID() > 0 ? order.getBill_Location() : order.getC_BPartner_Location();
	}

	private static final ModelDynAttributeAccessor<org.compiere.model.I_C_Order, BigDecimal> DYNATTR_QtyInvoicedSum = new ModelDynAttributeAccessor<>("QtyInvoicedSum", BigDecimal.class);
	private static final ModelDynAttributeAccessor<org.compiere.model.I_C_Order, BigDecimal> DYNATTR_QtyDeliveredSum = new ModelDynAttributeAccessor<>("QtyDeliveredSum", BigDecimal.class);
	private static final ModelDynAttributeAccessor<org.compiere.model.I_C_Order, BigDecimal> DYNATTR_QtyOrderedSum = new ModelDynAttributeAccessor<>("QtyOrderedSum", BigDecimal.class);

	@Override
	public void updateOrderQtySums(final org.compiere.model.I_C_Order order)
	{
		final IQueryBL queryBL = Services.get(IQueryBL.class);

		final IQueryAggregateBuilder<org.compiere.model.I_C_OrderLine, org.compiere.model.I_C_Order> aggregateOnOrder = queryBL
				.createQueryBuilder(org.compiere.model.I_C_OrderLine.class, order)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(org.compiere.model.I_C_OrderLine.COLUMNNAME_C_Order_ID, order.getC_Order_ID())
				.addEqualsFilter(I_C_OrderLine.COLUMNNAME_IsPackagingMaterial, false)
				.aggregateOnColumn(org.compiere.model.I_C_OrderLine.COLUMN_C_Order_ID);

		aggregateOnOrder.sum(DYNATTR_QtyInvoicedSum, org.compiere.model.I_C_OrderLine.COLUMN_QtyInvoiced);
		aggregateOnOrder.sum(DYNATTR_QtyDeliveredSum, org.compiere.model.I_C_OrderLine.COLUMN_QtyDelivered);
		aggregateOnOrder.sum(DYNATTR_QtyOrderedSum, org.compiere.model.I_C_OrderLine.COLUMN_QtyOrdered);

		final de.metas.order.model.I_C_Order fOrder = InterfaceWrapperHelper.create(order, de.metas.order.model.I_C_Order.class);

		final List<org.compiere.model.I_C_Order> queryiedOrders = aggregateOnOrder.aggregate();
		if (queryiedOrders.isEmpty())
		{
			// gh #1855: cover the case that the order has no lines or just packing lines.
			fOrder.setQtyInvoiced(BigDecimal.ZERO);
			fOrder.setQtyMoved(BigDecimal.ZERO);
			fOrder.setQtyOrdered(BigDecimal.ZERO);
		}
		else
		{
			final org.compiere.model.I_C_Order queriedOrder = CollectionUtils.singleElement(queryiedOrders);

			fOrder.setQtyInvoiced(DYNATTR_QtyInvoicedSum.getValue(queriedOrder));
			fOrder.setQtyMoved(DYNATTR_QtyDeliveredSum.getValue(queriedOrder));
			fOrder.setQtyOrdered(DYNATTR_QtyOrderedSum.getValue(queriedOrder));
		}
		InterfaceWrapperHelper.save(fOrder);
	}

	@Override
	public boolean isQuotation(@NonNull final I_C_Order order)
	{
		final boolean isSOTrx = order.isSOTrx();

		if (!isSOTrx)
		{
			// purchase orders are not quotations
			return false;
		}

		final I_C_DocType docType = Util.coalesceSuppliers(() -> order.getC_DocType(), () -> order.getC_DocTypeTarget());
		if (docType == null)
		{
			return false;
		}

		if (!(X_C_DocType.DOCBASETYPE_SalesOrder.equals(docType.getDocBaseType())))
		{
			// Quotation must be of BaseType Sales Order
			return false;
		}

		final String docSubType = docType.getDocSubType();

		if (docSubType == null)
		{
			// Quotation must have a docSubType
			return false;
		}

		return (docSubType.equals(X_C_DocType.DOCSUBTYPE_Proposal) || docSubType.equals(X_C_DocType.DOCSUBTYPE_Quotation));
	}
}
