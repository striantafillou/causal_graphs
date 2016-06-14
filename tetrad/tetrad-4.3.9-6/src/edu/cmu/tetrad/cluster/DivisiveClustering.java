package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of divisive clustering.
 *
 * @author Joseph Ramsey
 */
public class DivisiveClustering implements ClusteringAlgorithm {
    private Dissimilarity metric = new SquaredErrorLoss();
    private double threshold = 0.1;
    private DoubleMatrix2D data;
    private List<List<Integer>> clusters;

    public List<List<Integer>> getClusters() {
        return clusters;
    }

    public void cluster(DoubleMatrix2D data) {
        this.data = data;

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        List<List<Integer>> clustersToDivide = new ArrayList<List<Integer>>();
        List<Integer> initialCluster = new ArrayList<Integer>();

        for (int i = 0; i < data.rows(); i++) {
            initialCluster.add(i);
        }

        clustersToDivide.add(initialCluster);

        while (clustersToDivide.size() > 0) {
            for (List<Integer> cluster : new ArrayList<List<Integer>>(clustersToDivide)) {
                int[] farPoints = getFarPoints2(cluster);
                int i1 = farPoints[0];
                int i2 = farPoints[1];

                double dd = distance(i1, i2);

                System.out.println("dd = " + dd);

                if (dd < getThreshold()) {
                    clustersToDivide.remove(cluster);

                    if (cluster.size() > 50) {
                        clusters.add(cluster);
//                        System.out.println("Adding " + cluster);
                    }

                    continue;
                }

                List<Integer> cluster1 = new ArrayList<Integer>();
                List<Integer> cluster2 = new ArrayList<Integer>();

                for (int j : cluster) {
                    if (distance(i1, j) < distance(i2, j)) {
                        cluster1.add(j);
                    } else {
                        cluster2.add(j);
                    }
                }

                clustersToDivide.remove(cluster);

                if (!cluster1.isEmpty()) {
                    clustersToDivide.add(cluster1);
                }

                if (!cluster2.isEmpty()) {
                    clustersToDivide.add(cluster2);
                }
            }
        }

        this.clusters = clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setVerbose(boolean verbose) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private int[] getFarPoints1(List<Integer> cluster) {
        int i1 = cluster.get(0);
        int i2 = cluster.get(0);
        double max = 0.0;

        for (int i = 0; i < cluster.size(); i++) {
            for (int j = 0; j < i; j++) {
                double d = distance(i, j);

                if (d >= max) {
                    i1 = cluster.get(i);
                    i2 = cluster.get(j);
                    max = d;
                }
            }
        }

        return new int[] {i1, i2};
    }

    private int[] getFarPoints2(List<Integer> cluster) {
        int i1 = cluster.get(0);
        int i2 = -1;

        while (true) {
            int j = furthestPoint(i1, cluster);

            if (i2 == j) {
                break;
            }

            i2 = i1;
            i1 = j;
        }

        return new int[] {i1, i2};
    }

    private int furthestPoint(int i1, List<Integer> cluster) {
        double min = 0.0;
        int v2 = -1;

        for (int j : cluster) {
            double dissimilarity = distance(i1, j);

            if (dissimilarity >= min) {
                min = dissimilarity;
                v2 = j;
            }
        }

        if (v2 == -1) {
            System.out.println();
        }

        return v2;
    }

    private double distance(int i1, int j) {
        return getMetric().dissimilarity(data.viewRow(i1), data.viewRow(j));
    }

    public Dissimilarity getMetric() {
        return metric;
    }

    public void setMetric(Dissimilarity metric) {
        this.metric = metric;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
