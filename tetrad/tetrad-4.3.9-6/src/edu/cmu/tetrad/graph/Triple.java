package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Stores a triple (x, y, z) of nodes. Note that (x, y, z) = (z, y, x). Useful
 * for marking graphs.
 *
 * @author Joseph Ramsey, after Frank Wimberly.
 */
public final class Triple implements TetradSerializable {
    static final long serialVersionUID = 23L;

    // Note: Switching all uses of Underline to Triple, since they did the
    // same thing, and this allows for some useful generalizations, especially
    // since for triples it is always the case that (x, y, z) = (z, y, x).
    private Node x;
    private Node y;
    private Node z;

    /**
     * Constructs a triple of nodes.
     */
    public Triple(Node x, Node y, Node z) {
        if (x == null || y == null || z == null) {
            throw new NullPointerException();
        }

        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Triple serializableInstance() {
        return new Triple(new GraphNode("X"), new GraphNode("Y"), new GraphNode("Z"));
    }

    public final Node getX() {
        return x;
    }

    public final Node getY() {
        return y;
    }

    public final Node getZ() {
        return z;
    }

    public final int hashCode() {
        int hash = 17;
        hash += 19 * x.hashCode() * z.hashCode();
        hash += 23 * y.hashCode();
        return hash;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof Triple)) {
            return false;
        }

        Triple triple = (Triple) obj;
        return (x.equals(triple.x) && y.equals(triple.y) &&
                z.equals(triple.z))
                || (x.equals(triple.z) && y.equals(triple.y) &&
                z.equals(triple.x));
    }

    public String toString() {
        return "<" + x + ", " + y + ", " + z + ">";
    }
}
