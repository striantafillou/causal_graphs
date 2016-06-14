package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rtillman
 * Date: Aug 17, 2008
 * Time: 5:17:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestDci extends TestCase {

    public void testExampleTrace2() {
        Node X1 = new ContinuousVariable("X1");
        Node X2 = new ContinuousVariable("X2");
        Node X3 = new ContinuousVariable("X3");
        Node X4 = new ContinuousVariable("X4");
        Node X5 = new ContinuousVariable("X5");
        Node X6 = new ContinuousVariable("X6");
        Node X7 = new ContinuousVariable("X7");
        Node X8 = new ContinuousVariable("X8");
        Node X9 = new ContinuousVariable("X9");
        Node X10 = new ContinuousVariable("X10");

        Graph G1 = new EdgeListGraph(Arrays.asList(X2,X3,X4,X5,X7,X8,X9,X10));
        G1.addEdge(new Edge(X2, X10, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(X5, X4, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(X7, X9, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(X8, X9, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(X3, X10, Endpoint.ARROW, Endpoint.ARROW));
        G1.addEdge(new Edge(X3, X4, Endpoint.ARROW, Endpoint.ARROW));
        G1.addEdge(new Edge(X4, X9, Endpoint.ARROW, Endpoint.ARROW));

        Graph G2 = new EdgeListGraph(Arrays.asList(X1,X2,X3,X4,X5,X6,X8,X9,X10));
        G2.addEdge(new Edge(X2, X10, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(X5, X4, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(X8, X9, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(X3, X10, Endpoint.ARROW, Endpoint.ARROW));
        G2.addEdge(new Edge(X3, X4, Endpoint.ARROW, Endpoint.ARROW));
        G2.addEdge(new Edge(X4, X9, Endpoint.ARROW, Endpoint.ARROW));
        G2.addEdge(new Edge(X6, X1, Endpoint.CIRCLE, Endpoint.CIRCLE));

        List<IndependenceTest> tests = new ArrayList<IndependenceTest>();
        tests.add(new IndTestDSep(G1));
        tests.add(new IndTestDSep(G2));

        Dci search = new Dci(tests);

        System.out.println(search.search());

    }
}
