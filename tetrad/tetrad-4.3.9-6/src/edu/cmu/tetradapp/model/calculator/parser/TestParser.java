package edu.cmu.tetradapp.model.calculator.parser;

import edu.cmu.tetradapp.model.calculator.expression.ConstantExpression;
import edu.cmu.tetradapp.model.calculator.expression.Context;
import edu.cmu.tetradapp.model.calculator.expression.Expression;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyler Gibson
 */
public final class TestParser extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestParser(String name) {
        super(name);
    }


    /**
     * Tests misc invalid expressions.
     */
    public void testInvalidExpressions() {
        ExpressionParser parser = new ExpressionParser();

        parseInvalid(parser, "(1 + 3))");
        parseInvalid(parser, "1 = 2");
        parseInvalid(parser, "=(3,4)");
        parseInvalid(parser, "(1 + (4 * 5) + sqrt(5)");
        parseInvalid(parser, "1+");
        parseInvalid(parser, "113#");

    }


    public void testParseEquation() {
        ExpressionParser parser = new ExpressionParser(Arrays.asList("x", "y"));
        try {
            parser.parseEquation("x = (1 + y)");
        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }




    /**
     * Tests expressions without variables (mainly used while writing the parser)
     */
    public void testBasicExpressions() {
        ExpressionParser parser = new ExpressionParser();

        Expression expression = parse(parser, "+(1,1)");
        assertTrue(expression.evaluate(new TestingContext()) == 2.0);

        expression = parse(parser, "*(+(1,2), 5)");
        assertTrue(expression.evaluate(new TestingContext()) == 15.0);

        expression = parse(parser, "1 + 2.5");
        assertTrue(expression.evaluate(new TestingContext()) == 3.5);

        expression = parse(parser, "(2 + 3)");
        assertTrue(expression.evaluate(new TestingContext()) == 5.0);

        expression = parse(parser, "1 + (3 + 4)");
        assertTrue(expression.evaluate(new TestingContext()) == 8.0);

        expression = parse(parser, "1 + 2 + 5");
        assertTrue(expression.evaluate(new TestingContext()) == 8.0);

        expression = parse(parser, "1 + (2 * 3)");
        assertTrue(expression.evaluate(new TestingContext()) == 7.0);

        expression = parse(parser, "1 + (2 + (3 * 4))");
        assertTrue(expression.evaluate(new TestingContext()) == 15.0);

        expression = parse(parser, "(2 * 3) + (4 * 5)");
        assertTrue(expression.evaluate(new TestingContext()) == 26.0);

        expression = parse(parser, "((2 * 3) + (1 + (2 + (3 * 4))))");
        assertTrue(expression.evaluate(new TestingContext()) == 21.0);

        expression = parse(parser, "exp(2,3)");
        assertTrue(expression.evaluate(new TestingContext()) == 8.0);

        expression = parse(parser, "sqrt(exp(2,2))");
        assertTrue(expression.evaluate(new TestingContext()) == 2.0);

        expression = parse(parser, ConstantExpression.E.getName());
        assertTrue(expression.evaluate(new TestingContext()) == ConstantExpression.E.evaluate(null));

        expression = parse(parser, ConstantExpression.PI.getName());
        assertTrue(expression.evaluate(new TestingContext()) == ConstantExpression.PI.evaluate(null));

        expression = parse(parser, ConstantExpression.PI.getName() + "+ 2");
        assertTrue(expression.evaluate(new TestingContext()) == Math.PI + 2);

    }


    /**
     * Tests expressions with variables.
     */
    public void testVariables() {
        ExpressionParser parser = new ExpressionParser(Arrays.asList("x", "y", "z"));
        TestingContext context = new TestingContext();

        Expression expression = parse(parser, "x");
        context.assign("x", 5.6);
        assertTrue(expression.evaluate(context) == 5.6);


        expression = parse(parser, "(x + y) * z");
        context.assign("x", 1.0);
        context.assign("y", 2.0);
        context.assign("z", 3.0);
        assertTrue(expression.evaluate(context) == 9.0);


        expression = parse(parser, "3 + (x + (3 * y))");
        context.assign("x", 4.0);
        context.assign("y", 2.0);
        assertTrue(expression.evaluate(context) == 13.0);
    }


    public void testEvaluation(){
        ExpressionParser parser = new ExpressionParser(Arrays.asList("x", "y", "z"));
        TestingContext context = new TestingContext();

//        Expression exp = parse(parser, "y = \"YES\"");
//        context.assign("y", "YES");
//        assertTrue(exp.evaluate(context) == 1.0);
//
//        exp = parse(parser, "10 * (y = \"NO\")");
//        assertTrue(exp.evaluate(context) == 0.0);
    }


    /**
     * Tests that undefined variables aren't allowed.
     */
    public void testVariableRestriction() {
        ExpressionParser parser = new ExpressionParser(Arrays.asList("x", "y", "z"));
        parseInvalid(parser, "x + x1");
    }


    /**
     * Tests commutative operators
     */
    public void testCommutativeOperators() {
        ExpressionParser parser = new ExpressionParser();

        Expression expression = parse(parser, "1 + 2 + 3");
        assertTrue(expression.evaluate(new TestingContext()) == 6.0);

        expression = parse(parser, "1 + 1 + 1 + (3 * 4)");
        assertTrue(expression.evaluate(new TestingContext()) == 15.0);

        expression = parse(parser, "1 * 1 * 2 * 3 * (1 + 1)");
        assertTrue(expression.evaluate(new TestingContext()) == 12.0);
    }


    /**
     * Tests that ambiguous operator exceptions are thrown.
     */
    public void testAmbiguousOperatorOrder() {
        ExpressionParser parser = new ExpressionParser();

        parseInvalid(parser, "1 + 2 * 3");
    }

    //============================== Private Methods ===========================//


    private static void parseInvalid(ExpressionParser parser, String exp) {
        try {
            Expression e = parser.parseExpression(exp);
            fail("Should not have parsed, " + exp + ", but got " + e);
        } catch (ParseException ex) {
            System.out.println("Succussfully raised exception with message: ");
            System.out.println(ex.getMessage());
        }
    }


    /**
     * Tests the expression on the given parser.
     */
    private static Expression parse(ExpressionParser parser, String expression) {
        System.out.println("Parsing string: " + expression);
        try {
            return parser.parseExpression(expression);
        } catch (ParseException ex) {
            int offset = ex.getErrorOffset();
            System.out.println(expression);
            for (int i = 0; i < offset; i++) {
                System.out.print(" ");
            }
            System.out.println("^");
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        return null;
    }


    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestParser.class);
    }

    //====================== Inner class ========================//

    private static class TestingContext implements Context {

        private Map<String, Double> doubleVars = new HashMap<String, Double>();
        private Map<String, Object> vars = new HashMap<String, Object>();

        public void assign(String v, Object o) {
            if (o instanceof Double) {
                doubleVars.put(v, (Double) o);
            } else {
                vars.put(v, o);
            }
        }

        public void clear() {
            this.doubleVars.clear();
            vars.clear();
        }

        public double getDoubleValue(String var) {
            return doubleVars.get(var);
        }

        public Object getValue(String var) {
            return vars.get(var);
        }
    }

}
