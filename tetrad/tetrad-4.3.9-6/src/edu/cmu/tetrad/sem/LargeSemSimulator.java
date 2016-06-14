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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.dist.Distribution;
import edu.cmu.tetrad.util.dist.Normal;
import edu.cmu.tetrad.util.dist.Split;
import edu.cmu.tetrad.util.dist.Uniform;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores a SEM model, pared down, for purposes of simulating data sets with
 * large numbers of variables and sample sizes. Assumes acyclicity.
 *
 * @author Joseph Ramsey
 */
public final class LargeSemSimulator {
    static final long serialVersionUID = 23L;

    private DoubleMatrix2D edgeCoef;
    private DoubleMatrix2D errCovar;
    private double[] variableMeans;

    /**
     * Used for some linear algebra calculations.
     */
    private transient Algebra algebra;
    private List<Node> variableNodes;
    private Dag graph;

    //=============================CONSTRUCTORS============================//

    public LargeSemSimulator(Dag graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        this.graph = graph;

        this.variableNodes = graph.getNodes();
        int size = variableNodes.size();

        this.edgeCoef = new SparseDoubleMatrix2D(size, size);
        this.errCovar = new SparseDoubleMatrix2D(size, size);
        this.variableMeans = new double[size];

        Distribution edgeCoefDist = new Split(0.5, 1.5);
        Distribution errorCovarDist = new Uniform(1.0, 3.0);
        Distribution meanDist = new Normal(-1.0, 1.0);

        for (Edge edge : graph.getEdges()) {
            if (edge.getNode1().getNodeType() == NodeType.ERROR ||
                    edge.getNode2().getNodeType() == NodeType.ERROR) {
                continue;
            }

            Node tail = Edges.getDirectedEdgeTail(edge);
            Node head = Edges.getDirectedEdgeHead(edge);

            int _tail = variableNodes.indexOf(tail);
            int _head = variableNodes.indexOf(head);

            this.edgeCoef.set(_tail, _head, edgeCoefDist.nextRandom());
        }

        for (int i = 0; i < size; i++) {
            this.errCovar.set(i, i, errorCovarDist.nextRandom());
            this.variableMeans[i] = meanDist.nextRandom();
        }

//        System.out.println("Edge coefs: " + this.edgeCoef);
//        System.out.println("Error covars: " + this.errCovar);
//        System.out.println("Means: ");
//
//        for (int i = 0; i < size; i++) {
//            System.out.print(variableMeans[i] + "\t");
//        }
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Works, but will hang for cyclic models, and is very slow for
     * large numbers of variables (probably due to the heavyweight lookups of
     * various values--could be improved).
     */
    public DataSet simulateDataAcyclic(int sampleSize) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = getVariableNodes();

        // Make an empty data set.
        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            variables.add(var);
        }

//        System.out.println("Creating data set.");

        DataSet dataSet = new ColtDataSet(sampleSize, variables);
        constructSimulation(variableNodes, variables, sampleSize, dataSet);
        return dataSet;
    }

    public DataSet simulateDataAcyclic(DataSet dataSet) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = getVariableNodes();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            ContinuousVariable var = (ContinuousVariable) dataSet.getVariable(i);
            variables.add(var);
        }

        constructSimulation(variableNodes, variables, dataSet.getNumRows(), dataSet);
        return dataSet;
    }

    private void constructSimulation(List<Node> variableNodes,
                                     List<Node> variables, int sampleSize,
                                     DataSet dataSet) {
        // Create some index arrays to hopefully speed up the simulation.
        Dag graph = getGraph();
        List<Node> tierOrdering = graph.getTierOrdering();

        int[] tierIndices = new int[variableNodes.size()];

        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = variableNodes.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variableNodes.size(); i++) {
            Node node = variableNodes.get(i);
            List parents = graph.getParents(node);

            for (Iterator j = parents.iterator(); j.hasNext();) {
                Node _node = (Node) j.next();

                if (_node.getNodeType() == NodeType.ERROR) {
                    j.remove();
                }
            }

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = (Node) parents.get(j);
                _parents[i][j] = variableNodes.indexOf(_parent);
            }
        }

//        System.out.println("Starting simulation.");

        // Do the simulation.
        for (int row = 0; row < sampleSize; row++) {
//            if (row % 100 == 0) System.out.println("Row " + row);

            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
                double value =
                        RandomUtil.getInstance().nextNormal(0, 1) *
                                errCovar.get(col, col);

                for (int j = 0; j < _parents[col].length; j++) {
                    int parent = _parents[col][j];
                    value += dataSet.getDouble(row, parent) *
                            edgeCoef.get(parent, col);
                }

                value += variableMeans[col];
                dataSet.setDouble(row, col, value);
            }
        }
    }

    public Algebra getAlgebra() {
        if (algebra == null) {
            algebra = new Algebra();
        }

        return algebra;
    }

    private List<Node> getVariableNodes() {
        return variableNodes;
    }

    public Dag getGraph() {
        return graph;
    }
}

