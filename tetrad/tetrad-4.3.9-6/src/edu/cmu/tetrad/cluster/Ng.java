package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.AbsoluteErrorLoss;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.*;

/**
 * Implements the "Neural Gas" algorithm. A good reference is Thomas Martinetz,
 * "Neural-Gas" network for vector quantization and its application to
 * time-series prediction., IEEE Transactions on Neural Networks, 4:4 (1993).
 *
 * @author Joseph Ramsey
 */
public class Ng implements ClusteringAlgorithm {

    /**
     * The number of units to pick.
     */
    private int numUnits;

    /**
     * The data, columns as features, rows as cases.
     */
    private DoubleMatrix2D data;

    /**
     * The reference vectors.
     */
    private List<Unit> units;

    /**
     * Initial learning rate. The learning rate will be scheduled according to
     * this and episolonF
     */
    private double epsilonI = 0.05;

    /**
     * Final learning rate. The learning rate will be scheduled according to
     * this and epsilonI.
     */
    private double epsilonF = 6e-4;

    /**
     * Initial lambda--lambda will be scheduled according to this and lambdaF.
     */
    private double lambdaI = .5;

    /**
     * Final lambda-- lambda will be scheduled according to this and lambdaI.
     */
    private double lambdaF = .1;

    /**
     * The maximum iteration for a clustering run; after this it stops.
     */
    private int tMax = 10000;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    /**
     * True if verbose output should be spewed.
     */
    private boolean verbose = true;
    private List<Integer> clusters;

    //============================CONSTRUCTOR==========================//

    /**
     * Private constructor. (Please keep it that way.)
     */
    private Ng() {
    }

    /**
     * Constructs a new NeuralGas algorithm, initializing the algorithm by
     * picking <code>numUnits</code> units from the data itself.
     *
     * @param numUnits The number of Neural Gas units.
     * @return The constructed algorithm.
     */
    public static Ng randomUnitsFromData(int numUnits) {
        Ng algorithm = new Ng();
        algorithm.numUnits = numUnits;
        return algorithm;
    }

    //===========================PUBLIC METHODS=======================//

    public void cluster(DoubleMatrix2D data) {
        units = pickUnits(numUnits, data);
        this.data = data;

        DoubleMatrix1D delta = new DenseDoubleMatrix1D(data.columns());

        int t = 0;

        DoubleMatrix1D e = new DenseDoubleMatrix1D(data.columns());
        e.assign(1);
        Unit d2 = new Unit(e);

        int iteration = -1;

        while (length(d2) * data.columns() > 1e-08) {
            System.out.println("Length = " + length(d2) + " Iteration = " + (++iteration));

//            while (t < tMax) {
//            if (t % 500 == 0) {
                d2 = new Unit(new DenseDoubleMatrix1D(data.columns()));
//            }

            if (isVerbose()) {
                if ((t + 1) % 1000 == 0) System.out.println("Iteration " + (t + 1));
            }

            DoubleMatrix1D signal = randomPoint();

            for (Unit unit : units) {
                unit.calculateDistanceToSignal(signal);
            }

            Collections.sort(units);

            for (int i = 0; i < units.size(); i++) {
                Ng.Unit unit = units.get(i);

                for (int j = 0; j < signal.size(); j++) {
                    delta.set(j,  signal.get(j) - unit.getVector().get(j));
                }

                double multiplier = epsilon(t) * h((i + 1), lambda(t));

                for (int j = 0; j < signal.size(); j++) {
                    delta.set(j, multiplier * delta.get(j));
                }

                unit.moveVector(delta);
                d2.moveVector(delta);
            }

            t++;
        }

        List<Integer> clusters = new ArrayList<Integer>();
        ((ArrayList)clusters).ensureCapacity(data.rows());
        Dissimilarity metric = new AbsoluteErrorLoss();

        for (int i = 0; i < data.rows(); i++) {
            double distance = Double.POSITIVE_INFINITY;
            int cluster = -1;

            for (int k = 0; k < numUnits; k++) {
                double d = metric.dissimilarity(units.get(k).getVector(),
                        data.viewRow(i));
                if (d < distance) {
                    distance = d;
                    cluster = k;
                }
            }

            clusters.add(cluster);
        }

        this.clusters = clusters;
    }

    private double length(Unit d2) {
        DoubleMatrix1D vector = d2.getVector();

        double sum = 0.0;

        for (int i = 0; i < vector.size(); i++) {
            double v = vector.get(i);
            sum += v * v;
        }

        return Math.sqrt(sum);
    }


    public List<List<Integer>> getClusters() {
        return ClusterUtils.convertClusterIndicesToLists(clusters);
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getEpsilonI() {
        return epsilonI;
    }

    public void setEpsilonI(double epsilonI) {
        this.epsilonI = epsilonI;
    }

    public double getEpsilonF() {
        return epsilonF;
    }

    public void setEpsilonF(double epsilonF) {
        this.epsilonF = epsilonF;
    }

    public double getLambdaI() {
        return lambdaI;
    }

    public void setLambdaI(double lambdaI) {
        this.lambdaI = lambdaI;
    }

    public double getLambdaF() {
        return lambdaF;
    }

    public void setLambdaF(double lambdaF) {
        this.lambdaF = lambdaF;
    }

    /**
     * Return the maximum number of iterations, or -1 if the algorithm is
     * allowed to run unconstrainted.
     *
     * @return This value.
     */
    public int getTMax() {
        return tMax;
    }

    /**
     * Sets the maximum number of iterations.
     *
     * @param tMax This value.
     */
    public void setTMax(int tMax) {
        this.tMax = tMax;
    }

    public DoubleMatrix2D getUnitMatrix() {
        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(units.size(), units.get(0).getVector().size());

        for (int i = 0; i < matrix.rows(); i++) {
            for (int j = 0; j < matrix.columns(); j++) {
                matrix.set(i, j, units.get(i).getVector().get(j));
            }
        }

        return matrix;
    }

    /**
     * Returns a string representation of the cluster result.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < units.size(); i++) {
            buf.append("\nUnit " + i + ": " + units.get(i));
        }

        return buf.toString();
    }

    //==========================PRIVATE METHODS===========================//

    private double h(int k, double lambda) {
        return Math.exp(-k / lambda);
    }

    private double lambda(int t) {
        return lambdaI * Math.pow(lambdaF / lambdaI, t / (double) tMax);
    }

    private double epsilon(int t) {
        double epsilon = epsilonI * Math.pow(epsilonF / epsilonI, t / (double) tMax);
//        System.out.println("epsilon = " + epsilon);
        return epsilon;
    }

    // Returns a random point from the data. Note that it is assumed this
    // point will NOT BE ALTERED, as we are not returning a copy.
    private DoubleMatrix1D randomPoint() {
        int i = RandomUtil.getInstance().nextInt(data.rows());
        return data.viewRow(i);
    }

    private List<Unit> pickUnits(int numUnits, DoubleMatrix2D data) {

        // Pick distinct indices from the data.
        SortedSet<Integer> indexSet = new TreeSet<Integer>();

        while (indexSet.size() < numUnits) {
            int candidate = RandomUtil.getInstance().nextInt(data.rows());

            if (!indexSet.contains(candidate)) {
                indexSet.add(candidate);
            }
        }

        List<Unit> units = new ArrayList<Unit>();

        for (int i : indexSet) {
            units.add(new Unit(data.viewRow(i).copy()));
        }

        return units;
    }

    public Dissimilarity getMetric() {
        return metric;
    }

    public void setMetric(Dissimilarity metric) {
        this.metric = metric;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Represents a single unit vector for the neural gas algorithm. It contains
     * a vector, which can be moved using the moveVector command, and a distance
     * to a random vector, which can be set using the calculateDistanceToSignal
     * method. When Units in a List are sorted, they are sorted by increasing
     * distance to the signal, according to the most recent signals set.
     *
     * @author Joseph Ramsey
     */
    private class Unit implements Comparable {
        private DoubleMatrix1D vector;
        private double distance = Double.NaN;

        public Unit(DoubleMatrix1D unitVector) {
            this.vector = unitVector;
        }

        public void moveVector(DoubleMatrix1D delta) {
            for (int i = 0; i < vector.size(); i++) {
                vector.set(i, vector.get(i) + delta.get(i));
            }
        }

        public void calculateDistanceToSignal(DoubleMatrix1D signal) {
            distance = distance(vector, signal);
        }

        /**
         * Returns the squared norm distance.
         */
        private double distance(DoubleMatrix1D vector, DoubleMatrix1D signal) {
            return getMetric().dissimilarity(vector, signal);
        }

        public int compareTo(Object o) {
            Unit other = (Unit) o;
            return (int) Math.signum(this.distance - other.distance);
        }

        public DoubleMatrix1D getVector() {
            return vector;
        }

        public String toString() {
            return "" + vector;
        }
    }
}
