package edu.cmu.tetrad.regression;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Implements a multiple regression model, allowing data to be specified
 * either as a tabular data set or as a covariance matrix plus list of means.
 *
 * @author Joseph Ramsey
 */
public interface RegressionNew {

    /**
     * Sets the significance level at which coefficients are judged to be
     * significant.
     * @param alpha the significance level.
     */
    void setAlpha(double alpha);

    /**
     * Retresses <code>target</code> on the <code>regressors</code>, yielding
     * a regression plane.
     * @param target the target variable, being regressed.
     * @param regressors the list of variables being regressed on.
     * @return the regression plane.
     */
    RegressionResult regress(Node target, List<Node> regressors);

    /**
     * Returns the graph of significant regressors into the target.
     * @return This graph.
     */
    Graph getGraph();
}
