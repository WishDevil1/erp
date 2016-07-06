/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.adempiere.ad.expression.api.IExpressionFactory;
import org.adempiere.ad.expression.api.ILogicExpression;
import org.adempiere.ad.security.IUserRolePermissions;
import org.adempiere.ad.security.TableAccessLevel;
import org.adempiere.ad.security.asp.IASPFiltersFactory;
import org.adempiere.ad.security.permissions.UIDisplayedEntityTypes;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Evaluatee;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import de.metas.logging.LogManager;

/**
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: GridTabVO.java,v 1.4 2006/07/30 00:58:38 jjanke Exp $
 */
public class GridTabVO implements Evaluatee, Serializable
{
	private static final long serialVersionUID = -1425513230093430761L;

	/**************************************************************************
	 *	Create MTab VO, ordered by <code>SeqNo</code>.
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	static GridTabVO create (final GridWindowVO wVO, final int TabNo, final ResultSet rs, final boolean isRO, final boolean onlyCurrentRows)
	{
		logger.debug("TabNo={}", TabNo);

		GridTabVO vo = new GridTabVO (wVO.getCtx(), wVO.getWindowNo(), TabNo);
		vo.AD_Window_ID = wVO.getAD_Window_ID();
		//
		if (!loadTabDetails(vo, rs))
		{
			wVO.addLoadErrorMessage(vo.getLoadErrorMessage(), false); // metas: 01934
			return null;
		}

		if (isRO)
		{
			logger.debug("Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo._fields = ImmutableList.of();
		}
		wVO.addLoadErrorMessage(vo.getLoadErrorMessage(), false); // metas: 01934
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (final GridTabVO vo, final ResultSet rs)
	{
		final IUserRolePermissions role = Env.getUserRolePermissions(vo.ctx);
		boolean showTrl = "Y".equals(Env.getContext(vo.ctx, "#ShowTrl"));
		boolean showAcct = "Y".equals(Env.getContext(vo.ctx, Env.CTXNAME_ShowAcct));
		boolean showAdvanced = "Y".equals(Env.getContext(vo.ctx, "#ShowAdvanced"));

		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, GridTab.CTX_AD_Tab_ID, String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, GridTab.CTX_Name, vo.Name);
			vo.entityType = rs.getString("EntityType");

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");

			//	Translation Tab	**
			if ("Y".equals(rs.getString("IsTranslationTab")))
			{
				//	Document Translation
				//vo.TableName = rs.getString("TableName"); // metas: not necessary; is loaded above
				if (!Env.isBaseTranslation(vo.TableName)	//	C_UOM, ...
					&& !Env.isMultiLingualDocument(vo.ctx))
					showTrl = false;
				if (!showTrl)
				{
					vo.addLoadErrorMessage("TrlTab Not displayed (BaseTrl=" + Env.isBaseTranslation(vo.TableName) + ", MultiLingual=" + Env.isMultiLingualDocument(vo.ctx)+")"); // metas: 01934
					logger.info("TrlTab Not displayed - AD_Tab_ID="
							+ vo.AD_Tab_ID + "=" + vo.Name + ", Table=" + vo.TableName
							+ ", BaseTrl=" + Env.isBaseTranslation(vo.TableName)
							+ ", MultiLingual=" + Env.isMultiLingualDocument(vo.ctx));
					return false;
				}
			}

			//	Advanced Tab	**
			if (!showAdvanced && "Y".equals(rs.getString("IsAdvancedTab")))
			{
				vo.addLoadErrorMessage("AdvancedTab Not displayed"); // metas: 1934
				logger.info("AdvancedTab Not displayed - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}

			//	Accounting Info Tab	**
			if (!showAcct && "Y".equals(rs.getString("IsInfoTab")))
			{
				vo.addLoadErrorMessage("AcctTab Not displayed"); // metas: 1934
				logger.debug("AcctTab Not displayed - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}

			//
			// If EntityType is not displayed, hide this tab
			vo.entityType = rs.getString("EntityType");
			if (!Check.isEmpty(vo.entityType, true) && !UIDisplayedEntityTypes.isEntityTypeDisplayedInUIOrTrueIfNull(vo.entityType))
			{
				vo.addLoadErrorMessage("EntityType not displayed");
				return false;
			}

			//	DisplayLogic
			final String DisplayLogic = rs.getString("DisplayLogic");
			vo.DisplayLogicExpr = Services.get(IExpressionFactory.class)
					.compileOrDefault(DisplayLogic, DEFAULT_DisplayLogic, ILogicExpression.class); // metas: 03093

			//	Access Level
			vo.AccessLevel = TableAccessLevel.forAccessLevel(rs.getString("AccessLevel"));
			if (!role.canView(vo.AccessLevel))	// No Access
			{
				vo.addLoadErrorMessage("No Role Access - AccessLevel="+vo.AccessLevel+", UserLevel="+role.getUserLevel()); // 01934
				logger.debug("No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, GridTab.CTX_AccessLevel, vo.AccessLevel.getAccessLevelString());

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, GridTab.CTX_AD_Table_ID, String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				vo.addLoadErrorMessage("No Table Access (AD_Table_ID="+vo.AD_Table_ID+")"); // 01934
				logger.info("No Table Access - AD_Tab_ID="
					+ vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}

			if ("Y".equals(rs.getString("IsReadOnly")))
				vo.IsReadOnly = true;
			final String ReadOnlyLogic = rs.getString("ReadOnlyLogic");
			vo.ReadOnlyLogicExpr = Services.get(IExpressionFactory.class)
					.compileOrDefault(ReadOnlyLogic, DEFAULT_ReadOnlyLogicExpr, ILogicExpression.class); // metas: 03093
			if (rs.getString("IsInsertRecord").equals("N"))
				vo.IsInsertRecord = false;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if ("Y".equals(rs.getString("IsSingleRow")))
				vo.IsSingleRow = true;
			if ("Y".equals(rs.getString("HasTree")))
				vo.HasTree = true;

			if ("Y".equals(rs.getString("IsView")))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Link Column
			vo.Parent_Column_ID = rs.getInt("Parent_Column_ID");   // Parent tab link column

			if ("Y".equals(rs.getString("IsSecurityEnabled")))
				vo.IsSecurityEnabled = true;
			if ("Y".equals(rs.getString("IsDeleteable")))
				vo.IsDeleteable = true;
			if ("Y".equals(rs.getString("IsHighVolume")))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			//jz col=null not good for Derby
			if (vo.WhereClause.indexOf("=null") > 0)
			{
				logger.warn("Replaced '=null' with 'IS NULL' for " + vo);
				vo.WhereClause.replaceAll("=null", " IS NULL ");
			}
			// Where Clauses should be surrounded by parenthesis - teo_sarca, BF [ 1982327 ]
			if (vo.WhereClause.trim().length() > 0) {
				vo.WhereClause = "("+vo.WhereClause+")";
			}

			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, GridTab.CTX_TabLevel, String.valueOf(vo.TabLevel)); // metas: tsa: set this value here because here is the right place

			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//
			//	Replication Type - set R/O if Reference
			try
			{
				final int replicationTypeIdx = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (replicationTypeIdx);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
			loadTabDetails_metas(vo, rs); // metas
		}
		catch (SQLException ex)
		{
			logger.error("", ex);
			return false;
		}
		// Apply UserDef settings - teo_sarca [ 2726889 ] Finish User Window (AD_UserDef*) functionality
		if (!MUserDefWin.apply(vo))
		{
			vo.addLoadErrorMessage("Hidden by UserDef"); // 01934
			logger.debug("Hidden by UserDef - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
			return false;
		}

		return true;
	}	//	loadTabDetails


	/**************************************************************************
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static List<GridFieldVO> createFields (final GridTabVO mTabVO)
	{
		//local only or remote fail for vpn profile
		final ImmutableList.Builder<GridFieldVO> fields = ImmutableList.builder();

		final String sql = GridFieldVO.getSQL(mTabVO.getCtx());
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, ITrx.TRXNAME_None);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				GridFieldVO voF = GridFieldVO.create (mTabVO.getCtx(),
					mTabVO.WindowNo, mTabVO.TabNo,
					mTabVO.AD_Window_ID, mTabVO.AD_Tab_ID,
					mTabVO.IsReadOnly, rs);
				if (voF != null)
				{
					fields.add(voF);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed creating fields for {}", mTabVO, e);
			return ImmutableList.of();
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		return fields.build();
	}   //  createFields

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param ctx context
	 *  @return SQL SELECT String
	 */
	static String getSQL (final Properties ctx)
	{
		final String ASPFilter = Services.get(IASPFiltersFactory.class)
				.getASPFiltersForClient(Env.getAD_Client_ID(ctx))
				.getSQLWhereClause(I_AD_Tab.class);

		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=? "
			+ ASPFilter + " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=? "
				+ " AND AD_Language=" + DB.TO_STRING(Env.getAD_Language(ctx))
				+ ASPFilter + " ORDER BY SeqNo";
		return sql;
	}   //  getSQL


	/**************************************************************************
	 *  Private constructor - must use Factory
	 *  @param Ctx context
	 *  @param windowNo window
	 */
	private GridTabVO (final Properties ctx, final int windowNo, final int tabNo)
	{
		super();
		this.ctx = ctx;
		this.WindowNo = windowNo;
		this.TabNo = tabNo;
	}

	private static final transient Logger logger = LogManager.getLogger(GridTabVO.class);

	/** Context - replicated    */
	private Properties ctx;
	/** Window No - replicated  */
	public final int WindowNo;
	/** AD Window - replicated  */
	public  int AD_Window_ID;

	/** Tab No (not AD_Tab_ID) 0.. */
	public final int TabNo;

	/**	Tab	ID			*/
	public	int		    AD_Tab_ID;
	/** Name			*/
	public	String	    Name = "";
	/** Description		*/
	public	String	    Description = "";
	/** Help			*/
	public	String	    Help = "";
	private String entityType = null;
	/** Single Row		*/
	public	boolean	    IsSingleRow = false;
	/** Read Only		*/
	private boolean     IsReadOnly = false;
	/** Insert Record	*/
	public 	boolean		IsInsertRecord = true;
	/** Tree			*/
	public  boolean	    HasTree = false;
	/** Table			*/
	public  int		    AD_Table_ID;
	/** Primary Link Column   */
	private int		    AD_Column_ID = 0;
	/** Parent Tab Link Column */
	private	int			Parent_Column_ID = 0;
	/** Table Name		*/
	public  String	    TableName;
	/** Table is View	*/
	public  boolean     IsView = false;
	/** Table Access Level	*/
	public TableAccessLevel AccessLevel;
	/** Security		*/
	public  boolean	    IsSecurityEnabled = false;
	/** Table Deleteable	*/
	public  boolean	    IsDeleteable = false;
	/** Table High Volume	*/
	public  boolean     IsHighVolume = false;
	/** Process			*/
	private int		    AD_Process_ID = 0;
	/** Commot Warning	*/
	public  String	    CommitWarning;
	/** Where			*/
	public  String	    WhereClause;
	/** Order by		*/
	public  String      OrderByClause;
	/** Tab Read Only	*/
	private static final ILogicExpression DEFAULT_ReadOnlyLogicExpr = ILogicExpression.FALSE;
	private ILogicExpression ReadOnlyLogicExpr = DEFAULT_ReadOnlyLogicExpr;
	/** Tab Display		*/
	private static final ILogicExpression DEFAULT_DisplayLogic = ILogicExpression.TRUE;
	private ILogicExpression DisplayLogicExpr = DEFAULT_DisplayLogic;
	/** Level			*/
	public  int         TabLevel = 0;
	/** Image			*/
	public int          AD_Image_ID = 0;
	/** Included Tab	*/
	public int          Included_Tab_ID = 0;
	/** Replication Type	*/
	public String		ReplicationType = "L";

	/** Sort Tab			*/
	public boolean		IsSortTab = false;
	/** Column Sort			*/
	public int			AD_ColumnSortOrder_ID = 0;
	/** Column Displayed	*/
	public int			AD_ColumnSortYesNo_ID = 0;

	/** Only Current Rows - derived	*/
	public  boolean     onlyCurrentRows = true;
	/**	Only Current Days - derived	*/
	public int			onlyCurrentDays = 0;

	private List<GridFieldVO> _fields = null;

	public List<GridFieldVO> getFields()
	{
		if(_fields == null)
		{
			synchronized (this)
			{
				if(_fields == null)
				{
					_fields = createFields(this);
				}
			}
		}
		return _fields;
	}

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		final List<GridFieldVO> fields = this._fields;
		if (fields != null)
		{
			for (GridFieldVO field : fields)
			{
				field.setCtx(newCtx);
			}
		}
	}   //  setCtx

	public Properties getCtx()
	{
		return ctx;
	}


	/**
	 * 	Get Variable Value (Evaluatee)
	 *	@param variableName name
	 *	@return value
	 */
	@Override
	public String get_ValueAsString (String variableName)
	{
		return Env.getContext (ctx, WindowNo, variableName, false);	// not just window
	}	//	get_ValueAsString

	/**
	 * 	Clone
	 * 	@param Ctx context
	 * 	@param windowNo no
	 *	@return MTabVO or null
	 */
	protected GridTabVO clone(final Properties ctx, final int windowNo)
	{
		final GridTabVO clone = new GridTabVO(ctx, windowNo, this.TabNo);
		clone.AD_Window_ID = AD_Window_ID;
		Env.setContext(ctx, windowNo, clone.TabNo, GridTab.CTX_AD_Tab_ID, String.valueOf(clone.AD_Tab_ID));
		//
		clone.AD_Tab_ID = AD_Tab_ID;
		clone.Name = Name;
		Env.setContext(ctx, windowNo, clone.TabNo, GridTab.CTX_Name, clone.Name);
		clone.Description = Description;
		clone.Help = Help;
		clone.entityType = entityType;
		clone.IsSingleRow = IsSingleRow;
		clone.IsReadOnly = IsReadOnly;
		clone.IsInsertRecord = IsInsertRecord;
		clone.HasTree = HasTree;
		clone.AD_Table_ID = AD_Table_ID;
		clone.AD_Column_ID = AD_Column_ID;
		clone.Parent_Column_ID = Parent_Column_ID;
		clone.TableName = TableName;
		clone.IsView = IsView;
		clone.AccessLevel = AccessLevel;
		clone.IsSecurityEnabled = IsSecurityEnabled;
		clone.IsDeleteable = IsDeleteable;
		clone.IsHighVolume = IsHighVolume;
		clone.AD_Process_ID = AD_Process_ID;
		clone.CommitWarning = CommitWarning;
		clone.WhereClause = WhereClause;
		clone.OrderByClause = OrderByClause;
		clone.ReadOnlyLogicExpr = ReadOnlyLogicExpr;
		clone.DisplayLogicExpr = DisplayLogicExpr;
		clone.TabLevel = TabLevel;
		clone.AD_Image_ID = AD_Image_ID;
		clone.Included_Tab_ID = Included_Tab_ID;
		clone.ReplicationType = ReplicationType;
		Env.setContext(ctx, windowNo, clone.TabNo, GridTab.CTX_AccessLevel, clone.AccessLevel.getAccessLevelString());
		Env.setContext(ctx, windowNo, clone.TabNo, GridTab.CTX_AD_Table_ID, String.valueOf(clone.AD_Table_ID));

		//
		clone.IsSortTab = IsSortTab;
		clone.AD_ColumnSortOrder_ID = AD_ColumnSortOrder_ID;
		clone.AD_ColumnSortYesNo_ID = AD_ColumnSortYesNo_ID;
		//  Derived
		clone.onlyCurrentRows = true;
		clone.onlyCurrentDays = 0;
		clone_metas(ctx, windowNo, clone); // metas

		final List<GridFieldVO> fields = _fields;
		if(fields != null)
		{
			final ImmutableList.Builder<GridFieldVO> cloneFields = ImmutableList.builder();
			for (final GridFieldVO field : fields)
			{
				final GridFieldVO cloneField = field.clone(ctx, windowNo, TabNo, AD_Window_ID, AD_Tab_ID, IsReadOnly);
				if (cloneField == null)
					continue;
				cloneFields.add(cloneField);
			}
			
			clone._fields = cloneFields.build();
		}

		return clone;
	}	//	clone

	public void clearFields()
	{
		_fields = null;
	}

// metas: begin
	/** Grid Mode Only */
	// metas-2009_0021_AP1_CR059
	public boolean IsGridModeOnly = false; // metas-2009_0021_AP1_CR059
	/** Tab has search active */
	// metas-2009_0021_AP1_CR057
	public boolean IsSearchActive = true; // metas-2009_0021_AP1_CR057
	/** Search panel is collapsed */
	// metas-2009_0021_AP1_CR064
	public boolean IsSearchCollapsed = true; // metas-2009_0021_AP1_CR064
	/** Query tab data after open */
	// metas-2009_0021_AP1_CR064
	public boolean IsQueryOnLoad = true; // metas-2009_0021_AP1_CR064
	/** Deafault Where */
	public String DefaultWhereClause;
	public boolean IsRefreshAllOnActivate = false; // metas-2009_0021_AP1_CR050
	public int AD_Message_ID = 0; //metas-us092
	/** Check if the parents of this tab have changed */// 01962
	public boolean IsCheckParentsChanged = true;
	/** Max records to be queried (overrides the settings from AD_Role) */
	private int MaxQueryRecords = 0;

	private static void loadTabDetails_metas (final GridTabVO vo, final ResultSet rs) throws SQLException
	{
		if ("Y".equals(rs.getString("IsGridModeOnly"))) // metas-2009_0021_AP1_CR059
			vo.IsGridModeOnly = true; // metas-2009_0021_AP1_CR059
		vo.IsSearchActive = "Y".equals(rs.getString("IsSearchActive")); // metas-2009_0021_AP1_CR057
		vo.IsSearchCollapsed = "Y".equals(rs.getString("IsSearchCollapsed")); // metas-2009_0021_AP1_CR064
		vo.IsQueryOnLoad = "Y".equals(rs.getString("IsQueryOnLoad")); // metas-2009_0021_AP1_CR064

		// metas: default where clause
		vo.DefaultWhereClause = rs.getString("DefaultWhereClause");
		if (vo.DefaultWhereClause == null)
			vo.DefaultWhereClause = "";
		if (vo.DefaultWhereClause.indexOf("=null") > 0)
			vo.DefaultWhereClause.replaceAll("=null", " IS NULL ");
		if (vo.DefaultWhereClause.trim().length() > 0)
			vo.DefaultWhereClause = "(" + vo.DefaultWhereClause + ")";

		vo.IsRefreshAllOnActivate = "Y".equals(rs.getString("IsRefreshAllOnActivate")); // metas-2009_0021_AP1_CR050
		vo.AD_Message_ID = rs.getInt("AD_Message_ID"); // metas-us092
		vo.IsCheckParentsChanged = "Y".equals(rs.getString("IsCheckParentsChanged")); // 01962

		vo.MaxQueryRecords = rs.getInt("MaxQueryRecords");
		if(vo.MaxQueryRecords <= 0)
		{
			vo.MaxQueryRecords = 0;
		}
	}

	private void clone_metas(final Properties ctx, final int windowNo, final GridTabVO clone)
	{
		clone.IsSearchActive = IsSearchActive; // metas-2009_0021_AP1_CR057
		clone.IsSearchCollapsed = IsSearchCollapsed; // metas-2009_0021_AP1_CR064
		clone.IsQueryOnLoad = IsQueryOnLoad; // metas-2009_0021_AP1_CR064
		clone.DefaultWhereClause = DefaultWhereClause;
		clone.IsRefreshAllOnActivate = IsRefreshAllOnActivate; // metas-2009_0021_AP1_CR050
		clone.AD_Message_ID = AD_Message_ID; // metas-US092
		clone.IsCheckParentsChanged = IsCheckParentsChanged; // 01962
		clone.MaxQueryRecords = this.MaxQueryRecords;
	}


	private StringBuffer loadErrorMessages = null;
	protected void addLoadErrorMessage(String message)
	{
		if (Check.isEmpty(message, true))
			return;
		if (loadErrorMessages == null)
			loadErrorMessages = new StringBuffer();
		if (loadErrorMessages.length() > 0)
			loadErrorMessages.append("\n");
		loadErrorMessages.append(message);
	}
	public String getLoadErrorMessage()
	{
		if (loadErrorMessages == null || loadErrorMessages.length() == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("Tab ").append(this.Name).append("(").append(this.TableName).append("): ").append(loadErrorMessages);
		return sb.toString();
	}

	public ILogicExpression getReadOnlyLogic()
	{
		return ReadOnlyLogicExpr;
	}

	public ILogicExpression getDisplayLogic()
	{
		return DisplayLogicExpr;
	}
// metas: end
	
	public String getTableName()
	{
		return TableName;
	}
	
	public int getAD_Tab_ID()
	{
		return AD_Tab_ID;
	}

	public int getWindowNo()
	{
		return WindowNo;
	}
	
	public int getAD_Table_ID()
	{
		return AD_Table_ID;
	}

	public int getAD_Column_ID()
	{
		return AD_Column_ID;
	}

	public int getParent_Column_ID()
	{
		return Parent_Column_ID;
	}

	/**
	 * Gets max records to be queried (overrides the settings from AD_Role).
	 * @return
	 */
	public int getMaxQueryRecords()
	{
		return MaxQueryRecords;
	}

	public String getEntityType()
	{
		return entityType;
	}

	public int getTabLevel()
	{
		return TabLevel;
	}
	
	public String getName()
	{
		return Name;
	}
	
	public int getPrint_Process_ID()
	{
		return AD_Process_ID;
	}
	
	public boolean isReadOnly()
	{
		return IsReadOnly;
	}
	
	void setReadOnly(boolean isReadOnly)
	{
		IsReadOnly = isReadOnly;
	}
}   //  MTabVO
