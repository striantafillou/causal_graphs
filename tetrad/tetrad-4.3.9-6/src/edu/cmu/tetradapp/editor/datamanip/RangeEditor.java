package edu.cmu.tetradapp.editor.datamanip;

import edu.cmu.tetrad.data.ContinuousDiscretizationSpec;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.StringTextField;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An editor that allows one to edit a range while discretizing continuous data.
 *
 * @author Joseph Ramsey
 * @author Tyler Gibson
 */
@SuppressWarnings({"SuspiciousMethodCalls"})
final class RangeEditor extends JComponent {

    /**
     * The variable under consideration.
     */
    private Variable variable;

    /**
     * The breakpoints to utilize, this may be user defined or calculated by the editor.
     */
    private final double[] breakpoints;

    /**
     * The categories whos ranges are being edited.
     */
    private final List<String> categories;

    /**
     * The text fields that allow one to edit the name of the category.
     */
    private StringTextField[] categoryFields;

    /**
     * The fields that allows a user to edit the lower bound on the range.
     */
    private DoubleTextField[] leftRangeFields;

    /**
     * The fields that allow a user to edit the upper bound on the range.
     */
    private DoubleTextField[] rightRangeFields;


    /**
     * Used to keep track of which compoent has focus.
     */
    private LinkedList<JTextField> focusTraveralOrder = new LinkedList<JTextField>();

    /**
     * Label map.
     */
    private Map<Object, Integer> labels = new HashMap<Object, Integer>();


    /**
     * States whether the editor is editable.
     */
    private boolean editableRange;

    /**
     * Contructs the range editor given the variable that is being edited and
     * the continuous discreitization spec to base initial values on.
     *
     * @param variable
     * @param spec
     * @param editable
     */
    public RangeEditor(Variable variable, ContinuousDiscretizationSpec spec, boolean editable) {
        this.variable = variable;
        this.breakpoints = spec.getBreakpoints();
        this.categories = spec.getCategories();
        this.editableRange = editable;
        buildEditor();
    }

    //=============================== Public Methods ===========================//


    /**
     * Sets whether the editor is editable or not.
     *
     * @param editableRange
     */
    public void setEditableRange(boolean editableRange) {
        this.editableRange = editableRange;
        this.removeAll();
        buildEditor();
    }



    /**
     * Returns the <code>ContinuousDiscretizationSpec</code> that has been
     * created by the user.
     *
     * @return
     */
    public ContinuousDiscretizationSpec getDiscretizationSpec() {
        return new ContinuousDiscretizationSpec(this.breakpoints,
                this.categories);
    }

    //================================= Private Methods ==========================//


    /**
     * Builds the editor.
     */
    private void buildEditor() {
        Box rangeEditor = Box.createVerticalBox();

        createCategoryFields();
        createRangeFields();

        for (int i = 0; i < categories.size(); i++) {
            Box row = Box.createHorizontalBox();
            row.add(Box.createRigidArea(new Dimension(10, 0)));

            row.add(new JLabel((i + 1) + ". "));
            row.add(this.categoryFields[i]);
            row.add(new BigLabel(" = [ "));
            row.add(this.leftRangeFields[i]);
            row.add(new BigLabel(", "));
            row.add(this.rightRangeFields[i]);

            if (i < categories.size() - 1) {
                row.add(new BigLabel(" )"));
            } else {
                row.add(new BigLabel(" ]"));
            }

            row.add(Box.createHorizontalGlue());
            rangeEditor.add(row);
        }

        setLayout(new BorderLayout());
        add(rangeEditor, BorderLayout.CENTER);

        setFocusTraversalPolicy(new MyFocusTraversalPolicy());

        setFocusCycleRoot(true);
    }

    /**
     * Creates the category fields, these are allowed to be edited even when editable is false.
     */
    private void createCategoryFields() {
        this.categoryFields = new StringTextField[getNumCategories()];

        for (int i = 0; i < getNumCategories(); i++) {
            String category = this.categories.get(i);
            this.categoryFields[i] = new StringTextField(category, 6);
            final StringTextField _field = this.categoryFields[i];

            this.categoryFields[i].setFilter(new StringTextField.Filter() {
                public String filter(String value, String oldValue) {
                    if (labels.get(_field) != null) {
                        int index = labels.get(_field);

                        if (value == null) {
                            value = categories.get(index);
                        }

                        for (int i = 0; i < categories.size(); i++) {
                            if (i != index &&
                                    categories.get(i).equals(value)) {
                                value = categories.get(index);
                            }
                        }

                        categories.set(index, value);
                    }

                    return value;
                }
            });

            labels.put(this.categoryFields[i], i);
            this.focusTraveralOrder.add(this.categoryFields[i]);
        }
    }

    /**
     * Creates the range fields, if the editor is not editable then all these fields should
     * be not editable.
     */
    private void createRangeFields() {
        this.leftRangeFields = new DoubleTextField[getNumCategories()];
        this.rightRangeFields = new DoubleTextField[getNumCategories()];

        int maxCategory = getNumCategories() - 1;

        this.leftRangeFields[0] = new DoubleTextField(
                Double.NEGATIVE_INFINITY, 6, NumberFormatUtil.getInstance().getNumberFormat());
        this.leftRangeFields[0].setFilter(
                new DoubleTextField.Filter() {
                    public double filter(double value, double oldValue) {
                        return oldValue;
                    }
                });

        this.rightRangeFields[maxCategory] = new DoubleTextField(
                Double.POSITIVE_INFINITY, 6, NumberFormatUtil.getInstance().getNumberFormat());
        this.rightRangeFields[maxCategory].setFilter(
                new DoubleTextField.Filter() {
                    public double filter(double value, double oldValue) {
                        return oldValue;
                    }
                });

        this.leftRangeFields[0].setEditable(false);
        this.rightRangeFields[maxCategory].setEditable(false);
        this.leftRangeFields[0].setHorizontalAlignment(JTextField.CENTER);
        this.rightRangeFields[maxCategory].setHorizontalAlignment(
                JTextField.CENTER);

        for (int i = 0; i < getNumCategories() - 1; i++) {
            this.rightRangeFields[i] = new DoubleTextField(breakpoints[i], 6, NumberFormatUtil.getInstance().getNumberFormat());
            this.rightRangeFields[i].setEditable(false);
            labels.put(this.rightRangeFields[i], i);

            this.leftRangeFields[i + 1] = new DoubleTextField(breakpoints[i], 6, NumberFormatUtil.getInstance().getNumberFormat());
            this.leftRangeFields[i + 1].setEditable(this.editableRange);
            this.labels.put(this.leftRangeFields[i + 1], i + 1);

            final Object label = labels.get(this.leftRangeFields[i + 1]);
            this.leftRangeFields[i + 1].setFilter(
                    new DoubleTextField.Filter() {
                        public double filter(double value,
                                             double oldValue) {
                            if (label == null) {
                                return oldValue;
                            }

                            int index = (Integer) label;

                            if (index - 1 > 0 &&
                                    !(breakpoints[index - 2] < value)) {
                                value = breakpoints[index - 1];
                            }

                            if (index - 1 < breakpoints.length - 1 &&
                                    !(value < breakpoints[index])) {
                                value = breakpoints[index - 1];
                            }

                            breakpoints[index - 1] = value;

                            getRightRangeFields()[index - 1].setValue(
                                    value);
                            return value;
                        }
                    });


            labels.put(this.leftRangeFields[i + 1], i + 1);
            this.focusTraveralOrder.add(this.leftRangeFields[i + 1]);
        }
    }

    private DoubleTextField[] getRightRangeFields() {
        return this.rightRangeFields;
    }

    public Variable getVariable() {
        return variable;
    }

    private int getNumCategories() {
        return this.categories.size();
    }

    //========================== Inner Class ====================================//

    private final static class BigLabel extends JLabel {
        private static Font FONT = new Font("Dialog", Font.BOLD, 20);

        public BigLabel(String text) {
            super(text);
            setFont(FONT);
        }
    }

    private class MyFocusTraversalPolicy extends FocusTraversalPolicy {
        public Component getComponentAfter(Container focusCycleRoot,
                                           Component aComponent) {
            int index = focusTraveralOrder.indexOf(aComponent);
            int size = focusTraveralOrder.size();

            if (index != -1) {
                return focusTraveralOrder.get((index + 1) % size);
            } else {
                return getFirstComponent(focusCycleRoot);
            }
        }

        public Component getComponentBefore(Container focusCycleRoot,
                                            Component aComponent) {
            int index = focusTraveralOrder.indexOf(aComponent);
            int size = focusTraveralOrder.size();

            if (index != -1) {
                return focusTraveralOrder.get((index - 1) % size);
            } else {
                return getFirstComponent(focusCycleRoot);
            }
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return focusTraveralOrder.getFirst();
        }

        public Component getLastComponent(Container focusCycleRoot) {
            return focusTraveralOrder.getLast();
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return getFirstComponent(focusCycleRoot);
        }
    }
}
