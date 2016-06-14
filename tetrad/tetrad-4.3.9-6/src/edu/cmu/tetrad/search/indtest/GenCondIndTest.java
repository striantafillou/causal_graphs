package edu.cmu.tetrad.search.indtest;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetradapp.editor.ConditionalIndependenceWrapper;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.RegressionParams;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jun 28, 2008
 * Time: 3:40:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenCondIndTest implements IndependenceTest
{
    private ConditionalIndependenceWrapper wrapper;
    private DataSet data;
    private String displayString;
    private double alpha;

    public GenCondIndTest(DataSet data, double alpha)
    {
        //this.wrapper = new ConditionalIndependenceWrapper((DataWrapper)data, params);
        this.data = data;
        this.displayString = "";
        this.alpha = alpha;
    }

    /**
     * Returns an Independence test for a subset of the variables.
     */
    public IndependenceTest indTestSubset(List<Node> vars)
    {

        return null; //stub
    }

    /**
     * Returns true if the given independence question is judged true, false if
     * not. The independence question is of the form x _||_ y | z, z =
     * <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    public boolean isIndependent(Node x, Node y, List<Node> z)
    {
        RegressionParams originalParams = new RegressionParams();
        originalParams.setVarNames(this.data.getVariableNames());
        originalParams.setAlpha(this.alpha);
        originalParams.setTargetName(x.getName());
        String [] regressorArray = new String[z.size()];
        for (int i = 0; i < z.size(); i++)
            regressorArray[i] = (String)z.get(i).getName();
        originalParams.setRegressorNames(regressorArray);

        this.wrapper = new ConditionalIndependenceWrapper(new DataWrapper(data), originalParams);

        RegressionParams newParams = wrapper.execute(y.getName());
        RegressionResult report = wrapper.getResult();
        this.displayString = "";

        if (wrapper.failure)
        {
            throw new IllegalArgumentException("General independence test with discrete variables that are not binary is not\n" +
                    "currently supported! Please use other variables.");
        }
        else
        {
            if (!wrapper.performedSwap)
                displayString += "Testing to see whether the following relation holds:\n " + newParams.getTargetName() + " _||_ " + y.getName() + " | {";
            else
                displayString += "Testing to see whether the following relation holds:\n " + newParams.getTargetName() + " _||_ " + x.getName() + " | {";

            for (int i = 1; i < newParams.getRegressorNames().length; i++)
            {
                displayString += newParams.getRegressorNames()[i];
                if (i != newParams.getRegressorNames().length - 1)
                {
                    displayString += ", ";
               }
            }
            displayString += "}\n\n";

            if (wrapper.linear)
            {
                if (report.getP()[1] > newParams.getAlpha())
                {
                    displayString += "The relation HOLDS.\n " + newParams.getTargetName() + " is conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                    displayString += "Ran the following linear regression to obtain result:\n\n";
                    displayString += report.toString();
                    return true;
                }
                else
                {
                    displayString += "The relation DOES NOT HOLD.\n " + newParams.getTargetName() + " is NOT conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                    displayString += "Ran the following linear regression to obtain result:\n\n";
                    displayString += report.toString();
                    return false;
                }
            }
            else
            {
                if (wrapper.logRegResult.getpValues()[1] > newParams.getAlpha())
                {
                    displayString += "The relation HOLDS.\n " + newParams.getTargetName() + " is conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                    displayString += "Ran the following logistic regression to obtain result:\n\n";
                    displayString += wrapper.logRegResult.getResult().toString();
                    return true;
                }
                else
                {
                    displayString += "The relation DOES NOT HOLD.\n " + newParams.getTargetName() + " is NOT conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                    displayString += "Ran the following logistic regression to obtain result:\n\n";
                    displayString += wrapper.logRegResult.getResult().toString();
                    return false;
                }
            }
        }
    }

    public boolean isIndependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isIndependent(x, y, zList);        
    }

    /**
     * Returns true if the given independence question is judged false, true if
     * not. The independence question is of the form x _||_ y | z, z =
     * <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    public boolean isDependent(Node x, Node y, List<Node> z)
    {
        return !this.isIndependent(x, y, z);
    }

    public boolean isDependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);
    }

    /**
     * Returns the probability associated with the most recently executed
     * independence test, of Double.NaN if p value is not meaningful for tis
     * test.
     */
    public double getPValue()
    {
        return 0; //STUB
    }

    /**
     * Returns the list of variables over which this independence checker is
     * capable of determinining independence relations.
     */
    public List<Node> getVariables()
    {
        return null; //STUB
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
     * Returns true if y is determined the variable in z.
     */
    public boolean determines(List<Node> z, Node y)
    {
        return false; //stub
    }

    /**
     * Returns true if x or y is determined by the variables in z.
     */
    public boolean splitDetermines(List<Node> z, Node x, Node y)
    {
        return false; //stub
    }

    /**
     * Returns the significance level of the independence test.
     * @throws UnsupportedOperationException if there is no significance level.
     */
    public double getAlpha()
    {
        return 0; //STUB 
    }

    /**
     * Sets the significance level.
     */
    public void setAlpha(double alpha)
    {
        //this.alpha = alpha;
    }

    public DataSet getData()
    {
        //return this.data;
        return null; //STUB
    }

    public String toString()
    {
        return this.displayString;
    }

    public ConditionalIndependenceWrapper getWrapper()
    {
        return this.wrapper;
    }
}
