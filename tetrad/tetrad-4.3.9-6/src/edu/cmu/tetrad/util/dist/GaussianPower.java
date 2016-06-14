package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

public class GaussianPower implements Distribution {
    static final long serialVersionUID = 23L;

    private final double sd;
    private double power;
    private final String name;

    public String getName() {
        return this.name;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static GaussianPower serializableInstance() {
        return new GaussianPower(1, 2);
    }

    public GaussianPower(double power) {
        this(1, power);
    }

    public GaussianPower(double sd, double power) {
        this.sd = sd;
        this.power = power;
        this.name = "N^" + power + "(" + 0 + "," + sd + ")";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            power = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return sd;
        }
        else if (index == 1) {
            return power;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Standard Deviation";
        }
        else if (index == 1) {
            return "Power";
        }

        throw new IllegalArgumentException();
    }

    public int getNumParameters() {
        return 2;
    }

    public double nextRandom() {
        double value = RandomUtil.getInstance().nextNormal(0, 1);
        double poweredValue = java.lang.Math.pow(java.lang.Math.abs(value), power);
        return (value >= 0) ? poweredValue : -poweredValue;
    }
}
