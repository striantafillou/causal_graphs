package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;

/**
 * Adds any orientations implied by the given orientation.
 *
 * @author Joseph Ramsey
 */
public interface ImpliedOrientation {

    /**
     * Sets knowledge.
     */
    void setKnowledge(Knowledge knowledge);

    /**
     * Adds implied orientations.
     */
    void orientImplied(Graph graph);
}
