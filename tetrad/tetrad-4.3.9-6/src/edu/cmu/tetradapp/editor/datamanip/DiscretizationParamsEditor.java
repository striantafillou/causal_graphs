///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.editor.datamanip;

import edu.cmu.tetrad.data.ContinuousDiscretizationSpec;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.editor.FinalizingParameterEditor;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.datamanip.DiscretizationParams;
import edu.cmu.tetradapp.util.IntSpinner;
import edu.cmu.tetradapp.util.LayoutUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Allows the user to specify how a selected list of columns should be
 * discretized.
 *
 * @author Tyler Gibson
 * @author Joseph Ramsey
 */
public class DiscretizationParamsEditor extends JPanel implements FinalizingParameterEditor {

    /**
     * The data set that will be discretized.
     */
    private DataSet sourceDataSet;

    /**
     * A map from nodes to their editors.
     */
    private Map<Node, ContinuousDiscretizationEditor> nodeEditors = new HashMap<Node, ContinuousDiscretizationEditor>();


    /**
     * A list to display all the variables.
     */
    private JList variableList;


    /**
     * The list of variables to discretize.
     */
    private JList discretizeVariableList;


    /**
     * The params we are editing.
     */
    private DiscretizationParams params;


    /**
     * A tabbed pane to store the editors in.
     */
    private JTabbedPane editorPane;


    /**
     * Constructs a new editor that will allow the user to specify how to
     * discretize each of the columns in the given list. The editor will return
     * the discretized data set.
     */
    public DiscretizationParamsEditor() {

    }

    //============================= Public Methods ===================================//


    /**
     * Sets up the GUI.
     */
    public void setup() {
        final List<Node> variables = this.sourceDataSet.getVariables();
        Set<Node> previousNodes = this.params.getSpecs().keySet();
        List<Node> nondiscretizeVars = new LinkedList<Node>();
        List<Node> discretizeVars = new LinkedList<Node>();
        for (Node node : variables) {
            if (node instanceof ContinuousVariable && previousNodes.contains(node)) {
                discretizeVars.add(node);
            } else if (node instanceof ContinuousVariable) {
                nondiscretizeVars.add(node);
            }
        }

        // create discretized var list.
        this.discretizeVariableList = new JList(new VariableListModel(discretizeVars));
        this.discretizeVariableList.setCellRenderer(new VariableBoxRenderer());
        this.discretizeVariableList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.discretizeVariableList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                JList list = (JList) e.getSource();
                List<Node> selected = getSelected(list);
                if (selected.size() == 1) {
                    editorPane.removeAll();
                    Node node = selected.get(0);
                    editorPane.add(node.getName(), nodeEditors.get(node));
                } else if (1 < selected.size()) {
                    editorPane.removeAll();
                    Node first = selected.get(0);
                    Node last = selected.get(selected.size() - 1);
                    String label = first.getName() + " - " + last.getName();
                    editorPane.add(label, new VariableSelectionEditor(selected));
                }
            }
        });
        // Add entries for previously selected variables.
        for (Node node : discretizeVars) {
            ContinuousVariable continuousVariable = (ContinuousVariable) node;
            ContinuousDiscretizationEditor editor =
                    new ContinuousDiscretizationEditor(sourceDataSet, continuousVariable,
                            2, ContinuousDiscretizationEditor.Method.EVENLY_DIVIDED_INTERNVALS);
            editor.setDiscretizationSpec(this.params.getSpecs().get(node));
            this.nodeEditors.put(node, editor);
        }


        this.variableList = new JList(new VariableListModel(nondiscretizeVars));
        this.variableList.setCellRenderer(new VariableBoxRenderer());

        // set up the tabbed pane
        this.editorPane = new JTabbedPane();

        JScrollPane editorScrollPane = new JScrollPane(this.editorPane);
        editorScrollPane.setPreferredSize(new Dimension(400, 350));

        JCheckBox copyUnselectedCheckBox =
                new JCheckBox("Copy unselected columns into new data set");
        copyUnselectedCheckBox.setHorizontalTextPosition(AbstractButton.LEFT);
        copyUnselectedCheckBox.setSelected(Preferences.userRoot().getBoolean(
                "copyUnselectedColumns", false));
        copyUnselectedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                Preferences.userRoot().putBoolean("copyUnselectedColumns",
                        checkBox.isSelected());
            }
        });

        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createHorizontalStrut(5));

        // build the continuous variable box.
        Box selectionBox = Box.createVerticalBox();
        JLabel label = new JLabel("Continuous Variables:");

        JScrollPane variableListPane = new JScrollPane(this.variableList);
        int width1 = Math.max(100, label.getPreferredSize().width);
        LayoutUtils.setAllSizes(variableListPane, new Dimension(width1, 350 - label.getPreferredSize().height));

        selectionBox.add(Box.createVerticalStrut(5));
        selectionBox.add(LayoutUtils.leftAlignJLabel(label));
        selectionBox.add(variableListPane);
        selectionBox.add(Box.createVerticalGlue());

        // build the discrete variable box
        Box discreteSelectionBox = Box.createVerticalBox();
        JLabel discreteLabel = new JLabel("Variables to Discretize:");

        JScrollPane discreteListPane = new JScrollPane(this.discretizeVariableList);
        int width2 = Math.max(100, discreteLabel.getPreferredSize().width);
        LayoutUtils.setAllSizes(discreteListPane, new Dimension(width2, 350 - discreteLabel.getPreferredSize().height));

        discreteSelectionBox.add(Box.createVerticalStrut(5));
        discreteSelectionBox.add(LayoutUtils.leftAlignJLabel(discreteLabel));
        discreteSelectionBox.add(discreteListPane);
        discreteSelectionBox.add(Box.createVerticalGlue());

        hBox.add(selectionBox);
        hBox.add(Box.createHorizontalStrut(4));
        hBox.add(createMoveButtons());
        hBox.add(Box.createHorizontalStrut(4));
        hBox.add(discreteSelectionBox);
        hBox.add(Box.createHorizontalStrut(8));

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(5));
        vBox.add(editorScrollPane);

        Box b4 = Box.createHorizontalBox();
        b4.add(Box.createHorizontalGlue());
        b4.add(copyUnselectedCheckBox);

        vBox.add(b4);
        vBox.add(Box.createVerticalStrut(10));

        hBox.add(vBox);
        hBox.add(Box.createHorizontalStrut(5));

        add(hBox, BorderLayout.CENTER);
    }


    /**
     * Adds all the discretization info to the params.
     *
     * @return true iff the edit was finalized.
     */
    public boolean finalizeEdit() {
        // if there was no editors, then nothing can be done so return false.
        if (this.nodeEditors.isEmpty()) {
            return false;
        }
        Map<Node, ContinuousDiscretizationSpec> map = new HashMap<Node, ContinuousDiscretizationSpec>();
        for (Node node : this.nodeEditors.keySet()) {
            ContinuousDiscretizationEditor editor = this.nodeEditors.get(node);
            map.put(node, editor.getDiscretizationSpec());
        }
        this.params.setSpecs(map);
        return true;
    }


    /**
     * Sets the previous params, must be <code>DiscretizationParams</code>.
     *
     * @param params
     */
    public void setParams(Params params) {
        this.params = (DiscretizationParams) params;
    }

    /**
     * The parant model should be a <code>DataWrapper</code>.
     *
     * @param parentModels
     */
    public void setParentModels(Object[] parentModels) {
        if (parentModels == null || parentModels.length == 0) {
            throw new IllegalArgumentException("There must be parent model");
        }
        DataWrapper data = null;
        for (Object parent : parentModels) {
            if (parent instanceof DataWrapper) {
                data = (DataWrapper) parent;
            }
        }
        if (data == null) {
            throw new IllegalArgumentException("Should have have a data wrapper as a parent");
        }
        DataModel model = data.getSelectedDataModel();
        if (!(model instanceof DataSet)) {
            throw new IllegalArgumentException("The dataset must be a rectangular dataset");
        }
        this.sourceDataSet = (DataSet) model;
    }

    /**
     * Returns true
     *
     * @return - true
     */
    public boolean mustBeShown() {
        return true;
    }

    //=============================== Private Methods ================================//


    private Component getViewingComponent() {
        int tabs = editorPane.getTabCount();
        if (0 < tabs) {
            return editorPane.getComponentAt(0);
        }
        return null;
    }


    private void clearSelection() {
        this.variableList.clearSelection();
        this.discretizeVariableList.clearSelection();
    }


    private Box createMoveButtons() {
        Box box = Box.createVerticalBox();
        JButton add = new JButton(">");
        JButton remove = new JButton("<");

        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<Node> selected = getSelected(variableList);
                VariableListModel varModel = (VariableListModel) variableList.getModel();
                VariableListModel discreteModel = (VariableListModel) discretizeVariableList.getModel();
                // add components
                for (Node node : selected) {
                    nodeEditors.put(node, createEditor(node));
                }
                // change models
                if (!selected.isEmpty()) {
                    discreteModel.addAll(selected);
                    varModel.removeAll(selected);
                    clearSelection();
                }
            }
        });

        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<Node> selected = getSelected(discretizeVariableList);
                VariableListModel varModel = (VariableListModel) variableList.getModel();
                VariableListModel discreteModel = (VariableListModel) discretizeVariableList.getModel();
                // remove components from map
                Component comp = getViewingComponent();
                if (comp instanceof ContinuousDiscretizationEditor) {
                    ContinuousDiscretizationEditor editor = (ContinuousDiscretizationEditor) comp;
                    if (selected.contains(editor.getVariable())) {
                        editorPane.removeAll();
                    }

                } else {
                    VariableSelectionEditor editor = (VariableSelectionEditor) comp;
                    if (editor.contains(selected)) {
                        editorPane.removeAll();
                    }
                }
                for (Node node : selected) {
                    nodeEditors.remove(node);
                }
                // adjust models
                if (!selected.isEmpty()) {
                    discreteModel.removeAll(selected);
                    varModel.addAll(selected);
                    clearSelection();
                }
            }
        });

        box.add(Box.createVerticalStrut(100));
        box.add(add);
        box.add(Box.createVerticalStrut(3));
        box.add(remove);
        box.add(Box.createVerticalGlue());

        return box;
    }


    private static List<Node> getSelected(JList list) {
        Object[] selected = list.getSelectedValues();
        List<Node> nodes = new LinkedList<Node>();
        if (selected != null) {
            for (Object o : selected) {
                nodes.add((Node) o);
            }
        }
        return nodes;
    }


    private ContinuousDiscretizationEditor createEditor(Node node) {
        return new ContinuousDiscretizationEditor(this.sourceDataSet, (ContinuousVariable) node,
                3, ContinuousDiscretizationEditor.Method.EVENLY_DIVIDED_INTERNVALS);
    }


    private boolean globalChangeVerification() {
        if (!Preferences.userRoot().getBoolean("ignoreGlobalDiscretizationWarning", false)) {
            Box box = Box.createVerticalBox();
            String message = "<html>This action will change the number of categories for all selected variales<br>" +
                    "and override any previous work. Are you sure you want continue?</html>";
            box.add(new JLabel(message));
            box.add(Box.createVerticalStrut(5));
            JCheckBox checkBox = new JCheckBox("Don't show this again");
            checkBox.setHorizontalTextPosition(AbstractButton.LEFT);
            checkBox.setHorizontalAlignment(AbstractButton.RIGHT);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox box = (JCheckBox) e.getSource();
                    Preferences.userRoot().putBoolean("ignoreGlobalDiscretizationWarning", box.isSelected());
                }
            });
            box.add(checkBox);
            box.add(Box.createVerticalStrut(5));
            int option = JOptionPane.showConfirmDialog(this, box, "Discretization Warning", JOptionPane.YES_NO_OPTION);
            return JOptionPane.YES_OPTION == option;
        }
        return true;
    }

    /**
     * Changes the number of categories on the editors for the given nodes.
     */
    private void changeNumberOfCategories(int numOfCats, List<Node> nodes) {
        for (Node node : nodes) {
            ContinuousDiscretizationEditor editor = this.nodeEditors.get(node);
            if (editor != null) {
                editor.setNumCategories(numOfCats);
            }
        }
    }


    /**
     * Changes the method of the editor.
     */
    private void changeMethod(List<Node> nodes, ContinuousDiscretizationEditor.Method method) {
        for (Node node : nodes) {
            ContinuousDiscretizationEditor editor = this.nodeEditors.get(node);
            if (editor != null) {
                editor.setMethod(method);
            }
        }
    }


    /**
     * Returns the common mehtod if there is one.
     */
    private ContinuousDiscretizationEditor.Method getCommonMethod(List<Node> nodes) {
        ContinuousDiscretizationEditor.Method method = null;
        for (Node node : nodes) {
            ContinuousDiscretizationEditor editor = this.nodeEditors.get(node);
            if(method != null && method != editor.getMethod()){
                return null;
            }
            method = editor.getMethod();
        }
        return method;
    }


    /**
     * Returns the default category num to use for the given nodes. If they all have the same
     * number then its returned otherwise 3 is returned (or something else?)
     */
    private int getDefaultCategoryNum(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return 3;
        }
        ContinuousDiscretizationEditor editor = this.nodeEditors.get(nodes.get(0));
        int value = editor.getNumCategories();
        for (int i = 1; i < nodes.size(); i++) {
            editor = this.nodeEditors.get(nodes.get(i));
            if (value != editor.getNumCategories()) {
                return 3;
            }
        }
        return value;
    }

    //============================= Inner class ===============================//


    /**
     * Editor that edits a collection of variables.
     */
    private class VariableSelectionEditor extends JPanel {

        private List<Node> nodes;

        public VariableSelectionEditor(List<Node> vars) {
            setLayout(new BorderLayout());
            this.nodes = vars;
            IntSpinner spinner = new IntSpinner(getDefaultCategoryNum(vars), 1, 3);
            ContinuousDiscretizationEditor.Method method = getCommonMethod(vars);
            spinner.setMin(2);
            spinner.setFilter(new IntSpinner.Filter() {
                public int filter(int oldValue, int newValue) {
                    if (globalChangeVerification()) {
                        changeNumberOfCategories(newValue, nodes);
                        return newValue;
                    }
                    return oldValue;
                }
            });

            Box vBox = Box.createVerticalBox();

            vBox.add(new JLabel("Discretization Method: "));

            JRadioButton equalInterval = new JRadioButton("Evenly Distributed Intervals",
                    method == ContinuousDiscretizationEditor.Method.EVENLY_DIVIDED_INTERNVALS);
            JRadioButton equalBuckets = new JRadioButton("Evenly Distributed Values",
                    method == ContinuousDiscretizationEditor.Method.EQUAL_SIZE_BUCKETS);
            equalInterval.setHorizontalTextPosition(AbstractButton.RIGHT);
            equalBuckets.setHorizontalTextPosition(AbstractButton.RIGHT);

            equalInterval.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changeMethod(nodes, ContinuousDiscretizationEditor.Method.EVENLY_DIVIDED_INTERNVALS);
                }
            });

            equalBuckets.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changeMethod(nodes, ContinuousDiscretizationEditor.Method.EQUAL_SIZE_BUCKETS);
                }
            });

            ButtonGroup group = new ButtonGroup();
            group.add(equalInterval);
            group.add(equalBuckets);

            vBox.add(equalInterval);
            vBox.add(equalBuckets);

            Box buttons = Box.createHorizontalBox();
            buttons.add(vBox);
            buttons.add(Box.createHorizontalGlue());
            buttons.setBorder(new EmptyBorder(15, 5, 5, 5));

            Box cats = Box.createHorizontalBox();
            cats.add(new JLabel(" Change number of categories: "));
            cats.add(spinner);
            cats.add(Box.createHorizontalGlue());
            cats.setBorder(new EmptyBorder(5, 5, 5, 5));

            Box vBox1 = Box.createVerticalBox();
            vBox1.add(buttons);
            vBox1.add(cats);
            vBox1.add(Box.createVerticalGlue());

            this.add(vBox1, BorderLayout.NORTH);
        }


        public boolean contains(List<Node> nodes) {
            for (Node node : nodes) {
                if (this.nodes.contains(node)) {
                    return true;
                }
            }
            return false;
        }

    }


    private static class VariableListModel extends AbstractListModel {

        private Vector<Node> variables;


        public VariableListModel(List<Node> variables) {
            this.variables = new Vector<Node>(variables);
        }


        public int getSize() {
            return this.variables.size();
        }

        public Object getElementAt(int index) {
            return this.variables.get(index);
        }

        public void removeAll(List<Node> nodes) {
            int size = getSize();
            this.variables.removeAll(nodes);
            this.fireIntervalRemoved(this, 0, size - 1);
        }

        public void add(Node node) {
            if (!this.variables.contains(node)) {
                this.variables.add(node);
                this.fireIntervalAdded(this, getSize() - 2, getSize() - 1);
            }
        }


        public void addAll(List<Node> nodes) {
            for (Node node : nodes) {
                if (!this.variables.contains(node)) {
                    this.variables.add(node);
                }
            }
            this.fireIntervalAdded(this, getSize() - nodes.size(), nodes.size() - 1);
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



