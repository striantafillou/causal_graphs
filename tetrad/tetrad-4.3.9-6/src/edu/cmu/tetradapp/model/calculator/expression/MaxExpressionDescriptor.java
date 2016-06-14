package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
class MaxExpressionDescriptor extends AbstractExpressionDescriptor {


    public MaxExpressionDescriptor() {
        super("Maximum", "max", Position.PREFIX, false, true, "number", "number");
    }


    public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
        if (expressions.length < 2) {
            throw new ExpressionInitializationException("max must have two or more arguments.");
        }
        return new AbstractExpression(expressions) {
            public double evaluate(Context context) {
                double max = expressions.get(0).evaluate(context);
                for (int i = 1; i < expressions.size(); i++) {
                    double d = expressions.get(i).evaluate(context);
                    if(max < d){
                        max = d;
                    }
                }
                return max;
            }
        };
    }
}
