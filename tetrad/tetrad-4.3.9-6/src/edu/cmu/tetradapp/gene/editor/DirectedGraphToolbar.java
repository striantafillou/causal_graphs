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

import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * This is the toolbar for the LagGraphTetradEditor.  Its tools are as follows:
 * <ul> <li> The 'move' tool, allows the user to select and move items in the
 * editor editor. <li> The 'addObserved' tool, allows the user to add new
 * observed variables. <li> The 'addLatent' tool, allows the user to add new
 * latent variables. <li> The 'addDirectedEdge' tool, allows the user to add new
 * directed edges. <li> The 'addNondirectedEdge' tool, allows the user to add
 * new nondirected edges. <li> The 'addPartiallyOrientedEdge' tool, allows the
 * user to create new partially oriented edges. <li> The 'addBidirectedEdge'
 * tool, allows the user to create new bidirected edges. </ul>
 *
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 */
public class DirectedGraphToolbar extends JPanel
        implements PropertyChangeListener {
    static final long serialVersionUID = 23L;

    /**
     * The mutually exclusive button group for the buttons.
     */
    private ButtonGroup group;

    /**
     * The buttons in the toolbar.
     */
    private JToggleButton move, addObserved, addDirectedEdge, cleanup;

    /**
     * The editor this toolbar governs.
     */
    private DirectedGraphWorkbench workbench;

    /**
     * Constructs a new Graph toolbar governing the modes of the given
     * GraphWorkbench.
     */
    public DirectedGraphToolbar(DirectedGraphWorkbench workbench) {

        this.workbench = workbench;
        group = new ButtonGroup();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMinimumSize(new Dimension(200, 10));

        Border insideBorder = new MatteBorder(10, 10, 10, 10, getBackground());
        Border outsideBorder = new EtchedBorder();

        setBorder(new CompoundBorder(outsideBorder, insideBorder));

        // construct the bottons.
        move = new JToggleButton();
        addObserved = new JToggleButton();
        addDirectedEdge = new JToggleButton();
        cleanup = new JToggleButton();

        // Adding this listener fixes a previous bug where if you
        // select a button and then move the mouse away from the
        // button without releasing the mouse it would deselect. J
        // Ramsey 11/02/01
        FocusListener focusListener = new FocusAdapter() {

            public void focusGained(FocusEvent e) {

                JToggleButton component = (JToggleButton) e.getComponent();

                component.doClick();
            }
        };

        move.addFocusListener(focusListener);
        addObserved.addFocusListener(focusListener);
        addDirectedEdge.addFocusListener(focusListener);

        // add listeners
        move.addActionListener(new ActionListener() {

            /**
             * Sets the flowbench tool to MOVE.
             */
            public void actionPerformed(ActionEvent e) {
                move.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.SELECT_MOVE);
            }
        });
        addObserved.addActionListener(new ActionListener() {

            /**
             * Sets the flowbench tool to GRAPH.
             */
            public void actionPerformed(ActionEvent e) {
                addObserved.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.ADD_NODE);
            }
        });
        addDirectedEdge.addActionListener(new ActionListener() {

            /**
             * Sets the flowbench tool to IM.
             */
            public void actionPerformed(ActionEvent e) {
                addDirectedEdge.getModel().setSelected(true);
                setWorkbenchMode(GraphWorkbench.ADD_EDGE);
            }
        });
        cleanup.addActionListener(new ActionListener() {

            /**
             * Sets the flowbench tool to IM.
             */
            public void actionPerformed(ActionEvent e) {
                cleanup.setSelected(false);
                cleanup.getModel().setSelected(false);
                cleanup();
            }
        });

        // add buttons to the toolbar.
        addButton(move, "move", true);
        addButton(addObserved, "variable", true);
        //addButton(addLatent, "latent");
        addButton(addDirectedEdge, "directed", true);
        addButton(cleanup, "cleanup", false);
        //addButton(addNondirectedEdge, "nondirected");
        //addButton(addPartiallyOrientedEdge, "partiallyoriented");
        //addButton(addBidirectedEdge, "bidirected");
        workbench.addPropertyChangeListener(this);
        move.doClick();
        selectArrowTools();
    }

    /**
     * Convenience method to set the mode of the editor.  Placed here because
     * Java will not allow access to the variable 'editor' from inner classes.
     */
    private void setWorkbenchMode(int mode) {
        workbench.setWorkbenchMode(mode);
    }

    /**
     * Adds the various buttons to the toolbar, setting their properties
     * appropriately.
     */
    private void addButton(JToggleButton jb, String name, boolean addgroup) {

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
        if (addgroup) {
            group.add(jb);
        }
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
            return Toolkit.getDefaultToolkit().getImage(url);
        }
        catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Responds to property change events.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("graph".equals(e.getPropertyName())) {
            selectArrowTools();
        }
    }

    /**
     * For each editor type, enables the arrow tools which that editor can use
     * and disables all others.
     */
    private void selectArrowTools() {

        addDirectedEdge.setEnabled(true);
    }

    /**
     * Causes the editor to reload and reposition the editor
     */
    private void cleanup() {
        workbench.cleanup();
    }
}


