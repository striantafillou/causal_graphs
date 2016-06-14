package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Represents the signature of the expression, for example sqrt(number).
 *
 * @author Tyler Gibson
 */
public interface ExpressionSignature {


    /**
     * Returns the sigature as a string.
     */
    public String getSignature();


    /**
     * Returns the name of the function.
     */
    public String getFunctionName();


    /**
     * Returns the number of arguments.
     */
    public int getNumberOfArguments();


    /**
     * States whether an unlimited number of arguments is allowed.     
     */
    public boolean isUnlimited();


    /**
     * Returns the argument type at the given index.
     */
    public String getArgument(int index);

}
