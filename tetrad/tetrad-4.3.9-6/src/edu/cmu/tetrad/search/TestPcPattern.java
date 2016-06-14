package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndTestFisherZ;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestPcPattern extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestPcPattern(String name) {
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
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        checkWithKnowledge("A-->B,C-->B,B-->D", "A-->B,C-->B,D",
                knowledge);
    }

    public void compareToPc() {
        int success = 0;
        int fail = 0;
        int totBidirected = 0;
        int numCyclic = 0;

        for (int i = 0; i < 100; i++) {
            TetradLogger.getInstance().log("info", "# " + (i + 1));

            try {
                Graph graph = GraphUtils.randomDag(20, 0, 20, 4, 4, 4, false);
                SemPm pm = new SemPm(graph);
                SemIm im = new SemIm(pm);
                DataSet dataSet = im.simulateData(1000, false);
                IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);
//                PcSearch search = new PcSearch(test, new Knowledge());
                PcPattern search = new PcPattern(test);
                Graph graph2 = search.search();

                int numBidirected = 0;

                for (Edge edge : graph2.getEdges()) {
                    if (Edges.isBidirectedEdge(edge)) {
                        numBidirected++;
                    }
                }

                totBidirected += numBidirected;

                if (graph2.existsDirectedCycle()) {
                    numCyclic++;
                    TetradLogger.getInstance().log("info", "Cyclic pattern!");
                }

                new PatternToDag(new Pattern(graph2)).patternToDagMeekRules();
                success++;
            }
            catch (IllegalArgumentException e) {
                fail++;
            }
        }

        TetradLogger.getInstance().log("info", "success = " + success + " fail = " + fail);
        TetradLogger.getInstance().log("info", "Total bidirected edges = " + totBidirected + " num cyclic = " + numCyclic);
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
        Pcd pc = new Pcd(independence);

        // Run search
        Graph resultGraph = pc.search();

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

        // Set up search.
        IndependenceTest independence = new IndTestDSep(graph);
        PcPattern pcSearch = new PcPattern(independence);
        pcSearch.setKnowledge(knowledge);

        // Run search
        Graph resultGraph = pcSearch.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nKnowldge:");
        System.out.println(knowledge);
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
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPcPattern.class);
    }
}
