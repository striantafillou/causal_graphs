package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:28 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class MixtureOfGaussians implements Distribution {
    static final long serialVersionUID = 23L;

    private double a;
    private double mean1;
    private double sd1;
    private double mean2;
    private double sd2;

    public MixtureOfGaussians(double a, double mean1, double sd1, double mean2, double sd2) {
        if (a < 0 || a > 1) {
            throw new IllegalArgumentException();
        }

        if (sd1 <= 0) {
            throw new IllegalArgumentException();
        }

        if (sd2 <= 0) {
            throw new IllegalArgumentException();
        }

        this.a = a;
        this.mean1 = mean1;
        this.sd1 = sd1;
        this.mean2 = mean2;
        this.sd2 = sd2;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static MixtureOfGaussians serializableInstance() {
        return new MixtureOfGaussians(.5, -2, 2, 2, 2);
    }

    public int getNumParameters() {
        return 5;
    }

    public String getName() {
        return "Mixture of Gaussians";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            a = value;
        }
        else if (index == 1) {
            mean1 = value;
        }
        else if (index == 2) {
            sd1 = value;
        }
        else if (index == 3) {
            mean2 = value;
        }
        else if (index == 5) {
            sd2 = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return a;
        }
        else if (index == 1) {
            return mean1;
        }
        else if (index == 2) {
            return sd1;
        }
        else if (index == 3) {
            return mean2;
        }
        else if (index == 5) {
            return sd2;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Ratio";
        }
        else if (index == 1) {
            return "Mean 1";
        }
        else if (index == 2) {
            return "Standard Deviation 1";
        }
        else if (index == 3) {
            return "Mean 2";
        }
        else if (index == 5) {
            return "Standard Deviation 2";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        double r = RandomUtil.getInstance().nextDouble();

        if (r < a) {
            return RandomUtil.getInstance().nextNormal(mean1, sd1);
        } else {
            return RandomUtil.getInstance().nextNormal(mean2, sd2);
        }
    }

    public String toString() {
        return "MixtureOfGaussians(" + a + ", " + mean1 + ", " + sd1 + ", " + mean2 + ", " + sd2 + ")";
    }
}
