package org.rtportfolio;

import com.google.common.collect.Multimap;
import org.rtportfolio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

/**
 * Based on the price update of an individual stock, do the following
 * 1. update stock position's market value
 * 2. trigger price calculation of its associated option and update the market value
 * 3. update the portfolio's NAV
 * 4. Publish portfolio's position & NAV
 * <p>
 * TODO: Implement heartbeat/could have used SBE to define the protocol
 */
public class PortfolioUpdater {
    private static Logger LOG = LoggerFactory.getLogger(RTPortfolioChecker.class);

    //    private Portfolio targetPortfolio;
    private Multimap<String, String> symbol2OptionMap;
    private Map<String, Position> symbol2PositionMap;
    private double portfolioNav = 0;
    private final PortfolioPublisher portfolioPublisher;
    private final ByteBuffer bb;
    private int PUB_MSG_SIZE;

    public PortfolioUpdater(final Map<String, Position> symbol2PositionMap, final Multimap<String, String> symbol2OptionMap) {
        this.symbol2PositionMap = symbol2PositionMap;
        this.symbol2OptionMap = symbol2OptionMap;
        this.portfolioPublisher = new PortfolioPublisher(8001);
        bb = ByteBuffer.allocateDirect(8192);
        PUB_MSG_SIZE = 4 + 4 + symbol2PositionMap.size() * 48 + 8;
    }

    public void updatePortfolio(final PriceUpdate priceUpdate) {
        double delta = 0L; //for NAV

        final String updatingStockSymbol = priceUpdate.getSymbol();
        final double newPx = priceUpdate.getPrice() * RTConst.MARKET_PRICE_SCALED_FACTOR;
        Position stockPos = symbol2PositionMap.get(updatingStockSymbol);
        double oldStockPx = stockPos.getSymbolCurrentValPerShare();
        stockPos.setSymbolCurrentValPerShareAndDependent(newPx);
        delta += (newPx - oldStockPx) * stockPos.getPositionSize();

        Collection<String> associatedOptionSymbols = symbol2OptionMap.get(updatingStockSymbol);
        LOG.info("updatingStockSymbol {} newPx {} ", updatingStockSymbol, newPx);
        for (String optionSymbol : associatedOptionSymbols) {
            Position optPos = symbol2PositionMap.get(optionSymbol);
            Instrument optIns = optPos.getInstrument();
            if (optIns == null) {
                //not in db
                LOG.error("Option details for {} could not found", optionSymbol);
                continue;
            }
            double newOptionPx = 0d;
            LOG.info("updatingStockSymbol {} newPx {} optIns.getMaturityDate() {}", updatingStockSymbol, newPx, optIns.getMaturityDate());
            double ttm = OptionPriceCalculator.getTimeToMaturity(optIns.getMaturityDate());
            if (optIns.getInstrumentType() == InstrumentType.CALL_OPTION) {
                newOptionPx = OptionPriceCalculator.calculateCallPrice(newPx, ttm, optIns.getStrike());
                LOG.info("optionSymbol {} optIns.getStrike() {} ttm {} newOptionPx() {}", optionSymbol, optIns.getStrike(), ttm, newOptionPx);
            } else if (optIns.getInstrumentType() == InstrumentType.PUT_OPTION) {
                newOptionPx = OptionPriceCalculator.calculatePutPrice(newPx, ttm, optIns.getStrike());
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
        bb.clear();
        bb.putInt(PUB_MSG_SIZE);
        bb.putInt(symbol2PositionMap.size()); //number of positions
        for (Map.Entry<String, Position> map : symbol2PositionMap.entrySet()) {
            String symbol = map.getKey();
            Position position = map.getValue();
            int len = symbol.length();
            bb.put(symbol.getBytes(), 0, len);
            for (int i = len; i < RTConst.MSG_POSITION_SYMBOL_SIZE; i++) {
                bb.put(RTConst.PAD);
            }
            bb.putDouble(position.getSymbolCurrentValPerShare());
            bb.putDouble(position.getPositionMarketValue());
            bb.putInt(position.getPositionSize());
            bb.put(symbol.equals(updatingStockSymbol) ? RTConst.IS_UPDATED_BYTE : RTConst.NOT_UPDATED_BYTE);
            bb.put(RTConst.THREE_PADS, 0, 3);
            LOG.info("symbol {} price {} qty {} market value {}", symbol, position.getSymbolCurrentValPerShare(), position.getPositionSize(), position.getPositionMarketValue());
        }
        LOG.info("Portfolio NAV: {}", portfolioNav);
        bb.putDouble(portfolioNav);
        portfolioPublisher.doSend(bb);
    }

}
