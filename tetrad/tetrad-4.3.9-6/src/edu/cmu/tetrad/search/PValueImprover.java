package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.List;

/**
 * Improves the P value of a SEM IM by adding, removing, or reversing single
 * edges.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class PValueImprover {
    private DataSet dataSet;
    private Knowledge knowledge;
    private SemIm semIm;

    public PValueImprover(SemIm semIm, DataSet data, Knowledge knowledge) {
        this.semIm = semIm;
        this.dataSet = data;
        this.knowledge = knowledge;
    }

    public SemIm search() {
        double alpha = 0.05;

//        PcSearch search = new PcSearch(new IndTestFisherZ(dataSet, 0.05), new Knowledge());
//        GesSearch search = new GesSearch(dataSet);
//        search.setKnowledge(knowledge);
//        Graph graph = search.search();

        Graph graph = semIm.getSemPm().getGraph();
        System.out.println(scoreGraph(graph).getScore());

        SearchGraphUtils.pdagToDag(graph);
        removeHighPCoefs(graph, alpha);

        boolean changed = true;
        double delta = +.01;
        double pCutoff = 0.99;
        double bestScore = scoreGraph(graph).score;
        List<Node> variables = graph.getNodes();

        // Remove Edges.
        while (true) {
            Edge _edge = null;
            double _pValue = Double.NaN;

            for (Node x : variables) {
                for (Node y : variables) {
                    if (x == y) continue;

                    if (x.equals(y)) {
                        System.out.println();
                    }

                    Edge edge = graph.getEdge(x, y);
                    if (edge == null) continue;

                    graph.removeEdge(edge);
                    boolean producesHighP = producesHighPCoefs(
                            "Removing " + edge, graph, alpha);
                    Score score = scoreGraph(graph);
                    graph.addEdge(edge);


                    if (score.getScore() > bestScore) {
                        if (score.getEstimatedSem().getPValue() > pCutoff) {
                            continue;
                        }

                        if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                            continue;
                        }

                        if (producesHighP) {
//                            TetradLogger.getInstance().log("info", "Skipped " + edge);
//                            TetradLogger.getInstance().flush();
                            continue;
                        }

                        _edge = edge;
                        bestScore = score.getScore();
                        _pValue = score.getEstimatedSem().getPValue();
                    }
                }
            }

            if (_edge != null) {
                graph.removeEdge(_edge);
                TetradLogger.getInstance().log("info", "P = " + _pValue);
                TetradLogger.getInstance().log("info", "Removed " + _edge +
                        " (" + bestScore + ")");
                TetradLogger.getInstance().flush();
//                    removeHighPCoefs(graph, alpha);
                changed = true;
                continue;
            }

            break;
        }

        while (changed) {
            changed = false;

            while (true) {
                Edge _edge = null;
                double _pValue = Double.NaN;

                // Add edges.
                for (Node x : variables) {
                    for (Node y : variables) {
                        if (x == y) continue;
                        if (graph.isAdjacentTo(x, y)) continue;

                        Edge edge = Edges.directedEdge(x, y);
                        graph.addEdge(edge);
                        boolean producesHighP = producesHighPCoefs(
                                "Adding " + edge, graph, alpha);
                        Score score = scoreGraph(graph);
                        graph.removeEdge(edge);

                        if (producesHighP) {
//                            TetradLogger.getInstance().log("info", "Skipped " + edge);
//                            TetradLogger.getInstance().flush();
                            continue;
                        }

                        if (score.getScore() > bestScore + delta) {
                            if (score.getEstimatedSem().getPValue() > pCutoff) {
                                continue;
                            }

                            if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                                continue;
                            }

                            _edge = edge;
                            bestScore = score.getScore();
                            _pValue = score.getEstimatedSem().getPValue();
                        }
                    }
                }

                if (_edge != null) {
                    if (graph.isAdjacentTo(_edge.getNode1(), _edge.getNode2())) {
                        throw new IllegalArgumentException();
                    }

                    graph.addEdge(_edge);
//                    TetradLogger.getInstance().log("info", "P = " + _pValue);
                    TetradLogger.getInstance().log("info", "Added " + _edge +
                            " (" + bestScore + ")");
                    TetradLogger.getInstance().flush();
//                    removeHighPCoefs(graph, alpha);
                    changed = true;
                    continue;
                }

                break;
            }

            // Reorient edges.
            for (Node x : variables) {
                for (Node y : variables) {
                    if (x == y) continue;

                    Edge edge = graph.getEdge(x, y);
                    if (edge == null) continue;
                    graph.removeEdge(edge);

                    Edge edge1 = Edges.directedEdge(x, y);
                    graph.addEdge(edge1);
                    Score score1 = scoreGraph(graph);
                    graph.removeEdge(edge1);

                    Edge edge2 = Edges.directedEdge(y, x);
                    graph.addEdge(edge2);
                    Score score2 = scoreGraph(graph);
                    graph.removeEdge(edge2);

                    if (score2.getScore() > score1.getScore() && score2.getScore() > bestScore + delta) {
                        if (score2.getEstimatedSem().getPValue() > pCutoff) {
                            graph.addEdge(edge);
                            continue;
                        }

                        if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                            continue;
                        }

                        if (producesHighPCoefs(
                                "Orienting " + edge2, graph, alpha)) {
//                            TetradLogger.getInstance().log("info", "Skipped " + edge);
//                            TetradLogger.getInstance().flush();
                            graph.addEdge(edge);
                            continue;
                        }

                        graph.addEdge(edge2);

                        if (!edge.equals(edge2)) {
                            TetradLogger.getInstance().log("info", "P = " +
                                    score2.getEstimatedSem().getPValue());
                            TetradLogger.getInstance().log("info", "Orienting " + edge +
                                    " to " + edge2 + " (" + score2.getScore() + ")");
                            TetradLogger.getInstance().flush();
                            bestScore = score2.getScore();
//                            removeHighPCoefs(graph, alpha);
                            changed = true;
                        }
                    } else
                    if (score1.getScore() > score2.getScore() && score1.getScore() > bestScore + delta) {
                        if (score1.getEstimatedSem().getPValue() > pCutoff) {
                            graph.addEdge(edge);
                            continue;
                        }

                        if (knowledge.edgeForbidden(y.toString(), x.toString())) {
                            continue;
                        }

                        if (producesHighPCoefs("Orienting " + edge1, graph, alpha)) {
//                            TetradLogger.getInstance().log("info", "Skipped " + edge);
//                            TetradLogger.getInstance().flush();
                            graph.addEdge(edge);
                            continue;
                        }

                        graph.addEdge(edge1);

                        if (!edge.equals(edge1)) {
                            TetradLogger.getInstance().log("info", "P = " +
                                    score1.getEstimatedSem().getPValue());
                            TetradLogger.getInstance().log("info", "Orienting " + edge +
                                    " to " + edge1 + " (" + score1.getScore() + ")");
                            TetradLogger.getInstance().flush();
                            bestScore = score1.getScore();
//                            removeHighPCoefs(graph, alpha);
                            changed = true;
                        }
                    } else {
//                        TetradLogger.getInstance().log("info", "Taking out " + edge
//                            + " because orienting it doesn't improve the score.");
                        graph.addEdge(edge);
                    }
                }
            }
        }

        removeHighPCoefs(graph, alpha);

        Score score = scoreGraph(graph);
        return score.getEstimatedSem();
    }

    public SemIm search2() {
        double alpha = 0.05;

//        PcSearch search = new PcSearch(new IndTestFisherZ(dataSet, 0.05), new Knowledge());
//        GesSearch search = new GesSearch(dataSet);
//        search.setKnowledge(knowledge);
//        Graph graph = search.search();

        Graph graph = semIm.getSemPm().getGraph();
        SearchGraphUtils.pdagToDag(graph);
        removeHighPCoefs(graph, alpha);

        boolean changed = true;
        double delta = +.01;
        double pCutoff = 0.99;
        double bestScore = scoreGraph(graph).score;
        List<Node> variables = graph.getNodes();

        while (changed) {
            changed = false;

            // Add edges.
            for (Node x : variables) {
                for (Node y : variables) {
                    if (x == y) continue;
                    if (graph.isAdjacentTo(x, y)) continue;

                    Edge edge = Edges.directedEdge(x, y);
                    graph.addEdge(edge);
                    boolean producesHighP = producesHighPCoefs(
                            "Adding " + edge, graph, alpha);
                    Score score = scoreGraph(graph);
                    graph.removeEdge(edge);

                    if (score.getScore() > bestScore + delta) {
                        if (score.getEstimatedSem().getPValue() > pCutoff) {
                            continue;
                        }

                        if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                            continue;
                        }

                        if (producesHighP) {
                            continue;
                        }

                        graph.addEdge(edge);
                        TetradLogger.getInstance().log("info", "Added " + edge +
                                " (" + bestScore + ")");
                        TetradLogger.getInstance().flush();
                        bestScore = score.getScore();
                        changed = true;
                    }
                }
            }

            // Remove Edges.
            for (Node x : variables) {
                for (Node y : variables) {
                    if (x == y) continue;

                    if (x.equals(y)) {
                        System.out.println();
                    }

                    Edge edge = graph.getEdge(x, y);
                    if (edge == null) continue;

                    graph.removeEdge(edge);
                    boolean producesHighP = producesHighPCoefs(
                            "Removing " + edge, graph, alpha);
                    Score score = scoreGraph(graph);
                    graph.addEdge(edge);


                    if (score.getScore() > bestScore + delta) {
                        if (score.getEstimatedSem().getPValue() > pCutoff) {
                            continue;
                        }

                        if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                            continue;
                        }

                        if (producesHighP) {
                            continue;
                        }

                        graph.removeEdge(edge);
                        TetradLogger.getInstance().log("info", "Removed " + edge +
                                " (" + bestScore + ")");
                        TetradLogger.getInstance().flush();
                        bestScore = score.getScore();
                        changed = true;
                    }
                }
            }

            // Reorient edges.
            for (Node x : variables) {
                for (Node y : variables) {
                    if (x == y) continue;

                    Edge edge = graph.getEdge(x, y);
                    if (edge == null) continue;
                    graph.removeEdge(edge);

                    Edge edge1 = Edges.directedEdge(x, y);
                    graph.addEdge(edge1);
                    Score score1 = scoreGraph(graph);
                    graph.removeEdge(edge1);

                    Edge edge2 = Edges.directedEdge(y, x);
                    graph.addEdge(edge2);
                    Score score2 = scoreGraph(graph);
                    graph.removeEdge(edge2);

                    if (score2.getScore() > score1.getScore() && score2.getScore() > bestScore + delta) {
                        if (score2.getEstimatedSem().getPValue() > pCutoff) {
                            graph.addEdge(edge);
                            continue;
                        }

                        if (knowledge.edgeForbidden(x.toString(), y.toString())) {
                            continue;
                        }

                        if (producesHighPCoefs("Orienting " + edge2, graph, alpha)) {
                            graph.addEdge(edge);
                            continue;
                        }

                        graph.addEdge(edge2);

                        if (!edge.equals(edge2)) {
                            TetradLogger.getInstance().log("info", "P = " +
                                    score2.getEstimatedSem().getPValue());
                            TetradLogger.getInstance().log("info", "Orienting " + edge +
                                    " to " + edge2 + " (" + score2.getScore() + ")");
                            TetradLogger.getInstance().flush();
                            bestScore = score2.getScore();
                            changed = true;
                        }
                    } else
                    if (score1.getScore() > score2.getScore() && score1.getScore() > bestScore + delta) {
                        if (score1.getEstimatedSem().getPValue() > pCutoff) {
                            graph.addEdge(edge);
                            continue;
                        }

                        if (knowledge.edgeForbidden(y.toString(), x.toString())) {
                            continue;
                        }

                        if (producesHighPCoefs("Orienting " + edge1, graph, alpha)) {
                            graph.addEdge(edge);
                            continue;
                        }

                        graph.addEdge(edge1);

                        if (!edge.equals(edge1)) {
                            TetradLogger.getInstance().log("info", "P = " +
                                    score1.getEstimatedSem().getPValue());
                            TetradLogger.getInstance().log("info", "Orienting " + edge +
                                    " to " + edge1 + " (" + score1.getScore() + ")");
                            TetradLogger.getInstance().flush();
                            bestScore = score1.getScore();
                            changed = true;
                        }
                    } else {
                        graph.addEdge(edge);
                    }
                }
            }
        }

        removeHighPCoefs(graph, alpha);

        Score score = scoreGraph(graph);
        return score.getEstimatedSem();
    }


    private void removeHighPCoefs(Graph graph, double alpha) {
        SemPm semPm = new SemPm(graph);
        SemEstimator estimator = new SemEstimator(dataSet, semPm);
        estimator.estimate();
        SemIm estSem = estimator.getEstimatedSem();

        List<Parameter> parameters = estSem.getSemPm().getParameters();

        for (Parameter parameter : parameters) {
            if (parameter.getType() != ParamType.COEF) {
                continue;
            }

            double p = estSem.getPValue(parameter, 300);

            if (p > alpha) {
                graph.removeEdge(parameter.getNodeA(), parameter.getNodeB());
                TetradLogger.getInstance().log("info", "Removing parameter " +
                        parameter + " because p = " + p + " > " + alpha);
                TetradLogger.getInstance().flush();
            }
        }
    }

    private boolean producesHighPCoefs(String whatProducesIt, Graph graph, double alpha) {
        SemPm semPm = new SemPm(graph);
        SemEstimator estimator = new SemEstimator(dataSet, semPm, new SemOptimizerRegression());
        estimator.estimate();
        SemIm estSem = estimator.getEstimatedSem();

        List<Parameter> parameters = estSem.getSemPm().getParameters();

        for (Parameter parameter : parameters) {
            if (parameter.getType() != ParamType.COEF) {
                continue;
            }

            double p = estSem.getPValue(parameter, 300);

            if (p > alpha) {
//                TetradLogger.getInstance().log("info", whatProducesIt + " caused " +
//                        parameter + " to have p = " + p + " > " + alpha);
//                TetradLogger.getInstance().flush();
                return true;
            }
        }

        return false;
    }

    public Graph search3() {
        TetradLogger.getInstance().log("info", "Starting SEM Score Search");

        Graph graph = new EdgeListGraph(dataSet.getVariables());

//        GesSearch search1 = new GesSearch(dataSet);
//        Graph graph = search1.search();
//        SearchGraphUtils.pdagToDag(graph);

        double bestScore = scoreGraph(graph).getScore();

        System.out.println("Initial score = " + bestScore);

        Edge _edge;

        while (true) {
            _edge = null;

            for (Node x : dataSet.getVariables()) {
                Y:
                for (Node y : dataSet.getVariables()) {
                    if (x == y) continue;
                    if (graph.isAdjacentTo(x, y)) continue;

                    Edge edge = Edges.directedEdge(x, y);
                    graph.addEdge(edge);
                    Score scoreResult = scoreGraph(graph);
                    SemIm scoredSem = scoreResult.getEstimatedSem();
                    double score = scoreResult.getScore();
                    int dof = scoredSem.getSemPm().getDof();
                    double chiSquare = scoredSem.getChiSquare();

                    System.out.println("Attempt add edge " + edge + " score = " + score);
//                    System.out.println("DOF = " + dof);
//                    System.out.println("Graph = " + graph);

                    List<Node> adjacents = graph.getAdjacentNodes(Edges.getDirectedEdgeHead(edge));
                    adjacents.remove(Edges.getDirectedEdgeTail(edge));

                    for (Node adjacent : adjacents) {
                        Edge other = graph.getEdge(adjacent, Edges.getDirectedEdgeHead(edge));
                        if (other.getProximalEndpoint(Edges.getDirectedEdgeHead(edge)) == Endpoint.ARROW
                                && edge.getProximalEndpoint(Edges.getDirectedEdgeHead(edge)) == Endpoint.ARROW) {
                            Parameter p1 = scoredSem.getSemPm().getCoefficientParameter(edge.getNode1(), edge.getNode2());
                            Parameter p2 = scoredSem.getSemPm().getCoefficientParameter(other.getNode1(), other.getNode2());
                            double secondDerivative = secondDerivative(scoredSem, p1, p2);

                            if (Math.abs(secondDerivative) < 1e-5) {
                                graph.removeEdge(edge);
                                continue Y;
                            }
                        }
                    }

                    if (score < bestScore && dof > 1 && chiSquare > 0.0 && chiSquare > dof / 2) {
                        bestScore = score;
                        _edge = edge;

                        System.out.println("Attempt add edge " + edge + " score = " + score);
//                        System.out.println("DOF = " + dof);
//                        System.out.println("Graph = " + graph);

//                        TetradLogger.getInstance().log("info", "Attempt add edge " + edge + " score = " + score);
//                        TetradLogger.getInstance().log("info", "Graph = " + graph);
//                        TetradLogger.getInstance().flush();
                    }

                    graph.removeEdge(edge);
                }
            }

            if (_edge == null) {
                break;
            }

            graph.addEdge(_edge);
//            rebuildPattern(graph);
//            System.out.println("*** Added " + _edge + " score = " + scoreGraph(graph) + " graph = " + graph);
//            System.out.println();

//            TetradLogger.getInstance().log("info", "Added " + _edge + " FML = " + scoreGraph(graph));
//            TetradLogger.getInstance().flush();

//            _edge = null;
//
//            for (Edge edge : graph.getEdges()) {
//                graph.removeEdge(edge);
//                Score scoreResult = scoreGraph(graph);
//                SemIm scoredSem = scoreResult.getEstimatedSem();
//                double score = scoreResult.getScore();
//                int dof = scoredSem.getSemPm().getDof();
//                double chiSquare  = scoredSem.getChiSquare();
//
//                System.out.println("Attempt add edge " + edge + " score = " + score);
//                System.out.println("DOF = " + dof);
//                System.out.println("Graph = " + graph);
//
//                if (score < bestScore && dof > 1 && chiSquare > 0.0 && chiSquare > dof / 2) {
//                    bestScore = score;
//                    _edge = edge;
//
////                        TetradLogger.getInstance().log("info", "Attempt add edge " + edge + " score = " + score);
////                        TetradLogger.getInstance().log("info", "Graph = " + graph);
////                        TetradLogger.getInstance().flush();
//                }
//
//                graph.addEdge(edge);
//            }
//
//            if (_edge == null) {
//                continue;
//            }
//
//            graph.removeEdge(_edge);
//
//            System.out.println("Removed " + _edge + " FML = " + scoreGraph(graph));
//            TetradLogger.getInstance().log("info", "Added " + _edge + " FML = " + scoreGraph(graph));
//            TetradLogger.getInstance().flush();
        }

        return graph;
    }

    private Score scoreGraph(Graph graph) {
        SemPm semPm = new SemPm(graph);
        SemEstimator semEstimator = new SemEstimator(dataSet, semPm, new SemOptimizerRegression());
        semEstimator.estimate();
        SemIm estimatedSem = semEstimator.getEstimatedSem();
//        TetradLogger.getInstance().log("info", "P = " + estimatedSem.getPValue());
//        TetradLogger.getInstance().flush();

//        return new Score(estimatedSem, -(estimatedSem.getChiSquare() / semPm.getDof()));
        return new Score(estimatedSem, estimatedSem.getPValue());
//        return new Score(estimatedSem, -estimatedSem.getFml());
    }

    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        pdagWithBk(graph);
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to
     * adapt Chickering's suggested algorithm above (dagToPdag) to incorporate
     * background knowledge, so I am also implementing this algorithm based on
     * Meek's 1995 UAI paper. Notice it is the same implemented in PcSearch.
     * </p> *IMPORTANT!* *It assumes all colliders are oriented, as well as
     * arrows dictated by time order.*
     */
    private void pdagWithBk(Graph graph) {
        MeekRules rules = new MeekRules();
        rules.orientImplied(graph);
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

    private static class Score {
        private SemIm estimatedSem;
        private double score;

        public Score(SemIm estimatedSem, double score) {
            this.estimatedSem = estimatedSem;
            this.score = score;
        }

        public SemIm getEstimatedSem() {
            return estimatedSem;
        }

        public double getScore() {
            return score;
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

        double partial = fsSum / (4.0 * delt * delt);

//        if (Double.isNaN(partial) || partial < -0.5) {
//            double eraseme = 1.0;
//        }

        return partial;
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
