package edu.cmu.tetradapp.app;

import edu.cmu.tetradapp.workbench.DisplayComp;

/**
 * The appearance of a session node.
 *
 * @author Joseph Ramsey
 */
public interface SessionDisplayComp extends DisplayComp {

    /**
     * Sets the acronym (e.g. "PC") for the node.
     */
    void setAcronym(String acronym);

    /**
     * Sets whether the node has a model--i.e. whether it should be rendered
     * in the "filled" color or not.
     */
    void setHasModel(boolean b);


}
