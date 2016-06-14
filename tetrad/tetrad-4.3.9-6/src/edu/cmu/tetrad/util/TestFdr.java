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

package edu.cmu.tetrad.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Implements some tests of the FDR (False Discovery Rate) test.
 *
 * @author Joseph Ramsey
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TestFdr extends TestCase {
    public TestFdr(String name) {
        super(name);
    }

    public void testSimpleCase() {
        double[] p = new double[]{
                .8, .01, .2, .07, .003, .9, .05, .03, .0001
        };

        double alpha = 0.05;
        boolean negativelyCorrelated = false;

        double cutoff = StatUtils.fdr(alpha, p, negativelyCorrelated);

        System.out.println("Cutoff = " + cutoff);

        for (int i = 0; i < p.length; i++) {
            if (p[i] < cutoff) {
                System.out.println(i + ": " + p[i]);
            }
        }

        assertEquals(cutoff, .01);

        negativelyCorrelated = true;
        cutoff = StatUtils.fdr(alpha, p, negativelyCorrelated);
        assertEquals(cutoff, 0.003);

    }

    public static Test suite() {
        return new TestSuite(TestFdr.class);
    }
}