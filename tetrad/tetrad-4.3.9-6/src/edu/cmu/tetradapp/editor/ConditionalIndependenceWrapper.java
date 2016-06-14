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

package edu.cmu.tetradapp.editor;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.*;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.RegressionParams;
import edu.cmu.tetradapp.model.SearchParams;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Doesn't extend any "runner" interfaces because serialization seems to be a huge pain.
 * No reason this class shouldn't be editable in the future, so here it is.
 * In essence, this figures out what sort of regression needs to be run, and displays
 * both the regression results and the conditional independence results.
 *
 * @author Mike Freenor after Frank Wimberly's RegressionRunner after Joe Ramsey's PcRunner
 */
public class ConditionalIndependenceWrapper{

    private String name;
    private RegressionParams params;
    private String targetName;
    private DataModel dataSet;

    private Graph outGraph;

    /**
     * The result of the regression--that is, coefficients, p-values, etc.
     */
    private RegressionResult result;

    /**
     * @deprecated
     */
    private String report;

    public boolean linear; // what the last test was
    public boolean failure; //if the last test failed or not (for whatever reason)
    public LogisticRegression logRegResult;
    public boolean performedSwap; //set to true if Y is discrete and X isn't

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public ConditionalIndependenceWrapper(DataWrapper dataWrapper, RegressionParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        DataModel dataModel = dataWrapper.getSelectedDataModel();

        /*
        if (dataModel instanceof DataSet) {
            DataSet _dataSet = (DataSet) dataModel;
            if (!_dataSet.isContinuous()) {
                throw new IllegalArgumentException("Data set must be continuous.");
            }
        }
        */

        this.params = params;
        this.targetName = params.getTargetName();
        this.dataSet = dataModel;
        this.linear = false;
        this.failure = false;
        this.performedSwap = false;
    }

    //===========================PUBLIC METHODS============================//

    public DataModel getDataSet() {
        //return (DataModel) this.dataWrapper.getDataModelList().get(0);
        return this.dataSet;
    }

    public void resetParams(Object params) {
        //ignore
        //this.params = (RegressionParams) params;
    }

    public void setParams(RegressionParams params) {
        this.params = params;
    }

    public boolean isSearchingOverSubset() {
        return false;
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


    /**
     *
     * Returns the new RegressionParams that it uses, so anything that calls it can look
     * at them.
     *
     */
    public RegressionParams execute(String yName) {
        // check for the existence of X, Y, and conditioning set
        this.failure = false;
        this.performedSwap = false;
        if(this.params.getTargetName() == "")
        {
            return null;    
        }
        // add Y to the front of the conditioning set

        // check to see what kind of vars these are          
        String [] newRegressors = new String[this.params.getRegressorNames().length + 1];
        newRegressors[0] = yName;
        for (int i = 0; i < this.params.getRegressorNames().length; i++)
        {
            newRegressors[i + 1] = this.params.getRegressorNames()[i];
        }

        RegressionParams newParams = new RegressionParams();
        newParams.setAlpha(this.params.getAlpha());
        newParams.setSourceGraph(this.params.getSourceGraph());
        newParams.setVarNames(this.params.getVarNames());
        newParams.setTargetName(this.params.getTargetName());
        newParams.setRegressorNames(newRegressors);

        boolean logisticRegression = false; //flag for what kind of regression is appropriate
        boolean yIsDiscrete = false; //in X_||_Y if X is cont and Y discrete, swap them and do log reg
        boolean xIsDiscrete = false;

        String targetClassName = (((DataSet)dataSet).getVariable(newParams.getTargetName())).getClass().getName();
 
        if (targetClassName.equals("edu.cmu.tetrad.data.DiscreteVariable"))
        {
            if (((DiscreteVariable)(((DataSet)dataSet).getVariable(newParams.getTargetName()))).getCategories().size() > 2)
            {
                this.failure = true;
                return null;
            }
            logisticRegression = true;
            xIsDiscrete = true;
        }
        
        for (int i = 0; i < newParams.getRegressorNames().length; i++)
        {
            String varName = newParams.getRegressorNames()[i];
            String className = (((DataSet)dataSet).getVariable(varName)).getClass().getName();
            if (className.equals("edu.cmu.tetrad.data.DiscreteVariable"))
            {
                // fail if variable isn't binary
                if (((DiscreteVariable)(((DataSet)dataSet).getVariable(varName))).getCategories().size() > 2)
                {
                    this.failure = true;
                    return null;
                }
                
                if (yName.equals(varName))
                {
                    //System.out.println("Y is discrete!");
                    logisticRegression = true;
                    yIsDiscrete = true;
                }
            }
        }

        if (!logisticRegression)
        {
            this.linear = true;
            //linear regression time
            if (newParams.getRegressorNames().length == 0 ||
                newParams.getTargetName() == null) {
                outGraph = new EdgeListGraph();
                return null;
            }

            if (Arrays.asList(newParams.getRegressorNames()).contains(
                    newParams.getTargetName())) {
                outGraph = new EdgeListGraph();
                return null;
            }

            Regression regression;
            Node target;
            List<Node> regressors;

            if (dataSet instanceof DataSet) {
                DataSet _dataSet = (DataSet) dataSet;
                regression = new RegressionDataset(_dataSet);
                target = _dataSet.getVariable(newParams.getTargetName());
                String[] regressorNames = newParams.getRegressorNames();
                regressors = new LinkedList<Node>();

                for (String regressorName : regressorNames) {
                    regressors.add(_dataSet.getVariable(regressorName));
                }

                double alpha = newParams.getAlpha();
                regression.setAlpha(alpha);

                result = regression.regress(target, regressors);
                outGraph = regression.getGraph();
            }
            else if (dataSet instanceof CovarianceMatrix) {
                CovarianceMatrix covariances = (CovarianceMatrix) dataSet;
                regression = new RegressionCovariance(covariances);
                target = covariances.getVariable(newParams.getTargetName());
                String[] regressorNames = newParams.getRegressorNames();
                regressors = new LinkedList<Node>();

                for (String regressorName : regressorNames) {
                    regressors.add(covariances.getVariable(regressorName));
                }

                double alpha = newParams.getAlpha();
                regression.setAlpha(alpha);

                result = regression.regress(target, regressors);
                outGraph = regression.getGraph();
            }

            setResultGraph(outGraph);
        }
        else
        {
            this.linear = false;
            //logistic regression time
            //System.out.println("Performing logistic regression.");

            DenseDoubleMatrix2D data = (DenseDoubleMatrix2D)(((DataSet)dataSet).getDoubleData());
            double[][] dataMatrix = data.toArray();

            if (!xIsDiscrete && yIsDiscrete)
            {
                //swap x and y if x is continuous and y is discrete
                newRegressors[0] = newParams.getTargetName();
                newParams.setTargetName(yName);
                newParams.setRegressorNames(newRegressors);
                this.performedSwap = true;
            }

            double[] target = new double[dataMatrix.length];
            for (int i = 0; i < target.length; i++)
            {
                if(!xIsDiscrete && yIsDiscrete)
                {
                    target[i] = dataMatrix[i][0];
                }
                else
                {
                    target[i] = dataMatrix[i][0];
                }
            }

            int regressorIndices[] = new int[newParams.getRegressorNames().length];

            for (int i = 0; i < newParams.getRegressorNames().length; i++)
                regressorIndices[i] = (((DataSet)dataSet).getColumn(((DataSet)dataSet).getVariable(newParams.getRegressorNames()[i])));
                        

            double[][] regressors = new double[newParams.getRegressorNames().length][dataMatrix.length];

            int count = 0;

            for (int i = 0; i < dataMatrix[0].length; i++)
            {
                boolean isAnIndex = false;
                for(int k = 0; k < regressorIndices.length; k++)
                {
                    if (regressorIndices[k] == i)
                    {
                        isAnIndex = true;
                        break;
                    }
                }
                if (isAnIndex)
                {
                    //System.out.println(dataMatrix.length);
                    for (int j = 0; j < dataMatrix.length; j++)
                    {
                        regressors[count][j] = dataMatrix[j][i];
                    }
                    count++;
                }
            }

            LogisticRegression logReg = new LogisticRegression();            
            logReg.setRegressors(regressors);
            logReg.setVariableNames(newParams.getRegressorNames());
            logReg.regress(target, newParams.getTargetName());
            this.logRegResult = logReg;
            setResultGraph(logReg.getOutGraph());
        }

        /*

        */
        return newParams;
    }

    public boolean supportsKnowledge() {
        return false;
    }

    public ImpliedOrientation getMeekRules() {
        throw new UnsupportedOperationException();
    }

    public RegressionResult getResult() {
        return result;
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


