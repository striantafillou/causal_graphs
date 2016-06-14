package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import edu.cmu.tetradapp.model.*;
import edu.cmu.tetradapp.util.LayoutEditable;
import edu.cmu.tetradapp.util.LayoutMenu;
import edu.cmu.tetradapp.util.LoggingMenu;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits some algorithms to search for Markov blanket patterns.
 *
 * @author Joseph Ramsey
 */
public class MbSearchEditor extends AbstractSearchEditor
        implements LayoutEditable, KnowledgeEditable {

    //=========================CONSTRUCTORS============================//

    /**
     * Opens up an editor to let the user view the given PcxRunner.
     */
    public MbSearchEditor(MbfsRunner runner) {
        super(runner, "Result MB Pattern");
    }

    public MbSearchEditor(MbFanSearchRunner runner) {
        super(runner, "Result MB Pattern");
    }

    /**
     * Opens up an editor to let the user view the given PcxRunner.
     */
    public MbSearchEditor(CeFanSearchRunner runner) {
        super(runner, "Result Causal Environment");
    }

    public void setKnowledge(Knowledge knowledge) {
        getAlgorithmRunner().getParams().setKnowledge(knowledge);
    }

    public Knowledge getKnowledge() {
        return getAlgorithmRunner().getParams().getKnowledge();
    }

    public java.util.List<String> getVarNames() {
        SearchParams params = getAlgorithmRunner().getParams();
        return params.getVarNames();
    }

    public Graph getSourceGraph() {
        Graph sourceGraph = getWorkbench().getGraph();

        if (sourceGraph == null) {
            sourceGraph = getAlgorithmRunner().getSourceGraph();
        }
        return sourceGraph;
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


    protected JMenu createLoggingMenu(){
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getAlgorithmRunner().getClass());
        if(config != null){
            return new LoggingMenu(config);
        }
        return null;
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
        b1.add(getParamsPanel());
        b1.add(Box.createVerticalStrut(10));
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createGlue());
        b2.add(getExecuteButton());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(10));

        Box b3 = Box.createHorizontalBox();
        JLabel label = new JLabel("<html>" + "*Please note that some" +
                "<br>searches may take a" + "<br>long time to complete." +
                "</html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new TitledBorder(""));
        b3.add(label);
        b1.add(b3);

        toolbar.add(b1);
        return toolbar;
    }

    protected void addSpecialMenus(JMenuBar menuBar) {
        JMenu test = new JMenu("Independence");
        menuBar.add(test);

        DataModel dataModel = getAlgorithmRunner().getDataModel();

        if (dataModel == null &&
                getAlgorithmRunner().getSourceGraph() != null) {
            addGraphTestMenuItems(test);
        }
        else if (dataModel instanceof DataSet) {
            DataSet _dataSet = (DataSet) dataModel;

            if (_dataSet.isContinuous()) {
                addContinuousTestMenuItems(test);
            }
            else if (_dataSet.isDiscrete()) {
                addDiscreteTestMenuItems(test);
            }
            else {
                throw new IllegalArgumentException(
                        "Don't have any tests for " +
                                "mixed data sets right now.");
            }
        }
        else if (dataModel instanceof CovarianceMatrix) {
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

//        if (dataModel == null &&
//                getAlgorithmRunner().getSourceGraph() != null) {
//            addGraphTestMenuItems(independence);
//        }
//        else
//        if (getAlgorithmRunner().getDataModel() instanceof RectangularDataSet) {
//            addContinuousTestMenuItems(independence);
//        }
//        else
//        if (getAlgorithmRunner().getDataModel() instanceof RectangularDataSet) {
//            addDiscreteTestMenuItems(independence);
//        }
//
//        independence.addSeparator();
//
//        AlgorithmRunner algorithmRunner = getAlgorithmRunner();
//
//        if (algorithmRunner instanceof IndTestProducer) {
//            IndTestProducer p = (IndTestProducer) algorithmRunner;
//            IndependenceFactsAction action =
//                    new IndependenceFactsAction(this, p);
//            independence.add(action);
//        }

        if (getAlgorithmRunner() instanceof MbfsRunner) {
            JMenu graph = new JMenu("Graph");
            JMenuItem showDags = new JMenuItem("Show DAG's Consistent with Pattern");
            JMenuItem meekOrient = new JMenuItem("Meek Orientation");
            JMenuItem gesOrient = new JMenuItem("Global Score-based Reorientation");
            JMenuItem nextGraph = new JMenuItem("Next Graph");
            JMenuItem previousGraph = new JMenuItem("Previous Graph");

            graph.add(new GraphPropertiesAction(getWorkbench()));
            graph.add(new DirectedPathsAction(getWorkbench()));
            graph.add(new TreksAction(getWorkbench()));
            graph.add(new AllPathsAction(getWorkbench()));
            graph.add(new NeighborhoodsAction(getWorkbench()));
            graph.addSeparator();

            graph.add(meekOrient);
            graph.add(gesOrient);
            graph.addSeparator();

            graph.add(previousGraph);
            graph.add(nextGraph);
            graph.addSeparator();

            graph.add(showDags);
            menuBar.add(graph);

            showDags.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AlgorithmRunner runner = getAlgorithmRunner();

                    if (!(runner instanceof MbfsRunner)) {
                        return;
                    }

                    MbfsRunner mbRunner = (MbfsRunner) runner;
                    final Mbfs search = mbRunner.getMbFanSearch();

                    if (search == null) {
                        JOptionPane.showMessageDialog(
                                JOptionUtils.centeringComp(),
                                "The search was not stored.");
                        return;
                    }

                    MbPatternDisplay display = new MbPatternDisplay(search);

                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            display, "MB DAG's Consistent with Pattern",
                            JOptionPane.PLAIN_MESSAGE);
                }
            });

            meekOrient.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ImpliedOrientation rules = getAlgorithmRunner().getMeekRules();
                    rules.setKnowledge(getAlgorithmRunner().getParams().getKnowledge());
                    rules.orientImplied(getGraph());
                    getWorkbench().setGraph(getGraph());
                }
            });

            gesOrient.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DataModel dataModel = getAlgorithmRunner().getDataModel();
                    GesOrienter rules;

                    if (dataModel instanceof DataSet) {
                        rules = new GesOrienter((DataSet) dataModel, new Knowledge());
                    }
                    else {
                        rules = new GesOrienter((CovarianceMatrix) dataModel);
                    }

                    rules.setKnowledge(getAlgorithmRunner().getParams().getKnowledge());
                    rules.orient(getGraph());
                    getWorkbench().setGraph(getGraph());
                }
            });
        }

        if (getAlgorithmRunner().supportsKnowledge()) {
            menuBar.add(new KnowledgeMenu(this));
        }

        menuBar.add(new LayoutMenu(this));
    }

    //================================PRIVATE METHODS====================//

    private JPanel getParamsPanel() {
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
        paramsPanel.add(getSearchParamBox());
        paramsPanel.setBorder(new TitledBorder("Parameters"));
        return paramsPanel;
    }

    private Box getSearchParamBox() {
        if (!(getAlgorithmRunner().getParams() instanceof MbSearchParams)) {
            throw new IllegalStateException();
        }

        Box b = Box.createHorizontalBox();
        MbSearchParams params =
                (MbSearchParams) getAlgorithmRunner().getParams();
        MbSearchParamEditor comp = new MbSearchParamEditor();
        comp.setParams(params);

        comp.setParams(params);
        comp.setup();

        b.add(comp);
        return b;
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
                testType != IndTestType.CORRELATION_T &&
                testType != IndTestType.LINEAR_REGRESSION) {

            AlgorithmRunner algorithmRunner = getAlgorithmRunner();
            if (algorithmRunner instanceof PcdRunner) {
                setTestType(IndTestType.FISHER_ZD);
            }
            else {
                setTestType(IndTestType.FISHER_Z);
            }
        }

        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem fishersZ = new JCheckBoxMenuItem("Fisher's Z");
        group.add(fishersZ);
        test.add(fishersZ);

        JCheckBoxMenuItem fishersZD =
                new JCheckBoxMenuItem("Fisher's Z - Deterministic");
        group.add(fishersZD);
        test.add(fishersZD);

        JCheckBoxMenuItem tTest = new JCheckBoxMenuItem("Cramer's T");
        group.add(tTest);
        test.add(tTest);

        JCheckBoxMenuItem linRegrTest =
                new JCheckBoxMenuItem("Linear Regression Test");
        group.add(linRegrTest);
        test.add(linRegrTest);

        if (getTestType() == IndTestType.FISHER_Z) {
            fishersZ.setSelected(true);
        }
        else if (getTestType() == IndTestType.FISHER_ZD) {
            fishersZD.setSelected(true);
        }
        else if (getTestType() == IndTestType.CORRELATION_T) {
            tTest.setSelected(true);
        }
        else if (getTestType() == IndTestType.LINEAR_REGRESSION) {
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
//                testType != IndTestType.CORRELATION_T &&
//                testType != IndTestType.LINEAR_REGRESSION) {
//            setTestType(IndTestType.FISHER_Z);
//        }
//
//        ButtonGroup group = new ButtonGroup();
//        JCheckBoxMenuItem fishersZ = new JCheckBoxMenuItem("Fisher's Z");
//        group.add(fishersZ);
//        test.add(fishersZ);
//
//        JCheckBoxMenuItem tTest =
//                new JCheckBoxMenuItem("Conditional Correlation T Test");
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
//        }
//        else if (getTestType() == IndTestType.CORRELATION_T) {
//            tTest.setSelected(true);
//        }
//        else if (getTestType() == IndTestType.LINEAR_REGRESSION) {
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
//        tTest.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                setTestType(IndTestType.CORRELATION_T);
//                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                        "Using conditional correlation T test.");
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
        }
        else if (getTestType() == IndTestType.G_SQUARE) {
            gSquare.setSelected(true);
        }

        chiSquare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.CHI_SQUARE);
            }
        });

        gSquare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setTestType(IndTestType.G_SQUARE);
            }
        });
    }

    private void setTestType(IndTestType testType) {
        getAlgorithmRunner().getParams().setIndTestType(testType);
    }

    private IndTestType getTestType() {
        return getAlgorithmRunner().getParams().getIndTestType();
    }

    /**
     * Should always layout out nodes as in source graph. Otherwise, for source
     * graphs with many nodes, it's just impossible to find the nodes of the MB
     * DAG.
     */
    protected void doDefaultArrangement(Graph resultGraph) {
        Graph sourceGraph = getAlgorithmRunner().getSourceGraph();
        boolean arrangedAll =
                GraphUtils.arrangeBySourceGraph(resultGraph, sourceGraph);

        if (!arrangedAll) {
            GraphUtils.arrangeInCircle(resultGraph, 200, 200, 150);
        }
    }
}


