package edu.cmu.tetradapp.model.calculator.expression;

/**
 * An abstract expression descriptor.
 *
 * @author Tyler Gibson
 */
abstract class AbstractExpressionDescriptor implements ExpressionDescriptor {

    /**
     * The human readable name for the descriptor.
     */
    private String name;

    /**
     * States what positions the expression can occur in.
     */
    private Position position;

    /**
     * States whether the expression is commutative or not.
     */
    private boolean commutative;

    /**
     * The symbol used to represent the expression.
     */
    private String token;


    /**
     * The expression sig.
     */
    private ExpressionSignature signature;


    /**
     * Constructs an abstract expression descriptor.
     *
     * @param name          - The name of the descriptor.
     * @param token         The token of the descriptor, also used for the signature.
     * @param position      The position that the expression can occur in.
     * @param commutative   States whether the token can be commuative.
     * @param unlimited     States whether an unlimited number of arguments is allowed.
     * @param argumentTypes The argument types to use for the expression's signature.
     */
    public AbstractExpressionDescriptor(String name, String token, Position position, boolean commutative,
                                        boolean unlimited, String... argumentTypes) {
        if (name == null) {
            throw new NullPointerException("name was null.");
        }
        if (token == null) {
            throw new NullPointerException("token was null.");
        }
        if (position == null) {
            throw new NullPointerException("position was null.");
        }

        this.signature = new Signature(token, unlimited, commutative, argumentTypes);
        this.name = name;
        this.token = token;
        this.position = position;
        this.commutative = commutative;
    }


    public String getName() {
        return this.name;
    }


    public String getToken() {
        return this.token;
    }


    public ExpressionSignature getSignature() {
        return this.signature;
    }


    public Position getPosition() {
        return this.position;
    }


    public boolean isCommutative() {
        return this.commutative;
    }



    //=============================== Inner Class ==============================================//


    /**
     * Basic implementation of expression signature.
     */
    static class Signature implements ExpressionSignature {

        private String signature;
        private String function;
        private String[] arguments;
        private boolean unlimited;


        public Signature(String function, boolean unlimited, boolean commulative, String... arguments) {
            if (function == null) {
                throw new NullPointerException("function was null.");
            }
            this.function = function;
            this.arguments = arguments;
            this.unlimited = unlimited;
            this.signature = function;
            // create signature string.
            if (!commulative) {
                this.signature += "(";
                for (int i = 0; i < arguments.length; i++) {
                    if (i != 0) {
                        this.signature += ", ";
                    }
                    this.signature += arguments[i];
                }
                if (unlimited) {
                    if (arguments.length != 0) {
                        this.signature += ", ";
                    }
                    this.signature += "...";
                }
                this.signature += ")";
            }
        }


        public String getSignature() {
            return this.signature;
        }

        public String getFunctionName() {
            return this.function;
        }

        public int getNumberOfArguments() {
            return this.arguments.length;
        }

        public boolean isUnlimited() {
            return this.unlimited;
        }

        public String getArgument(int index) {
            return this.arguments[index];
        }
    }


}
