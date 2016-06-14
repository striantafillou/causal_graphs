package edu.cmu.tetrad.graph;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Stores a triple (x, y, z) of nodes. Note that (x, y, z) = (z, y, x). Useful
 * for marking graphs.
 *
 * @author Joseph Ramsey
 */
public final class IndependenceFact {
    private Node x;
    private Node y;
    private Set<Node> z;

    /**
     * Constructs a triple of nodes.
     */
    public IndependenceFact(Node x, Node y, List<Node> z) {
        if (x == null || y == null || z == null) {
            throw new NullPointerException();
        }

        this.x = x;
        this.y = y;
        this.z = new LinkedHashSet<Node>(z);
    }

    public final Node getX() {
        return x;
    }

    public final Node getY() {
        return y;
    }

    public final List<Node> getZ() {
        return new LinkedList<Node>(z);
    }

    public final int hashCode() {
        int hash = 17;
        hash += 19 * x.hashCode() * y.hashCode();
        hash += 23 * z.hashCode();
        return hash;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof IndependenceFact)) {
            return false;
        }

        IndependenceFact fact = (IndependenceFact) obj;

        return z.equals(fact.z) && ((x.equals(fact.x) && y.equals(fact.y)) || (x.equals(fact.y) && (y.equals(fact.x))));

//        return (x.equals(fact.x) && y.equals(fact.y) &&
//                z.equals(fact.z))
//                sameNodes(z, fact.z))
//                || (x.equals(fact.y) & y.equals(fact.x) &&
//                z.equals(fact.z));
//
//                sameNodes(z, fact.z));
    }


//    private boolean sameNodes(Collection<Node> list1, Collection<Node> list2) {
//        return list1.containsAll(list2) && list2.containsAll(list1);
//    }

    public String toString() {
        return x + " _||_ " + y + " | " + z ;
    }
}
