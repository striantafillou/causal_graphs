package edu.cmu.tetradapp.model.calculator.expression;

import java.util.*;

/**
 * Manager for expressions, this includes all implemenets of expression descriptors for the calculator.
 *
 * @author Tyler Gibson
 */
public class ExpressionManager {


    /**
     * A mapping from tokens to their descriptors.
     */
    private Map<String, ExpressionDescriptor> tokenMap = new HashMap<String, ExpressionDescriptor>();


    /**
     * List of all the descriptors.
     */
    private List<ExpressionDescriptor> descriptors;


    /**
     * Singleton instance.
     */
    private final static ExpressionManager INSTANCE = new ExpressionManager();


    private ExpressionManager() {
        this.descriptors = new ArrayList<ExpressionDescriptor>(listDescriptors());
        for (ExpressionDescriptor exp : this.descriptors) {
            if (this.tokenMap.containsKey(exp.getToken())) {
                throw new IllegalStateException("Expression descriptors must have unique tokens, but " + exp.getToken()
                        + " is not unique.");
            }
            this.tokenMap.put(exp.getToken(), exp);
        }
    }

    //===================================== Public Methods ====================================//


    /**
     * Returns an instanceo of the manager.
     */
    public static ExpressionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a list of all the descriptions.
     */
    public List<ExpressionDescriptor> getDescriptors() {
        return Collections.unmodifiableList(this.descriptors);
    }

    /**
     * Returns the descriptor to use for the given token.
     */
    public ExpressionDescriptor getDescriptorFromToken(String token) {
        return this.tokenMap.get(token);
    }                    

    //======================================= Private methods ===============================//


    /**
     * Builds all the descriptors.
     */
    private static List<ExpressionDescriptor> listDescriptors() {
        List<ExpressionDescriptor> descriptor = new LinkedList<ExpressionDescriptor>();
        descriptor.add(new AdditionExpressionDescriptor());
        descriptor.add(new SubtractionExpressionDescriptor());
        descriptor.add(new MultiplicationExpressionDescriptor());
        descriptor.add(new DivisionExpressionDescriptor());
        descriptor.add(new ExponentExpressionDescriptor());
        descriptor.add(new SquareRootExpressionDescriptor());
        descriptor.add(new CosExpressionDescriptor());
        descriptor.add(new SineExpressionDescriptor());
        descriptor.add(new TangentExpressionDescriptor());
        descriptor.add(new NaturalLogExpressionDescriptor());
        descriptor.add(new Log10ExpressionDescriptor());
        descriptor.add(new RoundExpressionDescriptor());
        descriptor.add(new CeilExpressionDescriptor());
        descriptor.add(new FloorExpressionDescriptor());
        descriptor.add(new AbsoluteValueExpressionDescriptor());
        descriptor.add(new RandomExpressionDescriptor());
        descriptor.add(new MaxExpressionDescriptor());
        descriptor.add(new MinExpressionDescriptor());

        descriptor.addAll(BooleanFunctions.getInstance().getBooleanExpressionDescriptors());

//        Collections.sort(descriptor, new Comp());
        return descriptor;
    }

    //================================ Inner class ==============================//

    private static class Comp implements Comparator<ExpressionDescriptor> {

        public int compare(ExpressionDescriptor o1, ExpressionDescriptor o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

    /**
     * Addition
     */
    private static class AdditionExpressionDescriptor extends AbstractExpressionDescriptor {


        public AdditionExpressionDescriptor() {
            super("Addition", "+", Position.BOTH, true, true);
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length < 2) {
                throw new ExpressionInitializationException("Must have at least two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    double value = 0.0;
                    for (Expression exp : expressions) {
                        value += exp.evaluate(context);
                    }
                    return value;
                }
            };
        }
    }


     /**
     * Addition
     */
    private static class SubtractionExpressionDescriptor extends AbstractExpressionDescriptor {


        public SubtractionExpressionDescriptor() {
            super("Subtraction", "-", Position.BOTH, false, false);
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length  != 2) {
                throw new ExpressionInitializationException("Must two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return expressions.get(0).evaluate(context) - expressions.get(1).evaluate(context);
                }
            };
        }
    }


    /**
     * Ceil
     */
    private static class CeilExpressionDescriptor extends AbstractExpressionDescriptor {


        public CeilExpressionDescriptor() {
            super("Ceil", "ceil", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Ceil must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.ceil(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    /**
     * Cosine
     */
    private static class CosExpressionDescriptor extends AbstractExpressionDescriptor {


        public CosExpressionDescriptor() {
            super("Cosine", "cos", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Cos must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.cos(expressions.get(0).evaluate(context));
                }
            };
        }
    }


    /**
     * Flooor.
     */
    private static class FloorExpressionDescriptor extends AbstractExpressionDescriptor {


        public FloorExpressionDescriptor() {
            super("Floor", "floor", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Floor must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.floor(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    /**
     * Flooor.
     */
    private static class AbsoluteValueExpressionDescriptor extends AbstractExpressionDescriptor {


        public AbsoluteValueExpressionDescriptor() {
            super("Abs", "abs", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Floor must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.abs(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    private static class Log10ExpressionDescriptor extends AbstractExpressionDescriptor {


        public Log10ExpressionDescriptor() {
            super("Log base 10", "log10", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Log10 must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.log10(expressions.get(0).evaluate(context));
                }
            };
        }
    }


    /**
     * Multiplication.
     */
    private static class MultiplicationExpressionDescriptor extends AbstractExpressionDescriptor {


        public MultiplicationExpressionDescriptor() {
            super("Multiplication", "*", Position.BOTH, true, true);
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length < 2) {
                throw new ExpressionInitializationException("Must have at least two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    double value = 1.0;
                    for (Expression exp : expressions) {
                        value = value * exp.evaluate(context);
                    }
                    return value;
                }
            };
        }

    }

    /**
     * Multiplication.
     */
    private static class DivisionExpressionDescriptor extends AbstractExpressionDescriptor {


        public DivisionExpressionDescriptor() {
            super("Division", "/", Position.BOTH, true, true);
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 2) {
                throw new ExpressionInitializationException("Must have two arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return expressions.get(0).evaluate(context)
                            / expressions.get(1).evaluate(context);
                }
            };
        }

    }

    /**
     * Natural log.
     */
    private static class NaturalLogExpressionDescriptor extends AbstractExpressionDescriptor {


        public NaturalLogExpressionDescriptor() {
            super("Log base e", "ln", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("log must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.log(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    /**
     * Random value.
     */
    private static class RandomExpressionDescriptor extends AbstractExpressionDescriptor {


        public RandomExpressionDescriptor() {
            super("Random", "random", Position.PREFIX, false, false);
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 0) {
                throw new ExpressionInitializationException("Random must have no arguments.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.random();
                }
            };
        }
    }

    /**
     * Round expression.
     */
    private static class RoundExpressionDescriptor extends AbstractExpressionDescriptor {


        public RoundExpressionDescriptor() {
            super("Round", "round", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Round must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.round(expressions.get(0).evaluate(context));
                }
            };
        }
    }


    /**
     * Tangent expression.
     */
    private static class TangentExpressionDescriptor extends AbstractExpressionDescriptor {


        public TangentExpressionDescriptor() {
            super("Tangent", "tan", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Tangent must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.tan(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    /**
     * Square Root.
     */
    private static class SquareRootExpressionDescriptor extends AbstractExpressionDescriptor {


        public SquareRootExpressionDescriptor() {
            super("Square Root", "sqrt", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Square Root must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.sqrt(expressions.get(0).evaluate(context));
                }
            };
        }
    }

    /**
     * Sine expression.
     */
    private static class SineExpressionDescriptor extends AbstractExpressionDescriptor {


        public SineExpressionDescriptor() {
            super("Sine", "sin", Position.PREFIX, false, false, "number");
        }


        public Expression createExpression(final Expression... expressions) throws ExpressionInitializationException {
            if (expressions.length != 1) {
                throw new ExpressionInitializationException("Sine must have one and only one" +
                        " argument.");
            }
            return new AbstractExpression(expressions) {
                public double evaluate(Context context) {
                    return StrictMath.sin(expressions.get(0).evaluate(context));
                }
            };
        }
    }


}