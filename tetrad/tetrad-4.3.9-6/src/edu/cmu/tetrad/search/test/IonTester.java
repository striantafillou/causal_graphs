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
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.Fci;
import edu.cmu.tetrad.search.IONSearchOriginal;
import edu.cmu.tetrad.search.SepsetMap;
import edu.cmu.tetrad.search.indtest.IndTestCramerT;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @deprecated
 */
public class IonTester {
    public static void main(String[] args) {

        //Test with continuous datasets
        double alpha = 0.05;

        //Input data from first file for first dataset/IndTestCramerT/FCI
        String file1 = "test_data/TestIonL.txt";

        DataSet dataSet1 = null;

        try {
            DataReader reader = new DataReader();
            dataSet1 = reader.parseTabular(new File(file1));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //Input data from second file for second dataset/IndTestCramerT/FCI
        String file2 = "test_data/TestIonR.txt";

        DataSet dataSet2 = null;

        try {
            DataReader reader = new DataReader();
            dataSet2 = reader.parseTabular(new File(file2));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Graph[] pag = new Graph[2];
        SepsetMap[] ss = new SepsetMap[2];

        IndependenceTest indTest1;
        List listOfNodes1;
        int numNodes1;
        Knowledge bk1;

        System.out.println("For alpha = " + alpha);
        indTest1 = new IndTestCramerT(dataSet1, alpha);

        listOfNodes1 = indTest1.getVariables();
        numNodes1 = listOfNodes1.size();

        System.out.println("List of nodes " + numNodes1);
        for (Object node : listOfNodes1) {
            Node var = (Node) node;
            System.out.println(var);
        }

        bk1 = new Knowledge();

        Fci fci1 = new Fci(indTest1);
        pag[0] = fci1.search();
        ss[0] = fci1.getSepsetMap();

        IndependenceTest indTest2;
        List listOfNodes2;
        int numNodes2;
        Knowledge bk2;

        System.out.println("For alpha = " + alpha);
        indTest2 = new IndTestCramerT(dataSet2, alpha);

        listOfNodes2 = indTest2.getVariables();
        numNodes2 = listOfNodes2.size();

        System.out.println("List of nodes " + numNodes2);
        for (Object node : listOfNodes2) {
            Node var = (Node) node;
            System.out.println(var);
        }

        bk2 = new Knowledge();

        Fci fci2 = new Fci(indTest2);
        pag[1] = fci2.search();
        ss[1] = fci2.getSepsetMap();

        IONSearchOriginal ionSearch = new IONSearchOriginal(pag, ss, bk1);
        Graph gOut = ionSearch.search();

        System.out.println("Result of ION Algorithm");
        System.out.println(gOut);

        boolean[][] confirmed = ionSearch.getConfirmed();
        List nodes = gOut.getNodes();
        int n = nodes.size();
        for (int i = 0; i < n; i++) {
            Node X = (Node) nodes.get(i);

            for (int j = i; j < n; j++) {
                Node Y = (Node) nodes.get(j);
                if (!gOut.isAdjacentTo(X, Y)) {
                    continue;
                }
                System.out.println("Edge between " + X + " and " + Y + " " +
                        confirmed[i][j]);
            }
        }
    }
}


