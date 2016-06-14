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
import edu.cmu.tetradapp.model.GraphParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits the parameters for generating random graphs.
 *
 * @author Joseph Ramsey
 */
public class GraphParamsEditor extends JPanel implements ParameterEditor {
    private GraphParams params = new GraphParams();

    /**
     * Constructs a dialog to edit the given workbench randomization
     * parameters.
     */
    public GraphParamsEditor() {
    }

    public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (GraphParams) params;
    }

    public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    public void setup() {
        final RandomDagEditor randomDagEditor = new RandomDagEditor();

        // construct the workbench.
        setLayout(new BorderLayout());

        JRadioButton manual = new JRadioButton(
                "An empty graph (to be constructed manually).");
        JRadioButton random = new JRadioButton("A random DAG.");
        ButtonGroup group = new ButtonGroup();
        group.add(manual);
        group.add(random);

        if (getParams().getInitializationMode() == GraphParams.MANUAL) {
            manual.setSelected(true);
            randomDagEditor.setEnabled(false);
        }
        else {
            random.setSelected(true);
            randomDagEditor.setEnabled(true);
        }

        manual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getParams().setInitializationMode(GraphParams.MANUAL);
                randomDagEditor.setEnabled(false);
            }
        });

        random.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getParams().setInitializationMode(GraphParams.RANDOM);
                randomDagEditor.setEnabled(true);
            }
        });

        Box b1 = Box.createVerticalBox();
        Box b2 = Box.createVerticalBox();

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("Make new graph:"));
        b3.add(Box.createHorizontalGlue());
        b2.add(b3);
        b1.add(Box.createVerticalStrut(5));

        Box b4 = Box.createHorizontalBox();
        b4.add(manual);
        b4.add(Box.createHorizontalGlue());
        b2.add(b4);

        Box b5 = Box.createHorizontalBox();
        b5.add(random);
        b5.add(Box.createHorizontalGlue());
        b2.add(b5);

        b2.setBorder(new TitledBorder(""));
        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        Box b6 = Box.createHorizontalBox();
        b6.add(randomDagEditor);
        b6.add(Box.createHorizontalGlue());

        b6.setBorder(new TitledBorder(""));
        b1.add(b6);

        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

    public boolean mustBeShown() {
        return false;
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private synchronized GraphParams getParams() {
        return this.params;
    }
}


