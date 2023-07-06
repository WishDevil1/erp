Feature: Call order contract

  Background:
    Given infrastructure and metasfresh are running
    And the existing user with login 'metasfresh' receives a random a API token for the existing role with name 'WebUI'
    And metasfresh has date and time 2022-03-01T13:30:13+01:00[Europe/Berlin]
    And set sys config boolean value true for sys config SKIP_WP_PROCESSOR_FOR_AUTOMATION

  @Id:S0282_100
  @from:cucumber
  Scenario: Happy flow for contract module log - purchase order with two lines completed => validate the two created Log Entries
    Given metasfresh contains M_PricingSystems
      | Identifier             | Name                              | Value                             |
      | moduleLogPricingSystem | moduleLogPricingSystem_06292023_2 | moduleLogPricingSystem_06292023_2 |
    And metasfresh contains M_Products:
      | Identifier            | Name                                  |
      | module_log_product_PO | module_log_product_PO_test_06292023_2 |
    And metasfresh contains M_PriceLists
      | Identifier     | M_PricingSystem_ID.Identifier | OPT.C_Country.CountryCode | C_Currency.ISO_Code | Name                      | SOTrx | IsTaxIncluded | PricePrecision |
      | moduleLogPL_PO | moduleLogPricingSystem        | DE                        | EUR                 | moduleLogPL_PO_06292023_2 | false | false         | 2              |
    And metasfresh contains M_PriceList_Versions
      | Identifier   | M_PriceList_ID.Identifier | Name                    | ValidFrom  |
      | moduleLogPLV | moduleLogPL_PO            | moduleLogPLV_06292023_2 | 2022-02-01 |
    And metasfresh contains M_ProductPrices
      | Identifier  | M_PriceList_Version_ID.Identifier | M_Product_ID.Identifier | PriceStd | C_UOM_ID.X12DE355 | C_TaxCategory_ID.InternalName |
      | moduleLogPP | moduleLogPLV                      | module_log_product_PO   | 2.00     | PCE               | Normal                        |

    And load M_Warehouse:
      | M_Warehouse_ID.Identifier | Value        |
      | warehouseStd              | StdWarehouse |
    And metasfresh contains C_BPartners:
      | Identifier     | Name                      | OPT.IsVendor | M_PricingSystem_ID.Identifier | OPT.C_PaymentTerm_ID.Value |
      | bp_moduleLogPO | bp_moduleLogPO_06292023_1 | Y            | moduleLogPricingSystem        | 1000002                    |
    And metasfresh contains C_BPartner_Locations:
      | Identifier              | GLN           | C_BPartner_ID.Identifier | OPT.IsShipToDefault | OPT.IsBillToDefault |
      | bp_moduleLogPO_Location | 5803098505483 | bp_moduleLogPO           | true                | true                |

    And load C_Calendar from metasfresh:
      | C_Calendar_ID.Identifier | Name                   |
      | harvesting_calendar      | Kalender - Verrechnung |

    And load C_Year from metasfresh:
      | C_Year_ID.Identifier | FiscalYear |
      | year                 | 2022       |

    And metasfresh contains ModCntr_Settings:
      | ModCntr_Settings_ID.Identifier | Name                    | M_Product_ID.Identifier | C_Calendar_ID.Identifier | C_Year_ID.Identifier | OPT.M_PricingSystem_ID.Identifier |
      | modCntr_settings_1             | testSettings_06292023_2 | module_log_product_PO   | harvesting_calendar      | year                 | moduleLogPricingSystem            |

    And metasfresh contains ModCntr_Types:
      | ModCntr_Type_ID.Identifier | Name              | Value             | Classname                                                               |
      | modCntr_type_1             | poLine_06292023_2 | poLine_06292023_2 | de.metas.contracts.modular.impl.PurchaseOrderLineModularContractHandler |

    And metasfresh contains ModCntr_Modules:
      | ModCntr_Module_ID.Identifier | SeqNo | Name                  | M_Product_ID.Identifier | InvoicingGroup | ModCntr_Settings_ID.Identifier | ModCntr_Type_ID.Identifier |
      | modCntr_module_1             | 10    | moduleTest_06292023_2 | module_log_product_PO   | Kosten         | modCntr_settings_1             | modCntr_type_1             |

    And metasfresh contains C_Flatrate_Conditions:
      | C_Flatrate_Conditions_ID.Identifier | Name                              | Type_Conditions | OPT.M_PricingSystem_ID.Identifier | OPT.OnFlatrateTermExtend | OPT.ModCntr_Settings_ID.Identifier |
      | moduleLogConditions_PO              | moduleLogConditions_po_06292023_2 | ModularContract | moduleLogPricingSystem            | Ca                       | modCntr_settings_1                 |

    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | C_BPartner_ID.Identifier | DateOrdered | OPT.DocBaseType | OPT.POReference                    |
      | po_order   | false   | bp_moduleLogPO           | 2022-03-03  | POO             | poModuleLogContract_ref_06292023_2 |
    And metasfresh contains C_OrderLines:
      | Identifier     | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Flatrate_Conditions_ID.Identifier |
      | po_orderLine   | po_order              | module_log_product_PO   | 1000       | moduleLogConditions_PO                  |
      | po_orderLine_2 | po_order              | module_log_product_PO   | 500        | moduleLogConditions_PO                  |

    And metasfresh contains C_Flatrate_Terms:
      | Identifier          | C_Flatrate_Conditions_ID.Identifier | Bill_BPartner_ID.Identifier | StartDate  | EndDate    | OPT.M_Product_ID.Identifier | OPT.C_OrderLine_Term_ID.Identifier |
      | moduleLogContract_1 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine                       |
      | moduleLogContract_2 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine_2                     |

    When the order identified by po_order is completed

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty  | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine         | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 1000 | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 500  | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |

  @Id:S0282_200
  @from:cucumber
  Scenario: Happy flow for contract module log - purchase order with two lines voided
    Given metasfresh contains M_PricingSystems
      | Identifier             | Name                              | Value                             |
      | moduleLogPricingSystem | moduleLogPricingSystem_06292023_4 | moduleLogPricingSystem_06292023_4 |
    And metasfresh contains M_Products:
      | Identifier            | Name                                  |
      | module_log_product_PO | module_log_product_PO_test_06292023_4 |
    And metasfresh contains M_PriceLists
      | Identifier     | M_PricingSystem_ID.Identifier | OPT.C_Country.CountryCode | C_Currency.ISO_Code | Name                      | SOTrx | IsTaxIncluded | PricePrecision |
      | moduleLogPL_PO | moduleLogPricingSystem        | DE                        | EUR                 | moduleLogPL_PO_06292023_4 | false | false         | 2              |
    And metasfresh contains M_PriceList_Versions
      | Identifier   | M_PriceList_ID.Identifier | Name                    | ValidFrom  |
      | moduleLogPLV | moduleLogPL_PO            | moduleLogPLV_06292023_4 | 2022-02-01 |
    And metasfresh contains M_ProductPrices
      | Identifier  | M_PriceList_Version_ID.Identifier | M_Product_ID.Identifier | PriceStd | C_UOM_ID.X12DE355 | C_TaxCategory_ID.InternalName |
      | moduleLogPP | moduleLogPLV                      | module_log_product_PO   | 2.00     | PCE               | Normal                        |

    And load M_Warehouse:
      | M_Warehouse_ID.Identifier | Value        |
      | warehouseStd              | StdWarehouse |
    And metasfresh contains C_BPartners:
      | Identifier     | Name                      | OPT.IsVendor | M_PricingSystem_ID.Identifier | OPT.C_PaymentTerm_ID.Value |
      | bp_moduleLogPO | bp_moduleLogPO_06292023_4 | Y            | moduleLogPricingSystem        | 1000002                    |
    And metasfresh contains C_BPartner_Locations:
      | Identifier              | GLN           | C_BPartner_ID.Identifier | OPT.IsShipToDefault | OPT.IsBillToDefault |
      | bp_moduleLogPO_Location | 5813098505483 | bp_moduleLogPO           | true                | true                |

    And load C_Calendar from metasfresh:
      | C_Calendar_ID.Identifier | Name                   |
      | harvesting_calendar      | Kalender - Verrechnung |

    And load C_Year from metasfresh:
      | C_Year_ID.Identifier | FiscalYear |
      | year                 | 2022       |

    And metasfresh contains ModCntr_Settings:
      | ModCntr_Settings_ID.Identifier | Name                    | M_Product_ID.Identifier | C_Calendar_ID.Identifier | C_Year_ID.Identifier | OPT.M_PricingSystem_ID.Identifier |
      | modCntr_settings_1             | testSettings_06292023_4 | module_log_product_PO   | harvesting_calendar      | year                 | moduleLogPricingSystem            |

    And metasfresh contains ModCntr_Types:
      | ModCntr_Type_ID.Identifier | Name              | Value             | Classname                                                               |
      | modCntr_type_1             | poLine_06292023_4 | poLine_06292023_4 | de.metas.contracts.modular.impl.PurchaseOrderLineModularContractHandler |

    And metasfresh contains ModCntr_Modules:
      | ModCntr_Module_ID.Identifier | SeqNo | Name                  | M_Product_ID.Identifier | InvoicingGroup | ModCntr_Settings_ID.Identifier | ModCntr_Type_ID.Identifier |
      | modCntr_module_1             | 10    | moduleTest_06292023_4 | module_log_product_PO   | Kosten         | modCntr_settings_1             | modCntr_type_1             |

    And metasfresh contains C_Flatrate_Conditions:
      | C_Flatrate_Conditions_ID.Identifier | Name                              | Type_Conditions | OPT.M_PricingSystem_ID.Identifier | OPT.OnFlatrateTermExtend | OPT.ModCntr_Settings_ID.Identifier |
      | moduleLogConditions_PO              | moduleLogConditions_po_06292023_4 | ModularContract | moduleLogPricingSystem            | Ca                       | modCntr_settings_1                 |

    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | C_BPartner_ID.Identifier | DateOrdered | OPT.DocBaseType | OPT.POReference                    |
      | po_order   | false   | bp_moduleLogPO           | 2022-03-03  | POO             | poModuleLogContract_ref_06292023_4 |
    And metasfresh contains C_OrderLines:
      | Identifier     | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Flatrate_Conditions_ID.Identifier |
      | po_orderLine   | po_order              | module_log_product_PO   | 1000       | moduleLogConditions_PO                  |
      | po_orderLine_2 | po_order              | module_log_product_PO   | 500        | moduleLogConditions_PO                  |

    And metasfresh contains C_Flatrate_Terms:
      | Identifier          | C_Flatrate_Conditions_ID.Identifier | Bill_BPartner_ID.Identifier | StartDate  | EndDate    | OPT.M_Product_ID.Identifier | OPT.C_OrderLine_Term_ID.Identifier |
      | moduleLogContract_1 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine                       |
      | moduleLogContract_2 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine_2                     |

    When the order identified by po_order is completed

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty  | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine         | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 1000 | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 500  | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |

    And the order identified by po_order is voided

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty   | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine         | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 1000  | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine         | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | -1000 | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | -2000      | year                              |
      | log_3                     | po_orderLine_2       | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 500   | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |
      | log_4                     | po_orderLine_2       | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | -500  | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | -1000      | year                              |

  @Id:S0282_300
  @from:cucumber
  Scenario: REACTIVATE | REVERSE purchase order with linked modular contract 
    Given metasfresh contains M_PricingSystems
      | Identifier             | Name                              | Value                             |
      | moduleLogPricingSystem | moduleLogPricingSystem_07032023_2 | moduleLogPricingSystem_07032023_2 |
    And metasfresh contains M_Products:
      | Identifier            | Name                                  |
      | module_log_product_PO | module_log_product_PO_test_07032023_2 |
    And metasfresh contains M_PriceLists
      | Identifier     | M_PricingSystem_ID.Identifier | OPT.C_Country.CountryCode | C_Currency.ISO_Code | Name                      | SOTrx | IsTaxIncluded | PricePrecision |
      | moduleLogPL_PO | moduleLogPricingSystem        | DE                        | EUR                 | moduleLogPL_PO_07032023_2 | false | false         | 2              |
    And metasfresh contains M_PriceList_Versions
      | Identifier   | M_PriceList_ID.Identifier | Name                    | ValidFrom  |
      | moduleLogPLV | moduleLogPL_PO            | moduleLogPLV_07032023_2 | 2022-02-01 |
    And metasfresh contains M_ProductPrices
      | Identifier  | M_PriceList_Version_ID.Identifier | M_Product_ID.Identifier | PriceStd | C_UOM_ID.X12DE355 | C_TaxCategory_ID.InternalName |
      | moduleLogPP | moduleLogPLV                      | module_log_product_PO   | 2.00     | PCE               | Normal                        |

    And load M_Warehouse:
      | M_Warehouse_ID.Identifier | Value        |
      | warehouseStd              | StdWarehouse |
    And metasfresh contains C_BPartners:
      | Identifier     | Name                      | OPT.IsVendor | M_PricingSystem_ID.Identifier | OPT.C_PaymentTerm_ID.Value |
      | bp_moduleLogPO | bp_moduleLogPO_07032023_2 | Y            | moduleLogPricingSystem        | 1000002                    |
    And metasfresh contains C_BPartner_Locations:
      | Identifier              | GLN           | C_BPartner_ID.Identifier | OPT.IsShipToDefault | OPT.IsBillToDefault |
      | bp_moduleLogPO_Location | 5823098505483 | bp_moduleLogPO           | true                | true                |

    And load C_Calendar from metasfresh:
      | C_Calendar_ID.Identifier | Name                   |
      | harvesting_calendar      | Kalender - Verrechnung |

    And load C_Year from metasfresh:
      | C_Year_ID.Identifier | FiscalYear |
      | year                 | 2022       |

    And metasfresh contains ModCntr_Settings:
      | ModCntr_Settings_ID.Identifier | Name                    | M_Product_ID.Identifier | C_Calendar_ID.Identifier | C_Year_ID.Identifier | OPT.M_PricingSystem_ID.Identifier |
      | modCntr_settings_1             | testSettings_07032023_2 | module_log_product_PO   | harvesting_calendar      | year                 | moduleLogPricingSystem            |

    And metasfresh contains ModCntr_Types:
      | ModCntr_Type_ID.Identifier | Name              | Value             | Classname                                                               |
      | modCntr_type_1             | poLine_07032023_2 | poLine_07032023_2 | de.metas.contracts.modular.impl.PurchaseOrderLineModularContractHandler |

    And metasfresh contains ModCntr_Modules:
      | ModCntr_Module_ID.Identifier | SeqNo | Name                  | M_Product_ID.Identifier | InvoicingGroup | ModCntr_Settings_ID.Identifier | ModCntr_Type_ID.Identifier |
      | modCntr_module_1             | 10    | moduleTest_07032023_2 | module_log_product_PO   | Kosten         | modCntr_settings_1             | modCntr_type_1             |

    And metasfresh contains C_Flatrate_Conditions:
      | C_Flatrate_Conditions_ID.Identifier | Name                              | Type_Conditions | OPT.M_PricingSystem_ID.Identifier | OPT.OnFlatrateTermExtend | OPT.ModCntr_Settings_ID.Identifier |
      | moduleLogConditions_PO              | moduleLogConditions_po_07032023_2 | ModularContract | moduleLogPricingSystem            | Ca                       | modCntr_settings_1                 |

    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | C_BPartner_ID.Identifier | DateOrdered | OPT.DocBaseType | OPT.POReference                    |
      | po_order   | false   | bp_moduleLogPO           | 2022-03-03  | POO             | poModuleLogContract_ref_07032023_2 |
    And metasfresh contains C_OrderLines:
      | Identifier     | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Flatrate_Conditions_ID.Identifier |
      | po_orderLine   | po_order              | module_log_product_PO   | 1000       | moduleLogConditions_PO                  |
      | po_orderLine_2 | po_order              | module_log_product_PO   | 500        | moduleLogConditions_PO                  |

    And metasfresh contains C_Flatrate_Terms:
      | Identifier          | C_Flatrate_Conditions_ID.Identifier | Bill_BPartner_ID.Identifier | StartDate  | EndDate    | OPT.M_Product_ID.Identifier | OPT.C_OrderLine_Term_ID.Identifier |
      | moduleLogContract_1 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine                       |
      | moduleLogContract_2 | moduleLogConditions_PO              | bp_moduleLogPO              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine_2                     |

    When the order identified by po_order is completed

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty  | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine         | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 1000 | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogPO                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogPO                      | bp_moduleLogPO                  | 500  | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |

    And load AD_Message:
      | Identifier             | Value                                                                                        |
      | reactivate_not_allowed | de.metas.contracts.modular.impl.PurchaseOrderLineModularContractHandler.ReactivateNotAllowed |

    And the order identified by po_order is reactivated expecting error
      | OPT.AD_Message_ID.Identifier |
      | reactivate_not_allowed       |

    And the order identified by po_order is reversed expecting error
      | OPT.AD_Message_ID.Identifier |
      | reactivate_not_allowed       |


  @Id:S0282_400
  @from:cucumber
  Scenario: Contract module log - purchase order -> material receipt
  validate receipt schedule flatrate term is propagated from po
  validate receiptline flatrate term is propagated from receipt schedule
  validate on receipt complete, reactivate and reverse logs are created correctly
  validate on receipt void error is thrown
  validate on receiptLine delete error is thrown if modular contract log exist for this line

    Given metasfresh contains M_PricingSystems
      | Identifier             | Name                              | Value                             |
      | moduleLogPricingSystem | moduleLogPricingSystem_05072023_1 | moduleLogPricingSystem_05072023_1 |
    And metasfresh contains M_Products:
      | Identifier            | Name                             |
      | module_log_product_PO | module_log_product_PO_05072023_1 |
      | module_log_product_MR | module_log_product_MR_05072023_1 |
    And metasfresh contains M_PriceLists
      | Identifier     | M_PricingSystem_ID.Identifier | OPT.C_Country.CountryCode | C_Currency.ISO_Code | Name                      | SOTrx | IsTaxIncluded | PricePrecision |
      | moduleLogPL_MR | moduleLogPricingSystem        | DE                        | EUR                 | moduleLogPL_MR_05072023_1 | false | false         | 2              |
    And metasfresh contains M_PriceList_Versions
      | Identifier   | M_PriceList_ID.Identifier | Name                    | ValidFrom  |
      | moduleLogPLV | moduleLogPL_MR            | moduleLogPLV_05072023_1 | 2022-02-01 |
    And metasfresh contains M_ProductPrices
      | Identifier   | M_PriceList_Version_ID.Identifier | M_Product_ID.Identifier | PriceStd | C_UOM_ID.X12DE355 | C_TaxCategory_ID.InternalName |
      | moduleLogPP  | moduleLogPLV                      | module_log_product_PO   | 2.00     | PCE               | Normal                        |
      | moduleLogPP2 | moduleLogPLV                      | module_log_product_MR   | 2.00     | PCE               | Normal                        |
    And load M_Warehouse:
      | M_Warehouse_ID.Identifier | Value        |
      | warehouseStd              | StdWarehouse |
    And metasfresh contains M_HU_PI:
      | M_HU_PI_ID.Identifier | Name            |
      | huPackingLU           | huPackingLU     |
      | huPackingTU           | huPackingTU     |
      | huPackingVirtualPI    | No Packing Item |
    And metasfresh contains M_HU_PI_Version:
      | M_HU_PI_Version_ID.Identifier | M_HU_PI_ID.Identifier | Name             | HU_UnitType | IsCurrent |
      | packingVersionLU              | huPackingLU           | packingVersionLU | LU          | Y         |
      | packingVersionTU              | huPackingTU           | packingVersionTU | TU          | Y         |
      | packingVersionCU              | huPackingVirtualPI    | No Packing Item  | V           | Y         |
    And metasfresh contains M_HU_PI_Item:
      | M_HU_PI_Item_ID.Identifier | M_HU_PI_Version_ID.Identifier | Qty | ItemType | OPT.Included_HU_PI_ID.Identifier |
      | huPiItemLU                 | packingVersionLU              | 10  | HU       | huPackingTU                      |
      | huPiItemTU                 | packingVersionTU              | 0   | MI       |                                  |
    And metasfresh contains M_HU_PI_Item_Product:
      | M_HU_PI_Item_Product_ID.Identifier | M_HU_PI_Item_ID.Identifier | M_Product_ID.Identifier | Qty | ValidFrom  |
      | huItemPOProduct                    | huPiItemTU                 | module_log_product_PO   | 10  | 2022-02-01 |
      | huItemMRProduct                    | huPiItemTU                 | module_log_product_MR   | 10  | 2022-02-01 |
    And metasfresh contains C_BPartners:
      | Identifier     | Name                      | OPT.IsVendor | M_PricingSystem_ID.Identifier | OPT.C_PaymentTerm_ID.Value |
      | bp_moduleLogMR | bp_moduleLogMR_05072023_1 | Y            | moduleLogPricingSystem        | 1000002                    |
    And metasfresh contains C_BPartner_Locations:
      | Identifier              | GLN           | C_BPartner_ID.Identifier | OPT.IsShipToDefault | OPT.IsBillToDefault |
      | bp_moduleLogMR_Location | 5823098505483 | bp_moduleLogMR           | true                | true                |

    And load C_Calendar from metasfresh:
      | C_Calendar_ID.Identifier | Name                   |
      | harvesting_calendar      | Kalender - Verrechnung |

    And load C_Year from metasfresh:
      | C_Year_ID.Identifier | FiscalYear |
      | year                 | 2022       |

    And metasfresh contains ModCntr_Settings:
      | ModCntr_Settings_ID.Identifier | Name                    | M_Product_ID.Identifier | C_Calendar_ID.Identifier | C_Year_ID.Identifier | OPT.M_PricingSystem_ID.Identifier |
      | modCntr_settings_1             | testSettings_05072023_1 | module_log_product_MR   | harvesting_calendar      | year                 | moduleLogPricingSystem            |

    And metasfresh contains ModCntr_Types:
      | ModCntr_Type_ID.Identifier | Name              | Value             | Classname                                                                 |
      | modCntr_type_1             | poLine_05072023_1 | poLine_05072023_1 | de.metas.contracts.modular.impl.PurchaseOrderLineModularContractHandler   |
      | modCntr_type_2             | mrLine_05072023_1 | mrLine_05072023_1 | de.metas.contracts.modular.impl.MaterialReceiptLineModularContractHandler |

    And metasfresh contains ModCntr_Modules:
      | ModCntr_Module_ID.Identifier | SeqNo | Name                  | M_Product_ID.Identifier | InvoicingGroup | ModCntr_Settings_ID.Identifier | ModCntr_Type_ID.Identifier |
      | modCntr_module_1             | 10    | moduleTest_05072023_1 | module_log_product_PO   | Kosten         | modCntr_settings_1             | modCntr_type_1             |
      | modCntr_module_2             | 20    | moduleTest_05072023_2 | module_log_product_MR   | Kosten         | modCntr_settings_1             | modCntr_type_2             |

    And metasfresh contains C_Flatrate_Conditions:
      | C_Flatrate_Conditions_ID.Identifier | Name                              | Type_Conditions | OPT.M_PricingSystem_ID.Identifier | OPT.OnFlatrateTermExtend | OPT.ModCntr_Settings_ID.Identifier |
      | moduleLogConditions_MR              | moduleLogConditions_po_05072023_1 | ModularContract | moduleLogPricingSystem            | Ca                       | modCntr_settings_1                 |

    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | C_BPartner_ID.Identifier | DateOrdered | OPT.DocBaseType | OPT.POReference                    |
      | po_order   | false   | bp_moduleLogMR           | 2022-03-03  | POO             | mrModuleLogContract_ref_05072023_1 |
    And metasfresh contains C_OrderLines:
      | Identifier     | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Flatrate_Conditions_ID.Identifier |
      | po_orderLine_1 | po_order              | module_log_product_PO   | 1000       | moduleLogConditions_MR                  |
      | po_orderLine_2 | po_order              | module_log_product_MR   | 500        | moduleLogConditions_MR                  |

    And metasfresh contains C_Flatrate_Terms:
      | Identifier          | C_Flatrate_Conditions_ID.Identifier | Bill_BPartner_ID.Identifier | StartDate  | EndDate    | OPT.M_Product_ID.Identifier | OPT.C_OrderLine_Term_ID.Identifier |
      | moduleLogContract_1 | moduleLogConditions_MR              | bp_moduleLogMR              | 2021-10-31 | 2022-10-30 | module_log_product_PO       | po_orderLine_1                     |
      | moduleLogContract_2 | moduleLogConditions_MR              | bp_moduleLogMR              | 2021-10-31 | 2022-10-30 | module_log_product_MR       | po_orderLine_2                     |

    When the order identified by po_order is completed

    Then after not more than 120s, M_ReceiptSchedule are found:
      | M_ReceiptSchedule_ID.Identifier | C_Order_ID.Identifier | C_OrderLine_ID.Identifier | C_BPartner_ID.Identifier | C_BPartner_Location_ID.Identifier | M_Product_ID.Identifier | QtyOrdered | M_Warehouse_ID.Identifier | OPT.C_Flatrate_Term_ID.Identifier |
      | receiptSchedule_05072023_1      | po_order              | po_orderLine_1            | bp_moduleLogMR           | bp_moduleLogMR_Location           | module_log_product_PO   | 1000       | warehouseStd              | moduleLogContract_1               |
      | receiptSchedule_05072023_2      | po_order              | po_orderLine_2            | bp_moduleLogMR           | bp_moduleLogMR_Location           | module_log_product_MR   | 500        | warehouseStd              | moduleLogContract_2               |

    And create M_HU_LUTU_Configuration for M_ReceiptSchedule and generate M_HUs
      | M_HU_LUTU_Configuration_ID.Identifier | M_HU_ID.Identifier | M_ReceiptSchedule_ID.Identifier | IsInfiniteQtyLU | QtyLU | IsInfiniteQtyTU | QtyTU | IsInfiniteQtyCU | QtyCU | M_HU_PI_Item_Product_ID.Identifier | OPT.M_LU_HU_PI_ID.Identifier |
      | huLuTuConfig_1                        | hu_1               | receiptSchedule_05072023_1      | N               | 1     | N               | 1     | N               | 1000  | huItemPOProduct                    | huPackingLU                  |
      | huLuTuConfig_2                        | hu_2               | receiptSchedule_05072023_2      | N               | 1     | N               | 1     | N               | 500   | huItemMRProduct                    | huPackingLU                  |
    When create material receipt
      | M_HU_ID.Identifier | M_ReceiptSchedule_ID.Identifier | M_InOut_ID.Identifier |
      | hu_1               | receiptSchedule_05072023_1      | material_receipt_1    |
      | hu_2               | receiptSchedule_05072023_2      | material_receipt_2    |

    Then validate the created material receipt lines
      | M_InOutLine_ID.Identifier | M_InOut_ID.Identifier | M_Product_ID.Identifier | movementqty | processed | OPT.C_OrderLine_ID.Identifier | OPT.C_Flatrate_Term_ID.Identifier |
      | shipmentLine_1            | material_receipt_1    | module_log_product_PO   | 1000        | true      | po_orderLine_1                | moduleLogContract_1               |
      | shipmentLine_2            | material_receipt_2    | module_log_product_MR   | 500         | true      | po_orderLine_2                | moduleLogContract_2               |

    And ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty  | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000 | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500  | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |
      | log_3                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000 | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_4                     | shipmentLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500  | M_InOutLine | moduleLogContract_2           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |

    When the material receipt identified by material_receipt_1 is reactivated

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty   | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |
      | log_3                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_4                     | shipmentLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | M_InOutLine | moduleLogContract_2           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_5                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | -1000 | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |



    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty   | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |
      | log_3                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_4                     | shipmentLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | M_InOutLine | moduleLogContract_2           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_5                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | -1000 | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_6                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |

    When the material receipt identified by material_receipt_2 is reversed

    Then ModCntr_Logs are found:
      | ModCntr_Log_ID.Identifier | Record_ID.Identifier | OPT.CollectionPoint_BPartner_ID.Identifier | OPT.M_Warehouse_ID.Identifier | OPT.M_Product_ID.Identifier | OPT.Producer_BPartner_ID.Identifier | OPT.Bill_BPartner_ID.Identifier | Qty   | TableName   | C_Flatrate_Term_ID.Identifier | OPT.ModCntr_Type_ID.Identifier | OPT.Processed | OPT.ModCntr_Log_DocumentType | OPT.C_Currency_ID.ISO_Code | OPT.C_UOM_ID.X12DE355 | OPT.Amount | OPT.Harvesting_Year_ID.Identifier |
      | log_1                     | po_orderLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | C_OrderLine | moduleLogContract_1           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 2000       | year                              |
      | log_2                     | po_orderLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | C_OrderLine | moduleLogContract_2           | modCntr_type_1                 | false         | PurchaseOrder                | EUR                        | PCE                   | 1000       | year                              |
      | log_3                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | 1000  | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_4                     | shipmentLine_2       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_MR       | bp_moduleLogMR                      | bp_moduleLogMR                  | 500   | M_InOutLine | moduleLogContract_2           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_5                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | -1000 | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |
      | log_6                     | shipmentLine_1       | bp_moduleLogMR                             | warehouseStd                  | module_log_product_PO       | bp_moduleLogMR                      | bp_moduleLogMR                  | -1000 | M_InOutLine | moduleLogContract_1           | modCntr_type_2                 | false         | MaterialReceipt              |                            | PCE                   |            | year                              |

    When load AD_Message:
      | Identifier         | Value                                                      |
      | delete_not_allowed | documentLineDeletionErrorBecauseOfRelatedModuleContractLog |

    Then delete M_InOutLine identified by shipmentLine_1 is expecting error
      | OPT.AD_Message_ID.Identifier |
      | delete_not_allowed           |

    And load AD_Message:
      | Identifier       | Value                                  |
      | void_not_allowed | de.metas.contracts.DocActionNotAllowed |

    And the material receipt identified by material_receipt_1 is voided expecting error
      | OPT.AD_Message_ID.Identifier |
      | void_not_allowed             |

