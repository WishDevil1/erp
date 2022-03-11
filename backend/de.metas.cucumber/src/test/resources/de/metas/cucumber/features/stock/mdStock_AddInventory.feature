@from:cucumber
@topic:stock
Feature: stock changes accordingly
  As a user
  I want stock to be updated properly if inventory is changed
  So that the QTY is always correct

  Background:
    Given the existing user with login 'metasfresh' receives a random a API token for the existing role with name 'WebUI'
    And metasfresh initially has no MD_Stock data
    And no product with value 'product_value222' exists
    And metasfresh contains M_Product with M_Product_ID '222'

  Scenario: Changes stock by adding inventory
    And metasfresh contains M_Inventories:
      | Identifier | M_Warehouse_ID | MovementDate | OPT.DocumentNo |
      | 11         | 540008         | 2021-07-12   | 1111           |
      | 12         | 540008         | 2021-07-12   | 2222           |
    And metasfresh contains M_InventoriesLines:
      | Identifier | M_Inventory_ID.Identifier | M_Product_ID.Identifier | UOM.X12DE355 | QtyCount | QtyBooked |
      | 21         | 11                        | 222                     | PCE          | 10       | 0         |
      | 22         | 12                        | 222                     | PCE          | 16       | 10        |
    When the inventory identified by 11 is completed
    Then metasfresh has MD_Stock data
      | M_Product_ID.Identifier | QtyOnHand |
      | 222                     | 10        |

    And the inventory identified by 12 is completed
    And metasfresh has MD_Stock data
      | M_Product_ID.Identifier | QtyOnHand |
      | 222                     | 16        |
