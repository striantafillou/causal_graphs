///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by authors listed in Javadoc.                          //
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

package edu.cmu.tetrad.search;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestMimBuild;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizerEm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Implements Generalized MimBuild, as specified on page 362 of Spirtes,
 * Glymour, and Scheines, "Causation, Prediction, and Search," 2nd edition. More
 * details are provided in Silva (2002).</p> <p>This implementation assumes the
 * measurement model is given in the Knowledge object passed by the constructor.
 * We assume that each latent has at least two measures.</p> <p>References:</p>
 * <p>Silva, R. (2002). "The structure of the unobserved". Technical report
 * CMU-CALD-02-102, Center for Automated Learning and Discovery, Carnegie Mellon
 * University.</p>
 *
 * @author Ricardo Silva
 */

public final class MimBuild {
    public static final String LATENT_PREFIX = "_L";

    private List<Node> latents;
    private IndTestMimBuild indTest;
    private Knowledge knowledge;

    public MimBuild(IndTestMimBuild indTest, Knowledge knowledge) {
        this.latents = new ArrayList<Node>();
        this.indTest = indTest;
        this.knowledge = knowledge;
    }

    public Graph search() {
        if (getIndTest() == null) {
            throw new NullPointerException();
        }

        if (getIndTest().getAlgorithmType() ==
                IndTestMimBuild.MIMBUILD_GES_ABIC || getIndTest()
                .getAlgorithmType() == IndTestMimBuild.MIMBUILD_GES_SBIC) {
            return mimBuildGesSearch();
        }
        else {
            return mimBuildPcSearch();
        }
    }

    public static String[] getTestDescriptions() {
        String tests[] = new String[2];
        tests[0] = "Gaussian maximum likelihood";
        tests[1] = "Two-stage least squares";
        return tests;
    }

    public static String[] getAlgorithmDescriptions() {
        String labels[] = new String[2];
        labels[0] = "GES";
        labels[1] = "PC Search";
        return labels;
    }

    public static List<String> generateLatentNames(int total) {
        List<String> output = new ArrayList<String>();
        for (int i = 0; i < total; i++) {
            output.add(LATENT_PREFIX + (i + 1));
        }
        return output;
    }

    /**
     * Perform MIMBuild with PC Search and Peter's independence test
     */
    private Graph mimBuildPcSearch() {
        Graph graph = new EdgeListGraph(getIndTest().getVariableList());
        startMeasurementModel(graph);
        MimAdjacencySearch adj = new MimAdjacencySearch(graph, getIndTest(),
                getKnowledge(), this.latents);
        SepsetMap sepset = adj.adjSearch();

        //Create new graph composed only of the edges among latent variables
        Graph latent_graph = new EdgeListGraph(this.latents);

        for (Node current : this.latents) {
            for (Node ad_node : graph.getAdjacentNodes(current)) {
                if (this.latents.contains(ad_node)) {
                    latent_graph.setEndpoint(ad_node, current, Endpoint.TAIL);
                }
            }
        }

//        SearchGraphUtils.pcOrient(sepset, getKnowledge(), latent_graph,
        TetradLogger.getInstance().info("Starting PC Orientation.");

        SearchGraphUtils.pcOrientbk(getKnowledge(), latent_graph, graph.getNodes());
        SearchGraphUtils.orientCollidersUsingSepsets(sepset, getKnowledge(), latent_graph);
        MeekRules rules = new MeekRules();
        rules.setKnowledge(getKnowledge());
        rules.orientImplied(latent_graph);

        TetradLogger.getInstance().info("Finishing PC Orientation");

        //Put the orientations back to 'graph'
        for (Node current : this.latents) {
            for (Node ad_node : latent_graph.getNodesInTo(current,
                    Endpoint.ARROW)) {
                graph.setEndpoint(ad_node, current, Endpoint.ARROW);
            }
        }

        return graph;
    }

    /**
     * Perform MIMBuild with GES search and BIC score.
     */
    private Graph mimBuildGesSearch() {
        double score, newScore;

        // Form the graph over measured and latent variables with a directed
        // edge from each latent to each of its measurements and a complete
        // undirected graph over the latents.
        Graph graph = new EdgeListGraph(getIndTest().getVariableList());
        startMeasurementModel(graph);

        // Make a list of all of the variables names in this graph plus all
        // of the latent variable names in this graph.
        String varNames[] = new String[graph.getNumNodes()];
        for (int i = 0; i < varNames.length; i++) {
            varNames[i] = graph.getNodes().get(i).toString();
        }
        String latentVarNames[] = new String[this.latents.size()];
        for (int i = 0; i < this.latents.size(); i++) {
            latentVarNames[i] = this.latents.get(i).toString();
        }

        // This is the covariance matrix over the measured variables.
        CovarianceMatrix covMatrix = getIndTest().getCovMatrix();

        // Get a DAG in the graph 'graph', respecting knowledge.
        DagInPatternIterator iterator = new DagInPatternIterator(graph);
        iterator.setKnowledge(getKnowledge());
        graph = iterator.next();
//        SearchGraphUtils.pdagToDag(graph);

        SemOptimizerEm optimizer = new SemOptimizerEm();
        SemEstimator estimator = new SemEstimator(covMatrix, new SemPm(graph),
                optimizer);
        estimator.estimate();
        newScore = scoreModel(estimator.getEstimatedSem());

        do {
            CovarianceMatrix expectedCovarianceMatrix = new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), new DenseDoubleMatrix2D(optimizer.getExpectedCovarianceMatrix()),
                    covMatrix.getSampleSize());
            CovarianceMatrix newCovMatrix = expectedCovarianceMatrix
                    .getSubmatrix(latentVarNames);
            score = newScore;
            Ges ges = new Ges(newCovMatrix);
            ges.setKnowledge(getKnowledge());
            Graph newStructuralModel = ges.search();
            Graph directedStructuralModel = new EdgeListGraph(
                    newStructuralModel);

            // Get a DAG in the structure model pattern.
//            iterator = new DagInPatternIterator(directedStructuralModel, getKnowledge());
//            directedStructuralModel = iterator.next();

            SearchGraphUtils.pdagToDag(directedStructuralModel);

            Graph newCandidate = getUpdatedGraph(graph,
                    directedStructuralModel);
            estimator = new SemEstimator(covMatrix, new SemPm(newCandidate),
                    optimizer);
            estimator.estimate();
            newScore = scoreModel(estimator.getEstimatedSem());
            if (newScore > score) {
                graph = getUpdatedGraph(graph, newStructuralModel);
            }
        } while (newScore > score);
        
        System.out.println("Yes, I got here!!!");
        System.out.println(graph);

        return graph;
    }

    private Graph getUpdatedGraph(Graph graph, Graph structuralModel) {
        Graph output = new EdgeListGraph(graph);
        List<Edge> edgesToRemove = new ArrayList<Edge>();
        for (Edge nextEdge : output.getEdges()) {
            if (nextEdge.getNode1().getNodeType() == NodeType.LATENT &&
                    nextEdge.getNode2().getNodeType() == NodeType.LATENT) {
                edgesToRemove.add(nextEdge);
            }
        }
        output.removeEdges(edgesToRemove);
        for (Edge nextEdge : structuralModel.getEdges()) {
            Node node1 = output.getNode(nextEdge.getNode1().toString());
            Node node2 = output.getNode(nextEdge.getNode2().toString());
            output.setEndpoint(node2, node1, nextEdge.getEndpoint1());
            output.setEndpoint(node1, node2, nextEdge.getEndpoint2());
        }
        return output;
    }

    /**
     * BIC score
     */
    private double scoreModel(SemIm semIm) {
        double fml = semIm.getFml();
        int freeParams = semIm.getNumFreeParams();
        int sampleSize = semIm.getSampleSize();

        return -fml - (freeParams * Math.log(sampleSize));
    }

    /**
     * Initialize the measurement model. It will look at the Knowledge object
     * and get information about which edges are required. Nodes that lie at the
     * tails of edges are considered to be latent and added to the latents
     * list.
     */
    private void startMeasurementModel(Graph graph) {

        // Add the arrows from latents to measured variables according to
        // specific background knowledge included on the independence checker.

        Iterator<KnowledgeEdge> it = getIndTest().getMeasurements().requiredEdgesIterator();

        while (it.hasNext()) {
            KnowledgeEdge temp = it.next();

            String xname = temp.getFrom();
            String yname = temp.getTo();

            Node x = graph.getNode(xname);
            Node y = graph.getNode(yname);

            graph.setEndpoint(x, y, Endpoint.ARROW);
            graph.setEndpoint(y, x, Endpoint.TAIL);

            if (latents.indexOf(x) == -1) {
                latents.add(x);
                x.setNodeType(NodeType.LATENT);
            }
        }

        /**
         * Now connect latent variables according
         */

        int size = latents.size();
        Iterator<Node> itl = latents.iterator();
        Node[] nodes = new Node[size];
        int count = 0;
        while (itl.hasNext()) {
            nodes[count++] = itl.next();
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                graph.setEndpoint(nodes[i], nodes[j], Endpoint.TAIL);
                graph.setEndpoint(nodes[j], nodes[i], Endpoint.TAIL);
            }
        }
    }

    private IndTestMimBuild getIndTest() {
        return indTest;
    }

    private Knowledge getKnowledge() {
        return knowledge;
    }
}