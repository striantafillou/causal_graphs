package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Triple;

import java.util.List;
import java.util.Set;

/**
 * Specifies what counts as a legal pair of edges X---Y---Z for purposes of
 * calculating possible d-separation sets for the FCI algorithm. In this case,
 * legal initial edges are those adjacent to initial nodes, and legal pairs of
 * edges are those for which either X-->Y<--Z or X is adjacent to Z--i.e. X, Y,
 * and Z form a triangle. (It is assumed (and checked) that is adjacent to Y and
 * Y is adjacent to Z.)
 *
 * @author Joseph Ramsey
 */
class FciDsepLegalPairs2 implements LegalPairs {

    /**
     * Graph with respect to which graph properties are tested.
     */
    private Graph graph;
    private Set<Triple> ambiguousTriples;

    /**
     * Constructs a new legal pairs object. See class level doc.
     *
     * @param graph The graph with respect to which legal pairs will be tested.
     */
    public FciDsepLegalPairs2(Graph graph, Set<Triple> unfaithfulTriples) {
        if (graph == null) {
            throw new NullPointerException();
        }

        this.graph = graph;
        this.ambiguousTriples = unfaithfulTriples;
    }

    /**
     * Returns true iff x is adjacent to y.
     */
    public boolean isLegalFirstEdge(Node x, Node y) {
        return this.graph.isAdjacentTo(x, y);
    }

    /**
     * Returns true iff x-->y<--z or else x is adjacent to z.
     *
     * @throws IllegalArgumentException if x is not adjacent to y or y is not
     *                                  adjacent to z.
     */
    public boolean isLegalPair(Node x, Node y, Node z, List<Node> c,
                               List<Node> d) {
        if (!(graph.isAdjacentTo(x, y)) || !(graph.isAdjacentTo(y, z))) {
            throw new IllegalArgumentException();
        }

        if (graph.isDefiniteCollider(x, y, z)) {
            return true;
        }

//        if (graph.getEndpoint(x, y) == Endpoint.TAIL || graph.getEndpoint(z, y) == Endpoint.TAIL) {
//            return false;
//        }

        if (ambiguousTriples.contains(new Triple(x, y, z))) {
            return true;
        }

        return graph.isAdjacentTo(x, z);
    }
}
