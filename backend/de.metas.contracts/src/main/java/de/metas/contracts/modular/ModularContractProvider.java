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

package de.metas.contracts.modular;

import de.metas.calendar.standard.CalendarId;
import de.metas.calendar.standard.YearId;
import de.metas.contracts.FlatrateTermId;
import de.metas.contracts.FlatrateTermRequest.ModularFlatrateTermQuery;
import de.metas.contracts.IFlatrateBL;
import de.metas.contracts.flatrate.TypeConditions;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.lang.SOTrx;
import de.metas.order.IOrderBL;
import de.metas.order.OrderAndLineId;
import de.metas.product.ProductId;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class ModularContractProvider
{
	private final IOrderBL orderBL = Services.get(IOrderBL.class);
	private final IFlatrateBL flatrateBL = Services.get(IFlatrateBL.class);
	private final IWarehouseBL warehouseBL = Services.get(IWarehouseBL.class);

	@NonNull
	public Stream<FlatrateTermId> streamSalesContractsForSalesOrderLine(@NonNull final OrderAndLineId orderAndLineId)
	{
		final I_C_Order order = orderBL.getById(orderAndLineId.getOrderId());
		if (!order.isSOTrx())
		{
			return Stream.empty();
		}

		return flatrateBL.getByOrderLineId(orderAndLineId.getOrderLineId(), TypeConditions.MODULAR_CONTRACT)
				.map(flatrateTerm -> FlatrateTermId.ofRepoId(flatrateTerm.getC_Flatrate_Term_ID()))
				.stream();
	}

	@NonNull
	public Stream<FlatrateTermId> streamPurchaseContractsForSalesOrderLine(@NonNull final OrderAndLineId orderAndLineId)
	{
		final I_C_Order order = orderBL.getById(orderAndLineId.getOrderId());
		final I_C_OrderLine orderLine = orderBL.getLineById(orderAndLineId);

		final WarehouseId warehouseId = WarehouseId.ofRepoId(order.getM_Warehouse_ID());

		final YearId harvestingYearId = YearId.ofRepoId(order.getHarvesting_Year_ID());

		final CalendarId harvestingCalendarId = CalendarId.ofRepoId(order.getC_Harvesting_Calendar_ID());

		final ModularFlatrateTermQuery query = ModularFlatrateTermQuery.builder()
				.bPartnerId(warehouseBL.getBPartnerId(warehouseId))
				.productId(ProductId.ofRepoId(orderLine.getM_Product_ID()))
				.calendarId(harvestingCalendarId)
				.yearId(harvestingYearId)
				.soTrx(SOTrx.PURCHASE)
				.typeConditions(TypeConditions.MODULAR_CONTRACT)
				.build();

		return flatrateBL.streamModularFlatrateTermsByQuery(query)
				.map(I_C_Flatrate_Term::getC_Flatrate_Term_ID)
				.map(FlatrateTermId::ofRepoId);
	}
}
