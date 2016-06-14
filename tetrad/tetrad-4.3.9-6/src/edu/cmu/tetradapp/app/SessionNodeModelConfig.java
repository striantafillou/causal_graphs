package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.editor.ParameterEditor;

import javax.swing.*;

/**
 * Represents the configuration details for a particular model
 *
 * @author Tyler Gibson
 */
public interface SessionNodeModelConfig {

    /**
     * Returns the identifier to use for help.
     *
     * @return - help id.
     */
    String getHelpIdentifier();


    /**
     * Returns the category that this model config belongs to or null if there isn't one.
     * This allows you to organize models into various groupings.
     *
     * @return - Returns the category for this config or null if one was not set (not a required attribute).
     */
    String getCategory();


    /**
     * Returns the <code>Class</code> that represents the model.
     *
     * @return - model
     */
    Class getModel();


    /**
     * Returns a descriptive name for the model.
     *
     * @return - descriptive name.
     */
    String getName();


    /**
     * Returns the acronym for the model.
     *
     * @return - acronym.
     */
    String getAcronym();



    /**
     * Returns an instance of the editor to use for the model.
     *
     * @param arguments
     * @return - An editor
     * @throws IllegalArgumentException - Throws an exception of the arguments aren't of the right sort.
     */
    JPanel getEditorInstance(Object[] arguments);


    /**
     * Returns a newly created instance of the parameters for this model.
     *
     * @return - newly created parameter object.
     */
    Params getParametersInstance();


    /**
     * Returns a newly created instance of the parameter editor for the params
     * returned by <code>getParametersInstance()</code> or null if there is no such
     * editor.
     *
     * @return - returns a new editor or null if there isn't one.
     */
    ParameterEditor getParameterEitorInstance();


}
