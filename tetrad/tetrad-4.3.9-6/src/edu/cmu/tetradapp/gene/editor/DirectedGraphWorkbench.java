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
import edu.cmu.tetrad.util.PointXy;
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
 * Extends AbstractWorkbench in the ways needed to display Tetrad graphs.
 *
 * @author Ethan Tira-Thompson
 * @author Joseph Ramsey
 * @author Willie Wheeler
 */
public final class DirectedGraphWorkbench extends JComponent
        implements LagGraphWorkbench {

    //===================PUBLIC STATIC FINAL FIELDS=============

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    private static final int SELECT_MOVE = 0;

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    private static final int ADD_NODE = 1;

    /**
     * The mode in which the user is permitted to select editor items or move
     * nodes.
     */
    private static final int ADD_EDGE = 2;

    /**
     * The base string that is used for factor names. Actual factors will have
     * this base string + a numerical identifier tacked on the end
     */
    private static final String BASE_FACTOR_NAME = "Gene";

    //=====================PRIVATE FIELDS=====================

    /**
     * The editor which this editor displays.
     */
    private ActiveLagGraph modelGraph;

    /**
     * The map from model elements to display elements.
     */
    private Map modelToDisplay;

    /**
     * True if current edge display state is showing labels.
     */
    private boolean showEdgeLabels;

    /**
     * True if current edge display state is showing multiple arrowheads.
     */
    private boolean showEdgeMulti;

    /**
     * Holds the location of the last mouse event, so when a node is added we
     * know where to place it on screen.
     */
    private Point lastMouseLoc;

    private boolean lastClickWasAdd;

    private JPopupMenu nodePopup, edgePopup;
    private static final String POPUP_DELETE = "Delete selection";
    private static final String POPUP_RENAME = "Rename factor";
    private static final String POPUP_EDIT = "Edit Lag";

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
    private LabeledGraphEdge ghostEdge;

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
    private final ComponentHandler compHandler = new ComponentHandler();

    /**
     * Handler for <code>MouseEvent</code>s and <code>MouseMotionEvent</code>s.
     */
    private MouseHandler mouseHandler = new MouseHandler();

    /**
     * Handler for <code>PropertyChangeEvent</code>s.
     */
    private PropertyChangeHandler propChangeHandler =
            new PropertyChangeHandler();

    /**
     * Constructor declaration
     */
    public DirectedGraphWorkbench(ActiveLagGraph graphModel) {
        if (graphModel == null) {
            throw new NullPointerException("Graph model must not be null.");
        }

        setModelGraph(graphModel);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        setCursor(null);
        setShowEdgeLabels(true);

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
        menuItem = new JMenuItem(POPUP_EDIT);
        menuItem.addActionListener(popupListener);
        edgePopup.add(menuItem);

        menuItem = new JMenuItem(POPUP_DELETE);
        menuItem.addActionListener(popupListener);
        menuItem.setAccelerator(delete);
        edgePopup.add(menuItem);
    }

    /* ============================================================*/
    /* ======================== NODE STUFF ========================*/
    /* ============================================================*/


    /**
     * Adds a session node to the editor centered at the specified location; the
     * type of node added is determined by the mode of the editor.
     *
     * @throws java.lang.IllegalArgumentException
     *
     */
    private void addModelNode(String name) throws IllegalArgumentException {

        getModelGraph().addFactor(name);
        LaggedFactor toSelf = new LaggedFactor(name, 1);
        getModelGraph().addEdge(name, toSelf);
        adjustPreferredSize();

        // if this editor is sitting inside of a scrollpane,
        // let the scrollpane know how big it is.
        scrollRectToVisible(getVisibleRect());

        //		addDisplayNode(loc,name);
    }

    /**
     * Adds the given model node to the model and adds a corresponding display
     * node to the display.
     */
    private void addDisplayNode(Point loc, String name) {

        // construct a display node for the model node.
        DisplayNode displayNode = new DirectedGraphNode(name);

        // link the display node to the model node.
        modelToDisplay.put(name, displayNode);

        // set the bounds of the display node.
        Dimension dim = displayNode.getPreferredSize();

        displayNode.setSize(dim);
        displayNode.setLocation(loc.x - dim.width / 2, loc.y - dim.height / 2);
        add(displayNode, 0);

        // Add edges for each of the edges in the model.
        SortedSet laggedFactors = getModelGraph().getParents(name);
        for (Iterator edge_it = laggedFactors.iterator(); edge_it.hasNext();) {
            LaggedFactor f = (LaggedFactor) edge_it.next();
            if (modelToDisplay.get(f.getFactor()) != null) {
                addDisplayEdge(f.getFactor(), name, f.getLag());
            }
        }

        // there are better ways to do this if we modify the editor to store
        // reverse links
        for (Iterator it =
                getModelGraph().getFactors().iterator(); it.hasNext();) {
            String factor = (String) it.next();
            if (!factor.equals(name)) {
                for (Iterator edge_it = getModelGraph().getParents(factor)
                        .iterator(); edge_it.hasNext();) {
                    LaggedFactor e = (LaggedFactor) edge_it.next();
                    if (e.getFactor().equals(name) &&
                            modelToDisplay.get(factor) != null) {
                        addDisplayEdge(name, factor, e.getLag());
                    }
                }
            }
        }

        // Add listeners.
        displayNode.addComponentListener(compHandler);
        displayNode.addMouseListener(mouseHandler);
        displayNode.addMouseMotionListener(mouseHandler);
        repaint();
        validate();

        // Fire notification event. jdramsey 12/11/01
        firePropertyChange("nodeAdded", null, displayNode);

        return;
    }

    /**
     * removes a node from the display.  Also removes all associated edges.
     *
     * @param name the name of the factor this node represented
     */
    private void removeDisplayNode(String name) {
        DisplayNode displayNode = (DisplayNode) modelToDisplay.get(name);
        remove(displayNode);
        modelToDisplay.remove(displayNode.getName());

        // need to remove all the edges that point to or from this node as well
        Set keys = modelToDisplay.keySet();
        List removals = new LinkedList();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String s = (String) it.next();
            if (s.startsWith(displayNode.getName() + ":") ||
                    s.indexOf(":" + displayNode.getName() + ":") != -1) {
                remove((Component) modelToDisplay.get(s));
                removals.add(s);
            }
        }
        for (Iterator it = removals.iterator(); it.hasNext();) {
            removeDisplayEdge((String) it.next());
        }

        repaint();
    }

    /**
     * removes a node from the underlying model. Relies on the model to sent a
     * PropertyChangeEvent back.
     *
     * @param factor the name of the factor to remove
     */
    private void removeModelNode(String factor) {
        getModelGraph().removeFactor(factor);
    }

    /* ============================================================*/
    /* ======================== EDGE STUFF ========================*/
    /* ============================================================*/


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
            remove(ghostEdge);
            trackedEdge = null;
            ghostEdge = null;
            repaint();
        }

        trackedEdge = new DisplayEdge(node, mouseLoc, DisplayEdge.DIRECTED);
        ghostEdge = new LabeledGraphEdge(node, findNearestNode(mouseLoc), null);
        ghostEdge.setAlpha(100);

        add(trackedEdge, -1);
        add(ghostEdge, -1);
        deselectAll();
    }

    /**
     * Finishes the drawing of a new edge.
     *
     * @see #startEdge
     */
    private void finishEdge() {
        // retrieve the two display components this edge should connect.
        if (trackedEdge == null) {
            return;
        }
        DisplayNode comp1 = trackedEdge.getComp1();
        Point p = trackedEdge.getTrackPoint();
        DisplayNode comp2 = findNearestNode(p);

        // construct the model edge
        String node1 = ((Component)comp1).getName();
        String node2 = ((Component) comp2).getName();

        String lagString = JOptionPane.showInputDialog("Time lag of edge:");
        if (lagString != null) {
            try {
                int lag = Integer.parseInt(lagString);
                if (lag > 0) {
                    addModelEdge(node1, node2, lag);
                }
            }
            catch (java.lang.NumberFormatException e) {
            }
        }

        remove(trackedEdge);
        remove(ghostEdge);
        trackedEdge = null;
        ghostEdge = null;
    }

    /**
     * adds an edge to the model
     */
    private void addModelEdge(String src, String dst, int lag) {
        getModelGraph().addEdge(dst, new LaggedFactor(src, lag));
    }

    /**
     * Calculates the offset in pixels of a given edge - this could use a little
     * tweaking still
     *
     * @param i the index of the given edge
     * @param n the number of edges
     * @param w the total width to spread edges over
     */
    private static double calcEdgeOffset(double i, double n, double w) {
        return w * (2 * i + 1 - n) / 2 / n;
    }

    /**
     * Scans through all edges between two nodes, resets those edge's offset
     * values. Note that these offsets are stored in the edges themselves so
     * this does not have to be recomputed all the time
     *
     * @param node1 the source node
     * @param node2 the destination node
     */
    private void resetEdgeOffsets(String node1, String node2) {
        int parallel = 0;
        for (Iterator it =
                getModelGraph().getParents(node2).iterator(); it.hasNext();) {
            if (((LaggedFactor) it.next()).getFactor().equals(node1)) {
                parallel++;
            }
        }
        for (Iterator it =
                getModelGraph().getParents(node1).iterator(); it.hasNext();) {
            if (((LaggedFactor) it.next()).getFactor().equals(node2)) {
                parallel++;
            }
        }
        int i = 1;
        for (Iterator it =
                getModelGraph().getParents(node2).iterator(); it.hasNext();) {
            LaggedFactor f = (LaggedFactor) it.next();
            if (f.getFactor().equals(node1)) {
                LabeledGraphEdge e = (LabeledGraphEdge) modelToDisplay.get(
                        node1 + ":" + node2 + ":" + f.getLag());
                if (e != null) {
                    e.setOffset(calcEdgeOffset(i, parallel + 1, 40));
                }
                i++;
            }
        }
        for (Iterator it =
                getModelGraph().getParents(node1).iterator(); it.hasNext();) {
            LaggedFactor f = (LaggedFactor) it.next();
            if (f.getFactor().equals(node2)) {
                LabeledGraphEdge e = (LabeledGraphEdge) modelToDisplay.get(
                        node2 + ":" + node1 + ":" + f.getLag());
                if (e != null) {
                    e.setOffset(calcEdgeOffset(i, parallel + 1, -40));
                }
                i++;
            }
        }
    }

    /**
     * add a DisplayEdge directly to the display, adding all necessary
     * connections to the workspace
     */
    private void addDisplayEdge(String node1, String node2, int lag) {
        DisplayNode src = (DisplayNode) modelToDisplay.get(node1);
        DisplayNode dst = (DisplayNode) modelToDisplay.get(node2);
        LabeledGraphEdge displayEdge =
                new LabeledGraphEdge(src, dst, String.valueOf(lag));
        displayEdge.setShowLabel(showEdgeLabels);
        displayEdge.setShowMulti(showEdgeMulti);
        // add the display edge to the editor.
        add(displayEdge, -1);                         // add to back

        // Add listeners.
        displayEdge.addComponentListener(compHandler);
        displayEdge.addMouseListener(mouseHandler);
        displayEdge.addMouseMotionListener(mouseHandler);
        displayEdge.addPropertyChangeListener(propChangeHandler);
        modelToDisplay.put(node1 + ":" + node2 + ":" + lag, displayEdge);
        resetEdgeOffsets(node1, node2);
    }

    /**
     * removes an edge from the model
     */
    private void removeModelEdge(String src, String dst, int lag) {
        getModelGraph().removeEdge(dst, new LaggedFactor(src, lag));
    }

    /**
     * removes an edge from the display
     */
    private void removeDisplayEdge(String src, String dst, int lag) {
        removeDisplayEdge(src + ":" + dst + ":" + lag);
    }

    /**
     * removes an edge from the display
     *
     * @param name the name of the edge - "srcname:dstname:lag"
     */
    private void removeDisplayEdge(String name) {
        DisplayEdge displayEdge = (DisplayEdge) (modelToDisplay.get(name));
        modelToDisplay.remove(name);
        remove(displayEdge);
        repaint();
    }

    /**
     * Sets whether to show the labels on the edges
     */
    public void setShowEdgeLabels(boolean showEdgeLabels) {
        this.showEdgeLabels = showEdgeLabels;
        Component[] components = getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];
            if (comp instanceof LabeledGraphEdge) {
                ((LabeledGraphEdge) comp).setShowLabel(showEdgeLabels);
            }
        }
    }

    /**
     * Sets whether to show the multiple arrowheads on the edges
     */
    public void setShowEdgeMulti(boolean showEdgeMulti) {
        this.showEdgeMulti = showEdgeMulti;
        Component[] components = getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];
            if (comp instanceof LabeledGraphEdge) {
                ((LabeledGraphEdge) comp).setShowMulti(showEdgeMulti);
            }
        }
    }

    /* ============================================================*/
    /* ======================== OTHER STUFF =======================*/
    /* ============================================================*/

    /**
     * Given base <b> (a String), returns the first node in the sequence "<b>1",
     * "<b>2", "<b>3", etc., which is not already the name of a node in the
     * editor.
     *
     * @param base the base string.
     * @return the first string in the sequence not already being used.
     */
    private String nextVariableName(String base) {

        // AbstractVariable names should start with "1."
        int i = 0;
        String name;

        loop:
        //ewww
        while (true) {
            i++;

            name = base + i;

            for (Iterator it =
                    modelGraph.getFactors().iterator(); it.hasNext();) {
                String fname = (String) (it.next());

                if (fname.equals(name)) {
                    continue loop;
                }
            }

            break;
        }

        return base + i;
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
        Rectangle r = new Rectangle(0, 0, 400, 400);

        for (int i = 0; i < components.length; i++) {
            r = r.union(components[i].getBounds());
        }

        setPreferredSize(new Dimension(r.width, r.height));
    }

    /**
     * Prompts whether or not to delete the selection, and then deletes it if
     * the user says "OK"
     */
    private void promptDeleteSelectedObjects() {
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
    private void deleteSelectedObjects() {

        Component[] components = getComponents();

        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];

            if (comp instanceof IDisplayEdge) {
                if (((IDisplayEdge) comp).isSelected()) {
                    DisplayNode node1 = ((IDisplayEdge) comp).getNode1();
                    String src = ((Component)node1).getName();
                    DisplayNode node2 = ((IDisplayEdge) comp).getNode2();
                    String dst = ((Component) node2).getName();
                    int lag = ((LabeledGraphEdge) comp).getWeight();
                    removeModelEdge(src, dst, lag);
                }
            }
            else if (comp instanceof DisplayNode) {
                if (((DisplayNode) comp).isSelected()) {
                    removeModelNode(comp.getName());
                }
            }
        }

        repaint();
    }

    /**
     * Deselects all edges and nodes in the editor.
     */
    private void deselectAll() {

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
    private static double distance(Point p1, Point p2) {

        double d;

        d = (p1.x - p2.x) * (p1.x - p2.x);
        d += (p1.y - p2.y) * (p1.y - p2.y);
        d = Math.sqrt(d);

        return d;
    }

    /**
     * Finds the nearest node to a given point.	 More specifically, finds the
     * node whose center point is nearest to the given point.	 (If more than one
     * such node exists, the one with lowest z-order is returned.)
     *
     * @param p the point for which the nearest node is requested.
     * @return the nearest node to point p.
     */
    private DisplayNode findNearestNode(Point p) {

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
     * Fires a property change event, property name = "selectedNodes", with the
     * new node selection as its new value (a List).
     */
    private void fireNodeSelection() {

        //        Component[] components = getComponents();
        List selection = new ArrayList();

        //        for (int i = 0; i < components.length; i++) {
        //            if (components[i] instanceof DisplayNode) {
        //                DisplayNode displayNode = (DisplayNode) components[i];
        //            }
        //        }

        if (allowMultipleNodeSelection) {
            firePropertyChange("selectedNodes", null, selection);
        }
        else {
            if (selection.size() == 1) {
                firePropertyChange("selectedNode", null, selection.get(0));
            }
            else {
                throw new IllegalStateException(
                        "Multiple or null selection detected " +
                                "when single selection mode is set.");
            }
        }
    }

    /**
     * Making this method public so that inner classes can fire property
     * changes. (It's protected in JComponent.)
     */
    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Returns the Graph this editor displays.
     *
     * @return this obect.
     */
    public ActiveLagGraph getModelGraph() {
        return modelGraph;
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
    private void registerKeys() {

        Action deleteAction = new AbstractAction() {

            /**
             * Deletes selected objects.
             */
            public void actionPerformed(ActionEvent e) {
                deleteSelectedObjects();
            }
        };

        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(backspace, "DELETE");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(delete, "DELETE");
        getActionMap().put("DELETE", deleteAction);

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
     * Sets the display editor model to the indicated model.		 (Called when the
     * editor is first constructed as well as whenever the editor model is
     * changed.)
     */
    public void setModelGraph(ActiveLagGraph modelGraph) {
        if (modelGraph == null) {
            throw new NullPointerException();
        }

        unregisterKeys();
        removeCurrentListeners();

        // This has to occur before the addDisplayNode line below.
        this.modelGraph = modelGraph;
        this.modelToDisplay = new HashMap();

        if (!nodesArrangedAlready()) {
            arrangeNodesInCircle();
        }

        SortedSet factors = getModelGraph().getFactors();
        for (Iterator it = factors.iterator(); it.hasNext();) {
            String factor = (String) it.next();
            PointXy point =
                    getModelGraph().getLocation(factor);
            addDisplayNode(new java.awt.Point(point.getX(), point.getY()),
                    factor);
        }

        adjustPreferredSize();
        getModelGraph().addPropertyChangeListener(propChangeHandler);
        registerKeys();
    }

    /**
     * Return true just in all of the nodes in the model are already assigned
     * positions.
     */
    private boolean nodesArrangedAlready() {
        SortedSet factors = getModelGraph().getFactors();

        for (Iterator it = factors.iterator(); it.hasNext();) {
            String factor = (String) it.next();

            if (getModelGraph().getLocation(factor) == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * The default arrangement is to arrange all of the nodes in a circle.
     */
    private void arrangeNodesInCircle() {
        SortedSet factors = getModelGraph().getFactors();
        double radius = 150 + 20 * (factors.size() - 5);
        int offset = (int) radius + 80;
        double dr = 2 * Math.PI / factors.size();
        double a = 0;
        for (Iterator it = factors.iterator(); it.hasNext();) {
            String factor = (String) it.next();
            int x = (int) (radius * Math.sin(a));
            int y = -(int) (radius * Math.cos(a));
            a += dr;
            int offsetX = offset + x;
            int offsetY = offset + y;

            PointXy point =
                    new PointXy(offsetX, offsetY);
            getModelGraph().setLocation(factor, point);
        }
    }

    private void removeCurrentListeners() {
        // Remove listeners from the current model.
        if (getModelGraph() != null) {
            getModelGraph().removePropertyChangeListener(propChangeHandler);
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
    private void unregisterKeys() {
        unregisterKeyboardAction(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    public void cleanup() {
        arrangeNodesInCircle();
        SortedSet factors = getModelGraph().getFactors();
        for (Iterator it = factors.iterator(); it.hasNext();) {
            String factor = (String) it.next();
            DisplayNode displayNode = (DisplayNode) modelToDisplay.get(factor);
            Dimension dim = displayNode.getPreferredSize();

            PointXy _point =
                    getModelGraph().getLocation(factor);
            Point point = new Point(_point.getX() - dim.width / 2,
                    _point.getY() - dim.height / 2);

            displayNode.setLocation(point);
        }
    }

    //
    // Event handler classes
    //

    /**
     * Handles <code>ComponentEvent</code>s.	We use an inner class instead of
     * the editor itself since we don't want to expose the handler methods on
     * the editor's public API.
     */
    final class ComponentHandler extends ComponentAdapter {

        /**
         * Adjusts scrollbars to automatically reflect the position of a
         * component which is being dragged.
         *
         * @param e the component event.
         */
        public void componentMoved(ComponentEvent e) {
            Component source = (Component) e.getSource();
            Rectangle bounds = source.getBounds();
            adjustPreferredSize();
            scrollRectToVisible(bounds);
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
    final class MouseHandler implements MouseListener, MouseMotionListener {

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
                            fireNodeSelection();
                        }
                    }
                    else {

                        // Clicking on anything other than a node
                        // deselects everything.	[WLW 02/19/01]
                        deselectAll();

                        if (source instanceof IDisplayEdge) {
                            IDisplayEdge edge = (IDisplayEdge) (source);

                            if (e.getClickCount() == 2) {
                                deselectAll();
                                edge.launchAssociatedEditor();
                                DirectedGraphWorkbench.this.firePropertyChange(
                                        "edgeLaunch", edge, edge);
                            }
                            else {
                                if (edge.isSelected()) {
                                    edge.setSelected(false);
                                }
                                else if (e.isShiftDown()) {
                                    edge.setSelected(true);
                                }
                                else {
                                    deselectAll();
                                    edge.setSelected(true);
                                }
                            }
                        }
                    }

                    break;
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            lastMouseLoc = e.getPoint();

            Object source = e.getSource();

            if (!allowUserEditing) {
                return;
            }

            if (maybeShowPopup(e)) {
                return;
            }

            Point loc = e.getPoint();

            switch (workbenchMode) {

                case SELECT_MOVE:
                    if (source == DirectedGraphWorkbench.this) {
                        startRubberband(loc);
                    }
                    else if (source instanceof DisplayNode) {
                        startNodeDrag(loc);
                    }
                    break;

                case ADD_NODE:
                    if (source == DirectedGraphWorkbench.this) {
                        lastClickWasAdd = true;
                        addModelNode(nextVariableName(BASE_FACTOR_NAME));
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

        public void mouseReleased(MouseEvent e) {
            Object source = e.getSource();

            if (!allowUserEditing) {
                return;
            }

            if (maybeShowPopup(e)) {
                return;
            }

            switch (workbenchMode) {

                case SELECT_MOVE:
                    if (source == DirectedGraphWorkbench.this) {
                        finishRubberband();
                    }
                    break;

                case ADD_EDGE:
                    finishEdge();
                    break;
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
                        setLocationInModel(point, node);
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
                        trackedEdge.updateTrackPoint(newPoint);
                        remove(ghostEdge);
                        Rectangle r = ghostEdge.getBounds();
                        ghostEdge = new LabeledGraphEdge(trackedEdge.getComp1(),
                                findNearestNode(newPoint), null);
                        ghostEdge.setAlpha(100);
                        add(ghostEdge, -1);
                        r.union(ghostEdge.getBounds());
                        repaint(r.x, r.y, r.width, r.height);
                    }
                    break;
            }
        }

        private void setLocationInModel(Point point, DisplayNode node) {
            ActiveLagGraph graph = DirectedGraphWorkbench.this.getModelGraph();
            PointXy _point =
                    new PointXy(point.x, point.y);
            graph.setLocation(node.getName(), _point);
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
                else if (source instanceof LabeledGraphEdge) {
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
    final class PropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {

            String propName = e.getPropertyName();
            Object old = e.getOldValue();
            Object _new = e.getNewValue();

            if (propName.equals("nodeAdded")) {
                Point loc = lastMouseLoc;
                if (!lastClickWasAdd) //This point picker could certainly stand to be more intelligent...
                {
                    loc = new Point(
                            (int) (Math.random() * (getWidth() - 100)) + 50,
                            (int) (Math.random() * (getHeight() - 100)) + 50);
                }
                addDisplayNode(loc, (String) _new);
                lastClickWasAdd = false;
            }
            else if (propName.equals("nodeRemoved")) {
                removeDisplayNode((String) old);
            }
            else if (propName.equals("edgeAdded")) {
                LaggedEdge le = (LaggedEdge) _new;
                LaggedFactor lf = le.getLaggedFactor();
                String src = lf.getFactor();
                String dst = le.getFactor();
                int lag = lf.getLag();
                addDisplayEdge(src, dst, lag);
            }
            else if (propName.equals("edgeRemoved")) {
                LaggedEdge le = (LaggedEdge) old;
                LaggedFactor lf = le.getLaggedFactor();
                String src = lf.getFactor();
                String dst = le.getFactor();
                int lag = lf.getLag();
                removeDisplayEdge(src, dst, lag);
            }
            else if (propName.equals("edgeLaunch")) {
                String s1 = ((DisplayEdge) _new).getComp1().getName();
                String s2 = ((DisplayEdge) _new).getComp2().getName();
                int lag = Integer.parseInt((String) old);
                //System.out.println("src="+s1+","+lag+",dst="+s2);
                SortedSet laggedFactors = modelGraph.getParents(s2);
                for (Iterator edge_it =
                        laggedFactors.iterator(); edge_it.hasNext();) {
                    LaggedFactor f = (LaggedFactor) edge_it.next();
                    //System.out.println(f.getFactor()+" "+f.getLag());
                    if (f.getFactor().equals(s1) && f.getLag() == lag) {
                        modelGraph.removeEdge(s2, f);
                        modelGraph.addEdge(s2, new LaggedFactor(f.getFactor(),
                                ((LabeledGraphEdge) _new).getWeight()));
                        break;
                    }
                }
            }
            else if (propName.equals("factorRenamed")) {
                String oldname = (String) old;
                String newname = (String) _new;
                DisplayNode oldnode = (DisplayNode) modelToDisplay.get(oldname);
                removeDisplayNode(oldname);
                addDisplayNode(oldnode.getCenterPoint(), newname);
            }
        }
    }

    final class PopupListener implements ActionListener {
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
            else if (source.getText().equals(POPUP_EDIT)) {
                JPopupMenu parent = (JPopupMenu) source.getParent();
                if (parent == edgePopup) {
                    ((LabeledGraphEdge) parent.getInvoker()).launchAssociatedEditor();
                }
            }
        }
    }
}



