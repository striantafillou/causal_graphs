///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemOptimizerRegression;
import edu.cmu.tetrad.sem.SemIm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.*;


/**
 * GesSearch is an implentation of the GES algorithm, as specified in Chickering
 * (2002) "Optimal structure identification with greedy search" Journal of
 * Machine Learning Research. It works for both BayesNets and SEMs.
 * <p/>
 * Some code optimization could be done for the scoring part of the graph for
 * discrete models (method scoreGraphChange). Some of Andrew Moore's approaches
 * for caching sufficient statistics, for instance.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class Ges2 implements GraphSearch {
    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private DataSet dataSet;

    /**
     * The correlation matrix for the data set.
     */
    private DoubleMatrix2D cov;

    /**
     * Sample size, either from the data set or from the variances.
     */
    private int sampleSize;

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * For discrete data scoring, the structure prior.
     */
    private double structurePrior;

    /**
     * For discrete data scoring, the sample prior.
     */
    private double samplePrior;

    /**
     * Map from variables to their column indices in the data set.
     */
    private HashMap<Node, Integer> hashIndices;

    /**
     * Array of variable names from the data set, in order.
     */
    private String varNames[];

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

    /**
     * True iff the data set is discrete.
     */
    private boolean discrete;

    /**
     * The true graph, if known. If this is provided, asterisks will be printed
     * out next to false positive added edges (that is, edges added that aren't
     * adjacencies in the true graph).
     */
    private Graph trueGraph;

    /**
     * For formatting printed numbers.
     */
    private final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * For linear algebra.
     */
    private final Algebra algebra = new Algebra();

    /**
     * Caches scores for discrete search.
     */
    private final LocalScoreCache localScoreCache = new LocalScoreCache();

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;


    /**
     * True if cycles are to be aggressively prevented. May be expensive
     * for large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    private transient List<PropertyChangeListener> listeners;
    private double penaltyDiscount = 1.0;
    private int maxNumEdges = -1;
    private double modelBic;

    //===========================CONSTRUCTORS=============================//

    public Ges2(DataSet dataSet) {
        setDataSet(dataSet);
        initialize(10., 0.001);
    }

    public Ges2(CovarianceMatrix covMatrix) {
        setCorrMatrix(new CorrelationMatrix(covMatrix));
        initialize(10., 0.001);
    }

    //==========================PUBLIC METHODS==========================//


    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    /**
     * Greedy equivalence search: Start from the empty graph, add edges till
     * model is significant. Then start deleting edges till a minimum is
     * achieved.
     *
     * @return the resulting Pattern.
     */
    public Graph search() {
        long startTime = System.currentTimeMillis();

        // Check for missing values.
        if (cov != null && DataUtils.containsMissingValue(cov)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        // Check for missing values.
        if (dataSet != null && DataUtils.containsMissingValue(dataSet)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }


        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));
        fireGraphChange(graph);
        buildIndexing(graph);
        addRequiredEdges(graph);

        // Method 1-- original.
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;
        this.modelBic = score;

        TetradLogger.getInstance().info("Elapsed time = " + (elapsedTime) / 1000. + " s");
        TetradLogger.getInstance().flush();
        return graph;

        // Method 2-- Ricardo's tweak.
//        double score = scoreGraph(graph), newScore;
//        int iter = 0;
//        do {
//            newScore = fes(graph, score);
//            if (newScore > score) {
//                score = newScore;
//                newScore = bes(graph, score);
//                if (score >= newScore) {
//                    break;
//                }
//                else {
//                    score = newScore;
//                }
//            }
//            else {
//                break;
//            }
//            //System.out.println("Current score = " + score);
//            iter++;
//        } while (iter < 100);
//        return graph;
    }

    public Graph search(List<Node> nodes) {
        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!dataSet().getVariables().containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in " + "the supplied data set.");
        }

        Graph graph = new EdgeListGraph(nodes);
        buildIndexing(graph);
        addRequiredEdges(graph);
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        TetradLogger.getInstance().info("Elapsed time = " + (elapsedTime) / 1000. + " s");
        TetradLogger.getInstance().flush();
        return graph;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     *
     * @param knowledge the knowledge object, specifying forbidden and required
     *                  edges.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    //===========================PRIVATE METHODS========================//

    private void initialize(double samplePrior, double structurePrior) {
        setStructurePrior(structurePrior);
        setSamplePrior(samplePrior);
    }

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence
     *              search.
     * @param score The score in the state prior to the forward equivalence
     *              search
     * @return the score in the state after the forward equivelance search.
     *         Note that the graph is changed as a side-effect to its state after
     *         the forward equivelance search.
     */
    private double fes(Graph graph, double score) {
        TetradLogger.getInstance().info("** FORWARD EQUIVALENCE SEARCH");
        double bestScore = score;
        TetradLogger.getInstance().info("Initial Score = " + nf.format(bestScore));

        Node x, y;
        Set<Node> t = new HashSet<Node>();

        do {
            x = y = null;
            List<Node> nodes = graph.getNodes();

//            TEMP:
            for (int i = 0; i < nodes.size(); i++) {
                Node _x = nodes.get(i);

                for (Node _y : nodes) {
                    if (_x == _y) {
                        continue;
                    }

                    if (graph.isAdjacentTo(_x, _y)) {
                        continue;
                    }

                    if (getKnowledge().edgeForbidden(_x.getName(),
                            _y.getName())) {
                        continue;
                    }

                    List<Node> tNeighbors = getTNeighbors(_x, _y, graph);
                    List<Set<Node>> tSubsets = powerSet(tNeighbors);

                    for (Set<Node> tSubset : tSubsets) {

                        if (!validSetByKnowledge(_x, _y, tSubset, true)) {
                            continue;
                        }

                        double insertEval = insertEval(_x, _y, tSubset, graph);
                        double evalScore = score + insertEval;

                        TetradLogger.getInstance().details("Attempt adding " + _x + "-->" + _y +
                                " " + tSubset + " (" + evalScore + ")");

//                        System.out.println("Attempt adding " + _x + "-->" + _y +
//                                " " + tSubset + " (" + evalScore + ")");

                        if (!(evalScore > bestScore && evalScore > score)) {
                            continue;
                        }

//                        if (!isDiscrete() && !(evalScore > bestScore + 50)) {
//                            continue;
//                        }

//                        double minJump = sampleSize * Math.log(1.12) - Math.log(sampleSize);
//
//                        System.out.println(minJump);
//
//                        if (!isDiscrete() &&
//                                !(evalScore > bestScore + minJump)) {
//                            continue;
//                        }

                        if (!validInsert(_x, _y, tSubset, graph)) {
                            continue;
                        }

                        bestScore = evalScore;
                        x = _x;
                        y = _y;
                        t = tSubset;

//                        break TEMP;

//                        System.out.println("Best score = " + bestScore);
                    }
                }
            }

            if (x != null) {
                insert(x, y, t, graph, bestScore);
                rebuildPattern(graph);
                score = bestScore;

                if (getMaxNumEdges() != -1 && graph.getNumEdges() > getMaxNumEdges()) {
                    break;
                }
            }
        } while (x != null);
        return score;
    }

    /**
     * Backward equivalence search.
     */
    private double bes(Graph graph, double initialScore) {
        TetradLogger.getInstance().info("** BACKWARD ELIMINATION SEARCH");
        TetradLogger.getInstance().info("Initial Score = " + nf.format(initialScore));
        double score = initialScore;
        double bestScore = score;
        Node x, y;
        Set<Node> t = new HashSet<Node>();
        do {
            x = y = null;
            List<Edge> edges1 = graph.getEdges();
            List<Edge> edges = new ArrayList<Edge>();

            for (Edge edge : edges1) {
                Node _x = edge.getNode1();
                Node _y = edge.getNode2();

                if (Edges.isUndirectedEdge(edge)) {
                    edges.add(Edges.directedEdge(_x, _y));
                    edges.add(Edges.directedEdge(_y, _x));
                } else {
                    edges.add(edge);
                }
            }

            for (Edge edge : edges) {
                Node _x = Edges.getDirectedEdgeTail(edge);
                Node _y = Edges.getDirectedEdgeHead(edge);

                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                    continue;
                }

                List<Node> hNeighbors = getHNeighbors(_x, _y, graph);
                List<Set<Node>> hSubsets = powerSet(hNeighbors);

                for (Set<Node> hSubset : hSubsets) {
                    if (!validSetByKnowledge(_x, _y, hSubset, false)) {
                        continue;
                    }

                    double deleteEval = deleteEval(_x, _y, hSubset, graph);
                    double evalScore = score + deleteEval;

                    TetradLogger.getInstance().details("Attempt removing " + _x + "-->" + _y + "(" +
                            evalScore + ")");

                    if (!(evalScore > bestScore)) {
                        continue;
                    }

                    if (!validDelete(_x, _y, hSubset, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = hSubset;
                }


                if (Edges.isUndirectedEdge(edge)) {
                    _x = edge.getNode1();
                    _y = edge.getNode2();
                } else {
                    _x = Edges.getDirectedEdgeTail(edge);
                    _y = Edges.getDirectedEdgeHead(edge);
                }

                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                    continue;
                }

                hNeighbors = getHNeighbors(_x, _y, graph);
                hSubsets = powerSet(hNeighbors);

                for (Set<Node> hSubset1 : hSubsets) {
                    if (!validSetByKnowledge(_x, _y, hSubset1, false)) {
                        continue;
                    }

                    double deleteEval = deleteEval(_x, _y, hSubset1, graph);
                    double evalScore = score + deleteEval;

//                        print("Attempt removing " + _x + "-->" + _y + "(" + evalScore + ")");

                    if (!(evalScore > bestScore)) {
                        continue;
                    }

                    if (!validDelete(_x, _y, hSubset1, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = hSubset1;
                }
            }
            if (x != null) {
                TetradLogger.getInstance().log("deleteEdge", "DELETE " + graph.getEdge(x, y) + t.toString() + " (" +
                        nf.format(bestScore) + ")");
                delete(x, y, t, graph);
                rebuildPattern(graph);
                score = bestScore;
            }
        } while (x != null);

        return score;
    }

    /**
     * Get all nodes that are connected to Y by an undirected edge and not
     * adjacent to X.
     */
    private static List<Node> getTNeighbors(Node x, Node y, Graph graph) {
        List<Node> tNeighbors = new LinkedList<Node>(graph.getAdjacentNodes(y));
        tNeighbors.removeAll(graph.getAdjacentNodes(x));

        for (int i = tNeighbors.size() - 1; i >= 0; i--) {
            Node z = tNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                tNeighbors.remove(z);
            }
        }

        return tNeighbors;
    }

    /**
     * Get all nodes that are connected to Y by an undirected edge and adjacent
     * to X
     */
    private static List<Node> getHNeighbors(Node x, Node y, Graph graph) {
        List<Node> hNeighbors = new LinkedList<Node>(graph.getAdjacentNodes(y));
        hNeighbors.retainAll(graph.getAdjacentNodes(x));

        for (int i = hNeighbors.size() - 1; i >= 0; i--) {
            Node z = hNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);
            if (!Edges.isUndirectedEdge(edge)) {
                hNeighbors.remove(z);
            }
        }

        return hNeighbors;
    }

    /**
     * Evaluate the Insert(X, Y, T) operator (Definition 12 from Chickering,
     * 2002).
     */
    private double insertEval(Node x, Node y, Set<Node> t, Graph graph) {
        Set<Node> set1 = new HashSet<Node>(findNaYX(x, y, graph));
        set1.addAll(t);
        set1.addAll(graph.getParents(y));
        Set<Node> set2 = new HashSet<Node>(set1);
        set1.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    /**
     * Evaluate the Delete(X, Y, T) operator (Definition 12 from Chickering,
     * 2002).
     */
    private double deleteEval(Node x, Node y, Set<Node> h, Graph graph) {
        Set<Node> set1 = new HashSet<Node>(findNaYX(x, y, graph));
        set1.removeAll(h);
        set1.addAll(graph.getParents(y));
        Set<Node> set2 = new HashSet<Node>(set1);
        set1.remove(x);
        set2.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    /*
    * Do an actual insertion
    * (Definition 12 from Chickering, 2002).
    **/
    private void insert(Node x, Node y, Set<Node> subset, Graph graph,
                        double score) {
        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        graph.addDirectedEdge(x, y);
        String label = trueGraph != null && trueEdge == null ? "*" : "";
        TetradLogger.getInstance().log("insertEdge", graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) + " (" +
                nf.format(score) + ") " + label);

        for (Node node : subset) {
            Edge oldEdge = graph.getEdge(node, y);

            graph.removeEdge(node, y);
            graph.addDirectedEdge(node, y);

            TetradLogger.getInstance().log("directEdge", "--- Directing " + oldEdge + " to " +
                    graph.getEdge(node, y));
        }
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private static void delete(Node x, Node y, Set<Node> subset, Graph graph) {
        graph.removeEdges(x, y);

        for (Node aSubset : subset) {
            if (!graph.isParentOf(aSubset, x) && !graph.isParentOf(x, aSubset)) {
                graph.removeEdge(x, aSubset);
                graph.addDirectedEdge(x, aSubset);
            }
            graph.removeEdge(y, aSubset);
            graph.addDirectedEdge(y, aSubset);
        }
    }

    /*
     * Test if the candidate insertion is a valid operation
     * (Theorem 15 from Chickering, 2002).
     **/
    private boolean validInsert(Node x, Node y, Set<Node> subset, Graph graph) {
        List<Node> naYXT = new LinkedList<Node>(subset);
        naYXT.addAll(findNaYX(x, y, graph));

        if (!isClique(naYXT, graph)) {
            return false;
        }

        return isSemiDirectedBlocked(x, y, naYXT, graph, new HashSet<Node>());
    }

    /**
     * Test if the candidate deletion is a valid operation (Theorem 17 from
     * Chickering, 2002).
     */
    private static boolean validDelete(Node x, Node y, Set<Node> h,
                                       Graph graph) {
        List<Node> naYXH = findNaYX(x, y, graph);
        naYXH.removeAll(h);
        return isClique(naYXH, graph);
    }

    //---Background knowledge methods.

    private void addRequiredEdges(Graph graph) {
        for (Iterator<KnowledgeEdge> it =
                this.getKnowledge().requiredEdgesIterator(); it.hasNext();) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (!graph.isAncestorOf(nodeB, nodeA)) {
                graph.removeEdges(nodeA, nodeB);
                graph.addDirectedEdge(nodeA, nodeB);
            }
        }
        for (Iterator<KnowledgeEdge> it =
                getKnowledge().forbiddenEdgesIterator(); it.hasNext();) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (nodeA != null && nodeB != null && graph.isAdjacentTo(nodeA, nodeB) &&
                    !graph.isChildOf(nodeA, nodeB)) {
//                System.out.println(graph);
                if (!graph.isAncestorOf(nodeA, nodeB)) {
                    graph.removeEdges(nodeA, nodeB);
                    graph.addDirectedEdge(nodeB, nodeA);
                }
            }
        }
    }

    /**
     * Use background knowledge to decide if an insert or delete operation does
     * not orient edges in a forbidden direction according to prior knowledge.
     * If some orientation is forbidden in the subset, the whole subset is
     * forbidden.
     */
    private boolean validSetByKnowledge(Node x, Node y, Set<Node> subset,
                                        boolean insertMode) {
        if (insertMode) {
            for (Node aSubset : subset) {
                if (getKnowledge().edgeForbidden(aSubset.getName(),
                        y.getName())) {
                    return false;
                }
            }
        } else {
            for (Node nextElement : subset) {
                if (getKnowledge().edgeForbidden(x.getName(),
                        nextElement.getName())) {
                    return false;
                }
                if (getKnowledge().edgeForbidden(y.getName(),
                        nextElement.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    //--Auxiliary methods.

    /**
     * Find all nodes that are connected to Y by an undirected edge that are
     * adjacent to X (that is, by undirected or directed edge) NOTE: very
     * inefficient implementation, since the current library does not allow
     * access to the adjacency list/matrix of the graph.
     */
    private static List<Node> findNaYX(Node x, Node y, Graph graph) {
        List<Node> naYX = new LinkedList<Node>(graph.getAdjacentNodes(y));
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (int i = 0; i < naYX.size(); i++) {
            Node z = naYX.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return naYX;
    }

    /**
     * Returns true iif the given set forms a clique in the given graph.
     */
    private static boolean isClique(List<Node> set, Graph graph) {
        List<Node> setv = new LinkedList<Node>(set);
        for (int i = 0; i < setv.size() - 1; i++) {
            for (int j = i + 1; j < setv.size(); j++) {
                if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies if every semidirected path from y to x contains a node in naYXT.
     */
    private boolean isSemiDirectedBlocked(Node x, Node y, List<Node> naYXT,
                                          Graph graph, Set<Node> marked) {
        if (naYXT.contains(y)) {
            return true;
        }

        if (y == x) {
            return false;
        }

        for (Node node1 : graph.getNodes()) {
            if (node1 == y || marked.contains(node1)) {
                continue;
            }

            if (graph.isAdjacentTo(y, node1) && !graph.isParentOf(node1, y)) {
                marked.add(node1);

                if (!isSemiDirectedBlocked(x, node1, naYXT, graph, marked)) {
                    return false;
                }

                marked.remove(node1);
            }
        }

        return true;
    }

    private static List<Set<Node>> powerSet(List<Node> nodes) {
        List<Set<Node>> subsets = new ArrayList<Set<Node>>();
        int total = (int) Math.pow(2, nodes.size());
        for (int i = 0; i < total; i++) {
            Set<Node> newSet = new HashSet<Node>();
            String selection = Integer.toBinaryString(i);
            for (int j = selection.length() - 1; j >= 0; j--) {
                if (selection.charAt(j) == '1') {
                    newSet.add(nodes.get(selection.length() - j - 1));
                }
            }
            subsets.add(newSet);
        }
        return subsets;
    }


    /**
     * Completes a pattern that was modified by an insertion/deletion operator
     * Based on the algorithm described on Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        addRequiredEdges(graph);
        pdagWithBk(graph, getKnowledge());
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to
     * adapt Chickering's suggested algorithm above (dagToPdag) to incorporate
     * background knowledge, so I am also implementing this algorithm based on
     * Meek's 1995 UAI paper. Notice it is the same implemented in PcSearch.
     * </p> *IMPORTANT!* *It assumes all colliders are oriented, as well as
     * arrows dictated by time order.*
     */
    private void pdagWithBk(Graph graph, Knowledge knowledge) {
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);
    }

    private void setDataSet(DataSet dataSet) {
        List<String> _varNames = dataSet.getVariableNames();

        // Fix an order for repeatability.
//        Collections.sort(_varNames);

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = dataSet.getVariables();
        this.dataSet = dataSet;
        this.discrete = dataSet.isDiscrete();

        if (!isDiscrete()) {
//            this.cov = dataSet.getCorrelationMatrix();
            this.cov = dataSet.getCovarianceMatrix();
        }

        this.sampleSize = dataSet.getNumRows();
    }

    private void setCorrMatrix(CorrelationMatrix corrMatrix) {
        this.cov = corrMatrix.getMatrix();
        List<String> _varNames = corrMatrix.getVariableNames();

        // Fix an order for repeatability.
//        Collections.sort(_varNames);

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = corrMatrix.getVariables();
        this.sampleSize = corrMatrix.getSampleSize();
    }

    private void buildIndexing(Graph graph) {
        this.hashIndices = new HashMap<Node, Integer>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < this.varNames.length; i++) {
                if (this.varNames[i].equals(next.getName())) {
                    this.hashIndices.put(next, i);
                    break;
                }
            }
        }
    }

    private static int getRowIndex(int dim[], int[] values) {
        int rowIndex = 0;
        for (int i = 0; i < dim.length; i++) {
            rowIndex *= dim[i];
            rowIndex += values[i];
        }
        return rowIndex;
    }

//    private static void print(String message) {
//        LogUtils.getInstance().fine(message);
//    }

    //===========================SCORING METHODS===========================//

    public double scoreGraph(Graph graph) {
        Graph dag = SearchGraphUtils.dagFromPattern(graph);
//        Graph dag = new EdgeListGraph(graph);
//        SearchGraphUtils.pdagToDag(dag);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Collection<Node> parents = dag.getParents(next);
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(next.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int parentIndices[] = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < getVariables().size(); i++) {
                    if (this.varNames[i].equals(nextParent.getName())) {
                        parentIndices[count++] = i;
                        break;
                    }
                }
            }

            if (this.isDiscrete()) {
                score += localBdeuScore(nextIndex, parentIndices);
            } else {
                score += localSemScore(nextIndex, parentIndices);
            }
        }
        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,
                                    Set<Node> parents2) {
        int yIndex = hashIndices.get(y);
        int parentIndices1[] = new int[parents1.size()];

        int count = 0;
        for (Node aParents1 : parents1) {
            parentIndices1[count++] = (hashIndices.get(aParents1));
        }

        int parentIndices2[] = new int[parents2.size()];

        int count2 = 0;
        for (Node aParents2 : parents2) {
            parentIndices2[count2++] = (hashIndices.get(aParents2));
        }

        if (this.isDiscrete()) {
            double score1 = localBdeuScore(yIndex, parentIndices1);
            double score2 = localBdeuScore(yIndex, parentIndices2);
//            double score1 = localDiscreteBicScore(yIndex, parentIndices1);
//            double score2 = localDiscreteBicScore(yIndex, parentIndices2);
            return score1 - score2;
        } else {
            double score1 = localSemScore(yIndex, parentIndices1);
            double score2 = localSemScore(yIndex, parentIndices2);

//            System.out.println(score1 - score2);

            return score1 - score2;
        }
    }

    /**
     * Compute the local BDeu score of (i, parents(i)). See (Chickering, 2002).
     */
    private double localBdeuScore(int i, int parents[]) {
        double oldScore = localScoreCache.get(i, parents);

        if (!Double.isNaN(oldScore)) {
            return oldScore;
        }

        // Number of categories for i.
        int r = numCategories(i);

//        if (r < 2) {
//            String variable = dataSet != null ? dataSet.getVariable(i).getName()
//                    : "?";
//
//            throw new IllegalArgumentException(
//                    "Number of categories for " + variable +
//                            " must be at least 2.");
//        }

        // Numbers of categories of parents.
        int dims[] = new int[parents.length];

        for (int p = 0; p < parents.length; p++) {
            dims[p] = numCategories(parents[p]);
        }

        // Number of parent states.
        int q = 1;
        for (int p = 0; p < parents.length; p++) {
            q *= dims[p];
        }

        // Conditional cell counts of data for i given parents(i).
        int n_ijk[][] = new int[q][r];
        int n_ij[] = new int[q];

        int values[] = new int[parents.length];

        for (int n = 0; n < sampleSize(); n++) {
            for (int p = 0; p < parents.length; p++) {
                int parentValue = dataSet().getInt(n, parents[p]);

                if (parentValue == -99) {
                    throw new IllegalStateException("Please remove or impute " +
                            "missing values.");
                }

                values[p] = parentValue;
            }

            int childValue = dataSet().getInt(n, i);

            if (childValue == -99) {
                throw new IllegalStateException("Please remove or impute missing " +
                        "values (record " + n + " column " + i + ")");

            }

            n_ijk[getRowIndex(dims, values)][childValue]++;
        }

        // Row sums.
        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                n_ij[j] += n_ijk[j][k];
            }
        }

        //Finally, compute the score
        double score = (r - 1) * q * Math.log(getStructurePrior());

        for (int j = 0; j < q; j++) {
            for (int k = 0; k < r; k++) {
                score += ProbUtils.lngamma(
                        getSamplePrior() / (r * q) + n_ijk[j][k]);
            }

            score -= ProbUtils.lngamma(getSamplePrior() / q + n_ij[j]);
        }

        score += q * ProbUtils.lngamma(getSamplePrior() / q);
//        score -= (r * q) * ProbUtils.lngamma(getSamplePrior() / (r * q));
        score -= r * ProbUtils.lngamma(getSamplePrior() / (r * q));

        localScoreCache.add(i, parents, score);

        return score;
    }                                             

//    private double localDiscreteBicScore(int i, int[] parents) {
//
//        // Number of categories for i.
//        int r = numCategories(i);
//
//        // Numbers of categories of parents.
//        int dims[] = new int[parents.length];
//
//        for (int p = 0; p < parents.length; p++) {
//            dims[p] = numCategories(parents[p]);
//        }
//
//        // Number of parent states.
//        int q = 1;
//        for (int p = 0; p < parents.length; p++) {
//            q *= dims[p];
//        }
//
//        // Conditional cell counts of data for i given parents(i).
//        double cell[][] = new double[q][r];
//
//        int values[] = new int[parents.length];
//
//        for (int n = 0; n < sampleSize(); n++) {
//            for (int p = 0; p < parents.length; p++) {
//                int value = dataSet().getInt(n, parents[p]);
//
//                if (value == -99) {
//                    throw new IllegalStateException("Complete data expected.");
//                }
//
//                values[p] = value;
//            }
//
//            int value = dataSet().getInt(n, i);
//
//            if (value == -99) {
//                throw new IllegalStateException("Complete data expected.");
//
//            }
//
//            cell[getRowIndex(dims, values)][value]++;
//        }
//
//        // Calculate row sums.
//        double rowSum[] = new double[q];
//
//        for (int j = 0; j < q; j++) {
//            for (int k = 0; k < r; k++) {
//                rowSum[j] += cell[j][k];
//            }
//        }
//
//        // Calculate log prob data given structure.
//        double score = 0.0;
//
//        for (int j = 0; j < q; j++) {
//            if (rowSum[j] == 0) {
//                continue;
//            }
//
//            for (int k = 0; k < r; k++) {
//                double count = cell[j][k];
//                double prob = count / rowSum[j];
//                score += count * Math.log(prob);
//            }
//        }
//
//        // Subtract penalty.
//        double numParams = q * (r - 1);
//        return score - numParams / 2. * Math.log(sampleSize());
//    }


    private int numCategories(int i) {
        return ((DiscreteVariable) dataSet().getVariable(i)).getNumCategories();
    }

    private double localSemScore(int i, int[] parents) {
        return localSemScore1(i, parents);
    }

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in
     * a simple SEM model.
     */
    private double localSemScore1(int i, int[] parents) {

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        double variance = getCorrMatrix().get(i, i);
        int n = sampleSize();
        double d = parents.length + 1;

        // Regress z onto i, yielding regression coefficients b.
        DoubleMatrix2D Czz =
                getCorrMatrix().viewSelection(parents, parents);
        DoubleMatrix2D inverse;
        try {
            inverse = algebra().inverse(Czz);
        }
        catch (Exception e) {
            StringBuffer buf = new StringBuffer();
            buf.append("Could not invert matrix for variables: ");

            for (int j = 0; j < parents.length; j++) {
                buf.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    buf.append(", ");
                }
            }

            throw new IllegalArgumentException(buf.toString());
        }

        DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
        Cyz = Cyz.viewSelection(parents);
        DoubleMatrix1D b = algebra().mult(inverse, Cyz);

        variance -= algebra().mult(Cyz, b);

        double penalty = getPenaltyDiscount();

//        return -(n / 2.) - (n / 2.) * Math.log(variance) - 2 * penalty * (d / 2.) * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n / (2 * Math.PI) - getPenaltyMutiplier() * d * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n * Math.log(2 * Math.PI) - 0.5 * n - getPenaltyMutiplier() * d * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n * Math.log(2 * Math.PI) - 0.5 * n - getPenaltyMutiplier() * d * Math.log(n);
        return - n * Math.log(variance) - d * Math.log(n);
    }

    private double localSemScore2(int i, int[] parents) {
        int[] allVars = new int[parents.length + 1];
        allVars[0] = i;
        System.arraycopy(parents, 0, allVars, 1, parents.length);
        DoubleMatrix2D c = getCorrMatrix().viewSelection(allVars, allVars);

        Graph graph = new EdgeListGraph();
        for (int m = 0; m < allVars.length; m++) graph.addNode(variables.get(allVars[m]));
        for (int m = 1; m < allVars.length; m++)
            graph.addDirectedEdge(variables.get(allVars[m]), variables.get(allVars[0]));
        SemPm pm = new SemPm(graph);
        CovarianceMatrix cov = new CovarianceMatrix(graph.getNodes(), c, sampleSize);
        SemEstimator estimator = new SemEstimator(cov, pm, new SemOptimizerRegression());
        estimator.estimate();
        SemIm estIm = estimator.getEstimatedSem();
        return estIm.getBicScore();
    }

    private double localSemScore3(int i, int[] parents) {

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        int n = sampleSize();
        double d = parents.length + 1;

        // Regress z onto i, yielding regression coefficients b.
        DoubleMatrix2D Czz =
                getCorrMatrix().viewSelection(parents, parents);
        DoubleMatrix2D inverse;
        try {
            inverse = algebra().inverse(Czz);
        }
        catch (Exception e) {
            StringBuffer buf = new StringBuffer();
            buf.append("Could not invert matrix for variables: ");

            for (int j = 0; j < parents.length; j++) {
                buf.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    buf.append(", ");
                }
            }

            throw new IllegalArgumentException(buf.toString());
        }

        DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
        Cyz = Cyz.viewSelection(parents);
        DoubleMatrix1D b = algebra().mult(inverse, Cyz);

        DoubleMatrix2D edgeCoef = new DenseDoubleMatrix2D(parents.length + 1, parents.length + 1);

        for (int k = 0; k < parents.length; k++) edgeCoef.set(k, parents.length, b.get(k));

        int[] allVars = new int[parents.length + 1];
        allVars[0] = i;
        System.arraycopy(parents, 0, allVars, 1, parents.length);

        DoubleMatrix2D errCovar = getCorrMatrix().viewSelection(allVars, allVars).copy();

        DoubleMatrix2D impliedCovar = MatrixUtils.impliedCovarC(edgeCoef, errCovar);

        double variance = impliedCovar.get(parents.length, parents.length);

        double sum = 0.0;
        Normal normal = new Normal(0, variance, new MersenneTwister(new Date()));

        for (int s = 0; s < dataSet.getNumRows(); s++) {
            double value = dataSet.getDouble(s, i);
            double p = normal.pdf(value);
            sum += Math.log(p);
        }

        return sum - d * Math.log(n);
    }

    private double localSemScore4(int i, int[] parents) {
        double variance = getCorrMatrix().get(i, i);
        int n = sampleSize();
        double d = parents.length + 1;

        // Regress z onto i, yielding regression coefficients b.
        DoubleMatrix2D Czz =
                getCorrMatrix().viewSelection(parents, parents);
        DoubleMatrix2D inverse;
        try {
            inverse = algebra().inverse(Czz);
        }
        catch (Exception e) {
            StringBuffer buf = new StringBuffer();
            buf.append("Could not invert matrix for variables: ");

            for (int j = 0; j < parents.length; j++) {
                buf.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    buf.append(", ");
                }
            }

            throw new IllegalArgumentException(buf.toString());
        }

        DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
        Cyz = Cyz.viewSelection(parents);
        DoubleMatrix1D b = algebra().mult(inverse, Cyz);

        variance -= algebra().mult(Cyz, b);


        double sum = 0.0;

        for (int s = 0; s < dataSet.getNumRows(); s++) {
            double value = dataSet.getDouble(s, i);

            double pred = 0.0;

            for (int m = 0; m < parents.length; m++) {
                pred += b.get(m) * dataSet.getDouble(s, parents[m]);
            }

            sum += lnf1(value, variance, pred);
        }

        return sum - 2 *  d * Math.log(n);
    }

    private double localSemScore5(int i, int[] parents) {
        int n = sampleSize();
        double d = parents.length + 1;

//        double variance = getCorrMatrix().get(i, i);
//
//        // Regress z onto i, yielding regression coefficients b.
//        DoubleMatrix2D Czz =
//                getCorrMatrix().viewSelection(parents, parents);
//        DoubleMatrix2D inverse;
//        try {
//            inverse = algebra().inverse(Czz);
//        }
//        catch (Exception e) {
//            StringBuffer buf = new StringBuffer();
//            buf.append("Could not invert matrix for variables: ");
//
//            for (int j = 0; j < parents.length; j++) {
//                buf.append(variables.get(parents[j]));
//
//                if (j < parents.length - 1) {
//                    buf.append(", ");
//                }
//            }
//
//            throw new IllegalArgumentException(buf.toString());
//        }
//
//        DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
//        Cyz = Cyz.viewSelection(parents);
//        DoubleMatrix1D b = algebra().mult(inverse, Cyz);
//
//        variance -= algebra().mult(Cyz, b);

        double variance = StatUtils.partialVariance(cov, i, parents);
        double sum = 0.0;

        for (int s = 0; s < dataSet.getNumRows(); s++) {
            double value = dataSet.getDouble(s, i);
            sum += lnf2(value, variance, 0.0);
        }

        return sum - 2 * d * Math.log(n);
    }

    private double localSemScore6(int i, int[] parents) throws IllegalArgumentException {
//
        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        int n = sampleSize();
        double d = parents.length + 1;
//
        // Regress z onto i, yielding regression coefficients b.
        DoubleMatrix2D Czz =
                getCorrMatrix().viewSelection(parents, parents);
        DoubleMatrix2D inverse;
        try {
            inverse = algebra().inverse(Czz);
        }
        catch (IllegalArgumentException e) {
            StringBuffer buf = new StringBuffer();
            buf.append("Could not invert matrix for variables: ");

            for (int j = 0; j < parents.length; j++) {
                buf.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    buf.append(", ");
                }
            }

            System.out.println(buf.toString());

            throw e;

//            throw new RuntimeException(buf.toString(), e);
        }
//
        DoubleMatrix1D Cyz = getCorrMatrix().viewColumn(i);
        Cyz = Cyz.viewSelection(parents);
        DoubleMatrix1D b = algebra().mult(inverse, Cyz);

        double rss = 0.0;

        for (int s = 0; s < dataSet.getNumRows(); s++) {
            double value = dataSet.getDouble(s, i);

            double pred = 0.0;

            for (int m = 0; m < parents.length; m++) {
                pred += b.get(m) * dataSet.getDouble(s, parents[m]);
            }

            rss += Math.pow(value - pred, 2.0);
        }

        return (- n * Math.log(rss / n)) - 2 * d * Math.log(n);

//        double penalty = getPenaltyMutiplier();
//
//        return -(n / 2.) - (n / 2.) * Math.log(variance) - penalty * (d / 2.) * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n / (2 * Math.PI) - getPenaltyMutiplier() * d * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n * Math.log(2 * Math.PI) - 0.5 * n - getPenaltyMutiplier() * d * Math.log(n);
//        return -n * Math.log(Math.sqrt(variance)) - n * Math.log(2 * Math.PI) - 0.5 * n - getPenaltyMutiplier() * d * Math.log(n);
//        return -n - n * Math.log(variance) - penalty * d * Math.log(n);
    }

    private double lnf1(double x, double variance, double mean) {
        return -Math.log(Math.sqrt(variance)) - Math.log(Math.sqrt(2 * Math.PI)) - (Math.pow(x - mean, 2.0) / variance);
    }

    private double lnf2(double x, double variance, double mean) {
        return -Math.log(Math.sqrt(variance)) - (x / variance);
    }

    private int sampleSize() {
//        System.out.println("sample size = " + sampleSize);
        return this.sampleSize;

//        return dataSet().getNumRows();
    }

    private List<Node> getVariables() {
        return variables;
    }

    private DoubleMatrix2D getCorrMatrix() {
        return cov;
    }

    private Algebra algebra() {
        return algebra;
    }

    private DataSet dataSet() {
        return dataSet;
    }

    private double getStructurePrior() {
        return structurePrior;
    }

    private double getSamplePrior() {
        return samplePrior;
    }

    private boolean isDiscrete() {
        return discrete;
    }

    public void setStructurePrior(double structurePrior) {
        this.structurePrior = structurePrior;
    }

    public void setSamplePrior(double samplePrior) {
        this.samplePrior = samplePrior;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    private void fireGraphChange(Graph graph) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(new PropertyChangeEvent(this, "graph", null, graph));
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getListeners().add(l);
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
//        if (penaltyDiscount < 0) {
//            throw new IllegalArgumentException("Penalty discount must be >= 0: "
//                    + penaltyDiscount);
//        }

        this.penaltyDiscount = penaltyDiscount;
    }

    public int getMaxNumEdges() {
        return maxNumEdges;
    }

    public void setMaxNumEdges(int maxNumEdges) {
        if (maxNumEdges < -1) throw new IllegalArgumentException();

        this.maxNumEdges = maxNumEdges;
    }

    public double getModelBic() {
        return modelBic;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }
}