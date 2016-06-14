package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:43 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class Logarithmic implements Distribution {
    static final long serialVersionUID = 23L;

    private double p;

    public Logarithmic(double p) {
        this.p = p;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Logarithmic serializableInstance() {
        return new Logarithmic(1);
    }

    public String toString() {
        return "Logarithmic(" + p + ")";
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Logarithmic";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            p = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (p == 0) {
            return p;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (p == 0) {
            return "P";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextLogarithmic(p);
    }
}
