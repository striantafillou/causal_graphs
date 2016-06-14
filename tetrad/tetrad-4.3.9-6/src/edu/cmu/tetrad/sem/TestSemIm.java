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

package edu.cmu.tetrad.sem;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.MatrixUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the MeasurementSimulator class using diagnostics devised by Richard
 * Scheines. The diagnostics are described in the Javadocs, below.
 *
 * @author Joseph Ramsey
 */
public class TestSemIm extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSemIm(String name) {
        super(name);
    }

    /**
     * Tests whether the the correlation matrix of a simulated sample is close
     * to the implied covariance matrix.
     */
    public void rtestSampleVsImpliedCorrlelations() {
        Graph randomGraph = GraphUtils.randomDagC(5, 0, 8);
        SemPm semPm1 = new SemPm(randomGraph);
        SemIm semIm1 = new SemIm(semPm1);
		System.out.println("semPm1 = " + semPm1);
        System.out.println("semIm1 = " + semIm1);

        DataSet DataSet = semIm1.simulateData(1000, false);
        SemEstimator semEstimator = new SemEstimator(DataSet, semPm1);
        semEstimator.estimate();
        CovarianceMatrix covMatrix = new CovarianceMatrix(DataSet);
        System.out.println("covMatrix = " + covMatrix);
        DoubleMatrix2D implCovarC =
                semEstimator.getEstimatedSem().getImplCovar();
        double[][] implCovar = implCovarC.toArray();
        System.out.println("Implied covariance matrix:");
        System.out.println(MatrixUtils.toString(implCovar));
    }

    public void rtest2() {
        Graph graph = constructGraph1();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        System.out.println(semIm);

        Node x1 = graph.getNode("X1");
        Node x2 = graph.getNode("X2");
        semIm.setEdgeCoef(x1, x2, 100.0);
        assertEquals(100.0, semIm.getEdgeCoef(x1, x2));

        semIm.setErrCovar(x1, x1, 25.0);
        assertEquals(25.0, semIm.getErrCovar(x1));
    }

    public void test3() {
        Graph graph = constructGraph1();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        System.out.println("Original SemIm: " + semIm);

        DataSet dataSetContColumnContinuous =
                semIm.simulateData(500, false);
        CovarianceMatrix covMatrix =
                new CovarianceMatrix(dataSetContColumnContinuous);
        SemEstimator estimator2 = new SemEstimator(covMatrix, semPm);
        estimator2.estimate();
        SemIm semIm2 = estimator2.getEstimatedSem();

        System.out.println("\nEstimated Sem #1: " + semIm2);

        SemEstimator estimator3 = new SemEstimator(covMatrix, semPm);
        estimator3.estimate();
        SemIm semIm3 = estimator3.getEstimatedSem();

        System.out.println("\nEstimated Sem #2: " + semIm3);

        SemPm semPm4 = new SemPm(graph);
        SemEstimator estimator4 = new SemEstimator(covMatrix, semPm4);
        estimator4.estimate();
        SemIm semIm4 = estimator4.getEstimatedSem();

        System.out.println("\nEstimated Sem #3: " + semIm4);

        SemPm semPm5 = new SemPm(graph);
        SemEstimator estimator5 = new SemEstimator(covMatrix, semPm5);
        estimator5.estimate();
        SemIm semIm5 = estimator5.getEstimatedSem();

        System.out.println("\nEstimated Sem #4: " + semIm5);
    }

    public void testCovariancesOfSimulated() {
        Graph randomGraph = GraphUtils.randomDagC(5, 0, 8);
        SemPm semPm1 = new SemPm(randomGraph);
        SemIm semIm1 = new SemIm(semPm1);

        DoubleMatrix2D implCovarC = semIm1.getImplCovar();
        double[][] impliedCovar = implCovarC.toArray();
        System.out.println("Implied covar of semIm = " +
                MatrixUtils.toString(impliedCovar));

        DataSet DataSet = semIm1.simulateData(1000, false);
        CovarianceMatrix covMatrix = new CovarianceMatrix(DataSet);
        System.out.println(
                "Covariance matrix of simulated data = " + covMatrix);
    }

    public void testIntercepts() {
        Graph randomGraph = GraphUtils.randomDagC(5, 0, 8);
        SemPm semPm = new SemPm(randomGraph);
        SemIm semIm = new SemIm(semPm);

        printIntercepts(semIm);
        semIm.setIntercept(semIm.getVariableNodes().get(0), 1.0);
        printIntercepts(semIm);
        semIm.setIntercept(semIm.getVariableNodes().get(1), 3.0);
        printIntercepts(semIm);
        semIm.setIntercept(semIm.getVariableNodes().get(2), -1.0);
        printIntercepts(semIm);
        semIm.setIntercept(semIm.getVariableNodes().get(3), 6.0);
        printIntercepts(semIm);

        assertEquals(1.0, semIm.getIntercept(semIm.getVariableNodes().get(0)));
        assertEquals(3.0, semIm.getIntercept(semIm.getVariableNodes().get(1)));
        assertEquals(-1.0, semIm.getIntercept(semIm.getVariableNodes().get(2)));
        assertEquals(6.0, semIm.getIntercept(semIm.getVariableNodes().get(3)));
        assertEquals(0.0, semIm.getIntercept(semIm.getVariableNodes().get(4)));

        System.out.println(semIm);
    }

    private void printIntercepts(SemIm semIm) {
        System.out.println();
        for (int i = 0; i < 5; i++) {
            Node node = semIm.getVariableNodes().get(i);
            System.out.println("Intercept of " + node + " = " + semIm.getIntercept(node));
        }
    }

    /**
     * The Cholesky decomposition of a symmetric, positive definite matrix
     * multiplied by the transpose of the Cholesky decomposition should be equal
     * to the original matrix itself.
     */
    public void testCholesky() {
        Graph graph = constructGraph2();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        System.out.println("Original SemIm: " + semIm);

        DataSet dataSet = semIm.simulateData(500, false);

        DoubleMatrix2D data = dataSet.getDoubleData();

        System.out.println("Data = ");
        System.out.println(data);

        double[][] a = new double[data.columns()][data.columns()];

        for (int i = 0; i < data.columns(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                DoubleArrayList icol =
                        new DoubleArrayList(data.viewColumn(i).toArray());
                DoubleArrayList jcol =
                        new DoubleArrayList(data.viewColumn(j).toArray());
                a[i][j] = Descriptive.covariance(icol, jcol);
            }
        }

        System.out.println("A = ");
        System.out.println(MatrixUtils.toString(a));

        System.out.println("L = ");
        double[][] l = MatrixUtils.cholesky(a);
        System.out.println(MatrixUtils.toString(l));

        System.out.println("L' = ");
        double[][] lT = MatrixUtils.transpose(l);
        System.out.println(MatrixUtils.toString(lT));

        System.out.println("L L' = ");
        double[][] product = MatrixUtils.product(l, lT);
        System.out.println(MatrixUtils.toString(product));

        assertTrue(MatrixUtils.equals(a, product, 1.e-10));
    }

    private Graph constructGraph1() {
        Graph graph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");

        x1.setNodeType(NodeType.LATENT);
        x2.setNodeType(NodeType.LATENT);

        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x1, x4);
        graph.addDirectedEdge(x4, x5);

        return graph;
    }

    private Graph constructGraph2() {
        Graph graph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");

        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x1, x4);
        graph.addDirectedEdge(x4, x5);

        return graph;
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestSemIm.class);
    }
}


