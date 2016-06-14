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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphConverter;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestGes extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGes(String name) {
        super(name);
    }

//    public void setUp() throws Exception {
//        TetradLogger.getInstance().addOutputStream(System.out);
//        TetradLogger.getInstance().setForceLog(true);
//    }
//
//
//    public void tearDown() {
//        TetradLogger.getInstance().setForceLog(false);
//        TetradLogger.getInstance().removeOutputStream(System.out);
//    }


    public void testBlank() {
        // Blank to keep the automatic JUnit runner happy.
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void rtestSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void rtestSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void rtestSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A---B,C---A,B-->C,C-->D,A-->D",
                knowledge);
    }

    public void testSearch3_5() {
        Dag dag = GraphUtils.randomDag(5, 0, 5, 5, 5, 5, false);
        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(1000, false);

        Ges ges = new Ges(dataSet);
        Graph graph = ges.search();

        System.out.println(graph);
    }

    public void testSearch4() {
        int numVars = 40;
        int numEdges = numVars;
        int sampleSize = 200;
        boolean latentDataSaved = false;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm pm = new SemPm(trueGraph);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(sampleSize, false);

//        BayesPm bayesPm = new BayesPm(trueGraph);
//        MlBayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
//        DataSet dataSet = bayesIm.simulateData(sampleSize, latentDataSaved);

        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(trueGraph);
//        ges.setStructurePrior(0.1);
//        ges.setSamplePrior(10);

        // Run search
        Graph pattern = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println("\nResult graph:");
        System.out.println(pattern);

        int adjFp = GraphUtils.countAdjErrors(pattern, trueGraph);
        int adjFn = GraphUtils.countAdjErrors(trueGraph, pattern);

        System.out.println("adj fp = " + adjFp + " adjFn = " + adjFn);
    }

    /**
     * Iterated so I can collect stats.
     */
    public void testSearch4a() {
        int numVars = 30;


        int numEdges = numVars;
        int sampleSize = 1000;
        int numIterations = 10;

        double sumFp = 0.0;
        double sumFn = 0.0;

        NumberFormat nf = new DecimalFormat("0.00");
        System.out.println("\tADJ_FP\tADJ_FN");

        for (int count = 0; count < numIterations; count++) {
            Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                    5, false);

            SemPm pm = new SemPm(trueGraph);
            SemIm im = new SemIm(pm);
            DataSet dataSet = im.simulateData(sampleSize, false);

            Ges ges = new Ges(dataSet);
            ges.setTrueGraph(trueGraph);
            Graph pattern = ges.search();

            int adjFp = GraphUtils.countAdjErrors(pattern, trueGraph);
            int adjFn = GraphUtils.countAdjErrors(trueGraph, pattern);

            sumFp += adjFp;
            sumFn += adjFn;

            System.out.println((count + 1) + "\t" + adjFp + "\t" + adjFn);
        }

        double avgFp = sumFp / (double) numIterations;
        double avgFn = sumFn / (double) numIterations;

        System.out.println("Means" + "\t" + nf.format(avgFp) + "\t" + nf.format(avgFn));
    }

    public void testSearch5() {
        int numVars = 10;
        int numEdges = 20;
        int sampleSize = 20000;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

//        SemPm bayesPm = new SemPm(trueGraph);
//        SemIm bayesIm = new SemIm(bayesPm);

        System.out.println("********** SAMPLE SIZE = " + sampleSize);

//            RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);

//            BayesPm semPm = new BayesPm(trueGraph);
//            BayesIm bayesIm = new MlBayesIm(semPm, MlBayesIm.RANDOM);
//            DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        SemPm semPm = new SemPm(trueGraph);
        SemIm bayesIm = new SemIm(semPm);
        DataSet dataSet = bayesIm.simulateData(sampleSize, false);

        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(trueGraph);

        // Run search
        Graph resultGraph = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        DataSet dataSet = semIM.simulateData(500, false);

        // Set up search.
        Ges ges = new Ges(dataSet);
        ges.setTrueGraph(graph);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = ges.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkWithKnowledge(String inputGraph, String outputGraph,
                                    Knowledge knowledge) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        DataSet dataSet = semIM.simulateData(1000, false);

        // Set up search.
        Ges ges = new Ges(dataSet);
        ges.setKnowledge(knowledge);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = ges.search();

        // PrintUtil out problem and graphs.
        System.out.println(knowledge);
        System.out.println("Input graph:");
        System.out.println(graph);
        System.out.println("Result graph:");
        System.out.println(resultGraph);

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestGes.class);
    }
}


