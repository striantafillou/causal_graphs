package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;

import java.util.LinkedList;

/**
 * Given a pattern, lists all of the DAGs in that pattern. In the form of an
 * iterator--call hasNext() to see if there's another one and next() to get it.
 * next() will return null if there are no more.
 *
 * @author Joseph Ramsey
 */
public class DagInPatternIterator {

    /**
     * The stack of graphs, with annotations as to the arbitrary undirected
     * edges chosen in them and whether or not these edges have already been
     * oriented left and/or right.
     */
    private LinkedList<DecoratedGraph> decoratedGraphs = new LinkedList<DecoratedGraph>();
    private Graph storedGraph;
    private boolean returnedOne = false;
    private Knowledge knowledge = new Knowledge();

    /**
     * The given pattern must be a pattern. If it does not consist entirely of
     * directed and undirected edges and if it is not acyclic, it is rejected.
     *
     * @throws IllegalArgumentException if the pattern is not a pattern.
     */
    public DagInPatternIterator(Graph pattern) {
        if (knowledge == null) {
            throw new IllegalArgumentException();
        }

        for (Edge edge : pattern.getEdges()) {
            if (Edges.isDirectedEdge(edge) || Edges.isUndirectedEdge(edge)) {
                continue;
            }

            throw new IllegalArgumentException("A pattern consist only of " +
                    "directed and undirected edges: " + edge);
        }

//        if (pattern.existsDirectedCycle()) {
//            int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
//                    "There is a cycle in the pattern: " +
//                            GraphUtils.directedCycle(pattern) + " Continue?",
//                    "Sticky Wicket", JOptionPane.YES_NO_OPTION);
//
//            if (ret == JOptionPane.NO_OPTION) {
//                throw new IllegalArgumentException();
//            }
//        }

        decoratedGraphs.add(new DecoratedGraph(pattern, getKnowledge()));
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
    public Graph next() {
        if (storedGraph != null) {
            Graph temp = storedGraph;
            storedGraph = null;
            return temp;
        }

        if (decoratedGraphs.size() == 1 && decoratedGraphs.getLast().getEdge() == null
                && !returnedOne) {
            returnedOne = true;
            return new EdgeListGraph(decoratedGraphs.getLast().getGraph());
        }

        while (!decoratedGraphs.isEmpty()) {
            DecoratedGraph graph = decoratedGraphs.removeLast();

            if (graph.isOrientable()) {
                decoratedGraphs.addLast(graph);
                break;
            }
        }

        if (decoratedGraphs.isEmpty()) {
            return null;
        }

        DecoratedGraph graph;

        while ((graph = decoratedGraphs.getLast().orient()) != null) {
            decoratedGraphs.addLast(graph);
        }

        return new EdgeListGraph(decoratedGraphs.getLast().getGraph());
    }

    /**
     * Returns true just in case there is still a DAG remaining in the
     * enumeration of DAGs for this pattern.
     */
    public boolean hasNext() {
        if (storedGraph == null) {
            storedGraph = next();
        }

        return storedGraph != null;
    }

    public Knowledge getKnowledge() {
        return new Knowledge(knowledge);
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) throw new IllegalArgumentException();
        this.knowledge = knowledge;
    }

    //==============================CLASSES==============================//

    private static class DecoratedGraph {
        private Graph graph;
        private Edge edge;
        private boolean triedLeft = false;
        private boolean triedRight = false;
        private Knowledge knowledge;

        public DecoratedGraph(Graph graph, Knowledge knowledge) {
            this.graph = graph;
            this.edge = findUndirectedEdge(graph);
            this.knowledge = knowledge;
        }

        //=============================PUBLIC METHODS=======================//

        private Edge findUndirectedEdge(Graph graph) {
            for (Edge edge : graph.getEdges()) {
                if (Edges.isUndirectedEdge(edge)) {
                    return edge;
                }
            }

            return null;
        }

        public Graph getGraph() {
            return graph;
        }

        public Edge getEdge() {
            return edge;
        }

        public String toString() {
            return graph.toString();
        }

        public void triedDirectLeft() {
            triedLeft = true;
        }

        public boolean isOrientable() {
            if (edge == null) {
                return false;
            }

            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            if (!triedLeft && !graph.isAncestorOf(node1, node2) &&
                    !getKnowledge().edgeForbidden(node2.getName(), node1.getName())) {
                return true;
            }

            if (!triedRight && !graph.isAncestorOf(node2, node1) &&
                    !getKnowledge().edgeForbidden(node1.getName(), node2.getName())) {
                return true;
            }

            return false;
        }

        public DecoratedGraph orient() {
            if (edge == null) {
                return null;
            }

            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            if (!triedLeft && !graph.isAncestorOf(node1, node2) &&
                    !getKnowledge().edgeForbidden(node2.getName(), node1.getName())) {
                Graph graph = new EdgeListGraph(this.graph);
                graph.removeEdge(edge.getNode1(), edge.getNode2());
                graph.addDirectedEdge(edge.getNode2(), edge.getNode1());
                MeekRules meek = new MeekRules();
                meek.setKnowledge(knowledge);
//                meek.setAggressivelyPreventCycles(true);
                meek.orientImplied(graph);
                triedLeft = true;
                return new DecoratedGraph(graph, getKnowledge());
            }

            if (!triedRight && !graph.isAncestorOf(node2, node1) &&
                    !getKnowledge().edgeForbidden(node1.getName(), node2.getName())) {
                Graph graph = new EdgeListGraph(this.graph);
                graph.removeEdge(edge.getNode1(), edge.getNode2());
                graph.addDirectedEdge(edge.getNode1(), edge.getNode2());
                MeekRules meek = new MeekRules();
                meek.setKnowledge(knowledge);
                meek.orientImplied(graph);
//                meek.setAggressivelyPreventCycles(true);
                meek.orientImplied(graph);
                triedRight = true;
                return new DecoratedGraph(graph, getKnowledge());
            }

            return null;
        }

        private Knowledge getKnowledge() {
            return knowledge;
        }
    }
}
