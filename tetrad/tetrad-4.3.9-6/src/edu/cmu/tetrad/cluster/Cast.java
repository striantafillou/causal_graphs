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
public class Cast implements ClusteringAlgorithm {

    /**
     * The dissimilarity metric used.
     */
    private Dissimilarity dissimilarity = new SquaredErrorLoss();

    /**
     * The cutoff value. If the affinity of a point with cOpen is less than t |
     * cOpen |, then that point is high affinity; otherwise, it is low affinity.
     * (Note that the original algorithm has been modified to use dissimilarity
     * rather than similarity.
     */
    private double threshold = 1.0;

    /**
     * The returned clusters.
     */
    private List<List<Integer>> clusters;

    /**
     * True if verbose output should be printed to System.out.
     */
    private boolean verbose = true;
    private Set<Integer> storedCluster;
    private double[] affinities;

    public void cluster(DoubleMatrix2D data) {
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        Set<Integer> setU = new LinkedHashSet<Integer>();
        for (int i = 0; i < data.rows(); i++) setU.add(i);

        Set<Integer> cOpen = new LinkedHashSet<Integer>();
        affinities = new double[data.rows()];

        while (!setU.isEmpty() || !cOpen.isEmpty()) {
            int u = maximalAffinity(setU, cOpen, data);

            if (u != -1 && affinity(u, cOpen, data) <= getThreshold() * cOpen.size()) {
                cOpen.add(u);
                setU.remove(u);
                System.out.println("Adding " + u + " affinity = " + affinity(u, cOpen, data)
                   + " <= " + getThreshold() + " * " + cOpen.size() + " = " + getThreshold() * cOpen.size());

                adjustAffinitiesUp(u, data);
                continue;
            }

            int v = minimalAffinity(cOpen, data);

            if (v != -1 && affinity(v, cOpen, data) > getThreshold() * (cOpen.size() - 1)) {
                System.out.println("Removing " + v + " affinity = " + affinity(v, cOpen, data)
                    + " > " + getThreshold() + " * " + (cOpen.size() - 1) + " = " + getThreshold() * (cOpen.size() - 1));
                cOpen.remove(v);
                setU.add(v);

                adjustAffinitiesDown(v, data);
                continue;
            }

            System.out.println("Closing: size = " + cOpen.size());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clusters.add(new ArrayList<Integer>(cOpen));
            cOpen = new LinkedHashSet<Integer>();

            for (int i = 0; i < data.rows(); i++) {
                affinities[i] = 0.0;
            }
        }

        this.clusters = clusters;
    }

    private void adjustAffinitiesUp(int u, DoubleMatrix2D data) {
        DoubleMatrix1D v1 = data.viewRow(u);

        for (int x = 0; x < data.rows(); x++) {
            DoubleMatrix1D v2 = data.viewRow(x);
            affinities[x] += similarity(v1, v2);
        }
    }

    private void adjustAffinitiesDown(int u, DoubleMatrix2D data) {
        DoubleMatrix1D v1 = data.viewRow(u);

        for (int x = 0; x < data.rows(); x++) {
            DoubleMatrix1D v2 = data.viewRow(x);
            affinities[x] -= similarity(v1, v2);
        }
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

    private int minimalAffinity(Set<Integer> set, DoubleMatrix2D data) {
        int p = -1;
        double max = Double.NEGATIVE_INFINITY;

        for (int p2 : set) {
            double affinity = affinity(p2, set, data);

            if (affinity > max) {
                max = affinity;
                p = p2;
            }
        }

        return p;
    }

    private int maximalAffinity(Set<Integer> set, Set<Integer> cOpen, DoubleMatrix2D data) {
        int p = -1;
        double min = Double.POSITIVE_INFINITY;

        for (int p2 : set) {
            double affinity = affinity(p2, cOpen, data);

            if (affinity < min) {
                min = affinity;
                p = p2;
            }
        }

        return p;
    }

    private double affinity(int point, Set<Integer> cluster, DoubleMatrix2D data) {
        if (affinities[point] == 0) {
            affinities[point] = affinity2(point, cluster, data);
        }

        return affinities[point];
    }

    private double affinity2(int point, Set<Integer> cluster, DoubleMatrix2D data) {
        double sum = 0.0;

        for (int otherPoint : cluster) {
            if (point == otherPoint) continue;

            DoubleMatrix1D v1 = data.viewRow(point);
            DoubleMatrix1D v2 = data.viewRow(otherPoint);

            sum += similarity(v1, v2);
        }

        return sum;
    }

    private double similarity(DoubleMatrix1D v1, DoubleMatrix1D v2) {
        return getDissimilarity().dissimilarity(v1, v2);
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public Dissimilarity getDissimilarity() {
        return dissimilarity;
    }

    public void setDissimilarity(Dissimilarity dissimilarity) {
        this.dissimilarity = dissimilarity;
    }
}
