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

package edu.cmu.tetrad.search.test;

import edu.cmu.tetrad.regression.LogisticRegression;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class LogisticRegressionTester {

    public static void main(String[] args) {

        //A Simple regression of 1 variable on another by Joe Ramsey probably
        double[] y = {0, 0, 0, 0, 1, 0, 1, 0, 1, 1};
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[][] regressors = {x};
        String[] names = {"x"};

        LogisticRegression logRegression = new LogisticRegression();
        logRegression.setRegressors(regressors);
        logRegression.setVariableNames(names);
        String report = logRegression.regress(y, "y");
        //System.out.println(result);
        System.out.print(report);


    }

    /*
    public static Test suite() {
        return new TestSuite(RegressionRunnerTester.class);
    }
    */
}


