package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Feb 14, 2004
 * Time: 8:46:08 PM
 * To change this template use Options | File Templates.
 *
 * This class describes the student guess of a manipulated graph.
 */
public class GuessedManipulatedGraph extends AbstractManipulatedGraph {
    static final long serialVersionUID = 23L;

    /**
     * Constructor.
     */
    public GuessedManipulatedGraph(Graph graph){
        super(graph);

        Iterator i;
        Node variable;
        String variableName;
        VariableManipulation manipulation;

        for(i = graph.getNodes().listIterator(); i.hasNext(); ){
            variable = (Node) i.next();
            variableName = variable.getName();
            manipulation = VariableManipulation.NONE;
            if(variable.getNodeType() == NodeType.LATENT){
                manipulation = VariableManipulation.NONE;
            }
            variableManipulations.put(variableName, manipulation);
        }

    }

    /**
     * Set a directed edge to two valid nodes.
     * @throws IllegalArgumentException
     */
    public void setEdge(String fromVariable, String toVariable) throws IllegalArgumentException {
        Node fromNode = getNode(fromVariable);
        Node toNode = getNode(toVariable);
        if(fromNode == null){ throw new IllegalArgumentException(fromNode + "not found");} //$NON-NLS-1$
        if(toNode   == null){ throw new IllegalArgumentException(toNode + "not found");} //$NON-NLS-1$

        removeEdgeFromGuess(fromVariable, toVariable);
        addDirectedEdge(fromNode, toNode);
    }

    /**
     * Set the edge from the start node to end node as frozen.
     */
    public void setEdgeFrozen(String fromVariable, String toVariable){
        removeEdgeFromGuess(fromVariable, toVariable);
        frozenEdges.add(new ManipulatedEdge(fromVariable, toVariable, EdgeManipulation.FROZEN));
    }

    /**
     * Set the edge from the start node to end node as broken.
     */
    public void setEdgeBroken(String fromVariable, String toVariable){
        removeEdgeFromGuess(fromVariable, toVariable);
        brokenEdges.add(new ManipulatedEdge(fromVariable, toVariable, EdgeManipulation.BROKEN));
    }

    /**
     * Set a particular variable as being not manipulated.
     * @throws IllegalArgumentException
     */
    public void setVariableNotManipulated(String variableName) throws IllegalArgumentException{
        setVariableManipulation(variableName, VariableManipulation.NONE);
    }

    /**
     * Set a particular variable as being not locked.
     * @throws IllegalArgumentException
     */
    public void setVariableLocked(String variableName) throws IllegalArgumentException{
        setVariableManipulation(variableName, VariableManipulation.LOCKED);
    }

    /**
     * Set a particular variable as being not randomized.
     * @throws IllegalArgumentException
     */
    public void setVariableRandomized(String variableName) throws IllegalArgumentException{
        setVariableManipulation(variableName, VariableManipulation.RANDOMIZED);
    }

    /**
     * Remove the edge between the two given nodes.
     * @param fromVariable
     * @param toVariable
     */
    public void removeEdgeFromGuess(String fromVariable, String toVariable){
        removeEdge(getNode(fromVariable), getNode(toVariable));
        removeBrokenEdge(fromVariable, toVariable);
        removeFrozenEdge(fromVariable, toVariable);
    }

    private void setVariableManipulation(String variableName, VariableManipulation type) throws IllegalArgumentException{
        if(variableManipulations.get(variableName) != null){
            variableManipulations.put(variableName, type);
        }else{
            throw new IllegalArgumentException(variableName + " not in guess"); //$NON-NLS-1$
        }
    }


    private void removeBrokenEdge(String fromVariable, String toVariable){
        ManipulatedEdge edge;
        List <ManipulatedEdge> edgesToRemove = new ArrayList<ManipulatedEdge> ();

        for(Iterator i = brokenEdges.iterator(); i.hasNext(); ){
            edge = (ManipulatedEdge) i.next();

            if(((edge.getFromNode().equals(fromVariable)) && (edge.getToNode().equals(toVariable))) ||
               ((edge.getFromNode().equals(toVariable))   && (edge.getToNode().equals(fromVariable)))){
                edgesToRemove.add(edge);
            }
        }
        for(ManipulatedEdge j : edgesToRemove){
            brokenEdges.remove(j);
        }
    }

    private void removeFrozenEdge(String fromVariable, String toVariable){
        ManipulatedEdge edge;
        List <ManipulatedEdge> edgesToRemove = new ArrayList<ManipulatedEdge> ();

        for(Iterator i = frozenEdges.iterator(); i.hasNext(); ){
            edge = (ManipulatedEdge) i.next();

            if(((edge.getFromNode().equals(fromVariable)) && (edge.getToNode().equals(toVariable))) ||
               ((edge.getFromNode().equals(toVariable))   && (edge.getToNode().equals(fromVariable)))){
                edgesToRemove.add(edge);
            }
        }
        for(ManipulatedEdge j : edgesToRemove){
            frozenEdges.remove(j);
        }
    }


}
