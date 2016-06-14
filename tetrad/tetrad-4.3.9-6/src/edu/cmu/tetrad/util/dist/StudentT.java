package edu.cmu.tetrad.util.dist;

import edu.cmu.tetrad.util.RandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:55 PM
* To change this template use File | Settings | File Templates.
*/
/**
 * Wraps a chi square distribution for purposes of drawing random samples.
 * Methods are provided to allow parameters to be manipulated in an interface.
 *
 * @author Joseph Ramsey
 */
public class StudentT implements Distribution {
    static final long serialVersionUID = 23L;

    private double df;

    public StudentT(double df) {
        if (df < 0) {
            throw new IllegalArgumentException("Degrees of Freedom must be >= 0: " + df);
        }

        this.df = df;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @return The exemplar.
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static StudentT serializableInstance() {
        return new StudentT(1);
    }

    public int getNumParameters() {
        return 1;
    }

    public String getName() {
        return "Student T";
    }

    public void setParameter(int index, double value) {
        if (index == 0) {
            df = value;
        }

        throw new IllegalArgumentException();
    }

    public double getParameter(int index) {
        if (index == 0) {
            return df;
        }

        throw new IllegalArgumentException();
    }

    public String getParameterName(int index) {
        if (index == 0) {
            return "Degrees of freedom";
        }

        throw new IllegalArgumentException();
    }

    public double nextRandom() {
        return RandomUtil.getInstance().nextStudentT(df);
    }

    public String toString() {
        return "StudentT(" + df + ")";
    }
}
