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
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.IndTestProducer;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;


/**
 * Lists independence facts specified by user and allows the list to be sorted
 * by independence fact or by p value.
 *
 * @author Joseph Ramsey
 */
public class IndependenceFactsAction extends AbstractAction {
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    private Component centeringComp;
    private LinkedList<String> vars;
    private JTextField textField;
    private IndTestProducer indTestProducer;
    private List<Result> results = new ArrayList<Result>();
    private AbstractTableModel tableModel;
    private IndependenceTest independenceTest;
    private int sortDir;
    private int lastSortCol;

    /**
     * Constructs a new action to open sessions.
     *
     * @param comp            The component it should be in front of.
     * @param indTestProducer The gadget you get the independence test from.
     * @param menuName        The name that appears in the menu.
     */
    public IndependenceFactsAction(Component comp,
                                   IndTestProducer indTestProducer,
                                   String menuName) {
        super(menuName);

        if (indTestProducer == null) {
            throw new NullPointerException();
        }

        this.centeringComp = comp;
        this.indTestProducer = indTestProducer;
        this.independenceTest = indTestProducer.getIndependenceTest();
        this.vars = new LinkedList<String>();
        this.textField = new JTextField(40);
        this.textField.setEditable(false);
        this.textField.setFont(new Font("Serif", Font.BOLD, 14));
        this.textField.setBackground(new Color(250, 250, 250));

        resetText();
    }

    //========================PUBLIC METHODS==========================//

    /**
     * Performs the action of opening a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        this.independenceTest = getIndTestProducer().getIndependenceTest();
        final List<String> varNames = new ArrayList<String>();
        varNames.add("VAR");
        varNames.addAll(getDataVars());
        varNames.add("?");
        varNames.add("+");

        final JComboBox variableBox = new JComboBox();
        DefaultComboBoxModel aModel1 = new DefaultComboBoxModel(varNames.toArray(new String[0]));
        aModel1.setSelectedItem("VAR");
        variableBox.setModel(aModel1);

//        variableBox.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                System.out.println(e);
//            }
//        });

        variableBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();

                String var = (String) box.getSelectedItem();
                LinkedList<String> vars = getVars();
                int size = vars.size();

                if ("VAR".equals(var)) {
                    return;
                }

                if ("?".equals(var)) {
                    if (size >= 0 && !vars.contains("+")) {
                        vars.addLast(var);
                    }
                } else if ("+".equals(var)) {
                    if (size >= 2) {
                        vars.addLast(var);
                    }
                } else if ((vars.indexOf("?") < 2) && !(vars.contains("+")) &&
                        !(vars.contains(var))) {
                    vars.add(var);
                }

                resetText();

                // This is a workaround to an introduced bug in the JDK whereby
                // repeated selections of the same item send out just one
                // action event.
                DefaultComboBoxModel aModel = new DefaultComboBoxModel(
                        varNames.toArray(new String[0]));
                aModel.setSelectedItem("VAR");
                variableBox.setModel(aModel);
            }
        });

        final JButton delete = new JButton("Delete");

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!getVars().isEmpty()) {
                    getVars().removeLast();
                    resetText();
                }
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if ('?' == e.getKeyChar()) {
                    variableBox.setSelectedItem("?");
                } else if ('+' == e.getKeyChar()) {
                    variableBox.setSelectedItem("+");
                } else if ('\b' == e.getKeyChar()) {
                    vars.removeLast();
                    resetText();
                }

                e.consume();
            }
        });

        delete.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if ('?' == e.getKeyChar()) {
                    variableBox.setSelectedItem("?");
                } else if ('+' == e.getKeyChar()) {
                    variableBox.setSelectedItem("+");
                } else if ('\b' == e.getKeyChar()) {
                    vars.removeLast();
                    resetText();
                }
            }
        });

        variableBox.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);

                if ('\b' == e.getKeyChar()) {
                    vars.removeLast();
                    resetText();
                }
            }
        });

        JButton list = new JButton("LIST");
        list.setFont(new Font("Dialog", Font.BOLD, 14));

        list.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateResults();
            }
        });

        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Test: "));
        b2.add(new JLabel(getIndependenceTest().toString()));
        b2.add(Box.createHorizontalGlue());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(10));

        Box b3 = Box.createHorizontalBox();
        b3.add(getTextField());
        b3.add(variableBox);
        b3.add(delete);
        b1.add(b3);
        b1.add(Box.createVerticalStrut(10));

        tableModel = new AbstractTableModel() {
            public String getColumnName(int column) {
                if (column == 0) {
                    return "Index";
                }
                if (column == 1) {
                    if (independenceTest instanceof IndTestDSep) {
                        return "D-Separation Relation";
                    } else {
                        return "Independence Relation";
                    }
                } else if (column == 2) {
                    return "Judgment";
                } else if (column == 3) {
                    return "P Value";
                }

                return null;
            }

            public int getColumnCount() {
                if (usesDSeparation()) {
                    return 3;
                } else {
                    return 4;
                }
            }

            public int getRowCount() {
                return getResults().size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                Result result = getResults().get(rowIndex);

                if (columnIndex == 0) {
                    return result.getIndex() + 1;
                }
                if (columnIndex == 1) {
                    return result.getFact();
                } else if (columnIndex == 2) {
                    if (independenceTest instanceof IndTestDSep) {
                        return result.isIndep() ? "D-Separated" : "D-Connected";
                    } else {
                        return result.isIndep() ? "Independent" : "Dependent";
                    }
                } else if (columnIndex == 3) {
                    return nf.format(result.getpValue());
                }

                return null;
            }

            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Number.class;
                }
                if (columnIndex == 1) {
                    return String.class;
                } else if (columnIndex == 2) {
                    return Number.class;
                } else if (columnIndex == 3) {
                    return Number.class;
                }

                return null;
            }
        };

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMinWidth(200);
        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(100);

        if (!(usesDSeparation())) {
            table.getColumnModel().getColumn(3).setMinWidth(80);
            table.getColumnModel().getColumn(3).setMaxWidth(80);
        }

        JTableHeader header = table.getTableHeader();

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getSource();
                Point point = e.getPoint();
                int col = header.columnAtPoint(point);
                int sortCol = header.getTable().convertColumnIndexToModel(col);

                sortByColumn(sortCol, true);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(400, 400));
        b1.add(scroll);

        Box b4 = Box.createHorizontalBox();
        b4.add(new JLabel("Limit list to "));


        IntTextField field = new IntTextField(getListLimit(), 7);

        field.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                try {
                    setListLimit(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        b4.add(field);
        b4.add(new JLabel(" items."));
        b4.add(Box.createHorizontalGlue());
        b4.add(list);

        b1.add(b4);
        b1.add(Box.createVerticalStrut(10));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(b1, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        EditorWindow editorWindow =
                new EditorWindow(panel, "Independence Facts", "Save", false);
        DesktopController.getInstance().addEditorWindow(editorWindow);
        editorWindow.setVisible(true);

        // Set the ok button so that pressing enter activates it.
        // jdramsey 5/5/02
        JRootPane root = SwingUtilities.getRootPane(editorWindow);
        if (root != null) {
            root.setDefaultButton(list);
        }
    }

    //=============================PRIVATE METHODS=======================//

    private Component getCenteringComp() {
        return centeringComp;
    }

    private boolean usesDSeparation() {
        return getIndependenceTest() instanceof IndTestDSep;
    }

    private void sortByColumn(final int sortCol, boolean allowReverse) {
        if (allowReverse && sortCol == getLastSortCol()) {
            setSortDir(-1 * getSortDir());
        } else {
            setSortDir(1);
        }

        setLastSortCol(sortCol);

        Collections.sort(results, new Comparator<Result>() {
            public int compare(Result r1, Result r2) {

                switch (sortCol) {
                    case 0:
                        return getSortDir() * (r1.getIndex() - r2.getIndex());
                    case 1:
                        return getSortDir() * (r1.getIndex() - r2.getIndex());
                    case 2:
                        int ind1 = r1.isIndep() ? 1 : 0;
                        int ind2 = r2.isIndep() ? 1 : 0;
                        return getSortDir() * (ind1 - ind2);
                    case 3:
                        double difference = getSortDir() *
                                (r1.getpValue() - r2.getpValue());

                        if (difference < 0) {
                            return -1;
                        } else if (difference == 0) {
                            return 0;
                        } else {
                            return 1;
                        }
                    default:
                        return 0;
                }
            }
        });

        tableModel.fireTableDataChanged();
    }

    private List<String> getDataVars() {
        return getIndependenceTest().getVariableNames();
    }

    private void resetText() {
        StringBuffer buf = new StringBuffer();

        if (getVars().size() == 0) {
            buf.append("Choose variables and wildcards from dropdown-->");
        }

        if (getVars().size() > 0) {
            buf.append(" ").append(getVars().get(0));
            buf.append(" _||_ ");
        }

        if (getVars().size() > 1) {
            buf.append(getVars().get(1));
        }

        if (getVars().size() > 2) {
            buf.append(" | ");
        }

        for (int i = 2; i < getVars().size() - 1; i++) {
            buf.append(getVars().get(i));
            buf.append(", ");
        }

        if (getVars().size() > 2) {
            buf.append(getVars().get(getVars().size() - 1));
        }

        textField.setText(buf.toString());
    }

    private void generateResults() {
        this.getResults().clear();
        List<String> dataVars = getDataVars();

        if (getVars().size() < 2) {
            tableModel.fireTableDataChanged();
            return;
        }

        int min = 0, max = 0;
        List<String> specifiedVars = new ArrayList<String>();

        for (int i = 0; i < getVars().size(); i++) {
            String var = getVars().get(i);

            if ("?".equals(var)) {
                min++;
                max++;
            } else if ("+".equals(var)) {
                max++;
            } else {
                specifiedVars.add(var);
            }
        }

        int[] pattern = new int[getVars().size()];

        for (int j = 0; j < getVars().size(); j++) {
            if ("?".equals(getVars().get(j)) || "+".equals(getVars().get(j))) {
                pattern[j] = 1;
            }
        }

        int resultIndex = -1;

        for (int n = min; n <= max; n++) {
            List<String> remainder = new ArrayList<String>(dataVars);
            remainder.removeAll(specifiedVars);

            if (remainder.size() < n) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(remainder.size(), n);
            int[] choice;

            while ((choice = cg.next()) != null) {
                if (resultIndex >= getListLimit() - 1) {
                    JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                            "List limit exceeded.");
                    break;
                }

                int[] spec = new int[n + specifiedVars.size()];
                int choiceIndex = -1;

                for (int j = 0; j < n + specifiedVars.size(); j++) {
                    if (pattern[j] == 0) {
                        spec[j] = dataVars.indexOf(getVars().get(j));
                    } else {
                        int index = choice[++choiceIndex];
                        String var1 = remainder.get(index);
                        spec[j] = dataVars.indexOf(var1);
                    }
                }

                Node x = getIndependenceTest().getVariable(
                        dataVars.get(spec[0]));
                Node y = getIndependenceTest().getVariable(
                        dataVars.get(spec[1]));

                List<Node> z = new LinkedList<Node>();

                for (int i = 2; i < spec.length; i++) {
                    z.add(getIndependenceTest().getVariable(
                            dataVars.get(spec[i])));
                }

                boolean indep = getIndependenceTest().isIndependent(x, y, z);
                double pValue = getIndependenceTest().getPValue();

                if (usesDSeparation()) {
                    getResults().add(new Result(++resultIndex,
                            dsepFactString(x, y, z), indep, pValue));
                } else {
                    getResults().add(new Result(++resultIndex,
                            independenceFactString(x, y, z), indep, pValue));
                }
            }
        }

        tableModel.fireTableDataChanged();
    }

    private LinkedList<String> getVars() {
        return vars;
    }

    private JTextField getTextField() {
        return textField;
    }

    private IndTestProducer getIndTestProducer() {
        return indTestProducer;
    }

    private List<Result> getResults() {
        return results;
    }

    private static final class Result {
        private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        private int index;
        private String fact;
        private boolean indep;
        private double pValue;

        public Result(int index, String fact, boolean indep, double pValue) {
            this.index = index;
            this.fact = fact;
            this.indep = indep;
            this.pValue = pValue;
        }

        public int getIndex() {
            return index;
        }

        public String getFact() {
            return fact;
        }

        public boolean isIndep() {
            return indep;
        }

        public double getpValue() {
            return pValue;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("Result: ");
            buf.append(getFact()).append("\t");
            buf.append(isIndep()).append("\t");
            buf.append(nf.format(getpValue()));
            return buf.toString();
        }
    }

    private static String independenceFactString(Node x, Node y,
                                                 List<Node> condSet) {
        StringBuffer sb = new StringBuffer();

        sb.append(" ").append(x.getName());
        sb.append(" _||_ ");
        sb.append(y.getName());

        Iterator<Node> it = condSet.iterator();

        if (it.hasNext()) {
            sb.append(" | ");
            sb.append(it.next());
        }

        while (it.hasNext()) {
            sb.append(", ");
            sb.append(it.next());
        }

        return sb.toString();
    }

    private static String dsepFactString(Node x, Node y, List<Node> condSet) {
        StringBuffer sb = new StringBuffer();

        sb.append(" ").append("dsep(");
        sb.append(x.getName());
        sb.append(", ");
        sb.append(y.getName());

        Iterator<Node> it = condSet.iterator();

        if (it.hasNext()) {
            sb.append(" | ");
            sb.append(it.next());
        }

        while (it.hasNext()) {
            sb.append(", ");
            sb.append(it.next());
        }

        sb.append(")");

        return sb.toString();
    }

    private IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    private int getLastSortCol() {
        return this.lastSortCol;
    }

    private void setLastSortCol(int lastSortCol) {
        if (lastSortCol < 0 || lastSortCol > 4) {
            throw new IllegalArgumentException();
        }

        this.lastSortCol = lastSortCol;
    }

    private int getSortDir() {
        return this.sortDir;
    }

    private void setSortDir(int sortDir) {
        if (!(sortDir == 1 || sortDir == -1)) {
            throw new IllegalArgumentException();
        }

        this.sortDir = sortDir;
    }

    private int getListLimit() {
        return Preferences.userRoot().getInt("indFactsListLimit", 10000);
    }

    private void setListLimit(int listLimit) {
        if (listLimit < 1) {
            throw new IllegalArgumentException();
        }

        Preferences.userRoot().putInt("indFactsListLimit", listLimit);
    }
}


