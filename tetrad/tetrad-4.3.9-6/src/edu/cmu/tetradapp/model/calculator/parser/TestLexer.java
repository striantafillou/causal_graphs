package edu.cmu.tetradapp.model.calculator.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Tyler Gibson
 */
public final class TestLexer extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestLexer(String name) {
        super(name);
    }

    


    public void testLexer(){
        String s = "(1 + 2.5)";
        ExpressionLexer lexer = new ExpressionLexer(s);

        Token token = lexer.nextToken();
        System.out.println("Token string: " + lexer.getTokenString());
        assertTrue(token == Token.LPAREN);

        token = lexer.nextToken();
        System.out.println("Token string: " + lexer.getTokenString());
        assertTrue(token == Token.NUMBER);

        token = lexer.nextToken();
        System.out.println("Token string: " + lexer.getTokenString());
        assertTrue(token == Token.OPERATOR);

        token = lexer.nextToken();
        System.out.println("Token string: " + lexer.getTokenString());
        assertTrue(token == Token.NUMBER);

        token = lexer.nextToken();
        System.out.println("Token string: " + lexer.getTokenString());
        assertTrue(token == Token.RPAREN);

        assertTrue(lexer.nextToken() == Token.EOF);
    }



    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestLexer.class);
    }
}
