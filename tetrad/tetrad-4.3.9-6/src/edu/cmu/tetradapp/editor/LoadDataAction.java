package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataModelList;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.model.DataWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * New data loading action.
 *
 * @author Joseph Ramsey
 */
final class LoadDataAction extends AbstractAction {

    /**
     * The dataEditor into which data is loaded.                          -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new load data action for the given dataEditor.
     */
    public LoadDataAction(DataEditor editor) {
        super("Load Data...");

        if (editor == null) {
            throw new NullPointerException("Data Editor must not be null.");
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        // first warn user about other datasets being removed.
        if (!this.isDataEmpty()) {
            String message = "Loading data from a file will remove all existing data in the data editor. " +
                    "Do you want to continue?";
            int option = JOptionPane.showOptionDialog(this.dataEditor, message, "Data Removal Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
            // if not yes, cancel action.
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }

        JFileChooser chooser = getJFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(this.dataEditor);

        final File[] files = chooser.getSelectedFiles();

        // Can this happen?
        if (files == null) {
            return;
        }

        if (files.length == 0) {
            return;
        }

        Preferences.userRoot().put("fileSaveLocation", files[0].getParent());

        DataModelList dataModel = new DataModelList();

//        if (files.length == 1) {
//            dataModel.setName(files[0].getAbsolutePath());
//        } else {
//            dataModel.setName("Multiple Data Sets from " + files[0].getParent());
//        }

        for (File file : files) {
            final LoadDataDialog dialog = new LoadDataDialog(file);

            int ret = JOptionPane.showOptionDialog(JOptionUtils.centeringComp(), dialog,
                    "Loading File " + file.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, new String[]{"Save", "Cancel"},
                    "Save");

            if (ret == 0) {
                DataModel _dataModel = dialog.getDataModel();

                _dataModel.setName(file.getName());

                if (_dataModel == null) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "Problems loading that one--skipping.");
                    continue;
                }

                dataModel.add(_dataModel);
            } else {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Problems loading that one--skipping.");
            }
        }

        dataEditor.replace(dataModel);
        dataEditor.selectLastTab();
        firePropertyChange("modelChanged", null, null);
    }

    //======================= private methods =========================//

    /**
     * States whether the data is empty.
     */
    private boolean isDataEmpty() {
        DataWrapper wrapper = this.dataEditor.getDataWrapper();
        DataModelList dataModels = wrapper.getDataModelList();
        for (DataModel model : dataModels) {
            if (model instanceof DataSet) {
                return ((DataSet) model).getNumRows() == 0;
            } else {
                // how do you know in this case? Just say false
                return false;
            }
        }
        return true;
    }


    private static JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String sessionSaveLocation =
                Preferences.userRoot().get("fileSaveLocation", "");
        chooser.setCurrentDirectory(new File(sessionSaveLocation));
        chooser.resetChoosableFileFilters();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return chooser;
    }
}
