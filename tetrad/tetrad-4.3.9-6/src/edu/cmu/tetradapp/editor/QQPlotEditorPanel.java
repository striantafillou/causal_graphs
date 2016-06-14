package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 *
 * Borrows from the histogram stuff yet again
 *
 * @author Michael Freenor
 */
public class QQPlotEditorPanel extends JPanel {


    /**
     * Combo box of all the variables.
     */
    private JComboBox variableBox;

    /**
     * The dataset being viewed.
     */
    private DataSet dataSet;

    private QQPlot qqPlot;


    /**
     * The discrete variables of the data set (may be empty).
     */
    private LinkedList<DiscreteVariable> variables = new LinkedList<DiscreteVariable>();


    /**
     * Constructs the editor panel given the initial histogram and the dataset.
     *
     * @param qqPlot
     * @param dataSet
     */
    public QQPlotEditorPanel(QQPlot qqPlot, DataSet dataSet) {
        //   construct components
        this.setLayout(new BorderLayout());
        // first build histogram and components used in the editor.
        this.qqPlot = qqPlot;
        Node selected = qqPlot.getSelectedVariable();
        this.dataSet = dataSet;
        this.variableBox = new JComboBox();
        ListCellRenderer renderer = new VariableBoxRenderer();
        this.variableBox.setRenderer(renderer);
        for (Node node : dataSet.getVariables()) {
            if (node instanceof ContinuousVariable) {
                this.variableBox.addItem(node);
                if (node == selected) {
                    this.variableBox.setSelectedItem(node);
                }
            }
        }
        this.variableBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Node node = (Node) e.getItem();
                    QQPlot newValue = new QQPlot(QQPlotEditorPanel.this.dataSet, node);
                    //categorySelector.setValue(newValue.getNumberOfCategories());
                 //   categorySelector.setMax(getMaxCategoryValue(newValue));
                    //System.out.println(node.getName());
                    changeQQPlot(newValue);
                }
            }
        });

        // build the gui.
        this.add(buildEditArea(), BorderLayout.CENTER);
    }

    //========================== Private Methods ================================//

    /**
     * Returns the max category value that should be accepted for the given histogrm.
     */
    private static int getMaxCategoryValue(Histogram histogram) {
        Node node = histogram.getSelectedVariable();
        if (node instanceof DiscreteVariable) {
            DiscreteVariable var = (DiscreteVariable) node;
            int categories = var.getCategories().size();
            return Math.max(categories, 2);
        }
        // otherwise allow up to 16.
        return 16;
    }



    private QQPlot getQQPlot() {
        return this.qqPlot;
    }


    private void changeQQPlot(QQPlot qqPlot) {
        this.qqPlot = qqPlot;
        // fire event
        this.firePropertyChange("histogramChange", null, qqPlot);
    }


    private static void setPreferredAsMax(JComponent component) {
        component.setMaximumSize(component.getPreferredSize());

    }


    private Box buildEditArea() {
        setPreferredAsMax(this.variableBox);

        Box main = Box.createVerticalBox();
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(new JLabel("Select Variable: "));
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(this.variableBox);
        hBox.add(Box.createHorizontalGlue());
        main.add(hBox);
        main.add(Box.createVerticalStrut(5));
        Box hBox2 = Box.createHorizontalBox();
        hBox2.add(Box.createHorizontalStrut(10));
        //hBox2.add(this.categoryField);
        hBox2.add(Box.createHorizontalGlue());
        main.add(hBox2);
        main.add(Box.createVerticalStrut(5));

        main.add(Box.createVerticalStrut(10));
        main.add(Box.createVerticalGlue());

        return main;
    }

    //========================== Inner classes ===========================//


    private static class VariableBoxRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Node node = (Node) value;
            if (node == null) {
                this.setText("");
            } else {
                this.setText(node.getName());
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }


}
