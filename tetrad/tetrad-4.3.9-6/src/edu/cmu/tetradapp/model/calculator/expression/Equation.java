package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Represents an equation of the form Variable = Expression, where the Variable represents
 * a column in some dataset.
 *
 * @author Tyler Gibson
 */
public class Equation {

    /**
     * The string value of the variable.
     */
    private String variable;


    /**
     * The expression that should be used to evaluate the variables new value.
     */
    private Expression expression;


    /**
     * The unparsed expression.
     */
    private String unparsedExpression;


    public Equation(String variable, Expression expression, String unparsed){
        if(variable == null){
            throw new NullPointerException("variable was null.");
        }
        if(expression == null){
            throw new NullPointerException("expression was null.");
        }
        if(unparsed == null){
            throw new NullPointerException("unparsed was null.");
        }
        this.unparsedExpression = unparsed;
        this.variable = variable;
        this.expression = expression;
    }

    //========================== Public Methods ======================//


    public String getUnparsedExpression(){
        return this.unparsedExpression;
    }


    public String getVariable(){
        return this.variable;
    }


    public Expression getExpression(){
        return this.expression;
    }

}
