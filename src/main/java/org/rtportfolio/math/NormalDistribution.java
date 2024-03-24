package org.rtportfolio.math;

/**
 * Copied from apache common maths lib
 */
public class NormalDistribution {

    private static final double HALF_LOG_TWO_PI = 0.9189385332046728;
    private final double mean;
    private final double standardDeviation;
    private final double logStandardDeviationPlusHalfLog2Pi;
    private final double sdSqrt2;
    private final double sdSqrt2pi;

    public NormalDistribution(double mean, double sd) {
        this.mean = mean;
        this.standardDeviation = sd;
        this.logStandardDeviationPlusHalfLog2Pi = Math.log(sd) + 0.9189385332046728;
        this.sdSqrt2 = ExtendedPrecision.sqrt2xx(sd);
        this.sdSqrt2pi = ExtendedPrecision.xsqrt2pi(sd);
    }

    public double cumulativeProbability(double x) {
        double dev = x - this.mean;
        return 0.5 * erfc(-dev / this.sdSqrt2);
    }

    static double erfc(double x) {
        return erfImp(x, true, false);
    }

    private static double erfImp(double z, boolean invert, boolean scaled) {
        if (Double.isNaN(z)) {
            return Double.NaN;
        } else if (z < 0.0) {
            if (!invert) {
                return -erfImp(-z, invert, false);
            } else {
                return z < -0.5 ? 2.0 - erfImp(-z, invert, false) : 1.0 + erfImp(-z, false, false);
            }
        } else {
            double result;
            double Y;
            double zm;
            double P;
            double Q;
            if (z < 0.5) {
                if (z < 1.0E-10) {
                    if (z == 0.0) {
                        result = z;
                    } else {
                        Y = 0.0033791670955125737;
                        result = z * 1.125 + z * 0.0033791670955125737;
                    }
                } else {
                    Y = 1.0449485778808594;
                    zm = z * z;
                    P = -3.227801209646057E-4;
                    P = -0.007727583458021333 + P * zm;
                    P = -0.050999073514677744 + P * zm;
                    P = -0.3381651344593609 + P * zm;
                    P = 0.08343058921465318 + P * zm;
                    Q = 3.70900071787748E-4;
                    Q = 0.008585719250744061 + Q * zm;
                    Q = 0.08752226001422525 + Q * zm;
                    Q = 0.455004033050794 + Q * zm;
                    Q = 1.0 + Q * zm;
                    result = z * (1.0449485778808594 + P / Q);
                }
            } else {
                label97: {
                    if (!scaled) {
                        label92: {
                            if (invert) {
                                if (z < 27.30078125) {
                                    break label92;
                                }
                            } else if (z < 5.9306640625) {
                                break label92;
                            }

                            result = 0.0;
                            invert = !invert;
                            break label97;
                        }
                    }

                    invert = !invert;
                    if (z < 1.5) {
                        Y = 0.40593576431274414;
                        zm = z - 0.5;
                        P = 0.0018042453829701423;
                        P = 0.01950490012512188 + P * zm;
                        P = 0.08889003689678844 + P * zm;
                        P = 0.19100369579677542 + P * zm;
                        P = 0.17811466584112035 + P * zm;
                        P = -0.09809059221628125 + P * zm;
                        Q = 3.3751147248309467E-6;
                        Q = 0.011338523357700142 + Q * zm;
                        Q = 0.12385097467900864 + Q * zm;
                        Q = 0.5780528048899024 + Q * zm;
                        Q = 1.4262800484551132 + Q * zm;
                        Q = 1.8475907098300222 + Q * zm;
                        Q = 1.0 + Q * zm;
                        result = 0.40593576431274414 + P / Q;
                        if (scaled) {
                            result /= z;
                        } else {
                            result *= expmxx(z) / z;
                        }
                    } else if (z < 2.5) {
                        Y = 0.5067281723022461;
                        zm = z - 1.5;
                        P = 2.3583911559688073E-4;
                        P = 0.0032396240629084215 + P * zm;
                        P = 0.017567943631180208 + P * zm;
                        P = 0.04394818964209516 + P * zm;
                        P = 0.03865403750357072 + P * zm;
                        P = -0.024350047620769845 + P * zm;
                        Q = 0.004103697239789046;
                        Q = 0.05639218374204782 + Q * zm;
                        Q = 0.3257329247824444 + Q * zm;
                        Q = 0.9824037091579202 + Q * zm;
                        Q = 1.5399149494855244 + Q * zm;
                        Q = 1.0 + Q * zm;
                        result = 0.5067281723022461 + P / Q;
                        if (scaled) {
                            result /= z;
                        } else {
                            result *= expmxx(z) / z;
                        }
                    } else if (z < 4.0) {
                        Y = 0.5405750274658203;
                        zm = z - 3.5;
                        P = 1.1321240664884757E-5;
                        P = 2.5026996154479465E-4 + P * zm;
                        P = 0.0021282562091461863 + P * zm;
                        P = 0.008408076155555853 + P * zm;
                        P = 0.013738442589635533 + P * zm;
                        P = 0.0029527671653097167 + P * zm;
                        Q = 4.7941126952171447E-4;
                        Q = 0.010598290648487654 + Q * zm;
                        Q = 0.09584927263010615 + Q * zm;
                        Q = 0.4425976594815631 + Q * zm;
                        Q = 1.0421781416693843 + Q * zm;
                        Q = 1.0 + Q * zm;
                        result = 0.5405750274658203 + P / Q;
                        if (scaled) {
                            result /= z;
                        } else {
                            result *= expmxx(z) / z;
                        }
                    } else {
                        Y = 1.0 / (z * z);
                        zm = 0.016315387137302097;
                        zm = 0.30532663496123236 + zm * Y;
                        zm = 0.36034489994980445 + zm * Y;
                        zm = 0.12578172611122926 + zm * Y;
                        zm = 0.016083785148742275 + zm * Y;
                        zm = 6.587491615298378E-4 + zm * Y;
                        P = 1.0;
                        P = 2.568520192289822 + P * Y;
                        P = 1.8729528499234604 + P * Y;
                        P = 0.5279051029514285 + P * Y;
                        P = 0.06051834131244132 + P * Y;
                        P = 0.0023352049762686918 + P * Y;
                        result = Y * zm / P;
                        result = (0.5641895835477563 - result) / z;
                        if (!scaled) {
                            result *= expmxx(z);
                        }
                    }
                }
            }

            if (invert) {
                result = 1.0 - result;
            }

            return result;
        }
    }

    static double expmxx(double x) {
        double a = x * x;
        double b = squareLowUnscaled(x, a);
        return expxx(-a, -b);
    }

    private static double expxx(double a, double b) {
        double ea = Math.exp(a);
        return ea * b + ea;
    }

    private static double highPartUnscaled(double value) {
        double c = 1.34217729E8 * value;
        return c - (c - value);
    }

    private static double squareLowUnscaled(double x, double xx) {
        double hx = highPartUnscaled(x);
        double lx = x - hx;
        return squareLow(hx, lx, xx);
    }

    private static double squareLow(double hx, double lx, double xx) {
        return lx * lx - (xx - hx * hx - 2.0 * lx * hx);
    }

}
