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
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.SearchGraphUtils;
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
import java.util.List;

/**
 * Edits some algorithms to search for Markov blanket patterns.
 *
 * @author Joseph Ramsey
 */
public class FciCcdSearchEditor extends AbstractSearchEditor
        implements KnowledgeEditable, LayoutEditable {

    //=========================CONSTRUCTORS============================//

    /**
     * Opens up an editor to let the user view the given PcRunner.
     */
    public FciCcdSearchEditor(PcRunner runner) {
        super(runner, "Result Pattern");
    }

    /**
     * Opens up an editor to let the user view the given FciRunner.
     */
    public FciCcdSearchEditor(FciRunner runner) {
        super(runner, "Result PAG");
    }

    public FciCcdSearchEditor(CfciRunner runner) {
        super(runner, "Result PAG");
    }

    /**
     * Opens up an editor to let the user view the given CcdRunner.
     */
    public FciCcdSearchEditor(CcdRunner runner) {
        super(runner, "Result PAG");
    }

    /**
     * Opens up an editor to let the user view the given GesRunner.
     */
    public FciCcdSearchEditor(GesRunner runner) {
        super(runner, "Result Pattern");
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


    protected JMenu createLoggingMenu() {
        AlgorithmRunner runner = this.getAlgorithmRunner();
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(runner.getClass());
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

        Box b3 = Box.createHorizontalBox();
        JLabel label = new JLabel("<html>" + "*Please note that some" +
                "<br>searches may take a" + "<br>long time to complete." +
                "</html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new TitledBorder(""));
        b3.add(label);

        b1.add(Box.createVerticalStrut(10));
        b1.add(b3);

        toolbar.add(b1);
        return toolbar;
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

        menuBar.add(new KnowledgeMenu(this));
        menuBar.add(new LayoutMenu(this));

//        if (!(getAlgorithmRunner() instanceof GesRunner)) {
//            JMenu test = new JMenu("Test");
//            menuBar.add(test);
//
//            if (getAlgorithmRunner().getDataModel() == null &&
//                    getAlgorithmRunner().getSourceGraph() != null) {
//                addGraphTestMenuItems(test);
//            }
//            else
//            if (getAlgorithmRunner().getDataModel() instanceof RectangularDataSet)
//            {
//                addContinuousTestMenuItems(test);
//            }
//            else
//            if (getAlgorithmRunner().getDataModel() instanceof CovarianceMatrix)
//            {
//                addContinuousTestMenuItems(test);
//            }
//            else
//            if (getAlgorithmRunner().getDataModel() instanceof RectangularDataSet)
//            {
//                addDiscreteTestMenuItems(test);
//            }
//
//            test.addSeparator();
//
//            AlgorithmRunner algorithmRunner = getAlgorithmRunner();
//
//            if (algorithmRunner instanceof IndTestProducer) {
//                IndTestProducer p = (IndTestProducer) algorithmRunner;
//                IndependenceFactsAction action =
//                        new IndependenceFactsAction(this, p);
//                test.add(action);
//            }
//        }
//
//        menuBar.add(new KnowledgeMenu(this));
//        menuBar.add(new LayoutMenu(this));
    }

    public Graph getSourceGraph() {
        Graph sourceGraph = getWorkbench().getGraph();

        if (sourceGraph == null) {
            sourceGraph = getAlgorithmRunner().getSourceGraph();
        }
        return sourceGraph;
    }

    public List getVarNames() {
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
                testType != IndTestType.CORRELATION_T &&
                testType != IndTestType.LINEAR_REGRESSION) {

            AlgorithmRunner algorithmRunner = getAlgorithmRunner();
            if (algorithmRunner instanceof PcdRunner) {
                setTestType(IndTestType.FISHER_ZD);
            } else {
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
        if (getTestType() == IndTestType.FISHER_ZD) {
            fishersZD.setSelected(true);
        } else if (getTestType() == IndTestType.CORRELATION_T) {
            tTest.setSelected(true);
        } else if (getTestType() == IndTestType.LINEAR_REGRESSION) {
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
//        JCheckBoxMenuItem tTest = new JCheckBoxMenuItem(
//                "Conditional Correlation T Test");
//        group.add(tTest);
//        test.add(tTest);
//
//        JCheckBoxMenuItem linRegrTest = new JCheckBoxMenuItem(
//                "Linear Regression Test");
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
            GesRunner gesRunner = ((GesRunner) getAlgorithmRunner());
            GesIndTestParams params = (GesIndTestParams) indTestParams;
            boolean discreteData =
                    gesRunner.getDataModel() instanceof DataSet;
            return new GesIndTestParamsEditor(params, discreteData);
        }

        if (indTestParams instanceof LagIndTestParams) {
            return new TimeSeriesIndTestParamsEditor(
                    (LagIndTestParams) indTestParams);
        }

        if (indTestParams instanceof GraphIndTestParams) {
            return new IndTestParamsEditor((GraphIndTestParams) indTestParams);
        }

        if (indTestParams instanceof PcIndTestParams) {
            return new PcIndTestParamsEditor((PcIndTestParams) indTestParams);
        }

        if (indTestParams instanceof FciIndTestParams) {
            return new FciIndTestParamsEditor((FciIndTestParams) indTestParams);
        }

        return new IndTestParamsEditor(indTestParams);
    }

    protected void doDefaultArrangement(Graph resultGraph) {
        if (getKnowledge().isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(resultGraph,
                    getKnowledge());
        } else {
            boolean arrangedAll = GraphUtils.arrangeBySourceGraph(resultGraph,
                    getLatestWorkbenchGraph());

            if (!arrangedAll) {
                GraphUtils.arrangeInCircle(resultGraph, 200, 200, 150);
            }
        }
    }
}


