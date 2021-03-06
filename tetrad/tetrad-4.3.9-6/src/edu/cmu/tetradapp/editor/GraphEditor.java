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
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetradapp.model.ChooseDagGraphWrapper;
import edu.cmu.tetradapp.model.CompletedPatternWrapper;
import edu.cmu.tetradapp.model.GraphWrapper;
import edu.cmu.tetradapp.model.IndTestProducer;
import edu.cmu.tetradapp.util.LayoutEditable;
import edu.cmu.tetradapp.util.LayoutMenu;
import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Displays a workbench editing workbench area together with a toolbench for
 * editing tetrad-style graphs.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey
 */
public final class GraphEditor extends JPanel
        implements GraphEditable, LayoutEditable, IndTestProducer {
    private final GraphWorkbench workbench;
    private GraphWrapper graphWrapper;

    //===========================PUBLIC METHODS========================//

    public GraphEditor(GraphWrapper graphWrapper) {
        this(graphWrapper.getGraph());
        this.graphWrapper = graphWrapper;

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("graph".equals(evt.getPropertyName())) {
                    getGraphWrapper().setGraph((Graph) evt.getNewValue());
                }
                else if ("modelChanged".equals(evt.getPropertyName())) {
                    firePropertyChange("modelChanged", null, null);
                }
            }
        });
    }

    public GraphEditor(ChooseDagGraphWrapper wrapper) {
        this(wrapper.getGraph());
    }

    public GraphEditor(CompletedPatternWrapper wrapper) {
        this(wrapper.getGraph());
    }

    //===========================PRIVATE METHODS======================//

    /**
     * Constructs a new GraphEditor for the given EdgeListGraph.
     */
    public GraphEditor(Graph graph) {
        setLayout(new BorderLayout());

        this.workbench = new GraphWorkbench(graph);
        GraphToolbar toolbar = new GraphToolbar(getWorkbench());
        JMenuBar menuBar = createGraphMenuBar();
        JScrollPane scroll = new JScrollPane(getWorkbench());
        scroll.setPreferredSize(new Dimension(450, 450));

        add(scroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
        add(menuBar, BorderLayout.NORTH);

        JLabel label = new JLabel("Double click variable to change name.");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(2));
        b.add(label);
        b.add(Box.createHorizontalGlue());
        b.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

        add(b, BorderLayout.SOUTH);

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();

                if ("graph".equals(propertyName)) {
                    Graph _graph = (Graph) evt.getNewValue();

                    if (getWorkbench() != null) {
                        getGraphWrapper().setGraph(_graph);
                    }
                }
            }
        });
    }

    /**
     * Sets the name of this editor.
     */
    public final void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
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
    public List getSelectedModelComponents() {
        List<Component> selectedComponents =
                getWorkbench().getSelectedComponents();
        List<TetradSerializable> selectedModelComponents =
                new ArrayList<TetradSerializable>();

        for (Iterator<Component> it =
                selectedComponents.iterator(); it.hasNext();) {
            Object comp = it.next();

            if (comp instanceof DisplayNode) {
                selectedModelComponents.add(
                        ((DisplayNode) comp).getModelNode());
            }
            else if (comp instanceof DisplayEdge) {
                selectedModelComponents.add(
                        ((DisplayEdge) comp).getModelEdge());
            }
        }

        return selectedModelComponents;
    }

    /**
     * Pastes list of session elements into the workbench.
     */
    public void pasteSubsession(List sessionElements, Point upperLeft) {
        getWorkbench().pasteSubgraph(sessionElements, upperLeft);
        getWorkbench().deselectAll();

        for (int i = 0; i < sessionElements.size(); i++) {

            Object o = sessionElements.get(i);

            if (o instanceof GraphNode) {
                Node modelNode = (Node) o;
                getWorkbench().selectNode(modelNode);
            }
        }

        getWorkbench().selectConnectingEdges();
    }

    public GraphWorkbench getWorkbench() {
        return workbench;
    }

    public Graph getGraph() {
        return workbench.getGraph();
    }

    public void setGraph(Graph graph) {
        workbench.setGraph(graph);
    }

    public Knowledge getKnowledge() {
        return null;
    }

    public Graph getSourceGraph() {
        return getWorkbench().getGraph();
    }

    public void layoutByGraph(Graph graph) {
        getWorkbench().layoutByGraph(graph);
    }

    public void layoutByKnowledge() {
        // Does nothing.
    }

    public Rectangle getVisibleRect() {
        return getWorkbench().getVisibleRect();
    }

    private GraphWrapper getGraphWrapper() {
        return graphWrapper;
    }

    //===========================PRIVATE METHODS========================//

    private JMenuBar createGraphMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu graphMenu = createGraphMenu();

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(graphMenu);
        menuBar.add(new LayoutMenu(this));

        return menuBar;
    }

    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * workbench models.
     *
     * @return this menu.
     */
    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");

        file.add(new LoadGraph(this, "Load Graph..."));
        file.add(new SaveGraph(this, "Save Graph..."));
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));

        return file;
    }

    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * workbench models.
     *
     * @return this menu.
     */
    private JMenu createEditMenu() {
        JMenu edit = new JMenu("Edit");

        JMenuItem copy = new JMenuItem(new CopySubgraphAction(this));
        JMenuItem paste = new JMenuItem(new PasteSubgraphAction(this));

        copy.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        paste.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        edit.add(copy);
        edit.add(paste);

        return edit;
    }

    private JMenu createGraphMenu() {
        JMenu graph = new JMenu("Graph");

        graph.add(new GraphPropertiesAction(getWorkbench()));
        graph.add(new DirectedPathsAction(getWorkbench()));
        graph.add(new TreksAction(getWorkbench()));
        graph.add(new AllPathsAction(getWorkbench()));
        graph.add(new NeighborhoodsAction(getWorkbench()));
        graph.addSeparator();

        JMenuItem correlateExogenous =
                new JMenuItem("Correlate Exogenous Variables");
        JMenuItem uncorrelateExogenous =
                new JMenuItem("Uncorrelate Exogenous Variables");
        graph.add(correlateExogenous);
        graph.add(uncorrelateExogenous);
        graph.addSeparator();

        correlateExogenous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                correlateExogenousVariables();
                getWorkbench().invalidate();
                getWorkbench().repaint();
            }
        });

        uncorrelateExogenous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uncorrelationExogenousVariables();
                getWorkbench().invalidate();
                getWorkbench().repaint();
            }
        });

        JMenuItem randomDag = new JMenuItem("Random DAG");
        graph.add(randomDag);

        randomDag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RandomDagEditor editor = new RandomDagEditor();

                int ret = JOptionPane.showConfirmDialog(
                        JOptionUtils.centeringComp(), editor,
                        "Edit Random DAG Parameters",
                        JOptionPane.PLAIN_MESSAGE);

                if (ret == JOptionPane.OK_OPTION) {
                    int numNodes = editor.getNumNodes();
                    int numLatentNodes = editor.getNumLatents();
                    int maxEdges = editor.getMaxEdges();
                    int maxDegree = editor.getMaxDegree();
                    int maxIndegree = editor.getMaxIndegree();
                    int maxOutdegree = editor.getMaxOutdegree();
                    boolean connected = editor.isConnected();
                    boolean uniformlySelected = editor.isUniformlySelected();
                    Dag dag;

                    if (uniformlySelected) {
                        dag = GraphUtils.randomDag(numNodes,
                            numLatentNodes, maxEdges, maxDegree, maxIndegree,
                            maxOutdegree, connected);
                    }
                    else {
//                        dag = GraphUtils.randomDagC(numNodes, numLatentNodes,
//                                maxEdges
//                        );
                        dag = GraphUtils.randomDag(numNodes, numLatentNodes,
                                maxEdges, 30, 15, 15, connected);
                    }

                    workbench.setGraph(new EdgeListGraph(dag));
                }
            }
        });

        JMenuItem randomIndicatorModel =
                new JMenuItem("Random Multiple Indicator Model");
        graph.add(randomIndicatorModel);

        randomIndicatorModel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RandomMimParamsEditor editor = new RandomMimParamsEditor();

                int ret = JOptionPane.showConfirmDialog(
                        JOptionUtils.centeringComp(), editor,
                        "Edit Random MIM Parameters",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                if (ret == JOptionPane.OK_OPTION) {
                    int numStructuralNodes = Preferences.userRoot().getInt(
                            "numStructuralNodes", 3);
                    int maxStructuralEdges = Preferences.userRoot().getInt(
                            "numStructuralEdges", 3);
                    int measurementModelDegree = Preferences.userRoot().getInt(
                            "measurementModelDegree", 3);
                    int numLatentMeasuredImpureParents = Preferences.userRoot()
                            .getInt("latentMeasuredImpureParents", 0);
                    int numMeasuredMeasuredImpureParents =
                            Preferences.userRoot()
                                    .getInt("measuredMeasuredImpureParents", 0);
                    int numMeasuredMeasuredImpureAssociations =
                            Preferences.userRoot()
                                    .getInt("measuredMeasuredImpureAssociations",
                                            0);

                    Graph graph = GraphUtils.randomMim(numStructuralNodes,
                            maxStructuralEdges, measurementModelDegree,
                            numLatentMeasuredImpureParents,
                            numMeasuredMeasuredImpureParents,
                            numMeasuredMeasuredImpureAssociations);

                    workbench.setGraph(graph);
                }
            }
        });


        graph.addSeparator();
        graph.add(new JMenuItem(new SelectBidirectedAction(getWorkbench())));
        graph.add(new JMenuItem(new SelectUndirectedAction(getWorkbench())));

        graph.addSeparator();
        IndependenceFactsAction action = new IndependenceFactsAction(
                JOptionUtils.centeringComp(), this, "D Separation Facts...");
        graph.add(action);

        return graph;
    }

    private void correlateExogenousVariables() {
        Graph graph = getWorkbench().getGraph();

        if (graph instanceof Dag) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Cannot add bidirected edges to DAG's.");
            return;
        }

        List<Node> nodes = graph.getNodes();

        List<Node> exoNodes = new LinkedList<Node>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (graph.isExogenous(node)) {
                exoNodes.add(node);
            }
        }

        for (int i = 0; i < exoNodes.size(); i++) {

            loop:
            for (int j = i + 1; j < exoNodes.size(); j++) {
                Node node1 = exoNodes.get(i);
                Node node2 = exoNodes.get(j);
                List<Edge> edges = graph.getEdges(node1, node2);

                for (int k = 0; k < edges.size(); k++) {
                    Edge edge = edges.get(k);
                    if (Edges.isBidirectedEdge(edge)) {
                        continue loop;
                    }
                }

                graph.addBidirectedEdge(node1, node2);
            }
        }
    }

    private void uncorrelationExogenousVariables() {
        Graph graph = getWorkbench().getGraph();

        List<Edge> edges = graph.getEdges();

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);

            if (Edges.isBidirectedEdge(edge)) {
                try {
                    graph.removeEdge(edge);
                }
                catch (Exception e) {
                    // Ignore. 
                }
            }
        }
    }

    public IndependenceTest getIndependenceTest() {
        Graph graph = workbench.getGraph();
        EdgeListGraph listGraph = new EdgeListGraph(graph);
        return new IndTestDSep(listGraph);
    }
}


