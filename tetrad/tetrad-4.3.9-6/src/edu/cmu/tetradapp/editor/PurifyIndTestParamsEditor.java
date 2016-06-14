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

import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.search.Purify;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.PurifyIndTestParams;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Edits the properties of a measurement params. See BasicIndTestParamsEditor
 * for more explanations.
 *
 * @author Ricardo Silva
 */
class PurifyIndTestParamsEditor extends JComponent {
    private PurifyIndTestParams params;

    public PurifyIndTestParamsEditor(PurifyIndTestParams params,
            boolean discreteData) {
        this.params = params;

        DoubleTextField alphaField = new DoubleTextField(getParams().getAlpha(),
                7, NumberFormatUtil.getInstance().getNumberFormat());
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

        JButton editClusters = new JButton("Edit");
        editClusters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClusterEditor editor =
                        new ClusterEditor(getClusters(), getVarNames());
                EditorWindow window = new EditorWindow(editor, "Edit Clusters",
                        "Save", false);
                DesktopController.getInstance().addEditorWindow(window);
                window.setVisible(true);
            }
        });

        JComboBox testSelector = new JComboBox();

        if (!discreteData) {
            final String[] descriptions = Purify.getTestDescriptions();
            for (String description : descriptions) {
                testSelector.addItem(description);
            }
            testSelector.setSelectedIndex(getParams().getTetradTestType());
            testSelector.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    String item = (String) e.getItem();
                    for (int i = 0; i < descriptions.length; i++) {
                        if (item.equals(descriptions[i])) {
                            getParams().setTetradTestType(i);
                            break;
                        }
                    }
                }
            });
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Clusters:"));
        b1.add(Box.createHorizontalStrut(10));
        b1.add(Box.createHorizontalGlue());
        b1.add(editClusters);
        add(b1);
        add(Box.createVerticalStrut(2));

        if (!discreteData) {
            Box b2 = Box.createHorizontalBox();
            b2.add(new JLabel("Test:"));
            b2.add(Box.createHorizontalStrut(15));
            b2.add(Box.createHorizontalGlue());
            b2.add(testSelector);
            add(b2);
            add(Box.createVerticalStrut(2));
        }

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("Alpha:"));
        b3.add(Box.createHorizontalStrut(10));
        b3.add(Box.createHorizontalGlue());
        b3.add(alphaField);
        add(b3);
    }

    private PurifyIndTestParams getParams() {
        return this.params;
    }

    private Clusters getClusters() {
        return params.getClusters();
    }

    private List<String> getVarNames() {
        return params.getVarNames();
    }
}


