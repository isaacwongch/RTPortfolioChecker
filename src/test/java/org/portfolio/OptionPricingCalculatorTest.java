package org.portfolio;

import org.rtportfolio.OptionPriceCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Referencing price from https://www.sup.org/ef1/Excel%20Templates/Black-Scholes%20Template.xls
 */

public class OptionPricingCalculatorTest {
    private static final double EPSILON = 10E-6;

    /**
     * Stock price is $40, expiration is 6 months from today with strike being 40. Risk-free rate is 10% per annum, and volaility is 20% per annum.
     */
    @Test
    public void testCallPutPrice1() {
        double callPrice = OptionPriceCalculator.calculateCallPrice(42, 40, 0.5, 0.1, 0.2);
        assertEquals(callPrice, 4.75942239, EPSILON);
        double putPrice = OptionPriceCalculator.calculatePutPrice(42, 40, 0.5, 0.1, 0.2);
        assertEquals(putPrice, 0.80859937, EPSILON);
    }

    /**
     * Stock price is $40, expiration is 6 months from today with strike being 40. Risk-free rate is 10% per annum, and volaility is 20% per annum.
     */
    @Test
    public void testCallPutPrice2() {
        double callPrice = OptionPriceCalculator.calculateCallPrice(50, 50, 5, 0.0366, 0.62);
        assertEquals(callPrice, 27.80402272, EPSILON);
        double putPrice = OptionPriceCalculator.calculatePutPrice(50, 50, 5, 0.0366, 0.62);
        assertEquals(putPrice, 19.44243051, EPSILON);
    }
}
