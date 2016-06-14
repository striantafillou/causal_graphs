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
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;

import java.text.NumberFormat;
import java.util.*;

/**
 * Checks conditional independence of variable in a continuous data set using
 * Fisher's Z test. See Spirtes, Glymour, and Scheines, "Causation, Prediction
 * and Search," 2nd edition, page 94.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 */
public final class IndTestFisherZ implements IndependenceTest {

    /**
     * The covariance matrix.
     */
    private final CovarianceMatrix covMatrix;

    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private List<Node> variables;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;

    /**
     * The cutoff value for 'alpha' area in the two tails of the partial
     * correlation distribution function.
     */
    private double thresh = Double.NaN;

    /**
     * The value of the Fisher's Z statistic associated with the las
     * calculated partial correlation.
     */
    private double fisherZ;

    /**
     * The FisherZD independence test, used when Fisher Z throws an exception
     * (i.e., when there's a collinearity).
     */
    private IndTestFisherZD deterministicTest;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * Stores a reference to the dataset being analyzed.
     */
    private DataSet dataSet;

    /**
     * A stored p value, if the deterministic test was used.     */
    private double pValue = Double.NaN;

    /**
     * If true, then the IndTestFisherZD test is run if determinism is encountered.
     */
    private boolean determinismAllowed = true;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based
     * on the correlation matrix implied by the given data set (must be
     * continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestFisherZ(DataSet dataSet, double alpha) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new CovarianceMatrix(dataSet);

        // This check fails for large data sets with few cases sometimes, even
        // though it's mostly OK to use it anyway. TODO.
        if (!(MatrixUtils.isSymmetricPositiveDefinite(this.covMatrix.getMatrix()))) {
            TetradLogger.getInstance().info("The matrix given to Fisher Z was not positive definite.");
        }

        List<Node> nodes = covMatrix.getVariables();

        this.variables = Collections.unmodifiableList(nodes);
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZD(dataSet, alpha);
        this.dataSet = dataSet;
    }

    public IndTestFisherZ(DoubleMatrix2D data, List<Node> variables, double alpha) {
        DataSet dataSet = ColtDataSet.makeContinuousData(variables, data);
        this.covMatrix = new CovarianceMatrix(dataSet);
        this.variables = Collections.unmodifiableList(variables);
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZD(dataSet, alpha);
    }

    /**
     * Constructs a new independence test that will determine conditional
     * independence facts using the given correlation matrix and the given
     * significance level.
     */
    public IndTestFisherZ(CovarianceMatrix corrMatrix, double alpha) {
        this.covMatrix = corrMatrix;
        this.variables = Collections.unmodifiableList(corrMatrix.getVariables());
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

        CovarianceMatrix newCovMatrix = covMatrix.getSubmatrix(indices);

        double alphaNew = getAlpha();
        return new IndTestFisherZ(newCovMatrix, alphaNew);
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
        DoubleMatrix2D submatrix = DataUtils.subMatrix(covMatrix, x, y, z);

        if (isDeterminismAllowed() && new Algebra().rank(submatrix) != submatrix.rows()) {
            boolean independent = deterministicTest.isIndependent(x, y, z);
            this.pValue = deterministicTest.getPValue();
            return independent;
        }

        double r = StatUtils.partialCorrelation(submatrix);

//        this.fisherZ = 0.5 * Math.sqrt(sampleSize() - z.size() - 3.0) *
//                Math.log(Math.abs(1.0 + r) / Math.abs(1.0 - r));

        this.fisherZ = Math.sqrt(sampleSize() - z.size() - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        if (Double.isNaN(this.fisherZ)) {
            throw new IllegalArgumentException("The Fisher's Z " +
                    "score for independence fact " + x + " _||_ " + y + " | " +
                    z + " is undefined.");
        }

        boolean independent = true;

        if (Double.isNaN(thresh)) {
            this.thresh = cutoffGaussian(getAlpha());
        }

        if (Math.abs(fisherZ) > thresh) {
            independent = false;  //Two sided
        }

        if (independent) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils
                    .independenceFactMsg(x, y, z, getPValue()));
        }

        return independent;
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
        if (!Double.isNaN(this.pValue)) {
            return Double.NaN;
        } else {
            return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));
        }
    }

    /**
     * Sets the significance level at which independence judgments should be
     * made.  Affects the cutoff for partial correlations to be considered
     * statistically equal to zero.
     */
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
        this.thresh = Double.NaN;
    }

    /**
     * Gets the current significance level.
     */
    public double getAlpha() {
        return this.alpha;
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
     * Returns the variable with the given name.
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

    /**
     * Returns the list of variable varNames.
     */
    public List<String> getVariableNames() {
        List<Node> variables = getVariables();
        List<String> variableNames = new ArrayList<String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    /**
     * If <code>isDeterminismAllowed()</code>, deters to IndTestFisherZD; otherwise
     * throws UnsupportedOperationException.
     */
    public boolean determines(List z, Node x) throws UnsupportedOperationException {
        if (determinismAllowed) {
            return deterministicTest.determines(z, x);
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * if <code>isDeterminismAllowed()</code>, defers to IndTestFisherZD; otherwise,
     * throws UnsupportedOperationException.
     */
    public boolean splitDetermines(List z, Node x, Node y) throws UnsupportedOperationException {
        if (determinismAllowed) {
            return deterministicTest.splitDetermines(z, x, y);
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns the data set being analyzed.
     */
    public DataSet getData() {
        return dataSet;
    }

    /**
     * Returns true if determinism is allowed.
     */
    public boolean isDeterminismAllowed() {
        return determinismAllowed;
    }

    /**
     * Sets to true iff determinism is allowed.
     */
    public void setDeterminismAllowed(boolean determinismAllowed) {
        this.determinismAllowed = determinismAllowed;
    }

    public void shuffleVariables() {
        List<Node> nodes = new ArrayList(this.variables);
        Collections.shuffle(nodes);
        this.variables = Collections.unmodifiableList(nodes);
    }

    /**
     * Returns a string representation of this test.
     */
    public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

    /**
     * Computes that value x such that P(abs(N(0,1) > x) < alpha.  Note that
     * this is a two sided test of the null hypothesis that the Fisher's Z
     * value, which is distributed as N(0,1) is not equal to 0.0.
     */
    private double cutoffGaussian(double alpha) {
        double upperTail = 1.0 - alpha / 2.0;
        double epsilon = 1e-14;

        // Find an upper bound.
        double lowerBound = -1.0;
        double upperBound = 0.0;

        while (ProbUtils.normalCdf(upperBound) < upperTail) {
            lowerBound += 1.0;
            upperBound += 1.0;
        }

        while (upperBound >= lowerBound + epsilon) {
            double midPoint = lowerBound + (upperBound - lowerBound) / 2.0;

            if (ProbUtils.normalCdf(midPoint) <= upperTail) {
                lowerBound = midPoint;
            } else {
                upperBound = midPoint;
            }
        }

        return lowerBound;
    }

    private int sampleSize() {
        return covMatrix().getSampleSize();
    }

    private CovarianceMatrix covMatrix() {
        return covMatrix;
    }
}

