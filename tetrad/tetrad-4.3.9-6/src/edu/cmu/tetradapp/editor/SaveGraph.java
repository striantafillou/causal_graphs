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

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetradapp.model.EditorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

/**
 * Saves out a PNG image for a component.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class SaveGraph extends AbstractAction {

    /**
     * The component whose image is to be saved.
     */
    private GraphEditable graphEditable;

    /**
     * A reference to the title, to be used a dialog title.
     */
    private String title = "";

    public SaveGraph(GraphEditable graphEditable, String title) {
        super(title);
        this.title = title;

        if (this.title == null) this.title = "Save";

        if (graphEditable == null) {
            throw new NullPointerException("Component must not be null.");
        }

        this.graphEditable = graphEditable;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        Graph graph = getGraphEditable().getGraph();
        Component parent = (Component) getGraphEditable();
        File file = EditorUtils.getSaveFile("graph", "txt", parent, false, title);
        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
            out.print(graph);
        }
        catch (IOException e1) {
            throw new IllegalArgumentException(
                    "Output file could not " + "be opened: " + file);
        }
        Preferences.userRoot().put("fileSaveLocation", file.getParent());

        out.close();
    }

    private GraphEditable getGraphEditable() {
        return graphEditable;
    }
}



