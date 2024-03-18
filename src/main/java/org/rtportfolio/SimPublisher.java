package org.rtportfolio;

import org.rtportfolio.ds.RTObjectPool;
import org.rtportfolio.ds.SPSCQueue;
import org.rtportfolio.model.PriceUpdate;
import org.rtportfolio.model.SymbolPub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/**
 * To simulate stock movement using discrete time geometric Brownian motion
 * Assume this is the receiver thread
 */
public class SimPublisher {
    private static Logger LOG = LoggerFactory.getLogger(SimPublisher.class);    //implied decimal places
    private String[] symbols = new String[]{"MSFT", "APPL", "TSLA"}; //assume we have 3 symbols only
    private SymbolPub[] symbolPubs = new SymbolPub[3];

    private SPSCQueue<PriceUpdate> spscQueue;
    private RTObjectPool<PriceUpdate> objectPool;
    private Map<String, Double> symbol2HistoricalClose;

    public SimPublisher(final Map<String, Double> symbol2HistoricalClose, final SPSCQueue<PriceUpdate> spscQueue, final RTObjectPool<PriceUpdate> objectPool) {
        this.objectPool = objectPool;
        this.spscQueue = spscQueue;
        this.symbol2HistoricalClose = symbol2HistoricalClose;
        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];
            SymbolPub symbolPub = new SymbolPub();
            symbolPub.setSymbol(symbol);
            symbolPub.setExpectedReturn(Math.random());
            symbolPub.setStandardVar(Math.random());
            if (symbol2HistoricalClose.containsKey(symbol)){
                symbolPub.setCurrentPx(symbol2HistoricalClose.get(symbol));
            }else {
                symbolPub.setCurrentPx(getRandomNumber(1, 100));
            }
            symbolPubs[i] = symbolPub;
            LOG.info(symbolPubs[0].toString());
        }
    }

    public void startPublishingMarketPxThread() {
        Thread publishThread = new Thread(() -> {
            Random random = new Random();
            int elapsedMillis = 0;
            while (true) {
                int toElapseMillis = (int) getRandomNumber(1000, 2500);
                try {
                    Thread.sleep(toElapseMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                elapsedMillis += toElapseMillis;
                for (SymbolPub symbolPub : symbolPubs) {
                    double rv = random.nextGaussian();
                    System.out.println("nextGuassian: " + rv);
                    double oldPx = symbolPub.getCurrentPx();
                    double elapsedMillisDb = elapsedMillis / 1000;
                    double newPx = oldPx + oldPx * (symbolPub.getExpectedReturn() * elapsedMillisDb / 7257600) + (symbolPub.getStandardVar() * rv * Math.sqrt(elapsedMillisDb / 7257600));
                    symbolPub.setCurrentPx(newPx);
                    PriceUpdate pu = this.objectPool.get();
                    pu.setSymbol(symbolPub.getSymbol());
                    System.out.println("Symbol: " + pu.getSymbol());
                    System.out.println("Price: " + newPx);
                    pu.setPrice((long) (newPx * 10000)); //imply 4 decimal places
                    this.spscQueue.offer(pu);
                    LOG.info("Symbol {} oldpx {} newpx {}", symbolPub.getSymbol(), oldPx, newPx);
                }
            }
        });
        publishThread.start();
    }

    public double getRandomNumber(int min, int max) {
        return (Math.random() * (max - min)) + min;
    }

//    public static void main(String[] args) {
//        SimPublisher n = new SimPublisher();
//        n.startPublishingMarketPx();
//    }
}
