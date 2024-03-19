**How to run the program**

* Run Main.java to create the db file first, it will generate sample.db for subsequent use
* Run RTPortfolioChecker, this acts as the publisher
* Run PortfolioSubscriber, this acts as the subscriber

**Assumption**

* Assume atomic market data update - each carries update of a single symbol
* For simplicity, assume RISK_FREE_RATE being 2% and IMPLIED_VOLATILITY being 10% for all calculation of option price

**Optimization**

* Pin receiver thread to a cpu
* ..........

**Communication Protocol**

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |   ML  |   NP  |R|           SYMBOL                            |
    +---------------+---------------+-------------------------------+
    |    Price    |     MAR VAL     |   QTY  | FLAG |   NAV         | 
    +---------------+---------------+-------------------------------+
    Message Length (ML) (4)
    Number of Positions (NP) (4)
    
    Symbol (24)
    Price (8)
    Market Value (MAR VAL) (8)
    Quantity (QTY) (4)
    FLAG (1) + 3 PADDING
    Portfolio NAV (8)
