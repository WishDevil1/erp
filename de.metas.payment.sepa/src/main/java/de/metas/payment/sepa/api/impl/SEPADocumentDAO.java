package de.metas.payment.sepa.api.impl;

/*
 * #%L
 * de.metas.payment.sepa
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.Query;

import de.metas.payment.sepa.api.ISEPADocumentDAO;
import de.metas.payment.sepa.interfaces.I_C_BP_BankAccount;
import de.metas.payment.sepa.model.I_SEPA_Export;
import de.metas.payment.sepa.model.I_SEPA_Export_Line;

public class SEPADocumentDAO implements ISEPADocumentDAO
{

	@Override
	public I_C_BP_BankAccount retrieveSEPABankAccount(I_C_BPartner bPartner)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(bPartner);
		final String trxName = InterfaceWrapperHelper.getTrxName(bPartner);

		final String whereClause = I_C_BP_BankAccount.COLUMNNAME_C_BPartner_ID + "=?";

		final List<Object> params = new ArrayList<Object>();
		params.add(bPartner.getC_BPartner_ID());

		return new Query(ctx, I_C_BP_BankAccount.Table_Name, whereClause, trxName)
				.setParameters(params)
				.setOrderBy(de.metas.banking.model.I_C_BP_BankAccount.COLUMNNAME_IsDefault + " DESC")
				.setOnlyActiveRecords(true)
				.first(I_C_BP_BankAccount.class);
	}

	@Override
	public Iterator<I_SEPA_Export_Line> retrieveLines(final I_SEPA_Export doc)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(doc);
		final String trxName = InterfaceWrapperHelper.getTrxName(doc);
		final String whereClause = I_SEPA_Export_Line.COLUMNNAME_SEPA_Export_ID + "=?";
		return new Query(ctx, I_SEPA_Export_Line.Table_Name, whereClause, trxName)
				.setParameters(doc.getSEPA_Export_ID())
				.setOrderBy(I_SEPA_Export_Line.COLUMNNAME_SEPA_Export_Line_ID)
				.iterate(I_SEPA_Export_Line.class);
	}

	@Override
	public List<I_SEPA_Export_Line> retrieveLinesChangeRule(Properties ctx, String trxName)
	{
		//
		// Placeholder for future functionality.
		return Collections.emptyList();
	}

}
