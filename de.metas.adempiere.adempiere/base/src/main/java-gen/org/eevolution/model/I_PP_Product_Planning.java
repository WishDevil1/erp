package org.eevolution.model;


/** Generated Interface for PP_Product_Planning
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public interface I_PP_Product_Planning 
{

    /** TableName=PP_Product_Planning */
    public static final String Table_Name = "PP_Product_Planning";

    /** AD_Table_ID=53020 */
//    public static final int Table_ID = org.compiere.model.MTable.getTable_ID(Table_Name);

//    org.compiere.util.KeyNamePair Model = new org.compiere.util.KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org
     */
//    java.math.BigDecimal accessLevel = java.math.BigDecimal.valueOf(3);

    /** Load Meta Data */

	/**
	 * Get Mandant.
	 * Client/Tenant for this installation.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Client_ID();

	public org.compiere.model.I_AD_Client getAD_Client();

    /** Column definition for AD_Client_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Client> COLUMN_AD_Client_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Client>(I_PP_Product_Planning.class, "AD_Client_ID", org.compiere.model.I_AD_Client.class);
    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/**
	 * Set Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setAD_Org_ID (int AD_Org_ID);

	/**
	 * Get Sektion.
	 * Organisatorische Einheit des Mandanten
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getAD_Org_ID();

	public org.compiere.model.I_AD_Org getAD_Org();

	public void setAD_Org(org.compiere.model.I_AD_Org AD_Org);

    /** Column definition for AD_Org_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Org> COLUMN_AD_Org_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Org>(I_PP_Product_Planning.class, "AD_Org_ID", org.compiere.model.I_AD_Org.class);
    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/**
	 * Set Workflow.
	 * Workflow or combination of tasks
	 *
	 * <br>Type: Table
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setAD_Workflow_ID (int AD_Workflow_ID);

	/**
	 * Get Workflow.
	 * Workflow or combination of tasks
	 *
	 * <br>Type: Table
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getAD_Workflow_ID();

	public org.compiere.model.I_AD_Workflow getAD_Workflow();

	public void setAD_Workflow(org.compiere.model.I_AD_Workflow AD_Workflow);

    /** Column definition for AD_Workflow_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Workflow> COLUMN_AD_Workflow_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_Workflow>(I_PP_Product_Planning.class, "AD_Workflow_ID", org.compiere.model.I_AD_Workflow.class);
    /** Column name AD_Workflow_ID */
    public static final String COLUMNNAME_AD_Workflow_ID = "AD_Workflow_ID";

	/**
	 * Set Geschäftspartner.
	 * Bezeichnet einen Geschäftspartner
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/**
	 * Get Geschäftspartner.
	 * Bezeichnet einen Geschäftspartner
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner();

	public void setC_BPartner(org.compiere.model.I_C_BPartner C_BPartner);

    /** Column definition for C_BPartner_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_C_BPartner> COLUMN_C_BPartner_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_C_BPartner>(I_PP_Product_Planning.class, "C_BPartner_ID", org.compiere.model.I_C_BPartner.class);
    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/**
	 * Get Erstellt.
	 * Date this record was created
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getCreated();

    /** Column definition for Created */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Created = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Created", null);
    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/**
	 * Get Erstellt durch.
	 * User who created this records
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getCreatedBy();

    /** Column definition for CreatedBy */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User> COLUMN_CreatedBy = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User>(I_PP_Product_Planning.class, "CreatedBy", org.compiere.model.I_AD_User.class);
    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/**
	 * Set Network Distribution.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setDD_NetworkDistribution_ID (int DD_NetworkDistribution_ID);

	/**
	 * Get Network Distribution.
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getDD_NetworkDistribution_ID();

	public org.eevolution.model.I_DD_NetworkDistribution getDD_NetworkDistribution();

	public void setDD_NetworkDistribution(org.eevolution.model.I_DD_NetworkDistribution DD_NetworkDistribution);

    /** Column definition for DD_NetworkDistribution_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.eevolution.model.I_DD_NetworkDistribution> COLUMN_DD_NetworkDistribution_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.eevolution.model.I_DD_NetworkDistribution>(I_PP_Product_Planning.class, "DD_NetworkDistribution_ID", org.eevolution.model.I_DD_NetworkDistribution.class);
    /** Column name DD_NetworkDistribution_ID */
    public static final String COLUMNNAME_DD_NetworkDistribution_ID = "DD_NetworkDistribution_ID";

	/**
	 * Set Zugesicherte Lieferzeit.
	 * Promised days between order and delivery
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setDeliveryTime_Promised (java.math.BigDecimal DeliveryTime_Promised);

	/**
	 * Get Zugesicherte Lieferzeit.
	 * Promised days between order and delivery
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getDeliveryTime_Promised();

    /** Column definition for DeliveryTime_Promised */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_DeliveryTime_Promised = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "DeliveryTime_Promised", null);
    /** Column name DeliveryTime_Promised */
    public static final String COLUMNNAME_DeliveryTime_Promised = "DeliveryTime_Promised";

	/**
	 * Set Aktiv.
	 * The record is active in the system
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsActive (boolean IsActive);

	/**
	 * Get Aktiv.
	 * The record is active in the system
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isActive();

    /** Column definition for IsActive */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsActive = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsActive", null);
    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/**
	 * Set Attributabhängig.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsAttributeDependant (boolean IsAttributeDependant);

	/**
	 * Get Attributabhängig.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isAttributeDependant();

    /** Column definition for IsAttributeDependant */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsAttributeDependant = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsAttributeDependant", null);
    /** Column name IsAttributeDependant */
    public static final String COLUMNNAME_IsAttributeDependant = "IsAttributeDependant";

	/**
	 * Set Beleg erstellen.
	 * Legt fest, ob das System die betreffenden Belege (z.B. Produktionsaufträge) gegebenenfalls direkt erstellen soll.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsCreatePlan (boolean IsCreatePlan);

	/**
	 * Get Beleg erstellen.
	 * Legt fest, ob das System die betreffenden Belege (z.B. Produktionsaufträge) gegebenenfalls direkt erstellen soll.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isCreatePlan();

    /** Column definition for IsCreatePlan */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsCreatePlan = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsCreatePlan", null);
    /** Column name IsCreatePlan */
    public static final String COLUMNNAME_IsCreatePlan = "IsCreatePlan";

	/**
	 * Set Beleg fertig stellen.
	 * Legt fest, ob ggf erstellte Belege (z.B. Produktionsaufträge) auch direkt automatisch fertig gestellt werden sollen.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsDocComplete (boolean IsDocComplete);

	/**
	 * Get Beleg fertig stellen.
	 * Legt fest, ob ggf erstellte Belege (z.B. Produktionsaufträge) auch direkt automatisch fertig gestellt werden sollen.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isDocComplete();

    /** Column definition for IsDocComplete */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsDocComplete = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsDocComplete", null);
    /** Column name IsDocComplete */
    public static final String COLUMNNAME_IsDocComplete = "IsDocComplete";

	/**
	 * Set Wird produziert.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setIsManufactured (java.lang.String IsManufactured);

	/**
	 * Get Wird produziert.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getIsManufactured();

    /** Column definition for IsManufactured */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsManufactured = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsManufactured", null);
    /** Column name IsManufactured */
    public static final String COLUMNNAME_IsManufactured = "IsManufactured";

	/**
	 * Set Is MPS.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setIsMPS (boolean IsMPS);

	/**
	 * Get Is MPS.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public boolean isMPS();

    /** Column definition for IsMPS */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsMPS = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsMPS", null);
    /** Column name IsMPS */
    public static final String COLUMNNAME_IsMPS = "IsMPS";

	/**
	 * Set Phantom.
	 * Phantom Component
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsPhantom (boolean IsPhantom);

	/**
	 * Get Phantom.
	 * Phantom Component
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isPhantom();

    /** Column definition for IsPhantom */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsPhantom = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsPhantom", null);
    /** Column name IsPhantom */
    public static final String COLUMNNAME_IsPhantom = "IsPhantom";

	/**
	 * Set Sofort Kommissionieren wenn möglich.
	 * Falls "Ja" und ein Bestand wird für einen bestimmten Lieferdispo-Eintrag bereit gestellt oder produziert, dann wird dieser sofort zugeordnet und als kommissioniert markiert.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setIsPickDirectlyIfFeasible (boolean IsPickDirectlyIfFeasible);

	/**
	 * Get Sofort Kommissionieren wenn möglich.
	 * Falls "Ja" und ein Bestand wird für einen bestimmten Lieferdispo-Eintrag bereit gestellt oder produziert, dann wird dieser sofort zugeordnet und als kommissioniert markiert.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isPickDirectlyIfFeasible();

    /** Column definition for IsPickDirectlyIfFeasible */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsPickDirectlyIfFeasible = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsPickDirectlyIfFeasible", null);
    /** Column name IsPickDirectlyIfFeasible */
    public static final String COLUMNNAME_IsPickDirectlyIfFeasible = "IsPickDirectlyIfFeasible";

	/**
	 * Set Eingekauft.
	 * Die Organisation kauft dieses Produkt ein
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setIsPurchased (java.lang.String IsPurchased);

	/**
	 * Get Eingekauft.
	 * Die Organisation kauft dieses Produkt ein
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getIsPurchased();

    /** Column definition for IsPurchased */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_IsPurchased = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "IsPurchased", null);
    /** Column name IsPurchased */
    public static final String COLUMNNAME_IsPurchased = "IsPurchased";

	/**
	 * Set Merkmale.
	 * Merkmals Ausprägungen zum Produkt
	 *
	 * <br>Type: PAttribute
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID);

	/**
	 * Get Merkmale.
	 * Merkmals Ausprägungen zum Produkt
	 *
	 * <br>Type: PAttribute
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getM_AttributeSetInstance_ID();

	public org.compiere.model.I_M_AttributeSetInstance getM_AttributeSetInstance();

	public void setM_AttributeSetInstance(org.compiere.model.I_M_AttributeSetInstance M_AttributeSetInstance);

    /** Column definition for M_AttributeSetInstance_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_AttributeSetInstance> COLUMN_M_AttributeSetInstance_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_AttributeSetInstance>(I_PP_Product_Planning.class, "M_AttributeSetInstance_ID", org.compiere.model.I_M_AttributeSetInstance.class);
    /** Column name M_AttributeSetInstance_ID */
    public static final String COLUMNNAME_M_AttributeSetInstance_ID = "M_AttributeSetInstance_ID";

	/**
	 * Set Produkt.
	 * Produkt, Leistung, Artikel
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setM_Product_ID (int M_Product_ID);

	/**
	 * Get Produkt.
	 * Produkt, Leistung, Artikel
	 *
	 * <br>Type: Search
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getM_Product_ID();

	public org.compiere.model.I_M_Product getM_Product();

	public void setM_Product(org.compiere.model.I_M_Product M_Product);

    /** Column definition for M_Product_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_Product> COLUMN_M_Product_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_Product>(I_PP_Product_Planning.class, "M_Product_ID", org.compiere.model.I_M_Product.class);
    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/**
	 * Set Lager.
	 * Storage Warehouse and Service Point
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setM_Warehouse_ID (int M_Warehouse_ID);

	/**
	 * Get Lager.
	 * Storage Warehouse and Service Point
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getM_Warehouse_ID();

	public org.compiere.model.I_M_Warehouse getM_Warehouse();

	public void setM_Warehouse(org.compiere.model.I_M_Warehouse M_Warehouse);

    /** Column definition for M_Warehouse_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_Warehouse> COLUMN_M_Warehouse_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_M_Warehouse>(I_PP_Product_Planning.class, "M_Warehouse_ID", org.compiere.model.I_M_Warehouse.class);
    /** Column name M_Warehouse_ID */
    public static final String COLUMNNAME_M_Warehouse_ID = "M_Warehouse_ID";

	/**
	 * Set Maximum Order Qty.
	 * Maximum order quantity in UOM
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Max (java.math.BigDecimal Order_Max);

	/**
	 * Get Maximum Order Qty.
	 * Maximum order quantity in UOM
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getOrder_Max();

    /** Column definition for Order_Max */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Max = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Max", null);
    /** Column name Order_Max */
    public static final String COLUMNNAME_Order_Max = "Order_Max";

	/**
	 * Set Mindestbestellmenge.
	 * Minimum order quantity in UOM
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Min (java.math.BigDecimal Order_Min);

	/**
	 * Get Mindestbestellmenge.
	 * Minimum order quantity in UOM
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getOrder_Min();

    /** Column definition for Order_Min */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Min = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Min", null);
    /** Column name Order_Min */
    public static final String COLUMNNAME_Order_Min = "Order_Min";

	/**
	 * Set Packungsgröße.
	 * Package order size in UOM (e.g. order set of 5 units)
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Pack (java.math.BigDecimal Order_Pack);

	/**
	 * Get Packungsgröße.
	 * Package order size in UOM (e.g. order set of 5 units)
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getOrder_Pack();

    /** Column definition for Order_Pack */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Pack = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Pack", null);
    /** Column name Order_Pack */
    public static final String COLUMNNAME_Order_Pack = "Order_Pack";

	/**
	 * Set Order Period.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Period (java.math.BigDecimal Order_Period);

	/**
	 * Get Order Period.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getOrder_Period();

    /** Column definition for Order_Period */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Period = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Period", null);
    /** Column name Order_Period */
    public static final String COLUMNNAME_Order_Period = "Order_Period";

	/**
	 * Set Order Policy.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Policy (java.lang.String Order_Policy);

	/**
	 * Get Order Policy.
	 *
	 * <br>Type: List
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getOrder_Policy();

    /** Column definition for Order_Policy */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Policy = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Policy", null);
    /** Column name Order_Policy */
    public static final String COLUMNNAME_Order_Policy = "Order_Policy";

	/**
	 * Set Order Qty.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setOrder_Qty (java.math.BigDecimal Order_Qty);

	/**
	 * Get Order Qty.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getOrder_Qty();

    /** Column definition for Order_Qty */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Order_Qty = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Order_Qty", null);
    /** Column name Order_Qty */
    public static final String COLUMNNAME_Order_Qty = "Order_Qty";

	/**
	 * Set Planner.
	 *
	 * <br>Type: Table
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setPlanner_ID (int Planner_ID);

	/**
	 * Get Planner.
	 *
	 * <br>Type: Table
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getPlanner_ID();

	public org.compiere.model.I_AD_User getPlanner();

	public void setPlanner(org.compiere.model.I_AD_User Planner);

    /** Column definition for Planner_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User> COLUMN_Planner_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User>(I_PP_Product_Planning.class, "Planner_ID", org.compiere.model.I_AD_User.class);
    /** Column name Planner_ID */
    public static final String COLUMNNAME_Planner_ID = "Planner_ID";

	/**
	 * Set Aggregate on BPartner Level.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setPP_POQ_AggregateOnBPartnerLevel (boolean PP_POQ_AggregateOnBPartnerLevel);

	/**
	 * Get Aggregate on BPartner Level.
	 *
	 * <br>Type: YesNo
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public boolean isPP_POQ_AggregateOnBPartnerLevel();

    /** Column definition for PP_POQ_AggregateOnBPartnerLevel */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_PP_POQ_AggregateOnBPartnerLevel = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "PP_POQ_AggregateOnBPartnerLevel", null);
    /** Column name PP_POQ_AggregateOnBPartnerLevel */
    public static final String COLUMNNAME_PP_POQ_AggregateOnBPartnerLevel = "PP_POQ_AggregateOnBPartnerLevel";

	/**
	 * Set BOM & Formula.
	 * BOM & Formula
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setPP_Product_BOM_ID (int PP_Product_BOM_ID);

	/**
	 * Get BOM & Formula.
	 * BOM & Formula
	 *
	 * <br>Type: TableDir
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getPP_Product_BOM_ID();

	public org.eevolution.model.I_PP_Product_BOM getPP_Product_BOM();

	public void setPP_Product_BOM(org.eevolution.model.I_PP_Product_BOM PP_Product_BOM);

    /** Column definition for PP_Product_BOM_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.eevolution.model.I_PP_Product_BOM> COLUMN_PP_Product_BOM_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.eevolution.model.I_PP_Product_BOM>(I_PP_Product_Planning.class, "PP_Product_BOM_ID", org.eevolution.model.I_PP_Product_BOM.class);
    /** Column name PP_Product_BOM_ID */
    public static final String COLUMNNAME_PP_Product_BOM_ID = "PP_Product_BOM_ID";

	/**
	 * Set Product Planning.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public void setPP_Product_Planning_ID (int PP_Product_Planning_ID);

	/**
	 * Get Product Planning.
	 *
	 * <br>Type: ID
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getPP_Product_Planning_ID();

    /** Column definition for PP_Product_Planning_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_PP_Product_Planning_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "PP_Product_Planning_ID", null);
    /** Column name PP_Product_Planning_ID */
    public static final String COLUMNNAME_PP_Product_Planning_ID = "PP_Product_Planning_ID";

	/**
	 * Set Safety Stock Qty.
	 * Safety stock is a term used to describe a level of stock that is maintained below the cycle stock to buffer against stock-outs
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setSafetyStock (java.math.BigDecimal SafetyStock);

	/**
	 * Get Safety Stock Qty.
	 * Safety stock is a term used to describe a level of stock that is maintained below the cycle stock to buffer against stock-outs
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getSafetyStock();

    /** Column definition for SafetyStock */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_SafetyStock = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "SafetyStock", null);
    /** Column name SafetyStock */
    public static final String COLUMNNAME_SafetyStock = "SafetyStock";

	/**
	 * Set Reihenfolge.
	 * Zur Bestimmung der Reihenfolge der Einträge;
 die kleinste Zahl kommt zuerst
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setSeqNo (int SeqNo);

	/**
	 * Get Reihenfolge.
	 * Zur Bestimmung der Reihenfolge der Einträge;
 die kleinste Zahl kommt zuerst
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getSeqNo();

    /** Column definition for SeqNo */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_SeqNo = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "SeqNo", null);
    /** Column name SeqNo */
    public static final String COLUMNNAME_SeqNo = "SeqNo";

	/**
	 * Set Ressource.
	 * Resource
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setS_Resource_ID (int S_Resource_ID);

	/**
	 * Get Ressource.
	 * Resource
	 *
	 * <br>Type: Search
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getS_Resource_ID();

	public org.compiere.model.I_S_Resource getS_Resource();

	public void setS_Resource(org.compiere.model.I_S_Resource S_Resource);

    /** Column definition for S_Resource_ID */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_S_Resource> COLUMN_S_Resource_ID = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_S_Resource>(I_PP_Product_Planning.class, "S_Resource_ID", org.compiere.model.I_S_Resource.class);
    /** Column name S_Resource_ID */
    public static final String COLUMNNAME_S_Resource_ID = "S_Resource_ID";

	/**
	 * Set StorageAttributesKey (technical).
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setStorageAttributesKey (java.lang.String StorageAttributesKey);

	/**
	 * Get StorageAttributesKey (technical).
	 *
	 * <br>Type: String
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.lang.String getStorageAttributesKey();

    /** Column definition for StorageAttributesKey */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_StorageAttributesKey = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "StorageAttributesKey", null);
    /** Column name StorageAttributesKey */
    public static final String COLUMNNAME_StorageAttributesKey = "StorageAttributesKey";

	/**
	 * Set Time Fence.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setTimeFence (java.math.BigDecimal TimeFence);

	/**
	 * Get Time Fence.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getTimeFence();

    /** Column definition for TimeFence */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_TimeFence = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "TimeFence", null);
    /** Column name TimeFence */
    public static final String COLUMNNAME_TimeFence = "TimeFence";

	/**
	 * Set Transfert Time.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setTransfertTime (java.math.BigDecimal TransfertTime);

	/**
	 * Get Transfert Time.
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getTransfertTime();

    /** Column definition for TransfertTime */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_TransfertTime = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "TransfertTime", null);
    /** Column name TransfertTime */
    public static final String COLUMNNAME_TransfertTime = "TransfertTime";

	/**
	 * Get Aktualisiert.
	 * Date this record was updated
	 *
	 * <br>Type: DateTime
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public java.sql.Timestamp getUpdated();

    /** Column definition for Updated */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Updated = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Updated", null);
    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/**
	 * Get Aktualisiert durch.
	 * User who updated this records
	 *
	 * <br>Type: Table
	 * <br>Mandatory: true
	 * <br>Virtual Column: false
	 */
	public int getUpdatedBy();

    /** Column definition for UpdatedBy */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User> COLUMN_UpdatedBy = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, org.compiere.model.I_AD_User>(I_PP_Product_Planning.class, "UpdatedBy", org.compiere.model.I_AD_User.class);
    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/**
	 * Set Working Time.
	 * Workflow Simulation Execution Time
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setWorkingTime (java.math.BigDecimal WorkingTime);

	/**
	 * Get Working Time.
	 * Workflow Simulation Execution Time
	 *
	 * <br>Type: Quantity
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public java.math.BigDecimal getWorkingTime();

    /** Column definition for WorkingTime */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_WorkingTime = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "WorkingTime", null);
    /** Column name WorkingTime */
    public static final String COLUMNNAME_WorkingTime = "WorkingTime";

	/**
	 * Set Yield %.
	 * The Yield is the percentage of a lot that is expected to be of acceptable wuality may fall below 100 percent
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public void setYield (int Yield);

	/**
	 * Get Yield %.
	 * The Yield is the percentage of a lot that is expected to be of acceptable wuality may fall below 100 percent
	 *
	 * <br>Type: Integer
	 * <br>Mandatory: false
	 * <br>Virtual Column: false
	 */
	public int getYield();

    /** Column definition for Yield */
    public static final org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object> COLUMN_Yield = new org.adempiere.model.ModelColumn<I_PP_Product_Planning, Object>(I_PP_Product_Planning.class, "Yield", null);
    /** Column name Yield */
    public static final String COLUMNNAME_Yield = "Yield";
}
