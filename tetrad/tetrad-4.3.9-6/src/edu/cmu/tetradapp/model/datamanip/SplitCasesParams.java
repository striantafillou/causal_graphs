package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.SplitCasesSpec;
import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Tyler was lazy and didn't document this....
 *
 * @author Tyler Gibson
 */
public class SplitCasesParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * True iff the row order of the data should be shuffled before splitting it.
     */
    private boolean dataShuffled = true;


    /**
     * The number of splits.
     */
    private int numSplits = 3;


    /**
     * The spec.
     */
    private SplitCasesSpec spec;


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SplitCasesParams serializableInstance() {
        SplitCasesParams params = new SplitCasesParams();
        params.setSpec(SplitCasesSpec.serializableInstance());
        return params;
    }

    //=============== Public Methods ===================//


    public boolean isDataShuffled() {
        return dataShuffled;
    }

    public void setDataShuffled(boolean dataShuffled) {
        this.dataShuffled = dataShuffled;
    }

    public int getNumSplits() {
        return numSplits;
    }

    public void setNumSplits(int numSplits) {
        if (numSplits < 1) {
            throw new IllegalArgumentException("Number of splits must be " +
                    "at least 1.");
        }
        this.numSplits = numSplits;
    }


    public SplitCasesSpec getSpec() {
        return spec;
    }

    public void setSpec(SplitCasesSpec spec) {
        if (spec == null) {
            throw new NullPointerException("The split case must not be null");
        }
        this.spec = spec;
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

    }
}
