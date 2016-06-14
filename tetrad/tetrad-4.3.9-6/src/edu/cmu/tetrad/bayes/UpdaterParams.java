package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Stores the parameters needed to initialize a BayesPm.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class UpdaterParams implements Params, TetradSerializable {
    static final long serialVersionUID = 23L;

    private Evidence evidence;
    private Node variable;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public UpdaterParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static UpdaterParams serializableInstance() {
        return new UpdaterParams();
    }

    //============================PUBLIC METHODS==========================//


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
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Node getVariable() {
        return variable;
    }

    public void setVariable(DiscreteVariable variable) {
        this.variable = variable;
    }
}
