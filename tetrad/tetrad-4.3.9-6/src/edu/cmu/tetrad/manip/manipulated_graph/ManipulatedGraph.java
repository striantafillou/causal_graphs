package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Sep 27, 2003
 * Time: 6:53:09 PM
 * To change this template use Options | File Templates.
 *
 * This class is the correct manipulated graph that is derived from a correct
 * graph and an experiment setup.
 */
public class ManipulatedGraph extends AbstractManipulatedGraph {
    static final long serialVersionUID = 23L;

    /**
     * Creates a new manipulated graph given a correct graph and an experimental setup.
     * Variables that have been disabled will be removed from the graph, and any
     * edges that are connected
     * @param graph
     * @param experimentalSetup
     */
    public ManipulatedGraph(Graph graph, ExperimentalSetup experimentalSetup) throws IllegalArgumentException{
        //copy constructor
        super(graph);

        if(experimentalSetup == null){
            throw new IllegalArgumentException("experimental setup was null"); //$NON-NLS-1$
        }

        Iterator i;
        Node variable;
        String variableName;
        VariableManipulation manipulation;

        //Keep track of the manipulations on each variable
        for(i = graph.getNodes().listIterator(); i.hasNext(); ){

            variable = (Node) i.next();
            variableName = variable.getName();

            manipulation = null;
            if(variable.getNodeType() == NodeType.MEASURED){
                manipulation = experimentalSetup.getVariable(variableName).getManipulation();
            }else if(variable.getNodeType() == NodeType.LATENT){
                manipulation = VariableManipulation.NONE;
            }
            variableManipulations.put(variableName, manipulation);

            if (manipulation == VariableManipulation.RANDOMIZED){
                breakEdges(findEdgesTo(variableName));
            } else if (manipulation == VariableManipulation.LOCKED){
                breakEdges(findEdgesTo(variableName));
                freezeEdges(findEdgesFrom(variableName));
            }
        }
    }

    private List findEdgesTo(String variableName){
        return findEdges(variableName, true);
    }

    private List findEdgesFrom(String variableName){
        return findEdges(variableName, false);
    }

    private List findEdges(String variableName, boolean isToVariable){
        List <Edge> edgesFound = new ArrayList<Edge>();

        for(Edge edge : getEdges(getNode(variableName))){
            Node node = (isToVariable) ? edge.getNode2() : edge.getNode1();
            if(node.getName().equals(variableName)){
                //mark the edge for removal
                edgesFound.add(edge);
            }
        }
        return edgesFound;
    }


}
