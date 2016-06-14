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

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.workbench.GraphNodeMeasured;

import javax.swing.*;

/**
 * @author ejt
 */
public class DirectedGraphNode extends GraphNodeMeasured {

    public DirectedGraphNode(String name) {
        super(name);
    }

    public void doDoubleClickAction() {
        JTextField nameField = new JTextField(8);

        nameField.setText(getName());
        nameField.setCaretPosition(0);
        nameField.moveCaretPosition(getName().length());

        JPanel message = new JPanel();

        message.add(new JLabel("Name:"));
        message.add(nameField);

        //JOptionPane pane   = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        //JDialog     dialog = pane.createDialog(this, "Node Properties");
        int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
                message, "Factor Name", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        //dialog.pack();
        //dialog.setVisible(true);

        if (ret == JOptionPane.OK_OPTION) {
            String newName = nameField.getText();
            ((DirectedGraphWorkbench) getParent()).getModelGraph()
                    .renameFactor(getName(), newName);
        }
        //System.out.println(ret);
        //System.out.println(newName);

    }
}

