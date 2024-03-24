package org.rtportfolio;

import org.rtportfolio.math.NormalDistribution;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class OptionPriceCalculator {
    private static final LocalDate now = LocalDate.now();
    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final NormalDistribution snd = new NormalDistribution(0, 1);

    public static double getTimeToMaturity(final String timeToMaturityDate) {
        //TODO Garbage
        LocalDate ldt = LocalDate.parse(timeToMaturityDate, f);
        return ChronoUnit.DAYS.between(now, ldt) / 365d;
    }

    public static double calculateCallPrice(final double stockPrice, final double strike, final double timeToMaturity, final double riskFreeRate, final double impliedVol) {
        double d1Numerator = Math.log(stockPrice / strike) + (riskFreeRate + impliedVol * impliedVol / 2) * timeToMaturity;
        double d1Denominator = impliedVol * Math.sqrt(timeToMaturity);
        double d1 = d1Numerator / d1Denominator;
        double d2 = d1 - d1Denominator;

        return stockPrice * snd.cumulativeProbability(d1) - strike * Math.exp(-riskFreeRate * timeToMaturity) * snd.cumulativeProbability(d2);
    }

    public static double calculatePutPrice(final double stockPrice, final double strike, final double timeToMaturity, final double riskFreeRate, final double impliedVol) {
        double d1Numerator = Math.log(stockPrice / strike) + (riskFreeRate + impliedVol * impliedVol / 2) * timeToMaturity;
        double d1Denominator = impliedVol * Math.sqrt(timeToMaturity);
        double d1 = d1Numerator / d1Denominator;
        double d2 = d1 - d1Denominator;

        return strike * Math.exp(-riskFreeRate * timeToMaturity) * snd.cumulativeProbability(-d2) - stockPrice * snd.cumulativeProbability(-d1);
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
//        System.out.println(calculateCallPrice(60, 1, 60));
        System.out.println(cdf(2));
        System.out.println(snd.cumulativeProbability(2));
    }
}
