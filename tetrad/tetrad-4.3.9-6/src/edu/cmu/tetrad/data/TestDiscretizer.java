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

package edu.cmu.tetrad.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the column discretizer.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public final class TestDiscretizer extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestDiscretizer(final String name) {
        super(name);
    }


    public static void testBreakpointCalculation(){
        double[] data = {13, 1.2, 2.2, 4.5, 12.005, 5.5, 10.1, 7.5, 3.4};
        double[] breakpoints = Discretizer.getEqualFrequencyBreakPoints(data, 3);

        assertTrue(breakpoints.length == 2);
        assertEquals(4.5, breakpoints[0]);
        assertEquals(10.1, breakpoints[1]);

        Discretizer.Discretization dis = Discretizer.discretize(data, breakpoints, "after", Arrays.asList("1", "2", "3"));
        System.out.println(dis);

        breakpoints = Discretizer.getEqualFrequencyBreakPoints(data, 4);
        assertTrue(breakpoints.length == 3);

        assertEquals(3.4, breakpoints[0]);
        assertEquals(5.5, breakpoints[1]);
        assertEquals(10.1, breakpoints[2]);

    }




    public static void testContinuous() {
        final double[] data = {1, 2, 2.5, 3, 4, 5};

        double[] cutoffs = new double[]{2.5, 3.2};
        List<String> categories = Arrays.asList("lo", "med", "hi");

        Discretizer.Discretization discretization = Discretizer.discretize(data, cutoffs, "after", categories);

        System.out.println(discretization);

        List<String> discretizedCategories =
                discretization.getVariable().getCategories();
        int[] discretizedData = discretization.getData();

        assertEquals("lo", discretizedCategories.get(discretizedData[0]));
        assertEquals("lo", discretizedCategories.get(discretizedData[1]));
        assertEquals("med", discretizedCategories.get(discretizedData[2]));
        assertEquals("med", discretizedCategories.get(discretizedData[3]));
        assertEquals("hi", discretizedCategories.get(discretizedData[4]));
        assertEquals("hi", discretizedCategories.get(discretizedData[5]));
    }

//    public static void testDiscrete() {
//        final int[] data = {1, 2, 1, 2, 0, 5, 3, 2, 3, 4, 3, 5};
//
//        DiscreteVariable variable = new DiscreteVariable("before", 6);
//
//        int[] remap = new int[]{0, 0, 1, 1, 2, 2};
//        List<String> categories =
//                Arrays.asList(new String[]{"lo", "med", "hi"});
//
//        Discretization discretization = Discretization.discretize(variable, data, remap, "after", categories);
//
//        System.out.println(discretization);
//
//        List<String> discretizedCategories =
//                discretization.getVariable().getCategories();
//        int[] discretizedData = discretization.getSimulatedData();
//
//        assertEquals("lo", discretizedCategories.get(discretizedData[0]));
//        assertEquals("med", discretizedCategories.get(discretizedData[1]));
//        assertEquals("lo", discretizedCategories.get(discretizedData[2]));
//        assertEquals("med", discretizedCategories.get(discretizedData[3]));
//        assertEquals("lo", discretizedCategories.get(discretizedData[4]));
//        assertEquals("hi", discretizedCategories.get(discretizedData[5]));
//    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestDiscretizer.class);
    }
}


