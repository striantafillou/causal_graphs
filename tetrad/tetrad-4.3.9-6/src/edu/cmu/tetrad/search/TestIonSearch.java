package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestCramerT;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestIonSearch extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestIonSearch(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
        TetradLogger.getInstance().setLogging(true);
    }


    public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    /**
     * Tests the example trace used in the grant write up for the Ion Algo.
     */


    public void testExampleTrace1() {

        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");

        Graph pag1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        pag1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.CIRCLE));
        pag1.addEdge(new Edge(Y, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        pag1.addUnderlineTriple(X, Y, Z);

        Graph pag2 = new EdgeListGraph(Arrays.asList(X, W, Z));
        pag2.addEdge(new Edge(X, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        pag2.addEdge(new Edge(W, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        pag2.addUnderlineTriple(X, W, Z);

        Ion search = new Ion(Arrays.asList(pag1, (Graph) pag2));

        List<Graph> pags = search.search();
        Set<Graph> outputPags = new HashSet<Graph>();
        for (Graph graph : pags) {
            outputPags.add(graph);
        }
        System.out.println(pags);

        Graph O1 = new EdgeListGraph(Arrays.asList(X, Y, W, Z));
        O1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addEdge(new Edge(Z, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addUnderlineTriple(Y, W, Z);
        O1.addUnderlineTriple(X, Y, W);

        Graph O2 = new EdgeListGraph(Arrays.asList(X, W, Y, Z));
        O2.addEdge(new Edge(X, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addEdge(new Edge(Y, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addUnderlineTriple(X, W, Y);
        O2.addUnderlineTriple(W, Y, Z);

        Set<Graph> expectedPags = new HashSet<Graph>();
        expectedPags.add(O2);
        expectedPags.add(O1);

        boolean containsExpected  = true;
        for (Graph expectedPag : expectedPags) {
            if (!outputPags.contains(expectedPag))
                containsExpected = false;
        }
        System.out.println(containsExpected);
        System.out.println(expectedPags.size());
        System.out.println(outputPags.size());

        System.out.println(outputPags);

        assertTrue(containsExpected&&expectedPags.size()==outputPags.size());

    }


    public void testExampleTrace2() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");

        Graph G1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Z, Y, Endpoint.CIRCLE, Endpoint.ARROW));

        Graph G2 = new EdgeListGraph(Arrays.asList(Y, W));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph) G2));

        search.search();

    }

    public void testExampleTrace3() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Graph G1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Graph G2 = new EdgeListGraph(Arrays.asList(Y, W, T));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph) G2));

        search.search();
        System.out.println(search.getStats());
    }

    public void testExampleTrace3b() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Graph G1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Graph G2 = new EdgeListGraph(Arrays.asList(Y, W, T));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph) G2));
        search.setPathLengthSearch(true);
        search.search();
        System.out.println(search.getStats());
    }

    public void testExampleTrace3c() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Graph G1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Graph G2 = new EdgeListGraph(Arrays.asList(Y, W, T));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph) G2));
        search.setPathLengthSearch(true);
        search.setAdjacencySearch(false);
        search.search();
        System.out.println(search.getStats());
    }

    public void testExampleTrace3d() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Graph G1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Graph G2 = new EdgeListGraph(Arrays.asList(Y, W, T));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph) G2));
        search.setAdjacencySearch(false);
        search.search();
        System.out.println(search.getStats());
    }

    public void testError() {
        Node X1 = new ContinuousVariable("X1");
        Node X2 = new ContinuousVariable("X2");
        Node X3 = new ContinuousVariable("X3");
        Node X4 = new ContinuousVariable("X4");
        Node X5 = new ContinuousVariable("X5");

        Graph pag1 = new EdgeListGraph(Arrays.asList(X3, X2, X4, X5));
        pag1.addEdge(new Edge(X4, X2, Endpoint.CIRCLE, Endpoint.ARROW));
        pag1.addEdge(new Edge(X5, X2, Endpoint.CIRCLE, Endpoint.ARROW));
        pag1.addEdge(new Edge(X3, X2, Endpoint.CIRCLE, Endpoint.ARROW));
        pag1.addEdge(new Edge(X3, X4, Endpoint.CIRCLE, Endpoint.CIRCLE));
        pag1.addEdge(new Edge(X5, X4, Endpoint.CIRCLE, Endpoint.CIRCLE));

        Graph pag2 = new EdgeListGraph(Arrays.asList(X3, X1, X4, X5));
        pag2.addEdge(new Edge(X1, X4, Endpoint.CIRCLE, Endpoint.ARROW));
        pag2.addEdge(new Edge(X3, X4, Endpoint.CIRCLE, Endpoint.ARROW));
        pag2.addEdge(new Edge(X4, X5, Endpoint.TAIL, Endpoint.ARROW));


        IonSearch search = new IonSearch(Arrays.asList(pag1, (Graph) pag2));

        System.out.println(search.search());

    }

    public void testExampleCompare() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Graph pag1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        pag1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        pag1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Graph pag2 = new EdgeListGraph(Arrays.asList(Y, W, T));
        pag2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        pag2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(pag1, (Graph) pag2));
        List<Graph> search1 = new ArrayList<Graph>();
        search1 = search.search();
        System.out.println(search.getStats());
        search.setPathLengthSearch(true);
        List<Graph> search3 = new ArrayList<Graph>();
        search3 = search.search();
        System.out.println(search.getStats());
        List<Graph> search2 = new ArrayList<Graph>();
        search.setAdjacencySearch(false);
        search2 = search.search();
        System.out.println(search.getStats());
    }

    public void testExampleTrace4() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node Z = new ContinuousVariable("Z");
        Node W = new ContinuousVariable("W");
        Graph trueGraph = new EdgeListGraph(Arrays.asList(X, Y, Z, W));
        trueGraph.addEdge(new Edge(W, X, Endpoint.TAIL, Endpoint.ARROW));
        trueGraph.addEdge(new Edge(W, Z, Endpoint.TAIL, Endpoint.ARROW));
        trueGraph.addEdge(new Edge(X, Y, Endpoint.TAIL, Endpoint.ARROW));
        trueGraph.addEdge(new Edge(Z, Y, Endpoint.TAIL, Endpoint.ARROW));
        Fci fci1 = new Fci(new IndTestDSep(trueGraph), Arrays.asList(W, X, Y));
        Graph pag1 = fci1.search();
        Fci fci2 = new Fci(new IndTestDSep(trueGraph), Arrays.asList(W, Z, Y));
        Graph pag2 = fci2.search();
        List<Graph> list = new ArrayList<Graph>();
        list.add(pag1);
        list.add(pag2);
        IonSearch search = new IonSearch(list);
        search.search();
    }

    public void testExampleTraceCycle() {
        Node A = new ContinuousVariable("X1");
        Node B = new ContinuousVariable("X2");
        Node C = new ContinuousVariable("X3");
        Node D = new ContinuousVariable("X4");

        Graph G1 = new EdgeListGraph(Arrays.asList(A, C, D));
        G1.addEdge(new Edge(A, C, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G1.addEdge(new Edge(C, D, Endpoint.CIRCLE, Endpoint.CIRCLE));

        Graph G2 = new EdgeListGraph(Arrays.asList(B, C, D));
        G2.addEdge(new Edge(B, D, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G2.addEdge(new Edge(D, C, Endpoint.CIRCLE, Endpoint.CIRCLE));

        Graph G3 = new EdgeListGraph(Arrays.asList(A, B, C));
        G3.addEdge(new Edge(A, B, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G3.addEdge(new Edge(A, C, Endpoint.CIRCLE, Endpoint.CIRCLE));

        List<Graph> graphs = new ArrayList<Graph>();
        graphs.add(G1);
        graphs.add(G2);
        graphs.add(G3);

        IonSearch search = new IonSearch(graphs);

        List<Graph> pags = search.search();
    }


    public void rtestIonSearchOnData() {
        DataSet dataSet1 = null;
        DataSet dataSet2 = null;
        try {
            DataReader reader = new DataReader();
            dataSet1 = reader.parseTabular(new File("test_data/TestIonL.txt"));
            dataSet2 = reader.parseTabular(new File("test_data/TestIonR.txt"));
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        List<Graph> pags = new LinkedList<Graph>();

        IndependenceTest independenceTest = new IndTestCramerT(dataSet1, .05);
        Fci fci = new Fci(independenceTest);
        pags.add(fci.search());

        independenceTest = new IndTestCramerT(dataSet2, .05);
        fci = new Fci(independenceTest);
        pags.add(fci.search());

        IonSearch search = new IonSearch(pags);
        search.search();
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestIonSearch.class);
    }
}
