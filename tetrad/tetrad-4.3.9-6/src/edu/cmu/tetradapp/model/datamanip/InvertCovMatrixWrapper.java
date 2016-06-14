package edu.cmu.tetradapp.model.datamanip;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Tyler Gibson
 */
public class InvertCovMatrixWrapper extends DataWrapper {
     static final long serialVersionUID = 23L;

    /**
     * Splits the given data set by collinear columns.
     *
     * @param wrapper
     */
    public InvertCovMatrixWrapper(DataWrapper wrapper) {
        if (wrapper == null) {
            throw new NullPointerException("The given data must not be null");
        }
        DataModel model = wrapper.getSelectedDataModel();
        if (model instanceof CovarianceMatrix) {
            CovarianceMatrix dataSet = (CovarianceMatrix) model;
            DoubleMatrix2D data = dataSet.getMatrix();
            DoubleMatrix2D inverse = MatrixUtils.inverse(data);
            String[] varNames = dataSet.getVariableNames().toArray(new String[0]);
            CovarianceMatrix covarianceMatrix = new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), inverse, dataSet.getSampleSize());
            setDataModel(covarianceMatrix);
            setSourceGraph(wrapper.getSourceGraph());
        } else {
            throw new IllegalArgumentException("Must be a covariance matrix");
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