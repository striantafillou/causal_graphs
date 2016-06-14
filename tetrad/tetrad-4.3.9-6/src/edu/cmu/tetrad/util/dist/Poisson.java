package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:49 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class Poisson implements Distribution {
    static final long serialVersionUID = 23L;

    private double mean;

    public Poisson(double mean) {
        this.mean = mean;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Poisson serializableInstance() {
        return new Poisson(1);
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Distibution";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            mean = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return mean;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Mean";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextPoisson(mean);
    }

    public String toString() {
        return "Poisson(" + mean + ")";
    }
}
