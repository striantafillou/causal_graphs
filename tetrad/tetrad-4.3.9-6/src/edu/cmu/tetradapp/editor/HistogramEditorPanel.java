package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.util.IntSpinner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tyler
 */
public class HistogramEditorPanel extends JPanel {


    /**
     * Combo box of all the variables.
     */
    private JComboBox variableBox;


    /**
     * The conditional variable section.
     */
    private JComboBox conditionalVariableBox;


    /**
     * The values that you can condition on.
     */
    private JComboBox conditionalValuesBox;


    /**
     * A spinner that deals with category selection.
     */
    private IntSpinner categorySelector;


    /**
     * The dataset being viewed.
     */
    private DataSet dataSet;


    /**
     * The discrete variables of the data set (may be empty).
     */
    private LinkedList<DiscreteVariable> variables = new LinkedList<DiscreteVariable>();


    /**
     * The condition editor (may be null).
     */
    private HistogramConditionEditorPanel conditionEditor;

    /**
     * The histogram we are working on.
     */
    private Histogram histogram;


    /**
     * Constructs the editor panel given the initial histogram and the dataset.
     *
     * @param histogram
     * @param dataSet
     */
    public HistogramEditorPanel(Histogram histogram, DataSet dataSet) {
        //   construct components
        this.setLayout(new BorderLayout());
        // first build histogram and components used in the editor.
        this.histogram = histogram;
        Node selected = histogram.getSelectedVariable();
        this.dataSet = dataSet;
        this.variableBox = new JComboBox();
        this.conditionalVariableBox = new JComboBox();
        this.conditionalValuesBox = new JComboBox();
        ListCellRenderer renderer = new VariableBoxRenderer();
        this.variableBox.setRenderer(renderer);
        this.conditionalVariableBox.setRenderer(renderer);
        for (Node node : dataSet.getVariables()) {
            this.variableBox.addItem(node);
            if (node == selected) {
                this.variableBox.setSelectedItem(node);
            }
            // only add discrete variables
            if (node instanceof DiscreteVariable) {
                this.variables.add((DiscreteVariable) node);
            }
        }
        this.variableBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Node node = (Node) e.getItem();
                    if (conditionEditor != null) {
                        conditionEditor.updateVariable(node);
                    }
                    Histogram newValue = new Histogram(HistogramEditorPanel.this.dataSet, node,
                            getHistogram().getNumberOfCategories());
                    categorySelector.setValue(newValue.getNumberOfCategories());
                 //   categorySelector.setMax(getMaxCategoryValue(newValue));
                    changeHistogram(newValue);
                }
            }
        });

        this.categorySelector = new IntSpinner(histogram.getNumberOfCategories(), 1, 3);
        this.categorySelector.setMin(2);
        this.categorySelector.setMax(getMaxCategoryValue(histogram));
        this.categorySelector.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner)e.getSource();
                Integer cat = (Integer)s.getValue();
                if(cat != getHistogram().getNumberOfCategories()){
                    changeHistogram(new Histogram(getHistogram(), cat));
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



    private Histogram getHistogram() {
        return this.histogram;
    }


    private void changeHistogram(Histogram histogram) {        
        this.histogram = histogram;
        this.categorySelector.setMax(getMaxCategoryValue(histogram));
        // fire event
        this.firePropertyChange("histogramChange", null, histogram);
    }


    private static void setPreferredAsMax(JComponent component) {
        component.setMaximumSize(component.getPreferredSize());

    }


    private Box buildEditArea() {
        setPreferredAsMax(this.variableBox);
        setPreferredAsMax(this.conditionalVariableBox);
        setPreferredAsMax(this.conditionalValuesBox);
        setPreferredAsMax(this.categorySelector);

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
        hBox2.add(new JLabel("Categories: "));
        //hBox2.add(this.categoryField);
        hBox2.add(this.categorySelector);
        hBox2.add(Box.createHorizontalGlue());
        main.add(hBox2);
        main.add(Box.createVerticalStrut(5));

        // only add this stuff if there are variables
        if (0 < this.variables.size()) {
            this.conditionEditor = new HistogramConditionEditorPanel(new ArrayList<DiscreteVariable>(this.variables), this.histogram.getSelectedVariable());
            this.conditionEditor.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("changeDependencies".equals(evt.getPropertyName())) {
                        Histogram histogram = new Histogram(HistogramEditorPanel.this.histogram,
                                (java.util.List<Histogram.ConditionalDependency>) evt.getNewValue());
                        changeHistogram(histogram);
                    }
                }
            });
            main.add(this.conditionEditor);
        }

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
