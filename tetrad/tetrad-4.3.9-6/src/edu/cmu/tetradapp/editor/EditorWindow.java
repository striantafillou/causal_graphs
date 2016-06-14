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

import edu.cmu.tetrad.session.ModificationRegistery;
import edu.cmu.tetradapp.util.EditorWindowIndirectRef;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Launches a dialog to display an editor component.
 *
 * @author Gregory Li, Joseph Ramsey
 */
public class EditorWindow extends JInternalFrame
        implements EditorWindowIndirectRef {

    private JPanel editor;

    /**
     * Set to true if the dialog was canceled.
     */
    private boolean canceled = false;

    /**
     * The name of the main button; normally "Save."
     */
    private String buttonName;

    /**
     * Pops a new editor window up from a dialog.
     */
    public EditorWindow(JPanel editor, String title, String buttonName,
            boolean cancellable) {
        super(title, true, true, true, true);

        if (editor == null) {
            throw new NullPointerException("Editor must not be null.");
        }

        if (buttonName == null) {
            throw new NullPointerException("Button name must not be null.");
        }

        this.buttonName = buttonName;
        doSetup(editor, cancellable);
    }

    /**
     * Constructs the dialog.
     */
    private void doSetup(JPanel editor, boolean cancellable) {
        this.editor = editor;

        addInternalFrameListener(new InternalFrameAdapter() {
            public void InternalFrameClosing(InternalFrameEvent evt) {
                canceled = true;
                closeDialog();
            }
        });

        JButton okButton = new JButton(buttonName);
        JButton cancelButton = new JButton("Cancel");

        okButton.setPreferredSize(new Dimension(70, 50));
        cancelButton.setPreferredSize(new Dimension(80, 50));

        okButton.addActionListener(new OkListener());
        cancelButton.addActionListener(new CancelListener());

        Box b0 = Box.createVerticalBox();
        Box b = Box.createHorizontalBox();

        b.add(Box.createHorizontalGlue());
        b.add(okButton);
        b.add(Box.createHorizontalStrut(5));

        if (cancellable) {
            b.add(cancelButton);                                             
        }

        b.add(Box.createHorizontalGlue());

        b0.add(editor);
        b0.add(b);

        getContentPane().add(b0);

        // Set the ok button so that pressing enter activates it.
        // jdramsey 5/5/02
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            root.setDefaultButton(okButton);
        }

        pack();
    }

    /**
     * Closes the dialog.
     */
    public void closeDialog() {
        setVisible(false);
        ModificationRegistery.unregisterEditor(getEditor());
        doDefaultCloseAction();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public JComponent getEditor() {
        return editor;
    }

    class OkListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            closeDialog();
        }
    }

    class CancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canceled = true;
            closeDialog();
        }
    }
}


