package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.*;

/**
 * Implements the "Growing Neural Gas" algorithm. Following Bernd Fritzke,
 * "A growing neural gas network learns topologies," Advances in Neural
 * Information Processing Systems 7, MIT Press, Cambridge MA, 1995.
 *
 * @author Joseph Ramsey
 */
public class Gng implements ClusteringAlgorithm {

    /**
     * The data, columns as features, rows as cases.
     */
    private DoubleMatrix2D data;

    /**
     * The reference vectors.
     */
    private List<Unit> units;

    /**
     * A graph over the units.
     */
    private EdgeListGraph graph;

    /**
     * The maximum age an edge can achieve before it is removed from the
     * graph.
     */
    private int ageMax = 80;

    /**
     * New units are added every <code>lambda</code> times a new signal is
     * generated.
     */
    private int lambda = 2000;

    /**
     * Rate at which errors for q and f are decreased.
     */
    private double alpha = 0.5;

    /**
     * Rate at which all errors are decreased.
     */
    private double beta = 5.0e-4 ;

    /**
     *
     */
    private double epsilonB = 0.1;

    /**
     *
     */
    private double epsilonN = 0.01;

    /**
     * The maximum number of units.
     */
    private int maxUnits = 50;

    /**
     * True iff verbose output should be printed.
     */
    private boolean verbose = true;

    /**
     * A reusable vector for representing deltas--that is changes for vectors,
     * moving them closer to a signal.
     */
    private DenseDoubleMatrix1D delta;

    /**
     * The index of the most recent unit created. Used for naming nodes in the
     * graph.
     */
    int unitIndex = 0;

    /**
     * The number of signals generated. Used to determine when new units should
     * be added.
     */
    private long numSignalsGenerated = 0;

    /**
     * Maps units to nodes.
     */
    private HashMap<Unit, Node> unitsToNodes;

    /**
     * Maps units to accumulated errors.
     */
    private HashMap<Unit, Double> unitsToErrors;

    /**
     * Maps nodes to units.
     */
    private HashMap<Node, Unit> nodesToUnits;

    /**
     * Maps edges to their ages. Ages are initialized to zero, reset to zero
     * for s1---s2, and incremented for each edge s1---d for d adjacent to s1.
     * Edges above a certain age <code>ageMax</code> are removed.
     */
    private HashMap<Edge, Integer> edgesToAges;

    /**
     * The dissimilarity metric being used. May be set from outside.
     */
    private Dissimilarity metric = new SquaredErrorLoss();

    private int numUnits = 0;
    private List<List<Integer>> clusters;

    //============================CONSTRUCTOR==========================//

    /**
     * Private constructor. (Please keep it that way.)
     */
    private Gng() {
    }

    /**
     * Constructs a new NeuralGas algorithm, initializing the algorithm by
     * picking <code>numUnits</code> units from the data itself.
     *
     * @return The constructed algorithm.
     */
    public static Gng init() {
        return new Gng();
    }

    //===========================PUBLIC METHODS=======================//

    public void cluster(DoubleMatrix2D data) {
        this.data = data;
        delta = new DenseDoubleMatrix1D(data.columns());

        units = new ArrayList<Unit>();
        graph = new EdgeListGraph();
        unitsToNodes = new HashMap<Unit, Node>();
        nodesToUnits = new HashMap<Node, Unit>();
        unitsToNodes = new HashMap<Unit, Node>();
        edgesToAges = new HashMap<Edge, Integer>();
        unitsToErrors = new HashMap<Unit, Double>();

        // Step 0
        Node node1 = addUnit(nextSignal());
        Node node2 = addUnit(nextSignal());
        Edge initialEdge = Edges.undirectedEdge(node1, node2);
        graph.addEdge(initialEdge);
        edgesToAges.put(initialEdge, 0);

        print("Adding initial edge: " + initialEdge + " age " + edgesToAges.get(initialEdge));


        while (!shouldStop()) {

//            print("Ages: " + edgesToAges);

            // Step 1.Generate an input signal s.
            DoubleMatrix1D signal = nextSignal();
//            print("" + signal);
            calculateDistancesToSignal(signal);

            // Step 2. Find the nearest unit s1 and next nearest unit s1 to s.
            Collections.sort(units);
            Unit s1 = units.get(0);
            Unit s2 = units.get(1);

            Node s1Node = unitsToNodes.get(s1);
            Node s2Node = unitsToNodes.get(s2);

            // Step 3 Increment the ages of all edges emanating from s1.
            List<Edge> s1Edges = graph.getEdges(s1Node);

            for (Edge edge : s1Edges) {
                int age = edgesToAges.get(edge);
                edgesToAges.put(edge, age + 1);
            }

            // Step 4 Add the squared distance between the input signal and
            // s1 to a local counter variable.
            incrementError(s1, signal);

            // Step 5 Move s1 and its direct topological neighbors toward s by
            // fractions eb and en, respectively, of the total distance.
            moveUnit(s1, signal, getEpsilonB());

            for (Node node : neighbors(s1)) {
                Unit unit = nodesToUnits.get(node);
                moveUnit(unit, signal, getEpsilonN());
            }

            // Step 6
            Edge s1S2Edge = graph.getEdge(s1Node, s2Node);

            if (s1S2Edge == null) {
                s1S2Edge = Edges.undirectedEdge(s1Node, s2Node);
                graph.addEdge(s1S2Edge);
                print("Adding edge between s1 and s2: " + s1S2Edge  + " age " + edgesToAges.get(s1S2Edge));
            }

            edgesToAges.put(s1S2Edge, 0);

            // Step 7
            List<Edge> edgesToRemove = new LinkedList<Edge>();

            for (Edge edge : edgesToAges.keySet()) {
                if (edgesToAges.get(edge) > getAgeMax()) {
                    edgesToRemove.add(edge);
                }
            }

            for (Edge edge : edgesToRemove) {
                print("Removing old edge: " + edge +
                        " num nodes = " + graph.getNumNodes() +
                        " num edges = " + graph.getNumEdges() +
                        " age " + edgesToAges.get(edge));
                removeEdge(edge);
            }

            for (Node node: graph.getNodes()) {
                if (graph.getEdges(node).isEmpty()) {
                    removeUnit(nodesToUnits.get(node));
                }
            }

            // Step 8
            if (numSignalsGenerated % getLambda() == 0) {

                // Determine the unit q with maximum error.
                Unit q = unitWithMaximumError();

                // Determing the neighbor f of q with maximum error.
                Unit f = neighborWithMaximumError(q);

                // Create a new unit r between f and q.
                DoubleMatrix1D rVector = new DenseDoubleMatrix1D(q.getVector().size());

                for (int j = 0; j < rVector.size(); j++) {
                    rVector.set(j, (q.getVector().get(j) + f.getVector().get(j)) / 2);
                }

                // Add edges r--q, r--f, remove edge q--f.
                Node rNode = addUnit(rVector);
                Node qNode = unitsToNodes.get(q);

                Edge qrEdge = Edges.undirectedEdge(qNode, rNode);
                graph.addEdge(qrEdge);
                edgesToAges.put(qrEdge, 0);

//                print("Adding qr edge: " + qrEdge + " age " + edgesToAges.get(qrEdge));

                Node fNode = unitsToNodes.get(f);
                Edge frEdge = Edges.undirectedEdge(fNode, rNode);
                graph.addEdge(frEdge);
                edgesToAges.put(frEdge, 0);

//                print("Adding rf edge: " + frEdge + " age " + edgesToAges.get(frEdge));

                Edge qfEdge = graph.getEdge(qNode, fNode);

//                print("Removing qf edge: " + qfEdge  + " age " + edgesToAges.get(qfEdge));
                removeEdge(qfEdge);

                print("Adding new node, " + qNode + "---" + rNode + "---" + fNode);

                // Decrease all error variables by multiplying them with a
                // constant alpha.
                for (Unit unit : unitsToErrors.keySet()) {
                    double error = unitsToErrors.get(unit);
                    error -= getAlpha() * error;
                    unitsToErrors.put(unit, error);
                }

                // Initialize the error variable of r with the new value of
                // the error variable of q.
                Unit r = nodesToUnits.get(rNode);
                unitsToErrors.put(r, unitsToErrors.get(q));
            }

            // Step 9 Decrease all error variables by multiplying them with a
            // constant d.
            for (Unit unit : unitsToErrors.keySet()) {
                double error = unitsToErrors.get(unit);
                error -= getBeta() * error;
                unitsToErrors.put(unit, error);
            }

            // Step 10--loop as indicated.
        }


        List<List<Node>> components = GraphUtils.connectedComponents(graph);
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();

        for (int i = 0; i < components.size(); i++) {
            clusters.add(new ArrayList<Integer>(components.size()));
        }

        List<Node> nodes = graph.getNodes();

        for (int i = 0; i < data.rows(); i++) {
            double min = Double.POSITIVE_INFINITY;
            Node _node = null;

            for (int j = 0; j < nodes.size(); j++) {
                Node node = nodes.get(j);
                Unit unit = nodesToUnits.get(node);

                double d = metric.dissimilarity(unit.getVector(),
                        data.viewRow(i));
                if (d < min) {
                    min = d;
                    _node = node;
                }
            }

            System.out.println("Node for " + i + " is " + _node);

            for (int j = 0; j < components.size(); j++) {
                if (components.get(j).contains(_node)) {
                    clusters.get(j).add(i);
                    System.out.println("Component " + j + " contains that node: " + components.get(j));
                    break;
                }
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

    private void print(String s) {
        if (isVerbose()) System.out.println(s);
    }

    private void removeUnit(Unit unit) {
        Node node = unitsToNodes.get(unit);

        if (!graph.getAdjacentNodes(node).isEmpty()) {
            throw new IllegalArgumentException();
        }

        graph.removeNode(node);
        units.remove(unit);
        unitsToNodes.remove(unit);
        nodesToUnits.remove(node);
    }

    private void removeEdge(Edge edge) {
        graph.removeEdge(edge);
        edgesToAges.remove(edge);
    }

    private Unit neighborWithMaximumError(Unit q) {
        Unit f = null;
        double max = Double.NEGATIVE_INFINITY;

        for (Node node : neighbors(q)) {
            Unit _f = nodesToUnits.get(node);

            if (unitsToErrors.get(_f) > max) {
                max = unitsToErrors.get(_f);
                f = _f;
            }
        }

        if (f == null) throw new IllegalArgumentException();

        return f;
    }

    private List<Node> neighbors(Unit q) {
        return graph.getAdjacentNodes(unitsToNodes.get(q));
    }

    private Unit unitWithMaximumError() {
        double max = Double.NEGATIVE_INFINITY;
        Unit q = null;

        for (Unit unit : unitsToErrors.keySet()) {
            if (unitsToErrors.get(unit) > max) {
                max = unitsToErrors.get(unit);
                q = unit;
            }
        }

        if (q == null) throw new IllegalArgumentException();

        return q;
    }

    private boolean shouldStop() {
        return unitIndex > getMaxUnits();
    }

    private void moveUnit(Unit unit, DoubleMatrix1D signal, double epsilon) {
        for (int j = 0; j < signal.size(); j++) {
            delta.set(j,  signal.get(j) - unit.getVector().get(j));
        }

        for (int j = 0; j < signal.size(); j++) {
            delta.set(j, epsilon * delta.get(j));
        }

        unit.moveVector(delta);
    }

    private void incrementError(Unit s1, DoubleMatrix1D signal) {
        double error = unitsToErrors.get(s1);
        error += getMetric().dissimilarity(s1.getVector(), signal);
        unitsToErrors.put(s1, error);
    }

    private void calculateDistancesToSignal(DoubleMatrix1D signal) {
        for (Unit unit : units) {
            unit.calculateDistanceToSignal(signal);
        }
    }

    private Node addUnit(DoubleMatrix1D vector) {
        Unit unit = new Unit(vector);
        String name = "X" + (++unitIndex);
        units.add(unit);
        GraphNode node = new GraphNode(name);
        graph.addNode(node);
        unitsToNodes.put(unit, node);
        nodesToUnits.put(node, unit);
        unitsToNodes.put(unit, node);
        unitsToErrors.put(unit, 0.0);
        return node;
    }

    public DoubleMatrix2D getUnitsAsMatrix() {
        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(units.size(), units.get(0).getVector().size());

        for (int i = 0; i < matrix.rows(); i++) {
            for (int j = 0; j < matrix.columns(); j++) {
                matrix.set(i, j, units.get(i).getVector().get(j));
            }
        }

        return matrix;
    }

    public int getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(int ageMax) {
        this.ageMax = ageMax;
    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }


    public double getEpsilonB() {
        return epsilonB;
    }

    public void setEpsilonB(double epsilonB) {
        this.epsilonB = epsilonB;
    }

    public double getEpsilonN() {
        return epsilonN;
    }

    public void setEpsilonN(double epsilonN) {
        this.epsilonN = epsilonN;
    }

    public int getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    /**
     * Returns a string representation of the cluster result.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

//        for (int i = 0; i < units.size(); i++) {
//            buf.append("\nUnit " + i + ": " + units.get(i));
//        }

        buf.append("Connected components: " );
        List<List<Node>> components = GraphUtils.connectedComponents(graph);

        for (int i = 0; i < components.size(); i++) {
            buf.append("\n" + i + ": ");
            buf.append(components.get(i));
        }

        buf.append("\nGraph = " );
        buf.append(graph);

        return buf.toString();
    }

    //==========================PRIVATE METHODS===========================//

    // Returns a random point from the data. Note that it is assumed this
    // point will NOT BE ALTERED, as we are not returning a copy.
    private DoubleMatrix1D nextSignal() {
        int i = RandomUtil.getInstance().nextInt(data.rows());
        numSignalsGenerated++;
        return data.viewRow(i);
    }


//    private DoubleMatrix1D nextSignal() {
//        DoubleMatrix1D vector = new DenseDoubleMatrix1D(data.columns());
//
//        int i1 = PersistentRandomUtil.getInstance().nextInt(2);
//
//        if (i1 == 0) {
//            for (int i = 0; i < vector.size(); i++) {
//                vector.set(i, PersistentRandomUtil.getInstance().nextDouble() + 5);
//            }
//        }
//        else {
//            for (int i = 0; i < vector.size(); i++) {
//                vector.set(i, PersistentRandomUtil.getInstance().nextDouble() - 5);
//            }
//        }
//
//        numSignalsGenerated++;
//
//        return vector;
//    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Dissimilarity getMetric() {
        return metric;
    }

    public void setMetric(Dissimilarity metric) {
        this.metric = metric;
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
            Gng.Unit other = (Gng.Unit) o;
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
