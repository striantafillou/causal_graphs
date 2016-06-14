package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

import java.util.List;

/**
 * Estimates the Markov blanket by first running PC on the data and then
 * graphically extracting the MB DAG.
 *
 * @author Joseph Ramsey
 */
public class Pcmb implements MbSearch {
    private IndependenceTest test;
    private int depth;

    public Pcmb(IndependenceTest test, int depth) {
        this.test = test;
        this.depth = depth;
    }

    public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);

        Pc search = new Pc(test);
        search.setDepth(depth);
        Graph graph = search.search();
        MbUtils.trimToMbNodes(graph, target, false);
        List<Node> mbVariables = graph.getNodes();
        mbVariables.remove(target);

        return mbVariables;
    }

    public String getAlgorithmName() {
        return "PCMB";
    }

    public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetVariableName) {
        Node target = null;

        for (Node V : test.getVariables()) {
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
