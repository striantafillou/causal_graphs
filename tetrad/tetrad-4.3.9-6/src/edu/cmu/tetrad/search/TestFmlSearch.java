package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphUtils;
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
public class TestFmlSearch extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestFmlSearch(String name) {
        super(name);
    }

    public void test1() {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);

        Dag dag = GraphUtils.randomDag(7, 0, 7, 3, 3, 3, false);

        System.out.println("True DAG: " + dag + "\n");

        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(2000, false);

        double alpha = 0.05;
        int maxEdges = 5;

        FmlSearch search = new FmlSearch(data, alpha, maxEdges);
        search.setTrueDag(dag);
        search.setTrueIm(im);
        search.search();
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