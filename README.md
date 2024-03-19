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

**TODO List**