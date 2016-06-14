package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetradapp.model.DataWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tyler
 */
public class DiscretizationWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    /**
     * The discretized data set.
     *
     * @serial Not null.
     */
    private DataSet discretizedDataSet;


    /**
     * Constructs the <code>DiscretizationWrapper</code> by discretizing the select
     * <code>DataModel</code>.
     *
     * @param data
     * @param params
     */
    public DiscretizationWrapper(DataWrapper data, DiscretizationParams params) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }
        if (params == null) {
            throw new NullPointerException("The given parameters must not be null");
        }
        DataSet originalData = (DataSet) data.getSelectedDataModel();
        this.discretizedDataSet = Discretizer.getDiscretizedDataSet(originalData, params);
        setDataModel(this.discretizedDataSet);
        setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DiscretizationWrapper serializableInstance() {
        return new DiscretizationWrapper(DataWrapper.serializableInstance(),
                DiscretizationParams.serializableInstance());
    }

    //=============================== Private Methods =========================//


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

        if (this.discretizedDataSet == null) {
            throw new NullPointerException();
        }
    }


}
