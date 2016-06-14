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

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import edu.cmu.tetrad.util.NumberFormatUtil;
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
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Edits some algorithms to search for Markov blanket patterns.
 *
 * @author Joseph Ramsey
 */
public class ImagesSearchEditor extends AbstractSearchEditor
        implements KnowledgeEditable, LayoutEditable {

    private JTabbedPane tabbedPane;
    private double pruningAlpha;

    //=========================CONSTRUCTORS============================//

    public ImagesSearchEditor(LingamRunner runner) {
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
                execute();
            }
        });

        Box b1 = Box.createVerticalBox();
        Box b21 = Box.createVerticalBox();

//        SearchParams params = getAlgorithmRunner().getParams();
//        IndTestParams indTestParams = params.getIndTestParams();
//        JComponent indTestParamBox = getIndTestParamBox(indTestParams);
//        if (indTestParamBox != null) {
//            b21.add(indTestParamBox);
//        }

        Box b211 = Box.createHorizontalBox();
        b211.add(new JLabel("Pruning Alpha "));
        DoubleTextField field = new DoubleTextField(0.05, 8, NumberFormatUtil.getInstance().getNumberFormat());

        field.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                if (value >= 0.0 && value <= 1.0) {
                    setPruningAlpha(value);
                    return value;
                }

                return oldValue;
            }
        });

        b211.add(field);

        b21.add(b211);

        JPanel paramsPanel = new JPanel();
        paramsPanel.add(b21);
        paramsPanel.setBorder(new TitledBorder("Parameters"));
        b1.add(paramsPanel);
        b1.add(Box.createVerticalStrut(10));

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createGlue());
        b2.add(getExecuteButton());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(10));

        if (getAlgorithmRunner().getDataModel() instanceof DataSet) {
            Box b3 = Box.createHorizontalBox();
            b3.add(Box.createGlue());
            b1.add(b3);
        }

        if (getAlgorithmRunner().getParams() instanceof MeekSearchParams) {
            b1.add(Box.createVerticalStrut(5));
            Box hBox = Box.createHorizontalBox();
            hBox.add(Box.createHorizontalGlue());
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


    private void setPruningAlpha(double value) {
        this.pruningAlpha = value;
        Preferences.userRoot().putDouble("lingamPruningAlpha", value);
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