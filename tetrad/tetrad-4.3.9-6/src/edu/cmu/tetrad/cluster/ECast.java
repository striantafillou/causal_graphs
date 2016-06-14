package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Oct 11, 2007 Time: 4:18:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ECast implements ClusteringAlgorithm {

    /**
     * The dissimilarity metric used.
     */
    private Dissimilarity dissimilarity = new SquaredErrorLoss();

    /**
     * The returned clusters.
     */
    private List<List<Integer>> clusters;

    /**
     * True if verbose output should be printed to System.out.
     */
    private boolean verbose = true;
    private Set<Integer> storedCluster;
    private double[] affinity;
    private Set<Integer> cOpen = new LinkedHashSet<Integer>();
    private DoubleMatrix2D data;

    public void cluster1(DoubleMatrix2D data) {
        this.data = data;
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        Set<Integer> setU = new LinkedHashSet<Integer>();

        for (int i = 0; i < data.rows(); i++) setU.add(i);

        affinity = new double[data.rows()];

        while (!setU.isEmpty()) {
            double threshold = calculateT(setU);

            System.out.println("threshold = " + threshold);

            for (int u = 0; u < affinity.length; u++) affinity[u] = 0;
            cOpen = new LinkedHashSet<Integer>();

            int u = maximalSimilarity(setU);
            cOpen.add(u);
            setU.remove(u);
            for (int x : setU) affinity[x] += similarity(x, u);
            System.out.println("Initially adding " + u);

            boolean changed = true;

            while (changed) {
                changed = false;

                while (!setU.isEmpty()) {
                    u = maxAffineElement(setU);

                    if (!(affinity[u] >= threshold * cOpen.size())) {
                        break;
                    }

                    System.out.println("Adding " + u + " affinity = " + affinity[u]);

                    cOpen.add(u);
                    setU.remove(u);
                    changed = true;

                    for (int x : setU) {
                        affinity[x] += similarity(x, u);
                    }

                    for (int x : cOpen) {
//                        if (x == u) continue;
                        affinity[x] += similarity(x, u);
                    }
                }

                while (!cOpen.isEmpty()) {
                    u = minAffineElement(cOpen);

                    if (!(affinity[u] < threshold * cOpen.size())) {
                        break;
                    }

                    System.out.println("Removing " + u + " affinity = " + affinity[u]);

                    cOpen.remove(u);
                    setU.add(u);
                    changed = true;

                    for (int x : setU) {
//                        if (x == u) continue;
                        affinity[x] -= similarity(x, u);
                    }

                    for (int x : cOpen) {
                        affinity[x] -= similarity(x, u);
                    }
                }
            }

            System.out.println("CLOSE");
            clusters.add(new ArrayList<Integer>(cOpen));
        }

        this.clusters = clusters;
    }

    public void cluster(DoubleMatrix2D data) {
        this.data = data;

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        Set<Integer> setU = new LinkedHashSet<Integer>();
        for (int i = 0; i < data.rows(); i++) setU.add(i);

        Set<Integer> cOpen = new LinkedHashSet<Integer>();
        affinity = new double[data.rows()];
        double threshold = calculateT(setU);

        int u = maximalSimilarity(setU);
        cOpen.add(u);
        setU.remove(u);
        for (int x : setU) affinity[x] += similarity(x, u);
        System.out.println("Initially adding " + u);

        while (!setU.isEmpty() || !cOpen.isEmpty()) {
            u = maxAffineElement(setU);

            if (u != -1 && affinity[u] >= threshold * cOpen.size()) {
                System.out.println("Adding " + u + " affinity = " + affinity[u]);
                cOpen.add(u);
                setU.remove(u);

                for (int x : setU) {
                    affinity[x] += similarity(x, u);
                }

                for (int x : cOpen) {
                    if (x == u) continue;
                    affinity[x] += similarity(x, u);
                }

                continue;
            }

            u = minAffineElement(cOpen);

            if (u != -1 && affinity[u] < threshold * (cOpen.size() - 1)) {
                System.out.println("Removing " + u + " affinity = " + affinity[u]);
                cOpen.remove(u);
                setU.add(u);

                for (int x : setU) {
                    affinity[x] -= similarity(x, u);
                }

                for (int x : cOpen) {
                    if (x == u) continue;
                    affinity[x] -= similarity(x, u);
                }

                continue;
            }

            System.out.println("Closing.");

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            clusters.add(new ArrayList<Integer>(cOpen));
            cOpen = new LinkedHashSet<Integer>();

            for (int i = 0; i < data.rows(); i++) {
                affinity[i] = 0.0;
            }
        }

        this.clusters = clusters;
    }

    private double calculateT(Set<Integer> U) {
        double a = 0;
        int count = 0;

        for (int i : U) {
            for (int j : U) {
                if (i >= j) continue;

                double s = similarity(i, j);

                if (s > 0.5) {
                    a += s - 0.5;
                    count++;
                }
            }
        }

        return (a / count);
    }

    public List<List<Integer>> getClusters() {
        return clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        throw new UnsupportedOperationException();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private int minAffineElement(Set<Integer> set) {
        int p = -1;
        double min = Double.POSITIVE_INFINITY;

        for (int q : set) {
            if (this.affinity[q] < min) {
                min = this.affinity[q];
                p = q;
            }
        }

        return p;
    }

    private int maximalSimilarity(Set<Integer> set) {
        int p = -1;
        double max = Double.NEGATIVE_INFINITY;

        for (int i : set) {
            for (int j : set) {
                if (i > j) continue;

                if (similarity(i, j) > max) {
                    max = similarity(i, j);
                    p = i;
                }
            }
        }

        return p;
    }

    private int maxAffineElement(Set<Integer> setU) {
        int p = -1;
        double max = Double.NEGATIVE_INFINITY;

        for (int q : setU) {

            if (this.affinity[q] > max) {
                max = this.affinity[q];
                p = q;
            }
        }

        return p;
    }

    private double similarity(int i, int j) {
        DoubleMatrix1D vi = data.viewRow(i);
        DoubleMatrix1D vj = data.viewRow(j);

        return Math.exp(-getDissimilarity().dissimilarity(vi, vj));
    }

    public Dissimilarity getDissimilarity() {
        return dissimilarity;
    }

    public void setDissimilarity(Dissimilarity dissimilarity) {
        this.dissimilarity = dissimilarity;
    }
}
