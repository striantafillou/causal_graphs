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

import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.MeasurementSimulatorParams;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;

/**
 * GUI editor for the measurement model parameters in a GeneSimulationParams
 * object. Should be used in the Tetrad application whenever this type of
 * parameters object needs to be edited--e.g. when simulating from a
 * BooleanGlassGeneIm.
 *
 * @author Joseph Ramsey
 * @see edu.cmu.tetradapp.model.BooleanGlassGeneIm
 * @deprecated Functionality moved to MeasurementSimulationPropertyEditor. This
 *             class may be eliminated after 1/1/02 if no problems arise.
 */
public class MeasurementModelPropertyEditor extends JPanel {

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
     * A text field to allow the user to enter the number of cells to generate
     * per dish (that is, the number of rows).
     */
    private IntTextField numSamplesPerDishField;

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
     * Constructs a dialog to edit the given gene simulation parameters object.
     */
    public MeasurementModelPropertyEditor(
            MeasurementSimulatorParams simulator) {

        this.simulator = simulator;

        // set up text and ties them to the parameters object being edited.
        dishDishVariabilityField = new DoubleTextField(
                getSimulator().getDishDishVariability(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        dishDishVariabilityField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getSimulator().setDishDishVariability(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        numSamplesPerDishField =
                new IntTextField(getSimulator().getNumSamplesPerDish(), 4);
        numSamplesPerDishField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getSimulator().setNumSamplesPerDish(value);
                    return value;
                }
                catch (Exception e) {
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
                catch (Exception e) {
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
                catch (Exception e) {
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
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        // construct the editor.
        buildGui();
    }

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    private final void buildGui() {

        Box b1, b2, b3, b4, b5, b6;

        setLayout(new BorderLayout());

        // continue editor construction.
        b1 = Box.createHorizontalBox();
        b2 = Box.createHorizontalBox();
        b3 = Box.createHorizontalBox();
        b4 = Box.createHorizontalBox();
        b5 = Box.createHorizontalBox();
        b6 = Box.createVerticalBox();

        b1.add(new JLabel("Dish to dish variability (in Percent): "));
        b1.add(Box.createHorizontalGlue());
        b1.add(dishDishVariabilityField);
        b2.add(new JLabel("Number of samples per dish: "));
        b2.add(Box.createHorizontalGlue());
        b2.add(numSamplesPerDishField);
        b3.add(new JLabel("Sample to sample variability: "));
        b3.add(Box.createHorizontalGlue());
        b3.add(sampleSampleVariabilityField);
        b4.add(new JLabel("Chip to chip variability: "));
        b4.add(Box.createHorizontalGlue());
        b4.add(chipChipVariabilityField);
        b5.add(new JLabel("Pixel digitalization variability: "));
        b5.add(Box.createHorizontalGlue());
        b5.add(pixelDigitalizationField);
        b6.add(b1);
        b6.add(b2);
        b6.add(b3);
        b6.add(b4);
        b6.add(b5);
        b6.add(Box.createHorizontalGlue());
        add(b6, BorderLayout.CENTER);
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    protected synchronized MeasurementSimulatorParams getSimulator() {
        return this.simulator;
    }
}


