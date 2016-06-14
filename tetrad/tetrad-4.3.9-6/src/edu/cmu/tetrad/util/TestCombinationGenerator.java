package edu.cmu.tetrad.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestCombinationGenerator extends TestCase {
    public TestCombinationGenerator(String name) {
        super(name);
    }

    public void test1() {
        CombinationGenerator.testPrint(new int[]{5, 3});
    }

    public void test2() {
        CombinationGenerator.testPrint(new int[]{2, 1});
    }

    public void test3() {
        CombinationGenerator.testPrint(new int[]{2, 3, 4});
    }

    public static Test suite() {
        return new TestSuite(TestCombinationGenerator.class);
    }
}
