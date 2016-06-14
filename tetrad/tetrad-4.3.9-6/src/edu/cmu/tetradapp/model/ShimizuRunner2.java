package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

/**
 * This has to be left here, due to a mistake in serialization. Sorry.
 *
 * @author Joseph Ramsey
 * @deprecated Use ShimuzuRunner instead.
 */
public class ShimizuRunner2 extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public ShimizuRunner2(DataWrapper dataWrapper, PcSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ShimizuRunner2 serializableInstance() {
        return new ShimizuRunner2(DataWrapper.serializableInstance(),
                PcSearchParams.serializableInstance());
    }

    public void execute() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public IndependenceTest getIndependenceTest() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public Graph getGraph() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
