package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Represents a von Mises distribution for sammpling, with the given
 * degrees of freedom.
 *
 * @author Joseph Ramsey
 */
public class VonMises implements Distribution {
    static final long serialVersionUID = 23L;

    private double freedom;

    public VonMises(double freedom) {
        this.freedom = freedom;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static VonMises serializableInstance() {
        return new VonMises(1);
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Von Mises";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            freedom = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return freedom;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        return "Freedom";
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextVonMises(freedom);
    }

    public String toString() {
        return "vonMises(" + freedom + ")";
    }
}
