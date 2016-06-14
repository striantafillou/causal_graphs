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

package edu.cmu.tetrad.graph;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.PermutationGenerator;
import edu.cmu.tetrad.search.indtest.SearchLogUtils;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.Pc;
import edu.cmu.tetrad.search.SearchGraphUtils;

/**
 * Tests the functions of EndpointMatrixGraph and EdgeListGraph through the
 * Graph interface.
 *
 * @author Joseph Ramsey
 */
public final class TestEdgeListGraph extends TestCase {
    private Node x1, x2, x3, x4, x5;
    private Graph graph;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestEdgeListGraph(String name) {
        super(name);
    }

    public void setUp() {
        x1 = new GraphNode("x1");
        x2 = new GraphNode("x2");
        x3 = new GraphNode("x3");
        x4 = new GraphNode("x4");
        x5 = new GraphNode("x5");
        graph = new EdgeListGraph();
        //        graph = new EndpointMatrixGraph();
    }

    public void testSequence1() {
        graph.clear();

        // Add and remove some nodes.
        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);

        List<Node> children = graph.getChildren(x1);
        List<Node> parents = graph.getParents(x4);

        assertEquals(children, Collections.singletonList(x2));
        assertEquals(parents, Collections.singletonList(x3));

        assertTrue(graph.isDConnectedTo(x1, x3, Collections.EMPTY_LIST));
        graph.removeNode(x2);

        // No cycles.
        assertTrue(!graph.existsDirectedCycle());

        // Copy the graph.
        Graph graph2 = new EdgeListGraph(graph);
        assertEquals(graph, graph2);

        Graph graph3 = new EdgeListGraph(graph);
        assertEquals(graph, graph3);
    }

    public void testSequence2() {
        graph.clear();

        // Add some edges in a cycle.
        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        assertTrue(!graph.existsDirectedCycle());

        graph.addDirectedEdge(x1, x3);

        try {
            graph.addDirectedEdge(x1, x3);
            fail("Shouldn't have been able to add an edge already in the graph.");
        }
        catch (IllegalArgumentException e) {
            // Ignore.
        }

        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x4, x1);
        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x5);
        graph.addDirectedEdge(x5, x2);

        System.out.println(graph);

        assertTrue(graph.existsDirectedCycle());

        graph.removeEdge(x1, x3);
        graph.removeEdge(graph.getEdge(x3, x4));

        System.out.println(graph);
    }

    /**
     * Tests to see if d separation facts are symmetric.
     */
    public void testDSeparation() {
        Graph graph = GraphUtils.randomDag(20, 0, 30, 3, 3, 3, false);
        List<Node> nodes = graph.getNodes();

        int depth = 2;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                List<Node> theRest = new ArrayList<Node>(nodes);
                theRest.remove(x);
                theRest.remove(y);

                DepthChoiceGenerator gen = new DepthChoiceGenerator(theRest.size(), depth);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    List<Node> z = new LinkedList<Node>();

                    for (int k = 0; k < choice.length; k++) {
                        z.add(theRest.get(choice[k]));
                    }

                    if (graph.isDSeparatedFrom(x, y, z) != graph.isDSeparatedFrom(y, x, z)) {
                        fail(SearchLogUtils.independenceFact(x, y, z) + " should have same d-sep result as " +
                                SearchLogUtils.independenceFact(y, x, z));
                    }
                }

            }
        }
    }

    /**
     * Tests to see if running PC on a graph using d separation returns the pattern of that graph.
     */
    public void testDSeparation2() {
        Graph graph = GraphUtils.randomDag(20, 0, 30, 3, 3, 3, false);
        IndTestDSep test = new IndTestDSep(graph);
        Pc pc = new Pc(test);
        Graph pattern = pc.search();
        Graph pattern2 = SearchGraphUtils.patternFromDag(pattern);
        assertEquals(pattern, pattern2);
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestEdgeListGraph.class);
    }
}


