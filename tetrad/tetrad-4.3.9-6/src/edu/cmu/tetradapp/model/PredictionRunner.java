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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.predict.ManipulatedVariable;
import edu.cmu.tetrad.predict.Prediction;
import edu.cmu.tetrad.session.SessionModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

/**
 * @author Erin Korber
 */
public class PredictionRunner implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private Graph pag;

    /**
     * @serial Cannot be null.
     */
    private DataSet dataSet;

    /**
     * @serial Can be null.
     */
    private ManipulatedVariable manipulatedVariable;

    /**
     * @serial Can be null.
     */
    private Set conditioningVariables;

    /**
     * @serial Can be null.
     */
    private Node predictedVariable;

    /**
     * The result of the prediction algorithm being run. TODO: Pin down the
     * interpretation of this! jdramsey 2/22/2005
     *
     * @serial No restriction on range.
     */
    private double result = Double.NaN;

    //=============================CONSTRUCTORS===========================//

    public PredictionRunner(Graph pag, DataSet dataSet) {
        if (pag == null) {
            throw new NullPointerException("Please specify a PAG.");
        }

        if (dataSet == null) {
            throw new NullPointerException("Please specify a data set.");
        }

        this.pag = pag;
        this.dataSet = dataSet;
    }

    public PredictionRunner(FciRunner f, DataWrapper w) {
        this(new EdgeListGraph(f.getResultGraph()),
                (DataSet) w.getSelectedDataModel());
    }

    public PredictionRunner(FciRunner f, SemDataWrapper w) {
        this(new EdgeListGraph(f.getResultGraph()),
                (DataSet) w.getSelectedDataModel());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PredictionRunner serializableInstance() {
        return new PredictionRunner(EdgeListGraph.serializableInstance(),
                DataUtils.continuousSerializableInstance());
    }

    //==============================PUBLIC METHODS=======================//

    public void execute() {
        if (predictedVariable == null || manipulatedVariable == null) {
            throw new NullPointerException(
                    "You must have both a predicted and " +
                            "a manipulated variable set.");
        }

        Prediction prediction = new Prediction(pag, dataSet,
                manipulatedVariable, predictedVariable, conditioningVariables);
        setResult(prediction.predict());
    }

    public double getResult() {
        return result;
    }

    public Graph getPag() {
        return pag;
    }

    public void setManipulatedVariable(
            ManipulatedVariable manipulatedVariable) {
        this.manipulatedVariable = manipulatedVariable;
    }

    public void setPredictedVariable(Node predictedVariable) {
        this.predictedVariable = predictedVariable;
    }

    public void setConditioningVariables(Set conditioningVariables) {
        this.conditioningVariables = conditioningVariables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //============================PRIVATE METHODS========================//

    private void setResult(double result) {
        this.result = result;
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

        if (pag == null) {
            throw new NullPointerException();
        }

        if (dataSet == null) {
            throw new NullPointerException();
        }
    }
}


