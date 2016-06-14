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

package edu.cmu.tetradapp.gene.editor;

import edu.cmu.tetrad.util.dist.Normal;
import edu.cmu.tetradapp.model.BooleanGlassGeneIm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Edits a BooleanGlassGeneIm.
 *
 * @author Joseph Ramsey
 */
public class BooleanGlassGeneImEditor extends JPanel
        implements PropertyChangeListener {

    private JTextField decayRateField;
    private JTextField booleanInfluenceRateField;

    private JTable booleansTable;

    // editing target...
    private BooleanGlassGeneIm im;

    /**
     * Constructs a new editor for the given BooleanGlassGeneIm.
     */
    public BooleanGlassGeneImEditor(BooleanGlassGeneIm im) {

        this.im = im;

        setLayout(new BorderLayout());

        // set up booleans panel
        JPanel booleansPanel = new JPanel();

        booleansPanel.setLayout(new BorderLayout());

        JPanel varsPanel = new JPanel();

        varsPanel.setLayout(new BorderLayout());
        varsPanel.add(new Label("Vars"), BorderLayout.NORTH);

        // variables list for booleans panel.
        List<String> displayFactors = new ArrayList<String>();
        List<String> modelFactors = im.getFactors();

        for (String modelFactor : modelFactors) {
            displayFactors.add(modelFactor + ":L0");
        }

        JList varList = new JList(displayFactors.toArray());

        ListSelectionModel selectionModel = new DefaultListSelectionModel();

        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        varList.setSelectionModel(selectionModel);
        varsPanel.add(new JScrollPane(varList), BorderLayout.CENTER);
        varList.addListSelectionListener(new ListSelectionListener() {

            /**
             * Listens to the variable list JList.
             *
             * @param e the event.
             */
            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    JList theList = (JList) e.getSource();
                    int index = theList.getSelectedIndex();

                    updateBooleanTable(index);
                }
            }
        });

        // booleans table for booleans panel.
        booleansPanel.add(varsPanel, BorderLayout.WEST);

        booleansTable = new JTable(new ErrorsTable(im));

        booleansTable.getTableHeader().setReorderingAllowed(false);
        booleansTable.getTableHeader().setResizingAllowed(false);
        booleansTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        booleansPanel.add(new JScrollPane(booleansTable), BorderLayout.CENTER);

        // set up errors panel
        JPanel errorsPanel = new JPanel();

        errorsPanel.setLayout(new BorderLayout());

        JTable errorsTable = new JTable(new ErrorsTable(im));

        errorsTable.getTableHeader().setReorderingAllowed(false);
        errorsTable.getTableHeader().setResizingAllowed(false);
        errorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        errorsPanel.add(new JScrollPane(errorsTable), BorderLayout.CENTER);
        errorsPanel.add(new JScrollPane(errorsTable), BorderLayout.CENTER);

        // construct tabbed pane.
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Booleans", booleansPanel);
        tabbedPane.addTab("Errors", errorsPanel);

        //tabbedPane.addTab("Measurement Model", measurementModelPanel);
        // add tabbed pane
        add(tabbedPane, BorderLayout.CENTER);

        // add lower panel
        JLabel decayRateLabel = new JLabel("DecayRate = ");

        decayRateLabel.setForeground(Color.black);

        decayRateField = new JTextField() {

            public Dimension getPreferredSize() {
                return new Dimension(100, 0);
            }

            public Dimension getMaximumSize() {
                return new Dimension(100, 100);
            }
        };

        decayRateField.setHorizontalAlignment(JTextField.RIGHT);
        decayRateField.setText(Double.toString(im.getBooleanGlassFunction()
                .getDecayRate()));
        decayRateField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String newValue = e.getActionCommand();

                try {
                    double d = Double.parseDouble(newValue);

                    getBooleanGlassGeneIm().getBooleanGlassFunction()
                            .setDecayRate(d);
                }
                catch (Exception e2) {
                    double decayRate = getBooleanGlassGeneIm()
                            .getBooleanGlassFunction().getDecayRate();
                    decayRateField.setText(Double.toString(decayRate));
                }
            }
        });

        JLabel booleanInfluenceRateLabel =
                new JLabel("BooleanInfluenceRate = ");

        booleanInfluenceRateLabel.setForeground(Color.black);

        booleanInfluenceRateField = new JTextField() {

            public Dimension getPreferredSize() {
                return new Dimension(100, 0);
            }

            public Dimension getMaximumSize() {
                return new Dimension(100, 100);
            }
        };

        booleanInfluenceRateField.setHorizontalAlignment(JTextField.RIGHT);
        double booleanInfluenceRate = im.getBooleanGlassFunction()
                .getBooleanInfluenceRate();
        booleanInfluenceRateField.setText(
                Double.toString(booleanInfluenceRate));
        booleanInfluenceRateField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String newValue = e.getActionCommand();

                try {
                    double d = Double.parseDouble(newValue);

                    getBooleanGlassGeneIm().getBooleanGlassFunction()
                            .setBooleanInfluenceRate(d);
                }
                catch (Exception e2) {
                    double _booleanInfluenceRate = getBooleanGlassGeneIm()
                            .getBooleanGlassFunction().getBooleanInfluenceRate();
                    booleanInfluenceRateField.setText(
                            Double.toString(_booleanInfluenceRate));
                }
            }
        });

        Box lowerBox = Box.createHorizontalBox();

        lowerBox.add(Box.createHorizontalStrut(30));
        lowerBox.add(decayRateLabel);
        lowerBox.add(decayRateField);
        lowerBox.add(Box.createHorizontalStrut(30));
        lowerBox.add(booleanInfluenceRateLabel);
        lowerBox.add(booleanInfluenceRateField);
        lowerBox.add(Box.createHorizontalStrut(20));
        lowerBox.add(Box.createHorizontalGlue());
        add(lowerBox, BorderLayout.SOUTH);

        // Put this last to avoid escaping the constructor before
        // private fields have been set (which are used in the
        // JList listener).
        varList.setSelectedIndex(0);
    }

    public void propertyChange(PropertyChangeEvent e) {

        if ("editorClosing".equals(e.getPropertyName())) {
            this.firePropertyChange("editorClosing", null, getName());
        }
    }

    private void updateBooleanTable(int factorIndex) {
        booleansTable.setModel(new BooleanTable(factorIndex, im));
    }

    private BooleanGlassGeneIm getBooleanGlassGeneIm() {
        return im;
    }
}

/**
 * Presents a boolean table as an AbstractTableModel.
 *
 * @author Joseph Ramsey
 */
class BooleanTable extends AbstractTableModel {

    private String t = "+1";
    private String f = "-1";
    private int factorIndex = 0;
    private BooleanGlassGeneIm im = null;
    private List parents = null;
    private int numParents;

    public BooleanTable(int factorIndex, BooleanGlassGeneIm im) {

        this.factorIndex = factorIndex;
        this.im = im;
        this.parents = im.getParents(factorIndex);
        this.numParents = im.getNumParents(factorIndex);
    }

    public int getRowCount() {
        return (int) (Math.pow(2.0, numParents));
    }

    public int getColumnCount() {

        if (numParents == 0) {
            return 0;
        } else {
            return numParents + 1;
        }
    }

    public String getColumnName(int column) {

        if (numParents == 0) {
            return null;
        } else if (column < numParents) {
            return (String) parents.get(column);
        } else if (column == numParents) {
            return "Fcn. Value";
        } else {
            return null;
        }
    }

    public boolean isCellEditable(int row, int column) {

        if (numParents == 0) {
            return false;
        } else if (column < numParents) {
            return false;
        } else {
            return column == numParents;
        }
    }

    public Object getValueAt(int row, int column) {

        if (numParents == 0) {
            return null;
        } else if (column < numParents) {

            // display combination
            int jump = 1;

            for (int i = 0; i < numParents - column - 1; i++) {
                jump *= 2;
            }

            return (row / jump) % 2 == 0 ? f : t;
        } else if (column == numParents) {

            // parent boolean combinations...
            return im.getRowValueAt(factorIndex, row) ? t : f;
        } else {
            return null;
        }
    }

    /**
     * Sets the value of the boolean function (the only editable column)
     *
     * @param value  Should be this.t or this.f.
     * @param row    the row to edit.
     * @param column The column to edit (like I said, the only editable column
     *               is the function value).
     */
    public void setValueAt(Object value, int row, int column) {

        String str = ((String) value).trim();

        if (this.t.equals(str)) {
            im.setRowValueAt(factorIndex, row, true);
        } else if (this.f.equals(str)) {
            im.setRowValueAt(factorIndex, row, false);
        }
    }
}

/**
 * Presents the error standard deviations of the given BooleanGlassGeneIm as an
 * editable AbstractTableModel.
 *
 * @author Joseph Ramsey
 */
class ErrorsTable extends AbstractTableModel {

    private BooleanGlassGeneIm im = null;
    private List factors = null;

    public ErrorsTable(BooleanGlassGeneIm im) {
        this.im = im;
        this.factors = im.getFactors();
    }

    public int getRowCount() {
        return factors.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int column) {

        if (column == 0) {
            return "Factor";
        } else if (column == 1) {
            return "St. Dev.";
        } else {
            return null;
        }
    }

    public boolean isCellEditable(int row, int column) {
        return column == 1;
    }

    public Object getValueAt(int row, int column) {

        if ((column == 0) && (row < factors.size())) {
            return factors.get(row);
        } else if ((column == 1) && (row < factors.size())) {
            Normal dist =
                    (Normal) im.getErrorDistribution(row);

            return dist.getParameter(1);
        }

        return null;
    }

    public void setValueAt(Object value, int row, int column) {

        if ((column == 1) && (row < factors.size())) {
            double d = new Double((String) value);

            if (d <= 0.0) {
                return;    // allow JTable to reinstate old value.
            }

            Normal dist =
                    (Normal) im.getErrorDistribution(row);

            dist.setParameter(1, d);
        }
    }
}


