package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * Add description.
 *
 * @author tyler
 */
public class HistogramConditionEditorPanel extends JPanel {


    /**
     * The variables we are allowed to condition on.
     */
    private final List<DiscreteVariable> variables;


    /**
     * The panel that the variables are in.
     */
    private final Box variablePanel = Box.createVerticalBox();


    /**
     * Lists used to store the component rows
     */
    private final List<JComboBox> variableBoxes = new LinkedList<JComboBox>();
    private final List<JComboBox> variableValueBoxes = new LinkedList<JComboBox>();
    private final List<JCheckBox> checkBoxes = new LinkedList<JCheckBox>();
    private final List<Box> row = new LinkedList<Box>();

    /**
     * The variable that the histogram is giving a view of.
     */
    private int forbidden = -1;


    /**
     * The button's box.
     */
    private final Box buttonBox = Box.createHorizontalBox();


    /**
     * Constructs the condition editor given a list of discrete variables (that one is allowed to condition on)
     * and the node that is currently selected
     *
     * @param variables
     * @param selected
     */
    public HistogramConditionEditorPanel(List<DiscreteVariable> variables, Node selected) {
        if (variables == null) {
            throw new NullPointerException("Given value(s) null");
        }
        if (selected instanceof DiscreteVariable) {
            //noinspection SuspiciousMethodCalls
            this.forbidden = variables.indexOf(selected);
        }
        // init variables.
        this.variables = variables;
        // build initial gui
        TitledBorder border = new TitledBorder("Condition on");
        border.setTitleColor(Color.BLACK);
        this.variablePanel.setBorder(border);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton add = createButton("Add Condition");
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSelectionBox();
                fireDependencyChange();
            }
        });
        JButton remove = createButton("Remove");
        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeDependencies();
            }
        });

        this.buttonBox.add(Box.createHorizontalGlue());
        this.buttonBox.add(Box.createHorizontalStrut(3));
        this.buttonBox.add(add);
        this.buttonBox.add(Box.createHorizontalStrut(3));
        this.buttonBox.add(remove);
        this.buttonBox.add(Box.createHorizontalStrut(3));
        this.buttonBox.add(Box.createHorizontalGlue());

        this.variablePanel.add(this.buttonBox);

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalStrut(10));
        box2.add(this.variablePanel);
        box2.add(Box.createHorizontalStrut(10));
        this.add(box2);
    }

    //========================================= Public Methods =================================//


    /**
     * Makes appropriate updates to the condition editor when the selected node is changed.
     *
     * @param node
     */
    public void updateVariable(Node node) {
        if (node instanceof DiscreteVariable) {
            //noinspection SuspiciousMethodCalls
            this.forbidden = this.variables.indexOf(node);
        } else {
            this.forbidden = -1;
        }
        removeAllConditions();
        this.revalidate();
        this.repaint();
    }

    //========================================== Private Methods ==============================//


    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(2, 5, 2, 5));
        Font current = button.getFont();
        button.setFont(current.deriveFont(Font.PLAIN, 10F));
        return button;
    }


    /**
     * Fires the "changeDependencies" event with a new list of dependencies.
     */
    private synchronized void fireDependencyChange() {
        //List<Histogram.ConditionalDependency> nodes = new LinkedList<Histogram.ConditionalDependency>();
        Map<Node, List<String>> map = new HashMap<Node, List<String>>(this.variableBoxes.size());
        for (int i = 0; i < this.variableBoxes.size(); i++) {
            JComboBox box = this.variableBoxes.get(i);
            JComboBox valuesBox = this.variableValueBoxes.get(i);
            Object selected = box.getSelectedItem();
            Object selectedValue = valuesBox.getSelectedItem();
            if (selected != null && selectedValue != null) {
                Node variable = (Node) selected;
                //int column = this.dataSet.getColumn(variable);
                List<String> values = map.get(variable);
                if (values == null) {
                    values = new ArrayList<String>(3);
                    values.add(selectedValue.toString());
                    map.put(variable, values);
                } else {
                    values.add(selectedValue.toString());
                }
            }
        }
        List<Histogram.ConditionalDependency> nodes = new ArrayList<Histogram.ConditionalDependency>(map.size());
        for (Map.Entry<Node, List<String>> entry : map.entrySet()) {
            List<String> value = entry.getValue();
            nodes.add(new Histogram.ConditionalDependency(entry.getKey(), value));
        }
        this.firePropertyChange("changeDependencies", null, nodes);
    }


    /**
     * Removes all rows where the box is checked.
     */
    private synchronized void removeDependencies() {
        for (int i = this.checkBoxes.size() - 1; 0 <= i; i--) {
            JCheckBox checkBox = this.checkBoxes.get(i);
            if (checkBox.isSelected()) {
                // remove them
                removeComponents(i);
            }
        }
        this.variablePanel.revalidate();
        this.revalidate();
        this.repaint();
        fireDependencyChange();
    }

    /**
     * Removes the components at the given index.
     */
    private void removeComponents(int i) {
        this.checkBoxes.remove(i);
        this.variableBoxes.remove(i);
        this.variableValueBoxes.remove(i);
        Box box = this.row.remove(i);
        this.variablePanel.remove(box);
    }


    private void removeAllConditions() {
        for(int i = this.row.size() -1; 0 <= i; i--){
            removeComponents(i);
        }
    }


    private int availableVariableSize() {
        if (this.forbidden == -1) {
            return this.variables.size();
        }
        return this.variables.size() - 1;
    }


    /**
     * Adds a selection box
     */
    private boolean addSelectionBox() {
        if (this.variableBoxes.size() < this.availableVariableSize()) {
            Box box = Box.createHorizontalBox();
            box.add(Box.createHorizontalGlue());
            box.add(Box.createHorizontalStrut(5));
            JComboBox variableBox = createVariableBox(this.variables);
            this.variableBoxes.add(variableBox);
            int anIndex = this.variableBoxes.size() - 1;
            variableBox.setSelectedIndex(anIndex);
            variableBox.addItemListener(new ConditionItemListener());
            box.add(variableBox);

            box.add(new JLabel(" = "));
            JComboBox comboBox = createVariableValuesBox(this.variables.get(anIndex));
            comboBox.addItemListener(new ConditionItemListener());
            this.variableValueBoxes.add(comboBox);
            setPreferredAsMax(comboBox);
            box.add(comboBox);
            box.add(Box.createHorizontalStrut(10));

            JCheckBox checkBox = new JCheckBox();
            this.checkBoxes.add(checkBox);
            box.add(checkBox);
            box.add(Box.createHorizontalStrut(5));
            box.add(Box.createHorizontalGlue());

            this.row.add(box);
            this.variablePanel.add(box);
            // remove and readd so its on the button
            this.variablePanel.remove(this.buttonBox);
            this.variablePanel.add(this.buttonBox);


            this.variablePanel.revalidate();
            this.revalidate();
            this.repaint();
            return true;
        }
        return false;
    }


    private static JComboBox createVariableValuesBox(DiscreteVariable variable) {
        JComboBox box = new JComboBox();
        for (String c : variable.getCategories()) {
            box.addItem(c);
        }
        setPreferredAsMax(box);
        return box;
    }


    private JComboBox createVariableBox(List<DiscreteVariable> variables) {
        JComboBox box = new JComboBox();
        for (int i = 0; i < variables.size(); i++) {
            if (this.forbidden != i) {
                box.addItem(variables.get(i));
            }
        }
        box.setRenderer(new VariableBoxRenderer());
        setPreferredAsMax(box);
        return box;
    }


    private static void setPreferredAsMax(JComponent component) {
        component.setMaximumSize(component.getPreferredSize());
    }

    //============================================ Inner class ================================//


    private class ConditionItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                fireDependencyChange();
            }
        }
    }


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
