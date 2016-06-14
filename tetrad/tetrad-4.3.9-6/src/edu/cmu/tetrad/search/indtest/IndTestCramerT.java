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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

/**
 * Checks conditional independence for continuous variables using Cramer's
 * T-test formula (Cramer, Mathematical Methods of Statistics (1951), page
 * 413).
 *
 * @author Joseph Ramsey
 */
public final class IndTestCramerT implements IndependenceTest {

    /**
     * The data set over which conditional independence judgements are being
     * formed.
     */
    private DataSet dataSet;

    /**
     * The correlation matrix.
     */
    private final CorrelationMatrix corrMatrix;

    /**
     * The variables of the correlation matrix, in order. (Unmodifiable list.)
     */
    private final List<Node> variables;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;

    /**
     * The last used partial correlation distribution function.
     */
    private PartialCorrelationPdf pdf;

    /**
     * The cutoff value for 'alpha' area in the two tails of the partial
     * correlation distribution function.
     */
    private double cutoff = 0.;

    /**
     * The last calculated partial correlation, needed to calculate relative
     * strength.
     */
    private double storedR = 0.;

    /**
     * Formats as 0.0000.
     */
    private static final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based
     * on the correlation matrix implied by the given data set (must be
     * continuous). The given significance level is used.
     *
     * @param dataSet A data set with all continuous columns.
     * @param alpha   the alpha level of the test.
     */
    public IndTestCramerT(DataSet dataSet, double alpha) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.dataSet = dataSet;
        this.corrMatrix = new CorrelationMatrix(dataSet);
        this.variables =
                Collections.unmodifiableList(corrMatrix.getVariables());
        setAlpha(alpha);
    }

    /**
     * Constructs a new independence test that will determine conditional
     * independence facts using the given correlation matrix and the given
     * significance level.
     */
    public IndTestCramerT(CorrelationMatrix corrMatrix, double alpha) {
        this.corrMatrix = corrMatrix;
        this.variables =
                Collections.unmodifiableList(corrMatrix.getVariables());
        setAlpha(alpha);
    }


    /**
     * Constructs a new independence test that will determine conditional
     * independence facts using the given correlation matrix and the given
     * significance level.
     */
    public IndTestCramerT(CovarianceMatrix covMatrix, double alpha) {
        CorrelationMatrix corrMatrix = new CorrelationMatrix(covMatrix);
        this.variables =
                Collections.unmodifiableList(corrMatrix.getVariables());
        this.corrMatrix = corrMatrix;
        setAlpha(alpha);
    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    public IndependenceTest indTestSubset(List<Node> vars) {
        if (vars.isEmpty()) {
            throw new IllegalArgumentException("Subset may not be empty.");
        }

        for (Node var : vars) {
            if (!variables.contains(var)) {
                throw new IllegalArgumentException(
                        "All vars must be original vars");
            }
        }

        int[] indices = new int[vars.size()];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = variables.indexOf(vars.get(i));
        }

        CorrelationMatrix newCorrMatrix = corrMatrix.getSubCorrMatrix(indices);

        double alphaNew = getAlpha();
        return new IndTestCramerT(newCorrMatrix, alphaNew);
    }

    /**
     * Determines whether variable x is independent of variable y given a list
     * of conditioning variables z.
     *
     * @param x the one variable being compared.
     * @param y the second variable being compared.
     * @param z the list of conditioning variables.
     * @return true iff x _||_ y | z.
     * @throws RuntimeException if a matrix singularity is encountered.
     */
    public boolean isIndependent(Node x, Node y, List<Node> z) {
        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        // Precondition: this.variables, this.corrMatrix properly set up.
        //
        // Postcondition: this.storedR and this.func should be the
        // most recently calculated partial correlation and
        // partial correlation distribution function, respectively.
        //
        // PROCEDURE:
        // calculate the partial correlation of x and y given z
        // by finding the submatrix of variables x, y, z1...zn,
        // inverting it, and examining the value at position (0, 1)
        // in the inverted matrix.  the partial correlation is
        // -1 * this value / the square root of the outerProduct of the
        // diagonal elements on the same row and column as this
        // value.
        //
        // Design consideration:
        // Minimize object creation by reusing submatrix and condition
        // arrays and inverting submatrix in place.

        // Create index array for the given variables.
        int size = z.size() + 2;
        int[] indices = new int[size];

        indices[0] = getVariables().indexOf(x);
        indices[1] = getVariables().indexOf(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = getVariables().indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix =
                covMatrix().getMatrix().viewSelection(indices, indices);

        // Check for missing values.
        if (DataUtils.containsMissingValue(submatrix)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        // Invert submatrix.
        if (new Algebra().rank(submatrix) != submatrix.rows()) {
            throw new IllegalArgumentException(
                    "Matrix singularity detected while using correlations " +
                            "\nto check for independence; probably due to collinearity " +
                            "\nin the data. The independence fact being checked was " +
                            "\n" + x + " _||_ " + y + " | " + z + ".");
        }

        submatrix = MatrixUtils.inverseC(submatrix);

        double a = -1.0 * submatrix.get(0, 1);
        double b = Math.sqrt(submatrix.get(0, 0) * submatrix.get(1, 1));
        this.storedR = a / b; // Store R so P value can be calculated.

        // Determine whether this partial correlation is statistically
        // nondifferent from zero.
        boolean indep = isZero(this.storedR, size, getAlpha());

        if (indep) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(x, y, z, getPValue()));
        }

        return indep;
    }

    public boolean isIndependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isIndependent(x, y, zList);        
    }

    public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    public boolean isDependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);                
    }

    /**
     * Returns the probability associated with the most recently computed
     * independence test.
     */
    public double getPValue() {
        return 2.0 * Integrator.getArea(pdf(), Math.abs(storedR), 1.0, 100);
    }

    /**
     * Sets the significance level for future tests.
     */
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
    }

    /**
     * Returns the current significance level.
     */
    public double getAlpha() {
        return this.alpha;
    }

    private CovarianceMatrix covMatrix() {
        return corrMatrix;
    }

    /**
     * Returns the list of variables over which this independence checker is
     * capable of determinine independence relations-- that is, all the
     * variables in the given graph or the given data set.
     */
    public List<Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable with the given name, or null if there is no
     * such variable.
     */
    public Node getVariable(String name) {
        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    public boolean determines(List z, Node x1) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean splitDetermines(List z, Node x, Node y)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public DataSet getData() {
        return dataSet;
    }

    /**
     * Returns the list of variable names
     */
    public List<String> getVariableNames() {
        List<Node> variables = getVariables();
        List<String> variableNames = new ArrayList<String>();

        for (Node variable : variables) {
            variableNames.add(variable.getName());
        }

        return variableNames;
    }

    /**
     * Returns a string representation of this test.
     */
    public String toString() {
        return "Partial Correlation T Test, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

    /**
     * Tests whether the given correlation is statistically non-different from
     * zero by determining whether |storedR| <= the cutoff calculated by placing
     * an area equal to the significance level in the two tails of the relevant
     * partial correlation distribution function.
     *
     * @param r     the sample correlation.
     * @param k     the number of compared variables.
     * @param alpha the alpha level.
     * @return true if the sample correlation is statically non-different from
     *         zero, false if not.
     */
    private boolean isZero(double r, int k, double alpha) {
        if (pdf() == null || pdf().getK() != k) {
            this.cutoff = cutoff(k, alpha);
            System.out.println("cutoff = " + cutoff);
        }
        return Math.abs(r) <= this.cutoff;
    }

    private double cutoff(int k, double alpha) {
        this.pdf = new PartialCorrelationPdf(sampleSize() - 1, k);
        final double upperBound = 1.0;
        final double delta = 0.00001;
        return CutoffFinder.getCutoff(pdf(), upperBound, alpha, delta);
    }

    private int sampleSize() {
        return corrMatrix().getSampleSize();
    }

    private CorrelationMatrix corrMatrix() {
        return corrMatrix;
    }

    private PartialCorrelationPdf pdf() {
        return pdf;
    }
}


