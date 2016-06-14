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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * This is the toolbar for the top of the GraphEditor.  Its tools are as
 * follows: <ul> <li>directed view - displays the current editor as a directed
 * editor <li>repeating view - displays the current editor as a lag editor
 * <li>tabular view - displays the current editor as a table </ul> In lag view a
 * slider will be available to change the level of transparency of the repeating
 * arrows.
 *
 * @author Ethan Tira-Thompson
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 * @see edu.cmu.tetradapp.editor.GraphEditor
 * @see edu.cmu.tetradapp.editor.GraphToolbar
 */
public class LagGraphTopToolbar extends JPanel
        implements PropertyChangeListener/*, Serializeable*/ {

    /**
     * The mutually exclusive button group for the buttons
     */
    protected ButtonGroup group;

    /**
     * The buttons in the toolbar.
     */
    protected JToggleButton directView, repeatView, tableView;

    /**
     * The editor this is part of.
     */
    protected LagGraphEditor editor;

    /**
     * Panel of options for the Repeating view's GraphTopToolbar
     */
    JPanel repeatingOptionsPanel;

    /**
     * Panel of options for the Repeating view's GraphTopToolbar
     */
    JPanel directedOptionsPanel;

    /**
     * Panel of options for the Repeating view's GraphTopToolbar
     */
    JPanel tabularOptionsPanel;

    /**
     * Current options panel
     */
    JPanel optionsPanel;

    /**
     * Constructs a new Graph toolbar.
     *
     * @param editor the GraphEditor which of which this toolbar governs the
     *               state.
     */
    public LagGraphTopToolbar(LagGraphEditor editor) {
        this.editor = editor;
        group = new ButtonGroup();
        //    	setBackground(BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        //        setMinimumSize(new Dimension(10, 50));

        Border insideBorder = new MatteBorder(10, 10, 10, 10, getBackground());
        Border outsideBorder = new EtchedBorder();

        setBorder(new CompoundBorder(outsideBorder, insideBorder));

        repeatingOptionsPanel = new JPanel();
        repeatingOptionsPanel.setLayout(
                new BoxLayout(repeatingOptionsPanel, BoxLayout.Y_AXIS));
        directedOptionsPanel = new JPanel();
        directedOptionsPanel.setLayout(
                new BoxLayout(directedOptionsPanel, BoxLayout.Y_AXIS));
        tabularOptionsPanel = new JPanel();
        tabularOptionsPanel.setLayout(
                new BoxLayout(tabularOptionsPanel, BoxLayout.Y_AXIS));

        // construct the bottons.
        directView = new JToggleButton();
        repeatView = new JToggleButton();
        tableView = new JToggleButton();
        JRadioButton lagGraphButton = new JRadioButton("Lag Graph", false);
        JRadioButton hybridGraphButton = new JRadioButton("Hybrid Graph", true);
        JRadioButton repeatGraphButton =
                new JRadioButton("Repeating Graph", false);
        repeatingOptionsPanel.add(lagGraphButton);
        repeatingOptionsPanel.add(hybridGraphButton);
        repeatingOptionsPanel.add(repeatGraphButton);
        ButtonGroup bg = new ButtonGroup();
        bg.add(lagGraphButton);
        bg.add(hybridGraphButton);
        bg.add(repeatGraphButton);
        JCheckBox multiArrowGraphButton = new JCheckBox("Multple Arrowheads");
        JCheckBox numLabelGraphButton = new JCheckBox("Numerical Label");
        numLabelGraphButton.setSelected(true);
        directedOptionsPanel.add(new JLabel("Show Lags as:"));
        directedOptionsPanel.add(multiArrowGraphButton);
        directedOptionsPanel.add(numLabelGraphButton);

        //		repeatArrowsSlider.setSize(100,50);
        //      repeatArrowsSlider.setMinimumSize(new Dimension(100,50));
        //      repeatArrowsSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 127);
        //      repeatArrowsSlider.setPreferredSize(new Dimension(100,50));
        //		repeatArrowsSlider.addChangeListener(editor);

        /* this causes problems with the repeating view- it causes it to go to home
		 * every time the window gains focus, which could be very annoying to the user
		 *
		FocusListener focusListener = new FocusAdapter() {

			public void focusGained(FocusEvent e) {

				JToggleButton component = (JToggleButton) e.getComponent();

				component.doClick();
			}
		};

		directView.addFocusListener(focusListener);
		repeatView.addFocusListener(focusListener);
		tableView.addFocusListener(focusListener);
		 */

        // add listeners
        directView.addActionListener(new ActionListener() {
            /**
             * Sets the editor to directed editor view.
             */
            public void actionPerformed(ActionEvent e) {
                doSwitchDirected();
            }
        });
        repeatView.addActionListener(new ActionListener() {
            /**
             * Sets the editor to directed editor view.
             */
            public void actionPerformed(ActionEvent e) {
                doSwitchRepeating();
            }
        });
        tableView.addActionListener(new ActionListener() {
            /**
             * Sets the editor to directed editor view.
             */
            public void actionPerformed(ActionEvent e) {
                doSwitchTabular();
            }
        });

        lagGraphButton.addActionListener(editor);
        hybridGraphButton.addActionListener(editor);
        repeatGraphButton.addActionListener(editor);
        multiArrowGraphButton.addActionListener(editor);
        numLabelGraphButton.addActionListener(editor);

        // add buttons to the toolbar.
        addButton(directView, "direct");
        addButton(repeatView, "repeat");
        addButton(tableView, "table");
        selectViews();
    }

    /**
     * Needed because can't access member value inside nested functions
     */
    public void doSwitchDirected() {
        if (optionsPanel != null) {
            remove(optionsPanel);
        }
        optionsPanel = directedOptionsPanel;
        add(optionsPanel);
        directView.setSelected(
                true); //initialization purposes, the group should be turning them on and off
        editor.switchDirected();
    }

    /**
     * Needed because can't access member value inside nested functions
     */
    public void doSwitchRepeating() {
        if (optionsPanel != null) {
            remove(optionsPanel);
        }
        optionsPanel = repeatingOptionsPanel;
        add(optionsPanel);
        repeatView.setSelected(
                true); //initialization purposes, the group should be turning them on and off
        editor.switchRepeating();
    }

    /**
     * Needed because can't access member value inside nested functions
     */
    public void doSwitchTabular() {
        if (optionsPanel != null) {
            remove(optionsPanel);
        }
        optionsPanel = tabularOptionsPanel;
        add(optionsPanel);
        tableView.setSelected(
                true); //initialization purposes, the group should be turning them on and off
        editor.switchTabular();
    }

    /**
     * Adds the various buttons to the toolbar, setting their properties
     * appropriately.
     *
     * @param jb   the toggle button to add.
     * @param name
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
        group.add(jb);
    }

    /**
     * Loads images for the toolbar using the resource method.  It is assumed
     * that all images are in a directory named "images" relative to this
     * class.
     *
     * @param path the pathname of the image beyond "images/".
     * @return the image.
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

    /**
     * Responds to property change events.
     *
     * @param e the property change event.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("graph".equals(e.getPropertyName())) {
            selectViews();
        }
    }

    /**
     * For each editor type, enables the button
     */
    private void selectViews() {
        directView.setEnabled(true);
        repeatView.setEnabled(true);
        tableView.setEnabled(true);
    }
}

















