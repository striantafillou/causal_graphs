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

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.Cefs;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the MB Fan Search
 * algorithm.
 *
 * @author Frank Wimberly after Joe Ramsey's PcRunner
 */

public class CeFanSearchRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public CeFanSearchRunner(DataWrapper dataWrapper, MbSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CeFanSearchRunner(Graph graph, MbSearchParams params) {
        super(graph, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CeFanSearchRunner(GraphWrapper graphWrapper, MbSearchParams params) {
        super(graphWrapper.getGraph(), params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CeFanSearchRunner(DagWrapper dagWrapper, MbSearchParams params) {
        super(dagWrapper.getDag(), params);
    }

    public CeFanSearchRunner(SemGraphWrapper dagWrapper,
            BasicSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CeFanSearchRunner serializableInstance() {
        return new CeFanSearchRunner(Dag.serializableInstance(),
                MbSearchParams.serializableInstance());
    }

    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {
        int pcDepth = ((MbSearchParams) getParams()).getDepth();
        Cefs search =
                new Cefs(getIndependenceTest(), pcDepth);
        SearchParams params = getParams();
        if(params instanceof MeekSearchParams){
            search.setAggressivelyPreventCycles(((MeekSearchParams) params).isAggressivelyPreventCycles());
        }
        String targetName = ((MbSearchParams) getParams()).getTargetName();
        Graph searchGraph = search.search(targetName);
        setResultGraph(searchGraph);
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());
    }

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataModel();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        MbSearchParams params = (MbSearchParams) getParams();
        IndTestType testType = params.getIndTestType();
        return new IndTestFactory().getTest(dataModel, params, testType);
    }

    public Graph getGraph() {
        return getResultGraph();
    }



}


