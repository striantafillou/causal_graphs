package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.session.SessionNode;

import java.util.List;

/**
 * Represents a device that allows one to select between available models.  A chooser must have
 * an empty constructor, after construction the chooser's set methods will called in the following order:
 * setId(), setTitle(), setNodeName(), setModelConfigs(). After all set methods have been called the
 * setup() method should be called.
 * 
 * @author Tyler Gibson
 */
public interface ModelChooser {


    /**
     * Returns the title of the chooser.
     *
     * @return - title.
     */
    public String getTitle();


    /**
     * Returns the model class that was selected or null if nothing was selected.
     *
     * @return - selected model class.
     */
    public Class getSelectedModel();


    /**
     * The title to use for the chooser.
     *
     * @param title
     */
    public void setTitle(String title);

    /**
     * Sets the models that this chooser should display.
     *
     * @param configs
     */
    public void setModelConfigs(List<SessionNodeModelConfig> configs);


    /**
     * Sets the id for the node.
     *
     * @param id
     */
    public void setNodeId(String id);


    /**
     * Call after the set methods are called so that the component can build itself.
     */
    public void setup();

    /**
     * Passes in the SessionNode for the current node.
     */
    void setSessionNode(SessionNode sessionNode);
}
