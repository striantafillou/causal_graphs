package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class LingamPatternRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;
    private Graph pattern;

    //============================CONSTRUCTORS============================//

//    public LingamPatternRunner(DataWrapper dataWrapper, PcSearchParams params) {
//        super(dataWrapper, params);
//    }

    public LingamPatternRunner(GraphWrapper graphWrapper, DataWrapper dataWrapper,
                               PcSearchParams params) {
        super(dataWrapper, params);
        this.pattern = graphWrapper.getGraph();
    }

    public LingamPatternRunner(PcRunner wrapper, DataWrapper dataWrapper,
                               PcSearchParams params) {
        super(dataWrapper, params);
        this.pattern = wrapper.getGraph();
    }

    public LingamPatternRunner(CpcRunner wrapper, DataWrapper dataWrapper,
                               PcSearchParams params) {
        super(dataWrapper, params);
        this.pattern = wrapper.getGraph();
    }

    public LingamPatternRunner(AcpcRunner wrapper, DataWrapper dataWrapper,
                               PcSearchParams params) {
        super(dataWrapper, params);
        this.pattern = wrapper.getGraph();
    }

    public LingamPatternRunner(GesRunner wrapper, DataWrapper dataWrapper,
                               PcSearchParams params) {
        super(dataWrapper, params);
        this.pattern = wrapper.getGraph();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LingamStructureRunner serializableInstance() {
        return new LingamStructureRunner(DataWrapper.serializableInstance(),
                PcSearchParams.serializableInstance());
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

        DataSet dataSet = (DataSet) source;

        if (!dataSet.isContinuous()) {
            throw new IllegalArgumentException("Expecting a continuous data set.");
        }

        if (pattern == null) {

//            Cpc search = new Cpc(getIndependenceTest(), getParams().getKnowledge());
            Ges search = new Ges(dataSet);
            search.setKnowledge(getParams().getKnowledge());
            pattern = search.search();
//            Acpc search = new Acpc(getIndependenceTest());
//            PcdSearch search = new PcdSearch(getIndependenceTest(), new Knowledge());
//            pattern = search.search();
        }

        LingamPattern lingamPattern = new LingamPattern(pattern, dataSet);
        lingamPattern.setKnowledge(getParams().getKnowledge());
//        lingamPattern.setNumSamples(200);

//        Graph estPattern = new PcSearch(test1, new Knowledge()).search();
//        Graph estPattern = new GesSearch(dataSet).search();
//        List<Graph> dags = SearchGraphUtils.getDagsInPatternMeek(pattern, getParams().getKnowledge());

//        LingamPattern.Result result = lingamPattern.search(dags, dataSet);
        Graph graph = lingamPattern.search();
        setResultGraph(graph);
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());


//        for (int i = 0; i < result.getDags().size(); i++) {
//            System.out.println("\n\nModel # " + (i + 1) + " # votes = " + result.getCounts().get(i));
//            System.out.println(result.getDags().get(i));
//        }
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

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataModel();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        IndTestType testType = (getParams()).getIndTestType();
        return new IndTestFactory().getTest(dataModel, getParams(), testType);
    }
}