package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.model.Params;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Mar 25, 2006 Time: 1:28:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SessionAppModule {

    Class getSessionNodeClass();

    JPanel newEditor();

    Params newParams();

    JPanel getParameterEditor(Params params, Object[] parentModels);

}
