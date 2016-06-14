package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndependenceTest;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements the Grow-Shrink algorithm of Margaritis and Thrun. Reference:
 * "Bayesian Network Induction via Local Neighborhoods."
 *
 * @author Joseph Ramsey
 */
public class GrowShrink implements MbSearch {

    /**
     * The independence test used to perform the search.
     */
    private IndependenceTest independenceTest;

    /**
     * The list of variables being searched over. Must contain the target.
     */
    private List<Node> variables;

    /**
     * Constructs a new search.
     *
     * @param test The source of conditional independence information for the
     *             search.
     */
    public GrowShrink(IndependenceTest test) {
        if (test == null) {
            throw new NullPointerException();
        }

        this.independenceTest = test;
        this.variables = test.getVariables();
    }

    /**
     * Finds the Markov blanket of the given target.
     *
     * @param targetName the name of the target
     * @return the list of node in the Markov blanket.
     */
    public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);
        List<Node> blanket = new LinkedList<Node>();

        boolean changed = true;

        while (changed) {
            changed = false;

            List<Node> remaining = new LinkedList<Node>(variables);
            remaining.removeAll(blanket);
            remaining.remove(target);

            for (Node node : remaining) {
                if (!independenceTest.isIndependent(node, target, blanket)) {
                    blanket.add(node);
                    changed = true;
                }
            }
        }

        changed = true;

        while (changed) {
            changed = false;

            for (Node node : new LinkedList<Node>(blanket)) {
                blanket.remove(node);

                if (independenceTest.isIndependent(node, target, blanket)) {
                    changed = true;
                    continue;
                }

                blanket.add(node);
            }
        }

        return blanket;
    }

    public String getAlgorithmName() {
        return "Grow Shrink";
    }

    public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetName) {
        Node target = null;

        for (Node V : variables) {
            if (V.getName().equals(targetName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetName);
        }

        return target;
    }
}
