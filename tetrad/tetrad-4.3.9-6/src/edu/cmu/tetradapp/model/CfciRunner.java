package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.Cfci;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.SearchGraphUtils;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the FCI algorithm.
 *
 * @author Joseph Ramsey
 */
public class CfciRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS================================//

    public CfciRunner(DataWrapper dataWrapper, FciSearchParams params) {
        super(dataWrapper, params);
    }

    public CfciRunner(Graph graph, FciSearchParams params) {
        super(graph, params);
    }

    public CfciRunner(GraphWrapper graphWrapper, FciSearchParams params) {
        super(graphWrapper.getGraph(), params);
    }

    public CfciRunner(DagWrapper dagWrapper, FciSearchParams params) {
        super(dagWrapper.getDag(), params);
    }

    public CfciRunner(SemGraphWrapper dagWrapper, FciSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CfciRunner serializableInstance() {
        return new CfciRunner(Dag.serializableInstance(),
                FciSearchParams.serializableInstance());
    }

    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {
        Knowledge knowledge = getParams().getKnowledge();
        SearchParams searchParams = getParams();

        IndTestParams indTestParams = searchParams.getIndTestParams();

        Cfci cfci = new Cfci(getIndependenceTest());
        cfci.setKnowledge(knowledge);
        cfci.setDepth(indTestParams.getDepth());
        Graph graph = cfci.search();

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
        IndTestType testType;

        if (getParams() instanceof BasicSearchParams) {
            BasicSearchParams _params = (BasicSearchParams) params;
            testType = _params.getIndTestType();
        }
        else {
            FciSearchParams _params = (FciSearchParams) params;
            testType = _params.getIndTestType();
        }

        return new IndTestFactory().getTest(dataModel, params, testType);
    }

    public Graph getGraph() {
        return getResultGraph();
    }
}
