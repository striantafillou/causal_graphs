package edu.cmu.tetrad.cluster;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.RegexTokenizer;
import edu.cmu.tetrad.util.RandomUtil;

import java.io.*;
import java.util.*;

/**
 * Some general utilities for dealing with clustering input and output.
 *
 * @author Joseph Ramsey
 */
public class ClusterUtils {
    public static DoubleMatrix2D restrictToRows(DoubleMatrix2D data, List<Integer> rows) {
        int[] _rows = asArray(rows);
        int[] _cols = new int[data.columns()];
        for (int j = 0; j < data.columns(); j++) _cols[j] = j;
        return data.viewSelection(_rows, _cols);
    }
                                   
    private static int[] asArray(List<Integer> indices) {
        int[] _indices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) _indices[i] = indices.get(i);
        return _indices;
    }

    public static List<List<Integer>> convertClusterIndicesToLists(List<Integer> clusterIndices) {
        int max = 0;

        for (int i = 0; i < clusterIndices.size(); i++) {
            if (clusterIndices.get(i) > max) max = clusterIndices.get(i);
        }

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();

        for (int i = 0; i <= max; i++) {
            clusters.add(new LinkedList<Integer>());
        }

        for (int i = 0; i < clusterIndices.size(); i++) {
            Integer index = clusterIndices.get(i);

            if (index == -1) continue;

            clusters.get(index).add(i);
        }

        return clusters;
    }

    public static void printPcaTranslations(DoubleMatrix2D selection, int k) {
        System.out.println("\nPCA:");

        DoubleMatrix2D covariance = Statistic.covariance(selection);
        EigenvalueDecomposition decomposition = null;
        try {
            decomposition = new EigenvalueDecomposition(covariance);
        } catch (Exception e) {
            return;
        }
        DoubleMatrix1D eigenvalues = decomposition.getRealEigenvalues();

        System.out.println("Eigenvalues: " + eigenvalues);

        DoubleMatrix2D featureVector = decomposition.getV();
        selection = new Algebra().mult(featureVector.viewDice(), selection.viewDice().copy()).viewDice();

        DoubleArrayList cluster0 = new DoubleArrayList(selection.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(selection.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(selection.viewColumn(2).toArray());

        double min0 = Descriptive.min(cluster0);
        double min1 = Descriptive.min(cluster1);
        double min2 = Descriptive.min(cluster2);

        double max0 = Descriptive.max(cluster0);
        double max1 = Descriptive.max(cluster1);
        double max2 = Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("Cluster " + k + ":");
        System.out.println("Dimension 0 = " + min0 + " to " + max0 + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Dimension 1 = " + min1 + " to " + max1 + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Dimension 2 = " + min2 + " to " + max2 + " mean = " + mean2 + " SD = " + sd2);
    }

    public static PrintWriter writeOutPrototypesVertically(DoubleMatrix2D prototypes,
                                                          String path
                                                          ) throws FileNotFoundException {
        System.out.println("Writing prototypes to file " + path);
        File file = new File(path);
        new File(file.getParent()).mkdirs();
        PrintWriter out = new PrintWriter(file);

        prototypes = prototypes.viewDice();

        for (int i = 0; i < prototypes.rows(); i++) {
            for (int j = 0; j < prototypes.columns(); j++) {
                out.print(prototypes.get(i, j));

                if (j < prototypes.columns() - 1) {
                    out.print("\t");
                }
            }

            out.println();
        }

//        out.println(prototypes.viewDice());
        out.close();
        return out;
    }

    static double standardDeviation(DoubleArrayList array) {
        double sumX = Descriptive.sum(array);
        double sumSqX = Descriptive.sumOfSquares(array);
        double variance = Descriptive.variance(array.size(), sumX, sumSqX);
        return Descriptive.standardDeviation(variance);
    }

    /**
     * Converting the data to standardized form and clustering using
     * squared error loss is equivalent to clustering in correlation space.
     * Note that the standardization is ROWWISE.
     */
    public static DoubleMatrix2D convertToStandardized(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int i = 0; i < data.rows(); i++) {
            double sum = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.columns();

            double norm = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                double v = data.get(i, j) - mean;
                norm += v * v;
            }

            norm = Math.sqrt(norm);

            for (int j = 0; j < data.columns(); j++) {
                data2.set(i, j, (data.get(i, j) - mean) / norm);
            }
        }

        return data2;
    }

    public static DoubleMatrix2D convertToSeriesZScores(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int i = 0; i < data.rows(); i++) {
            DoubleMatrix1D row = data.viewRow(i);
            DoubleArrayList _row = new DoubleArrayList(row.toArray());

            double mean = Descriptive.mean(_row);
            double sd = ClusterUtils.standardDeviation(_row);

            for (int j = 0; j < data.columns(); j++) {
                data2.set(i, j, (data.get(i, j) - mean) / sd);
            }
        }

        return data2;
    }

    public static void initRandomly(DoubleMatrix2D x) {
        for (int i = 0; i < x.rows(); i++) {
            for (int j = 0; j < x.columns(); j++) {
                x.set(i, j, RandomUtil.getInstance().nextDouble());
            }
        }
    }

    public static List<Integer> getTopFractionScoreRows(DoubleMatrix1D scores, 
                                                 double topFraction,
                                                 DoubleMatrix2D timeSeries) {
        List<Integer> _points = new ArrayList<Integer>();
        final Map<Integer, Double> _values = new HashMap<Integer, Double>();

        for (int i = 0; i < timeSeries.rows(); i++) {
            _points.add(i);
            _values.put(i, scores.get(i));
        }

        Collections.sort(_points, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                double v1 = _values.get(o1);
                double v2 = _values.get(o2);
                return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
            }
        });

        List<Integer> points = new ArrayList<Integer>();

        for (int i = (int) ((1.0 - topFraction) * _points.size()); i < _points.size();
             i++) {
            points.add(_points.get(i));
        }
        return points;
    }

    public static List<Integer> getAboveThresholdRows(DoubleMatrix1D scores,
                                                      double cutoff,
                                                      DoubleMatrix2D timeSeries) {
        List<Integer> includedRows = new ArrayList<Integer>();

        for (int i = 0; i < timeSeries.rows(); i++) {
            double score = scores.get(i);

            if (score > cutoff) {
                includedRows.add(i);
            }
        }

        return includedRows;
    }

    public static List<Integer> getSignificantlyChangingRows(DoubleMatrix2D data,
                                                             int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        List<Integer> includedRows = new ArrayList<Integer>();

        for (int i = 0; i < data.rows(); i++) {
            if (Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold) {
                includedRows.add(i);
            }
        }

        return includedRows;
    }

    public static boolean isSignificantlyChangingUp(DoubleMatrix2D data, int i,
                                                 int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        return Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold
                && data.get(i, tIndex) > data.get(i, tIndex - 1);
    }

    public static boolean isSignificantlyChangingDown(DoubleMatrix2D data, int i,
                                                   int tIndex, double threshold) {
        if (!(tIndex >= 1 && tIndex < data.columns())) {
            throw new IllegalArgumentException("tIndex must be in range [1, " + data.columns() + "]");
        }

        return Math.abs(data.get(i, tIndex - 1) - data.get(i, tIndex)) > threshold
                && data.get(i, tIndex) < data.get(i, tIndex - 1);
    }

    /**
     * Returns the top fraction threshold for the entire data set--that is,
     * if all of the values in the dataset were sorted bottom to top, the
     * value the tresholds the top fraction is given.
     * @param data A 2D real data set.
     * @param fraction A number between 0 and 1, inclusive.
     * @return The top frction threshold.
     */
    public static double getTopFactionThresholdOverall(DoubleMatrix2D data,
                                                              double fraction) {
        int numEntries = data.rows() * data.columns();
        int numTopFraction = (int) (numEntries * fraction);
        TreeSet<Double> set = new TreeSet<Double>();

        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                double datum = data.get(i, j);

                if (set.size() < numTopFraction) {
                    set.add(datum);
                }
                else {
                    if (datum > set.first()) {
                        set.remove(set.first());
                        set.add(datum);
                    }
                }
            }
        }

        return set.first();
    }

    /**
     * Returns a list of view of the data corresponding to the given clusters.
     */
    public static List<DoubleMatrix2D> getClusterViews(DoubleMatrix2D xyzData,
                                                       List<List<Integer>> clusters) {
        List<DoubleMatrix2D> views = new ArrayList<DoubleMatrix2D>();

        int[] cols = new int[xyzData.columns()];
        for (int j = 0; j < xyzData.columns(); j++) cols[j] = j;

        for (int k = 0; k < clusters.size(); k++) {
            int clusterSize = clusters.get(k).size();
            int[] rows = new int[clusterSize];

            for (int i = 0; i < clusterSize; i++) {
                rows[i] = clusters.get(k).get(i);
            }

            DoubleMatrix2D clusterView = xyzData.viewSelection(rows, cols);
            views.add(clusterView);
        }

        return views;
    }


    public static DoubleMatrix2D restrictToTimeSeries(DoubleMatrix2D data) {
        return data.viewPart(0, 3, data.rows(), data.columns() - 3);
    }

    public static DoubleMatrix2D restrictToXyz(DoubleMatrix2D data) {
        return data.viewPart(0, 0, data.rows(), 3);
    }


    /**
     * Prints the XYZ extents of the given XYZ dataset--that is, the minimum and
     * maximum of each dimension. It is assumed that the dataset has three
     * columns.
     */
    public static void printXyzExtents(DoubleMatrix2D xyzData) {
        if (!(xyzData.columns() == 3)) {
            throw new IllegalArgumentException();
        }

        if (xyzData.rows() == 0) {
            return;
        }

        DoubleArrayList cluster0 = new DoubleArrayList(xyzData.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(xyzData.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(xyzData.viewColumn(2).toArray());

        double min0 = Descriptive.min(cluster0);
        double min1 = Descriptive.min(cluster1);
        double min2 = Descriptive.min(cluster2);

        double max0 = Descriptive.max(cluster0);
        double max1 = Descriptive.max(cluster1);
        double max2 = Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("X = " + min0 + " to " + max0 + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Y = " + min1 + " to " + max1 + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Z = " + min2 + " to " + max2 + " mean = " + mean2 + " SD = " + sd2);
    }

    /**
     * Prints XYZ extents for each of the given clusters, where the clusters
     * point to rows in the given data set <code>xyzData</code>
     */
    public static void printXyzExtents(DoubleMatrix2D xyzData,
                                       List<List<Integer>> clusters) {
        List<DoubleMatrix2D> views = getClusterViews(xyzData, clusters);

        for (int i = 0; i < views.size(); i++) {
            System.out.println("Cluster " + i);
            printXyzExtents(views.get(i));
        }
    }

    public static DoubleMatrix2D loadMatrix(String path, int n, int m,
                                            boolean ignoreFirstRow,
                                            boolean ignoreFirstCol) throws IOException {
        System.out.println("Loading data from " + path);

        File file = new File(path);
        BufferedReader in = new BufferedReader(new FileReader(file));

        // Skip first line.
        if (ignoreFirstRow) {
            in.readLine();
        }

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, m);

        for (int i = 0; i < n; i++) {
            if ((i + 1) % 1000 == 0) System.out.println("Loading " + (i + 1));

            String line = in.readLine();
            RegexTokenizer tokenizer = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

            if (ignoreFirstCol) {
                tokenizer.nextToken();
            }

            for (int j = 0; j < m; j++) {
                double datum = Double.parseDouble(tokenizer.nextToken());
                data.set(i, j, datum);
            }
        }
        return data;
    }

    public static void writerClustersToGnuPlotFile(DoubleMatrix2D xyzData,
                                                   List<List<Integer>> clusters,
                                                   String path) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(path));

        for (int j = 0; j < clusters.size(); j++) {
            List<Integer> cluster = clusters.get(j);

            if (cluster.isEmpty()) {
                continue;
            }

            for (int i : cluster) {
                double x = xyzData.get(i, 0);
                double y = xyzData.get(i, 1);
                double z = xyzData.get(i, 2);
                out.println((z) + " " + (x) + " " + (y));
            }

            out.println();
            out.println();
        }

        out.close();
    }

    public static void writerClustersToGnuPlotFile(DoubleMatrix2D xyzData,
                                                   List<List<Integer>> clusters,
                                                   List<List<Integer>> colors,
                                                   String path
    ) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(path));

        for (int j = 0; j < clusters.size(); j++) {
            List<Integer> cluster = clusters.get(j);
            List<Integer> clusterColors = colors.get(j);

            if (cluster.isEmpty()) {
                continue;
            }

            for (int _i = 0; _i < cluster.size(); _i++) {
                int i = cluster.get(_i);

                double x = xyzData.get(i, 0);
                double y = xyzData.get(i, 1);
                double z = xyzData.get(i, 2);

                int color = clusterColors.get(_i);

                out.println(z + " " + x + " " + y + " " + color);
            }

            out.println();
            out.println();
        }

        out.close();
    }

    public static DoubleMatrix2D convertToMeanCentered(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = new DenseDoubleMatrix2D(data.rows(), data.columns());

        for (int i = 0; i < data.rows(); i++) {
            double sum = 0.0;

            for (int j = 0; j < data.columns(); j++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.columns();

            for (int j = 0; j < data.columns(); j++) {
                double v = data.get(i, j) - mean;
                data2.set(i, j, v);
            }
        }

        return data2;
    }
}
