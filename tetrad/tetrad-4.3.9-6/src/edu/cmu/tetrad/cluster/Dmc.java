package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.util.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the dense mode clustering algorithm in "Dense mode clustering in
 * brain maps," Hanson et al., in press.
 *
 * @author Joseph Ramsey
 */
public class Dmc implements ClusteringAlgorithm {

    /**
     * The list of thresholded points.
     */
    private List<Point> points;

    /**
     * The list of clusters when <code>cluster()</code> finishes.
     */
    private List<Cluster> clusters;

    /**
     * The number of thresholded neighbors within r of a point needed for it
     * to be considered "dense."
     */
    private int densityTreshold = 20;

    /**
     * The maximum distance between two points for them to be considered
     * neighbors.
     */
    private double neighborDistance = 2.5;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    /**
     * True if verbose output should be written to System.out.
     */
    private boolean verbose = true;

    //============================CONSTRUCTOR==========================//

    /**
     * Private constructor. (Please keep it that way.)
     */
    private Dmc() {
    }

    public static Dmc rjSearch() {
        return new Dmc();
    }

    public void cluster(DoubleMatrix2D data) {
        points = new ArrayList<Point>();

        for (int i = 0; i < data.rows(); i++) {
            points.add(new Point(data.viewRow(i)));
        }

        System.out.println("# thresholded points = " +
                points.size() + ", k = " + getDensityTreshold() + ", r = " + getNeighborDistance());

        // Determine the list of dense points.
        List<Point> densePoints = new ArrayList<Point>();
        List<Cluster> clusters = new ArrayList<Cluster>();

        for (int i = 0; i < points.size(); i++) {
            if (isVerbose()) {
                if (i % 1000 == 0) System.out.println(i);
            }
            
            Point p = points.get(i);
            int count = 0;

            for (Point q : points) {
                double v = distance(p, q);
                double v1 = getNeighborDistance();
                if (v < v1) {
                    count++;
                }
            }

//            System.out.println("Count for " + p + " = " + count);

            if (count > getDensityTreshold()) {
                if (isVerbose()) {
//                    System.out.println("Adding dense point " + p + ",  count = " + count);
                }

                densePoints.add(p);
            }
        }

        if (isVerbose()) {
            System.out.println("# dense points = " + densePoints.size());
        }

        // Introduction phase.
        if (isVerbose()) {
            System.out.println("Merging dense point clusters.");
        }

        for (int i = 0; i < densePoints.size(); i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            Point p = densePoints.get(i);

            List<Integer> foundIndices = new LinkedList<Integer>();

            CLUSTER:
            for (int j = 0; j < clusters.size(); j++) {
                Cluster cluster = clusters.get(j);
                if (cluster == null) continue;

                for (Point q : cluster.getPoints()) {
                    if (p == q) continue;

                    if (distance(p, q) < 4) {
                        foundIndices.add(j);
                        continue CLUSTER;
                    }
                }
            }

            if (foundIndices.size() == 0) {
                clusters.add(Cluster.pointCluster(p));
            }
            if (foundIndices.size() == 1) {
                clusters.get(foundIndices.get(0)).addPoint(p);
            }
            else if (foundIndices.size() > 1) {
                Cluster cluster = Cluster.emptyCluster();

                for (Integer index : foundIndices) {
                    cluster.merge(clusters.get(index));
                    clusters.set(index, null);
                }

                cluster.addPoint(p);
                clusters.add(cluster);
            }
        }

        clusters = removeNulls(clusters);

//        System.out.println(printableClusterString(clusters));

        if (isVerbose()) {
            System.out.println("Initial phase -- # clusters = " + clusters.size());
        }

        // Merging phase
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                Cluster ci = clusters.get(i);
                Cluster cj = clusters.get(j);

                if (ci == null || cj == null) {
                    continue;
                }

//                System.out.println("Trying to merge " + i + " " + j);

                if (mergeable(ci, cj)) {
                    clusters.set(i, null);
                    clusters.set(j, null);

                    Cluster d = Cluster.emptyCluster();
                    d.merge(ci);
                    d.merge(cj);
                    clusters.add(d);

                    if (isVerbose()) {
                        System.out.println("Merging clusters " + i + " and " + j);
                        printNonNullClusters(clusters);
                    }
                }
            }
        }

        this.clusters = removeNulls(clusters);
        System.out.println("Merging phase -- # clusters = " + this.clusters.size());
    }

    public List<List<Integer>> getClusters() {
        List<List<Integer>> _clusters = new ArrayList<List<Integer>>();

        for (int i = 0; i < this.clusters.size(); i++) {
            Cluster cluster = this.clusters.get(i);
            ArrayList<Integer> _cluster = new ArrayList<Integer>();

            for (Point p : cluster.getPoints()) {
                _cluster.add(points.indexOf(p));
            }

            _clusters.add(_cluster);
        }

        return _clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void writePointsToFile(List<Point> points) {
        try {
            File file = new File("/home/jdramsey/points.dat");
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter out = new PrintWriter(fileWriter);

            for (Point point : points) {
                out.println(point.getValue(2) + " " + point.getValue(0) + " " + (72 - point.getValue(1)));
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printNonNullClusters(List<Cluster> clusters) {
        for (int k = 0; k < clusters.size(); k++) {
            Cluster cluster = clusters.get(k);

            if (cluster == null) {
                continue;
            }

            System.out.print(k + " ");
        }

        System.out.println();
    }

    private List<Cluster> removeNulls(List<Cluster> clusters) {
        List<Cluster> absentNulls = new ArrayList<Cluster>();

        for (Cluster cluster : clusters) {
            if (cluster != null) {
                absentNulls.add(cluster);
            }
        }
        return absentNulls;
    }

    private boolean mergeable(Cluster ci, Cluster cj) {
        return romeoAndJuliutRule(ci, cj);
//        return averageLinkageRule(ci, cj);
    }

    private boolean averageLinkageRule(Cluster ci, Cluster cj) {
        double threshold = 10;
        double sum = 0.0;

        for (Point pPrime : ci.getPoints()) {
            for (Point qPrime : cj.getPoints()) {
                for (int k = 0; k < pPrime.getSize(); k++) {
                    sum += distance(pPrime, qPrime);
                }
            }
        }

        return sum / (ci.size() * cj.size()) < threshold;

    }

    private boolean romeoAndJuliutRule(Cluster ci, Cluster cj) {
        Point p = null;
        Point q = null;
        double leastDistance = Double.POSITIVE_INFINITY;

        for (Point pPrime : ci.getPoints()) {

            QPRIME:
            for (Point qPrime : cj.getPoints()) {
                for (int k = 0; k < pPrime.getSize(); k++) {
                    if (Math.abs(pPrime.getValue(k) - qPrime.getValue(k)) > leastDistance) {
                        continue QPRIME;
                    }
                }

                double distance = distance(pPrime, qPrime);
                if (distance < leastDistance) {
                    p = pPrime;
                    q = qPrime;
                    leastDistance = distance;
                }
            }
        }

        if (p == null || q == null) {
            throw new IllegalStateException("Impossible to get here.");
        }

        double a = 0.0;

        for (Point x : ci.getPoints()) {
            a += distance(p, x);
        }

        a /= ci.size();

        double b = 0.0;

        for (Point y : cj.getPoints()) {
            b += distance(q, y);
        }

        b /= cj.size();

        return leastDistance < (a + b) / 2.0;
    }

    private double distance(Point p, Point q) {
        return metric.dissimilarity(p.getVector(), q.getVector());
    }

    /**
     * Returns a string representation of the cluster result.
     */
    public String toString() {
        return printableClusterString(clusters);
    }

    private String printableClusterString(List<Cluster> clusters) {
        StringBuffer buf = new StringBuffer();

        int i = 0;

        for (Cluster cluster : clusters) {
            System.out.println("Cluster # " + (++i) + " size = " + cluster.size()
                    + " " + cluster);
        }

        return buf.toString();
    }


    public int getDensityTreshold() {
        return densityTreshold;
    }

    public void setDensityTreshold(int densityTreshold) {
        this.densityTreshold = densityTreshold;
    }

    public double getNeighborDistance() {
        return neighborDistance;
    }

    public void setNeighborDistance(double neighborDistance) {
        this.neighborDistance = neighborDistance;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
