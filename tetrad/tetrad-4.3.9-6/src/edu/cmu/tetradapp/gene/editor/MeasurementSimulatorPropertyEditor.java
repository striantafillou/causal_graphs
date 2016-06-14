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

package edu.cmu.tetradapp.gene.editor;

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.editor.ParameterEditor;
import edu.cmu.tetradapp.model.MeasurementSimulatorParams;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits the properties of a measurement simulator.
 *
 * @author Joseph Ramsey
 */
public class MeasurementSimulatorPropertyEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private MeasurementSimulatorParams simulator = null;

    /**
     * A text field to allow the user to enter the number of dishes to
     * generate.
     */
    private DoubleTextField dishDishVariabilityField;

    /**
     * A text field to allow the user to enter the number of steps generated.
     */
    private DoubleTextField sampleSampleVariabilityField;

    /**
     * A text field to allow the user to enter the index of the first step
     * stored.
     */
    private DoubleTextField chipChipVariabilityField;

    /**
     * A text field to allow the user to enter the pixelDigitalization between
     * steps stored.
     */
    private DoubleTextField pixelDigitalizationField;

    /**
     * A checkbox to indicate whether initialization of cells should be
     * synchronized.
     */
    private JCheckBox synchronizedCheckBox;

    /**
     * A text field to allow the user to enter the number of dishes to
     * generate.
     */
    private IntTextField numDishesField;

    /**
     * A text field to allow the user to enter the number of cells to generate
     * per dish (that is, the number of rows).
     */
    private IntTextField numCellsPerDishField;

    /**
     * A text field to allow the user to enter the number of cells to generate
     * per dish (that is, the number of rows).
     */
    private IntTextField numSamplesPerDishField;

    /**
     * A text field to allow the user to enter the number of steps generated.
     */
    private IntTextField stepsGeneratedField;

    /**
     * A text field to allow the user to enter the index of the first step
     * stored. Note that this is 1-indexed, even though the model is 0-indexed,
     * so an adjustment has to be made.
     */
    private IntTextField firstStepStoredField;

    /**
     * A text field to allow the user to enter the interval between steps
     * stored.
     */
    private IntTextField intervalField;

    /**
     * A checkbox to indicate whether raw data should be saved.
     */
    private JCheckBox rawDataSavedCheckBox;

    /**
     * A checkbox to indicate whether the antilog of expression levels should be
     * saved.
     */
    private JCheckBox antilogCalculatedCheckBox;

    /**
     * Constructs a dialog to edit the given gene simulation parameters object.
     */
    public MeasurementSimulatorPropertyEditor() {
    }

    public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.simulator = (MeasurementSimulatorParams) params;
    }

    public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    public void setup() {
        // set up text and ties them to the parameters object being edited.
        dishDishVariabilityField = new DoubleTextField(
                getSimulator().getDishDishVariability(), 6, NumberFormatUtil.getInstance().getNumberFormat());

        dishDishVariabilityField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getSimulator().setDishDishVariability(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        sampleSampleVariabilityField = new DoubleTextField(
                getSimulator().getSampleSampleVariability(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        sampleSampleVariabilityField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getSimulator().setSampleSampleVariability(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        chipChipVariabilityField = new DoubleTextField(
                getSimulator().getChipChipVariability(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        chipChipVariabilityField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getSimulator().setChipChipVariability(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        pixelDigitalizationField = new DoubleTextField(
                getSimulator().getPixelDigitalization(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        pixelDigitalizationField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getSimulator().setPixelDigitalization(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        numDishesField = new IntTextField(getSimulator().getNumDishes(), 6);
        numDishesField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setNumDishes(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        numCellsPerDishField =
                new IntTextField(getSimulator().getNumCellsPerDish(), 6);
        numCellsPerDishField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setNumCellsPerDish(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        numSamplesPerDishField =
                new IntTextField(getSimulator().getNumSamplesPerDish(), 6);
        numSamplesPerDishField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setNumSamplesPerDish(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        stepsGeneratedField =
                new IntTextField(getSimulator().getStepsGenerated(), 6);
        stepsGeneratedField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setStepsGenerated(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        firstStepStoredField =
                new IntTextField(getSimulator().getFirstStepStored(), 6);
        firstStepStoredField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setFirstStepStored(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        intervalField = new IntTextField(getSimulator().getInterval(), 3);
        intervalField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setInterval(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        synchronizedCheckBox = new JCheckBox("Synchronize cell initialization?",
                getSimulator().isInitSync());

        synchronizedCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JCheckBox checkBox = (JCheckBox) e.getSource();

                getSimulator().setInitSync(checkBox.isSelected());
            }
        });

        rawDataSavedCheckBox = new JCheckBox("Save raw data?",
                getSimulator().isRawDataSaved());

        rawDataSavedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                getSimulator().setRawDataSaved(checkBox.isSelected());
            }
        });

        antilogCalculatedCheckBox = new JCheckBox("Calculate antilogs of data?",
                getSimulator().isAntilogCalculated());

        antilogCalculatedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                getSimulator().setAntilogCalculated(checkBox.isSelected());
            }
        });

        // construct the editor.
        buildGui();
    }

    public boolean mustBeShown() {
        return true;
    }

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    private void buildGui() {

        Box b1, b2, b3, b4, b5;

        setLayout(new BorderLayout());

        // continue editor construction.
        b1 = Box.createHorizontalBox();
        b2 = Box.createHorizontalBox();
        b3 = Box.createHorizontalBox();
        b4 = Box.createHorizontalBox();
        b5 = Box.createVerticalBox();

        b1.add(new JLabel("Dish to dish variability (in Percent): "));
        b1.add(Box.createHorizontalGlue());
        b1.add(dishDishVariabilityField);
        b2.add(new JLabel("Sample to sample variability: "));
        b2.add(Box.createHorizontalGlue());
        b2.add(sampleSampleVariabilityField);
        b3.add(new JLabel("Chip to chip variability: "));
        b3.add(Box.createHorizontalGlue());
        b3.add(chipChipVariabilityField);
        b4.add(new JLabel("Pixel digitalization variability: "));
        b4.add(Box.createHorizontalGlue());
        b4.add(pixelDigitalizationField);
        b5.add(b1);
        b5.add(b2);
        b5.add(b3);
        b5.add(b4);
        b5.add(Box.createHorizontalGlue());

        //////////////////////
        Box bb1, bb2, bb3, bb4, bb5, bb6, bb7, bb8, bb9, bb10;

        setLayout(new BorderLayout());

        // continue editor construction.
        bb1 = Box.createHorizontalBox();
        bb2 = Box.createHorizontalBox();
        bb3 = Box.createHorizontalBox();
        bb4 = Box.createHorizontalBox();
        bb5 = Box.createHorizontalBox();
        bb6 = Box.createHorizontalBox();
        bb7 = Box.createHorizontalBox();
        bb8 = Box.createHorizontalBox();
        bb9 = Box.createHorizontalBox();
        bb10 = Box.createVerticalBox();

        bb1.add(new JLabel("Number of dishes: "));
        bb1.add(Box.createHorizontalGlue());
        bb1.add(numDishesField);
        bb2.add(new JLabel("Number of cells per dish: "));
        bb2.add(Box.createHorizontalGlue());
        bb2.add(numCellsPerDishField);
        bb3.add(new JLabel("Number of samples per dish: "));
        bb3.add(Box.createHorizontalGlue());
        bb3.add(numSamplesPerDishField);
        bb4.add(new JLabel("Steps generated: "));
        bb4.add(Box.createHorizontalGlue());
        bb4.add(stepsGeneratedField);
        bb5.add(new JLabel("First step stored: "));
        bb5.add(Box.createHorizontalGlue());
        bb5.add(firstStepStoredField);
        bb6.add(new JLabel("Keep every "));
        bb6.add(intervalField);
        bb6.add(new JLabel(" steps."));
        bb7.add(synchronizedCheckBox);
        bb8.add(rawDataSavedCheckBox);
        bb9.add(antilogCalculatedCheckBox);
        bb10.add(bb1);
        bb10.add(bb2);
        bb10.add(bb3);
        bb10.add(bb4);
        bb10.add(bb5);
        bb10.add(bb6);
        bb10.add(bb7);
        bb10.add(bb8);
        bb10.add(bb9);
        bb10.add(Box.createHorizontalGlue());

        // Construct panel.
        JPanel top = new JPanel();

        top.setLayout(new BorderLayout());
        top.add(b5, BorderLayout.CENTER);
        top.setBorder(new TitledBorder("Measurement Model"));

        JPanel bottom = new JPanel();

        bottom.setLayout(new BorderLayout());
        bottom.add(bb10, BorderLayout.CENTER);
        bottom.setBorder(new TitledBorder("Simulation Parameters"));

        Box both = Box.createVerticalBox();

        both.add(top);
        both.add(bottom);
        add(both, BorderLayout.CENTER);
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    protected synchronized MeasurementSimulatorParams getSimulator() {

        String ret =
                (this.simulator == null) ? "null" : this.simulator.toString();

        return this.simulator;
    }
}


