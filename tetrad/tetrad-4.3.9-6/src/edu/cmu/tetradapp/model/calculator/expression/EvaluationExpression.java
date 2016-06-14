package edu.cmu.tetradapp.model.calculator.expression;

import java.util.Collections;
import java.util.List;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
public class EvaluationExpression implements Expression {

    /**
     * The variable part of the expression.
     */
    private VariableExpression variable;


    /**
     * The string you are testing the variable against.
     */
    private String string;


    public EvaluationExpression(VariableExpression exp, String s){
        if(exp == null){
            throw new NullPointerException("Variable must not be null.");
        }
        if(s == null){
            throw new NullPointerException("String must not be null.");
        }
        this.variable = exp;
        this.string = s;
    }


    public double evaluate(Context context) {
        Object o = variable.evaluateGeneric(context);
        if(o != null && string.equals(o.toString())){
            return 1.0;
        }
        return 0.0;
    }

    public List<Expression> getSubExpressions() {
        return Collections.singletonList((Expression)variable);
    }
}
