package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetrad.search.Lingam;
import edu.cmu.tetrad.search.MeekRules;
import edu.cmu.tetrad.search.Images;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class ImagesRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;

    //============================CONSTRUCTORS============================//

    public ImagesRunner(DataWrapper dataWrapper) {
        super(dataWrapper, new GesParams());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ImagesRunner serializableInstance() {
        return new ImagesRunner(DataWrapper.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    public DataModel getDataModel(DataWrapper dataWrapper) {
        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (dataModel instanceof DataModelList) {
            return dataModel;
        }

        DataModelList list = new DataModelList();

        if (dataModel instanceof DataSet) {
            DataSet dataSet = (DataSet) dataModel;

            if (dataSet.isDiscrete()) {
                list.add(dataSet);
            }
            else if (dataSet.isContinuous()) {
                list.add(dataSet);
            }

            throw new IllegalArgumentException("<html>" +
                    "This dataModel set contains a mixture of discrete and continuous " +
                    "<br>columns; there are no algorithms in Tetrad currently to " +
                    "<br>search over such data sets." + "</html>");
        } else if (dataModel instanceof CovarianceMatrix) {
            return dataModel;
        } else if (dataModel instanceof TimeSeriesData) {
            return dataModel;
        }

        return list;
    }

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    public void execute() {
        if (!(getDataModel() instanceof DataModelList)) {
            throw new IllegalArgumentException("Expecting a list of data sets.");
        }

        DataModelList source = (DataModelList) getDataModel();

        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (DataModel dataModel : source) {
            if (!(dataModel instanceof DataSet)) {
                throw new IllegalArgumentException("Expecting data sets.");
            }

            DataSet dataSet = (DataSet) dataModel;

            if (!dataSet.isContinuous()) {
                throw new IllegalArgumentException("Expecting a continuous data set.");
            }


            dataSets.add(dataSet);
        }

        Images images = new Images(dataSets);
        GesIndTestParams params = (GesIndTestParams) getParams().getIndTestParams();
        images.setPenaltyDiscount(params.getPenaltyDiscount());
        Graph graph = images.search();

        setResultGraph(graph);
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());
    }

    public Graph getGraph() {
        return getResultGraph();
    }

    public boolean supportsKnowledge() {
        return true;
    }

    public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(getParams().getKnowledge());
        return rules;
    }

    private boolean isAggressivelyPreventCycles() {
        SearchParams params = getParams();
        if (params instanceof MeekSearchParams) {
            return ((MeekSearchParams) params).isAggressivelyPreventCycles();
        }
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }

    private void firePropertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(evt);
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (!getListeners().contains(l)) getListeners().add(l);
    }
}