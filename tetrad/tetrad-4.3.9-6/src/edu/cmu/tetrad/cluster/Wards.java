package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.util.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements Ward's hierarchical clustering algorithm. The data is first
 * thrsholded. Following Goutte, On clustering fMRI time series, NeuroImage 9,
 * 298-319 (1999).
 *
 * @author Joseph Ramsey
 */
public class Wards implements ClusteringAlgorithm {

    /**
     * The data, columns as features, rows as cases.
     */
    private List<Point> points;

    /**
     * The cluster result, a tree node that can be decomposed.
     */
    private TreeNode resultTreeNode;

    /**'
     * The level in the tree at which clusters are reported.
     */
    private int depth = 3;

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
    private Wards() {
    }

    /**
     * Constructs a new NeuralGas algorithm, initializing the algorithm by
     * picking <code>numUnits</code> units from the data itself.
     *
     * @return The constructed algorithm.
     */
    public static Wards initialize() {
        return new Wards();
    }

    //===========================PUBLIC METHODS=======================//

    public void cluster(DoubleMatrix2D data) {
        points = new ArrayList<Point>();

        for (int i = 0; i < data.rows(); i++) {
            points.add(new Point(data.viewRow(i)));
        }

        if (isVerbose()) {
            System.out.println("# thresholded points = " +
                    points.size());
        }

        List<TreeNode> nodes = new LinkedList<TreeNode>();
        DoubleMatrix2D dissimilarities = new DenseDoubleMatrix2D(points.size(), points.size());
        TreeNode resultNode = null;

        // Initialize list of clusters to one cluster per vector.
        for (Point point : points) {
            TreeNode node = new LeafTreeNode(point);
            nodes.add(node);
        }

        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < i; j++) {
                LeafTreeNode a = (LeafTreeNode) nodes.get(i);
                LeafTreeNode b = (LeafTreeNode) nodes.get(j);

                DoubleMatrix1D vectorA = a.getPoints().get(0).getVector();
                DoubleMatrix1D vectorB = b.getPoints().get(0).getVector();

                double dissimilarity = metric.dissimilarity(vectorA, vectorB);

                dissimilarities.set(i, j, dissimilarity);
                dissimilarities.set(j, i, dissimilarity);
            }
        }

        if (isVerbose()) {
            System.out.println("Matrix constructed.");
        }

        while (true) {
            if (nodes.size() == 1) {
                break;
            }

            // Join the least dissimilar clusters.
            DissimilarityResult dissimilarityResult = findLeastDissimilarity(dissimilarities);

            int aIndex = dissimilarityResult.getAIndex();
            int bIndex = dissimilarityResult.getBIndex();

            if (aIndex == -1) {
                break;
            }

            TreeNode a = nodes.get(aIndex);
            TreeNode b = nodes.get(bIndex);

            if (isVerbose()) {
                System.out.println("leastDissimilarity = " + dissimilarityResult.getDissimilarity());
            }

//            System.out.println(a + ", " + b);

            // We're going to erase a and b's dissimilarities and put the join
            // of a and b where row b was. To do this, we need to keep a
            // temporary copy of a and b's rows in the dissimilarity matrix
            // in order to calculated dissimilarities for the joins.
            DoubleMatrix1D aRowCopy = copyRow(dissimilarities, aIndex);
            DoubleMatrix1D bRowCopy = copyRow(dissimilarities, bIndex);

            JoinTreeNode aUb = new JoinTreeNode(a, b);
            resultNode = aUb;
            nodes.set(aIndex, null);
            nodes.set(bIndex, null);
            nodes.set(bIndex, aUb);

            clearIndex(nodes, dissimilarities, aIndex);
            clearIndex(nodes, dissimilarities, bIndex);

            for (int cIndex = 0; cIndex < nodes.size(); cIndex++) {
                TreeNode c = nodes.get(cIndex);

                if (c == null) {
                    continue;
                }

                if (cIndex == aIndex || cIndex == bIndex) {
                    continue;
                }

                double wA = a.getWeight();
                double wB = b.getWeight();
                double wC = c.getWeight();

                double deltaAC = aRowCopy.get(cIndex);
                double deltaBC = bRowCopy.get(cIndex);
                double deltaAB = aRowCopy.get(bIndex);

                double dissimilarity = ((wA + wB) * deltaAC + (wB + wC) * deltaBC
                        + wC * deltaAB) / (wA + wB + wC);
                dissimilarities.set(cIndex, bIndex, dissimilarity);
                dissimilarities.set(bIndex, cIndex, dissimilarity);
            }
        }

        this.resultTreeNode = resultNode;
    }


    /**
     * Reports clusters for the result tree node at the set depth.
     */
    public List<List<Integer>> getClusters() {
        return getClustersAtDepth(resultTreeNode, depth);
    }

    /**
     * Starting with the given tree node, reports clusters at the given
     * depth.
     */
    public List<List<Integer>> getClustersAtDepth(TreeNode node, int depth) {
        List<TreeNode> nodesAtDepth = getNodesAtDepth(resultTreeNode, depth);
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();

        for (TreeNode aNodesAtDepth : nodesAtDepth) {
            ArrayList<Integer> cluster = new ArrayList<Integer>();
            List<Point> points = aNodesAtDepth.getPoints();

            for (Point p : points) {
                int index = this.points.indexOf(p);
                cluster.add(index);
            }

            clusters.add(cluster);
        }

        return clusters;
    }

    public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TreeNode clusterResult() {
        return this.resultTreeNode;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("Ward's method clustering.");

        for (int i = 0; i < 6; i++) {
            List<TreeNode> nodes = getNodesAtDepth(resultTreeNode, i);

            buf.append("\n At depth = ").append(i).append(" there are ")
                    .append(nodes.size()).append(" clusters with these sizes:");

            for (int j = 0; j < nodes.size(); j++) {
                TreeNode node = nodes.get(j);
                List<Point> points = node.getPoints();
                int size = points.size();
                buf.append("\n\t").append(j).append(". ").append(size);
            }
        }

        return buf.toString();
    }

    public List<TreeNode> getNodesAtDepth(TreeNode treeNode, int depth) {
        if (treeNode == null) {
            throw new IllegalArgumentException("The given tree node is null.");
        }

        List<TreeNode> nodes = Collections.singletonList(treeNode);

        for (int i = 0; i < depth; i++) {
            List<TreeNode> _nodes = new ArrayList<TreeNode>();

            for (TreeNode node : nodes) {
                if (node instanceof JoinTreeNode) {
                    JoinTreeNode _node = (JoinTreeNode) node;
                    _nodes.add(_node.getNode1());
                    _nodes.add(_node.getNode2());
                }
                else {
                    _nodes.add(node);
                }
            }

            nodes = _nodes;
        }

        return nodes;
    }

    private DoubleMatrix1D copyRow(DoubleMatrix2D dissimilarities, int aIndex) {
        return dissimilarities.viewRow(aIndex).copy();
    }

    private void clearIndex(List<TreeNode> nodes, DoubleMatrix2D dissimilarities, int index) {
        for (int i = 0; i < nodes.size(); i++) {
            dissimilarities.set(i, index, Double.NaN);
            dissimilarities.set(index, i, Double.NaN);
        }
    }

    private DissimilarityResult findLeastDissimilarity(DoubleMatrix2D dissimilarities) {
        double leastDissimilarity = Double.POSITIVE_INFINITY;
        int aIndex = -1;
        int bIndex = -1;

        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (dissimilarities.get(i, j) < leastDissimilarity) {
                    leastDissimilarity = dissimilarities.get(i, j);
                    aIndex = i;
                    bIndex = j;
                }
            }
        }

        return new DissimilarityResult(aIndex, bIndex, leastDissimilarity);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Represents a node in the tree of clusters produced by Ward's method.
     * @author Joseph Ramsey
     */
    public interface TreeNode {

        /**
         * Returns the weight of the cluster.
         */
        int getWeight();

        /**
         * Returns the points in the cluster.
         */
        List<Point> getPoints();

        /**
         * Prints out this node as a string. (Maybe be long.)
         */
        String toString();
    }

    /**
     * Represents a leaf node in the tree of clusters for Ward's method,
     * containing exactly one point.
     * @author Joseph Ramsey
     */
    public static class LeafTreeNode implements TreeNode {

        /**
         * The single point in this leaf cluster.
         */
        private Point point;

        /**
         * The weight of this cluster, which is 1.
         */
        private int weight = 1;

        /**
         * Constructs a leaf node with with given point.
         */
        public LeafTreeNode(Point point) {
            this.point = point;
        }

        /**
         * Returns the weight, which is 1.
         */
        public int getWeight() {
            return weight;
        }

        /**
         * Returns the (singleton) list of points.
         */
       public List<Point> getPoints() {
            List<Point> points = new LinkedList<Point>();
            points.add(point);
            return points;
        }

        /**
         * Prints out the point in this cluster.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();

            buf.append("Single Point: ");
            buf.append("\n 1. ").append(point);

            return point.toString();
        }
    }

    /**
     * Represents the join of two clusters for Ward's method.
     * @author Joseph Ramsey
     */
    public static class JoinTreeNode implements TreeNode {

        /**
         * The "left" node.
         */
        private TreeNode node1;

        /**
         * The "right" node.
         */
        private TreeNode node2;

        /**
         * The combined weight.
         */
        private int weight;

        /**
         * Constructs a new join tree node with the given two nodes and
         * calculates its weight as the sum of the component weights.
         */
        public JoinTreeNode(TreeNode node1, TreeNode node2) {
            if (node1 == null || node2 == null) {
                throw new IllegalArgumentException();
            }

            this.node1 = node1;
            this.node2 = node2;
            this.weight = node1.getWeight() + node2.getWeight();
        }

        /**
         * The "left" node.
         */
        public TreeNode getNode1() {
            return node1;
        }

        /**
         * The "right" node.
         */
        public TreeNode getNode2() {
            return node2;
        }

        /**
         * The combined weight of <code>node1</code> and <code>node2</code>
         */
        public int getWeight() {
            return weight;
        }

        /**
         * Returns the list of points in the clusters corresponding to this node,
         * which is the combined list of points from <code>node1</code> and
         * <code>node2</code>
         */
        public List<Point> getPoints() {
            List<Point> points = new ArrayList<Point>();
            points.addAll(node1.getPoints());
            points.addAll(node2.getPoints());
            return points;
        }

        /**
         * Prints the points out as a list.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            List<Point> points = getPoints();

            buf.append("Points: ");

            for (int i = 0; i < points.size(); i++) {
                buf.append("\n ").append(i + 1).append(". ").append(points.get(i));
            }

            return getPoints().toString();
        }
    }

    private static class DissimilarityResult {
        private int aIndex;
        private int bIndex;
        private double dissimilarity;

        public DissimilarityResult(int aIndex, int bIndex, double dissimilarity) {
            this.aIndex = aIndex;
            this.bIndex = bIndex;
            this.dissimilarity = dissimilarity;
        }

        public int getAIndex() {
            return aIndex;
        }

        public int getBIndex() {
            return bIndex;
        }

        public double getDissimilarity() {
            return dissimilarity;
        }

        public String toString() {
            return "aIndex = " + aIndex + " bIndex = " + bIndex + " dissimilarity = " + dissimilarity;
        }
    }
}
