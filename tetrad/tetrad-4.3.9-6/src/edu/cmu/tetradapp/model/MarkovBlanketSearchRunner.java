package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Represents a runner for a Markov blanket search.
 *
 * @author Tyler Gibson
 */
public interface MarkovBlanketSearchRunner extends Executable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the search params.
     */
    MbSearchParams getParams();


    /**
     * Return the source for the search.
     */
    DataSet getSource();
    

    /**
     * Returns the data model for the variables in the markov blanket.
     */
    DataSet getDataModelForMarkovBlanket();


    /**
     * Returns the variables in the markov blanket.
     */
    List<Node> getMarkovBlanket();


    /**
     * Returns the name of the search.
     */
    String getSearchName();


    /**
     * Sets the search name.
     */
    void setSearchName(String n);


}
