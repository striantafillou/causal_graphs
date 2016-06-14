package edu.cmu.tetrad.search.indtest;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.ResolveSepsets;
import edu.cmu.tetrad.util.NumberFormatUtil;

import java.text.NumberFormat;
import java.util.*;

/**
 * Pools together a set of independence tests using a specified methods
 *
 * @author Robert Tillman
 */
public final class IndTestMulti implements IndependenceTest {

    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private final List<Node> variables;

    /**
     * The independence test associated with each data set.
     */
    private List<IndependenceTest> independenceTests;

    /**
     * Pooling method
     */
    private String method;

    //==========================CONSTRUCTORS=============================//

    public IndTestMulti(List<IndependenceTest> independenceTests, String method) {
        Set<String> nodeNames = new HashSet<String>();
        for (IndependenceTest independenceTest :independenceTests) {
            nodeNames.addAll(independenceTest.getVariableNames());
        }
        if (independenceTests.get(0).getVariables().size()!=nodeNames.size()) {
            throw new IllegalArgumentException("Data sets must have same variables.");
        }
        this.variables = independenceTests.get(0).getVariables();
        this.independenceTests = independenceTests;
        this.method = method;
    }

    //==========================PUBLIC METHODS=============================//

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
        return ResolveSepsets.isIndependentPooled(method, independenceTests, x, y, z);
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
        throw new UnsupportedOperationException();
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
        return "Pooled Independence Test:  " + independenceTests;
    }
}