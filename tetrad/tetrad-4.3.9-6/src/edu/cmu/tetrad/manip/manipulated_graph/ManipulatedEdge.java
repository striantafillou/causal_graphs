package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.Edge;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Sep 27, 2003
 * Time: 9:18:18 PM
 * To change this template use Options | File Templates.
 *
 * This class describes the properties of an edge.
 */
public class ManipulatedEdge {

    private EdgeManipulation type;
    private String fromNode;
    private String toNode;

    /**
     * Default constructor setting the Edge as a normal one.
     */
    public ManipulatedEdge(Edge edge){
        this(edge, EdgeManipulation.NORMAL);
    }

    /**
     * Use this constructor if you want to specify what type of edge this is (eg
     * normal, broken or frozen).
     *
     */
    public ManipulatedEdge(Edge edge, EdgeManipulation type){
        this(edge.getNode1().getName(), edge.getNode2().getName(), type);
    }

    /**
     * Use this constructor if you want to specify the start and end node, as well
     * as the manipulation type of the edge.
     */
    public ManipulatedEdge(String fromNode, String toNode, EdgeManipulation type){
        this.type = type;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    /**
     * @return start node of edge.
     */
    public String getFromNode(){
        return fromNode;
    }

    /**
     * @return end node of edge.
     */
    public String getToNode(){
        return toNode;
    }

    /**
     * @return manipulation type of this edge.
     */
    public EdgeManipulation getType(){
        return type;
    }

    /**
     * @return a string representation of this edge.
     */
    public String toString(){
        return new String(fromNode + " --> " + toNode); //$NON-NLS-1$
    }

    /**
     * Verify that object o is the same as this EdgeInfo.
     * @return true if so.
     */
    public boolean equals (Object o) {
        return o instanceof ManipulatedEdge && toString().equals(o.toString());
    }
}
