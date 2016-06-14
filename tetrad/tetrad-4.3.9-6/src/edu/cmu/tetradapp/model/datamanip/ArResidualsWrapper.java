package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.search.TimeSeriesUtils;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * @author Tyler
 */
public class ArResidualsWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Constructs a new time series dataset.
     *
     * @param data   - Previous data (from the parent node)
     * @param params - The parameters.
     */
    public ArResidualsWrapper(DataWrapper data, ArResidualsParams params) {
        DataModel model = data.getSelectedDataModel();
        if (!(model instanceof DataSet)) {
            throw new IllegalArgumentException("The data model must be a rectangular dataset");
        }
        model = TimeSeriesUtils.ar((DataSet) model, params.getNumOfTimeLags());
        this.setDataModel(model);
        this.setSourceGraph(data.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new TimeSeriesWrapper(DataWrapper.serializableInstance(),
                TimeSeriesParams.serializableInstance());
    }

    //=============================== Private Methods =========================//


}