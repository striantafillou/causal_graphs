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

import edu.cmu.tetrad.gene.graph.StoredLagGraphParams;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Allows the user to set the file to which records should be written.
 *
 * @author Joseph Ramsey
 */
public class StoredLagGraphParamsEditor extends JPanel {
    StoredLagGraphParams params;

    /**
     * Constructs a new editor for the given StoredLagGraphParams.
     */
    public StoredLagGraphParamsEditor(StoredLagGraphParams params,
            Object[] parentModels) {

        if (params == null) {
            throw new NullPointerException("Params must not be null.");
        }

        this.params = params;

        File file = new File(params.getFilename());
        String filename = (file == null) ? "(Not set yet)" : file.getName();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box b1 = Box.createHorizontalBox();

        b1.add(new JLabel("Current file to read from is:"));
        b1.add(Box.createGlue());
        add(b1);

        Box b2 = Box.createHorizontalBox();

        b2.add(Box.createGlue());
        b2.add(new JLabel(filename));
        add(b2);

        Box b3 = Box.createHorizontalBox();

        b3.add(Box.createGlue());

        JButton chooseFile = new JButton("Choose File");

        b3.add(chooseFile);
        add(b3);
        chooseFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = getJFileChooser();

                chooser.showOpenDialog(StoredLagGraphParamsEditor.this);

                // get the file
                setFile(chooser.getSelectedFile());
            }
        });
    }

    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String sessionSaveLocation =
                Preferences.userRoot().get("fileSaveLocation", "");
        chooser.setCurrentDirectory(new File(sessionSaveLocation));
        chooser.resetChoosableFileFilters();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return chooser;
    }

    /**
     * Sets the file chosen in the getMappings.
     */
    private void setFile(File file) {
        this.params.setFilename(file.getPath());
    }
}


