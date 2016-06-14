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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.session.ExecutionRestarter;
import edu.cmu.tetrad.session.SessionAdapter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores a reference to a file to which records can be appended.
 *
 * @author Joseph Ramsey
 */
public class GraphComparisonParams extends SessionAdapter
        implements Params, ExecutionRestarter {
    static final long serialVersionUID = 23L;

    /**
     * The data set to which records are appended.
     *
     * @serial Cannot be null.
     */
    private DataSet dataSet;

    /**
     * True iff the data table should be reset every time. Must be true by
     * default so dataSet will be initialized.
     *
     * @serial True, false both OK.
     */
    private boolean resetTableOnExecute = true;

    /**
     * True if the user wants to compare with the exact reference graph instead
     * of removing the latent variables.
     *
     * @serial True, false both OK.
     */
    private boolean keepLatents = false;

    /**
     * The name of the session model that has the true graph in it.
     *
     * @serial Can be null.
     */
    private String referenceGraphName;

    /**
     * @serial
     * @deprecated
     */
    private DiscreteVariable missingEdgesVar;

    /**
     * @serial
     * @deprecated
     */
    private DiscreteVariable correctEdgesVar;

    /**
     * @serial
     * @deprecated
     */
    private DiscreteVariable extraEdgesVar;

    //===========================CONSTRUCTORS============================//

    /**
     * Constructs a getMappings object with no file set.
     */
    public GraphComparisonParams() {
        newExecution();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GraphComparisonParams serializableInstance() {
        return new GraphComparisonParams();
    }

    //==========================PUBLIC METHODS===========================//

    public void addRecord(int adjCorrect, int adjFn, int adjFp,
            int arrowptCorrect, int arrowptFn, int arrowptFp) {
        int newRow = dataSet.getNumRows();
        dataSet.setInt(newRow, 0, adjCorrect);
        dataSet.setInt(newRow, 1, adjFn);
        dataSet.setInt(newRow, 2, adjFp);
        dataSet.setInt(newRow, 3, arrowptCorrect);
        dataSet.setInt(newRow, 4, arrowptFn);
        dataSet.setInt(newRow, 5, arrowptFp);
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public final void newExecution() {
        if (isResetTableOnExecute()) {
            DiscreteVariable adjCorrect = new DiscreteVariable("ADJ_COR");
            DiscreteVariable adjFn = new DiscreteVariable("ADJ_FN");
            DiscreteVariable adjFp = new DiscreteVariable("ADJ_FP");

            DiscreteVariable arrowptCorrect = new DiscreteVariable("APT_COR");
            DiscreteVariable arrowptFn = new DiscreteVariable("APT_FN");
            DiscreteVariable arrowptFp = new DiscreteVariable("APT_FP");

            List<Node> variables = new LinkedList<Node>();
            variables.add(adjCorrect);
            variables.add(adjFn);
            variables.add(adjFp);
            variables.add(arrowptCorrect);
            variables.add(arrowptFn);
            variables.add(arrowptFp);

            dataSet = new ColtDataSet(0, variables);
        }
    }

    public boolean isResetTableOnExecute() {
        return resetTableOnExecute;
    }

    public void setResetTableOnExecute(boolean resetTableOnExecute) {
        this.resetTableOnExecute = resetTableOnExecute;
    }

    public boolean isKeepLatents() {
        return keepLatents;
    }

    public void setKeepLatents(boolean keepLatents) {
        this.keepLatents = keepLatents;
    }

    public void setReferenceGraphName(String name) {
        this.referenceGraphName = name;
    }

    public String getReferenceGraphName() {
        return referenceGraphName;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}


