package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.SearchGraphUtils;

/**
 * @author Tyler Gibson
 */
public class PatternFromDagGraphWrapper extends GraphWrapper {
    static final long serialVersionUID = 23L;

    
    public PatternFromDagGraphWrapper(GraphSource source) {
        this(source.getGraph());
    }


    public PatternFromDagGraphWrapper(Graph graph) {
        // make sure the given graph is a dag.
        super(getPattern(new Dag(graph)));
    }

    public static PatternFromDagGraphWrapper serializableInstance() {
        return new PatternFromDagGraphWrapper(EdgeListGraph.serializableInstance());
    }

    //======================== Private Method ======================//


    private static Graph getPattern(Dag dag) {
        return SearchGraphUtils.patternFromDag(dag);
    }


}
