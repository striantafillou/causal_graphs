package edu.cmu.tetrad.sem;

/**
 * An enumeration of the types of distributions used by SEM2 for exogenous
 * terms, together with some descriptive informationa about them.
 *
 * @author Joseph Ramsey
 */
public enum DistributionType {
    ZERO_CENTERED_NORMAL("Zero-centered Normal", "N", 1),
    NORMAL("Normal", "N", 2),
    UNIFORM("Uniform", "U", 2),
    BETA("Beta", "Beta", 2),
    GAUSSIAN_POWER("GaussianPower", "GP", 1);

    /**
     * The name of the distribution (for example, "Normal").
     */
    private String name;

    /**
     * The function symbol for the distribution (for example, "N").
     */
    private String functionSymbol;

    /**
     * The number of arguments for the distribution (for example, 2).
     */
    private int numArgs;

    /**
     * Constructs a distribution type. Private.
     * @param name The name of the distribution.
     * @param functionSymbol The function symbol of the distribution.
     * @param numArgs The number of arguments of the distribution.
     */
    private DistributionType(String name, String functionSymbol, int numArgs) {
        this.name = name;
        this.functionSymbol = functionSymbol;
        this.numArgs = numArgs;
    }

    /**
     * Returns the name of the distribution. E.g. "Normal."
     * @return the name of the distribution.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the function symbol. (For normal this is "N.")
     * @return the function symbol.
     */
    public String getFunctionSymbol() {
        return functionSymbol;
    }

    /**
     * Returns the number of argument of the function. (For normal, this is 2.)
     * @return The number of arguments.
     */
    public int getNumArgs() {
        return numArgs;
    }
}
