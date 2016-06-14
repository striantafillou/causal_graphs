package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.RandomUtil;

import java.text.NumberFormat;
import java.util.*;

/**
 * Implements the "batch" version of the K Medoid clustering algorithm-- that is,
 * in one sweep, assign each point to its nearest center, and then in a second
 * sweep, reset each center to the mean of the cluster for that center,
 * repeating until convergence.
 *
 * @author Joseph Ramsey
 */
public class KMedoids implements ClusteringAlgorithm {

    /**
     * The data, columns as features, rows as cases.
     */
    private DoubleMatrix2D data;

    /**
     * The centers.
     */
    private DoubleMatrix2D centers;

    /**
     * The maximum number of interations.
     */
    private int maxIterations = 50;

    /**
     * Current clusters.
     */
    private List<Integer> clusters;

    /**
     * Number of iterations of algorithm.
     */
    private int iterations;

    /**
     * The number of centers.
     */
    private int numCenters;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    /**
     * True if verbose output should be printed.
     */
    private boolean verbose = true;

    //============================CONSTRUCTOR==========================//

    /**
     * Private constructor. (Please keep it that way.)
     */
    private KMedoids() {
    }

    /**
     * Constructs a new KMedoids, assigning each point initial to one of the
     * k clusters.
     */
    public static KMedoids randomClusters(int numCenters) {
        KMedoids algorithm = new KMedoids();
        algorithm.numCenters = numCenters;
        return algorithm;
    }

    /**
     * Runs the batch K-means clustering algorithm on the data, returning a
     * result.
     */
    public void cluster(DoubleMatrix2D data) {
        this.data = data;
        centers = pickCenters(numCenters, data);
        clusters = new ArrayList<Integer>();

        for (int i = 0; i < data.rows(); i++) {
            clusters.add(-1);
        }

        boolean changed = true;
        iterations = 0;

//        System.out.println("Original centers: " + centers);

        while (changed && (maxIterations == -1 || iterations < maxIterations)) {
            iterations++;
            System.out.println("Iteration = " + iterations);

            // Step #1: Assign each point to its closest center, forming a cluster for
            // each center.
            int numChanged = reassignPoints();
            changed = numChanged > 0;

            // Step #2: Replace each center by the center of mass of its cluster.
            moveCentersToMedoids();

//            System.out.println("New centers: " + centers);
            System.out.println("Cluster counts: " + countClusterSizes());
        }

    }


    public List<List<Integer>> getClusters() {
        return ClusterUtils.convertClusterIndicesToLists(clusters);
    }

    public DoubleMatrix2D getPrototypes() {
        return centers.copy();
    }

    /**
     * Return the maximum number of iterations, or -1 if the algorithm is
     * allowed to run unconstrainted.
     *
     * @return This value.
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Sets the maximum number of iterations, or -1 if the algorithm is allowed
     * to run unconstrainted.
     *
     * @param maxIterations This value.
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getNumClusters() {
        return centers.rows();
    }

    public List<Integer> getCluster(int k) {
        List<Integer> cluster = new ArrayList<Integer>();

        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i) == k) {
                cluster.add(i);
            }
        }

        return cluster;
    }

    /**
     * Returns the current iteration.
     *
     * @return the number of iterations.
     */
    public int iterations() {
        return iterations;
    }

    /**
     * The squared error of the kth cluster.
     *
     * @param k The index of the cluster in question.
     * @return this squared error.
     */
    public double squaredError(int k) {
        double squaredError = 0.0;

        for (int i = 0; i < data.rows(); i++) {
            if (clusters.get(i) == k) {
                DoubleMatrix1D datum = data.viewRow(i);
                DoubleMatrix1D center = centers.viewRow(k);
                squaredError += metric.dissimilarity(datum, center);
            }
        }
        return squaredError;
    }

    /**
     * Total squared error for most recent run.
     *
     * @return the total squared error.
     */
    public double totalSquaredError() {
        double totalSquaredError = 0.0;

        for (int k = 0; k < centers.rows(); k++) {
            totalSquaredError += squaredError(k);
        }

        return totalSquaredError;
    }

    /**
     * Returns a string representation of the cluster result.
     */
    public String toString() {
        NumberFormat n1 = NumberFormatUtil.getInstance().getNumberFormat();

        DoubleMatrix1D counts = countClusterSizes();
        double totalSquaredError = totalSquaredError();

        StringBuffer buf = new StringBuffer();
        buf.append("Cluster Result (").append(clusters.size())
                .append(" cases, ").append(centers.columns())
                .append(" feature(s), ").append(centers.rows())
                .append(" clusters)");

        for (int k = 0; k < centers.rows(); k++) {
            buf.append("\n\tCluster #").append(k + 1).append(": n = ").append(counts.get(k));
            buf.append(" Squared Error = ").append(n1.format(squaredError(k)));
        }

        buf.append("\n\tTotal Squared Error = ").append(n1.format(totalSquaredError));
        return buf.toString();
    }

    //==========================PRIVATE METHODS=========================//

    private int reassignPoints() {
        int numChanged = 0;

        for (int i = 0; i < data.rows(); i++) {
            DoubleMatrix1D datum = data.viewRow(i);
            double minDissimilarity = Double.POSITIVE_INFINITY;
            int cluster = -1;

            for (int k = 0; k < centers.rows(); k++) {
                DoubleMatrix1D center = centers.viewRow(k);
                double dissimilarity = dissimilarity(datum, center);

                if (dissimilarity < minDissimilarity) {
                    minDissimilarity = dissimilarity;
                    cluster = k;
                }
            }

            if (cluster != clusters.get(i)) {
                clusters.set(i, cluster);
                numChanged++;
            }
        }

        System.out.println("Moved " + numChanged + " points.");
        return numChanged;
    }

    private void moveCentersToMedoids() {
        for (int k = 0; k < centers.rows(); k++) {
            List<Integer> cluster = new LinkedList<Integer>();

            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i) == k) {
                    cluster.add(i);
                }
            }

            if (cluster.isEmpty()) {
                continue;
            }

            double min = Double.POSITIVE_INFINITY;
            int i = -1;

            for (int j = 0; j < cluster.size(); j++) {
                double totalDistance = totalDistance(j, cluster);

                if (totalDistance < min) {
                    min = totalDistance;
                    i = j;
                }
            }

            centers.viewRow(k).assign(data.viewRow(cluster.get(i)));
        }
    }

    private double totalDistance(int j, List<Integer> cluster) {
        double sum = 0.0;

        for (int i = 0; i < cluster.size(); i++) {
            if (i == j) continue;
            sum += dissimilarity(data.viewRow(cluster.get(i)), data.viewRow(cluster.get(j)));
        }

        return sum;
    }

    private DoubleMatrix2D pickCenters(int numCenters, DoubleMatrix2D data) {
        SortedSet<Integer> indexSet = new TreeSet<Integer>();

        while (indexSet.size() < numCenters) {
            int candidate = RandomUtil.getInstance().nextInt(data.rows());

            if (!indexSet.contains(candidate)) {
                indexSet.add(candidate);
            }
        }

        int[] rows = new int[numCenters];

        int i = -1;

        for (int row : indexSet) {
            rows[++i] = row;
        }

        int[] cols = new int[data.columns()];

        for (int j = 0; j < data.columns(); j++) {
            cols[j] = j;
        }

        return data.viewSelection(rows, cols).copy();
    }

    private double dissimilarity(DoubleMatrix1D d1, DoubleMatrix1D d2) {
        return metric.dissimilarity(d1, d2);
    }

    private DoubleMatrix1D countClusterSizes() {
        DoubleMatrix1D counts = new DenseDoubleMatrix1D(centers.rows());

        for (int cluster : clusters) {
            counts.set(cluster, counts.get(cluster) + 1);
        }

        return counts;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
