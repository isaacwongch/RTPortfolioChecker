package org.rtportfolio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class OptionPriceCalculator {
    private static final double RISK_FREE_RATE = 0.02; //2%
    private static final double IMPLIED_VOLATILITY = 0.1; //for simplicity assume same for all
    private static final LocalDate now = LocalDate.now();
    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static double getTimeToMaturity(final String timeToMaturityDate) {
        //TODO Garbage
        LocalDate ldt = LocalDate.parse(timeToMaturityDate, f);
        return ChronoUnit.DAYS.between(now, ldt) / 365d;
    }

    public static double calculateCallPrice(final double stockPrice, final double timeToMaturity, final double strike) {
        double d1Numerator = Math.log(stockPrice / strike) + (RISK_FREE_RATE + IMPLIED_VOLATILITY * IMPLIED_VOLATILITY / 2) * timeToMaturity;
        double d1Denominator = IMPLIED_VOLATILITY * Math.sqrt(timeToMaturity);
        double d1 = d1Numerator / d1Denominator;
        double d2 = d1 - d1Denominator;

        return stockPrice * cdf(d1) - strike * Math.exp(-RISK_FREE_RATE * timeToMaturity) * cdf(d2);
    }

    public static double calculatePutPrice(final double stockPrice, final double timeToMaturity, final double strike) {
        double d1Numerator = Math.log(stockPrice / strike) + (RISK_FREE_RATE + IMPLIED_VOLATILITY * IMPLIED_VOLATILITY / 2) * timeToMaturity;
        double d1Denominator = IMPLIED_VOLATILITY * Math.sqrt(timeToMaturity);
        double d1 = d1Numerator / d1Denominator;
        double d2 = d1 - d1Denominator;

        return strike * Math.exp(-RISK_FREE_RATE * timeToMaturity) * cdf(-d2) - stockPrice * cdf(-d1);
    }


    public static double cdf(double z) {
        // using Taylor approximation
        if (z < -8.0) return 0.0;
        if (z > 8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * pdf(z);
    }

    public static double pdf(double x) {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    public static void main(String[] args) {
        System.out.println(calculateCallPrice(60, 1, 60));
    }
}
