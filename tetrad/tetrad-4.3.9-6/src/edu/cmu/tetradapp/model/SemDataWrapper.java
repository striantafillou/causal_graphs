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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.session.SessionModel;

/**
 * Wraps a data model so that a random sample will automatically be drawn on
 * construction from a SemIm.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class SemDataWrapper extends DataWrapper implements SessionModel {
    static final long serialVersionUID = 23L;

    //==============================CONSTRUCTORS=============================//

    public SemDataWrapper(SemImWrapper wrapper, SemDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isIncludeLatents();
        DataSet columnDataModel =
                wrapper.getSemIm().simulateData(sampleSize, latentDataSaved);
        this.setDataModel(columnDataModel);
        this.setSourceGraph(wrapper.getSemIm().getSemPm().getGraph());       
    }

    public SemDataWrapper(SemEstimatorWrapper wrapper, SemDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isIncludeLatents();
        DataSet dataModelContinuous = wrapper.getSemEstimator()
                .getEstimatedSem().simulateData(sampleSize, latentDataSaved);
        setDataModel(dataModelContinuous);
        setSourceGraph(wrapper.getSemEstimator().getEstimatedSem().getSemPm()
                .getGraph());

    }

    public SemDataWrapper(SemUpdaterWrapper wrapper, SemDataParams params) {
        int sampleSize = params.getSampleSize();
        boolean latentDataSaved = params.isIncludeLatents();
        DataSet dataModelContinuous = wrapper.getSemUpdater()
                .getUpdatedSemIm().simulateData(sampleSize, latentDataSaved);
        setDataModel(dataModelContinuous);
        setSourceGraph(wrapper.getSemUpdater().getUpdatedSemIm().getSemPm()
                .getGraph());

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new SemDataWrapper(SemImWrapper.serializableInstance(),
                SemDataParams.serializableInstance());
    }
}


