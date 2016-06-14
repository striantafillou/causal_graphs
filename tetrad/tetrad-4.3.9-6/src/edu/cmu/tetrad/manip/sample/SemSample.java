package edu.cmu.tetrad.manip.sample;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariablesStudied;
import edu.cmu.tetrad.manip.population.CorrectManipulatedGraphSemIm;
import edu.cmu.tetrad.sem.SemIm;

import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: May 20, 2004
 * Time: 11:51:26 PM
 * To change this template use File | Settings | File Templates.
 *
 * This describes the SEM sample.
 */
public class SemSample implements Sample {

    private DataSet data;

    /**
     * Constructor.
     * @param correctSemIm
     * @param experiment
     * @param sampleSize
     * @param sampleSeed
     */
    public SemSample(SemIm correctSemIm,
                     ExperimentalSetup experiment,
                     int sampleSize,
                     long sampleSeed){

        SemIm correctManipulatedGraphIM = CorrectManipulatedGraphSemIm.createIm(correctSemIm, experiment);


        //DataSet data = correctManipulatedGraphIM.simulateData(sampleSize, sampleSeed);
        data = correctManipulatedGraphIM.simulateData(sampleSize, false, sampleSeed);
    }

    /**
     * @return the associated dataset of this SEM sample.
     */
    public DataSet getDataSet(){
        return data;
    }

    /**
     * @param col
     * @param studiedVariables
     * @return the header name of the given column.
     */
    public String getColumnName(int col, VariablesStudied studiedVariables) {
        if(col == 0){ return ""; } //$NON-NLS-1$
        return studiedVariables.getNamesOfStudiedVariables()[col-1];
    }

    /**
     * @return the number of rows in the dataset.
     */
    public int getRowCount(){
        return data.getNumRows();//getMaxRowCount();
    }

    /**
     * @param studiedVariables
     * @return the number of columns in the dataset table. Need to add 1 for the
     * count.
     */
    public int getColumnCount(VariablesStudied studiedVariables){
        return studiedVariables.getNumVariablesStudied() + 1;
    }

    /**
     * @param row
     * @param column
     * @param studiedVariables
     * @return the value at the given row and column of the dataset.
     */
    public Object getValueAt(int row, int column, VariablesStudied studiedVariables){
        if(column == 0){
            return new Integer(row+1);
        }
        //String columnName = getColumnName(column, studiedVariables);
        //return data.getColumn(columnName).get(row);
        //return data.getDouble(row, column);

        if (column > 0) {
            return data.getObject(row, column-1);
        } else {
            throw new IllegalArgumentException("SemSample: Invalid column value = " + column);
        }
    }

    /**
     * @param row
     * @param varName
     * @return the value at a given row given a variable being set.
     */
    public Object getValueAtRowGivenColumnName(int row, String varName){
        //return data.getColumn(varName).get(row);
        return data.getDouble(row, data.getColumn(data.getVariable(varName)));
    }

    /**
     * @return the dataset given the particular constraints.
     */
    public DataSet getDataSetGivenConstraints() {
        return new ColtDataSet(0, Collections.EMPTY_LIST);
    }
}
