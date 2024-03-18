package org.rtportfolio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.rtportfolio.ds.RTObjectPool;
import org.rtportfolio.ds.SPSCQueue;
import org.rtportfolio.model.*;
import org.rtportfolio.util.PriceUpdateObjectCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class RTPortfolioChecker {
    private static Logger LOG = LoggerFactory.getLogger(RTPortfolioChecker.class);

    public static void main(String[] args) {
        Map<String, Instrument> symbol2InstrumentMap = new HashMap<>();
        Map<String, Position> symbol2PositionMap = new HashMap<>();
        Multimap<String, String> symbol2OptionSymbolsMap = ArrayListMultimap.create();
        Map<String, Double> symbol2HistoricalCloseMap = new HashMap<>();
        Set<String> interestedSymbols = new HashSet<>();
        //2. load instruments from the db
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from instruments");
            String[] id2SymbolArray = new String[200]; //TODO
            while (rs.next()) {
                int id = rs.getInt(RTConst.ID);
                String symbol = rs.getString(RTConst.TICKER);
                id2SymbolArray[id] = symbol;
                int typeInt = rs.getInt(RTConst.INSTRUMENT_TYPE);
                InstrumentType type = InstrumentType.getById(typeInt);
                if (type != InstrumentType.STOCK) {
                    long strike = rs.getLong(RTConst.STRIKE);
                    String maturityDate = rs.getString(RTConst.MATURITY_DATE);
                    int underlying = rs.getInt(RTConst.UNDERLYING);
                    if (underlying != 0) {
                        String underlyingSymbol = id2SymbolArray[underlying];
                        symbol2OptionSymbolsMap.put(underlyingSymbol, symbol);
                    }
                    symbol2InstrumentMap.put(symbol, new Instrument(symbol, type, strike, maturityDate));
                } else {
                    symbol2InstrumentMap.put(symbol, new Instrument(symbol, type, 0, null));
                }
            }
            rs = statement.executeQuery("select * from priceHistory");
            while (rs.next()) {
                String symbol = rs.getString(RTConst.TICKER);
                double historicalClose = rs.getDouble(RTConst.HST_CLOSE);
                symbol2HistoricalCloseMap.put(symbol, historicalClose);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }

        //1. read CSV (expect one portfolio?)
        try {
            List<String> portfolioStr = Files.readAllLines(Paths.get(RTPortfolioChecker.class.getClassLoader().getResource("portfolio.csv").toURI()));
            List<Position> positions = new ArrayList<>(portfolioStr.size());
            for (String itemStr : portfolioStr) {
                String[] symbolPos = itemStr.split(RTConst.DELIMITER);
                if (symbolPos.length == 2) {
                    String symbol = symbolPos[0];
                    int pos = Integer.valueOf(symbolPos[1]);
                    Instrument instrument = symbol2InstrumentMap.get(symbol);
                    Position position = new Position(instrument, pos);
                    positions.add(position);
                    symbol2PositionMap.put(symbol, position);
                    interestedSymbols.add(symbol);
                }
            }
//            portfolio = new Portfolio(positions);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.gc(); //init complete
        //init simulated receiver thread
        RTObjectPool<PriceUpdate> rtObjectPool = new RTObjectPool<>(10, 100, new PriceUpdateObjectCreator());
        SPSCQueue<PriceUpdate> spscQueue = new SPSCQueue<>(100);
        SimPublisher simPublisher = new SimPublisher(symbol2HistoricalCloseMap, spscQueue, rtObjectPool);
        simPublisher.startPublishingMarketPxThread();
        PortfolioUpdater portfolioUpdater = new PortfolioUpdater(symbol2PositionMap, symbol2OptionSymbolsMap);
        while (true) {
            final PriceUpdate pu = spscQueue.poll();
            try {
                if (pu != null) {
                    String symbol = pu.getSymbol();
                    //ignore other symbols not in the portfolio
                    if (interestedSymbols.contains(symbol)) {
                        double price = pu.getPrice() * RTConst.MARKET_PRICE_SCALED_FACTOR;
                        System.out.println("Symbol: " + pu.getSymbol());
                        System.out.println("Price: " + price);
                        portfolioUpdater.updatePortfolio(pu);
                        rtObjectPool.free(pu);
                    }
                }
            } catch (Exception ex) {
                LOG.error("Exception when processing price update message", ex);
            }

        }
    }
}
