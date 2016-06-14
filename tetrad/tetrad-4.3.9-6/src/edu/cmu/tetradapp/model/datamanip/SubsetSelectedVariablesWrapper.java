package edu.cmu.tetradapp.model.datamanip;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Add description
 *
 * @author Tyler Gibson
 */
public class SubsetSelectedVariablesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    public SubsetSelectedVariablesWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The givan data must not be null");
        }

        DataModel model = data.getSelectedDataModel();

        if (model instanceof ColtDataSet) {
            this.setDataModel(createRectangularModel(new ColtDataSet((ColtDataSet) model)));
        } else if (model instanceof CovarianceMatrix) {
            this.setDataModel(createCovarianceModel((CovarianceMatrix) model));
        } else {
            throw new IllegalArgumentException("Expecting a rectangular data " +
                    "set or a covariance matrix.");
        }

        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new SubsetSelectedVariablesWrapper(new DataWrapper(DataUtils.continuousSerializableInstance()));
    }

    //=========================== Private Methods =================================//


    private static DataModel createRectangularModel(DataSet data) {
        for (int i = data.getNumColumns() -1; i >= 0; i--) {
            if (!data.isSelected(data.getVariable(i))) {
                data.removeColumn(i);
            }
        }                                    
        return data;
    }

    private static DataModel createCovarianceModel(CovarianceMatrix data) {
        int numSelected = 0;

        for (Node node : data.getVariables()) {
            if (data.isSelected(node)) {
                numSelected++;
            }
        }

        int[] selectedIndices = new int[numSelected];
        String[] nodeNames = new String[numSelected];
        int index = -1;

        for (int i = 0; i < data.getVariables().size(); i++) {
            Node node = data.getVariables().get(i);
            if (data.isSelected(node)) {
                ++index;
                selectedIndices[index] = i;
                nodeNames[index] = node.getName();
            }
        }

        DoubleMatrix2D matrix = data.getMatrix();

        DoubleMatrix2D newMatrix = matrix.viewSelection(
                selectedIndices, selectedIndices).copy();


        CovarianceMatrix newCov = new CovarianceMatrix(DataUtils.createContinuousVariables(nodeNames), newMatrix, data.getSampleSize());

        return newCov;
    }


}
