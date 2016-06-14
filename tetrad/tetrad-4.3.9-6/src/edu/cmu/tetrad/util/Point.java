package edu.cmu.tetrad.util;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * Implements an n-dimensional point.
 *
 * @author Joseph Ramsey
 */
public class Point implements Comparable {

    /**
     * A vector representing the point coordinates.
     */
    private final DoubleMatrix1D vector;

    /**
     * Constructs a point with coordinates as in the given vector.
     * @param vector a vector representing the point coordinates, in order.
     */
    public Point(DoubleMatrix1D vector) {
        this.vector = vector.copy();
    }

    /**
     * Returns the coordinate at the given index.
     * @param index Ibid.
     * @return Ibid.
     */
    public double getValue(int index) {
        return vector.get(index);
    }

    /**
     * Returns the size of the vector.
     * @return Ibid.
     */
    public int getSize() {
        return vector.size();
    }

    /**
     * True iff the given object is a point with the same coordinates as
     * this one.
     * @param o Ibid.
     * @return Ibid.
     */
    public int compareTo(Object o) {
        if (o == this) {
            return 0;
        }

        Point p = (Point) o;

        for (int i = 0; i < getSize(); i++) {
            if (getValue(i) != p.getValue(i)) {
                return (int) Math.signum(p.getValue(i) - getValue(i));
            }
        }

        return 0;
    }

    /**
     * Returns a string representation of this point.
     * @return Ibid.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("<");

        for (int i = 0; i < getSize(); i++) {
            buf.append(getValue(i));

            if (i < getSize() - 1) {
                buf.append(", ");
            }
        }

        buf.append(">");
        return buf.toString();
    }

    /**
     * Returns the vector of coordinates.
     * @return Ibid.
     */
    public DoubleMatrix1D getVector() {
        return this.vector.copy();
    }
}
