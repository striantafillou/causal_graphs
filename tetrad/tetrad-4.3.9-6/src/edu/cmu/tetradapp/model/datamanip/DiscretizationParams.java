package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.ContinuousDiscretizationSpec;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The parameters for the discretizer.
 *
 * @author Tyler Gibson
 */
public class DiscretizationParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * A map from node's to the continuous discretizations to use for them.
     *
     * @serial Not null.
     */
    private Map<Node, ContinuousDiscretizationSpec> map = new HashMap<Node, ContinuousDiscretizationSpec>();


    /**
     * Constructs the discretization params.
     */
    public DiscretizationParams() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DiscretizationParams serializableInstance() {
        return new DiscretizationParams();
    }


    /**
     * Returns the specs.
     *
     * @return - specs.
     */
    public Map<Node, ContinuousDiscretizationSpec> getSpecs() {
        return map;
    }


    /**
     * Sets the mapping.
     *
     * @param map
     */
    public void setSpecs(Map<Node, ContinuousDiscretizationSpec> map) {
        if (!(map instanceof Serializable)) {
           throw new IllegalArgumentException("The given map must be serializable");
        }
        this.map = map;
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

        if (map == null) {
            throw new NullPointerException("Mapping must not be null");
        }
    }

}
