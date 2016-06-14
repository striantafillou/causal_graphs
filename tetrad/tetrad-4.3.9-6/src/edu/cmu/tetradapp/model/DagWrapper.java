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
/////////////////////////////////////////////////////////////////////////////
package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Holds a tetrad dag with all of the constructors necessary for it to serve as
 * a model for the tetrad application.
 *
 * @author Joseph Ramsey
 */
public class DagWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private Dag dag;

    //=============================CONSTRUCTORS==========================//

    public DagWrapper(Dag graph) {
        if (graph == null) {
            throw new NullPointerException("Tetrad dag must not be null.");
        }
        this.dag = graph;
        log();
    }

    public DagWrapper(GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            dag = new Dag();
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public DagWrapper(DagWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.dag = new Dag(graphWrapper.getDag());
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public DagWrapper(SemGraphWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.dag = new Dag(graphWrapper.getSemGraph());
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public DagWrapper(GraphWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.dag = new Dag(graphWrapper.getGraph());
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public DagWrapper(AbstractAlgorithmRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(PcRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(FciRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(CcdRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(GesRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(MimBuildRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(PurifyRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(BuildPureClustersRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(MbfsRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(CeFanSearchRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    public DagWrapper(DataWrapper wrapper) {
        this(new Dag(new EdgeListGraph(wrapper.getVariables())));
    }

    public DagWrapper(BayesPmWrapper wrapper) {
        this(new Dag(wrapper.getBayesPm().getDag()));
    }

    public DagWrapper(BayesImWrapper wrapper) {
        this(new Dag(wrapper.getBayesIm().getBayesPm().getDag()));
    }

    public DagWrapper(BayesEstimatorWrapper wrapper) {
        this(new Dag(wrapper.getEstimatedBayesIm().getBayesPm().getDag()));
    }

    public DagWrapper(CptInvariantUpdaterWrapper wrapper) {
        this(new Dag(wrapper.getBayesUpdater().getManipulatedGraph()));
    }

    public DagWrapper(SemPmWrapper wrapper) {
        this(new Dag(wrapper.getSemPm().getGraph()));
    }

    public DagWrapper(SemImWrapper wrapper) {
        this(new Dag(wrapper.getSemIm().getSemPm().getGraph()));
    }

    public DagWrapper(SemEstimatorWrapper wrapper) {
        this(new Dag(wrapper.getSemEstimator().getEstimatedSem().getSemPm()
                .getGraph()));
    }

    public DagWrapper(RegressionRunner wrapper) {
        this(new Dag(wrapper.getResultGraph()));
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DagWrapper serializableInstance() {
        return new DagWrapper(Dag.serializableInstance());
    }

    //================================PUBLIC METHODS=======================//

    public Dag getDag() {
        return dag;
    }

    public void setDag(Dag graph) {
        this.dag = graph;
    }

    //============================PRIVATE METHODS========================//


    private void log(){
        TetradLogger.getInstance().setTetradLoggerConfigForModel(DagWrapper.class);
        TetradLogger.getInstance().info("Graph type = DAG");
        TetradLogger.getInstance().log("graph", "Graph = " + dag);
        TetradLogger.getInstance().reset();
    }



    private void createRandomDag(GraphParams params) {
        if (params.isUniformlySelected()) {
            dag = GraphUtils.randomDag(params.getNumNodes(),
                    params.getNumLatents(), params.getMaxEdges(),
                    params.getMaxDegree(), params.getMaxIndegree(),
                    params.getMaxOutdegree(), params.isConnected());
        } else {
//            dag = GraphUtils.randomDagC(params.getNumNodes(),
//                    params.getNumLatents(), params.getMaxEdges()
//            );
            dag = GraphUtils.randomDag(params.getNumNodes(),
                    params.getNumLatents(), params.getMaxEdges(),
                    30, 15, 15, params.isConnected()
            );
        }
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

        if (dag == null) {
            throw new NullPointerException();
        }
    }

    public Graph getGraph() {
        return dag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


