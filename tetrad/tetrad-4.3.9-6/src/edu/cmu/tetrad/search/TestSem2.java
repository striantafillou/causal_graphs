package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.dist.Beta;
import edu.cmu.tetrad.util.dist.Uniform;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests Sem.
 *
 * @author Joseph Ramsey
 */
public class TestSem2 extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSem2(String name) {
        super(name);
    }


    //linear non-normal
    public void testSem2() {
        DataSet data = getData();

        System.out.println(data);

        GraphWithParameters result = new Shimizu2006SearchOld(0.05).lingamDiscovery_DAG(data);
        System.out.println(result);

        Graph graph = result.getGraph();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        for (Parameter parameter : semPm.getParameters()) {
            if (parameter.getType() == ParamType.COEF) {
                Node node1 = parameter.getNodeA();
                Node node2 = parameter.getNodeB();
                Edge edge = graph.getEdge(node1, node2);
                double weight = result.getWeightHash().get(edge);
                semIm.setEdgeCoef(node1, node2, weight);
            }
        }

        System.out.println(semIm);


        try {
            FileWriter out = new FileWriter("test17.txt");
            DataWriter.writeRectangularData(data, out, ',');
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testSem2_2() {
        Graph graph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");

        graph.addNode(x1);
        graph.addNode(x2);

        graph.addDirectedEdge(x1, x2);

        SemPm2 pm = new SemPm2(graph);
        pm.setDistributionType(x1, DistributionType.BETA);
        pm.setDistributionType(x2, DistributionType.BETA);

        SemIm2 im = new SemIm2(pm);
        im.setCoef(x1, x2, 1.4);

        DataSet data = im.simulateData(500, new Uniform(-1, 1));

        GraphWithParameters result = new Shimizu2006SearchOld(0.05).lingamDiscovery_DAG(data);
        System.out.println(result);

        try {
            FileWriter out = new FileWriter("test19.txt");
            DataWriter.writeRectangularData(data, out, ',');
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataSet getData() {
        Graph graph = constructGraph1();

        System.out.println("Graph: " + graph);

        //======================PM================================//

        SemPm2 pm = new SemPm2(graph);
        System.out.println(pm.getParameters());

        assertEquals(15, pm.getParameters().size());

        Node x1 = graph.getNode("X1");
        Node x2 = graph.getNode("X2");
        Node x3 = graph.getNode("X3");
        Node x4 = graph.getNode("X4");
        Node x5 = graph.getNode("X5");

        System.out.println(pm.getDistributionParameters(x1));

        for (Node node : graph.getNodes()) {
            pm.setDistributionType(node, DistributionType.GAUSSIAN_POWER);
        }

//        pm.setDistributionType(x1, DistributionType.UNIFORM);
        assertEquals(pm.getDistributionType(x1), DistributionType.GAUSSIAN_POWER);
        assertEquals(15, pm.getParameters().size());

        System.out.println(pm.getDistributionParameters(x1));

        System.out.println("PM Variable nodes: " + pm.getVariableNodes());
        System.out.println("PM Measured nodes: " + pm.getMeasuredNodes());


        SemIm2 im = new SemIm2(pm);
        im.setCoef(x1, x2, 1.4);
        im.setCoef(x2, x3, 1.5);
        im.setCoef(x3, x4, 1.5);
        im.setCoef(x1, x4, 1.5);
        im.setCoef(x4, x5, 1.5);

        try {
            im.setCoef(x1, x5, 2);
            fail("No such parameter.");
        } catch (Exception e) {
            // Should throw an exception.
        }

        System.out.println(im.getCoefMatrix());

        assertEquals(1.4, im.getCoef(x1, x2), 1E-10);

        System.out.println("IM Variable nodes: " + im.getVariableNodes());
        System.out.println("IM Measured nodes: " + im.getMeasuredNodes());

        for (Node node : graph.getNodes()) {
            System.out.println(node + "--> " + im.getDistribution(node));
        }

        List<Parameter> _params = pm.getDistributionParameters(x1);
        Parameter p1 = _params.get(0);
        System.out.println(p1);

        return im.simulateData(1000, new Uniform(-1, 1));
    }

    private DataSet getData2() {
        Graph graph = constructGraph1();

        System.out.println("Graph: " + graph);

        //======================PM================================//

        SemPm pm = new SemPm(graph);
        System.out.println(pm.getParameters());

        assertEquals(15, pm.getParameters().size());

        Node x1 = graph.getNode("X1");
        Node x2 = graph.getNode("X2");
        Node x3 = graph.getNode("X3");
        Node x4 = graph.getNode("X4");
        Node x5 = graph.getNode("X5");

//        System.out.println(pm.getDistributionParameters(x1));

//        for (Node node : graph.getNodes()) {
//            pm.setDistributionType(node, DistributionType.BETA);
//        }

//        pm.setDistributionType(x1, DistributionType.UNIFORM);
//        assertEquals(pm.getDistributionType(x1), DistributionType.BETA);
//        assertEquals(15, pm.getParameters().size());

//        System.out.println(pm.getDistributionParameters(x1));

        System.out.println("PM Variable nodes: " + pm.getVariableNodes());
        System.out.println("PM Measured nodes: " + pm.getMeasuredNodes());


        SemIm im = new SemIm(pm);
        im.setEdgeCoef(x1, x2, 1.4);
        im.setEdgeCoef(x2, x3, 1.5);
        im.setEdgeCoef(x3, x4, 1.5);
        im.setEdgeCoef(x1, x4, 1.5);
        im.setEdgeCoef(x4, x5, 1.5);

        System.out.println("IM Variable nodes: " + im.getVariableNodes());
        System.out.println("IM Measured nodes: " + im.getMeasuredNodes());

        GraphWithParameters pwp = new GraphWithParameters(im, new Dag(graph));

        DoubleMatrix2D matrix = TestFastIca.simulate(pwp, 1000, new Beta(0.2, 0.3));

        List<Node> variables = new ArrayList<Node>();

        for (int i = 0; i < matrix.columns(); i++) {
            variables.add(new ContinuousVariable("X" + i));
        }

        return ColtDataSet.makeContinuousData(variables, matrix);
    }


    public void testSampleBeta() {
        for (int i = 0; i < 100; i++) {
            System.out.println(RandomUtil.getInstance().nextBeta(1, 2));
        }
    }


    private Graph constructGraph1() {
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
        return new TestSuite(TestSem2.class);
    }
}
