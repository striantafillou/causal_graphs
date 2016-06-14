package edu.cmu.tetradapp.model;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a continuous data set to a correlation matrix.
 *
 * @author Joseph Ramsey
 */
public class DataStandardizer extends DataWrapper {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS==============================//

    public DataStandardizer(DataWrapper wrapper) {
        if (!(wrapper.getSelectedDataModel() instanceof DataSet)) {
            throw new IllegalArgumentException(
                    "That is not a continuous " + "data set.");
        }

        DataSet dataSet =
                (DataSet) wrapper.getSelectedDataModel();

        if (!(dataSet.isContinuous())) {
            throw new RuntimeException("Only continuous data sets can be " +
                    "standardized.");
        }

        DoubleMatrix2D data2 = DataUtils.standardizeData(dataSet.getDoubleData());
        List<Node> list = dataSet.getVariables();
        List<Node> list2 = new ArrayList<Node>();

        for (Node node: list) {
            list2.add(node);
        }

        ColtDataSet dataSet2 = ColtDataSet.makeContinuousData(list2, data2);
        setDataModel(dataSet2);
        setSourceGraph(wrapper.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        DataWrapper wrapper =
                new DataWrapper(DataUtils.continuousSerializableInstance());
        return new DataStandardizer(wrapper);
    }

}
