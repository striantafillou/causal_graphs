package edu.cmu.tetradapp.model.calculator.parser;

import edu.cmu.tetradapp.model.calculator.expression.*;

import java.text.ParseException;
import java.util.*;

/**
 * Parses a string into a tree-like expression.
 *
 * @author Tyler Gibson
 */
public class ExpressionParser {


    /**
     * The current token.
     */
    private Token token;


    /**
     * The lexer.
     */
    private ExpressionLexer lexer;


    /**
     * The expressin manager used to get the actual expressions from.
     */
    private ExpressionManager expressions = ExpressionManager.getInstance();


    /**
     * The the variables that are allowed in an expression.
     */
    private Set<String> variables;


    /**
     * Constructrs a parser that has no allowable variables.
     */
    public ExpressionParser() {
        this.variables = Collections.emptySet();
    }


    /**
     * Constructs the parser given a collection of allowable variables.
     */
    public ExpressionParser(Collection<String> variables) {
        if (variables == null) {
            throw new NullPointerException("variables null.");
        }

        if (variables.contains("$")) {
            throw new IllegalArgumentException("Variable list must not " +
                    "contain the wildcard '$'.");
        }

        this.variables = new HashSet<String>(variables);
        this.variables.add("$");
    }

    //================================ Public methods ===================================//


    /**
     * Parses the given expression, or throws an exception if its not possible.
     */
    public Expression parseExpression(String expression) throws ParseException {
        System.out.println(expression);
        this.lexer = new ExpressionLexer(expression);
        readToken();
        Expression exp = parseExpression();
        expect(Token.EOF);
        return exp;
    }


    /**
     * Parses an equation of the form Variable = Expression.
     */
    public Equation parseEquation(String equation) throws ParseException {
        int index = equation.indexOf("=");
        if(index < 1){
            throw new ParseException("Equations must be of the form Var = Exp", 0);
        }
        String variable = equation.substring(0, index).trim();
        if (!variable.matches("[^0-9]?[^ \t]*")) {
            throw new ParseException("Invalid variable name.", 1);
        }

        return new Equation(variable, parseExpression(equation.substring(index + 1).trim()), equation);
    }

    //================================ Private Methods =================================//


    /**
     * Moves to the next token.
     */
    private void readToken() {
        this.token = this.lexer.nextToken();
    }


    /**
     * Parses the expression.
     */
    private Expression parseExpression() throws ParseException {
        return parseInfix();
    }






    /**
     * Deals with infix expressions.
     */
    private Expression parseInfix() throws ParseException {
        // now parse infix expression.
        Expression expression = chompExpression();
        ExpressionDescriptor previous = null;
        while (token == Token.OPERATOR) {
            ExpressionDescriptor descriptor = getDescriptor();
            if (descriptor.getPosition() == ExpressionDescriptor.Position.PREFIX) {
                throw new ParseException("The expression " + descriptor.getName() + " cannot occur" +
                        " in a infix context.", lexer.getOffset());
            }
            readToken();
            Expression expression2 = chompExpression();
            try {
                expression = descriptor.createExpression(expression, expression2);
            } catch (ExpressionInitializationException e) {
                throw new ParseException("Wrong number of arguments for expressoin " + descriptor.getName(),
                        lexer.getOffset());
            }
            if (descriptor == previous && !descriptor.isCommutative()) {
                throw new ParseException("Operator is not commutative, " + descriptor.getName(), lexer.getOffset());
            }
            if (previous != null && descriptor != previous) {
                throw new ParseException("Ambiguous operator ordering.", lexer.getOffset());
            }

            previous = descriptor;
        }


        return expression;
    }







    /**
     * Chomps an expression.
     */
    private Expression chompExpression() throws ParseException {
        // parse a number.
        if (token == Token.NUMBER) {
            Expression exp = new ConstantExpression(parseNumber(lexer.getTokenString()));
            readToken();
            return exp;
        }
        // parse a variable.
        if (token == Token.VARIABLE) {
            String stringToken = this.lexer.getTokenString();
            if (!this.variables.contains(stringToken)) {
                throw new ParseException("Variable " + stringToken + " is not known.", lexer.getOffset());
            }
            VariableExpression exp = new VariableExpression(stringToken);
            readToken();
            if(this.token == Token.EQUATION){
                return parseEvaluation(exp);
            }
            return exp;
        }

        // deal with prefix operator.
        if (token == Token.OPERATOR) {
            ExpressionDescriptor descriptor = getDescriptor();
            readToken();
            Expression[] expressions;
            expect(Token.LPAREN);
            if (token == Token.RPAREN) {
                readToken();
                expressions = new Expression[0];
            } else {
                List<Expression> expressionList = parseExpressionList();
                expect(Token.RPAREN);
                expressions = expressionList.toArray(new Expression[expressionList.size()]);
            }

            try {
                return descriptor.createExpression(expressions);
            } catch (ExpressionInitializationException e) {
                throw new ParseException("Expression " + descriptor.getName()
                        + " was given the wrong number of arguments.", lexer.getOffset());
            }

        }

        // deal with parens.
        if (token == Token.LPAREN) {
            readToken();
            Expression exp = parseExpression();
            expect(Token.RPAREN);
            return exp;
        }

        // deal with pi
        if (token == Token.PI) {
            readToken();
            return ConstantExpression.PI;
        }

        // deal with e
        if (token == Token.E) {
            readToken();
            return ConstantExpression.E;
        }

        throw new ParseException("Token " + token + " was not expected.", this.lexer.getOffset());
    }


    /**
     * Creates an evaluation expression.
     */
    private Expression parseEvaluation(VariableExpression variable) throws ParseException {
        expect(Token.EQUATION);
        if(token != Token.STRING){
            throw new ParseException("Evaluations must be of the form Var = String", lexer.getOffset());
        }
        String s = lexer.getTokenString();
        readToken();
        return new EvaluationExpression(variable, s.replace("\"", ""));
    }


    /**
     * Pareses a comma seperated list of expressions.
     */
    private List<Expression> parseExpressionList() throws ParseException {
        List<Expression> expressions = new LinkedList<Expression>();

        expressions.add(parseExpression());
        while (token == Token.COMMA) {
            readToken();
            expressions.add(parseExpression());
        }

        return expressions;
    }


    private double parseNumber(String number) throws ParseException {
        try {
            return Double.parseDouble(number);
        } catch (Exception ex) {
            throw new ParseException("String " + number + " was not a number.", lexer.getOffset());
        }
    }


    /**
     * Returns the descriptor represented by the current token or throws an exception if there isn't one.
     */
    private ExpressionDescriptor getDescriptor() throws ParseException {
        String tokenString = lexer.getTokenString();
        ExpressionDescriptor descriptor = this.expressions.getDescriptorFromToken(tokenString);
        if (descriptor == null) {
            throw new ParseException("No descriptor for expression token " + tokenString, lexer.getOffset());
        }
        return descriptor;
    }


    /**
     * Expects the given token and then reads the next token.
     */
    private void expect(Token token) throws ParseException {
        if (token != this.token) {
            throw new ParseException("Unexpected token: " + this.token, this.lexer.getOffset());
        }
        readToken();
    }


}
