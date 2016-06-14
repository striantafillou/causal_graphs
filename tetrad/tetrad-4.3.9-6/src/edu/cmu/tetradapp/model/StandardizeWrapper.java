package edu.cmu.tetradapp.model;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetradapp.model.DataWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Joe Ramsey
 */
public class StandardizeWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    /**
     * Constructs the <code>DiscretizationWrapper</code> by discretizing the select
     * <code>DataModel</code>.
     *
     * @param data
     */
    public StandardizeWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }

        DataSet dataSet = (DataSet) data.getDataModelList().get(0);
        DoubleMatrix2D matrix2D = DataUtils.standardizeData(dataSet.getDoubleData());
        DataSet _dataSet = ColtDataSet.makeContinuousData(dataSet.getVariables(), matrix2D);
        setDataModel(_dataSet);
        setSourceGraph(data.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static StandardizeWrapper serializableInstance() {
        return new StandardizeWrapper(DataWrapper.serializableInstance());
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
    @SuppressWarnings({"MethodMayBeStatic"})
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}
