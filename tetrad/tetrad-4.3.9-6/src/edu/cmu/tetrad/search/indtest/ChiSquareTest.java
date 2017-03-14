///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search.indtest;

import edu.cmu.tetrad.data.CellTable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.util.CombinationIterator;
import edu.cmu.tetrad.util.ProbUtils;

import java.util.Arrays;

/**
 * Calculates marginal chi square test results for a discrete dataset.
 *
 * @author Frank Wimberly original version
 * @author Joseph Ramsey revision 10/01
 */
public final class ChiSquareTest {

    /**
     * The data set this test uses.
     */
    private DataSet dataSet;

    /**
     * The number of values for each variable in the data.
     */
    private int[] dims;

    /**
     * Stores the data in the form of a cell table.
     */
    private CellTable cellTable;

    /**
     * The significance level of the test.
     */
    private double alpha;

    //==============================CONSTRUCTORS=========================//

    /**
     * Constructs a test using the given data set and significance level.
     * @param dataSet A data set consisting entirely of discrete variables.
     * @param alpha The significance level, usually 0.05.
     */
    public ChiSquareTest(DataSet dataSet, double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance level must be in " +
                    "[0, 1]: " + alpha);
        }

        this.dims = new int[dataSet.getNumColumns()];

        for (int i = 0; i < dims.length; i++) {
            DiscreteVariable variable =
                    (DiscreteVariable) dataSet.getVariable(i);
            this.dims[i] = variable.getNumCategories();
        }

        this.dataSet = dataSet;
        this.alpha = alpha;
        this.cellTable = new CellTable(null);
        this.cellTable.setMissingValue(DiscreteVariable.MISSING_VALUE);
    }

    /**
     * Calculates chi square for a conditional crosstabulation table for
     * independence question 0 _||_ 1 | 2, 3, ...max by summing up chi square
     * and degrees of freedom for each conditional table in turn, where rows or
     * columns that consist entirely of zeros have been removed.
     */
    public ChiSquareTest.Result calcChiSquare(int[] testIndices) {

        // Reset the cell table for the columns referred to in
        // 'testIndices.' Do cell counts for those columns.
        this.cellTable.addToTable(dataSet, testIndices);

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        int[] firstVar = new int[]{0};
        int[] secondVar = new int[]{1};
        int[] bothVars = new int[]{0, 1};

        double xSquare = 0.0;
        int df = 0;

        int[] condDims = new int[testIndices.length - 2];
        System.arraycopy(selectFromArray(dims, testIndices), 2, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numRows = this.cellTable.getNumValues(0);
        int numCols = this.cellTable.getNumValues(1);

        boolean[] attestedRows = new boolean[numRows];
        boolean[] attestedCols = new boolean[numCols];

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();

            System.arraycopy(combination, 0, coords, 2, combination.length);
            Arrays.fill(attestedRows, true);
            Arrays.fill(attestedCols, true);

            long total = this.cellTable.calcMargin(coords, bothVars);

            if (total == 0) {
                continue;
            }

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    coords[0] = i;
                    coords[1] = j;

                    long sumRow = this.cellTable.calcMargin(coords, secondVar);
                    long sumCol = this.cellTable.calcMargin(coords, firstVar);
                    long observed = (int) this.cellTable.getValue(coords);

                    boolean skip = false;

                    if (sumRow == 0) {
                        attestedRows[i] = false;
                        skip = true;
                    }

                    if (sumCol == 0) {
                        attestedCols[j] = false;
                        skip = true;
                    }

                    if (skip) {
                        continue;
                    }

                    double expected =
                            (double) (sumCol * sumRow) / (double) total;
                    xSquare += Math.pow(observed - expected, 2.0) / expected;
                }
            }

            int numAttestedRows = 0;
            int numAttestedCols = 0;

            for (boolean attestedRow : attestedRows) {
                if (attestedRow) {
                    numAttestedRows++;
                }
            }

            for (boolean attestedCol : attestedCols) {
                if (attestedCol) {
                    numAttestedCols++;
                }
            }

            df += (numAttestedRows - 1) * (numAttestedCols - 1);
        }

        // If df == 0, return indep.
        if (df == 0) {
            df = 1;
        }

        double pValue = 1.0 - ProbUtils.chisqCdf(xSquare, df);
        boolean indep = (pValue > this.alpha);
        return new ChiSquareTest.Result(xSquare, pValue, df, indep);
    }

    /**
     * Returns true if the variable at index 0 is determined by the variables
     * at the other indices at the given probability p.
     * @param testIndices An array of indices for variables in the dataset
     * supplied in the constructor.
     * @param p The probability that some marginal for some table dominates.
     * A good value is 0.99.
     * @return True if the variable at index 0 is determined by the variables
     * at the other indices.
     */
    public boolean isDetermined(int[] testIndices, double p) {

        // Reset the cell table for the columns referred to in
        // 'testIndices.' Do cell counts for those columns.
        this.cellTable.addToTable(dataSet, testIndices);

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        int[] firstVar = new int[]{0};

        int[] condDims = new int[testIndices.length - 1];
        System.arraycopy(selectFromArray(dims, testIndices), 1, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numValues = this.cellTable.getNumValues(0);

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();
            System.arraycopy(combination, 0, coords, 1, combination.length);

            long total = this.cellTable.calcMargin(coords, firstVar);

            if (total == 0) {
                continue;
            }

            boolean dominates = false;

            for (int i = 0; i < numValues; i++) {
                coords[0] = i;

                long numi = this.cellTable.getValue(coords);

                if ((double) numi / total >= p) {
                    dominates = true;
                }
            }

            if (!dominates) {
                return false;
            }
        }
                                                
        return true;
    }

    /**
     * Returns true if the variables at index 0 and 1 are each determined by
     * the variables at the other indices at the given probability p.
     * @param testIndices An array of indices for variables in the dataset
     * supplied in the constructor.
     * @param p The probability that some marginal for some table dominates.
     * A good value is 0.99.
     * @return True if the variables at index 0 and 1 are each determined by
     * the variables at the other indices.
     */
    public boolean isSplitDetermined(int[] testIndices, double p) {

        // Reset the cell table for the columns referred to in
        // 'testIndices.' Do cell counts for those columns.
        this.cellTable.addToTable(dataSet, testIndices);

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        int[] firstVar = new int[]{0};
        int[] secondVar = new int[]{1};
        int[] bothVars = new int[]{0, 1};

        int[] condDims = new int[testIndices.length - 2];
        System.arraycopy(selectFromArray(dims, testIndices), 2, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numRows = this.cellTable.getNumValues(0);
        int numCols = this.cellTable.getNumValues(1);

        boolean[] attestedRows = new boolean[numRows];
        boolean[] attestedCols = new boolean[numCols];

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();

            System.arraycopy(combination, 0, coords, 2, combination.length);
            Arrays.fill(attestedRows, true);
            Arrays.fill(attestedCols, true);

            long total = this.cellTable.calcMargin(coords, bothVars);

            if (total == 0) {
                continue;
            }

            // For every table, some marginal has to dominate, either a row
            // marginal or a column marginal.
            boolean dominates = false;

            marginals:
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    coords[0] = i;
                    coords[1] = j;

                    long sumRow = this.cellTable.calcMargin(coords, secondVar);
                    long sumCol = this.cellTable.calcMargin(coords, firstVar);

                    if ((double) sumRow / total >= p) {
                        dominates = true;
                        break marginals;
                    }

                    if ((double) sumCol / total >= p) {
                        dominates = true;
                        break marginals;
                    }
                }
            }

            if (!dominates) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the current significance level being used for tests.
     */
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Sets the significance level to be used for tests.
     */
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance level must be in " +
                    "[0, 1]: " + alpha);
        }

        this.alpha = alpha;
    }

    //================================PRIVATE==============================//

    private int[] selectFromArray(int[] arr, int[] indices) {
        int[] retArr = new int[indices.length];

        for (int i = 0; i < indices.length; i++) {
            retArr[i] = arr[indices[i]];
        }

        return retArr;
    }

    //===============================CLASSES==============================//

    /**
     * Simple class to store the parameters of the result returned by the G
     * Square test.
     *
     * @author Frank Wimberly
     */
    public static class Result {

        /**
         * The chi square value.
         */
        private double chiSquare;

        /**
         * The pValue of the result.
         */
        private double pValue;

        /**
         * The adjusted degrees of freedom.
         */
        private int df;

        /**
         * Whether the conditional independence holds or not. (True if it does,
         * false if it doesn't.
         */
        private boolean isIndep;

        /**
         * Constructs a new g square result using the given parameters.
         */
        public Result(double chiSquare, double pValue, int df, boolean isIndep) {
            this.chiSquare = chiSquare;
            this.pValue = pValue;
            this.df = df;
            this.isIndep = isIndep;
        }

        public double getXSquare() {
            return chiSquare;
        }

        public double getPValue() {
            return pValue;
        }

        public int getDf() {
            return df;
        }

        public boolean isIndep() {
            return isIndep;
        }
    }
}

