package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Puts up a panel showing some graph properties, e.g., number of nodes and
 * edges in the graph, etc.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class DirectedPathsAction extends AbstractAction implements ClipboardOwner {
    private GraphWorkbench workbench;

    /**
     * Creates a new copy subsession action for the given LayoutEditable and
     * clipboard.
     */
    public DirectedPathsAction(GraphWorkbench workbench) {
        super("Directed Paths");
        this.workbench = workbench;
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    public void actionPerformed(ActionEvent e) {
        Box b = Box.createVerticalBox();
        Graph graph = workbench.getGraph();

        JTextArea textArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 600));

        textArea.append("Directed Paths:");

        for (int i = 0; i < graph.getNodes().size(); i++) {
            for (int j = 0; j < graph.getNodes().size(); j++) {
                Node node1 = graph.getNodes().get(i);
                Node node2 = graph.getNodes().get(j);

                List<List<Node>> treks = GraphUtils.directedPathsFromTo(graph, node1, node2);

                if (treks.isEmpty()) {
                    continue;
                }

                textArea.append("\n\nFrom " + node1 + " to " + node2 + ":");

                for (int k = 0; k < treks.size(); k++) {
                    textArea.append("\nPath " + k + ": ");
                    List<Node> trek = treks.get(k);

                    textArea.append(trek.get(0).toString());

                    for (int m = 1; m < trek.size(); m++) {
                        Node n0 = trek.get(m - 1);
                        Node n1 = trek.get(m);

                        Edge edge = graph.getEdge(n0, n1);

                        Endpoint endpoint0 = edge.getProximalEndpoint(n0);
                        Endpoint endpoint1 = edge.getProximalEndpoint(n1);

                        textArea.append(endpoint0 == Endpoint.ARROW ? "<" : "-");
                        textArea.append("-");
                        textArea.append(endpoint1 == Endpoint.ARROW ? ">" : "-");

                        textArea.append(n1.toString());
                    }
                }
            }
        }


        Box b2 = Box.createHorizontalBox();
        b2.add(scroll);
        textArea.setCaretPosition(0);
        b.add(b2);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(b);

        EditorWindow window = new EditorWindow(panel,
                "Directed Paths", "Close", false);
        DesktopController.getInstance().addEditorWindow(window);
        window.setVisible(true);

//        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), b,
//                "Graph Properties", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Required by the AbstractAction interface; does nothing.
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }


}