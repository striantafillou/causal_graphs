package edu.cmu.tetradapp.editor;


import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.util.DesktopController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tyler Gibson
 */
public class HistogramAction extends AbstractAction {


    /**
     * The data edtitor that action is attached to.
     */
    private DataEditor dataEditor;


    /**
     * Constructs the <code>HistogramAction</code> given the <code>DataEditor</code>
     * that its attached to.
     *
     * @param editor
     */
    public HistogramAction(DataEditor editor) {
        super("Show Histogram");
        this.dataEditor = editor;
    }



    


    public void actionPerformed(ActionEvent e) {
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();
        if(dataSet == null || dataSet.getNumColumns() == 0){
            JOptionPane.showMessageDialog(findOwner(), "Cannot display a histogram for an empty data set.");
            return;
        }
        // if there are missing values warn and don't display historm.
//        if(DataUtils.containsMissingValue(dataSet)){
//            JOptionPane.showMessageDialog(findOwner(), new JLabel("<html>Data has missing values, " +
//                    "remove all missing values before<br>" +
//                    "displaying data in a histogram.</html>"));
//            return;
//        }

        int[] selected = dataSet.getSelectedIndices();
        // if more then one column is selected then open up more than one histogram
        if (selected != null && 1 < selected.length) {
            // warn user if they selected more than 10
            if(10 < selected.length){
                int option = JOptionPane.showConfirmDialog(findOwner(), "You are about to open " + selected.length +
                " histograms, are you sure you want to proceed?", "Histogram Warning", JOptionPane.YES_NO_OPTION);
                // if selected no, return
                if(option == JOptionPane.NO_OPTION){
                    return;
                }
            }
            for (int index : selected) {
                JPanel component = createHistogramPanel(dataSet.getVariable(index));
                EditorWindow editorWindow = new EditorWindow(component, "Histogram", "Close", false);
                DesktopController.getInstance().addEditorWindow(editorWindow);
                editorWindow.pack();
                editorWindow.setVisible(true);
            }
        } else {
            JPanel component = createHistogramPanel(null);
            EditorWindow editorWindow = new EditorWindow(component, "Histogram", "Close", false);
            DesktopController.getInstance().addEditorWindow(editorWindow);
            editorWindow.pack();
            editorWindow.setVisible(true);
        }
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
    private JPanel createHistogramPanel(Node selected) {
//        JDialog dialog = new JDialog(findOwner(), "Histogram", false);
//        dialog.setResizable(false);
//        dialog.getContentPane().setLayout(new BorderLayout());
        DataSet dataSet = (DataSet) dataEditor.getSelectedDataModel();
        Histogram histogram = new Histogram(dataSet, selected, 12);
        HistogramEditorPanel editorPanel = new HistogramEditorPanel(histogram, dataSet);
        HistogramDisplayPanel display = new HistogramDisplayPanel(histogram);
        editorPanel.addPropertyChangeListener(new HistogramListener(display));

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(new JMenuItem(new SaveComponentImage(display, "Save Histogram")));
        bar.add(menu);

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

//        dialog.getContentPane().add(bar, BorderLayout.NORTH);
//        dialog.getContentPane().add(vBox, BorderLayout.CENTER);
//        return dialog;

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
    private static class HistogramListener implements PropertyChangeListener {

        private HistogramDisplayPanel display;


        public HistogramListener(HistogramDisplayPanel display) {
            this.display = display;
        }


        public void propertyChange(PropertyChangeEvent evt) {
            if ("histogramChange".equals(evt.getPropertyName())) {
                this.display.updateHistogram((Histogram) evt.getNewValue());
            }
        }
    }


}
