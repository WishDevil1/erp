/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2009 www.metas.de                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.DBException;
import org.adempiere.model.ZoomInfoFactory.IZoomSource;
import org.adempiere.model.ZoomInfoFactory.ZoomInfo;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_M_RMA;
import org.compiere.model.MQuery;
import org.compiere.model.POInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.google.common.collect.ImmutableList;

/**
 * Generic provider of zoom targets. Contains pieces of {@link org.compiere.apps.AZoomAcross}
 * methods <code>getZoomTargets</code> and <code>addTarget</code>
 *
 * @author Tobias Schoeneberg, www.metas.de - FR [ 2897194  ] Advanced Zoom and RelationTypes
 *
 */
public class GenericZoomProvider implements IZoomProvider
{
	public static final GenericZoomProvider instance = new GenericZoomProvider();
	
	private GenericZoomProvider()
	{
		super();
	}
	
	@Override
	public List<ZoomInfo> retrieveZoomInfos(final IZoomSource source)
	{
		final String sourceKeyColumnName = source.getKeyColumnName();
		if (sourceKeyColumnName == null)
		{
			return ImmutableList.of();
		}
		
		final List<Object> sqlParams = new ArrayList<>();
		String sql = "SELECT DISTINCT ws.AD_Window_ID,ws.Name, wp.AD_Window_ID,wp.Name, t.TableName "
				+ "\nFROM AD_Table t ";
		final boolean baseLanguage = Env.isBaseLanguage(Env.getCtx(), "AD_Window");
		if (baseLanguage)
		{
			sql += "\n INNER JOIN AD_Window ws ON (t.AD_Window_ID=ws.AD_Window_ID)"
					+ "\n LEFT OUTER JOIN AD_Window wp ON (t.PO_Window_ID=wp.AD_Window_ID) ";
		}
		else
		{
			sql += "\n INNER JOIN AD_Window_Trl ws ON (t.AD_Window_ID=ws.AD_Window_ID AND ws.AD_Language=?)"
					+ "\n LEFT OUTER JOIN AD_Window_Trl wp ON (t.PO_Window_ID=wp.AD_Window_ID AND wp.AD_Language=?) ";
			
			final String adLanguage = Env.getAD_Language(Env.getCtx());
			sqlParams.add(adLanguage);
			sqlParams.add(adLanguage);
		}
		//
		//@formatter:off
		sql += "WHERE t.TableName NOT LIKE 'I%'" // No Import
				//
				// Consider first window tab or any tab if our column has AllowZoomTo set
				+ " AND EXISTS ("
					+ "SELECT 1 FROM AD_Tab tt "
						+ "WHERE (tt.AD_Window_ID=ws.AD_Window_ID OR tt.AD_Window_ID=wp.AD_Window_ID)"
						+ " AND tt.AD_Table_ID=t.AD_Table_ID"
						+ " AND ("
							// First Tab
							+ " tt.SeqNo=10"
							// Or tab contains our column and AllowZoomTo=Y
							+ " OR EXISTS (SELECT 1 FROM AD_Column c where c.AD_Table_ID=t.AD_Table_ID AND ColumnName=? AND "+I_AD_Column.COLUMNNAME_AllowZoomTo+"='Y')" // #1
						+ ")"
				+ ")"
				//
				// Consider tables which have a reference to our column
				+ " AND (" // metas
					+ " t.AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column WHERE ColumnName=? AND IsKey='N' AND IsParent='N') " // #2
					// metas: begin: support for "Zoomable Record_IDs" (03921)
					+ " OR ("
						+ " t.AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column WHERE ColumnName='AD_Table_ID' AND IsKey='N' AND IsParent='N')"
						+ " AND t.AD_Table_ID IN (SELECT AD_Table_ID FROM AD_Column WHERE ColumnName='Record_ID' AND IsKey='N' AND IsParent='N' AND "+I_AD_Column.COLUMNNAME_AllowZoomTo+"='Y')"
					+ ") "
				+ ") "
				// metas: end: support for "Zoomable Record_IDs" (03921)
				+ "ORDER BY 2";
		//@formatter:on
		sqlParams.add(sourceKeyColumnName);
		sqlParams.add(sourceKeyColumnName);


		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, ITrx.TRXNAME_None);
			DB.setParameters(pstmt, sqlParams);
			rs = pstmt.executeQuery();

			final ImmutableList.Builder<ZoomInfo> result = ImmutableList.builder();
			while (rs.next())
			{
				final int AD_Window_ID = rs.getInt(1);
				final String name = rs.getString(2);
				final int PO_Window_ID = rs.getInt(3);
				final String targetTableName = rs.getString(5);

				if (PO_Window_ID <= 0)
				{
					final MQuery query = evaluateQuery(targetTableName, AD_Window_ID, null, source);
					result.add(ZoomInfoFactory.ZoomInfo.of(AD_Window_ID, query, name));
				}
				else
				{
					final MQuery soQuery = evaluateQuery(targetTableName, AD_Window_ID, Boolean.TRUE, source);
					result.add(ZoomInfoFactory.ZoomInfo.of(AD_Window_ID, soQuery, name));
					
					final String poName = rs.getString(4);
					final MQuery poQuery = evaluateQuery(targetTableName, PO_Window_ID, Boolean.FALSE, source);
					result.add(ZoomInfoFactory.ZoomInfo.of(PO_Window_ID, poQuery, poName));
				}
			}
			return result.build();
		}
		catch (SQLException e)
		{
			throw new DBException(e, sql, sqlParams);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
	}

	private static MQuery evaluateQuery(final String targetTableName, final int AD_Window_ID, Boolean isSO, final IZoomSource source)
	{
		final POInfo targetTableInfo = POInfo.getPOInfo(targetTableName);
		if (targetTableInfo == null)
		{
			return MQuery.getNoRecordQuery(targetTableName, false);
		}
		
		final String targetColumnName = source.getKeyColumnName();
		final boolean hasTargetColumnName = targetTableInfo.hasColumnName(targetColumnName);
		
		// metas: begin: support for "Zoomable Record_IDs" (03921)
		if (!hasTargetColumnName
				&& targetTableInfo.hasColumnName("AD_Table_ID")
				&& targetTableInfo.hasColumnName("Record_ID"))
		{
			final MQuery query = new MQuery(targetTableName);
			query.addRestriction("AD_Table_ID", MQuery.EQUAL, source.getAD_Table_ID());
			query.addRestriction("Record_ID", MQuery.EQUAL, source.getRecord_ID());
			query.setZoomTableName(targetTableName);
			//query.setZoomColumnName(po.get_KeyColumns()[0]);
			query.setZoomValue(source.getRecord_ID());

			final int count = DB.getSQLValue(ITrx.TRXNAME_None, "SELECT COUNT(*) FROM " + targetTableName + " WHERE "+ query.getWhereClause(false));
			query.setRecordCount(count > 0 ? count : 0);

			return query;
		}
		// metas: end

		if (!hasTargetColumnName)
		{
			return MQuery.getNoRecordQuery(targetTableName, false);
		}

		final MQuery query = new MQuery();
		if (targetTableInfo.isVirtualColumn(targetColumnName))
		{
			final String columnSql = targetTableInfo.getColumnSql(targetColumnName);
			query.addRestriction("(" + columnSql + ") = " + source.getRecord_ID());
		}
		else
		{
			query.addRestriction(targetColumnName + "=" + source.getRecord_ID());
		}
		query.setZoomTableName(targetTableName);
		query.setZoomColumnName(source.getKeyColumnName());
		query.setZoomValue(source.getRecord_ID());

		String sql = "SELECT COUNT(*) FROM " + targetTableName + " WHERE " + query.getWhereClause(false);
		String sqlAdd = "";
		if (isSO != null && targetTableInfo.hasColumnName("IsSOTrx"))
		{
			/*
			 * For RMA, Material Receipt window should be loaded for
			 * IsSOTrx=true and Shipment for IsSOTrx=false
			 */
			if (I_M_RMA.Table_Name.equals(source.getTableName()) && (AD_Window_ID == 169 || AD_Window_ID == 184))
			{
				isSO = !isSO;
			}
			
			// TODO: handle the case when IsSOTrx is a virtual column
			
			sqlAdd = " AND IsSOTrx=" + DB.TO_BOOLEAN(isSO);
		}
		
		int count = DB.getSQLValue(ITrx.TRXNAME_None, sql + sqlAdd);
		if (count < 0 && isSO != null) // error try again w/o SO
		{
			count = DB.getSQLValue(ITrx.TRXNAME_None, sql);
		}
		query.setRecordCount(count);

		return query;
	}

}
