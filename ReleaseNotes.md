
# About this document

This file contains the tasks/issues which we implement in metasfresh, in a chronological fashion (latest first)

Additional notes:
 * The metasfresh source code is hosted at https://github.com/metasfresh/metasfresh
 * The metasfresh website is at http://metasfresh.com/en, http://metasfresh.com/ (german)
 * You can also follow us on twitter: @metasfresh (english), @metasfreshDE (german)

Here come the actual release notes:

# metasfresh 5.5 (2017-15)

**upcoming**

## Features
* metasfresh-backend
  * [#992](https://github.com/metasfresh/metasfresh/issues/992) Allow subscription without shipment

## Fixes
* metasfresh-backend
  * [#695](https://github.com/metasfresh/metasfresh/issues/695) swingUI: provide lib-dirs to access user jars and dlls at runtime
  * [#1057](https://github.com/metasfresh/metasfresh/issues/1057) Role window WebUI
    * Initial Layout configuration for Role maintenance Window.
  * [#1241](https://github.com/metasfresh/metasfresh/issues/1241) metasfresh does not notify procurement-webui about new contracts
  * [#1291](https://github.com/metasfresh/metasfresh/issues/1291) Manufacturing Order Window adjustments
    * Adding further adjustments to new Manufactirung Order in WebUI, allowing an easier User experience.
  * [#1294](https://github.com/metasfresh/metasfresh/issues/1294) Report with Attribute Set Parameter cannot be started anymore
    * Fixing an exception when calling the Attribute Editor in Process Parameters.

# metasfresh 5.4 (2017-14)

## Features
* metasfresh-backend
  * [#741](https://github.com/metasfresh/metasfresh/issues/741) packing material product category config
    * New functionality to be able to define the package material category per organisational unit, so that jasper reports can do the package material groupings because of that.
  * [#995](https://github.com/metasfresh/metasfresh/issues/995) Translation en_US for Material Receipt Candidates Window
    * Adding the initial set of Translation en_US to the material Receipt Candidates Window in WebUI.
  * [#1181](https://github.com/metasfresh/metasfresh/issues/1181) Refine Layout for Attribute Window in WebUI
    * Adding the initial Layout for the Attribute Window in WebUI.
  * [#1182](https://github.com/metasfresh/metasfresh/issues/1182) Create Layout for User Window in WebUI
    * Adding the initial Layout for the User window into WebUI.
  * [#1185](https://github.com/metasfresh/metasfresh/issues/1185) Initial Layout for vendor Invoices in WebUI
    * Adding the initial Layout for vendor Invoice Window in WebUI.
  * [#1205](https://github.com/metasfresh/metasfresh/issues/1205) Provide Default KPI Config
    * Prepared a default KPI Configuration to show an example Dashboard based on elasticsearch and with KPI done with D3JS on the entry screen of metasfresh after login.
  * [#1206](https://github.com/metasfresh/metasfresh/issues/1206) Remove Gebinderückgabe from Shipment Note for mf15 endcustomer
    * Removing the Text for "empties return" in Shipment Documents for default Jasper Docuiment. The Text does not make sense for companies that don't use the empties management functionality.
  * [#1222](https://github.com/metasfresh/metasfresh/issues/1222) Show orderline description only in the first column of the Jasper
    * Enhanced Reports definition, so that orderline desciptions are only shown in the first column, and not overlaying the others anymore.
  * [#1228](https://github.com/metasfresh/metasfresh/issues/1228) create translate properties for footer report
    * New properties file for the footer subreport for Jasper documents. Now allowing to add translations for that.
  * [#1247](https://github.com/metasfresh/metasfresh/issues/1247) Don't try to create empties movements if empties warehouse same as current warehouse
    * Now not creating movements anymore if the source and target warehouse of empty movements are identical.
  * [#1249](https://github.com/metasfresh/metasfresh/issues/1249) Initial Layout for Material Movement Window WebUI
    * Adding the initial Windows Layout for material movements in WebUI.
  * [#1268](https://github.com/metasfresh/metasfresh/issues/1268) Do not print label automatically by default on material receipt
    * Switching off the automatic label printing in material receipt for default configurations.
  * [#1277](https://github.com/metasfresh/metasfresh/issues/1277) Migrate current WebUI Menu from Development to master
    * Migration script for the currents metasfresh webUI menu tree, now showing the new windows.

* metasfresh-webui-frontend  
  * [#541](https://github.com/metasfresh/metasfresh-webui-frontend/issues/541) Hide new and delete included documents when they are not available.
    * New Functionality to show/ not show the new and delete buttons on included tabs/ subtabs if not available.
  
## Fixes
* metasfresh-backend
  * [#1191](https://github.com/metasfresh/metasfresh/issues/1191) small adjustments in jasper documents
    * Adding a few detailed adjustments to the default metasfresh documents layout, especially for cases when generating documents with many lines.
  * [#1222](https://github.com/metasfresh/metasfresh/issues/1222) Show orderline description only in the first column of the Jasper
    * Adjustment of the document line description field now defining boundaries for the field content on the generated Jasper Documents making the content better readable.
  * [#1225](https://github.com/metasfresh/metasfresh/issues/1225) Drop legacy jasper sql logic
    * Maintainance of Jasper Document SQL. Removing legace SQL that's not needed anymore.
  * [#1240](https://github.com/metasfresh/metasfresh/issues/1240) Number-of-copies parameter is ignored in direct print
    * Fixes the document printing copies configuration. The number of copies parameter is now also considered in direct print.
  * [#1244](https://github.com/metasfresh/metasfresh/issues/1244) Shipment Schedule's QtyDeliveredTU is not updated correctly
    * Fixed a minor bug that prevented the update of the QtyDelivered TU Field in Shipment schdules.
  * [#1248](https://github.com/metasfresh/metasfresh/issues/1248) Empties movements are not generated from empties shipment/receipt
    * Fixing a bug that did not create movements for empties receive documents after completion.
  * [#1256](https://github.com/metasfresh/metasfresh/issues/1256) Database tables are created in wrong schema
    * Fixes a Bug that created db tables in wrong schema. Now the tables are created in public schema again.
  * [#1260](https://github.com/metasfresh/metasfresh/issues/1260) DocumentNo not generated for manual invoices in WebUI
    * Adding a minor fix to the customer Invoice Window in WebUI, that prevented the creation of manual Invoices for customers.
  * [#1263](https://github.com/metasfresh/metasfresh/issues/1263) ITrxListener.afterCommit is fired twice with TrxPropagation.REQUIRES_NEW
    * Fixes the double tap of ITrxListener.afterCommit.
  * [#1267](https://github.com/metasfresh/metasfresh/issues/1267) Cannot open ASI editor in Swing
    * The Attrubute Set Instance Widget could not be opened in Swing Client anymore. This Bugfix now enables that again.
  * [#1272](https://github.com/metasfresh/metasfresh/issues/1272) Vendor ADR configuration not initially considered in Orderline
    * Fixing a Bug that prevented the default settings of vendor attributes in orderline.
  * [#1274](https://github.com/metasfresh/metasfresh/issues/1274) webui - allow using the session's remote host name or IP as hostkey
    * New feature that allows to use the sessions host name or IP Address as hostkey for printing rounting and configuration.
  * [#1282](https://github.com/metasfresh/metasfresh/issues/1282) Exception splitting aggregate HU with UOM that has no UOMType
    * Fixes a Bug in Splitting HU action. Now also allowing splitting action with compressed Handling Units that don't have a Unit of measure Type.
  
* metasfresh-webui-api
  * [#277](https://github.com/metasfresh/metasfresh-webui-api/issues/277) Don't export JSONDocument.fields if empty
    * Fixes a Bug that exported empty JSON Document Fields
  * [#283](https://github.com/metasfresh/metasfresh-webui-api/issues/283) Build does not use the specified parent version
    * Fix for maven/ build system to fetch the specified parent version for build.
  * [#284](https://github.com/metasfresh/metasfresh-webui-api/issues/284) HU editor: Cannot receive stand alone TUs by default
    * Now also standalone Transport Units can be received and processed in Material Receipt.
  * [#289](https://github.com/metasfresh/metasfresh-webui-api/issues/289) New/Delete buttons missing when a document was initially loaded
    * Now showing the New and Delete Buttons in Subtabs/ included Tabs, when initially loaded.
  
* metasfresh-webui-frontend
  * [#594](https://github.com/metasfresh/metasfresh-webui-frontend/issues/594) inform users that only Chrome is currently supported on login screen
    * The current development for metasfresh WebUI ist done optimized for Chrome browser. Now informing the user about that on login screen, if trying to login with other browser.
  * [#595](https://github.com/metasfresh/metasfresh-webui-frontend/issues/595) kpi disappears when minimize
    * Fixing a Bug that vanished the KPI widgets after minimizing action.
  * [#597](https://github.com/metasfresh/metasfresh-webui-frontend/issues/597) cancel on "Do you really want to leave?" sends you 2 steps back
    * Fix for the leave confirmation popup. Now only going back 1 step after confiormation.
  * [#609](https://github.com/metasfresh/metasfresh-webui-frontend/issues/609) Included subtab height 100% broken again
    * Fix for the 100% height Layout of windows with included Tab. Subtab Shall always expand to 100% of screen resolution height until available spave is used. After that exceeding page size.
  * [#616](https://github.com/metasfresh/metasfresh-webui-frontend/issues/616) delete option missing after add new
    * The delete Button is now available again after starting the add new action.
  
# metasfresh 5.3 (2017-13)

## Important Changes
* metasfresh-backend
  * [#1199](https://github.com/metasfresh/metasfresh/issues/1199) user credentials of "SuperUser" are renamed to metasfresh
    * Changing the default login credentials to the vanilla system to metasfresh/ metasfresh.
  
## Features
* metasfresh-backend
  * [#1197](https://github.com/metasfresh/metasfresh/issues/1197) Introduce AdempiereException setParameter/getParameters
    * Introducing getter and setter for Parameters in ADempiereException.
  * [#1201](https://github.com/metasfresh/metasfresh/issues/1201) Add Manufacturing Order Window for WebUI
    * Adding a new window to webUI for Manufacturing Order. This is the first step in the new Manufacuring Project introducing Manufacturing disposition and WebUI Interface. The first Milestone of the Project is planned for early June 2017.
  * [#1202](https://github.com/metasfresh/metasfresh/issues/1202) Clean up ReplicationException
    * Maintenance Task. Cleaning up the addParameter method in Replication Exception which is now available in ADempiereException and therfor deprecated.


* metasfresh-webui-api
  * [#273](https://github.com/metasfresh/metasfresh-webui-api/issues/273) remove deprecated staleTabIds
    * Adding a functionality to not show stale Tabs in WebUI.
  * [#272](https://github.com/metasfresh/metasfresh-webui-api/issues/272) Document Line Delete Behaviour
    * Adjusting the delete behavior of document lines. Now the line also visibly dissappears after delete action.
  * [#276](https://github.com/metasfresh/metasfresh-webui-api/issues/276) Cannot change BPartner address
    * Now allowing the change of BusinessPartner Locations after initial creation.

* metasfresh-webui-frontend
  * [#118](https://github.com/metasfresh/metasfresh-webui-frontend/issues/118) Copy-paste behaviour for document and lists
    * New functionality to Copy-Paste Grid view content in webUI. This was an activly used functionality in Swing User Interface now ported to the new WebUI.
  * [#442](https://github.com/metasfresh/metasfresh-webui-frontend/issues/442) Image Widget from Attachment in User Window
    * New Image widget for WebUI. Will be able to used to include images/ phots for record which are attached to a record.
  
## Fixes
* metasfresh-backend
  * [#1194](https://github.com/metasfresh/metasfresh/issues/1194) HU "Herkunft" HU_Attribute is not propagated from LU to VHU
    * Fix for the the propagation of attributes top-down. In this case an Attribute added to the Top-Level HU was not propagated down to the included Customer Units.
  * [#1203](https://github.com/metasfresh/metasfresh/issues/1203) Rounding of weights after split in HU Editors is not working correctly
    * Fixing the rounding of weights after Split with the new HU compression Implementation.
  * [#1237](https://github.com/metasfresh/metasfresh/issues/1237) Split fails with aggregate HUs that don't have an integer storage qty
    * Fixes a Bug that appeared when splitting HU generated with the new HU Compression functionality and dis not lead into an integer number storage quantity.

* metasfresh-webui-frontend
  * [#133](https://github.com/metasfresh/metasfresh-webui-frontend/issues/133) Notification display problem
    * Fixes a display bug in notifications created for the user after finishing asynchronous generated documents. Some documents were displayed more than once.
  * [#469](https://github.com/metasfresh/metasfresh-webui-frontend/issues/469) missing value to for range date-time filter
    * Fixes the Bug in Date Range filter widget, where the selection possibility for date-to was missing.
  * [#470](https://github.com/metasfresh/metasfresh-webui-frontend/issues/470) changing AM to PM in Date+Time fields
    * Adjustment of Date-Time Fields, fixing the wrong AP/ PM declaration.
  * [#475](https://github.com/metasfresh/metasfresh-webui-frontend/issues/475) When editing the text field in grid mode i cannot see selected text
    * Color Adjustment of selected Text. Now allowing the user to see what is selected.
  * [#593](https://github.com/metasfresh/metasfresh-webui-frontend/issues/593) DocAction bug when opening a document from references of another
    * Fixes a Bug that appeared when jumping to referenced documents without a valid docaction.
  
# metasfresh 5.2 (2017-12)

## Features
* metasfresh-webui-frontend
  * [#453](https://github.com/metasfresh/metasfresh-webui-frontend/issues/453) Closing modal or window with unsaved changes
    * Providing a better usability when changing the window or modal overlay with unsaved changes.
  * [#461](https://github.com/metasfresh/metasfresh-webui-frontend/issues/461) If tab layout's "supportQuickInput" is false then don't show the "Batch entry" button
    * The batch entry buttrons for quick input are now not shown when the layout provided by rest-api does not support that.
  * [#462](https://github.com/metasfresh/metasfresh-webui-frontend/issues/462) If document was not found forward to documents view
    * The User Nterface now automatically forwards to documents view in the case that a document is not found.
  * [#484](https://github.com/metasfresh/metasfresh-webui-frontend/issues/484) Login Screen 2nd Window usability
    * Improving the usability of the login screen so that the user can navigate, edit and confirm with keyboard completely without mouse usage.
  * [#491](https://github.com/metasfresh/metasfresh-webui-frontend/issues/491) Line height "jump" when editing mode
    * Improving the behavior of grid view so the lines height dows not change depending of its content.
  * [#532](https://github.com/metasfresh/metasfresh-webui-frontend/issues/532) KPI: Remove the Refresh option
    * Removing the refresh button from KPI because this is not needed anymore since D3JS implementation.
  * [#533](https://github.com/metasfresh/metasfresh-webui-frontend/issues/533) KPI: maximize/restore when double clicking on title bar
    * Improved usability on dashboard. When the user double clicks on the titlebar, then the KPI widget maximizes/ minimizes automatically.
  * [#540](https://github.com/metasfresh/metasfresh-webui-frontend/issues/540) Don't use deprecated staleTabIds
    * Improving the behavior of included tab and staled information.

* metasfresh-webui-api
  * [#252](https://github.com/metasfresh/metasfresh-webui-api/issues/252) Provide to frontend: tab allow create new and delete as they change
    * New functionalities to tab rows deleting/ adding functions. These shall only be shown in user interface if the api provides the  possibilities.
  * [#264](https://github.com/metasfresh/metasfresh-webui-api/issues/264) Support different printers for same user and different login locations
    * Additional api improvements to allow different printers for the same users with different login locations.

* metasfresh-backend
  * [#1145](https://github.com/metasfresh/metasfresh/issues/1145) Refactor adempiereJasper servlets and implement them with @RestController
    * Complete refactoring of adempiereJasper servlets so that they now can work together with the Rest API Controller.
  * [#1146](https://github.com/metasfresh/metasfresh/issues/1146) Change "sent by" in Request Notifications
    * New information in Requests, now keeping the infomration about the notification sender.
  * [#1152](https://github.com/metasfresh/metasfresh/issues/1152) Support address sequence configuration in multi org environment
    * Improvement of the country location sequence configuration in multi organisational environments.
  * [#1178](https://github.com/metasfresh/metasfresh/issues/1178) Warehouse Window in WebUI Layout
    * New Window in Web User Interface to allow the creation and maintenance of Warehouses.

## Fixes

* metasfresh-webui-frontend
  * [#451](https://github.com/metasfresh/metasfresh-webui-frontend/issues/451) Bug in Sales Order Line, Add new
    * Fixed an issue in the "add new" functionality of Sales Order Line.
  * [#474](https://github.com/metasfresh/metasfresh-webui-frontend/issues/474) Editing in the middle of a text field makes the cursor jump to the end
    * This change fixes a user experience issue that let the curson jump to the end when the user tried to edit in the middle of a text.
  * [#502](https://github.com/metasfresh/metasfresh-webui-frontend/issues/502) Lookup field layout issue when it has red border
    * Adjusting the layout of red bordered lookup elements, now aboiding that existing icons overlap the border.
  * [#526](https://github.com/metasfresh/metasfresh-webui-frontend/issues/526) Running a process from menu does not work
    * Fixing a Bug that prevented to run processes called from navigation menu.
  * [#539](https://github.com/metasfresh/metasfresh-webui-frontend/issues/539) Confirm autocomplete Field entry in grid functionality
    * Now it is possible for the user to select and confirm autocomplete entries in the grid view of included tabs.
  * [#545](https://github.com/metasfresh/metasfresh-webui-frontend/issues/545) View's windowId is not matching the expected one
    * This is fixing a Bug which mixed up the viewID's when navigating fast via browser forth and back through the screens.
  * [#547](https://github.com/metasfresh/metasfresh-webui-frontend/issues/547) Menu's first element is hidden behind on mobile 
    * Fixing an issue in mobile responsive navigation design. Now also showing the first link on mobile size resolution.
  * [#550](https://github.com/metasfresh/metasfresh-webui-frontend/issues/550) Clicking on grid view breadcrumb item does not work
    * Bugfix for the breadcrumb navigaion on griwd view items.
  * [#558](https://github.com/metasfresh/metasfresh-webui-frontend/issues/558) Respect saveStatus in connected modal
    * Fixing a Bug to repect the saveStatus also in connected modal overlays.
  * [#561](https://github.com/metasfresh/metasfresh-webui-frontend/issues/561) KPI Pie Chart on Start defect

* metasfresh-webui-api
  * [#256](https://github.com/metasfresh/metasfresh-webui-api/issues/256) Cannot create a new BPartner contact
    * Bugfix for the creation of a business partner contact in Web UI.
  * [#259](https://github.com/metasfresh/metasfresh-webui-api/issues/259) New Warehouse is not saveable
    * Bugfix for the creation of a new warehouses in Web UI.
  * [#260](https://github.com/metasfresh/metasfresh-webui-api/issues/260) cannot create receipt with multiple TU on LU
    * Fix for the Material Receipt that did not properly generate receipt lines whan receiving Handling Units with Transprot Units on Load Units.
  * [#263](https://github.com/metasfresh/metasfresh-webui-api/issues/263) Bug in Warehouse window: Auftragsübersicht (intern) NPE
    * Eliminated the reason for the Null Pointer Exception in Warehouse Window for the Sales Order Overview.

* metasfresh-backend
  * [#473](https://github.com/metasfresh/metasfresh/issues/473) Adjust ESR layout for E-Druck
    * Adjusted the Layout of the swiss ESR bill for electronic exchange.
  * [#1165](https://github.com/metasfresh/metasfresh/issues/1165) QtyDelivered not set back correctly after reactivating and voiding a material receipt
    * Now correctly resetting the Qty delivered when reactivating or voiding a material receipt.
  * [#1177](https://github.com/metasfresh/metasfresh/issues/1177) Qties in Material Receipt not correct after several splitting and transforming in HU Editor
    * Handling Unit Transforming is now delivering the correct results after splitting and merging via the new Handling Unit editor and Handling Unit compression.
  * [#1184](https://github.com/metasfresh/metasfresh/issues/1184) Price is found for C_OlCand despite it was deactivated
    * Although the price was deactivated in Product Price it was used in Order Line Candidates. This is now fixed.
  * [#1192](https://github.com/metasfresh/metasfresh/issues/1192) Pricing: IsDefault was not properly migrated
    * Fixing a Bug with the product price migration after price refactoring.
  
# metasfresh 5.1 (2017-11)

## Features
* metasfresh-backend
  * [#1102](https://github.com/metasfresh/metasfresh/issues/1102) Field Price List Version as search Field
    * Adjusted the Fieldtype for PriceListVersion. Ist now a search field (before a direct Table drop-down). This allows the user now to do a fulltext search and autocomplete and the usage of wildcards.
  * [#1122](https://github.com/metasfresh/metasfresh/issues/1122) Reporting SQL for Products and vendor/ customer
    * New Reporting for Customer and Vendor delivered quantities for a specified Time range.
  * [#1124](https://github.com/metasfresh/metasfresh/issues/1124) metasfresh App Server start takes considerably longer
    * Improvement of Startup time for the metasfresh App server.
  * [#1134](https://github.com/metasfresh/metasfresh/issues/1134) Show Order ID in main window of Empties Receive
    * Add a new Field in Empties receive to show the Document No. of the Sales Order.
  * [#1142](https://github.com/metasfresh/metasfresh/issues/1142) Improve migration scripts handling
    * Adjustment of the Migration Scripts handling. Now saving the migration scripts in a dedicated folder called migration_scripts instead of tmp folder.
  * [#1161](https://github.com/metasfresh/metasfresh/issues/1161) Picking Terminal add Packing Material to Picking Slot takes too long
    * Large Performance Improvement of Picking processing duration when adding Packing Material to a picking Slot. This solution improves the overall performance of the picking workflow.
    
* metasfresh-webui-api
  * [#244](https://github.com/metasfresh/metasfresh-webui-api/issues/244) KPIs: Introduce TimeRange ending offset
    * Extended functionalitie for KPI definition. Now allowing to set an offet that is used for a ofsetted timerange dataset used for comparison in bar charts.
  * [#246](https://github.com/metasfresh/metasfresh-webui-api/issues/246) Row is not always marked as not saved
    * Extended Functionality to mark rows as saved, allowing the front-end save indicator to react on that.
  
* metasfresh-webui-frontend
  * [#200](https://github.com/metasfresh/metasfresh-webui-frontend/issues/200) D3JS API definition
    * Integration of D3JS into the webui frontend. D3JS is used for the generation of Barchart and Piechart KPI on the metasfresh Dashboard.
  * [#444](https://github.com/metasfresh/metasfresh-webui-frontend/issues/444) KPI Number Indicator w/ comparator
    * Implementation of the Number Indicator/ comparator widget for the Target KPI in Dashboard.
  * [#459](https://github.com/metasfresh/metasfresh-webui-frontend/issues/459) If quick input fails then don't show the quick input fields
    * Improvement of User Experience. Not showing Quick Input Fields only if these are configured in the application dictionary.
  
## Fixes
* metasfresh-backend
  * [#1140](https://github.com/metasfresh/metasfresh/issues/1140) too many prices in pricelist report
    * Fixed a Bug that showes to many prices on printed Partner Pricelists.
  * [#1153](https://github.com/metasfresh/metasfresh/issues/1153) Pricing wrong w/ more than 1 Packing Instruction
    * Fixed a bug that did not wlloe to define and use prices for product prices withe more than one packing instruction.
  * [#1160](https://github.com/metasfresh/metasfresh/issues/1160) Material receipt label is printed for each TU on an LU
    * Bug Fix for the automatic Label Printing in Material Receipt Process. Now only the defined amount of copies are printed.
  * [#1162](https://github.com/metasfresh/metasfresh/issues/1162) Split from non-aggregate HUs can lead to wrong TU quantities
    * Bug Fix for the Split action in Handling Unit Editor.
  * [#1171](https://github.com/metasfresh/metasfresh/issues/1171) Inactive BPartner is not shown in open items report
    * Fix for the Open Items report. Now also showing Open Items for decativated Business Partners.
  * [#1172](https://github.com/metasfresh/metasfresh/issues/1172) Show only those weighing machines which are available for HU's warehouse
    * Filtering the shown weighing devices to thiose that are valid for the given Warehouse in HU Storage.

* metasfresh-webui-api
  * [#179](https://github.com/metasfresh/metasfresh-webui-api/issues/179) Cleanup metasfresh-webui repository
    * Housekeeping task to cleanup the metasfresh-webui repository, getting rid of discontinued stuff.
  * [#238](https://github.com/metasfresh/metasfresh-webui/issues/238) Attributes propagated on everything in TU
    * Fixes a Bug that allowed to propoagate Attribute Values on a whole Handling Unit just through moving a low level HU into the Handling Unit.
  
* metasfresh-webui-frontend
  * [#524](https://github.com/metasfresh/metasfresh-webui-frontend/issues/524) Location Editor cannot read property bug
    * Fixing a Bug that broke the functionality of the Location Editor.

# metasfresh 5.0 (2017-10)

## Features
* metasfresh-backend
  * [#987](https://github.com/metasfresh/metasfresh/issues/987) Create a vertical solution for organisations
    * Adding customizations for associations/ organisations as a vertical solution based on metasfresh.
  * [#1000](https://github.com/metasfresh/metasfresh/issues/1000) Support long address in letter window
    * Now long addresses are supported in documents for a proper display in a letter window.
  * [#1035](https://github.com/metasfresh/metasfresh/issues/1035) DLM - restrict number of mass-archived records
    * Performance and Database autovacuum improvement. Segmenting the mass archiving to allow better database maintenance.
  * [#1046](https://github.com/metasfresh/metasfresh/issues/1046) Automatic Naming of Pricelist Version Name
    * Introducing the automatic naming of pricelist Version, adding the needed Information into the Version identifier.
  * [#1070](https://github.com/metasfresh/metasfresh/issues/1070) Price not transferred to flatrate term
    * Enhancing the processing of flatrate Terms after recording in orderline. Now also transferring the price into the flatrate term data after order completion.
  * [#1071](https://github.com/metasfresh/metasfresh/issues/1071) Empties document Jasper takes very long to generate
    * Improving the performance of empties document generation.
  * [#1075](https://github.com/metasfresh/metasfresh/issues/1075) Create Window for Material Tracking in WebUI 
    * New Window "Material Tracking" in WebUI, allowing the creation and maintenance of material Tracking datasets in preparation for usage in procurement, receipt and manufacuring. Material Tracking is important for the retraceability of products in metasfresh.
  * [#1079](https://github.com/metasfresh/metasfresh/issues/1079) Refine the Material Receipt Candidates Grid View
    * Adding detailes Layout to material Receipts Candidates Grid view to allow the receipt user to have a better overview.
  * [#1080](https://github.com/metasfresh/metasfresh/issues/1080) Virtual Column for qtyenteredTU from Purchase Orderline in Receipt Candidate
    * Introduced a virtual column qtyEnteredTU in Material Receipt candidate, to be able to compare ordered TU qty with the already received TU Quantity.
  * [#1081](https://github.com/metasfresh/metasfresh/issues/1081) Allow the generic configuration of C_Printing_Queue_Recipient_IDs for system users
    * New Printing Client Functionality to allow the generic configuration of Printing Queue recepients for system users.
  * [#1090](https://github.com/metasfresh/metasfresh/issues/1090) Introduce C_BPartner_QuickInput table/window to capture new BPartners
    * New Table created for Business Partner creation on the fly during Order editing.
  * [#1105](https://github.com/metasfresh/metasfresh/issues/1105) Translate Action in Material Receipt Schedule for de_DE
    * Translating some of the Quickactions in Material Receipt Candidate window.
  * [#1107](https://github.com/metasfresh/metasfresh/issues/1107) Implement interactive mode for RolloutMigrate
    * Adding a interactive mode for sql_remote.
  * [#1113](https://github.com/metasfresh/metasfresh/issues/1113) BPartner quick input modal overlay Layout for WebUI
    * Created a new Layout based on C_BPartner_QuickInput table and open it in Sales Order editing of Order Partner as "new Partner"  action.
  * [#1118](https://github.com/metasfresh/metasfresh/issues/1118) Change migration scripts folder from TEMP to METASFRESH_HOME/migration_scripts/
    * Customizing and migration enhancement. Now the automatically generated migration scripts for application dictionary changes are saved in dedicated migration-scripts filder instead of TEMP.
  * [#1126](https://github.com/metasfresh/metasfresh/issues/1126) Possibility for easiest regression Test with old and new Pricing Hierarchy
    * Testing SQL to check if the migration of old Pricing Hierarchy to new one was sucessful.
  * [#1130](https://github.com/metasfresh/metasfresh/issues/1130) LU Transform "own Palette" Packing material
    * New functionality to set a "own palette" flag in Handling Unit Editor of Material Receipt, to allow the usage of own Packing Material (here only palettes) that are then not added to Material Receipt Lines from the vendor.
  * [#1132](https://github.com/metasfresh/metasfresh/issues/1132) Close/ Open Action for Material Receipt Candidate Lines
    * Adding the Open/ Close processes to quickaction drop-down in Material Receipt candidates Window allowing the user to mark rows as "processed" or reopen processed rows.
  * [#1133](https://github.com/metasfresh/metasfresh/issues/1133) Reorder the columns in Grid View for Material Receipt Window
    * Reordering columns in Material Receipt Grid View in WebUI.
  * [#1135](https://github.com/metasfresh/metasfresh/issues/1135) Translation de_DE for Process Empties receive
    * Adding a new Translation for "Empties receive" action in Material Receipt Candidates Window in WebUI.

  
* metasfresh-webui-api
  * [#181](https://github.com/metasfresh/metasfresh-webui/issues/181) Transforming HU in Handling Unit Editor
    * New Functionality in Handling Unit Management. Possibility to apply different actions on Handling Units to transform the Packing, Quantities and Hierarchy easily.
  * [#199](https://github.com/metasfresh/metasfresh-webui/issues/199) KPI master data shall provide to frontend a recommended refresh interval
    * New automatic refresh functionality for Dashboard KPI. The KPI now automatically gets the fresh data automatically and updates the KPI with changed data.
  * [#206](https://github.com/metasfresh/metasfresh-webui/issues/206) CU Handling Unit creation without multiline
    * Now the multiline functionality for creating CU without packing material (TU, LU) is allowed also for only 1 selected line.
  * [#207](https://github.com/metasfresh/metasfresh-webui/issues/207) Prohibit Material Receipt w/ selection of multi BP
    * Disallowing the Material Receipt workflow when lines with different vendors are selected. This was a confusing experience for the user, so we decided to switch off this functioanlity.
  * [#208](https://github.com/metasfresh/metasfresh-webui/issues/208) Receive CU w/ possible quantity adjustment
    * Adjustments to the action "Receive CU", now allowing the change of the initial quantity to be received.
  * [#209](https://github.com/metasfresh/metasfresh-webui/issues/209) HU Automatic Label Printing when received
    * New Functionality that automatically prints the Material Receipt Label for the LU as soon as the HU is switched from planning status to active. This allows a more efficient workflow for the user and avoids the printing of labels before being operative (active).
  * [#210](https://github.com/metasfresh/metasfresh-webui/issues/210) Print Material Receipt Document via Material Receipt Candidates Window
    * Added an action that provides the possibility to print the material Receipt Document for all Handling Units of a given Material Receipt candidate Line.
  * [#223](https://github.com/metasfresh/metasfresh-webui/issues/223) Attributes shall be shown on CU level too
    * Possibility to now edit Attributes also on CU Level.
  * [#228](https://github.com/metasfresh/metasfresh-webui/issues/228) Remaining TU Qty in Quickaction and HU Config
    * When using transform actions in Handling Unit editor, now setting the quantity of the source HU automatically in the Parameter window as initial, but overwritable quantity.
  * [#234](https://github.com/metasfresh/metasfresh-webui/issues/234) HU Config in handling Unit Editor
    * Providing an action to transform Handling Units into other Handling Unit combinations in Handling Unit Editor.
  * [#236](https://github.com/metasfresh/metasfresh-webui/issues/236) Remaining TU Qty in Receive HUs window
    * Automatically calculating the remaining TU Quantity to receive in Material Receipt Canddate Window.
  
* metasfresh-webui-frontend
  * [#126](https://github.com/metasfresh/metasfresh-webui-frontend/issues/126) Add new Business Partner functionality in search Field
    * New Feature that allows to create a new customer Business Partner on the fly when editing a sales Order. This new functionality automatically is suggested to the user when the businesspartner search does not find a result.
  * [#446](https://github.com/metasfresh/metasfresh-webui-frontend/issues/446) Error indicator for fields
    * Introducing new Error indicators for fields, allowing to provide detailed information to the user in case of error situations for a field. The available information is shown with a red color on the field and available when hovering then field with the mouse.
  * [#447](https://github.com/metasfresh/metasfresh-webui-frontend/issues/447) Not saved indicator for Grid view lines
    * Introducting a "not saved indicator" for grid view lines, now giving the user the transparency to recognize why a line cannot be saved.
  * [#448](https://github.com/metasfresh/metasfresh-webui-frontend/issues/448) Query on activate document included tab
    * New functionality to reduce the traffic and ressource load whan opening a window with included subtabs. This new feature allows to flag subtabs as high volume or frequent change and then reloads the data only when the user selects that subtab.
  * [#452](https://github.com/metasfresh/metasfresh-webui-frontend/issues/452) Saving Indicator Bar in modal window
    * Adding the global save indicator bar also to modal overlays.
  * [#482](https://github.com/metasfresh/metasfresh-webui-frontend/issues/482) Quick actions not queried in HU editor for included rows
    * Added a missing functionality, so that now quick actions can be used also for newly added rows in Handling Unit Editor.
  * [#488](https://github.com/metasfresh/metasfresh-webui-frontend/issues/488) Restrict the minimum size of action menu
    * Layout change to prohibit the action menu getting to narrow, disallowing the user to read the actions.
  * [#490](https://github.com/metasfresh/metasfresh-webui-frontend/issues/490) Filter drop-down list too narrow
    * Layout change to prohibit the filter drop-down list getting to narrow, disallowing the user to read the filter criteria.
  * [#510](https://github.com/metasfresh/metasfresh-webui-frontend/issues/510) Improved Not Saved Indicator design
    * Nice improvement for the design of the "not saved" indicator. It is now a thin red line instead of the "progress and save" indicator.
  * [#1073](https://github.com/metasfresh/metasfresh/issues/1073) Material Receipt Candidates order by Purchase Order ID desc
    * New Order by criteria for the Grid view in the Material Receipt Candidates Window.
  
## Fixes
* metasfresh-backend
  * [#936](https://github.com/metasfresh/metasfresh/issues/936) Logfile on application-server gets flooded
    * Fixes an issue that leads to high volume logging on metasfresh application server.
  * [#1039](https://github.com/metasfresh/metasfresh/issues/1039) Make C_OrderLine.M_Product_ID mandatory
    * Minor Fix making the Orderline ID mandatory.
  * [#1056](https://github.com/metasfresh/metasfresh/issues/1056) Purchase Order from Sales Order process, wrong InvoiceBPartner
    * Fixes a Bug that did not select the correct Invoice Business Partrner vendor from the Partner Releationship having the delivery vendor Partner different than the Invoice Business Partner vendor.
  * [#1059](https://github.com/metasfresh/metasfresh/issues/1059) ShipmentScheduleBL.updateSchedules fails after C_Order was voided
    * Minor fix in the ShipmentScheduler update that failed for a cornercase when the sales Order was voided.
  * [#1068](https://github.com/metasfresh/metasfresh/issues/1068) Settings on Swing Client are saved but window does not close
    * Minor fix in Swing Client Settings window. Changes to the settings were save but it was not possible to close the window via done after any change.
  * [#1076](https://github.com/metasfresh/metasfresh/issues/1076) NoDataFoundHandlers can cause StackOverflowError
    * Fix for a Bug cause through new Data Lifecycle Management Feature.
  * [#1088](https://github.com/metasfresh/metasfresh/issues/1088) pricing problem wrt proccurement candidates
    * Fix a minor Bug that was introduced with the new pricing changes.
  * [#1094](https://github.com/metasfresh/metasfresh/issues/1094) Drop deprecated and confusing I_AD_User get/setFirstName methods
    * Dropping lagacy getter/ setter for FirstName LastName of User data.
  * [#1097](https://github.com/metasfresh/metasfresh/issues/1097) field too small in jasper document for invoice
    * Enhancing a field size in JasperReports Invoice Document. 
  * [#1099](https://github.com/metasfresh/metasfresh/issues/1099) Fix Materialentnahme movement creation
    * Includes a fix for the Material Movement creation for used products in Swing Handling Unit editor. 
  * [#1103](https://github.com/metasfresh/metasfresh/issues/1103) TU Ordered Qty in Material Receipt Schedule shows movedQty
    * Fix in the virtual column Ordered Quantitity TU that showed the moved Quantity instead.
  * [#1106](https://github.com/metasfresh/metasfresh/issues/1106) inactive org still selectable on login
    * Fixes the login procedure, now disallowing the loging for inactive Organisations.
  * [#1110](https://github.com/metasfresh/metasfresh/issues/1110) webui HU Editor: conversion error when selecting SubProducer
    * Fixing an error that prohibited the editing of Subproducers in the Material Receipt Handling Unit Editor.
  * [#1121](https://github.com/metasfresh/metasfresh/issues/1121) TU receipt label is just a white sheet of paper
    * Fixes an error in the printing process of Material Receipts and Labels via the standalone printing client and WebUI Interface.
  
* metasfresh-webui-api
  * [#159](https://github.com/metasfresh/metasfresh-webui/issues/159) Error opening the "Report & Process" window
    * Fixes a Bug that prevented the preview of reports via Chrome new Tab.
  * [#187](https://github.com/metasfresh/metasfresh-webui/issues/187) Documents shall automatically have a default value for standard fields
    * Enhancement to automaticall have default values for document standard fields.
  * [#188](https://github.com/metasfresh/metasfresh-webui/issues/188) Field's mandatory flag is not considered
    * Fixes cases in WebUI that shall trigger a mandatory behavior for input fields.
  * [#213](https://github.com/metasfresh/metasfresh-webui/issues/213) Use document's BPartner language when printing
    * Fixes the output of Documents. Now also in WebUI the documents are created in the Business Partner Language.
  * [#222](https://github.com/metasfresh/metasfresh-webui/issues/222) Carrot Paloxe Error in Material Receipt
    * Fixing an error that appeared when receiving HU which were not LU Level.
  * [#225](https://github.com/metasfresh/metasfresh-webui/issues/225) Receipt Candidates - Foto process is not attaching the picture
    * Fixes the save process when taking a photo via webcam in material Receipt Candidates. The Photo is automatically uploaded and saved as attachment to the candidate record.
  * [#237](https://github.com/metasfresh/metasfresh-webui/issues/237) Transform CU on existing TU not working
    * Fixes an error when trying to transform a CU Handling Unit to an already existing TU.
  
* metasfresh-webui-frontend
  * [#214](https://github.com/metasfresh/metasfresh-webui-frontend/issues/214) Global shortcuts are not working when focused in inputs
    * Fixing some of the global shortcuts when the focus is on an input field.
  * [#465](https://github.com/metasfresh/metasfresh-webui-frontend/issues/465) DateTime widget not respected in included tab
    * Fix that now also uses the correct widget for DateTime fields.
  * [#473](https://github.com/metasfresh/metasfresh-webui-frontend/issues/473) Sidelist broken
    * Fixing a bug that broke the sidelist.
  * [#485](https://github.com/metasfresh/metasfresh-webui-frontend/issues/485) Subtab 100% height broken again
    * Fixing the Layout of included subtabs when subtab lines are able to fit completely on one screen.
  * [#487](https://github.com/metasfresh/metasfresh-webui-frontend/issues/487) Expand batch entry when completed docuement
    * Fixed an error that appeared when the user tried to use the batch entry mode expansion with a completed document.
  * [#489](https://github.com/metasfresh/metasfresh-webui-frontend/issues/489) Grid view not refreshed on websocket event
    * Fix for websocket eventy that triggers the refresh of the Grid View after external data changes.

# metasfresh 4.58.57 (2017-09)

## Features
* metasfresh-backend
  * [#850](https://github.com/metasfresh/metasfresh/issues/850) Add Migration Script to rename Attribute Set Instance Field
    * Global renaming of Label "Attribute Set Instance" to "Attribute".
  * [#968](https://github.com/metasfresh/metasfresh/issues/968) Include webui in the normal rollout process
    * Optimizing the Continuous Integration workflow to allow the build of WebUI in default Rollout Process.
  * [#1028](https://github.com/metasfresh/metasfresh/issues/1028) extract distributable stuff into dedicated repo
    * Create a dedicated repository and moved distributably stuff there for betterseperation of core and customized implementations.
  * [#1040](https://github.com/metasfresh/metasfresh/issues/1040) Have new request type opportunity
    * Included the new Request Type "Opportunity". First step preparing data structure for our new Opportunity Dashboard in WebUI.
  * [#1049](https://github.com/metasfresh/metasfresh/issues/1049) inDispute Fields and Quality% missing in main Invoice Candidate Window
    * Adjustments in Invoice Candidates Window of WebUI. Included Fields with infomration about the Dispute Status and Quality Inspection information of Material Receipt.
  
* metasfresh-webui-api
  * [#171](https://github.com/metasfresh/metasfresh-webui/issues/171) No packing item selectable for M_HU_PI_Item_Product
    * Now allowing the recording/ editing of Packing Items in CU:TU Configuration of Product.
  * [#182](https://github.com/metasfresh/metasfresh-webui/issues/182) Material Receipt w/ multiple lines and solitary CU Buckets
    * Implementation of multi line receiving in Material Receipt and Handling Unit Editor.
  * [#183](https://github.com/metasfresh/metasfresh-webui/issues/183) Implement userSession endpoint which also provides the timeZone upcoming
    * When parsing JSON Dates without time, always ignoring the sent timezone.
  * [#184](https://github.com/metasfresh/metasfresh-webui/issues/184) Implement KPI service
    * First prototype Implementation for new metasfresh WebUI Dashboard with usage of D3JS Charts.
  * [#191](https://github.com/metasfresh/metasfresh-webui/issues/191) "Not saved yet" info in REST-API
    * Now providing the information up to Frontend that if data has been saved yet in database. This implementation allows to give the user more feedback about editing errors or missing mandatory data.
  * [#200](https://github.com/metasfresh/metasfresh-webui/issues/200) Generic "add new" search field functionality
    * Implementation of a generic functionality to add new data when not found during autocomplete search workflow. This new functionality will be used in search widget and allows to record data on the fly that belongs to the search field column and ID.

* metasfresh-webui-frontend
  * [#377](https://github.com/metasfresh/metasfresh-webui-frontend/issues/377) grid view: when initially clicking on first row, the second one is first selected
    * Changed the focus behavior in Grid View rows. The cursor "jump" is now eliminated. The user can use the keyboard arrow down to now firstly focus and further navigate trhough the grid.
  * [#392](https://github.com/metasfresh/metasfresh-webui-frontend/issues/392) Filters are not respecting mandatory property
    * Adjustment on filter datils for webUI. Now respecting and showing if parameter/ filter fields are mandatory to be filled.
  * [#416](https://github.com/metasfresh/metasfresh-webui-frontend/issues/416) moving scrollbar on arrow key in dropdown
    * Large dropdown lists now scroll down together with keyboard arrow down navigation.
  * [#428](https://github.com/metasfresh/metasfresh-webui-frontend/issues/428) When calling a process from HU editor open the process parameters on top of the HU editor modal
    * Now allowing modal over modal process windows, to allow the usage of actions also in modal overlays.
  * [#435](https://github.com/metasfresh/metasfresh-webui-frontend/issues/435) filter in Material Receipt Candidates does not work
    * Fixing a minor issue that occured when creating and using a filter criteria that does not have any variable parameters.
  * [#458](https://github.com/metasfresh/metasfresh-webui-frontend/issues/458) HU Editor Attribute editing not possible
    * Fixing a Bug that occured when trying to edit attributes in HU Editor for a selected HU Level.
  * [#1004](https://github.com/metasfresh/metasfresh/issues/1004) Order by C_Order_ID desc in Sales Order Window
    * New Order by criteria in Order Window, so that newest Orders always occur at Tio initially.
  * [#1007](https://github.com/metasfresh/metasfresh/issues/1007) Window for PMM_PurchaseCandidate in WebUI
    * New Window in WebUI for Procurement candidates.
  * [#1032](https://github.com/metasfresh/metasfresh/issues/1032) Material Receipt Candidates Grid View finetuning
    * Adjustments to the Material Receipt Candidates fintuning. Rearranged and reduced the columns shown, so allow a better recognition of important data for the Material Receipt end user.

## Fixes
* metasfresh-backend
  * [#1036](https://github.com/metasfresh/metasfresh/issues/1036) Harmonize BL for ModelCacheService.IsExpired
    * Fixing a Bug in the ModelCacheService that considered records to be expired under certain conditions although they weren't.

* metasfresh-webui-api
  * [#140](https://github.com/metasfresh/metasfresh-webui/issues/140) Failed retrieving included documents when one of them is no longer in repository
    * Fixed a Bug that occured in one time situation and restricted the retrieving of included documents. 
  * [#160](https://github.com/metasfresh/metasfresh-webui/issues/160) Don't load documents when dealing with attachments API
    * Fix Material Receipt in WebUI to avoid interfering attachments api that cause error in minor cases when receiving HU.
  * [#176](https://github.com/metasfresh/metasfresh-webui/issues/176) Attributes editor problems
    * Fixes an issue that only showed 3 Attribute lines when opening the Attribute Editor.
  * [#177](https://github.com/metasfresh/metasfresh-webui/issues/177) Date range parameters are not consistent with the ones I introduce
    * Harmonized the date ranges selected in WebUI Frontend with the Parameter Date Ranges used for filtering of data in Backend.
  * [#194](https://github.com/metasfresh/metasfresh-webui/issues/194) Quality discount not considered when receipving HUs
    * Now the Quality discount is considered in Material Receipt WebUI.
 
* metasfresh-webui-frontend
  * [#404](https://github.com/metasfresh/metasfresh-webui-frontend/issues/404) Wrong viewId used when running "Create material receipt" using keyboard shotcuts
    * Fixes an issue that connected the wrong viewID in "created material receipt" workflow using keyboard navigation and selection.
 

# metasfresh 4.57.56 (2017-08)

## Features
* metasfresh 
  * [#739](https://github.com/metasfresh/metasfresh/issues/739) remove X_BPartner_Stats_MV
    * Removing the legacy DB Table with Business Partner statistics, because of big performance penalty. Will be replaces later via modern data storage and KPI in webUI.
  * [#920](https://github.com/metasfresh/metasfresh/issues/920) Show date promised on order confirmation
    * Now displaying the date promised on order confirmation document.
  * [#927](https://github.com/metasfresh/metasfresh/issues/927) Use partner specific product number and name in documents
    * Extending the Jasperreports documents for order confirmation, inouts and invoices. Now displaying further customer specific product numbers and names.
  * [#928](https://github.com/metasfresh/metasfresh/issues/928) have a way to control which contact is used on addresses in order
    * Enhanced functionality for the selection and usage of contact information on order confirmations. Now its possible to do detailed presettings of Business Partner contacts to be used in Sales and purchase workflows.
  * [#941](https://github.com/metasfresh/metasfresh/issues/941) Make TableSequenceChecker more robust with corner cases
    * Enhancing the Table Sequence Checker to allow more robustness. Issues was recognized in corner cases of Data Life-cycle Management archiving process of legacy data.
  * [#942](https://github.com/metasfresh/metasfresh/issues/942) Improve Transparency & Usability of Pricing definitions
    * Huge Improvement of Transparency & Usability of Pricing definitions. The Pricing Data Structure has grown during the last decade to allow very detailed price definitions bases on countries, currencies, products, packages, attributes and more. This lead into a data structure that is tough to understand and maintain for new users. This implementation improves the price hierarchy to allow much easier maintenance and better transparency.
  * [#954](https://github.com/metasfresh/metasfresh/issues/954) Address Layout Dunning Document
    * Changed the Address Layout on Dunning Document to fit better in standardized letter envelope windows.
  * [#956](https://github.com/metasfresh/metasfresh/issues/956) Adjust Price list reports to new data structure
    * Adjustments to existing Price list reports dues to refactoring of new pricing hierarchy and functionality.
  * [#957](https://github.com/metasfresh/metasfresh/issues/957) Empties Returns for Vendors and Customers in Material Receipt Window
    * Implementation of empties return and receive for Business Partners. The new functionality can be used in Material Receipt candidates window and automatically creates Shipment/ Receive documents for empties with predefined settings.
  * [#969](https://github.com/metasfresh/metasfresh/issues/969) mass migration for 2014, 2015 and further
    * Process to allow to archive during operational time for large chunks of data. This is a spacial migration process to prepare environments for the usage of metasfresh Data Life-cycle Management.
  * [#975](https://github.com/metasfresh/metasfresh/issues/975) Set DLM_Level via properties/preferences UI
    * Allows the user to set if his client shall work with archived data or only show operational data (highly improved performance).
  * [#993](https://github.com/metasfresh/metasfresh/issues/993) New ProductPrice Window for Price Maintenance 
    * A new Windows called Product Price which allows the easy filtering and maintenance of all product prices. The user filters the data via preset Price List version and is able to maintain the data vie Grid view or single view/ advanced edit.
  * [#981](https://github.com/metasfresh/metasfresh/issues/981) Introduce AD_PInstance(AD_Pinstance_ID, ColumnName) unique index
    * Minor improvement to raise the reading performance of Process Instances.
  * [#983](https://github.com/metasfresh/metasfresh/issues/983) Upgrade guava version from 18 to 21
    * Upgrading guava (google core libraries) from version 18 to 21.
  * [#986](https://github.com/metasfresh/metasfresh/issues/986) Handle the case that PO can't load a record
    * Allows now fallback scenarios (for example retry) for the Persistance Object to retry in case of thrown exceptions during record loading.
  * [#1018](https://github.com/metasfresh/metasfresh/issues/1018) support description fields in shipment
    * Add the fields description bottom and description to metasfresh client in Shipment Window and displays the field content also on Shipment Jasperreports.
  
* webUI
  * [#146](https://github.com/metasfresh/metasfresh-webui/issues/146) Receipt schedules: show empties receive/return actions only when only one row is selected
    * Possible actions are now only shown when at least 1 Grid View row is selected. if none are selected then the actions menu is shown, but actions are not able to be started.
  * [#147](https://github.com/metasfresh/metasfresh-webui/issues/147) HU editor: hide filters because they are not supported atm
    * Currently hiding the filter selection in Handling Unit editor until the implementation of filtering is also done in this modal overlay window.
  * [#138](https://github.com/metasfresh/metasfresh-webui/issues/138) change name of new record button
    * The button for new Record is now variable depending on the settings in ad_menu and ad_menu_trl.
  * [#252](https://github.com/metasfresh/metasfresh-webui-frontend/issues/252) Add keyboard handling in POS   
    * Implemented an improved keyboard navigation in the new metasfresh WebUI. Shortcuts and unified behavior is now provided among different windows.
  * [#215](https://github.com/metasfresh/metasfresh-webui-frontend/issues/215) Shortcut for direct Document Action "complete"
    * Further usability Improvement. Added a new shortcut for document complete action. This action is mostly used among all documents and reduces the amount of user keystrokes or mouse click by 1 per document.
  * [#227](https://github.com/metasfresh/metasfresh-webui-frontend/issues/277) Processed HU in Material Receipt Workflow
    * When creating a Material Receipt in handlign Unit editor, then the processed Handling Units are now read-only and are grayed out, so the user can distinguis very well beween proceed and unprocessed Handling Units.
  * [#282](https://github.com/metasfresh/metasfresh-webui-frontend/issues/282) Implement attachments list in Actions menu
    * The action menu now shows another section called "attachments" where all files are listet that are attached to the currently selected record (e.g. product record, document). Photos made per Webcam and uploaded are also automatically shown as Photo attachment.
  * [#323](https://github.com/metasfresh/metasfresh-webui-frontend/issues/323) Provide login credentials in request body
    * Created a new API to provide the login credentials in the request body instead of parameters.
  * [#996](https://github.com/metasfresh/metasfresh/issues/996) Add Warehouse and processed Filter to Material Receipt Candidates Window
    * The Window Material Receipt candidates has now 2 further filter selections (Warehouse, processed). This allows the user to search and filter the needed Data much faster.
  * [#265](https://github.com/metasfresh/metasfresh-webui-frontend/issues/265) Create Jenkinsfile for metasfresh-webui-frontend
    * Including the automatic build of the metasfresh WebUI into the Jenkins Build Infrastructure.
  * [#345](https://github.com/metasfresh/metasfresh-webui-frontend/issues/345) Grid view layout: honor supportNewRecord and newRecordCaption
    * Implementation that brings New Record information up to the frontend and allowss the frontend now to react in a more flexible way.
  * [#162](https://github.com/metasfresh/metasfresh-webui/issues/162) Possibility to choose used filter criteria for webUI
    * Reducing the amount of predefined filter selections per window to saved filter sets created by special user.
  * [#1014](https://github.com/metasfresh/metasfresh/issues/1014) Window Layout Purchase Order: Warehouse
    * This is an Feature List Item that is part of an Feature List. Notice the connector between the three graphics to show that they are related.
  
## Fixes

* metasfresh
  * [#161](https://github.com/metasfresh/metasfresh-webui/issues/161) Button Action in Subtab for Price List Version creation
    * Enabled the functionality in WebUI to generate new Product prices via the Pricelist Version record and configured calculation Schema with source Price List.
  * [#912](https://github.com/metasfresh/metasfresh/issues/912) New role added .. login not possible after that
    * Fixed a minor Bug that restricted the login after creating a new Role.
  * [#998](https://github.com/metasfresh/metasfresh/issues/998) ClassNotFoundException: de.metas.dlm.swingui.model.interceptor.Main
    * Bugfix for ClassNotFound Exception in Data Life-cycle Management Interceptor.
  * [#1010](https://github.com/metasfresh/metasfresh/issues/1010) M_ProductPrice with Season fix price=Y should not be modified when copied
    * Fix that now prohibits the modification of Product Prices which are flagged as SeasonFixPrice during copy.

* webUI
  * [#124](https://github.com/metasfresh/metasfresh-webui/issues/124) Default/ Standard Filter not correct in WebUI
    * Fix to show the right default Filters in WebUI as defined in Search Columns in Swing Client for a given window.
  * [#135](https://github.com/metasfresh/metasfresh-webui/issues/135) process parameter defaults are not set
    * Fix that ensures the proper setting of report Parameters in WebUI to Jasperreports to allow creation of reports.
  * [#137](https://github.com/metasfresh/metasfresh-webui/issues/137) Make sure ProcessInstance is not override by concurrent REST api call
    * Fixing timing conditions that lead to overriding ProcessInstance through concurrent REST API call.
  * [#141](https://github.com/metasfresh/metasfresh-webui/issues/141) edit address not working anymore
    * Fix to allow the usage of buttons in the advanced edit overlays of WebUI (for example Button for location editor or Attributes Editor)
  * [#145](https://github.com/metasfresh/metasfresh-webui/issues/145) HU editor - Create material receipt not enabled when the whole palet is selected
    * Fix that enables the QuickAction in Handling Unit Editor of Material Receipt when selecting an HU with LU Level (e.g. Pallet)
  * [#150](https://github.com/metasfresh/metasfresh-webui/issues/150) Material Receipt Candidates not updated after receive HU 
    * Fixed a Bug that occured when doing mass enqueing of invoice candidate lines of different business partner.
  * [#155](https://github.com/metasfresh/metasfresh-webui/issues/155) Error in invoice candidate enqueuing
    * Fixed a Bug that occured when doing mass enqueing of invoice candidate lines of different business partners.
  * [#157](https://github.com/metasfresh/metasfresh-webui/issues/157) Receive HU opens with 10 LUs
    * Bugfix for Material Recipts via Handling Unit Editor. Always opened with wrong amount of Top Level Handling Units (LU).
  * [#164](https://github.com/metasfresh/metasfresh-webui/issues/164) Create migration script for missing Menu
    * Recreated the missing migration for the newly built WebUI menu with flatter and more comprehensible Hierarchy and Structure.
  * [#168](https://github.com/metasfresh/metasfresh-webui/issues/168) internal: Don't create layout elements if there are no fields inside
    * Fixed a Bug that occured first time in General Ledger window in WebUI, caused through layout elements without included fields.
  * [#170](https://github.com/metasfresh/metasfresh-webui/issues/170) date attributes in hu modal window
    * Bugfix to allow the setting and editing of attributes in date format.
  * [#171](https://github.com/metasfresh/metasfresh-webui/issues/171) No packing item selectable for M_HU_PI_Item_Product
    * Fix to allow the user to select the Packing Item field in WebUI.
  * [#172](https://github.com/metasfresh/metasfresh-webui/issues/172) Entries skipped at the begining of pages
    * Fixed a User Interface bug that caused ugly jumps of the Grid View/ Table View when turning pages.
  * [#263](https://github.com/metasfresh/metasfresh-webui-frontend/issues/263) Grid view attributes were queried when there are no rows
    * Fix in Handling Unit Editor that was trying to read data for an already reversed/ vanished Handling unit.
  * [#294](https://github.com/metasfresh/metasfresh-webui-frontend/issues/294) Leave Location editor with no entry
    * Bugfix. The user is now allowed to leave the location editor in Business Partner Location without changing any data.
  * [#311](https://github.com/metasfresh/metasfresh-webui-frontend/issues/311) Fix the scrollbars when having an overlay grid view
    * Bugfix. The user is now allowed to leave the location editor in Business Partner Location without changing any data.
  * [#313](https://github.com/metasfresh/metasfresh-webui-frontend/issues/313) Scrollbar missing on LU/ TU Level in HU Editor
    * Rearrangement of the Handling Unit Editor layout to not overload the window with too many scrollbars when not really needed.
  * [#314](https://github.com/metasfresh/metasfresh-webui-frontend/issues/314) Grid view filtering: don't send valueTo if it's not a range parameter
    * Avoid the sending of the valueTo as longs it's not a range parameter.
  * [#315](https://github.com/metasfresh/metasfresh-webui-frontend/issues/315) Quick action button layout is broken on smaller resolution
    * Fixed the responsive layout of the Quick Input Button behavior on smaller screen resolutions.
  * [#317](https://github.com/metasfresh/metasfresh-webui-frontend/issues/317) Quick actions are not refreshed when opening the modal HU editor
    * Fix that now refreshes the list of available quick actions when opening the modal Handling Unit Editor overlay.
  * [#322](https://github.com/metasfresh/metasfresh-webui-frontend/issues/322) Quick input's mandatory=false not respected
    * Now evaluating the mandatory false parameter in Quick Batch entry functionality.
  * [#330](https://github.com/metasfresh/metasfresh-webui-frontend/issues/330) HUEditor displays HUs which are destroyed
    * The Handling Unit is now not showing Handling Units anymore which are destroyed (e.g. after reversing the creation of an already active Handling Unit).
  * [#331](https://github.com/metasfresh/metasfresh-webui-frontend/issues/331) Debug/fix: if a PATCH operation fails some wrong calls are performed 
    * Bugfix for Patch Operation that lead into wrong calls to REST API.
  * [#333](https://github.com/metasfresh/metasfresh-webui-frontend/issues/333) The whole process parameters content vanished
    * Process Panel fix in Handling Unit Editor that caused the initialization of the whole panel and left it empty.
  * [#353](https://github.com/metasfresh/metasfresh-webui-frontend/issues/353) Wrong sitemap breadcrumb
    * Fix for a minor Issue that showed the wrong breadcrumb path when opening the sitemap.
  * [#354](https://github.com/metasfresh/metasfresh-webui-frontend/issues/354) Filter w/o variable Parameters not working
    * Fixed a Bug that prevented Filter selections without variable Filter criteria included.
  * [#364](https://github.com/metasfresh/metasfresh-webui-frontend/issues/364) Handling Unit Double click Icon changes data underneath overlay
    * Now preventing clicks on modal overlay leading to navigational main window changes underneath.
  * [#259](https://github.com/metasfresh/metasfresh-webui-frontend/issues/259) Grid view selection lost when trying to use the scroll bar
    * Now preventing clicks on modal overlay leading to navigational main window changes underneath.
  * [#352](https://github.com/metasfresh/metasfresh-webui-frontend/issues/352) Lines not "refreshed" after docaction reactivation
    * Bugfix that now refreshes the document lines after reactivation action.
  * [#383](https://github.com/metasfresh/metasfresh-webui-frontend/issues/383) Shortcut for Batch entry space not usable when in input field
    * Fix for keyboard navigation shortcut that now allows the direct jump to Quick Batch entry even when in field focus.
  * [#376](https://github.com/metasfresh/metasfresh-webui-frontend/issues/376) Don't render unknown widget types but log in console
    * Built restriction to not render unnown widget types in metasfresh WebUI.
  * [#350](https://github.com/metasfresh/metasfresh-webui-frontend/issues/350) Strange pulse effect in Subtab Gridview
    * Fix for minor issue in Pulse effect when updating document rows.
  * [#176](https://github.com/metasfresh/metasfresh-webui/issues/176) Attributes editor problems
    * Now the editing of all listed attributes are allowed in Attribute editor when displayed.
  
# metasfresh 4.56.55 (2017-07)

## Features
* metasfresh
  * #913 include branch name in build version string
    * Added the branch name into the build version string to be able to distinguish between builds & rollouts in development branches.
  
* webUI
  * #112 On login page, deactivate the fields while logging in
    * Now making the login fields read-only as soon ad the authentication process is triggered.
  * #118 Functionality to easily add files to current record in webUI
    * New Functionality to upload files to a given dataset in the new WebUI. This implementation is also used for the new attachment functionality.
  * #120 Material Receipt WebUI: Attribute Values wrong
    * Instead of short Attribute identifiers now the resolved values are shon in the Attribute Editor.
  * #121 Empties Returns for Vendors and Customers in Material Receipt Window
    * New Functionality in WebUI. Possibility to create empties return/ receive documents quickly from Material Receipts window.
  * #127 Receipt schedules - Receive with configuration improvements
    * Enhanced configuration and saving functionality in material receipt workflow in WebUI.
  * #132 Receiving HUs: already received HUs shall be flagged as processed
    * Now flagging the already processed Handling Units in material Receipts workflow as processed and make visible to the user.
  * #226 Implement document field device support
    * New generic Device functionality to add device buttons to an input field. In the first Implementation used to attach weighing machines to receive the current value for gross weight field. Can be used in Material Receipt window/ Handling Unit editor.
  * #227 Wrong breadcrumb when the view is opened after process execution
    * Adjust for the breadcrumb navigation to show the corresponding path after process execution from window action.
  * #254 Cannot see the HU editor icons
    * Added missing HU editor icons for logistic-, transport- and customer-unit level.
  * #256 Implement document attachments
    * New, fast and easy implematation to upload attachments to an existing record in metasfresh.
  * #257 login page: focus on username
    * When opening the login page to enter metasfresh webUI then initially have the focus on Username field.
  * #275 None of the menus could be opened when in full screen mode
    * Allows to open all top bar menues now, also when quick batch entry mode is activated.
  * #281 Remove margin from wrapper modal to make 0-padding inside
    * Adjusting the look and feel of the modal overlay, now reducing the padding to minimum.
  * #734 Add Translation for en_US in WebUI
    * Translation of metasfresh is now available in en_US.
  * #833 Invoice Process in Invoice Candidates WebUI
    * Add the Invoiceing process to easy create customer and vendor invoices from filtered and selected records in invoice candidates window in webUI.
  * #894 Payment Allocation Window WebUI
    * Include the Payment allocation window in new Web User Interface.
  * #895 Dunning Candidates Window WebUI
    * Include the Dunning Candidates window in new Web User Interface.
  * #947 Window Greeting Add Translation for en_US
    * Minor enhancement, translating the Greeting window for language en_US.
  * #966 Provide WebUI Default Role
    * Add a default role and permission to use the already implemented functionalities for the new Web User Interface.

## Fixes 
* metasfresh
  * #797 Zoom does not open new document
    * Fix that allows to open a referenced document record in metasfresh via the reference action and zoom accross.
  
* webUI
  * #119 Error when Pressing the Attribute Button
    * Fixes an error that occured in sales order advancededit and grid view edit when trying to record product attributes.
  * #116 qty 0 in purchase order
    * Fix for the Quick batch entry bug that leads into wrong quantity 0 in generated order line.
  * #229 Location editor button called "edit attributes"
    * Minor fix renaming the button for Location editing.
  * #261 When the attribute is readonly don't show the Device button(s)
    * Making the new Field device buttons invisible when the corresponding field in read-only.
  * #264 Included tabs layout is broken
    * Fix for the broken layout of included tabs when opened in lower screen resolution.
  * #268 Wrong viewId when starting the process
    * Fix that now provides the correct View-ID as process Parameter.
  * #276 Wrong timing when completing a quick input entry
    * Fix for the Quick Order Batch entry that leads to wrong prices in certain cases.
  * #279 While browsing a document, pressing New does nothing
    * Fixing the new document functionality in action menu while viewing a document in detail view.
  * #295 Attribute Editor too narrow
    * Makes the Attribute Editor now better readable.

# metasfresh 4.55.54 (2017-06)

## Features
* metasfresh
  * #877 Make "Wareneingang POS (Jasper)" report work with M_ReceiptSchedule_ID as parameter
    * Adapt the reports for Material Receipt to work with Receipt Schedule ID as Paramater. This is needed for the new Material Receipt Workflow in WebUI which is now based on generic Material Receipt Schedule Window.
  * #460 Provide aggregate HUs
    * Introduce the Handling Unit compression to only store and process the minimal needed information about Handling Units per step in supply chain. This Implementation is a huge Performance gain in Handling Unit processing.
  * #815 Jasper Footer: Show bank account in one line
    * Adjust the Jasper Reports Footer subdocument. Show all bank information now in same line.
  * #904 New Field "Zulieferant" in R_Request Window
    * Add new Field in Request window to allow the storage of an explicit Vendor Businesspartner.
  * #914 adjust weight in Docs_Purchase_InOut_Customs_Details function
    * Adjust the customs report to fit for swiss requirements in customs reporting. The gross weight is now calculated as Handling Units weight minus weight Logistics Unit Package Item.
  
* webui
  * #873 Customer & Vendor Subtab in BPartner Window WebUI
    * Add and arrange the customer and vendor subtab in Businesspartner Window in Web User Interface.
  * #196 Grid View 100% height
    * Adjust the Grid view height to expand to screensize.
  * #795 Price Window WebUI Layout
    * Add the Layout for the Price window in WebUI.
  * #896 Dunning Window WebUI
    * Add the Layout for the Dunning Window in WebUI.
  * #194 Open views from process execution result 
    * Add a new functionality that allows process results to receive a Window ID and open the corresponding Window after finishing the process execution.
     

## Fixes
* metasfresh
  * #857 Fix String Attributes Save in POS
    * Fix that now allows the possibility to save String Attribute in POS Windows also without loosing focus for recorded field.
  * #863 No Result Window for Prosesses that don't allow rerun
    * Fix for rerun parameter in Processes. Now possible to switch off the rerun confirmation Window after Process.
  * #879 Fix "ValueType not supported: D" when HU attributes are generated
    * Minor Fix for Value Type of generated HU Attributes.
  * #781 ESR scan processing returns improper bpartner
    * Fix and Enhancement of ESR Scan functionality in Purchase Invoices. Now allowing to select alternative Business Partner for on the Fly Bank Account generation. 
  * #783 DocAction on Sales Order not available although permission existing
    * Sysconfig to certrally enable/ disable the Document Action Close.
  * #813 hide packing instruction and qty when null
    * Fix that does not show the Packing Instruction and Packing Qty on documents anymore when null.
  * #903 Jenkins build error with slash in branch name
    * Minor Fix for Jenkins Build.
  * #870 Invoice Candidate price-qty overwrite lost when ReverseCorrect
    * Fix that stores the price & qty override in Invoice Candidates after Reverse-Correct of Invoice.
  * #910 Put explicit delivery date on invoice
    * Add the precise description for delivery date as demanded by german law.

* webui
  * #204 Can not complete Order
    * Minor Fix that now allows the completion of Sales Order in Web User Interface.
  * #886 GrandTotal missing in Purchase Order Grid view
    * Fix that enables the display of Grandtotal Field in Purchase Order Grid View.
  * #179 Fields too short for documentno in Breadcrumbs
    * Fix that extends the number of visible digits (now 9 digits) in Breadcrumb menu for Document or masterdata identifiers.

# metasfresh 4.54.53 (2017-05)

## Features
* metasfresh
  * #858 Adjustments for Shipment Schedule Grid View
    * Minor changes on the Grid View for the Shipment Schedules Window.
  * #868 Weekly Revenue Report
    * Create a weekly Revenue Report in Jasperreports. Similar to the montly report, just comparing different weeks instead months.
  * #827 use the barcode field to select HU using attribute value
    * New feature to be able to scan barcode attrivutes attached to a Handling Unit fir precise identifying.

* webUI
  * #198 Process with parameters cannot be started
    * Fix a Bug in WebUI that did not allow the start of processes with parameters.
  * #205 Batch entry Dropdown for Handling Unit Missing in Workflow
    * Add a Packing Unit dropdown in combined Product Field in Sales Orderline Batchentry.
  * #206 Deleting Batch entry product with "X" only deletes Product not Handling unit
    * Now allowing to delete the whole content of the combined Product-Packing Unit Field.
  * #208 After New autofocus on first record field
    * New UX Feature that automatically sets the focus onto the first recordable Field in Window when "New Record"
  * #213 Do not focus fields "in background" when in Expanded view
    * Adjust the navigation behavior and sequence when using TAB jumping from field to field, now avoids that the focus gets "under" the overlay panel.
  * #218 Reduce Gap Height between Layout Sections
    * Refine UX. Reduced the height between Layout Sections to not have the feeling of having a too big gap between them.
  * #862 Payment Window in WebUI
    * Include the payment Window in WebUI.
  * #873 Customer and Vendor Subtab in BPartner WebUI Window
    * Include the Customer and Vendor Subtab in Business Partner Window.
  * #883 Sales Purchase Order Window Grid View
    * Include the Grid View for the Sales and Purchase Order in WebUI.
  * #878 Purchase Order Window WebUI
    * Include the Purchase Order Window in WebUI.

## Fixes
* metasfresh 
  * #782 Focus on the first process parameter
    * Fix to allow the first recordable Field having focus when opening a process paramater window.
  * #864 Adjust C_Country Location Print generation DE
    * Adjust the Location capture Sequence for Germany.

# metasfresh 4.53.52 (2017-04)

## Features
* metasfresh
  * #800 Order by product name and partner name in pmm_PurchaseCandidates
    * Add a new possibility to be able to sort combined search fields by a selected element in the combined Field, e.g. a field combined as Value + Name can now be sorted with Name, and not just Value + Name.
  * #829 receivedVia entry not translated in Baselanguage de_DE
    * Add the Translation for receivedVia Field in current Baselanguage de_DE.
  * #810 Propagate Attribute from Issue to Receive in Production
    * New Functionality to propagate selected Attributes vertically though a manufacturing process, from action issue to action receipt.
  * #835 Switch off Process Confirmation Window
    * Switch off all process confirmation Windows per default. These can be switched on individually per Process where wanted.

* webui
  * #817 Request Window in WebUI
    * Initial setup of Request Window in Web User Interface including default view, advanced edit, grid view and sidelist.
  * #831 Default Document Layout for WebUI
    * Overhaul of the current general Document Layout for WebUI
  * #847 Shipment Schedule Window in WebUI
    * Initial setup of the Shipment Schedule Window in Web User Interface.
  * #853 Shipment Schedule Window Subtabs in WebUI
    * Add the Subtabs definition to Shipment Schedule Window in the new metasfresh Web User Interface, including Sidelist.
  * #855 Shipment Schedule Advanced Edit Mode
    * Setup for the Advanced Edit Mode of Shipment Scheule in WebUI.
  * #108 Create Callout for DocNo in Request
    * Adjustment/ Enhancement of the DocumentNo Generation in non Document datastrucures of WebUI
  

## Fixes
* metasfresh 
  * #785 Make M_InOutLine.IsInvoiceCandidate Iscalculated
    * Adjust the flag isInvoiceCandidate to be calculated for M_InoutLine records.
  * #808 DocActionBL.retrieveString method is broken
    * Fixing the method that retrieves the Document Action Name.
  * #819 fix/refactor CalloutRequest
    * Adopting the Callouts in Request window to also work in Web User Interface.
  * #806 Customs report minor fixes
    * Minor adjustments and fixes in the Customs report.
  * #837 Marginal return accounts doubled
    * Minor Bug Fix in marginal return report that doubled the sums on certain accounts.
  * #844 Gear from Pricing System and PriceList show wrong processes
    * Fixing a Bug that leads into wrong representation of Processes in Gear of Pricing System and Priceliste Window.

* parent
  * #3 Add repo.metasfresh.com also as plugin repo
    * thx to @sramazzina

# metasfresh 4.52.51 (2017-03)

## Features
* metasfresh 
  * #774 show address on all docs so it fits the letter window
    * Adjust all Documents so that the address fits into the letter window od envelops C5/ C6 according to ISO 269 und DIN 678.
  * #773 show delivery address on sales order
    * Show the deliverTo location on Sales Order Documents.
  * #507 Copy with Details for PP_Product_BOM
    * Add a new Functionality to allow copy-with-details on Bill of Materials records.
  * #780 Have logo on jasper report that spans from left to right
    * Rearranged the logo placing on Documents to allow the upload and usage of large, page-spanning Logos from left to right.
  * #816 Do not show prices on shipment note
    * Undisplayed the prices on shipment documents.

## Fixes
* metasfresh 
  * #615 Purchase Order wrong Price from Contract or Pricesystem for specific Product
    * Addresses the possible case of different procurement products that have different attributes and still both match equally well.
  * #791 Create Nachbelastung from Invoice Cast exception
    * Fixes a Bug that lead into an exception when creating an adjustment charge to an existing invoice.
  * #761 Reactivating an InOut fails sometimes
    * Now the reactivation of InOuts also works with records that were deleted at the time the async-package is processed
    
# metasfresh 4.51.50 (2017-02)

## Features
* metasfresh
  * #696 add multi line description per order line
    * Possibility to now add multiline descriptions. These can be used to add individual Texts to an Orderline.
  * #755 Automatic upload orders in csv file with COPY into c_olcand
    * Enhancement to allow the Upload of Sales Orders into Order Candidates via COPY.

## Fixes
* metasfresh  
  * #752 request report does not show requests that don't have product or inout
    * Fix to show request lines in report, which don't have a product included nor a reference to an inout line.
  * #759 Destroyed HU causes problem with shipment creation
    * Fix problem when checking for not-yet-delivered M_ShipmentSchedule_QtyPicked records, the system did not check if they reference actually destroyed HUs.
  * #766 fix for "DocumentPA will not be intercepted because final classes are not supported"
    * Fix this error shown on server startup. Making DocumentPA not final anymore.
  * #770 When extending a procurement contract, null becomes 0.00
    * Fix an issue that set the price to 0,00 when extending a procurement contract, although the initial price was null which means "not set".
  
# metasfresh 4.50.49 (2017-01)

## Features
* metasfresh
  * #615 Purchase Order wrong Price from Contract or Pricesystem for specific Product
    * Working Increment that works for the current requirement at hand.
  * #653 Calculated DailyLotNo in Material Receipt Candidates
    * Add a Daily Lot No. thats calculated as Day from year, from a given Date in Purchase Order, Orderline Attributes.
  * #714 Marginal Return report calculation add additional Costcenter
    * Adjust the marginal return report to show allow more columns with cost center sums on 1 page.
  * #742 R_Request column c_order_id autocomplete too slow
    * Take out the autocomplete of c_order_id to speed up the lookup performance in R_Request.

## Fixes
* metasfresh
  * #757 Automatic contract extension doesn't work anymore
    * Fix the automatic extension of contracts when flatrate term conditions are met.
  * #681 Automatic filling of BPartner and Location only shows value
    * Fix the Search Field reference that showed only the value, instead of Name and value, in case of BPartner and BPartner Location.
  * #718 Wrong location in empties vendor return
    * Close the Gap that allows to record empty returs with BPartner Location that does not belog to the empties BPartner.
  * #744 Report Bestellkontrolle promised Date-Time seems to have am/pm time formatting
    * Adjust the Purchase Order control report to have the correct locale for time formatting.
  * #763 material receipt HU label always shown in preview
    * Migrate the Handling Unit label enabling direct print, without print preview.
  
* webui
  * #89 Adjust DocAction Names
    * Adjust/ migrate DocAction Names for WebUI.

# metasfresh 4.49.48 (2016-51)
 
## Features
* metasfresh
  * #489 Implement DLM within single logical tables
    * Data Life-cycle Management Implementation to enable archiving of non-operational data to separate partitions.
  * #682 Translation in window Vendor Returns
    * Add german translation of additional Fields in Window Vendor Returns.

* webui
  * #698 Pipeline - add webui deployment
    * Add a new Pipeline into Continuous Integration/ Deployment for metasfresh WebUI.

## Fixes
* metasfresh
  * #380 duplicate lines in inout
    * Worked over each jasper report in order not to display materdata records that were deactivated.
  * #710 MRP Product Info: Qtyies issued to a production shall be subtracted from onhand qty
    * Fix a Bug that prevented Handling Units Storage to be adjusted when adding raw material to manufacturing order via action issue.
  * #724 Aggregation Shipment Jasper Documents shows reference from other ad_org_id
    * Extend the where clause for matching of PO References in Aggregation inout documents. Additional Aggregation matching criteria now are ad_org_id, c_bpartner_id.
  * #713 Marginal Return Report (short version) doubled sums for accounting group
    * Fix a partially double summed up accounting group in marginal return Report.
  
# metasfresh 4.48.47 (2016-50)

## Features
* metasfresh
  * #677 make customs report faster
    * Significant improvement of the customs report performance.
  * #541 Remove PiPo from metasfresh removing code and data
    * Remove the legacy code from Pack-In and Pack-out from metasfresh. The underlying concept is flawed and does not scale.

* webui
  * #625 Shipment Schedule Window WebUI
    * Add initial Layout configuration of Shipment Schedule window in metasfresh WebUI.
  * #687 webUI bundle
    * Add different Layout changes in a fair amount of windows for Web User Interface.
    
## Fixes
* metasfresh
  * #679 Bug in ClientUpdateValidator
    * Fix a Bug in ClientUpdateValidator that avoided starting the client via eclipse for local-build. 
  * #721 Wrong error message displayed when user enters wrong password on login
    * Fix for Bug when entering wrong password in Login. Said "locked" but was just wrong credentials/ password.

# metasfresh 4.47.46 (2016-49)

## Features 
* metasfresh
  * #639 Marginal Return report calculation does not check ad_org_id
    * Extend the marginal return report with ad_org_id parm to allow to seperate user for other organisations.
  * #585 Adjust the remaining Property names
    * Change properties to metasfresh namespace.
  * #661 Cultivation Planning report adjustments
    * Adjustments made to the cultivation planning report in procurement.
  * #515 Generating C_Flatrate_Term from C_RfQ_Response then don't complete the term
    * Avoid automatic completion of flatrate term contracts when triffered manually from process gear. This allows the user to record further adjustments after creation.

* webui
  * #48 Add initial setup of kibana kpi for new webUI dashboard
    * Setup an initial set of 10 key perfroamnce indicators for the new metasfresh webui.
  * #59 User friendly URL for Print Endpoint
    * Add a user frindly/ comprehensive endpoint for document printing tab in webUI.
  * #45 Dashboard Target area backend support
    * Add support for Target widgets and target widget area in webUI dashboard
  * #567 WebUI - Material Receipt Schedule
    * Initial set of windows, grid views, sidelist and elements and fields for material receipt schedule window.

## Fixes
* metasfresh
  * #658 make Ini more robust: throw ex if file can't be read
    * Fix error with long loading of ini file in Tomcat.
  * #664 R_Request Performance Issue
    * Swap Table direct references against search in all R_Request table/ subtable fields to reduce current performance issues.
  * #674 Filter operator "between" broken
    * Fix the operator "between" which is used in filtering/ search criteria.
  
* webui
  * #67 Error when introducing parameters to report
    * Fix parameters support for report usage in webui.
  * #70 Add BPLocation Error
    * Fix Errors that prevented the creation of new Business Partner Location lines in webUI.

# metasfresh 4.46.45 (2016-48)

## Features 
* webui
  * #425 Kickstart elasticsearch integration
    * Add the first prototype of elasticsearch integration in WebUI environment of metasfresh ERP. Data for elasticsearch index is created via metasfresh async framework.
  * #598 WebUI Dashboard initial Prototype definition
    * Create a prototype dashboard in new metasfresh WebUI. Current prototype uses kibana for KPI and data visualization.

## Fixes
* metasfresh
  * #583 Reports without ad_org_id show wrong results
    * Add support for multi organisation usage of selected 22 reports.
  * #620 Marginal Return Report doubled sums for accounting group
    * Fix the doubled sums in Marginal return report for specific accounting group.    
  * #656 Bug in Import Format - Copy lines process
    * Fix a minor Bug in Import format.
  * #646 Fix support for groovy scripts
    * Fix groovy Script support and extend fieldsize for script recording.

# metasfresh 4.45.44 (2016-47)

This week's RC

## Features
* metasfresh
  * #515 Generating C_Flatrate_Term from C_RfQ_Response then don't complete the term
    * Adjust the completion process of Flatrate terms created manually. Now the flatrate term in not completed and can be manually adjusted by the user without reactivating.
  * #563 Report Statistics qty per Week
    * New sales qty report that shows the sold product quanities per week and in comparison the last 11 weeks.
  * #579 Handling units without washing cycle shall be allowed in washing Manufacturing Order
    * Adjustment of Handling Unit permissions in manufacturing order, initially filtering out HU with washing cycle set.
  * #597 Empties mask and functionality with autom. set the selected bpartner
    * New functionality to add informations about Businsspartner, Location and Purchase Order Reference. This allows the to raise the efficiency when checking and creating purchase invoices via invoice candidates.
  * #576 Report Reclamation result, quality note and minor changes
    * New reqirements implemented in reclamation report.
  * #539 Add missing FK constraints
    * Add further missin Foreign Key constraints surfacing during Data Lifecycle Management implementation.
* webui
  * #567 WebUI - Material Receipt Schedule
    * Add initial Screen Layout for Material receipt schedule in metasfresh WebUI.
  * #497 WebUI - ShipmentSchedule Window
    * Add initial Screen Layout for Shipment Schedule in metasfresh WebUI.

## Fixes
* metasfresh
  * #589 console error when doing bpartner setup
    * Fix a minor bug with jax-rs/ jms timeout in Business Partner setup workflow, which contantly popped up in console.
  * #553 Report Account Info adjustments. Add parms date range.
    * Add the parms date range back into Account Info report in Jasper.
  * #611 IBAN Error for RBS Bank
    * Add support for RBS Bank in metasfresh IBAN check when creating a new Businesspartner Bankaccount.

# metasfresh 4.44.43 (2016-46)

## Features
* metasfresh
  * #553 Report Account Info adjustments. Add parms date range.
    * Enhancing the Filter parms to allow variable daterange for selection.
  * #557 Report Saldenbilanz & Account Info native Excel Export
    * Now Allowing an Excel Export though Report viewer process.
  * #558 Marginal return calculation - Accountings without c_activity_id
    * Marginal Return now considers specific records without activity to be calculated on account specific one.
  * #568 Change on Report "Lieferschein" for one specific Customer
    * Add properties File for Shipment Report.
  * #555 Businesspartner Location isEDI shall not be ticked by default
    * Don't set the Flag isEDI per default when recording new Businesspartner Loactions.
  * #548 keep M_QualityNote and M_AttributeValue in sync
    * New Functionality to sync the M_QualityNote and M_AttributeValue for R_Request complaints usage.
  * #577 Button Request shows too many results
    * Adjust the Filtering of Request Button in main menue and show Role Based counter.
  * #565 Report Revenue per Week and BPartner also show qty
    * Add a new Quantity value in reports Revenue per week and week Businesspartner.
  * #416 Extended async notification features
    * Prepare the notification features for WebUI exposure in metasfresh nextGen.
 
## Fixes
* metasfresh
  * #578 Request Window Attachment Image too large in viewer
    * Fixes a Bug that scales window too large after uplaoding a large image.

# metasfresh 4.43.42 (2016-45)

## Features
* metasfresh
  * #504 new filter in saldobilanz report
    * Added a new filter in saldobilanz report to exclude the year end accountings (profit & loss) from report.

* metasfresh-webui
  * #41 Implement Dashboard REST endpoint
    * Added a new REST-API endpoint for WebUI KPI widgets.

## Fixes
* metasfresh
  * #552 division by 0 in costprice report
    * Fixed a division by 0 Bug in costprice report.

* metasfresh-webui
  * #40 Account fields are not working
    * Fix in new WebUI Implementation. An exception occured because of Field Type account.
  
# metasfresh 4.42.41 (2016-44)

## Features
* metasfresh
 * #500 Migration: Create Requests for all inout lines with quality issues
   * SQL Migration Path for all Material Receipts with Quality Issues. Reclamation requests are created.
 * #514 Reclamations report: group the inouts with ff.
   * Create a new Report to analyze the Performance Issues in Vendor receipts/ customer deliveries. The report shows all details to performance issues (Quanitity-, Quality-, Delivery-, Receipt-Performance).

## Fixes
* metasfresh
 * FRESH-823:#536 Context bug in MLookup
   * Fixed a minor context Bug in MLookup Fields.
 * #540 Table and Columns - IsLazyLoading flag is not displayed
   * Fixed a Bug that occured in Table and Columns Definition, preventing isLazyLoading to be shown.


# metasfresh 4.41.40 (2016-43)

## Features
* metasfresh
 * #505 Possibility to define multiple Washing Testcycles for Carrots
   * Quality Assurance Feature for Long Term Storage vegetables. Prossibility to define Washing cycles and route  the Logistic Units to manufacturing Order.
 * #503 Beautify C_PaySelection_CreateFrom and C_PaymentTerm fields
   * Adding better descriptions for Parameters in Payment selection Process.
 * #412 Get rid of AD_Tab.OrderByClause
   * Adapting the sorting machanism in Tabs to allow Layout engines to receive precise Informations which columns are sorted. Initially needed for new WebUI.
 * #424 Migrate spring-boot from 1.3.3 to 1.4.x
   * Updated spring boot-from to to allow the usage of a recent elasticsearch version.
  
* metasfresh-webui
 * #27 Support for custom order bys in browseView
   * Added new Support for a custom order by criteria in grid-/ browse view.
 * #29 Adapt Invoice candidates window to webui
   * Adapted the Invoice Candidates window to WebUI.
 * #31 Implement document actions
   * Implemented the Document Action for the Web User Interface.
 * #32 Implement document references
   * Provided the Document References to embed these in navigation contex of each document.
 * #33 Implement document filters from AD_UserQuery
   * New and much easier Filtering criteria for data selections in metasfresh nextgen.
 * #20 Cache lookups
   * Optimize lookups content loading with cache functionality.

## Fixes
* metasfresh
 * #508 Creating User without Business Partner throws Exception
 * #487 Attribute editor dialog stores empty field as ''

# metasfresh 4.40.39 (2016-42)

## Features
 * #443 Add is to be sent as email to doc outbound log
 * #418 Improve sales and purchase tracking reports

## Fixes
 * #407 CCache always creates HashMap cache even if LRU was requested
 * #428 NPE when reversing an invoice including a product with inactive UOM conversion
 * #492 build issue with jaxb2-maven-plugin 1.6 and java-8
 * #483 Gebindeübersicht Report Typo fix
 * #482 Unable to issue certain HUs to a PP_Order
 * #494 R_Request new Request context missing

# metasfresh 4.39.38 (2016-41)

## Features
 * #388 make M_ReceiptSchedule.IsPackagingMaterial a physical column
   - Changing the Field in Material Receipt Schedule fpr Packing Materiel. Swapped from pirtual to physical column.


## Fixes
 * #448 Rounding issue with partical credit memos
   - Fixing a rounding issue which popped up after createing a partial credit memo for referenced invoice document.
 * #270 Purchase Order from Sales Order Process wrong Aggregation
   - Optimized the Purchase Order creation process from Procurement candidates. Purchase Orders are now aggregated properly when identical Vendor and products (and further details).
 * #433 C_Order copy with details: Packing Instructions are not copied
   - Fixed a Bug when using Copy with details in c_order. Packing Instructions are now copied too.

# metasfresh 4.38.37 (2016-40)

## Features
 * #395 Add Description in Jasper Invoice Vendor
   - Added a new row in to allow the display of optional line text in further invoices

## Fixes
 * #451 OCRB not available in JVM but needed for ESR page
 * #431 QtyTU does not update in wareneingang pos
 * #436 Single lookup/list value for mandatory field is not automatically set
 * #454 barcode field is reset after 500ms
 * #455 autocomplete in non-generic fields not working anymore

# metasfresh 4.37.36 (2016-39)

## Features
 * #302 Add different onError policies to TrxItemChunkProcessorExecutor
   - Added further policies for InvoiceCandidate processing.
 * #213 Add onhand qty to MRP Product Info
   - Added a new column in MRP Product Info to now show the Handling Unit Storage On Hand Quantity.
 * #375 Jasper: extend product name on report_details
   - Extension of name Field in Jasper Report (report_details).

## Fixes
 * #409 MRP Product Info might leave back stale entries after fast changes
   - Making sure that statistics in MRP Product Info are updated also after quick complete and reactivate of sales and purchase orders.
 * #387 Purchase Order generation in Procurement Candidates not to be grouped by user
   - Ensured that Purchase Order Candidates are aggregated to 1 Purchase Order per Vendor when triggered.
 * #370 Material Receipt - Somtimes double click needed for weighing machine
   - Fixing a bug that occured on certain Windows clients with connected weighing machines.
 * #420 NPE in CalloutOrder.bPartner
   - Eliminated a Null Pointer Exception in Sales Order Callout CalloutOrder.bPartner.
 * #410 sscc label org fix
   - Fixed a minor issue in SSCC Label to load the correct Orgabnisation and Logo when generated and printed.
 * #422 pricelist report do not show virtual HU
   - Fixed the pricelist report to also show virtual HU.
 * #331 Activating the trace log file doesn't always work
   - Stabilized the new trace log feature. Here switching on/ off visibility.
 * #437 Old window Produktion is opened automatically by menu search
   - Fixed the autocall in menu search.

# metasfresh 4.36.35 (2016-38)

## Features
 * #395 Add Description in Jasper Invoice Vendor
   - Added a new row in Vendor Invoice to allow the display of optional line Text.
 * ME-46 Support Ubuntu 16.04 server with metasfresh server installer
   - Milestone Feature: Provided a new metasfresh installer for Ubuntu 16.04
 * #369 request report
   - Provided an excel report for quality analysis bases on dispute request history and data.
 * #361 Request change for customer service
   - Added possibility for dispute requests
 * #377 Implement executed SQLs tracing
   - Admin Functionality to enable better Performance Tracking of SQL.
 * #338 Get rid of legacy NOT-EQUALS operators from logic expression
   - Getting rid of not-equals operators in logic expressions.
 * #333 All tables shall have a single column primary key
   - Change needed for metasfresh WebUI and Rest API. All tables used in WebUI/ Rest API shall have a primary Key.
 * #21 UI Style default for elements
   - WebUI Fallback Szenario for elements when UI Style is not explicitly set.
 * #20 Cache lookups
   - WebUI Performance tweak. Now allowing caching for lookups.
 * #18 Optimization of root & node requests.
   - Added limitCount to path elements to allow accurate results for Navigational structure in WebUI.
 * #16 Implement virtual document fields support
   - New functionality for WebUI to allow fields with content computed by Business Logic.
 * #14 Layout documentSummaryElement field to be used for rendering breadcrumb info
   - Added DocumentSummary support for breadcrumbs in metasfresh WebUI/ Rest API.
 * #13 elementPath should return path without element
   - Possibility to return path without leaf node.
 * #11 Implement grid view support
   - Awesome, new possibility to open Windows in grid view representation.
 * #10 implement documents filtering support
   - Providing metadata for filtering via RestAPI for example for grid view.
 * #9 provide precision for numeric layout elements
   - WebUI: Detailed precision funcionality for amount and costs/ prices elements.
 * #7 provide "grid-align" for layout elements
   - Generic alignment possibility via application dictionary used for metasfresh WebUI.
 * #24 Breadcrumb Navigation Plural caption
   - Added a plural caption for WebUI Breadycrumb navigation.

## Fixes
 * #411 missing index on C_OrderTax.C_Order_ID
   - Performance change. Adding index for c_ordertax.
 * #367 Invoice candidates invoicing Pricelist not found
   - Fixing a minor issue during invoiceing. Pricelist was not found under certain circumstances.
 * #380 duplicate lines in inout
   - Eliminating an issue when deactivating a product and adding another product with same EDI GLN.
 * #348 Sort tabs shall consider Link column and parent link column if set
   - Fixing issue considering link and parent column when sorting.
 * #330 Process's RefreshAllAfterExecution does not work when the record was moved
   - Eliminiating an issue when refreshing after execution of processes.
 * #327 Got NPE when completing a drafted order
   - Fixing a Null Pointer Exception when trying to complete a drafted order document.
 * #337 ERROR: duplicate key value violates unique constraint "c_bpartner_stats_c_bpartner_id_unique" triggered from some callouts 
   - Fixing an exception when trying to select a BPartner without valid ship location in Sales Order.
 * FRESH-257 WI1 - rendering a window with tab, one field per field type incl. editor and fieldgroup
   - Initial WebUI Proof of Concept Task. A lot has already done since this one, even more to be expected.
 * FRESH-369 Change bpartner in order -> pricelist does not update
   - Fixing callout issue not updateing the correct pricelist whan changing a Business Partner in Sales Order.
 * #379 Included tab randomly not working in inout and invoice
   - Fixed a bug that ranomly prevented the correct rendering of included Tabs in Invoice window.
 * #12 Data not shown in SubTab
   - Adjusted the data defined in RestAPI for Subtab content.
 * #311 Payment Selection Exception when not able to find bpartner account
   - Added further Account seelection functionality to prevent Exception when selecting BPartner without Bank account.
 * #378 Bug in validation of field docsubtype
   - Eliminated an issue which apperared in Doctype Definition when selecting a DocSubtype.
 * #262 sales and purchase tracking
   - Minor tweaks and fixes in sales and purchase tracking Report.

# metasfresh 4.35.34 (2016-37)

## Features
 * FRESH-112 metasfresh web 
   - Integrated recent backend related changes done for metasfresh REST API Implementation.
 * #359 document Note not displayed on invoice
   - Fixed the issue that c_doctype.documentnote was not shown properly on Jasper invoice documents.
 * #262 sales and purchase tracking
   - Implemenation of a large Sales and purchase Tracking Report inclusing possibility to export to excel.
 * #354 Rearrange unloading fields in Sales Order Window
   - Adjusted the validation- and display-rules in sales order window abould fields for unloading (Partner, Location).

## Fixes
 * #366 Faulty unique constraint on M_PriceList
   - Fixed a wrong contraint in M_Pricelist, that prevented creating Product-BPartner-Price-Combination with BPartner recorded on client level.

# metasfresh 4.34.33 (2016-36)

## Features
* #297 Performance problems related to zoom-to
  - Improved user experience, massively reduced loading times for generic zoom-to links in icon-bar.
* #249 Referenzliste in AttributeValue
  - Getting rid of Reference List (System) in client side Attribute Values.
* #347 change default docaction after complete
  - After eliminating all close docactions in permissions now changing the next docaction in document Workflow.

## Fixes
* #315 ReceiptSchedule.QtyToMove not properly updated on reopen
  - Fixed a Bug that prevented the correct update of ReceiptSchedule.QtyToMove after reactivating the record.
* #319 material tracking - deduplicate numbers in article statistics report
  - Getting rid of duplicated statistic qmounts in material Tracking report.
* #329 Revenue reports BPartner & Week show different amounts when HU Price
  - Fixing an issue in Business Partner Revenue Weeek Report that appeared with Usage of Transportation Units with multiple Customer Units in shipments.
* #340 Validation Rule in C_BPartner_Product for C_BPartner_ID wrong
  - Minor Fix of the Validation Rule in C_BPartner_Product. Now also allowing Businesspartners on Client Level.
* #351 translate order summary
  - The Order summary is now covered by translation.
* #335 Invoicing taking wrong Documenttype for Producer Invoice
  - Issue solved when changing the Parms for Producer Invoiceing in Business Partner after already having created Invoice Candidates.

# metasfresh 4.33.32 (2016-35)

## Features
 * #320 material tracking - provide excel friendly information view
   - New SQL report for material tracking

## Fixes
 * #299 Report "Leergutausgabe" from Window "Lieferantenrücklieferung"
   - Localized the empties Report so that one can now include alternative languages.
 * #315 M_ReceiptSchedule.QtyToMove not properly updated on reopen
   - The QtyToMove is now updated after reactivating/ reopening the Material Receipt Schedule.
 * #225 Allocation - Accounting 0,00 when Posted
   - Fixed further minor issues in reposting manchanism for Payment Allocations.
 * #277 Invoice candidates sums at the bottom are not considering org-assignment
   - Fixed an issue with Org Role Access in Window Invoice Candidates. Now the Status Row considers the Org Access Permission.

# metasfresh 4.32.31 (2016-34)

## Features
 * #276 Report Konten-Info new Parameter
   - Added new Parameters (VAT-Period from-to, Account No. from-to) to Account Information Report.
 * #273 Report "Anbauplanung" addition & adjustment
   - Adjusted  the Report for "Anbauplanung" to print out additional information.
 * #272 Report Karottenendabrechnung / Translate Headlines in Reportlanguage = FRENCH
   - Implemented the localization for the fresh produce invoice document.
 * #295 sql in purchase inout takes too long
   - Performance improvement in the jasper file.
 * #292 Automatically add reference no from purchase order to invoice candidate
   - The purchase order reference No is now automatically included in the corresponding invoice canidates.
 * #293 Create cron job for archiving the async-tables
   - Created a cron Job to automatically archive the async data. This speeds up the overall async Performance for large environments.

## Fixes
 * #251 Invoice Candidates double invoiced
   - Fixed a bug that seldomly appeared after invoiceing and caused that an invoice candidate could be invoiced twice.

# metasfresh 4.31.30a (2016-33a)

## Features
 - #297 Performance problems related to zoom-to
   * improve the documentation, both in that code and in the client
   
## Fixes
 - #298 ShipmentSchedule updating fails on missing UOM conversion
   * prevent an NPE on missing master data

# metasfresh 4.31.30 (2016-33)

## Features
 - #288 Problem with individual client log settings
   * outputting the individual log settings on user login to ease support

## Fixes
 - #275 In Picking HU Editor. New Flag ignore attributes for Filter
   * fixing some corner cases

# metasfresh 4.30.29 (2016-32)

## Features
 - #279 Set document type Bestellung as default value in purchase order
 - #275 In Picking HU Editor. New Flag ignore attributes for Filter
 - #283 Make Gebindesaldo Report support Multi Org

## Fixes
 - #255 Invoice candidates action bar is not considering org-assignment
 - #274 Purchase Order without BPartner Contact, Billto Contact wrong email
 - #252 Fix the code for ADR Attribute Retrieval
 - #243 C_Invoice_Candidate - Processed not always updated if IsToClear

# metasfresh 4.29.28 (2016-31)

## Features
 - #241 Excel Export for Open Items accounting currency
   * Adjusted the Excel Export of Open Items report ro show the sums in accounting currency.
 - #240 Consitency check page for Saldobilanz
   * Added a new Page in Salsobilanz report to allow quick consistency check of accountings.
 - #225 Allocation - Accounting 0,00 when Posted
   * Added a process which double checks the accountings of the day. If a document ist posted and the accounting results are 0,00 then a the document information is logged and the document is reposted.

## Fixes
 - #176 Bestellkontrolle add Promised Date
   * Added the Date promised onto report Bestellkontrolle.
 - #263 Delivery Conditions Flag sometimes not set in Procurement Candidates
   * Fixed a Bug that did not detect contracted lines in Procurement candidates if the contractline was not defined with given price.
 - #248 Admin Login when deleted properties
   * Adressed a security issue that allowed to gain admin permissions after deleting the client properties.

# metasfresh 4.28.27 (2016-30)

## Features
 - #201 KPI Accounted Documents
   * Created the logic for our new KPI "Accounted Documents" which will be included into our new metasfresh webUI.
 - #212 MRPProductInfo display Conference flag with sys config
   * Added a configuration possibility to be able to switch the conference flitering functionality on-off in MRP Product Info.
 - #226 show accounting currency in open items report
   * Implemented a counter check Open Items Reports (customer, vendor) with Accounting Balance. Open Items in foreign currency now show also the sums in accounting currency.
 
## Fixes
 - #153 PaymentRule = S in Invoice
   * Checked Code for direct setting of PaymentRule cheque got rid of it.
 - #220 Do not load pricelist and pricelist version  on login
   * Not loading Pricelist and Pricelist Version into context anymore during login.
 - FRESH-402 Procurement bidding
   * Minor jasper fix
 - #232 Separate c_flatrate_terms from the normal procurement and RfQ in procurement Excel
   * Separated the sums of procurement contracts vs. sums of RfQ biddings.

# metasfresh 4.27.26 (2016-29)

## Features
 - #152 Improvements in  counter documents view and window
   * Added different fields. Renamed fields for better understanding. Added different validation rules.
 - #173 Window Dunning Candidates - new Field DocumentNo
   * Included the Invoice Document No. in Dunning Candidates window.
 - #183 Error in material tracking if one partner has two contracts (with different conditions) for the same product
   * Skipping a number of unneccesary things if an invoice candidate's `Processed_Override` value is set to "Y"
 - FRESH-402 Procurement bidding
   * Adding jasper file for the procurement documents
 - #181 Customer specific Lieferschein without Price
   * New Jasper Shipment Document that does not show the prices for each line.

## Fixes
 - #216 Accounting: Invoice grand total Fact_Acct line was not found
   * Fixed a Bug in accounting of Allocation Header that appeared in certain circumstances when writing off paid amount completely.
 - #100 EDI wrong handover location in Picking Terminal
   * Sales Orders created via EDI Import could have the wrong handover Locations in a certain condition. This wrong Location appeared in Picking Terminal. Fixed this.
 - #174 Report Konten-Information empty c_activity_id
   * Added a new Flag in Report Parameters to show all entries which have a empty c_activity_id.
 - #203 Payment writeoff not possible for Incoming Payment
   * Fixed a Bug that prohibited the writeoff in incoming Payments.
 - #175 C_Invoice_Update_PaymentRule
   * Changed Lagacy Code that set the Payment Rule to cheque.
 - #210 product appears twice in invoice print preview
   * Minor Bug in Jasper Document that printed out the wrong quantity and total for an invoiceline when shipment quantity was 0 and the invoiceline was aggregated with more than 1 shipment.
 - FRESH-529 drop qtyreserved from product info
   * Dropped the column qtyreserved from product info window.

# metasfresh 4.26.25a (2016-28a)

## Fixes
 - #204 FRESH-525 db_columns_fk view is not working correctly anymore
 - #194 FRESH-517 Jasper Report Error: java.net.BigDecimal
 - #158 FRESH-495 Make de.metas.fresh.printing.spi.impl.C_Order_MFGWarehouse_Report_NotificationCtxProvider thread safe
 - #202 FRESH-522 Payment-in-out-allocation buggy when partial allocation
    * Fixed the newly introduced Feature about allocating 2 Payments (in-out) in cornercase with partial allocated Payments.

# metasfresh 4.26.25 (2016-28)

## Features
 - #182 FRESH-510 Report "Wareneingangsbeleg" with Information "1." / "2. Waschprobe" ergänzen
    * Small layout-change and additional field for quality inspection. Thanks to our new contributor @Spavetti
 - #185 Fresh-512 Receipt POS - sometimes gets wrong numbers from weighting machines
    * Additional glasspane implementation to avoid uncontrolled button activations during the wighing process in Material receipt. Extended logging of weighing information.
 - FRESH-402 Procurement bidding
    * Major new Feature allowing an efficient Procurement Request for bidding workflow, including the extended Procurement bidding web application and automated creation of procurement candidates for selected winners.
 - #119 FRESH-455 different email per org in inout print preview
    * Possibility to define and use different eMail adresses for InOuts, depending on document Organization.
 - #142 FRESH-479 C_AllocationHdr.C_AllocationHdr_ID: Loader too many records
    * Changed the Fieldreference in Subtab to Search-Field to improve opening Performance.
 - #150 FRESH-492 Fix implementation for BPartner Statistics
    * Refactoring, code improvement
 - #128 FRESH-465 Extend Record_ID Column Implementation
    * Extended the Record_ID column Feature to allow more than 1 generic table-record-button to jump to referenced Dataset.
 - #145 FRESH-482 Don't log migration scripts if the transaction failed

## Fixes
 - #197 FRESH-519 Payment void or reverse correct
    * Fixed a Bug that could occure when trying to void or reverse-correct a Payment Document.
 - #151 FRESH-491 When creating a new organization, don't create org access for System role
    * Now we don't automatically create an Org Access for System Role anymore.

# metasfresh 4.25.24a (2016-27a)

## Features
 - #162 FRESH-499 modernize the server's index.html
    * Adjusted the index.html to upgrade of metasfresh to Java 8 usage.
 - #139 FRESH-475 Check for java8 in the rollout-scripts
    * Adjusted the rollout scripts of metasfresh to Java 8 usage.

## Fixes
 - #123 FRESH-460 Users find window name "window, tab, field" confusing
   * also updated menu items
 - #147 FRESH-484 Error creating manual DD_Order
 - #148 FRESH-485 de.metas.async.api.<WP-Name>.AD_User_InCharge_ID can't be overriden on org level

# metasfresh 4.25.24 (2016-27)

## Features
 - **FRESH-399 Upgrade to java-8**
    * Existing users, please see [this howto](http://docs.metasfresh.org/howto_collection/Wie_aktualisiere_ich_die_Java_Version_auf_meinem_server.html) for instructions on how to update your metasfresh server
 - FRESH-397 Upgrade to JasperStudio and latest jasper version
    * Update of Reports and Documents in metasfresh to use the latest Jasper Studio and Jasper Reports Version. Updating to jasperreports-6.2.1
 - #136 FRESH-472 Sequence on Org for more than 1 Doctype
    * Enhancement of Document Sequence seperation among different Organisations. Implemented so that Organisations may uses same Doctypes but seperated Doc Sequences.
 - #90 FRESH-417 Create view and window to identify missing counter documents
    * Implemented View to allow check if counter Documents are missing.
 - #132 FRESH-468 Excel Export of report Konten-Information not working
 - #123 FRESH-460 Users find window name "window, tab, field" confusing
    * Adjusted Name of Window "Windows, Tabs and Fields"
 - #124 FRESH-461 Role "System Administrator" is disabled
    * Note: Not a "fix" because we deliberately deactivated it before and now find that the normal user is better off with the role being available.
 - #125 FRESH-462 enable all entity types
    * Not a fix, the reasoning is similar to #124
 
## Fixes
 - #137 FRESH-473 Glitches running metasfresh out of eclipse
    * adding a lauch config to run the client with embedded server
    * removing a not-needed dependency that might not be available
    * ignoring local activemq data
    * thx to @pmaingi for going through them with us

# metasfresh 4.24.23a

## Features
 - #121 FRESH-457 Make recipient of the mail configurable in Process SendPDFMailsForSelection

## Fixes

# metasfresh 4.24.23

## Features
 - FRESH-378 process to close invoice candidates
    * New Feature in Invoice Candidates Window which allows the mass manipulation of records setting these to "processed". Also checking and updating referenced shipment candidates during this workflow.
 
## Fixes
 - #118 FRESH-454 Dont create InvoiceCandidates for DocSubType Saldokorrektur
    * Changed InOut Handler to not create Invoice candidates when DocSubType is "Saldokorrektur".
 - #104 FRESH-441 Notification bar in Material Receipt (POS) covers OK Button
    * Moved the Notification Bar slightly up, so the OK Button, Cancel Button and Changelog Link is not covered anymore.
 - #107 FRESH-445 Awkward eMail encoding in Swiss language
    * Simple Fix to ensure the right encoding when sending eMails.
 - FRESH-280 Period sorting in all Dropdowns where uses year-month numeric
    * Fix related to Order by of Calender and Periods in all relevant Dropdown Lists.
 - #105 FRESH-442 Annotated model interceptor
    * Annotated model interceptor with timing after delete and ifColumnChanged does not work correctly. Fixed.
 - FRESH-438 Make MRP Product Info Work
    * Removed stale data and added FK-constraints and improved logging to avoid Null Pointer Exception
 - FRESH-306 Customer alloc with Vendor Payment: Wrong Accounting
    * Adjusted the accounting of the Alocoation of Incoming and Outgoing Payments.

## Documentation
 - FRESH-323 metasfresh Developer Documentation
    * Added some clarifications and described how to import the initial DB-dump

# metasfresh 4.23.22a

## Features

## Fixes
 - FRESH-408 Picking Issue

# metasfresh 4.23.22

## Features
 - FRESH-280 Period sorting in all Dropdowns where uses year-month numeric
    * Changed sorting for all Dropdown entries about Periods (Month-Year) to have order-by year-month numeric desc

## Fixes
 - FRESH-412 quick input in orders not working
    * Fixed an issue in Quick order entry in Orders (sales and purchase) window
 - FRESH-409 Creating Partner Relation throws Exception
    * Fixing an exception that appeared when creating and saving a Business Partner relation
 - FRESH-407 M_ShipperTransportation Terminated after complete
    * Fixing an exception that popped up when completing a shipper transportation document
 - FRESH-339 Order Candidates BPartner Change does not effect Delivery Adress
    * Introduced additional callout to adjust the corresponding locations when choosing a different Business Partner in Order Candidates Window
 - FRESH-279 DD Order CU calculation wrong when TU = 1
    * Adjusted the Qty CU Calculation when TUQty is 1
 - FRESH-309 Missing ADR ASIs in purchase order lines since february
    * Restored attribute set instances that might have been missing in the past on some systems
 - FRESH-386 another error when sales order is automatically created as counter doc from a purchase order with packagings
    * Fixed error that appears in sales order counter document because of automatic Handling Unit generation in complete.
 - FRESH-388 Invoice Candidates not updated for some material receipts
    * Introducing a view to assist support troubleshooting

# metasfresh 4.22.21

## Features
 - FRESH-275 Search Field in Role _Access Windows with autocomplete
    * In Window Role, allow the user to search and autocomplete Windows, Processes, Forms and more, instead of using a dropdown list. This Functioanality allows a faster creation of Permission rules.
 - FRESH-349 KPI: Printing Performance
    * Adding a window to show per-shipment performance. This will help to understand if printing performance changes over time.
 - FRESH-350 check if purchase inout label and print preview can run faster
    * Improving Performance of material Receipt Labels in Print Preview and Printing.
 - FRESH-377 make invoice print preview faster
    * Improving Performance of Purchase- and Sales-Invoice Document in Print Preview and Printing.
 - FRESH-383 make orders print preview run faster
    * Improving Performance of Purchase- and Sales-Order-Document in Print Preview and Printing.

## Fixes
 - FRESH-400 Cut off in invoice jaspers
    * Header Label for UOM was cut off in Invoice Document. Fixed.
 - FRESH-344 Move KPI SQL to repository and new DB Schema
    * Fix: KPI SQLs were in the default/public schema
 - FRESH-356 make logo work for any org
 
## Documentation
 - Creation of HowTo's
    * You can now find a quickly growing Set of HowTo's in our metasfresh documentation Project. Check the details here : <a href="http://metasfresh.github.io/metasfresh-documentation/">http://metasfresh.github.io/metasfresh-documentation/</a>

# metasfresh 4.21.20

## Features
 - FRESH-349 KPI: Printing Performance
    * First step in creating queries for printing performance analysis. In near future these queries will be part of an administrator Dashboard and show average Printing performance for different documenttypes.
 - FRESH-344 Move KPI SQL to repository and new DB Schema
    * Moving all prepared KPI Queries to an own Database Schema called de_metas_fresh_kpi.
 - FRESH-347: Relation type between PMM_Purchase_Candidate and C_Order
    * Create an AD_relationType between PMM_PurchaseCandidate and C_Order.
 - FRESH-352 Colored Bar
    * Extending WindowHeaderNotice to also allow setting the notice's foreground and background color. Extending WindowHeaderNotice to also allow setting the notice's foreground and background color. Requirement to be able to create a different color Bar in metasfresh, so visually seperate Logins from different Organizations.
 - FRESH-342 Shipments not created
    * Made the shipment schedule enqueuer's doings more transparent to the user. Also added a house keeping tasks to reenqueue stale shipment schedules.

## Fixes
 - FRESH-374 duplicates asi in purchase inout print preview
    * Fixed a minor issue in Jasper Layout for meterial receipt document.
 - FRESH-363 Client metasfresh not getting results from server due to cxf bug
    * Workaround: Never log incoming payload with JMS transport until https://issues.apache.org/jira/browse/CXF-6930 is solved.
 - FRESH-358 Producer Invoice: Jasper Document shows Recapitulation for technical Tax
    * Fixed a wrong display of special Tax for Urproduzenten in Switzerland.
 - FRESH-360 EDI files occasianally still have wrong encoding
    * Fixed occasionally apperaring wrong encoding in EDI communication.
 - FRESH-356 make logo work for any org
    * Possibility to show the Logo on Jasper Documents. The Logo is taken from Organisation or Businesspartner joined to Org-ID.
 - FRESH-351 Error when sales order is automatically created as counter doc from a purchase order with packagings
    * Solving an issues which appeared in usage of counter documents, because of not matching packagings in each Organisation.
 - FRESH-348 purchase orders created with wrong IsTaxIncluded value
    * Ensuring that whenever the price list changes in an order, IsTaxInCluded, M_PricingSystem_ID and C_Currency are updated.

## Documentation
 - GROWTH-65 Community and Legal Files
    * Added LICENSE.md, CODE_OF_CONDUCT.md and modified The Contributing Guidelines.

# metasfresh 4.20.19

## Features
 - FRESH-254 Customer-Vendor Returns manual flag
    * Set the "manual" flag's default to Y in vendor and customer return windows allowing a more efficient recording.
 - FRESH-334 Product BPartner Contraint Issue
    * Prevent the user from accidentally creating C_BPartner_Product record whose AD_Org_ID makes no sense.
 - FRESH-326 Set the Correct Org in Fact_Acct_Summary
    * changed the migration script to be more repeatable 
 
## Fixes
 - FRESH-152 Extract statistics fields from C_BPartner and put them to a new table called C_BPartner_Stats
    * Fix to avoid multiple updates of same statistical value.
 - FRESH-343 Unwanted PInstance log shown after olCands were cleared for processing
    * Took out changelog for Orderline Candidates which were cleared for processing.
 - FRESH-314 Foreign BPartner reference included in sales order C_Order.C_BPartner_ID.
    * Some polishing around AD_ChangeLog creation.

# metasfresh 4.19.18

## Features
 - FRESH-335 create an initial contributor's guideline
    * Initial Setup of Contributing Guidelines. Further improving.
 - FRESH-278 Umsatzreport Geschäftspartner copy and modify details
    * Created an new Report for Turnover. Data of Report now depending on Delivered Quantities and value.

## Fixes
 - FRESH-338 Async not running
    * Fixed an additional problem with creating AD_ChangeLogs
 - FRESH-314 Foreign BPartner reference included in sales order C_Order.C_BPartner_ID
    * fixed a problem with creating AD_ChangeLogs
 - FRESH-311 Packvorschriften from different Org shown in Leergut
    * Make sure that the Empties Return Window only shows Packing Material that is defined in Logged in Organisation.
 - FRESH-333 Procurement candidate prices not updated correctly
    * Solved an Issue in Proce Calculation of Procurement Candidates when New Pricelist was created for already existing Procurement candidates.
 - FRESH-307 New Organisation: Financial Data of existing Org
    * Make sure that Financial Reports only show Data from selected Organisation.
 - FRESH-326 Set the Correct Org in Fact_Acct_Summary
    * Bug with AD_Org_ID not properly set in FACT_ACCT. Solved the Issue and Created Migration Script for Old Data.
 - FRESH-331 Double click needed for weighing machine and occasional NPE
    * Improved logging and making the application more robust
 - FRESH-329: periods missing in dropdown because of no translations
    * Fixed an Issue with Calendar Periods. These were  not shown because of missing Translations.
 - FRESH-327 Subsequent change of logo not working correctly without cache reset
    * Fixing an issue with Caching of Logo. Cache was not resetted after changing the Logo in running Client Session.
 - FRESH-312 Project cannot be compiled when downloading from github directly
    * Removed references to our internal maven repo, fixed a wrong groupid and provided a public keystore for development purposes.
 - FRESH-302 make inout print preview faster
    * Improved the Performance of Print Preview of In-Out Jasper Documents.

# metasfresh 4.18.17

## Features
 - FRESH-314 Foreign BPartner reference included in sales order C_Order.C_BPartner_ID
    * We could'nt reproduce this issue. We improved AD_ChangeLog to also log server-side changes (which have no session-id) and also store the AD_Pinstance_ID if available. If it happens again we will be will be able to trace it.
 - FRESH-320: Swing UI: License aggrement popup shall have an icon down in task bar
    * Adding an Icon in License agreement popup.
 - FRESH-278 Umsatzreport Geschäftspartner copy and modify
    * Adding a revenue report that is week-based and also based on the delivered quantities. The original revenue report still exits, but is based on invoiced quantities instead.
 - FRESH-305 Reduce Warehouse Dropdown List in Wareneingang (POS)
    * After selecting the source warehouse in Wareneingang POS, check all the Distribution Network Line entries that have this specific warehouse as source and provide the Target Warehouses for selection in Target Warehouse Dropdown List.
 - FRESH-312 Project cannot be compiled when downloading from github directly
    * Adding 3rd-party jars that are not available from the standard maven repos to a github hosted repo. Thanks a lot to @mckayERP and @e-Evolution for pointing us to the issue.
 - FRESH-228 Change jxls-poi version from 1.0.8 to 1.0.9 when it will be released
    * Updated to jxls-poi 1.0.9
 - FRESH-302 make inout print preview faster
    * Improved the InOut Print Preview Performance. Is now nearly 50% faster as before.
 - FRESH-298 Setup Printing Dunning Docs to separate tray for ESR Zahlschein
    * Adjusted the layout of Jasper Dunning Documents to better match pre-printed paper without having to use calibration.
 - FRESH-304 Report Konten-Information Rev+Exp accounts Saldovortrag year end
    * Switching the Report "Account -Information" to automatic Year-End initialization of Revenue and Expense Account balance.

## Fixes
 - FRESH-318 ESR String Processing not working with multiple partner bank accounts
    * C_PaySelectionLine: combining two methods into one, to avoid duplicate effort and FUD with their execution order. 
    * making sure that annotated model interceptor methods are ordered by their method name 
 - FRESH-251 Inout created from Picking-Parm shall only have picked Qty LU-TU too
    * The creation of InOuts shall consider the Picked Quantities of LU-TU via Picking Terminal, when Inout Creations is done from Inout-Candidate Window with Parm PickedQty = 'Y'.
 - FRESH-300 client not starting when config is not completed
    * Fixed a Bug that appeared in Client/ Org Setup when this initially was cancelled or not completed.
 - FRESH-152 Extract statistics fields from C_BPartner and put them to a new table called C_BPartner_Stats
    * Additional Fix.
 - FRESH-93 Purchase Order 848092, Row Missing in Invoice Candidates
    * Extending/ fixing the views to also find wrong Quantity ordered from Inout Lines referencing Invoice Candidates.
	* adding the ddl to our repository
	
# metasfresh 4.17.16

## Features
 - Fresh 271 Allow easy and riskless experimental builds
    * DevOps - we can now build the complete metasfresh distributable for an issue branch, without artifact GAV collisions
    * Some changes in the buildsystem that allow us to build and rollout feature branches before they were integrated.
 - FRESH-265 Procurement Candidates: Packvorschrift overwrite
    * Possibility to Overwrite the default Packing Instructions/ Handling Units for a reported Product Quantity. The Repor Informations come from Procurement Application.
 - FRESH-286 jenkins envInject plugin overwrites BUILD_URL value
    * DevOps - introducing a new environment variable ROLLOUT_BUILD_URL to be set by the caller. Fallback to BUILD_URL if the new var is not set.
 - FRESH-203 Procurement: Mail in BPartner language, other eMail Address	
    * CRM - Enhancing and extending mail configuration and functionality to select an eMail configuration by document type or base type.

## Fixes
 - FRESH-203 Procurement: Mail in BPartner language, other eMail Address	
	* Fixed the formatting and encoding problem in the mails sent by our async processor
	
# metasfresh 4.16.15

## Features
 - FRESH-259 Completely remove zkwebui from metasfresh
 - FRESH-261 Create simple process to change the hostname of a device configuration
    * adding a simple DB function to do the job for now

## Fixes
 - FRESH-267 aparently Loglevels are changed somewhere in the code
 - FRESH-270 material tracking: total received qty and scrap sometimes missing on invoice
     * fixed a problem where those two invoice detail records were attached to a not-displayed group
 - FRESH-234 report sales inout qtys for products and TUs are not alligned

# metasfresh 4.15.14

## Features
  - FRESH-245 filter columns in procurement for year as well
     * Possibility to hide also the columns filtered by year in Excel Reporting. Here especially done for Procurement contract report.
  - FRESH-152 Extract statistics fields from C_BPartner and put them to a new table called C_BPartner_Stats
     * Moving the Business Partner Status out of C_BPartner Data structure into C_BPartner_Stats. This was we avoid performance and blocking issues when updateing the Business Partner statistics.
  - FRESH-252 New Field datePromissed in Invoice Candidates Window
     * User Requirement for selecting Service Data to be invoiced via Invoice Candidates Window. Now the User has the possibility to also filter rows which are not triggered by an Inout Document.

## Fixes
  - FRESH-234 report sales inout qtys for products and TUs are not alligned
     * Minor Layout change for customer individual Inout Documents (Alignment of  identical Column headers on same Page).
  - FRESH-249 hubalance general report missing TU when no carry
     * Small fix in Handling Unit Balance Report.

  
# metasfresh 4.14.13a

## Features

  - FRESH-206 metasfresh server informs the procurement webui server about what supplies were synchronized
     * when data is received by metasfresh, it now asynchronously sends back a confirmation to the procurement UI. This way it is possible to monitor the procurement webUI for supply reports that were not yet received by the metasfresh system , e.g. due to internet problems.
  - FRESH-187 Filter date-from and date-to in procurement excel
     * also fixing a problem that the library-version we use only supports a hardcoded 50 columns
  - FRESH-218 Create archives in partner's language
     * adding language info to archive records, so when mailing the PDF, the system can choose a mail template in the correct language
	 
## Fixes
 - FRESH-235: User to Role assignment not working with some postgres versions
    * Fix of sql alias Issue. Recognized in User to Role Assignment.
 - FRESH-191 Procurement Excel: Although received no qties does not show
    * Fixed Issue. The quantities were not shown correctly in Procurement contract Excel Report.
 - FRESH-220 Autocomplete does not work if the underlying table has translated columns
    * Fixing the Issue with autocomplete not working whan the underlying table has translated columns.
 - FRESH-210 Org Name not updated after setting different name in Set Up Wizard
    * Using the SetupWizard, the Org name was not updated correctly.
 - FRESH-213 Process panel's Back button not working
    * Fixing an Issue with the back Button in Process Panel.
 - FRESH-222 QtyDelivered not updated for PMM_Balance
    * The PMM_Balance was not updateing QtyDelivered for contracted PMM_Products correctly. Fixed this Issue.
 
# metasfresh 4.14.13

## Features
 - FRESH-87 Log the JSON-Packages that are exchanged between the service endpoints
    * enabling the cfx LoggingFeature so we can log the data that comes in or goes out via jax-rs
 - FRESH-196 Procurement WebUI: Initially open with tomorrow
 - FRESH-197 Procurement WebUI: firefox's remember password does not popup
 - FRESH-176 Procurement WebUI: Allow switch enabled users between BPartners
 - FRESH-186 change gebindeubersicht to show all until selected date
 - ME-30 09951 Move code to github (108691256234)
	* switching from our internal mercurial server to actually work with and on github
 - FRESH-183 Price editable in Procurement Candidates
 - FRESH-97 Improve metas fresh server and client logging
 - FRESH-59 09915 procurement conditions jasper process (109344045046)
    * finetuning
 - FRESH-59 09915 procurement conditions jasper process (109344045046)FRESH-59 09915 procurement conditions jasper process (109344045046)
    * adding french translations
 - FRESH-172 Procurement: Double entries in PMM_Product: adding unique constraint after having cleaned up

## Fixes
 - FRESH-241 Get rid of com.verisign together with all those legacy payment processors
 - FRESH-215 Procurement WebUI language not updated when changed in bpartner
 - FRESH-219 Procurement: Contract missing in Procurement webUI: 
    * making queue subscriptions durable to make sure data is not lost if a subscriber is not present
 - FRESH-205 Doc Outbound: eMail sent not updated
    * the EMail-sent counter is now correct
 - FRESH-219 - Procurement: Contract missing in Procurement webUI
    * adding JMX operation to send all contracts to the webui
 - FRESH-216 Error creating InvoiceCandidates for PP_Orders without issued quantities
    * the system now deals more graciously with material tracking PP_Orders that have receipts but no issues
 - FRESH-194 Picking: Uncompatible CU-TU Issue
 - FRESH-179 Remove customer specifics from jasper and DB functions
 - FRESH-193 problems after temporary JMS link outage
 - FRESH-184 Saldobilanz report year switching saldo for R+E Accounts
 - FRESH-176: Allow switch enabled users between BPartners
 - FRESH-160 Fix Report "Gebindesaldo all"
 - FRESH-177 MRP Product Info Window with empty columns: removing obsolete DB-functions
 - FRESH-37 09955 excel Report Procurement Statistics (105226320154)

# metasfresh 4.13.12

## Features
 - FRESH-70 Model interceptor to prevent deactivating PMM_Products that are unter contract
    * and making sure that activating a record is still allowed
 - FRESH-170 Laufender Vertrag, show Lieferprodukt also on Tab "Liefersatz"
 - FRESH-168 Procurement: Support logon for vendors with no contract
 - FRESH-86 Procurement Show aggregation of reported Qty in MRP Product Info
    * adding a column for the (offered) vendor supply quantity to the MRP product info window
 - FRESH-134 Make syncing on the webUI side more robust
    * refactored how we import the data, make it fail only bpartner level, product level
 - FRESH-139 webui log: show user's ip address and email as part of the log line
 - FRESH-141 Procurement: Procurement Candidates initial qtyToOrder 0
 - FRESH-128 Provide Basic CRM Features
    * adding a simple table to the business partner window that displays related documents etc in cronological order
 - FRESH-71 - extend metasfresh and provide Product-TRLs to the webUI 
    * exposing a JMX operation to push all products and translations to the webUI

## Fixes
 - FRESH-171 fix qtyPlanned in procurements excel
 - FRESH-55 Add ASI-support to procurement contracts
    * fix: not all UOMs were eligible anymore
 - FRESH-167: Procurement: Provide Trend also when qty reported = 0
 - FRESH-164 Procurement: multiple Purchase Candidates after reporting qty for same day
 - FRESH-129 contact email in jaspers only after setting the user created as sales contact
 - FRESH-93 Purchase Order 848092, Row Missing in Invoice Candidates
    * adding an SQL based process that allows us to identify, fix and log occasional stale ICs
 - FRESH-126 Procurement WebUI: Qty 4 digits needed
 - FRESH-108 Fix Summary in C_Flatrate_Term_Create_ProcurementContract
 - FRESH-132 Assumption failure when trying to change the payment method in an invoice
 - FRESH-119 Setup wizard does not update field C BPartner Location.address
 - FRESH-125 Default User Record is on Wrong Org *
 - FRESH-30 09628 Procurement vendor-webUI purchasing and contract management
    * make sure that a PMM_Product can't be deactivated if it is part of a contract

----------------------------------------------------------------

# metasfresh 4.12.11

## Features
 - FRESH-82 Adapt PMM Balance reports to respect our new aggregations
 - FRESH-95 Modify the report C_Flatrate_Term_Create_ProcurementContract
    * now displaying the PMM_Product's Name which can also contain ASI-Information
 - FRESH-55 Add ASI-support to procurement contracts
 - FRESH-71 extend metasfresh and provide Product-TRLs to the webUI
 - FRESH-81 PMM purchase candidate: qty aggregation this/next week shall be on BP, product, ASI but NOT hupiitemproduct
 - FRESH-83 webui: another button called "Info" which displays a plain text which was configured in metasfresh

## Fixes
 - FRESH-78 In webui user is reporting in Qty CU and not Qty TU
 - FRESH-108 Fix Summary in C_Flatrate_Term_Create_ProcurementContract
 - FRESH-115 09933 InvoiceCandidates not created - Deadlock
    * had to revisit the task and fix some more

# metasfresh 4.11.10

## Features
 - FRESH-47	Don't create oracle migration scripts
    * removing another legacy piece of code
 - FRESH-37: 09955 excel Report Procurement Statistics (105226320154)
    * now using a high level excel templating engine (jxls)
 - 09961 MRP Product Info row selection persistency (109095061891)
    * when switching between days and filter settings, the selected row remains the same (if is is still there with the new filter settings)
 - when implementing a process, it is now possible to specifiy which record shall be selected after the process is executed
 - 09915 procurement conditions jasper process (109344045046)
    * now also supports contracts that have quantities, but no prices
 - 09628 procurement management (106716240958)
    * all changes in products, partners, contracts etc are now directly pushed to the mobile webUI
 - 09931 Bootify metasfresh (100627676679)
    * metasfresh now runs with spring-boot (server and client)
	* also replacing the JUL-based CLogger with slf4j

## Fixes
 - 09844 Login Problem: Client level Mandant and Org for System Administrator (102554242797)
 - 09915 procurement conditions jasper process (109344045046)
    * fix related to the contract start and end dates
 - usability fix in VNumber: first key press was lost
 - 09945 Pricelist for french customer - excel export not in customer language (107056752126)
 - 09944 Performance: Inout Reactivate-Complete taking too long (107539809839)
 - 09502 quality inspection (100925494419)
    * the final settlement invoice didn't have the correct witholding amount

## Instructions
 - When updateing an existing instalation, the script minor_remote.sh will stop. 
   Please read the instructions to update your instalation to the new JBoss free version.


# metasfresh 4.10.9 

## Features
 - 09915 procurement conditions jasper process (109344045046)
    * adding jasper, so that a procurement contract can be printed and singned by the vendor
 - 09924 Remove legacy oracle drivers and code (107845685842)
 - 09925 Create Balance Sheet Jasper (107146104064)
 
## Fixes
 - 09844 Login Problem: Client level Mandant and Org for System Administrator (102554242797)
 - 09939 Inconsistent ASI in InOutLine (109676742143)
 - 09922 null in tageslot sscc etikett (100681187457)
 - 09923 sepa export npe (106359694025)
	* if a given bank account info has no aasociated bank, we now give an informative error message
 - 09933 InvoiceCandidates not created - Deadlock
    * now we generally try processing a work package on deadlock.
 - Fixing NPE when invoicing failed *and* the user in charge had no C_BPartner

# metasfresh 4.9.8b

## Features
 - 09628 procurement management (106716240958)
    *  working on having metasfresh also talking to the webUI and communicate various changes instead of just responding to requests
 - FRESH-21: 09848 enable metasfresh to provide jax-rs services (101763395402)
    * when the client inititally starts and tries to connect the server, the timout is reduces from 1 minute to 2 seconds, to not stall the client and give quicker feedback if there is a problem with the server
	
## Fixes
 - 09628 procurement management (106716240958)
    * the first event was created with AD_Client_ID=0 and was therefore not processed

# metasfresh 4.9.8
  
## Features
 - 09920 Support sending invoice without an order via EDI: send the invoice's POReference if there is no cctop111 record coming from metasfresh
 - 09628 procurement management (106716240958)
	* the procurementUI communicates with metasfresh via jax-rs on a jms transport
	* support for the procurementUI to run an embedded broker and connect to metasfresh's broker over SSL
	* metasfresh contains a list with goods that are avaiable to be supplied by any vendor (without contract)
	* supply notes from the UI are sent to metasfresh and are shown to the metasfresh user as purchase order candidates which can directly be transformed into purchase orders
 - 09910 extend sql rollout tool to first create a new db from template (100074461801):
    * extending the tool so we can start by creating a new database from a templated-DB and then apply the scripts to that new database

## Fixes
 - 09912 AD EntityType ID is not acquired from project ID server (106063434593)

# metasfresh 4.8.7
  
## Features
 - 09628 procurement management (106716240958)
     * still a work in progress
	 * standalone procurement web application that is optimized for mobile and communicated with metasfresh
	 * procurement management in metasfresh
	    * manintain vendor contracts
		* create purchase orders for the supplies reproced via the web app
  - FRESH-21: 09848 enable metasfresh to provide jax-rs services (101763395402) 
    *Moved and extended the AD_JavaClasses framework
    *Removed javax.jnlp (it was only needed because there was some code wrt WebStart, 
     but that's not done anymore)
    *Removed javax.ejb (we now use jax.rs for the invokations)
     CConnection now also uses a proxy provided by jax-rs to query the application 
     server state
     The next step can be to change jboss for a less old & heavy environment,
     like tomcat or something else
    *Extracted de.metas.event's JMS coded into de.metas.jms, because it's now also
     used by de.metas.jax.rs
    *Added table AD_JAXRS_Enpoint to manage our endpoints
    *moving replication and metas-esb folgers into a new ad_menu folder called 
     "communication". Also moving the new AD_JAXRS_Enpoint windo to that folder
    *Changed startup-behavior so that when running in embedded-server-mode, the 
     client always starts a local jms broker and also connects to it
	 
## Fixes
 - 09643 Zahlung und Zahlung-Zuordnung Datum unterschiedlich (108395556223)
 - 09894 take email address on inout from contact and not properties (108765877478)
 - 09901 Report for packaging material balance: recap differences
 - 09890 Manual Fixing of voided Bankstatement after ESR Import (108847010077)
    * when a bankstatement is voided, all ESR import lines (if any) are unlinked from the bank statement

# metasfresh 4.7.6

## Features
 - FRESH-28: 09869 Enable subscription contracts (104949638829)
    * re-enabling a on-the-fly creation of subscription contracts with automated regular deliveries (via shipment schedule, of course).
	  We implemented this some years ago, but it was in use only briefly, so in this task, adapt and polish the old code.
 - 09859 Fix open items report - the whole query shall go in the reporting function (103228618621)
    * refactored the reports to be more maintainable and easier to support
 - 09766 VAT codes (107418136945)
    * new accounting report centered on VAT codes

## Fixes
 - 09862 Report Footer missing in inout jasper from Wareneingang POS
 - 09861 Error ESR Import with Partner that has Value bigger than 1000
 - when creating an accounting fact_acct for a C_BankStatementLine_Ref, we now set the fact_acct's C_BPartner_ID correctly
 - 09643 Zahlung und Zahlung-Zuordnung Datum unterschiedlich (108395556223)
    * when allocating an invoice with a payment, we now use the later date of the two documents in that allocation

# metasfresh 4.6.5

## Features
 - 09873 translate all of the metasfresh UI to english (105052594715)
    * translated more than 300 window, tab and field names that don't exist in adempiere and therefore weren't translated in task 09847

## Fixes
 - adding an index to C_InvoiceTax to solve a performance issue

# metasfresh 4.5.4

## Features
 - 09863 add comment to SQL generated by Jasper reports to identify (102778506388)
    * with this feature we can now see which running query belong to which jasper report
 - 09836 Show on HU-Receipt label if a HU was flagged for quality instpection (104096142493)
 - 09833 Show additional infos in print problem notification (105117588718)
    * extending the API to allow addtional context info to be added to a notification
 - FRESH-22: 09847 Import adempiere terminology as english trl into metasfresh (107370493155)
    * imported current en_US language translations from adempiere ERP
 - 09824 use locking API to prevent concurrent doc action on the same document (100066000843)

## Fixes
  - FRESH-22: 09847 Import adempiere terminology as english trl into metasfresh (107370493155):
     * fixed a bug in metasfresh that prevented to select the language by its name
      (just its value would have worked) as soon as "English" is available
 - 09776 EDI - Receiver without ORDERS (100584995833)
    * allow to distinguish between Edi-ORDERS and Excel files and allow both kinds to be processed in an EDI fashion
 - FRESH-20: 09661 Statistik für monatliche Lagermeldung - grouping per Product and ASI (106566269211)
    * fix for some quantities being counted multiple times
 - 09852 ESR-Import allow importing with invoice-partners that have AD Org ID 0 (109927070478):
	* when importing ESR-Data, we now accept C_BPartners with AD_Org_ID=0, because besides being a different number, it's not an inconsistency to have a partner with no org
  - 09823 Tax Code Migration (107275491843)
    * making a fix to the work-package processor that is used by the database function "de.metas.async".executeSqlAsync(p_Sql text)
 - 09812 create report for daily packaging material balance (101400050316)
    * minor layouting fixes
 - 09834 adjustment possibility for empties (105285329048)	
    * minor layouting fixes
	
## Instructions

# metasfresh 4.4.3

## Features
 - 09834 adjustment possibility for empties (105285329048)
    * introducing a new doctype to be used for packaging material (empties) adjustments
 - 09839 Customer Individual InOut (109838130709)
    * another alternative shipment document that among other things also lists product which are generally delivered to the customer, even if they are not part of  this particular shipment.
 - 09822 Inter-Org Product Mapping Process (104151535488)
    * allows a user to add and remove product mappings between different organisations
 - 09661 Report "Statistik für monatliche Lagermeldung" - grouping per product and selected attributes (106566269211)
 - 09837 Report Gebinde Übersicht change (103471986337)
 - 09740 ADR revenue report by product categories (101851459609) +it +feature
    * added french translations

## Fixes
 - 09812 create report for daily packaging material balance (101400050316)
    * minor layouting fixes
 - 09281 create report for packaging material balance (106483495857)
    * grouping/summing fixes

# metasfresh 4.3.2

## Features

 - 09826 Take out foot note in purchase order Jasper
    * it's now configurable via property file
 - 09827 Do not show ADR Keine/Leer on HU material receipt label
 - 09828 Salesgroups report add a filter for domestic/foreign country (101738461475)
 - 09832 Allow the printing client to retry on error (106406507107)
 - 09814 Send valid json to the printing client, also if there is an exception in the ESB (103239718792)
 - 09823 Tax Code Migration (107275491843)
    * implemented the database function "de.metas.async".executeSqlAsync(p_Sql text) as a tool to perform time-consuming SQL-migrations in the background
 - 09812 create report for daily packaging material balance (101400050316)
 - 09776 EDI - Receiver without ORDERS (100584995833)
    * we now can specify a default POReference value for orders that is autumatically set by the system, unless there already was another POReference set by a users of from an importnert order
	* for a DESADV lines with unknown CUperTU, we can now set a default-value such a "1" per C_BPartner.
	
## Fixes

 - 09829 ESB sends HTTP code 204 to the printing client (108552946334)
 - 09281 create report for packaging material balance (106483495857)
    * the former version also showed for a partner also packgings that were not on a particular partner's contract
 - 09820 Header notification bar not shown in main menu (100919535984)
 - 09831 Exception in the Initial Setup Wizard (108054071490)
 - In process parameter panel, display VImage preview, but bound to a maximum size
 - ClientSetup: company name changes were not persisted

## Instructions

 - to use the latest printing ESB bundle (tasks 09829 and 09814), one needs to install the jackson-jaxrs-json provider in the OSGI container (smx):
 ```
bundle:install mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-base/2.6.3 
bundle:install mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider/2.6.3
 ```
 
# metasfresh 4.2.1

 - 09281 create report for packaging material balance (106483495857) +it +feature
 - 09740 ADR revenue report by product categories (101851459609) +it +feature
 - 09819 Eliminate duplicated TaxBL and throw an informative exception if... (103899585460) +it +feature
    * removed a code redundancy and source of FUD
    * made it easier to understand why no C_Tax was found for a particular order line
 - 09799 Confusion around Picking Process (108712103881) +it +fix
    * fixing a problem that the picked quantity was sometimes not updated properly in the shipment schedule
 - 09700 Counter orders with mapped products (100691234288) +it +fix
    * fixing a problem with inconsistencies between the newly copied counter order line's old PLV and its new order's PL
 - 09811 EDI creating adjustment charge fails with NullPointerException (100746048824) +it +fix
 - 09797 Confirm completion of order with enter after popup (104892862993) +it +fix
 - 09802 Docaction after Prepared shall be Completed (100319295346) +it +fix
    * when createing purchase orders from sales orders, the drafted purchase orders' doc action is now "complete"
 - 09794 Metasfresh performance improvements (109453118224) +it +feature
    * speedup for the check whether the client still has a DB connection
	* not always loading each field's context menu items without them being needed
 - 09800 Scrolling in Picking terminal first window looks weird (106972300023) +it +fix
 - 09502 quality inspection (100925494419) +it +fix
    * recompute invoice candidates, also on PP_Order unclose
	* when computing the already-paid amount in the final settlement, don't take into account ordinary invoiced like packaging releated invoices
 - 09809 Report direct costing Year Title wrong (103628559355) +it +fix
 - 09803 Revenue report fix (109269170462) +it +fix
 - 09281 create report for packaging material balance (106483495857) +it +feature
 - 09779 Report C Activity ID swap changes (101962781663) +it +feature
 - 09783 Improve Salesgroups - migration and report (105684868719) +it +feature
 - 09801 Customer Individual Shipment change (104284980744) +it +feature
    * minor change to the alternative shipment jasper
 - 09804 Account-Information: don't show and sum Budget (106521617847) +it +fix
 - 09677 extending the BPartner changes report (107837562286) +it +feature
 - 09502_Support quality based invoicing +it +fix
    * directly refresh/recreate existing invoice candidates when a PP_Order is unclosed; don't wait for it to be closed again
 - 09700 Counter orders with mapped products (100691234288) +it +feature
    * allow counter orders (sames order in one org => purchase order in another) with org-internal products that are mapped against each other
 - 09773 Signature fix in shipment jasper (100363111538) +it +fix
 - 09782 remove note from purchase invoice (109638032503) +it +fix
 - 09788 Show isInfiniteCapacity in Pricelist (106197421720) +it +fix
 - 09785 F4-save does not work anymore in included invoice line tab +it +fix
 - 09780 Sorting of List Reference for PriceListVersion (105389853564) +it +feature
 - 09777 German Translations for Salesgroup (106405148729) +it +fix
    * small changes for Salesgroup Translations
 - 09625 Costing short report incl Budget (105806249331) +it +fix
 - 09710 Report regarding effective prices (107746499502) +uat +fix
    * layout/display fix rergarding the report's page number
 - 09766 VAT codes (107418136945) +uat +feature
    * allow defining different VAT codes for sales and purchase, and matching them with each other
 - 09739 gain and loss during bank transfers in foreign currencies (108136874441) +uat +feature
    * introducing default conversion type that can be specified to be active at a given time
	* currency gain and loss from bank account transfers are computed by comparing default conversion type with the default conversion rate
 - 09110 Make activity mandatory in accounting documents and allow the user to select one on demand (105477200774) +uat +feature
 - 09775 Import GL Journal Number Format Exception (104021981594): +it +feature
    * when importing values into a non-text filed that can't be parsed as number, date etc, then don't fail the whole import
 - minor, unrelated fix: when retrieving dunning levels, order them by +it +fix
    *"DaysAfterDue" to make sure they are dealt with in chronological order.
    *Actually, the order might not matter, but a counterintuitive ordering causes FUD.
 - 09771 Dunning docs mail (102929053917) +it +fix +feature
    * small changes around the dunning jasper

# it-S16_05-20160202
 - fixed the default location of the client properties file from <user.home>/.metas-fresh to <user.home>/.metasfresh +it +fix
 - 09741 Problems with HU labels for split HUs (104680331233) +uat +fix
 - 09765 Process to manually re-open C_PAySelection records that were already prepared (108508031142) +uat +feature
 - 09745 alternative jasper shipment document without ADR but explicit GMAA-values (107947997555) +uat +feature
 - 09726 Deep-copy support for AD Roles (106651676304) +uat +feature
    * we now also copy user-role assignements to the target role
 - 09625 Costing short report incl Budget (105806249331) +uat +feature
 - 09767 DBMoreThenOneRecordsFoundException when retrieving from picking slot queue (105944016827) +uat +fix
 - 09710 Report regarding effective prices (107746499502)
 - 09704 Migration ADempiere to metasfresh (100169279454) +it +feature
    * making hardcoded endcustomer-feature configurable for all metasfresh users
 - 09752 system creates two printing queue items for gernic reports (107420055849) +it +fix
 - 09764 servicemix update (102943200308): the esb bundles now use 
