package edu.cmu.tetrad.graph;

/**
 * An unordered pair of nodes.
 *
 * @author Tyler Gibson
 */
public class NodePair {


    /**
     * The "First" node.
     */
    private Node first;


    /**
     * The "second" node.
     */
    private Node second;


    public NodePair(Node first, Node second){
        if(first == null){
            throw new NullPointerException("First node must not be null.");
        }
        if(second == null){
            throw new NullPointerException("Second node must not be null.");
        }
        this.first = first;
        this.second = second;
    }

    //============================== Public methods =============================//

    public Node getFirst(){
        return this.first;
    }

    public Node getSecond(){
        return this.second;
    }

    public int hashCode(){
        return this.first.hashCode() + this.second.hashCode();
    }


    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(!(o instanceof NodePair)){
            return false;
        }
        NodePair thatPair = (NodePair)o;
        if(this.first.equals(thatPair.first) && this.second.equals(thatPair.second)){
            return true;
        }
        return this.first.equals(thatPair.second) && this.second.equals(thatPair.first);
    }

    public String toString(){
        return "{" + this.first + ", " + this.second + "}";
    }

}
