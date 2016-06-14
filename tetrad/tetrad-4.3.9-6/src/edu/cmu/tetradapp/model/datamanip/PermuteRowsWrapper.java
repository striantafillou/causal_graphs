package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * GUI model for the permute rows function in RectangularDataSet.
 *
 * @author Tyler Gibson
 */
public class PermuteRowsWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Constructs the wrapper given some data and the params.
     *
     * @param data
     */
    public PermuteRowsWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }

        ColtDataSet originalData = (ColtDataSet) data.getSelectedDataModel();
        DataSet copy = new ColtDataSet(originalData);
        copy.permuteRows();
        this.setDataModel(copy);
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new PermuteRowsWrapper(DataWrapper.serializableInstance());
    }
}