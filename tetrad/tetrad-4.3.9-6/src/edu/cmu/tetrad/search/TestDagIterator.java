package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestDagIterator extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestDagIterator(String name) {
        super(name);
    }

    public void test1() {
//        Dag dag = GraphUtils.createRandomDag(6, 0, 6, 3, 3, 3, false);

        Dag dag = new Dag();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");
        Node w = new GraphNode("W");

        dag.addNode(x);
        dag.addNode(y);
        dag.addNode(z);
        dag.addNode(w);

        dag.addDirectedEdge(x, y);
        dag.addDirectedEdge(y, z);
        dag.addDirectedEdge(x, z);
        dag.addDirectedEdge(z, w);

        System.out.println("DAG " + dag);

//        Graph dag = SearchGraphUtils.patternFromDag(dag);
        Graph graph = GraphUtils.undirectedGraph(dag);

        for (Edge edge : graph.getEdges()) {
            graph.removeEdge(edge);
            graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
        }

        System.out.println("Pattern " + graph);

        DagIterator iterator = new DagIterator(graph);
//        DagIterator2 iterator = new DagIterator2(graph);
        int i = 0;

        while (iterator.hasNext()) {
            System.out.println("DAG # " + (++i) + iterator.next());
        }

    }

    public void test2() {
        Graph pattern = new EdgeListGraph();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        pattern.addNode(x);
        pattern.addNode(y);
        pattern.addDirectedEdge(x, y);

        DagIterator iterator = new DagIterator(pattern);

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    public void test3() {
        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");
        Node x6 = new GraphNode("X6");

        List<Node> nodes = new ArrayList<Node>();

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);
        nodes.add(x4);
//        nodes.add(x5);
//        nodes.add(x6);

        int minEdges = 0;
        int maxEdges = 3;

        DagIterator3 iterator = new DagIterator3(nodes, minEdges, maxEdges);
        int i = 0;

        while (iterator.hasNext()) {
            System.out.println("" + (++i) + iterator.next());
        }
    }

    public void test4() {
        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");

        List<Node> nodes = new ArrayList<Node>();

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);
        nodes.add(x4);

        DagIterator3 iterator = new DagIterator3(nodes, 0, 0);
        int i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 1);

        iterator = new DagIterator3(nodes, 1, 1);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 28);

        iterator = new DagIterator3(nodes, 2, 2);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 198);

        iterator = new DagIterator3(nodes, 3, 3);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 316);

        iterator = new DagIterator3(nodes, 0, 3);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 1 + 28 + 198 + 316);

        Node x5 = new GraphNode("X5");
        nodes.add(x5);

        iterator = new DagIterator3(nodes, 0, 0);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 1);

        iterator = new DagIterator3(nodes, 1, 1);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 75);

        iterator = new DagIterator3(nodes, 2, 2);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 10610);

        iterator = new DagIterator3(nodes, 3, 3);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 10710);

        iterator = new DagIterator3(nodes, 4, 4);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 16885);

        iterator = new DagIterator3(nodes, 0, 4);
        i = 0;

        while (iterator.hasNext()) {
            i++;
            iterator.next();
        }

        assert(i == 1 + 75 + 10610 + 10710 + 16885);

    }

    public void test5() {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);

        Graph pattern = new EdgeListGraph();

        GraphNode x1 = new GraphNode("X1");
        GraphNode x2 = new GraphNode("X2");
        GraphNode x3 = new GraphNode("X3");
        GraphNode x4 = new GraphNode("X4");
        GraphNode x5 = new GraphNode("X5");
        GraphNode x6 = new GraphNode("X6");

        pattern.addNode(x1);
        pattern.addNode(x2);
        pattern.addNode(x3);
        pattern.addNode(x4);
        pattern.addNode(x5);
        pattern.addNode(x6);

        pattern.addUndirectedEdge(x1, x2);
        pattern.addUndirectedEdge(x1, x3);
        pattern.addUndirectedEdge(x1, x4);
        pattern.addUndirectedEdge(x2, x5);
        pattern.addUndirectedEdge(x4, x5);
        pattern.addUndirectedEdge(x5, x6);

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);
        List<Graph> dags = new ArrayList<Graph>();

        while (iterator.hasNext()) {
            Graph graph = iterator.next();

            try {
                Dag dag = new Dag(graph);
                dags.add(dag);
            } catch (IllegalArgumentException e) {
                //
            }
        }

        for (int i = 0; i < dags.size(); i++) {
            System.out.println("DAG # " + (i + 1));
            System.out.println(dags.get(i));
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