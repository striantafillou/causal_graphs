package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.model.Params;

/**
 * Implements an editor some specific type of parameter object. It is assumed
 * that the parameter editor implementing this class has a blank constructor,
 * that <code>setParams</code> is called first, followed by
 * <code>setParantModel</code>, then <code>setup</code>. It is also assumed
 * that the implementing class will implement JComponent.
 *
 * @author Joseph Ramsey
 */
public interface ParameterEditor {

    /**
     * Sets the parameter object to be edited.
     */
    void setParams(Params params);

    /**
     * Sets the parent models that can be exploited for information in the
     * editing process.
     */
    void setParentModels(Object[] parentModels);

    /**
     * Sets up the GUI. Preupposes that the parameter class has been set and
     * that parent models have been passed, if applicable.
     */
    void setup();

    /**
     * True if this parameter editor must be shown when available.
     */
    boolean mustBeShown();
}
