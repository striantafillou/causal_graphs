package edu.cmu.tetrad.manip.experimental_setup;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Oct 7, 2004
 * Time: 5:01:34 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariablesStudied {

    /**
     * Returns true if the variable is studied in the experimental setup.
     * @param variableName the name of the variable.
     * @return true if variableName is studied.
     * @throws IllegalArgumentException
     */
    public boolean isVariableStudied(String variableName) throws IllegalArgumentException;

    /**
     * Returns # variables studied.
     */
    public int getNumVariablesStudied();

    /**
     * Returns names of variables studied.
     */
    public String [] getNamesOfStudiedVariables();

}
