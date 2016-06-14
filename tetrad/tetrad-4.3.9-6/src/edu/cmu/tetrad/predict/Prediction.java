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

package edu.cmu.tetrad.predict;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;


/**
 * Implements the Prediction Algorithm from Chapter 7 (2000) of Spirtes,
 * Glymour, and Scheines, "Causation, Prediction, and Search".
 * <p/>
 * NOTE: THIS IMPLEMENTATION IS CURRENTLY UNDER DEVELOPMENT. PLEASE DO NOT USE.
 * JDRAMSEY 2005/2/7
 *
 * @author Erin Korber.   July 2004
 */
public final class Prediction implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The PAG from the FCI search
     *
     * @serial
     */
    private Graph pag;

    /**
     * Eventually, this will be more generic.
     *
     * @serial
     */
    private DataSet dataSet;

    /**
     * Will eventually be able to be a set  .
     *
     * @serial
     */
    private ManipulatedVariable manipulatedVariable;

    /**
     * @serial
     */
    private Set zSet;

    /**
     * Eventually a set. (Note to Erin: When this becomes a set, call it 'ySet';
     * otherwise, old sessions won't load.)
     *
     * @serial
     */
    private Node yNode;

    //=============================CONSTRUCTOR===========================//

    public Prediction(Graph pag, DataSet dataContinuous,
                      ManipulatedVariable manipulated, Node pred, Set condSet) {

        if (pag == null) {
            throw new IllegalArgumentException("PAG must not be null");
        }

        if (dataContinuous == null) {
            throw new IllegalArgumentException("PAG must not be null");
        }

        this.pag = pag;
        this.dataSet = dataContinuous;
        this.manipulatedVariable = manipulated;
        this.zSet = condSet;
        this.yNode = pred;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Prediction serializableInstance() {
        return new Prediction(EdgeListGraph.serializableInstance(),
                DataUtils.continuousSerializableInstance(),
                ManipulatedVariable.serializableInstance(),
                GraphNode.serializableInstance(), new HashSet());
    }

    //=============================PUBLIC METHODS========================//

    public Graph getPag() {
        return pag;
    }

    //returns a string at the moment as no sensible error results for double
    //In general, this probably ought to be returning a marginal dist., but that
    //   would be silly now because of all the simplifying assumptions.
    public double predict() {

        //initial sanity checks
        if (zSet.contains(yNode) || manipulatedVariable.equals(yNode)) {
            throw new IllegalStateException(
                    "yNode,zSet not disjoint or X = yNode");
        }

        Set<Node> Yset = Collections.singleton(
                yNode);     //many things written expecting a set for later ease of expansion

        OrderingGenerator O = new OrderingGenerator(pag);
        List orders = O.getOrders();

        for (Iterator it = orders.iterator(); it.hasNext();) {
            LinkedList currOrd = (LinkedList) it.next();
            Dag currImap = genImap(currOrd);

            if (invarianceTest(currImap)) {

                //if this is an acceptable imap, form the subgraph among relevant variables
                Set<Node> subgraphSet = new HashSet<Node>();
                subgraphSet.addAll(IV(Yset, zSet, currImap));
                subgraphSet.addAll(IP(Yset, zSet, currImap));
                subgraphSet.add(yNode);
                subgraphSet.addAll(getParentsSet(currImap, subgraphSet));
                subgraphSet.add(manipulatedVariable.getNode());

                LinkedList<Edge> allEdges =
                        new LinkedList<Edge>(currImap.getEdges());
                while (allEdges.size() > 0) {
                    Edge e = allEdges.removeFirst();
                    if (!subgraphSet.contains(e.getNode1()) ||
                            !subgraphSet.contains(e.getNode2())) {
                        currImap.removeEdge(e);
                    }
                }

                //estimating the subgraph
                SemIm est = estimatedSemIm(currImap, dataSet);
                System.out.println("subgraph estimated");

                //set all edge coefs between X and its parents to 0 (break these edges)
                List<Node> Xparents =
                        currImap.getParents(manipulatedVariable.getNode());
                while (Xparents.size() > 0) {
                    try {
                        est.setParamValue(manipulatedVariable.getNode(),
                                Xparents.remove(0), 0);
                    }
                    catch (IllegalArgumentException e) {
                    }
                    //if no such param in model, doing nothing should work
                }

                //TODO: figure out how to make this work
                //Node exogForX = SemGraph.getExogenous(X.getNode());
                Node exogForX = manipulatedVariable.getNode();
                est.setParamValue(exogForX, exogForX,
                        manipulatedVariable.getVariance());

                //compute the new covariance matrix, and form submatrix of variables just in yNode and zSet
                DoubleMatrix2D implCovarC = est.getImplCovar();
                double[][] newCovMatrix = implCovarC.toArray();
                Set YuZ = zSet;
                YuZ.add(yNode);
                int currVarIndex;
                List<Node> orderedVars = est.getVariableNodes();

                List<Node> YZonly = new LinkedList<Node>(orderedVars);
                YZonly.retainAll(
                        YuZ);     //this should retain the ordering in orderedVars, right?

                //removing all rows/cols not corresponding to nodes in yNode or zSet
                LinkedList<Integer> integers = new LinkedList<Integer>();
                for (int i = 0; i < newCovMatrix.length; i++) {
                    integers.add(i);
                }

                //creates list of all integers not corresponding to an index of something in yNode or zSet
                for (Iterator yz = YuZ.iterator(); yz.hasNext();) {
                    currVarIndex = orderedVars.indexOf(yz.next());
                    integers.remove(currVarIndex);
                }

                while (integers.size() > 0) {
                    newCovMatrix = MatrixUtils.submatrix(newCovMatrix,
                            integers.removeFirst());
                }

                System.out.println("submatrix made");

                //calculating cov(yNode|zSet) using the new matrix
                int Yindex = YZonly.indexOf(yNode);
                double[] Yrow = newCovMatrix[Yindex];
                double[] Ycol = (MatrixUtils.transpose(newCovMatrix))[Yindex];
                double[][] ZsMatrix =
                        MatrixUtils.submatrix(newCovMatrix, Yindex);

                double sigmaYY = Yrow[Yindex];
                double sigmaZY = 0;
                double sigmaYZ = 0;
                for (int i = 0; i < Yrow.length; i++) {
                    if (i != Yindex) {
                        sigmaZY = sigmaZY + Yrow[i];
                    }
                }
                for (int i = 0; i < Ycol.length; i++) {
                    if (i != Yindex) {
                        sigmaYZ = sigmaYZ + Ycol[i];
                    }
                }

                double sigmaZZ = MatrixUtils.zSum(ZsMatrix);
                double covYcondZ =
                        sigmaYY - (sigmaYZ * (1 / sigmaZZ) * sigmaZY);

                //                return new Double(covYcondZ).toString();
                return covYcondZ;
            }

        }
        //        System.out.println("Prediction failed");
        //        return ("Prediction failed");

        return Double.NaN;
        //Remaining tasks:
        //link up interface
        //DEBUG

    }


    //Parents of V in the i-map are SP(V,Ord).
    //I don't think this is the most general possible algorithm; see CPS 177-8
    private Dag genImap(List Ord) {

        Dag resultImap = new Dag();

        for (int i = 0; i < Ord.size(); i++) {
            resultImap.addNode((Node) Ord.get(i));
        }

        for (Iterator it = Ord.iterator(); it.hasNext();) {
            Node n = (Node) it.next();
            Set<Node> sp = definiteSP(n, Ord);
            for (Iterator<Node> p = sp.iterator(); p.hasNext();) {
                Node parent = p.next();
                Edge e = new Edge(parent, n, Endpoint.TAIL, Endpoint.ARROW);
                resultImap.addEdge(e);
            }
        }
        return resultImap;
    }

    /**
     * Uses theorems 7.3 and 7.4 (CPS page 179) to determine if P(yNode|zSet) is
     * invariant under manipulation of X in the graph G.
     *
     * @return true if the desired invariance is achieved, false if not.
     */
    private boolean invarianceTest(Graph G) {
        Set Yset = Collections.singleton(yNode);
        Node x = this.manipulatedVariable.getNode();

        //all other disjointness conditions were taken care of already.
        // be careful with this when generalizing!

        //theorem 7.3:
        if (zSet.contains(x)) {
            return !possibly_IP(Yset, zSet, G).contains(x);

        } else {   //theorem 7.4
            return !possibly_IV(Yset, zSet, G).contains(x);
        }
    }

    private SemIm estimatedSemIm(Dag G, DataSet d) {
        SemPm sempm = new SemPm(G);
        SemEstimator est = new SemEstimator(d, sempm);
        est.estimate();
        return est.getEstimatedSem();
    }


    /**
     * Determines the set of possibly-IV nodes for yNode,zSet based on a graph
     * as per CPS page 178. The yNode,zSet disjoint check is skipped here as it
     * is done earlier in the algorithm.  Must be inserted if this is used for
     * another app!
     */
    private Set<Node> possibly_IV(Set Y, Set Z, Graph G) {
        Set<Node> result = new HashSet<Node>();
        //v not in zSet, poss dconn path from v to something in yNode given zSet, semidir path from manipulatedVariable to a member of YuZ

        LinkedList<Node> poss = new LinkedList<Node>(G.getNodes());
        poss.removeAll(Z);

        Set YuZ = new HashSet(Y);
        YuZ.addAll(Z);

        while (poss.size() > 0) {
            Node v = poss.removeFirst();
            if (possDConnectedToAltered(G, v, Y, Z, false) &&
                    existsSemiDirectedPathFromToSet(G, v, YuZ)) {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * Determines the set of possibly-IP nodes for yNode,zSet based on a graph G
     * as per CPS page 178. The yNode,zSet disjoint check is skipped here as it
     * is done earlier in the algorithm.  Must be inserted if this is used for
     * another app!
     */
    private Set<Node> possibly_IP(Set Y, Set Z, Graph G) {
        Set<Node> result = new HashSet<Node>();

        for (Iterator it = Z.iterator(); it.hasNext();) {
            Node v = (Node) it.next();
            Set ZminusV = new HashSet(Z);
            ZminusV.remove(v);
            if (possDConnectedToAltered(G, v, Y, ZminusV, true)) {
                result.add(v);
            }
        }
        return result;
    }


    private boolean existsSemiDirectedPathFromToSet(Graph G, Node node1,
                                                    Set nodes2) {
        return existsSemiDirectedPathVisit(G, node1, nodes2,
                new LinkedList<Node>());
    }

    private boolean existsSemiDirectedPathVisit(Graph G, Node node1, Set nodes2,
                                                LinkedList<Node> path) {
        path.addLast(node1);

        for (Iterator<Edge> it = G.getEdges(node1).iterator(); it.hasNext();) {
            Node child = Edges.traverseSemiDirected(node1, (it.next()));

            if (child == null) {
                continue;
            }

            if (nodes2.contains(child)) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsSemiDirectedPathVisit(G, child, nodes2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    //copied from Graph, but with some changes to make specific to app
    private boolean possDConnectedToAltered(Graph G, Node node1, Set nodes2,
                                            Set condNodes, boolean special_flag) {
        LinkedList<Node> allNodes = new LinkedList<Node>(G.getNodes());
        int sz = allNodes.size();
        int[][] edgeStage = new int[sz][sz];
        int stage = 1;

        int n1x = allNodes.indexOf(node1);
        edgeStage[n1x][n1x] = 1;

        List<int[]> currEdges;
        List<int[]> nextEdges = new LinkedList<int[]>();

        int[] temp1 = new int[2];
        temp1[0] = n1x;
        temp1[1] = n1x;
        nextEdges.add(temp1);

        STAGE:
        while (true) {
            currEdges = nextEdges;
            nextEdges = new LinkedList<int[]>();
            for (int i = 0; i < currEdges.size(); i++) {
                int[] edge = currEdges.get(i);
                Node center = allNodes.get(edge[1]);
                LinkedList<Node> adj =
                        new LinkedList<Node>(G.getAdjacentNodes(center));
                if (special_flag && center.equals(node1)) {     //paths out of node 1 not allowed for IP
                    adj.removeAll(G.getNodesInTo(center, Endpoint.TAIL));
                }

                ADJACENT:
                for (int j = 0; j < adj.size(); j++) {
                    // check if we've hit this edge before
                    int testIndex = allNodes.indexOf(adj.get(j));
                    if (edgeStage[edge[1]][testIndex] != 0) {
                        continue ADJACENT;
                    }

                    // if the edge pair violates possible dataSet-connection,
                    // then go to the next adjacent node.
                    //check this:  might be wonky on 1st step
                    Node X = allNodes.get(edge[0]);
                    Node Y = allNodes.get(edge[1]);
                    Node Z = allNodes.get(testIndex);

                    if (!((G.isDefiniteNoncollider(X, Y, Z) &&
                            !(condNodes.contains(Y))) || (
                            G.isDefiniteCollider(X, Y, Z) &&
                                    possibleAncestorSet(G, Y, condNodes)))) {
                        continue ADJACENT;
                    }

                    // if it gets here, then it's legal, so:
                    // (i) if this is one we want, we're done
                    if (nodes2.contains(adj.get(j))) {
                        return true;
                    }

                    // (ii) if we need to keep going,
                    // add the edge to the nextEdges list
                    int[] nextEdge = new int[2];
                    nextEdge[0] = edge[1];
                    nextEdge[1] = testIndex;
                    nextEdges.add(nextEdge);

                    // (iii) set the edgeStage array
                    edgeStage[edge[1]][testIndex] = stage;
                    edgeStage[testIndex][edge[1]] = stage;
                }
            }

            // find out if there's any reason to move to the next stage
            if (nextEdges.size() == 0) {
                break STAGE;
            }
            stage++;
        }

        return false;
    }

    private boolean possibleAncestorSet(Graph G, Node node1, Set nodes2) {
        for (Iterator it = nodes2.iterator(); it.hasNext();) {
            if (G.possibleAncestor(node1, (Node) it.next())) {
                return true;
            }
        }
        return false;
    }


    /**
     * A variable V is in IV(yNode,zSet) iff V is dataSet-connected to yNode
     * given zSet, and V is not in ND(YZ); ie, V has a descendant in yNode or
     * zSet. ND(YZ) is the set of all vertices with no decendant in yNode union
     * zSet.
     */
    private Set<Node> IV(Set<Node> Y, Set Z, Graph G) {
        Set<Node> result = new HashSet<Node>();

        /*  This check not needed here as it's done earlier in prediction.
            Must add if used for another app.

        if (intersection(yNode, zSet).size() != 0) {
            throw new IllegalArgumentException(
                    "Intersection of yNode and zSet nonempty");
        } */

        LinkedList<Node> allNodes = new LinkedList<Node>(G.getNodes());

        LinkedList<Node> possV = new LinkedList<Node>();

        while (allNodes.size() > 0) {
            Node curr = allNodes.removeFirst();

            List zlist = new LinkedList(Z);
            for (Iterator<Node> yit = Y.iterator(); yit.hasNext();) {
                Node nextY = yit.next();
                if (G.isDConnectedTo(curr, nextY, zlist)) {
                    possV.add(curr);
                }
            }
        }

        while (possV.size() > 0) {
            Node v = possV.removeFirst();
            List<Node> desc = G.getDescendants(Collections.singletonList(v));
            for (Iterator<Node> it = desc.iterator(); it.hasNext();) {
                Node n = it.next();
                if (Y.contains(n) || Z.contains(n)) {
                    result.add(v);
                    break;
                }

            }
        }

        return result;
    }

    /**
     * W is in IP(yNode,zSet) iff W is a member of zSet and W has a parent in
     * IV(yNode,zSet) union yNode.
     */
    private Set<Node> IP(Set<Node> Y, Set<Node> Z, Graph G) {
        Set<Node> result = new HashSet<Node>();

        Set<Node> ivYZ = IV(Y, Z, G);

        Set<Node> ivUy = new HashSet<Node>(ivYZ);
        ivUy.addAll(Y);

        for (Node aZ : Z) {
            Node possW = aZ;
            List<Node> parents = G.getParents(possW);
            parents.retainAll(ivUy);
            if (parents.size() != 0) {
                result.add(possW);
            }
        }

        return result;
    }

    //    /**
    //     * Given a node X and an ordering Ord (in List form), returns the set of
    //     * all nodes in Possible-SP(Ord,X).  A node V is in Possible-SP(Ord,X) iff
    //     * V!=X and there is an undirected path U in the pag P between V and X such
    //     * that every vertex on U is a predecessor of X in Ord, and no vertex on U
    //     * is a definite non-collider on U. (CPS pg 177).
    //     */
    //    private Set possibleSP(Node X, List Ord) {
    //        return generalSP(X, Ord, false);
    //    }

    /**
     * Given a node X and an ordering Ord (in List form), returns the set of all
     * nodes in Definite-SP(Ord,X).  A node V is in Definite-SP(Ord,X) iff V!=X
     * and there is an undirected path U in the pag P between V and X such that
     * every vertex on U is a predecessor of X in Ord, and every vertex on U is
     * a definite collider on U. (CPS pg 177).
     */
    private Set<Node> definiteSP(Node X, List Ord) {
        return generalSP(X, Ord, true);
    }

    //The boolean flag for definite/possible is there to minimize code duplication.
    private Set<Node> generalSP(Node X, List<Node> Ord, boolean defT_possF) {

        Set<Node> result = new HashSet<Node>();
        LinkedList<Node> reachable = new LinkedList<Node>();

        int xindex = Ord.indexOf(X);

        //Any node under consideration must be in this set,
        Set<Node> beforeX = new HashSet<Node>(Ord.subList(0, xindex));

        List<Node> step1 = pag.getAdjacentNodes(X);
        step1.retainAll(beforeX);

        //All nodes adjacent to X and before it in the ordering are
        // automatically reachable and in the SP set.
        result.addAll(step1);
        reachable.addAll(step1);

        //The nodes of the triple whose collider properties are being tested.
        Node one = X;
        Node currTwo;
        Node possThree;

        while (!reachable.isEmpty()) {
            currTwo = reachable.removeFirst();
            if (beforeX.contains(currTwo)) {        //redundant
                List<Node> adj = pag.getAdjacentNodes(currTwo);
                while (!adj.isEmpty()) {
                    possThree = adj.remove(0);
                    if (beforeX.contains(possThree) && ((!defT_possF &&
                            !pag.isDefiniteNoncollider(one, currTwo, possThree)) || (
                            defT_possF &&
                                    pag.isDefiniteCollider(one, currTwo, possThree)))) {
                        result.add(possThree);
                        reachable.add(possThree);
                    }
                }
            } else {
                System.out.println("This should never happen");
            }
            one = currTwo;
        }

        return result;
    }

    //    /* This generates a MAG from the PAG simply by converting circles into segments.
    //      This will not really work as a MAG must have only directed and bidirected
    //      edges, but the PAG might have some nondirected edges, which this would try to
    //      convert into an undirected edge.  The setEndpoint method is not
    //      constraint-safe, so the result might not actually be a MAG, even though it
    //      will have type Mag.  This is probably bad.
    //      TODO: fix that.
    //      Currently not being used as there are all sorts of problems generating it.
    //    */
    //    private Mag genMag() {
    //        Mag result = new Mag(pag);
    //        LinkedList edges = new LinkedList(result.getEdges());
    //        while (!edges.isEmpty()) {
    //            Edge curr = (Edge) edges.removeFirst();
    //            if (curr.getEndpoint1() == Endpoint.CIRCLE) {
    //                curr.setEndpoint1(Endpoint.TAIL);
    //            }
    //            if (curr.getEndpoint2() == Endpoint.CIRCLE) {
    //                curr.setEndpoint2(Endpoint.TAIL);
    //            }
    //        }
    //        return result;
    //    }

    //set of all parents of nodes in the given set

    private Set<Node> getParentsSet(Graph G, Set<Node> N) {
        Set<Node> result = new HashSet<Node>();
        for (Node aN : N) {
            result.addAll(G.getParents(aN));
        }
        return result;
    }

    //    private Set intersection(Set A, Set B) {
    //        Set inter = A;
    //        inter.retainAll(B);
    //        return inter;
    //    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (pag == null) {
            throw new NullPointerException();
        }

        if (dataSet == null) {
            throw new NullPointerException();
        }
    }

}

