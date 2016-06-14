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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetrad.session.ParamsResettable;
import edu.cmu.tetrad.util.Unmarshallable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a stub that basic algorithm wrappers can extend if they take
 * either a dataModel model or a workbench model as parent. Contains basic
 * methods for executing algorithms and returning results.
 *
 * @author Joseph Ramsey
 */
public abstract class AbstractAlgorithmRunner
        implements AlgorithmRunner, ParamsResettable, Unmarshallable {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * The parameters guiding this search (when executed).
     *
     * @serial Cannot be null.
     */
    private SearchParams params;

    /**
     * Keeps a reference to the dataModel source that has been provided
     * (hopefully either a dataModel model or a graph).
     *
     * @serial Can be null.
     */
    private DataModel dataModel;

    /**
     * Keeps a reference to the source graph, if there is one.
     *
     * @serial Can be null.
     */
    private Graph sourceGraph;

    /**
     * Keeps a reference to the result graph for the algorithm.
     *
     * @serial Can be null.
     */
    private Graph resultGraph;

    //===========================CONSTRUCTORS===========================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DatWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public AbstractAlgorithmRunner(DataWrapper dataWrapper,
            SearchParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }
        if (params == null) {
            throw new NullPointerException();
        }
        this.params = params;
        this.sourceGraph = dataWrapper.getSourceGraph();

        DataModel dataSource = getDataModel(dataWrapper);
        getParams().setKnowledge(dataWrapper.getKnowledge());
        List names = dataSource.getVariableNames();
        transferVarNamesToParams(names);
        new IndTestFactory().adjustIndTestParams(dataSource, params);
        this.dataModel = dataSource;
    }

    /**
     * Constucts a wrapper for the given graph.
     */
    public AbstractAlgorithmRunner(Graph sourceGraph, SearchParams params) {
        if (sourceGraph == null) {
            throw new NullPointerException(
                    "Tetrad sourceGraph must not be null.");
        }
        if (params == null) {
            throw new NullPointerException("Params must not be null.");
        }
        this.params = params;
        List<String> names = measuredNames(sourceGraph);
        transferVarNamesToParams(names);
        new IndTestFactory().adjustIndTestParams(sourceGraph, params);
        this.sourceGraph = sourceGraph;
    }

    //============================PUBLIC METHODS==========================//

    public final Graph getResultGraph() {
        return this.resultGraph;
    }

    /**
     * By default, algorithms do not support knowledge. Those that do will
     * speak up.
     */
    public boolean supportsKnowledge() {
        return false;
    }

    public ImpliedOrientation getMeekRules() {
        return null;
    }

    public final Graph getSourceGraph() {
        return this.sourceGraph;
    }

    public final DataModel getDataModel() {
        return dataModel;
    }

    public final void setResultGraph(Graph resultGraph) {
        this.resultGraph = resultGraph;
    }

    public final SearchParams getParams() {
        return this.params;
    }

    public Object getResettableParams() {
        return this.getParams();
    }

    public void resetParams(Object params) {
        this.params = (SearchParams) params;
    }

    //===========================PRIVATE METHODS==========================//

    /**
     * Find the dataModel model. (If it's a list, take the one that's
     * selected.)
     */
    public DataModel getDataModel(DataWrapper dataWrapper) {
        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (dataModel instanceof DataModelList) {
            DataModelList dataModelList = (DataModelList) dataModel;
            dataModel = dataModelList.getSelectedModel();
        }

        if (dataModel instanceof DataSet) {
            DataSet dataSet = (DataSet) dataModel;

            if (dataSet.isDiscrete()) {
                return dataSet;
            }
            else if (dataSet.isContinuous()) {
                return dataSet;
            }

            throw new IllegalArgumentException("<html>" +
                    "This dataModel set contains a mixture of discrete and continuous " +
                    "<br>columns; there are no algorithms in Tetrad currently to " +
                    "<br>search over such data sets." + "</html>");
        } else if (dataModel instanceof CovarianceMatrix) {
            return dataModel;
        } else if (dataModel instanceof TimeSeriesData) {
            return dataModel;
        }

        throw new IllegalArgumentException(
                "Unexpected dataModel source: " + dataModel);
    }

    private List<String> measuredNames(Graph graph) {
        List<String> names = new ArrayList<String>();
        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.MEASURED) {
                names.add(node.getName());
            }
        }
        return names;
    }

    private void transferVarNamesToParams(List names) {
        getParams().setVarNames(names);
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

        if (getParams() == null) {
            throw new NullPointerException();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


