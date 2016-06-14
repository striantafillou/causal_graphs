package edu.cmu.tetrad.cluster.metrics;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * Euclidean dissimilarity metric--i.e., the sum of the differences in
 * corresponding variable values.
 *
 * @author Joseph Ramsey
 */
public class SquaredErrorLoss implements Dissimilarity {
    public double dissimilarity(DoubleMatrix1D v1, DoubleMatrix1D v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Vectors not the same length.");
        }

        double sum = 0.0;

        for (int j = 0; j < v1.size(); j++) {
            double diff = v1.get(j) - v2.get(j);
            sum += diff * diff;
        }

        return sum;
    }
}
