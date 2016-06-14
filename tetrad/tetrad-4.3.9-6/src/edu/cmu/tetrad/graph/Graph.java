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

import edu.cmu.tetrad.util.TetradSerializable;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;

/**
 * <p>Implements a graph capable of storing edges of type N1 *-# N2 where * and
 * # are endpoints of type Endpoint.</p> <p>We stipulate by extending
 * TetradSerializable that all graphs implementing this interface are
 * serializable. This is because for Tetrad they must be serializable. (For
 * randomUtil, in order to be able to cancel operations, they must be
 * serializable.)</p>
 *
 * @author Joseph Ramsey
 * @see Endpoint
 */
public interface Graph extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Adds a bidirected edges <-> to the graph.
     */
    boolean addBidirectedEdge(Node node1, Node node2);

    /**
     * Adds a directed edge --> to the graph.
     */
    boolean addDirectedEdge(Node node1, Node node2);

    /**
     * Adds an undirected edge --- to the graph.
     */
    boolean addUndirectedEdge(Node node1, Node node2);

    /**
     * Adds an nondirected edges o-o to the graph.
     */
    boolean addNondirectedEdge(Node node1, Node node2);

    /**
     * Adds a partially oriented edge o-> to the graph.
     */
    boolean addPartiallyOrientedEdge(Node node1, Node node2);

    /**
     * Adds the specified edge to the graph, provided it is not already in the
     * graph.
     *
     * @return true if the edge was added, false if not.
     */
    boolean addEdge(Edge edge);

    /**
     * Adds a graph constraint.
     *
     * @return true if the constraint was added, false if not.
     */
    boolean addGraphConstraint(GraphConstraint gc);

    /**
     * Adds a node to the graph. Precondition: The proposed name of the node
     * cannot already be used by any other node in the same graph.
     *
     * @return true if nodes were added, false if not.
     */
    boolean addNode(Node node);

    /**
     * Adds a PropertyChangeListener to the graph.
     */
    void addPropertyChangeListener(PropertyChangeListener e);

    /**
     * Removes all nodes (and therefore all edges) from the graph.
     */
    void clear();

    /**
     * Determines whether this graph contains the given edge.
     *
     * @return true iff the graph contain 'edge'.
     */
    boolean containsEdge(Edge edge);

    /**
     * Determines whether this graph contains the given node.
     *
     * @return true iff the graph contains 'node'.
     */
    boolean containsNode(Node node);

    /**
     * Returns true iff there is a directed cycle in the graph.
     */
    boolean existsDirectedCycle();

    /**
     * Returns true iff there is a directed path from node1 to node2 in the
     * graph.
     */
    boolean existsDirectedPathFromTo(Node node1, Node node2);

    /**
     * @return true iff there is a semi-directed path from node1 to something in
     *         nodes2 in the graph
     */
    boolean existsUndirectedPathFromTo(Node node1, Node node2);

    /**
     * </p> A semi-directed path from A to B is an undirected path in which no
     * edge has an arrowhead pointing "back" towards A.
     *
     * @return true iff there is a semi-directed path from node1 to something in
     *         nodes2 in the graph
     */
    boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes);

    /**
     * Determines whether an inducing path exists between node1 and node2, given
     * a set O of observed nodes and a set sem of conditioned nodes.
     *
     * @param node1             the first node.
     * @param node2             the second node.
     * @param observedNodes     the set of observed nodes.
     * @param conditioningNodes the set of nodes conditioned upon.
     * @return true if an inducing path exists, false if not.
     */
    boolean existsInducingPath(Node node1, Node node2, Set<Node> observedNodes,
            Set<Node> conditioningNodes);

    /**
     * Returns true iff a trek exists between two nodes in the graph.  A trek
     * exists if there is a directed path between the two nodes or else, for
     * some third node in the graph, there is a path to each of the two nodes in
     * question.
     */
    boolean existsTrek(Node node1, Node node2);

    /**
     * Determines whether this graph is equal to some other graph, in the sense
     * that they contain the same nodes and the sets of edges defined over these
     * nodes in the two graphs are isomorphic typewise. That is, if node A and B
     * exist in both graphs, and if there are, e.g., three edges between A and B
     * in the first graph, two of which are directed edges and one of which is
     * an undirected edge, then in the second graph there must also be two
     * directed edges and one undirected edge between nodes A and B.
     */
    boolean equals(Object o);

    /**
     * Removes all edges from the graph and fully connects it using #-# edges,
     * where # is the given endpoint.
     */
    void fullyConnect(Endpoint endpoint);

    /**
     * Reorients all edges in the graph with the given endpoint.
     */
    void reorientAllWith(Endpoint endpoint);

    /**
     * Returns a mutable list of nodes adjacent to the given node.
     */
    List<Node> getAdjacentNodes(Node node);

    /**
     * Returns a mutable list of ancestors for the given nodes.
     */
    List<Node> getAncestors(List<Node> nodes);

    /**
     * Returns a mutable list of children for a node.
     */
    List<Node> getChildren(Node node);

    /**
     * Returns the connectivity of the graph.
     */
    int getConnectivity();

    /**
     * Returns a mutable list of descendants for the given nodes.
     */
    List<Node> getDescendants(List<Node> nodes);

    /**
     * Returns the edge connecting node1 and node2, provided a unique such edge
     * exists.
     *
     * @throws UnsupportedOperationException if the graph allows multiple edges
     *                                       between node pairs.
     */
    Edge getEdge(Node node1, Node node2);

    /**
     * Returns the directed edge from node1 to node2, if there is one.
     *
     * @throws UnsupportedOperationException if the graph allows multiple edges
     *                                       between node pairs.
     */
    Edge getDirectedEdge(Node node1, Node node2);

    /**
     * Returns the list of edges connected to a particular node. No particular
     * ordering of the edges in the list is guaranteed.
     */
    List<Edge> getEdges(Node node);

    /**
     * Returns the edges connecting node1 and node2.
     */
    List<Edge> getEdges(Node node1, Node node2);

    /**
     * Returns the list of edges in the graph.  No particular ordering of the
     * edges in the list is guaranteed.
     */
    List<Edge> getEdges();

    /**
     * Returns the endpoint along the edge from node to node2 at the node2 end.
     */
    Endpoint getEndpoint(Node node1, Node node2);

    /**
     * Returns a matrix of endpoints for the nodes in this graph, with nodes in
     * the same order as getNodes().
     */
    Endpoint[][] getEndpointMatrix();

    /**
     * Returns the list of graph constraints for this graph.
     */
    List<GraphConstraint> getGraphConstraints();

    /**
     * Returns the number of arrow endpoints adjacent to a node.
     */
    int getIndegree(Node node);

    /**
     * Returns the node with the given string name.  In case of accidental
     * duplicates, the first node encountered with the given name is returned.
     * In case no node exists with the given name, null is returned.
     */
    Node getNode(String name);

    /**
     * Returns the list of nodes for the graph.
     */
    List<Node> getNodes();

    /**
     * Returns the number of edges in the (entire) graph.
     */
    int getNumEdges();

    /**
     * Returns the number of edges in the graph which are connected to a
     * particular node.
     */
    int getNumEdges(Node node);

    /**
     * Returns the number of nodes in the graph.
     */
    int getNumNodes();

    /**
     * Returns the number of null endpoints adjacent to an edge.
     */
    int getOutdegree(Node node);

    /**
     * Returns the list of parents for a node.
     */
    List<Node> getParents(Node node);

    /**
     * Returns true iff node1 is adjacent to node2 in the graph.
     */
    boolean isAdjacentTo(Node node1, Node node2);

    /**
     * Determines whether one node is an ancestor of another.
     */
    boolean isAncestorOf(Node node1, Node node2);

    /**
     * added by ekorber, 2004/06/12
     *
     * @return true if node1 is a possible ancestor of node2.
     */
    boolean possibleAncestor(Node node1, Node node2);

    /**
     * Returns true iff node1 is a child of node2 in the graph.
     */
    boolean isChildOf(Node node1, Node node2);

    /**
     * Determines whether node1 is a parent of node2.
     */
    boolean isParentOf(Node node1, Node node2);

    /**
     * Determines whether one node is a proper ancestor of another.
     */
    boolean isProperAncestorOf(Node node1, Node node2);

    /**
     * Determines whether one node is a proper decendent of another.
     */
    boolean isProperDescendentOf(Node node1, Node node2);

    /**
     * Returns true iff node1 is a (non-proper) descendant of node2.
     */
    boolean isDescendentOf(Node node1, Node node2);

    /**
     * A node Y is a definite nondescendent of a node X just in case there is no
     * semi-directed path from X to Y.
     * <p/>
     * added by ekorber, 2004/06/12.
     *
     * @return true if node 2 is a definite nondecendent of node 1
     */
    boolean defNonDescendent(Node node1, Node node2);

    /**
     * Added by ekorber, 2004/6/9.
     *
     * @return true if node 2 is a definite noncollider between 1 and 3
     */
    boolean isDefiniteNoncollider(Node node1, Node node2, Node node3);

    /**
     * Added by ekorber, 2004/6/9.
     *
     * @return true if node 2 is a definite collider between 1 and 3
     */
    boolean isDefiniteCollider(Node node1, Node node2, Node node3);

    /**
     * Determines whether one node is d-connected to another. According to
     * Spirtes, Richardson & Meek, two nodes are d- connected given some
     * conditioning set Z if there is an acyclic undirected path U between them,
     * such that every collider on U is an ancestor of some element in Z and
     * every non-collider on U is not in Z.  Two elements are d-separated just
     * in case they are not d-connected.  A collider is a node which two edges
     * hold in common for which the endpoints leading into the node are both
     * arrow endpoints.
     */
    boolean isDConnectedTo(Node node1, Node node2, List<Node> z);

    /**
     * Determines whether one node is d-separated from another. Two elements are   E
     * d-separated just in case they are not d-connected.
     */
    boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z);

    /**
     * Determines if nodes 1 and 2 are possibly d-connected given conditioning
     * set z.  A path U is possibly-d-connecting if every definite collider on U
     * is a possible ancestor of a node in z and every definite non-collider is
     * not in z.
     * <p/>
     * added by ekorber, 2004/06/15.
     *
     * @return true iff nodes 1 and 2 are possibly d-connected given z
     */
    boolean possDConnectedTo(Node node1, Node node2, List<Node> z);

    /**
     * Returns true iff there is a single directed edge from node1 to node2 in
     * the graph.
     */
    boolean isDirectedFromTo(Node node1, Node node2);

    /**
     * Returns true iff there is a single undirected edge from node1 to node2 in
     * the graph.
     */
    boolean isUndirectedFromTo(Node node1, Node node2);

    /**
     * A directed edge A->B is definitely visible if there is a node C not
     * adjacent to B such that C*->A is in the PAG. Added by ekorber,
     * 2004/06/11.
     *
     * @return true if the given edge is definitely visible (Jiji, pg 25)
     * @throws IllegalArgumentException if the given edge is not a directed edge
     *                                  in the graph
     */
    boolean defVisible(Edge edge);

    /**
     * Returns true iff the given node is exogenous in the graph.
     */
    boolean isExogenous(Node node);

    /**
     * Nodes adjacent to the given node with the given proximal endpoint.
     */
    List<Node> getNodesInTo(Node node, Endpoint n);

    /**
     * Nodes adjacent to the given node with the given distal endpoint.
     */
    List<Node> getNodesOutTo(Node node, Endpoint n);

    /**
     * Removes the given edge from the graph.
     *
     * @return true if the edge was removed, false if not.
     */
    boolean removeEdge(Edge edge);

    /**
     * Removes the edge connecting the two given nodes, provided there is
     * exactly one such edge.
     *
     * @throws UnsupportedOperationException if multiple edges between node
     *                                       pairs are not supported.
     */
    boolean removeEdge(Node node1, Node node2);

    /**
     * Removes all edges connecting node A to node B.  In most cases, this will
     * remove at most one edge, but since multiple edges are permitted in some
     * graph implementations, the number will in some cases be greater than
     * one.
     *
     * @return true if edges were removed, false if not.
     */
    boolean removeEdges(Node node1, Node node2);

    /**
     * Iterates through the list and removes any permissible edges found.  The
     * order in which edges are added is the order in which they are presented
     * in the iterator.
     *
     * @return true if edges were added, false if not.
     */
    boolean removeEdges(List<Edge> edges);

    /**
     * Removes a node from the graph.
     *
     * @return true if the node was removed, false if not.
     */
    boolean removeNode(Node node);

    /**
     * Iterates through the list and removes any permissible nodes found.  The
     * order in which nodes are removed is the order in which they are presented
     * in the iterator.
     *
     * @return true if nodes were added, false if not.
     */
    boolean removeNodes(List<Node> nodes);

    /**
     * Sets the endpoint type at the 'to' end of the edge from 'from' to 'to' to
     * the given endpoint.  Note: NOT CONSTRAINT SAFE
     */
    boolean setEndpoint(Node from, Node to, Endpoint endPoint);

    /**
     * Returns true iff graph constraints will be checked for future graph
     * modifications.
     */
    boolean isGraphConstraintsChecked();

    /**
     * Set whether graph constraints will be checked for future graph
     * modifications.
     */
    void setGraphConstraintsChecked(boolean checked);

    /**
     * Constructs and returns a subgraph consisting of a given subset of the
     * nodes of this graph together with the edges between them.
     */
    Graph subgraph(List<Node> nodes);

    /**
     * Returns a string representation of the graph.
     */
    String toString();

    /**
     * Transfers nodes and edges from one graph to another.  One way this is
     * used is to change graph types.  One constructs a new graph based on the
     * old graph, and this method is called to transfer the nodes and edges of
     * the old graph to the new graph.
     *
     * @param graph the graph from which nodes and edges are to be pilfered.
     * @throws java.lang.IllegalArgumentException
     *          This exception is thrown if adding some node or edge violates
     *          one of the basicConstraints of this graph.
     */
    void transferNodesAndEdges(Graph graph) throws IllegalArgumentException;

    /**
     * Marks the given triple with the given label.
     */
    void setAmbiguous(Triple triple, boolean ambiguous);

    /**
     * Marks the given triple with the given label.
     */
    void setAmbiguous(Pair pair, boolean ambiguous);

    /**
     * Returns true if the given triple was marked ambiguous.
     * @param x
     * @param y
     * @param z
     */
    boolean isAmbiguous(Node x, Node y, Node z);

    /**
     * Returns true if the given triple was marked ambiguous.
     * @param x
     * @param y
     */
    boolean isAmbiguous(Node x, Node y);

    /**
     * Returns the list of ambiguous triples associated with this graph.
     */
    Set<Triple> getAmbiguousTriples();

    Set<Pair> getAmbiguousPairs();

    Set<Triple> getUnderLineTriples();

    Set<Triple> getDottedUnderLineTriples();

    boolean isUnderlineTriple(Node x, Node y, Node z);

    void addUnderlineTriple(Node x, Node y, Node Z);

    void removeUnderlineTriple(Node x, Node y, Node z);

    void setUnderLineTriples(Set<Triple> triples);

    void setDottedUnderLineTriples(Set<Triple> triples);

    List<String> getNodeNames();
}


