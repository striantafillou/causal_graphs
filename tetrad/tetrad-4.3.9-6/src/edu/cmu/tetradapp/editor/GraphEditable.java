package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import java.awt.*;
import java.util.List;

/**
 * Inteface for graph editors, useful for situations where graph editors need to
 * be treated generally.
 *
 * @author Joseph Ramsey
 */
public interface GraphEditable {

    /**
     * Sets the name of the editor.
     * @param name The name to be set.
     */
    void setName(String name);

    /**
     * Returns the selected components (display nodes and display edges) in the
     * editor.
     * @return the selected components.
     */
    List getSelectedModelComponents();

    /**
     * Pastes a list of components (display nodes and display edges) into the
     * workbench of the editor.
     * @param sessionElements The session elements.
     * @param upperLeft the upper left point of the paste area.
     */
    void pasteSubsession(List sessionElements, Point upperLeft);

    /**
     * Returns the graph workbench.
     * @return the workbench.
     */
    GraphWorkbench getWorkbench();

    /**
     * Returns the graph.
     * @return the graph.
     */
    Graph getGraph();

    /**
     * Sets the graph.
     * @param graph The graph to be set.
     */
    void setGraph(Graph graph);
}
