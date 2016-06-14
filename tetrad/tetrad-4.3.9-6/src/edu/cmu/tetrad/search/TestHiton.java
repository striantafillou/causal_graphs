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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.indtest.IndTestChiSquare;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

public class TestHiton extends TestCase {
    static Graph testGraphSub;
    static Graph testGraphSubCorrect;

    public TestHiton(String name) {
        super(name);
    }


    public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    public void testHiton() {
        try {
            //Test with discrete data.
            String filenameD1 = "test_data/markovBlanketTestDisc.dat";
            File fileD1 = new File(filenameD1);
            double alpha = 0.05;
            String targetVariableString = "A1";
//        int depth = -1;
            int depth = 2;

            DataReader reader = new DataReader();
            reader.setDelimiter(DelimiterType.TAB);
            reader.setCommentMarker("#");
            DataSet dds1 = reader.parseTabular(fileD1);

            IndTestChiSquare test = new IndTestChiSquare(dds1, alpha);

            HitonOld hitonSearch = new HitonOld(test, depth);

            //List currentPc = hitonSearch.mmpc(targetVariableString);
            Graph currentMb = hitonSearch.search(targetVariableString);

            //System.out.println("Output of HITON-PC = " + currentPc);
            System.out.println("Output of HITON-MB = " + currentMb);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TestHiton("").testHiton();
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestHiton.class);
    }

}


