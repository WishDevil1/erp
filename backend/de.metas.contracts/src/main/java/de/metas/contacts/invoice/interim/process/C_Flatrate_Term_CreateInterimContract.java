/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2022 metas GmbH
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

package de.metas.contacts.invoice.interim.process;

import de.metas.contacts.invoice.interim.command.InterimInvoiceFlatrateTermCreateCommand;
import de.metas.contracts.ConditionsId;
import de.metas.contracts.order.model.I_C_OrderLine;
import de.metas.order.OrderLineId;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.Param;
import de.metas.process.ProcessPreconditionsResolution;
import lombok.NonNull;
import org.compiere.util.TimeUtil;

import java.sql.Timestamp;

public class C_Flatrate_Term_CreateInterimContract extends JavaProcess implements IProcessPrecondition
{
	@Param(mandatory = true, parameterName = "C_Flatrate_Conditions_ID")
	private int p_C_Flatrate_Conditions_ID;
	@Param(mandatory = true, parameterName = "DateFrom")
	private Timestamp p_DateFrom;
	@Param(mandatory = true, parameterName = "DateTo")
	private Timestamp p_DateTo;

	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(final @NonNull IProcessPreconditionsContext context)
	{
		if (context.isNoSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}
		if (context.isMoreThanOneSelected())
		{
			return ProcessPreconditionsResolution.rejectBecauseNotSingleSelection();
		}
		final I_C_OrderLine salesOrder = context.getSelectedModel(I_C_OrderLine.class);
		if (!salesOrder.isProcessed())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("only processed order lines are allowed");
		}

		return ProcessPreconditionsResolution.accept();
	}

	@Override
	protected String doIt() throws Exception
	{
		InterimInvoiceFlatrateTermCreateCommand.builder()
				.conditionsId(ConditionsId.ofRepoId(p_C_Flatrate_Conditions_ID))
				.orderLineId(OrderLineId.ofRepoId(getRecord_ID()))
				.dateFrom(TimeUtil.asInstantNonNull(p_DateFrom))
				.dateTo(TimeUtil.asInstantNonNull(p_DateTo))
				.build()
				.execute();

		return MSG_OK;
	}

}
