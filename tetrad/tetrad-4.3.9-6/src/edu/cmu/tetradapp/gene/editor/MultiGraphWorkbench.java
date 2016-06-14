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

import edu.cmu.tetrad.gene.graph.ActiveLagGraph;
import edu.cmu.tetrad.gene.history.LaggedEdge;
import edu.cmu.tetrad.gene.history.LaggedFactor;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.util.ImageUtils;
import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.IDisplayEdge;
import edu.cmu.tetradapp.workbench.Rubberband;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Provides a editor for creating/editing lag graphs.
 *
 * @author Gregory Li
 * @author Joseph Ramsey
 * @author Aaron Powell
 * @author Willie Wheeler
 */
public class MultiGraphWorkbench extends JComponent
        implements LagGraphWorkbench, Scrollable {

    //===================PUBLIC STATIC FINAL FIELDS=============

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    public static final int SELECT_MOVE = 0;

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    public static final int ADD_NODE = 1;

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    public static final int ADD_EDGE = 2;

    /**
     * The base string that is used for factor names. Actual factors will have
     * this base string + a numerical identifier tacked on the end
     */
    public static final String BASE_FACTOR_NAME = "Gene";

    // edge display modes- sets the visiblity of the edges
    public static final int EDGE_LAG = 0;
    public static final int EDGE_HYBRID = 1;
    public static final int EDGE_REPEATING = 2;

    // time display modes- works in conjunction with edgeMode to paint
    // principle edges as coming into nodes, or coming out
    /**
     * edges affecting time 0 nodes will be painted as principle
     */
    public static final int TIME_DEST = 0;
    /**
     * edges that come from time 0 will be painted as principle
     */
    public static final int TIME_SRC = 1;

    // because the canvas cannot scroll into negative numbers, I have to reserve
    // space for the past nodes. This constant specifies how many time steps to
    // reserve space for
    public static final int MAX_TIMELAG = 10;
    //=====================PROTECTED FIELDS=====================

    protected ActiveLagGraph modelGraph;
    protected Map displayFactors;

    //=====================PRIVATE FIELDS========================

    /**
     * The current mode of the editor.
     */
    private int workbenchMode = SELECT_MOVE;

    /**
     * The current background color of the editor.
     */
    private final Color BACKGROUND = Color.white;

    /**
     * When edges are being constructed, one edge is anchored to a node and the
     * other edge tracks mouse dragged events; this is the edge that does this.
     * This edge should be null unless an edge is actually being tracked.
     */
    private DisplayEdge trackedEdge;

    /**
     * For dragging nodes, a click point is needed; this is that click point.
     */
    private Point clickPoint;

    /**
     * For selecting multiple nodes using a rubberband, a rubberband is needed;
     * this is it.
     */
    private Rubberband rubberband;

    /**
     * Indicates whether rubberband selection is permitted.
     */
    private boolean allowRubberband = true;

    /**
     * Indicates whether user editing is permitted.
     */
    private boolean allowUserEditing = true;

    /**
     * Indicates whether multiple node selection is allowed.
     */
    private boolean allowMultipleNodeSelection = true;

    /**
     * Handler for <code>ComponentEvent</code>s.
     */
    private ComponentHandler compHandler = new ComponentHandler();

    /**
     * Handler for <code>MouseEvent</code>s and <code>MouseMotionEvent</code>s.
     */
    private MouseHandler mouseHandler = new MouseHandler();

    /**
     * Handler for <code>PropertyChangeEvent</code>s.
     */
    private PropertyChangeHandler propChangeHandler =
            new PropertyChangeHandler();

    private JPopupMenu nodePopup, edgePopup;

    /**
     * dictates where the next factor should be placed
     */
    private int next_factor_cursor = 0;

    /**
     * how many past and future timesteps should be shown
     */
    private int past_steps_shown, future_steps_shown;

    /**
     * control spacing between elements
     */
    private int hgap, vgap;

    /* the label for the current timestep */
    private JPanel curTimeGroup;
    private JPanel pastPanel, curPanel, futurePanel;
    private JPanel[] curTimePanels;

    //=================PUBLIC STATIC FINAL FIELDS=================
    public static final int MEASURED_NODE = 0;
    public static final int DIRECTED_EDGE = 0;

    //====================PRIVATE FIELDS=======================
    // these currently behave like constants (there are no modifier functions), but
    // I left them in in case in the future different node
    private int edgeMode = DIRECTED_EDGE;
    private int edgeDisplayMode = EDGE_HYBRID;
    private int timeMode = TIME_DEST;

    // these constants used to identify menu actions
    private static final String POPUP_DELETE = "Delete selection";
    private static final String POPUP_RENAME = "Rename factor";

    /**
     * Constructor declaration
     */
    public MultiGraphWorkbench(ActiveLagGraph lagGraph) {
        //setGraph(graph);

        // It seems these have to be fixed, since accessing the
        // AbstractWorkbench from here would break the architecture.
        hgap = 120;
        vgap = 100;

        initComponents();

        displayFactors = new HashMap();

        setModelGraph(lagGraph);

        // this can only be done once during initialization- doing it more than once
        // causes only the results of the first call to be displayed
        Dimension group_size = curTimeGroup.getPreferredSize();
        curTimeGroup.setBounds(10, vgap * (MAX_TIMELAG) - 10, group_size.width,
                group_size.height);
        repaint();
        revalidate();
        adjustPreferredSize();

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        setCursor(null);
    }

    private void initComponents() {
        JMenuItem menuItem;
        ActionListener popupListener = new PopupListener();

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        // create the popup menu for nodes
        nodePopup = new JPopupMenu();
        menuItem = new JMenuItem(POPUP_RENAME);
        menuItem.addActionListener(popupListener);
        nodePopup.add(menuItem);

        menuItem = new JMenuItem(POPUP_DELETE);
        menuItem.addActionListener(popupListener);
        menuItem.setAccelerator(delete);
        nodePopup.add(menuItem);

        // create the popup menu for edges
        edgePopup = new JPopupMenu();
        menuItem = new JMenuItem(POPUP_DELETE);
        menuItem.addActionListener(popupListener);
        menuItem.setAccelerator(delete);
        edgePopup.add(menuItem);

        // add stuff for marking current time
        // the layout of this was a pain- all to get the three labels aligned to the
        // left on the same axis
        Color transparentWhite = new Color(255, 255, 255, 0);

        JLabel pastLabel = new JLabel("Past time",
                new ImageIcon(ImageUtils.getImage(this, "arrow-up.gif")),
                JLabel.CENTER);
        JLabel futureLabel = new JLabel("Future time",
                new ImageIcon(ImageUtils.getImage(this, "arrow-down.gif")),
                JLabel.CENTER);
        JLabel curLabel = new JLabel("Current Time",
                new ImageIcon(ImageUtils.getImage(this, "arrow-left.gif")),
                JLabel.CENTER);

        // create the panels- past, present and future
        pastPanel = new JPanel();
        pastPanel.setBackground(transparentWhite);
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.X_AXIS));
        futurePanel = new JPanel();
        futurePanel.setBackground(transparentWhite);
        futurePanel.setLayout(new BoxLayout(futurePanel, BoxLayout.X_AXIS));
        curPanel = new JPanel();
        curPanel.setBackground(transparentWhite);
        curPanel.setBorder(
                BorderFactory.createLineBorder(curLabel.getForeground()));
        curPanel.setLayout(new BoxLayout(curPanel, BoxLayout.X_AXIS));

        // create outer frame
        curTimeGroup = new JPanel();
        //GridBagLayout gridBag = new GridBagLayout();
        //GridBagConstraints gCon = new GridBagConstraints();
        curTimeGroup.setLayout(new BorderLayout());
        curTimeGroup.setBackground(transparentWhite);

        // calculate layout-related stuff
        Dimension labelSize = curLabel.getPreferredSize();
        int boxWidth = hgap * (next_factor_cursor) + labelSize.width + 20;
        /*
        pastSpacer = new JPanel();
        pastSpacer.setBounds(new Rectangle(boxWidth - labelSize.width, labelSize.height+10));
        curSpacer = new JPanel();
        curSpacer.setBounds(new Rectangle(boxWidth - labelSize.width, vgap));
        futureSpacer = new JPanel();
        futureSpacer.setBounds(new Rectangle(boxWidth - labelSize.width, labelSize.height+10));
         */
        pastPanel.add(Box.createRigidArea(new Dimension(
                boxWidth - labelSize.width, labelSize.height + 10)));
        curPanel.add(Box.createRigidArea(
                new Dimension(boxWidth - labelSize.width, vgap)));
        futurePanel.add(Box.createRigidArea(new Dimension(
                boxWidth - labelSize.width, labelSize.height + 10)));
        JPanel[] temp = {pastPanel, curPanel, futurePanel};
        curTimePanels = temp;

        //spacer.setPreferredSize(new Dimension(boxWidth - labelSize.width, labelSize.height));
        //innerSpacer.setPreferredSize(new Dimension(boxWidth - labelSize.width, labelSize.height));
        //curTimeBox.setPreferredSize(new Dimension(boxWidth + 20, vgap));

        // set up grid basicConstraints
        /*
        gCon.gridx = 0;
        gCon.gridy = 0;
        //gCon.fill = GridBagConstraints.HORIZONTAL;
        gridBag.setConstraints(spacer, gCon);
        innerGridBag.setConstraints(innerSpacer, gCon);

        //gCon.fill = GridBagConstraints.REMAINDER;
        //gCon.fill = GridBagConstraints.HORIZONTAL;
        gCon.gridx = 1;
        gCon.gridy = 0;
        gCon.anchor = GridBagConstraints.WEST;
        gCon.ipady = 10;
        gridBag.setConstraints(pastTime, gCon);
        innerGridBag.setConstraints(curTime, gCon);

        gCon.gridx = 0;
        gCon.gridy = 1;
        gCon.gridwidth = 2;
        gCon.anchor = GridBagConstraints.WEST;
        gCon.ipady = 0;
        gridBag.setConstraints(curTimeBox, gCon);

        gCon.gridx = 1;
        gCon.gridy = 2;
        gCon.gridwidth = 1;
        gCon.anchor = GridBagConstraints.WEST;
        gCon.ipady = 10;
        gridBag.setConstraints(futureTime, gCon);
         */

        // add everything
        //curPanel.add(innerSpacer);
        //pastPanel.add(pastSpacer);
        pastPanel.add(pastLabel);
        //curPanel.add(curSpacer);
        curPanel.add(curLabel);
        curPanel.add(Box.createRigidArea(new Dimension(10, vgap)));
        //futurePanel.add(futureSpacer);
        futurePanel.add(futureLabel);
        /*
         curTimeGroup.add(spacer);
        curTimeGroup.add(pastTime);
        curTimeGroup.add(curTimeBox);
        curTimeGroup.add(futureTime);
         */
        curTimeGroup.add(pastPanel, BorderLayout.NORTH);
        curTimeGroup.add(curPanel, BorderLayout.CENTER);
        curTimeGroup.add(futurePanel, BorderLayout.SOUTH);

        //Dimension group_size = curTimeGroup.getPreferredSize();
        //curTimeGroup.setBounds(10, vgap*(MAX_TIMELAG), group_size.width, group_size.height);

        add(curTimeGroup);
    }

    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    /**
     * public accessor to vgap used to determine how many timesteps are shown
     * using only information about the viewable portion of the editor
     */
    public int getVGap() {
        return vgap;
    }

    /**
     * Returns the number of timesteps into the past shown
     */
    public int getPastSteps() {
        return past_steps_shown;
    }

    /**
     * Returns the number of timesteps into the future shown
     */
    public int getFutureSteps() {
        return future_steps_shown;
    }

    /**
     * Sets the number of timesteps into the past shown
     */
    public void setPastSteps(int past_steps) {
        if (past_steps != past_steps_shown) {
            //            int old_pastSteps = past_steps_shown;
            past_steps_shown = past_steps;
            Iterator it = displayFactors.values().iterator();
            while (it.hasNext()) {
                ((TimesteppedFactor) it.next()).setPastSteps(getPastSteps());
            }
            //firePropertyChange("pastSteps", old_pastSteps, getPastSteps());
        }
    }

    /**
     * Sets the number of timesteps into the future shown
     */
    public void setFutureSteps(int future_steps) {
        if (future_steps != future_steps_shown) {
            //            int old_futureSteps = future_steps_shown;
            future_steps_shown = future_steps;
            Iterator it = displayFactors.values().iterator();
            while (it.hasNext()) {
                ((TimesteppedFactor) it.next()).setFutureSteps(
                        getFutureSteps());
            }

            //firePropertyChange("futureSteps", old_futureSteps, getFutureSteps());
        }
    }

    /**
     * Returns the display mode of the edges
     */
    public int getEdgeDisplayMode() {
        return edgeDisplayMode;
    }

    /**
     * Sets the display mode of edges. There are 3 possible modes: <ul> <li>
     * EDGE_LAG - only the representative edges are shown <li> EDGE_HYBRID - the
     * representative edges are shown as dark blue, and implied edges shown as
     * ghosted <li> EDGE_REPEATING - all edges, both representative and implied
     * are shown dark blue </ul>
     */
    public void setEdgeDisplayMode(int edgeMode) {
        if (edgeMode != this.edgeDisplayMode) {
            switch (edgeMode) {
                case EDGE_LAG:
                case EDGE_HYBRID:
                case EDGE_REPEATING:
                    this.edgeDisplayMode = edgeMode;
                    Iterator it = displayFactors.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        getDisplayFactor(key).setEdgeMode(getEdgeDisplayMode());
                    }

                    firePropertyChange("edgeDisplayMode", null,
                            getEdgeDisplayMode());
                    break;
                default:
                    throw new IllegalArgumentException("illegal edge mode");
            }
        }
    }

    /**
     * Returns the time mode used
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
        if (timeMode != this.timeMode) {
            switch (timeMode) {
                case TIME_DEST:
                case TIME_SRC:
                    this.timeMode = timeMode;
                    Iterator it = displayFactors.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        getDisplayFactor(key).setTimeMode(getTimeMode());
                    }
                    //firePropertyChange("timeMode", null, getTimeMode());
                    break;
                default:
                    throw new IllegalArgumentException("illegal time mode");
            }
        }
    }

    /**
     * Returns the model editor of the editor (the underlying model of the
     * view)
     */
    public ActiveLagGraph getModelGraph() {
        return this.modelGraph;
    }

    /**
     * Sets the model editor of the editor (the underlying model of the view)
     */
    public void setModelGraph(
            ActiveLagGraph modelGraph) { // throws IllegalStateException, IllegalAccessException {
        if (getModelGraph() != null) {
            getModelGraph().removePropertyChangeListener(propChangeHandler);
        }
        this.modelGraph = modelGraph;
        unregisterKeys();
        if (modelGraph != null) {
            // Get a reference to the factors in the editor.
            SortedSet factors = getModelGraph().getFactors();

            int maxLag = getModelGraph().getMaxLagAllowable();
            setPastSteps(MAX_TIMELAG + maxLag);
            setFutureSteps(MAX_TIMELAG + maxLag);

            // synch the display set to the model set
            /*            Set toRemove = displayFactors.keySet().clone();
            //            Set toCreate = new TreeSet(factors);
            //            toCreate.removeAll(toRemove);
            //            toRemove.removeAll(factors);

                        for (Iterator it = toRemove.iterator(); it.hasNext();) {
                            removeNode((String) it.next());
            //                it.remove();
                        }
            */
            while (displayFactors.size() > 0) {
                String f = (String) displayFactors.keySet().iterator().next();
                next_factor_cursor = 0;
                removeNode(f);
            }

            for (Iterator it = factors.iterator(); it.hasNext();) {
                addNode((String) it.next());
            }

            // Add edges for each of the edges in the model.
            for (Iterator it = factors.iterator(); it.hasNext();) {
                String factor = (String) it.next();
                getDisplayFactor(factor).synchDisplayEdges();
            }

            adjustPreferredSize();

            getModelGraph().addPropertyChangeListener(propChangeHandler);
            registerKeys();
            //firePropertyChange("graph", null, getModelGraph());
        }
    }

    /**
     * Moves the scrollbar so that the current time is displayed, with one
     * future timestep, and as many past timesteps as will fit
     */
    public void scrollToHome() {
        Iterator it = displayFactors.keySet().iterator();
        if (it.hasNext()) {
            String itm = (String) it.next();

            Rectangle init = null;
            for (int timestep = -1; timestep <= 1; timestep++) {
                if (getDisplayFactor(itm).getPastSteps() >= timestep ||
                        getDisplayFactor(itm).getFutureSteps() >= timestep) {
                    if (init == null) {
                        init = getDisplayFactor(itm).getDisplayNode(timestep)
                                .getBounds();
                    }
                    while (it.hasNext()) {
                        itm = (String) it.next();
                        init = init.union(
                                getDisplayFactor(itm).getDisplayNode(timestep)
                                        .getBounds());
                    }
                }
            }

            // pad the rectangle with a little vgap on the top and bottom
            init.translate(0, -vgap / 2);
            init.setSize((int) init.getWidth(), (int) init.getHeight() + vgap);

            scrollRectToVisible(init);
        }
        else {
            scrollRectToVisible(getVisibleRect());
        }
    }

    /**
     * Returns the display group corresponding to the name
     */
    public TimesteppedFactor getDisplayFactor(String name) {
        return (TimesteppedFactor) displayFactors.get(name);
    }

    /**
     * Adds a node to the display and model. The name is automatically
     * generated
     */
    public void newNode() {
        String name = nextVariableName(BASE_FACTOR_NAME);
        getModelGraph().addFactor(name);

        // add edge to self
        LaggedFactor toSelf = new LaggedFactor(name, 1);
        getModelGraph().addEdge(name, toSelf);
        //adjustPreferredSize();
    }

    /**
     * Adds the given model node to the model and adds a corresponding display
     * node to the display.
     *
     * @return the display node which was added.
     */
    protected TimesteppedFactor addNode(String name) {

        // move the curTime label
        //curTime.setLocation(curTime.getX()+hgap, curTime.getY());
        for (int i = 0; i < curTimePanels.length; i++) {
            //Dimension size = spacers[i].getSize();
            //spacers[i].setSize(new Dimension(size.width + hgap, size.height));
            curTimePanels[i].add(Box.createRigidArea(new Dimension(hgap, 1)),
                    0);
            //spacers[i].getParent().invalidate();
        }

        //curTimeGroup.invalidate();
        //curTimeGroup.revalidate();
        //Point group_pos = curTimeGroup.getLocation();
        //curTimeGroup.repaint();

        //curTimeGroup.invalidate();

        // determine next position
        int nodeX = -(hgap / 2) + hgap * ++next_factor_cursor;
        int nodeY = 70 + vgap *
                MAX_TIMELAG; // getModelGraph().getMaxLagAllowable(); //70 + vgap * (displayMlag - lag);

        //Point center = new Point(nodeX, nodeY);
        TimesteppedFactor displayNode =
                new TimesteppedFactor(name, this, nodeX, nodeY);

        displayFactors.put(name, displayNode);

        displayNode.setPastSteps(getPastSteps());
        displayNode.setFutureSteps(getFutureSteps());
        displayNode.setTimeMode(getTimeMode());
        displayNode.setEdgeMode(getEdgeDisplayMode());

        return displayNode;

        // Fire notification event. jdramsey 12/11/01
        //firePropertyChange("nodeAdded", null, displayNode);
    }

    /**
     * Removes a display mode from the editor. This method should not be called
     * directly, and is instead meant upon recieving an event from the model
     * editor
     */
    public void removeNode(String name) {
        TimesteppedFactor factor =
                (TimesteppedFactor) displayFactors.remove(name);
        factor.removeDisplayNodes();
        displayFactors.remove(name);
        adjustPreferredSize();
    }

    /**
     * Adds a single timestep of a factor to the display. This is not meant to
     * be called directly. Instead, a TimesteppedFactor uses this method to add
     * individual display nodes to the display
     */
    public void bindNode(DisplayNode displayNode) {
        Component displayNodeComp = (Component) displayNode;
        add(displayNodeComp, 0);

        // Add listeners.
        displayNodeComp.addComponentListener(compHandler);
        displayNodeComp.addMouseListener(mouseHandler);
        displayNodeComp.addMouseMotionListener(mouseHandler);
        displayNodeComp.setSize(displayNodeComp.getPreferredSize());

        // Fire notification event. jdramsey 12/11/01
        //firePropertyChange("nodeAdded", null, displayNode);
        adjustPreferredSize();
    }

    /**
     * add a DisplayEdge directly to the display, adding all necessary
     * connections to the workspace. This is not meant to be called directly.
     * Instead, a TimesteppedFactor uses this method to add individual display
     * nodes to the display
     */
    public void bindEdge(DisplayEdge displayEdge) {
        // add the display edge to the editor.
        add(displayEdge, -1);    // add to back

        //deselectAll();
        // Add listeners.
        displayEdge.addComponentListener(compHandler);
        displayEdge.addMouseListener(mouseHandler);
        displayEdge.addMouseMotionListener(mouseHandler);
        displayEdge.addPropertyChangeListener(propChangeHandler);

        // Fire notification. jdramsey 12/11/01
        //firePropertyChange("edgeAdded", null, displayEdge);
    }

    /**
     * Gets a new "tracking edge"--that is, an edge which is anchored at one end
     * to a node but tracks the mouse at the other end. Used for drawing new
     * edges.
     *
     * @param node     the node to anchor to.
     * @param mouseLoc the location of the mouse.
     * @return the new tracking edge (a display edge).
     */
    public DisplayEdge getNewTrackingEdge(DisplayNode node, Point mouseLoc) {

        DisplayEdge trackedEdge = null;

        switch (edgeMode) {

            case DIRECTED_EDGE:
                trackedEdge =
                        new DisplayEdge(node, mouseLoc, DisplayEdge.DIRECTED);
                break;
            default :
                throw new IllegalStateException();
        }

        return trackedEdge;
    }

    /**
     * Starts a tracked edge by anchoring it to one node and specifying the
     * initial mouse track point.
     *
     * @param node     the initial anchored node.
     * @param mouseLoc the initial tracking mouse location.
     */
    private void startEdge(DisplayNode node, Point mouseLoc) {

        if (trackedEdge != null) {
            remove(trackedEdge);

            trackedEdge = null;

            repaint();
        }

        trackedEdge = getNewTrackingEdge(node, mouseLoc);

        add(trackedEdge, -1);
        deselectAll();
    }

    /**
     * Finishes the drawing of a new edge.
     *
     * @see #startEdge
     */
    private void finishEdge() {

        if (trackedEdge == null) {
            return;
        }

        // retrieve the two display components this edge should connect.
        DisplayNode comp1 = trackedEdge.getComp1();
        Point p = trackedEdge.getTrackPoint();
        DisplayNode comp2 = findNearestNode(p);

        // drawn from node1 to node2. That means a laggedfactor should be added to node2
        MultiGraphNode node1 = (MultiGraphNode) comp1;
        MultiGraphNode node2 = (MultiGraphNode) comp2;
        int lag = node2.getTimestep() - node1.getTimestep();

        // reset the tracked edge to null to wait for the next attempt
        // at adding an edge.
        remove(trackedEdge);
        trackedEdge = null;

        LaggedFactor lf =
                new LaggedFactor(node1.getFactorGroup().getName(), lag);
        try {
            getModelGraph().addEdge(node2.getFactorGroup().getName(), lf);
        }
        catch (IllegalArgumentException e) { /*ignore*/
        }

        repaint();

    }

    /**
     * adjusts min past & future timesteps shown to reflect edges being added
     * that were greater than the previous maxlag (this can happen if an edge is
     * added from the past into the future)
     */
    protected void adjustTimestepsShown() {
        int maxlag = getModelGraph().getMaxLagAllowable();
        if (getPastSteps() < maxlag) {
            setPastSteps(maxlag);
        }
        if (getFutureSteps() < maxlag) {
            setFutureSteps(maxlag);
        }
    }

    /**
     * Given base <b> (a String), returns the first node in the sequence "<b>1",
     * "<b>2", "<b>3", etc., which is not already the name of a node in the
     * editor.
     *
     * @param base the base string.
     * @return the first string in the sequence not already being used.
     */

    public String nextVariableName(String base) {

        // AbstractVariable names should start at 1
        int i = 0;
        String name = null;

        loop:
        while (true) {
            i++;

            name = base + i;
            if (modelGraph.existsFactor(name)) {
                continue loop;
            }

            return name;
        }
    }

    /**
     * the following to implement Scrollable interface. Returns a space big
     * enough to display the maxLagAllowable number of timesteps
     */
    public Dimension getPreferredScrollableViewportSize() {
        Component[] components = getComponents();

        // starts at (0, 0, 0, 0).
        Rectangle rw =
                curTimeGroup.getBounds(); //new Rectangle(0,0, 330,330); //400, 400);
        Rectangle rh = curTimeGroup.getBounds(); //new Rectangle(0,0, 330,330);

        int timeMode = getTimeMode();

        // for the width, take the intersection of all items
        // for the height, only display enough to show all the past/future timesteps,
        // the current time step, and one future/past timestep
        int maxLag = getModelGraph().getMaxLagAllowable();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof MultiGraphNode) {
                MultiGraphNode n = (MultiGraphNode) components[i];

                if (Math.abs(n.getTimestep()) <= 1 || (
                        Math.abs(n.getTimestep()) <= maxLag && (
                                timeMode == TIME_DEST && n.getTimestep() < 0 ||
                                        timeMode == TIME_SRC &&
                                                n.getTimestep() > 0))) {
                    rh = rh.union(n.getBounds());
                }
            }

            rw = rw.union(components[i].getBounds());
        }
        rh.setSize(rh.width, rh.height + vgap);

        return new Dimension(rw.width, rh.height);
        //setPreferredSize(new Dimension(rw.width, rh.height));
    }

    /**
     * Returns an increment equivalent to 3 timesteps
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return vgap * 3;
    }

    /**
     * Returns false
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Returns false
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Returns an increment big enough to scroll one timestep
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return vgap;
    }

    /**
     * Adjusts the bounds of the editor to included the point (0, 0) and the
     * union of the bounds rectangles of all of the components in the editor.
     * This allows for scrollbars to automatically reflect the position of a
     * component which is being dragged.
     */
    private void adjustPreferredSize() {
        Component[] components = getComponents();

        // starts at (0, 0, 0, 0).
        Rectangle r =
                new Rectangle(0, 0, 330, 2 * vgap * MAX_TIMELAG); //400, 400);

        // for the width, take the intersection of all items
        // for the height, only display enough to show all the past/future timesteps,
        // the current time step, and one future/past timestep

        for (int i = 0; i < components.length; i++) {
            r = r.union(components[i].getBounds());
        }
        r.setSize(r.width + 20,
                vgap * (2 * MAX_TIMELAG + 1));//r.height + vgap);

        setPreferredSize(new Dimension(r.width, r.height));
    }

    /**
     * Prompts whether or not to delete the selection, and then deletes it if
     * the user says "OK"
     */
    protected void promptDeleteSelectedObjects() {
        JLabel message = new JLabel("Really deleted selected objects?");
        int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
                message, "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION);
        if (ret == JOptionPane.OK_OPTION) {
            deleteSelectedObjects();
        }
    }

    /**
     * Deletes all selected edges or nodes in the editor.
     */
    protected void deleteSelectedObjects() {

        Component[] components = getComponents();

        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];

            if (comp instanceof IDisplayEdge) {
                if (((IDisplayEdge) comp).isSelected()) {
                    //removeEdge((DisplayEdge) comp);
                    MultiGraphNode node1 =
                            (MultiGraphNode) ((DisplayEdge) comp).getComp1();
                    MultiGraphNode node2 =
                            (MultiGraphNode) ((DisplayEdge) comp).getComp2();
                    int lag = node2.getTimestep() - node1.getTimestep();
                    getModelGraph().removeEdge(node2.getName(),
                            new LaggedFactor(node1.getName(), lag));
                }
            }
            else if (comp instanceof DisplayNode) {
                if (((DisplayNode) comp).isSelected()) {
                    //removeNode((DisplayNode) comp);
                    getModelGraph().removeFactor(comp.getName());
                }
            }

        }

        repaint();
    }

    /**
     * Deselects all edges and nodes in the editor.
     */
    protected void deselectAll() {

        Component[] components = getComponents();

        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];

            if (comp instanceof IDisplayEdge) {
                ((IDisplayEdge) comp).setSelected(false);
            }
            else if (comp instanceof DisplayNode) {
                ((DisplayNode) comp).setSelected(false);
            }
        }

        repaint();
    }

    /**
     * Calculates the distance between two points.
     *
     * @param p1 the 'from' point.
     * @param p2 the 'to' point.
     * @return the distance between p1 and p2.
     */
    protected double distance(Point p1, Point p2) {

        double d = 0.0;

        d = (p1.x - p2.x) * (p1.x - p2.x);
        d += (p1.y - p2.y) * (p1.y - p2.y);
        d = Math.sqrt(d);

        return d;
    }

    /**
     * Finds the nearest node to a given point.  More specifically, finds the
     * node whose center point is nearest to the given point.  (If more than one
     * such node exists, the one with lowest z-order is returned.)
     *
     * @param p the point for which the nearest node is requested.
     * @return the nearest node to point p.
     */
    protected DisplayNode findNearestNode(Point p) {

        Component[] components = getComponents();
        double distance, leastDistance = Double.POSITIVE_INFINITY;
        int index = -1;

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof DisplayNode) {
                DisplayNode node = (DisplayNode) components[i];

                distance = distance(p, node.getCenterPoint());

                if (distance < leastDistance) {
                    leastDistance = distance;
                    index = i;
                }
            }
        }

        if (index != -1) {
            return (DisplayNode) (components[index]);
        }
        else {
            return null;
        }
    }

    /**
     * Finishes drawing a rubberband.
     *
     * @see #startRubberband
     */
    private void finishRubberband() {

        if (rubberband != null) {
            remove(rubberband);

            rubberband = null;

            repaint();
        }
    }

    /**
     * Returns the internal mode as an int.
     *
     * @return the editor mode. One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     * @deprecated Replaced by {@link #getWorkbenchMode()}.
     */
    public int getWorkbenchState() {
        return workbenchMode;
    }

    /**
     * Returns the internal mode as an int.
     *
     * @return The editor mode. One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     */
    public int getWorkbenchMode() {
        return workbenchMode;
    }

    /**
     * Returns the currently selected nodes as a vector.
     *
     * @return this vector.
     */
    public List getSelectedNodes() {

        List selectedNodes = new LinkedList();
        Component[] components = getComponents();

        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];

            if ((comp instanceof DisplayNode) &&
                    ((DisplayNode) comp).isSelected()) {
                selectedNodes.add(comp);
            }
        }

        return selectedNodes;
    }

    /**
     * Detemines whether user editing is allowed in the editor--that is, whether
     * the editor will respond to user-generated events.
     *
     * @return true if user editing is allowed, false if not.
     */
    public boolean getAllowUserEditing() {
        return allowUserEditing;
    }

    /**
     * Paints the background of the editor.
     *
     * @param g
     */
    public void paint(Graphics g) {

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

    /**
     * Registers the remove and backspace keys to remove selected objects.
     */
    protected void registerKeys() {

        Action deleteAction = new AbstractAction() {
            /**
             * Deletes selected objects.
             */
            public void actionPerformed(ActionEvent e) {
                promptDeleteSelectedObjects();
            }
        };

        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(backspace, "DELETE");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(delete, "DELETE");

        getActionMap().put("DELETE", deleteAction);

    }

    /**
     * Removes the given display node from the editor by requesting that the
     * model remove the corresponding model node.
     *
     * @param displayNode the display node.
     */
    protected void unbindNode(DisplayNode displayNode) {

        if (displayNode == null) {
            return;
        }

        //Node modelNode = (Node) (displayToModel.get(displayNode));

        remove((Component) displayNode);
        repaint();
    }

    /**
     * remove a graphEdge in the display
     */
    protected void unbindEdge(DisplayEdge displayEdge) {

        try {
            remove(displayEdge);
            repaint();

            // Fire notification. jdramsey 12/11/01
            //firePropertyChange("edgeRemoved", displayEdge, null);
        }
        catch (NullPointerException ex) {
        }
    }

    /**
     * Selects all nodes inside the rubberband and deselects nodes (and edge)
     * outside the rubberband.
     *
     * @param rubberband the rubberband.
     */
    private void selectAllInRubberband(Rubberband rubberband) {

        Shape rubberShape = rubberband.getShape();
        Point rubberLoc = rubberband.getLocation();
        Component[] components = getComponents();

        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            Rectangle bounds = comp.getBounds();

            bounds.translate(-rubberLoc.x, -rubberLoc.y);

            if (rubberShape.intersects(bounds)) {
                if (comp instanceof DisplayNode) {
                    ((DisplayNode) comp).setSelected(true);
                }
            }
            else {
                if (comp instanceof IDisplayEdge) {
                    ((IDisplayEdge) comp).setSelected(false);
                }
                else if (comp instanceof DisplayNode) {
                    ((DisplayNode) comp).setSelected(false);
                }
            }
        }
    }

    /**
     * Sets the mode of the editor to the indicated new mode. (Ignores
     * unrecognized modes.)
     *
     * @param workbenchMode One of SELECT_MOVE, ADD_NODE, ADD_EDGE.
     */
    public void setWorkbenchMode(int workbenchMode) {

        switch (workbenchMode) {

            case SELECT_MOVE:
            case ADD_NODE:
            case ADD_EDGE:
                if (this.workbenchMode != workbenchMode) {
                    this.workbenchMode = workbenchMode;

                    deselectAll();
                }
                break;

            default :
                throw new IllegalStateException();
        }
    }

    /**
     * Sets whether user editing will be allowed--that is, whether the editor
     * will react to events (mouse events, action events, etc.) generated by the
     * user.
     *
     * @param allowUserEditing true if user editing is allowed, false if not.
     * @see #getAllowUserEditing
     */
    public void setAllowUserEditing(boolean allowUserEditing) {

        if (this.allowUserEditing && !allowUserEditing) {
            unregisterKeys();

            this.allowUserEditing = false;
        }
        else if (!this.allowUserEditing && allowUserEditing) {
            registerKeys();

            this.allowUserEditing = true;
        }
    }

    /**
     * Starts dragging a node.
     *
     * @param p the click point for the drag.
     */
    private void startNodeDrag(Point p) {
        clickPoint = p;
    }

    /**
     * Starts drawing a rubberband to allow selection of multiple nodes.
     *
     * @param p the point where the rubberband begins.
     * @see #finishRubberband
     */
    private void startRubberband(Point p) {

        if (rubberband != null) {
            remove(rubberband);

            rubberband = null;

            repaint();
        }

        if (allowRubberband) {
            rubberband = new Rubberband(p);

            add(rubberband, 0);
            rubberband.repaint();
        }
    }

    /**
     * Unregistered the keyboard actions which are normally registered when the
     * user is allowed to edit the editor directly.
     *
     * @see #registerKeys
     */
    protected void unregisterKeys() {

        unregisterKeyboardAction(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    //
    // Event handler classes
    //

    /**
     * Handles <code>ComponentEvent</code>s.  We use an inner class instead of
     * the editor itself since we don't want to expose the handler methods on
     * the editor's public API.
     */
    protected class ComponentHandler implements ComponentListener {

        public void componentHidden(ComponentEvent e) {
        }

        /**
         * Adjusts scrollbars to automatically reflect the position of a
         * component which is being dragged.
         *
         * @param e the component event.
         */
        public void componentMoved(ComponentEvent e) {

            Component source = (Component) e.getSource();
            Rectangle bounds = source.getBounds();

            /*
                  if (source instanceof DisplayNode) {
              // TODO
                      //GraphNode modelNode = (GraphNode) (displayToModel.get(source));
                      Point      p         = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
                      //modelNode.setNodeCenter(p);
                  }
             */

            Rectangle visible = getVisibleRect();
            if (!visible.contains(bounds)) {
                adjustPreferredSize();
                scrollRectToVisible(bounds);
            }
        }

        public void componentResized(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }
    }

    /**
     * Handles <code>MouseEvent</code>s and <code>MouseMotionEvent</code>s. We
     * use an inner class instead of the editor itself since we don't want to
     * expose the handler methods on the editor's public API. </p> jdramsey
     * 11/07/01: One problem with this approach is that the firePropertyChange
     * method must be exposed as public so that the mouse handler can fire
     * property change events on behalf of the Abstract Workbench. Perhaps a
     * better approach would be to add a constructor to the handler with a
     * reference to the AbstractWorkbench.
     */
    protected class MouseHandler implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent e) {
            Object source = e.getSource();

            switch (workbenchMode) {
                case SELECT_MOVE:
                case ADD_NODE:
                case ADD_EDGE:
                    if (source instanceof DisplayNode) {
                        DisplayNode node = (DisplayNode) source;

                        if (e.getClickCount() == 2) {
                            deselectAll();
                            node.doDoubleClickAction();
                        }
                        else {
                            if (!(e.isShiftDown() &&
                                    allowMultipleNodeSelection)) {
                                deselectAll();
                            }

                            node.setSelected(true);
                        }
                    }
                    else if (source instanceof IDisplayEdge) {
                        IDisplayEdge edge = (IDisplayEdge) (source);

                        if (e.getClickCount() == 2) {
                            deselectAll();
                        }
                        else {
                            if (edge.isSelected()) {
                                edge.setSelected(false);
                            }
                            else {
                                if (!(e.isShiftDown() &&
                                        allowMultipleNodeSelection)) {
                                    deselectAll();
                                }
                                edge.setSelected(true);
                            }
                        }
                    }
                    else {
                        deselectAll();
                    }

                    break;
            }
            //}
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {

            Object source = e.getSource();

            if (/*(source instanceof DisplayEdge) ||*/ !allowUserEditing) {
                return;
            }

            if (!maybeShowPopup(e)) {
                Point loc = e.getPoint();
                switch (workbenchMode) {

                    case SELECT_MOVE:
                        if (source == MultiGraphWorkbench.this) {
                            startRubberband(loc);
                        }
                        else if (source instanceof DisplayNode) {
                            startNodeDrag(loc);
                        }
                        break;

                    case ADD_NODE:
                        if (source == MultiGraphWorkbench.this) {
                            newNode();
                        }
                        break;

                    case ADD_EDGE:
                        if (source instanceof DisplayNode) {
                            Point o = ((Component) (source)).getLocation();

                            loc.translate(o.x, o.y);
                        }

                        DisplayNode nearestNode = findNearestNode(loc);

                        if (nearestNode != null) {
                            startEdge(nearestNode, loc);
                        }
                        break;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {

            Object source = e.getSource();

            if (!allowUserEditing) { // || (source instanceof DisplayEdge)) {
                return;
            }

            if (!maybeShowPopup(e)) {
                switch (workbenchMode) {

                    case SELECT_MOVE:
                        if (source == MultiGraphWorkbench.this) {
                            finishRubberband();
                        }
                        break;

                    case ADD_EDGE:
                        finishEdge();
                        break;
                }
            }

        }

        public void mouseDragged(MouseEvent e) {

            if (!allowUserEditing) {
                return;
            }

            Object source = e.getSource();
            Point newPoint = e.getPoint();

            switch (workbenchMode) {

                case SELECT_MOVE:
                    if (source instanceof DisplayNode && clickPoint != null) {
                        DisplayNode node = (DisplayNode) source;
                        Point point = new Point(node.getLocation());

                        point.x += newPoint.x - clickPoint.x;
                        point.y += newPoint.y - clickPoint.y;

                        if (point.x < 0) {
                            point.x = 0;
                        }

                        if (point.y < 0) {
                            point.y = 0;
                        }

                        node.setLocation(point);
                    }
                    else if (rubberband != null) {
                        rubberband.updateTrackPoint(newPoint);
                        selectAllInRubberband(rubberband);
                    }
                    break;

                case ADD_EDGE:
                    if (source instanceof DisplayNode) {
                        Point o = ((Component) source).getLocation();

                        newPoint.translate(o.x, o.y);
                    }

                    if (trackedEdge != null) {
                        Rectangle bounds = trackedEdge.getBounds();
                        Rectangle visible = getVisibleRect();
                        if (!visible.contains(bounds)) {
                            //adjustPreferredSize();
                            scrollRectToVisible(bounds);
                        }

                        trackedEdge.updateTrackPoint(newPoint);
                    }
                    break;
            }
        }

        public void mouseMoved(MouseEvent e) {
        }

        // checks if a popup should be shown. Before the popup is shown, will adjust
        // the selection- nothing is ever deselected, but if an item that wasn't
        // selected was clicked on, it will select that item (and deselect others if
        // shift was not down)
        private boolean maybeShowPopup(MouseEvent e) {
            Object source = e.getSource();
            if (e.isPopupTrigger()) {
                if (source instanceof DisplayNode) {
                    // tune selection
                    if (!((DisplayNode) source).isSelected()) {
                        if (!(e.isShiftDown() && allowMultipleNodeSelection)) {
                            deselectAll();
                        }
                        ((DisplayNode) source).setSelected(true);
                    }

                    // show popup
                    nodePopup.show(e.getComponent(), e.getX(), e.getY());
                    return true;
                }
                else if (source instanceof MultiGraphEdge) {
                    // tune selection
                    if (!((IDisplayEdge) source).isSelected()) {
                        if (!(e.isShiftDown() && allowMultipleNodeSelection)) {
                            deselectAll();
                        }
                        ((IDisplayEdge) source).setSelected(true);
                    }

                    // show popup
                    edgePopup.show(e.getComponent(), e.getX(), e.getY());
                    return true;
                }
            }

            return false;
        }

    }

    /**
     * Handles <code>PropertyChangeEvent</code>s.
     */
    protected class PropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {

            String propName = e.getPropertyName();
            Object old = e.getOldValue();
            Object _new = e.getNewValue();

            if (propName.equals("nodeAdded")) {
                TimesteppedFactor newNode =
                        addNode((String) _new);    // checks if node already added.
                newNode.synchDisplayEdges();
                Dimension group_size = curTimeGroup.getPreferredSize();
                curTimeGroup.setBounds(10, vgap * (MAX_TIMELAG) - 10,
                        group_size.width, group_size.height);
                repaint();
                revalidate();
                adjustPreferredSize();
            }
            else if (propName.equals("nodeRemoved")) {
                removeNode((String) old);
            }
            else if (propName.equals("edgeAdded")) {
                LaggedEdge le = (LaggedEdge) _new;
                getDisplayFactor(le.getFactor()).synchDisplayEdges();
                adjustTimestepsShown();
            }
            else if (propName.equals("edgeRemoved")) {
                LaggedEdge le = (LaggedEdge) old;
                getDisplayFactor(le.getFactor()).synchDisplayEdges();
            }
            else if (propName.equals("edgeLaunch")) {
                System.out.println("Attempt to launch edge.");
            }
            else if (propName.equals("maxLagAllowable")) {
                int maxLag = getModelGraph().getMaxLagAllowable();
                setPastSteps(MAX_TIMELAG + maxLag);
                setFutureSteps(MAX_TIMELAG + maxLag);
            }
            else if (propName.equals("factorRenamed")) {
                getDisplayFactor((String) old).setName((String) _new);
                displayFactors.put(_new, displayFactors.remove(old));
                repaint();
            }
        }
    }

    class PopupListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());
            if (source.getText().equals(POPUP_DELETE)) {
                promptDeleteSelectedObjects();
            }
            else if (source.getText().equals(POPUP_RENAME)) {
                JPopupMenu parent = (JPopupMenu) source.getParent();
                if (parent == nodePopup) {
                    ((DisplayNode) parent.getInvoker()).doDoubleClickAction();
                }
            }
        }

    }

}


