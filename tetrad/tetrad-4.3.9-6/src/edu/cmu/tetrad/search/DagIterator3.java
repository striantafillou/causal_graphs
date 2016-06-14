package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.CombinationGenerator;
import edu.cmu.tetrad.util.DepthChoiceGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a graph, lists all DAGs over the given list of variables. Does it the
 * old fashioned way, by actually producing all of the graphs you would get by
 * orienting each uoriented edge each way, in every combination, and then
 * rembembering just the ones that were acyclic.
 *
 * @author Joseph Ramsey
 */
public class DagIterator3 {
    private LinkedList<Dag> dags = new LinkedList<Dag>();
    private int index = -1;
    private DepthChoiceGenerator generator1;
    private CombinationGenerator generator2;
    private List<Edge> edges;
    private List<Node> nodes;
    private Dag storedDag;
    private ArrayList<Edge> undirectedEdges;
    private int minEdges;
    private int maxEdges;

    /**                                          
     * The given graph must be a graph. If it does not consist entirely of
     * directed and undirected edges and if it is not acyclic, it is rejected.
     *
     * @throws IllegalArgumentException if the graph is not a graph.
     */
    public DagIterator3(List<Node> nodes, int minEdges, int maxEdges) {
        if (nodes == null) {
            throw new IllegalArgumentException("No nodes provided.");
        }

        if (minEdges < 0) {
            throw new IllegalArgumentException("Min edges should >= 0");
        }

        if (maxEdges < -1) {
            throw new IllegalArgumentException("Min edges should >= 0 or -1 (unbounded)");
        }

        int n = nodes.size();
        int realMaxEdges = n * (n - 1) / 2;
        if (maxEdges < -1 || maxEdges > realMaxEdges) {
            throw new IllegalArgumentException("Max edges should be in [-1, " + realMaxEdges +"]: " + maxEdges);
        }

        this.nodes = new ArrayList<Node>(nodes);
        this.minEdges = minEdges;
        this.maxEdges = maxEdges;
        Graph graph = new EdgeListGraph(nodes);
        graph.fullyConnect(Endpoint.TAIL);
        edges = graph.getEdges();
        generator1 = new DepthChoiceGenerator(edges.size(), maxEdges);
        nextEdgeCombination();
    }

    /**
     * Successive calls to this method return successive DAGs in the pattern, in
     * a more or less natural enumeration of them in which an arbitrary
     * undirected edge is picked, oriented one way, Meek rules applied, then a
     * remaining unoriented edge is picked, oriented one way, and so on, until a
     * DAG is obtained, and then by backtracking the other orientation of each
     * chosen edge is tried. Nonrecursive, obviously.
     * <p/>
     * Returns a Graph instead of a DAG because sometimes, due to faulty
     * patterns, a cyclic graph is produced, and the end-user may need to decide
     * what to do with it. The simplest thing is to construct a DAG (Dag(graph))
     * and catch an exception.
     */
    public Dag next() {
        if (storedDag != null) {
            Dag temp = storedDag;
            storedDag = null;
            return temp;
        }

        if (generator2 != null) {
            Dag dag = nextDag();

            if (dag != null) {
                return dag;
            }
        }

        if (generator1 != null) {
            nextEdgeCombination();

            if (generator2 != null) {
                Dag dag = nextDag();

                if (dag != null) {
                    return dag;
                }
            }
        }

        return null;
    }

    /**
     * Returns true just in case there is still a DAG remaining in the
     * enumeration of DAGs for this pattern.
     */
    public boolean hasNext() {
        if (generator1 == null) {
            return false;
        }

        if (storedDag == null) {
            storedDag = next();
        }

        return storedDag != null;
    }

    private void nextEdgeCombination() {
        if (generator1 == null) {
            throw new IllegalArgumentException();
        }

//        int[] choice = generator1.next();

        int[] choice;

        while ((choice = generator1.next()) != null) {
            if (choice.length >= minEdges) {
                break;
            }
        }

        if (choice == null || (maxEdges != -1 && choice.length > maxEdges)) {
            generator1 = null;
            return;            
        }

//        if (choice == null) {
//            generator1 = null;
//            return;
//        }

        undirectedEdges = new ArrayList<Edge>();

        for (int i = 0; i < choice.length; i++) {
            undirectedEdges.add(edges.get(choice[i]));
        }

        int[] dims = new int[undirectedEdges.size()];

        for (int i = 0; i < undirectedEdges.size(); i++) {
            dims[i] = 2;
        }

        generator2 = new CombinationGenerator(dims);
    }

    private Dag nextDag() {
        if (generator2 == null) {
            throw new IllegalArgumentException();
        }

        int[] combination;

        while ((combination = generator2.next()) != null) {
            Graph graph = new EdgeListGraph(nodes);

            for (int k = 0; k < combination.length; k++) {
                Edge edge = undirectedEdges.get(k);
                graph.removeEdge(edge.getNode1(), edge.getNode2());

                if (combination[k] == 0) {
                    graph.addDirectedEdge(edge.getNode1(), edge.getNode2());
                } else {
                    graph.addDirectedEdge(edge.getNode2(), edge.getNode1());
                }
            }

            if (!graph.existsDirectedCycle()) {
                return new Dag(graph);
            }
        }
        
        generator2 = null;
        return null;
    }

}