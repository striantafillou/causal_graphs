package edu.cmu.tetrad.search;

import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.Arrays;

/**
 * Implements the Anderson-Darling test for normality, with P values calculated
 * as in R's ad.test method (in package nortest).
 * <p/>
 * Note that in the calculation, points x such that log(1 - normal_cdf(x))
 * is infinite are ignored.
 *
 * @author Joseph Ramsey
 */
public class AndersonDarlingTest {

    /**
     * The column of data being analyzed.
     */
    private double[] data;

    /**
     * The A^2 statistic for <code>data</code>
     */
    private double aSquared;

    /**
     * The A^2 statistic adjusted for sample size.
     */
    private double aSquaredStar;

    /**
     * The interpolated p value for the adjusted a squared.
     */
    private double p;

    //============================CONSTRUCTOR===========================//

    /**
     * Constructs an Anderson-Darling test for the given column of data.
     */
    public AndersonDarlingTest(double[] data) {
        this.data = data;
        runTest();
    }

    //============================PUBLIC METHODS=========================//

    /**
     * Returns a copy of the data being analyzed.
     */
    public double[] getData() {
        double[] data2 = new double[data.length];
        System.arraycopy(data, 0, data2, 0, data.length);
        return data2;
    }

    /**
     * Returns the A^2 statistic.
     */
    public double getASquared() {
        return aSquared;
    }

    /**
     * Returns the A^2* statistic, which is the A^2 statistic adjusted
     * heuristically for sample size.
     */
    public double getASquaredStar() {
        return aSquaredStar;
    }

    /**
     * Returns the p value of the A^2* statistic, which is interpolated using
     * exponential functions.
     */
    public double getP() {
        return p;
    }

    //============================PRIVATE METHODS========================//

    private void runTest() {
        int n = data.length;
        double[] x = new double[data.length];
        System.arraycopy(data, 0, x, 0, data.length);
        Arrays.sort(x);

        double mean = StatUtils.mean(x);
        double sd = StatUtils.standardDeviation(x);
        double[] y = new double[n];

//        Normal phi = new Normal(0, 1, RandomUtil.getInstance().getEngine());

        for (int i = 0; i < n; i++) {
            y[i] = (x[i] - mean) / sd;
        }

        double h = 0.0;

        int numSummed = 0;

        for (int i = 0; i < n; i++) {
            double y1 = y[i];
            double a1 = Math.log(RandomUtil.getInstance().normalCdf(0, 1, y1));

            double y2 = y[n - i - 1];
            double a2 = Math.log(1.0 - RandomUtil.getInstance().normalCdf(0, 1, y2));

            if (Double.isInfinite(a1) || Double.isInfinite(a2)) {
                continue;
            }

            h += (2 * (i + 1) - 1) * (a1 + a2);
            numSummed++;
        }

        double a = -numSummed - (1.0 / numSummed) * h;
        double aa = (1 + 0.75 / n + 2.25 / Math.pow(numSummed, 2)) * a;
        double p;

        if (aa < 0.2) {
            p = 1 - Math.exp(-13.436 + 101.14 * aa - 223.73 * aa * aa);
        } else if (aa < 0.34) {
            p = 1 - Math.exp(-8.318 + 42.796 * aa - 59.938 * aa * aa);
        } else if (aa < 0.6) {
            p = Math.exp(0.9177 - 4.279 * aa - 1.38 * aa * aa);
        } else {
            p = Math.exp(1.2937 - 5.709 * aa + 0.0186 * aa * aa);
        }

        this.aSquared = a;
        this.aSquaredStar = aa;
        this.p = p;
    }
}
