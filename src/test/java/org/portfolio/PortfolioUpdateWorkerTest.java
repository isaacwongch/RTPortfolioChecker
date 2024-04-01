package org.portfolio;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.rtportfolio.PortfolioPublisher;
import org.rtportfolio.PortfolioUpdateWorker;
import org.rtportfolio.ds.RTObjectPool;
import org.rtportfolio.ds.SPSCQueue;
import org.rtportfolio.model.Instrument;
import org.rtportfolio.model.InstrumentType;
import org.rtportfolio.model.Position;
import org.rtportfolio.model.PriceUpdate;
import org.rtportfolio.util.PriceUpdateObjectCreator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PortfolioUpdateWorkerTest {
    private final ArgumentCaptor<Integer> msgSizeCaptor = ArgumentCaptor.forClass(Integer.class);
    private final ArgumentCaptor<String> updatedSymbolCaptor = ArgumentCaptor.forClass(String.class);
    private final ArgumentCaptor<Double> updatedPriceCaptor = ArgumentCaptor.forClass(Double.class);
    private final ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
    private final ArgumentCaptor<Double> navCaptor = ArgumentCaptor.forClass(Double.class);
    private static final double EPSILON = 10E-4;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    public void testPortfolioUpdate() {
        //Prepare position
        int nvdaStockPos = 100;
        int nvdaCallPos = 10000;
        Instrument nvda = new Instrument("NVDA", InstrumentType.STOCK, 0L, null);
        Position p1 = new Position(nvda, nvdaStockPos);
        //make sure the test will be good
        LocalDate oneYearLater = LocalDate.now().plusDays(365);
        Instrument nvda_call = new Instrument("NVDA-JUN-2024-1000-C", InstrumentType.CALL_OPTION, 1000L, oneYearLater.format(formatter));
        Position p2 = new Position(nvda_call, nvdaCallPos);

        final Map<String, Position> symbol2PositionMap = new HashMap<>();
        symbol2PositionMap.put("NVDA", p1);
        symbol2PositionMap.put("NVDA-JUN-2024-1000-C", p2);
        final Multimap<String, String> stock2OptionMap = ArrayListMultimap.create();
        stock2OptionMap.put("NVDA", "NVDA-JUN-2024-1000-C");
        PortfolioPublisher mockPortfolioPublisher = Mockito.mock(PortfolioPublisher.class);
        RTObjectPool<PriceUpdate> rtObjectPool = new RTObjectPool<>(10, 100, new PriceUpdateObjectCreator());
        SPSCQueue<PriceUpdate> spscQueue = new SPSCQueue<>(100);
        PortfolioUpdateWorker portfolioUpdateWorker = new PortfolioUpdateWorker(spscQueue, rtObjectPool, symbol2PositionMap, stock2OptionMap, mockPortfolioPublisher);

        double updatedStockPrice = 900L;
        portfolioUpdateWorker.updatePortfolio("NVDA", updatedStockPrice);
        Mockito.verify(mockPortfolioPublisher).publishLatestPortfolio(msgSizeCaptor.capture(), updatedSymbolCaptor.capture(), updatedPriceCaptor.capture(), Mockito.anyMap(), navCaptor.capture());
        assertEquals(112, (int) msgSizeCaptor.getValue());
        assertEquals("NVDA", updatedSymbolCaptor.getValue());
        assertEquals(updatedStockPrice, updatedPriceCaptor.getValue(), EPSILON);
        double expectedNav = nvdaStockPos * updatedStockPrice + nvdaCallPos * 10.25453734d;
        assertEquals(expectedNav, navCaptor.getValue(), EPSILON);
    }


}
