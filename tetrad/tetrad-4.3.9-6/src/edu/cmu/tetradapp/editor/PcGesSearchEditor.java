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

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.BayesProperties;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import edu.cmu.tetradapp.model.*;
import edu.cmu.tetradapp.util.*;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edits some algorithms to search for Markov blanket patterns.
 *
 * @author Joseph Ramsey
 */
public class PcGesSearchEditor extends AbstractSearchEditor
        implements KnowledgeEditable, LayoutEditable {

    private JTextArea modelStatsText;
    private JTabbedPane tabbedPane;

    //=========================CONSTRUCTORS============================//

    /**
     * Opens up an editor to let the user view the given PcRunner.
     */
    public PcGesSearchEditor(PcRunner runner) {
        super(runner, "Result Pattern");
    }

    public PcGesSearchEditor(PcPatternRunner runner) {
        super(runner, "Result Pattern");
    }

    public PcGesSearchEditor(AcpcRunner runner) {
        super(runner, "Result Pattern");
    }

    public PcGesSearchEditor(CpcRunner runner) {
        super(runner, "Result Pattern");
    }

    public PcGesSearchEditor(MbfsPatternRunner runner) {
        super(runner, "Result Pattern");
    }

    /**
     * Opens up an editor to let the user view the given PcRunner.
     */
    public PcGesSearchEditor(PcdRunner runner) {
        super(runner, "Result Pattern");
    }

    /**
     * Opens up an editor to let the user view the given GesRunner.
     */
    public PcGesSearchEditor(GesRunner runner) {
        super(runner, "Result Pattern");

//        runner.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//
//                if ("graph".equals(evt.getPropertyName())) {
//                    Graph graph = (Graph) evt.getNewValue();
//                    getWorkbench().setGraph(graph);
//                }
//            }
//        });
    }

    public PcGesSearchEditor(PValueImproverWrapper runner) {
        super(runner, "Result Graph");

        runner.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                if ("graph".equals(evt.getPropertyName())) {
                    Graph graph = (Graph) evt.getNewValue();
                    getWorkbench().setGraph(graph);
                }
            }
        });
    }

//    public PcGesSearchEditor(MmhcRunner runner) {
//        super(runner, "Result Pattern");
//    }

    public PcGesSearchEditor(LingamPatternRunner runner) {
        super(runner, "Result Graph");
    }

    public PcGesSearchEditor(LingamStructureRunner runner) {
        super(runner, "Result Graph");
    }

    //=============================== Public Methods ==================================//

    public Graph getGraph() {
        return getWorkbench().getGraph();
    }

    public void layoutByGraph(Graph graph) {
        getWorkbench().layoutByGraph(graph);
    }

    public void layoutByKnowledge() {
        GraphWorkbench resultWorkbench = getWorkbench();
        Graph graph = resultWorkbench.getGraph();
        Knowledge knowledge = getAlgorithmRunner().getParams().getKnowledge();
        SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
        resultWorkbench.setGraph(graph);
    }

    public Rectangle getVisibleRect() {
        return getWorkbench().getVisibleRect();
    }

    //==========================PROTECTED METHODS============================//


    /**
     * Creates the logging menu based on what runner is being used.
     *
     * @return
     */
    protected JMenu createLoggingMenu() {
        AlgorithmRunner runner = this.getAlgorithmRunner();
        TetradLoggerConfig configForModel = TetradLogger.getInstance().getTetradLoggerConfigForModel(runner.getClass());
        if (configForModel != null) {
            return new LoggingMenu(configForModel, this);
        }
        return null;
    }


    /**
     * Sets up the editor, does the layout, and so on.
     */
    protected void setup(String resultLabel) {
        setLayout(new BorderLayout());
        add(getToolbar(), BorderLayout.WEST);
        //JTabbedPane tabbedPane = new JTabbedPane();
        modelStatsText = new JTextArea();
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Result", workbenchScroll(resultLabel));

        /*if (getAlgorithmRunner().getDataModel() instanceof DataSet) {
            tabbedPane.add("Model Statistics", modelStatsText);
            tabbedPane.add("DAG in pattern", dagWorkbench);
        }*/

        add(tabbedPane, BorderLayout.CENTER);
        add(menuBar(), BorderLayout.NORTH);
    }

    /**
     * Construct the toolbar panel.
     */
    protected JPanel getToolbar() {
        JPanel toolbar = new JPanel();

        getExecuteButton().setText("Execute*");
        getExecuteButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeStatsTabs();
                execute();
            }
        });

        JButton statsButton = new JButton("Calc Stats");
        statsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window owner = (Window) getTopLevelAncestor();

                new WatchedProcess(owner) {
                    public void watch() {
                        calcStats();
                    }
                };
            }
        });


        Box b1 = Box.createVerticalBox();
        b1.add(getParamsPanel());
        b1.add(Box.createVerticalStrut(10));

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createGlue());
        b2.add(getExecuteButton());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(10));

        if (getAlgorithmRunner().getDataModel() instanceof DataSet) {
            Box b3 = Box.createHorizontalBox();
            b3.add(Box.createGlue());
            b3.add(statsButton);
            b1.add(b3);
        }

        if (getAlgorithmRunner().getParams() instanceof MeekSearchParams) {
            MeekSearchParams params = (MeekSearchParams) getAlgorithmRunner().getParams();
            JCheckBox preventCycles = new JCheckBox("Aggressively Prevent Cycles");
            preventCycles.setHorizontalTextPosition(AbstractButton.RIGHT);
            preventCycles.setSelected(params.isAggressivelyPreventCycles());

            preventCycles.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox box = (JCheckBox) e.getSource();
                    MeekSearchParams params = (MeekSearchParams) getAlgorithmRunner().getParams();
                    params.setAggressivelyPreventCycles(box.isSelected());
                }
            });

            b1.add(Box.createVerticalStrut(5));
            Box hBox = Box.createHorizontalBox();
            hBox.add(Box.createHorizontalGlue());
            hBox.add(preventCycles);
            b1.add(hBox);
            b1.add(Box.createVerticalStrut(5));
        }

        Box b4 = Box.createHorizontalBox();
        JLabel label = new JLabel("<html>" + "*Please note that some" +
                "<br>searches may take a" + "<br>long time to complete." +
                "</html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new TitledBorder(""));
        b4.add(label);

        b1.add(Box.createVerticalStrut(10));
        b1.add(b4);

        toolbar.add(b1);
        return toolbar;
    }

    protected void doPostExecutionSteps() {
//        calcStats();
        System.out.println("Post execution.");

//        getWorkbench().addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//                System.out.println(evt.getPropertyName());
//            }
//        });
    }


    private void calcStats() {
        Graph resultGraph = getAlgorithmRunner().getResultGraph();

        if (getAlgorithmRunner().getDataModel() instanceof DataSet) {

            //resultGraph may be the output of a PC search.
            //Such graphs sometimes contain doubly directed edges.
            //We converte such edges to directed edges here.
            //For the time being an orientation is arbitrarily selected.
            List<Edge> allEdges = resultGraph.getEdges();

            for (Edge edge : allEdges) {
                if (edge.getEndpoint1() == Endpoint.ARROW &&
                        edge.getEndpoint2() == Endpoint.ARROW) {
                    //Option 1 orient it from node1 to node2
                    resultGraph.setEndpoint(edge.getNode1(),
                            edge.getNode2(), Endpoint.ARROW);

                    //Option 2 remove such edges:
                    resultGraph.removeEdge(edge);
                }
            }

            Pattern pattern = new Pattern(resultGraph);
            PatternToDag ptd = new PatternToDag(pattern);
            Graph dag = ptd.patternToDagMeekRules();

            DataSet dataSet =
                    (DataSet) getAlgorithmRunner().getDataModel();
            String report;

            if (dataSet.isContinuous()) {
                report = reportIfContinuous(dag, dataSet);
            } else if (dataSet.isDiscrete()) {
                report = reportIfDiscrete(dag, dataSet);
            } else {
                throw new IllegalArgumentException("");
            }

            JScrollPane dagWorkbenchScroll = dagWorkbenchScroll("Dag", dag);
            modelStatsText.setLineWrap(true);
            modelStatsText.setWrapStyleWord(true);
            modelStatsText.setText(report);

            removeStatsTabs();
            tabbedPane.addTab("DAG in pattern", dagWorkbenchScroll);
            tabbedPane.addTab("DAG Model Statistics", modelStatsText);
        }
    }

    private String reportIfContinuous(Graph dag, DataSet dataSet) {
        SemPm semPm = new SemPm(dag);

        SemEstimator estimator = new SemEstimator(dataSet, semPm);
        estimator.estimate();
        SemIm semIm = estimator.getEstimatedSem();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);

        StringBuffer buf = new StringBuffer();
        buf.append("\nDegrees of Freedom = ").append(semPm.getDof())
                .append("Chi-Square = ").append(nf.format(semIm.getChiSquare()))
                .append("\nP Value = ").append(nf.format(semIm.getPValue()))
                .append("\nBIC Score = ").append(nf.format(semIm.getBicScore()));

        buf.append("\n\nThe above chi square test assumes that the maximum " +
                "likelihood function over the measured variables has been " +
                "maximized. Under that assumption, the null hypothesis for " +
                "the test is that the population covariance matrix over all " +
                "of the measured variables is equal to the estimated covariance " +
                "matrix over all of the measured variables written as a function " +
                "of the free model parameters--that is, the unfixed parameters " +
                "for each directed edge (the linear coefficient for that edge), " +
                "each exogenous variable (the variance for the error term for " +
                "that variable), and each bidirected edge (the covariance for " +
                "the exogenous variables it connects).  The model is explained " +
                "in Bollen, Structural Equations with Latent Variable, 110. ");

        return buf.toString();
    }

    private String reportIfDiscrete(Graph dag, DataSet dataSet) {
        List vars = dataSet.getVariables();
        Map<String, DiscreteVariable> nodesToVars =
                new HashMap<String, DiscreteVariable>();
        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            DiscreteVariable var = (DiscreteVariable) vars.get(i);
            String name = var.getName();
            Node node = new GraphNode(name);
            nodesToVars.put(node.getName(), var);
        }

        BayesPm bayesPm = new BayesPm(new Dag(dag));
        List<Node> nodes = bayesPm.getDag().getNodes();

        for (Node node : nodes) {
            Node var = nodesToVars.get(node.getName());

            if (var instanceof DiscreteVariable) {
                DiscreteVariable var2 = nodesToVars.get(node.getName());
                int numCategories = var2.getNumCategories();
                List<String> categories = new ArrayList<String>();
                for (int j = 0; j < numCategories; j++) {
                    categories.add(var2.getCategory(j));
                }
                bayesPm.setCategories(node, categories);
            }
        }


        BayesProperties properties = new BayesProperties(dataSet, dag);
        properties.setGraph(dag);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);

        StringBuffer buf = new StringBuffer();
        buf.append("\nP-value = ").append(properties.getPValue());
        buf.append("\nDf = ").append(properties.getPValueDf());
        buf.append("\nChi square = ")
                .append(nf.format(properties.getPValueChisq()));
        buf.append("\nBIC score = ").append(nf.format(properties.getBic()));
        buf.append("\n\nH0: Completely disconnected graph.");

        return buf.toString();
    }

    private void removeStatsTabs() {
        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
            String name = tabbedPane.getTitleAt(i);

            if (name.equals("Model Statistics")) {
                tabbedPane.removeTabAt(i);
            } else if (name.equals("DAG in pattern")) {
                tabbedPane.removeTabAt(i);
            }
        }
    }

    protected void addSpecialMenus(JMenuBar menuBar) {
        if (!(getAlgorithmRunner() instanceof GesRunner)) {
            JMenu test = new JMenu("Independence");
            menuBar.add(test);

            DataModel dataModel = getAlgorithmRunner().getDataModel();

            if (dataModel == null &&
                    getAlgorithmRunner().getSourceGraph() != null) {
                addGraphTestMenuItems(test);
            } else if (dataModel instanceof DataSet) {
                DataSet _dataSet = (DataSet) dataModel;

                if (_dataSet.isContinuous()) {
                    addContinuousTestMenuItems(test);
                } else if (_dataSet.isDiscrete()) {
                    addDiscreteTestMenuItems(test);
                } else {
                    throw new IllegalArgumentException(
                            "Don't have any tests for " +
                                    "mixed data sets right now.");
                }
            } else if (dataModel instanceof CovarianceMatrix) {
                addContinuousTestMenuItems(test);
            }

            test.addSeparator();

            AlgorithmRunner algorithmRunner = getAlgorithmRunner();

            if (algorithmRunner instanceof IndTestProducer) {
                IndTestProducer p = (IndTestProducer) algorithmRunner;
                IndependenceFactsAction action =
                        new IndependenceFactsAction(this, p, "Independence Facts...");
                test.add(action);
            }
        }

        JMenu graph = new JMenu("Graph");
        JMenuItem showDags = new JMenuItem("Show DAGs in Pattern");
        JMenuItem meekOrient = new JMenuItem("Meek Orientation");
        JMenuItem dagInPattern = new JMenuItem("Choose DAG in Pattern");
        JMenuItem gesOrient = new JMenuItem("Global Score-based Reorientation");
        JMenuItem nextGraph = new JMenuItem("Next Graph");
        JMenuItem previousGraph = new JMenuItem("Previous Graph");

//        graph.add(new LayoutMenu(this));
        graph.add(new GraphPropertiesAction(getWorkbench()));
        graph.add(new DirectedPathsAction(getWorkbench()));
        graph.add(new TreksAction(getWorkbench()));
        graph.add(new AllPathsAction(getWorkbench()));
        graph.add(new NeighborhoodsAction(getWorkbench()));
        graph.addSeparator();

        graph.add(meekOrient);
        graph.add(dagInPattern);
        graph.add(gesOrient);
        graph.addSeparator();

        graph.add(previousGraph);
        graph.add(nextGraph);
        graph.addSeparator();

        graph.add(showDags);

        graph.addSeparator();
        graph.add(new JMenuItem(new SelectBidirectedAction(getWorkbench())));
        graph.add(new JMenuItem(new SelectUndirectedAction(getWorkbench())));

        menuBar.add(graph);

        showDags.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window owner = (Window) getTopLevelAncestor();

                new WatchedProcess(owner) {
                    public void watch() {

                        // Needs to be a pattern search; this isn't checked
                        // before running the algorithm because of allowable
                        // "slop"--e.g. bidirected edges.
                        AlgorithmRunner runner = getAlgorithmRunner();
                        Graph graph = runner.getResultGraph();

                        if (graph == null) {
                            JOptionPane.showMessageDialog(
                                    JOptionUtils.centeringComp(),
                                    "No result gaph.");
                            return;
                        }

                        PatternDisplay display = new PatternDisplay(graph);

                        EditorWindow editorWindow =
                                new EditorWindow(display, "Independence Facts",
                                        "Close", false);
                        DesktopController.getInstance().addEditorWindow(editorWindow);
                        editorWindow.setVisible(true);
                    }
                };
            }
        });

        meekOrient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImpliedOrientation rules = getAlgorithmRunner().getMeekRules();
                rules.setKnowledge(getAlgorithmRunner().getParams().getKnowledge());
                rules.orientImplied(getGraph());
                getGraphHistory().add(getGraph());
                getWorkbench().setGraph(getGraph());
            }
        });

        dagInPattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph graph = new EdgeListGraph(getGraph());

                // Removing bidirected edges from the pattern before selecting a DAG.                                   4
                for (Edge edge : graph.getEdges()) {
                    if (Edges.isBidirectedEdge(edge)) {
                        graph.removeEdge(edge);
                    }
                }

                PatternToDag search = new PatternToDag(new Pattern(graph));
                Graph dag = search.patternToDagMeekRules();

                getGraphHistory().add(dag);
                getWorkbench().setGraph(dag);

                ((AbstractAlgorithmRunner) getAlgorithmRunner()).setResultGraph(dag);
            }
        });

        gesOrient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                DataModel dataModel = getAlgorithmRunner().getDataModel();
                GesOrienter rules;

                if (dataModel instanceof DataSet) {
                    rules = new GesOrienter((DataSet) dataModel, new Knowledge());
                } else {
                    rules = new GesOrienter((CovarianceMatrix) dataModel);
                }

                rules.setKnowledge(getAlgorithmRunner().getParams().getKnowledge());
                rules.orient(getGraph());
                getGraphHistory().add(getGraph());
                getWorkbench().setGraph(getGraph());
            }
        });

        nextGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph next = getGraphHistory().next();
                getWorkbench().setGraph(next);
                ((AbstractAlgorithmRunner) getAlgorithmRunner()).setResultGraph(next);
            }
        });

        previousGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph previous = getGraphHistory().previous();
                getWorkbench().setGraph(previous);
                ((AbstractAlgorithmRunner) getAlgorithmRunner()).setResultGraph(previous);
            }
        });

        if (getAlgorithmRunner().supportsKnowledge()) {
            menuBar.add(new KnowledgeMenu(this));
        }

        menuBar.add(new LayoutMenu(this));
    }

    public Graph getSourceGraph() {
        Graph sourceGraph = getWorkbench().getGraph();

        if (sourceGraph == null) {
            sourceGraph = getAlgorithmRunner().getSourceGraph();
        }
        return sourceGraph;
    }

    public List<String> getVarNames() {
        SearchParams params = getAlgorithmRunner().getParams();
        return params.getVarNames();
    }

    private void addGraphTestMenuItems(JMenu test) {
        IndTestType testType = getTestType();
        if (testType != IndTestType.D_SEPARATION) {
            setTestType(IndTestType.D_SEPARATION);
        }

        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem dsep = new JCheckBoxMenuItem("D-Separation");
        group.add(dsep);
        test.add(dsep);
        dsep.setSelected(true);

        dsep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.D_SEPARATION);
            }
        });
    }

    private void addContinuousTestMenuItems(JMenu test) {
        IndTestType testType = getTestType();
        if (testType != IndTestType.FISHER_Z &&
                testType != IndTestType.FISHER_ZD &&
                testType != IndTestType.FISHER_Z_BOOTSTRAP &&
                testType != IndTestType.CORRELATION_T &&
                testType != IndTestType.LINEAR_REGRESSION) {
            setTestType(IndTestType.FISHER_Z);
        }

        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem fishersZ = new JCheckBoxMenuItem("Fisher's Z");
        group.add(fishersZ);
        test.add(fishersZ);

        JCheckBoxMenuItem fishersZD =
                new JCheckBoxMenuItem("Fisher's Z - Deterministic");
        group.add(fishersZD);
        test.add(fishersZD);

        JCheckBoxMenuItem fishersZBootstrap =
                new JCheckBoxMenuItem("Fisher's Z - Bootstrap");
        group.add(fishersZBootstrap);
        test.add(fishersZBootstrap);

        JCheckBoxMenuItem tTest = new JCheckBoxMenuItem("Cramer's T");
        group.add(tTest);
        test.add(tTest);

        JCheckBoxMenuItem linRegrTest =
                new JCheckBoxMenuItem("Linear Regression Test");
        group.add(linRegrTest);
        test.add(linRegrTest);

        testType = getTestType();
        if (testType == IndTestType.FISHER_Z) {
            fishersZ.setSelected(true);
        } else if (testType == IndTestType.FISHER_ZD) {
            fishersZD.setSelected(true);
        } else if (testType == IndTestType.FISHER_Z_BOOTSTRAP) {
            fishersZBootstrap.setSelected(true);
        } else if (testType == IndTestType.CORRELATION_T) {
            tTest.setSelected(true);
        } else if (testType == IndTestType.LINEAR_REGRESSION) {
            linRegrTest.setSelected(true);
        }

        fishersZ.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.FISHER_Z);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using Fisher's Z.");
            }
        });

        fishersZD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.FISHER_ZD);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using Fisher's Z - Deterministic.");
            }
        });

        fishersZBootstrap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.FISHER_Z_BOOTSTRAP);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using Fisher's Z - Bootstrap.");
            }
        });

        tTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.CORRELATION_T);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using Cramer's T.");
            }
        });

        linRegrTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.LINEAR_REGRESSION);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using linear regression test.");
            }
        });
    }


//    private void addContinuousTestMenuItems(JMenu test) {
//        IndTestType testType = getTestType();
//        if (testType != IndTestType.FISHER_Z &&
//                testType != IndTestType.FISHER_ZD &&
//                testType != IndTestType.CORRELATION_T &&
//                testType != IndTestType.LINEAR_REGRESSION) {
//
//            AlgorithmRunner algorithmRunner = getAlgorithmRunner();
//            if (algorithmRunner instanceof PcdRunner) {
//                setTestType(IndTestType.FISHER_ZD);
//            } else {
//                setTestType(IndTestType.FISHER_Z);
//            }
//        }
//
//        ButtonGroup group = new ButtonGroup();
//        JCheckBoxMenuItem fishersZ = new JCheckBoxMenuItem("Fisher's Z");
//        group.add(fishersZ);
//        test.add(fishersZ);
//
//        JCheckBoxMenuItem fishersZD =
//                new JCheckBoxMenuItem("Fisher's Z - Deterministic");
//        group.add(fishersZD);
//        test.add(fishersZD);
//
//        JCheckBoxMenuItem tTest = new JCheckBoxMenuItem("Cramer's T");
//        group.add(tTest);
//        test.add(tTest);
//
//        JCheckBoxMenuItem linRegrTest =
//                new JCheckBoxMenuItem("Linear Regression Test");
//        group.add(linRegrTest);
//        test.add(linRegrTest);
//
//        if (getTestType() == IndTestType.FISHER_Z) {
//            fishersZ.setSelected(true);
//        } else if (getTestType() == IndTestType.FISHER_ZD) {
//            fishersZD.setSelected(true);
//        } else if (getTestType() == IndTestType.CORRELATION_T) {
//            tTest.setSelected(true);
//        } else if (getTestType() == IndTestType.LINEAR_REGRESSION) {
//            linRegrTest.setSelected(true);
//        }
//
//        fishersZ.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                setTestType(IndTestType.FISHER_Z);
//                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                        "Using Fisher's Z.");
//            }
//        });
//
//        fishersZD.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                setTestType(IndTestType.FISHER_ZD);
//                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                        "Using Fisher's Z - Deterministic.");
//            }
//        });
//
//        tTest.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                setTestType(IndTestType.CORRELATION_T);
//                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                        "Using Cramer's T.");
//            }
//        });
//
//        linRegrTest.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                setTestType(IndTestType.LINEAR_REGRESSION);
//                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                        "Using linear regression test.");
//            }
//        });
//    }

    private void addDiscreteTestMenuItems(JMenu test) {
        IndTestType testType = getTestType();
        if (testType != IndTestType.CHI_SQUARE &&
                testType != IndTestType.G_SQUARE) {
            setTestType(IndTestType.CHI_SQUARE);
        }

        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem chiSquare = new JCheckBoxMenuItem("Chi Square");
        group.add(chiSquare);
        test.add(chiSquare);

        JCheckBoxMenuItem gSquare = new JCheckBoxMenuItem("G Square");
        group.add(gSquare);
        test.add(gSquare);

        if (getTestType() == IndTestType.CHI_SQUARE) {
            chiSquare.setSelected(true);
        } else if (getTestType() == IndTestType.G_SQUARE) {
            gSquare.setSelected(true);
        }

        chiSquare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.CHI_SQUARE);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using Chi Square.");
            }
        });

        gSquare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.G_SQUARE);
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Using G square.");
            }
        });
    }

    private void setTestType(IndTestType testType) {
        getAlgorithmRunner().getParams().setIndTestType(testType);
    }

    private IndTestType getTestType() {
        return getAlgorithmRunner().getParams().getIndTestType();
    }

    public void setKnowledge(Knowledge knowledge) {
        getAlgorithmRunner().getParams().setKnowledge(knowledge);
    }

    public Knowledge getKnowledge() {
        return getAlgorithmRunner().getParams().getKnowledge();
    }

    //================================PRIVATE METHODS====================//

    private JPanel getParamsPanel() {
        JPanel paramsPanel = new JPanel();

        Box b2 = Box.createVerticalBox();

        JComponent indTestParamBox = getIndTestParamBox();
        if (indTestParamBox != null) {
            b2.add(indTestParamBox);
        }

        paramsPanel.add(b2);
        paramsPanel.setBorder(new TitledBorder("Parameters"));
        return paramsPanel;
    }

    private JComponent getIndTestParamBox() {
        SearchParams params = getAlgorithmRunner().getParams();
        IndTestParams indTestParams = params.getIndTestParams();
        return getIndTestParamBox(indTestParams);
    }

    /**
     * Factory to return the correct param editor for independence test params.
     * This will go in a little box in the search editor.
     */
    private JComponent getIndTestParamBox(IndTestParams indTestParams) {
        if (indTestParams == null) {
            throw new NullPointerException();
        }

        if (indTestParams instanceof GesIndTestParams) {
            if (getAlgorithmRunner() instanceof  GesRunner) {
                GesRunner gesRunner = ((GesRunner) getAlgorithmRunner());
                GesIndTestParams params = (GesIndTestParams) indTestParams;
                boolean discreteData =
                        gesRunner.getDataModel() instanceof DataSet;
                return new GesIndTestParamsEditor(params, discreteData);
            }
//            else if (getAlgorithmRunner() instanceof PValueImproverWrapper) {
//                PValueImproverWrapper runner = ((PValueImproverWrapper) getAlgorithmRunner());
//                GesIndTestParams params = (GesIndTestParams) indTestParams;
//                boolean discreteData =
//                        runner.getDataModel() instanceof RectangularDataSet;
//                return new GesIndTestParamsEditor(params, discreteData);
//            }
        }

        if (indTestParams instanceof LagIndTestParams) {
            return new TimeSeriesIndTestParamsEditor(
                    (LagIndTestParams) indTestParams);
        }

        if (indTestParams instanceof GraphIndTestParams) {
            return new IndTestParamsEditor((GraphIndTestParams) indTestParams);
        }

        if (indTestParams instanceof DiscDetIndepParams) {
            return new DiscDetIndepParamsEditor(
                    (DiscDetIndepParams) indTestParams);
        }

        if (indTestParams instanceof PcIndTestParams) {
            return new PcIndTestParamsEditor((PcIndTestParams) indTestParams);
        }

        return new IndTestParamsEditor(indTestParams);
    }

    protected void doDefaultArrangement(Graph resultGraph) {
        if (getKnowledge().isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(resultGraph,
                    getKnowledge());
        } else {
            GraphUtils.arrangeBySourceGraph(resultGraph,
                    getLatestWorkbenchGraph());
        }
    }

    private JScrollPane dagWorkbenchScroll(String resultLabel, Graph dag) {

        GraphWorkbench dagWorkbench = new GraphWorkbench(dag);
        dagWorkbench.setAllowDoubleClickActions(false);
        dagWorkbench.setAllowNodeEdgeSelection(true);
        JScrollPane dagWorkbenchScroll = new JScrollPane(dagWorkbench);
        dagWorkbenchScroll.setPreferredSize(new Dimension(450, 450));
        dagWorkbenchScroll.setBorder(new TitledBorder(resultLabel));

        dagWorkbench.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                storeLatestWorkbenchGraph();
            }
        });

        return dagWorkbenchScroll;
    }

}

