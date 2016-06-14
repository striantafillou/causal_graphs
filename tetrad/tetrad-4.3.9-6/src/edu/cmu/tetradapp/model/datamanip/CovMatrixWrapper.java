package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Tyler Gibson
 */
public class CovMatrixWrapper extends DataWrapper {
     static final long serialVersionUID = 23L;

    /**
     * Splits the given data set by collinear columns.
     *
     * @param wrapper
     */
    public CovMatrixWrapper(DataWrapper wrapper) {
        if (wrapper == null) {
            throw new NullPointerException("The given data must not be null");
        }
        DataModel model = wrapper.getSelectedDataModel();
        if (model instanceof DataSet) {
            DataSet dataSet = (DataSet) model;
            if (!(dataSet.isContinuous())) {
               throw new IllegalArgumentException("The data must be continuous");
            }

            CovarianceMatrix covarianceMatrix = new CovarianceMatrix(dataSet);
            setDataModel(covarianceMatrix);
            setSourceGraph(wrapper.getSourceGraph());
        } else if (model instanceof CovarianceMatrix) {
            CovarianceMatrix covarianceMatrix = new CovarianceMatrix((CovarianceMatrix)model);
            setDataModel(covarianceMatrix);
            setSourceGraph(wrapper.getSourceGraph());
        } else {
            throw new IllegalArgumentException("Must be a dataset or a covariance  matrix");
        }
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new CovMatrixWrapper(new DataWrapper(DataUtils.continuousSerializableInstance()));
    }


}
