package edu.cmu.tetrad.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestMultiDimIntTable2 extends TestCase {

    private MultiDimIntTable table;

    /**
     * Change the name of this constructor to match the name of the test class.
     */
    public TestMultiDimIntTable2(String name) {
        super(name);
    }

    public void setUp() {

        int[] dims = new int[]{2, 3, 4, 5};

        table = new MultiDimIntTable(dims);
    }

    public void tearDown() {

        // Do takedown for tests here.
    }

//    public void testSize() {
//        assertEquals(table.getNumCells(), 2 * 3 * 4 * 5);
//    }

    public void testIndexCalculation1() {

        int[] coords = new int[]{0, 0, 1, 0};
        int index = table.getCellIndex(coords);

        assertEquals(5, index);
    }

    public void testIndexCalculation2() {

        int[] coords = new int[]{0, 1, 2, 0};
        int index = table.getCellIndex(coords);

        assertEquals(30, index);
    }

    public void testCoordinateCalculation() {

        int[] coords = table.getCoordinates(30);

        assertEquals(1, coords[1]);
    }

    public void testCellIncrement() {

        int[] coords = table.getCoordinates(30);

        table.increment(coords, 1);
        assertEquals(1, table.getValue(coords));
    }

    public void testNumDimensions() {
        assertEquals(4, table.getNumDimensions());
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestMultiDimIntTable2.class);
    }
}
