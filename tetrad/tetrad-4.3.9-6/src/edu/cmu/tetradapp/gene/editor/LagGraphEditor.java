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
import edu.cmu.tetrad.gene.graph.ManualActiveLagGraph;
import edu.cmu.tetradapp.model.RandomActiveLagGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Displays a editor editing editor area together with a toolbench for editing
 * tetrad-style graphs.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey
 * @author Ethan Tira-Thompson
 */
public final class LagGraphEditor extends JPanel
        implements PropertyChangeListener, ActionListener {

    /**
     * The editor model being edited.
     */
    private ActiveLagGraph graphModel;

    /**
     * The current editor shown in the editor... when a view switches, we
     * replace this.
     */
    private LagGraphWorkbench curBench;

    private DirectedGraphWorkbench directBench;
    private MultiGraphWorkbench multiBench;
    private GraphTableWorkbench tableBench;

    /**
     * The pane which holds the current editor scroller - used to switch views.
     */
    private JPanel benchPane;

    /**
     * The pane which holds the current editor - used to switch views.
     */
    private JScrollPane workbenchScroll;

    /**
     * The pane which holds the current side (left) toolbar.
     */
    private JPanel toolbar;

    /**
     * Holds the scroll adjustment handler for switching time modes in the
     * repeating editor mode.
     */
    private final MultiGraphScrollHandler scrollHandler =
            new MultiGraphScrollHandler();

    //========================CONSTRUCTORS===========================//

    public LagGraphEditor(ManualActiveLagGraph lagGraph) {
        initialize(lagGraph);
    }

    public LagGraphEditor(RandomActiveLagGraph lagGraph) {
        initialize(lagGraph);
    }

    //=======================PUBLIC METHODS===========================//

    /**
     * Called by top toolbar when the user switches to Directed view
     */
    public void switchDirected() {
        if (toolbar != null) {
            remove(toolbar);
        }

        if (workbenchScroll != null) {
            benchPane.remove(workbenchScroll);
        }

        if (directBench == null) {
            directBench = new DirectedGraphWorkbench(graphModel);
        }

        curBench = directBench;
        workbenchScroll = new JScrollPane((JComponent) curBench);
        toolbar = new DirectedGraphToolbar(directBench);
        add(toolbar, BorderLayout.WEST);
        benchPane.add(workbenchScroll, BorderLayout.CENTER);
        invalidate();
        revalidate();
    }

    /**
     * Called by top toolbar when the user switches to Repeating view
     */
    public void switchRepeating() {
        if (toolbar != null) {
            remove(toolbar);
        }

        if (workbenchScroll != null) {
            benchPane.remove(workbenchScroll);
        }

        if (multiBench == null) {
            multiBench = new MultiGraphWorkbench(graphModel);
        }

        curBench = multiBench;
        workbenchScroll = new JScrollPane(multiBench);
        workbenchScroll.getVerticalScrollBar().addAdjustmentListener(
                scrollHandler);
        scrollHandler.setWorkbench(multiBench);
        toolbar = new MultiGraphToolbar(multiBench);
        add(toolbar, BorderLayout.WEST);
        benchPane.add(workbenchScroll, BorderLayout.CENTER);

        revalidate();
        multiBench.scrollToHome();

    }

    /**
     * Called by top toolbar when user switches to tabular view
     */
    public void switchTabular() {
        if (toolbar != null) {
            remove(toolbar);
        }

        if (workbenchScroll != null) {
            benchPane.remove(workbenchScroll);
        }

        if (tableBench == null) {
            tableBench = new GraphTableWorkbench(graphModel);
        }

        curBench = tableBench;
        workbenchScroll = new JScrollPane((JComponent) curBench);
        toolbar = new TableToolbar(tableBench);
        add(toolbar, BorderLayout.WEST);
        benchPane.add(workbenchScroll, BorderLayout.CENTER);
        invalidate();
        revalidate();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if ("graph".equals(e.getPropertyName())) {
            this.graphModel = (ActiveLagGraph) (e.getNewValue());
            firePropertyChange("graph", e.getOldValue(), e.getNewValue());
        }
    }

    public void actionPerformed(ActionEvent e) {
        String text = ((AbstractButton) e.getSource()).getText();

        if (text.equals("Lag Graph")) {
            MultiGraphWorkbench multiGraphWorkbench =
                    ((MultiGraphWorkbench) curBench);
            multiGraphWorkbench.setEdgeDisplayMode(
                    MultiGraphWorkbench.EDGE_LAG);
        }
        else if (text.equals("Hybrid Graph")) {
            MultiGraphWorkbench multiGraphWorkbench =
                    ((MultiGraphWorkbench) curBench);
            multiGraphWorkbench.setEdgeDisplayMode(
                    MultiGraphWorkbench.EDGE_HYBRID);
        }
        else if (text.equals("Repeating Graph")) {
            MultiGraphWorkbench multiGraphWorkbench =
                    ((MultiGraphWorkbench) curBench);
            multiGraphWorkbench.setEdgeDisplayMode(
                    MultiGraphWorkbench.EDGE_REPEATING);
        }
        else if (text.equals("Multple Arrowheads")) {
            DirectedGraphWorkbench directedGraphWorkbench =
                    ((DirectedGraphWorkbench) curBench);
            boolean selected = ((JCheckBox) e.getSource()).isSelected();
            directedGraphWorkbench.setShowEdgeMulti(selected);
        }
        else if (text.equals("Numerical Label")) {
            DirectedGraphWorkbench directedGraphWorkbench =
                    ((DirectedGraphWorkbench) curBench);
            boolean selected = ((JCheckBox) e.getSource()).isSelected();
            directedGraphWorkbench.setShowEdgeLabels(selected);
        }
    }

    //=======================PRIVATE METHODS===========================//

    /**
     * Constructs a new LagGraphMultiTetradEditor.
     *
     * @param lagGraph The Tetrad editor model to be edited.
     */
    private final void initialize(ActiveLagGraph lagGraph) {
        setLayout(new BorderLayout());

        this.graphModel = lagGraph;
        this.benchPane = new JPanel(new BorderLayout());

        LagGraphTopToolbar tools = new LagGraphTopToolbar(this);
        this.benchPane.add(tools, BorderLayout.NORTH);
        add(this.benchPane, BorderLayout.CENTER);

        LagGraphTetradMenu menu = new LagGraphTetradMenu(this);
        add(menu, BorderLayout.NORTH);

        tools.doSwitchDirected();
    }
}


