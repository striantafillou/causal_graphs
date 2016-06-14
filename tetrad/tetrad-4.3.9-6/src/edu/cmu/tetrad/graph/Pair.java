package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Stores a pair (x, y) of nodes. Note that (x, y) = (z, y). Useful
 * for marking graphs.
 *
 * @author Joseph Ramsey, after Frank Wimberly.
 */
public final class Pair implements TetradSerializable {
    static final long serialVersionUID = 23L;

    // Note: Switching all uses of Underline to Triple, since they did the
    // same thing, and this allows for some useful generalizations, especially
    // since for triples it is always the case that (x, y, z) = (z, y, x).
    private Node x;
    private Node y;

    /**
     * Constructs a triple of nodes.
     */
    public Pair(Node x, Node y) {
        if (x == null || y == null) {
            throw new NullPointerException();
        }

        this.x = x;
        this.y = y;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Pair serializableInstance() {
        return new Pair(new GraphNode("X"), new GraphNode("Y"));
    }

    public final Node getX() {
        return x;
    }

    public final Node getY() {
        return y;
    }

    public final int hashCode() {
        int hash = 17;
        hash += 19 * (x.hashCode() + y.hashCode());
        return hash;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair triple = (Pair) obj;
        return (x.equals(triple.x) && y.equals(triple.y))
                || (y.equals(triple.x) && x.equals(triple.y));
    }

    public String toString() {
        return "<" + x + ", " + y + ">";
    }
}