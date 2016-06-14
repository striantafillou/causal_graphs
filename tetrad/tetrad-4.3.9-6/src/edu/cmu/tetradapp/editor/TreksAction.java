package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
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
public class TreksAction extends AbstractAction implements ClipboardOwner {
    private GraphWorkbench workbench;

    /**
     * Creates a new copy subsession action for the given LayoutEditable and
     * clipboard.
     */
    public TreksAction(GraphWorkbench workbench) {
        super("Treks");
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

        textArea.append("Treks:");

        for (int i = 0; i < graph.getNodes().size(); i++) {
            for (int j = 0; j < graph.getNodes().size(); j++) {
                Node node1 = graph.getNodes().get(i);
                Node node2 = graph.getNodes().get(j);

                List<List<Node>> treks = GraphUtils.treks(graph, node1, node2);

                if (treks.isEmpty()) {
                    continue;
                }

                textArea.append("\n\nBetween " + node1 + " and " + node2 + ":");

                for (int k = 0; k < treks.size(); k++) {
                    textArea.append("\nTrek " + k + ": ");
                    List<Node> trek = treks.get(k);

                    textArea.append(GraphUtils.pathString(graph, trek));
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
                "Treks", "Close", false);
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