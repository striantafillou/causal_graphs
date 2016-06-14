package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Represents an exponential power distribution for purposes of sampling.
 *
 * @author Joseph Ramsey
*/
public class ExponentialPower implements Distribution {
    static final long serialVersionUID = 23L;

    private double tau;

    public ExponentialPower(double tau) {
        this.tau = tau;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static ExponentialPower serializableInstance() {
        return new ExponentialPower(2);
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Exponential Power";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            tau = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return tau;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Tau";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextExponentialPower(tau);
    }

    public String toString() {
        return "ExponentialPower(" + tau + ")";
    }
}
