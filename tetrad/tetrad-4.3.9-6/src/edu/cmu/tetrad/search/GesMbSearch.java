package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Estimates the Markov blanket by first running GES on the data and then
 * graphically extracting the MB DAG.
 *
 * @author Joseph Ramsey
 */
public class GesMbSearch implements MbSearch {
    private DataSet dataSet;

    public GesMbSearch(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);

        Ges search = new Ges(dataSet);
        Graph graph = search.search();
        MbUtils.trimToMbNodes(graph, target, false);
        List<Node> mbVariables = graph.getNodes();
        mbVariables.remove(target);

        return mbVariables;
    }

    public String getAlgorithmName() {
        return "GESMB";
    }

    public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetVariableName) {
        Node target = null;

        for (Node V : dataSet.getVariables()) {
            if (V.getName().equals(targetVariableName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetVariableName);
        }

        return target;
    }

}
