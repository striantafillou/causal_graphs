///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetradapp.model.AlgorithmRunner;
import edu.cmu.tetradapp.util.GraphHistory;
import edu.cmu.tetradapp.util.WatchedProcess;
import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.MarshalledObject;
import java.util.ArrayList;

/**
 * Abstract base class for a number of search editors. The advantage of
 * extending this class, in case you were wondering, is that it will handle
 * threading for you, provide a stop button for algorithms, and do logging. The
 * execute button used must be getExecuteButton(), or else logging won't work.
 *
 * @author Joseph Ramsey
 */
public abstract class AbstractSearchEditor extends JPanel implements GraphEditable {

    /**
     * The algorithm wrapper being viewed.
     */
    private AlgorithmRunner algorithmRunner;

    /**
     * The workbench displaying the result workbench.
     */
    private GraphWorkbench workbench;


    /**
     * The button one clicks to executeButton the algorithm.
     */
    private JButton executeButton = new JButton();

    /**
     * The label for the result graph workbench.
     */
    private String resultLabel;

    /**
     * The scrollpange for the result workbench.
     */
    private JScrollPane workbenchScroll;

    /**
     * True if the warning message that previously defined knowledge is being
     * used has already been shown and doesn't need to be shown again.
     */
    private boolean knowledgeMessageShown = false;

    /**
     * History of graph edits.
     */
    private GraphHistory graphHistory = new GraphHistory();

    //============================CONSTRUCTOR===========================//

    public AbstractSearchEditor(AlgorithmRunner algorithmRunner, String resultLabel) {
        if (algorithmRunner == null) {
            throw new NullPointerException();
        }

        if (resultLabel == null) {
            throw new NullPointerException();
        }

        this.algorithmRunner = algorithmRunner;
        this.resultLabel = resultLabel;

        setup(resultLabel);
    }

    //========================== Public Methods required by GraphEditable ======//


    /**
     * Returns the work bench of null if there isn't one.
     */
    public GraphWorkbench getWorkbench() {
        return this.workbench;
    }


    /**
     * Not supported.
     */
    public void setGraph(Graph g) {
        throw new UnsupportedOperationException("Cannot set the graph on a search editor.");
    }


    /**
     * Returns the graph.
     */
    public Graph getGraph() {
        if (this.workbench != null) {
            return this.workbench.getGraph();
        }
        return new EdgeListGraph();
    }


    /**
     * Does nothing.
     */
    public void setName(String name) {

    }

    /**
     * Returns a list of all the SessionNodeWrappers (TetradNodes) and
     * SessionNodeEdges that are model components for the respective
     * SessionNodes and SessionEdges selected in the workbench. Note that the
     * workbench, not the SessionEditorNodes themselves, keeps track of the
     * selection.
     *
     * @return the set of selected model nodes.
     */
    public java.util.List getSelectedModelComponents() {
        java.util.List<Component> selectedComponents =
                workbench.getSelectedComponents();
        java.util.List<TetradSerializable> selectedModelComponents =
                new ArrayList<TetradSerializable>();

        for (Component comp : selectedComponents) {
            if (comp instanceof DisplayNode) {
                selectedModelComponents.add(
                        ((DisplayNode) comp).getModelNode());
            } else if (comp instanceof DisplayEdge) {
                selectedModelComponents.add(
                        ((DisplayEdge) comp).getModelEdge());
            }
        }

        return selectedModelComponents;
    }


    /**
     * Not supported.
     */
    public void pasteSubsession(java.util.List sessionElements, Point upperLeft) {
        throw new UnsupportedOperationException("Cannot paste into Search editor.");
    }

    //===========================PROTECTED METHODS==========================//

    /**
     * Constructs the toolbar panel. For the execute button, must use
     * getExecuteButton() in order for logging to work.
     */
    protected abstract JPanel getToolbar();

    /**
     * Adds any special menus needed for a particular search editor. These will
     * be added to the right of the normal ones.
     */
    protected abstract void addSpecialMenus(JMenuBar menuBar);


    /**
     * Creates the logging menu for the editor, by default null is returned and nothing
     * is displayed.
     *
     * @return - Menu
     */
    protected JMenu createLoggingMenu() {
        return null;
    }


    /**
     * Executes the algorithm. The execution takes place inside a thread, so one
     * cannot count on a result graph having been found when the method
     * returns.
     */
    public void execute() {
        Window owner = (Window) getTopLevelAncestor();

        final WatchedProcess process = new WatchedProcess(owner) {
            public void watch() {
                getExecuteButton().setEnabled(false);
                setErrorMessage(null);

                if (!knowledgeMessageShown) {
                    Knowledge knowledge = getAlgorithmRunner().getParams().getKnowledge();
                    if (!knowledge.equals(new Knowledge())) {
                        JOptionPane.showMessageDialog(
                                JOptionUtils.centeringComp(),
                                "Using previously set knowledge. (To edit, use " +
                                        "the Knowledge menu.)");
                        knowledgeMessageShown = true;
                    }
                }

                try {
                    startLogging();
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "Could not start logging.");
                    ex.printStackTrace();
                    return;
                }

                try {
                    storeLatestWorkbenchGraph();
                    getAlgorithmRunner().execute();
                } catch (Exception e) {
                    CharArrayWriter writer1 = new CharArrayWriter();
                    PrintWriter writer2 = new PrintWriter(writer1);
                    e.printStackTrace(writer2);
                    String message = writer1.toString();
                    writer2.close();

                    e.printStackTrace(System.out);

                    TetradLogger.getInstance().error(message);

                    String messageString = e.getMessage();

                    if (e.getCause() != null) {
                        messageString = e.getCause().getMessage();
                    }

                    if (messageString == null) {
                        messageString = message;
                    }
                    setErrorMessage(messageString);

                    TetradLogger.getInstance().error("************Algorithm stopped!");

                    getExecuteButton().setEnabled(true);
                    stopLogging();
                    throw new RuntimeException(e);
                }

                getWorkbenchScroll().setBorder(
                        new TitledBorder(getResultLabel()));
                Graph resultGraph = resultGraph();

                doDefaultArrangement(resultGraph);
                getWorkbench().setBackground(Color.WHITE);
                getWorkbench().setGraph(resultGraph);
                getGraphHistory().clear();
                getGraphHistory().add(resultGraph);
                getWorkbench().repaint();

                // For Mimbuild, e.g., that need to do a second stage.
                firePropertyChange("algorithmFinished", null, null);
                stopLogging();
                getExecuteButton().setEnabled(true);
                firePropertyChange("modelChanged", null, null);
            }
        };

        Thread watcher = new Thread() {
            public void run() {
                while (true) {
                    try {
                        sleep(300);

                        if (!process.isAlive()) {
                            getExecuteButton().setEnabled(true);
                            return;
                        }
                    }
                    catch (InterruptedException e) {
                        getExecuteButton().setEnabled(true);
                        return;
                    }
                }
            }
        };

        watcher.start();
    }

    protected void doPostExecutionSteps() {
        // Do nothing for abstract search editor.
    }

    protected abstract void doDefaultArrangement(Graph resultGraph);


    public JButton getExecuteButton() {
        return executeButton;
    }

    public AlgorithmRunner getAlgorithmRunner() {
        return algorithmRunner;
    }

    //===========================PRIVATE METHODS==========================//

    private Graph resultGraph() {
        Graph resultGraph = algorithmRunner.getResultGraph();

        if (resultGraph == null) {
            resultGraph = new EdgeListGraph();
        }

        return resultGraph;
    }


    private void startLogging() {
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.algorithmRunner.getClass());
        if (config != null) {
            TetradLogger.getInstance().setNextOutputStream();
            TetradLogger.getInstance().setTetradLoggerConfig(config);
        }
    }


    private static void stopLogging() {
        TetradLogger.getInstance().removeNextOutputStream();
        TetradLogger.getInstance().reset();
    }


    /**
     * Sets up the editor, does the layout, and so on.
     */
    void setup(String resultLabel) {
        setLayout(new BorderLayout());
        add(getToolbar(), BorderLayout.WEST);
        add(workbenchScroll(resultLabel), BorderLayout.CENTER);
        add(menuBar(), BorderLayout.NORTH);
    }


    JScrollPane workbenchScroll(String resultLabel) {
        Graph resultGraph = resultGraph();

        Graph sourceGraph = algorithmRunner.getSourceGraph();
        Graph latestWorkbenchGraph = algorithmRunner.getParams()
                .getSourceGraph();

        boolean arrangedAll = GraphUtils.arrangeBySourceGraph(resultGraph,
                latestWorkbenchGraph);

        if (!arrangedAll) {
            arrangedAll =
                    GraphUtils.arrangeBySourceGraph(resultGraph, sourceGraph);
        }

        if (!arrangedAll) {
            GraphUtils.arrangeInCircle(resultGraph, 200, 200, 150);
        }

        this.workbench = new GraphWorkbench(resultGraph);

        graphHistory.clear();
        graphHistory.add(resultGraph);

        this.workbench.setAllowDoubleClickActions(false);
        this.workbench.setAllowNodeEdgeSelection(true);
        this.workbenchScroll = new JScrollPane(workbench);
        workbenchScroll.setPreferredSize(new Dimension(450, 450));
        workbenchScroll.setBorder(new TitledBorder(resultLabel));

        this.workbench.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                storeLatestWorkbenchGraph();
            }
        });

        return workbenchScroll;
    }

//     JMenuItem copy = new JMenuItem(new CopySubgraphAction(this));
//        JMenuItem paste = new JMenuItem(new PasteSubgraphAction(this));
//
//        copy.setAccelerator(
//                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
//        paste.setAccelerator(
//                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
//
//        edit.add(copy);
//        edit.add(paste);

    /**
     * Creates the menubar for the search editor.
     */
    protected JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));

        JMenu edit = new JMenu("Edit");
        JMenuItem copy = new JMenuItem(new CopySubgraphAction(this));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        edit.add(copy);

        menuBar.add(edit);


        JMenu loggingMenu = createLoggingMenu();
        if (loggingMenu != null) {
            menuBar.add(loggingMenu);
        }

        addSpecialMenus(menuBar);

        return menuBar;
    }


    private String getResultLabel() {
        return resultLabel;
    }

    private JScrollPane getWorkbenchScroll() {
        return workbenchScroll;
    }

    Graph getLatestWorkbenchGraph() {
        Graph graph = algorithmRunner.getParams().getSourceGraph();

        if (graph == null) {
            return algorithmRunner.getSourceGraph();
        }

        return graph;
    }

    void storeLatestWorkbenchGraph() {
        Graph latestWorkbenchGraph = workbench.getGraph();

        if (latestWorkbenchGraph.getNumNodes() == 0) {
            return;
        }

        try {
            Graph graph =
                    (Graph) new MarshalledObject(latestWorkbenchGraph).get();
            algorithmRunner.getParams().setSourceGraph(graph);
        }
        catch (IOException e) {
            algorithmRunner.getParams().setSourceGraph(null);
        }
        catch (ClassNotFoundException e) {
            algorithmRunner.getParams().setSourceGraph(null);
            e.printStackTrace();
        }
    }

    public GraphHistory getGraphHistory() {
        return graphHistory;
    }
}


