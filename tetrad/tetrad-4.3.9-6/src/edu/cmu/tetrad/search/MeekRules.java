package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.SearchLogUtils;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implements Meek's complete orientation rule set for PC (Chris Meek (1995),
 * "Causal inference and causal explanation with background knowledge"),
 * modified for Conservative PC to check noncolliders against recorded
 * noncolliders before orienting.
 * <p/>
 * For now, the fourth rule is always performed.
 *
 * @author Joseph Ramsey
 */
public class MeekRules implements ImpliedOrientation {

    private Knowledge knowledge;

    /**
     * True if cycles are to be aggressively prevented. May be expensive for
     * large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;


    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();
    private Map<Edge, Edge> changedEdges = new HashMap<Edge, Edge>();


    /**
     * Constructs the <code>MeekRules</code> with no logging.
     */
    public MeekRules() {

    }

    //======================== Public Methods ========================//


    public void orientImplied(Graph graph) {
        orientUsingMeekRulesLocally(knowledge, graph);
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    //============================== Private Methods ===================================//

    private void orientUsingMeekRulesLocally(Knowledge knowledge, Graph graph) {
//        this.logger.log("details", "Starting Orientation Step D.");
        boolean changed;

        do {
            changed = meekR2(graph, knowledge) ||
                    meekR1Locally(graph, knowledge) || meekR3(graph, knowledge) ||
                    meekR4(graph, knowledge);
        } while (changed);


//        this.logger.log("details", "Finishing Orientation Step D.");
    }

    /**
     * Meek's rule R1: if b-->a, a---c, and a not adj to c, then a-->c
     */
    private boolean meekR1Locally(Graph graph, Knowledge knowledge) {
        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);

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
                    if (!isUnshieldedNoncollider(b, a, c, graph)) {
                        continue;
                    }

                    if (isArrowpointAllowed(a, c, knowledge) && !createsCycle(a, c, graph)) {
                        Edge before = graph.getEdge(a, c);
                        graph.setEndpoint(a, c, Endpoint.ARROW);
                        Edge after = graph.getEdge(a, c);
                        changedEdges.put(after, before);

                        this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg(
                                "Meek R1 triangle (" + b + "-->" + a + "---" + c + ")", graph.getEdge(a, c)));
                        changed = true;
                    }
                }
                else if (graph.getEndpoint(c, a) == Endpoint.ARROW &&
                        graph.isUndirectedFromTo(a, b)) {
                    if (!isUnshieldedNoncollider(b, a, c, graph)) {
                        continue;
                    }

                    if (isArrowpointAllowed(a, b, knowledge) && !createsCycle(a, b, graph)) {
                        Edge before = graph.getEdge(a, c);
                        graph.setEndpoint(a, b, Endpoint.ARROW);
                        Edge after = graph.getEdge(a, c);
                        changedEdges.put(after, before);

                        this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg(
                                "Meek R1 (" + c + "-->" + a + "---" + b + ")", graph.getEdge(a, b)));
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    /**
     * If b-->a-->c, b--c, then b-->c.
     */
    private boolean meekR2(Graph graph, Knowledge knowledge) {
        List<Node> nodes = graph.getNodes();
        boolean changed = false;

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

                if (graph.isDirectedFromTo(b, a) &&
                        graph.isDirectedFromTo(a, c) &&
                        graph.isUndirectedFromTo(b, c)) {
                    if (isArrowpointAllowed(b, c, knowledge) && !createsCycle(b, c, graph)) {
                        Edge before = graph.getEdge(b, c);
                        graph.setEndpoint(b, c, Endpoint.ARROW);
                        Edge after = graph.getEdge(b, c);
                        changedEdges.put(after, before);
                        this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg("Meek R2", graph.getEdge(b, c)));
                    }
                }
                else if (graph.isDirectedFromTo(c, a) &&
                        graph.isDirectedFromTo(a, b) &&
                        graph.isUndirectedFromTo(c, b)) {
                    if (isArrowpointAllowed(c, b, knowledge) && !createsCycle(c, b, graph)) {
                        Edge before = graph.getEdge(c, b);
                        graph.setEndpoint(c, b, Endpoint.ARROW);
                        Edge after = graph.getEdge(c, b);
                        changedEdges.put(after, before);
                        this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg("Meek R2", graph.getEdge(c, b)));
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Meek's rule R3. If a--b, a--c, a--d, c-->b, d-->b, then orient a-->b.
     */
    private boolean meekR3(Graph graph, Knowledge knowledge) {

        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);

            if (adjacentNodes.size() < 3) {
                continue;
            }

            for (Node b : adjacentNodes) {
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

                    if (!isUnshieldedNoncollider(c, a, d, graph)) {
                        continue;
                    }

                    if (graph.isDirectedFromTo(c, b) &&
                            graph.isDirectedFromTo(d, b)) {
                        if (isArrowpointAllowed(a, b, knowledge) && !createsCycle(a, b, graph)) {
                            Edge before = graph.getEdge(a, b);
                            graph.setEndpoint(a, b, Endpoint.ARROW);
                            Edge after = graph.getEdge(a, b);
                            changedEdges.put(after, before);

                            this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg("Meek R3", graph.getEdge(a, b)));
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        return changed;
    }

    private boolean meekR4(Graph graph, Knowledge knowledge) {
        if (knowledge == null) {
            return false;
        }

        List<Node> nodes = graph.getNodes();
        boolean changed = false;

        for (Node a : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(a);

            if (adjacentNodes.size() < 3) {
                continue;
            }

            for (Node d : adjacentNodes) {
                if (!graph.isAdjacentTo(a, d)) {
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

                    if (!graph.isUndirectedFromTo(a, b)) {
                        continue;
                    }

                    if (!graph.isUndirectedFromTo(a, c)) {
                        continue;
                    }

                    if (!isUnshieldedNoncollider(c, a, b, graph)) {
                        continue;
                    }

                    if (graph.isDirectedFromTo(b, d) &&
                            graph.isDirectedFromTo(d, c)) {
                        if (isArrowpointAllowed(a, c, knowledge) && !createsCycle(a, c, graph)) {
                            Edge before = graph.getEdge(a, c);
                            graph.setEndpoint(a, c, Endpoint.ARROW);
                            Edge after = graph.getEdge(a, c);
                            changedEdges.put(after, before);

                            this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg("Meek R4", graph.getEdge(a, c)));
                            changed = true;
                            break;
                        }
                    }
                    else if (graph.isDirectedFromTo(c, d) &&
                            graph.isDirectedFromTo(d, b)) {
                        if (isArrowpointAllowed(a, b, knowledge) && !createsCycle(a, b, graph)) {
                            Edge before = graph.getEdge(a, b);
                            graph.setEndpoint(a, b, Endpoint.ARROW);
                            Edge after = graph.getEdge(a, b);
                            changedEdges.put(after, before);

                            this.logger.log("edgeOriented", SearchLogUtils.edgeOrientedMsg("Meek R4", graph.getEdge(a, b)));
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }

        return changed;
    }

    private static boolean isUnshieldedNoncollider(Node a, Node b, Node c,
            Graph graph) {
        if (graph.isAmbiguous(a, b, c)) {
            return false;
        }

        if (!graph.isAdjacentTo(a, b)) {
            return false;
        }

        if (!graph.isAdjacentTo(c, b)) {
            return false;
        }

        if (graph.isAdjacentTo(a, c)) {
            return false;
        }

        return !(graph.getEndpoint(a, b) == Endpoint.ARROW &&
                graph.getEndpoint(c, b) == Endpoint.ARROW);

    }


    private static boolean isArrowpointAllowed(Object from, Object to,
            Knowledge knowledge) {
        if (knowledge == null) return true;
        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }

    /**
     * Returns true if orienting x-->y would create a cycle.
     */
    private boolean createsCycle(Node x, Node y, Graph graph) {
        if (aggressivelyPreventCycles) {
            return graph.isAncestorOf(y, x);
        }
        else {
            return false;
        }
    }

    public boolean isAggressivelyPreventCycles() {
        return aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    public Map<Edge, Edge> getChangedEdges() {
        return changedEdges;
    }
}
