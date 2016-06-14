package edu.cmu.tetradapp.model;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tyler
 */
public interface StandardRegressionParams extends SearchParams{
    /**
     * Sets the target variable for the regression.
     */
    void setTargetName(String targetName);

    /*
    * Returns the target variable for the PCX search.
    */
    String getTargetName();

    /**
     * Sets the significance level for the search.
     */
    void setAlpha(double alpha);

    /**
     * Returns the significance level for the search.
     */
    double getAlpha();

    /**
     * Sets the array of regressor indices
     */
    void setRegressorNames(String[] names);

    /**
     * Returns the array of regressor indices
     */
    String[] getRegressorNames();
}
