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

import edu.cmu.tetrad.util.NamingProtocol;
import edu.cmu.tetradapp.workbench.DisplayNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Represents a single factor over time by controlling a group of MultiGraphNode
 * objects. The base location of the group is specified, and is used to position
 * the timestep 0 element. Other elements are positioned at fixed intervals
 * above and below the timestep 0 element. </p> A timestep 0 element is always
 * created. The number of past and future timesteps is set by pastSteps and
 * futureSteps properties.
 *
 * @author gmli
 */
public class TimesteppedFactor {

    protected MultiGraphWorkbench container;

    // the amount into the past/future that gets displayed. both must be >= max
    // lag of editor
    protected ArrayList pastNodes;
    protected ArrayList futureNodes;

    //protected GraphNode modelNode;
    protected String name;
    protected Point basePosition;

    protected int stepSpacing; // spacing of time step to time step (vertical spacing)
    //protected String name;

    private int edgeMode = MultiGraphWorkbench.EDGE_HYBRID;
    private int timeMode = MultiGraphWorkbench.TIME_DEST;

    /**
     * Creates new TimesteppedFactor
     *
     * @param name      Name of the factor
     * @param container The editor to draw the graphical elements in
     * @param baseX     base x-coordinate, sets default position for current
     *                  time lag node. other nodes are positioned vertically
     *                  with respect to baseX, baseY
     * @param baseY     base y-coordinate, sets default position for current
     *                  time lag node. other nodes are positioned vertically
     *                  with respect to baseX, baseY
     */
    public TimesteppedFactor(String name, MultiGraphWorkbench container,
            int baseX, int baseY) {
        this.container = container;
        this.name = name;
        setBasePosition(baseX, baseY);

        stepSpacing = 100;

        // create the node for the current timestep (default is to only show
        // that one- additional timesteps must be changed using the setPast/FutureSteps
        // methods. Will be added as element 0 into both lists
        MultiGraphNode cur = new MultiGraphNode(this, 0);
        pastNodes = new ArrayList();
        futureNodes = new ArrayList();
        pastNodes.add(cur);
        futureNodes.add(cur);

        // add to display
        addNodeToDisplay(0);
    }

    /**
     * Sets the number of steps into the past that are displayed. Does not add
     * edges for newly created nodes (the synchDisplayEdges() method should be
     * called after the setPastSteps (and setFutureSteps) methods have been
     * called for all factors).
     *
     * @param pastSteps A positive number for the desired number of past
     *                  timesteps displayed
     */
    public void setPastSteps(int pastSteps) {
        // it is either going to be remove, or add- these two while loops are
        // mutually exclusive
        int nextTimestep = getPastSteps();
        while (nextTimestep < pastSteps) { // add
            nextTimestep = -(nextTimestep + 1);
            MultiGraphNode next = new MultiGraphNode(this, nextTimestep);
            pastNodes.add(next);
            // getPastSteps()-1 is the timestep of the newly added item
            addNodeToDisplay(nextTimestep);

            nextTimestep = getPastSteps();
        }
        int toRemove = getPastSteps();
        while (toRemove > pastSteps) { // remove
            MultiGraphNode itm = (MultiGraphNode) pastNodes.remove(toRemove);
            itm.removeDisplayEdges();
            getContainer().unbindNode(itm);

            toRemove = getPastSteps();
        }
    }

    /**
     * Returns the number of pastSteps displayed
     */
    public int getPastSteps() {
        return pastNodes.size() - 1;
    }

    /**
     * Sets the number of steps into the future that are displayed. Does not add
     * edges for newly created nodes (the synchDisplayEdges() method should be
     * called after the setFutureSteps (and setPastSteps) methods have been
     * called for all factors).
     *
     * @param futureSteps A positive number for the desired number of future
     *                    timesteps displayed      .
     */
    public void setFutureSteps(int futureSteps) {
        // it is either going to be remove, or add- these two while loops are
        // mutually exclusive
        int nextTimestep = getFutureSteps();
        while (nextTimestep < futureSteps) { // add
            nextTimestep++;
            MultiGraphNode next = new MultiGraphNode(this, nextTimestep);
            futureNodes.add(next);
            addNodeToDisplay(nextTimestep);

            nextTimestep = getFutureSteps();
        }
        int toRemove = getFutureSteps();
        while (toRemove > futureSteps) { // remove
            MultiGraphNode itm = (MultiGraphNode) futureNodes.remove(toRemove);
            itm.removeDisplayEdges();
            getContainer().unbindNode(itm);

            toRemove = getFutureSteps();
        }

    }

    /**
     * Returns the number of steps into the future displayed
     */
    public int getFutureSteps() {
        return futureNodes.size() - 1;
    }

    /**
     * sets position of node according to defaults and then adds to container
     * note that it does NOT add edges to the node. This has to be done after
     * all nodes are added
     */
    protected void addNodeToDisplay(int timestep) {
        Point pos = new Point(getBasePosition());
        pos.translate(0, timestep * getStepSpacing());
        //timestep = Math.abs(timestep);

        DisplayNode itm = getDisplayNode(timestep);
        Dimension dim = itm.getPreferredSize();
        itm.setSize(dim);
        itm.setLocation(pos.x - (dim.width >> 1), pos.y - (dim.height >> 1));

        getContainer().bindNode(itm);
    }

    /**
     * creates/removes edges as necessary to match display with modelNode
     */
    public void synchDisplayEdges() {
        // go through all nodes and ensure that alll of them have the right edges
        // connected to them

        // retrieve lagged factors from the model
        SortedSet laggedFactors =
                getContainer().getModelGraph().getParents(getName());

        Iterator items = pastNodes.iterator();
        while (items.hasNext()) {
            ((MultiGraphNode) items.next()).synchEdges(laggedFactors);
        }

        items = futureNodes.iterator();
        while (items.hasNext()) {
            ((MultiGraphNode) items.next()).synchEdges(laggedFactors);
        }

    }

    /**
     * removes all display edges, to be called before the factor is deleted
     */
    public void removeDisplayEdges() {
        Iterator items = pastNodes.iterator();
        while (items.hasNext()) {
            ((MultiGraphNode) items.next()).removeDisplayEdges();
        }
        items = futureNodes.iterator();
        while (items.hasNext()) {
            ((MultiGraphNode) items.next()).removeDisplayEdges();
        }
    }

    /**
     * removes all associated display nodes and edges
     */
    public void removeDisplayNodes() {
        // remove all items from past, except for the present time step. The
        // present timestep node will be removed when the future nodes are removed
        for (int toRemove = getPastSteps(); toRemove > 0; toRemove--) {
            MultiGraphNode itm = (MultiGraphNode) pastNodes.remove(toRemove);
            itm.removeDisplayEdges();
            getContainer().unbindNode(itm);
        }

        Iterator items = futureNodes.iterator();
        while (items.hasNext()) {
            MultiGraphNode itm = (MultiGraphNode) items.next();
            itm.removeDisplayEdges();
            getContainer().unbindNode(itm);
            items.remove();
        }
    }

    /*
	void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	 */

    /**
     * Returns the graphical element in the display corresponding to the
     * requested timestep
     *
     * @param timestep negative for past time steps, 0 for the current time step
     *                 node, positive for future time steps
     */
    public MultiGraphNode getDisplayNode(int timestep) {
        if (timestep < 0) {
            return (MultiGraphNode) pastNodes.get(-timestep);
        }
        else {
            return (MultiGraphNode) futureNodes.get(timestep);
        }
    }

    /**
     * Returns the display mode of the edges
     */
    public int getEdgeMode() {
        return edgeMode;
    }

    /**
     * Sets the display mode of edges. There are 3 possible modes: <ul> <li>
     * EDGE_LAG - only the representative edges are shown <li> EDGE_HYBRID - the
     * representative edges are shown as dark blue, and implied edges shown as
     * ghosted <li> EDGE_REPEATING - all edges, both representative and implied
     * are shown dark blue </ul>
     */
    public void setEdgeMode(int edgeMode) {
        switch (edgeMode) {
            case MultiGraphWorkbench.EDGE_LAG:
            case MultiGraphWorkbench.EDGE_HYBRID:
            case MultiGraphWorkbench.EDGE_REPEATING:
                this.edgeMode = edgeMode;
                synchDisplayEdges();
                break;
            default:
                throw new IllegalArgumentException("illegal edge mode");
        }
    }

    /**
     * Returns the time mode of the factor
     */
    public int getTimeMode() {
        return timeMode;
    }

    /**
     * Sets the time mode of the factor. There are only two possible modes: <ul>
     * <li> TIME_DEST - edges going into current time are the representative
     * edges <li> TIME_SRC - edges sourced from the current time are the
     * representative edges </ul>
     */
    public void setTimeMode(int timeMode) {
        switch (timeMode) {
            case MultiGraphWorkbench.TIME_DEST:
            case MultiGraphWorkbench.TIME_SRC:
                this.timeMode = timeMode;
                synchDisplayEdges();
                break;
            default:
                throw new IllegalArgumentException("illegal time mode");
        }
    }

    /**
     * Returns the base position of the factor (the position of the timestep 0
     * element)
     */
    public Point getBasePosition() {
        return basePosition;
    }

    /**
     * Sets the base position of the factor (the position of the timestep 0
     * element)
     */
    public void setBasePosition(int x, int y) {
        /* TODO: translate all existing nodes */

        basePosition = new Point(x, y);
    }

    /**
     * Returns vertical spacing between timesteps
     */
    public int getStepSpacing() {
        return stepSpacing;
    }

    /**
     * Sets the vertical spacing between timesteps
     */
    public void setStepSpacing(int stepSpacing) {
        /* TODO: rescale all nodes' vertical spacing */

        this.stepSpacing = stepSpacing;
    }

    /**
     * Returns the editor that the display nodes are drawn in
     */
    public MultiGraphWorkbench getContainer() {
        return container;
    }

    /**
     * Returns the name of the factor
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the factor
     */
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        if (!NamingProtocol.isLegalName(name)) {
            throw new IllegalArgumentException(
                    NamingProtocol.getProtocolDescription());
        }

        this.name = name;
        Iterator it;
        for (it = pastNodes.iterator(); it.hasNext();) {
            DisplayNode itm = (DisplayNode) it.next();
            itm.setName(name);
            //itm.repaint();
        }
        for (it = futureNodes.iterator(); it.hasNext();) {
            DisplayNode itm = (DisplayNode) it.next();
            itm.setName(name);
            //itm.repaint();
        }

    }

}


