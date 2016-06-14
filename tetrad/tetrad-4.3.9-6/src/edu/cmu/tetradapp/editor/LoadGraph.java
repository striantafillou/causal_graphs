package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.RegexTokenizer;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Saves out a PNG image for a component.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class LoadGraph extends AbstractAction {

    /**
     * The component whose image is to be saved.
     */
    private GraphEditable graphEditable;

    public LoadGraph(GraphEditable graphEditable, String title) {
        super(title);

        if (graphEditable == null) {
            throw new NullPointerException("Component must not be null.");
        }

        this.graphEditable = graphEditable;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = getJFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.showOpenDialog((Component) this.graphEditable);

        final File file = chooser.getSelectedFile();

        if(file == null){
            return;
        }

        Preferences.userRoot().put("fileSaveLocation", file.getParent());

        try {
            Reader reader = new FileReader(file);
            BufferedReader in = new BufferedReader(reader);
            String line = in.readLine();

            RegexTokenizer tokenizer = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

            List<Node> nodes = new ArrayList<Node>();

            while (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
                nodes.add(new GraphNode("X" + nodes.size()));
            }

            Graph graph = new EdgeListGraph(nodes);

            for (int i = 0; i < nodes.size(); i++) {
                RegexTokenizer t2 = new RegexTokenizer(line, DelimiterType.WHITESPACE.getPattern(), '\"');

                for (int j = 0; j < nodes.size(); j++) {
                    if (!t2.hasMoreTokens()) {
                        break;
                    }

                    String token = t2.nextToken();
                    if ("1".equals(token.trim())) {
                        graph.addDirectedEdge(nodes.get(i), nodes.get(j));
                    }
                }

                line = in.readLine();

                if (line == null) {
                    break;
                }
            }

            graphEditable.setGraph(graph);
        }
        catch (FileNotFoundException e1) {

        }
        catch (IOException e2) {

        }
    }

    private static JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String sessionSaveLocation =
                Preferences.userRoot().get("fileSaveLocation", "");
        chooser.setCurrentDirectory(new File(sessionSaveLocation));
        chooser.resetChoosableFileFilters();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return chooser;
    }

    private GraphEditable getGraphEditable() {
        return graphEditable;
    }
}
