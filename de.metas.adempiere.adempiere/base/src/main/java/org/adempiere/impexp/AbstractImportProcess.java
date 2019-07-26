package org.adempiere.impexp;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.DBException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.LoggerLoggable;
import org.adempiere.util.api.IParams;
import org.adempiere.util.lang.IMutable;
import org.adempiere.util.lang.Mutable;
import org.compiere.Adempiere;
import org.compiere.model.ModelValidationEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.ISqlUpdateReturnProcessor;
import org.compiere.util.TrxRunnableAdapter;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import ch.qos.logback.classic.Level;
import de.metas.cache.CacheMgt;
import de.metas.cache.model.CacheInvalidateMultiRequest;
import de.metas.logging.LogManager;
import de.metas.util.Check;
import de.metas.util.ILoggable;
import de.metas.util.Services;
import lombok.Getter;
import lombok.NonNull;

/**
 * Base implementation of {@link IImportProcess}.
 *
 * Implementors shall extend this class instead of implementing {@link IImportProcess}.
 *
 * @author tsa
 *
 * @param <ImportRecordType> import table model (e.g. I_I_BPartner).
 */
public abstract class AbstractImportProcess<ImportRecordType> implements IImportProcess<ImportRecordType>
{
	public enum ImportRecordResult
	{
		Inserted, Updated, Nothing,
	}

	public static final String COLUMNNAME_I_IsImported = "I_IsImported";
	public static final String COLUMNNAME_I_ErrorMsg = "I_ErrorMsg";
	public static final String COLUMNNAME_Processed = "Processed";
	public static final String COLUMNNAME_Processing = "Processing";
	public static final String COLUMNNAME_C_DataImport_ID = "C_DataImport_ID";

	// services
	protected final transient Logger log = LogManager.getLogger(getClass());
	protected final ITrxManager trxManager = Services.get(ITrxManager.class);
	protected final DBFunctionsRepository dbFunctionsRepo = Adempiere.getBean(DBFunctionsRepository.class);

	//
	// Parameters
	private Properties _ctx;
	private IParams _parameters = IParams.NULL;
	private ILoggable loggable = LoggerLoggable.of(log, Level.INFO);

	@Getter(lazy = true)
	private final DBFunctions dbFunctions = createDBFunctions();

	@Override
	public final AbstractImportProcess<ImportRecordType> setCtx(final Properties ctx)
	{
		this._ctx = ctx;
		return this;
	}

	@Override
	public final Properties getCtx()
	{
		Check.assumeNotNull(_ctx, "_ctx not null");
		return _ctx;
	}

	protected final int getAD_Client_ID()
	{
		return Env.getAD_Client_ID(getCtx());
	}

	@Override
	public final AbstractImportProcess<ImportRecordType> setParameters(final IParams params)
	{
		Check.assumeNotNull(params, "params not null");
		this._parameters = params;
		return this;
	}

	protected final IParams getParameters()
	{
		return _parameters;
	}

	private DBFunctions createDBFunctions()
	{
		return dbFunctionsRepo.retrieveByTableName(getImportTableName());
	}

	@Override
	public final AbstractImportProcess<ImportRecordType> setLoggable(final ILoggable loggable)
	{
		Check.assumeNotNull(loggable, "loggable not null");
		this.loggable = loggable;
		return this;
	}

	protected final ILoggable getLoggable()
	{
		return loggable;
	}

	private final boolean isValidateOnly()
	{
		return getParameters().getParameterAsBool(PARAM_IsValidateOnly);
	}

	protected final boolean isInsertOnly()
	{
		return getParameters().getParameterAsBool(PARAM_IsInsertOnly);
	}

	private final boolean isDeleteOldImported()
	{
		return getParameters().getParameterAsBool(PARAM_DeleteOldImported);
	}

	protected final String getImportKeyColumnName()
	{
		return getImportTableName() + "_ID";
	}

	protected abstract String getTargetTableName();

	@Override
	public final String getWhereClause()
	{
		StringBuilder whereClause = new StringBuilder();

		// AD_Client
		whereClause.append(" AND AD_Client_ID=").append(getAD_Client_ID());

		// Selection_ID
		final int selectionId = getParameters().getParameterAsInt(PARAM_Selection_ID, -1);
		if (selectionId > 0)
		{
			whereClause.append(" AND EXISTS (SELECT 1 FROM T_SELECTION s WHERE s.AD_PInstance_ID=" + selectionId + " AND s.T_Selection_ID=" + getImportKeyColumnName() + ")");
		}

		return whereClause.toString();
	}

	@Override
	public final ImportProcessResult run()
	{
		// Assume we are not running in another transaction because that could introduce deadlocks,
		// because we are creating the transactions here.
		trxManager.assertThreadInheritedTrxNotExists();

		//
		// Delete old imported records (out of trx)
		if (isDeleteOldImported())
		{
			final int deletedCount = deleteImportRecords(ImportDataDeleteRequest.builder()
					.mode(ImportDataDeleteMode.ONLY_IMPORTED)
					.build());
			loggable.addLog("Deleted Old Imported =" + deletedCount);
		}

		//
		// Reset standard columns (out of trx)
		resetStandardColumns();

		final ImportProcessResult importResult = ImportProcessResult.newInstance(getTargetTableName());

		//
		// Update and validate
		ModelValidationEngine.get().fireImportValidate(this, null, null, IImportInterceptor.TIMING_BEFORE_VALIDATE);
		trxManager.run(new TrxRunnableAdapter()
		{
			@Override
			public void run(final String localTrxName) throws Exception
			{
				updateAndValidateImportRecords();
			}
		});
		ModelValidationEngine.get().fireImportValidate(this, null, null, IImportInterceptor.TIMING_AFTER_VALIDATE);
		if (isValidateOnly())
		{
			return importResult;
		}

		//
		// Actual import (allow the method to manage the transaction)
		importData(importResult);

		loggable.addLog("" + importResult);

		return importResult;
	}

	@Override
	public final int deleteImportRecords(@NonNull final ImportDataDeleteRequest request)
	{
		final StringBuilder sql = new StringBuilder("DELETE FROM " + getImportTableName() + " WHERE 1=1");

		//
		sql.append("\n /* standard import filter */ ").append(getWhereClause());

		//
		// Delete mode filters
		final boolean appendViewSqlWhereClause;
		final ImportDataDeleteMode mode = request.getMode();
		if (ImportDataDeleteMode.ONLY_SELECTED.equals(mode))
		{
			appendViewSqlWhereClause = false;
			if (!Check.isEmpty(request.getSelectionSqlWhereClause(), true))
			{
				sql.append("\n /* selection */ AND ").append(request.getSelectionSqlWhereClause());
			}
		}
		else if (ImportDataDeleteMode.ALL.equals(mode))
		{
			// allow to delete ALL for current selection
			appendViewSqlWhereClause = true;
		}
		else if (ImportDataDeleteMode.ONLY_IMPORTED.equals(mode))
		{
			appendViewSqlWhereClause = true;
			sql.append("\n /* only imported */ AND ").append(COLUMNNAME_I_IsImported).append("='Y'");
		}
		else
		{
			throw new AdempiereException("Unknown mode: " + mode);
		}

		//
		// View filter
		if (appendViewSqlWhereClause
				&& !Check.isEmpty(request.getViewSqlWhereClause(), true))
		{
			sql.append("\n /* view */ AND (").append(request.getViewSqlWhereClause()).append(")");
		}

		//
		// Delete
		final int deletedCount = DB.executeUpdateEx(sql.toString(), ITrx.TRXNAME_ThreadInherited);
		return deletedCount;
	}

	/** @return a map of ImportTable_ColumnName to DefaultValue, to be used when the value is null */
	protected Map<String, Object> getImportTableDefaultValues()
	{
		return ImmutableMap.of();
	}

	/**
	 * Reset standard columns (Client, Org, IsActive, Created/Updated).
	 *
	 * Called before starting to validate.
	 */
	protected void resetStandardColumns()
	{
		final StringBuilder sql = new StringBuilder("UPDATE " + getImportTableName()
				+ " SET AD_Client_ID = COALESCE (AD_Client_ID, ").append(getAD_Client_ID()).append("),"
						+ " AD_Org_ID = COALESCE (AD_Org_ID, 0),"
						+ " IsActive = COALESCE (IsActive, 'Y'),"
						+ " Created = COALESCE (Created, now()),"
						+ " CreatedBy = COALESCE (CreatedBy, 0),"
						+ " Updated = COALESCE (Updated, now()),"
						+ " UpdatedBy = COALESCE (UpdatedBy, 0),"
						+ COLUMNNAME_I_ErrorMsg + " = ' ',"
						+ COLUMNNAME_I_IsImported + "= 'N' ");
		final List<Object> sqlParams = new ArrayList<>();

		for (final Map.Entry<String, Object> defaultValueEntry : getImportTableDefaultValues().entrySet())
		{
			final String columnName = defaultValueEntry.getKey();
			final Object value = defaultValueEntry.getValue();

			sql.append("\n, " + columnName + "=COALESCE(" + columnName + ", ?)");
			sqlParams.add(value);
		}

		sql.append("\n WHERE (" + COLUMNNAME_I_IsImported + "<>'Y' OR " + COLUMNNAME_I_IsImported + " IS NULL) " + getWhereClause());
		final int no = DB.executeUpdateEx(sql.toString(),
				sqlParams.toArray(),
				ITrx.TRXNAME_ThreadInherited);
		log.debug("Reset=" + no);

	}

	/**
	 * Prepare data import: fill missing fields (if possible) and validate the records.
	 */
	protected abstract void updateAndValidateImportRecords();

	/**
	 * Actual data import.
	 *
	 * @param importResult
	 */
	protected final void importData(final ImportProcessResult importResult)
	{
		final Properties ctx = getCtx();

		//
		// Build SQL
		final String whereClause = getWhereClause();
		final StringBuilder sql = new StringBuilder("SELECT * FROM " + getImportTableName() + " WHERE " + COLUMNNAME_I_IsImported + "='N' ").append(whereClause);
		// ORDER BY
		sql.append(" ORDER BY ");
		final String sqlOrderBy = getImportOrderBySql();
		if (!Check.isEmpty(sqlOrderBy, true))
		{
			sql.append(sqlOrderBy);
		}
		else
		{
			sql.append(getImportKeyColumnName());
		}

		//
		// Go through Records
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), ITrx.TRXNAME_None); // i.e. out of transaction
			rs = pstmt.executeQuery();

			final IMutable<Object> state = new Mutable<>();
			while (rs.next())
			{
				final ImportRecordType importRecord = retrieveImportRecord(ctx, rs);
				trxManager.run(new TrxRunnableAdapter()
				{
					private ImportRecordResult recordImportResult;
					private Throwable error;

					@Override
					public void run(final String localTrxName) throws Exception
					{
						this.recordImportResult = importRecord(state, importRecord, isInsertOnly());
						//
						markImported(importRecord);
						//
						runSQLAfterRowImport(importRecord); // run after markImported because we need the recordId saved
					}

					@Override
					public boolean doCatch(final Throwable e) throws Throwable
					{
						this.error = e;
						return true; // rollback
					}

					@Override
					public void doFinally()
					{
						if (error != null)
						{
							reportError(importRecord, error.getLocalizedMessage());
						}
						else if (recordImportResult == ImportRecordResult.Inserted)
						{
							importResult.incrementInsertCounter();
						}
						else if (recordImportResult == ImportRecordResult.Updated)
						{
							importResult.incrementUpdateCounter();
						}
					}
				});
			}

			afterImport();

		}
		catch (final SQLException e)
		{
			throw new DBException(e, sql.toString());
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;

			final int noErrors = markNotImportedAllWithErrors();
			importResult.setErrorCount(noErrors);
		}
	}

	protected abstract String getImportOrderBySql();

	protected abstract ImportRecordType retrieveImportRecord(final Properties ctx, final ResultSet rs) throws SQLException;

	protected abstract ImportRecordResult importRecord(final IMutable<Object> state, final ImportRecordType importRecord, final boolean isInsertOnly) throws Exception;

	private final void reportError(final ImportRecordType importRecord, final String errorMsg)
	{
		final String tableName = InterfaceWrapperHelper.getModelTableName(importRecord);
		final String keyColumnName = InterfaceWrapperHelper.getKeyColumnName(tableName);
		final int importRecordId = InterfaceWrapperHelper.getId(importRecord);

		final StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET " + COLUMNNAME_I_IsImported + "=?, " + COLUMNNAME_I_ErrorMsg + "=I_ErrorMsg || ?")
				.append(" WHERE " + keyColumnName + "=?");
		final Object[] sqlParams = new Object[] {
				"E" // I_IsImported
				, Check.isEmpty(errorMsg, true) ? "" : errorMsg + ", " // ErrorMsg
				, importRecordId // record Id
		};

		DB.executeUpdateEx(sql.toString(),
				sqlParams,
				ITrx.TRXNAME_ThreadInherited,
				0, // no timeOut
				(ISqlUpdateReturnProcessor)null);

		InterfaceWrapperHelper.markStaled(importRecord); // just in case some BL wants to get values from it

		CacheMgt.get().resetLocalNowAndBroadcastOnTrxCommit(ITrx.TRXNAME_ThreadInherited, CacheInvalidateMultiRequest.fromTableNameAndRecordId(tableName, importRecordId));
	}

	protected final int markNotImportedAllWithErrors()
	{
		final StringBuilder sql = new StringBuilder("UPDATE " + getImportTableName()
				+ " SET " + COLUMNNAME_I_IsImported + "='N', Updated=now() "
				+ " WHERE " + COLUMNNAME_I_IsImported + "<>'Y' ").append(getWhereClause());
		final int no = DB.executeUpdateEx(sql.toString(), ITrx.TRXNAME_ThreadInherited);
		return no >= 0 ? no : 0;
	}

	protected void markImported(final ImportRecordType importRecord)
	{
		InterfaceWrapperHelper.setValue(importRecord, COLUMNNAME_I_IsImported, true);
		InterfaceWrapperHelper.setValue(importRecord, COLUMNNAME_Processed, true);
		InterfaceWrapperHelper.setValue(importRecord, COLUMNNAME_Processing, false);
		InterfaceWrapperHelper.save(importRecord);
	}

	protected void afterImport()
	{
		// nothing to do here
	}

	protected final void runSQLAfterRowImport(@NonNull final ImportRecordType importRecord)
	{
		final List<DBFunction> functions = getDbFunctions().getAvailableAfterRowFunctions();
		final Optional<Integer> dataImportId = Optional.ofNullable(InterfaceWrapperHelper.getValueOrNull(importRecord, COLUMNNAME_C_DataImport_ID));
		final Optional<Integer> recordId = InterfaceWrapperHelper.getValue(importRecord, getImportKeyColumnName());
		functions.forEach(function -> DBFunctionHelper.doDBFunctionCall(function, dataImportId.orElse(0), recordId.orElse(0)));
	}
}
