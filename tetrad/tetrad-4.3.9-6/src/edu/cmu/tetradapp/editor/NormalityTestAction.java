package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.util.DesktopController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Displays a Q-Q plot for a random variable.
 *
 * A lot of the code borrows heavily from HistogramAction
 *
 * @author Michael Freenor
 */

public class NormalityTestAction extends AbstractAction {


    /**
     * The data edtitor that action is attached to.
     */
    private DataEditor dataEditor;


    /**
     * Constructs the <code>QQPlotAction</code> given the <code>DataEditor</code>
     * that its attached to.
     *
     * @param editor
     */
    public NormalityTestAction(DataEditor editor) {
        super("Run Normality Tests");
        this.dataEditor = editor;
    }






    public void actionPerformed(ActionEvent e) {
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();
        if(dataSet == null || dataSet.getNumColumns() == 0){
            JOptionPane.showMessageDialog(findOwner(), "Cannot run normality tests on an empty data set.");
            return;
        }
        // if there are missing values warn and don't display q-q plot.
        if(DataUtils.containsMissingValue(dataSet)){
            JOptionPane.showMessageDialog(findOwner(), new JLabel("<html>Data has missing values, " +
                    "remove all missing values before<br>" +
                    "running normality tests.</html>"));
            return;
        }

        JPanel panel = createNormalityTestDialog(null);

        EditorWindow window = new EditorWindow(panel,
                "Normality Tests", "Close", false);
        DesktopController.getInstance().addEditorWindow(window);
        window.setVisible(true);

    }

    //============================== Private methods ============================//





    /**
     * Sets the location on the given dialog for the given index.
     */
    private void setLocation(JDialog dialog, int index) {
        Rectangle bounds = dialog.getBounds();
        JFrame frame = findOwner();
        Dimension dim;
        if (frame == null) {
            dim = Toolkit.getDefaultToolkit().getScreenSize();
        } else {
            dim = frame.getSize();
        }

        int x = (int) (150 * Math.cos(index * 15 * (Math.PI / 180)));
        int y = (int) (150 * Math.sin(index * 15 * (Math.PI / 180)));
        x += (dim.width - bounds.width)/2;
        y += (dim.height - bounds.height)/2;
        dialog.setLocation(x, y);
    }


    /**
     * Creates a dialog that is showing the histogram for the given node (if null
     * one is selected for you)
     */
    private JPanel createNormalityTestDialog(Node selected) {
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();

        QQPlot qqPlot = new QQPlot(dataSet, selected);
        NormalityTestEditorPanel editorPanel = new NormalityTestEditorPanel(qqPlot, dataSet);

        JTextArea display = new JTextArea(NormalityTests.runNormalityTests(dataSet, (ContinuousVariable)qqPlot.getSelectedVariable()), 20, 65);
        display.setEditable(false);
        editorPanel.addPropertyChangeListener(new NormalityTestListener(display));

        Box box = Box.createHorizontalBox();
        box.add(display);

        box.add(Box.createHorizontalStrut(3));
        box.add(editorPanel);
        box.add(Box.createHorizontalStrut(5));
        box.add(Box.createHorizontalGlue());

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(15));
        vBox.add(box);
        vBox.add(Box.createVerticalStrut(5));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(vBox, BorderLayout.CENTER);

        return panel;
    }


    private JFrame findOwner() {
        return (JFrame) SwingUtilities.getAncestorOfClass(
                JFrame.class, dataEditor);
    }

    //================================= Inner Class ======================================//



    /**
     * Glue between the editor and the display.
     */
    private static class NormalityTestListener implements PropertyChangeListener {

        private JTextArea display;


        public NormalityTestListener(JTextArea display) {
            this.display = display;
        }


        public void propertyChange(PropertyChangeEvent evt) {
            if ("histogramChange".equals(evt.getPropertyName())) {
                this.display.setText((String) evt.getNewValue());
            }
        }
    }


}