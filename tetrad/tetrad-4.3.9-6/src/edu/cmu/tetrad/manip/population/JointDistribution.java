package edu.cmu.tetrad.manip.population;


/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Oct 13, 2003
 * Time: 5:56:31 PM
 * To change this template use Options | File Templates.
 *
 * This class describes the joint distribution of the variables.
 */
public class JointDistribution {

    private String[][] combinations;
    private double[] probabilities;
    private String[] header;

    /**
     * Constructor.
     * @param combinations a 2D string array of all possible combinations of the
     * variable values.
     * @param probabilities an array of double representing the probability of
     * each combination.
     * @param header a string array of the column headers.
     */
    public JointDistribution(String[][] combinations, double[] probabilities, String[] header) {
        this.combinations = combinations;
        this.probabilities = probabilities;
        this.header = header;
    }

    //Accessors
    /**
     * @return the column header. This could be the variable name or the
     * probability type.
     */
    public String getColumnString(int column) {
        return header[column];
    }

    /**
     * @return the number of different combinations of the variable values.
     */
    public int getNumRows() {
        return combinations.length;
    }

    /**
     * The each column of the jointDistributionCombinations array represents
     * a variable.  The table returned to the user of this class also has one
     * more column which is the probabilities for a given combination of
     * variables.  For example, the variables might be "A", "B" and "C" and
     * the fourth column will be the probaility (such as "0.4") that a given
     * combination ofvariables (such as "A" == true, "B" == true, "C" == true)
     * occurs in the population.
     *
     * @return the number of columns in the joing distribution table.
     */
    public int getNumColumns() {
        return header.length;
    }

    /**
     * @return the value in the joint distribution table.
     */
    public Object getValueAt(int row, int column) {
        if (row >= getNumRows()) {
            return null;
        }

        if (column < getNumColumns() - 1) {
            return combinations[row][column];
        } else if (column == getNumColumns() - 1) {
            return new Double(probabilities[row]);
        } else {
            return null;
        }
    }


}
