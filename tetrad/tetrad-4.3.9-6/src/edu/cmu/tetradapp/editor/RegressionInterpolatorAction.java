package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.regression.RegressionInterpolator;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Ricardo Silva
 */
public final class RegressionInterpolatorAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public RegressionInterpolatorAction(DataEditor editor) {
        super("Replace Missing Values with Regression Predictions");

        if (editor == null) {
            throw new NullPointerException();
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        DataModel dataModel = getDataEditor().getSelectedDataModel();

        if (dataModel instanceof DataSet) {
            DataSet dataSet = (DataSet) dataModel;

            DataFilter interpolator = new RegressionInterpolator();
            DataSet newDataSet = null;
            try {
                newDataSet = interpolator.filter(dataSet);
            } catch (IllegalArgumentException e1) {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Error: " + e1.getMessage());
                throw e1;
            }

            DataModelList list = new DataModelList();
            list.add(newDataSet);
            getDataEditor().reset(list);
            getDataEditor().selectLastTab();
        }
        else if (dataModel instanceof CovarianceMatrix) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Must be a tabular data set.");
        }
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}
