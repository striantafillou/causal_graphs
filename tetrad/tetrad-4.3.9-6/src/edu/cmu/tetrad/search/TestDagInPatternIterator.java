package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestDagInPatternIterator extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestDagInPatternIterator(String name) {
        super(name);
    }

    public void test1() {
        Dag dag = GraphUtils.randomDag(10, 0, 10, 3, 3, 3, false);
        
        System.out.println("DAG " + dag);

        Graph pattern = SearchGraphUtils.patternFromDag(dag);

        System.out.println("Pattern " + pattern);

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

    }

    public void test2() {
        Graph pattern = new EdgeListGraph();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        pattern.addNode(x);
        pattern.addNode(y);
        pattern.addDirectedEdge(x, y);

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    public void test3() {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);

        Graph pattern = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");
        Node x6 = new GraphNode("X6");

        pattern.addNode(x1);
        pattern.addNode(x2);
        pattern.addNode(x3);
        pattern.addNode(x4);
        pattern.addNode(x5);
        pattern.addNode(x6);

        pattern.addDirectedEdge(x5, x1);
        pattern.addDirectedEdge(x3, x1);
        pattern.addDirectedEdge(x3, x4);
        pattern.addDirectedEdge(x6, x5);
        pattern.addUndirectedEdge(x1, x6);
        pattern.addUndirectedEdge(x4, x6);

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    public void test4() {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);

        Graph pattern = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");
        Node x6 = new GraphNode("X6");

        pattern.addNode(x1);
        pattern.addNode(x2);
        pattern.addNode(x3);
        pattern.addNode(x4);
        pattern.addNode(x5);
        pattern.addNode(x6);

        pattern.addDirectedEdge(x5, x1);
        pattern.addDirectedEdge(x3, x1);
        pattern.addDirectedEdge(x3, x4);
        pattern.addDirectedEdge(x6, x5);
        pattern.addUndirectedEdge(x1, x6);
        pattern.addUndirectedEdge(x4, x6);

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestDagInPatternIterator.class);
    }
}
