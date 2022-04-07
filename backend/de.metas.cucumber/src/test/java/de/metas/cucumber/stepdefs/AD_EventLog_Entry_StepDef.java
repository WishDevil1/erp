/*
 * #%L
 * de.metas.cucumber
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

package de.metas.cucumber.stepdefs;

import de.metas.event.model.I_AD_EventLog_Entry;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.trx.api.ITrx;
import org.compiere.util.DB;

import java.util.Map;
import java.util.function.Supplier;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static org.assertj.core.api.Assertions.*;

public class AD_EventLog_Entry_StepDef
{
	final IQueryBL queryBL = Services.get(IQueryBL.class);

	private final AD_EventLog_Entry_StepDefData eventLogEntryTable;

	public AD_EventLog_Entry_StepDef(@NonNull final AD_EventLog_Entry_StepDefData eventLogEntryTable)
	{
		this.eventLogEntryTable = eventLogEntryTable;
	}

	@And("metasfresh has no AD_EventLog_Entry records")
	public void truncateADEventLogEntry()
	{
		DB.executeUpdateEx("TRUNCATE TABLE AD_EventLog_Entry cascade", ITrx.TRXNAME_None);
	}

	@And("^after not more than (.*)s, AD_EventLog_Entry are found$")
	public void findAD_EventLog_Entry(final int timeoutSec, @NonNull final DataTable dataTable) throws InterruptedException
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String classname = DataTableUtil.extractStringForColumnName(row, I_AD_EventLog_Entry.COLUMNNAME_Classname);
			final String msgText = DataTableUtil.extractStringForColumnName(row, I_AD_EventLog_Entry.COLUMNNAME_MsgText);
			final boolean processed = DataTableUtil.extractBooleanForColumnName(row, I_AD_EventLog_Entry.COLUMNNAME_Processed);
			final String eventLogIdentifier = DataTableUtil.extractStringForColumnName(row, I_AD_EventLog_Entry.COLUMNNAME_AD_EventLog_Entry_ID + "." + TABLECOLUMN_IDENTIFIER);

			final Supplier<Boolean> logEntryRecordFound = () -> {
				final I_AD_EventLog_Entry eventLogEntryRecord = queryBL.createQueryBuilder(I_AD_EventLog_Entry.class)
						.addEqualsFilter(I_AD_EventLog_Entry.COLUMNNAME_Classname, classname)
						.addEqualsFilter(I_AD_EventLog_Entry.COLUMNNAME_Processed, processed)
						.addStringLikeFilter(I_AD_EventLog_Entry.COLUMNNAME_MsgText, msgText, true)
						.create()
						.firstOnly(I_AD_EventLog_Entry.class);

				if (eventLogEntryRecord == null)
				{
					return false;
				}

				eventLogEntryTable.put(eventLogIdentifier, eventLogEntryRecord);
				return true;
			};

			final boolean recordFound = StepDefUtil.tryAndWait(timeoutSec, 1000, logEntryRecordFound);
			assertThat(recordFound).isTrue();
		}
	}
}
