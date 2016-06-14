package edu.cmu.tetradapp.model.calculator.expression;

import java.util.Collections;
import java.util.List;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
public class VariableExpression implements Expression {


    /**
     * The variable that is being evaluated.
     */
    private String variable;


    public VariableExpression(String variable){
        if(variable == null){
            throw new NullPointerException("variable is null.");
        }
        this.variable = variable;
    }

    //======================== Public methods ===================//

    /**
     * Returns the variable.     
     */
    public String getVariable(){
        return this.variable;
    }


    public Object evaluateGeneric(Context context){
        return context.getValue(variable);
    }


    public double evaluate(Context context) {
        return context.getDoubleValue(variable);
    }


    public List<Expression> getSubExpressions() {
        return Collections.emptyList();
    }
}
