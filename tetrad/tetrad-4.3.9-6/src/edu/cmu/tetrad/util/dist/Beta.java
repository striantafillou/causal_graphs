package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Implements a Beta distribution for purposes of drawing random numbers.
 * The parameters are alpha and beta. See Wikipedia.
 *
 * @author Joseph Ramsey
 */
public class Beta implements Distribution {
    static final long serialVersionUID = 23L;

    /**
     * Ibid.
     */
    private double alpha = 0.5;

    /**
     * Ibid.
     */
    private double beta = 0.5;

    /**
     * Ibid.
     * @param alpha Ibid.
     * @param beta  Ibid.
     */
    public Beta(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Beta serializableInstance() {
        return new Beta(.5, .5);
    }

    /**
     * See interface.
     */
    public double nextRandom() {
        return RandomUtil.getInstance().nextBeta(alpha, beta);
    }

    /**
     * The order of parameters is alpha = 0, beta = 1.
     */
    public void setParameter(int index, double value) {
        if (index == 0) {
            alpha = value;
        } else if (index == 1 && value >= 0) {
            beta = value;
        } else {
            throw new IllegalArgumentException("Illegal value: " + value);
        }
    }

    /**
     * The order of parameters is alpha = 0, beta = 1.
     */
    public double getParameter(int index) {
        if (index == 0) {
            return alpha;
        } else if (index == 1) {
            return beta;
        } else {
            throw new IllegalArgumentException("Illegal index: " + index);
        }
    }

    /**
     * The order of parameters is alpha = 0, beta = 1.
     */
    public String getParameterName(int index) {
        if (index == 0) {
            return "Alpha";
        } else if (index == 1) {
            return "Beta";
        } else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    /**
     * Uh, there are 2 parameters...
     * @return Ibid.
     */
    public int getNumParameters() {
        return 2;
    }

    /**
     * Please don't make me say it...
     */
    public String getName() {
        return "Beta";
    }

    /**
     * A string representation of the distribution.
     */
    public String toString() {
        return "B(" + alpha + ", " + beta + ")";
    }
}
