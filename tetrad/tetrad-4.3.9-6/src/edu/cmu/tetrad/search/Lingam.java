package edu.cmu.tetrad.search;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;
import cern.jet.random.ChiSquare;
import cern.jet.random.Normal;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.cluster.FastIca;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Implements the LiNGAM algorithm in Shimizu, Hoyer, Hyvarinen, and Kerminen, A
 * linear nongaussian acyclic model for causal discovery, JMLR 7 (2006).
 *
 * @author Gustavo Lacerda Original Java code.
 * @aurhor Joseph Ramsey refactored code.
 */
public class Lingam {

    /**
     * The alpha level for the bootstrap sampling procedure.
     */
    private double alpha;

    /**
     * True if the pruning steps are done. Defaiult = true.
     */
    private boolean pruningDone = true;

    /**
     * True if output is printed to the console.
     */
    private boolean verbose = false;

    /**
     * True if the upper triangle of linear parameters is kept.
     */
    private boolean upperTriangleKept = true;

    //================================CONSTRUCTORS==========================//

    /**
     * Constructs a new LiNGAM algorithm with the given alpha level (used for
     * pruning).
     */
    public Lingam() {
    }

    //================================PUBLIC METHODS========================//

    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha is in range [0, 1]: " + alpha);
        }

        this.alpha = alpha;
    }

    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Runs the LiNGAM algorithm on gthe given data, producing a DAG with edge
     * coefficient parameters.
     */
    public GraphWithParameters lingam(DataSet dataSet) {
        printAndersonDarlingPs(dataSet);

        DoubleMatrix2D data = dataSet.getDoubleData();
        List<Node> variables = dataSet.getVariables();
        Data result = algorithmA(new Data(data, variables));
//        System.out.println(result);

//        System.out.println(result.getSimulatedData());
        return makeDagWithParms(result);
    }

    private void printAndersonDarlingPs(DataSet dataSet) {
        System.out.println("Anderson Darling P value for (Processed) Variables\n");

        NumberFormat nf = new DecimalFormat("0.0000");
        DoubleMatrix2D m = dataSet.getDoubleData();

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            double[] x = m.viewColumn(j).toArray();
            double p = new AndersonDarlingTest(x).getP();
            System.out.println("For " + dataSet.getVariable(j) +
                    ", Anderson-Darling p = " + nf.format(p)
                    + (p > 0.05 ? " = Gaussian" : " = Nongaussian"));
        }

    }

    /**
     * Returns true iff the pruning step should be done.
     */
    public boolean isPruningDone() {
        return pruningDone;
    }

    /**
     * Set to true just in case pruning should be done.
     */
    public void setPruningDone(boolean pruningDone) {
        this.pruningDone = pruningDone;
    }

    /**
     * Returns true just in case verbose output should be done.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Set to true just in case verbose output should be done.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns true just in case the upper triangle of coefficients should be
     * kept.
     */
    public boolean isUpperTriangleKept() {
        return upperTriangleKept;
    }

    /**
     * Set to true just in case the upper triangle of coefficients should be
     * kept.
     */
    public void setUpperTriangleKept(boolean upperTriangleKept) {
        this.upperTriangleKept = upperTriangleKept;
    }

    /**
     * Returns a string representation of this algorithm.
     */
    public String toString() {
        return "Lingam(alpha=" + getAlpha() + ")";
    }

    //===============================PRIVATE METHODS========================//

    /**
     * Estimates BHat, permutes it to the best lower triangular approximation,
     * and prunes it.
     *
     * @param data The data that algorithm A is to be run on.
     * @return a pattern with parameters for each edge.
     */
    private Data algorithmA(Data data) {

        // Steps A1-A4
        Data BHat = calculateBHat(data);

        // Step A5.

        // Permute BHat into the best lower triangular approximation. All
        // edges are represented.
        Data BTilde = algorithmC(BHat);

        // Prune edges from BHat so that a DAG can be returned.
        if (isPruningDone()) {
            int nSamples = 15;
            pruneEdgesBySampling(BTilde, data, getAlpha(), nSamples);
//            pruneEdgesByRegression(BTilde, data, getAlpha());
//            pruneEdgesByRegression2(BTilde, data, getAlpha());
//            pruneEdgesByResampling2(BTilde, data, getAlpha());
        }

        return BTilde;
    }

    /**
     * Steps A1 through A4.
     */
    private Data calculateBHat(Data data) {

        // Step A1. Calculate W.
        // Data is centered by ICA.
        DoubleMatrix2D A = getMixingMatrix(data.getData());
        DoubleMatrix2D W = MatrixUtils.inverse(A);

        // Step A2. Find the permutation W~ of W that has no zeroes on the diagonal.
        DoubleMatrix2D wTilde = permuteZerolessDiagonal(W);

        // Step A3. Normalize each row by the diagonal element, yielding a
        // matrix W~' with all 1's on the diagonal.
        DoubleMatrix2D wTildePrime = MatrixUtils.normalizeDiagonal(wTilde);

        // Step A4. Compute an estimate of BHat as I - W~'.
        int n = W.rows();
        DoubleMatrix2D BHat = DoubleFactory2D.dense.identity(n);
        BHat.assign(wTildePrime, PlusMult.plusMult(-1));
        DoubleMatrix2D BhatT = BHat.viewDice();

        // Bhat corresponds to a complete graph
        return new Data(BhatT, data.getVariables());
    }

    /**
     * Swaps the rows and columns of bHat simultaneously to produce a
     * lower-triangular matrix.
     *
     * @throws IllegalArgumentException if a permutation resulting in a lower
     *                                  triangular matrix cannot be found.
     */
    private Data algorithmC(Data bHat) {
        DoubleMatrix2D _bHat = bHat.getData();
        int m = _bHat.rows();

        LinkedList<Entry> entries = getEntries(_bHat);

        // Sort entries by absolute value.
        java.util.Collections.sort(entries);

        // Set the smallest m(m+1)/2 to zero.
        if (isUpperTriangleKept()) {
            _bHat = _bHat.copy();
        }

        int numUpperTriangle = m * (m + 1) / 2;
        int numTotal = m * m;

        for (int i = 0; i < numUpperTriangle; i++) {
            Entry entry = entries.get(i);
            _bHat.set(entry.row, entry.column, 0);
        }

        // If that doesn't result in a permutation, try setting one more entry
        // to zero, iteratively, until you get a permutation.
        for (int i = numUpperTriangle; i < numTotal; i++) {
            List<Integer> permutation = algorithmB(_bHat);

            if (permutation != null) {
                return permute(permutation, bHat);
            }

            Entry entry = entries.get(i);
            _bHat.set(entry.row, entry.column, 0);
        }

        throw new IllegalArgumentException("No permutation was found.");
    }

    /**
     * Uses the Hungarian algorithm to permute the given matrix so that it has
     * no zeroes on the diagonal.
     */
    private DoubleMatrix2D permuteZerolessDiagonal(DoubleMatrix2D mat) {

        // The method that calls assign() twice could be a problem for the
        // negative coefficients
        DoubleMatrix2D temp = new DenseDoubleMatrix2D(mat.rows(), mat.columns());
        temp.assign(mat);
        temp.assign(cern.jet.math.Functions.inv);
        temp.assign(cern.jet.math.Functions.abs);

        //this is an n x 2 matrix, i.e. a list of index pairs
        int[][] assignment = Hungarian.hgAlgorithm(MatrixUtils.convert(temp), "min");

        return assignmentToMatrix(mat, assignment);
    }

    /**
     * Returns a matrix in which elements are swapped according to the given
     * assignment.
     */
    private DoubleMatrix2D assignmentToMatrix(DoubleMatrix2D mat, int[][] assignment) {
        DoubleMatrix2D swappedMat = new DenseDoubleMatrix2D(mat.rows(), mat.columns());

        for (int i = 0; i < mat.rows(); i++) {
            int newRowIndex = assignment[i][1];
            swappedMat.viewRow(newRowIndex).assign(mat.viewRow(i));
        }

        return swappedMat;
    }

    /**
     * Finds the mixing matrix using the R translation of FastICA.
     */
    private DoubleMatrix2D getMixingMatrix(DoubleMatrix2D data0) {
        FastIca fastIca = new FastIca(data0, data0.columns());
        fastIca.setVerbose(false);
        fastIca.setAlgorithmType(FastIca.DEFLATION);
        fastIca.setFunction(FastIca.LOGCOSH);
        fastIca.setTolerance(1e-26);
        FastIca.IcaResult result = fastIca.findComponents();
        return result.getA().viewDice();
    }

    /**
     * If <code>mat</code> can be permuted into a lower-triangular matrix, it
     * returns such a permutation; otherwise, it returns null.
     */
    public List<Integer> algorithmB(DoubleMatrix2D mat) {
        List<Integer> removedIndices = new ArrayList<Integer>();
        List<Integer> permutation = new ArrayList<Integer>();

        while (removedIndices.size() < mat.rows()) {
            int allZerosRow = -1;

            // Find a new row with zeroes in new columns.
            for (int i = 0; i < mat.rows(); i++) {
                if (removedIndices.contains(i)) {
                    continue;
                }

                if (zeroesInNewColumns(mat.viewRow(i), removedIndices)) {
                    allZerosRow = i;
                    break;
                }
            }

            // No such row.
            if (allZerosRow == -1) {
                return null;
            }

            removedIndices.add(allZerosRow);
            permutation.add(allZerosRow);
        }

        return permutation;
    }

    /**
     * Returns a list of the individual indexed entries for the given matrix.
     */
    private LinkedList<Entry> getEntries(DoubleMatrix2D mat) {
        LinkedList<Entry> entries = new LinkedList<Entry>();

        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.columns(); j++) {
                Entry entry = new Entry(i, j, mat.get(i, j));
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * Permutes rows, columns, and variables of the given dataset according to
     * the given permutation order.
     */
    private Data permute(List<Integer> permutation, Data dataSet) {
        List<Node> variables = dataSet.getVariables();
        List<Node> nodes = new ArrayList<Node>(variables.size());

        for (int i1 = 0; i1 < variables.size(); i1++) {
            nodes.add(variables.get(permutation.get(i1)));
        }

        int[] _permutation = new int[permutation.size()];

        for (int i = 0; i < permutation.size(); i++) {
            _permutation[i] = permutation.get(i);
        }

        DoubleMatrix2D resMatrix = dataSet.getData().viewSelection(_permutation, _permutation);
        return new Data(resMatrix, nodes);
    }

    /**
     * Returns true iff all entries in the row except for the ones corresponding
     * to removed indices are zero.
     */
    private boolean zeroesInNewColumns(DoubleMatrix1D vec, List<Integer> removedIndices) {
        for (int i = 0; i < vec.size(); i++) {
            if (vec.get(i) != 0 && !removedIndices.contains(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Turns a lower-triangular matrix into a DAG.
     */
    private GraphWithParameters makeDagWithParms(Data ltDataSet) {

        // Lower-triangular matrix
        DoubleMatrix2D ltMat = ltDataSet.getData();

        List<Node> variables = ltDataSet.getVariables();
        Dag dag = new Dag(variables);

        GraphWithParameters dwp = new GraphWithParameters(dag);

        // Adding edges to 'dag', and populating 'weightHash'
        for (int i = 0; i < ltMat.rows(); i++) {
            for (int j = 0; j < i; j++) {
                if (ltMat.get(i, j) != 0) {
                    Edge edge = new Edge(variables.get(i), variables.get(j),
                            Endpoint.TAIL, Endpoint.ARROW);
                    dwp.getGraph().addEdge(edge);
                    dwp.getWeightHash().put(edge, ltMat.get(i, j));
                }
            }
        }

        return dwp;
    }

    /**
     * Given a data set, this method prunes edges that don't pass the
     * significance test.
     */
    private void pruneEdgesBySampling(Data BTilde, Data data, double alpha,
                                      int numSamples) {

        // We have one B-hat for each piece of the data
        Data[] bHats = new Data[numSamples];

        // Calculate bHat for each bootstrap samples.
        for (int i = 0; i < numSamples; i++) {
            Data sample = getBootstrapSample(data, data.getData().rows() / 2);
            bHats[i] = calculateBHat(sample);
        }

        // For each entry in Bhat, record its mean and s.d.

        // Mean
        DoubleMatrix2D meanMat = new DenseDoubleMatrix2D(BTilde.getData().rows(),
                BTilde.getData().columns());

        for (int i = 0; i < BTilde.getData().rows(); i++)
            for (int j = 0; j < BTilde.getData().columns(); j++) {
                double sum = 0;
                for (int z = 0; z < numSamples; z++)
                    sum += bHats[z].getData().get(i, j);
                meanMat.set(i, j, sum / (numSamples - 1));
            }

        if (isVerbose()) {
            System.out.println("meanMat = " + meanMat);
        }

        // Standard error of each coefficient
        DoubleMatrix2D sdMat = new DenseDoubleMatrix2D(BTilde.getData().rows(),
                BTilde.getData().columns());
        for (int i = 0; i < BTilde.getData().rows(); i++) {
            for (int j = 0; j < BTilde.getData().columns(); j++) {
                double sumOfSquares = 0;

                for (int z = 0; z < numSamples; z++) {
                    double diff = bHats[z].getData().get(i, j) - meanMat.get(i, j);
                    sumOfSquares += diff * diff;
                }

                double variance = sumOfSquares / (numSamples - 1);
                sdMat.set(i, j, Math.sqrt(variance));
            }
        }

        if (isVerbose()) {
            System.out.println("sdMat = " + sdMat);
        }

        for (int i = 0; i < BTilde.getData().rows(); i++) {
            for (int j = 0; j < BTilde.getData().columns(); j++) {

                // if don't reject the zero hypothesis...
                double score = new Normal(meanMat.get(i, j), sdMat.get(i, j), null).cdf(0.0);
                boolean rejected = (score < alpha / 2.0) || (score > 1.0 - (alpha) / 2.0);

                List<Node> swappedVariables = BTilde.getVariables();
                List<Node> originalVariables = data.getVariables();

                //gives us the location of the node named "X_i+1"
                int xi = findColumn(i, swappedVariables, originalVariables);
                int xj = findColumn(j, swappedVariables, originalVariables);

                if (!rejected) {
                    //set it to zero
                    BTilde.getData().set(xi, xj, 0);
                }
//                else {
//                    BTilde.getSimulatedData().set(xi, xj, meanMat.get(i, j));
//                }
            }
        }
    }

    private void pruneEdgesByRegression(Data BTilde, Data data, double alpha) {
        Dag graph = new Dag(makeDagWithParms(BTilde).getGraph());
        List<Node> variables = new ArrayList<Node>();

        for (Node node : BTilde.getVariables()) {
            variables.add(graph.getNode(node.getName()));
        }

        DataSet _data = ColtDataSet.makeContinuousData(variables, data.getData());
        Regression regression = new RegressionDataset(_data);

        for (Node y : graph.getNodes()) {
            List<Node> parents = graph.getParents(y);

            if (parents.isEmpty()) {
                continue;
            }

            RegressionResult result = regression.regress(y, parents);

            for (int i = 0; i < parents.size(); i++) {
                double p = result.getP()[i];

//                double se = result.getSe()[i];
                int from = variables.indexOf(parents.get(i));
                int to = variables.indexOf(y);
//                double value = BTilde.getData().get(from, to);
//                double wald = (value * value) / (se * se);
//                double score = new ChiSquare(1 + parents.size() + 2, null).cdf(wald);
//                boolean rejected = score > 1.0 - alpha;
//
//                if (!rejected) {
//                    BTilde.getData().set(from, to, 0);
//                }

                if (p > alpha) BTilde.getData().set(from, to, 0);
            }
        }
    }

    private void pruneEdgesByRegression2(Data BTilde, Data data, double alpha) {
        Dag graph = new Dag(makeDagWithParms(BTilde).getGraph());
        List<Node> variables = new ArrayList<Node>();

        for (Node node : BTilde.getVariables()) {
            variables.add((ContinuousVariable) graph.getNode(node.getName()));
        }

        for (Node y : graph.getNodes()) {
            List<Node> parents = graph.getParents(y);

            if (parents.isEmpty()) {
                continue;
            }

            int numSamples = 15;
            double[][] values = new double[parents.size()][numSamples];

            for (int j = 0; j < numSamples; j++) {
//                Data bootstrap = getBootstrapSample(data, data.getData().rows() / 2);

//                DataSet _data = ColtDataSet.makeContinuousData(variables, bootstrap.getData());
//                Regression regression = new RegressionDataset(_data);

//                RegressionResult result = regression.regress(y, parents);

                for (int i = 0; i < parents.size(); i++) {
                    int from = variables.indexOf(parents.get(i));
                    int to = variables.indexOf(y);
                    double value = BTilde.getData().get(from, to);
                    values[i][j] = value;
                }
            }

            for (int i = 0; i < parents.size(); i++) {
                double mean = 0.0;

                for (int j = 0; j < numSamples; j++) {
                    mean += values[i][j];
                }

                mean /= numSamples;

                double var = 0.0;

                for (int j = 0; j < numSamples; j++) {
                    var += Math.pow(values[i][j] - mean, 2);
                }

                var /= numSamples;

                int from = variables.indexOf(parents.get(i));
                int to = variables.indexOf(y);
                double value = BTilde.getData().get(from, to);
                double wald = (value * value) / var;
                double score = new ChiSquare(1 + parents.size() + 2, null).cdf(wald);
                boolean rejected = score > 1.0 - alpha;

                if (!rejected) {
                    BTilde.getData().set(from, to, 0);
                }
            }
        }
    }

    private void pruneEdgesByResampling2(Data BTilde, Data data, double alpha) {
        System.out.println("BTilde " + BTilde);

        int numSamples = 100;
        int bootstrapSize = data.getData().rows();

        List<Node> originalVariables = data.getVariables();
        List<Node> swappedVariables = BTilde.getVariables();

//        System.out.println("Original vars = " + originalVariables);
//        System.out.println("Swapped vars = " + swappedVariables);


        int dims = data.getData().columns();

        int[] k = new int[dims];

        for (int j = 0; j < k.length; j++) {
            k[j] = findColumn(j, swappedVariables, originalVariables);
        }

        int[] ik = new int[dims];

        for (int j = 0; j < k.length; j++) {
            ik[j] = findColumn(j, originalVariables, swappedVariables);
        }

        int[] allRows = new int[bootstrapSize];
        for (int i = 0; i < allRows.length; i++) allRows[i] = i;

        DoubleMatrix2D[] Bpieces = new DoubleMatrix2D[numSamples];
        DoubleMatrix1D[] diststdpieces = new DoubleMatrix1D[numSamples];
        DoubleMatrix1D[] cpieces = new DoubleMatrix1D[numSamples];

        for (int s = 0; s < numSamples; s++) {

            // Select the subset of data, and permute the variables to the causal order.
            Data bootstrap = getBootstrapSample(data, bootstrapSize);
            DoubleMatrix2D Xp = bootstrap.getData().viewSelection(allRows, k).copy();
            DoubleMatrix1D Xpm = new DenseDoubleMatrix1D(dims);

            // Remember to subtract out the mean.
            for (int j = 0; j < dims; j++) {
                DoubleArrayList column = new DoubleArrayList();

                for (int i = 0; i < bootstrapSize; i++) {
                    column.add(Xp.get(i, j));
                }

                double mean = Descriptive.mean(column);
                double std = Descriptive.standardDeviation(Descriptive.variance(bootstrapSize,
                        Descriptive.sum(column), Descriptive.sumOfSquares(column)));

                Xpm.set(j, mean);

                for (int i = 0; i < Xp.rows(); i++) {
                    Xp.set(i,  j, (Xp.get(i, j) - mean));
                }
            }

            // Calculate the covariance matrix.
            DoubleMatrix2D C = new Algebra().mult(Xp.viewDice(), Xp);
            C.assign(Mult.mult(1.0 / bootstrapSize));

//            System.out.println("C " + C);

//            EigenvalueDecomposition eigen = new EigenvalueDecomposition(V);
//
//            DoubleMatrix2D _V = eigen.getV();
//            DoubleMatrix2D _D = eigen.getD();
//
//            for (int i = 0; i < dims; i++) {
//                _D.set(i, i, Math.pow(_D.get(i, i), 0.5));
//            }
//
//            DoubleMatrix2D C = new Algebra().mult(_V, _D);
//            C = new Algebra().mult(C, V.viewDice());

//            System.out.println("C = " + C);

            CholeskyDecomposition cholesky = new CholeskyDecomposition(C);
            DoubleMatrix2D L = cholesky.getL();

            // The estimated disturbance-stds are one over the abs of the diag of L
            DoubleMatrix1D diag = DoubleFactory2D.dense.diagonal(L);
            DoubleMatrix2D D = DoubleFactory2D.dense.diagonal(diag);
            DoubleMatrix1D newestDisturbancesstd = diag.copy();

//            for (int i = 0; i < dims; i++) {
//                newestDisturbancesstd.set(i, 1.0 / Math.abs(newestDisturbancesstd.get(i)));
//            }

//            System.out.println("L" + L);

            // Calculate corresponding B.
            DoubleMatrix2D identity = DoubleFactory2D.dense.identity(dims);
            DoubleMatrix2D DLInv = new Algebra().mult(D, new Algebra().inverse(L));
            DoubleMatrix2D Bnewest = identity.assign(DLInv, PlusMult.plusMult(-1));

//            System.out.println("Bnewest" + Bnewest);

            // Also calculate constants.
            DoubleMatrix1D cnewest = new Algebra().mult(L, Xpm);

            // Permute back to original order;
//            Bnewest = Bnewest.viewSelection(ik, ik);
//            newestDisturbancesstd = newestDisturbancesstd.viewSelection(ik);
//            cnewest = cnewest.viewSelection(ik);

            // Save results
            Bpieces[s] = Bnewest;
            diststdpieces[s] = newestDisturbancesstd;
            cpieces[s] = cnewest;
        }

        for (int i = 0; i < dims; i++) {
            for (int j = 0; j < dims; j++) {
                DoubleArrayList sampleData = new DoubleArrayList();

                for (int s = 0; s < numSamples; s++) {
                    sampleData.add(Bpieces[s].get(i, j));
                }

                double themean = Descriptive.mean(sampleData);
                double thestd = Descriptive.standardDeviation(Descriptive.variance(numSamples,
                        Descriptive.sum(sampleData), Descriptive.sumOfSquares(sampleData)));

                double pruneFactor = 10.0;

//                if (themean == 0) {
//                    continue;
//                }
//
//                if (Math.abs(themean) < pruneFactor * thestd) {
//                    BTilde.getSimulatedData().set(i, j, 0);
//                }
//                else {
//                    BTilde.getSimulatedData().set(i, j, themean);
//                }

                int xi = findColumn(i, swappedVariables, originalVariables);
                int xj = findColumn(j, swappedVariables, originalVariables);

                if (Math.abs(themean) < pruneFactor * thestd) {
                    //set it to zero
                    BTilde.getData().set(xi, xj, 0);
                }
//                else {
//                    BTilde.getSimulatedData().set(xi, xj, meanMat.get(i, j));
//                }


            }
        }

    }


    /**
     * Iterates through the columns of variables to find the one that matches
     * the originalVariables.get(index).
     */
    private int findColumn(int index, List<Node> variables, List<Node> originalVariables) {
        String originalName = originalVariables.get(index).getName();

        for (int i = 0; i < variables.size(); i++) {
            String newName = variables.get(i).getName();

            if (newName.equals(originalName)) {
                return i;
            }
        }

        throw new IllegalArgumentException("Column not found.");
    }

    /**
     * Returns a sample with replacement with the given sample size from the
     * given dataset.
     */
    private Data getBootstrapSample(Data dataSet, int sampleSize) {
        DoubleMatrix2D data = dataSet.getData();
        int actualSampleSize = dataSet.data.rows();

        int[] rows = new int[sampleSize];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = RandomUtil.getInstance().nextInt(actualSampleSize);
        }

        int[] cols = new int[dataSet.getData().columns()];
        for (int i = 0; i < cols.length; i++) cols[i] = i;

        DoubleMatrix2D newData = data.viewSelection(rows, cols).copy();
        return new Data(newData, dataSet.variables);
    }

    //==============================CLASSES===============================//

    /**
     * An individual indexed entry from a matrix.
     */
    private static class Entry implements Comparable<Entry> {
        private int row;
        private int column;
        private double value;

        public Entry(int row, int col, double val) {
            this.row = row;
            this.column = col;
            this.value = val;
        }

        /**
         * Used for sorting. An entry is smaller than another if its absolute
         * value is smaller.
         *
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Entry entry) {
            double thisVal = Math.abs(value);
            double entryVal = Math.abs(entry.value);
            return (new Double(thisVal).compareTo(entryVal));
        }

        public String toString() {
            return "[" + row + "," + column + "]:" + value + " ";
        }
    }

    /**
     * Just what it says--stores a matrix with a list of variables. The basic
     * thing that needs to be passed around in this algorithm. More efficient
     * than a COLT dataset.
     */
    private static class Data {
        private DoubleMatrix2D data;
        private List<Node> variables;

        public Data(DoubleMatrix2D array, List<Node> variables) {
            this.data = array;
            this.variables = variables;
        }

        public DoubleMatrix2D getData() {
            return data;
        }

        public List<Node> getVariables() {
            return variables;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();

            buf.append("\n").append(variables);
            buf.append("\n").append(data);

            return buf.toString();
        }
    }
}
