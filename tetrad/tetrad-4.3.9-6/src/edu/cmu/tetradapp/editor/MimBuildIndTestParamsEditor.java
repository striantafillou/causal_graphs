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
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.MimBuild;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.knowledge_editor.KnowledgeEditor;
import edu.cmu.tetradapp.model.MimBuildIndTestParams;
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
class MimBuildIndTestParamsEditor extends JComponent {
    private MimBuildIndTestParams params;

    public MimBuildIndTestParamsEditor(MimBuildIndTestParams params) {
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

        JButton editClusters = new JButton("Edit...");
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

        JButton editKnowledge = new JButton("Edit...");
        editKnowledge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List latentVarList = MimBuild.generateLatentNames(
                        getClusters().getNumClusters());
                KnowledgeEditor editor = new KnowledgeEditor(getKnowledge(),
                        latentVarList, getSourceGraph());
                EditorWindow window = new EditorWindow(editor,
                        "Edit Knowledge (Latents Only)", "Save", false);
                DesktopController.getInstance().addEditorWindow(window);
                window.setVisible(true);
            }
        });

        final String[] descriptions = MimBuild.getAlgorithmDescriptions();
        JComboBox algorithmSelector = new JComboBox(descriptions);
        algorithmSelector.setSelectedIndex(params.getAlgorithmType());

        algorithmSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                int index = combo.getSelectedIndex();
                getParams().setAlgorithmType(index);
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Clusters:"));
        b1.add(Box.createHorizontalStrut(10));
        b1.add(Box.createHorizontalGlue());
        b1.add(editClusters);
        add(b1);
        add(Box.createVerticalStrut(2));

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Knowledge:"));
        b2.add(Box.createHorizontalStrut(10));
        b2.add(Box.createHorizontalGlue());
        b2.add(editKnowledge);
        add(b2);
        add(Box.createVerticalStrut(2));

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("Algorithm:"));
        b3.add(Box.createHorizontalStrut(10));
        b3.add(Box.createHorizontalGlue());
        b3.add(algorithmSelector);
        add(b3);
        add(Box.createVerticalStrut(2));

        Box b4 = Box.createHorizontalBox();
        b4.add(new JLabel("Alpha:"));
        b4.add(Box.createHorizontalStrut(10));
        b4.add(Box.createHorizontalGlue());
        b4.add(alphaField);
        add(b4);
    }

    private Graph getSourceGraph() {
        return params.getSourceGraph();
    }

    private Knowledge getKnowledge() {
        return params.getKnowledge();
    }

    private Clusters getClusters() {
        return params.getClusters();
    }

    private List getVarNames() {
        return params.getVarNames();
    }

    private MimBuildIndTestParams getParams() {
        return params;
    }
}


