package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.bayes.ManipulatingBayesUpdater;
import edu.cmu.tetrad.bayes.UpdaterParams;

/**
 * Stuff the GUI needs to know. Wrapped so that the GUI can access the params.
 *
 * @author Joseph Ramsey
 */
public interface UpdaterWrapper {

    /**
     * Makes the params object accessible to the GUI.
     */
    UpdaterParams getParams();

    ManipulatingBayesUpdater getBayesUpdater();
}
