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

import java.util.*;

/**
 * This class implements the ION Algorithm as described in David Danks' paper
 * "Learning Integrated Structure from Distributed Databases with Overlapping
 * Variables". </p> In the search method comments refer to Steps 1-4 of the
 * algorithm description on page 5 of the paper.
 *
 * @author Frank C. Wimberly
 */
public final class IONSearchOriginal {
    private Graph[] Gsub;          //The graphs of the POIPGS
    private int n;                       //The number of POIPGS
    //private Graph[] graphs;
    //private IndependenceTest[] indtests;
    private List[][][] sepsets;          //sepsets[i][][] is sepsets for graph i
    private Knowledge bk;

    private Graph G;
    private boolean[][] confirmed;

    private final static Endpoint NONE = null;
    private final static Endpoint NULL = Endpoint.TAIL;
    private final static Endpoint ARROW = Endpoint.ARROW;
    private final static Endpoint CIRCLE = Endpoint.CIRCLE;

    /**
     * The arguments of the constructor are an array of SearchGraphs each of
     * which represents one of the POIPGs learned from the datasets. Typically
     * they will be output from an FCI search or some other search. The second
     * argument is an array which stores the SepSets for each POIPG.  These are
     * also computed in the FCI search but may also be computed by the static
     * method findSepSets of the SepSetFinder class. sepsets[i] references a two
     * dimensional array which contains a separating set given the indices of
     * two variables in the ith dataset.  Hence sepset[0][1][2] is a List whose
     * elements separate variables 1 and 2 of the first (0th) dataset.
     */
    public IONSearchOriginal(Graph[] Gsub, List[][][] sepsets, Knowledge bk) {

        this.Gsub = Gsub;
        this.sepsets = sepsets;
        this.bk = bk;
        this.n = Gsub.length;

        //for(int i = 0; i < n; i++) {
        /* DEBUG PRINT
        System.out.println();
        System.out.println(" *** i = " + i);
        Gsub[i].print();
        */

        //}

    }

    /**
     * This constructor is the same as the first one except that the second
     * argument is an array instances of the SepSet class.  These are used to
     * construct the sepsets array so that the rest of the methods can be used
     * without modification.
     */
    public IONSearchOriginal(Graph[] Gsub, SepsetMap[] sepSets, Knowledge bk) {

        this.Gsub = Gsub;
        //this.sepsets = sepsets;
        this.n = Gsub.length;
        this.sepsets = new ArrayList[n][][];
        this.bk = bk;

        for (int i = 0; i < n; i++) {
            List<Node> v = Gsub[i].getNodes();
            this.sepsets[i] = new ArrayList[v.size()][v.size()];

            for (int j = 0; j < v.size(); j++) {
                Node x = v.get(j);
                for (int k = 0; k < v.size(); k++) {
                    if (j == k) {
                        continue;
                    }
                    Node y = v.get(k);

                    if (sepSets[i].get(x, y) == null) {
                        sepsets[i][j][k] = null;
                    } else {
                        sepsets[i][j][k] = new ArrayList<Node>(sepSets[i].get(x, y));
                    }
                }
            }
        }
    }

    /**
     * The search method straightforwardly implements the algorithm as described
     * in the paper.  It returns a Graph (instantiated as a EndpointMatrixGraph)
     * and also computes a boolean[][] array which indicates the edges that have
     * been confirmed.  In terms of the algorithm description they have had
     * their "?" subscripts removed.
     */
    public Graph search() {
        List[] Vsub = new List[n];

        //Step 1.
        //V will contain the union of the sets of variables
        List<Node> V = new ArrayList<Node>();
        for (int i = 0; i < n; i++) {

            Vsub[i] = Gsub[i].getNodes();
            for (Iterator it = Vsub[i].iterator(); it.hasNext();) {
                Node node = (Node) it.next();
                if (!V.contains(node)) {
                    V.add(node);
                }
            }
        }

        int nVnodes = V.size();

        confirmed = new boolean[nVnodes][nVnodes];
        for (int i = 0; i < nVnodes; i++) {
            for (int j = 0; j < nVnodes; j++) {
                confirmed[i][j] = false;
            }
        }

        G = new EdgeListGraph(V);
        G.fullyConnect(CIRCLE);  //Complete graph with circles.
        //End Step 1.

        //Step 1a.  Applies background knowledge.

        if (!bk.isEmpty()) {
            for (Iterator<KnowledgeEdge> req = bk.requiredEdgesIterator(); req.hasNext();) {
                KnowledgeEdge reqEdge = req.next();
                String A = reqEdge.getFrom();
                String B = reqEdge.getTo();
                Node nodeA = null;
                Node nodeB = null;

                for (Iterator<Node> itv = V.iterator(); itv.hasNext();) {
                    nodeA = itv.next();
                    if (A.equals(nodeA.getName())) {
                        break;
                    }
                }

                for (Iterator<Node> itv = V.iterator(); itv.hasNext();) {
                    nodeB = itv.next();
                    if (B.equals(nodeB.getName())) {
                        break;
                    }
                }

                G.setEndpoint(nodeA, nodeB, ARROW);
                G.setEndpoint(nodeB, nodeA, NULL);

                System.out.println(
                        "In IonSearch.search confirming edge between " +
                                nodeA.getName() + " " + nodeB.getName() +
                                V.indexOf(nodeA) + " " + V.indexOf(nodeB));
                confirmed[V.indexOf(nodeA)][V.indexOf(nodeB)] = true;
                confirmed[V.indexOf(nodeB)][V.indexOf(nodeA)] = true;

            }

            for (Iterator<KnowledgeEdge> fbd = bk.forbiddenEdgesIterator(); fbd.hasNext();) {
                KnowledgeEdge reqEdge = fbd.next();
                String A = reqEdge.getFrom();
                String B = reqEdge.getTo();
                Node nodeA = null;
                Node nodeB = null;

                for (Iterator<Node> itv = V.iterator(); itv.hasNext();) {
                    nodeA = itv.next();
                    if (A.equals(nodeA.getName())) {
                        break;
                    }
                }

                for (Iterator<Node> itv = V.iterator(); itv.hasNext();) {
                    nodeB = itv.next();
                    if (B.equals(nodeB.getName())) {
                        break;
                    }
                }

                G.setEndpoint(nodeA, nodeB, NONE);
                G.setEndpoint(nodeB, nodeA, NONE);

            }
        }

        //End Step 1a.

        //Step 2.
        for (int i = 0; i < n; i++) {                  //For each Gi

            int nNodes = Vsub[i].size();

            for (int x = 0; x < nNodes; x++) {
                for (int y = x + 1; y < nNodes; y++) {

                    //For each pair of non-adjacent nodes
                    if (Gsub[i].isAdjacentTo((Node) Vsub[i].get(x),
                            (Node) Vsub[i].get(y))) {
                        continue;
                    }

                    //Step 2a.
                    G.removeEdge((Node) Vsub[i].get(x), (Node) Vsub[i].get(y));

                    //Step 2b.
                    //remove remains true if no variable in sepset(x,y) in G(i)
                    //is a potential ancestor of x.
                    boolean remove = true;

                    //This loop checks whether a variable in sepset(x, y) is a
                    //potential ancestor of x in Gsub[i].
                    for (Iterator it =
                            (sepsets[i][x][y]).iterator(); it.hasNext();) {
                        Node isPotAnc = (Node) it.next();
                        if (isPotentialAncestor(Gsub[i], isPotAnc,
                                (Node) Vsub[i].get(x))) {
                            remove = false;
                            break;
                        }
                    }

                    //Definite ancestor across all G[j]
                    //If remove is true then remove all edges in G between y and
                    //any node which is a definite ancestor of x in some G(j).
                    if (remove) {
                        List<Node> ancestorsX =
                                getDefiniteAncestors((Node) Vsub[i].get(x));

                        for (int j = 0; j < n; j++) {
                            for (Iterator it =
                                    Vsub[j].iterator(); it.hasNext();) {
                                Node isDefAnc = (Node) it.next();
                                if (getDefiniteAncestors((Node) Vsub[i].get(x))
                                        .contains(isDefAnc)) {
                                    G.removeEdge((Node) Vsub[i].get(y),
                                            isDefAnc);
                                }
                            }
                        }
                    }

                    //remove remains true if no variable in sepset(x,y) in G(i)
                    //is a potential ancestor of y.
                    remove = true;
                    for (Iterator it =
                            sepsets[i][x][y].iterator(); it.hasNext();) {
                        Node isPotAnc = (Node) it.next();
                        if (isPotentialAncestor(Gsub[i], isPotAnc,
                                (Node) Vsub[i].get(y))) {
                            remove = false;
                            break;
                        }
                    }

                    //If remove is true then remove all edges in G between x and
                    //any node which is a definite ancestor of y in some G(j).
                    if (remove) {
                        for (int j = 0; j < n; j++) {
                            for (Iterator it =
                                    Vsub[j].iterator(); it.hasNext();) {
                                Node isDefAnc = (Node) it.next();

                                if (getDefiniteAncestors((Node) Vsub[i].get(y))
                                        .contains(isDefAnc)) {
                                    G.removeEdge((Node) Vsub[i].get(x),
                                            isDefAnc);
                                }

                            }
                        }
                    }
                }
            }
        }

        //Step 3.
        for (int j = 0; j < n; j++) {
            for (Iterator it = Vsub[j].iterator(); it.hasNext();) {
                Node nodex = (Node) it.next();
                List<Node> adjacentNodes = Gsub[j].getNodesOutTo(nodex, ARROW);
                for (Iterator<Node> ita = adjacentNodes.iterator(); ita.hasNext();) {
                    Node nodey = ita.next();
                    Endpoint fromEndPoint = Gsub[j].getEndpoint(nodey, nodex);
                    if (fromEndPoint == NULL || fromEndPoint == CIRCLE) {
                        if (G.isAdjacentTo(nodex, nodey)) {
                            G.setEndpoint(nodex, nodey, ARROW);
                            G.setEndpoint(nodey, nodex, fromEndPoint);
                        }
                    }
                }
            }
        }

        //Step 4.
        for (int x = 0; x < nVnodes; x++) {
            for (int y = x + 1; y < nVnodes; y++) {
                if (x == y) {
                    continue;
                }

                Node X = V.get(x);
                Node Y = V.get(y);
                if (G.isAdjacentTo(X, Y)) {

                    boolean commit = false;

                    List<Node> varT = getVarsOnTreks(G, X, Y);

                    List<Node> varTUnionXY = new ArrayList<Node>(varT);
                    varTUnionXY.add(X);
                    varTUnionXY.add(Y);

                    for (int i = 0; i < n; i++) {
                        if (!Vsub[i].contains(X) || !Vsub[i].contains(Y)) {
                            continue;
                        }

                        //Does Vsub[i] contain varTUnionXY?
                        //If so, commit = true.
                        boolean subset = true;

                        for (Iterator<Node> it =
                                varTUnionXY.iterator(); it.hasNext();) {
                            Node v = it.next();
                            if (!Vsub[i].contains(v)) {
                                subset = false;
                            }
                        }

                        if (subset) {
                            commit = true;
                        }
                    }

                    if (commit) {
                        confirmed[x][y] = true;
                        confirmed[y][x] = true;
                    }
                }

            }
        }

        return G;
    }

    //Returns a doubly subscripted boolean array confirmed.  confirmed[i][j]
    //indicates whether the edge between the ith and jth variables is confirmed.
    public boolean[][] getConfirmed() {
        return confirmed;
    }

    //Is nodeFrom a definite ancestor of NodeTo within a given graph?
    private boolean isDefiniteAncestor(Graph g, Node nodeFrom, Node nodeTo) {
        return g.existsDirectedPathFromTo(nodeFrom, nodeTo);
    }

    //Returns the definite ancestors of nodeTo.
    //Ancestor relationships are transitive over all graphs.
    private List<Node> getDefiniteAncestors(Node nodeTo) {
        List<Node> ancestors = new ArrayList<Node>();

        for (int i = 0; i < n; i++) {
            Graph g = Gsub[i];
            List<Node> vars = g.getNodes();
            if (!vars.contains(nodeTo)) {
                continue;
            }
            for (Node v : vars) {
                if (g.getEndpoint(v, nodeTo) == ARROW &&
                        g.getEndpoint(nodeTo, v) == NULL) {
                    ancestors.add(v);
                }
            }
        }

        int numAnc = ancestors.size();
        for (int i = 0; i < numAnc; i++) {
            Node ancestor = ancestors.get(i);
            List<Node> ancestorAncestors = getDefiniteAncestors(ancestor);

            for (Node aa : ancestorAncestors) {
                if (!ancestors.contains(aa)) {
                    ancestors.add(aa);
                    numAnc++;
                }
            }
        }

        return ancestors;
    }

    private List<Node> getVarsOnTreks(Graph g, Node nodex, Node nodey) {
        List<Node> varT = new ArrayList<Node>();

        //Find every path between nodex and nodey that does not contain a
        //definite collider.
        List<Node> adjacentToX = g.getAdjacentNodes(nodex);

        for (Node adjNode : adjacentToX) {
            List<Node> currentPath = new ArrayList<Node>();
            currentPath.add(nodex);

            if (adjNode == nodey) {
                continue;
            }

            if (!currentPath.contains(adjNode)) {
                currentPath.add(adjNode);
            }
            buildPath(g, currentPath, varT, nodey);
        }

        varT.remove(nodex);

        return varT;
    }

    private void buildPath(Graph g, List<Node> currentPath,
                           List<Node> onPotTrek, Node endPoint) {

        int last = currentPath.size() - 1;
        Node currentLast = (Node) currentPath.get(last);

        List<Node> adjacentToCurrentLast = g.getAdjacentNodes(currentLast);

        for (Node nextNode : adjacentToCurrentLast) {
            if (currentPath.contains(nextNode)) {
                continue;
            }

            //If currentLast is a collider between the edge before it on currentPath
            //and nextNode, don't add it to currentPath.
            if (g.getEndpoint((Node) currentPath.get(last - 1), currentLast) ==
                    ARROW && g.getEndpoint(currentLast, nextNode) == ARROW) {
                continue;
            }

            if (nextNode == endPoint) {
                for (Node cpNode : currentPath) {
                    if (!onPotTrek.contains(cpNode)) {
                        onPotTrek.add(cpNode);
                    }
                }
            } else {
                currentPath.add(nextNode);
                buildPath(g, currentPath, onPotTrek, endPoint);
            }
        }

        currentPath.remove(currentLast);
    }

    //Is nodeFrom a potential ancestor of NodeTo?
    private boolean isPotentialAncestor(Graph g, Node nodeFrom, Node nodeTo) {
        Set<Node> s = new HashSet<Node>();
        return potentialPathFind(s, g, nodeFrom, nodeTo);
    }

    private boolean potentialPathFind(Set<Node> s, Graph g, Node nodeFrom,
                                      Node nodeTo) {

        if (nodeFrom == nodeTo) {
            return false;
        }

        if (g.getEndpoint(nodeFrom, nodeTo) != NONE &&
                g.getEndpoint(nodeTo, nodeFrom) != ARROW &&
                g.getEndpoint(nodeTo, nodeFrom) != NONE) {
            return true;
        }

        s.add(nodeFrom);

        for (Iterator<Node> i =
                (g.getAdjacentNodes(nodeFrom)).iterator(); i.hasNext();) {
            Node o = i.next();
            if ((!s.contains(o)) && (g.getEndpoint(nodeFrom, o) != NONE) &&
                    (g.getEndpoint(o, nodeFrom) != ARROW) &&
                    (g.getEndpoint(o, nodeFrom) != NONE)) {
                if (potentialPathFind(s, g, o, nodeTo)) {
                    return true;
                }
            }
        }

        return false;
    }
}


