package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <p>Maps a parameter to the matrix element where its value is stored in the
 * model.</p>
 *
 * @author Frank Wimberly
 * @author Joe Ramsey
 */
public class Sem2MatrixMapping implements Sem2Mapping, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The SemIm for which this is a mapping.
     *
     * @serial Can't be null.
     */
    private SemIm2 semIm;

    /**
     * The parameter this mapping maps.
     *
     * @serial Can't be null.
     */
    private Parameter parameter;

    /**
     * The 2D double array whose element at (i, j) to be manipulated.
     *
     * @serial Can't be null.
     */
    private double[][] a;
    private DoubleMatrix2D aC;

    /**
     * The left-hand coordinate of a[i][j].
     *
     * @serial Any value.
     */
    private int i;

    /**
     * The right-hand coordinate of a[i][j].
     *
     * @serial Any value.
     */
    private int j;

    /**
     * Constructs matrix new mapping using the given parameters.
     *
     * @param parameter The parameter that this maps.
     * @param matrix    The array containing matrix[i][j], the element to be
     *                  manipulated.
     * @param i         Left coordinates of matrix[i][j].
     * @param j         Right coordinate of matrix[i][j].
     */
    public Sem2MatrixMapping(SemIm2 semIm2, Parameter parameter, DoubleMatrix2D matrix,
            int i, int j) {
        if (semIm2 == null) {
            throw new NullPointerException("SemIm must not be null.");
        }

        if (parameter == null) {
            throw new NullPointerException("Parameter must not be null.");
        }

        if (matrix == null) {
            throw new NullPointerException("Supplied array must not be null.");
        }

        if (i < 0 || j < 0) {
            throw new IllegalArgumentException("Indices must be non-negative");
        }

        this.semIm = semIm2;
        this.parameter = parameter;
        this.aC = matrix;
        this.i = i;
        this.j = j;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Sem2MatrixMapping serializableInstance() {
        return new Sem2MatrixMapping(SemIm2.serializableInstance(),
                Parameter.serializableInstance(), new DenseDoubleMatrix2D(0, 0),
                1, 1);
    }

    /**
     * Sets the value of the array element at the stored coordinates (i, j).
     * If the array is symmetric sets two elements.
     */
    public void setValue(double x) {
        if (this.semIm.isParameterBoundsEnforced() &&
                getParameter().getType() == ParamType.VAR && x < 0.0) {
            throw new IllegalArgumentException(
                    "Variances cannot " + "have values <= 0.0: " + x);
        }

        aC.setQuick(i, j, x);

        if (getParameter().getType() == ParamType.VAR ||
                getParameter().getType() == ParamType.COVAR) {
            aC.setQuick(j, i, x);
        }
    }

    /**
     * Returns the value of the array element at (i, j).
     */
    public double getValue() {
        return aC.getQuick(i, j);
    }

    /**
     * Returns the paramter that this mapping maps.
     */
    public Parameter getParameter() {
        return this.parameter;
    }

    /**
     * Returns a String containing information (array name and values of
     * subscripts) about the array element associated with this mapping.
     */
    public String toString() {
        return "<" + getParameter().getName() + " " + getParameter().getType() +
                "[" + i + "][" + j + "]>";
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (semIm == null) {
            throw new NullPointerException();
        }

        if (parameter == null) {
            throw new NullPointerException();
        }

//        if (aC == null) {
//            throw new NullPointerException();
//        }
    }
}
