package edu.cmu.tetradapp.editor;


/**
 * Represents the tokens used for reading in data in Tetrad.
 *
 * @author Joseph Ramsey
 */
public enum DataToken implements Token {
    WHITESPACE("WHITESPACE"),

    BLANK_LINE("BLANK_LINE"),
    REST_OF_LINE("REST_OF_LINE"),
    COMMENT_LINE("COMMENT_LINE"),

    VARIABLES_MARKER("VARIABLES_MARKER"),
    VAR_TYPE("VAR_TYPE"),
    COLON("COLON"),
    LPAREN("LPAREN"),
    DISCRETE_STATE("DISCRETE_STATE"),
    COMMA("COMMA"),
    RPAREN("RPAREN"),

    DATA_MARKER("DATA_MARKER"),
    CONTINUOUS_TOKEN("CONTINUOUS_TOKEN"),
    DISCRETE_TOKEN("DISCRETE_TOKEN"),
    STRING_TOKEN("STRING_TOKEN"),
    MISSING_VALUE("MISSING_VALUE"),

    KNOWLEDGE_MARKER("KNOWLEEDGE_MARKER"),
    ADD_TEMPORAL_HEADER("ADD_TEMPORAL"),
    FORBID_DIRECT_HEADER("FORBID_DIRECT"),
    REQUIRE_DIRECT_HEADER("REQUIRE_DIRECT"),

    EOF("EOF");

    /**
     * The name of the token
     */
    private final String name;

    /**
     * Constructs the enum
     *
     * @param name
     */
    private DataToken(String name){
        if (name == null) {
            throw new NullPointerException();
        }

        this.name = name;
    }


    /**
     * Returns the name of the token
     *
     * @return - Name
     */
    public String getName(){
       return this.name;
    }
}
