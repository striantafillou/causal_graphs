package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
public interface Context {


    /**
     * Returns the double value for the given var.
     */
    double getDoubleValue(String var);


    /**
     * Returns the value as a generic object.
     */
    Object getValue(String var);


}
