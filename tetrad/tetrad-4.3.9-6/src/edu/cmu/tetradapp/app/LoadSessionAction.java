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

package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.model.SessionWrapper;
import edu.cmu.tetradapp.model.TetradMetadata;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.Version;
import edu.cmu.tetradapp.util.WatchedProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

/**
 * Opens a session from a file.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
final class LoadSessionAction extends AbstractAction {

    /**
     * Constructs a new action to open sessions.
     */
    public LoadSessionAction() {
        super("Open Session...");
    }

    /**
     * Performs the action of opening a session from a file.
     */
    public void actionPerformed(ActionEvent e) {

        Window owner = (Window) JOptionUtils.centeringComp().getTopLevelAncestor();

        new WatchedProcess(owner) {
            public void watch() {

                // select a file to open using the file chooser
                JFileChooser chooser = new JFileChooser();
                String sessionSaveLocation =
                        Preferences.userRoot().get("fileSaveLocation", "");
                chooser.setCurrentDirectory(new File(sessionSaveLocation));
                chooser.addChoosableFileFilter(new TetFileFilter());
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret1 = chooser.showOpenDialog(JOptionUtils.centeringComp());

                if (!(ret1 == JFileChooser.APPROVE_OPTION)) {
                    return;
                }

                File file = chooser.getSelectedFile();
                file = EditorUtils.ensureSuffix(file, "tet");

                if (file == null) {
                    return;
                }

                Preferences.userRoot().put("fileSaveLocation", file.getParent());

                if (DesktopController.getInstance().existsSessionByName(file.getName()))
                {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "Cannot open two sessions with the same name.");
                    return;
                }

                try {
                    FileInputStream in = new FileInputStream(file);
                    ObjectInputStream objIn = new ObjectInputStream(in);
                    Object o = objIn.readObject();

                    TetradMetadata metadata = null;
                    SessionWrapper sessionWrapper = null;

                    if (o instanceof TetradMetadata) {
                        metadata = (TetradMetadata) o;

                        try {
                            sessionWrapper = (SessionWrapper) objIn.readObject();
                        }
                        catch (Exception e2) {
                            e2.printStackTrace();
                            sessionWrapper = null;
                        }
                    }
                    else if (o instanceof SessionWrapper) {
                        metadata = null;
                        sessionWrapper = (SessionWrapper) o;
                    }

                    in.close();

                    if (metadata == null) {
                        throw new NullPointerException("Could not read metadata.");
                    }

                    if (sessionWrapper == null) {
                        Version version = metadata.getVersion();
                        Date date = metadata.getDate();
                        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");

                        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                                "Could not load session. The version of the session was \n" +
                                        version + "; it was saved on " +
                                        df.format(date) + ". You " +
                                        "\nmight try loading it with that version instead.");
                        return;
                    }

                    SessionEditorWorkbench graph =
                            new SessionEditorWorkbench(sessionWrapper);

                    String name = file.getName();
                    sessionWrapper.setName(name);
                                        
                    SessionEditor editor = new SessionEditor(name, graph);

                    DesktopController.getInstance().addSessionEditor(editor);
                    DesktopController.getInstance().closeEmptySessions();
                    DesktopController.getInstance().putMetadata(sessionWrapper,
                            metadata);
                }
                catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "That file does not exist: " + file);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "An error occurred attempting to load the session.");
                }
            }
        };

    }
}


