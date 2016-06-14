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
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndTestFisherZ;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests the PC search.
 *
 * @author Joseph Ramsey
 */
public class TestPc extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestPc(String name) {
        super(name);
    }


    public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void testSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void testSearch2() {
        checkSearch2("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch3() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch4() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A-->B,C-->B,A-->D,C-->D",
                knowledge);
    }

    public void testSearch5() {
        for (int i = 0; i < 1; i++) {
            Graph graph = GraphUtils.randomDag(15, 0, 30, 3, 3, 3, false);
            
            System.out.println("True graph: " + graph);

            SemPm pm = new SemPm(graph);
            SemIm im = new SemIm(pm);
            DataSet dataSet = im.simulateData(1000, false);

            IndTestFisherZ test = new IndTestFisherZ(dataSet, 0.05);
            Pc2 pc = new Pc2(test);
            pc.setTrueGraph(graph);
            Graph pattern = pc.search();
            System.out.println(pattern);
        }
    }

    public void rtestShowInefficiency() {

        int numVars = 20;
        int numEdges = 20;
        int maxSample = 2000;
        int increment = 1;

        Dag trueGraph = GraphUtils.randomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm semPm = new SemPm(trueGraph);
        SemIm semIm = new SemIm(semPm);
        DataSet _dataSet = semIm.simulateData(maxSample, false);
        Graph previousResult = null;

        for (int n = 3; n <= maxSample; n += increment) {
            int[] rows = new int[n];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = i;
            }

            DataSet dataSet = _dataSet.subsetRows(rows);
            IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);

            Cpc search = new Cpc(test);
            Graph resultGraph = search.search();

            if (previousResult != null) {
                List<Edge> resultEdges = resultGraph.getEdges();
                List<Edge> previousEdges = previousResult.getEdges();

                List<Edge> addedEdges = new LinkedList<Edge>();

                for (Edge edge : resultEdges) {
                    if (!previousEdges.contains(edge)) {
                        addedEdges.add(edge);
                    }
                }

                List<Edge> removedEdges = new LinkedList<Edge>();

                for (Edge edge : previousEdges) {
                    if (!resultEdges.contains(edge)) {
                        removedEdges.add(edge);
                    }
                }

                if (!addedEdges.isEmpty() && !removedEdges.isEmpty()) {
                    System.out.println("\nn = " + n + ":");

                    if (!addedEdges.isEmpty()) {
                        System.out.println("Added: " + addedEdges);
                    }

                    if (!removedEdges.isEmpty()) {
                        System.out.println("Removed: " + removedEdges);
                    }
                }
            }

            previousResult = resultGraph;
        }

        System.out.println("Final graph = " + previousResult);
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);

        // Set up search.
        IndependenceTest independence = new IndTestDSep(graph);
        GraphSearch cpcMb = new Pc(independence);

        // Run search
        Graph resultGraph = cpcMb.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
        System.out.println("\nTrue graph:");
        System.out.println(trueGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkSearch2(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);

        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        DataSet dataSet = semIM.simulateData(1000, false);

        // Set up search.
//        IndependenceTest independence = new IndTestDSep(graph);
        IndependenceTest independence = new IndTestFisherZ(dataSet, 0.001);
//        IndependenceTest independence = new IndTestFisherZBootstrap(dataSet, 0.001, 15, 1000);

        GraphSearch pcSearch = new Pc(independence);
//        GraphSearch pcSearch = new Npc(independence, knowledge);

        // Run search
        Graph resultGraph = pcSearch.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
        System.out.println("\nTrue graph:");
        System.out.println(trueGraph);

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
//        IndependenceTest independence = new IndTestGraph(graph);
        IndependenceTest independence = new IndTestFisherZ(dataSet, 0.001);
        Pc pcSearch = new Pc(independence);
        pcSearch.setKnowledge(knowledge);

        // Run search
        Graph resultGraph = pcSearch.search();

        // Build comparison graph.
//        Graph trueGraph = GraphConverter.convert(outputGraph);
        GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
//        System.out.println("\nKnowledge:");
        System.out.println(knowledge);
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
//        System.out.println("\nTrue graph:");
//        System.out.println(trueGraph);

        // Do test.
//        assertTrue(resultGraph.equals(trueGraph));
    }

    public void test5() {
        Graph graph = GraphUtils.randomDag(20, 0, 20, 3, 2, 2, false);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);
        DataSet dataSet = im.simulateData(1000, false);
        IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);

//        Pc pc = new Pc(test);
//        pc.search();

        allAtDepth(test, 3);
    }

    private void allAtDepth(IndependenceTest test, int depth) {
//        System.out.println("depth = " + depth);
        List<Double> pValues = new ArrayList<Double>();

        List<Node> nodes = new LinkedList<Node>(test.getVariables());

        for (int d = 0; d <= depth; d++) {
            for (Node x : nodes) {

//            System.out.println("Adjacent nodes for " + x + " = " + b);
//            System.out.println("Depth = " + depth);

                for (Node y : nodes) {
                    if (x == y) continue;

                    ChoiceGenerator cg = new ChoiceGenerator(nodes.size(), d);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = SearchGraphUtils.asList(choice, nodes);

                        if (condSet.contains(x) || condSet.contains(y)) continue;

                        test.isIndependent(x, y, condSet);

                        pValues.add(test.getPValue());

                        double[] p = new double[pValues.size()];
                        for (int t = 0; t < p.length; t++) p[t] = pValues.get(t);

                        System.out.println("FDR cutoff = " + StatUtils.fdr(test.getAlpha(), p, false));
                    }
                }
            }
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPc.class);
    }
}


