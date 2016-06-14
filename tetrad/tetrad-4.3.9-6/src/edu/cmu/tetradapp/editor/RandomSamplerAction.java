package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataModelList;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.RandomSampler;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Samples cases from a dataset without replacement.
 *
 * @author Joseph Ramsey
 */
final class RandomSamplerAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;
    private int sampleSize = 100;

    /**
     * Creates a new action to split by collinear columns.
     */
    public RandomSamplerAction(DataEditor editor) {
        super("Random Sample (Without Replacement)");

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

            if (dataSet.getNumRows() == 0) {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Data set is empty.");
                return;
            }

            JComponent editor = editor();
            int selection = JOptionPane.showOptionDialog(
                    JOptionUtils.centeringComp(), editor, "Sample Size",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"Done", "Cancel"}, "Done");

            if (selection != 0) {
                return;
            }

            try {
                DataSet newDataSet =
                        RandomSampler.sample(dataSet, getSampleSize());

                DataModelList list = new DataModelList();
                list.add(newDataSet);
                getDataEditor().reset(list);
                getDataEditor().selectLastTab();
            }
            catch (Exception e1) {
                String s = e1.getMessage();

                if (s == null || "".equals(s)) {
                    s = "Could not construct random sample.";
                }

                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), s);
            }
        }
        else {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Must be a tabular data set.");
        }
    }

    private JComponent editor() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        final IntTextField sampleSizeField = new IntTextField(getSampleSize(), 6);
        sampleSizeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    setSampleSize(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        // continue workbench construction.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("<html>" +
                "The input dataset will be sampled with replacement to create a new" +
                "<br>dataset with the number of samples entered below." +
                "<br>The editable default sample size is 100." + "</html>"));

        Box b7 = Box.createHorizontalBox();
        b7.add(Box.createHorizontalGlue());
        b7.add(new JLabel("<html>" + "<i>Sample size:  </i>" + "</html>"));
        b7.add(sampleSizeField);

        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));
        b1.add(b7);
        b1.add(Box.createHorizontalGlue());
        panel.add(b1, BorderLayout.CENTER);

        return panel;
    }

    private void setSampleSize(int sampleSize) {
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size must be >= 1.");
        }
        this.sampleSize = sampleSize;
    }

    private int getSampleSize() {
        return this.sampleSize;
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}
