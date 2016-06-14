package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.IndTestType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Stores the parameters needed for the PC search and wizard.
 *
 * @author Raul Salinas
 */

public final class LingamParams implements SearchParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Range >= -1.
     */
    private int depth;

    /**
     * @serial Can be null.
     */
    private IndTestParams indTestParams;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private IndTestType testType;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */
    public LingamParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LingamParams serializableInstance() {
        return new LingamParams();
    }

    //============================PUBLIC METHODS===========================//

    public IndTestParams getIndTestParams() {
        return indTestParams;
    }

    public void setIndTestParams2(IndTestParams indTestParams) {
        this.indTestParams = indTestParams;
    }

    public List getVarNames() {
        return this.varNames;
    }

    public void setVarNames(List varNames) {
        this.varNames = varNames;
    }

    public Graph getSourceGraph() {
        return this.sourceGraph;
    }

    public void setSourceGraph(Graph graph) {
        this.sourceGraph = graph;
    }

    public void setIndTestType(IndTestType testType) {
        this.testType = testType;
    }

    public IndTestType getIndTestType() {
        return this.testType;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

    public Knowledge getKnowledge() {
        return new Knowledge(knowledge);
    }

    /**
     * Sets the depth of the associated PC search.
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * @return the int containing the depth of the associated PC search.
     */
    public int getDepth() {
        return depth;
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

        if (knowledge == null) {
            throw new NullPointerException();
        }

        if (depth < -1) {
            throw new IllegalStateException("Depth out of range: " + depth);
        }
    }
}
