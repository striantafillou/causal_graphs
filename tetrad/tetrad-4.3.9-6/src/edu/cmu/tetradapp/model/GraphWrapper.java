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

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Holds a tetrad-style graph with all of the constructors necessary for it to
 * serve as a model for the tetrad application.
 *
 * @author Joseph Ramsey
 */
public class GraphWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private Graph graph;

    //=============================CONSTRUCTORS==========================//

    public GraphWrapper(Graph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }
        this.graph = graph;
        log(graph);
    }

    public GraphWrapper(GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            graph = new EdgeListGraph();
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            makeRandomGraph(params);
        }
        log(graph);
    }

    public GraphWrapper(GraphWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.graph = new EdgeListGraph(graphWrapper.getGraph());
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            makeRandomGraph(params);
        }
        log(graph);
    }

    public GraphWrapper(DagWrapper dagWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.graph = new EdgeListGraph(dagWrapper.getDag());
        } else if (params.getInitializationMode() == GraphParams.RANDOM) {
            makeRandomGraph(params);
        }
        log(graph);
    }

    public GraphWrapper(AlgorithmRunner wrapper) {
        this(new EdgeListGraph(wrapper.getResultGraph()));
    }

    public GraphWrapper(DataWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getVariables()));
    }

    public GraphWrapper(BayesPmWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getBayesPm().getDag()));
    }

    public GraphWrapper(BayesImWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getBayesIm().getBayesPm().getDag()));
    }

    public GraphWrapper(BayesEstimatorWrapper wrapper) {
        this(new EdgeListGraph(
                wrapper.getEstimatedBayesIm().getBayesPm().getDag()));
    }

    public GraphWrapper(CptInvariantUpdaterWrapper wrapper) {
        this(new EdgeListGraph(
                wrapper.getBayesUpdater().getManipulatedGraph()));
    }

    public GraphWrapper(SemPmWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getSemPm().getGraph()));
    }

    public GraphWrapper(SemImWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getSemIm().getSemPm().getGraph()));
    }

    public GraphWrapper(SemEstimatorWrapper wrapper) {
        this(new EdgeListGraph(wrapper.getSemEstimator().getEstimatedSem()
                .getSemPm().getGraph()));
    }

    public GraphWrapper(PurifyRunner wrapper) {
        this(wrapper.getResultGraph());
    }

    public GraphWrapper(BuildPureClustersRunner wrapper) {
        this(wrapper.getResultGraph());
    }

    public GraphWrapper(MimBuildRunner wrapper) {
        this(wrapper.getResultGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GraphWrapper serializableInstance() {
        return new GraphWrapper(Dag.serializableInstance());
    }

    //==============================PUBLIC METHODS======================//

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    //==========================PRIVATE METHODS===========================//

    private void log(Graph graph) {
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());
        if (config != null) {
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("Graph Type = General Graph");
            TetradLogger.getInstance().log("graph", "Graph = " + graph);
            TetradLogger.getInstance().reset();
        }
    }

    private void makeRandomGraph(GraphParams params) {
        Dag dag;

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

        graph = new EdgeListGraph(dag);
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

        if (graph == null) {
            graph = new EdgeListGraph();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


