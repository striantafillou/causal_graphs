package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;

/**
 * Reorients all or part of the given graph.
 *
 * @author Joseph Ramsey
 */
public interface Reorienter {

    /**
     * Sets the knowledge.
     */
    void setKnowledge(Knowledge knowledge);

    /**
     * Globally reorients the graph.
     */
    void orient(Graph graph);
}
