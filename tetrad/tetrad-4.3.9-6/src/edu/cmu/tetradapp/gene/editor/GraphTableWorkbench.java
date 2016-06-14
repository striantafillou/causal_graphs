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

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * @author Gregory Li
 * @author Joseph Ramsey
 * @author Aaron Powell
 * @author Willie Wheeler
 */
public class GraphTableWorkbench extends JTable implements LagGraphWorkbench {

    //===================PUBLIC STATIC FINAL FIELDS=============

    /**
     * The base string that is used for factor names. Actual factors will have
     * this base string + a numerical identifier tacked on the end
     */
    public static final String BASE_FACTOR_NAME = "Gene";

    //=====================PROTECTED FIELDS=====================

    //protected ActiveLagGraph modelGraph;
    protected JTable table;
    //=====================PRIVATE FIELDS========================

    /** The current background color of the editor. */
    //private final Color BACKGROUND = Color.white;

    /** Handler for <code>ComponentEvent</code>s. */
    //private ComponentHandler compHandler = new ComponentHandler();

    /** Handler for <code>MouseEvent</code>s and
     * <code>MouseMotionEvent</code>s. */
    //private MouseHandler mouseHandler = new MouseHandler();

    /** Handler for <code>PropertyChangeEvent</code>s. */
    //private PropertyChangeHandler propChangeHandler = new PropertyChangeHandler();

    /**
     * dictates where the next factor should be placed
     */
    //private int next_factor_cursor = 0;
    private boolean allowUserEditing;

    /**
     * Constructor declaration
     */
    public GraphTableWorkbench(ActiveLagGraph lagGraph) {
        super();
        /*
    GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		setLayout(layout);

		JLabel destLabel = new JLabel("Destination factors (affected by source factors)");
		c.gridy = 0;
		c.gridx = 2;
		layout.setConstraints(destLabel, c);
		add(destLabel);

		JLabel srcLabel = new JLabel("Source factors");
		c.gridy = 1;
		c.gridx = 0;
		layout.setConstraints(srcLabel, c);
		add(srcLabel);

		table = new JTable();
		c.gridy = 1;
		c.gridx = 1;
		c.gridwidth = 2;
		layout.setConstraints(table, c);
		add(table);
		 */
        table = this;
        table.setModel(new LagGraphTableModel(lagGraph));
        //setModelGraph(lagGraph);

    }

    public ActiveLagGraph getModelGraph() {
        return ((LagGraphTableModel) table.getModel()).getModelGraph();
    }

    public void setModelGraph(
            ActiveLagGraph modelGraph) { // throws IllegalStateException, IllegalAccessException {
        ((LagGraphTableModel) table.getModel()).setModelGraph(modelGraph);
        /*
            // Get a reference to the factors in the editor.
            this.modelGraph = modelGraph;
            SortedSet factors = getModelGraph().getFactors();

            // Find the longest name of all the factors and adjust the hgap.
            int maxLag = getModelGraph().getMaxLagAllowable();
            past_steps_shown = maxLag;
        future_steps_shown = maxLag;

            //hgap = 60 + longest * 7;

            // Make the display editor.
            //try {
                for (Iterator it = factors.iterator(); it.hasNext(); ) {
                    String factor = (String) it.next();

                    addNode(factor);
                }

                // Add edges for each of the edges in the model.
                for (Iterator it = factors.iterator(); it.hasNext(); ) {
                    String factor = (String) it.next();

            getDisplayFactor(factor).synchDisplayEdges();
          }

            adjustPreferredSize();

            // if this editor is sitting inside of a scrollpane,
            // let the scrollpane know how big it is.

        if (displayFactors.size()==0) {
          scrollRectToVisible(getVisibleRect());
        } else {
          scrollToHome();
        }
            registerKeys();

         */

    }

    //
    // table model class
    //
    // Represents a lag editor as a square table, with
    protected class LagGraphTableModel extends AbstractTableModel
            implements PropertyChangeListener {
        ActiveLagGraph sourceGraph;
        List factors;

        public LagGraphTableModel(ActiveLagGraph sourceGraph) {
            setModelGraph(sourceGraph);
        }

        public void addFactor(String name) {
            factors = new ArrayList(getModelGraph().getFactors());
            int newRowIndex = factors.size() - 1;
            fireTableStructureChanged(); // refresh cols
            fireTableRowsInserted(newRowIndex, newRowIndex);
        }

        public void removeFactor(String name) {
            // find the index of the factor that was removed from the old factors
            int index = factors.indexOf(name);
            //if (index==-1)
            //  throw new Exception("lag editor instructed to remove a factor that doesn't exist in display");

            factors = new ArrayList(getModelGraph().getFactors());
            fireTableStructureChanged(); // refresh cols
            fireTableRowsDeleted(index, index); // refresh rows
        }

        public void renameFactor(String oldName) {
            int index = factors.indexOf(oldName);
            factors = new ArrayList(getModelGraph().getFactors());
            fireTableStructureChanged(); // refresh cols
            fireTableRowsUpdated(index, index); // refresh rows
        }

        public void setModelGraph(ActiveLagGraph sourceGraph) {
            if (getModelGraph() != null) {
                getModelGraph().removePropertyChangeListener(this);
            }
            this.sourceGraph = sourceGraph;
            if (sourceGraph != null) {
                factors = new ArrayList(getModelGraph().getFactors());

                getModelGraph().addPropertyChangeListener(this);
                fireTableStructureChanged(); // refresh cols
                fireTableRowsInserted(0, factors.size() - 1);
            }
        }

        public ActiveLagGraph getModelGraph() {
            return sourceGraph;
        }

        public String getColumnName(int col) {
            if (col == 0) {
                return "Source Factors";
            }
            else {
                return "-> " + factors.get(col - 1);
            }
        }

        public String getRowFactor(int row) {
            return (String) factors.get(row);
        }

        public int getRowCount() {
            return factors.size();
        }

        public int getColumnCount() {
            return factors.size() + 1;
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return factors.get(row);
            }
            else {
                // return the lag factors between factor specified by row and col
                // row represents the source factor
                // col-1 represents the dest factor
                col--;

                // retrieve the parents of the column- this gives possible source factors
                String factor = (String) factors.get(col);
                SortedSet parents = getModelGraph().getParents(factor);

                // next scan those source factors to see if row is among them. If so, add
                // all lags coming from that factor into the lags vector
                String sourceFactor = (String) factors.get(row);
                Iterator it = parents.iterator();
                TreeSet lags =
                        new TreeSet(); // will hold all lags from source factor to dest factor
                while (it.hasNext()) {
                    LaggedFactor lf = (LaggedFactor) it.next();
                    if (sourceFactor.equals(lf.getFactor())) {
                        lags.add(lf.getLag());
                    }
                }

                // put the return string together
                String ret = "";
                it = lags.iterator();
                if (it.hasNext()) {
                    ret = it.next().toString();
                }

                while (it.hasNext()) {
                    ret = ret + ", " + it.next().toString();
                }

                return ret;
            }
        }

        public boolean isCellEditable(int row, int col) {
            // first column is reserved for factor names
            return true;//(col>0);
        }

        public void setValueAt(Object value, int row, int col) {
            //System.out.println(value);
            if (col == 0) { // rename factor
                getModelGraph().renameFactor((String) getValueAt(row, col),
                        (String) value);
            }
            else { // set a lag
                SortedSet toCreate, toRemove;
                toCreate = new TreeSet();

                col--; // first column stores factor names, not data
                String sourceFactor = (String) factors.get(row);
                String destFactor = (String) factors.get(col);

                // parse into a list of integers
                StringTokenizer parser =
                        new StringTokenizer(value.toString(), ", ");

                //List lags = new LinkedList();

                while (parser.hasMoreTokens()) {
                    try {
                        int lag = Integer.parseInt(parser.nextToken());
                        toCreate.add(new LaggedFactor(sourceFactor, lag));
                    }
                    catch (NumberFormatException e) {
                        // TODO
                    }
                }

                // synchronize edges
                toRemove = new TreeSet(getModelGraph().getParents(
                        destFactor)); // get current display
                Iterator it = toRemove.iterator();
                while (it.hasNext()) {
                    LaggedFactor lf = (LaggedFactor) it.next();
                    if (!lf.getFactor().equals(sourceFactor)) {
                        it.remove();
                    }
                }

                SortedSet toKeep = new TreeSet(toCreate);
                toCreate.removeAll(
                        toRemove); // create anything that's not in current model editor
                toRemove.removeAll(
                        toKeep); // remove anything in current that's not in what the user just entered

                it = toRemove.iterator();
                while (it.hasNext()) {
                    getModelGraph().removeEdge(destFactor,
                            (LaggedFactor) it.next());
                }
                it = toCreate.iterator();
                while (it.hasNext()) {
                    getModelGraph().addEdge(destFactor,
                            (LaggedFactor) it.next());
                }
            }
        }

        // handles events fired upward from the lagGraph
        public void propertyChange(PropertyChangeEvent e) {

            String propName = e.getPropertyName();
            Object old = e.getOldValue();
            Object _new = e.getNewValue();

            if (propName.equals("nodeAdded")) {
                addFactor((String) _new);    // checks if node already added.
            }
            else if (propName.equals("nodeRemoved")) {
                removeFactor((String) old);
            }
            else if (propName.equals("edgeAdded")) {
                LaggedEdge le = (LaggedEdge) _new;
                int row = factors.indexOf(le.getFactor());
                int col = factors.indexOf(le.getLaggedFactor().getFactor()) + 1;
                //for (int row=0; row<getFixedRowSize(); row++) {
                fireTableCellUpdated(row, col);
                //}
            }
            else if (propName.equals("edgeRemoved")) {
                LaggedEdge le = (LaggedEdge) old;
                int row = factors.indexOf(le.getFactor());
                int col = factors.indexOf(le.getLaggedFactor().getFactor()) + 1;
                fireTableCellUpdated(row, col);
                //fireTableDataChanged();
            }
            else if (propName.equals("edgeLaunch")) {
                System.out.println("Attempt to launch edge.");
            }
            else if (propName.equals("factorRenamed")) {
                renameFactor((String) old);
            }
        }

    }

    /**
     * Adds a node to the display and model. The name is automatically
     * generated
     */

    public void newFactor() {
        String name = nextVariableName(BASE_FACTOR_NAME);
        //addNode(name);
        // add to model
        getModelGraph().addFactor(name);
        // add edge to self
        LaggedFactor toSelf = new LaggedFactor(name, 1);
        getModelGraph().addEdge(name, toSelf);
        //adjustPreferredSize();

        // if this editor is sitting inside of a scrollpane,
        // let the scrollpane know how big it is.
        //scrollRectToVisible(getVisibleRect());
    }

    /**
     * Removes selected factors from model
     */
    public void clearFactors() {
        LagGraphTableModel m = (LagGraphTableModel) table.getModel();
        int[] selection = getSelectedRows();
        List toRemove = new LinkedList();
        for (int i = 0; i < selection.length; i++) {
            //System.out.println("row " + selection[i] + " to be deleted (" + m.getRowFactor(selection[i]) + ")");
            toRemove.add(m.getRowFactor(selection[i]));
        }

        Iterator it = toRemove.iterator();
        while (it.hasNext()) {
            m.getModelGraph().removeFactor((String) it.next());
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
            if (getModelGraph().existsFactor(name)) {
                continue loop;
            }

            return name;
        }

        //return base + i;
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

}

/*
 * 11/7/01: Moved this change log entry to the bottom of the file;
 * we're now putting change logs directly into CVS.
 *
 * Change log:
 *
 */


