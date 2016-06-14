package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Represents a definition for some expression.
 *
 * @author Tyler Gibson
 */
public interface ExpressionDescriptor {


    public static enum Position {
        INFIX,
        PREFIX,
        BOTH
    }



    /**
     * Returns the name that the expressions is known under.
     */
    public String getName();


    /**
     * Returns the token that represents the expression, such as "+".
     */
    public String getToken();


    /**
     * Returns the signature that should be used.
     */
    public ExpressionSignature getSignature();



    /**
     * Returns the position that the expression can occur in.     
     */
    Position getPosition();



    /**
     * States whether the expression is cummutative, so something like x1 + x2 + x3 is allowed.
     */
    boolean isCommutative();


    /**
     * Creates the actual expression that can be used to evaluate matters from the given
     * expressions.
     */
    public Expression createExpression(Expression ... expressions) throws ExpressionInitializationException;


}
