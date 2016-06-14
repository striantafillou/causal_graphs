package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.sem.SemPm2;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 */
public class Sem2PmWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * The wrapped SemPm.
     *
     * @serial Cannot be null.
     */
    private final SemPm2 semPm2;

    //==============================CONSTRUCTORS==========================//

    private Sem2PmWrapper(Graph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

//        if (graph.getNodes().isEmpty()) {
//            throw new IllegalArgumentException("The parent graph is empty.");
//        }

        if (graph instanceof SemGraph) {
            this.semPm2 = new SemPm2(graph);
        }
        else {
            try {
                this.semPm2 = new SemPm2(new SemGraph(graph));
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        log(semPm2);
    }

    /**
     * Creates a new BayesPm from the given workbench and uses it to construct a
     * new BayesPm.
     */
    public Sem2PmWrapper(GraphWrapper graphWrapper) {
        this(new EdgeListGraph(graphWrapper.getGraph()));
    }

    /**
     * Creates a new BayesPm from the given workbench and uses it to construct a
     * new BayesPm.
     */
    public Sem2PmWrapper(DagWrapper dagWrapper) {
        this(new EdgeListGraph(dagWrapper.getDag()));
    }

    /**
     * Creates a new BayesPm from the given workbench and uses it to construct a
     * new BayesPm.
     */
    public Sem2PmWrapper(SemGraphWrapper semGraphWrapper) {
        this(semGraphWrapper.getSemGraph());
    }

    public Sem2PmWrapper(AlgorithmRunner wrapper) {
        this(new EdgeListGraph(wrapper.getResultGraph()));
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Sem2PmWrapper serializableInstance() {
        return new Sem2PmWrapper(Dag.serializableInstance());
    }

    //============================PUBLIC METHODS=========================//

    public SemPm2 getSemPm2() {
        return this.semPm2;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (semPm2 == null) {
            throw new NullPointerException();
        }
    }

    public Graph getGraph() {
        return semPm2.getGraph();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //======================= Private methods ====================//

    private void log(SemPm2 pm){
        TetradLogger.getInstance().setTetradLoggerConfigForModel(this.getClass());
        TetradLogger.getInstance().info("PM type = SEM");
        TetradLogger.getInstance().log("pm", pm.toString());
        TetradLogger.getInstance().reset();
    }


}
