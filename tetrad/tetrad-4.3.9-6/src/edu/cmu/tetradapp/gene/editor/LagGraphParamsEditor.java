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
import edu.cmu.tetradapp.model.LagGraphParams;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class LagGraphParameterSelector
 *
 * @author Joseph Ramsey
 */
public class LagGraphParamsEditor extends JPanel implements ParameterEditor {

    /**
     * Stores a reference to the lag editor getParams().
     */
    private LagGraphParams params;

    /**
     * Constructs an editor for the lag editor getMappings object.
     */
    public LagGraphParamsEditor() {
    }

    public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (LagGraphParams) params;
    }

    public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    public void setup() {
        IntTextField varsPerIndField =
                new IntTextField(getParams().getVarsPerInd(), 6);
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

        IntTextField mlagField = new IntTextField(getParams().getMlag(), 6);
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

        IntTextField indegreeField = new IntTextField(getParams().getIndegree(), 6);
        indegreeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    getParams().setIndegree(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        DoubleTextField percentUnregulatedField = new DoubleTextField(
                getParams().getPercentUnregulated(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        percentUnregulatedField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getParams().setPercentUnregulated(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        JLabel l1 = new JLabel("# Vars Per Individual = ");
        JLabel l2 = new JLabel("Maximum Time Lag = ");
        String[] options = new String[]{"Constant", "Maximum", "Mean"};
        JComboBox c3 = new JComboBox(options);

        switch (getParams().getIndegreeType()) {
            case LagGraphParams.CONSTANT:
                c3.setSelectedIndex(0);
                break;

            case LagGraphParams.MAX:
                c3.setSelectedIndex(1);
                break;

            case LagGraphParams.MEAN:
                c3.setSelectedIndex(2);
                break;

            default :
                throw new IllegalStateException();
        }

        c3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JComboBox comboBox = (JComboBox) e.getSource();
                int index = comboBox.getSelectedIndex();

                switch (index) {
                    case 0:
                        getParams().setIndegreeType(LagGraphParams.CONSTANT);
                        break;

                    case 1:
                        getParams().setIndegreeType(LagGraphParams.MAX);
                        break;

                    case 2:
                        getParams().setIndegreeType(LagGraphParams.MEAN);
                        break;

                    default :
                        throw new IllegalStateException();
                }
            }
        });
        c3.setAlignmentX(1.0f);

        JLabel l3 = new JLabel(" Indegree = ");
        JLabel l4 = new JLabel("Approximate percent unregulated genes: ");
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

        Box b3 = Box.createHorizontalBox();

        b3.add(c3);
        b3.add(l3);
        b3.add(indegreeField);
        b.add(b3);

        Box b4 = Box.createHorizontalBox();

        b4.add(l4);
        b4.add(Box.createGlue());
        b4.add(percentUnregulatedField);
        b.add(b4);
        add(b);
    }

    public boolean mustBeShown() {
        return false;
    }

    /**
     * Returns the getMappings object being edited.
     */
    public LagGraphParams getParams() {
        return this.params;
    }
}


