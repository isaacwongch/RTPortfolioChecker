package org.rtportfolio;

import com.google.common.collect.Multimap;
import org.rtportfolio.ds.RTObjectPool;
import org.rtportfolio.ds.SPSCQueue;
import org.rtportfolio.model.Instrument;
import org.rtportfolio.model.InstrumentType;
import org.rtportfolio.model.Position;
import org.rtportfolio.model.PriceUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Based on the price update of an individual stock, do the following
 * 1. update stock position's market value
 * 2. trigger price calculation of its associated option and update the market value
 * 3. update the portfolio's NAV
 * 4. Publish portfolio's position & NAV
 * <p>
 */
public final class PortfolioUpdateWorker {
    private static final double RISK_FREE_RATE = 0.02; //2%
    private static final double IMPLIED_VOLATILITY = 0.1; //for simplicity assume same for all
    private static final Logger LOG = LoggerFactory.getLogger(RTPortfolioChecker.class);

    //    private Portfolio targetPortfolio;
    private SPSCQueue<PriceUpdate> spscQueue;
    private final Multimap<String, String> symbol2OptionMap;
    private final Map<String, Position> symbol2PositionMap;
    private double portfolioNav = 0;
    private final PortfolioPublisher portfolioPublisher;
    private final Set<String> interestedSymbols;
    private final RTObjectPool<PriceUpdate> objectPool;
    private final int PUB_MSG_SIZE;

    public PortfolioUpdateWorker(final SPSCQueue<PriceUpdate> spscQueue, final RTObjectPool<PriceUpdate> priceUpdateRTObjectPool, final Map<String, Position> symbol2PositionMap, final Multimap<String, String> symbol2OptionMap, final PortfolioPublisher portfolioPublisher) {
        this.spscQueue = spscQueue;
        this.symbol2PositionMap = symbol2PositionMap;
        this.interestedSymbols = symbol2PositionMap.keySet();
        this.symbol2OptionMap = symbol2OptionMap;
        this.portfolioPublisher = portfolioPublisher;
        this.objectPool = priceUpdateRTObjectPool;
        PUB_MSG_SIZE = Integer.BYTES + Integer.BYTES + symbol2PositionMap.size() * RTConst.REPEATED_POSITION_COMPONENT_SIZE + Long.BYTES;
    }

    public void start(){
        try {
            //pin thread to cpu programmatically
            Thread t = new Thread(() -> {
                doWork();
            });
            t.start();
        } catch (Exception ex){
            LOG.error("Failure to start PortfolioUpdaterWorker");
        }
    }

    private void doWork() {
        while (true) {
            final PriceUpdate pu = spscQueue.poll();
            try {
                if (pu != null) {
                    String symbol = pu.getSymbol();
                    //ignore other symbols not in the portfolio
                    if (interestedSymbols.contains(symbol)) {
                        double scaledPrice = pu.getPrice() * RTConst.MARKET_PRICE_SCALED_FACTOR;
                        updatePortfolio(symbol, scaledPrice);
                        objectPool.free(pu);
                    }
                }
            } catch (Exception ex) {
                LOG.error("Exception when processing price update message - PriceUpdate[{},{}]", pu.getSymbol(), pu.getPrice(), ex);
            }
        }
    }

    public void updatePortfolio(final String updatingStockSymbol, final double updatedStockPrice) {
        double delta = 0L; //for NAV

        Position stockPos = symbol2PositionMap.get(updatingStockSymbol);
        double oldStockPx = stockPos.getSymbolCurrentValPerShare();
        stockPos.setSymbolCurrentValPerShareAndDependent(updatedStockPrice);
        delta += (updatedStockPrice - oldStockPx) * stockPos.getPositionSize();

        Collection<String> associatedOptionSymbols = symbol2OptionMap.get(updatingStockSymbol);
        LOG.info("updatingStockSymbol {} newPx {} ", updatingStockSymbol, updatedStockPrice);
        for (String optionSymbol : associatedOptionSymbols) {
            Position optPos = symbol2PositionMap.get(optionSymbol);
            Instrument optIns = optPos.getInstrument();
            if (optIns == null) {
                //not in db
                LOG.error("Option details for {} could not found", optionSymbol);
                continue;
            }
            double newOptionPx;
            LOG.info("updatingStockSymbol {} newPx {} optIns.getMaturityDate() {}", updatingStockSymbol, updatedStockPrice, optIns.getMaturityDate());
            double ttm = OptionPriceCalculator.getTimeToMaturity(optIns.getMaturityDate());
            if (optIns.getInstrumentType() == InstrumentType.CALL_OPTION) {
                newOptionPx = OptionPriceCalculator.calculateCallPrice(updatedStockPrice, optIns.getStrike(), ttm, RISK_FREE_RATE, IMPLIED_VOLATILITY);
                LOG.info("optionSymbol {} optIns.getStrike() {} ttm {} newOptionPx() {}", optionSymbol, optIns.getStrike(), ttm, newOptionPx);
            } else if (optIns.getInstrumentType() == InstrumentType.PUT_OPTION) {
                newOptionPx = OptionPriceCalculator.calculatePutPrice(updatedStockPrice, optIns.getStrike(), ttm, RISK_FREE_RATE, IMPLIED_VOLATILITY);
                LOG.info("optionSymbol {} optIns.getStrike() {} ttm {} newOptionPx() {}", optionSymbol, optIns.getStrike(), ttm, newOptionPx);
            } else {
                LOG.error("Unexpected instrument type, ignoring optionSymbol {}", optionSymbol);
                continue;
            }
            double oldOptPx = optPos.getSymbolCurrentValPerShare();
            optPos.setSymbolCurrentValPerShareAndDependent(newOptionPx);
            delta += (newOptionPx - oldOptPx) * optPos.getPositionSize();
        }
        portfolioNav += delta;
        //publish
        //TODO: add ring buffer here
        portfolioPublisher.publishLatestPortfolio(PUB_MSG_SIZE, updatingStockSymbol, updatedStockPrice, symbol2PositionMap, portfolioNav);
    }

//    private void putSymbol(final String symbol) {
//        int len = symbol.length();
//        bb.put(symbol.getBytes(), 0, len);
//        for (int i = len; i < RTConst.MSG_SYMBOL_SIZE; i++) {
//            bb.put(RTConst.PAD);
//        }
//    }
//
//    private void publishLatestPortfolio(final int messageSize, final String updatedSymbol, final double updatedPrice, final Map<String, Position> symbol2PosMap, final double portfolioNav) {
//        bb.clear();
//        bb.putInt(messageSize);
//        bb.putInt(symbol2PosMap.size()); //number of positions
//        putSymbol(updatedSymbol);
//        bb.putDouble(updatedPrice);
//        for (Map.Entry<String, Position> map : symbol2PosMap.entrySet()) {
//            String symbol = map.getKey();
//            Position position = map.getValue();
//            putSymbol(symbol);
//            bb.putDouble(position.getSymbolCurrentValPerShare());
//            bb.putDouble(position.getPositionMarketValue());
//            bb.putInt(position.getPositionSize());
//            LOG.info("symbol {} price {} qty {} market value {}", symbol, position.getSymbolCurrentValPerShare(), position.getPositionSize(), position.getPositionMarketValue());
//        }
//        LOG.info("Portfolio NAV: {}", portfolioNav);
//        bb.putDouble(portfolioNav);
//        portfolioPublisher.doSend(bb);
//    }

}
