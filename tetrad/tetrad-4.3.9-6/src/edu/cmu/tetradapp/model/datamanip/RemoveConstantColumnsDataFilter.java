package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tyler was lazy and didn't document this....
 *
 * @author Tyler Gibson
 */
public class RemoveConstantColumnsDataFilter implements DataFilter {


    /**
     * Removes any constant columns from the given dataset.
     *
     * @param dataSet
     * @return - new dataset with constant columns removed.
     */
    public DataSet filter(DataSet dataSet) {
        int columns = dataSet.getNumColumns();
        int rows = dataSet.getNumRows();
        if (rows == 0) {
            return dataSet;
        }
        List<NodeWrapper> nodes = new ArrayList<NodeWrapper>(columns);
        for (int col = 0; col < columns; col++) {
            Object previous = dataSet.getObject(0, col);
            boolean constant = true;
            for (int row = 1; row < rows; row++) {
                Object current = dataSet.getObject(row, col);
                if (!previous.equals(current)) {
                    constant = false;
                    break;
                }
            }
            // only add if its not constant
            if (!constant) {
                nodes.add(new NodeWrapper(col, dataSet.getVariable(col)));
            }
        }
        // no change, return original dataset
        if (nodes.size() == columns) {
            return dataSet;
        }
        if(nodes.size() == 0){
            return new ColtDataSet(0, new LinkedList<Node>());
        }
        // otherwise copy over all the non-constant columns.
        ColtDataSet newDataSet = new ColtDataSet(rows, getNodes(nodes));
        for (int k = 0; k < nodes.size(); k++) {
            NodeWrapper node = nodes.get(k);
            for (int i = 0; i < rows; i++) {
                newDataSet.setObject(i, k, dataSet.getObject(i, node.column));
            }
        }

        return newDataSet;
    }

    //==================== Private Methods ========================================//


    private static List<Node> getNodes(List<NodeWrapper> wrappers) {
        List<Node> nodes = new ArrayList<Node>(wrappers.size());
        for (NodeWrapper wrapper : wrappers) {
            nodes.add(wrapper.node);
        }
        return nodes;
    }

    //================================ Inner classes ===============================//

    /**
     * Stores a node and the original column, so that the column index doesn't need to be looked
     * up again (which is slow)
     */
    private static class NodeWrapper {
        private int column;
        private Node node;

        public NodeWrapper(int column, Node node) {
            this.column = column;
            this.node = node;
        }
    }


}
