package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.util.*;

/**
 * @author Joseph Ramsey
 */
public class SemOptimizerScattershot implements SemOptimizer {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS=========================//

    /**
     * Blank constructor.
     */
    public SemOptimizerScattershot() {
        TetradLoggerConfig config = new DefaultTetradLoggerConfig("info");
        config.setEventActive("info", true);
        TetradLogger.getInstance().setTetradLoggerConfig(config);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerScattershot serializableInstance() {
        return new SemOptimizerScattershot();
    }

    //==============================PUBLIC METHODS========================//

    /**
     * Optimizes the fitting function of the given Sem using the Powell method
     * from Numerical Recipes by adjusting the parameters of the Sem.
     */
    public void optimize(SemIm semIm) {

        TetradLogger.getInstance().log("info", "Trying EM...");
        new SemOptimizerEm().optimize(semIm);

        TetradLogger.getInstance().log("info", "Trying scattershot...");
        FittingFunction f = new SemFittingFunction(semIm);

        double[] p = semIm.getFreeParamValues();
        double[] pRef = new double[p.length];

//        iterateFindLowerRandom(f, p, 1.0, 500);
//        iterateFindLowerRandom(f, p, 0.5, 500);
//        iterateFindLowerRandom(f, p, 0.1, 500);
//
//        while (true) {
//            System.arraycopy(p, 0, pRef, 0, p.length);
//
//            iterateFindLowerRandom(f, p, 0.05, 500);
//            iterateFindLowerRandom(f, p, 0.01, 500);
//            iterateFindLowerRandom(f, p, 0.005, 500);
//
//            if (pointsEqual(p, pRef)) break;
//        }

        while (true) {
            System.arraycopy(p, 0, pRef, 0, p.length);

            semIm.setFreeParamValues(p);
            double width = semIm.getFml() / 40.0;
//            if (width > 1.0) width = 1.0;

            iterateFindLowerRandom(f, p, width, 500);

            if (pointsEqual(p, pRef)) break;
        }

//        logger.getInstance().log("info", "Wiggling each parameter in turn...");
//
//        slideIndividualParameters(f, p, pRef, .1);
//        slideIndividualParameters(f, p, pRef, .01);
//        slideIndividualParameters(f, p, pRef, .001);
//        slideIndividualParameters(f, p, pRef, .0001);
//        slideIndividualParameters(f, p, pRef, .00001);
//        slideIndividualParameters(f, p, pRef, .000001);

        semIm.setFreeParamValues(p);
    }

    private void slideIndividualParameters(FittingFunction f, double[] p, double[] pRef, double delta) {
        TetradLogger.getInstance().log("info", "Sliding parameters delta = " + delta);

        double min = f.evaluate(p);

        while (true) {
            System.arraycopy(p, 0, pRef, 0, p.length);

            for (int i = 0; i < p.length; i++) {
                p[i] += delta;
                double value = f.evaluate(p);

                if (value < min) {
                    TetradLogger.getInstance().log("info", "Sliding parameter " + i + " up, min = " + value);
                    min = value;
                } else {
                    p[i] -= 2 * delta;
                    value = f.evaluate(pRef);

                    if (value < min) {
                        TetradLogger.getInstance().log("info", "Sliding parameter " + i + " down, min = " + value);
                        min = value;
                    } else {
                        p[i] = pRef[i];
                    }
                }
            }

            if (pointsEqual(p, pRef)) break;
        }
    }

    private double[] tryCds(SemIm semIm, double[] p) {
        semIm.setFreeParamValues(p);
        new SemOptimizerPalCds().optimize(semIm);
        return semIm.getFreeParamValues();
    }

    private void iterateFindLowerRandom(FittingFunction fcn, double[] p,
                                        double range, int iterations) {
        while (true) {
            boolean found = false;
            try {
                found = findLowerRandom(fcn, p, range, iterations);
            } catch (Exception e) {
                return;
            }

            if (!found) {
                return;
            }
        }
    }

    /**
     * Returns true iff a new point was found with a lower score.
     */
    private boolean findLowerRandom(FittingFunction fcn, double[] p,
                                    double width, int numPoints) {
        double fMin = fcn.evaluate(p);

        if (Double.isNaN(fMin)) {
            throw new IllegalArgumentException("Center point must evaluate!");
        }

        // This point will remain fixed, the center of the search.
        double[] fixedP = new double[p.length];
        System.arraycopy(p, 0, fixedP, 0, p.length);

//        boolean changed = false;

        // This point will move around randomly. If it ever has a lower
        // score than p, it will be copied into p (and returned).
        double[] pTemp = new double[p.length];
        System.arraycopy(p, 0, pTemp, 0, p.length);

        for (int i = 0; i < numPoints; i++) {
            randomPointAboutCenter(pTemp, fixedP, width);
            double f = fcn.evaluate(pTemp);

            if (f < fMin) {
//                double partial = getPartial(fMin, f, fixedP, pTemp);

                fMin = f;
                System.arraycopy(pTemp, 0, p, 0, pTemp.length);
                TetradLogger.getInstance().log("info", "Cube width = " + width + " FML = " + f);

//                System.out.println("Cube width = " + width + " partial = " + partial + " FML = " + f);
                return true;
            }
        }

        return false;
    }

    private double getPartial(double fBefore, double fAfter, double[] pBefore, double[] pAfter) {
        double distance = distance(pBefore, pAfter);
        double height = fAfter - fBefore;
        return height / distance;
    }

    private double distance(double[] pBefore, double[] pAfter) {
        double sum = 0.0;

        for (int i = 0; i < pBefore.length; i++) {
            double diff = pAfter[i] - pBefore[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    private void randomPointAboutCenter(double[] pTemp, double[] fixedP, double width) {
        for (int j = 0; j < pTemp.length; j++) {
            double v = getRandom().nextDouble();
            pTemp[j] = fixedP[j] + (-width / 2.0 + width * v);
        }
    }

    private boolean pointsEqual(double[] p, double[] pTemp) {
        for (int i = 0; i < p.length; i++) {
            if (p[i] != pTemp[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluates a fitting function for an array of parameters.
     *
     * @author Joseph Ramsey
     */
    static interface FittingFunction {

        /**
         * Returns the value of the function for the given array of parameter
         * values.
         */
        double evaluate(double[] argument);

        /**
         * Returns the number of parameters.
         */
        int getNumParameters();
    }

    /**
     * Wraps a Sem for purposes of calculating its fitting function for given
     * parameter values.
     *
     * @author Joseph Ramsey
     */
    static class SemFittingFunction implements SemOptimizerScattershot.FittingFunction {

        /**
         * The wrapped Sem.
         */
        private final SemIm sem;

        /**
         * Constructs a new PalFittingFunction for the given Sem.
         */
        public SemFittingFunction(SemIm sem) {
            this.sem = sem;
        }

        /**
         * Computes the maximum likelihood function value for the given                                   G
         * parameters values as given by the optimizer. These values are mapped
         * to parameter values.
         */
        public double evaluate(double[] parameters) {
            sem.setFreeParamValues(parameters);

            double fml = sem.getFml();

            if (Double.isNaN(fml)) {
                return Double.POSITIVE_INFINITY;
            }

            return fml;
        }

        /**
         * Returns the number of arguments. Required by the MultivariateFunction
         * interface.
         */
        public int getNumParameters() {
            return this.sem.getNumFreeParams();
        }
    }

    private RandomUtil getRandom() {
        return RandomUtil.getInstance();
    }
}
