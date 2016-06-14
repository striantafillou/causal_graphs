package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.MbUtils;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.ArrayList;
import java.util.List;

/**
 * Picks a DAG from the given graph.
 *
 * @author Tyler Gibson
 */
public class ExtractMarkovBlanketWrapper extends GraphWrapper{
    static final long serialVersionUID = 23L;

    public ExtractMarkovBlanketWrapper(GraphSource source){
        this(source.getGraph());
    }


    public ExtractMarkovBlanketWrapper(Graph graph){
        super(new EdgeListGraph());

        String targetName = getVariableName(graph);
        Graph mb = getMb(graph, targetName);
        super.setGraph(mb);
    }


    public static BidirectedToUndirectedWrapper serializableInstance(){
        return new BidirectedToUndirectedWrapper(EdgeListGraph.serializableInstance());
    }


    //======================== Private Methods ================================//

    private Graph getMb(Graph graph, String target) {
        if (target == null) {
            return new EdgeListGraph();
        }

        Graph mb = new EdgeListGraph(graph);
        Node _target = mb.getNode(target);

        MbUtils.trimToMbNodes(mb, _target, false);
        MbUtils.trimEdgesAmongParents(mb, _target);
        MbUtils.trimEdgesAmongParentsOfChildren(mb, _target);

        System.out.println("MB # nodes = " + mb.getNumNodes());

        return mb;
    }

    private String getVariableName(final Graph graph) {
        Box box = Box.createVerticalBox();
        List<Node> nodes = graph.getNodes();

        List<String> nodeNames = new ArrayList<String>();

        for (Node node : nodes) {
            nodeNames.add(node.getName());
        }

        if (nodeNames.isEmpty()) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "There are no nodes in the graph.");
            return null;
        }

        JComboBox comboBox = new JComboBox(nodeNames.toArray());
        box.add(comboBox);
        box.add(Box.createVerticalStrut(4));

        box.setBorder(new TitledBorder("Parameters"));

        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                box, "Which target variable?", JOptionPane.QUESTION_MESSAGE);

        return (String) comboBox.getSelectedItem();
    }
}