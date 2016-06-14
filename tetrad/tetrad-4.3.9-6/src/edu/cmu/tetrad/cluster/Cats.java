package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.util.ProbUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the CATS algorithm--i.e. "Clustering after transformation and
 * Smoothing"--by Nicoleta Serban and Larry Wasserman.
 *
 * @author Joseph Ramsey
 */
public class Cats implements ClusteringAlgorithm {
    private static double SQRT2 = Math.sqrt(2);

    /**
     * The data. It is assumed that all of the columns in the data are
     * relevant to clustering.
     */
    private DoubleMatrix2D Y;

    /**
     * True if FDR screening should be done to eliminate flat curves.
     * (Expensive.)
     */
    private boolean fdrScreeningDone = false;

    /**
     * True if confidence ball screening should be done to eliminate flat
     * curves.
     */
    private boolean confidenceBallScreeningDone = false;

    /**
     * True if verbose output should be generated.
     */
    private boolean verbose = true;

    /**
     * True just in case fHat should be calculated--that is, the matrix in which
     * Y has been premultiplied by the orthogonalization of the cosine basis.
     * This is not needed in the algorithm but will be saved for retrieval if
     * this field is set to true.
     */
    private boolean fHatCalculated = false;

    /**
     * The cluster index for each case.
     */
    private List<List<Integer>> clusters;

    /**
     * The clustering algorithm to use to cluster thetaTilde.
     */
    private ClusteringAlgorithm clusteringAlgorithm;

    /**
     * The final transformed data. Euclidean distance between rows in this
     * data should correspond to correlation between rows for Y.
     */
    private DoubleMatrix2D thetaTilde;

    /**
     * The smoothed data, if it is calculated.
     */
    private DoubleMatrix2D fHat;

    //=========================CONSTRUCTORS==============================//

    /**
     * Constructs a new Cats algorithm. The parameter kMax is the maximum
     * number of "harnomics" in the cosine basis.
     */
    public Cats() {
        setClusteringAlgorithm(KMeans.randomPoints(15));
    }

    /**
     * Calculates the transformed data and then runs the clustering algorithm
     * on it.
     */
    public void cluster(DoubleMatrix2D data) {
        this.Y = data;
        int n = Y.rows();
        int m = Y.columns();

        DoubleMatrix2D thetaTilde = calculateTransformedData(m, n);

        // Do K Means in the fourier domain.
        System.out.println("Starting K Means");

        ClusteringAlgorithm algorithm = getClusteringAlgorithm();
        algorithm.setVerbose(isVerbose());
        algorithm.cluster(thetaTilde);
        clusters = algorithm.getClusters();
    }

    //==========================PUBLIC METHODS===========================//

    public boolean isFdrScreeningDone() {
        return fdrScreeningDone;
    }

    public void setFdrScreeningDone(boolean fdrScreeningDone) {
        this.fdrScreeningDone = fdrScreeningDone;
    }

    public boolean isConfidenceBallScreeningDone() {
        return confidenceBallScreeningDone;
    }

    public void setConfidenceBallScreeningDone(boolean confidenceBallScreeningDone) {
        this.confidenceBallScreeningDone = confidenceBallScreeningDone;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;

        if (this.clusteringAlgorithm != null) {
            this.clusteringAlgorithm.setVerbose(isVerbose());
        }
    }

    public boolean isFHatCalculated() {
        return fHatCalculated;
    }

    public void setFHatCalculated(boolean fHatCalculated) {
        this.fHatCalculated = fHatCalculated;
    }

    public DoubleMatrix2D getFHat() {
        return this.fHat;
    }

    public ClusteringAlgorithm getClusteringAlgorithm() {
        return clusteringAlgorithm;
    }

    public void setClusteringAlgorithm(ClusteringAlgorithm clusteringAlgorithm) {
        this.clusteringAlgorithm = clusteringAlgorithm;
        this.clusteringAlgorithm.setVerbose(isVerbose());
    }

    public DoubleMatrix2D getThetaTilde() {
        return thetaTilde;
    }

    public List<List<Integer>> getClusters() {
        return clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        return null;
    }

    /**
     * Calculates the transformed data.
     */
    public DoubleMatrix2D calculateTransformedData(int m, int n) {
        thetaTilde = null;

        // Transform data into the Fourier domain.
        // Phi has phi_J as rows, m time points as columns.
        DoubleMatrix2D phi = calculatePhi(m, m);
        phi = grahamSchmidt(phi);

        final DoubleMatrix2D thetaHat = calculateThetaHat(Y, phi);
        DoubleMatrix1D sigmaHatSquared = calculateSigmaHatSquared(thetaHat);
        DoubleMatrix2D RHat = calculateRHat(sigmaHatSquared, thetaHat);
        DoubleMatrix2D rHat = calculateLittleRHat(RHat);
        DoubleMatrix1D t = calculateT(rHat);
        int JHat = calculateJHat(m, t);

        if (isFHatCalculated()) {
            calculateFHat(n, m, JHat, phi);
        }

        if (isFdrScreeningDone()) {
            screenFlatCurvesFdr(n, m, thetaHat);
        }

        // Screen out guys that are flat in the sense that if you construct
        // a confidence ball about them, the origin is in the confidence ball.
        if (isConfidenceBallScreeningDone()) {
            screenFlatCurvesConfidenceBalls(n, sigmaHatSquared, m, JHat, thetaHat);
        }

        thetaTilde = calculateThetaTilde(thetaHat, n, JHat);
        return getThetaTilde();
    }

    //==========================PRIVATE METHODS=========================//

    private DoubleMatrix2D calculatePhi(int m, int kMax) {
        if (isVerbose()) {
            System.out.println("Calculating phi.");
        }

        DoubleMatrix2D phi = new DenseDoubleMatrix2D(m, kMax);

        for (int i = 0; i < m; i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            for (int j = 0; j < m; j++) {
                if (j == 0) {
                    phi.set(i, j, 1.0);
                } else {
                    phi.set(i, j, phi(j, t(i, m)));
                }
            }
        }

        return phi;
    }

    /**
     * Returns Graham-Schmidt on thetaHat, returning the orthogonalized matrix.
     */
    private DoubleMatrix2D grahamSchmidt(DoubleMatrix2D thetaHat) {
        Algebra a = new Algebra();

        DoubleMatrix2D u = new DenseDoubleMatrix2D(thetaHat.rows(), thetaHat.columns());
        u.viewColumn(0).assign(thetaHat.viewColumn(0));
        u.viewColumn(0).assign(Mult.div(length(u.viewColumn(0))));

        for (int j1 = 1; j1 < thetaHat.columns(); j1++) {
            u.viewColumn(j1).assign(thetaHat.viewColumn(j1));

            for (int j2 = 0; j2 < j1 - 1; j2++) {
                double v1 = a.mult(u.viewColumn(j2), thetaHat.viewColumn(j1));
                double v2 = a.mult(u.viewColumn(j2), u.viewColumn(j2));
                double v3 = v1 / v2;
                u.viewColumn(j1).assign(u.viewColumn(j2), PlusMult.plusMult(-v3));
            }

            u.viewColumn(j1).assign(Mult.div(length(u.viewColumn(j1))));
        }

        return u;
    }

    private double length(DoubleMatrix1D u) {
        double sum = 0.0;

        for (int i = 0; i < u.size(); i++) {
            sum += u.get(i) * u.get(i);
        }

        return Math.sqrt(sum);
    }

    private DoubleMatrix2D calculateThetaHat(DoubleMatrix2D Y, DoubleMatrix2D phi) {
        if (isVerbose()) {
            System.out.println("Calculating theta hat.");
        }
//
//        DoubleMatrix2D thetaHat = new DenseDoubleMatrix2D(Y.rows(), phi.columns());
//
//        for (int i = 0; i < Y.rows(); i++) {
//            if (i % 1000 == 0) System.out.println(i);
//
//            for (int j = 0; j < phi.columns(); j++) {
//                double sum = 0.0;
//
//                for (int k = 0; k < Y.columns(); k++) {
//                    sum += Y.get(i, k) * phi.get(k, j);
//                }
//
//                thetaHat.setQuick(i, j, sum);
//            }
//        }

        DoubleMatrix2D thetaHat = new Algebra().mult(Y, phi);
        thetaHat.assign(Mult.div(Y.columns()));
        return thetaHat;
    }

    private DoubleMatrix1D calculateSigmaHatSquared(DoubleMatrix2D thetaHat) {
        if (isVerbose()) {
            System.out.println("Calculating sigma hat squared.");
        }

        int n = thetaHat.rows();
        int m = thetaHat.columns();

        int L = m / 3;
        DoubleMatrix1D sigmaHatSquared = new DenseDoubleMatrix1D(n);

        for (int i = 0; i < n; i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            double sum = 0.0;

            for (int j = L; j < m; j++) {
                double v = thetaHat.get(i, j);
                sum += v * v;
            }

            sum *= m / (double) (m - L);
            sigmaHatSquared.set(i, sum);
        }

        return sigmaHatSquared;
    }

    private void screenFlatCurvesFdr(int n, int m, DoubleMatrix2D thetaHat) {
        if (isVerbose()) {
            System.out.println("Screening out flat curves using FDR.");
        }

        DoubleMatrix1D T = new DenseDoubleMatrix1D(n);

        for (int i = 0; i < n; i++) {
            double sum = 0.0;

            for (int j = 1; j < m; j++) {
                double v = thetaHat.get(i, j);
                sum += v * v;
            }

            T.set(i, sum);
        }

        int[] sumB = new int[n];

        List<Integer> indices = new ArrayList<Integer>();

        for (int i = 0; i < n; i++) {
            indices.add(i);
        }

        for (int p = 0; p < 10000; p++) {
            if (isVerbose()) {
                System.out.println("Shuffle " + p);
            }

            Collections.shuffle(indices);

            for (int i = 0; i < n; i++) {
                double tStar = 0.0;

                for (int j = 1; j < m; j++) {
                    double v = thetaHat.get(indices.get(i), j);
                    tStar += v * v;
                }

                if (tStar > T.get(i)) {
                    sumB[i] += 1;
                }
            }
        }

        List<Double> pValues = new ArrayList<Double>();

        for (int i = 0; i < n; i++) {
            pValues.add((1 / (double) 10000) * sumB[i]);
        }

        List<Double> sortedPValues = new ArrayList<Double>(pValues);
        sortedPValues.add(0, 0.0);

        Collections.sort(sortedPValues);
        System.out.println("P values sorted " + sortedPValues);

        int _j = n - 1;
        double alpha = 0.05;

        for (int i = n; i >= 0; i--) {
            double cutoff = (i * alpha) / n;
            Double ithPValue = sortedPValues.get(i);

            if (ithPValue <= cutoff) {
                _j = i;
                break;
            }
        }

        double T0 = sortedPValues.get(_j);

        List<Integer> rowMask = new ArrayList<Integer>();

        // Reject if pValues.get(i) <= T0.
        for (int i = 0; i < n; i++) {
            if (pValues.get(i) >= T0) {
                rowMask.add(i);
            } else {
                System.out.println("Leaving out " + i);
            }
        }

        int[] _rowMask = new int[rowMask.size()];

        for (int i = 0; i < rowMask.size(); i++)
            _rowMask[i] = rowMask.get(i);

        int[] allColumns = new int[m];
        for (int i = 0; i < m; i++) allColumns[i] = i;

        Y.viewSelection(_rowMask, allColumns);
    }

    private void screenFlatCurvesConfidenceBalls(int n, DoubleMatrix1D sigmaHatSquared, int m, int JHat, DoubleMatrix2D thetaHat) {
        for (int i = 0; i < n; i++) {
            if (isVerbose()) {
                System.out.println("Screening out flat curves using confidence balls.");
            }

            double sigmaSquared = sigmaHatSquared.get(i);
            double sum1 = 0.0;

            for (int j = 0; j < m; j++) {
                int c = j >= JHat ? 1 : 0;
                sum1 += 1.0;
                sum1 += (1 - 2 * c) / (double) (m - JHat);
                double theta = theta(j, m, thetaHat);
                sum1 *= 4 * (theta - sigmaSquared) + sigmaSquared + 2 * sigmaSquared * sigmaSquared;
            }

            double sum2 = 0.0;

            for (int j = 0; j < m; j++) {
                int c = j >= JHat ? 1 : 0;
                double theta = theta(j, m, thetaHat);
                sum2 += (2 * c / (double) (m - JHat)) * positive(theta - sigmaSquared);
            }

            sum2 *= 4 * sigmaSquared;

            double tau = Math.sqrt(sum1 + sum2);
            double quantile = ProbUtils.normalQuantile(.05 / n);

            double _rHat = (JHat * sigmaSquared / m);

            for (int j = JHat; j < m; j++) {
                _rHat += theta(j, m, thetaHat);
                _rHat += sigmaSquared / m;
            }

            double sSquared = (quantile * tau / Math.sqrt(m)) + _rHat;
            DoubleMatrix1D v = thetaHat.viewRow(i);
            double distanceFromZero = 0.0;

            for (int j = 0; j < m; j++) {
                double a = v.get(j);
                distanceFromZero += a * a;
            }

            if (distanceFromZero < sSquared) {
                System.out.println("Get rid of " + i + "!");
            }
        }
    }

    private DoubleMatrix2D calculateRHat(DoubleMatrix1D sigmaHatSquared, DoubleMatrix2D thetaHat) {
        if (isVerbose()) {
            System.out.println("Calculating R hat.");
        }

        int n = thetaHat.rows();
        int m = thetaHat.columns();

        DoubleMatrix2D RHat = new DenseDoubleMatrix2D(n, m);

        for (int i = 0; i < n; i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            for (int J = 0; J < m; J++) {
                double sum = (J * sigmaHatSquared.get(i)) / m;

                for (int j = J; j < m; j++) {
                    double v = thetaHat.get(i, j);
                    double value = v * v - sigmaHatSquared.get(i) / m;
                    sum += positive(value);
                }

                RHat.set(i, J, sum);
            }
        }

        return RHat;
    }

    private DoubleMatrix2D calculateLittleRHat(DoubleMatrix2D RHat) {
        if (isVerbose()) {
            System.out.println("Calculating r hat.");
        }

        int n = RHat.rows();
        int m = RHat.columns();

        DoubleMatrix2D rHat = new DenseDoubleMatrix2D(n, m);

        for (int i = 0; i < n; i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            double min = Double.POSITIVE_INFINITY;

            for (int j = 0; j < m; j++) {
                if (RHat.get(i, j) < min) min = RHat.get(i, j);
            }

            for (int j = 0; j < RHat.columns(); j++) {
                rHat.set(i, j, RHat.get(i, j) - min);
            }
        }
        return rHat;
    }

    private DoubleMatrix1D calculateT(DoubleMatrix2D rHat) {
        if (isVerbose()) {
            System.out.println("Calculating t.");
        }

        int n = rHat.rows();
        int m = rHat.columns();

        DoubleMatrix1D t = new DenseDoubleMatrix1D(m);

        for (int J = 0; J < m; J++) {
            double sum = 0.0;

            for (int i = 0; i < n; i++) {
                sum += rHat.get(i, J);
            }

            t.set(J, sum);
        }

        return t;
    }

    private int calculateJHat(int m, DoubleMatrix1D t) {
        if (isVerbose()) {
            System.out.println("Calculating J hat.");
        }

        int JHat = Integer.MAX_VALUE;
        double minT = Double.POSITIVE_INFINITY;

        for (int J = 0; J < m; J++) {
            if (t.get(J) < minT) {
                minT = t.get(J);
                JHat = J;
            }
        }

        if (isVerbose()) {
            System.out.println("JHat = " + JHat);
        }

        return JHat;
    }

    private void calculateFHat(int n, int m, int JHat, DoubleMatrix2D phi) {
        if (isVerbose()) {
            System.out.println("Calculating f hat.");
        }

//        DoubleMatrix2D fHat = new Algebra().mult(Y, phi.viewDice());
//        fHat.assign(Mult.div(m));

        DoubleMatrix2D fHat = new DenseDoubleMatrix2D(n, m);

        for (int i = 0; i < n; i++) {
            if (isVerbose()) {
                if ((i + 1) % 1000 == 0) System.out.println(i + 1);
            }

            for (int r = 0; r < m; r++) {
                double sum = 0.0;

                for (int j = 0; j <= JHat; j++) {
                    sum += Y.get(i, j) * phi.get(r, j);
                }

                sum /= m;
                fHat.set(i, r, sum);
            }
        }

        this.fHat = fHat;
    }

    private double theta(int j, int m, DoubleMatrix2D thetaHat) {
        double theta = 0.0;

        for (int r = 0; r < m; r++) {
            theta += thetaHat.get(r, j);
        }

        return theta;
    }

    private DoubleMatrix2D calculateThetaTilde(DoubleMatrix2D thetaHat, int n, int JHat) {
        if (isVerbose()) {
            System.out.println("Restricting theta to smoothed components.");
        }

        // Not sure whether normalization should be done here before or after
        // truncating columns to JHat.

        int m0 = thetaHat.columns() - 1;
        DoubleMatrix2D thetaHatRestriction = thetaHat.viewPart(0, 1, n, m0);

        for (int i = 0; i < n; i++) {
            DoubleMatrix1D v = thetaHatRestriction.viewRow(i);
            double sum = 0.0;

            for (int j = 0; j < m0; j++) {
                double vj = v.get(j);
                sum += vj * vj;
            }

            double norm = Math.sqrt(sum);
            v.assign(Mult.div(norm));
        }

        thetaHatRestriction = thetaHatRestriction.viewPart(0, 0, n, JHat);
        return thetaHatRestriction;
    }

    private double t(int i, int n) {
        return i / (double) (n - 1);
    }

    private double phi(int j, double t) {
        if (!(t >= 0 && t <= 1)) {
            throw new IllegalArgumentException("t must be in [0, 1]: " + t);
        }

        return SQRT2 * Math.cos(j * Math.PI * t);
    }

    private double positive(double value) {
        return value > 0 ? value : 0;
    }
}
