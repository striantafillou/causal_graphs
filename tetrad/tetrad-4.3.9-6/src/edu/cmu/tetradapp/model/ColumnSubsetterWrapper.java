package edu.cmu.tetradapp.model;

/**
 * @deprecated Use VariableSubsetterWrapper instead.
 */
public class ColumnSubsetterWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new VariableSubsetterWrapper(DataWrapper.serializableInstance());
    }
}
