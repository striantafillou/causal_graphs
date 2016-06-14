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

package edu.cmu;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks for package cycles.
 */
public class TestCyclicity extends TestCase {
    private JDepend jdepend;

    public TestCyclicity(String name) {
        super(name);
    }

    public void setUp() {
        jdepend = new JDepend();
        File file = new File("./build/tetrad/classes");

        try {
            jdepend.addDirectory(file.getAbsolutePath());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void tearDown() {
        jdepend = null;
    }

    /**
     * Tests that a package dependency cycle does not exist for any of the
     * analyzed packages.
     */
    public void testAllPackagesCycle() {
        Collection packages = jdepend.analyze();

        for (Object aPackage : packages) {
            JavaPackage p = (JavaPackage) aPackage;

            if (p.containsCycle()) {
                System.out.println("\n***Package: " + p.getName() + ".");
                System.out.println();
                System.out.println(
                        "This package participates in a package cycle. In the following " +
                                "\nlist, for each i, some class in package i depends on some " +
                                "\nclass in package i + 1. Please find the cycle and remove it.");

                List l = new LinkedList();
                p.collectCycle(l);
                System.out.println();

                for (int j = 0; j < l.size(); j++) {
                    JavaPackage pack = (JavaPackage) l.get(j);
                    System.out.println((j + 1) + ".\t" + pack.getName());
                }

                System.out.println();
            }
        }

        if (jdepend.containsCycles()) {
            fail("Package cycle(s) found!");
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TestCyclicity.class);
    }
}

