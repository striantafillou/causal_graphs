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

package edu.cmu.tetrad.data;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.RandomUtil;

import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/**
 * Some static utility methods for dealing with data sets.
 *
 * @author Various folks.
 */
public final class DataUtils {


    public static void copyColumn(Node node, DataSet source, DataSet dest) {
        int sourceColumn = source.getColumn(node);
        int destColumn = dest.getColumn(node);
        if(sourceColumn < 0){
            throw new NullPointerException("The given node was not in the source dataset");
        }
        if(destColumn < 0){
            throw new NullPointerException("The given node was not in the destination dataset");
        }
        int sourceRows = source.getNumRows();
        int destRows = dest.getNumRows();
        if (node instanceof ContinuousVariable) {
            for (int i = 0; i < destRows && i < sourceRows; i++) {
               dest.setDouble(i, destColumn, source.getDouble(i, sourceColumn));
            }
        } else if (node instanceof DiscreteVariable) {
            for(int i = 0; i<destRows && i < sourceRows; i++){
                dest.setInt(i, destColumn, source.getInt(i, sourceColumn));
            }
        } else {
            throw new IllegalArgumentException("The given variable most be discrete or continuous");
        }
    }


    /**
     * States whether the given column of the given data set is binary.
     * @param data Ibid.
     * @param column Ibid.
     * @return true iff the column is binary.
     */
    public static boolean isBinary(DataSet data, int column) {
        Node node = data.getVariable(column);
        int size = data.getNumRows();
        if (node instanceof DiscreteVariable) {
            for (int i = 0; i < size; i++) {
                int value = data.getInt(i, column);
                if (value != 1 && value != 0) {
                    return false;
                }
            }
        } else if (node instanceof ContinuousVariable) {
            for (int i = 0; i < size; i++) {
                double value = data.getDouble(i, column);
                if (value != 1.0 && value != 0.0) {
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("The given column is not discrete or continuous");
        }
        return true;
    }


    /**
     * Throws an exception just in case not all of the variables from
     * <code>source1</code> are found in <code>source2</code>. A variable from
     * <code>source1</code> is found in <code>source2</code> if it is equal to a
     * variable in <code>source2</code>.
     * @param source1 The first variable source.
     * @param source2 The second variable source. (See the interface.)
     */
    public static void ensureVariablesExist(VariableSource source1,
                                            VariableSource source2) {
        List<Node> variablesNotFound = source1.getVariables();
        variablesNotFound.removeAll(source2.getVariables());

        if (!variablesNotFound.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expected to find these variables from the given Bayes PM " +
                            "\nin the given discrete data set, but didn't (note: " +
                            "\ncategories might be different or in the wrong order): " +
                            "\n" + variablesNotFound);
        }
    }

    /**
     * Returns the default category for index i. (The default category should
     * ALWAYS be obtained by calling this method.)
     * @param index Ond plus the given index.
     */
    public static String defaultCategory(int index) {
        return Integer.toString(index);
    }

    /**
     * Adds missing data values to cases in accordance with probabilities
     * specified in a double array which has as many elements as there are
     * columns in the input dataset.  Hence if the first element of the array of
     * probabilities is alpha, then the first column will contain a -99 (or
     * other missing value code) in a given case with probability alpha. </p>
     * This method will be useful in generating datasets which can be used to
     * test algorithms that handle missing data and/or latent variables. </p>
     * Author:  Frank Wimberly
     * @param inData The data to which random missing data is to be added.
     * @param probs The probability of adding missing data to each column.
     * @return The new data sets with missing data added.
     */
    public static DataSet addMissingData(
            DataSet inData, double[] probs) {
        DataSet outData;

        try {
            outData = (DataSet) new MarshalledObject(inData).get();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (probs.length != outData.getNumColumns()) {
            throw new IllegalArgumentException(
                    "Wrong number of elements in prob array");
        }

        for (double prob : probs) {
            if (prob < 0.0 || prob > 1.0) {
                throw new IllegalArgumentException("Probability out of range");
            }
        }

        for (int j = 0; j < outData.getNumColumns(); j++) {
            Node variable = outData.getVariable(j);

            for (int i = 0; i < outData.getNumRows(); i++) {
                double test = RandomUtil.getInstance().nextDouble();

                if (test < probs[j]) {
                    outData.setObject(i, j,
                            ((Variable) variable).getMissingValueMarker());
                }
            }
        }

        return outData;
    }

    /**
     * A continuous data set used to construct some other serializable
     * instances.
     */
    public static DataSet continuousSerializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        variables.add(new ContinuousVariable("X"));
        ColtDataSet dataSet = new ColtDataSet(10, variables);

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                dataSet.setDouble(i, j, RandomUtil.getInstance().nextDouble());
            }
        }

        return dataSet;
    }

    /**
     * A discrete data set used to construct some other serializable instances.
     */
    public static DataSet discreteSerializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        variables.add(new DiscreteVariable("X", 2));
        DataSet dataSet = new ColtDataSet(2, variables);
        dataSet.setInt(0, 0, 0);
        dataSet.setInt(1, 0, 1);
        return dataSet;
    }

    /**
     * Returns true iff the data sets contains a missing value.
     */
    public static boolean containsMissingValue(DoubleMatrix2D data) {
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                if (Double.isNaN(data.getQuick(i, j))) {
                    return true;
                }
            }
        }

        return false;
    }


    public static boolean containsMissingValue(DataSet data) {
        for (int j = 0; j < data.getNumColumns(); j++) {
            Node node = data.getVariable(j);

            if (node instanceof ContinuousVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (Double.isNaN(data.getDouble(i, j))) {
                        return true;
                    }
                }
            }

            if (node instanceof DiscreteVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (data.getDouble(i, j) == -99) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static DoubleMatrix2D standardizeData(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int j = 0; j < data.columns(); j++) {
            double sum = 0.0;

            for (int i = 0; i < data.rows(); i++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.rows();

            double norm = 0.0;

            for (int i = 0; i < data.rows(); i++) {
                double v = data.get(i, j) - mean;
                norm += v * v;
            }

            norm /= data.rows();

            norm = Math.sqrt(norm);

            for (int i = 0; i < data.rows(); i++) {
                data2.set(i, j, (data.get(i, j) - mean) / norm);
            }
        }

        return data2;
    }

    public static DataSet discretize(DataSet dataSet, int numCategories) {

        List<String> categories = new ArrayList<String>();

        for (int i = 0; i < numCategories; i++) {
            categories.add(Integer.toString(i));
        }

        List<Node> variables = new ArrayList<Node>();

        for (Node node : dataSet.getVariables()) {
            Node variable = new DiscreteVariable(node.toString(), categories);
            variables.add(variable);
        }

        DataSet discreteData = new ColtDataSet(dataSet.getNumRows(),
                variables);

        DoubleMatrix2D data = dataSet.getDoubleData();

//        println(data);

        for (int j = 0; j < data.columns(); j++) {
            double[] columnData = data.viewColumn(j).toArray();
            ContinuousDiscretizationSpec spec = getEqualFreqDiscretizationSpec(numCategories, columnData);

            Discretizer.Discretization discretization = Discretizer.discretize(columnData,
                    spec.getBreakpoints(), dataSet.getVariables().get(j).getName(),
                    spec.getCategories());
            int[] discretizedData = discretization.getData();

            for (int i = 0; i < data.rows(); i++) {
                discreteData.setInt(i, j, discretizedData[i]);
            }
        }

//        println(discreteData);

        return discreteData;
    }

    /**
     * Calculates the equal freq discretization spec
     */
    public static ContinuousDiscretizationSpec getEqualFreqDiscretizationSpec(int numCategories, double[] data) {
        double[] breakpoints = Discretizer.getEqualFrequencyBreakPoints(data, numCategories);
        List<String> cats = defaultCategories(numCategories);
        return new ContinuousDiscretizationSpec(breakpoints, cats);
    }

    public static List<String> defaultCategories(int numCategories) {
        List<String> categories = new LinkedList<String>();
        for (int i = 0; i < numCategories; i++) {
            categories.add(defaultCategory(i));
        }
        return categories;
    }

    public static List<Node> createContinuousVariables(String[] varNames) {
        List<Node> variables = new LinkedList<Node>();

        for (String varName : varNames) {
            variables.add(new ContinuousVariable(varName));
        }

        return variables;
    }

    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static DoubleMatrix2D subMatrix(CovarianceMatrix m, Node x, Node y, List<Node> z) {
        if (x == null) {
            throw new NullPointerException();
        }

        if (y == null) {
            throw new NullPointerException();
        }

        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        List<Node> variables = m.getVariables();
        DoubleMatrix2D _covMatrix = m.getMatrix();

        // Create index array for the given variables.
        int[] indices = new int[2 + z.size()];

        indices[0] = variables.indexOf(x);
        indices[1] = variables.indexOf(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = variables.indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix = _covMatrix.viewSelection(indices, indices);

        if (containsMissingValue(submatrix)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        return submatrix;
    }

    /**
     * Returns a new data sets, copying the given on but with the columns shuffled.
     * @param dataSet The data set to shuffle.
     * @return Ibid.
     */
    public static DataSet shuffleColumns(DataSet dataSet) {
        int numVariables;

        numVariables = dataSet.getNumColumns();

        List<Integer> indicesList = new ArrayList<Integer>();
        for (int i = 0; i < numVariables; i++) indicesList.add(i);
        Collections.shuffle(indicesList);

        int[] indices = new int[numVariables];

        for (int i = 0; i < numVariables; i++) {
            indices[i] = indicesList.get(i);
        }

        return dataSet.subsetColumns(indices);
    }

    public static DataSet convertNumericalDiscreteToContinuous(
            DataSet dataSet) throws NumberFormatException {
        List<Node> variables = new ArrayList<Node>();

        for (Node variable : dataSet.getVariables()) {
            if (variable instanceof ContinuousVariable) {
                variables.add(variable);
            }
            else {
                variables.add(new ContinuousVariable(variable.getName()));
            }
        }

        DataSet continuousData = new ColtDataSet(dataSet.getNumRows(),
                variables);

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            Node variable = dataSet.getVariable(j);

            if (variable instanceof ContinuousVariable) {
                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    continuousData.setDouble(i, j, dataSet.getDouble(i, j));
                }
            }
            else {
                DiscreteVariable discreteVariable = (DiscreteVariable) variable;

                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    int index = dataSet.getInt(i, j);
                    String catName = discreteVariable.getCategory(index);
                    double value = Double.parseDouble(catName);
                    continuousData.setDouble(i, j, value);
                }
            }
        }

        return continuousData;
    }
}


