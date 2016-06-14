package edu.cmu.tetrad.data;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Stores a 2D array of data. Different implementations may store data in
 * different ways, allowing for space or time efficiency.
 *
 * @author Joseph Ramsey
 */
public interface DataBox extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the (fixed) number of rows of the dataset.
     */
    int numRows();

    /**
     * Returnse the (fixed) number of columns of the dataset.
     */
    int numCols();

    /**
     * Sets the value at the given row and column to the given Number. This
     * number may be interpreted differently depending on how values are
     * stored. A value of null is interpreted as a missing value.
     *
     * @throws IllegalArgumentException if the given value cannot be stored
     * (because it's out of range or cannot be converted or whatever).
     */
    void set(int row, int col, Number value) throws IllegalArgumentException;

    /**
     * Returns the value at the given row and column as a Number. If the
     * value is missing, null is uniformly returned.
     */
    Number get(int row, int col);

    /**
     * Returns a copy of this data box.
     */
    DataBox copy();

    /**
     * Returns a new data box of the same type as this one with the given
     * dimensions.
     */
    DataBox like(int rows, int cols);
}
