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

package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.GraphIndTestParams;
import edu.cmu.tetradapp.model.IndTestParams;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;

/**
 * Edits the properties of a measurement params.
 *
 * @author Joseph Ramsey
 */
class IndTestParamsEditor extends JComponent {

    /**
     * The parameters object being edited.
     */
    private IndTestParams params = null;

    /**
     * A text field to allow the user to enter the number of dishes to
     * generate.
     */
    private DoubleTextField alphaField;

    /**
     * A text field to allow the user to enter the number of dishes to
     * generate.
     */
    private IntTextField depthField;

    /**
     * Constructs a dialog to edit the given gene simulation parameters object.
     */
    public IndTestParamsEditor(IndTestParams params) {
        this.params = params;

        // set up text and ties them to the parameters object being edited.
        alphaField = new DoubleTextField(indTestParams().getAlpha(), 5,
                NumberFormatUtil.getInstance().getNumberFormat());
        alphaField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    indTestParams().setAlpha(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        depthField = new IntTextField(indTestParams().getDepth(), 5);
        depthField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    indTestParams().setDepth(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        buildGui();
    }

    public IndTestParamsEditor(GraphIndTestParams params) {
        this.params = params;

        depthField = new IntTextField(indTestParams().getDepth(), 5);
        depthField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    indTestParams().setDepth(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        buildGui();
    }

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    private void buildGui() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (alphaField != null) {
            Box b1 = Box.createHorizontalBox();
            b1.add(new JLabel("Alpha:"));
            b1.add(Box.createHorizontalGlue());
            b1.add(alphaField);
            add(b1);
        }

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Depth:"));
        b2.add(Box.createHorizontalGlue());
        b2.add(depthField);
        add(b2);

        add(Box.createHorizontalGlue());
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private IndTestParams indTestParams() {
        return params;
    }
}


