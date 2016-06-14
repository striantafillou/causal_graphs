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

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.search.indtest.GenCondIndTest;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetradapp.model.RegressionParams;
import edu.cmu.tetradapp.model.RegressionRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.util.TextTable;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Mike Freenor, borrowing from a bunch of stuff as usual.
 * Date: Jul 3, 2008
 * Time: 4:04:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionalIndependenceDialog extends JPanel {

    /**
     * The workbench used to display the graph of significant regression into
     * the target.
     */
    private GraphWorkbench workbench;

    /**
     * A large text area into which the (String) output of the regression result
     * is dumped. (This is what needs to change.)
     */
    private JTextArea reportText = new JTextArea();

    /**
     * Presents the same information in reportText as a text preamble with a
     * table of coefficients etc.
     */
    private JComponent textWithTable = TextWithTable.emptyCompoenent();

    /**
     * The gadget that does the regression.
     */
    private ConditionalIndependenceWrapper wrapper;

    private ConditionalIndependenceParamsPanel editorPanel;

    /**
     * Constructs a regression editor. A regression runner is required, since
     * that's what does the actual regression.
     *
     * @throws NullPointerException if <code>regressionRunner</code> is null.
     */
    public ConditionalIndependenceDialog(ConditionalIndependenceWrapper condInd) {
        if (condInd == null) {
            throw new NullPointerException("The regression runner is required.");
        }

        this.wrapper = condInd;
        Graph outGraph = new EdgeListGraph();

        final JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTest();
            }
        });

        workbench = new GraphWorkbench(outGraph);

        JScrollPane workbenchScroll = new JScrollPane(workbench);
        workbenchScroll.setPreferredSize(new Dimension(400, 400));

        reportText = new JTextArea();
        reportText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportText.setTabSize(10);
        reportText.setText("Usage:\n" +
                "\n" +
                "Tests to see if X _||_ Y | Conditioning Set");

        if (wrapper != null && wrapper.getResult() != null) {
            reportText.setText(wrapper.getResult().toString());
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(600, 400));
        tabbedPane.add("Model", new JScrollPane(reportText));
//        tabbedPane.add("Tabularized Model", new JScrollPane(textWithTable));
        tabbedPane.add("Output Graph", workbenchScroll);

        Box b = Box.createVerticalBox();
        Box b1 = Box.createHorizontalBox();
        ConditionalIndependenceParamsPanel editorPanel = new ConditionalIndependenceParamsPanel(
                (RegressionParams) wrapper.getParams(),
                this.wrapper.getDataSet());

        editorPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();

                if ("significanceChanged".equals(propertyName)) {
                    runTest();
                }
            }
        });

        b1.add(editorPanel);
        this.editorPanel = editorPanel;
        b1.add(tabbedPane);
        b.add(b1);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(executeButton);
        b.add(buttonPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(b, BorderLayout.CENTER);

        setName("Regression Result:");
    }

    /**
     * Sets the name of this editor.
     */
    public void setName(String name) {
        String oldName = getName();
        super.setName(name);
        this.firePropertyChange("name", oldName, getName());
    }

    //========================= Private Methods ========================//

    /**
     * Runs the regression, resetting the text output and graph output.
     */
    private void runTest() {
        // here we need to add Y to the front of the regressors temporarily, maybe by
        // passing new RegressorParams as the argument to execute()

        DataSet dataSet = (DataSet)wrapper.getDataSet();
        GenCondIndTest test = new GenCondIndTest((DataSet)wrapper.getDataSet(), ((RegressionParams)(wrapper.getParams())).getAlpha());

        Node x = dataSet.getVariable(ConditionalIndependenceParamsPanel.getXField().getText());
        Node y = dataSet.getVariable(ConditionalIndependenceParamsPanel.getYField().getText());
        //java.util.List<Node> z = new java.util.List<Node>(); okay all that needs to be done is to pull this, then pass it off to CondIndTest which'll unpack it into RegParams
        JList predictors = ConditionalIndependenceParamsPanel.getPredictorsList();
        
        String displayString = "";

        ListModel m = predictors.getModel();
        LinkedList<Node> z = new LinkedList<Node>();
        for (int i = 0; i < m.getSize(); i++)
        {
            z.add(dataSet.getVariable((String)(m.getElementAt(i))));
        }
        
        test.isIndependent(x, y, z);
        //System.out.println(test);
        //LinkedList<Node> z = new LinkedList<Node>(ConditionalIndependenceParamsPanel.getPredictors());

        Graph graph = test.getWrapper().getOutGraph();
        if (graph != null)
        {
            GraphUtils.arrangeInCircle(graph, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(graph);
            workbench.setGraph(graph);
        }

        reportText.setText(test.toString());

        /*
        RegressionParams newParams = wrapper.execute(ConditionalIndependenceParamsPanel.getYField().getText());

        Graph graph = wrapper.getOutGraph();
        if (graph != null)
        {
            GraphUtils.arrangeInCircle(graph, 200, 200, 150);
            GraphUtils.fruchtermanReingoldLayout(graph);
            workbench.setGraph(graph);
        }
        RegressionResult report = wrapper.getResult();
        String displayString = "";
        if (wrapper.failure)
        {
            displayString += "General independence test with discrete variables that are not binary is not\n" +
                    "currently supported! Please use other variables.";
        }
        else
        {
            if (!wrapper.performedSwap)
                displayString += "Testing to see whether the following relation holds:\n " + newParams.getTargetName() + " _||_ " + this.editorPanel.getYField().getText() + " | {";
            else
                displayString += "Testing to see whether the following relation holds:\n " + newParams.getTargetName() + " _||_ " + this.editorPanel.getXField().getText() + " | {";

            for (int i = 1; i < newParams.getRegressorNames().length; i++)
            {
                displayString += newParams.getRegressorNames()[i];
                if (i != newParams.getRegressorNames().length - 1)
                {
                    displayString += ", ";
               }
            }
            displayString += "}\n\n";

            if (wrapper.linear)
            {
                if (report.getP()[1] > newParams.getAlpha())
                {
                    displayString += "The relation HOLDS.\n " + newParams.getTargetName() + " is conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                }
                else
                {
                    displayString += "The relation DOES NOT HOLD.\n " + newParams.getTargetName() + " is NOT conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                }
                displayString += "Ran the following linear regression to obtain result:\n\n";
                displayString += report.toString();
            }
            else
            {
                if (wrapper.logRegResult.getpValues()[1] > newParams.getAlpha())
                {
                    displayString += "The relation HOLDS.\n " + newParams.getTargetName() + " is conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                }
                else
                {
                    displayString += "The relation DOES NOT HOLD.\n " + newParams.getTargetName() + " is NOT conditionally independent of " +
                            newParams.getRegressorNames()[0] + " given the above conditioning set.\n\n";
                }
                displayString += "Ran the following logistic regression to obtain result:\n\n";
                displayString += wrapper.logRegResult.getResult().toString();
            }
        }
        reportText.setText(displayString);
        textWithTable.removeAll();
        textWithTable.setLayout(new BorderLayout());
        if (report != null)
            textWithTable.add(TextWithTable.component(report.getPreamble(),
                    report.getResultsTable()));
        textWithTable.revalidate();
        textWithTable.repaint();
        */

    }

    /**
     * Puts the output of getReport() into a component with preamble in a
     * textarea and the rest in a JTable backed by the TextTable. The
     * <code>TextTable.emptyComponent</code> is just a white JPanel, for initial
     * use. After that, call <code>TextTable.component</code>.
     *
     * @author Joseph Ramsey
     */
    private static class TextWithTable {

        private TextWithTable() {
            // Hidden.
        }

        public static JComponent emptyCompoenent() {
            JPanel jPanel = new JPanel();
            jPanel.setBackground(Color.WHITE);
            return jPanel;
        }

        public static JComponent component(String preamble, TextTable textTable) {
            TextWithTable textWithTable = new TextWithTable();

            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);

            Box b = Box.createVerticalBox();

            Box b1 = Box.createHorizontalBox();
            b1.add(new JTextArea(preamble));
            b.add(b1);

            Box b2 = Box.createHorizontalBox();
            JScrollPane pane = new JScrollPane(textWithTable.getJTableFor(textTable));
            b2.add(pane);
            b.add(b2);

            panel.setLayout(new BorderLayout());
            panel.add(b, BorderLayout.CENTER);

            return panel;
        }

        private JTable getJTableFor(final TextTable textTable) {

            TableModel model = new AbstractTableModel() {

                public int getRowCount() {
                    return textTable.getNumRows();
                }

                public int getColumnCount() {
                    return textTable.getNumColumns();
                }

                public Object getValueAt(int rowIndex, int columnIndex) {
                    return textTable.getTokenAt(rowIndex, columnIndex);
                }

                public String getColumnName(int column) {
                    return null;
                }
            };

            JTable table = new JTable(model);

            for (int j = 0; j < 6; j++) {
                TableColumn column = table.getColumnModel().getColumn(j);

                column.setCellRenderer(new DefaultTableCellRenderer() {

                    // implements javax.swing.table.TableCellRenderer
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component renderer = super.getTableCellRendererComponent(table,
                                value, isSelected, hasFocus, row, column);
                        setText((String) value);
                        setHorizontalAlignment(JLabel.RIGHT);
                        return renderer;
                    }
                });
            }

            return table;
        }
    }
}


