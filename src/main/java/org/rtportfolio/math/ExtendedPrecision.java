package org.rtportfolio.math;

/**
 * Copied from apache common maths lib
 */
public final class ExtendedPrecision {
    private static final double SQRT2PI_H = highPartUnscaled(2.5066282746310007);
    private static final double SQRT2PI_L;
    private ExtendedPrecision() {
    }

    private static double highPartUnscaled(double value) {
        double c = 1.34217729E8 * value;
        return c - (c - value);
    }

    static double sqrt2xx(double x) {
        if (x > 3.273390607896142E150) {
            return x == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : computeSqrt2aa(x * 2.409919865102884E-181) * 4.149515568880993E180;
        } else {
            return x < 3.0549363634996047E-151 ? computeSqrt2aa(x * 4.149515568880993E180) * 2.409919865102884E-181 : computeSqrt2aa(x);
        }
    }

    static double xsqrt2pi(double x) {
        if (x > 3.273390607896142E150) {
            return x == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : computeXsqrt2pi(x * 2.409919865102884E-181) * 4.149515568880993E180;
        } else {
            return x < 3.0549363634996047E-151 ? computeXsqrt2pi(x * 4.149515568880993E180) * 2.409919865102884E-181 : computeXsqrt2pi(x);
        }
    }

    private static double computeXsqrt2pi(double a) {
        double ha = highPartUnscaled(a);
        double la = a - ha;
        double x = a * 2.5066282746310007;
        double xx = productLow(ha, la, SQRT2PI_H, SQRT2PI_L, x);
        return a * -1.8328579980459167E-16 + xx + x;
    }

    private static double computeSqrt2aa(double a) {
        double ha = highPartUnscaled(a);
        double la = a - ha;
        double x = 2.0 * a * a;
        double xx = productLow(ha, la, 2.0 * ha, 2.0 * la, x);
        double c = Math.sqrt(x);
        if (xx == 0.0) {
            return c;
        } else {
            double hc = highPartUnscaled(c);
            double lc = c - hc;
            double u = c * c;
            double uu = productLow(hc, lc, hc, lc, u);
            double cc = (x - u - uu + xx) * 0.5 / c;
            return c + cc;
        }
    }

    private static double productLow(double hx, double lx, double hy, double ly, double xy) {
        return lx * ly - (xy - hx * hy - lx * hy - hx * ly);
    }


    static {
        SQRT2PI_L = 2.5066282746310007 - SQRT2PI_H;
    }

}
