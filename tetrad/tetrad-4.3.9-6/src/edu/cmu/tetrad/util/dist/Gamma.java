package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:27 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class Gamma implements Distribution {
    static final long serialVersionUID = 23L;

    private double alpha;
    private double lambda;

    public Gamma(double alpha, double lambda) {
        this.alpha = alpha;
        this.lambda = lambda;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Gamma serializableInstance() {
        return new Gamma(.5, .7);
    }

    public int getNumParameters() {
        return 2;
    }

    public String getName() {
        return "Gamma";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            alpha = value;
        }
        else if (index == 1) {
            lambda = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return alpha;
        }
        else if (index == 1) {
            return lambda;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Alpha";
        }
        else if (index == 1) {
            return "Lambda";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextGamma(alpha, lambda);
    }

    public String toString() {
        return "Gamma(" + alpha + ", " + lambda + ")";
    }
}
