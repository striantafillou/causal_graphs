package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:35 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
@SuppressWarnings({"WeakerAccess"})
public class Hyperbolic implements Distribution {
    static final long serialVersionUID = 23L;

    private double alpha;
    private double gamma;

    @SuppressWarnings({"SameParameterValue", "UnusedDeclaration"})
    public Hyperbolic(double alpha, double gamma) {
        this.alpha = alpha;
        this.gamma = gamma;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static Hyperbolic serializableInstance() {
        return new Hyperbolic(1, 2);
    }

    public int getNumParameters() {
        return 2;
    }

    public String getName() {
        return "Hyperbolic";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            alpha = value;
        }
        else if (index == 1) {
            gamma = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return alpha;
        }
        else if (index == 1) {
            return gamma;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Alpha";
        }
        else if (index == 1) {
            return "Gamma";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextHyperbolic(alpha, gamma);
    }

    public String toString() {
        return "Hyperbolic(" + alpha + ", " + gamma + ")";
    }
}
