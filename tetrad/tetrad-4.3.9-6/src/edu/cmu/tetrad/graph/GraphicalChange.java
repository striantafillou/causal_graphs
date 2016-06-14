package edu.cmu.tetrad.graph;

/**
 * Represents a graphical change.
 *
 * @author Tyler Gibson
 */
public interface GraphicalChange {


    /**
     * Applies the graphical change to the given graph if applicable.
     */
    void apply(Graph g);

    

}
