package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.PatternToDag;

/**
 * Picks a DAG from the given graph.
 *
 * @author Tyler Gibson
 */
public class ChooseDagGraphWrapper extends GraphWrapper{
    static final long serialVersionUID = 23L;

    public ChooseDagGraphWrapper(GraphSource source){
        this(source.getGraph());
    }


    public ChooseDagGraphWrapper(Graph graph){
        super(pickDagFromPattern(graph));
    }


    public static ChooseDagGraphWrapper serializableInstance(){
        return new ChooseDagGraphWrapper(EdgeListGraph.serializableInstance());
    }


    //======================== Private Methods ================================//


    private static Graph pickDagFromPattern(Graph graph){
        Graph newGraph = new EdgeListGraph(graph);

        for(Edge edge : newGraph.getEdges()){
            if(Edges.isBidirectedEdge(edge)){
                newGraph.removeEdge(edge);
            }
        }
        PatternToDag search = new PatternToDag(new Pattern(newGraph));
        Graph dag = search.patternToDagMeekRules();
        GraphUtils.arrangeBySourceGraph(dag, graph);
        return dag;
    }
}
