package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Represents a lognormal distribution for purposes of sampling.
 *
 * @author Joseph Ramsey
*/
public class LogNormal implements Distribution {
    static final long serialVersionUID = 23L;

    private double sd;

    public LogNormal() {
        this(1);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static LogNormal serializableInstance() {
        return new LogNormal(.5);
    }

    public LogNormal(double sd) {
        this.sd = sd;
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "LogNormal";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            sd = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return sd;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        return "Standard Deviation";
    }

    public double nextRandom() {
        double random = RandomUtil.getInstance().nextNormal(0, sd);
        return Math.exp(random);
    }

    public String toString() {
        return "LogNormal";
    }
}
