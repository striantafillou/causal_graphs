///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.session.SessionModel;

/**
 * Wraps a data model so that a random sample will automatically be drawn on
 * construction from a BayesIm.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class BayesDataWrapper extends DataWrapper implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    //============================CONSTRUCTORS=========================//

    public BayesDataWrapper(BayesImWrapper wrapper, BayesDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isLatentDataSaved();
        setDataModel(wrapper.getBayesIm().simulateData(sampleSize, latentDataSaved));
        setSourceGraph(wrapper.getBayesIm().getDag());
    }

    public BayesDataWrapper(BayesPmWrapper wrapper) {
        setSourceGraph(wrapper.getBayesPm().getDag());
        setKnownVariables(wrapper.getBayesPm().getVariables());
    }

    public BayesDataWrapper(BayesEstimatorWrapper wrapper,
                            BayesDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isLatentDataSaved();
        setDataModel(wrapper.getEstimatedBayesIm().simulateData(sampleSize, latentDataSaved));
        setSourceGraph(wrapper.getEstimatedBayesIm().getDag());
    }

    public BayesDataWrapper(DirichletEstimatorWrapper wrapper,
                            BayesDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isLatentDataSaved();
        setDataModel(wrapper.getEstimatedBayesIm().simulateData(sampleSize, latentDataSaved));
        setSourceGraph(wrapper.getEstimatedBayesIm().getDag());
    }

    public BayesDataWrapper(CptInvariantUpdaterWrapper wrapper,
                            BayesDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isLatentDataSaved();
        setDataModel(wrapper.getBayesUpdater().getUpdatedBayesIm()
                .simulateData(sampleSize, latentDataSaved));
        setSourceGraph(wrapper.getBayesUpdater().getUpdatedBayesIm().getDag());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new BayesDataWrapper(BayesPmWrapper.serializableInstance());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


