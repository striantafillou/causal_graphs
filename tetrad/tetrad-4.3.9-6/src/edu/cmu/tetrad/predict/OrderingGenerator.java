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

package edu.cmu.tetrad.predict;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Erin Korber.   October 2004
 */
public class OrderingGenerator {
    private int N;
    private boolean block[];          //initialized all to false by default
    private int solution[];
    private Graph pi;
    private List<Node> allNodes;
    private List<List<Node>> answers;

    public OrderingGenerator(Graph P) {
        pi = P;
        allNodes = pi.getNodes();
        N = allNodes.size();
        answers = new LinkedList<List<Node>>();
        block = new boolean[N];
        solution = new int[N];
        genOrds(0);
    }

    public void genOrds(int n) {
        if (n < N) {
            for (int i = 0; i < N; i++) {
                if (!block[i]) {
                    block[i] = true;
                    solution[n] = i;
                    genOrds(n + 1);
                    block[i] = false;
                }
            }
        } else {
            boolean[] flags = new boolean[N];
            for (int i = 0; i < N; i++) {
                flags[i] = true;
            }
            //Check to see if solution is valid for the graph
            for (int i = 0; i < N; i++) {
                int currIndex = solution[i];
                Node curr = allNodes.get(currIndex);
                List<Node> preds =
                        pi.getAncestors(Collections.singletonList(curr));
                for (Node pred : preds) {
                    if (flags[i]) {
                        flags[i] = false;
                        int ancIndex = allNodes.indexOf(pred);
                        for (int a = 0; a <= i; a++) {
                            if (solution[a] == ancIndex) {
                                flags[i] = true;
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            boolean flg = true;
            for (int i = 0; i < N; i++) {
                if (!flags[i]) {
                    flg = false;
                    break;
                }
            }
            if (flg) {
                List<Node> solList = new LinkedList<Node>();
                for (int i = 0; i < N; i++) {
                    solList.add(allNodes.get(solution[i]));
                }
                answers.add(solList);
            }
        }
    }

    public List<List<Node>> getOrders() {
        return answers;
    }

}


