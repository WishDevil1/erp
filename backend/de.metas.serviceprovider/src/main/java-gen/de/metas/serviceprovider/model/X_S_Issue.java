/** Generated Model - DO NOT CHANGE */
package de.metas.serviceprovider.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for S_Issue
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_S_Issue extends org.compiere.model.PO implements I_S_Issue, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 80795629L;

    /** Standard Constructor */
    public X_S_Issue (Properties ctx, int S_Issue_ID, String trxName)
    {
      super (ctx, S_Issue_ID, trxName);
      /** if (S_Issue_ID == 0)
        {
			setCurrentEffort (0); // 0
			setEffort_UOM_ID (0);
			setEstimatedEffort (BigDecimal.ZERO); // 0
			setIsApproved (false); // N
			setIsEffortIssue (false); // N
			setIssueType (null); // N
			setName (null);
			setProcessed (false); // N
			setS_Issue_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_S_Issue (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** Set Ansprechpartner.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	@Override
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 0) 
			set_Value (COLUMNNAME_AD_User_ID, null);
		else 
			set_Value (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get Ansprechpartner.
		@return User within the system - Internal or Business Partner Contact
	  */
	@Override
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Budgetiert.
		@param BudgetedEffort 
		Ursprünglich geplanter oder erwarteter Aufwand.
	  */
	@Override
	public void setBudgetedEffort (java.math.BigDecimal BudgetedEffort)
	{
		set_Value (COLUMNNAME_BudgetedEffort, BudgetedEffort);
	}

	/** Get Budgetiert.
		@return Ursprünglich geplanter oder erwarteter Aufwand.
	  */
	@Override
	public java.math.BigDecimal getBudgetedEffort () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_BudgetedEffort);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set Project.
		@param C_Project_ID 
		Financial Project
	  */
	@Override
	public void setC_Project_ID (int C_Project_ID)
	{
		if (C_Project_ID < 1) 
			set_Value (COLUMNNAME_C_Project_ID, null);
		else 
			set_Value (COLUMNNAME_C_Project_ID, Integer.valueOf(C_Project_ID));
	}

	/** Get Project.
		@return Financial Project
	  */
	@Override
	public int getC_Project_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Project_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Aktueller Aufwand.
		@param CurrentEffort Aktueller Aufwand	  */
	@Override
	public void setCurrentEffort (int CurrentEffort)
	{
		set_Value (COLUMNNAME_CurrentEffort, Integer.valueOf(CurrentEffort));
	}

	/** Get Aktueller Aufwand.
		@return Aktueller Aufwand	  */
	@Override
	public int getCurrentEffort () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_CurrentEffort);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Beschreibung.
		@param Description Beschreibung	  */
	@Override
	public void setDescription (java.lang.String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Beschreibung.
		@return Beschreibung	  */
	@Override
	public java.lang.String getDescription () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Description);
	}

	/** Set Einheit.
		@param Effort_UOM_ID Einheit	  */
	@Override
	public void setEffort_UOM_ID (int Effort_UOM_ID)
	{
		if (Effort_UOM_ID < 1) 
			set_Value (COLUMNNAME_Effort_UOM_ID, null);
		else 
			set_Value (COLUMNNAME_Effort_UOM_ID, Integer.valueOf(Effort_UOM_ID));
	}

	/** Get Einheit.
		@return Einheit	  */
	@Override
	public int getEffort_UOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Effort_UOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Geschätzter Aufwand.
		@param EstimatedEffort Geschätzter Aufwand	  */
	@Override
	public void setEstimatedEffort (java.math.BigDecimal EstimatedEffort)
	{
		set_Value (COLUMNNAME_EstimatedEffort, EstimatedEffort);
	}

	/** Get Geschätzter Aufwand.
		@return Geschätzter Aufwand	  */
	@Override
	public java.math.BigDecimal getEstimatedEffort () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_EstimatedEffort);
		if (bd == null)
			 return BigDecimal.ZERO;
		return bd;
	}

	/** Set External ID.
		@param ExternalId External ID	  */
	@Override
	public void setExternalId (java.lang.String ExternalId)
	{
		set_Value (COLUMNNAME_ExternalId, ExternalId);
	}

	/** Get External ID.
		@return External ID	  */
	@Override
	public java.lang.String getExternalId () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ExternalId);
	}

	/** Set ExternalIssueNo.
		@param ExternalIssueNo 
		External issue number ( e.g. github issue number )
	  */
	@Override
	public void setExternalIssueNo (java.lang.String ExternalIssueNo)
	{
		set_Value (COLUMNNAME_ExternalIssueNo, ExternalIssueNo);
	}

	/** Get ExternalIssueNo.
		@return External issue number ( e.g. github issue number )
	  */
	@Override
	public java.lang.String getExternalIssueNo () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_ExternalIssueNo);
	}

	/** Set Freigegeben.
		@param IsApproved 
		Zeigt an, ob dieser Beleg eine Freigabe braucht
	  */
	@Override
	public void setIsApproved (boolean IsApproved)
	{
		set_Value (COLUMNNAME_IsApproved, Boolean.valueOf(IsApproved));
	}

	/** Get Freigegeben.
		@return Zeigt an, ob dieser Beleg eine Freigabe braucht
	  */
	@Override
	public boolean isApproved () 
	{
		Object oo = get_Value(COLUMNNAME_IsApproved);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Effort issue.
		@param IsEffortIssue Effort issue	  */
	@Override
	public void setIsEffortIssue (boolean IsEffortIssue)
	{
		set_Value (COLUMNNAME_IsEffortIssue, Boolean.valueOf(IsEffortIssue));
	}

	/** Get Effort issue.
		@return Effort issue	  */
	@Override
	public boolean isEffortIssue () 
	{
		Object oo = get_Value(COLUMNNAME_IsEffortIssue);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** 
	 * IssueType AD_Reference_ID=541105
	 * Reference name: Budget_Issue_Type
	 */
	public static final int ISSUETYPE_AD_Reference_ID=541105;
	/** Internal = Internal */
	public static final String ISSUETYPE_Internal = "Internal";
	/** External = External */
	public static final String ISSUETYPE_External = "External";
	/** Set Type.
		@param IssueType Type	  */
	@Override
	public void setIssueType (java.lang.String IssueType)
	{

		set_Value (COLUMNNAME_IssueType, IssueType);
	}

	/** Get Type.
		@return Type	  */
	@Override
	public java.lang.String getIssueType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_IssueType);
	}

	/** Set Issue-URL.
		@param IssueURL 
		URL der Issue, z.B. auf github
	  */
	@Override
	public void setIssueURL (java.lang.String IssueURL)
	{
		set_Value (COLUMNNAME_IssueURL, IssueURL);
	}

	/** Get Issue-URL.
		@return URL der Issue, z.B. auf github
	  */
	@Override
	public java.lang.String getIssueURL () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_IssueURL);
	}

	/** Set Name.
		@param Name Name	  */
	@Override
	public void setName (java.lang.String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Name	  */
	@Override
	public java.lang.String getName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Name);
	}

	/** Set Verarbeitet.
		@param Processed 
		Checkbox sagt aus, ob der Datensatz verarbeitet wurde. 
	  */
	@Override
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Verarbeitet.
		@return Checkbox sagt aus, ob der Datensatz verarbeitet wurde. 
	  */
	@Override
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Issue.
		@param S_Issue_ID Issue	  */
	@Override
	public void setS_Issue_ID (int S_Issue_ID)
	{
		if (S_Issue_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_S_Issue_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_S_Issue_ID, Integer.valueOf(S_Issue_ID));
	}

	/** Get Issue.
		@return Issue	  */
	@Override
	public int getS_Issue_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_S_Issue_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public de.metas.serviceprovider.model.I_S_Milestone getS_Milestone()
	{
		return get_ValueAsPO(COLUMNNAME_S_Milestone_ID, de.metas.serviceprovider.model.I_S_Milestone.class);
	}

	@Override
	public void setS_Milestone(de.metas.serviceprovider.model.I_S_Milestone S_Milestone)
	{
		set_ValueFromPO(COLUMNNAME_S_Milestone_ID, de.metas.serviceprovider.model.I_S_Milestone.class, S_Milestone);
	}

	/** Set Meilenstein.
		@param S_Milestone_ID Meilenstein	  */
	@Override
	public void setS_Milestone_ID (int S_Milestone_ID)
	{
		if (S_Milestone_ID < 1) 
			set_Value (COLUMNNAME_S_Milestone_ID, null);
		else 
			set_Value (COLUMNNAME_S_Milestone_ID, Integer.valueOf(S_Milestone_ID));
	}

	/** Get Meilenstein.
		@return Meilenstein	  */
	@Override
	public int getS_Milestone_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_S_Milestone_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Suchschlüssel.
		@param Value 
		Suchschlüssel für den Eintrag im erforderlichen Format - muss eindeutig sein
	  */
	@Override
	public void setValue (java.lang.String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Suchschlüssel.
		@return Suchschlüssel für den Eintrag im erforderlichen Format - muss eindeutig sein
	  */
	@Override
	public java.lang.String getValue () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Value);
	}
}