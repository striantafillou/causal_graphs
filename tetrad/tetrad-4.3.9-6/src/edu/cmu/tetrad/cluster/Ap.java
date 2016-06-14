package edu.cmu.tetrad.cluster;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Affinity Propagation
 */
public class Ap implements ClusteringAlgorithm {
    DoubleMatrix2D data;

    private boolean verbose = false;

    private double lambda = 0.0;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    public Ap() {
        // Private constructor.
    }

//    public static AffinityPropagation


    public void cluster(DoubleMatrix2D data) {
        this.data = data;

        int n = data.rows();
        int m = data.columns();

        DoubleMatrix2D s = new DenseDoubleMatrix2D(n, n);

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                s.set(i, k, - metric.dissimilarity(data.viewRow(i), data.viewRow(k)));
            }
        }

        List<Double> offDiagonals = new ArrayList<Double>();

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                offDiagonals.add(s.get(i, k));
            }
        }

        Collections.sort(offDiagonals);
        int size = offDiagonals.size();
        Double m1 = offDiagonals.get(size / 2 - 1);
        Double m2 = offDiagonals.get(size - (size / 2));
        double median = (m1 + m2) / 2;

        for (int i = 0; i < n; i++) {
            s.set(i, i, median);
        }

        DoubleMatrix2D r = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D rOld = r.like();
        DoubleMatrix2D a = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D aOld = a.like();

        for (int repetition = 0; repetition < 100; repetition++) {
            System.out.println("Repetition " + repetition);

            rOld.assign(r);

            for (int i = 0; i < n; i++) {
                for (int k = 0; k < n; k++) {
                    double max = Double.NEGATIVE_INFINITY;

                    for (int kk = 0; kk < n; kk++) {
                        if (kk == k) continue;
                        double mmax = a.get(i, kk) + s.get(i, kk);
                        if (mmax > max) max = mmax;
                    }

                    r.set(i, k, s.get(i, k) - max);
                }
            }

//            System.out.println(r);

            r.assign(rOld, new DoubleDoubleFunction() {
                public double apply(double v, double v1) {
                    return (1 - lambda) * v + lambda * v1;
                }
            });

            aOld.assign(a);

            for (int i = 0; i < n; i++) {
                for (int k = 0; k < n; k++) {
                    double sum = r.get(k, k);

                    for (int ii = 0; ii < n; ii++) {
                        if (ii == i || ii == k) continue;
                        sum += Math.max(0, r.get(ii, k));
                    }

                    if (i == k) {
                        a.set(i, k, sum);
                    } else {

                        a.set(i, k, Math.min(0, sum));
                    }
                }
            }

//            System.out.println(a);

            a.assign(aOld, new DoubleDoubleFunction() {
                public double apply(double v, double v1) {
                    return (1 - lambda) * v + lambda * v1;
                }
            });
        }

        DoubleMatrix2D e = r.copy();
        e.assign(a, PlusMult.plusMult(1.0));

        List<Integer> exemplars = new ArrayList<Integer>();

        for (int i = 0; i < n; i++) {
            if (r.get(i, i) > 0) {
                exemplars.add(i);
            }
        }

        System.out.println(exemplars);
    }


    public List<List<Integer>> getClusters() {
        return new ArrayList<List<Integer>>();
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
