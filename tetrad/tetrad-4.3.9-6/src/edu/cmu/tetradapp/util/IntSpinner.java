package edu.cmu.tetradapp.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A JSpinner that uses a <code>IntTextField</code> as its editor.  When changes are made from the
 * editor or the spinner a property change event is invoked with the property name "changedIntValue" and
 * the new value (old value is not given).
 *
 * @author Tyler Gibson
 */
public class IntSpinner extends JSpinner {


    /**
     * The eidtor
     */
    private IntTextField editor;

    /**
     * Filter to use.
     */
    private Filter filter;


    /**
     * Min value
     */
    private Integer min;

    /**
     * Max value
     */
    private Integer max;

    /**
     * Constructs the int spinner given the initial value, the min value to accept,
     * the max value to accept and the step.
     *
     * @param value - Initial value to dispaly
     * @param step  - The step (the amount that is "jumped" when the spinner is activated)
     * @param size  - The size of the int text field.
     */
    public IntSpinner(Integer value, Integer step, Integer size) {
        super(new SpinnerNumberModel(value, null, null, step));
        this.editor = new IntTextField(value, size);
        // make the spinner a bit bigger than the text field (looks better)
        this.setPreferredSize(increment(this.editor.getPreferredSize(), 2));
        this.setMaximumSize(increment(this.editor.getMaximumSize(), 2));
        this.setMinimumSize(increment(this.editor.getMinimumSize(), 2));
        this.setSize(increment(this.editor.getSize(), 2));

        this.editor.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (min != null && value < min) {
                    value = min;
                } else if (max != null && max < value) {
                    value = max;
                }
                return value;
            }
        });

        // Can't do this in the filter, due to other events calling the filter
        this.editor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IntTextField field = (IntTextField) e.getSource();
                String text = field.getText();
                try {
                    // parse value and let the field filter it.
                    int value = Integer.parseInt(text);
                    field.setValue(value);
                    value = field.getValue();
                    if (!IntSpinner.this.getValue().equals(value)) {
                        IntSpinner.this.setValue(value);
                    }
                } catch (Exception ex) {
                    // do nothing in this case
                }
            }
        });

        this.setEditor(this.editor);
    }

    //=========================== Public Methods ============================//

    public void setMin(Integer min) {
        if (this.min != min) {
            this.min = min;
            SpinnerNumberModel model = (SpinnerNumberModel) this.getModel();
            if (min == null) {
                model.setMinimum(null);
            } else {
                model.setMinimum(min);
            }
            // update the text filed by resetting value
            this.editor.setValue(this.editor.getValue());
        }
    }


    /**
     * Sets the max value for the spinner.
     *
     * @param max
     */
    public void setMax(Integer max) {
        if (this.max != max) {
            this.max = max;
            SpinnerNumberModel model = (SpinnerNumberModel) this.getModel();
            if (max == null) {
                model.setMaximum(null);
            } else {
                model.setMaximum(max);
            }
            // update the text filed by resetting value
            this.editor.setValue(this.editor.getValue());
        }
    }


    public void setFilter(Filter filter) {
        this.filter = filter;
    }


    public void setValue(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }

        int value = filter((Integer) object);
        if (!getValue().equals(value)) {
            super.setValue(value);
            editor.setUnfilteredValue(value);
        }
    }


    private static Dimension increment(Dimension dim, int increment) {
        return new Dimension(dim.width + increment, dim.height + increment);
    }

    //=========================== private methods ======================//


    private int filter(int value) {
        if (this.filter == null) {
            return value;
        }
        if (getValue().equals(value)) {
            return value;
        }
        return this.filter.filter((Integer) getValue(), value);
    }

    //============================ Inner classer ====================================//


    public static interface Filter {

        public int filter(int oldValue, int newValue);

    }

}
