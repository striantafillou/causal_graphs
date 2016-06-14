package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.ParamType;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemIm2;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.Sem2ImWrapper;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.GraphNodeMeasured;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

/**
 * Edits a SEM instantiated model.
 *
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 */
public final class Sem2ImEditor extends JPanel {

    /**
     * The SemIm being edited.
     */
    private SemIm2 semIm2;

    /**
     * The graphical editor for the SemIm.
     */
    private Sem2ImGraphicalEditor sem2ImGraphicalEditor;

    /**
     * Maximum number of free parameters for which statistics will be
     * calculated. (Calculating standard errors is high complexity.)
     */
    private int maxFreeParamsForStatistics = 100;

    /**
     * True iff covariance parameters are edited as correlations.
     */
    private boolean editCovariancesAsCorrelations = false;

    /**
     * True iff covariance parameters are edited as correlations.
     */
    private boolean editIntercepts = false;
    private JTabbedPane tabbedPane;
    private String graphicalEditorTitle = "Graphical Editor";
    private String tabularEditorTitle = "TabularEditor";
    private boolean editable = true;
    private JCheckBoxMenuItem meansItem;
    private JCheckBoxMenuItem interceptsItem;

    //========================CONSTRUCTORS===========================//

    public Sem2ImEditor(SemIm2 semIm2) {
        this(semIm2, "Graphical Editor", "Tabular Editor");
    }

    /**
     * Constructs an editor for the given SemIm.
     */
    public Sem2ImEditor(SemIm2 semIm2, String graphicalEditorTitle,
                       String tabularEditorTitle) {
        if (semIm2 == null) {
            throw new NullPointerException("SemIm must not be null.");
        }

        this.semIm2 = semIm2;
//        semIm2.getSemPm2().getGraph().setShowErrorTerms(false);
        this.graphicalEditorTitle = graphicalEditorTitle;
        this.tabularEditorTitle = tabularEditorTitle;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        tabbedPane.add(graphicalEditorTitle, graphicalEditor());

        add(tabbedPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(sem2ImGraphicalEditor.getWorkbench(),
                "Save Graph Image..."));

        JCheckBoxMenuItem covariances =
                new JCheckBoxMenuItem("Show covariances");
        JCheckBoxMenuItem correlations =
                new JCheckBoxMenuItem("Show correlations");

        ButtonGroup correlationGroup = new ButtonGroup();
        correlationGroup.add(covariances);
        correlationGroup.add(correlations);
        covariances.setSelected(true);

        JMenuItem errorTerms = new JMenuItem();

        // By default, hide the error terms.
        getSemGraph().setShowErrorTerms(false);

        if (getSemGraph().isShowErrorTerms()) {
            errorTerms.setText("Hide Error Terms");
        } else {
            errorTerms.setText("Show Error Terms");
        }

        errorTerms.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem menuItem = (JMenuItem) e.getSource();

                if ("Hide Error Terms".equals(menuItem.getText())) {
                    menuItem.setText("Show Error Terms");
                    getSemGraph().setShowErrorTerms(false);
                    graphicalEditor().resetLabels();
                } else if ("Show Error Terms".equals(menuItem.getText())) {
                    menuItem.setText("Hide Error Terms");
                    getSemGraph().setShowErrorTerms(true);
                    graphicalEditor().resetLabels();
                }
            }
        });

//        menuBar.add(graph);

        meansItem = new JCheckBoxMenuItem("Show means");
        interceptsItem = new JCheckBoxMenuItem("Show intercepts");

        ButtonGroup meansGroup = new ButtonGroup();
        meansGroup.add(meansItem);
        meansGroup.add(interceptsItem);
        meansItem.setSelected(true);

        JMenu params = new JMenu("Parameters");
        params.add(errorTerms);
        params.addSeparator();
        params.add(covariances);
        params.add(correlations);
        params.addSeparator();

        menuBar.add(params);

        add(menuBar, BorderLayout.NORTH);
    }

    private SemGraph getSemGraph() {
        return semIm2.getSemPm2().getGraph();
    }

    /**
     * Constructs a new SemImEditor from the given OldSemEstimateAdapter.
     */
    public Sem2ImEditor(Sem2ImWrapper semIm2Wrapper) {
        this(semIm2Wrapper.getSemIm2());
    }

    /**
     * Returns the index of the currently selected tab. Used to construct a new
     * SemImEditor in the same state as a previous one.
     */
    public int getTabSelectionIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Sets a new SemIm to edit.
     */
    public void setSemIm(SemIm2 semIm2, int tabSelectionIndex,
                         int matrixSelection) {
        if (semIm2 == null) {
            throw new NullPointerException();
        }

        if (tabSelectionIndex < 0 || tabSelectionIndex >= 4) {
            throw new IllegalArgumentException(
                    "Tab selection must be 0, 1, 2, or 3: " + tabSelectionIndex);
        }

        if (matrixSelection < 0 || matrixSelection >= 4) {
            throw new IllegalArgumentException(
                    "Matrix selection must be 0, 1, 2, or 3: " + matrixSelection);
        }

        Graph oldGraph = this.semIm2.getSemPm2().getGraph();

        this.semIm2 = semIm2;
        GraphUtils.arrangeBySourceGraph(semIm2.getSemPm2().getGraph(), oldGraph);

        this.sem2ImGraphicalEditor = null;

        tabbedPane.removeAll();
        tabbedPane.add(getGraphicalEditorTitle(), graphicalEditor());

        tabbedPane.setSelectedIndex(tabSelectionIndex);
        tabbedPane.validate();
    }

    //========================PRIVATE METHODS===========================//

    private SemIm2 getSemIm2() {
        return semIm2;
    }

    private Sem2ImGraphicalEditor graphicalEditor() {
        if (this.sem2ImGraphicalEditor == null) {
            this.sem2ImGraphicalEditor = new Sem2ImGraphicalEditor(getSemIm2(),
                    this, this.maxFreeParamsForStatistics);
            this.sem2ImGraphicalEditor.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            firePropertyChange(evt.getPropertyName(), null,
                                    null);
                        }
                    });
        }
        return this.sem2ImGraphicalEditor;
    }

    public boolean isEditCovariancesAsCorrelations() {
        return editCovariancesAsCorrelations;
    }

    public boolean isEditIntercepts() {
        return editIntercepts;
    }

    private String getGraphicalEditorTitle() {
        return graphicalEditorTitle;
    }

    private String getTabularEditorTitle() {
        return tabularEditorTitle;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        graphicalEditor().setEditable(editable);
        this.editable = editable;
    }
}

/**
 * Edits the parameters of the SemIm using a graph workbench.
 */
final class Sem2ImGraphicalEditor extends JPanel {

    /**
     * Font size for parameter values in the graph.
     */
    private static Font SMALL_FONT = new Font("Dialog", Font.PLAIN, 10);

    /**
     * Background color of the edit panel when you click on the parameters.
     */
    private static Color LIGHT_YELLOW = new Color(255, 255, 215);

    /**
     * Formats numbers.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The SemIM being edited.
     */
    private SemIm2 semIm2;

    /**
     * Workbench for the graphical editor.
     */
    private GraphWorkbench workbench;

    /**
     * Stores the last active edge so that it can be reset properly.
     */
    private Object lastEditedObject = null;

    /**
     * This delay needs to be restored when the component is hidden.
     */
    private int savedTooltipDelay = 0;

    /**
     * The editor that sits inside the SemImEditor that allows the user to edit
     * the SemIm graphically.
     */
    private Sem2ImEditor editor = null;

    /**
     * True iff this graphical display is editable.
     */
    private boolean editable = true;

    /**
     * Constructs a SemIm2 graphical editor for the given SemIm2.
     */
    public Sem2ImGraphicalEditor(SemIm2 semIm2, Sem2ImEditor editor,
                                int maxFreeParamsForStatistics) {
        this.semIm2 = semIm2;
        this.editor = editor;

        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(workbench());
        scroll.setPreferredSize(new Dimension(450, 450));

        add(scroll, BorderLayout.CENTER);
        setBorder(new TitledBorder("Click parameter values to edit"));

        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        setSavedTooltipDelay(toolTipManager.getInitialDelay());

        // Laborious code that follows is intended to make sure tooltips come
        // almost immediately within the editor but more slowly outside.
        // Ugh.
        workbench().addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                resetLabels();
                ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.setInitialDelay(100);
            }

            public void componentHidden(ComponentEvent e) {
                ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.setInitialDelay(getSavedTooltipDelay());
            }
        });

        workbench().addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (workbench().contains(e.getPoint())) {

                    // Commenting out the resetLabels, since it seems to make
                    // people confused when they can't move the mouse away
                    // from the text field they are editing without the
                    // textfield disappearing. jdramsey 3/16/2005.
//                    resetLabels();
                    ToolTipManager toolTipManager =
                            ToolTipManager.sharedInstance();
                    toolTipManager.setInitialDelay(100);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!workbench().contains(e.getPoint())) {
                    ToolTipManager toolTipManager =
                            ToolTipManager.sharedInstance();
                    toolTipManager.setInitialDelay(getSavedTooltipDelay());
                }
            }
        });

        // Make sure the graphical editor reflects changes made to parameters
        // in other editors.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                resetLabels();
            }
        });
    }

    //========================PRIVATE METHODS===========================//

    private void beginEdgeEdit(Edge edge) {
    }

    private void beginNodeEdit(Node node) {
//        finishEdit();
//
//        if (!isEditable()) {
//            return;
//        }
//
//        if (node.getNodeType() != NodeType.ERROR) {
//
//        }
//
//        Parameter parameter = getNodeParameter(node);
//        if (editor.isEditCovariancesAsCorrelations() &&
//                parameter.getType() == ParamType.VAR) {
//            return;
//        }
//
//        double d;
//        String prefix;
//        String postfix = "";
//
//        if (parameter.getType() == ParamType.MEAN) {
//            if (editor.isEditIntercepts()) {
//                d = semIm().getIntercept(node);
//                prefix = "B0_" + node.getName() + " = ";
//            } else {
//                d = semIm().getMean(node);
//                prefix = "Mean(" + node.getName() + ") = ";
//            }
//        } else {
//            d = Math.sqrt(semIm().getParamValue(parameter));
//            prefix = node.getName() + " ~ N(0,";
//            postfix = ")";
//        }
//
//        DoubleTextField field = new DoubleTextField(d, 7, NumberFormatUtil.getInstance().getNumberFormat());
//        field.setPreferredSize(new Dimension(60, 20));
//        field.addActionListener(new NodeActionListener(this, node));
//
//        field.addFocusListener(new FocusAdapter() {
//            public void focusLost(FocusEvent e) {
//                DoubleTextField field = (DoubleTextField) e.getSource();
//                field.grabFocus();
//            }
//        });
//
//        JLabel instruct = new JLabel("Press Enter when done");
//        instruct.setFont(SMALL_FONT);
//        instruct.setForeground(Color.GRAY);
//
//        Box b1 = Box.createHorizontalBox();
//        b1.add(new JLabel(prefix));
//        b1.add(field);
//        b1.add(new JLabel(postfix));
//
//        Box b2 = Box.createHorizontalBox();
//        b2.add(instruct);
//
//        JPanel editPanel = new JPanel();
//        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
//        editPanel.setBackground(LIGHT_YELLOW);
//        editPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//        editPanel.add(b1);
//        editPanel.add(Box.createVerticalStrut(5));
//        editPanel.add(b2);
//
//        workbench().setNodeLabel(node, editPanel, 15, 2);
//        setLastEditedObject(node);
//
//        workbench().repaint();
//        field.grabFocus();
//        field.selectAll();
    }

    private void finishEdit() {
    }

    private SemIm2 semIm2() {
        return this.semIm2;
    }

    private Graph graph() {
        return this.semIm2().getSemPm2().getGraph();
    }

    private GraphWorkbench workbench() {
        if (this.getWorkbench() == null) {
            this.workbench = new GraphWorkbench(graph());
            this.getWorkbench().setAllowDoubleClickActions(false);
            this.getWorkbench().addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("BackgroundClicked".equals(
                                    evt.getPropertyName())) {
                                finishEdit();
                            }
                        }
                    });
            resetLabels();
            addMouseListenerToGraphNodesMeasured();
        }
        return getWorkbench();
    }

    public void resetLabels() {
        for (Object o : graph().getEdges()) {
            resetEdgeLabel((Edge) (o));
        }

        java.util.List<Node> nodes = graph().getNodes();

        for (Object node : nodes) {
            resetNodeLabel((Node) node);
        }

        workbench().repaint();
    }

    private void resetEdgeLabel(Edge edge) {
        Parameter parameter = getEdgeParameter(edge);

        if (parameter != null) {
            double val = semIm2().getParamValue(parameter);

            JLabel label = new JLabel();

            if (parameter.getType() == ParamType.COVAR) {
                label.setForeground(Color.red);
            }

            label.setBackground(Color.white);
            label.setOpaque(true);
            label.setFont(SMALL_FONT);
            label.setText(" " + asString1(val) + " ");
            label.setToolTipText(parameter.getName() + " = " + asString1(val));
            label.addMouseListener(new EdgeMouseListener(edge, this));

            workbench().setEdgeLabel(edge, label);
        } else {
            workbench().setEdgeLabel(edge, null);
        }
    }

    private String asString1(double value) {
        if (Double.isNaN(value)) {
            return " * ";
        } else {
            return nf.format(value);
        }
    }

    private String asString2(double value) {
        if (Double.isNaN(value)) {
            return "*";
        } else {
            return nf.format(value);
        }
    }

    private void resetNodeLabel(Node node) {
    }

    /**
     * Returns the parameter for the given edge, or null if the edge does not
     * have a parameter associated with it in the model. The edge must be either
     * directed or bidirected, since it has to come from a SemGraph. For
     * directed edges, this method automatically adjusts if the user has changed
     * the endpoints of an edge X1 --> X2 to X1 <-- X2 and returns the correct
     * parameter.
     *
     * @throws IllegalArgumentException if the edge is neither directed nor
     *                                  bidirected.
     */
    public Parameter getEdgeParameter(Edge edge) {
        if (Edges.isDirectedEdge(edge)) {
            return semIm2().getSemPm2().getCoefficientParameter(edge.getNode1(), edge.getNode2());
        } else if (Edges.isBidirectedEdge(edge)) {
            return semIm2().getSemPm2().getCovarianceParameter(edge.getNode1(), edge.getNode2());
        }

        throw new IllegalArgumentException(
                "This is not a directed or bidirected edge: " + edge);
    }

    private void setEdgeValue(Edge edge, String text) {
    }

    private void setNodeValue(Node node, String text) {
    }

    private int getSavedTooltipDelay() {
        return savedTooltipDelay;
    }

    private void setSavedTooltipDelay(int savedTooltipDelay) {
        if (this.savedTooltipDelay == 0) {
            this.savedTooltipDelay = savedTooltipDelay;
        }
    }

    private void addMouseListenerToGraphNodesMeasured() {
        java.util.List nodes = graph().getNodes();

        for (Object node : nodes) {
            Object displayNode = workbench().getModelToDisplay().get(node);

            if (displayNode instanceof GraphNodeMeasured) {
                DisplayNode _displayNode = (DisplayNode) displayNode;
                _displayNode.setToolTipText(
                        getEquationOfNode(_displayNode.getModelNode())
                );
            }
        }
    }

    private String getEquationOfNode(Node node) {
        String eqn = node.getName() + " = B0_" + node.getName();

        SemGraph semGraph = semIm2().getSemPm2().getGraph();
        java.util.List parentNodes = semGraph.getParents(node);

        for (Object parentNodeObj : parentNodes) {
            Node parentNode = (Node) parentNodeObj;
//            Parameter edgeParam = semIm2().getSemPm2().getEdgeParameter(
//                    semGraph.getEdge(parentNode, node));
            Parameter edgeParam = getEdgeParameter(
                    semGraph.getDirectedEdge(parentNode, node));

            if (edgeParam != null) {
                eqn = eqn + " + " + edgeParam.getName() + "*" + parentNode;
            }
        }

        eqn = eqn + " + " + semIm2().getSemPm2().getGraph().getExogenous(node);

        return eqn;
    }

    public GraphWorkbench getWorkbench() {
        return workbench;
    }

    private boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        workbench().setAllowEdgeReorientations(editable);
//        workbench().setAllowMultipleSelection(editable);
//        workbench().setAllowNodeDragging(false);
        workbench().setAllowDoubleClickActions(editable);
        workbench().setAllowNodeEdgeSelection(editable);
        this.editable = editable;
    }

    final static class EdgeMouseListener extends MouseAdapter {
        private Edge edge;
        private Sem2ImGraphicalEditor editor;

        public EdgeMouseListener(Edge edge, Sem2ImGraphicalEditor editor) {
            this.edge = edge;
            this.editor = editor;
        }

        private Edge getEdge() {
            return edge;
        }

        private Sem2ImGraphicalEditor getEditor() {
            return editor;
        }

        public void mouseClicked(MouseEvent e) {
            getEditor().beginEdgeEdit(getEdge());
        }
    }

    final static class NodeMouseListener extends MouseAdapter {
        private Node node;
        private Sem2ImGraphicalEditor editor;

        public NodeMouseListener(Node node, Sem2ImGraphicalEditor editor) {
            this.node = node;
            this.editor = editor;
        }

        private Node getNode() {
            return node;
        }

        private Sem2ImGraphicalEditor getEditor() {
            return editor;
        }

        public void mouseClicked(MouseEvent e) {
            getEditor().beginNodeEdit(getNode());
        }
    }

    final static class EdgeActionListener implements ActionListener {
        private Sem2ImGraphicalEditor editor;
        private Edge edge;

        public EdgeActionListener(Sem2ImGraphicalEditor editor, Edge edge) {
            this.editor = editor;
            this.edge = edge;
        }

        public void actionPerformed(ActionEvent ev) {
            DoubleTextField doubleTextField = (DoubleTextField) ev.getSource();
            String s = doubleTextField.getText();
            getEditor().setEdgeValue(getEdge(), s);
        }

        private Sem2ImGraphicalEditor getEditor() {
            return editor;
        }

        private Edge getEdge() {
            return edge;
        }
    }

    final static class NodeActionListener implements ActionListener {
        private Sem2ImGraphicalEditor editor;
        private Node node;

        public NodeActionListener(Sem2ImGraphicalEditor editor, Node node) {
            this.editor = editor;
            this.node = node;
        }

        public void actionPerformed(ActionEvent ev) {
            DoubleTextField doubleTextField = (DoubleTextField) ev.getSource();
            String s = doubleTextField.getText();
            getEditor().setNodeValue(getNode(), s);
        }

        private Sem2ImGraphicalEditor getEditor() {
            return editor;
        }

        private Node getNode() {
            return node;
        }
    }
}

