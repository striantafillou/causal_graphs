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

package edu.cmu.tetrad.graph;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Represents a directed acyclic graph--that is, a graph containing only
 * directed edges, with no cycles. Variables are permitted to be either measured
 * or latent, with at most one edge per node pair, and no edges to self.
 *
 * @author Joseph Ramsey
 */
public final class Dag implements Graph {
    static final long serialVersionUID = 23L;

    /**
     * The constraints that the graph must satisfy.
     */
    private final static GraphConstraint[] constraints = {
            new MeasuredLatentOnly(), new AtMostOneEdgePerPair(),
            new NoEdgesToSelf(), new DirectedEdgesOnly(), new InArrowImpliesNonancestor()};

    /**
     * The wrapped graph.
     *
     * @serial
     */
    private final Graph graph;

    /**
     * A dpath matrix for the DAG. If used, it is updated (where necessary) each
     * time the getDirectedPath method is called with whatever edges are stored
     * in the dpathNewEdges list. New edges that are added are appended to the
     * dpathNewEdges list. When edges are removed and when nodes are added or
     * removed, dpath is set to null.
     */
    private transient int[][] dpath;

    /**
     * New edges that need to be added to the dpath matrix.
     */
    private transient LinkedList<Edge> dpathNewEdges = new LinkedList<Edge>();

    /**
     * The order of nodes used for dpath.
     */
    private transient List<Node> dpathNodes;

    //===============================CONSTRUCTORS=======================//

    /**
     * Constructs a new directed acyclic graph (DAG).
     */
    public Dag() {
        this.graph = new EdgeListGraph();
        setGraphConstraintsChecked(true);
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }
    }

    public Dag(List<Node> nodes) {
        this.graph = new EdgeListGraph(nodes);
        setGraphConstraintsChecked(true);
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }
    }

    /**
     * Constructs a new directed acyclic graph from the given graph object.
     *
     * @param graph the graph to base the new DAG on.
     * @throws IllegalArgumentException if the given graph cannot for some
     *                                  reason be converted into a DAG.
     */
    public Dag(Graph graph) throws IllegalArgumentException {
        this.graph = new EdgeListGraph();

        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }

        transferNodesAndEdges(graph);
        resetDPath();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Dag serializableInstance() {
        Dag dag = new Dag();
        GraphNode node1 = new GraphNode("X");
        dag.addNode(node1);
        return dag;
    }

    //===============================PUBLIC METHODS======================//

    public boolean addBidirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    public boolean addEdge(Edge edge) {
        reconstituteDpath();
        Node _node1 = Edges.getDirectedEdgeTail(edge);
        Node _node2 = Edges.getDirectedEdgeHead(edge);

        int _index1 = dpathNodes.indexOf(_node1);
        int _index2 = dpathNodes.indexOf(_node2);

        if (dpath[_index2][_index1] == 1) {
            return false;
        }

        boolean added = getGraph().addEdge(edge);

        if (added) {
            dpathNewEdges().add(edge);
        }

        return added;
    }

    public boolean addDirectedEdge(Node node1, Node node2) {
        return addEdge(Edges.directedEdge(node1, node2));
    }

    public boolean addGraphConstraint(GraphConstraint gc) {
        return getGraph().addGraphConstraint(gc);
    }

    public boolean addPartiallyOrientedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    public boolean addNode(Node node) {
        boolean added = getGraph().addNode(node);

        if (added) {
            resetDPath();
        }

        return added;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getGraph().addPropertyChangeListener(l);
    }

    public boolean addUndirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    public boolean addNondirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        getGraph().clear();
    }

    public boolean containsEdge(Edge edge) {
        return getGraph().containsEdge(edge);
    }

    public boolean containsNode(Node node) {
        return getGraph().containsNode(node);
    }

    public boolean defNonDescendent(Node node1, Node node2) {
        return getGraph().defNonDescendent(node1, node2);
    }

    public boolean existsDirectedCycle() {
        return false;
    }

    public boolean defVisible(Edge edge) {
        return getGraph().defVisible(edge);
    }

    public boolean isDefiniteNoncollider(Node node1, Node node2, Node node3) {
        return getGraph().isDefiniteNoncollider(node1, node2, node3);
    }

    public boolean isDefiniteCollider(Node node1, Node node2, Node node3) {
        return getGraph().isDefiniteCollider(node1, node2, node3);
    }

    public boolean existsTrek(Node node1, Node node2) {
        return getGraph().existsTrek(node1, node2);
    }

    public boolean equals(Object o) {
        return getGraph().equals(o);
    }

    public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        resetDPath();
        reconstituteDpath();

        //System.out.println(MatrixUtils.toString(dpath));

        int index1 = dpathNodes.indexOf(node1);
        int index2 = dpathNodes.indexOf(node2);

        return dpath[index1][index2] == 1;
    }

    public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return false;
    }


    public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes) {
        return getGraph().existsSemiDirectedPathFromTo(node1, nodes);
    }

    public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return getGraph().existsInducingPath(node1, node2, observedNodes,
                conditioningNodes);
    }

    public void fullyConnect(Endpoint endpoint) {
        throw new UnsupportedOperationException();
        //graph.fullyConnect(endpoint);
    }

    public Endpoint getEndpoint(Node node1, Node node2) {
        return getGraph().getEndpoint(node1, node2);
    }

    public Endpoint[][] getEndpointMatrix() {
        return getGraph().getEndpointMatrix();
    }

    public List<Node> getAdjacentNodes(Node node) {
        return getGraph().getAdjacentNodes(node);
    }

    public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        return getGraph().getNodesInTo(node, endpoint);
    }

    public List<Node> getNodesOutTo(Node node, Endpoint n) {
        return getGraph().getNodesOutTo(node, n);
    }

    public List<Node> getNodes() {
        return getGraph().getNodes();
    }

    public List<Edge> getEdges() {
        return getGraph().getEdges();
    }

    public List<Edge> getEdges(Node node) {
        return getGraph().getEdges(node);
    }

    public List<Edge> getEdges(Node node1, Node node2) {
        return getGraph().getEdges(node1, node2);
    }

    public Node getNode(String name) {
        return getGraph().getNode(name);
    }

    public int getNumEdges() {
        return getGraph().getNumEdges();
    }

    public int getNumNodes() {
        return getGraph().getNumNodes();
    }

    public int getNumEdges(Node node) {
        return getGraph().getNumEdges(node);
    }

    public List<GraphConstraint> getGraphConstraints() {
        return getGraph().getGraphConstraints();
    }

    /**
     * Finds the set of nodes which have no children, followed by the set of
     * their parents, then the set of the parents' parents, and so on.  The
     * result is returned as a List of Lists.
     *
     * @return the tiers of this digraph.
     * @see #printTiers
     */
    public List<List<Node>> getTiers() {
        Set<Node> found = new HashSet<Node>();
        Set<Node> notFound = new HashSet<Node>();
        List<List<Node>> tiers = new LinkedList<List<Node>>();

        // first copy all the nodes into 'notFound'.
        for (Node node1 : getNodes()) {
            notFound.add(node1);
        }

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // current tier.
        while (!notFound.isEmpty()) {
            List<Node> thisTier = new LinkedList<Node>();

            for (Node node : notFound) {
                if (found.containsAll(getParents(node))) {
                    thisTier.add(node);
                }
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            notFound.removeAll(thisTier);
            found.addAll(thisTier);

            // add the current tier to the list of tiers.
            tiers.add(thisTier);
        }

        return tiers;
    }

    public List<Node> getChildren(Node node) {
        return getGraph().getChildren(node);
    }

    public int getConnectivity() {
        return getGraph().getConnectivity();
    }

    public List<Node> getDescendants(List<Node> nodes) {
        return getGraph().getDescendants(nodes);
    }

    public Edge getEdge(Node node1, Node node2) {
        return getGraph().getEdge(node1, node2);
    }

    public Edge getDirectedEdge(Node node1, Node node2) {
        return getGraph().getDirectedEdge(node1, node2);
    }

    public List<Node> getParents(Node node) {
        return getGraph().getParents(node);
    }

    public int getIndegree(Node node) {
        return getGraph().getIndegree(node);
    }

    public int getOutdegree(Node node) {
        return getGraph().getOutdegree(node);
    }

    /**
     * This method returns the nodes of a digraph in such an order that as one
     * iterates through the list, the parents of each node have already been
     * encountered in the list.
     *
     * @return a tier ordering for the nodes in this graph.
     * @see #printTierOrdering
     */
    public List<Node> getTierOrdering() {
        List<Node> found = new LinkedList<Node>();
        Set<Node> notFound = new HashSet<Node>();

        for (Node node1 : getNodes()) {
            notFound.add(node1);
        }

        while (!notFound.isEmpty()) {
            for (Iterator<Node> it = notFound.iterator(); it.hasNext();) {
                Node node = it.next();

                if (found.containsAll(getParents(node))) {
                    found.add(node);
                    it.remove();
                }
            }
        }

        return found;
    }

    public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return getGraph().isAdjacentTo(nodeX, nodeY);
    }

    public boolean isAncestorOf(Node node1, Node node2) {
        return (node1 == node2) || isProperAncestorOf(node1, node2);
        //return graph.isAncestorOf(node1, node2);
    }

    public boolean isDirectedFromTo(Node node1, Node node2) {
        return getGraph().isDirectedFromTo(node1, node2);
    }

    public boolean isUndirectedFromTo(Node node1, Node node2) {
        return false;
    }

    public boolean isGraphConstraintsChecked() {
        return getGraph().isGraphConstraintsChecked();
    }

    public boolean isParentOf(Node node1, Node node2) {
        return getGraph().isParentOf(node1, node2);
    }

    public boolean isProperAncestorOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node1, node2);
        //return graph.isProperAncestorOf(node1, node2);
    }

    public boolean isProperDescendentOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node2, node1);
    }

    public boolean isExogenous(Node node) {
        return getGraph().isExogenous(node);
    }

    public boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes) {
        return getGraph().isDConnectedTo(node1, node2, conditioningNodes);
    }

    public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return getGraph().isDSeparatedFrom(node1, node2, z);
    }

    public boolean isChildOf(Node node1, Node node2) {
        return getGraph().isChildOf(node1, node2);
    }

    public boolean isDescendentOf(Node node1, Node node2) {
        return (node1 == node2) || isProperDescendentOf(node1, node2);
        //return graph.isDescendentOf(node1, node2);
    }

    /**
     * Prints the tiers found by method getTiers() to System.out.
     *
     * @param out the printstream to sent output to.
     * @see #getTiers
     */
    public void printTiers(PrintStream out) {

        List<List<Node>> tiers = getTiers();

        System.out.println();

        for (List<Node> thisTier : tiers) {
            for (Node thisNode : thisTier) {
                out.print(thisNode + "\t");
            }

            out.println();
        }

        out.println("done");
    }

    /**
     * Prints the tier ordering found by method getTierOrdering() to
     * System.out.
     *
     * @see #getTierOrdering
     */
    public void printTierOrdering() {
        List<Node> v = getTierOrdering();

        System.out.println();

        for (Node aV : v) {
            System.out.print(aV + "\t");
        }

        System.out.println();
    }

    public boolean removeEdge(Node node1, Node node2) {
        boolean removed = getGraph().removeEdge(node1, node2);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    public boolean removeEdges(Node node1, Node node2) {
        boolean removed = getGraph().removeEdges(node1, node2);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    public boolean setEndpoint(Node node1, Node node2, Endpoint endpoint) {
        boolean ret = getGraph().setEndpoint(node1, node2, endpoint);

        resetDPath();

        return ret;
    }

    public Graph subgraph(List<Node> nodes) {
        return getGraph().subgraph(nodes);
    }

    public void setGraphConstraintsChecked(boolean checked) {
        getGraph().setGraphConstraintsChecked(checked);
    }

    public boolean removeEdge(Edge edge) {
        boolean removed = getGraph().removeEdge(edge);
        resetDPath();
        return removed;
    }

    public boolean removeEdges(List<Edge> edges) {
        boolean change = false;

        for (Edge edge : edges) {
            boolean _change = removeEdge(edge);
            change = change || _change;
        }

        return change;

        //return graph.removeEdges(edges);
    }

    public boolean removeNode(Node node) {
        boolean removed = getGraph().removeNode(node);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    public boolean removeNodes(List<Node> nodes) {
        return getGraph().removeNodes(nodes);
    }

    public void reorientAllWith(Endpoint endpoint) {
        throw new UnsupportedOperationException();
        //graph.reorientAllWith(endpoint);
    }

    public boolean possibleAncestor(Node node1, Node node2) {
        return getGraph().possibleAncestor(node1, node2);
    }

    public List<Node> getAncestors(List<Node> nodes) {
        return getGraph().getAncestors(nodes);
    }

    public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return getGraph().possDConnectedTo(node1, node2, z);
    }

    private void resetDPath() {
        dpath = null;
        dpathNewEdges().clear();
        dpathNewEdges().addAll(getEdges());
    }

    private void reconstituteDpath() {
        if (dpath == null) {
            dpathNodes = getNodes();
            int numNodes = dpathNodes.size();
            dpath = new int[numNodes][numNodes];
        }

        while (!dpathNewEdges().isEmpty()) {
            Edge edge = dpathNewEdges().removeFirst();
            Node _node1 = Edges.getDirectedEdgeTail(edge);
            Node _node2 = Edges.getDirectedEdgeHead(edge);
            int _index1 = dpathNodes.indexOf(_node1);
            int _index2 = dpathNodes.indexOf(_node2);
            dpath[_index1][_index2] = 1;

            for (int i = 0; i < dpathNodes.size(); i++) {
                if (dpath[i][_index1] == 1) {
                    dpath[i][_index2] = 1;
                }

                if (dpath[_index2][i] == 1) {
                    dpath[_index1][i] = 1;
                }
            }
        }
    }

    public final void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException {
        this.getGraph().transferNodesAndEdges(graph);
    }

    public void setAmbiguous(Triple triple, boolean ambiguous) {
        getGraph().setAmbiguous(triple, true);
    }

    public void setAmbiguous(Pair pair, boolean ambiguous) {
        getGraph().setAmbiguous(pair, ambiguous);
    }

    public boolean isAmbiguous(Node x, Node y, Node z) {
        return getGraph().isAmbiguous(x, y, z);
    }

    public boolean isAmbiguous(Node x, Node y) {
        return getGraph().isAmbiguous(x, y);
    }

    public Set<Triple> getAmbiguousTriples() {
        return getGraph().getAmbiguousTriples();
    }

    public Set<Pair> getAmbiguousPairs() {
        return getGraph().getAmbiguousPairs();
    }

    /**
     * Returns the list of underline triples for the PAG computed by the search
     * method.
     */
    public Set<Triple> getUnderLineTriples() {
        return getGraph().getUnderLineTriples();
    }

    /**
     * Returns the list of dotted underline triples for the PAG computed by the
     * search method.
     */
    public Set<Triple> getDottedUnderLineTriples() {
        return getGraph().getDottedUnderLineTriples();
    }


    /**
     * States whether x-y-x is an underline triple or not.
     */
    public boolean isUnderlineTriple(Node x, Node y, Node z){
        return getGraph().isUnderlineTriple(x, y, z);
    }

    /**
     * Adds an underline triple.
     */
    public void addUnderlineTriple(Node x, Node y, Node z){
        getGraph().addUnderlineTriple(x, y, z);
    }

    /**
     * Removes an underline triple.
     */
    public void removeUnderlineTriple(Node x, Node y, Node z) {
        getGraph().removeUnderlineTriple(x, y, z);
    }


    public void setUnderLineTriples(Set<Triple> triples) {
        getGraph().setUnderLineTriples(triples);
    }


    public void setDottedUnderLineTriples(Set<Triple> triples) {
        getGraph().setDottedUnderLineTriples(triples);
    }

    public List<String> getNodeNames() {
        return getGraph().getNodeNames();
    }

    public String toString() {
        return getGraph().toString();
    }

    private LinkedList<Edge> dpathNewEdges() {
        if (dpathNewEdges == null) {
            dpathNewEdges = new LinkedList<Edge>();
        }
        return dpathNewEdges;
    }

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

        if (getGraph() == null) {
            throw new NullPointerException();
        }
    }

    //Gustavo 5 May 2007
    //this returns the nodes that have zero parents
    //  
    //should we use getTiers() instead?
	public List<Node> getExogenousTerms() {
		List<Node> errorTerms = new Vector();

		List<Node> nodes = getNodes();
		for (int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			if (getParents(node).isEmpty())
				errorTerms.add(node);
		}
		
		return errorTerms;
	}

    private Graph getGraph() {
        return graph;
    }

    public static boolean isDag(Graph graph) {
        try {
            new Dag(graph);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}


