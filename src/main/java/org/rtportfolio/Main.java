package org.rtportfolio;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //1. read CSV (expect one portfolio?)
        //2. load instruments from the db
        //3. Run the mock market data publisher
        //4. do not recalculate the whole portfolio NAV again, delta

        //a separate price publisher publishing price
        //main apps a separate thread receiving price --> pass it to the associate worker by hash --> int array to store price?
        //to update and calculate the associated option's price

        //MPSC queue pass it to the portfolio updater (atomic) publish the delta???
        //filter out not interested instrument in the portfolio (delta cal?)
        // NOTE: Connection and Statement are AutoClosable.
        //       Don't forget to close them both in order to avoid leaks.
        try
                (
                        // create a database connection
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
                        Statement statement = connection.createStatement();
                ) {
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists instruments");
            statement.executeUpdate("create table instruments (ID integer primary key, TICKER varchar(32) not null, INSTRUMENT_TYPE int not null, STRIKE real null, MATURITY_DATE varchar(8) null, UNDERLYING integer null, FOREIGN KEY (underlying) REFERENCES instruments(id))");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE) values('APPL', '1')");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE,STRIKE,MATURITY_DATE,UNDERLYING) values('APPL-OCT-2025-200-C','2','200','20251010', (select ID from instrumentS where TICKER = 'APPL'))");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE,STRIKE,MATURITY_DATE,UNDERLYING) values('APPL-OCT-2024-160-P','3','160','20241010', (select ID from instrumentS where TICKER = 'APPL'))");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE) values('MSFT', '1')");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE,STRIKE,MATURITY_DATE,UNDERLYING) values('MSFT-NOV-2025-420-C','2','420','20251110', (select ID from instrumentS where TICKER = 'MSFT'))");
            statement.executeUpdate("insert into instruments (TICKER,INSTRUMENT_TYPE,STRIKE,MATURITY_DATE,UNDERLYING) values('MSFT-DEC-2025-350-P','3','350','20251217', (select ID from instrumentS where TICKER = 'MSFT'))");
            statement.executeUpdate("drop table if exists priceHistory");
            statement.executeUpdate("create table priceHistory (TICKER varchar(32) not null, HST_CLOSE real not null)");
            statement.executeUpdate("insert into priceHistory (TICKER,HST_CLOSE) values('APPL', '177.30')");
            statement.executeUpdate("insert into priceHistory (TICKER,HST_CLOSE) values('MSFT', '418.53')");
            ResultSet rs = statement.executeQuery("select * from instruments");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("ticker"));
                System.out.println("id = " + rs.getInt("id"));
                System.out.println("id = " + rs.getInt("underlying"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            e.printStackTrace(System.err);
        }
    }
//***Design a simple hash function to reduce memory usage 

//        Random random = new Random();
//        double openPrice = 10;
//        double expectedReturn = 0.68;
//        double aStandardDev = 1;
//        int numberOfS = 1;
//        while(true) {
//            double rv = random.nextGaussian();
//            System.out.println("nextGuassian: "+ rv);
//            openPrice = openPrice + openPrice * (expectedReturn * numberOfS / 7257600) + (aStandardDev*rv*Math.sqrt(numberOfS/7257600));
//            System.out.println("openPrice: "+ openPrice);
//            Thread.sleep(1000);
//            numberOfS++;
//        }
}
