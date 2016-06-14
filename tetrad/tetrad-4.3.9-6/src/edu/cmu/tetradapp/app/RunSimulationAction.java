package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.session.SessionNode;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetradapp.util.WatchedProcess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

/**
 * Sets up parameters for logging.
 *
 * @author Joseph Ramsey
 */
class RunSimulationAction extends AbstractAction {



    private SessionEditorNode sessionEditorNode;

    /**
     * Constructs a new action to open sessions.
     */
    public RunSimulationAction(SessionEditorNode sessionEditorNode) {
        super("Run Simulation...");
        this.sessionEditorNode = sessionEditorNode;
    }


    public void actionPerformed(ActionEvent e) {
        // display dialog to user.
        JComponent comp = SetupLoggingAction.buildSetupLoggingComponent();
        int option = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(), comp,
                "File Logging Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // only try to save if OK_OPTION was selected.
        if (option == JOptionPane.OK_OPTION) {
            executeNode();
        }
    }



    private void executeNode() {
        Set<SessionNode> children = sessionEditorNode.getChildren();
        boolean noEmptyChildren = true;

        for (SessionNode child : children) {
            if (child.getModel() == null) {
                noEmptyChildren = false;
                break;
            }
        }

        if (!noEmptyChildren) {
            Component centeringComp = sessionEditorNode;
            JOptionPane.showMessageDialog(centeringComp, "Nothing to run.");
            return;
        }

        Object[] options = {"Simulate", "Cancel"};

        int selection = JOptionPane.showOptionDialog(
                JOptionUtils.centeringComp(),
                "Executing this node will erase the model for this node, " +
                        "\nerase the models for any descendant nodes, and recreate " +
                        "\nthem all using new models with new values. Continue?",
                "Warning", JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[1]);

        if (selection == 0) {
            executeSessionNode(sessionEditorNode.getSessionNode(), true);
        }
    }


    private SessionEditorWorkbench getWorkbench(){
        Class c = SessionEditorWorkbench.class;
        Container container = SwingUtilities.getAncestorOfClass(c,sessionEditorNode);
        return (SessionEditorWorkbench) container;
    }


    private void executeSessionNode(final SessionNode sessionNode, final boolean overwrite) {
        Window owner = (Window) sessionEditorNode.getTopLevelAncestor();

        new WatchedProcess(owner) {
            public void watch() {
                SessionEditorWorkbench workbench = getWorkbench();
                try {
                    TetradLogger.getInstance().setNextOutputStream();
                } catch (IllegalStateException e){
                    TetradLogger.getInstance().removeNextOutputStream();
                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), e.getMessage());
                    return;
                }
                workbench.getSimulationStudy().execute(sessionNode, overwrite);
                TetradLogger.getInstance().removeNextOutputStream();
            }
        };
    }

}
