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

import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.HitonOld;
import edu.cmu.tetrad.search.indtest.IndTestChiSquare;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Frank Wimberly
 * @deprecated
 */
public class HitonTester {
    public static void main(String[] args) {

        try {
            //Test with discrete data.
            String filenameD1 = args[0];
            File fileD1 = new File(filenameD1);
            //String fileD = "../../../test_data/markovBlanketTestDisc.dat";

            //        FileReader frD1 = null;

            //double alpha = 0.05;
            double alpha = Double.parseDouble(args[1]);

            String targetVariableString = args[2];

            int depth = Integer.parseInt(args[3]);

            //        try {
            //            frD1 = new FileReader(filenameD1);
            //        }
            //        catch (IOException e) {
            //            System.out.println("Error opening file " + filenameD1);
            //            System.exit(0);
            //        }

            List knownVariables = null;

            DataReader reader = new DataReader();
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
}


