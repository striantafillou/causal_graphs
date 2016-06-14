package edu.cmu.tetradapp.model.calculator.expression;

import java.util.Collections;
import java.util.List;

/**
 * Represents a constant expression, that is an expression that always evaluates to the same value.
 *
 * @author Tyler Gibson
 */
public class ConstantExpression implements Expression {


    /**
     * Constant expression for PI.
     */
    public static final ConstantExpression PI = new ConstantExpression(Math.PI, "\u03C0");


    /**
     * Constant expression for e.
     */
    public static final ConstantExpression E = new ConstantExpression(Math.E, "e");

    /**
     * THe value of the expression.
     */
    private double value;


    /**
     * The name of the constant or null if there isn't one.
     */
    private String name;


    /**
     * Constructs the constant expression given the value to use.
     */
    public ConstantExpression(double value) {
        this.value = value;
    }


    /**
     * Constructs the constant expression given the value and the name.
     */
    public ConstantExpression(double value, String name){
        if(name == null){
            throw new NullPointerException("name was null.");
        }
        this.value = value;
        this.name = name;
    }

    //========================== Public Methods ===============================//


    /**
     * Returns the name of the constant or null if there isn't one.
     */
    public String getName(){
        return this.name;
    }

    /**
     * Returns the constant value.
     */
    public double evaluate(Context context) {
        return this.value;
    }


    public List<Expression> getSubExpressions() {
        return Collections.emptyList();
    }
}
