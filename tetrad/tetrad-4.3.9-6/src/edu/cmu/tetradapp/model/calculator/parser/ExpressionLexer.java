package edu.cmu.tetradapp.model.calculator.parser;

import edu.cmu.tetradapp.model.calculator.expression.ConstantExpression;
import edu.cmu.tetradapp.model.calculator.expression.ExpressionDescriptor;
import edu.cmu.tetradapp.model.calculator.expression.ExpressionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
public class ExpressionLexer {


    /**
     * The current position of the lexer.
     */
    private int offset;


    /**
     * Mapping between tokens to their matchers.
     */
    private Map<Token, Matcher> matchers = new HashMap<Token, Matcher>();


    /**
     * The last matcher.
     */
    private Matcher lastMatcher;


    /**
     * The car sequenced being lexed.
     */
    private CharSequence charSequence;


    /**
     * The tokens.
     */
    private Token[] tokens = new Token[]{Token.WHITESPACE, Token.COMMA, Token.LPAREN,
            Token.NUMBER, Token.OPERATOR, Token.RPAREN, Token.E, Token.VARIABLE, Token.PI, Token.EQUATION, Token.STRING
    };


    /**
     * Cached PATTERNS.
     */
    private static Map<Token, Pattern> PATTERNS;


    public ExpressionLexer(CharSequence seq) {
        if (seq == null) {
            throw new NullPointerException("CharSequence must not be null.");
        }
        if (PATTERNS == null) {
            PATTERNS = createPatterns();
        }
        this.charSequence = seq;
        this.matchers = createMatches(PATTERNS, seq);
    }

    //=================================== Public Methods =====================================//

    /**
     * Returns the type of the next token. For words and quoted charSequence
     * tokens, the charSequence that the token represents can be fetched by
     * calling the getString method.
     */
    public final Token nextToken() {
        readToken(Token.WHITESPACE);
        for (Token token : tokens) {
            if (readToken(token)) {
                return token;
            }
        }
        if (this.charSequence.length() <= offset) {
            return Token.EOF;
        }

        return Token.UNKNOWN;
    }


    /**
     * Returns the string corresponding to the last token lexed.
     */
    public String getTokenString() {
        if (this.lastMatcher == null) {
            return null;
        }
        return this.lastMatcher.group();
    }

    /**
     * Returns the current offset.
     */
    public int getOffset() {
        return this.offset;
    }

    //=================================== Private Methods ====================================//


    private boolean readToken(Token token) {
        Matcher matcher = this.matchers.get(token);
        boolean found = matcher.find(this.offset);
        if (found) {
            this.offset = matcher.end();
            this.lastMatcher = matcher;
        }
        return found;
    }


    /**
     * Creates a map from tokens to regex Matchers for the given CharSequence,
     * given a map from tokens to regex Patterns (and the CharSequence).
     */
    private static Map<Token, Matcher> createMatches(Map<Token, Pattern> patterns, CharSequence charSequence) {
        Map<Token, Matcher> matchers = new HashMap<Token, Matcher>();
        for (Token token : patterns.keySet()) {
            Pattern pattern = patterns.get(token);
            Matcher matcher = pattern.matcher(charSequence);
            matchers.put(token, matcher);
        }
        return matchers;
    }


    private static Map<Token, Pattern> createPatterns() {
        Map<Token, Pattern> map = new HashMap<Token, Pattern>();
        Map<Token, String> regex = new HashMap<Token, String>();

        regex.put(Token.WHITESPACE, " ");
        regex.put(Token.LPAREN, "\\(");
        regex.put(Token.RPAREN, "\\)");
        regex.put(Token.COMMA, ",");
        regex.put(Token.NUMBER, "[0-9]+((\\.?)[0-9]+)?");
        regex.put(Token.OPERATOR, getExpressionRegex());
        regex.put(Token.VARIABLE, "\\$|(([a-zA-Z]{1})([a-zA-Z0-9-_/]*))");
        regex.put(Token.PI, ConstantExpression.PI.getName());
        regex.put(Token.E, ConstantExpression.E.getName());
        regex.put(Token.EQUATION, "\\=");
        regex.put(Token.STRING, "\\\".*\\\"");


        for (Token token : regex.keySet()) {
            map.put(token, Pattern.compile("\\G" + regex.get(token)));
        }

        return map;
    }


    /**
     * Builds a regex that can identify expressions.
     */
    private static String getExpressionRegex() {
        String str = "(";
        List<ExpressionDescriptor> descriptors = ExpressionManager.getInstance().getDescriptors();
        for (int i = 0; i < descriptors.size(); i++) {
            ExpressionDescriptor exp = descriptors.get(i);
            str += "(" + exp.getToken() + ")";
            if (i < descriptors.size() - 1) {
                str += "|";
            }
        }
        // replace meta characters where necessary.
        str = str.replace("+", "\\+");
        str = str.replace("*", "\\*");
        str = str.replace("^", "\\^");

        return str + ")";
    }


}
