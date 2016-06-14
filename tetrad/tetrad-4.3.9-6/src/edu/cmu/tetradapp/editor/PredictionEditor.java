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

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.predict.ManipulatedVariable;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.PredictionRunner;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Allows the user to specify information to run the Prediction algorithm.
 *
 * @author Erin Korber
 * @author Joseph Ramsey
 */
public final class PredictionEditor extends JPanel {

    /**
     * The wrapper that runs the prediction algorithm.
     */
    private final PredictionRunner predictionRunner;

    /**
     * Map from nodes to NodeInfo objects. This map is edited by clicking
     * "Change" buttons and modifying the settings in the panels that pop up.
     */
    private final Map<Node, NodeInfo> input;

    /**
     * Displays the information about the current status of variables. After the
     * prediction algorithm is run predicted values are noted in this panel.
     */
    private final JPanel layoutPanel;

    /**
     * Formats all numbers.
     */
    private final NumberFormat nf;

    //===============================CONSTRUCTORS========================//

    /**
     * Constructs an editor for the given prediction runner.
     */
    public PredictionEditor(PredictionRunner predictionRunner) {
        if (predictionRunner == null) {
            throw new NullPointerException();
        }

        this.predictionRunner = predictionRunner;

        setLayout(new BorderLayout());

        JPanel layoutPanel = new JPanel();
        add(layoutPanel, BorderLayout.CENTER);
        layoutPanel.setLayout(new BorderLayout());
        this.layoutPanel = layoutPanel;
        this.input = new HashMap<Node, NodeInfo>();
        this.nf = NumberFormatUtil.getInstance().getNumberFormat();

        resetLayoutPanel();
    }

    //==============================PRIVATE METHODS======================//

    /**
     * Content of user input.
     */
    private Map<Node, NodeInfo> input() {
        return input;
    }

    /**
     * Returns the prediction runner passed at construction.
     */
    private PredictionRunner predictionRunner() {
        return getPredictionRunner();
    }

    /**
     *
     */
    private void resetLayoutPanel() {
        Box b = Box.createVerticalBox();
        setBorder(new MatteBorder(10, 10, 10, 10, getBackground()));
        JButton predictButton = new JButton("Do Prediction Now");

        // Do layout.
        Box b0 = Box.createHorizontalBox();
        b0.add(new JLabel("<html>" +
                "Click the 'change' button next to a variable to set it to manipulated, " +
                "<br>predicted, or conditioned on.  When done, click on the " +
                "<br>Do Prediction Now button to execute the prediction." +
                "</html>"));

        b.add(b0);

        List<Node> nodes = predictionRunner().getPag().getNodes();

        for (Object node : nodes) {
            final Node currentNode = (Node) node;
            Box c = Box.createHorizontalBox();

            c.add(new JLabel(currentNode.getName()) {
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            });

            NodeInfo nodeInfo = getNodeInfo(currentNode);
            char type = nodeInfo.getChar();

            if (type == 'm') {
                c.add(Box.createHorizontalStrut(10));
                String text = "Manipulated, Mean = " +
                        nf.format(nodeInfo.getMean()) + ", Variance = " +
                        nf.format(nodeInfo.getVariance());
                c.add(new JLabel(text));
            }
            else if (type == 'p') {
                c.add(Box.createHorizontalStrut(10));
                double result = getPredictionRunner().getResult();
                String text = Double.isNaN(result) ? "(Undefined)" : nf.format(
                        result);
                c.add(new JLabel("Predicted: " + text));
            }
            else if (type == 'c') {
                c.add(Box.createHorizontalStrut(10));
                c.add(new JLabel("Conditioned on"));
            }

            JButton changeButton = new JButton("Change");

            changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    launchSelectionButton(currentNode);
                }
            });

            c.add(Box.createHorizontalGlue());
            c.add(changeButton);

            b.add(c);
        }

        Box b2 = Box.createHorizontalBox();
        b2.add(predictButton);
        b.add(b2);

        predictButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doClickPredictButton();
            }
        });

        layoutPanel.removeAll();
        layoutPanel.add(b, BorderLayout.CENTER);
        layoutPanel.revalidate();
        layoutPanel.repaint();
    }

    private void launchSelectionButton(Node node) {
        NodeInfo nodeInfo = getNodeInfo(node);

        double mean = nodeInfo.getMean();
        double variance = nodeInfo.getVariance();

        final DoubleTextField meanField = new DoubleTextField(mean, 10, NumberFormatUtil.getInstance().getNumberFormat());
        final DoubleTextField varianceField =
                new DoubleTextField(variance, 10, NumberFormatUtil.getInstance().getNumberFormat());

        Box b = Box.createVerticalBox();
        b.setBorder(new EmptyBorder(10, 10, 10, 10));

        final JRadioButton none = new JRadioButton("(none)");
        final JRadioButton manipulate = new JRadioButton("Manipulate  ");
        final JRadioButton condition = new JRadioButton("Condition on");
        final JRadioButton predict = new JRadioButton("Predict");

        none.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFieldsEnabled(meanField, manipulate, varianceField);
            }
        });

        manipulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFieldsEnabled(meanField, manipulate, varianceField);
            }
        });

        condition.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFieldsEnabled(meanField, manipulate, varianceField);
            }
        });

        predict.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFieldsEnabled(meanField, manipulate, varianceField);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(none);
        group.add(condition);
        group.add(predict);
        group.add(manipulate);

        if (nodeInfo.getIdentifier() == 'n') {
            none.setSelected(true);
            setFieldsEnabled(meanField, manipulate, varianceField);
        }
        else if (nodeInfo.getChar() == 'm') {
            manipulate.setSelected(true);
            setFieldsEnabled(meanField, manipulate, varianceField);
        }
        else if (nodeInfo.getChar() == 'p') {
            predict.setSelected(true);
            setFieldsEnabled(meanField, manipulate, varianceField);
        }
        else if (nodeInfo.getChar() == 'c') {
            condition.setSelected(true);
            setFieldsEnabled(meanField, manipulate, varianceField);
        }

        JLabel nodeName = new JLabel(node.getName());
        JLabel meanLabel = new JLabel("Mean = ");
        JLabel varianceLabel = new JLabel(" Var = ");

        Box b0 = Box.createHorizontalBox();
        b0.add(nodeName);
        b0.add(Box.createHorizontalGlue());

        Box b4 = Box.createHorizontalBox();
        b4.add(none);
        b4.add(Box.createHorizontalGlue());

        Box b1 = Box.createHorizontalBox();
        b1.add(manipulate);
        b1.add(meanLabel);
        b1.add(meanField);
        b1.add(varianceLabel);
        b1.add(varianceField);
        b1.add(Box.createHorizontalGlue());

        Box b2 = Box.createHorizontalBox();
        b2.add(predict);
        b2.add(Box.createHorizontalGlue());

        Box b3 = Box.createHorizontalBox();
        b3.add(condition);
        b3.add(Box.createHorizontalGlue());

        b.add(b0);
        b.add(b4);
        b.add(b1);
        b.add(b2);
        b.add(b3);

        int ret = JOptionPane.showOptionDialog(PredictionEditor.this, b,
                "Select Node Properties", -1, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"OK"}, "OK");

        if (ret == 0) {
            if (none.isSelected()) {
                nodeInfo.setIdentifier('n');
            }
            else if (manipulate.isSelected()) {
                nodeInfo.setIdentifier('m');
                nodeInfo.setMean(meanField.getValue());
                nodeInfo.setVar(varianceField.getValue());

//                input().put(node, new NodeInfo('m', meanField.getValue(),
//                        varianceField.getValue()));
            }
            else if (predict.isSelected()) {
                nodeInfo.setIdentifier('p');
//                input().put(node, new NodeInfo('p'));
            }
            else if (condition.isSelected()) {
                nodeInfo.setIdentifier('c');
//                input().put(node, new NodeInfo('c'));
            }
        }

        resetLayoutPanel();
    }

    private void setFieldsEnabled(final DoubleTextField meanField,
            final JRadioButton manipulate,
            final DoubleTextField varianceField) {
        meanField.setEnabled(manipulate.isSelected());
        varianceField.setEnabled(manipulate.isSelected());
    }

    private NodeInfo getNodeInfo(Node node) {
        if (input().get(node) == null) {
            input().put(node, new NodeInfo('n'));
        }

        return input().get(node);
    }

    /**
     * Actually runs the prediction algorithm--that is, gets the information
     * from the interface, sets up the prediction runner, and executes.
     */
    private void doClickPredictButton() {
        List<Node> nodes = predictionRunner().getPag().getNodes();

        ManipulatedVariable variable = null;
        Node predictedVariable = null;
        Set<Node> conditioningSet = new HashSet<Node>();

        for (Object node : nodes) {
            Node currentNode = (Node) node;

            if (input().containsKey(currentNode)) {
                NodeInfo nodeInfo = getNodeInfo(currentNode);

                if (nodeInfo.getChar() == 'm') {
                    variable = new ManipulatedVariable(currentNode,
                            nodeInfo.getMean(), nodeInfo.getVariance());
                }
                else if (nodeInfo.getChar() == 'p') {
                    predictedVariable = currentNode;
                }
                else if (nodeInfo.getChar() == 'c') {
                    conditioningSet.add(currentNode);
                }
            }
        }

        try {
            predictionRunner().setManipulatedVariable(variable);
            predictionRunner().setPredictedVariable(predictedVariable);
            predictionRunner().setConditioningVariables(conditioningSet);
            predictionRunner().execute();
        }
        catch (Exception exc) {
            String s = exc.getMessage();

            if ("".equals(s) || s == null) {
                s = "Execution failed.";
            }

            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), s);
        }

        resetLayoutPanel();
    }

    private PredictionRunner getPredictionRunner() {
        return predictionRunner;
    }

    /**
     * When the user chooses options, the node is mapped to a NodeInfo object.
     * 'm' is manipulated, 'p' is predicted, 'c' is conditioned on. The Double
     * fields are used if its manipulated, otherwise they are null. Thus calling
     * getMean or getVar on a non-manipulated node will throw a null pointer
     * exception, which is reasonable behavior.
     */
    private final class NodeInfo {
        private char identifier;
        private double mean = 0.0;
        private double var = 1.0;

        NodeInfo(char k) {
            this.identifier = k;
        }

        NodeInfo(char k, double mean, double var) {
            this.identifier = k;
            this.setMean(mean);
            this.setVar(var);
        }

        public char getChar() {
            return getIdentifier();
        }

        public double getMean() {
            return mean;
        }

        public double getVariance() {
            return var;
        }

        public char getIdentifier() {
            return identifier;
        }

        public void setIdentifier(char identifier) {
            this.identifier = identifier;
        }

        public void setMean(double mean) {
            this.mean = mean;
        }

        public void setVar(double var) {
            this.var = var;
        }
    }
}




