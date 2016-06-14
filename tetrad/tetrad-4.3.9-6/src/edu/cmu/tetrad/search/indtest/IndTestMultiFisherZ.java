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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.ResolveSepsets;
import edu.cmu.tetrad.util.NumberFormatUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks conditional independence of variable in a continuous data set using
 * Fisher's Z test. See Spirtes, Glymour, and Scheines, "Causation, Prediction
 * and Search," 2nd edition, page 94.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 */
public final class IndTestMultiFisherZ implements IndependenceTest {

    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private final List<Node> variables;

    /**
     * The independence test associated with each data set.
     */
    private List<IndependenceTest> independenceTests;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    //==========================CONSTRUCTORS=============================//

    public IndTestMultiFisherZ(List<DataSet> dataSets, double alpha) {
        for (int i = 1; i < dataSets.size(); i++) {
            if (!dataSets.get(i).isContinuous()) {
                throw new IllegalArgumentException("Continuous data sets only.");
            }

            if (!dataSets.get(i).getVariables().equals(dataSets.get(0).getVariables())) {
                throw new IllegalArgumentException("Data sets must have same variables.");
            }
        }

        List<IndependenceTest> independenceTests = new ArrayList<IndependenceTest>();

        for (DataSet dataSet : dataSets) {
            independenceTests.add(new IndTestFisherZ(dataSet, alpha));
        }

        this.variables = dataSets.get(0).getVariables();
        this.independenceTests = independenceTests;
    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    public IndependenceTest indTestSubset(List<Node> vars) {
        throw new UnsupportedOperationException();
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
        return ResolveSepsets.isIndependentPooledFisher(independenceTests, x, y, z);
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
     * @throws UnsupportedOperationException
     */
    public double getPValue() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setAlpha(double alpha) {
        throw new UnsupportedOperationException();
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
     * @throws UnsupportedOperationException
     */
    public boolean determines(List z, Node x) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean splitDetermines(List z, Node x, Node y) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throw UnsupportedOperationException
     */
    public DataSet getData() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if determinism is allowed.
     */
    public boolean isDeterminismAllowed() {
        return false;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setDeterminismAllowed(boolean determinismAllowed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation of this test.
     */
    public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }
}