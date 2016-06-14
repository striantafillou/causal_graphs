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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * This class implements algorithms for finding the Markov Blanket using methods
 * described in Aliferis, Tsamardinos & Statnikov, HITON, A Novel Markov Blanket
 * Algorithm for Optimal Variable Selection, Technical Report DSL-03-08,
 * Vanderbilt University, 2003; also, AMIA, 2003. The algorithms are also
 * described in Bai, Glymour, Ramsey, Spirtes and Wimberly, PCX:  Markov Blanket
 * Classification for Large Data Sets with Few Cases.
 *
 * @author Frank Wimberly
 */
public final class HitonOld implements MbSearch {

    private IndependenceTest test;
    private List<Node> variables;
    private int depth;
    private Graph markovBlanket;

    /**
     * The constructor takes two arguments:
     *
     * @param test  a conditional independence oracle and
     * @param depth the depth to be used in the PC search.
     */
    public HitonOld(IndependenceTest test, int depth) {
        if (test == null) {
            throw new NullPointerException();
        }

        this.test = test;
        this.depth = depth;
        this.variables = test.getVariables();
    }

    public Graph search(String targetVariableName) {
        if (targetVariableName == null) {
            throw new IllegalArgumentException(
                    "Null target name not permitted");
        }

        Node W = null;

        for (Node variable : variables) {
            if (variable.getName().equals(targetVariableName)) {
                W = variable;
                break;
            }
        }

        if (W == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset.");
        }

        //DEBUG Print:
        TetradLogger.getInstance().details("target = " + targetVariableName + " W = " + W);

        TetradLogger.getInstance().details("Will use HITON procedure.");

        List<Node> associated = hitonMb(targetVariableName);

        if (!associated.contains(W)) {
            associated.add(W);
        }

        //DEBUG Print stmts
        TetradLogger.getInstance().details("Variables ouput by findRamsey--used in PC Search");
        for (Node V : associated) {
            TetradLogger.getInstance().details("" + V);
        }


        TetradLogger.getInstance().details("Size of associated = " + associated.size());

        IndependenceTest indAssociated = test.indTestSubset(associated);

        //Having found the variables selected by HITON-MB start the PCX
        //algorithm at step 4.

        //Step 4.
        //Run a PC search on this smaller dataset and store the resulting
        //EndpointMatrixGraph (a pattern) in associatedPattern.
        TetradLogger.getInstance().details("Entering step 4");
        Pc pcAssociated = new Pc(indAssociated);

        //        pcAssociated.addObserver(new BasicSearchObserver());
        //        pcAssociated.setDepth(4);  //Debug Xue's dd1_100.txt case
        if (depth > 0) {
            pcAssociated.setDepth(depth); //Debug Xue's dd1_100.txt case
        }

        Graph associatedPattern = pcAssociated.search();

        //Graph associatedPattern = new EndpointMatrixGraph(assocPattern);

        //Debug print:
        TetradLogger.getInstance().details("Pattern produced by PC algorithm");
        TetradLogger.getInstance().details("" + associatedPattern);

        //Step 5.
        //For each double-headed edge X<-->W and each undirected edge
        //X--W, replace the edge with W-->X.
        TetradLogger.getInstance().details("\nEntering step 5.");

        List<Node> adjW = associatedPattern.getAdjacentNodes(W);

        for (Node x : adjW) {

            //DEBUG Print
            //LogUtils.getInstance().fine("Considering the edge between " + W + " and " + x);

            if ((associatedPattern.getEndpoint(W, x) == Endpoint.ARROW &&
                    associatedPattern.getEndpoint(x, W) == Endpoint.ARROW) || (
                    associatedPattern.getEndpoint(W, x) == Endpoint.TAIL &&
                            associatedPattern.getEndpoint(x, W) == Endpoint
                                    .TAIL)) {
                associatedPattern.setEndpoint(W, x, Endpoint.ARROW);
                associatedPattern.setEndpoint(x, W, Endpoint.TAIL);
                TetradLogger.getInstance().details("Setting edge " + W + "-->" + x);  //DEBUG
            }
        }

        TetradLogger.getInstance().details("\nEntering Step 6."); //DEBUG

        //Step 6.
        //For each X which is a parent of W:
        List<Node> parentsOfW = associatedPattern.getParents(W);
        for (Node x : parentsOfW) {

            //x is a parent of W.
            List<Node> adjX = associatedPattern.getAdjacentNodes(x);
            for (Node y : adjX) {
                if (y != W) {
                    associatedPattern.removeEdge(y, x);
                }
            }
        }

        //Step 7.
        //For all edges that are doubly directed or undirected either orient them or
        //remove them depending on whether they involve children of W.
        TetradLogger.getInstance().details("\nEntering Step 7.");

        List<Edge> allEdges = associatedPattern.getEdges();
        for (Edge edge : allEdges) {
            Endpoint fromEndpoint = edge.getEndpoint1();
            Endpoint toEndpoint = edge.getEndpoint2();

            Node fromNode = edge.getNode1();
            Node toNode = edge.getNode2();

            if ((fromEndpoint == Endpoint.ARROW &&
                    toEndpoint == Endpoint.ARROW) || (
                    fromEndpoint == Endpoint.TAIL &&
                            toEndpoint == Endpoint.TAIL)) {

                if (associatedPattern.isChildOf(fromNode, W) &&
                        !associatedPattern.isChildOf(toNode, W)) {
                    //if(childrenOfW.contains(fromNode) && !childrenOfW.contains(toNode)) {
                    associatedPattern.setEndpoint(toNode, fromNode,
                            Endpoint.ARROW);
                    associatedPattern.setEndpoint(fromNode, toNode,
                            Endpoint.TAIL);
                } else if (associatedPattern.isChildOf(toNode, W) &&
                        !associatedPattern.isChildOf(fromNode, W)) {
                    //if(childrenOfW.contains(toNode) && !childrenOfW.contains(fromNode)) {
                    associatedPattern.setEndpoint(fromNode, toNode,
                            Endpoint.ARROW);
                    associatedPattern.setEndpoint(toNode, fromNode,
                            Endpoint.TAIL);
                } else {
                    associatedPattern.removeEdge(fromNode, toNode);
                }
            }
        }

        //Recompute childrenOfW and parentsOfW here?
        List<Node> childrenOfW = associatedPattern.getChildren(W);
        parentsOfW = associatedPattern.getParents(W);

        //Step 8.
        TetradLogger.getInstance().details("\nEntering Step 8.");
        Set<Node> remaining = new HashSet<Node>();
        remaining.add(W);
        //List parentOfChildOfW = null;
        //List parentOfChildOfW = new LinkedList();

        //Include parents and children of W.
        for (Node parent : parentsOfW) {
            remaining.add(parent);
        }

        for (Node child : childrenOfW) {
            remaining.add(child);
        }

        for (Node child : childrenOfW) {
            List<Node> parChild = associatedPattern.getAdjacentNodes(child);
            for (Node parentOfChild : parChild) {

                //Include parents of children of W.
                remaining.add(parentOfChild);
            }
        }


        if (!remaining.contains(W)) {
            //remaining.add(W);
            throw new IllegalArgumentException("Target missing from MB");
        }

        TetradLogger.getInstance().details("After Step 8 there are " + remaining.size() +
                " variables remaining.");

        List<Node> remainingList = new LinkedList<Node>(remaining);

        //Create a subgraph containing the remaining variables.

        this.markovBlanket = associatedPattern.subgraph(remainingList);

        //Step 9 version suggested by Xue
        //Delete all edges into parents of W.
        TetradLogger.getInstance().details("\nEntering Step 9.");

        List<Node> parents = markovBlanket.getParents(W);

        for (Node parent : parents) {
            List<Node> parPar = markovBlanket.getParents(parent);

            for (Node parentPar : parPar) {
                markovBlanket.removeEdge(parentPar, parent);
            }

        }

        //Then delete all edges out of children of W.
        List<Node> children = markovBlanket.getChildren(W);

        for (Node child : children) {
            List<Node> childChild = markovBlanket.getChildren(child);

            for (Node childCh : childChild) {
                markovBlanket.removeEdge(childCh, child);
            }

        }

        //Step 10.  Delete edges between parents of children of the target.
        TetradLogger.getInstance().details("\nEntering Step 10.");

        childrenOfW = markovBlanket.getChildren(W);
        List<Node> parentsOfChildren = new LinkedList<Node>();

        for (Node child : childrenOfW) {
            List<Node> parChild = markovBlanket.getParents(child);
            for (Node parentOfChild : parChild) {
                if (parentOfChild == W) {
                    continue;  //Don't include W.
                }
                parentsOfChildren.add(parentOfChild);
            }
        }

        for (int i = 0; i < parentsOfChildren.size(); i++) {
            Node childParent1 = parentsOfChildren.get(i);
            for (int j = 0; j < parentsOfChildren.size(); j++) {
                if (i == j) {
                    continue;
                }
                Node childParent2 = parentsOfChildren.get(j);
                markovBlanket.removeEdge(childParent1, childParent2);
            }
        }

        //        //Step 11.  The Glymour designated "hack".
        //        parents = markovBlanket.getParents(W);   //Might have changed in Step 9.
        //        if(parents.size() >= 8) {
        //            VariableScorePair[] vsp = new VariableScorePair[parents.size()];
        //            int numberElements = 0;        //Index of the vsp array as it is filled.
        //            //LogUtils.getInstance().fine("Associated with " + W + " before sort.");  //Debug print
        //            for (Iterator it = parents.iterator(); it.hasNext();) {
        //                Variable par = (Variable) it.next();
        //                if (par == W) continue;
        //
        //                boolean ind = (!test.isIndependent(W, par, Collections.EMPTY_LIST));
        //                //double score = ((IndTestChiSquare) test).getXSquare();
        //                double score = ((IndTestChiSquare) test).getPValue();
        //                //double score = ((IndTestGSquare2) test).getPValue();
        //                int index = parents.indexOf(par);
        //                vsp[numberElements] = new VariableScorePair(par, score, index);
        //                //LogUtils.getInstance().fine(numberElements + " " + par + " " + score);     //Debug print
        //                numberElements++;
        //
        //            }
        //
        //            Arrays.sort(vsp, 0, numberElements);
        //
        //            //Debug print:
        //            LogUtils.getInstance().fine("Associated with " + W + " in order from strongest to weakes assoc.");
        //
        //            for (int i = 0; i < numberElements; i++) {
        //                LogUtils.getInstance().fine("H1" + vsp[i].getVariable());
        //                LogUtils.getInstance().fine(" " + vsp[i].getBic());
        //            }
        //
        //
        //            for(int i = 5; i < numberElements; i++) {
        //                markovBlanket.removeEdge((Variable) vsp[i], W);
        //            }
        //        }

        //Delete nodes which have no adjacent nodes.  Not necessary?
        for (Node r : remainingList) {
            if (markovBlanket.getEdges(r).isEmpty()) {
                markovBlanket.removeNode(r);
            }
        }

        TetradLogger.getInstance().details("DEBUG: Markov Blanket after Step 10");
        TetradLogger.getInstance().details("" + markovBlanket);

        return markovBlanket;
    }

    /**
     * This method implements the HITON-PC procedure of Aliferis et al (2003).
     * It is used by the HITON-MB procedure.
     *
     * @param targetName a String containing the name of the target variable.
     * @return a List containing the parents and children of the target
     *         variable.
     */
    public List<Node> hitonPc(String targetName) {
        Node t = getVariableForName(targetName);

        //The List currentPc implements the set CurrentPC in the algorithm description.
        //It is initialized to be the empty List.
        LinkedList<Node> currentPc = new LinkedList<Node>();

        //The array sortedVars contains all the variables associated with the independence
        //test other than the target.  They are ordered from the variable with the highest
        //association with the target (sortedVars[0]) to that with the lowest association.
        Node[] sortedVars = maxAssoc(t, variables);

        //According to the strength of their association with the target,
        //admit each variable into currentPc
        for (Node node : sortedVars) {
            currentPc.add(node);

            //If there is a variable X and a subset S of currentPc s.t. X is independent
            //of the target t given the set S, remove it from currentPc.

            //For each element of currentPc, test whether to remove it.

            NODES:
            for (Node x : new LinkedList<Node>(currentPc)) {

                //Variable x corresponds to x in the algorithm description.
                int index = currentPc.indexOf(x);
                currentPc.remove(x);

                //See whether x is ind of t for some conditioning set.

                //Test conditioning sets of size up to the min of depth and the number
                //of elements in currentPc - 1.
                for (int d = 0; d <= Math.min(depth, currentPc.size()); d++) {

                    //The ChoiceGenerator selects elements of currentPc for inclusion
                    //in the conditioning set.  x is never included.
                    ChoiceGenerator cg =
                            new ChoiceGenerator(currentPc.size(), d);
                    int[] indices;

                    SETS:
                    while ((indices = cg.next()) != null) {
                        List<Node> condSet = new LinkedList<Node>();

                        for (int k = 0; k < d; k++) {
                            condSet.add(currentPc.get(indices[k]));
                        }

                        //The Variable being added must either be x or must be in the
                        //conditioning set or else the independence test will be redundant.
                        if (test.isIndependent(x, t, condSet)) {
                            currentPc.remove(x);
                            continue NODES;
                        }
                    }
                }

                currentPc.add(index, x);
            }
        }

        //The List currentPc now contains the result of the HITON-PC procedure.
        return currentPc;
    }

    public List<Node> findMb(String targetName) {
        return hitonMb(targetName);
    }

    public String getAlgorithmName() {
        return "HITON";
    }

    public int getNumIndependenceTests() {
        return 0;
    }

    /**
     * This method implements the HITON-MB procedure described in Aliferis et al
     * (2003).
     *
     * @param targetName the name of the Target.
     * @return a List containing a set of candidate Markov Blanket nodes of the
     *         Target.
     */
    public List<Node> hitonMb(String targetName) {
        Node t = getVariableForName(targetName);
        List<Node> pc = hitonPc(targetName);
        List<Node> currentMb = new LinkedList<Node>();

        //After this loop currentMb will contain the parents and children of the parents and
        //children of the Target.
        for (Node node : pc) {
            List<Node> pcOfPc = hitonPc(node.getName());

            for (Node v : pcOfPc) {
                if (!currentMb.contains(v)) {
                    currentMb.add(v);
                }
            }
        }

        //Add elements of pc to currentMb
        for (Node node : pc) {
            if (!currentMb.contains(node)) {
                currentMb.add(node);
            }
        }

        List<Node> currentMbCopy = new LinkedList<Node>(currentMb);

        X:
        for (Node x : currentMbCopy) {
            if (x == t) {
                continue;
            }

            TetradLogger.getInstance().details("x = " + x);

            List<Node> vMinusTx = new LinkedList<Node>(variables);
            vMinusTx.remove(t);
            vMinusTx.remove(x);

            for (Node y : pc) {
                if (y == x || y == t) {
                    continue;
                }

                TetradLogger.getInstance().details("y = " + y);

                //For each subset S of V - {t, x}, compute S union {y}.
                for (int d = 0; d <= Math.min(depth, vMinusTx.size()); d++) {
                    TetradLogger.getInstance().details("Cond sets of d " + d);

                    ChoiceGenerator cg = new ChoiceGenerator(vMinusTx.size(), d);
                    int[] indices;

                    while ((indices = cg.next()) != null) {
                        List<Node> cond = new LinkedList<Node>();

                        for (int k : indices) {
                            cond.add(vMinusTx.get(k));
                        }

                        if (!cond.contains(y)) {
                            cond.add(y);
                        }

                        if (test.isIndependent(x, t, cond)) {
                            currentMb.remove(x);
                            continue X;
                        }
                    }
                }
            }
        }

        return currentMb;
    }

    /**
     * Constructs an array containing the variables in the List variables in
     * which they are ordered according to the strength of their association
     * with the target variables.
     *
     * @param target
     * @return the list of variables which are not removed.
     */
    public Node[] maxAssoc(Node target, List<Node> variables) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Null target name not permitted");
        }

        //Find the variables associated with W and put them in the array
        //vsp which will be sorted by strength of association with W.

        VariableScorePair[] vsp = new VariableScorePair[variables.size()];

        int numberElements = 0;        //Index of the vsp array as it is filled.
        //LogUtils.getInstance().fine("Associated with " + W + " before sort.");  //Debug print

        for (Node possibleAssoc : variables) {
            if (possibleAssoc == target) {
                continue;
            }

            test.isIndependent(target, possibleAssoc,
                    new LinkedList<Node>());
            double score = test.getPValue();
            int index = variables.indexOf(possibleAssoc);
            vsp[numberElements] =
                    new VariableScorePair(possibleAssoc, score, index);
            numberElements++;

        }

        Arrays.sort(vsp, 0, numberElements);
        Node[] varsInOrder = new Node[numberElements];

        //Since the sort method will put the most weakly associated variables first,
        //the array varsInOrder should reverse the order of the variables.
        //LogUtils.getInstance().fine("Variables in order by strength of association with target.");
        for (int i = 0; i < numberElements; i++) {
            VariableScorePair pair = vsp[numberElements - i - 1];
            varsInOrder[i] = pair.getVariable();
        }


        return varsInOrder;
    }

    public Graph getMarkovBlanket() {
        return markovBlanket;
    }

    private Node getVariableForName(String targetName) {
        Node target = null;

        for (Node V : variables) {
            if (V.getName().equals(targetName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetName);
        }

        return target;
    }

    /**
     * Inner class used by search11.  Members of this class can be stored in
     * arrays and be sorted by score, which is usually computed by the
     * GSquareTest class.
     */
    private static class VariableScorePair implements Comparable {
        private Node v;   //A variable or pair of variables.
        private double score;   //The score associated with this set.
        private int index;

        public VariableScorePair(Node v, double score, int index) {
            this.v = v;
            this.score = score;
            this.index = index;
        }

        public Node getVariable() {
            return v;
        }

        public double getScore() {
            return score;
        }

        public int getIndex() {
            return index;
        }

        public int compareTo(Object other) {
            if (getScore() < ((VariableScorePair) other).getScore()) {
                return 1;
            }
            else if (getScore() == ((VariableScorePair) other).getScore()) {
                return 0;
            }
            else {
                return -1;
            }
        }
    }
}


