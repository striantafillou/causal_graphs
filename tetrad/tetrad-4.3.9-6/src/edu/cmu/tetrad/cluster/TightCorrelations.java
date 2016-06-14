package edu.cmu.tetrad.cluster;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.stat.Descriptive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Implements the CBA algorithm, as defined in Heller et al. (2006),
 * "Cluster-based analysis of FMRI data," NeuroImage 33, 599-608.
 *
 * @author Joseph Ramsey
 */
public class TightCorrelations {
    private DoubleMatrix2D xyzCoords;
    private List<Tuple> tuples;
    private boolean verbose = true;

    public TightCorrelations(DoubleMatrix2D xyzCoords) {
        this.xyzCoords = xyzCoords;

    }


    public void findTightCorrelations(DoubleMatrix2D timeSeries) {
        DoubleArrayList cluster0 = new DoubleArrayList(xyzCoords.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(xyzCoords.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(xyzCoords.viewColumn(2).toArray());

        int minX = (int) Descriptive.min(cluster0);
        int minY = (int) Descriptive.min(cluster1);
        int minZ = (int) Descriptive.min(cluster2);

        int maxX = (int) Descriptive.max(cluster0);
        int maxY = (int) Descriptive.max(cluster1);
        int maxZ = (int) Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("X = " + minX + " to " + maxX + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Y = " + minY + " to " + maxY + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Z = " + minZ + " to " + maxZ + " mean = " + mean2 + " SD = " + sd2);

        int[][][] brainMap = new int[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    brainMap[x - minX][y - minY][z - minZ] = -1;
                }
            }
        }

        for (int i = 0; i < xyzCoords.rows(); i++) {
            int x = (int) xyzCoords.get(i, 0);
            int y = (int) xyzCoords.get(i, 1);
            int z = (int) xyzCoords.get(i, 2);
            brainMap[x - minX][y - minY][z - minZ] = i;
        }

        List<Set<Integer>> protoClusters = new ArrayList<Set<Integer>>();

        for (int i = 0; i < xyzCoords.rows(); i++) {
            protoClusters.add(new LinkedHashSet<Integer>());
        }

        double minAvg = Double.POSITIVE_INFINITY;
        double maxAvg = Double.NEGATIVE_INFINITY;

        List<Tuple> tuples = new ArrayList<Tuple>();

        for (int x = minX; x <= maxX; x++) {
            System.out.println("x = " + x);

            for (int y = minY; y <= maxY; y++) {

                for (int z = minZ; z <= maxZ; z++) {
                    int i1 = brainMap[x - minX][y - minY][z - minZ];
                    if (i1 == -1) continue;
//                    System.out.println("*** x = " + x + " y = " + y + " z = " + z);

                    int centerPoint = brainMap[x - minX][y - minY][z - minZ];
                    protoClusters.get(centerPoint).add(centerPoint);

                    double sumCorr = 0.0;
                    int numCellsTapped = 0;
                    int scope = 3;

                    for (int _x = x - scope; _x <= x + scope; _x++) {
                        for (int _y = y - scope; _y <= y + scope; _y++) {
                            for (int _z = z - scope; _z <= z + scope; _z++) {
                                if (_x < minX || _x > maxX) continue;
                                if (_y < minY || _y > maxY) continue;
                                if (_z < minZ || _z > maxZ) continue;

                                if (_x == x && _y == y && _z == z) {
                                    continue;
                                }

                                int i2 = brainMap[_x - minX][_y - minY][_z - minZ];
                                if (i2 == -1) continue;

                                double[] array1 = timeSeries.viewRow(i1).toArray();
                                double[] array2 = timeSeries.viewRow(i2).toArray();
//                                double v = StatUtils.rXY(array1, array2);

                                double v = correlation(array1, array2);

//                                double v = metric.dissimilarity(timeSeries.viewRow(i1),
//                                        timeSeries.viewRow(i2));
                                if (v > 0.95) {
                                    sumCorr += v;
                                    numCellsTapped++;
                                }

//                                if (numCellsTapped % 100 == 0) System.out.println(numCellsTapped);
                            }
                        }
                    }

                    double avg = sumCorr / numCellsTapped;

                    if (avg < minAvg) minAvg = avg;
                    if (avg > maxAvg) maxAvg = avg;

                    if (numCellsTapped > 150) {
                        System.out.print(".");
                        tuples.add(new Tuple(centerPoint, avg, numCellsTapped));
                    }

                }

//                System.out.println("Min avg corr = " + minAvg);
//                System.out.println("Max avg corr = " + maxAvg);
            }
        }


        this.tuples = tuples;

        try {
            PrintWriter out = new PrintWriter(new File("test_data/points7.txt"));

            Collections.sort(tuples);

            for (int i = 0; i < tuples.size(); i++) {
                int point = tuples.get(i).getPoint();
                int x = (int) xyzCoords.get(point, 0);
                int y = (int) xyzCoords.get(point, 1);
                int z = (int) xyzCoords.get(point, 2);

                double avgCorrelation = tuples.get(i).getAvgCorrelation();
                int numCellsTapped = tuples.get(i).getNumAveraged();

                System.out.println((i + 1) + ". <" + x + ", " + y + ", " + z + "> " +
                        avgCorrelation + " n = " + numCellsTapped);
//                out.println((70 - x) + " " + (90 - z) + " " + (60 - y));
                out.println((z) + " " + (x) + " " + (y));
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void findTightCorrelations2(DoubleMatrix2D timeSeries) {
        DoubleArrayList cluster0 = new DoubleArrayList(xyzCoords.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(xyzCoords.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(xyzCoords.viewColumn(2).toArray());

        int minX = (int) Descriptive.min(cluster0);
        int minY = (int) Descriptive.min(cluster1);
        int minZ = (int) Descriptive.min(cluster2);

        int maxX = (int) Descriptive.max(cluster0);
        int maxY = (int) Descriptive.max(cluster1);
        int maxZ = (int) Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("X = " + minX + " to " + maxX + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Y = " + minY + " to " + maxY + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Z = " + minZ + " to " + maxZ + " mean = " + mean2 + " SD = " + sd2);

        int[][][] brainMap = new int[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    brainMap[x - minX][y - minY][z - minZ] = -1;
                }
            }
        }

        for (int i = 0; i < xyzCoords.rows(); i++) {
            int x = (int) xyzCoords.get(i, 0);
            int y = (int) xyzCoords.get(i, 1);
            int z = (int) xyzCoords.get(i, 2);
            brainMap[x - minX][y - minY][z - minZ] = i;
        }

        List<Set<Integer>> protoClusters = new ArrayList<Set<Integer>>();

        for (int i = 0; i < xyzCoords.rows(); i++) {
            protoClusters.add(new LinkedHashSet<Integer>());
        }

        double minAvg = Double.POSITIVE_INFINITY;
        double maxAvg = Double.NEGATIVE_INFINITY;

        List<Tuple> tuples = new ArrayList<Tuple>();

        for (int x = minX; x <= maxX; x++) {
            System.out.println("x = " + x);

            for (int y = minY; y <= maxY; y++) {

                for (int z = minZ; z <= maxZ; z++) {
                    int i1 = brainMap[x - minX][y - minY][z - minZ];
                    if (i1 == -1) continue;
//                    System.out.println("*** x = " + x + " y = " + y + " z = " + z);

                    int centerPoint = brainMap[x - minX][y - minY][z - minZ];
                    protoClusters.get(centerPoint).add(centerPoint);

                    double sumCorr = 0.0;
                    int numCellsTapped = 0;
                    int scope = 3;

                    for (int _x = x - scope; _x <= x + scope; _x++) {
                        for (int _y = y - scope; _y <= y + scope; _y++) {
                            for (int _z = z - scope; _z <= z + scope; _z++) {
                                if (_x < minX || _x > maxX) continue;
                                if (_y < minY || _y > maxY) continue;
                                if (_z < minZ || _z > maxZ) continue;

                                if (_x == x && _y == y && _z == z) {
                                    continue;
                                }

                                int i2 = brainMap[_x - minX][_y - minY][_z - minZ];
                                if (i2 == -1) continue;

                                double[] array1 = timeSeries.viewRow(i1).toArray();
                                double[] array2 = timeSeries.viewRow(i2).toArray();
//                                double v = StatUtils.rXY(array1, array2);

                                int sectionLength = 50;

                                double v = maxSectionCorrelation(array1, array2, sectionLength);

//                                double v = metric.dissimilarity(timeSeries.viewRow(i1),
//                                        timeSeries.viewRow(i2));
                                if (v > 0.95) {
                                    sumCorr += v;
                                    numCellsTapped++;
                                }

//                                if (numCellsTapped % 100 == 0) System.out.println(numCellsTapped);
                            }
                        }
                    }

                    double avg = sumCorr / numCellsTapped;

                    if (avg < minAvg) minAvg = avg;
                    if (avg > maxAvg) maxAvg = avg;

                    if (numCellsTapped > 150) {
                        System.out.print(".");
                        tuples.add(new Tuple(centerPoint, avg, numCellsTapped));
                    }

                }

//                System.out.println("Min avg corr = " + minAvg);
//                System.out.println("Max avg corr = " + maxAvg);
            }
        }


        this.tuples = tuples;

        try {
            PrintWriter out = new PrintWriter(new File("test_data/points7.txt"));

            Collections.sort(tuples);

            for (int i = 0; i < tuples.size(); i++) {
                int point = tuples.get(i).getPoint();
                int x = (int) xyzCoords.get(point, 0);
                int y = (int) xyzCoords.get(point, 1);
                int z = (int) xyzCoords.get(point, 2);

                double avgCorrelation = tuples.get(i).getAvgCorrelation();
                int numCellsTapped = tuples.get(i).getNumAveraged();

                System.out.println((i + 1) + ". <" + x + ", " + y + ", " + z + "> " +
                        avgCorrelation + " n = " + numCellsTapped);
//                out.println((70 - x) + " " + (90 - z) + " " + (60 - y));
                out.println((z) + " " + (x) + " " + (y));
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assumes that the data is standardized.
     */
    private double maxSectionCorrelation(double[] array1, double[] array2, int sectionLength) {

        double maxCorr = -1;
        int n = array1.length;

        double[] section1 = new double[sectionLength];
        double[] section2 = new double[sectionLength];

        for (int s = 0; s < n - sectionLength; s++) {
            fill(section1, array1, 0);
            fill(section2, array2, 0);

            double correlation = correlation(section1, section2);

            if (correlation > maxCorr) {
                maxCorr = correlation;
            }
        }

        return maxCorr;
    }

    private void fill(double[] section, double[] array, int index) {
        for (int i = 0; i < section.length; i++) {
            section[i] = array[i + index];
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private static class Tuple implements Comparable {
        private int point;
        private double avgCorrelation;
        private int numAveraged;

        public Tuple(int point, double avgCorrelation, int numAveraged) {
            this.point = point;
            this.avgCorrelation = avgCorrelation;
            this.numAveraged = numAveraged;
        }

        public int compareTo(Object o) {
            TightCorrelations.Tuple p = (TightCorrelations.Tuple) o;
            return p.getAvgCorrelation() > getAvgCorrelation() ? -1 : (p.getAvgCorrelation() == getAvgCorrelation() ? 0 : 1);
        }

        public int getPoint() {
            return point;
        }

        public double getAvgCorrelation() {
            return avgCorrelation;
        }

        public int getNumAveraged() {
            return numAveraged;
        }
    }


    private double correlation(double[] array1, double[] array2) {
        DoubleArrayList list1 = new DoubleArrayList(array1);
        DoubleArrayList list2 = new DoubleArrayList(array2);

        double ss1 = Descriptive.sumOfSquares(list1);
        double ss2 = Descriptive.sumOfSquares(list2);

        double s1 = Descriptive.sum(list1);
        double s2 = Descriptive.sum(list2);

        double var1 = Descriptive.variance(list1.size(), s1, ss1);
        double var2 = Descriptive.variance(list2.size(), s2, ss2);

        double v = Descriptive.correlation(
                list1, Descriptive.standardDeviation(var1),
                list2, Descriptive.standardDeviation(var2)
        );
        return v;
    }


    public List<List<Integer>> getClusters() {
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        clusters.add(new ArrayList<Integer>());

        for (Tuple tuple : tuples) {
            clusters.get(0).add(tuple.getPoint());
        }

        return clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private double distance(int x, int y, int z, int x1, int y1, int z1) {
        int d1 = x - x1;
        int d2 = y - y1;
        int d3 = z - z1;

        return Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
    }


    private double standardDeviation(DoubleArrayList cluster) {
        double sumX = Descriptive.sum(cluster);
        double sumSqX = Descriptive.sumOfSquares(cluster);
        double variance = Descriptive.variance(cluster.size(), sumX, sumSqX);
        return Descriptive.standardDeviation(variance);
    }
}
