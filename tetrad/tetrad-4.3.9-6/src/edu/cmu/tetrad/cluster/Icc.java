package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

/**
 * Implements the dense mode clustering algorithm in "Dense mode clustering in
 * brain maps," Hanson et al., in press.
 *
 * @author Joseph Ramsey
 */
public class Icc {

    /**
     * The data, rows are cases.
     */
    private DoubleMatrix2D data;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    //============================CONSTRUCTOR==========================//

    /**
     * Private constructor. (Please keep it that way.)
     */
    public Icc(DoubleMatrix2D data) {
        this.data = data;
    }

    public void cluster() {
        DoubleMatrix2D x = data;

        FastIca fastIca = new FastIca(x, 1);
        fastIca.setVerbose(true);

        FastIca.IcaResult w = fastIca.findComponents();

        System.out.println(w.getS());
    }

}
