package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;

import java.util.LinkedList;

/**
 * Stores a history of graph objects.
 *
 * @author Joseph Ramsey
 */
public class GraphHistory {

    /**
     * The history.
     */
    private LinkedList<Graph> graphs;

    /**
     * The index of the current graph.
     */
    private int index;

    /**
     * Constructs a graph history.
     */
    public GraphHistory() {
        graphs = new LinkedList<Graph>();
        index = -1;
    }

    public void add(Graph graph) {
        if (graph == null) {
            throw new NullPointerException();
        }

        for (int i = graphs.size() - 1; i > index; i--) {
            graphs.remove(i);
        }

        graphs.addLast(new EdgeListGraph(graph));
        index++;
    }

    public Graph next() {
        if (index == -1) {
            throw new IllegalArgumentException("Graph history has not been " +
                    "initialized yet.");
        }

        if (index < graphs.size() - 1) {
            index++;
        }

        return graphs.get(index);
    }

    public Graph previous() {
        if (index == -1) {
            throw new IllegalArgumentException("Graph history has not been " +
                    "initialized yet.");
        }

        if (index > 0) {
            index--;
        }

        return graphs.get(index);
    }

    public void clear() {
        graphs.clear();
        index = -1;
    }
}
