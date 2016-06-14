package edu.cmu.tetrad.cluster.metrics;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Oct 1, 2007 Time: 11:47:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Dissimilarity {
    double dissimilarity(DoubleMatrix1D v1, DoubleMatrix1D v2);
}
