package edu.cmu.tetrad.manip.sample;


import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.DataSetProbs;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.bayes.Proposition;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.VariableSource;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariablesStudied;
import edu.cmu.tetrad.manip.population.CorrectManipulatedGraphBayesIm;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA. User: Matthew Easterday Date: Oct 20, 2003 Time:
 * 3:44:31 PM To change this template use Options | File Templates.
 * <p/>
 * This describes the Bayes sample.
 */
public class BayesSample implements Sample {

    private static int VALUE_NOT_VIEWED = -1;
    private DataSetProbs dataSetProbs;
    private MlBayesIm correctManipulatedGraphIM;


    /**
     * The BayesSample basically consists of two tables: sample cases, and
     * sample case frequencies.  The first table has the same number of columns
     * as studied variables in the experimental setup.  Each row is a sample
     * case--an "measured" instance observed from some population.  Each cell in
     * the sample case is a possible value for the variable represented in
     * the column.  For example, if the columns are "education" "income" and
     * "happiness", then the first row/sample case might be "high school"
     * "50,000" "very happy".
     * <p/>
     * The second table, the sample case frequencies, is just the number of
     * times a given sample case occurs.  This table has the same structure as
     * the Population table, i.e. each row is a possible sample case, but in
     * this table, the last column is a double representing the frequency with
     * which this possible sample case occurs in the sample case tables.  The
     * rows in the sample case frequency table are the sampe combinations of
     * varaible values as in the population table.
     *
     * @param correctIm
     * @param experiment
     * @param sampleSize
     */
    public BayesSample(BayesIm correctIm,
                       ExperimentalSetup experiment,
                       int sampleSize,
                       long sampleSeed) {

        correctManipulatedGraphIM = CorrectManipulatedGraphBayesIm.createIm(correctIm, experiment);
        DataSet dataSet = correctManipulatedGraphIM.simulateData(sampleSize, sampleSeed, false);
        //DiscreteDataSet dataSet = new DiscreteDataSet(data);
        dataSetProbs = new DataSetProbs(dataSet);
    }

    /**
     * @return the dataset associated with this sample.
     */
    public DataSet getDataSet() {
        return dataSetProbs.getDataSet();
    }

    private int getVariableIndexInData(String varName) throws IllegalArgumentException {
        Iterator it;
        int index;
        for (index = 0, it = getDataSet().getVariableNames().iterator(); it.hasNext(); index++)
        {
            String name = (String) it.next();
            if (name.equals(varName)) {
                return index;
            }
        }

        throw new IllegalArgumentException("That varName does not appear in the data"); //$NON-NLS-1$
    }

    private int getVariableValueIndexInData(String varName, String varValue, BayesIm IM) {
        return IM.getBayesPm().getCategoryIndex(IM.getBayesPm().getDag().getNode(varName), varValue);
    }

    /**
     * Given pair(s) of variable names and values, derive for the probability
     * for those values of the variables.
     *
     * @param nameValuePairs the constraints state that the variables should
     *                       have
     * @return the probability, eg P(education="college")
     */
    public double getHistogramProbabilityOfVarValue(Properties nameValuePairs) {
        return getHistogramProbability(nameValuePairs, correctManipulatedGraphIM);
    }

    private double getHistogramProbability(Properties nameValuePairs, BayesIm IM) {
        int [] indexCombination = new int[getDataSet().getNumColumns()];

        //initialize the indexCombination to not viewed
        for (int i = 0; i < indexCombination.length; i++) {
            indexCombination[i] = VALUE_NOT_VIEWED;
        }

        for (Enumeration varNames = nameValuePairs.propertyNames(); varNames.hasMoreElements();)
        {
            String varName = (String) varNames.nextElement();
            String varValue = nameValuePairs.getProperty(varName);
            indexCombination[getVariableIndexInData(varName)] =
                    getVariableValueIndexInData(varName, varValue, IM);
        }
        Proposition proposition = createProposition(dataSetProbs.getDataSet(), indexCombination);
        return dataSetProbs.getProb(proposition);
    }

    //-------------- METHODS FOR SAMPLE CASES ------------------------------

    /**
     * Calculates how many rows there should be in the sample cases table given
     * the variables that are being studied
     *
     * @return the number of rows in the sample cases table.
     */
    public int getNumSampleCases() {
        return dataSetProbs.getDataSet().getNumRows();// getMaxRowCount();
    }

    /**
     * @param studiedVariables
     * @return a string array of the names of the sample case columns.
     */
    public String [] getSampleCaseColumnNames(VariablesStudied studiedVariables) {
        int i, j;
        //String [] varNames = dataSetProbs.getDataModel().getVariableNames();
        //String [] varNames = (String[]) dataSetProbs.getDataModel().getVariableNames().toArray();

        Iterator it;
        String [] varNames = new String[dataSetProbs.getDataSet().getVariableNames().size()];
        for (i = 0, it = dataSetProbs.getDataSet().getVariableNames().iterator(); it.hasNext(); i++)
        {
            varNames[i] = (String) it.next();
        }

        String [] names = new String [studiedVariables.getNumVariablesStudied() + 1];

        names[0] = "#";
        for (i = 0, j = 1; i < dataSetProbs.getDataSet().getNumColumns(); i++) {
            if (studiedVariables.isVariableStudied(varNames[i])) {
                names[j++] = varNames[i];
            }
        }
        return names;
    }

    /**
     * Returns a string representing a single sample case, i.e. if the variables
     * are Education, income, & happiness, then a sample case might be: "High
     * school, $50,000, very happy".
     *
     * @param row              which sample cases to generate.
     * @param studiedVariables the variables the user is examining, in the
     *                         example above, educaiton, income & happiness.
     * @return an array of strings where each string is the value of one
     *         variable.
     */
    public String [] getSampleCase(int row, VariablesStudied studiedVariables) {
        int i, j;
        Iterator it;
        //String [] varNames = dataSetProbs.getDataModel().getVariableNames();
        //String [] varNames = (String[]) dataSetProbs.getDataModel().getVariableNames();

        String [] varNames = new String[dataSetProbs.getDataSet().getVariableNames().size()];
        for (i = 0, it = dataSetProbs.getDataSet().getVariableNames().iterator(); it.hasNext(); i++)
        {
            varNames[i] = (String) it.next();
        }

        DataSet dataSet = dataSetProbs.getDataSet();
        String [] sampleCase = new String[studiedVariables.getNumVariablesStudied() + 1];

        sampleCase[0] = new Integer(row + 1).toString();
        for (i = 0, j = 1; i < dataSet.getNumColumns(); i++) {
            if (studiedVariables.isVariableStudied(varNames[i])) {
                sampleCase[j++] = dataSet.getObject(row, i).toString();
            }
        }
        return sampleCase;
    }

    //---------------- METHODS FOR SAMPLE CASE FREQUENCIES ----------------------------

    /**
     * Gets the number of rows in the second table, i.e. the sample case
     * frequencies table.
     *
     * @param studiedVariables the variables the user is examining.
     * @return the number of rows / possible sample cases.
     */
    public int getNumSampleCaseFrequencies(VariablesStudied studiedVariables) {
        int numSampleCases = 1;
        Node node;
        int numValues;
        //String [] varNames = dataSetProbs.getDataModel().getVariableNames();
        //String [] varNames = (String[]) dataSetProbs.getDataModel().getVariableNames().toArray();
        int i;
        Iterator it;

        String [] varNames = new String[dataSetProbs.getDataSet().getVariableNames().size()];
        for (i = 0, it = dataSetProbs.getDataSet().getVariableNames().iterator(); it.hasNext(); i++)
        {
            varNames[i] = (String) it.next();
        }

        if (studiedVariables.getNumVariablesStudied() == 0) {
            return 0;
        }
        for (i = 0; i < varNames.length; i++) {
            if (studiedVariables.isVariableStudied(varNames[i])) {
                node = correctManipulatedGraphIM.getNode(varNames[i]);
                numValues = correctManipulatedGraphIM.getBayesPm().getNumCategories(node);
                numSampleCases *= numValues;
            }
        }
        return numSampleCases;
    }

    /**
     * @param studiedVariables
     * @return the string array of the names of the sample frequency columns.
     */
    public String [] getSampleFrequenciesColumnNames(VariablesStudied studiedVariables) {
        String [] columns = new String[studiedVariables.getNumVariablesStudied() + 1];

        String [] names = studiedVariables.getNamesOfStudiedVariables();
        System.arraycopy(names, 0, columns, 0, names.length);
        columns[columns.length - 1] = "%"; //$NON-NLS-1$

        return columns;
    }


    /**
     * Gets a string representation of a possible sample case in the sample case
     * frequency table, for example, if the studied variables are education,
     * income, and happiness, then the first possible sample case might be "high
     * school, $50,000, very happy"
     *
     * @param row              the given sample case combination to get.
     * @param studiedVariables the variables the user has made visible.
     * @return the sample combination.
     */
    public String[] getSampleCaseFrequencyCombination(int row, VariablesStudied studiedVariables) {
        dataSetProbs.getDataSet();
        String [] stringCombination = new String[studiedVariables.getNumVariablesStudied()];
        int [] indexCombination;
        int i, j;
        Node node;
        String varName;

        indexCombination = sampleFrequenciesIndexFunction(correctManipulatedGraphIM,
                studiedVariables, dataSetProbs.getDataSet(), row);
        for (i = 0, j = 0; i < indexCombination.length; i++) {
            if (indexCombination[i] != VALUE_NOT_VIEWED) {
                varName = dataSetProbs.getDataSet().getVariable(i).getName();

                node = correctManipulatedGraphIM.getNode(varName);
                stringCombination[j++] = correctManipulatedGraphIM.getBayesPm()
                        .getCategory(node, indexCombination[i]);
            }
        }
        return stringCombination;
    }

    /**
     * Gets the frequency (number of occurances of a particular case / total
     * number of cases observed) of a given sample case from all sample cases
     * observed in the sample case table.
     *
     * @param row              the sample whose frequency you want.
     * @param studiedVariables the variables the user has made visible.
     * @return the frequency in which the sample case occurs in the data.
     */
    public double getSampleCaseFrequency(int row, VariablesStudied studiedVariables) {
        int [] indexCombination = sampleFrequenciesIndexFunction(correctManipulatedGraphIM,
                studiedVariables, dataSetProbs.getDataSet(), row);
        ;
        Proposition proposition = createProposition(dataSetProbs.getDataSet(), indexCombination);
        return dataSetProbs.getProb(proposition);
    }

    //--------------- PRIVATE METHODS -------------------------------------------------

    /**
     * This function converts a row number into a combination of node values,
     * i.e. if the studied variables are education, income, and happiness, and
     * String representation of the first possible sample case is "high school,
     * $50,000, very happy", then the int representation is 0, 0, 0.
     *
     * @param IM               the original correct manipulated graph IM
     * @param studiedVariables the variables the user has made visible
     * @param data             the data set collected from the correct
     *                         manipulated graph IM
     * @param row              the given row /sample case to calculate
     * @return and int representation of the row
     */
    private static int [] sampleFrequenciesIndexFunction(BayesIm IM, VariablesStudied studiedVariables, DataSet data, int row) {
        int productPreviousNumValues = 1;
        int currentNumValues, value, varIndex;
        int [] combination;
        Node node;

        if (data.getNumColumns() == 0) {
            return null;
        }
        combination = new int[data.getNumColumns()];

        for (varIndex = data.getNumColumns() - 1; varIndex >= 0; varIndex--) {
            //Get the node whose index == varIndex
            node = IM.getNode(data.getVariable(varIndex).getName());
            if (studiedVariables.isVariableStudied(node.getName())) {
                currentNumValues = IM.getBayesPm().getNumCategories(node);
                value = (row / productPreviousNumValues) % currentNumValues;
                combination[varIndex] = value;
                productPreviousNumValues = productPreviousNumValues * currentNumValues;
            } else { //filter out unstudied variables
                combination[varIndex] = VALUE_NOT_VIEWED;
            }
        }
        return combination;
    }


    /**
     * In order to calculate the frequency of a given sample case, we need to
     * know which variables are marginalized, for example suppose the variables
     * are education, income, happiness, and we have the following frequencies.
     * education=none, income=50,000, happiness=high    0.2, education=none,
     * income=50,000, happiness=low     0.1, If the user decides to make
     * happiness invisible by not studying it, then the frequency becomes:
     * education=none, income=50,000,                   0.3, Propositions allow
     * us to encode this marginalization, and query the data set for the
     * margninalized frequency without having to do any other calculations.
     *
     * @param varSource
     * @param combination
     * @return the proposition of the variable with the given combination.
     */
    private static Proposition createProposition(VariableSource varSource, int [] combination) {
        Proposition proposition;
        int value;

        proposition = Proposition.tautology(varSource);
        for (int nodeIndex = 0; nodeIndex < combination.length; nodeIndex++) {
            value = combination[nodeIndex];
            if (value == VALUE_NOT_VIEWED) {
                proposition.setVariable(nodeIndex, true);
            } else {
                proposition.setVariable(nodeIndex, false);
                proposition.setCategory(nodeIndex, value); //, true);
            }
        }
        return proposition;
    }
}
