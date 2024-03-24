**How to run the program**

* Run DatabaseGenerator.java to create the db file first, it will generate sample.db for subsequent use
* Run RTPortfolioChecker, this acts as the publisher
* Run PortfolioSubscriber, this acts as the subscriber

**Assumption**

* Assume atomic market data update - each carries update of a single symbol
* For simplicity, assume RISK_FREE_RATE being 2% and IMPLIED_VOLATILITY being 10% for all calculation of option price

**Communication Protocol**

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |   ML  |   NP  |             UPDATED SYMBOL                    |
    +---------------+---------------+-------------------------------+
    | UPDATED PRICE ||R                SYMBOL                       | 
    +---------------+---------------+-------------------------------+
    |    PRICE      |    MAR VAL    |   QTY  ||        NAV          |
    +---------------+---------------+-------------------------------+
    Message Length (ML) (4)
    Number of Positions (NP) (4)
    
    Updated Symbol (24)
    Updated Price (8)

    //Repeated
    Symbol (24)
    Price (8)
    Market Value (MAR VAL) (8)
    Quantity (QTY) (4)

    Portfolio NAV (NAV) (8)
