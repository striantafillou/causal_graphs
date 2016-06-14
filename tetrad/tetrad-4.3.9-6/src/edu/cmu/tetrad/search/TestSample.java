package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.stat.Probability;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.RegexTokenizer;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.RandomUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestSample extends TestCase {
    public TestSample(String name) {
        super(name);
    }

    public void test1() {
        try {
            DataReader reader = new DataReader();
            DataSet data = reader.parseTabular(new File("test_data/eigen4c.csv.dat"));
//            DoubleMatrix2D matrix2D = data.getDoubleData();
//            DoubleMatrix2D m2 = matrix2D.viewDice();
//            System.out.println(m2);

            GraphWithParameters m3 = new Shimizu2006SearchOld(0.05).lingamDiscovery_DAG(data);

            System.out.println(m3);
        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void rtest2a() {
        try {
//            File file = new File("C:/work/proj/other/r1/r1_62.txt");
            File file = new File("/home/jdramsey/proj/other/r1/r1_62.txt");
            BufferedReader in = new BufferedReader(new FileReader(file));

            String line = in.readLine();
            int numFeatures = 0;
            int numCases = 0;

            RegexTokenizer tokenizer = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                System.out.println("Token: " + token);
                numFeatures++;
            }

            while ((line = in.readLine()) != null) {
                System.out.println(numCases);
                numCases++;
            }

            System.out.println("# Features = " + numFeatures);
            System.out.println("# Cases = " + numCases);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2b() {
        try {
            File file = new File("C:/work/proj/other/r1/r1_62.txt");
            BufferedReader in = new BufferedReader(new FileReader(file));

            // Skip first line.
            in.readLine();
            
            String line;
            int lines = 0;
            int numCases = 53000;
            int numFeatures = 200;

            DoubleMatrix2D data = new DenseDoubleMatrix2D(numCases, numFeatures);

            for (int i = 0; i  < numCases; i++) {
                line = in.readLine();
                RegexTokenizer tokenizer = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

                for (int j = 0; j < numFeatures; j++) {
                    double datum = Double.parseDouble(tokenizer.nextToken());
                    data.set(i, j, datum);
                }

                lines++;
            }

            System.out.println("# lines = " + lines);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void rtest3() {
        for (int dof = 1; dof <= 50; dof++) {
            double chiSquare = Probability.chiSquare(3 * dof, dof);
            double ratio = chiSquare / dof;

            System.out.println("DOF = " + dof + " ratio = " + ratio);
        }
    }


    public void rtest4() {
        Graph graph = GraphUtils.randomDag(10, 0, 10, 4, 4, 4, false);
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        DataSet data = semIm.simulateData(1000, false);
        SemEstimator estimator = new SemEstimator(data, semPm);
        estimator.estimate();
        SemIm estSem = estimator.getEstimatedSem();
        double fml = estSem.getFml();

        double fmlSum = 0.0;

        for (Node node : graph.getNodes()) {
            List<Node> subList = new ArrayList<Node>();
            subList.add(node);
            subList.addAll(graph.getParents(node));
            Graph subGraph = graph.subgraph(subList);

            SemPm subSemPm = new SemPm(subGraph);
            SemEstimator subEstimator = new SemEstimator(data, subSemPm);
            subEstimator.estimate();
            SemIm subEstSem = subEstimator.getEstimatedSem();

            double subFml = subEstSem.getFml();

            System.out.println("node " + node + " fml = " + subFml + " graph = " + subGraph);

            fmlSum += subFml;
        }

        System.out.println("Orig fml = " + fml + " sum of parts = " + fmlSum);

    }

    public void rtest5() {
        Graph graph = GraphUtils.randomDag(16, 0, 20, 4, 4, 4, false);
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        DataSet data = semIm.simulateData(1000, false);

        List<Node> nodes = graph.getNodes();
        Node x = nodes.get(0);
        nodes.remove(0);

        LinkedList<Node> trueSubNodes = new LinkedList<Node>();
        trueSubNodes.add(x);
        trueSubNodes.addAll(graph.getParents(x));

        Graph trueSubgraph = graph.subgraph(trueSubNodes);
        SemPm trueSemPm = new SemPm(trueSubgraph);
        SemEstimator trueEst = new SemEstimator(data, trueSemPm);
        trueEst.estimate();
        SemIm trueEstSem = trueEst.getEstimatedSem();
        double trueFml = trueEstSem.getFml();
        System.out.println("True FML " + trueFml + " for " + trueSubgraph);

        for (int i = 0; i < 10; i++) {
            List<Node> parents = new LinkedList<Node>();

//            for (Node node : nodes) {
//                if (PersistentRandomUtil.getInstance().nextDouble() < 0.2) {
//                    parents.add(node);
//                }
//            }

            for (int j = 0; j < graph.getParents(x).size(); j++) {
                int index = RandomUtil.getInstance().nextInt(nodes.size());
                Node parent = nodes.get(index);

                if (parents.contains(parent)) {
                    j--;
                    continue;
                }

                parents.add(parent);                       
            }

            List<Node> subNodes = new LinkedList<Node>();
            subNodes.add(x);
            subNodes.addAll(parents);

            Graph subGraph = new EdgeListGraph(subNodes);

            for (Node parent : parents) {
                subGraph.addDirectedEdge(parent, x);
            }

            double fml = scoreSubgraph(subGraph,  data);

            System.out.println("FML " + fml + " for " + subGraph);
        }

    }


    private double scoreSubgraph(Graph subGraph, DataSet data) {
        SemPm subSemPm = new SemPm(subGraph);
        SemEstimator _estimator = new SemEstimator(data, subSemPm);
        _estimator.estimate();
        SemIm subEstSem = _estimator.getEstimatedSem();

        return subEstSem.getFml();
    }

    public void testTest() {
        

    }


    public static Test suite() {
        return new TestSuite(TestSample.class);
    }
}
