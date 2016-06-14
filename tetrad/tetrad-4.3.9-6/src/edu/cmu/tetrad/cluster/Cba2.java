package edu.cmu.tetrad.cluster;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.*;

/**
 * Implements the CBA algorithm, as defined in Heller et al. (2006),
 * "Cluster-based analysis of FMRI data," NeuroImage 33, 599-608.
 *
 * @author Joseph Ramsey
 */
public class Cba2 implements ClusteringAlgorithm {
    private DoubleMatrix2D xyzCoords;
    private boolean verbose = true;

    public Cba2(DoubleMatrix2D xyzCoords) {
        this.xyzCoords = xyzCoords;

    }


    public void cluster(DoubleMatrix2D timeSeries) {
        Dissimilarity metric = new SquaredErrorLoss();

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

        for (int x = minX; x <= maxX; x++) {
            System.out.println("x = " + x);

            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int i1 = brainMap[x - minX][y - minY][z - minZ];
                    if (i1 == -1) continue;
//                    System.out.println("*** x = " + x + " y = " + y + " z = " + z);

                    int centerPoint = brainMap[x - minX][y - minY][z - minZ];
                    protoClusters.get(centerPoint).add(centerPoint);

//                    double maxCorr = Double.NEGATIVE_INFINITY;
//                    int foundX = -1;
//                    int foundY = -1;
//                    int foundZ = -1;

                    for (int _x = x - 1; _x <= x + 1; _x++) {
                        for (int _y = y - 1; _y <= y + 1; _y++) {
                            for (int _z = z - 1; _z <= z + 1; _z++) {
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

//                                double v = correlation(array1, array2);

                                double v = metric.dissimilarity(timeSeries.viewRow(i1),
                                        timeSeries.viewRow(i2));
//                                System.out.println(v);

                                if (v < 0.001) {
                                    int otherPoint = brainMap[_x - minX][_y - minY][_z - minZ];
                                    protoClusters.get(centerPoint).add(otherPoint);
//                                    System.out.println(centerPoint + " --> " + otherPoint);
                                }

//                                if (v > maxCorr) {
//                                    maxCorr = v;
//                                    foundX = _x;
//                                    foundY = _y;
//                                    foundZ = _z;
//                                }
                            }
                        }
                    }

//                    if (foundX == -1 || foundY == -1 || foundZ == -1) {
//                        continue;
//                    }
                }
            }
        }

        for (int i = 0; i < protoClusters.size(); i++) {
            System.out.println("i = " + i);
            if (protoClusters.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < protoClusters.size(); j++) {
                if (protoClusters.get(j) == null) {
                    continue;
                }

                for (int k : protoClusters.get(j)) {
                    if (protoClusters.get(i).contains(k)) {
//                        System.out.println("Merging " + protoClusters.get(i) + " and " + protoClusters.get(j));
                        protoClusters.get(i).addAll(protoClusters.get(j));
                        protoClusters.set(j, null);
                        break;
                    }
                }
            }
        }

//        // Clump together slightly to form clusters.
//        for (int i = 0; i < protoClusters.size(); i++) {
//            List<Integer> cluster = protoClusters.get(i);
//
//            if (cluster.size() == 0) {
//                continue;
//            }
//
//            if (cluster.size() == 1) {
//                continue;
//            }
//
//            Integer reference = cluster.get(1);
//            List<Integer> _cluster = protoClusters.get(reference);
//
//            while (_cluster.size() == 1) {
//                reference = _cluster.get(0);
//                _cluster = protoClusters.get(reference);
//            }
//
//            Set<Integer> set = new LinkedHashSet<Integer>(_cluster);
//            set.addAll(cluster);
//            List<Integer> newCluster = new LinkedList<Integer>(set);
//
//            protoClusters.set(reference, newCluster);
//
//            cluster.clear();
//            cluster.add(reference);
//        }

        // Pull the actual clusters out of the list.
        List<Set<Integer>> clusters = new ArrayList<Set<Integer>>();

        int minSize = Integer.MAX_VALUE;
        int maxSize = Integer.MIN_VALUE;

        for (int i = 0; i < protoClusters.size(); i++) {
            Set<Integer> cluster = protoClusters.get(i);

            if (cluster == null) {
                continue;
            }

            int size = cluster.size();

            clusters.add(protoClusters.get(i));

            if (size < minSize) minSize = size;
            if (size > maxSize) maxSize = size;
        }

        System.out.println(clusters.size() + " clusters");
        System.out.println("Min size = " + minSize);
        System.out.println("Max size = " + maxSize);
    }

    public void findNeighbors(DoubleMatrix2D timeSeries) {
        Dissimilarity metric = new SquaredErrorLoss();

        int row = RandomUtil.getInstance().nextInt(xyzCoords.rows());

        int x1 = (int) xyzCoords.get(row, 0);
        int y1 = (int) xyzCoords.get(row, 1);
        int z1 = (int) xyzCoords.get(row, 2);

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

        int i1 = brainMap[x1 - minX][y1 - minY][z1 - minZ];

        if (i1 == -1) {
            throw new IllegalArgumentException();
        }

        System.out.println("*** x = " + x1 + " y = " + y1 + " z = " + z1);

        int centerPoint = brainMap[x1 - minX][y1 - minY][z1 - minZ];
        protoClusters.get(centerPoint).add(centerPoint);

        int scope = 5;

        List<Tuple> pairs = new ArrayList<Tuple>();

        for (int _x = x1 - scope; _x <= x1 + scope; _x++) {
            for (int _y = y1 - scope; _y <= y1 + scope; _y++) {
                for (int _z = z1 - scope; _z <= z1 + scope; _z++) {
                    if (_x < minX || _x > maxX) continue;
                    if (_y < minY || _y > maxY) continue;
                    if (_z < minZ || _z > maxZ) continue;

                    if (_x == x1 && _y == y1 && _z == z1) {
                        continue;
                    }

                    int i2 = brainMap[_x - minX][_y - minY][_z - minZ];
                    if (i2 == -1) continue;

                    double[] array1 = timeSeries.viewRow(i1).toArray();
                    double[] array2 = timeSeries.viewRow(i2).toArray();
//                    double v = StatUtils.rXY(array1, array2);

                    double correlation = correlation(array1, array2);

//                    double v = metric.dissimilarity(timeSeries.viewRow(i1),
//                            timeSeries.viewRow(i2));
//                                System.out.println(v);

//                    if (v > 0.99) {
                    int otherPoint = brainMap[_x - minX][_y - minY][_z - minZ];
                    protoClusters.get(centerPoint).add(otherPoint);
                    double distance = distance(x1, y1, z1, _x, _y, _z);

                    pairs.add(new Tuple(distance, correlation, otherPoint));

//                    System.out.println(distance + " corr = " + correlation);
//                    System.out.println(" " + centerPoint + " --> " + otherPoint);
//                    }
                }
            }
        }

        Collections.sort(pairs);

        for (Tuple pair : pairs) {
            double distance = pair.distance;
            double correlation = pair.correlation;

            int x2 = (int) xyzCoords.get(pair.point, 0);
            int y2 = (int) xyzCoords.get(pair.point, 1);
            int z2 = (int) xyzCoords.get(pair.point, 2);

            System.out.println(distance + " " + correlation
                + " <" + x2 + ", " + y2 + ", " + z2 + ">");
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private static class Tuple implements Comparable {
        public double distance;
        public double correlation;
        public int point;

        public Tuple(double distance, double correlation, int point) {
            this.distance = distance;
            this.correlation = correlation;
            this.point = point;
        }

        public int compareTo(Object o) {
            Tuple p = (Tuple) o;
            return p.distance > distance ? -1 : (p.distance == distance ? 0 : 1);
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
        return null;
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
