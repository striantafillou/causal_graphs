package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.session.SessionModel;

/**
 * Wraps a data model so that a random sample will automatically be drawn on
 * construction from a SemIm2.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class Sem2DataWrapper extends DataWrapper implements SessionModel {
    static final long serialVersionUID = 23L;

    //==============================CONSTRUCTORS=============================//

    public Sem2DataWrapper(Sem2ImWrapper wrapper, Sem2DataParams params) {
        int sampleSize = params.getSampleSize();
        DataSet columnDataModel =
                wrapper.getSemIm2().simulateData(sampleSize, params.getDistribution());
        this.setDataModel(columnDataModel);
        this.setSourceGraph(wrapper.getSemIm2().getSemPm2().getGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new Sem2DataWrapper(Sem2ImWrapper.serializableInstance(),
                Sem2DataParams.serializableInstance());
    }
}
