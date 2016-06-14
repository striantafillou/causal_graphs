package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:02 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class Exponential implements Distribution {
    static final long serialVersionUID = 23L;

    private double lambda;

    public Exponential(double lambda) {
        this.lambda = lambda;
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Exponential";
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Exponential serializableInstance() {
        return new Exponential(.5);
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            lambda = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return lambda;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Lambda";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextExponential(lambda);
    }

    public String toString() {
        return "Exponential(" + lambda + ")";
    }
}
