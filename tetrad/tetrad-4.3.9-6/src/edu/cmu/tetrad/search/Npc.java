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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.search.indtest.SearchLogUtils;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.util.*;

/**
 * Implements the PC ("Peter/Clark") algorithm, as specified in Chapter 6 of
 * Spirtes, Glymour, and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, with a modified rule set in step D due to Chris Meek. For the
 * modified rule set, see Chris Meek (1995), "Causal inference and causal
 * explanation with background knowledge."
 *
 * @author Joseph Ramsey (this version).
 */
public class Npc implements GraphSearch {

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private Knowledge knowledge;

    /**
     * The maximum number of nodes conditioned on in the search.
     */
    private int depth = Integer.MAX_VALUE;

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;


    /**
     * True if cycles are to be aggressively prevented. May be expensive for
     * large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * Count of independence tests.
     */
    private int numIndependenceTests = 0;


    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    private double alpha;

    /**
     * The list of all unshielded triples.
     */
    private Set<Triple> allTriples;

    /**
     * Set of unshielded colliders from the triple orientation step.
     */
    private Set<Triple> colliderTriples;

    /**
     * Set of unshielded noncolliders from the triple orientation step.
     */
    private Set<Triple> noncolliderTriples;

    /**
     * Set of ambiguous unshielded triples.
     */
    private Set<Triple> ambiguousTriples;
    private Graph trueGraph;

    private SepsetMap sepsetMap = new SepsetMap();

    int n1 = 0;
    int n2 = 0;

    //=============================CONSTRUCTORS==========================//

    public Npc(IndependenceTest independenceTest, Knowledge knowledge) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.knowledge = knowledge;
        this.allTriples = new HashSet<Triple>();
        this.ambiguousTriples = new HashSet<Triple>();
        this.colliderTriples = new HashSet<Triple>();
        this.noncolliderTriples = new HashSet<Triple>();

//        System.out.println(new CovarianceMatrix(independenceTest.getSimulatedData()));

        TetradLoggerConfig config = logger.getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            logger.setTetradLoggerConfig(config);
        }
    }

    Npc(double alpha) {
        this.alpha = alpha;
    }

    //==============================PUBLIC METHODS========================//

    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Runs PC starting with a fully connected graph over all of the variables
     * in the domain of the independence test.
     */
    public Graph search() {
        IndependenceTest test = getIndependenceTest();
        List<Node> nodes = test.getVariables();

        Graph graph = new EdgeListGraph(nodes);
        graph.fullyConnect(Endpoint.TAIL);

        knowledge = new Knowledge();

        Set<Edge> addedEdges = new HashSet<Edge>();

        boolean changed = true;

        while (changed) {
            changed = false;
            Fas fas = new Fas(graph, test);
            fas.setKnowledge(knowledge);
            SepsetMap sepsetMap = fas.search();

            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node x = nodes.get(i);
                    Node y = nodes.get(j);

                    List<Node> sepset = sepsetMap.get(x, y);
                    orientPc(graph, knowledge, sepsetMap);

//                    System.out.println(graph);

                    if (sepset != null) {
                        for (Node node : sepset) {
                            if (!graph.isAdjacentTo(x, node)) {
                                List<List<Node>> paths = GraphUtils.allPathsFromToExcluding(graph, x, node,
                                        Collections.singletonList(y));

                                if (!paths.isEmpty() && !addedEdges.contains(Edges.undirectedEdge(x, node))) {
                                    graph.addUndirectedEdge(x, node);
                                    addedEdges.add(Edges.undirectedEdge(x, node));
                                    changed = true;
                                }
                            }

                            if (!graph.isAdjacentTo(y, node)) {
                                List<List<Node>> paths = GraphUtils.allPathsFromToExcluding(graph, y, node,
                                        Collections.singletonList(x));

                                if (!paths.isEmpty() && !addedEdges.contains(Edges.undirectedEdge(y, node))) {
                                    graph.addUndirectedEdge(y, node);
                                    addedEdges.add(Edges.undirectedEdge(y, node));
                                    changed = true;
                                }
                            }


//                            if (!graph.existsUndirectedPathFromTo(x, node)) {
//                                graph.addUndirectedEdge(x, node);
//                                changed = true;
//                            }
//
//                            if (!graph.existsUndirectedPathFromTo(y, node)) {
//                                graph.addUndirectedEdge(y, node);
//                                changed = true;
//                            }

                        }
                    }
                }
            }
        }

//        System.out.println("n1 = " + n1 + " n2 = " + n2);

        return graph;
    }

    private List<Node> independent(IndependenceTest test, Graph graph, Node x, Node y) {
        List<Node> commonAdjacents = graph.getAdjacentNodes(x);
        commonAdjacents.retainAll(graph.getAdjacentNodes(y));
        commonAdjacents.remove(y);

        DepthChoiceGenerator generator = new DepthChoiceGenerator(commonAdjacents.size(), commonAdjacents.size());
        int[] choice;

        while ((choice = generator.next()) != null) {
            List<Node> excludes = GraphUtils.asList(choice, commonAdjacents);
            List<Node> sepset = getPseudoMarkovBlanket3(graph, x, y, excludes);
//            printTrueDseps(test, x, y, sepset);

            if (test.isIndependent(x, y, sepset)) {
                sepsetMap.set(x, y, sepset);
                return sepset;
            }
        }

        sepsetMap.set(x, y, null);
        return null;
    }

    private void printTrueDseps(IndependenceTest test, Node x, Node y, List<Node> sepset) {
        if (trueGraph != null) {
            IndTestDSep dsep = new IndTestDSep(trueGraph);

            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());

            List<Node> _sepset = new LinkedList<Node>();

            for (Node node : sepset) {
                _sepset.add(trueGraph.getNode(node.getName()));
            }

            if (dsep.isDSeparated(_x, _y, _sepset)) {
                test.isIndependent(x, y, sepset);
                System.out.println("1\t" + test.getPValue());
            } else {
                test.isIndependent(x, y, sepset);
                System.out.println("0\t" + test.getPValue());
            }
        }
    }

    private List<Node> getPseudoMarkovBlanket(Graph graph, Node x, Node y, List<Node> excludes) {
        List<Node> condSet = graph.getAdjacentNodes(x);
        condSet.remove(y);
        condSet.removeAll(excludes);

        for (Node adj : new LinkedList<Node>(condSet)) {
            if (graph.getEdge(x, adj).getProximalEndpoint(adj) == Endpoint.ARROW) {
                List<Node> parents = graph.getNodesInTo(adj, Endpoint.ARROW);

                for (Node parent : parents) {
//                    System.out.println("Parents of " + adj + " are " + parents);

                    if (parent == x || parent == y) {
                        continue;
                    }

                    if (excludes.contains(parent)) {
                        continue;
                    }

                    if (!condSet.contains(parent)) {
                        condSet.add(parent);
                    }
                }
            }
        }

//        System.out.println("condSet " + x + " --- " + y + " = " + condSet + " excludes = " + excludes);

        return condSet;
    }

    private List<Node> getPseudoMarkovBlanket2(Graph graph, Node x, Node y, List<Node> excludes) {
        List<Node> condSet = new LinkedList<Node>();

        for (Node adj : graph.getAdjacentNodes(x)) {
            if (adj == y) continue;
            if (excludes.contains(adj)) continue;

            List<Node> parents = graph.getParents(adj);
            List<Node> adjacents = graph.getAdjacentNodes(adj);

            if (parents.size() < 2) {
                condSet.add(adj);
            } else if (parents.size() < adjacents.size()) {
                condSet.add(adj);

                for (Node parent : parents) {
                    if (parent != x && parent != y && !condSet.contains(parent)) {
                        condSet.add(parent);
                    }
                }
            }
        }

//        System.out.println("condSet " + x + " --- " + y + " = " + condSet + " excludes = " + excludes);

        return condSet;
    }

    private List<Node> getPseudoMarkovBlanket3(Graph graph, Node x, Node y, List<Node> excludes) {
        List<Node> condSet = new LinkedList<Node>();

        for (Node adj : graph.getAdjacentNodes(x)) {
            if (adj == y) continue;
            if (excludes.contains(adj)) continue;

            List<Node> parents = graph.getParents(adj);
            List<Node> adjacents = graph.getAdjacentNodes(adj);

            for (Node adj2 : graph.getAdjacentNodes(adj)) {
                List<Node> path = new LinkedList<Node>();
                path.add(x);
                path.add(adj);
                path.add(adj2);

                Node _x = trueGraph.getNode(x.getName());
                Node _adj = trueGraph.getNode(adj.getName());
                Node _adj2 = trueGraph.getNode(adj2.getName());

                List<Node> _path = new LinkedList<Node>();
                _path.add(_x);
                _path.add(_adj);
                _path.add(_adj2);

//                boolean b1 = graph.isUndirectedFromTo(x, adj); //  !graph.isParentOf(adj, x);
                boolean b1 = !graph.isParentOf(adj, x);
                boolean b2 = graph.isUndirectedFromTo(adj, adj2);

                if (trueGraph != null && b1 && b2 &&
                        !trueGraph.isDefiniteCollider(_x, _adj, _adj2)) {
                    n1++;
//                    System.out.println(GraphUtils.pathString(graph, path) + ", " + GraphUtils.pathString(trueGraph, _path));
                }

                if (trueGraph != null && b1 && b2 &&
                        trueGraph.isDefiniteCollider(_x, _adj, _adj2)) {
                    n2++;
//                    System.out.println(GraphUtils.pathString(graph, path) + ", " + GraphUtils.pathString(trueGraph, _path));
                }

            }

            if (parents.size() < 2) {
                condSet.add(adj);

            } else if (parents.size() < adjacents.size()) {
                condSet.add(adj);

                if (graph.isChildOf(adj, x)) {
                    for (Node parent : graph.getAdjacentNodes(adj)) {
                        if (parent != x && parent != y && !condSet.contains(parent)) {
                            condSet.add(parent);
                        }
                    }
                }
            }
        }

//        System.out.println("condSet " + x + " --- " + y + " = " + condSet + " excludes = " + excludes);

        return condSet;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    private Graph orientPc(Graph graph, Knowledge knowledge, SepsetMap sepset) {
        for (Edge edge : graph.getEdges()) {
            graph.removeEdge(edge);
            graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
        }

        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
        SearchGraphUtils.orientCollidersUsingSepsets(sepset, knowledge, graph);
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);
        return graph;
    }


    private Graph orientCpc(Graph graph, Knowledge knowledge, int depth) {
        graph = GraphUtils.undirectedGraph(graph);
        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
        graph = orientUnshieldedTriples(graph, knowledge, getIndependenceTest(), depth);
        MeekRules meekRules = new MeekRules();
        meekRules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        meekRules.setKnowledge(knowledge);
        meekRules.orientImplied(graph);
        return graph;
    }

    @SuppressWarnings({"SameParameterValue"})
    private Graph orientUnshieldedTriples(Graph graph, Knowledge knowledge,
                                          IndependenceTest test, int depth) {
        TetradLogger.getInstance().info("Starting Collider Orientation:");

        colliderTriples = new HashSet<Triple>();
        noncolliderTriples = new HashSet<Triple>();
        ambiguousTriples = new HashSet<Triple>();

        for (Node y : graph.getNodes()) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(y);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node x = adjacentNodes.get(combination[0]);
                Node z = adjacentNodes.get(combination[1]);

                if (graph.isAdjacentTo(x, z)) {
                    continue;
                }

                allTriples.add(new Triple(x, y, z));

                SearchGraphUtils.CpcTripleType type = SearchGraphUtils.getCpcTripleType(x, y, z, test, depth, graph);

                if (type == SearchGraphUtils.CpcTripleType.COLLIDER) {
                    if (colliderAllowed(x, y, z, knowledge)) {
                        graph.setEndpoint(x, y, Endpoint.ARROW);
                        graph.setEndpoint(z, y, Endpoint.ARROW);

                        TetradLogger.getInstance().log("colliderOriented", SearchLogUtils.colliderOrientedMsg(x, y, z));
                    }

                    colliderTriples.add(new Triple(x, y, z));
                } else if (type == SearchGraphUtils.CpcTripleType.AMBIGUOUS) {
                    Triple triple = new Triple(x, y, z);
                    ambiguousTriples.add(triple);
                    graph.setAmbiguous(triple, true);
                } else {
                    noncolliderTriples.add(new Triple(x, y, z));
                }
            }
        }

        TetradLogger.getInstance().info("Finishing Collider Orientation.");

        return graph;
    }

    private boolean colliderAllowed(Node x, Node y, Node z, Knowledge knowledge) {
        return isArrowpointAllowed1(x, y, knowledge) &&
                isArrowpointAllowed1(z, y, knowledge);
    }

    private static boolean isArrowpointAllowed1(Node from, Node to,
                                                Knowledge knowledge) {
        if (knowledge == null) {
            return true;
        }

        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }
}