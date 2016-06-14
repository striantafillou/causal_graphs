package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.GraphComparison;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

/**
 * Provides a little display/editor for notes in the session workbench. This
 * may be elaborated in the future to allow marked up text.
 *
 * @author Joseph Ramsey
 */
public class GraphComparisonEditor extends JPanel {

    /**
     * The model for the note.
     */
    private GraphComparison comparison;


    /**
     * Constructs the editor given the model
     *
     * @param comparison
     */
    public GraphComparisonEditor(GraphComparison comparison) {
        this.comparison = comparison;
        setup();
    }

    //============================ Private Methods =========================//


    private boolean isLegal(String text) {
//        if (!NamingProtocol.isLegalName(text)) {
//            JOptionPane.showMessageDialog(this, NamingProtocol.getProtocolDescription() + ": " + text);
//            return false;
//        }
        return true;
    }

    private void setup() {
        StringBuffer buf = new StringBuffer();

        buf.append("\nEdges added:");

        if (comparison.getEdgesAdded().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            List<Edge> edgesAdded = comparison.getEdgesAdded();

            for (int i = 0; i < edgesAdded.size(); i++) {
                Edge edge = edgesAdded.get(i);

                Graph graph = comparison.getTargetGraph();
                boolean ambiguous = graph.isAmbiguous(edge.getNode1(), edge.getNode2());
                buf.append("\n").append(i + 1).append(". <> ====> ").append(edge)
                        .append(ambiguous ? " (Ambiguous)" : "");

            }
        }

        buf.append("\n\nEdge removed:");

        if (comparison.getEdgesRemoved().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            List<Edge> edgesRemoved = comparison.getEdgesRemoved();

            for (int i = 0; i < edgesRemoved.size(); i++) {
                Edge edge = edgesRemoved.get(i);
                buf.append("\n" + (i + 1) + ". ").append(edge).append(" ====> <>");
            }
        }

        buf.append("\n\nEdges reoriented:");

        if (comparison.getEdgesReorientedFrom().isEmpty()) {
            buf.append("\n  --NONE--");
        }
        else {
            List<Edge> edgeReorientedFrom = comparison.getEdgesReorientedFrom();
            List<Edge> edgesReorientedTo = comparison.getEdgesReorientedTo();

            for (int i = 0; i < comparison.getEdgesReorientedFrom().size(); i++) {
                Edge from = edgeReorientedFrom.get(i);
                Edge to = edgesReorientedTo.get(i);
                Graph graph = comparison.getTargetGraph();
                boolean ambiguous = graph.isAmbiguous(from.getNode1(), from.getNode2());
                buf.append("\n").append(i + 1).append(". ").append(from)
                        .append(" ====> ").append(to)
                        .append(ambiguous ? " (Ambiguous)" : "");
            }
        }

        Font font = new Font("Monospaced", Font.PLAIN, 14);
        final JTextArea textPane = new JTextArea();
        textPane.setText(buf.toString());

        textPane.setFont(font);
//        textPane.setCaretPosition(textPane.getStyledDocument().getLength());

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(400, 400));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(10));

        Box box = Box.createHorizontalBox();
        this.add(box);
        this.add(Box.createVerticalStrut(10));

        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel("Graph Comparison: "));
        box1.add(Box.createHorizontalGlue());

        add(box1);
        setLayout(new BorderLayout());
        add(scroll);
    }
}