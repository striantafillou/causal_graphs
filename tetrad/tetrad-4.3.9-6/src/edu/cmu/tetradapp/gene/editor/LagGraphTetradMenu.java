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

import edu.cmu.tetradapp.editor.SaveScreenshot;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the menu for the editor editor.
 *
 * @author Joseph Ramsey
 */
public class LagGraphTetradMenu extends JMenuBar {

    /**
     * The editor editor that this menubar is associated with. The menu items
     * are permitted to call public methods on this editor.
     */
    LagGraphEditor editor;

    /**
     * Constructs a new editor menu.
     *
     * @param workbench the editor for which this is the menu.
     */
    public LagGraphTetradMenu(LagGraphEditor workbench) {
        if (workbench == null) {
            throw new NullPointerException("Workbench must not be null.");
        }
        this.editor = workbench;
        add(createFileMenu());
    }

    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * editor models.
     *
     * @return this menu.
     */
    private JMenu createFileMenu() {

        JMenu menu = new JMenu("File");
        JMenuItem loadGraph = new JMenuItem("Load Graph...");
        JMenuItem saveGraph = new JMenuItem("Save Graph...");
        JMenuItem postGraph = new JMenuItem("Post Graph...");

        menu.add(loadGraph);
        menu.add(saveGraph);
        menu.add(postGraph);
        menu.add(new SaveScreenshot(this, true, "Save Screenshot..."));

        saveGraph.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveGraph();
            }
        });

        return menu;
    }

    public void saveGraph() {
        System.out.println(editor);
    }
}


