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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.LogisticRegression;
import edu.cmu.tetrad.regression.LogisticRegressionResult;
import edu.cmu.tetrad.search.ImpliedOrientation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the Regression
 * algorithm.
 *
 * @author Frank Wimberly after Joe Ramsey's PcRunner
 */
public class LogisticRegressionRunner implements AlgorithmRunner {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private LogisticRegressionParams params;

    /**
     * @serial Cannot be null.
     */
    private String targetName;

    /**
     * @serial Cannot be null.
     */
    private DataSet dataSet;

    /**
     * @serial Can be null.
     */
    private String report;

    /**
     * @serial Can be null.
     */
    private Graph outGraph;


    /**
     *@serial Can be null.
     */
    private LogisticRegressionResult result;


    private double[] coefficients;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public LogisticRegressionRunner(DataWrapper dataWrapper,
            LogisticRegressionParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (!(dataModel instanceof DataSet)) {
            throw new IllegalArgumentException("Data set must be tabular.");
        }

        DataSet dataSet =
                (DataSet) dataModel;

//        if (!ds.isContinuous()) {
//            throw new IllegalArgumentException("Data set must be continuous");
//        }

        this.params = params;
        this.targetName = params.getTargetName();
        this.dataSet = dataSet;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LogisticRegressionRunner serializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        ContinuousVariable var1 = new ContinuousVariable("X");
        ContinuousVariable var2 = new ContinuousVariable("Y");

        variables.add(var1);
        variables.add(var2);

        DataSet dataSet = new ColtDataSet(3, variables);
        double[] col1data = new double[]{0.0, 1.0, 2.0};
        double[] col2data = new double[]{2.3, 4.3, 2.5};

        for (int i = 0; i < 3; i++) {
            dataSet.setDouble(i, 0, col1data[i]);
            dataSet.setDouble(i, 1, col2data[i]);
        }

        DataWrapper dataWrapper = new DataWrapper(dataSet);
        return new LogisticRegressionRunner(dataWrapper,
                LogisticRegressionParams.serializableInstance());
    }

    //===========================PUBLIC METHODS============================//

    public DataModel getDataModel() {
        //return (DataModel) this.dataWrapper.getDataModelList().get(0);
        return this.dataSet;
    }

    public void resetParams(Object params) {
        //ignore
        //this.params = (RegressionParams) params;
    }

    public void setParams(LogisticRegressionParams params) {
        this.params = params;
    }


    /**
     * Returns the alpha or -1.0 if the params aren't set.     
     */
    public double getAlpha(){
        if(this.params != null){
            return this.params.getAlpha();
        }
        return -1.0;
    }


    public boolean isSearchingOverSubset() {
        return false;
    }

    public LogisticRegressionResult getResult(){
        return this.result;
    }

    public SearchParams getParams() {
        return params;
    }

    public Graph getResultGraph() {
        return outGraph;
    }

    public void setResultGraph(Graph graph) {
        this.outGraph = graph;
    }

    public Graph getSourceGraph() {
        return null;
    }
    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {

        if (params.getRegressorNames().length == 0 ||
                params.getTargetName() == null) {
            report = "Response and predictor variables not set.";
            outGraph = new EdgeListGraph();
            return;
        }

        if (Arrays.asList(params.getRegressorNames()).contains(
                params.getTargetName())) {
            report = "Response var must not be a predictor.";
            outGraph = new EdgeListGraph();
            return;
        }

        //Regression regression = new Regression();
        //String targetName = ((RegressionParams) getParams()).getTargetName();
        String targetName = params.getTargetName();
        double alpha = params.getAlpha();

        DataSet regressorsDataSet =
                new ColtDataSet((ColtDataSet) dataSet);
        Node variable = regressorsDataSet.getVariable(targetName);
        int targetIndex = dataSet.getVariables().indexOf(variable);
        regressorsDataSet.removeColumn(variable);

        Object[] namesObj = (regressorsDataSet.getVariableNames()).toArray();
        String[] names = new String[namesObj.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = (String) namesObj[i];
        }

        //Get the list of regressors selected by the user
        String[] regressorNames = params.getRegressorNames();
        List regressorNamesList = Arrays.asList(regressorNames);

        //If the user selected none, use them all
        if (regressorNames.length > 0) {
            for (String name1 : names) {
                Node regressorVar = regressorsDataSet.getVariable(name1);
                if (!regressorNamesList.contains(regressorVar.getName())) {
                    regressorsDataSet.removeColumn(regressorVar);
                }
            }
        }
        else {
            regressorNames = names;  //All names except the target
        }

        //double[][] regressorsT = regressorsDataSet.getDoubleData();
        //int ncases = regressorsT.length;
        //int nvars = regressorsT[0].length;

        int ncases = regressorsDataSet.getNumRows();
        int nvars = regressorsDataSet.getNumColumns();

        double[][] regressors = new double[nvars][ncases];

        for (int i = 0; i < nvars; i++) {
            for (int j = 0; j < ncases; j++) {
                //regressors[i][j] = regressorsT[j][i];
                regressors[i][j] = regressorsDataSet.getDouble(j, i);
            }
        }

        //target is the array storing the values of the target variable
        double[] target = new double[ncases];

        for (int j = 0; j < ncases; j++) {
            target[j] = dataSet.getDouble(j, targetIndex);
        }

        LogisticRegression logRegression = new LogisticRegression();
        logRegression.setRegressors(regressors);
        logRegression.setVariableNames(regressorNames);
        logRegression.setAlpha(alpha);

        report = logRegression.regress(target, targetName);
        this.result = logRegression.getResult();
        coefficients = logRegression.getCoefficients();
        outGraph = logRegression.getOutGraph();
    }

    public boolean supportsKnowledge() {
        return false;
    }

    public ImpliedOrientation getMeekRules() {
        throw new UnsupportedOperationException();
    }

    public String getReport() {
        return report;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public Graph getOutGraph() {
        return outGraph;
    }

    public String getTargetName() {
        return targetName;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (params == null) {
            throw new NullPointerException();
        }

        /*
        if (targetName == null) {
            throw new NullPointerException();
        }
        */

        if (dataSet == null) {
            throw new NullPointerException();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Graph getGraph() {
        return outGraph;
    }
}



