package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Add description
 *
 * @author Tyler Gibson
 */
public class ConvertNumericalDiscreteToContinuousWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    public ConvertNumericalDiscreteToContinuousWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }
        DataModel model = data.getSelectedDataModel();
        if ((!(model instanceof DataSet))) {
            throw new IllegalArgumentException("Data must be tabular");
        }

        DataSet originalData = (DataSet) model;
        DataSet convertedData =
                null;
        try {
            convertedData = DataUtils.convertNumericalDiscreteToContinuous(originalData);
        } catch (NumberFormatException e) {
            throw new RuntimeException("There were some non-numeric values in that dataset.");
        }
        this.setDataModel(convertedData);
        this.setSourceGraph(data.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ConvertNumericalDiscreteToContinuousWrapper serializableInstance() {
        return new ConvertNumericalDiscreteToContinuousWrapper(DataWrapper.serializableInstance());
    }


}