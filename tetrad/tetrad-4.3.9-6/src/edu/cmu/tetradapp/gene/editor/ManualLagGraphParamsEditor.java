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

import edu.cmu.tetrad.gene.graph.ManualLagGraphParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;

/**
 * Class LagGraphParameterSelector
 *
 * @author Joseph Ramsey
 */
public class ManualLagGraphParamsEditor extends JPanel {

    /**
     * Stores a reference to the lag editor getParams().
     */
    private ManualLagGraphParams params;

    /**
     * Allows the user to edit the number of variables per individual.
     */
    private IntTextField varsPerIndField;

    /**
     * Allows the user to edit the maximum lag.
     */
    private IntTextField mlagField;

    /**
     * Constructs an editor for the lag editor getMappings object.
     */
    public ManualLagGraphParamsEditor(ManualLagGraphParams params,
            Object[] parentModels) {

        if (params != null) {
            this.params = params;
        }
        else {
            throw new NullPointerException();
        }

        varsPerIndField = new IntTextField(getParams().getVarsPerInd(), 6);
        varsPerIndField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getParams().setVarsPerInd(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        mlagField = new IntTextField(getParams().getMlag(), 6);
        mlagField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getParams().setMlag(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }

            }
        });

        JLabel l1 = new JLabel("# Vars Per Individual = ");
        JLabel l2 = new JLabel("Maximum Time Lag = ");

        Box b = Box.createVerticalBox();
        Box b1 = Box.createHorizontalBox();
        b1.add(Box.createGlue());
        b1.add(l1);
        b1.add(varsPerIndField);
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createGlue());
        b2.add(l2);
        b2.add(mlagField);
        b.add(b2);

        add(b);
    }

    /**
     * Returns the getMappings object being edited.
     */
    public ManualLagGraphParams getParams() {
        return this.params;
    }
}


