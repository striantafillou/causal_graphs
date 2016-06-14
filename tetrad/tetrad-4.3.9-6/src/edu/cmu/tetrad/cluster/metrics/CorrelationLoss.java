package edu.cmu.tetrad.cluster.metrics;

import cern.colt.matrix.DoubleMatrix1D;

/**
 * For *standardized data* (means all zero, lengths of vectors all 1), this
 * return 1 - correlation between v1 and v2. For other types of data, probably
 * returns nonsense. We're returning 1 - correlation because this is supposed to
 * be a dissimilarity measure.
 *
 * @author Joseph Ramsey
 */
public class CorrelationLoss implements Dissimilarity {
    private Dissimilarity loss = new SquaredErrorLoss();

    public double dissimilarity(DoubleMatrix1D v1, DoubleMatrix1D v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Vectors not the same length.");
        }

        return (1 - (0.5 * loss.dissimilarity(v1, v2) + 1));
    }
}
