package edu.cmu.tetradapp.model.calculator.expression;

import java.util.List;

/**
 * @author Tyler Gibson
 */
public interface Expression {


    /**
     * Evaluates the expression using the given context
     */
    public double evaluate(Context context);


//    /**
//     * Evaluates the expression and returns a generic object representation of the value, can
//     * be used in cases where a double is not retured.
//     */
//    public Object evaluateGeneric(Context context);



    /**
     * Returns the sub expressions of this expression.     
     */
    public List<Expression> getSubExpressions();


}
