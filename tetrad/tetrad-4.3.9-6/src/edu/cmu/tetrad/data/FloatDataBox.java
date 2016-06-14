package edu.cmu.tetrad.data;

/**
 * Stores a 2D array of float data. Note that the missing value marker for this
 * box is Float.NaN.
 */
public class FloatDataBox implements DataBox {
    static final long serialVersionUID = 23L;

    /**
     * The stored float data.
     */
    private float[][] data;

    /**
     * Constructs an 2D float array consisting entirely of missing values (-99).
     * @param rows
     * @param cols
     */
    public FloatDataBox(int rows, int cols) {
        this.data = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = Float.NaN;
            }
        }
    }

    /**
     * Constructs a new data box using the given 2D float data array as data.
     */
    public FloatDataBox(float[][] data) {
        int length = data[0].length;

        for (float[] datum : data) {
            if (datum.length != length) {
                throw new IllegalArgumentException("All rows must have same length.");
            }
        }

        this.data = data;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BoxDataSet serializableInstance() {
        return new BoxDataSet(new ShortDataBox(4, 4), null);
    }
    
    /**
     * Returns the number of rows in this data box.
     */
    public int numRows() {
        return data.length;
    }

    /**
     * Returns the number of columns in this data box.n
     */
    public int numCols() {
        return data[0].length;
    }

    /**
     * Sets the value at the given row/column to the given Number value.
     * The value used is number.floatValue().
     */
    public void set(int row, int col, Number value) {
        if (value == null) {
            data[row][col] = Float.NaN;
        } else {
            data[row][col] = value.floatValue();
        }
    }

    /**
     * Returns the Number value at the given row and column. If the value
     * is missing (-99), null, is returned.
     */
    public Number get(int row, int col) {
        float datum = data[row][col];

        if (datum == Float.NaN) {
            return null;
        }
        else {
            return datum;
        }
    }

    /**
     * Returns a copy of this data box.
     */
    public DataBox copy() {
        FloatDataBox box = new FloatDataBox(numRows(), numCols());

        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numCols(); j++) {
                box.set(i, j, get(i, j));
            }
        }

        return box;
    }

    /**
     * Returns a DataBox of type FloatDataBox, but with the given dimensions.
     */
    public DataBox like(int rows, int cols) {
        return new FloatDataBox(rows, cols);
    }
}