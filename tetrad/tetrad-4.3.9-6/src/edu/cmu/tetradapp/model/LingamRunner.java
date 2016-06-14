package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetrad.search.Lingam;
import edu.cmu.tetrad.search.MeekRules;

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

public class LingamRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;

    //============================CONSTRUCTORS============================//

    public LingamRunner(DataWrapper dataWrapper) {
        super(dataWrapper, new LingamParams());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LingamRunner serializableInstance() {
        return new LingamRunner(DataWrapper.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    public void execute() {
        DataModel source = getDataModel();

        if (!(source instanceof DataSet)) {
            throw new IllegalArgumentException("Expecting a rectangular data set.");
        }

        DataSet data = (DataSet) source;

        if (!data.isContinuous()) {
            throw new IllegalArgumentException("Expecting a continuous data set.");
        }

        Lingam lingam = new Lingam();
        lingam.setAlpha(getParams().getIndTestParams().getAlpha());
        lingam.setPruningDone(true);

        double lingamPruningAlpha = Preferences.userRoot().getDouble("lingamPruningAlpha", 0.05);

        lingam.setAlpha(lingamPruningAlpha);
        Graph graph = lingam.lingam(data).getGraph();
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