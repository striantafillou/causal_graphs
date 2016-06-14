package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;

import java.util.List;

/**
 * Represents a clusting algorithm to cluster some data. The data is a
 * DoubleMatrix2D matrix with rows as cases and columns as variables. The
 * purpose of this interface is to allow a clustering algorithm to have
 * parameters set so thet it can be passed to another class to do clustering on
 * data.
 *
 * @author Joseph Ramsey
 */
public interface ClusteringAlgorithm {

    /**
     * Clusters the given data set.
     *
     * @param data An n x m double matrix with n cases (rows) and m variables
     *             (columns).
     * @return an int array c such that c[i] is the cluster that case i is
     *         placed into, or -1 if case i is not placed into a cluster (as a
     *         result of its being eliminated from consideration, for
     *         instance).
     */
    void cluster(DoubleMatrix2D data);

    /**
     * Returns a list of clusters, each consisting of a list of indices in the
     * dataset provided as an argument to <code>cluster</code>, or null if the
     * data has not yet been clustered.
     *
     * @see #cluster(cern.colt.matrix.DoubleMatrix2D)
     */
    List<List<Integer>> getClusters();

    /**
     * Returns the list of prototypes for clusters as a 2D array, or null if
     * there are not prototypes or the data has not yet been clustered. The
     * array at (k, j) is the jth element of the kth prototype. The number of
     * columns in this array is equal to the number of columns in the dataset
     * provided as an argument to <code>cluster</code>.
     *
     * @see #cluster(cern.colt.matrix.DoubleMatrix2D)
     */
    DoubleMatrix2D getPrototypes();

    /**
     * True iff verbose output should be printed.
     */
    void setVerbose(boolean verbose);
}
