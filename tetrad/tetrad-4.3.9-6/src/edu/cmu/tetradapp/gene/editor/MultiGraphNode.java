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

import edu.cmu.tetrad.gene.history.LaggedFactor;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.DisplayNodeUtils;
import edu.cmu.tetradapp.workbench.GraphNodeMeasured;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Extends a GraphNodeMeasured to add functionality specific to factors over
 * time. Specifically, the node is able to synchronize the edges going into it,
 * and also alter its appearance to express its timestep
 *
 * @author gmli
 */
public class MultiGraphNode extends GraphNodeMeasured {

    protected Color FUTURE_COLOR = new Color(255, 255, 153);

    private int timestep = 0;

    TimesteppedFactor factorGroup;

    // used to synchronize to model (tell what is/isn't already in).
    Map modelToDisplay;

    /**
     * Creates new MultiGraphNode
     */
    public MultiGraphNode(TimesteppedFactor factorGroup, int timestep) {
        super(factorGroup.getName());
        this.factorGroup = factorGroup;

        // set the display text
        // setName(getFactorGroup().getName());

        modelToDisplay = new HashMap();
        setTimestep(timestep);
    }

    /**
     * Set the timestep that this node represents
     */
    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    /**
     * Returns the timestep that this node represents
     */
    public int getTimestep() {
        return timestep;
    }

    /**
     * Returns the group that controls this node
     */
    public TimesteppedFactor getFactorGroup() {
        return factorGroup;
    }

    /**
     * takes in a set of LaggedFactor objects that represent all edges going
     * into this factor, and creates corresponding edges (if they don't already
     * exist)
     */
    public void synchEdges(SortedSet laggedFactors) {
        SortedSet toCreate, toRemove;

        // i believe new TreeSets have to be created in order to not disturb the
        // source sets (toCreate and toRemove are only temporary sets used for
        // determining what elements to remove- the original sets must not be
        // modified though, because removing an element is more complicated than
        // just removing it from the set)
        toRemove = new TreeSet(modelToDisplay.keySet()); // get current display
        toCreate = new TreeSet(laggedFactors);

        // if edgeMode is EDGE_LAG, only edges in/out of time 0 should be added
        if (getFactorGroup().getEdgeMode() == MultiGraphWorkbench.EDGE_LAG) {

            // in TIME_DEST mode, laggedFactors should only be added if this node's timestep is 0
            if (getFactorGroup().getTimeMode() == MultiGraphWorkbench.TIME_DEST)
            {
                if (getTimestep() != 0) {
                    toCreate.clear();
                }
            }
            else { // TIME_SRC
                // only add edges if they were sourced from time 0
                int myTime = getTimestep();
                Iterator it = toCreate.iterator();
                while (it.hasNext()) {
                    LaggedFactor itm = (LaggedFactor) it.next();
                    if (myTime - itm.getLag() != 0) {
                        it.remove();
                    }
                }
            }
        }

        /*
        System.out.println("PRE-OPTIMIZATION: ");
        System.out.print("current display: ");
        for (Iterator pit = modelToDisplay.keySet().iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("");
        System.out.print("to remove: ");
        for (Iterator pit = toRemove.iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("");
        System.out.print("to create: ");
        for (Iterator pit = toCreate.iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("\n");
         */

        // optimize toCreate and toRemove by
        //  1) removing items in toCreate that are already there
        //  2) removing items in toRemove that are supposed to be there
        SortedSet toKeep = new TreeSet(toCreate);
        toCreate.removeAll(
                toRemove); // create anything that's not in current display
        toRemove.removeAll(
                toKeep); // remove anything in current that's not in the original toCreate set

        /*
        System.out.println("POST-OPTIMIZATION: ");
        System.out.print("current display: ");
        for (Iterator pit = modelToDisplay.keySet().iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("");
        System.out.print("to remove: ");
        for (Iterator pit = toRemove.iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("");
        System.out.print("to create: ");
        for (Iterator pit = toCreate.iterator(); pit.hasNext(); ) {
          LaggedFactor itm = (LaggedFactor) pit.next();
          System.out.print(itm + " ");
        }
        System.out.println("\n");
         */


        LaggedFactor itm;

        // remove items to be removed
        Iterator items = toRemove.iterator();
        while (items.hasNext()) {
            itm = (LaggedFactor) items.next();
            getFactorGroup().getContainer().unbindEdge(
                    (DisplayEdge) modelToDisplay.get(itm));
            modelToDisplay.remove(itm);
            items.remove();
        }

        // add items to be added
        items = toCreate.iterator();
        while (items.hasNext()) {
            LaggedFactor lf = (LaggedFactor) items.next();

            // get the source node- first retrieve the display group, then retrieve
            // the timestep from the display group
            TimesteppedFactor factor = getFactorGroup().getContainer()
                    .getDisplayFactor(lf.getFactor());

            // calculate the target lag -
            int lag = getTimestep() - lf.getLag();
            if (factor != null)
            { // usually OK- sometimes synchEdges gets called before all necessary nodes
                // are added.. wish there was a cleaner way to do this though
                try {
                    DisplayNode src = factor.getDisplayNode(lag);
                    DisplayEdge displayEdge = new MultiGraphEdge(src, this);
                    modelToDisplay.put(new LaggedFactor(lf), displayEdge);
                    getFactorGroup().getContainer().bindEdge(displayEdge);
                }
                catch (IndexOutOfBoundsException e) { /* OK- display groups have a finite number of timesteps displayed */
                }
            }
        }

        // set the visibility of edges
        items = modelToDisplay.keySet().iterator();
        int myTime = getTimestep();
        boolean iAmZero = (myTime == 0);

        while (items.hasNext()) {
            LaggedFactor key = (LaggedFactor) items.next();
            MultiGraphEdge edge = (MultiGraphEdge) modelToDisplay.get(key);

            if (edge == null) {
                System.out.println(key + " doesn't have an associated edge");
            }

            // simple case- for these two modes, if the edges are there, they should be
            // fully visible
            if (getFactorGroup().getEdgeMode() == MultiGraphWorkbench
                    .EDGE_REPEATING || getFactorGroup()
                    .getEdgeMode() == MultiGraphWorkbench.EDGE_LAG) {
                edge.setVisibility(255);
            }
            else {
                if (getFactorGroup().getTimeMode() == MultiGraphWorkbench
                        .TIME_DEST) {
                    // if this node is time 0, all edges should be fully visible, otherwise
                    // make them 50%
                    edge.setVisibility(iAmZero ? 255 : 32);
                }
                else {
                    // otherwise, set visibility to full only if the edge was sourced from 0
                    int srcTime = myTime - key.getLag();
                    edge.setVisibility((srcTime == 0) ? 255 : 32);
                }
            }

            edge.repaint();
        }

    }

    /**
     * Removes all display edges going into this node
     */
    public void removeDisplayEdges() {
        //Set toRemove = modelToDisplay.entrySet();
        DisplayEdge itm;
        Iterator items = modelToDisplay.values().iterator();
        while (items.hasNext()) {
            itm = (DisplayEdge) items.next();
            getFactorGroup().getContainer().unbindEdge(itm);
            items.remove();
        }
    }

    /**
     * Launches an editor for this node.
     */
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
            getFactorGroup().getContainer().getModelGraph().renameFactor(
                    getName(), newName);
        }
        //System.out.println(ret);
        //System.out.println(newName);

    }

    /**
     * Returns the string that will be displayed (the name + ":" + the
     * timestep)
     */
    protected String getDisplayString() {
        return getName() + ":" + Integer.toString(getTimestep());
    }

    /**
     * Calculates the size of the component based on its name.
     *
     * @return the size of the component.
     */
    public Dimension getPreferredSize() {

        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        int width = fm.stringWidth(getDisplayString()) + fm.getMaxAdvance();
        int height = 2 * DisplayNodeUtils.getPixelGap() + fm.getAscent() + 3;

        width = (width < 60) ? 60 : width;

        return new Dimension(width, height);
    }

    /**
     * Paints the component.
     *
     * @param g the graphics context.
     */
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        int width = getPreferredSize().width;
        int stringWidth = fm.stringWidth(getName());
        int stringX = (width - stringWidth) / 2;
        int stringY = fm.getAscent() + DisplayNodeUtils.getPixelGap();

        Color fill, draw;
        if (super.isSelected()) {
            fill = DisplayNodeUtils.getNodeSelectedFillColor();
            draw = DisplayNodeUtils.getNodeSelectedEdgeColor();
        }
        else if (getTimestep() < 0) { // past
            // no item should have alpha < 1- therefore, add 1 to divisor
            int divisor = getFactorGroup().getPastSteps() + 1;
            int alpha = (int) (255.0f *
                    ((float) (divisor + getTimestep()) / divisor));
            fill = new Color(DisplayNodeUtils.getNodeFillColor().getRed(),
                    DisplayNodeUtils.getNodeFillColor().getGreen(), DisplayNodeUtils.getNodeFillColor().getBlue(),
                    alpha);
            draw = new Color(DisplayNodeUtils.getNodeEdgeColor().getRed(),
                    DisplayNodeUtils.getNodeEdgeColor().getGreen(), DisplayNodeUtils.getNodeEdgeColor().getBlue(),
                    alpha);
        }
        else if (getTimestep() > 0) { // future
            // first item should be full opacity, since it is a different color, therefore
            // subtract 1
            int divisor = getFactorGroup().getFutureSteps();
            int alpha = (int) (255.0f *
                    ((float) (divisor - (getTimestep() - 1)) / divisor));
            fill = new Color(FUTURE_COLOR.getRed(), FUTURE_COLOR.getGreen(),
                    FUTURE_COLOR.getBlue(), alpha);
            draw = new Color(DisplayNodeUtils.getNodeEdgeColor().getRed(),
                    DisplayNodeUtils.getNodeEdgeColor().getGreen(), DisplayNodeUtils.getNodeEdgeColor().getBlue(),
                    alpha);
        }
        else {
            fill = DisplayNodeUtils.getNodeFillColor();
            draw = DisplayNodeUtils.getNodeEdgeColor();
        }
        g2.setColor(fill);
//        g2.fill(getShape());
        g2.setColor(draw);
//        g2.draw(getShape());

        //g2.setColor(selected ? nodeSelectedFillColor : nodeFillColor);
        //g2.setColor(selected ? nodeSelectedEdgeColor : nodeEdgeColoir);
        g2.setColor(DisplayNodeUtils.getNodeTextColor());
        g2.drawString(getDisplayString(), stringX, stringY);
    }

}


