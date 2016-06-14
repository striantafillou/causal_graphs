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

import edu.cmu.tetrad.bayes.*;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndTestChiSquare;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains a method classify which uses a DataSet to create an
 * instantiated Bayes net (BayesIm) by using the estimate method of that class.
 * Then for each case (record) in  the second DataSet it uses the values of all
 * variables but the target variable to update the distributions of all the
 * variables.  It then computes an estimated value for the target variable by
 * selecting the value with the greatest probability in the update
 * distribution. The method returns a crosstabulation table in the form of a
 * two-dimensional integer array in which counts of observed versus estimated
 * values of the target variable are stored. </p> Note that the variables will
 * be the same in the two datasets but, usually, the values in each case will be
 * different.
 *
 * @author Frank Wimberly based on a specification by Clark Glymour
 */
public final class HitonClassifier implements DiscreteClassifier {

    /**
     * The dataset used to create the instantiated Bayes net.
     */
    private DataSet trainingData;

    /**
     * The dataset to be classified.
     */
    private DataSet testingData;

    /*
     * The name of the target variable.
     */
    private String target;

    /*
     * The percentage of correct estimates of the target variable.  This will be set
     * to a meaningful value upon completion of the crossTabulate method.
     */
    private double percentCorrect;

    /*
     * The level of significance of the independence test.
     */
    private double alpha;

    /*
     * The depth of the PC search.
     */
    private int depth;

    /*
     * The target variable (inferred from its name)
     */
    private DiscreteVariable targetVariable;

    /*
     * The list of nodes in the Markov blanket of the target variable in the training
     * dataset.
     */
    private List<Node> markovBlanketNodes;

    /*
     * The constructor sets the values of the private member variables.
     */
    public HitonClassifier(DataSet ddsTrain,
            DataSet ddsClassify, String target, double alpha,
            int depth) {

        this.trainingData = ddsTrain;
        this.testingData = ddsClassify;
        this.target = target;
        this.percentCorrect = Double.NaN;
        this.alpha = alpha;
        this.depth = depth;

        List<Node> trainVars = ddsTrain.getVariables();
        List<Node> classifyVars = ddsClassify.getVariables();

        for (int i = 0; i < trainVars.size(); i++) {
            if (!(trainVars.get(i)).equals(classifyVars.get(i))) {
                throw new IllegalArgumentException(
                        "Datasets must contain same vars.");
            }
        }

        //Find the target variable using its name.
        this.targetVariable = null;
        for (Node trainVar : trainVars) {
            DiscreteVariable dv = (DiscreteVariable) trainVar;
            if (dv.getName().equals(target)) {
                this.targetVariable = dv;
                break;
            }
        }

        if (targetVariable == null) {
            throw new IllegalArgumentException("Target variable not in data.");
        }
    }

    /**
     * Computes and returns the crosstabulation of observed versus estimated
     * values of the target variable as described above.
     */
    public int[] classify() {

        //A discrete independence test for the training dataset.
        IndTestChiSquare test = new IndTestChiSquare(trainingData, alpha);
        //        IndTestGSquare2 test = new IndTestGSquare2(trainingData, alpha);

        //Create a PcxSearch and compute the Markov blanket of the target.
        //PcxSearch pcxs = new PcxSearch(test);
        //PcxrSearch pcxs = new PcxrSearch(test, depth);
        HitonOld hitons = new HitonOld(test, depth);
        Graph markovBlanket = hitons.search(target);

        //Debug print
        //LogUtils.getInstance().fine("Markov blanket for " + target);
        //LogUtils.getInstance().fine("" + markovBlanket);

        //Get the nodes of the Markov blanket.
        List<Node> nodes = markovBlanket.getNodes();

        List<Node> variables = new LinkedList<Node>();
        for (Node node : nodes) {
            nodes.add(node);
        }

        this.markovBlanketNodes = variables;

        //LogUtils.getInstance().fine("Vars in training dataset:  ");  //DEBUG
        //LogUtils.getInstance().fine(varsTrain);                      //DEBUG

        //The Markov blanket nodes will correspond to a subset of the variables
        //in the training dataset.  Find the subset dataset.
        DataSet ddsMBTrain =
                trainingData.subsetColumns(markovBlanketNodes);

        //The variables that correspond to the nodes in the Markov blanket.
        List<Node> varsTrain = ddsMBTrain.getVariables();

        //To create a Bayes net for the Markov blanket we need the DAG.
        Dag mbDag = new Dag(markovBlanket);
        BayesPm mbBayesPm = new BayesPm(mbDag);

        //To parameterize the Bayes net we need the number of values
        //of each variable.
        for (int i = 0; i < varsTrain.size(); i++) {
            DiscreteVariable dv = (DiscreteVariable) varsTrain.get(i);
            int ncats = dv.getNumCategories();

            //LogUtils.getInstance().fine("Num categories for " + name + " = " + ncats);
            mbBayesPm.setNumCategories(markovBlanketNodes.get(i), ncats);
        }

        //Use the training dataset to estimate values for the Bayes net's
        //parameters using the training dataset and the estimate method.
        MlBayesEstimator estimator = new MlBayesEstimator();
        MlBayesIm mbBayesIm =
                (MlBayesIm) estimator.estimate(mbBayesPm, ddsMBTrain);

        //Create an updater for the instantiated Bayes net.
        BayesUpdater bayesUpdaterMb = new RowSummingExactUpdater(mbBayesIm);

        //The subset dataset of the dataset to be classified containing
        //the variables in the Markov blanket.
        DataSet ddsMBClassify =
                testingData.subsetColumns(markovBlanketNodes);

        //Get the raw data from the dataset to be classified, the number
        //of variables and the number of cases.
        int ncases = ddsMBClassify.getNumRows();
        int[] estimatedValues = new int[ncases];
        Arrays.fill(estimatedValues, -1);

        //The variables in the dataset.
        List<Node> varsClassify = ddsMBClassify.getVariables();

        //LogUtils.getInstance().fine("Vars in other dataset:  ");  //DEBUG
        //LogUtils.getInstance().fine(varsClassify);                //DEBUG

        //For each case in the dataset to be classified compute the estimated
        //value of the target variable and increment the appropriate element
        //of the crosstabulation array.
        for (int i = 0; i < ncases; i++) {
            //Compute the estimated value of the target variable by using
            //the observed values of the other variables and Bayesian updating.

            //Create an Evidence instance for the instantiated Bayes net
            //which will allow that updating.
            Evidence evidence = Evidence.tautology(mbBayesIm);

            //Let the target variable range over all its values.
            int itarget = evidence.getNodeIndex(target);
            evidence.getProposition().setVariable(itarget, true);

            //Restrict all other variables to their observed values in
            //this case.
            for (int j = 0; j < varsClassify.size(); j++) {
                if (j == varsClassify.indexOf(targetVariable)) {
                    continue;
                }
                String other = varsClassify.get(j).getName();
                int iother = evidence.getNodeIndex(other);

                evidence.getProposition().setCategory(iother,
                        ddsMBClassify.getInt(i, j));

            }

            //DEBUG Print
            //LogUtils.getInstance().fine("Proposition = " + evidence.getProposition());

            //Update using those values.
            bayesUpdaterMb.setEvidence(evidence);
            BayesIm updatedIM = bayesUpdaterMb.getBayesIm();

            //for each possible value of target compute its probability in
            //the updated Bayes net.  Select the value with the highest
            //probability as the estimated value.
            int indexTargetBN = updatedIM.getNodeIndex(targetVariable);
            //LogUtils.getInstance().fine("indexTargetBN = " + indexTargetBN);  //DEBUG

            //Straw man values--to be replaced.
            double highestProb = -0.1;
            int estimatedValue = -1;

            for (int k = 0; k < targetVariable.getNumCategories(); k++) {
                //Debug print
                //LogUtils.getInstance().fine("marginal = "
                //                    + bayesUpdaterMb.getMarginal(indexTargetBN, k));

                if (bayesUpdaterMb.getMarginal(indexTargetBN, k) >= highestProb)
                {
                    highestProb = bayesUpdaterMb.getMarginal(indexTargetBN, k);
                    estimatedValue = k;
                }
            }

            //Sometimes the marginal cannot be computed because certain
            //combinations of values of the variables do not occur in the
            //training dataset.  If that happens skip the case.
            if (estimatedValue < 0) {
                //Debug print
                TetradLogger.getInstance().details("Case " + i + " does not return valid marginal.");
                continue;
            }

            estimatedValues[i] = estimatedValue;

        }

        return estimatedValues;
    }

    /**
     * Computes the "confusion matrix" of counts of the number of cases
     * associated with each combination of estimated and observed values in the
     * test dataset.  Each row, column i,j corresponds to the ith and jth
     * categories of the target variable.
     *
     * @return an int[][] array containing the counts.
     */
    public int[][] crossTabulation() {

        int[] estimatedValues = classify();
        DataSet ddsMBClassify =
                testingData.subsetColumns(markovBlanketNodes);

        List<Node> varsClassify = ddsMBClassify.getVariables();

        //Find the index in the test dataset of the target variable.
        int indexTargetDDS = varsClassify.indexOf(targetVariable);

        //Create a crosstabulation table to store the counts of observed
        //versus estimated occurrences of each value of the target variable.
        int nvalues = targetVariable.getNumCategories();
        int[][] crosstabs = new int[nvalues][nvalues];
        for (int i = 0; i < nvalues; i++) {
            for (int j = 0; j < nvalues; j++) {
                crosstabs[i][j] = 0;
            }
        }
        int ntot = 0;

//        int ncases = rawData[0].length;
        int ncases = ddsMBClassify.getNumRows();

        int numberCorrect =
                0;  //Will count the number of cases where the target variable
        //is correctly classified.

        for (int i = 0; i < ncases; i++) {
            int estimatedValue = estimatedValues[i];
//            int observedValue = rawData[indexTargetDDS][i];
            int observedValue = ddsMBClassify.getInt(i, indexTargetDDS);

            if (estimatedValue < 0) {
                continue;
            }
            ntot++;
            crosstabs[observedValue][estimatedValue]++;
            if (observedValue == estimatedValue) {
                numberCorrect++;
            }

        }

        this.percentCorrect =
                100.0 * ((double) numberCorrect) / ((double) ncases);

        TetradLogger.getInstance().details("Total no usable cases= " + ntot + " out of " + ncases);

        return crosstabs;
    }

    /**
     * @return the percentage of cases in which the target variable is correctly
     *         classified.
     */
    public double getPercentCorrect() {
        if (Double.isNaN(percentCorrect)) {
            crossTabulation();
        }
        return percentCorrect;
    }

    /**
     * @return the DiscreteVariable which is the target variable.
     */
    public DiscreteVariable getTargetVariable() {
        return targetVariable;
    }

    /*  This may eventually replace the calculation above and may be used
        to get estimated values apart from the process of computing the
        crosstabulation of observed versus estimated values.
    public int getEstimatedValue(BayesIm bayesIm, int[][] data, i) {
      Evidence evidence = new Evidence(bayesIm);
      int itarget = evidence.getNodeIndex(target);

      //Let the target variable range over all its values.
      evidence.getProposition().setVariable(itarget, true);

      List vars = bayesIm.getVariableNames();
      //Restrict all other variables to their observed values in this case.
      for(int j = 0; j < vars.size(); j++) {
        if(j == vars.get(j).getName().equals(target)) continue;
      }
    }
    */
}

