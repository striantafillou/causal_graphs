package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
class MinExpressionDescriptor extends AbstractExpressionDescriptor {


    public MinExpressionDescriptor() {
        super("Minimum", "min", Position.PREFIX, false, true, "number", "number");
    }


    public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
        if (expressions.length < 2) {
            throw new ExpressionInitializationException("min must have two or more arguments.");
        }
        return new AbstractExpression(expressions) {
            public double evaluate(Context context) {
                double min = expressions.get(0).evaluate(context);
                for (int i = 1; i < expressions.size(); i++) {
                    double d = expressions.get(i).evaluate(context);
                    if(d < min){
                        min = d;
                    }
                }
                return min;
            }
        };
    }
}
