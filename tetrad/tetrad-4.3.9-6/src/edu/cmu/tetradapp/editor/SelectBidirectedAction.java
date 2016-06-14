package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Edges;
import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

/**
 * Copies a selection of session nodes in the frontmost session editor, to the
 * clipboard.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
class SelectBidirectedAction extends AbstractAction implements ClipboardOwner {

    /**
     * The desktop containing the target session editor.
     */
    private GraphWorkbench workbench;

    /**
     * Creates a new copy subsession action for the given desktop and
     * clipboard.
     */
    public SelectBidirectedAction(GraphWorkbench workbench) {
        super("Highlight Bidirected Edges");

        if (workbench == null) {
            throw new NullPointerException("Desktop must not be null.");
        }

        this.workbench = workbench;
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    public void actionPerformed(ActionEvent e) {
        workbench.deselectAll();

        for (Component comp : workbench.getComponents()) {
            if (comp instanceof DisplayEdge) {
                Edge edge = ((DisplayEdge) comp).getModelEdge();
                if (Edges.isBidirectedEdge(edge)) {
                    workbench.selectEdge(edge);
                }
            }
        }
    }

    /**
     * Required by the AbstractAction interface; does nothing.
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
