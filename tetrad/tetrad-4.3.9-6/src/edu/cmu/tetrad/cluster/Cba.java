package edu.cmu.tetrad.cluster;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.util.StatUtils;

import java.util.*;

/**
 * Implements the CBA algorithm, as defined in Heller et al. (2006),
 * "Cluster-based analysis of FMRI data," NeuroImage 33, 599-608.
 *
 * @author Joseph Ramsey
 */
public class Cba implements ClusteringAlgorithm {
    private DoubleMatrix2D xyzCoords;
    private boolean verbose = true;
    private List<List<Integer>> clusters;

    public Cba(DoubleMatrix2D xyzCoords) {
        this.xyzCoords = xyzCoords;

    }


    public void cluster(DoubleMatrix2D timeSeries) {
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

            if (!(brainMap[x - minX][y - minY][z - minZ] == -1)) {
                System.out.println("Duplicate coordinates: <" + x + ", " + y + ", " + z + ">");
            }

            brainMap[x - minX][y - minY][z - minZ] = i;
        }

        List<List<Integer>> protoClusters = new ArrayList<List<Integer>>();

        for (int i = 0; i < xyzCoords.rows(); i++) {
            protoClusters.add(null);
        }

        for (int x = minX; x <= maxX; x++) {
            System.out.println("x = " + x);

            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int i1 = brainMap[x - minX][y - minY][z - minZ];
                    if (i1 == -1) continue;

                    double maxCorr = Double.NEGATIVE_INFINITY;
                    int foundX = -1;
                    int foundY = -1;
                    int foundZ = -1;

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

                                double v = StatUtils.correlation(
                                        timeSeries.viewRow(i1).toArray(),
                                        timeSeries.viewRow(i2).toArray());

                                if (v > maxCorr) {
                                    maxCorr = v;
                                    foundX = _x;
                                    foundY = _y;
                                    foundZ = _z;
                                }
                            }
                        }
                    }

                    // Completely isolated point
                    if (foundX == -1 || foundY == -1 || foundZ == -1) {
                        continue;
                    }

                    int centerPoint = brainMap[x - minX][y - minY][z - minZ];
                    int otherPoint = brainMap[foundX - minX][foundY - minY][foundZ - minZ];

                    List<Integer> cluster = new LinkedList<Integer>();
                    cluster.add(centerPoint);
                    cluster.add(otherPoint);

                    protoClusters.set(centerPoint, cluster);
                }
            }
        }

        // Clump together slightly to form clusters.
        for (int i = 0; i < protoClusters.size(); i++) {
            List<Integer> cluster = protoClusters.get(i);

            // Skip isolated points.
            if (cluster == null) {
                continue;
            }

            // Skip guys that just refer.
            if (cluster.size() == 1) {
                continue;
            }

            Integer reference = cluster.get(1);
            List<Integer> _cluster = protoClusters.get(reference);

            while (_cluster.size() == 1) {
                reference = _cluster.get(0);
                _cluster = protoClusters.get(reference);
            }

            Set<Integer> set = new LinkedHashSet<Integer>();
            set.addAll(_cluster);
            set.addAll(cluster);
            List<Integer> newCluster = new LinkedList<Integer>(set);

            protoClusters.set(reference, newCluster);

            // If a cluster contains just one guy, it's just a reference to
            // some other cluster.
            cluster.clear();
            cluster.add(reference);
        }

        // Pull the actual clusters out of the list.
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();

        int minSize = Integer.MAX_VALUE;
        int maxSize = Integer.MIN_VALUE;

        for (List<Integer> cluster : protoClusters) {
            if (cluster == null) {
                continue;
            }

            if (cluster.size() >= 2) {
                clusters.add(cluster);

                if (cluster.size() < minSize) minSize = cluster.size();
                if (cluster.size() > maxSize) maxSize = cluster.size();
            }
        }

        this.clusters = clusters;
    }

    public List<List<Integer>> getClusters() {
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

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
