package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDatasetGeneralized;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.regression.RegressionDataset;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * Contains some utilities for doing autoregression. Should probably be improved
 * by somebody.
 *
 * @author Joseph Ramsey
 */
public class TimeSeriesUtils {
    public static DataSet ar(DataSet timeSeries, int numLags) {
        DataSet timeLags = createLagData(timeSeries, numLags);
        List<Node> regressors = new ArrayList<Node>();

        for (int i = timeSeries.getNumColumns(); i < timeLags.getNumColumns(); i++) {
            regressors.add(timeLags.getVariable(i));
        }

        Regression regression = new RegressionDatasetGeneralized(timeLags);

        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(timeLags.getNumRows(),
                timeSeries.getNumColumns());

        for (int i = 0; i < timeSeries.getNumColumns(); i++) {
            Node target = timeLags.getVariable(i);
            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        return ColtDataSet.makeContinuousData(timeSeries.getVariables(), residuals);
    }

    public static VarResult var(DataSet timeSeries, int numLags) {
        DataSet timeLags = TimeSeriesUtils.createLagData(timeSeries, numLags);
        Knowledge knowledge = new Knowledge(timeLags.getKnowledge());

        for (int i = 0; i <= numLags; i++) {
            knowledge.setTierForbiddenWithin(i, true);
        }

//        IndependenceTest test = new IndTestFisherZ(timeLags, 0.05);
//        Cpc search = new Cpc(test);
        Ges search = new Ges(timeLags);
        search.setKnowledge(knowledge);
        Graph graph = search.search();

        // want to collapse graph here...
        Graph collapsedVarGraph = new EdgeListGraph(timeSeries.getVariables());

        for (Edge edge : graph.getEdges()) {
            String node1_before = edge.getNode1().getName();
            String node2_before = edge.getNode2().getName();

            String node1_after = node1_before.substring(0, node1_before.indexOf("."));
            String node2_after = node2_before.substring(0, node2_before.indexOf("."));

            Node node1 = collapsedVarGraph.getNode(node1_after);
            Node node2 = collapsedVarGraph.getNode(node2_after);

            Edge _edge = new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());

            if (!collapsedVarGraph.containsEdge(_edge)) {
                collapsedVarGraph.addEdge(_edge);
            }
        }

        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(timeLags.getNumRows(),
                timeSeries.getNumColumns());
        Regression regression = new RegressionDatasetGeneralized(timeLags);

        for (int i = 0; i < timeSeries.getNumColumns(); i++) {
            Node target = timeLags.getVariable(i);

            List<Node> regressors = new ArrayList<Node>();

            // Collect up parents from each lagged variable behind
            // timelags.getVariable(i).
            for (int j = 0; j <= 0 /*numLags*/; j++) {
                Node variable = timeLags.getVariable(i + j * timeSeries.getNumColumns());
                regressors.addAll(graph.getParents(variable));
            }

            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }


        return new VarResult(ColtDataSet.makeContinuousData(timeSeries.getVariables(), residuals),
                collapsedVarGraph);
    }

    public static class VarResult {
        private DataSet residuals;
        private Graph collapsedVarGraph;

        public VarResult(DataSet dataSet, Graph collapsedVarGraph) {
            this.residuals = dataSet;
            this.collapsedVarGraph = collapsedVarGraph;
        }

        public DataSet getResiduals() {
            return residuals;
        }

        public Graph getCollapsedVarGraph() {
            return collapsedVarGraph;
        }
    }


    public static double[] getSelfLoopCoefs(DataSet timeSeries) {
        DataSet timeLags = createLagData(timeSeries, 1);

        double[] coefs = new double[timeSeries.getNumColumns()];

        for (int j = 0; j < timeSeries.getNumColumns(); j++) {
            Node target = timeLags.getVariable(j);
            Node selfLoop = timeLags.getVariable(j + timeSeries.getNumColumns());
            List<Node> regressors = Collections.singletonList(selfLoop);

            Regression regression = new RegressionDatasetGeneralized(timeLags);
            RegressionResult result = regression.regress(target, regressors);
            coefs[j] = result.getCoef()[1];
        }

        return coefs;
    }

    public static double sumOfArCoefficients(DataSet timeSeries, int numLags) {
        DataSet timeLags = createLagData(timeSeries, numLags);
        List<Node> regressors = new ArrayList<Node>();

        for (int i = timeSeries.getNumColumns(); i < timeLags.getNumColumns(); i++) {
            regressors.add(timeLags.getVariable(i));
        }

        Regression regression = new RegressionDatasetGeneralized(timeLags);
        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(timeLags.getNumRows(),
                timeSeries.getNumColumns());

        double sum = 0.0;
        int n = 0;

        for (int i = 0; i < timeSeries.getNumColumns(); i++) {
            Node target = timeLags.getVariable(i);
            RegressionResult result = regression.regress(target, regressors);

            double[] coef = result.getCoef();

//            NumberFormat nf = new DecimalFormat("0.00");
//
//            for (int k = 0; k < coef.length; k++) {
//                System.out.print(nf.format(coef[k]) + " ");
//            }
////
//            System.out.println();

            for (int k = 0; k < coef.length; k++) {
//                if (Math.abs(coef[k]) > 0.3) {
                    sum += coef[k] * coef[k];
                    n++;

//                    System.out.println("sum = " + sum + " n = " + n);
//                }
            }

            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        return sum / n;
    }


    /**
     * Calculates the dth difference of the given data. If d = 0, the original data is returned.
     * If d = 1, the data (with one fewer rows) is returned, with each row subtracted from
     * its successor. If d = 1, the same operation is applied to the result of d = 1. And so on.
     * @param data the data to be differenced.
     * @param d the number of differences to take, >= 0.
     * @return the differenced data.
     */
    public static DataSet difference(DataSet data, int d) {
        if (d == 0) return data;

        DoubleMatrix2D _data = data.getDoubleData();

        for (int k = 1; k <= d; k++) {
            DoubleMatrix2D _data2 = new DenseDoubleMatrix2D(_data.rows() - 1, _data.columns());

            for (int i = 1; i < _data.rows(); i++) {
                for (int j = 0; j < _data.columns(); j++) {
                    _data2.set(i - 1, j, _data.get(i, j) - _data.get(i - 1, j));
                }
            }

            _data = _data2;
        }

        return ColtDataSet.makeContinuousData(data.getVariables(), _data);
    }

    /**
     * Creates new time series dataset from the given one (fixed to deal with mixed datasets)
     */
    public static DataSet createLagData(DataSet data, int numLags) {
        List<Node> variables = data.getVariables();
        int dataSize = variables.size();
        int laggedRows = data.getNumRows() - numLags;
        Knowledge knowledge = new Knowledge();
        Node[][] laggedNodes = new Node[numLags + 1][dataSize];
        List<Node> newVariables = new ArrayList<Node>((numLags + 1) * dataSize + 1);
        for (int lag = 0; lag <= numLags; lag++) {
            for (int col = 0; col < dataSize; col++) {
                Node node = variables.get(col);
                String varName = node.getName();
                Node laggedNode;
                if (node instanceof ContinuousVariable) {
                    laggedNode = new ContinuousVariable(varName + "." + (lag == 0 ? "" : "-") + (lag));
                } else if (node instanceof DiscreteVariable) {
                    DiscreteVariable var = (DiscreteVariable) node;
                    laggedNode = new DiscreteVariable(var);
                    var.setName(varName + "." + (lag == 0 ? "" : "-") + (lag));
                } else {
                    throw new IllegalStateException("Node must be either continuous or discrete");
                }
                newVariables.add(laggedNode);
                laggedNode.setCenter(80 * col + 50, 80 * (numLags - lag) + 50);
                laggedNodes[lag][col] = laggedNode;
                knowledge.addToTier(numLags - lag, laggedNode.getName());
            }
        }
        DataSet laggedData = new ColtDataSet(laggedRows, newVariables);
        for (int lag = 0; lag < numLags + 1; lag++) {
            for (int col = 0; col < dataSize; col++) {
                for (int row = 0; row < laggedRows; row++) {
                    Node laggedNode = laggedNodes[lag][col];
                    if (laggedNode instanceof ContinuousVariable) {
                        double value = data.getDouble(row + numLags - lag, col);
                        laggedData.setDouble(row, col + lag * dataSize, value);
                    } else {
                        int value = data.getInt(row + numLags - lag, col);
                        laggedData.setInt(row, col + lag * dataSize, value);
                    }
                }
            }
        }

        knowledge.setDefaultToKnowledgeLayout(true);
        laggedData.setKnowledge(knowledge);
        return laggedData;
    }
}
