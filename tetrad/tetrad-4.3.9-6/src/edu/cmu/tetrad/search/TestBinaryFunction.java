package edu.cmu.tetrad.search;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

public class TestBinaryFunction extends TestCase {

    public TestBinaryFunction(String name) {
        super(name);
    }

    public void rtest1() {
        BinaryFunctionUtils utils = new BinaryFunctionUtils(4);

        System.out.println("# validated = ");
        long numValidated = utils.count();
        System.out.println("TOTAL " + numValidated + " OUT OF " + utils.getNumFunctions());
    }

    public void rtest2() {
        BinaryFunctionUtils utils = new BinaryFunctionUtils(4);
        List<BinaryFunction> functions = utils.findNontransitiveTriple();
    }

    public void rtest3() {
        BinaryFunctionUtils utils = new BinaryFunctionUtils(3);
        long num = utils.count2();

        System.out.println("... " + num);
    }

    public void rtest4() {
        BinaryFunctionUtils utils = new BinaryFunctionUtils(4);

        utils.checkTriple(4,
                new boolean[]{true, false, true, false, true, true, false, false,
                        false, false, true, true, false, true, true, true},
                new boolean[]{true, false, true, false, false, false, true, true,
                        true, true, false, false, false, true, false, true},
                new boolean[]{true, true, false, false, false, true, false, true,
                        true, false, true, false, false, false, true, true}
        );
    }

    public void test() {
        // Keep the unit test runner happy.
    }

    /**
     * This method uses reflection to collect up all of the test methods from this
     * class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestBinaryFunction.class);
    }
}
