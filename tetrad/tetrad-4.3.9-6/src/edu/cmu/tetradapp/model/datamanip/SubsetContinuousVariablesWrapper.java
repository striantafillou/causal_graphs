package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.DataWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Add description
 *
 * @author Tyler Gibson
 */
public class SubsetContinuousVariablesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    public SubsetContinuousVariablesWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The givan data must not be null");
        }
        DataModel model = data.getSelectedDataModel();
        if (!(model instanceof DataSet)) {
            throw new IllegalArgumentException("The given dataset must be tabular");
        }
        this.setDataModel(createModel((DataSet) model));
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new SubsetContinuousVariablesWrapper(new DataWrapper(DataUtils.continuousSerializableInstance()));
    }

    //=========================== Private Methods =================================//


    private static DataModel createModel(DataSet data) {
        List<Node> variables = data.getVariables();

        int n = 0;
        for (Node variable : variables) {
            if (variable instanceof ContinuousVariable) {
                n++;
            }
        }
        if (n == 0) {
            return new ColtDataSet(0, new ArrayList<Node>());
        }

        int[] indices = new int[n];
        int m = 0;
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i) instanceof ContinuousVariable) {
                indices[m++] = i;
            }
        }

        return data.subsetColumns(indices);
    }


}
