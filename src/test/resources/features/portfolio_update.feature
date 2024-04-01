Feature: Portfolio Update Triggered By Market Data Update

  Scenario Outline: Check whether the portfolio subscriber receive the accurate data
    Given the following instrument available in the database
      | Symbol               | Strike | MaturityDate | Underlying |
      | APPL                 | #BLANK | #BLANK       | #BLANK     |
      | APPL-JUN-2024-1000-C | 230    | 20250630     | APPL       |
    Given the following positions in the portfolio
      | Symbol              | PositionSize |
      | APPL                | 2700         |
      | APPL-JUN-2025-230-C | 9000         |
    Then start the PortfolioUpdateWorker
    When suppose we receive the below price update
      | Symbol | <symbol> |
      | Price  | <price>  |
    Then portfolio subscriber should receive the below update
      | MessageSize       | <MSG_SIZE>       |
      | NumberOfPositions | <NUM_OF_POS>     |
      | UpdatedSymbol     | <UPDATED_SYMBOL> |
      | UpdatedPrice      | <UPDATED_PRICE>  |
      | NetAssetValue     | <NAV>            |
    Examples:
      | symbol | price  | MSG_SIZE | NUM_OF_POS | UPDATED_SYMBOL | UPDATED_PRICE | CURRENT_POSITION | NAV  |
      | APPL   | 170.32 | 336      | 6          | APPL           | 170.32        |                  | 5000 |
