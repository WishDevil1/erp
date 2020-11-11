package de.metas.document.archive.api.impl;

/*
 * #%L
 * de.metas.document.archive.base
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


import java.util.List;
import java.util.Properties;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.util.Services;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.util.proxy.Cached;
import org.compiere.model.I_AD_Archive;
import org.compiere.model.Query;
import org.compiere.util.Env;

import de.metas.document.archive.model.I_C_Doc_Outbound_Config;
import de.metas.document.archive.model.I_C_Doc_Outbound_Log;
import lombok.NonNull;

public class DocOutboundDAO extends AbstractDocOutboundDAO
{
	@Override
	public final I_C_Doc_Outbound_Log retrieveLog(@NonNull final I_AD_Archive archiveRecord)
	{
		final TableRecordReference tableRecordReference = TableRecordReference.ofReferenced(archiveRecord);
		return retrieveLog(tableRecordReference);
	}

	@Override
	public I_C_Doc_Outbound_Log retrieveLog(@NonNull final TableRecordReference tableRecordReference)
	{
		final I_C_Doc_Outbound_Log docExchange = Services
				.get(IQueryBL.class)
				.createQueryBuilder(I_C_Doc_Outbound_Log.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_Doc_Outbound_Log.COLUMN_AD_Table_ID, tableRecordReference.getAD_Table_ID())
				.addEqualsFilter(I_C_Doc_Outbound_Log.COLUMN_Record_ID, tableRecordReference.getRecord_ID())
				.create()
				.firstOnly(I_C_Doc_Outbound_Log.class);

		return docExchange;
	}


	@Override
	@Cached(cacheName = I_C_Doc_Outbound_Config.Table_Name + "#All")
	public List<I_C_Doc_Outbound_Config> retrieveAllConfigs()
	{
		final Properties ctx = Env.getCtx();
		return new Query(ctx, I_C_Doc_Outbound_Config.Table_Name, null, ITrx.TRXNAME_None)
				.setOnlyActiveRecords(true)
				.list(I_C_Doc_Outbound_Config.class);
	}


}
