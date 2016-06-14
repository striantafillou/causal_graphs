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
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.search.indtest.SearchLogUtils;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;


/**
 * Implements the FCI ("Fast Causal Inference") algorithm from Chapter 6 of
 * Spirtes, Glymour and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, chapter 6, with a modified arrow-complete rule set. This version of
 * FCI returns a PAG instead of a POIPG, and underlinings in the PAG are not
 * used.
 *
 * @author Erin Korber, June 2004
 */
public final class Fci {

    /**
     * The PAG being constructed.
     */
    private Graph graph;

    /**
     * The SepsetMap being constructed.
     */
    private SepsetMap sepsetMap;

    /**
     * The background knowledge.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * The variables to search over (optional)
     */
    private List<Node> variables = new ArrayList<Node>();

    /**
     * The independence test.
     */
    private IndependenceTest independenceTest;

    /**
     * change flag for repeat rules
     */
    private boolean changeFlag = true;

    /**
     * The depth for the fast adjacency search.
     */
    private int depth = -1;

    /**
     * Elapsed time of last search.
     */
    private long elapsedTime;

    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new FCI search for the given independence test and
     * background knowledge.
     */
    public Fci(IndependenceTest independenceTest) {
        if (independenceTest == null || knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.variables.addAll(independenceTest.getVariables());
    }

    /**
     * Constructs a new FCI search for the given independence test and
     * background knowledge and a list of variables to search over.
     */
    public Fci(IndependenceTest independenceTest, List<Node> searchVars) {
        if (independenceTest == null || knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.variables.addAll(independenceTest.getVariables());

        Set<Node> remVars = new HashSet<Node>();
        for (Node node1 : this.variables) {
            boolean search = false;
            for (Node node2 : searchVars) {
                if (node1.getName().equals(node2.getName())) {
                    search = true;
                }
            }
            if (!search) {
                remVars.add(node1);
            }
        }
        this.variables.removeAll(remVars);
    }

    //========================PUBLIC METHODS==========================//

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0: " + depth);
        }

        this.depth = depth;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public Graph search() {
        long beginTime = System.currentTimeMillis();
        logger.info("Starting FCI algorithm.");
        logger.info("Independence test = " + independenceTest + ".");

        //List<Node> variables = independenceTest.getVariables();       - Robert Tillman 2008
        List<Node> nodes = new LinkedList<Node>();

        for (Node variable : variables) {
            nodes.add(variable);
        }

        this.graph = new EdgeListGraph(nodes);

        // Step FCI A.
        graph.fullyConnect(Endpoint.CIRCLE);

        // Step FCI B.
        Fas adj = new Fas(graph, independenceTest);
        adj.setKnowledge(getKnowledge());
        adj.setDepth(depth);
        this.sepsetMap = adj.search();

        // Step FCI C
        long time1 = System.currentTimeMillis();
        orientColliders();

        long time2 = System.currentTimeMillis();
        logger.info("Step C: " + (time2 - time1) / 1000. + "s");

        // Step FCI D.
        long time3 = System.currentTimeMillis();

        PossibleDsepFci possibleDSep =
                new PossibleDsepFci(graph, independenceTest, getSepsetMap());
        possibleDSep.setDepth(getDepth());
        possibleDSep.setKnowledge(getKnowledge());
        sepsetMap = possibleDSep.search();

        // Reorient all edges as o-o.
        long time4 = System.currentTimeMillis();
        logger.info("Step D: " + (time4 - time3) / 1000. + "s");

        graph.reorientAllWith(Endpoint.CIRCLE);

        // Step CI C
        long time5 = System.currentTimeMillis();
        //fciOrientbk(getKnowledge(), graph, independenceTest.getVariables());    - Robert Tillman 2008
        fciOrientbk(getKnowledge(), graph, variables);
        orientColliders();

        long time6 = System.currentTimeMillis();
        logger.info("Step CI C: " + (time6 - time5) / 1000. + "s");

        // Step CI D.
        doFinalOrientation();

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - beginTime;

//        graph.closeInducingPaths();   //to make sure it's a legal PAG
        return graph;
    }

    public SepsetMap getSepsetMap() {
        return sepsetMap;
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

    //===========================PRIVATE METHODS=========================//

    /**
     * Orients colliders in the graph.  (FCI Step C)
     */
    private void orientColliders() {
        List<Node> nodes = graph.getNodes();

        for (Node b : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(b);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node a = adjacentNodes.get(combination[0]);
                Node c = adjacentNodes.get(combination[1]);

                // Skip triples that are shielded.
                if (graph.isAdjacentTo(a, c)) {
                    continue;
                }

                if (!sepsetMap.get(a, c).contains(b)) {
                    if (!isArrowpointAllowed(a, b)) {
                        continue;
                    }

                    if (!isArrowpointAllowed(c, b)) {
                        continue;
                    }

                    graph.setEndpoint(a, b, Endpoint.ARROW);
                    graph.setEndpoint(c, b, Endpoint.ARROW);
                    logger.colliderOriented(SearchLogUtils.colliderOrientedMsg(a, b, c));
                }
            }
        }
    }

    private void doFinalOrientation() {
        while (changeFlag) {
            changeFlag = false;
            doubleTriangle();
            awayFromColliderAncestorCycle();
            discrimPaths();   //slowest, so do last
        }
    }

    /**
     * Orients according to background knowledge
     */
    private void fciOrientbk(Knowledge bk, Graph graph, List<Node> variables) {
        logger.info("Starting BK Orientation.");

        for (Iterator<KnowledgeEdge> it =
                bk.forbiddenEdgesIterator(); it.hasNext();) {
            KnowledgeEdge edge = it.next();

            //match strings to variables in the graph.
            Node from = SearchGraphUtils.translate(edge.getFrom(), variables);
            Node to = SearchGraphUtils.translate(edge.getTo(), variables);

            if (from == null || to == null) {
                continue;
            }

            if (graph.getEdge(from, to) == null) {
                continue;
            }

            // Orient to*->from
            graph.setEndpoint(to, from, Endpoint.ARROW);
            logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
        }

        for (Iterator<KnowledgeEdge> it =
                bk.requiredEdgesIterator(); it.hasNext();) {
            KnowledgeEdge edge = it.next();

            //match strings to variables in this graph
            Node from = SearchGraphUtils.translate(edge.getFrom(), variables);
            Node to = SearchGraphUtils.translate(edge.getTo(), variables);

            if (from == null || to == null) {
                continue;
            }

            if (graph.getEdge(from, to) == null) {
                continue;
            }

            // Orient from*->to (?)
            // Orient from-->to
            graph.setEndpoint(to, from, Endpoint.TAIL);
            graph.setEndpoint(from, to, Endpoint.ARROW);
            logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
        }

        logger.info("Finishing BK Orientation.");
    }

    /**
     * Implements the double-triangle orientation rule, which states that if
     * D*-oB, A*->B<-*C and A*-*D*-*C is a noncollider, then D*->B.
     */
    private void doubleTriangle() {
        List<Node> nodes = graph.getNodes();

        for (Node B : nodes) {

            List<Node> intoBArrows = graph.getNodesInTo(B, Endpoint.ARROW);
            List<Node> intoBCircles = graph.getNodesInTo(B, Endpoint.CIRCLE);

            //possible A's and C's are those with arrows into B
            List<Node> possA = new LinkedList<Node>(intoBArrows);
            List<Node> possC = new LinkedList<Node>(intoBArrows);

            //possible D's are those with circles into B
            for (Node D : intoBCircles) {
                for (Node A : possA) {
                    for (Node C : possC) {
                        if (C == A) {
                            continue;
                        }

                        //skip anything not a double triangle
                        if (!graph.isAdjacentTo(A, D) ||
                                !graph.isAdjacentTo(C, D)) {
                            continue;
                        }

                        //skip if A,D,C is a collider
                        if (graph.isDefiniteCollider(A, D, C)) {
                            continue;
                        }

                        //if all of the previous tests pass, orient D*-oB as D*->B
                        if (!isArrowpointAllowed(D, B)) {
                            continue;
                        }

                        graph.setEndpoint(D, B, Endpoint.ARROW);
                        logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Double triangle", graph.getEdge(D, B)));
                        changeFlag = true;
                    }
                }
            }
        }
    }

    //Does all 3 of these rules at once instead of going through all
    // triples multiple times per iteration of doFinalOrientation.
    private void awayFromColliderAncestorCycle() {
        List<Node> nodes = graph.getNodes();

        for (Node B : nodes) {
            List<Node> adj = graph.getAdjacentNodes(B);

            if (adj.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adj.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node A = adj.get(combination[0]);
                Node C = adj.get(combination[1]);

                //choice gen doesnt do diff orders, so must switch A & C around.
                awayFromCollider(A, B, C);
                awayFromCollider(C, B, A);
                awayFromAncestor(A, B, C);
                awayFromAncestor(C, B, A);
                awayFromCycle(A, B, C);
                awayFromCycle(C, B, A);
            }
        }
    }


    private boolean isArrowpointAllowed(Node x, Node y) {
        if (graph.getEndpoint(x, y) == Endpoint.ARROW) {
            return true;
        }

        if (graph.getEndpoint(x, y) == Endpoint.TAIL) {
            return false;
        }

        if (graph.getEndpoint(y, x) == Endpoint.ARROW &&
                graph.getEndpoint(x, y) == Endpoint.CIRCLE) {
            return true;
        }

        return !knowledge.isForbiddenByTiers(x.getName(), y.getName());
    }


    // if a*->Bo-oC and not a*-*c, then a*->b-->c
    // (orient either circle if present, don't need both)
    private void awayFromCollider(Node a, Node b, Node c) {
        Endpoint BC = graph.getEndpoint(b, c);
        Endpoint CB = graph.getEndpoint(c, b);

        if (!(graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, b) == Endpoint.ARROW)) {
            if (CB == Endpoint.CIRCLE || CB == Endpoint.TAIL) {
                if (BC == Endpoint.CIRCLE) {
                    if (!isArrowpointAllowed(b, c)) {
                        return;
                    }

                    graph.setEndpoint(b, c, Endpoint.ARROW);
                    logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", graph.getEdge(b, c)));
                    changeFlag = true;
                }
            }

            if (BC == Endpoint.CIRCLE || BC == Endpoint.ARROW) {
                if (CB == Endpoint.CIRCLE) {
                    graph.setEndpoint(c, b, Endpoint.TAIL);
                    logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", graph.getEdge(c, b)));
                    changeFlag = true;
                }
            }
        }
    }

    //if a*-oC and either a-->b*->c or a*->b-->c, then a*->c
    private void awayFromAncestor(Node a, Node b, Node c) {
        if ((graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, c) == Endpoint.CIRCLE)) {

            if ((graph.getEndpoint(a, b) == Endpoint.ARROW) &&
                    (graph.getEndpoint(b, c) == Endpoint.ARROW) && (
                    (graph.getEndpoint(b, a) == Endpoint.TAIL) ||
                            (graph.getEndpoint(c, b) == Endpoint.TAIL))) {

                if (!isArrowpointAllowed(a, c)) {
                    return;
                }

                graph.setEndpoint(a, c, Endpoint.ARROW);
                logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from ancestor", graph.getEdge(a, c)));
                changeFlag = true;
            }
        }
    }

    //if Ao->c and a-->b-->c, then a-->c
    private void awayFromCycle(Node a, Node b, Node c) {
        if ((graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, c) == Endpoint.ARROW) &&
                (graph.getEndpoint(c, a) == Endpoint.CIRCLE)) {
            if (graph.isDirectedFromTo(a, b) && graph.isDirectedFromTo(b, c)) {
                graph.setEndpoint(c, a, Endpoint.TAIL);
                logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from cycle", graph.getEdge(c, a)));
                changeFlag = true;
            }
        }
    }

    /**
     * The triangles that must be oriented this way (won't be done by another
     * rule) all look like the ones below, where the dots are a collider path
     * from L to A with each node on the path (except L) a parent of C.
     * <pre>
     *          B
     *         xo           x is either an arrowhead or a circle
     *        /  \
     *       v    v
     * L....A --> C
     * </pre>
     */
    private void discrimPaths() {
        List<Node> nodes = graph.getNodes();

        for (Node b : nodes) {

            //potential A and C candidate pairs are only those
            // that look like this:   A<-oBo->C  or  A<->Bo->C
            List<Node> possAandC = graph.getNodesOutTo(b, Endpoint.ARROW);

            //keep arrows and circles
            List<Node> possA = new LinkedList<Node>(possAandC);
            possA.removeAll(graph.getNodesInTo(b, Endpoint.TAIL));

            //keep only circles
            List<Node> possC = new LinkedList<Node>(possAandC);
            possC.retainAll(graph.getNodesInTo(b, Endpoint.CIRCLE));

            for (Node a : possA) {
                for (Node c : possC) {
                    if (!graph.isParentOf(a, c)) {
                        continue;
                    }

                    LinkedList<Node> reachable = new LinkedList<Node>();
                    reachable.add(a);
                    reachablePathFind(a, b, c, reachable);
                }
            }
        }
    }

    /**
     * a method to search "back from a" to find a DDP. It is called with a
     * reachability list (first consisting only of a). This is breadth-first,
     * utilizing "reachability" concept from Geiger, Verma, and Pearl 1990. </p>
     * The body of a DDP consists of colliders that are parents of c.
     */
    private void reachablePathFind(Node a, Node b, Node c,
                                   LinkedList<Node> reachable) {
        Set<Node> cParents = new HashSet<Node>(graph.getParents(c));

        // Needed to avoid cycles in failure case.
        Set<Node> visited = new HashSet<Node>();
        visited.add(b);
        visited.add(c);

        // We don't want to include a,b,or c on the path, so they are added to
        // the "visited" set.  b and c are added explicitly here; a will be
        // added in the first while iteration.
        while (reachable.size() > 0) {
            Node x = reachable.removeFirst();
            visited.add(x);

            // Possible DDP path endpoints.
            List<Node> pathExtensions = graph.getNodesInTo(x, Endpoint.ARROW);
            pathExtensions.removeAll(visited);

            for (Node l : pathExtensions) {

                // If l is reachable and not adjacent to c, its a DDP
                // endpoint, so do DDP orientation. Otherwise, if l <-> c,
                // add l to the list of reachable nodes.
                if (!graph.isAdjacentTo(l, c)) {

                    // Check whether <a, b, c> should be reoriented given
                    // that l is not adjacent to c; if so, orient and stop.
                    doDdpOrientation(l, a, b, c);
                    return;
                } else if (cParents.contains(l)) {
                    if (graph.getEndpoint(x, l) == Endpoint.ARROW) {
                        reachable.add(l);
                    }
                }
            }
        }
    }

    /**
     * Orients the edges inside the definte discriminating path triangle. Takes
     * the left endpoint, and a,b,c as arguments.
     */
    private void doDdpOrientation(Node l, Node a, Node b, Node c) {
        List<Node> sepset = sepsetMap.get(l, c);

        if (sepset == null) {
            throw new IllegalArgumentException("The edge from l to c must have " +
                    "been removed at this point.");
        }

        if (sepset.contains(b)) {
            graph.setEndpoint(c, b, Endpoint.TAIL);
            logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Definite discriminating path l = " + l, graph.getEdge(b, c)));
            changeFlag = true;
        } else {
            if (!isArrowpointAllowed(a, b)) {
                return;
            }

            if (!isArrowpointAllowed(c, b)) {
                return;
            }

            graph.setEndpoint(a, b, Endpoint.ARROW);
            graph.setEndpoint(c, b, Endpoint.ARROW);
            logger.colliderOriented(SearchLogUtils.colliderOrientedMsg("Definite discriminating path.. l = " + l, a, b, c));
            changeFlag = true;
        }
    }

    /**
     * Helper Method: Checks if a directed edge is allowed by background
     * knowledge.
     */
    private boolean isDirEdgeAllowed(Node from, Node to) {
        return !getKnowledge().edgeRequired(to.getName(), from.getName()) &&
                !getKnowledge().edgeForbidden(from.getName(), to.getName());
    }
}

