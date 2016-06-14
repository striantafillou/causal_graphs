package edu.cmu.tetradapp.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;


/**
 * A text field which is specialized for displaying and editing doubles. Handles
 * otherwise annoying GUI-related functions like keeping the textbox the right
 * size and listening to itself.
 *
 * @author Joseph Ramsey
 */
public final class DoubleTextField extends JTextField {

    /**
     * The current value of the text field.
     */
    private double value;

    /**
     * The number formatter for the number displayed.
     */
    private NumberFormat format;

    /**
     * If set, filters the value input by the user. (Side effects are allowed.)
     */
    private Filter filter;

    //============================CONSTRUCTORS=========================//

    /**
     * Constructs a new text field to display double values and allow them to be
     * edited. The initial value and character width of the text field can be
     * specified, along with the format with which the numbers should be
     * displayed. To accept only certain values, set a value filter using the
     * <code>setValueChecker</code> method.
     *
     * @param value  the initial value to be displayed.
     * @param width  the width (in characters) of the text field.
     * @param format the number formatter, for example new Decimal("0.0000").
     */
    public DoubleTextField(double value, int width, NumberFormat format) {
        super(width);
        setup(value, format);
    }

    //============================PUBLIC FIELDS=========================//

    /**
     * Sets the value of the text field to the given double value. Should
     * be overridden for more specific behavior.
     *
     * @param value the value to be set.
     */
    public void setValue(double value) {
        if (value == this.value) {
            return;
        }

        double newValue = filter(value, this.value);

        if (newValue == this.value) {
            smartSetText(format, this.value);
        }
        else {
            this.value = newValue;
            smartSetText(format, this.value);
            firePropertyChange("newValue", null, this.value);
        }
    }

    /**
     * Accesses the double value currently displayed.
     *
     * @return the current value.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets whether the given value should be accepted.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Convinces the text field to stay the right size in layouts that are
     * trying to expand it like a balloon by returning the preferred size.
     *
     * @return the maximum size.
     */
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    //==============================PRIVATE METHODS=======================//

    private double filter(double value, double oldValue) {
        if (filter == null) {
            return value;
        }

        return filter.filter(value, oldValue);
    }

    private void setup(double value, NumberFormat nf) {
        if (nf == null) {
            throw new NullPointerException();
        }

        this.value = filter(value, 0.0);
        this.format = nf;
        smartSetText(nf, this.value);

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(e.getActionCommand());
                    setValue(value);
                }
                catch (NumberFormatException e1) {
                    setText(format.format(getValue()));
//                    if ("".equals(getText().trim())) {
//                        setValue(Double.NaN);
//                    }
//                    else {
//                        setValue(getValue());
//                    }
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                DoubleTextField source = (DoubleTextField) e.getSource();

                if (source.isEditable()) {
                    source.selectAll();
                }
            }

            public void focusLost(FocusEvent e) {
                try {
                    double value = Double.parseDouble(getText());
                    setValue(value);
                }
                catch (NumberFormatException e1) {
                    if ("".equals(getText().trim())) {
                        setValue(Double.NaN);
                    }
                    else {
                        setValue(getValue());
                    }
                }
            }
        });
    }

    private void smartSetText(NumberFormat nf, double value) {
        if (Double.isNaN(value)) {
            setHorizontalAlignment(JTextField.RIGHT);
            setText("");
        }
        else {
            setHorizontalAlignment(JTextField.RIGHT);
            setText(nf.format(value));
        }
    }

    //==============================Interfaces============================//

    /**
     * Filters the given value, returning the value that should actually be
     * displayed. Typical use is to return either the value or the old value,
     * depending on whether the value is in range, though more complicated
     * uses are permitted. Side effects (such as storing the value in the
     * process of filtering it) are permitted.
     */
    public static interface Filter {

        /**
         * Filters the given value, returning the new value that should be
         * displayed.
         *
         * @param value The value entered by the user.
         * @param oldValue The value previously displayed, in case it needs
         * to be reverted to.
         */
        double filter(double value, double oldValue);
    }
}
