package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.search.indtest.SearchLogUtils;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

import java.text.NumberFormat;
import java.util.*;

/**
 * Searches for a pattern representing all of the Markov blankets for a given
 * target T consistent with the given independence information. This pattern may
 * be used to generate the actual list of DAG's that might be Markov blankets.
 *
 * @author Joseph Ramsey
 */
public final class CefsSepset {

    /**
     * If <code>stopPoint</code> is set to this, then the graph after step 3
     * will be returned as the result--that is, after the three fan-construction
     * stages.
     */
    public static final int UNTRIMMED_UNORIENTED_ADJACENCY_GRAPH = 0;

    /**
     * If <code>stopPoint</code> is set to this, then the graph after step 4
     * will be returned--that is, after PC orientation has been done.
     */
    public static final int UNTRIMMED_ORIENTED_ADJACENCY_GRAPH = 1;

    /**
     * If <code>stopPoint</code> is set to this, then the algorithm will proceed
     * to the normal stopping point; the fan constructions steps will be
     * performed, then PC orientation, then trimming to a Markov blanket
     * pattern.
     */
    public static final int TRIMMED_ORIENTED_ADJACENCY_GRAPH = 2;

    /**
     * The point at this the algorithm will stop. Must be one of
     * UNTRIMMED_UNORIENTED_ADJACENCY_GRAPH, UNTRIMMED_ORIENTED_ADJACENCY_GRAPH,
     * or TRIMMED_ORIENTED_ADJACENCY_GRAPH.
     */
    private int stopPoint = CefsSepset.TRIMMED_ORIENTED_ADJACENCY_GRAPH;

    /**
     * The independence test used to perform the search.
     */
    private IndependenceTest test;

    /**
     * The list of variables being searched over. Must contain the target.
     */
    private List<Node> variables;

    /**
     * The target variable.
     */
    private Node target;

    /**
     * The depth to which independence tests should be performed--i.e. the
     * maximum number of conditioning variables for any independence test.
     */
    private int depth;

    /**
     * The pattern output by the most recent search. This is saved in case the
     * user wants to generate the list of MB DAGs.
     */
    private Graph resultGraph;

    /**
     * A count of the number of independence tests performed in the course of
     * the most recent search.
     */
    private long numIndependenceTests;

    /**
     * Information to help understand what part of the search is taking the most
     * time.
     */
    private int[] maxRemainingAtDepth;

    /**
     * Information to help understand what part of the search is taking the most
     * time.
     */
    private Node[] maxVariableAtDepth;

    /**
     * The set of nodes that edges should not be drawn to in the
     * addDepthZeroAssociates method.
     */
    private Set<Node> visited;

    /**
     * Elapsed time for the last run of the algorithm.
     */
    private long elapsedTime;

    /**
     * The true graph, if known. If this is provided, notes will be printed out
     * for edges removed that are in the true Markov blanket.
     */
    private Dag trueMb;

    private SepsetMap sepset;

    //==============================CONSTRUCTORS==========================//

    /**
     * Constructs a new search.
     *
     * @param test  The source of conditional independence information for the
     *              search.
     * @param depth The maximum number of variables conditioned on for any
     *              independence test in the search.
     */
    public CefsSepset(IndependenceTest test, int depth) {
        if (test == null) {
            throw new NullPointerException();
        }

        if (depth == -1) {
            depth = Integer.MAX_VALUE;
        }

        if (depth < 0) {
            throw new IllegalArgumentException("Depth must be >= -1: " + depth);
        }

        this.test = test;
        this.depth = depth;
        this.variables = test.getVariables();
    }

    //===============================PUBLIC METHODS=======================//

    /**
     * Searches for the MB Pattern for the given target.
     *
     * @param targetName The name of the target variable.
     */
    public Graph search(String targetName) {
        long start = System.currentTimeMillis();
        this.numIndependenceTests = 0;

        this.sepset = new SepsetMap();

        if (targetName == null) {
            throw new IllegalArgumentException(
                    "Null target name not permitted");
        }

        this.target = getVariableForName(targetName);

        // Some statistics.
        this.maxRemainingAtDepth = new int[20];
        this.maxVariableAtDepth = new Node[20];
        Arrays.fill(maxRemainingAtDepth, -1);
        Arrays.fill(maxVariableAtDepth, null);

        TetradLogger.getInstance().info("target = " + getTarget());

        Graph graph = new EdgeListGraph();

        // Each time the addDepthZeroAssociates method is called for a node
        // v, v is added to this set, and edges to elements in this set are
        // not added to the graph subsequently. This is to visited the situation
        // of adding v1---v2 to the graph, removing it by conditioning on
        // nodes adjacent to v1, then re-adding it and not being able to
        // remove it by conditioning on nodes adjacent to v2. Once an edge
        // is removed, it should not be re-added to the graph.
        // jdramsey 8/6/04
        this.visited = new HashSet<Node>();

        // Step 1. Get associates for the target.
        TetradLogger.getInstance().info("BEGINNING step 1 (prune target).");

        graph.addNode(getTarget());
        constructFan(getTarget(), graph);

        TetradLogger.getInstance().log("graph","After step 1 (prune target)" + graph);

        // Step 2. Get associates for each variable adjacent to the target,
        // removing edges based on those associates where possible. After this
        // step, adjacents to the target are parents or children of the target.
        // Call this set PC.
        TetradLogger.getInstance().info("BEGINNING step 2 (prune PC).");

        for (Node v : graph.getAdjacentNodes(getTarget())) {
            constructFan(v, graph);
        }

        TetradLogger.getInstance().log("graph","After step 2 (prune PC)" + graph);

//        // Step 3. Get associates for each node now two links away from the
//        // target, removing edges based on those associates where possible.
//        // After this step, adjacents to adjacents of the target are parents
//        // or children of adjacents to the target. Call this set PCPC.
//        LogUtils.getInstance().info("BEGINNING step 3 (prune PCPC).");
//
//        for (Node v : graph.getAdjacentNodes(getTarget())) {
//            for (Node w : graph.getAdjacentNodes(v)) {
//                if (getVisited().contains(w)) {
//                    continue;
//                }
//
//                constructFan(w, graph);
//            }
//        }
//
//        if (getStopPoint() == UNTRIMMED_UNORIENTED_ADJACENCY_GRAPH) {
//            finishUp(start, graph);
//            return graph;
//        }
//
//        LogUtils.getInstance().fine("After step 3 (prune PCPC)" + graph);

        TetradLogger.getInstance().info("BEGINNING step 4 (PC Orient).");

        Knowledge bk = new Knowledge();
        SearchGraphUtils.pcOrientbk(bk, graph, graph.getNodes());

        List<Node> _visited = new LinkedList<Node>(getVisited());

        Knowledge knowledge = new Knowledge();
        TetradLogger.getInstance().info("Staring PC Orientation.");

        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
        SearchGraphUtils.orientCollidersUsingSepsets(sepset, knowledge, graph);
        MeekRules rules = new MeekRules();
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);

        TetradLogger.getInstance().info("Finishing PC Orientation");
//        MeekRules orientation = new MeekRules(sepset, new Knowledge(), graph.getNodes());
//        orientation.doImpliedOrientations(graph);

        if (getStopPoint() == CefsSepset.UNTRIMMED_ORIENTED_ADJACENCY_GRAPH) {
            finishUp(start, graph);
            return graph;
        }

        TetradLogger.getInstance().log("graph","After step 4 (PC Orient)" + graph);

        TetradLogger.getInstance().info("BEGINNING step 5 (Trim graph to {T} U PC U " +
                "{Parents(Children(T))}).");

        MbUtils.trimToNeighborhood(graph, _visited);
//        MbUtils.trimToMbNodes(graph, getTarget());

        TetradLogger.getInstance().log("graph",
                "After step 5 (Trim graph to {T} U PC U {Parents(Children(T))})" +
                        graph);

//        LogUtils.getInstance().info("BEGINNING step 6 (Remove edges among P and P of C).");
//
//        MbUtils.trimEdgesAmongParents(graph, getTarget());
//        MbUtils.trimEdgesAmongParentsOfChildren(graph, getTarget());
//
//        LogUtils.getInstance().fine("After step 6 (Remove edges among P and P of C)" + graph);

        TetradLogger.getInstance().details("Bounds: ");
        for (int i = 0; i < maxRemainingAtDepth.length; i++) {
            if (maxRemainingAtDepth[i] != -1) {
                TetradLogger.getInstance().details("\ta" + i + " = " + maxRemainingAtDepth[i] +
                        " (" + maxVariableAtDepth[i] + ")");
            }
        }

        finishUp(start, graph);
        return graph;
    }

    private void finishUp(long start, Graph graph) {
        long stop = System.currentTimeMillis();
        this.elapsedTime = stop - start;
        double seconds = this.elapsedTime / 1000d;

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
//        System.out.println("MB fan search took " + nf.format(seconds) + " seconds.");
        TetradLogger.getInstance().info("MB fan search took " + nf.format(seconds) + " seconds.");
        TetradLogger.getInstance().info("Number of independence tests performed = " +
                getNumIndependenceTests());

        this.resultGraph = graph;
    }

    /**
     * Generates the list of MB DAGs consistent with the MB Pattern returned by
     * the previous search.
     *
     * @param orientBidirectedEdges True iff bidirected edges should be oriented
     *                              as if they were undirected.
     * @return a list of Dag's.
     */
    public List<Graph> generateDags(boolean orientBidirectedEdges) {
        return new LinkedList<Graph>(listDags(new EdgeListGraph(resultGraph()),
                orientBidirectedEdges));
    }

    public List<Graph> generateDags(Graph resultGraph,
            boolean orientBidirectedEdges) {
        return new LinkedList<Graph>(listDags(new EdgeListGraph(resultGraph),
                orientBidirectedEdges));
    }

    public long getNumIndependenceTests() {
        return numIndependenceTests;
    }

    public Node getTarget() {
        return target;
    }

    public String getAlgorithmName() {
        return "CE Fan Search Sepset";
    }

    public Graph getMbPattern() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Dag getTrueMb() {
        return trueMb;
    }

    public void setTrueMb(Dag trueMb) {
        this.trueMb = trueMb;
    }


    public int getStopPoint() {
        return stopPoint;
    }

    public void setStopPoint(int stopPoint) {
        switch (stopPoint) {
            case CefsSepset.UNTRIMMED_UNORIENTED_ADJACENCY_GRAPH:
            case CefsSepset.UNTRIMMED_ORIENTED_ADJACENCY_GRAPH:
            case CefsSepset.TRIMMED_ORIENTED_ADJACENCY_GRAPH:
                break;
            default:
                throw new IllegalArgumentException("Must be one of " +
                        "UNTRIMMED_UNORIENTED_ADJACENCY_GRAPH," +
                        "UNTRIMMED_ORIENTED_ADJACENCY_GRAPH, or " +
                        "TRIMMED_ORIENTED_ADJACENCY_GRAPH.");
        }

        this.stopPoint = stopPoint;
    }

    //================================PRIVATE METHODS====================//

    /**
     * Adds associates of the target and prunes edges using subsets of adjacents
     * to the target.
     */
    private void constructFan(Node target, Graph graph) {
        addAllowableAssociates(target, graph);
        prune(target, graph);
    }

    private void addAllowableAssociates(Node v, Graph graph) {
        this.getVisited().add(v);
        int numAssociated = 0;

        for (Node w : variables) {
            if (getVisited().contains(w)) {
                continue;
            }

            if (graph.containsNode(w) && graph.isAdjacentTo(v, w)) {
                continue;
            }

            if (!independent(v, w, new LinkedList<Node>())) {
                addEdge(graph, w, v);
                numAssociated++;
            }
        }

        noteMaxAtDepth(0, numAssociated, v);
    }

    private void prune(Node node, Graph graph) {
        for (int depth = 1; depth <= getDepth(); depth++) {
            if (graph.getAdjacentNodes(node).size() < depth) {
                return;
            }

            prune(node, graph, depth);
        }
    }

    /**
     * Tries node remove the edge 'node'---from using adjacent nodes node
     * 'from', then tries node remove each other edge adjacent node 'from' using
     * remaining edges adjacent node 'from.' If the edge 'node' is removed, the
     * method immediately returns.
     */
    private void prune(Node node, Graph graph, int depth) {
        TetradLogger.getInstance().details("Trying node remove edges adjacent node " + node +
                ", depth = " + depth + ".");

        // Otherwise, try removing all other edges adjacent node node. Return
        // true if more edges could be removed at the next depth.
        List<Node> a = new LinkedList<Node>(graph.getAdjacentNodes(node));

        nextEdge:
        for (Node y : a) {
            List<Node> adjX =
                    new LinkedList<Node>(graph.getAdjacentNodes(node));
            adjX.remove(y);

            if (adjX.size() >= depth) {
                ChoiceGenerator cg = new ChoiceGenerator(adjX.size(), depth);
                int[] choice;

                while ((choice = cg.next()) != null) {
                    List<Node> condSet = SearchGraphUtils.asList(choice, adjX);

                    if (independent(node, y, condSet)) {
                        graph.removeEdge(node, y);

                        // The target itself must not be removed.
                        if (graph.getEdges(y).isEmpty() && y != getTarget()) {
                            graph.removeNode(y);
                        }

                        continue nextEdge;
                    }
                }
            }
        }

        int numAdjacents = graph.getAdjacentNodes(node).size();
        noteMaxAtDepth(depth, numAdjacents, node);
    }

    private boolean independent(Node v, Node w, List<Node> z) {
        boolean independent = getTest().isIndependent(v, w, z);

        if (independent) {
            this.sepset.set(v, w, z);

            if (getTrueMb() != null) {
                Node node1 = getTrueMb().getNode(v.getName());
                Node node2 = getTrueMb().getNode(w.getName());

                if (node1 != null && node2 != null) {
                    Edge edge = getTrueMb().getEdge(node1, node2);

                    if (edge != null) {
                        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
                        System.out.println(
                                "Edge removed that was in the true MB:");
                        System.out.println("\tTrue edge = " + edge);
                        System.out.println("\t" +
                                SearchLogUtils.independenceFact(v, w, z) +
                                "\tp = " +
                                nf.format(getTest().getPValue()));
                    }
                }
            }
        }

        this.numIndependenceTests++;
        return independent;
    }

    private void addEdge(Graph graph, Node w, Node v) {
        if (!graph.containsNode(w)) {
            graph.addNode(w);
        }

        graph.addUndirectedEdge(v, w);
    }

    private Node getVariableForName(String targetVariableName) {
        Node target = null;

        for (Node V : variables) {
            if (V.getName().equals(targetVariableName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetVariableName);
        }

        return target;
    }

    private void noteMaxAtDepth(int depth, int numAdjacents, Node to) {
        if (depth < maxRemainingAtDepth.length &&
                numAdjacents > maxRemainingAtDepth[depth]) {
            maxRemainingAtDepth[depth] = numAdjacents;
            maxVariableAtDepth[depth] = to;
        }
    }

    private int getDepth() {
        return this.depth;
    }

    /**
     * The recursive method used to list the MB DAGS consistent with an MB
     * Pattern (i.e. with the independence information available to the search.
     */
    private Set<Graph> listDags(Graph pattern, boolean orientBidirectedEdges) {
        Set<Graph> dags = new HashSet<Graph>();
        Graph graph = new EdgeListGraph(pattern);
        doAbbreviatedOrientation(graph);
        List<Edge> edges = graph.getEdges();
        Edge edge = null;

        for (Edge _edge : edges) {
            if (orientBidirectedEdges && Edges.isBidirectedEdge(_edge)) {
                edge = _edge;
                break;
            }

            if (Edges.isUndirectedEdge(_edge)) {
                edge = _edge;
                break;
            }
        }

        if (edge == null) {
            dags.add(graph);
            return dags;
        }

        graph.setEndpoint(edge.getNode2(), edge.getNode1(), Endpoint.TAIL);
        graph.setEndpoint(edge.getNode1(), edge.getNode2(), Endpoint.ARROW);
        dags.addAll(listDags(graph, orientBidirectedEdges));

        graph.setEndpoint(edge.getNode1(), edge.getNode2(), Endpoint.TAIL);
        graph.setEndpoint(edge.getNode2(), edge.getNode1(), Endpoint.ARROW);
        dags.addAll(listDags(graph, orientBidirectedEdges));

        return dags;
    }

    /**
     * A reiteration of orientation steps 5-7 of the search for use in
     * generating the list of MB DAGs.
     */
    private void doAbbreviatedOrientation(Graph graph) {
        SearchGraphUtils.orientUsingMeekRulesLocally(new Knowledge(), graph,
                getTest(), depth);
        MbUtils.trimToMbNodes(graph, getTarget(), false);
        MbUtils.trimEdgesAmongParents(graph, getTarget());
        MbUtils.trimEdgesAmongParentsOfChildren(graph, getTarget());
    }

    private Graph resultGraph() {
        return resultGraph;
    }

    public static void orientCollidersLocally(Knowledge knowledge, Graph graph,
            IndependenceTest test,
            int depth, List<Node> nodesToVisit) {
        TetradLogger.getInstance().info("Starting Collider Orientation:");

//        List<Node> nodes = graph.getNodes();
        if (nodesToVisit == null) {
            nodesToVisit = graph.getNodes();
        }

        for (Node a : nodesToVisit) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node b = adjacentNodes.get(combination[0]);
                Node c = adjacentNodes.get(combination[1]);

                // Skip triples that are shielded.
                if (graph.isAdjacentTo(b, c)) {
                    continue;
                }

                if (SearchGraphUtils.isArrowpointAllowed1(b, a, knowledge) &&
                        SearchGraphUtils.isArrowpointAllowed1(c, a, knowledge)) {
                    if (!SearchGraphUtils.existsLocalSepsetWith(b, a, c, test, graph, depth)) {
                        graph.setEndpoint(b, a, Endpoint.ARROW);
                        graph.setEndpoint(c, a, Endpoint.ARROW);
                        TetradLogger.getInstance().colliderOriented(SearchLogUtils.colliderOrientedMsg(b, a, c));
                    }
                }
            }
        }

        TetradLogger.getInstance().info("Finishing Collider Orientation.");
    }

    public static void orientUsingMeekRulesLocally(Knowledge knowledge,
            Graph graph,
            IndependenceTest test,
            int depth,
            List<Node> neighborhood) {
        TetradLogger.getInstance().info("Starting Orientation Step D.");

        // Repeat until no more orientations are made.
        while (true) {
            if (CefsSepset.meekR1Locally(graph, knowledge, test, depth, neighborhood)) {
                continue;
            }

            if (CefsSepset.meekR2(graph, knowledge, neighborhood)) {
                continue;
            }

            if (CefsSepset.meekR3(graph, knowledge, neighborhood)) {
                continue;
            }

            if (CefsSepset.meekR4(graph, knowledge, neighborhood)) {
                continue;
            }

            break;
        }

        TetradLogger.getInstance().info("Finishing Orientation Step D.");
    }

    /**
     * Orient away from collider.
     */
    public static boolean meekR1Locally(Graph graph, Knowledge knowledge,
            IndependenceTest test, int depth,
            List<Node> neighborhood) {
//        List<Node> nodes = graph.getNodes();
        boolean changed = true;

        while (changed) {
            changed = false;

            for (Node a : neighborhood) {
                List<Node> adjacentNodes = graph.getAdjacentNodes(a);
                adjacentNodes.retainAll(neighborhood);

                if (adjacentNodes.size() < 2) {
                    continue;
                }

                ChoiceGenerator cg =
                        new ChoiceGenerator(adjacentNodes.size(), 2);
                int[] combination;

                while ((combination = cg.next()) != null) {
                    Node b = adjacentNodes.get(combination[0]);
                    Node c = adjacentNodes.get(combination[1]);

                    // Skip triples that are shielded.
                    if (graph.isAdjacentTo(b, c)) {
                        continue;
                    }

                    if (graph.getEndpoint(b, a) == Endpoint.ARROW &&
                            graph.isUndirectedFromTo(a, c)) {
                        if (SearchGraphUtils.existsLocalSepsetWithout(b, a, c, test, graph,
                                depth)) {
                            continue;
                        }

                        if (CefsSepset.isArrowpointAllowed(a, c, knowledge)) {
                            graph.setEndpoint(a, c, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R1", graph.getEdge(a, c)));
                            changed = true;
                        }
                    }
                    else if (graph.getEndpoint(c, a) == Endpoint.ARROW &&
                            graph.isUndirectedFromTo(a, b)) {
                        if (SearchGraphUtils.existsLocalSepsetWithout(b, a, c, test, graph,
                                depth)) {
                            continue;
                        }

                        if (CefsSepset.isArrowpointAllowed(a, b, knowledge)) {
                            graph.setEndpoint(a, b, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R1", graph.getEdge(a, b)));
                            changed = true;
                        }
                    }
                }
            }
        }

        return changed;
    }

    /**
     * If
     */
    public static boolean meekR2(Graph graph, Knowledge knowledge,
            List<Node> neighborhood) {
//        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : neighborhood) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);
            adjacentNodes.retainAll(neighborhood);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node b = adjacentNodes.get(combination[0]);
                Node c = adjacentNodes.get(combination[1]);

                if (graph.isDirectedFromTo(b, a) &&
                        graph.isDirectedFromTo(a, c) &&
                        graph.isUndirectedFromTo(b, c)) {
                    if (CefsSepset.isArrowpointAllowed(b, c, knowledge)) {
                        graph.setEndpoint(b, c, Endpoint.ARROW);
                        TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R2", graph.getEdge(b, c)));
                    }
                }
                else if (graph.isDirectedFromTo(c, a) &&
                        graph.isDirectedFromTo(a, b) &&
                        graph.isUndirectedFromTo(c, b)) {
                    if (CefsSepset.isArrowpointAllowed(c, b, knowledge)) {
                        graph.setEndpoint(c, b, Endpoint.ARROW);
                        TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R2", graph.getEdge(c, b)));
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Meek's rule R3. If a--b, a--c, a--d, c-->b, c-->b, then orient a-->b.
     */
    public static boolean meekR3(Graph graph, Knowledge knowledge,
            List<Node> neighborhood) {

//        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : neighborhood) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);
            adjacentNodes.retainAll(neighborhood);

            if (adjacentNodes.size() < 3) {
                continue;
            }

            for (Node b : adjacentNodes) {
//            for (int j = 0; j < adjacentNodes.size(); j++) {
//                Node b = adjacentNodes.get(j);

                List<Node> otherAdjacents = new LinkedList<Node>(adjacentNodes);
                otherAdjacents.remove(b);

                if (!graph.isUndirectedFromTo(a, b)) {
                    continue;
                }

                ChoiceGenerator cg =
                        new ChoiceGenerator(otherAdjacents.size(), 2);
                int[] combination;

                while ((combination = cg.next()) != null) {
                    Node c = otherAdjacents.get(combination[0]);
                    Node d = otherAdjacents.get(combination[1]);

                    if (graph.isAdjacentTo(c, d)) {
                        continue;
                    }

                    if (!graph.isUndirectedFromTo(a, c)) {
                        continue;
                    }

                    if (!graph.isUndirectedFromTo(a, d)) {
                        continue;
                    }

                    if (graph.isDirectedFromTo(c, b) &&
                            graph.isDirectedFromTo(d, b)) {
                        if (CefsSepset.isArrowpointAllowed(a, b, knowledge)) {
                            graph.setEndpoint(a, b, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R3", graph.getEdge(a, b)));
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        return changed;
    }

    /**
     *
     */
    public static boolean meekR4(Graph graph, Knowledge knowledge,
            List<Node> neighborhood) {
        if (knowledge == null) {
            return false;
        }

//        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : neighborhood) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);
            adjacentNodes.retainAll(neighborhood);

            if (adjacentNodes.size() < 3) {
                continue;
            }

            for (Node d : adjacentNodes) {
//            for (int j = 0; j < adjacentNodes.size(); j++) {
//                Node d = adjacentNodes.get(j);

                if (!graph.isUndirectedFromTo(a, d)) {
                    continue;
                }

                List<Node> otherAdjacents = new LinkedList<Node>(adjacentNodes);
                otherAdjacents.remove(d);

                ChoiceGenerator cg =
                        new ChoiceGenerator(otherAdjacents.size(), 2);
                int[] combination;

                while ((combination = cg.next()) != null) {
                    Node b = otherAdjacents.get(combination[0]);
                    Node c = otherAdjacents.get(combination[1]);

                    if (graph.isAdjacentTo(b, d)) {
                        continue;
                    }

                    if (!graph.isUndirectedFromTo(a, b)) {
                        continue;
                    }

                    if (!graph.isAdjacentTo(a, c)) {
                        continue;
                    }

//                    if (!(graph.isUndirectedFromTo(a, c) ||
//                            graph.isDirectedFromTo(a, c) ||
//                            graph.isDirectedFromTo(c, a))) {
//                        continue;
//                    }

                    if (graph.isDirectedFromTo(b, c) &&
                            graph.isDirectedFromTo(c, d)) {
                        if (CefsSepset.isArrowpointAllowed(a, d, knowledge)) {
                            graph.setEndpoint(a, d, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R4", graph.getEdge(a, d)));
                            changed = true;
                            break;
                        }
                    }
                    else if (graph.isDirectedFromTo(d, c) &&
                            graph.isDirectedFromTo(c, b)) {
                        if (CefsSepset.isArrowpointAllowed(a, b, knowledge)) {
                            graph.setEndpoint(a, b, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Meek R4", graph.getEdge(a, b)));
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Checks if an arrowpoint is allowed by background knowledge.
     */

    public static boolean isArrowpointAllowed(Object from, Object to,
            Knowledge knowledge) {
        if (knowledge == null) {
            return true;
        }
        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }

    public List<Node> findMb(String targetName) {
        Graph graph = search(targetName);
        List<Node> nodes = graph.getNodes();
        nodes.remove(target);
        return nodes;
    }

    public IndependenceTest getTest() {
        return test;
    }

    public Set<Node> getVisited() {
        return visited;
    }
}
