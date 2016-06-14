package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.ContinuousDiscretizationSpec;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.Discretizer;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Immutable object that wraps a dataset and gives a histogram view of things.
 *
 * @author Tyler Gibson
 */
public class Histogram {


    /**
     * The complete data set that this histogram deals with.
     */
    private DataSet dataSet;


    /**
     * The values of the selected column (non-discretized)
     */
    private double[] values;


    /**
     * The variable that we are showing a histogram for.
     */
    private Node selectedVariable;


    /**
     * The min value in the histogram
     */
    private double min = 0.0;


    /**
     * The max value in the histogram
     */
    private double max = 0.0;


    /**
     * The frequences, that is the number of items in each category (integers).
     */
    private int[] frequencies;


    /**
     * The number of categories.
     */
    private int categories = 2;


    /**
     * List of conditions, may be empty.
     */
    private List<ConditionalDependency> conditions = new ArrayList<ConditionalDependency>();


    /**
     * Constructs the histogram given the dataset to wrap and the node that should be viewed.
     *
     * @param dataSet
     * @param selectedNode
     */
    public Histogram(DataSet dataSet, Node selectedNode, int categories) {
        if (dataSet == null) {
            throw new NullPointerException("the given dataset must not be null");
        }
        if (dataSet.getNumColumns() == 0) {
            throw new IllegalArgumentException("The given dataset should not be empty");
        }
        if (categories < 2) {
            throw new IllegalArgumentException("Category must be at least 2");
        }
        this.dataSet = dataSet;
        if (selectedNode == null && dataSet.getNumColumns() != 0) {
            int[] selected = dataSet.getSelectedIndices();
            if (selected == null || selected.length == 0) {
                selectedNode = dataSet.getVariable(0);
            } else {
                selectedNode = dataSet.getVariable(selected[0]);
            }
        }
        // pick a good default for the categories (maybe overriding the given value)
        setCategories(selectedNode, categories);

        // set the node
        this.setSelectedVariable(selectedNode);
    }


    /**
     * Constructs a histrogram, just like the given one except for the category value.
     *
     * @param histogram
     * @param categories
     */
    public Histogram(Histogram histogram, int categories) {
        this.dataSet = histogram.dataSet;
        this.selectedVariable = histogram.selectedVariable;
        this.conditions = histogram.conditions;
        setCategories(this.selectedVariable, categories);
    }


    /**
     * Constructs a histogram, just like the given one except that it has the given
     * list of conditional dependencies.
     *
     * @param histogram
     * @param dependencies
     */
    public Histogram(Histogram histogram, List<ConditionalDependency> dependencies) {
        this.dataSet = histogram.dataSet;
        this.selectedVariable = histogram.selectedVariable;
        this.categories = histogram.categories;
        this.conditions = dependencies;
        // set the column values for them, for efficiecy
        for (ConditionalDependency condition : dependencies) {
            condition.column = this.dataSet.getColumn(condition.node);
        }
    }

    //==================================== Public Methods ====================================//


    /**
     * Returns a list of conditions that this histogram is using.
     *
     * @return - conditions.
     */
    public List<ConditionalDependency> getConditionalDependencies() {
        return Collections.unmodifiableList(this.conditions);
    }


    /**
     * Returns the number of categories that are being used.
     *
     * @return - number of categories.
     */
    public int getNumberOfCategories() {
        return this.categories;
    }


    /**
     * Returns the max value.
     *
     * @return - max Value.
     */
    public double getMaxValue() {
        return this.max;
    }


    /**
     * Returns the min value.
     *
     * @return - min value.
     */
    public double getMinValue() {
        return this.min;
    }

    /**
     * Returns the node that has been selected.
     *
     * @return selected
     */
    public Node getSelectedVariable() {
        return this.selectedVariable;
    }


    /**
     * Returns the frequences as an array of integers. For each category 0,....,n the
     * integer at the category represents the number of values that fall within its range.
     *
     * @return - frequences.
     */
    public int[] getFrequencies() {
        buildHistogramData();
        int[] freqs = new int[this.frequencies.length];
        System.arraycopy(this.frequencies, 0, freqs, 0, this.frequencies.length);
        return freqs;
    }

    //============================ Private Methods =======================//


    private void setCategories(Node selectedNode, int categories) {
        if (selectedNode instanceof DiscreteVariable) {
            this.categories = Math.min(((DiscreteVariable) selectedNode).getCategories().size(), categories);
        } else {
            this.categories = categories;
        }
    }


    /**
     * Returns the values for the selected column.
     *
     * @return - values.
     */
    private double[] getValues() {
        if (this.values == null) {
            int column = this.dataSet.getColumn(this.selectedVariable);
            int rows = this.dataSet.getNumRows();
            this.values = new double[rows];
            for (int i = 0; i < rows; i++) {
                boolean satisfied = true;
                for (ConditionalDependency condition : conditions) {
                    Object value = this.dataSet.getObject(i, condition.column);
                    if (!condition.satisfied(value)) {
                        satisfied = false;
                        break;
                    }
                }
                // only add if the conditions have been satisfied
                if (satisfied) {
                    this.values[i] = this.dataSet.getDouble(i, column);
                }
            }
        }
        return this.values;
    }


    private ContinuousDiscretizationSpec calculateSpec() {
        // first get min/max
        calculateMinMax();

        // get the break points.
        double[] breakPoints = defaultBreakpoints(max, min, this.categories);
        // get categories
        LinkedList<String> categories = new LinkedList<String>();
        for (int i = 1; i <= this.categories; i++) {
            categories.add(String.valueOf(i));
        }
        return new ContinuousDiscretizationSpec(breakPoints, categories);
    }


    /**
     * Calculates and sets the min/max.  If the min/max are equal then they need to be handled specially
     * since the Discretization excepts the sequence of cut offs to be monotone. So in these cases
     * the max value is increment some small trivial amount that in the display amounts to it being zero.
     * (The discretization could be changed...but its easier just to do this). 
     */
    private void calculateMinMax() {
        double[] values = getValues();
        if(values.length == 0){
            this.min = 0.0;
            this.max = 0.0000000008;
            return;
        }
        double min = values[0];
        double max = values[0];
        for (double value : getValues()) {
            if (!Double.isNaN(value)
                    && value != Double.NEGATIVE_INFINITY
                    && value != Double.POSITIVE_INFINITY
                    && value < min) {
                min = value;
            }
            if (!Double.isNaN(value)
                    && value != Double.NEGATIVE_INFINITY
                    && value != Double.POSITIVE_INFINITY
                    && max < value) {
                max = value;
            }
        }
        if(min == max){
            this.max = this.min + .0000000008;
        }

        this.min = min;
        this.max = max;
    }


    /**
     * Retursn the default break points.
     */
    private static double[] defaultBreakpoints(double max, double min,
                                               int numCategories) {
        if (Double.isNaN(max) || max == Double.NEGATIVE_INFINITY || max == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Max cannot be NaN or positive or negative infinity: " + max);
        }

        if (Double.isNaN(min) || min == Double.NEGATIVE_INFINITY || min == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Min cannot be NaN or positive or negative infinity: " + min);
        }

        double interval = (max - min) / numCategories;
        double[] breakpoints = new double[numCategories - 1];
        for (int i = 0; i < breakpoints.length; i++) {
            breakpoints[i] = min + (i + 1) * interval;
        }
        return breakpoints;
    }

    /**
     * Sets the column that will be discretized.
     *
     * @param node
     */
    private void setSelectedVariable(Node node) {
        this.selectedVariable = node;
        this.values = null;
    }


    /**
     * Builds the histrogram data if required, otherwise does nothing
     */
    private void buildHistogramData() {
        if (this.frequencies == null) {
            ContinuousDiscretizationSpec spec = calculateSpec();
            if (spec == null) {
                this.frequencies = new int[this.categories];
            } else {
                Discretizer.Discretization discretization = Discretizer.discretize(getValues(),
                        spec.getBreakpoints(), this.selectedVariable.getName(),
                        spec.getCategories());
                int[] data = discretization.getData();
                this.frequencies = new int[this.categories];
                for (int category : data) {
                    this.frequencies[category]++;
                }
            }
        }
    }

    //======================== Inner classes ===================================//

    public static class ConditionalDependency {
        private int column = -1;
        private Node node;
        private List<String> values;

        public ConditionalDependency(Node node, List<String> values) {
            this.node = node;
            this.values = values;
        }


        public Node getNode() {
            return this.node;
        }


        public List<String> getValues() {
            return this.values;
        }


        public boolean satisfied(Object object) {
            for (String value : values) {
                if (value.equals(object.toString())) {
                    return true;
                }
            }
            return false;
        }

    }


}
