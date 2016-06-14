package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestMaxPValueSearch extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestMaxPValueSearch(String name) {
        super(name);
    }

    public void test1() {
        Dag dag = GraphUtils.randomDag(4, 0, 3, 3, 3, 3, false);

        System.out.println("True DAG: " + dag + "\n");

        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(1000, false);

        double alpha = 0.05;
        int maxEdges = 5;

        MaxPValueSearch search = new MaxPValueSearch(data, alpha, maxEdges);
        search.setTrueDag(dag);
        search.setTrueIm(im);

        MaxPValueSearch.Result result = search.search();

        for (int i = 0; i < result.getDags().size(); i++) {
            System.out.println("Result #" + (i + 1) + result.getDags().get(i));
            System.out.println("P Value #" + (i + 1) + " = " + result.getPValues().get(i));
        }
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