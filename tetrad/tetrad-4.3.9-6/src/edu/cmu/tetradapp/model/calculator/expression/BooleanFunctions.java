package edu.cmu.tetradapp.model.calculator.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of boolean functions.
 *
 * @author Tyler Gibson
 */
class BooleanFunctions {

    /**
     * Singleton instance.
     */
    private final static BooleanFunctions INSTANCE = new BooleanFunctions();


    /**
     * The descriptors.
     */
    private List<ExpressionDescriptor> descriptors = new ArrayList<ExpressionDescriptor>();

    /**
     * Private constructor.
     */
    private BooleanFunctions() {
        descriptors.add(new AndExpressionDescriptor());
        descriptors.add(new OrExpressionDescriptor());
        descriptors.add(new XOrExpressionDescriptor());
    }

    public static BooleanFunctions getInstance(){
        return INSTANCE;
    }


    public List<ExpressionDescriptor> getBooleanExpressionDescriptors() {
        return Collections.unmodifiableList(descriptors);
    }

    //=========================== Inner classes ============================//

    /**
     * For boolean "and". Will return true if all sub-expressions evaluate to a non-zero value and
     * false otherwise.
     */
    private static class AndExpressionDescriptor extends AbstractExpressionDescriptor {


        public AndExpressionDescriptor() {
            super("And", "AND", Position.PREFIX, false, true, "number", "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length < 2) {
                throw new ExpressionInitializationException("Must have at least two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    for (Expression exp : expressions) {
                        if (exp.evaluate(context) == 0.0) {
                            return 0.0;
                        }
                    }
                    return 1.0;
                }
            };
        }
    }


    /**
     * For boolean "Or". Will return 1.0 if at least one of the sub-expressions is non-zero.
     */
    private static class OrExpressionDescriptor extends AbstractExpressionDescriptor {

        public OrExpressionDescriptor() {
            super("Or", "OR", Position.PREFIX, false, true, "number", "number");
        }

        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length < 2) {
                throw new ExpressionInitializationException("Must have at least two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    for (Expression exp : expressions) {
                        if (exp.evaluate(context) != 0.0) {
                            return 1.0;
                        }
                    }
                    return 0.0;
                }
            };
        }
    }

      /**
     * For boolean "Or". Will return 1.0 if at least one of the sub-expressions is non-zero.
     */
    private static class XOrExpressionDescriptor extends AbstractExpressionDescriptor {

        public XOrExpressionDescriptor() {
            super("Exclusive or", "XOR", Position.PREFIX, false, false, "number", "number");
        }

        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 2) {
                throw new ExpressionInitializationException("Must have two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    double first = expressions.get(0).evaluate(context);
                    double second = expressions.get(1).evaluate(context);
                    first = first != 0.0 ? 1.0 : first;
                    second = second != 0.0 ? 1.0 : second;

                    return first == second ? 0.0 : 1.0;
                }
            };
        }
    }


}
