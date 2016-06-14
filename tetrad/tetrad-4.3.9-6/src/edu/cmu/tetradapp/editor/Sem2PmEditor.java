package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.DistributionType;
import edu.cmu.tetrad.sem.ParamType;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemPm2;
import edu.cmu.tetrad.session.DelegatesEditing;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.Sem2PmWrapper;
import edu.cmu.tetradapp.util.DoubleTextField;
import edu.cmu.tetradapp.util.LayoutUtils;
import edu.cmu.tetradapp.util.StringTextField;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.GraphNodeMeasured;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Edits a SEM PM model.
 *
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 */
public final class Sem2PmEditor extends JPanel implements DelegatesEditing {

    /**
     * The SemPm being edited.
     */
    private SemPm2 semPm2;

    /**
     * Tabbed pane for views.
     */
    private JTabbedPane tabbedPane;

    /**
     * The graphical editor for the SemPm.
     */
    private Sem2PmGraphicalEditor graphicalEditor;

    //========================CONSTRUCTORS===========================//

    /**
     * Constructs an editor for the given SemIm.
     */
    public Sem2PmEditor(SemPm2 semPm2) {
        if (semPm2 == null) {
            throw new NullPointerException("SemPm must not be null.");
        }

        this.semPm2 = semPm2;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        tabbedPane.add("Graph", graphicalEditor());
        tabbedPane.add("Error Terms", exogenousTermEditor());

        add(tabbedPane, BorderLayout.CENTER);

//        add(graphicalEditor(), BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(graphicalEditor.getWorkbench(),
                "Save Graph Image..."));

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

        JMenu params = new JMenu("Parameters");
        params.add(errorTerms);
        menuBar.add(params);

        add(menuBar, BorderLayout.NORTH);
    }

    private SemGraph getSemGraph() {
        return semPm2.getGraph();
    }

    /**
     * Constructs a new SemImEditor from the given OldSemEstimateAdapter.
     */
    public Sem2PmEditor(Sem2PmWrapper sem2ImWrapper) {
        this(sem2ImWrapper.getSemPm2());
    }

    public JComponent getEditDelegate() {
        return graphicalEditor();
    }

    //========================PRIVATE METHODS===========================//

    private SemPm2 getSemPm2() {
        return semPm2;
    }

    private Sem2PmGraphicalEditor graphicalEditor() {
        if (this.graphicalEditor == null) {
            this.graphicalEditor = new Sem2PmGraphicalEditor(getSemPm2());
        }
        return this.graphicalEditor;
    }

    private JPanel exogenousTermEditor() {
        return new Sem2ExogenousTermEditor(semPm2);
    }

    static class Sem2PmGraphicalEditor extends JPanel {

        /**
         * Font size for parameter values in the graph.
         */
        private static Font SMALL_FONT = new Font("Dialog", Font.PLAIN, 10);

        /**
         * The SemPm being edited.
         */
        private SemPm2 semPm2;

        /**
         * Workbench for the graphical editor.
         */
        private GraphWorkbench workbench;

        /**
         * This delay needs to be restored when the component is hidden.
         */
        private int savedTooltipDelay;

        /**
         * Constructs a SemPm graphical editor for the given SemIm.
         */
        public Sem2PmGraphicalEditor(SemPm2 semPm2) {
            this.semPm2 = semPm2;
            setLayout(new BorderLayout());
            JScrollPane scroll = new JScrollPane(workbench());
            scroll.setPreferredSize(new Dimension(450, 450));

            add(scroll, BorderLayout.CENTER);
//        setBorder(new TitledBorder(
//                "Double click parameter names to edit parameters"));

            addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    resetLabels();
                    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                    setSavedTooltipDelay(toolTipManager.getInitialDelay());
                    toolTipManager.setInitialDelay(100);
                }

                public void componentHidden(ComponentEvent e) {
                    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                    toolTipManager.setInitialDelay(getSavedTooltipDelay());
                }
            });
        }

        //========================PRIVATE PROTECTED METHODS======================//

        private void beginEdgeEdit(Edge edge) {
            Parameter parameter = getEdgeParameter(edge);
            ParameterEditor paramEditor = new ParameterEditor(parameter, semPm2());

            int ret = JOptionPane.showOptionDialog(workbench(), paramEditor,
                    "Parameter Properties", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (ret == JOptionPane.OK_OPTION) {
                parameter.setName(paramEditor.getParamName());
                parameter.setFixed(paramEditor.isFixed());
                parameter.setInitializedRandomly(
                        paramEditor.isInitializedRandomly());
                parameter.setStartingValue(paramEditor.getStartingValue());
                resetLabels();
            }
        }

        private void beginNodeEdit(Node node) {
            Parameter parameter = getNodeParameter(node);

            if (parameter == null) {
                throw new IllegalStateException(
                        "There is no variance parameter in " + "model for node " +
                                node + ".");
            }

            ParameterEditor paramEditor = new ParameterEditor(parameter, semPm2());

            int ret = JOptionPane.showOptionDialog(workbench(), paramEditor,
                    "Parameter Properties", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (ret == JOptionPane.OK_OPTION) {
                parameter.setName(paramEditor.getParamName());
                parameter.setFixed(paramEditor.isFixed());
                parameter.setInitializedRandomly(
                        paramEditor.isInitializedRandomly());
                parameter.setStartingValue(paramEditor.getStartingValue());
                resetLabels();
            }
        }

        private SemPm2 semPm2() {
            return this.semPm2;
        }

        private SemGraph graph() {
            return semPm2().getGraph();
        }

        private GraphWorkbench workbench() {
            if (this.getWorkbench() == null) {
                this.workbench = new GraphWorkbench(graph());
                this.getWorkbench().setAllowDoubleClickActions(false);
                resetLabels();
                addMouseListenerToGraphNodesMeasured();
            }
            return getWorkbench();
        }

        public void resetLabels() {
            for (Object o : graph().getEdges()) {
                resetEdgeLabel((Edge) (o));
            }

            java.util.List nodes = graph().getNodes();

            for (Object node : nodes) {
                resetNodeLabel((Node) node);
            }

            workbench().repaint();
        }

        private void resetEdgeLabel(Edge edge) {
            Parameter parameter = getEdgeParameter(edge);

            if (parameter != null) {
                JLabel label = new JLabel();

                if (parameter.getType() == ParamType.COVAR) {
                    label.setForeground(Color.red);
                }

                label.setBackground(Color.white);
                label.setOpaque(true);
                label.setFont(SMALL_FONT);
                label.setText(parameter.getName());
                label.addMouseListener(new EdgeMouseListener(edge, this));
                workbench().setEdgeLabel(edge, label);
            } else {
                workbench().setEdgeLabel(edge, null);
            }
        }

        private void resetNodeLabel(Node node) {
            if (node.getNodeType() != NodeType.ERROR) {
                return;
            }

            Node varNode = graph().getVarNode(node);
            DistributionType type = semPm2().getDistributionType(varNode);
            List<Parameter> parameters = semPm2().getDistributionParameters(varNode);

            StringBuffer buf = new StringBuffer();
            buf.append(type.getFunctionSymbol() + "(");

            for (int i = 0; i < parameters.size(); i++) {
                buf.append(parameters.get(i).getName());

                if (i < parameters.size() - 1) {
                    buf.append(",");
                }
            }

            buf.append(")");

            JLabel label = new JLabel();
            label.setForeground(Color.blue);
            label.setBackground(Color.white);
            label.setFont(SMALL_FONT);
            label.setText(buf.toString());
            workbench().setNodeLabel(node, label, 15, 2);
        }

        private Parameter getNodeParameter(Node node) {
            Parameter parameter = semPm2().getMeanParameter(node);

            if (parameter == null) {
                parameter = semPm2().getVarianceParameter(node);
            }
            return parameter;
        }

        /**
         * Returns the parameter for the given edge, or null if the edge does
         * not have a parameter associated with it in the model. The edge must
         * be either directed or bidirected, since it has to come from a
         * SemGraph. For directed edges, this method automatically adjusts if
         * the user has changed the endpoints of an edge X1 --> X2 to X1 <-- X2
         * and returns the correct parameter.
         *
         * @throws IllegalArgumentException if the edge is neither directed nor
         *                                  bidirected.
         */
        public Parameter getEdgeParameter(Edge edge) {
            if (Edges.isDirectedEdge(edge)) {
                return semPm2().getCoefficientParameter(edge.getNode1(), edge.getNode2());
            } else if (Edges.isBidirectedEdge(edge)) {
                return semPm2().getCovarianceParameter(edge.getNode1(), edge.getNode2());
            }

            throw new IllegalArgumentException(
                    "This is not a directed or bidirected edge: " + edge);
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

            SemGraph semGraph = semPm2().getGraph();
            java.util.List parentNodes = semGraph.getParents(node);

            for (Object parentNodeObj : parentNodes) {
                Node parentNode = (Node) parentNodeObj;

//            Parameter edgeParam = semPm2().getEdgeParameter(
//                    semGraph.getEdge(parentNode, node));

                Parameter edgeParam = getEdgeParameter(
                        semGraph.getDirectedEdge(parentNode, node));

                if (edgeParam != null) {
                    eqn = eqn + " + " + edgeParam.getName() + "*" + parentNode;
                }
            }

            eqn = eqn + " + " + semPm2().getGraph().getExogenous(node);

            return eqn;
        }

        private int getSavedTooltipDelay() {
            return savedTooltipDelay;
        }

        private void setSavedTooltipDelay(int savedTooltipDelay) {
            this.savedTooltipDelay = savedTooltipDelay;
        }

        public GraphWorkbench getWorkbench() {
            return workbench;
        }

        //=======================PRIVATE INNER CLASSES==========================//

        private final static class EdgeMouseListener extends MouseAdapter {
            private Edge edge;
            private Sem2PmGraphicalEditor editor;

            public EdgeMouseListener(Edge edge, Sem2PmGraphicalEditor editor) {
                this.edge = edge;
                this.editor = editor;
            }

            private Edge getEdge() {
                return edge;
            }

            private Sem2PmGraphicalEditor getEditor() {
                return editor;
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    getEditor().beginEdgeEdit(getEdge());
                }
            }
        }

        private final static class NodeMouseListener extends MouseAdapter {
            private Node node;
            private Sem2PmGraphicalEditor editor;

            public NodeMouseListener(Node node, Sem2PmGraphicalEditor editor) {
                this.node = node;
                this.editor = editor;
            }

            private Node getNode() {
                return node;
            }

            private Sem2PmGraphicalEditor getEditor() {
                return editor;
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    getEditor().beginNodeEdit(getNode());
                }
            }
        }

        /**
         * Edits the properties of a parameter.
         */
//    private static final class NodeEqnEditor extends JPanel {
//        private Parameter parameter;
//        private double mean;
//        private double rangeFrom;
//        private double rangeTo;
//
//        public NodeEqnEditor(Parameter parameter) {
//            if (parameter == null) {
//                throw new NullPointerException();
//            }
//
//            this.parameter = parameter;
//            setupEditor();
//        }
//
//        private void setupEditor() {
//            int length = 8;
//
//            final DoubleTextField meanField =
//                    new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());
//
//            meanField.setEditable(true);
//
//            final DoubleTextField rangeFromField =
//                    new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());
//
//            rangeFromField.setEditable(true);
//
//            final DoubleTextField rangeToField =
//                    new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());
//
//            rangeToField.setEditable(true);
//
//
//            Box b1 = Box.createHorizontalBox();
//            b1.add(new JLabel("Mean: "));
//            b1.add(Box.createHorizontalGlue());
//            b1.add(meanField);
//
//            Box b2 = Box.createHorizontalBox();
//            b2.add(new JLabel("Range: "));
//            b2.add(Box.createHorizontalGlue());
//            b2.add(rangeFromField);
//            b2.add(new JLabel(" to "));
//            b2.add(rangeToField);
//
//            Box p = Box.createVerticalBox();
//
//            p.add(b1);
//            p.add(b2);
//
//            add(p);
//        }
//
//        public double getMean() {
//            return mean;
//        }
//
//        public void setMean(double mean) {
//            this.mean = mean;
//        }
//
//        public double getRangeFrom() {
//            return rangeFrom;
//        }
//
//        public void setRangeFrom(double rangeFrom) {
//            this.rangeFrom = rangeFrom;
//        }
//
//        public double getRangeTo() {
//            return rangeTo;
//        }
//
//        public void setRangeTo(double rangeTo) {
//            this.rangeTo = rangeTo;
//        }
//
//        public Parameter getParameter() {
//            return parameter;
//        }
//    }

        /**
         * Edits the properties of a parameter.
         */
        private static final class ParameterEditor extends JPanel {

            // Needed to avoid paramName conflicts.
            private SemPm2 semPm2;
            private Parameter parameter;
            private String paramName;
            private boolean fixed;
            private boolean initializedRandomly;
            private double startingValue;

            public ParameterEditor(Parameter parameter, SemPm2 semPm2) {
                if (parameter == null) {
                    throw new NullPointerException();
                }

                if (semPm2 == null) {
                    throw new NullPointerException();
                }

                this.parameter = parameter;
                setParamName(parameter.getName());
                setFixed(parameter.isFixed());
                setInitializedRandomly(parameter.isInitializedRandomly());
                setStartingValue(parameter.getStartingValue());

                this.semPm2 = semPm2;
                setupEditor();
            }

            private void setupEditor() {
                int length = 8;

                StringTextField nameField =
                        new StringTextField(getParamName(), length);
                nameField.setFilter(new StringTextField.Filter() {
                    public String filter(String value, String oldValue) {
                        try {
                            Parameter paramForName =
                                    semPm().getParameter(value);

                            // Ignore if paramName already exists.
                            if (paramForName == null &&
                                    !value.equals(getParamName())) {
                                setParamName(value);
                                firePropertyChange("modelChanged", null,
                                        null);
                            }

                            return getParamName();
                        }
                        catch (IllegalArgumentException e) {
                            return getParamName();
                        }
                        catch (Exception e) {
                            return getParamName();
                        }
                    }
                });

                nameField.setHorizontalAlignment(JTextField.RIGHT);
                nameField.grabFocus();
                nameField.selectAll();

                JCheckBox fixedCheckBox = new JCheckBox();
                fixedCheckBox.setSelected(isFixed());
                fixedCheckBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox checkBox = (JCheckBox) e.getSource();
                        setFixed(checkBox.isSelected());
                    }
                });

                final DoubleTextField startingValueField =
                        new DoubleTextField(getStartingValue(), length,
                                NumberFormatUtil.getInstance().getNumberFormat());

                startingValueField.setEditable(!isInitializedRandomly());

                JRadioButton randomRadioButton = new JRadioButton(
                        "Drawn randomly from " + parameter.getDistribution() + ".");
                randomRadioButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setInitializedRandomly(true);
                        startingValueField.setEditable(false);
                    }
                });

                JRadioButton startRadioButton = new JRadioButton();
                startRadioButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setInitializedRandomly(false);
                        startingValueField.setEditable(true);
                    }
                });

                ButtonGroup buttonGroup = new ButtonGroup();
                buttonGroup.add(randomRadioButton);
                buttonGroup.add(startRadioButton);

                if (isInitializedRandomly()) {
                    buttonGroup.setSelected(randomRadioButton.getModel(), true);
                } else {
                    buttonGroup.setSelected(startRadioButton.getModel(), true);
                }

                final DoubleTextField meanField =
                        new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());

                meanField.setEditable(!isInitializedRandomly());

                final DoubleTextField rangeFromField =
                        new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());

                rangeFromField.setEditable(!isInitializedRandomly());

                final DoubleTextField rangeToField =
                        new DoubleTextField(0, length, NumberFormatUtil.getInstance().getNumberFormat());

                rangeToField.setEditable(!isInitializedRandomly());

                Box b0 = Box.createHorizontalBox();
                b0.add(new JLabel("Parameter Type: "));
                b0.add(Box.createHorizontalGlue());
                b0.add(new JLabel(parameter.getType().toString()));

                Box b1 = Box.createHorizontalBox();
                b1.add(new JLabel("Parameter Name: "));
                b1.add(Box.createHorizontalGlue());
                b1.add(nameField);

                Box b2 = Box.createHorizontalBox();
                b2.add(new JLabel("Fixed for Estimation? "));
                b2.add(Box.createHorizontalGlue());
                b2.add(fixedCheckBox);

                Box b3 = Box.createHorizontalBox();
                b3.add(new JLabel("Starting Value for Estimation:"));
                b3.add(Box.createHorizontalGlue());

                Box b4 = Box.createHorizontalBox();
                b4.add(Box.createHorizontalStrut(10));
                b4.add(randomRadioButton);
                b4.add(Box.createHorizontalGlue());

                Box b5 = Box.createHorizontalBox();
                b5.add(Box.createHorizontalStrut(10));
                b5.add(startRadioButton);
                b5.add(new JLabel("Set to: "));
                b5.add(Box.createHorizontalGlue());
                b5.add(startingValueField);

                Box p = Box.createVerticalBox();

                p.add(b0);
                p.add(b1);
                p.add(b2);
                p.add(b3);
                p.add(b4);
                p.add(b5);

                add(p);
            }

            private SemPm2 semPm() {
                return this.semPm2;
            }

            public String getParamName() {
                return paramName;
            }

            public void setParamName(String name) {
                this.paramName = name;
            }

            public double getStartingValue() {
                return startingValue;
            }

            public void setStartingValue(double startingValue) {
                this.startingValue = startingValue;
            }

            public boolean isFixed() {
                return fixed;
            }

            public void setFixed(boolean fixed) {
                this.fixed = fixed;
            }

            public boolean isInitializedRandomly() {
                return initializedRandomly;
            }

            public void setInitializedRandomly(boolean initializedRandomly) {
                this.initializedRandomly = initializedRandomly;
            }

            public Parameter getParameter() {
                return parameter;
            }
        }
    }

    static class Sem2ExogenousTermEditor extends JPanel {
        private JList variableList;
        private SemPm2 semPm2;

        /**
         * A tabbed pane to store the editors in.
         */
        private JTabbedPane editorPane;

        /**
         * A map from nodes to their editors.
         */
        private Map<Node, DistributionEditor> nodeEditors = new HashMap<Node, DistributionEditor>();


        public Sem2ExogenousTermEditor(SemPm2 semPm2) {
            this.semPm2 = semPm2;

            setup();
        }

        public void setup() {
            variableList = new JList(new VariableListModel(semPm2.getVariableNodes()));
            variableList.setCellRenderer(new VariableBoxRenderer());
            variableList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            variableList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    JList list = (JList) e.getSource();
                    List<Node> selected = getSelected(list);
                    if (selected.size() == 1) {
                        editorPane.removeAll();
                        Node node = selected.get(0);
                        editorPane.add(node.getName(), nodeEditors.get(node));
                    } else if (1 < selected.size()) {
                        editorPane.removeAll();
                        Node first = selected.get(0);
                        Node last = selected.get(selected.size() - 1);
                        String label = first.getName() + " - " + last.getName();
                        editorPane.add(label, new RangeDistributionEditor(selected));
                    }

                }
            });

            for (Node node : semPm2.getVariableNodes()) {
                DistributionEditor editor = new DistributionEditor(node, semPm2);
                nodeEditors.put(node, editor);
            }

//            this.variableList = new JList(new VariableListModel(semPm2.getVariableNodes()));
//            this.variableList.setCellRenderer(new VariableBoxRenderer());

            this.editorPane = new JTabbedPane();

            JScrollPane editorScrollPane = new JScrollPane(this.editorPane);
            editorScrollPane.setPreferredSize(new Dimension(400, 350));

            Box hBox = Box.createHorizontalBox();
            hBox.add(Box.createHorizontalStrut(5));

            Box selectionBox = Box.createVerticalBox();
            JLabel label = new JLabel("Variables");

            JScrollPane variableListPane = new JScrollPane(this.variableList);
//            int width1 = Math.max(100, label.getPreferredSize().width);
//            LayoutUtils.setAllSizes(variableListPane, new Dimension(width1, 350 - label.getPreferredSize().height));

            selectionBox.add(Box.createVerticalStrut(5));
            selectionBox.add(LayoutUtils.leftAlignJLabel(label));
            selectionBox.add(variableListPane);
//            selectionBox.add(Box.createVerticalGlue());

            hBox.add(selectionBox);
            hBox.add(Box.createHorizontalStrut(4));

            Box vBox = Box.createVerticalBox();
            vBox.add(Box.createVerticalBox());
            vBox.add(editorScrollPane);
            hBox.add(vBox);
            hBox.add(Box.createHorizontalBox());

            setLayout(new BorderLayout());
            add(hBox, BorderLayout.CENTER);
        }

        private static List<Node> getSelected(JList list) {
            Object[] selected = list.getSelectedValues();
            List<Node> nodes = new LinkedList<Node>();
            if (selected != null) {
                for (Object o : selected) {
                    nodes.add((Node) o);
                }
            }
            return nodes;
        }
    }

    private static class VariableListModel extends AbstractListModel {

        private Vector<Node> variables;


        public VariableListModel(List<Node> variables) {
            this.variables = new Vector<Node>(variables);
        }


        public int getSize() {
            return this.variables.size();
        }

        public Object getElementAt(int index) {
            return this.variables.get(index);
        }

        public void removeAll(List<Node> nodes) {
            int size = getSize();
            this.variables.removeAll(nodes);
            this.fireIntervalRemoved(this, 0, size - 1);
        }

        public void add(Node node) {
            if (!this.variables.contains(node)) {
                this.variables.add(node);
                this.fireIntervalAdded(this, getSize() - 2, getSize() - 1);
            }
        }


        public void addAll(List<Node> nodes) {
            for (Node node : nodes) {
                if (!this.variables.contains(node)) {
                    this.variables.add(node);
                }
            }
            this.fireIntervalAdded(this, getSize() - nodes.size(), nodes.size() - 1);
        }

    }

    private static class VariableBoxRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Node node = (Node) value;
            if (node == null) {
                this.setText("");
            } else {
                this.setText(node.getName());
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    private static class DistributionEditor extends JPanel {

        public DistributionEditor(final Node node, final SemPm2 semPm2) {
            setLayout(new BorderLayout());

             Box b1 = Box.createVerticalBox();

             Box b2 = Box.createHorizontalBox();
             JLabel label = new JLabel("Choose a parametric distribution for " + node);
             label.setFont(new Font("Dialog", Font.BOLD, 12));
             b2.add(label);
             b2.add(Box.createHorizontalGlue());
             b1.add(b2);

             Box b3 = Box.createHorizontalBox();
             JComboBox comboBox = new JComboBox(new String[]{"Normal", "Uniform"});

             comboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     JComboBox combo = (JComboBox) e.getSource();
                     String selected = (String) combo.getSelectedItem();

                     if ("Normal".equals(selected)) {
                         semPm2.setDistributionType(node, DistributionType.NORMAL);
                     }
                     else if ("Uniform".equals(selected)) {
                         semPm2.setDistributionType(node, DistributionType.UNIFORM);
                     }
                 }
             });

             comboBox.setMaximumSize(new Dimension(200, 50));
             b3.add(Box.createHorizontalGlue());
             b3.add(comboBox);
             b1.add(b3);

             b1.add(Box.createVerticalGlue());

             add(b1, BorderLayout.CENTER);
         }

    }

    private static class RangeDistributionEditor extends JPanel {

        public RangeDistributionEditor(List<Node> nodes) {
            setLayout(new BorderLayout());

            Box b1 = Box.createVerticalBox();

            Box b2 = Box.createHorizontalBox();
            JLabel label = new JLabel("Choose a parametric distribution for " + nodes);
            label.setFont(new Font("Dialog", Font.BOLD, 12));
            b2.add(label);
            b2.add(Box.createHorizontalGlue());
            b1.add(b2);

            Box b3 = Box.createHorizontalBox();
            JComboBox comboBox = new JComboBox(new String[]{"Normal", "Uniform"});
            comboBox.setMaximumSize(new Dimension(200, 50));
            b3.add(Box.createHorizontalGlue());
            b3.add(comboBox);
            b1.add(b3);

            b1.add(Box.createVerticalGlue());

            add(b1, BorderLayout.CENTER);
        }
    }
}
