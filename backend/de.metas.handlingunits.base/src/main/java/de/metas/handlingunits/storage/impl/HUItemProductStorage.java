package de.metas.handlingunits.storage.impl;

/*
 * #%L
 * de.metas.handlingunits.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.math.BigDecimal;
import java.util.Date;

import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_Product;

import de.metas.handlingunits.allocation.IAllocationRequest;
import de.metas.handlingunits.storage.IHUItemStorage;
import de.metas.handlingunits.storage.IProductStorage;
import de.metas.quantity.CapacityInterface;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMConversionBL;

/**
 * It's an {@link IHUItemStorage} but on given product level.
 *
 * @author tsa
 *
 */
/* package */class HUItemProductStorage implements IProductStorage
{
	private final IHUItemStorage itemStorage;
	private final I_M_Product product;
	private final I_C_UOM uom;
	private final Date date;

	public HUItemProductStorage(final IHUItemStorage itemStorage,
			final I_M_Product product,
			final I_C_UOM uom,
			final Date date)
	{
		super();

		Check.assumeNotNull(itemStorage, "itemStorage not null");
		this.itemStorage = itemStorage;

		Check.assumeNotNull(product, "product not null");
		this.product = product;

		Check.assumeNotNull(uom, "uom not null");
		this.uom = uom;

		Check.assumeNotNull(date, "date not null");
		this.date = date;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "["
				+ "\nProduct: " + product.getName()
				+ "\nQty: " + getQty()
				+ "\nCapacity: " + getTotalCapacity()
				+ "\nItem storage: " + itemStorage
				+ "]";

	}

	public CapacityInterface getTotalCapacity()
	{
		return itemStorage.getCapacity(product, uom, date);
	}

	@Override
	public I_M_Product getM_Product()
	{
		return product;
	}

	@Override
	public I_C_UOM getC_UOM()
	{
		return uom;
	}

	@Override
	public BigDecimal getQtyFree()
	{
		final CapacityInterface capacityAvailable = itemStorage.getAvailableCapacity(getM_Product(), getC_UOM(), date);
		if (capacityAvailable.isInfiniteCapacity())
		{
			return Quantity.QTY_INFINITE;
		}
		return capacityAvailable.getCapacityQty();
	}

	@Override
	public BigDecimal getQty()
	{
		final BigDecimal qty = itemStorage.getQty(getM_Product(), getC_UOM());
		return qty;
	}

	@Override
	public final BigDecimal getQty(final I_C_UOM uom)
	{
		final I_M_Product product = getM_Product();
		final BigDecimal qty = getQty();
		final I_C_UOM uomFrom = getC_UOM();
		return Services.get(IUOMConversionBL.class).convertQty(product, qty, uomFrom, uom);
	}

	@Override
	public BigDecimal getQtyCapacity()
	{
		final CapacityInterface capacityTotal = getTotalCapacity();
		return capacityTotal.getCapacityQty();
	}

	@Override
	public IAllocationRequest addQty(final IAllocationRequest request)
	{
		return itemStorage.requestQtyToAllocate(request);
	}

	@Override
	public IAllocationRequest removeQty(final IAllocationRequest request)
	{
		return itemStorage.requestQtyToDeallocate(request);
	}

	@Override
	public void markStaled()
	{
		// nothing, so far, itemStorage is always database coupled, no in memory values
	}

	@Override
	public boolean isEmpty()
	{
		return itemStorage.isEmpty(getM_Product());
	}

	@Override
	public boolean isAllowNegativeStorage()
	{
		return itemStorage.isAllowNegativeStorage();
	}
}
