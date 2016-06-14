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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * This is the toolbar for the GraphTableWorkbench.  Its tools are as follows:
 * <ul> <li> The 'addObserved' tool, adds new observed variables. <li> The
 * 'clearObserved' tool, removes selected observed variables </ul>
 *
 * @author Gregory Li
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 * @see GraphTableWorkbench
 */
public class TableToolbar extends JPanel {
    static final long serialVersionUID = 23L;

    /**
     * The buttons in the toolbar.
     */
    private JToggleButton addObserved;
    private JToggleButton clearObserved;

    /**
     * The editor this toolbar governs.
     */
    private GraphTableWorkbench workbench;

    /**
     * Constructs a new Graph toolbar governing the modes of the given
     * GraphWorkbench.
     */
    public TableToolbar(GraphTableWorkbench workbench) {

        this.workbench = workbench;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMinimumSize(new Dimension(200, 10));

        Border insideBorder = new MatteBorder(10, 10, 10, 10, getBackground());
        Border outsideBorder = new EtchedBorder();

        setBorder(new CompoundBorder(outsideBorder, insideBorder));

        // construct the bottons.
        addObserved = new JToggleButton();
        clearObserved = new JToggleButton();

        addObserved.addActionListener(new ActionListener() {

            /**
             * Sets the flowbench tool to GRAPH.
             */
            public void actionPerformed(ActionEvent e) {
                createNewFactor();
                addObserved.setSelected(false);
            }
        });

        clearObserved.addActionListener(new ActionListener() {
            /**
             * Removes selected factor
             */
            public void actionPerformed(ActionEvent e) {
                clearFactors();
                clearObserved.setSelected(false);
            }
        });

        // add buttons to the toolbar.
        addButton(addObserved, "variable");
        addButton(clearObserved, "clearVariable");
    }

    private void createNewFactor() {
        workbench.newFactor();
    }

    private void clearFactors() {
        workbench.clearFactors();
    }

    /**
     * Adds the various buttons to the toolbar, setting their properties
     * appropriately.
     */
    private void addButton(JToggleButton jb, String name) {

        jb.setIcon(new ImageIcon(getImage(name + "Up.gif")));
        jb.setRolloverIcon(new ImageIcon(getImage(name + "Roll.gif")));
        jb.setPressedIcon(new ImageIcon(getImage(name + "Down.gif")));
        jb.setSelectedIcon(new ImageIcon(getImage(name + "Down.gif")));
        jb.setDisabledIcon(new ImageIcon(getImage(name + "Off.gif")));
        jb.setRolloverEnabled(true);
        jb.setBorder(new EmptyBorder(1, 1, 1, 1));
        jb.setSize(100, 50);
        jb.setMinimumSize(new Dimension(100, 50));
        jb.setPreferredSize(new Dimension(100, 50));
        add(jb);
    }

    /**
     * Loads images for the toolbar using the resource method.  It is assumed
     * that all images are in a directory named "images" relative to this
     * class.
     *
     * @param path the pathname of the image beyond "images/".
     * @return the loaded image.
     */
    private Image getImage(String path) {
        try {
            String fullPath = "/resources/images/" + path;
            URL url = getClass().getResource(fullPath);
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            return image;
        }
        catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}


