package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Result of the Shimizu search.
 *
 * @author Gustavo Lacerda
 */
public final class ShimizuResult {

    private Graph graph;

    /**
     * A Dag has a list of edges; therefore, Hashmap from edges to weights.
     */
    private HashMap<Edge, Double> weightHash;

    /**
     * Only non-null when graph is a pattern.
     */
    private Dag patternDag = null;

    private int errorsOfOmission = 0;
    private int errorsOfCommission = 0;
    private int oriEvaluated = 0;
    private int oriCorrect = 0;
    private int oriIncorrect = 0;
    private int oriUndirected = 0;
    private List<Edge> correctOrientationEdges;

    /**
     * Evaluating coefficients.
     */
    private double totalCoeffErrorSq = 0;

    //=============================CONSTRUCTORS============================//

    public ShimizuResult(Graph graph) {
        this.graph = graph;
        weightHash = new HashMap<Edge, Double>();
    }

    public ShimizuResult(Graph graph, Dag patDag) {
        this(graph);
        this.patternDag = patDag;
    }

    //=============================PUBLIC METHODS=========================//


    public Graph getGraph() {
        return graph;
    }

    public double getWeight(Edge edge) {
        return weightHash.get(edge);
    }

    public void setWeight(Edge edge, double v) {
        weightHash.put(edge, v);
    }

    public Dag getPatternDag() {
        return patternDag;
    }

    /**
     * Iterate through the edges and print their weight too.
     */
    public String toString() {
        String str = "";
        for (Edge edge : getGraph().getEdges()) {
            str += edge.toString();
            str += "   " + getWeight(edge) + "\n";
        }
        return str;
    }

    /**
     * For each edge in this DAG, check whether it is in genDag. If it isn't
     * that's an error of commission. (Can we generalize this to Graph?)
     */
    public void evalAdjacency(Dag genDag) {
        for (Edge thisEdge : this.getGraph().getEdges()) {
            System.out.print("thisEdge = " + thisEdge);

            //is it in this DAG?
            Edge genEdge = getCorrespondingEdge(genDag, thisEdge);
            System.out.println(", genEdge = " + genEdge);

            boolean adjCorrect = (thisEdge != null);
            if (!adjCorrect) {
                errorsOfCommission++;
            }
        }

        //for each edge in genDag, check whether it is in this DAG. If it isn't, that's an error of
        //omission.
        for (Edge genEdge : genDag.getEdges()) {
            System.out.print("genEdge = " + genEdge);

            //is it in this DAG?
            Edge thisEdge = getCorrespondingEdge(this.getGraph(), genEdge);
            System.out.println(", thisEdge = " + thisEdge);

            boolean adjCorrect = (thisEdge != null);
            if (!adjCorrect) {
                errorsOfOmission++;
            }
        }
    }


    public void printAdjacencyEvaluation() {
        System.out.println("== Results of evaluating adjacency ==");
        System.out.println("errorsOfOmission = " + errorsOfOmission);
        System.out.println("errorsOfCommission = " + errorsOfCommission);
    }

    /**
     * Evaluating orientations. Should only evaluate on the adjacencies that are
     * correct
     */
    public void evalOrientations(Dag genDag) {
        correctOrientationEdges = new Vector();

        for (Edge genEdge : genDag.getEdges()) {

            Edge thisEdge = getCorrespondingEdge(this.getGraph(), genEdge);
            System.out.print("genEdge = " + genEdge);
            System.out.println(", thisEdge = " + thisEdge);

            //skip the ones that are not adjacent
            if (thisEdge == null)
                continue;

            oriEvaluated++;

            if (thisEdge.isDirected()) { //directed: compare direction
                if (getCorrespondingDirectedEdge(this.getGraph(), genEdge) != null) {
                    oriCorrect++;
                    correctOrientationEdges.add(thisEdge);
                }
                else {
                    oriIncorrect++;
                }

            }
            else { //undirected, do nothing
                oriUndirected++;
            }
        }
        System.out.print("\n");

    }


    public void printOrientationEvaluation() {
        System.out.println("== Results of evaluating orientation ==");
        System.out.println("oriCorrect = " + oriCorrect + "  oriIncorrect = " + oriIncorrect + "  oriUndirected = " + oriUndirected);
        System.out.println("oriEvaluated = " + oriEvaluated);
    }


    /**
     * Should only evaluate those whose orientation is correct.
     */
    public void evalCoeffs(ShimizuResult genDag) {
        List<Edge> edgesToEvaluate;
        if (getPatternDag() != null) //we use it
        {
            edgesToEvaluate = new Vector();
            //add only the patDag edges whose orientation is correct
            for (Edge patDagEdge : getPatternDag().getEdges()) {
                Edge genEdge = getCorrespondingEdge(genDag.getGraph(), patDagEdge);
                if (oriAgrees(patDagEdge, genEdge))
                    edgesToEvaluate.add(getCorrespondingEdge(this.getGraph(), patDagEdge));
            }
        }
        else
            edgesToEvaluate = correctOrientationEdges;

        for (Edge edge : edgesToEvaluate) {
            double thisCoeff = this.getWeight(edge);
            Edge genEdge = getCorrespondingEdge(genDag.getGraph(), edge);
            double genCoeff = genDag.getWeight(genEdge);
            double diff = thisCoeff - genCoeff;
            totalCoeffErrorSq += java.lang.Math.pow(diff, 2);
        }

    }

    public void printCoefficientEvaluation() {
        System.out.println("== Results of evaluating coefficients ==");
        System.out.println("totalCoeffErrorSq = " + totalCoeffErrorSq + ", from " +
                correctOrientationEdges.size() + " edges.");
    }


    /**
     * Returns the edge of graph corresponding to edge.
     */
    public static Edge getCorrespondingEdge(Graph graph, Edge edge) {
//		System.out.println("entered getCorrespondingEdge: edge = " + edge);
        String nodeName1 = edge.getNode1().getName();
        String nodeName2 = edge.getNode2().getName();
        Node node1 = graph.getNode(nodeName1);
        Node node2 = graph.getNode(nodeName2);
//		System.out.println("getCorrespondingEdge: node1 = " + node1);
//		System.out.println("getCorrespondingEdge: node2 = " + node2);
        Edge result = graph.getEdge(node1, node2);
//		System.out.println("getCorrespondingEdge: result = " + result);
        return result;
    }


    /**
     * Returns the directed edge of graph corresponding to edge.
     */
    public static Edge getCorrespondingDirectedEdge(Graph graph, Edge edge) {
        String nodeName1 = edge.getNode1().getName();
        String nodeName2 = edge.getNode2().getName();
        Node node1 = graph.getNode(nodeName1);
        Node node2 = graph.getNode(nodeName2);
        Edge result = graph.getDirectedEdge(node1, node2);
        return result;
    }

    //==============================PRIVATE METHODS========================//

    //either both point to the left or both point to the right

    // Gustavo---this method didn't do what you though it did, since edges
    // oriented one way are always oriented to the right. (See the constructor
    // Edge.)

    private boolean oriAgrees(Edge edge1, Edge edge2) {
        return edge1.pointsTowards(edge1.getNode2()) && edge2.pointsTowards(edge1.getNode2());
    }
}

