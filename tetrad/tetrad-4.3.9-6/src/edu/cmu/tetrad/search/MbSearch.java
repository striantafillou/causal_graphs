package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;

import java.util.List;


/**
 * @author Joseph Ramsey
 */
public interface MbSearch {


    /**
     * Given the target this returns all the nodes in the Markov Blanket.
     */
    List<Node> findMb(String targetName);


    /**
     * The name of the algorithm.
     */
    String getAlgorithmName();


    /**
     * Number of independent tests.
     */
    int getNumIndependenceTests();
}
