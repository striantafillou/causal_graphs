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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.Pcd;
import edu.cmu.tetrad.search.SearchGraphUtils;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the PC algorithm.
 *
 * @author Joseph Ramsey
 */
public class PcdRunner2 extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public PcdRunner2(DataWrapper dataWrapper, PcSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public PcdRunner2(Graph graph, PcSearchParams params) {
        super(graph, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public PcdRunner2(GraphWrapper graphWrapper, PcSearchParams params) {
        super(graphWrapper.getGraph(), params);
    }

    public PcdRunner2(DagWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getDag(), params);
    }

    public PcdRunner2(SemGraphWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PcdRunner serializableInstance() {
        return new PcdRunner(Dag.serializableInstance(),
                PcSearchParams.serializableInstance());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
//    public void execute() {
//        Knowledge knowledge = getParams().getKnowledge();
//        IndependenceTest independenceTest = getIndependenceTest();
//        PcdSearch pcdSearch = new PcdSearch(independenceTest, knowledge);
//        pcdSearch.setDepth(getParams().getIndTestParams().getDepth());
//        Graph resultGraph = pcdSearch.search();
//
//        if (knowledge.isDefaultToKnowledgeLayout()) {
//            SearchGraphUtils.arrangeByKnowledgeTiers(resultGraph, knowledge);
//        }
//
//        setResultGraph(resultGraph);
//    }
    public void execute() {
        Knowledge knowledge = getParams().getKnowledge();
        PcSearchParams searchParams = (PcSearchParams) getParams();

        PcIndTestParams indTestParams =
                (PcIndTestParams) searchParams.getIndTestParams();

        Pcd pc = new Pcd(getIndependenceTest());
        pc.setKnowledge(knowledge);
        pc.setDepth(indTestParams.getDepth());
        Graph graph = pc.search();

        if (knowledge.isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
        }

        setResultGraph(graph);
    }

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataModel();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        SearchParams params = getParams();
        IndTestType testType = params.getIndTestType();
        return new IndTestFactory().getTest(dataModel, getParams(), testType);
    }

    public Graph getGraph() {
        return getResultGraph();
    }
}


