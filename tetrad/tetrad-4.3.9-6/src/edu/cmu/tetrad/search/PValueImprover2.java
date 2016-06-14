package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.CombinationGenerator;

import java.util.*;

/**
 * Improves the P value of a SEM IM by adding, removing, or reversing single
 * edges.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class PValueImprover2 {
    private DataSet dataSet;
    private Knowledge knowledge;
    private SemIm semIm;
    private Graph trueDag;

    public PValueImprover2(SemIm semIm, DataSet data, Knowledge knowledge) {
        this.semIm = semIm;
        this.dataSet = data;
        this.knowledge = knowledge;
    }

    public PValueImprover2(DataSet data, Knowledge knowledge) {
        this.dataSet = data;
        this.knowledge = knowledge;
    }

    public SemIm search() {
        double alpha = .05;
        Graph graph = new EdgeListGraph(semIm.getSemPm().getGraph());
        double pValue = scoreGraph(graph).getPValue();

        System.out.println(graph);
        System.out.println(pValue);

        pValue = scoreGraph(graph).getPValue();
        addRequiredEdges(graph);

        removeEdgesToLowerFml(graph);

        if (pValue < alpha) {
            adjustOrientations(alpha, graph);
        }

//        if (pValue < alpha) {
//            addEdges(alpha, graph);
//        }

        // Remove edges and adjust orientations.
        boolean changed = true;

        while (changed) {
            changed = false;
            adjustOrientations(alpha, graph);
            changed = removeEdges(alpha, graph, changed);
        }

        Score score = scoreGraph(graph);
        return score.getEstimatedSem();
    }

    public SemIm search2() {
        Graph graph = new EdgeListGraph(semIm.getSemPm().getGraph());

        for (Edge edge : graph.getEdges()) {
            List<Node> nodes = new ArrayList<Node>();
            nodes.add(edge.getNode1());
            nodes.add(edge.getNode2());

            Node n1 = trueDag.getNode(edge.getNode1().getName());
            Node n2 = trueDag.getNode(edge.getNode2().getName());

            Score score = scoreGraph(graph, nodes, getTrueDag());
            Edge trueEdge = trueDag.getEdge(n1, n2);
            List<List<Node>> treks = GraphUtils.treks(trueDag, n1, n2);

            System.out.println(edge + " pValue = " + score.getPValue() + " in graph : " +
                    trueEdge);

            for (List<Node> trek : treks) {
                System.out.println("\t" + trek);
            }
        }

        System.out.println("Other edges...");

        Graph fullyConnected = new EdgeListGraph(graph.getNodes());
        fullyConnected.fullyConnect(Endpoint.TAIL);
        List<Edge> allEdges = fullyConnected.getEdges();

        for (Edge edge : allEdges) {
            if (graph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                continue;
            }

            List<Node> nodes = new ArrayList<Node>();
            nodes.add(edge.getNode1());
            nodes.add(edge.getNode2());

            Node n1 = trueDag.getNode(edge.getNode1().getName());
            Node n2 = trueDag.getNode(edge.getNode2().getName());

            Score score = scoreGraph(graph, nodes, getTrueDag());
            Edge trueEdge = trueDag.getEdge(n1, n2);

            if (score.getPValue() < 0.005) {
                List<List<Node>> treks = GraphUtils.treks(trueDag, n1, n2);

                System.out.println(edge + " pValue = " + score.getPValue() + " in graph : " +
                        trueEdge);

                for (List<Node> trek : treks) {
                    System.out.println("\t" + trek);
                }
            }
        }

        return semIm;
    }

    public Graph search3() {

        double alpha = 0.005;
//        Graph graph = new EdgeListGraph(semIm.getSemPm().getGraph());
        List<Node> allNodes = dataSet.getVariables();

        Graph fullyConnected = new EdgeListGraph(allNodes);
        fullyConnected.fullyConnect(Endpoint.TAIL);
        List<Edge> allEdges = fullyConnected.getEdges();
        Graph trekGraph = new EdgeListGraph(allNodes);

        for (Edge edge : allEdges) {
            if (trekGraph.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                continue;
            }

            List<Node> nodes = new ArrayList<Node>();
            nodes.add(edge.getNode1());
            nodes.add(edge.getNode2());

            Graph testGraph = new EdgeListGraph(nodes);

            Score score = scoreGraph(testGraph);

            if (score.getPValue() < alpha) {
                trekGraph.addEdge(edge);
            }
        }

        System.out.println(trekGraph);

        Graph reducedGraph = new EdgeListGraph();

        for (Node node : trekGraph.getNodes()) {
            System.out.println("Adding node to reduced graph: " + node);
            reducedGraph.addNode(node);

            for (Edge edge : trekGraph.getEdges(node)) {
                if (!(reducedGraph.containsNode(edge.getDistalNode(node)))) {
                    continue;
                }

                if (reducedGraph.getEdge(edge.getNode1(), edge.getNode2()) != null) {
                    continue;
                }

                reducedGraph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
                System.out.println("Adding edge " + reducedGraph.getEdge(edge.getNode1(), edge.getNode2()));
            }

            for (Edge edge : reducedGraph.getEdges()) {
                System.out.println("### + " + edge);

                Node node1 = edge.getNode1();
                Node node2 = edge.getNode2();

//                reducedGraph.removeEdge(edge);

//                List<List<Node>> treks = GraphUtils.treks(reducedGraph, node1, node2);
//
//                for (List<Node> trek : treks) {
//                    System.out.println("\t" + trek);
//                }


//                if (!reducedGraph.existsUndirectedPathFromTo(node1, node2)) {
////                    System.out.println("Continuing");
//                    reducedGraph.addEdge(edge);
//                    continue;
//                }

//                reducedGraph.addEdge(edge);

                if (reducedGraph.isAdjacentTo(node1, node2)) {
//                    List<Node> commonAdj = new LinkedList<Node>();

                    Set<Node> adj1 = new LinkedHashSet<Node>(reducedGraph.getAdjacentNodes(node1));
                    Set<Node> adj2 = new LinkedHashSet<Node>(reducedGraph.getAdjacentNodes(node2));

                    adj1.retainAll(adj2);

                    List<Node> commonAdj = new LinkedList<Node>(adj1);


//                    for (Node _node : reducedGraph.getNodes()) {
//                        if (reducedGraph.isAdjacentTo(_node, node1) &&
//                                reducedGraph.isAdjacentTo(_node, node2)) {
//                            commonAdj.add(_node);
//                        }
//                    }

                    System.out.println("Common adjacencies for " + node1 + " and " + node2 + ": " + commonAdj);

                    if (commonAdj.isEmpty()) {
                        continue;
                    }

                    List<Node> nodes = new LinkedList<Node>();

                    nodes.add(commonAdj.get(0));
                    nodes.add(node1);
                    nodes.add(node2);

                    Graph subGraph = new EdgeListGraph(nodes);

                    for (int i = 0; i < nodes.size(); i++) {
                        for (int j = i + 1; j < nodes.size(); j++) {
                            if (reducedGraph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                                subGraph.addDirectedEdge(nodes.get(i), nodes.get(j));
                            }
                        }
                    }

                    subGraph.removeEdge(node1, node2);

                    Score score = scoreGraph(subGraph);
                    System.out.println("p = " + score.getPValue());

                    if (score.getPValue() > alpha) {
                        Edge _edge = reducedGraph.getEdge(node1, node2);
                        reducedGraph.removeEdge(edge);
                        System.out.println("Removing edge " + _edge);
                    }
                }
            }
        }

        for (Edge edge : reducedGraph.getEdges()) {
            reducedGraph.removeEdge(edge);
            reducedGraph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
        }

//        boolean changed = true;
//
//        while (changed) {
//            changed = false;
//            adjustOrientations(alpha, reducedGraph);
//            changed = removeEdges(alpha, reducedGraph, changed);
//        }
//        adjustOrientations(0.005, graph);


        orientColliders(trekGraph, reducedGraph);
        new MeekRules().orientImplied(reducedGraph);
//        removeEdges(alpha, SearchGraphUtils.dagFromPattern(reducedGraph), false);

        return reducedGraph;
    }

    public static void orientColliders(Graph trekMap, Graph graph) {
        List<Node> nodes = graph.getNodes();

        for (Node a : nodes) {
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

                if (!trekMap.isAdjacentTo(b, c)) {
                    graph.setEndpoint(b, a, Endpoint.ARROW);
                    graph.setEndpoint(c, a, Endpoint.ARROW);
                }
            }
        }
    }

    private void addRequiredEdges(Graph graph) {
        // Add required edges.
        List<Node> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                if (knowledge.edgeRequired(nodes.get(i).getName(), nodes.get(j).getName())) {
                    if (!graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                        graph.addDirectedEdge(nodes.get(i), nodes.get(j));
                    }
                }
            }
        }
    }

    private void addEdges(double alpha, Graph graph) {
        List<Node> nodes = graph.getNodes();
        Score score = scoreGraph(graph);
        double bestFml = score.getFml();
        double bestPValue = score.getPValue();
        Edge bestEdge = null;
        int n = -1;
        double ratio = 1.0;

        while (bestPValue < alpha && (++n) < 3) {
            for (int i = 0; i < graph.getNodes().size(); i++) {
                for (int j = 0; j < graph.getNodes().size(); j++) {
                    if (i == j) {
                        continue;
                    }

                    if (graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                        continue;
                    }

                    if (knowledge.edgeForbidden(nodes.get(i).getName(),
                            nodes.get(j).getName())) {
                        continue;
                    }

                    Edge edge = Edges.directedEdge(nodes.get(i), nodes.get(j));
                    graph.addEdge(edge);
                    final Score _score = scoreGraph(graph);
                    double newFml = _score.getFml();
                    double newPValue = _score.getPValue();

                    if (newFml < bestFml) {
                        System.out.println("Ratio = " + newFml / bestFml);
                        ratio = newFml / bestFml;

                        bestFml = newFml;
                        bestPValue = newPValue;
                        bestEdge = edge;
                    }

                    graph.removeEdge(edge);
                }
            }

            if (bestEdge == null) {
                return;
            }

//            if (ratio > 0.5) {
//                return;
//            }

            graph.addEdge(bestEdge);
            System.out.println("Adding edge " + bestEdge + " p = " + bestPValue);
        }
    }

    private void adjustOrientations(double alpha, Graph graph) {
        double pValue;

        for (Node node : graph.getNodes()) {
            System.out.println("Node " + node);

            List<Node> adj = graph.getAdjacentNodes(node);

            if (adj.size() < 2) {
                continue;
            }

            List<Edge> adjEdges = new LinkedList<Edge>();

            for (Node _node : adj) {
                adjEdges.add(graph.getEdge(node, _node));
            }

            int[] dims = new int[adj.size()];
            for (int i = 0; i < adj.size(); i++) dims[i] = 2;

            CombinationGenerator gen = new CombinationGenerator(dims);
            int[] comb;
            int[] bestComb = null;
            double bestScore = 0; //Double.POSITIVE_INFINITY;

            WHILE:
            while ((comb = gen.next()) != null) {
                for (Node _node : adj) {
                    graph.removeEdge(node, _node);
                }

                StringBuffer buf = new StringBuffer();

                for (int i = 0; i < comb.length; i++) {
                    Node _node = adj.get(i);

                    if (comb[i] == 0) {
                        if (knowledge.edgeForbidden(node.getName(), _node.getName())) {
                            continue WHILE;
                        }

                        graph.addDirectedEdge(node, _node);
                        buf.append(graph.getEdge(node, _node)).append(" ");
                    } else {
                        if (knowledge.edgeForbidden(_node.getName(), node.getName())) {
                            continue WHILE;
                        }

                        graph.addDirectedEdge(_node, node);
                        buf.append(graph.getEdge(node, _node)).append(" ");
                    }
                }

                Score score = scoreGraph(graph);
                pValue = score.getPValue();

                if (pValue > bestScore && pValue > alpha) {
                    bestComb = new int[comb.length];
                    System.arraycopy(comb, 0,
                            bestComb, 0, comb.length);
                    bestScore = pValue;
                }
            }

            if (bestComb == null) {
                for (Node _node : adj) {
                    graph.removeEdge(node, _node);
                }

                for (Edge edge : adjEdges) {
                    graph.addEdge(edge);
                }

                continue;
            }

            for (Node _node : adj) {
                graph.removeEdge(node, _node);
            }

            for (int i = 0; i < bestComb.length; i++) {
                Node _node = adj.get(i);

                if (bestComb[i] == 0) {
                    graph.addDirectedEdge(node, _node);
                    System.out.print(graph.getEdge(node, _node) + " ");
                } else {
                    graph.addDirectedEdge(_node, node);
                    System.out.print(graph.getEdge(node, _node) + " ");
                }
            }

            System.out.print(" chosen... " + scoreGraph(graph).getPValue() + "\t");

            System.out.println(graph);
        }
    }

    private void adjustOrientations2(double alpha, Graph graph) {
        double pValue = 0.0;

        List<Edge> adj = graph.getEdges();

        int[] dims = new int[adj.size()];
        for (int i = 0; i < adj.size(); i++) dims[i] = 2;

        CombinationGenerator gen = new CombinationGenerator(dims);
        int[] comb;
        int[] bestComb = null;
        double bestScore = 0; //Double.POSITIVE_INFINITY;

        WHILE:
        while ((comb = gen.next()) != null) {
            for (Edge edge : adj) {
                graph.removeEdge(edge.getNode1(), edge.getNode2());
            }

            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < comb.length; i++) {
                if (comb[i] == 0) {
                    graph.addDirectedEdge(adj.get(i).getNode1(), adj.get(i).getNode2());
                } else {
                    graph.addDirectedEdge(adj.get(i).getNode2(), adj.get(i).getNode1());
                }
            }

            if (pValue >= bestScore) {
                bestComb = new int[comb.length];
                System.arraycopy(comb, 0,
                        bestComb, 0, comb.length);
                bestScore = pValue;
            }
        }

        for (Edge edge : adj) {
            graph.removeEdge(edge.getNode1(), edge.getNode2());
        }

        for (int i = 0; i < bestComb.length; i++) {
            if (bestComb[i] == 0) {
                graph.addDirectedEdge(adj.get(i).getNode1(), adj.get(i).getNode2());
            } else {
                graph.addDirectedEdge(adj.get(i).getNode2(), adj.get(i).getNode1());
            }
        }

        System.out.print(" chosen... " + scoreGraph(graph).getPValue() + "\t");

        System.out.println(graph);
    }

    private void removeEdgesToLowerFml(Graph graph) {
        System.out.println("Removing edges to lower FML.");

        double fml = scoreGraph(graph).getFml();

        for (Edge edge : graph.getEdges()) {
            if (knowledge.edgeRequired(edge.getNode1().getName(), edge.getNode2().getName())) {
                continue;
            }

            graph.removeEdge(edge);
            Score score = scoreGraph(graph);
            double _fml = score.getFml();

            System.out.println(edge + ": " + score.getPValue());

            if (_fml < fml) {
                System.out.println("Removed: " + edge + " p = " + score.getPValue());
                fml = _fml;
            } else {
                graph.addEdge(edge);
            }
        }

        System.out.println(scoreGraph(graph).getPValue());
    }

    private boolean removeEdges(double alpha, Graph graph, boolean changed) {
        for (Edge edge : graph.getEdges()) {
            if (knowledge.edgeRequired(edge.getNode1().getName(), edge.getNode2().getName())) {
                continue;
            }

            graph.removeEdge(edge);
            Score score = scoreGraph(graph);

            System.out.println(edge + ": " + score.getPValue());

            if (score.getPValue() > alpha) {
                System.out.println("Removed: " + edge);
                changed = true;
            } else {
                graph.addEdge(edge);
            }

            System.out.println(scoreGraph(graph).getPValue());
        }
        return changed;
    }

    private Score scoreGraph(Graph graph) {
        SemPm semPm = new SemPm(graph);
        SemEstimator semEstimator = new SemEstimator(dataSet, semPm, new SemOptimizerEm());
        semEstimator.estimate();
        SemIm estimatedSem = semEstimator.getEstimatedSem();
//        TetradLogger.getInstance().log("info", "P = " + estimatedSem.getPValue());
//        TetradLogger.getInstance().flush();

//        return new Score(estimatedSem, -(estimatedSem.getChiSquare() / semPm.getDof()));
        return new Score(estimatedSem, estimatedSem.getPValue(), estimatedSem.getFml());
//        return new Score(estimatedSem, -estimatedSem.getFml());
    }

    private Score scoreGraph(Graph graph, List<Node> nodes, Graph trueDag) {
        Graph subGraph = graph.subgraph(nodes);

        subGraph.removeEdges(subGraph.getEdges());

        SemPm semPm = new SemPm(subGraph);
        SemEstimator semEstimator = new SemEstimator(dataSet, semPm, new SemOptimizerEm());
        semEstimator.estimate();
        SemIm estimatedSem = semEstimator.getEstimatedSem();
//        TetradLogger.getInstance().log("info", "P = " + estimatedSem.getPValue());
//        TetradLogger.getInstances().flush();

//        for (Edge edge : subGraph.getEdges()) {
//            System.out.println(edge + " coef = " + estimatedSem.getEdgeCoef(edge.getNode1(),  edge.getNode2()));
//        }

//        return new Score(estimatedSem, -(estimatedSem.getChiSquare() / semPm.getDof()));
        return new Score(estimatedSem, estimatedSem.getPValue(), estimatedSem.getFml());
//        return new Score(estimatedSem, -estimatedSem.getFml());
    }

    public double secondDerivative(SemIm semIm, Parameter p1, Parameter p2) {

        double delta = 0.005;
        FittingFunction fcn = new SemFittingFunction(semIm);

        List<Parameter> freeParameters = semIm.getFreeParameters();
        int i = freeParameters.indexOf(p1);
        int j = freeParameters.indexOf(p2);

        double[] params = semIm.getFreeParamValues();

        //The Hessian matrix of second order partial derivatives is called the
        //information matrix.
        return secondPartialDerivative(fcn, i, j, params, delta);
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public Graph getTrueDag() {
        return trueDag;
    }

    public void setTrueDag(Graph trueDag) {
        this.trueDag = trueDag;
    }

    private static class Score {
        private SemIm estimatedSem;
        private double pValue;
        private double fml;

        public Score(SemIm estimatedSem, double score, double fml) {
            this.estimatedSem = estimatedSem;
            this.pValue = score;
            this.fml = fml;
        }

        public SemIm getEstimatedSem() {
            return estimatedSem;
        }

        public double getPValue() {
            return pValue;
        }

        public double getFml() {
            return fml;
        }
    }

    /**
     * This method straightforwardly applies the standard definition of the
     * numerical estimates of the second order partial derivatives.  See for
     * example Section 5.7 of Numerical Recipes in C.
     */
    public double secondPartialDerivative(FittingFunction f, int i, int j,
                                          double[] p, double delt) {
        double[] arg = new double[p.length];
        System.arraycopy(p, 0, arg, 0, p.length);

        arg[i] += delt;
        arg[j] += delt;
        double ff1 = f.evaluate(arg);

        arg[j] -= 2 * delt;
        double ff2 = f.evaluate(arg);

        arg[i] -= 2 * delt;
        arg[j] += 2 * delt;
        double ff3 = f.evaluate(arg);

        arg[j] -= 2 * delt;
        double ff4 = f.evaluate(arg);

        double fsSum = ff1 - ff2 - ff3 + ff4;

        return fsSum / (4.0 * delt * delt);
    }

    /**
     * Evaluates a fitting function for an array of parameters.
     *
     * @author Joseph Ramsey
     */
    static interface FittingFunction {

        /**
         * Returns the value of the function for the given array of parameter
         * values.
         */
        double evaluate(double[] argument);

        /**
         * Returns the number of parameters.
         */
        int getNumParameters();
    }

    /**
     * Wraps a Sem for purposes of calculating its fitting function for given
     * parameter values.
     *
     * @author Joseph Ramsey
     */
    static class SemFittingFunction implements FittingFunction {

        /**
         * The wrapped Sem.
         */
        private final SemIm sem;

        /**
         * Constructs a new PalFittingFunction for the given Sem.
         */
        public SemFittingFunction(SemIm sem) {
            this.sem = sem;
        }

        /**
         * Computes the maximum likelihood function value for the given
         * parameters values as given by the optimizer. These values are mapped
         * to parameter values.
         */
        public double evaluate(double[] parameters) {
            sem.setFreeParamValues(parameters);

            // This needs to be FML-- see Bollen p. 109.
            return sem.getFml();
        }

        /**
         * Returns the number of arguments. Required by the MultivariateFunction
         * interface.
         */
        public int getNumParameters() {
            return this.sem.getNumFreeParams();
        }
    }
}