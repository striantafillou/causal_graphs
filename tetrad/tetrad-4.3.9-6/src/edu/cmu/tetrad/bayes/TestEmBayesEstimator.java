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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Frank Wimberly
 */
public final class TestEmBayesEstimator extends TestCase {

    public TestEmBayesEstimator(String name) {
        super(name);
    }


    public void setUp(){
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    public void tearDown(){
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }


    public static void testEstimate1() {

        try {
            //String fileD = "c:/tetrad-4.2/test_data/embayes_l1x1x2x3MD.dat";
            String fileD = "test_data/embayes_l1x1x2x3MD.dat";
            File file = new File(fileD);

            DataSet dds;

            DataReader reader = new DataReader();
            reader.setMissingValueMarker("-99");
            dds = reader.parseTabular(file);

            List<Node> vars = dds.getVariables();
            System.out.println(vars);

            Dag graph = new Dag();
            Node L1 = new GraphNode("L1");
            Node X1 = new GraphNode("X1");
            Node X2 = new GraphNode("X2");
            Node X3 = new GraphNode("X3");

            L1.setNodeType(NodeType.LATENT);
            X1.setNodeType(NodeType.MEASURED);
            X2.setNodeType(NodeType.MEASURED);
            X3.setNodeType(NodeType.MEASURED);

            graph.addNode(L1);
            graph.addNode(X1);
            graph.addNode(X2);
            graph.addNode(X3);

            graph.addDirectedEdge(L1, X1);
            graph.addDirectedEdge(L1, X2);
            graph.addDirectedEdge(X1, X3);
            graph.addDirectedEdge(X2, X3);

            BayesPm bayesPm = new BayesPm(graph);
            bayesPm.setNumCategories(L1, 2);
            bayesPm.setNumCategories(X1, 2);
            bayesPm.setNumCategories(X2, 2);
            bayesPm.setNumCategories(X3, 2);

            EmBayesEstimator emb = new EmBayesEstimator(bayesPm, dds);
            emb.expectationOnly();

            double[][][] condProbs = emb.getCondProbs();

            assertEquals(0.4925, condProbs[3][0][0], 0.001);
            assertEquals(0.8977, condProbs[3][1][0], 0.001);
            assertEquals(0.6372, condProbs[3][2][0], 0.001);
            assertEquals(0.2154, condProbs[3][3][0], 0.001);

            System.out.println(emb.getEstimatedIm());
        }
        catch (IOException e) {
            e.printStackTrace(); 
        }

    }

    public void test2() {
        Dag graph = new Dag();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        
        x2.setNodeType(NodeType.LATENT);

        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);

        graph.addDirectedEdge(x2, x1);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x1, x3);

        BayesPm pm = new BayesPm(graph);
        MlBayesIm im = new MlBayesIm(pm, MlBayesIm.RANDOM);

        System.out.println(im);

        DataSet data = im.simulateData(1000, false);

        EmBayesEstimator estimator = new EmBayesEstimator(pm, data);
        estimator.expectationOnly();
        estimator.maximization(0.0001);

        System.out.println(estimator.getEstimatedIm());

    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.                                                                       7
        return new TestSuite(TestEmBayesEstimator.class);
    }
}


