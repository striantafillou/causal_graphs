package edu.cmu.tetrad.manip.population;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesImProbs;
import edu.cmu.tetrad.bayes.Proposition;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.VariableSource;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariablesStudied;
import edu.cmu.tetrad.sem.SemIm;

import java.util.Iterator;


/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Oct 5, 2003
 * Time: 3:19:28 PM
 *
 * This class describes the population and its related functions.
 */
public class Population {

    private static int VALUE_NOT_VIEWED = -1;

    //==============================================================================================
    //  Population methods for SEM models
    //
    //
    //==============================================================================================

    /**
     * first column header is "", the other column headers are the variable names.
     * @param correctSemIm
     * @param studiedVariables
     * @return a string array of the column headers.
     */
    public static String [] getColumnHeaders(SemIm correctSemIm, VariablesStudied studiedVariables){
        return createHeaders(correctSemIm, studiedVariables);
    }


    /**
     * first column header is "", the other column headers are the variable names.
     * @param col
     * @param correctSemIm
     * @param studiedVariables
     * @return the column header at a particular column.
     */
    public static String getColumnHeader(int col, SemIm correctSemIm, VariablesStudied studiedVariables){
        return getColumnHeaders(correctSemIm, studiedVariables)[col];
    }


    /**
     * row n >0, column 0 is name of a variable.
     * @param row
     * @param studiedVariables
     * @return the name of the variable at a given row.
     */
    public static String getFirstColumn(int row, VariablesStudied studiedVariables){
        return (studiedVariables.getNamesOfStudiedVariables())[row];
    }


    /**
     * the row, column indices are variables, the cells of the table are how the variables covary.
     * @param semIm
     * @param var1
     * @param var2
     * @return the covariance of the two variables in the SEM IM graph.
     */
    public static double getCovariance(SemIm semIm, String var1, String var2) throws IllegalArgumentException {
        int var1Index = -1, var2Index = -1;
        int i = 0;
        Node node;
        for(Iterator nodes = semIm.getVariableNodes().iterator(); nodes.hasNext(); i++){
            node = (Node) nodes.next();
            if(node.getName().equals(var1)){
                var1Index = i;
            }
            if(node.getName().equals(var2)){
                var2Index = i;
            }
        }
        return semIm.getImplCovar().toArray()[var1Index][var2Index];
    }


    /**
     * the row, column indices are variables, the cells of the table are how the variables covary.
     * @param semIm
     * @param var1
     * @param var2
     * @return the correlation of the two variables in the SEM IM graph.
     */
    public static double getCorrelation(SemIm semIm, String var1, String var2) throws IllegalArgumentException {
        DoubleMatrix2D covariance = semIm.getImplCovar();
        String [] varNames = new String[semIm.getVariableNodes().size()];
        int i = 0;
        int x = -1, y= -1;

        for(Iterator n = semIm.getVariableNodes().iterator(); n.hasNext(); i++){
            varNames[i] = ((Node)n.next()).getName();
            if(varNames[i].equals(var1)){ x = i; }
            if(varNames[i].equals(var2)){ y = i; }
        }

        CovarianceMatrix covarMatrix = new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), covariance, covariance.rows()+1);
        CorrelationMatrix corrMatrix = new CorrelationMatrix(covarMatrix);
        return corrMatrix.getValue(x,y);
    }

   /**
    * number of rows is just number of observed variables.
    * @param studiedVariables
    * @return the number of observed variables.
    */
    public static int getNumRows(ExperimentalSetup studiedVariables){

        if(studiedVariables == null){
            return 0;
        }
        return studiedVariables.getNumVariablesStudied();
    }

    /**
     * @return the value in the population table at a given column and row.
     */
    public static Object getValueAt(int row, int col, SemIm correctSemIm, ExperimentalSetup experiment, VariablesStudied studiedVariables){
        SemIm correctManipulatedSemIm = createCorrectManipulatedSemIm(correctSemIm, experiment);
        if(col==0){
            return getFirstColumn(row, studiedVariables);
        } else {
            if(col > row + 1){ return null;}
            return new Double(
                    getCorrelation(
                            correctManipulatedSemIm,
                            getFirstColumn(row, studiedVariables),
                            getColumnHeader(col, correctManipulatedSemIm, studiedVariables)));
        }
    }


    private static String[] createHeaders(
            SemIm correctSemIm,
            VariablesStudied studiedVariables){

        Node node;
        int j;
        Iterator nodes;
        String[] headers;
        NodeType type;

        headers = new String[getNumViewedColumns(studiedVariables)+1];
        headers[0] = ""; //$NON-NLS-1$
        for(j=1, nodes = correctSemIm.getSemPm().getGraph().getNodes().iterator(); nodes.hasNext();){
            node = (Node) nodes.next();
            type = node.getNodeType();
            if(type == NodeType.MEASURED){
                if(studiedVariables.isVariableStudied(node.getName())){
                    headers[j++] = node.getName();
                }
            }
        }
        return headers;
    }

    /**
     * Create the correct manipulated graph SEM IM.
     * @param correctSemIm
     * @param experiment
     * @return the correct manipulated graph SEM IM.
     */
    public static SemIm createCorrectManipulatedSemIm(SemIm correctSemIm, ExperimentalSetup experiment){
        //AbstractManipulatedGraph corManipulatedGraph = new ManipulatedGraph(correctSemIm.getSemPm().getGraph(), experiment);
        return CorrectManipulatedGraphSemIm.createIm(correctSemIm, experiment);
    }

    //==============================================================================================
    //  Population methods for Bayes models
    //
    //
    //==============================================================================================

    /**
     * @param correctIm
     * @param experiment
     * @param studiedVariables
     * @param row
     * @return the given combination of the variable values, based on the row.
     */
    public static String [] getCase(BayesIm correctIm,
                                    ExperimentalSetup experiment,
                                    VariablesStudied studiedVariables,
                                    int row){
        BayesIm IM = CorrectManipulatedGraphBayesIm.createIm( correctIm, experiment);
        //BayesIm IM = CorrectManipulatedGraphBayesIm.createIm(correctManipulatedGraph, correctIm, experimentQN);
        int [] combination = magicIndexFunction(IM, studiedVariables, row);
        return createStringCombination(IM, studiedVariables, combination);
    }

    /**
     * @param correctIm
     * @param experimentQN
     * @param studiedVariables
     * @param row
     * @return the probability of a particular combination of variable values.
     */
    public static double getProbability(BayesIm correctIm,
                                        ExperimentalSetup experimentQN,
                                        VariablesStudied studiedVariables,
                                        int row){

        BayesIm IM = CorrectManipulatedGraphBayesIm.createIm(correctIm, experimentQN);
        BayesImProbs cellProbabilities = new BayesImProbs(IM);
        int [] combination = magicIndexFunction(IM, studiedVariables, row);
        Proposition proposition = createProposition(IM, combination);
        return cellProbabilities.getProb(proposition);
    }

    /**
     * Returns the number of combniations of variable values in the joint distribution.
     * @param IM the BayesIm that has the varialbes.
     * @param studiedVariables if variables are not studied, there will be
     * fewer rows.
     * @return number of rows
     */
    public static int getNumCombinationRows(BayesIm IM, VariablesStudied studiedVariables){
        int num =1;
        Node node;
        boolean anythingVisible = false;

        if(studiedVariables == null){
            return 0;
        }

        for(int variable = 0; variable < IM.getNumNodes(); variable++){
            node = IM.getNode(variable);
            if(node.getNodeType() == NodeType.MEASURED){
                if(studiedVariables.isVariableStudied(node.getName())){
                    num *= IM.getBayesPm().getNumCategories(node);
                    anythingVisible = true;
                }
            }
        }
        if(!anythingVisible){return 0;}
        return num;
    }

    /**
     * @return the number of combination columns.
     */
    public static int getNumColumns(BayesIm IM){
        return getNumCombinationColumns(IM);
    }

    /**
     * @return the string array of the column headers.
     */
    public static String [] getColumnHeaders(BayesIm IM, VariablesStudied studiedVariables){
        return createHeaders(IM, studiedVariables);
    }


    //------------------PROTECTED AND PRIVATE METHODS ------------------------------------------

    /**
     * This is a helper method for getProbability() that converts
     * @param varSource the instantiated model with the variables and their
     * probabilites of occuring.
     * @param combination an array of ints where if combination[i] = n, then
     * i is the ith node in the varSource, and n is a value of that
     * node.  if n = VALUE_NOT_VIEWED, then that node is not being observed in
     * the experimental setup.
     * @return a data structure that translates the parameters into something
     * tetrad can understand.
     */
    protected static Proposition createProposition(VariableSource varSource,
                                                   int [] combination){
        Proposition proposition;
        int value;

        proposition =  Proposition.tautology(varSource);
        for(int nodeIndex = 0; nodeIndex < combination.length; nodeIndex++){
            value = combination[nodeIndex];
            if(value == VALUE_NOT_VIEWED){
                proposition.setVariable(nodeIndex, true);
            }else{
                proposition.setVariable(nodeIndex, false);
                proposition.setCategory(nodeIndex, value); //, true);
            }
        }
        return proposition;
    }

    /**
     * Each row in the population joint distribution table represents a combination
     * of variable values and the probability that that combnation occurs in the population.
     * This function takes a row index and returns the combination of variable values
     * for that row (as an array of ints).
     * @return an array of ints where the index i represents the ith node in the IM.
     * The value n (stored at the ith index) is the index of the ith node's nth value,
     * i.e. array[i] = n where i is the node, n is the node's value
     */
    protected static int [] magicIndexFunction(
            BayesIm IM,
            VariablesStudied studiedVariables,
            int row){

        int productPreviousNumValues = 1;
        int currentNumValues, value, variable;
        int [] combination;

        Node node;
        NodeType type;

        if(studiedVariables.getNumVariablesStudied()==0){ return null; }

        combination = new int[getNumCombinationColumns(IM)];

        for(variable = IM.getNumNodes() - 1; variable >=0 ; variable--){
            node = IM.getNode(variable);
            type = node.getNodeType();
            if((type == NodeType.MEASURED) && (studiedVariables.isVariableStudied(node.getName()))){
                currentNumValues = IM.getBayesPm().getNumCategories(node);
                value = (row / productPreviousNumValues) % currentNumValues;
                combination[variable] = value;
                productPreviousNumValues = productPreviousNumValues * currentNumValues;
            }else {
                combination[variable] = VALUE_NOT_VIEWED;
            }
        }
        return combination;
    }



    /**
     * Returns the number of columns for the combinations.
     * @return the number of columns.
     */
    protected static int getNumCombinationColumns(BayesIm IM){
        //int num = 0;
        NodeType type;

        for(int i = 0; i < IM.getNumNodes(); i++){
            type = IM.getNode(i).getNodeType();
            if(type == NodeType.MEASURED){
                //num += 1;
            }
        }
        return IM.getNumNodes();
    }

    /**
     * Returns the number of columns that will be in the final joint distribution - 1.
     * In otherwords, latent variables will be included if showLatents == true
     * and viewed variables will be included.
     * @param studiedVariables
     * @return the number of columns that will be in the final joint distribution - 1.
     */
    private static int getNumViewedColumns(VariablesStudied studiedVariables){
        return  studiedVariables.getNumVariablesStudied();
    }

    /**
     * Takes a combination of variable values as an array of ints and converts it
     * to the names of the values.  Variables that are not viewed will not be
     * converted.
     * @param IM the IM with the varialbes.
     * @param combination and array of variable value indicies.
     * @param studiedVariables
     * @return an array of variable value names.
     */
    protected static String [] createStringCombination(
            BayesIm IM,
            VariablesStudied studiedVariables,
            int []combination){

        Node node;
        String [] stringCombination = new String [getNumViewedColumns(studiedVariables)];
        int i,j;

        for(j=0, i = 0; i < IM.getNumNodes(); i++){
            node = IM.getNode(i);
            if(node.getNodeType() == NodeType.MEASURED){
                if(studiedVariables.isVariableStudied(node.getName())){
                    stringCombination[j++] = IM.getBayesPm().getCategory(node, combination[i]);
                }
            }
         }
        return stringCombination;
    }

    /**
     * Returns the headers (names of visibile variables) for the joint distribution
     * depending on what variables are viewed.
     */
    private static String[] createHeaders(
            BayesIm IM,
            VariablesStudied studiedVariables){

        Node node;
        int j;
        Iterator nodes;
        String[] headers;
        NodeType type;

        headers = new String[getNumViewedColumns(studiedVariables)+1];
        for(j=0, nodes = IM.getDag().getNodes().iterator(); nodes.hasNext();){
            node = (Node) nodes.next();
            type = node.getNodeType();
            if(type == NodeType.MEASURED){
                if(studiedVariables.isVariableStudied(node.getName())){
                    headers[j++] = node.getName();
                }
            }
        }
        headers[j++] = "%"; //$NON-NLS-1$
        return headers;
    }


    /**
     * Helper function to display the population table for testing.
     * @param IM
     */
    public void printTables(BayesIm IM){
        int nodeIndex, rowIndex, parentIndex, valueIndex;
        Node node, parent;
        int[]parentValues;

        for( nodeIndex = 0; nodeIndex < IM.getNumNodes(); nodeIndex++){
            node = IM.getNode(nodeIndex);
            System.out.println();
            System.out.println(node.getName());
            for(valueIndex = 0; valueIndex <IM.getNumColumns(nodeIndex); valueIndex++){
                System.out.print(IM.getBayesPm().getCategory(node, valueIndex) +" "); //$NON-NLS-1$
            }
            System.out.println();
            System.out.println("----------------"); //$NON-NLS-1$
            for(rowIndex = 0; rowIndex < IM.getNumRows(nodeIndex); rowIndex++){
                parentValues =IM.getParentValues(nodeIndex, rowIndex);
                for(parentIndex = 0; parentIndex < IM.getNumParents(nodeIndex); parentIndex++){
                    parent = IM.getNode(IM.getParent(nodeIndex, parentIndex));
                    System.out.print(parent.getName() + "=" + IM.getBayesPm().getCategory(parent,parentValues[parentIndex])); //$NON-NLS-1$
                }
                System.out.print(" : "); //$NON-NLS-1$
                for(valueIndex = 0; valueIndex <IM.getNumColumns(nodeIndex); valueIndex++){
                    System.out.print(IM.getProbability(nodeIndex, rowIndex, valueIndex) +" "); //$NON-NLS-1$
                }
                System.out.println();
           }
            System.out.println("----------------"); //$NON-NLS-1$
        }
    }

    /**
     * Helper function to print the Bayes IM graph.
     * @param IM
     */
    public void printIM(BayesIm IM){
        System.out.println(IM.toString());
    }
}
