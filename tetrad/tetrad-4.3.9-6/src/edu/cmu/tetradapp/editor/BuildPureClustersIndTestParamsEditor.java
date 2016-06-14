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

import edu.cmu.tetrad.search.Bpc;
import edu.cmu.tetradapp.model.BuildPureClustersIndTestParams;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * Edits the properties of a measurement paramsPureClusters. See
 * BasicIndTestParamsEditor for more explanations.
 *
 * @author Ricardo Silva
 */
class BuildPureClustersIndTestParamsEditor extends JComponent {
    private BuildPureClustersIndTestParams paramsPureClusters;

    public BuildPureClustersIndTestParamsEditor(
            BuildPureClustersIndTestParams paramsPureClusters,
            boolean discreteData) {
        this.paramsPureClusters = paramsPureClusters;

        DoubleTextField alphaField = new DoubleTextField(getParams().getAlpha(),
                8, new DecimalFormat("0.0########"));
        alphaField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getParams().setAlpha(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        JComboBox testSelector = null;
        JComboBox purifySelector = null;

        if (!discreteData) {
            final String[] descriptions =
                    Bpc.getTestDescriptions();
            testSelector = new JComboBox(descriptions);
            testSelector.setSelectedIndex(getParams().getTetradTestType());

            testSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    int index = combo.getSelectedIndex();
                    getParams().setTetradTestType(index);
                }
            });

            final String[] purifyDescriptions =
                    Bpc.getPurifyTestDescriptions();
            purifySelector = new JComboBox(purifyDescriptions);
            purifySelector.setSelectedIndex(getParams().getPurifyTestType());

            purifySelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    int index = combo.getSelectedIndex();
                    getParams().setPurifyTestType(index);
                }
            });
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (!discreteData) {
            Box b1 = Box.createHorizontalBox();
            b1.add(new JLabel("Test:"));
            b1.add(Box.createHorizontalGlue());
            b1.add(testSelector);
            add(b1);
            add(Box.createHorizontalGlue());

            Box b2 = Box.createHorizontalBox();
            b2.add(new JLabel("Purify:"));
            b2.add(Box.createHorizontalGlue());
            b2.add(purifySelector);
            add(b2);
            add(Box.createHorizontalGlue());
        }

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("Alpha:"));
        b3.add(Box.createHorizontalGlue());
        b3.add(alphaField);
        add(b3);
        add(Box.createHorizontalGlue());

//        if (discreteData) {
//            paramsPureClusters.setPurifyTestType(
//                    BuildPureClusters.PURIFY_TEST_DISCRETE_LRT);
//            paramsPureClusters.setTetradTestType(BuildPureClusters.TEST_DISCRETE);
//
//        }
    }

    private BuildPureClustersIndTestParams getParams() {
        return this.paramsPureClusters;
    }
}


