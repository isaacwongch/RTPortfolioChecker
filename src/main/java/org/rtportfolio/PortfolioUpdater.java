package org.rtportfolio;

import com.google.common.collect.Multimap;
import org.rtportfolio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Based on the price update of an individual stock, do the following
 * 1. update stock position's market value
 * 2. trigger price calculation of its associated option and update the market value
 * 3. update the portfolio's NAV
 * 4. Publish portfolio's position & NAV
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
        bb = ByteBuffer.allocateDirect(8096);
        PUB_MSG_SIZE = 4 + 4 + symbol2PositionMap.size() * 48 + 8;
    }

    public void updatePortfolio(final PriceUpdate priceUpdate) {
        double delta = 0L; //for NAV

        final String stockSymbol = priceUpdate.getSymbol();
        final double newPx = priceUpdate.getPrice() * RTConst.MARKET_PRICE_SCALED_FACTOR;
        Position stockPos = symbol2PositionMap.get(stockSymbol);
        double oldStockPx = stockPos.getSymbolCurrentValPerShare();
        stockPos.setSymbolCurrentValPerShareAndDependent(newPx);
        delta += (newPx - oldStockPx) * stockPos.getPositionSize();

        Collection<String> associatedOptionSymbols = symbol2OptionMap.get(stockSymbol);
        LOG.info("stockSymbol {} newPx {} ", stockSymbol, newPx);
        for (String optionSymbol : associatedOptionSymbols) {
            Position optPos = symbol2PositionMap.get(optionSymbol);
            Instrument optIns = optPos.getInstrument();
            double newOptionPx = 0d;
            LOG.info("stockSymbol {} newPx {} optIns.getMaturityDate() {}", stockSymbol, newPx, optIns.getMaturityDate());
            double ttm = OptionPriceCalculator.getTimeToMaturity(optIns.getMaturityDate());
            if (optIns.getInstrumentType() == InstrumentType.CALL_OPTION) {
                newOptionPx = OptionPriceCalculator.calculateCallPrice(newPx, ttm, optIns.getStrike());
                LOG.info("optionSymbol {} optIns.getStrike() {} ttm {} newOptionPx() {}", optionSymbol, optIns.getStrike(), ttm, newOptionPx);
            } else if (optIns.getInstrumentType() == InstrumentType.PUT_OPTION) {
                newOptionPx = OptionPriceCalculator.calculatePutPrice(newPx, ttm, optIns.getStrike());
                LOG.info("optionSymbol {} optIns.getStrike() {} ttm {} newOptionPx() {}", optionSymbol, optIns.getStrike(), ttm, newOptionPx);
            } else {
                //??
            }
            double oldOptPx = optPos.getSymbolCurrentValPerShare();
            optPos.setSymbolCurrentValPerShareAndDependent(newOptionPx);
            delta += (newOptionPx - oldOptPx) * optPos.getPositionSize();
        }
        portfolioNav += delta;
        //publish
        bb.clear();
        bb.putInt(PUB_MSG_SIZE);
        portfolioPublisher.doSend(bb);
    }

}
