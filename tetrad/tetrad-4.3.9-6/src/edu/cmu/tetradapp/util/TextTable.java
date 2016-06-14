package edu.cmu.tetradapp.util;

/**
 * Stores a 2D array of Strings for printing out tables. The table can print out
 * columns either left justified or right justified, with a given number of
 * spaces between columns.
 *
 * @author Joseph Ramsey
 */
public class TextTable {

    /**
     * Set <code>justification</code> to this if the columns should be left
     * justified.
     */
    public static int LEFT_JUSTIFIED = 0;

    /**
     * Set <code>justification</code> to this if the columns should be right
     * justified.
     */
    public static int RIGHT_JUSTIFIED = 1;

    /**
     * The tokens to be printed out.
     */
    private String[][] tokens;

    /**
     * True if columns should be left justified, false if right justified.
     */
    private int justification = RIGHT_JUSTIFIED;

    /**
     * The number of spaces between columns. By default, 2.
     */
    private int columnSpacing = 2;

    /**
     * Construct the text table; the table has a fixed number of rows and
     * columns, each greater than zero.
     */
    public TextTable(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException();
        }

        this.tokens = new String[rows][columns];

        for (int i = 0; i < tokens.length; i++) {
            for (int j = 0; j < tokens[0].length; j++) {
                tokens[i][j] = "";
            }
        }
    }

    /**
     * Sets the token at the given row and column, each of which must be >= 0
     * and less than the number of rows or columns, respectively.
     */
    public void setToken(int row, int column, String token) {
        if (token == null) {
            throw new NullPointerException();
        }

        tokens[row][column] = token;
    }

    /**
     * Returns the token at the given row and column.
     */
    public String getTokenAt(int row, int column) {
        return tokens[row][column];
    }

    /**
     * Returns the number of rows, as set in the constructor.
     */
    public int getNumRows() {
        return tokens.length;
    }

    /**
     * Returns the number of rows, as set in the constructor.
     */
    public int getNumColumns() {
        return tokens[0].length;
    }

    /**
     * Returns the number of spaces between columns, by default 2.
     */
    public int getColumnSpacing() {
        return columnSpacing;
    }

    /**
     * Sets the number of spaces between columns, to some number >= 0.
     */
    public void setColumnSpacing(int numSpaces) {
        if (numSpaces < 0) {
            throw new IllegalArgumentException();
        }

        this.columnSpacing = numSpaces;
    }

    /**
     * Returns the justification, either LEFT_JUSTIFIED or RIGHT_JUSTIFIED.
     */
    public int getJustification() {
        return justification;
    }

    /**
     * Sets the justification, either LEFT_JUSTIFIED or RIGHT_JUSTIFIED.
     */
    public void setJustification(int justification) {
        if (!(justification == LEFT_JUSTIFIED || justification == RIGHT_JUSTIFIED)) {
            throw new IllegalArgumentException();
        }

        this.justification = justification;
    }

    /**
     * Construct the table string and returns it.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        int[] colWidths = new int[tokens[0].length];

        for (int j = 0; j < tokens[0].length; j++) {
            for (String[] token : tokens) {
                if (token[j].length() > colWidths[j]) {
                    colWidths[j] = token[j].length();
                }
            }
        }

        for (String[] token1 : tokens) {
            buffer.append("\n");

            for (int j = 0; j < tokens[0].length; j++) {
                for (int k = 0; k < getColumnSpacing(); k++) {
                    buffer.append(' ');
                }

                int numPaddingSpaces = colWidths[j] - token1[j].length();

                if (getJustification() == RIGHT_JUSTIFIED) {
                    for (int k = 0; k < numPaddingSpaces; k++) {
                        buffer.append(' ');
                    }
                }

                buffer.append(token1[j]);

                if (getJustification() == LEFT_JUSTIFIED) {
                    for (int k = 0; k < numPaddingSpaces; k++) {
                        buffer.append(' ');
                    }
                }
            }
        }

        return buffer.toString();

    }
}
