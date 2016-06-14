package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.RegressionRunner;
import edu.cmu.tetradapp.model.RegressionParams;
import edu.cmu.tetradapp.model.DataWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * For General Conditional Independence Test
 *
 * A lot of the code borrows heavily from HistogramAction
 *
 * @author Michael Freenor
 */

public class ConditionalIndependenceTestAction extends AbstractAction {


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
    public ConditionalIndependenceTestAction(DataEditor editor) {
        super("Test for Conditional Independencies");
        this.dataEditor = editor;
    }


    public void actionPerformed(ActionEvent e) {
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();
        if(dataSet == null || dataSet.getNumColumns() == 0){
            JOptionPane.showMessageDialog(findOwner(), "Cannot run conditional independence tests on an empty data set.");
            return;
        }

        JDialog dialog = createConditionalIndependenceTestDialog();
        dialog.pack();
        dialog.setLocation(0, 0); //this changed so if position is funky check this
        dialog.setVisible(true);
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
    private JDialog createConditionalIndependenceTestDialog() {
        String dialogTitle = "Conditional Independence Testing";
        JDialog dialog = new JDialog(findOwner(), dialogTitle, false);
        dialog.setResizable(false);
        dialog.getContentPane().setLayout(new BorderLayout());
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();

        /*
        QQPlot qqPlot = new QQPlot(dataSet, selected);
        QQPlotEditorPanel editorPanel = new QQPlotEditorPanel(qqPlot, dataSet);
        QQPlotDisplayPanel display = new QQPlotDisplayPanel(qqPlot);
        editorPanel.addPropertyChangeListener(new QQPlotListener(display));
        */

        ConditionalIndependenceDialog display = new ConditionalIndependenceDialog(new ConditionalIndependenceWrapper(new DataWrapper(dataSet), new RegressionParams()));

        JMenuBar bar = new JMenuBar();

        //JMenu menu = new JMenu("File");
        //bar.add(menu);

        Box box = Box.createHorizontalBox();
        box.add(display);

        /*
        box.add(Box.createHorizontalStrut(3));
        box.add(editorPanel);
        box.add(Box.createHorizontalStrut(5));
        box.add(Box.createHorizontalGlue());
        */

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(15));
        vBox.add(box);
        vBox.add(Box.createVerticalStrut(5));

        dialog.getContentPane().add(bar, BorderLayout.NORTH);
        dialog.getContentPane().add(vBox, BorderLayout.CENTER);
        return dialog;
    }


    private JFrame findOwner() {
        return (JFrame) SwingUtilities.getAncestorOfClass(
                JFrame.class, dataEditor);
    }

    //================================= Inner Class ======================================//



    /**
     * Glue between the editor and the display.
     */
    private static class ConditionalIndependenceTestListener implements PropertyChangeListener {

        private QQPlotDisplayPanel display;


        public ConditionalIndependenceTestListener(QQPlotDisplayPanel display) {
            this.display = display;
        }


        public void propertyChange(PropertyChangeEvent evt) {
            if ("histogramChange".equals(evt.getPropertyName())) {
                this.display.updateQQPlot((QQPlot) evt.getNewValue());
            }
        }
    }


}