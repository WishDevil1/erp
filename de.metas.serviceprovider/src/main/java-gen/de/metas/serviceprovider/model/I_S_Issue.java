package de.metas.serviceprovider.model;


/** Generated Interface for S_Issue
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public interface I_S_Issue 
{

    /** TableName=S_Issue */
    public static final String Table_Name = "S_Issue";

    /** AD_Table_ID=541468 */
//    public static final int Table_ID = org.compiere.model.MTable.getTable_ID(Table_Name);

//    org.compiere.util.KeyNamePair Model = new org.compiere.util.KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 1 - Org
     */
//    java.math.BigDecimal accessLevel = java.math.BigDecimal.valueOf(1);

    /** Load Meta Data */

	/**
	 * Get Mandant.
	 * Mandant für diese Installation.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Client_ID();

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/**
	 * Set Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setAD_Org_ID (int AD_Org_ID);

	/**
	 * Get Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Org_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/**
	 * Set Ansprechpartner.
	 * User within the system - Internal or Business Partner Contact
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setAD_User_ID (int AD_User_ID);

	/**
	 * Get Ansprechpartner.
	 * User within the system - Internal or Business Partner Contact
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getAD_User_ID();

    /** Column name AD_User_ID */
    public static final String COLUMNNAME_AD_User_ID = "AD_User_ID";

	/**
	 * Set Budgetiert.
	 * Ursprünglich geplanter oder erwarteter Aufwand.
	 *
	 * <br>Type: Number
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setBudgetedEffort (java.math.BigDecimal BudgetedEffort);

	/**
	 * Get Budgetiert.
	 * Ursprünglich geplanter oder erwarteter Aufwand.
	 *
	 * <br>Type: Number
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getBudgetedEffort();

    /** Column definition for BudgetedEffort */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_BudgetedEffort = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "BudgetedEffort", null);
    /** Column name BudgetedEffort */
    public static final String COLUMNNAME_BudgetedEffort = "BudgetedEffort";

	/**
	 * Set Project.
	 * Financial Project
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setC_Project_ID (int C_Project_ID);

	/**
	 * Get Project.
	 * Financial Project
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getC_Project_ID();

    /** Column name C_Project_ID */
    public static final String COLUMNNAME_C_Project_ID = "C_Project_ID";

	/**
	 * Get Erstellt.
	 * Datum, an dem dieser Eintrag erstellt wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getCreated();

    /** Column definition for Created */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Created = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Created", null);
    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/**
	 * Get Erstellt durch.
	 * Nutzer, der diesen Eintrag erstellt hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getCreatedBy();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/**
	 * Set Aktueller Aufwand.
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setCurrentEffort (int CurrentEffort);

	/**
	 * Get Aktueller Aufwand.
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getCurrentEffort();

    /** Column definition for CurrentEffort */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_CurrentEffort = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "CurrentEffort", null);
    /** Column name CurrentEffort */
    public static final String COLUMNNAME_CurrentEffort = "CurrentEffort";

	/**
	 * Set Beschreibung.
	 *
	 * <br>Type: TextLong
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setDescription (java.lang.String Description);

	/**
	 * Get Beschreibung.
	 *
	 * <br>Type: TextLong
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getDescription();

    /** Column definition for Description */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Description = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Description", null);
    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/**
	 * Set Einheit.
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setEffort_UOM_ID (int Effort_UOM_ID);

	/**
	 * Get Einheit.
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getEffort_UOM_ID();

    /** Column name Effort_UOM_ID */
    public static final String COLUMNNAME_Effort_UOM_ID = "Effort_UOM_ID";

	/**
	 * Set Geschätzter Aufwand.
	 *
	 * <br>Type: Number
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setEstimatedEffort (java.math.BigDecimal EstimatedEffort);

	/**
	 * Get Geschätzter Aufwand.
	 *
	 * <br>Type: Number
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getEstimatedEffort();

    /** Column definition for EstimatedEffort */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_EstimatedEffort = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "EstimatedEffort", null);
    /** Column name EstimatedEffort */
    public static final String COLUMNNAME_EstimatedEffort = "EstimatedEffort";

	/**
	 * Set External ID.
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setExternalId (java.lang.String ExternalId);

	/**
	 * Get External ID.
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getExternalId();

    /** Column definition for ExternalId */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_ExternalId = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "ExternalId", null);
    /** Column name ExternalId */
    public static final String COLUMNNAME_ExternalId = "ExternalId";

	/**
	 * Set ExternalIssueNo.
	 * External issue number ( e.g. github issue number )
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setExternalIssueNo (java.lang.String ExternalIssueNo);

	/**
	 * Get ExternalIssueNo.
	 * External issue number ( e.g. github issue number )
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getExternalIssueNo();

    /** Column definition for ExternalIssueNo */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_ExternalIssueNo = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "ExternalIssueNo", null);
    /** Column name ExternalIssueNo */
    public static final String COLUMNNAME_ExternalIssueNo = "ExternalIssueNo";

	/**
	 * Set Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsActive (boolean IsActive);

	/**
	 * Get Aktiv.
	 * Der Eintrag ist im System aktiv
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isActive();

    /** Column definition for IsActive */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_IsActive = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "IsActive", null);
    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/**
	 * Set Freigegeben.
	 * Zeigt an, ob dieser Beleg eine Freigabe braucht
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsApproved (boolean IsApproved);

	/**
	 * Get Freigegeben.
	 * Zeigt an, ob dieser Beleg eine Freigabe braucht
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isApproved();

    /** Column definition for IsApproved */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_IsApproved = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "IsApproved", null);
    /** Column name IsApproved */
    public static final String COLUMNNAME_IsApproved = "IsApproved";

	/**
	 * Set Effort issue.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsEffortIssue (boolean IsEffortIssue);

	/**
	 * Get Effort issue.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isEffortIssue();

    /** Column definition for IsEffortIssue */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_IsEffortIssue = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "IsEffortIssue", null);
    /** Column name IsEffortIssue */
    public static final String COLUMNNAME_IsEffortIssue = "IsEffortIssue";

	/**
	 * Set Type.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIssueType (java.lang.String IssueType);

	/**
	 * Get Type.
	 *
	 * <br>Type: List
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getIssueType();

    /** Column definition for IssueType */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_IssueType = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "IssueType", null);
    /** Column name IssueType */
    public static final String COLUMNNAME_IssueType = "IssueType";

	/**
	 * Set Issue-URL.
	 * URL der Issue, z.B. auf github
	 *
	 * <br>Type: URL
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setIssueURL (java.lang.String IssueURL);

	/**
	 * Get Issue-URL.
	 * URL der Issue, z.B. auf github
	 *
	 * <br>Type: URL
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getIssueURL();

    /** Column definition for IssueURL */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_IssueURL = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "IssueURL", null);
    /** Column name IssueURL */
    public static final String COLUMNNAME_IssueURL = "IssueURL";

	/**
	 * Set Name.
	 *
	 * <br>Type: String
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setName (java.lang.String Name);

	/**
	 * Get Name.
	 *
	 * <br>Type: String
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getName();

    /** Column definition for Name */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Name = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Name", null);
    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/**
	 * Set Verarbeitet.
	 * Checkbox sagt aus, ob der Datensatz verarbeitet wurde.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setProcessed (boolean Processed);

	/**
	 * Get Verarbeitet.
	 * Checkbox sagt aus, ob der Datensatz verarbeitet wurde.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isProcessed();

    /** Column definition for Processed */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Processed = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Processed", null);
    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/**
	 * Set Issue.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setS_Issue_ID (int S_Issue_ID);

	/**
	 * Get Issue.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getS_Issue_ID();

    /** Column definition for S_Issue_ID */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_S_Issue_ID = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "S_Issue_ID", null);
    /** Column name S_Issue_ID */
    public static final String COLUMNNAME_S_Issue_ID = "S_Issue_ID";

	/**
	 * Set Meilenstein.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setS_Milestone_ID (int S_Milestone_ID);

	/**
	 * Get Meilenstein.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getS_Milestone_ID();

	public de.metas.serviceprovider.model.I_S_Milestone getS_Milestone();

	public void setS_Milestone(de.metas.serviceprovider.model.I_S_Milestone S_Milestone);

    /** Column definition for S_Milestone_ID */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, de.metas.serviceprovider.model.I_S_Milestone> COLUMN_S_Milestone_ID = new org.adempiere.model.ModelColumn<I_S_Issue, de.metas.serviceprovider.model.I_S_Milestone>(I_S_Issue.class, "S_Milestone_ID", de.metas.serviceprovider.model.I_S_Milestone.class);
    /** Column name S_Milestone_ID */
    public static final String COLUMNNAME_S_Milestone_ID = "S_Milestone_ID";

	/**
	 * Get Aktualisiert.
	 * Datum, an dem dieser Eintrag aktualisiert wurde
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getUpdated();

    /** Column definition for Updated */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Updated = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Updated", null);
    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/**
	 * Get Aktualisiert durch.
	 * Nutzer, der diesen Eintrag aktualisiert hat
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getUpdatedBy();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/**
	 * Set Suchschlüssel.
	 * Suchschlüssel für den Eintrag im erforderlichen Format - muss eindeutig sein
	 *
	 * <br>Type: String
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setValue (java.lang.String Value);

	/**
	 * Get Suchschlüssel.
	 * Suchschlüssel für den Eintrag im erforderlichen Format - muss eindeutig sein
	 *
	 * <br>Type: String
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.lang.String getValue();

    /** Column definition for Value */
    public static final org.adempiere.model.ModelColumn<I_S_Issue, Object> COLUMN_Value = new org.adempiere.model.ModelColumn<I_S_Issue, Object>(I_S_Issue.class, "Value", null);
    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";
}
