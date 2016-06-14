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
import edu.cmu.tetradapp.model.SemDataParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Edits the parameters for simulating data from OldSem nets.
 *
 * @author Joseph Ramsey
 */
public class SemDataParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private SemDataParams params = null;

    /**
     * Constructs a dialog to edit the given workbench OldSem simulation
     * getMappings object.
     */
    public SemDataParamsEditor() {
    }

    public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (SemDataParams) params;
    }

    public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    public void setup() {

        // set up text and ties them to the parameters object being edited.
        IntTextField sampleSizeField = new IntTextField(getParams().getSampleSize(), 4);

        sampleSizeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getParams().setSampleSize(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        JCheckBox latentDataSaved = new JCheckBox("Include Latent Variables",
                Preferences.userRoot().getBoolean("latentDataSaved", getParams().isIncludeLatents()));

        latentDataSaved.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JCheckBox b = (JCheckBox)e.getSource();
                getParams().setIncludeLatents(b.isSelected());
            }
        });

        setLayout(new BorderLayout());

        // continue workbench construction.
        Box b = Box.createVerticalBox();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Sample size:  "));
        b1.add(Box.createHorizontalGlue());
        b1.add(sampleSizeField);
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        b2.add(latentDataSaved);
        b.add(b2);

        add(b, BorderLayout.CENTER);
    }

    public boolean mustBeShown() {
        return true;
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private synchronized SemDataParams getParams() {
        return this.params;
    }
}


