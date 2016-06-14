package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Returns a data set in which missing values in each column are filled using
 * the mean of that column.
 *
 * @author Joseph Ramsey
 */
public final class MeanInterpolator implements DataFilter {
    public DataSet filter(DataSet dataSet) {
        List<Node> variables = new LinkedList<Node>();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node variable = dataSet.getVariable(i);
            variables.add(variable);
        }

        DataSet newDataSet = new ColtDataSet((ColtDataSet) dataSet);

        for (int j = 0; j < newDataSet.getNumColumns(); j++) {
            if (newDataSet.getVariable(j) instanceof ContinuousVariable) {
                double sum = 0.0;
                int count = 0;

                for (int i = 0; i < newDataSet.getNumRows(); i++) {
                    if (!Double.isNaN(newDataSet.getDouble(i, j))) {
                        sum += newDataSet.getDouble(i, j);
                        count++;
                    }
                }

                double mean = sum / count;

                for (int i = 0; i < newDataSet.getNumRows(); i++) {
                    if (Double.isNaN(newDataSet.getDouble(i, j))) {
                        newDataSet.setDouble(i, j, mean);
                    }
                }
            }
        }

        return newDataSet;
    }
}
