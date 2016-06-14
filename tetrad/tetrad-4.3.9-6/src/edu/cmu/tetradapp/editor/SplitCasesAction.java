package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Discretizes selected columns in a data set.
 *
 * @author Joseph Ramsey
 */
public final class SplitCasesAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates new action to discretize columns.
     */
    public SplitCasesAction(DataEditor editor) {
        super("Split Data by Cases");

        if (editor == null) {
            throw new NullPointerException();
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        DataModel selectedDataModel = getDataEditor().getSelectedDataModel();

        if (!(selectedDataModel instanceof DataSet)) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Requires a tabular data set.");
        }

        List<Node> selectedVariables = new LinkedList<Node>();

        DataSet dataSet = (DataSet) selectedDataModel;
        int numColumns = dataSet.getNumColumns();

        for (int i = 0; i < numColumns; i++) {
            Node variable = dataSet.getVariable(i);

            if (dataSet.isSelected(variable)) {
                selectedVariables.add(variable);
            }
        }

        if (dataSet.getNumRows() == 0) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Data set is empty.");
            return;
        }

        if (selectedVariables.isEmpty()) {
            selectedVariables.addAll(dataSet.getVariables());
        }
//
//        SplitCasesParamsEditor editor = new SplitCasesParamsEditor(dataSet, 3);
//
//        int ret = JOptionPane.showOptionDialog(JOptionUtils.centeringComp(),
//                editor, "Split Data by Cases", JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.PLAIN_MESSAGE, null, null, null);
//
//        if (ret == JOptionPane.CANCEL_OPTION) {
//            return;
//        }
//
//        DataModelList list = editor.getSplits();
//        getDataEditor().reset(list);
//        getDataEditor().selectLastTab();

    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}
