package edu.cmu.tetradapp.model.calculator;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tyler Gibson
 */
public final class TestTransform extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestTransform(String name) {
        super(name);
    }


    public void testTransformWithNewColumnVariable(){
        List<Node> list = Arrays.asList((Node) new ContinuousVariable("x"),
                new ContinuousVariable("y"));
        DataSet data = new ColtDataSet(1, list);
        data.setDouble(0, 0, 1);
        data.setDouble(1, 0, 1);

        data.setDouble(0, 1, 1);
        data.setDouble(1, 1, 1);

        System.out.println("Starting with: \n " + data);

        try {
            String eq = "w = (x + y) * x";
            Transformation.transform(data, eq);
            System.out.println("Transformed using " + eq + " and got: \n" + data);
            assertTrue(data.getDouble(0, 2) == 2.0);
            assertTrue(data.getDouble(0, 2) == 2.0);
        } catch(Exception ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }



    public void testSingleTransforms() {
        // build a dataset.
        List<Node> list = Arrays.asList((Node) new ContinuousVariable("x"),
                new ContinuousVariable("y"),
                new ContinuousVariable("z"));
        DataSet data = new ColtDataSet(2, list);
        data.setDouble(0, 0, 2);
        data.setDouble(1, 0, 3);
        data.setDouble(2, 0, 4);

        data.setDouble(0, 1, 1);
        data.setDouble(1, 1, 6);
        data.setDouble(2, 1, 5);

        data.setDouble(0, 2, 8);
        data.setDouble(1, 2, 8);
        data.setDouble(2, 2, 8);


        System.out.println("Staring with: \n" + data);

        DataSet copy = new ColtDataSet((ColtDataSet) data);
        // test transforms on it.
        try {
            String eq = "z = (x + y)";
            Transformation.transform(copy, eq);
            System.out.println("Transformed using " + eq + " and got: \n" + copy);
            assertTrue(copy.getDouble(0, 2) == 3.0);
            assertTrue(copy.getDouble(1, 2) == 9.0);
            assertTrue(copy.getDouble(2, 2) == 9.0);

            copy = new ColtDataSet((ColtDataSet) data);
            eq = "x = x + 3";
            Transformation.transform(copy, eq);
            System.out.println("Transformed using " + eq + " and got: \n" + copy);
            assertTrue(copy.getDouble(0, 0) == 5.0);
            assertTrue(copy.getDouble(1, 0) == 6.0);
            assertTrue(copy.getDouble(2, 0) == 7.0);


            copy = new ColtDataSet((ColtDataSet) data);
            eq = "x = exp(x,2) + y + z";
            Transformation.transform(copy, eq);
            System.out.println("Transformed using " + eq + " and got: \n" + copy);
            assertTrue(copy.getDouble(0, 0) == 13.0);
            assertTrue(copy.getDouble(1, 0) == 23.0);
            assertTrue(copy.getDouble(2, 0) == 29.0);

        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }


    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestTransform.class);
    }
}
