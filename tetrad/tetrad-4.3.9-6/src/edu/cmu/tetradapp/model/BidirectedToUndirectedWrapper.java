package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;

/**
 * Picks a DAG from the given graph.
 *
 * @author Tyler Gibson
 */
public class BidirectedToUndirectedWrapper extends GraphWrapper{
    static final long serialVersionUID = 23L;



    public BidirectedToUndirectedWrapper(GraphSource source){
        this(source.getGraph());
    }


    public BidirectedToUndirectedWrapper(Graph graph){
        super(pickDagFromPattern(graph));
    }


    public static BidirectedToUndirectedWrapper serializableInstance(){
        return new BidirectedToUndirectedWrapper(EdgeListGraph.serializableInstance());
    }


    //======================== Private Methods ================================//


    private static Graph pickDagFromPattern(Graph graph){
        return GraphUtils.bidirectedToUndirected(graph);
    }
}