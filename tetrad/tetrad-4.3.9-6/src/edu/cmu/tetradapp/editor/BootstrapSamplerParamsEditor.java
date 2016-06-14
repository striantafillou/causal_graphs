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

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.model.BootstrapSamplerParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;

/**
 * Edits the parameters for simulating data from Bayes nets.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @author Frank Wimberly based on similar classes by Joe Ramsey
 */
public class BootstrapSamplerParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private BootstrapSamplerParams params = null;



    public void setParams(Params params) {
        this.params = (BootstrapSamplerParams)params;
    }

    public void setParentModels(Object[] parentModels) {

    }

    public void setup() {
        buildGui();
    }

    public boolean mustBeShown() {
        return true;
    }

    //================================= Private Methods ===============================//

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    private void buildGui() {
        setLayout(new BorderLayout());

        final IntTextField sampleSizeField = new IntTextField(params.getSampleSize(), 6);
        sampleSizeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    params.setSampleSize(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        // continue workbench construction.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("<html>" +
                "The input dataset will be sampled with replacement to create a new" +
                "<br>dataset with the number of samples entered below." +
                "<br>The editable default sample size is 100." + "</html>"));

        Box b7 = Box.createHorizontalBox();
        b7.add(Box.createHorizontalGlue());
        b7.add(new JLabel("<html>" + "<i>Sample size:  </i>" + "</html>"));
        b7.add(sampleSizeField);

        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));
        b1.add(b7);
        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

}


