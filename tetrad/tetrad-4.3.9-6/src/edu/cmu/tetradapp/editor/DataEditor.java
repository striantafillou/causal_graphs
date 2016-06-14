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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.KnowledgeEditable;
import edu.cmu.tetradapp.model.TabularComparison;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Displays data objects and allows users to edit these objects as well as load
 * and save them.
 *
 * @author Joseph Ramsey
 */
public final class DataEditor extends JPanel implements KnowledgeEditable,
        PropertyChangeListener {

    /**
     * The data wrapper being displayed.
     */
    private DataWrapper dataWrapper;

    /**
     * A tabbed pane containing displays for all data models and displaying
     * 'dataModel' currently.
     */
    private JTabbedPane tabbedPane = new JTabbedPane();

    //==========================CONSTUCTORS===============================//

    /**
     * Constructs the data editor with an empty list of data displays.
     */
    public DataEditor() {
    }

    public DataEditor(TabularComparison comparison) {
        this(new DataWrapper(comparison.getDataSet()));
    }


    /**
     * Constructs a standalone data editor.
     */
    public DataEditor(DataWrapper dataWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException("Data wrapper must not be null.");
        }

        this.dataWrapper = dataWrapper;
        setLayout(new BorderLayout());
        reset();

        tabbedPane().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (SwingUtilities.isRightMouseButton(e)) {             
                    Point point = e.getPoint();
                    final int index = tabbedPane().indexAtLocation(point.x, point.y);

                    if (index == -1) {
                        return;
                    }

                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem close = new JMenuItem("Close Tab");
                    menu.add(close);

                    menu.show(DataEditor.this, point.x, point.y);

                    close.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            closeTab(index);
                            DataEditor.this.grabFocus();
                            firePropertyChange("modelChanged", null, null);
                        }
                    });
                }
            }
        });
    }

    //==========================PUBLIC METHODS=============================//


    /**
     * Replaces the current Datamodels with the given one. Note, that by calling this
     * you are removing ALL the current data-models, they will be lost forever!
     *
     * @param model - The model, must not be null
     */
    public final void replace(DataModel model) {
        if (model == null) {
            throw new NullPointerException("The given model must not be null");
        }
        tabbedPane.removeAll();
        setPreferredSize(new Dimension(600, 400));
        DataModelList dataModelList = dataWrapper.getDataModelList();
        dataModelList.clear();

        // now rebuild
        if (model instanceof DataModelList) {
            for (DataModel dataModel : (DataModelList) model) {
                dataModelList.add(dataModel);
            }
        }
        else {
            dataModelList.add(model);
        }

        removeAll();

        if (model instanceof DataModelList) {
            for (int i = 0; i < ((DataModelList) model).size(); i++) {
                DataModel _model = ((DataModelList) model).get(i);
                this.tabbedPane.addTab(tabName(_model, 1), dataDisplay(_model));
                add(this.tabbedPane, BorderLayout.CENTER);
                add(menuBar(), BorderLayout.NORTH);
            }
        } else {
            this.tabbedPane.addTab(tabName(model, 1), dataDisplay(model));
            add(this.tabbedPane, BorderLayout.CENTER);
            add(menuBar(), BorderLayout.NORTH);
            validate();
        }
    }


    /**
     * Sets this editor to display contents of the given data model wrapper.
     */
    public final void reset() {
        tabbedPane().removeAll();
        setPreferredSize(new Dimension(600, 400));

        DataModelList dataModelList = dataWrapper.getDataModelList();
        DataModel selectedModel = dataWrapper.getSelectedDataModel();

        removeAll();
        removeEmptyModels(dataModelList);

        int selectedIndex = -1;

        for (int i = 0; i < dataModelList.size(); i++) {
            DataModel dataModel = dataModelList.get(i);
            tabbedPane().addTab(tabName(dataModel, i + 1),
                    dataDisplay(dataModel));
            if (selectedModel == dataModel) {
                selectedIndex = i;
            }
        }

        tabbedPane().setSelectedIndex(selectedIndex);

        tabbedPane().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                DataModel selectedModel = getSelectedDataModel();

                if (selectedModel == null) {
                    return;
                }

                getDataWrapper().getDataModelList().setSelectedModel(
                        selectedModel);
            }
        });

        add(tabbedPane(), BorderLayout.CENTER);
        add(menuBar(), BorderLayout.NORTH);
        validate();
    }


    public final void reset(DataModelList extraModels) {
        tabbedPane().removeAll();
        setPreferredSize(new Dimension(600, 400));

        DataModelList dataModelList = dataWrapper.getDataModelList();
        dataModelList.addAll(extraModels);

        removeAll();
        tabbedPane().removeAll();
        removeEmptyModels(dataModelList);

        int tabIndex = 0;

        for (DataModel dataModel : dataModelList) {
            tabbedPane().addTab(tabName(dataModel, ++tabIndex),
                    dataDisplay(dataModel));
        }

        add(tabbedPane(), BorderLayout.CENTER);
        add(menuBar(), BorderLayout.NORTH);
        validate();

        firePropertyChange("modelChanged", null, null);
    }

    public final void reset(DataModel dataModel) {
        tabbedPane().removeAll();
        setPreferredSize(new Dimension(600, 400));

        DataModelList dataModelList = dataWrapper.getDataModelList();
        dataModelList.clear();
        dataModelList.add(dataModel);

        removeEmptyModels(dataModelList);
        tabbedPane().removeAll();

        for (int i = 0; i < dataModelList.size(); i++) {
            Object _dataModel = dataModelList.get(i);
            tabbedPane().addTab(tabName(dataModel, i + 1),
                    dataDisplay(_dataModel));
        }

        add(tabbedPane(), BorderLayout.CENTER);
        add(menuBar(), BorderLayout.NORTH);
        validate();

        firePropertyChange("modelChanged", null, null);
    }


    /**
     * Returns the data sets that's currently in front.
     */
    public DataModel getSelectedDataModel() {
        Component selectedComponent = tabbedPane().getSelectedComponent();
        DataModelContainer scrollPane = (DataModelContainer) selectedComponent;

        if (scrollPane == null) {
            return null;
        }

        return scrollPane.getDataModel();
    }

    public void selectLastTab() {
        tabbedPane().setSelectedIndex(tabbedPane().getTabCount() - 1);
        DataModel selectedModel = getSelectedDataModel();

        if (selectedModel == null) {
            return;
        }

        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (dataModel instanceof DataModelList) {
            DataModelList dataModelList = (DataModelList) dataModel;
            dataModelList.setSelectedModel(selectedModel);
        }
    }

    public int getTabCount() {
        return tabbedPane().getTabCount();
    }

    public List<Node> getKnownVariables() {
        return dataWrapper.getKnownVariables();
    }

    public List<String> getVarNames() {
        return dataWrapper.getVarNames();
    }

    public Graph getSourceGraph() {
        return dataWrapper.getSourceGraph();
    }

    /**
     * Retrieves the data wrapper for this editor (read-only).
     */
    public DataWrapper getDataWrapper() {
        return this.dataWrapper;
    }

    public Knowledge getKnowledge() {
        return dataWrapper.getKnowledge();
    }

    public void setKnowledge(Knowledge knowledge) {
        dataWrapper.setKnowledge(knowledge);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("modelChanged".equals(evt.getPropertyName())) {
            firePropertyChange("modelChanged", null, null);
        }
    }

    //=============================PRIVATE METHODS======================//

    private static void removeEmptyModels(DataModelList dataModelList) {
        for (int i = dataModelList.size() - 1; i >= 0; i--) {
            DataModel dataModel = dataModelList.get(i);

            if (dataModel instanceof DataSet &&
                    ((DataSet) dataModel).getNumColumns() == 0) {
                if (dataModelList.size() > 1) {
                    dataModelList.remove(dataModel);
                }
            }
        }
    }

    private JTable getSelectedJTable() {
        Object display = tabbedPane().getSelectedComponent();

        if (display instanceof DataDisplay) {
            return ((DataDisplay) display).getDataDisplayJTable();
        } else if (display instanceof CovMatrixDisplay) {
            return ((CovMatrixDisplay) display).getCovMatrixJTable();
        }

        return null;
    }

    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        menuBar.add(file);

        JMenuItem fileItem = new JMenuItem(new LoadDataAction(this));
        file.add(fileItem);
        JMenuItem saveItem = new JMenuItem(new SaveDataAction(this));
        file.add(saveItem);
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));

        fileItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        saveItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        JMenu editMenu = new JMenu("Edit");

        JMenuItem clearCells = new JMenuItem("Clear Cells");
        final JMenuItem deleteRowsOrColumns =
                new JMenuItem("Delete Rows or Columns");
        JMenuItem copyCells = new JMenuItem("Copy Cells");
        JMenuItem cutCells = new JMenuItem("Cut Cells");
        JMenuItem pasteCells = new JMenuItem("Paste Cells");

        clearCells.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
        deleteRowsOrColumns.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        copyCells.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        cutCells.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        pasteCells.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        clearCells.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TabularDataJTable table =
                        (TabularDataJTable) getSelectedJTable();
                table.clearSelected();
            }
        });

        final ActionListener deleteActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TabularDataJTable tableTabular =
                        (TabularDataJTable) getSelectedJTable();

                if (!tableTabular.getRowSelectionAllowed() ||
                        !tableTabular.getColumnSelectionAllowed()) {
                    tableTabular.deleteSelected();
                }
            }
        };

        deleteRowsOrColumns.addActionListener(deleteActionListener);

        copyCells.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTable table = getSelectedJTable();
                Action copyAction = TransferHandler.getCopyAction();
                ActionEvent actionEvent = new ActionEvent(table,
                        ActionEvent.ACTION_PERFORMED, "copy");
                copyAction.actionPerformed(actionEvent);
            }
        });

        cutCells.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTable table = getSelectedJTable();
                Action cutAction = TransferHandler.getCutAction();
                ActionEvent actionEvent = new ActionEvent(table,
                        ActionEvent.ACTION_PERFORMED, "cut");
                cutAction.actionPerformed(actionEvent);
            }
        });

        pasteCells.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTable table = getSelectedJTable();
                Action pasteAction = TransferHandler.getPasteAction();
                ActionEvent actionEvent = new ActionEvent(table,
                        ActionEvent.ACTION_PERFORMED, "paste");
                pasteAction.actionPerformed(actionEvent);
            }
        });

        JCheckBoxMenuItem categoryNames =
                new JCheckBoxMenuItem("Show Category Names");
        JTable selectedJTable = getSelectedJTable();

        if (selectedJTable != null && selectedJTable instanceof TabularDataJTable) {
            TabularDataJTable tableTabular = (TabularDataJTable) selectedJTable;
            categoryNames.setSelected(tableTabular.isShowCategoryNames());
        }

        categoryNames.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTable selectedJTable = getSelectedJTable();
                TabularDataJTable tableTabular =
                        (TabularDataJTable) selectedJTable;
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
                tableTabular.setShowCategoryNames(source.isSelected());
            }
        });

        editMenu.add(clearCells);
        editMenu.add(deleteRowsOrColumns);
        editMenu.add(copyCells);
        editMenu.add(cutCells);
        editMenu.add(pasteCells);
        editMenu.addSeparator();
        editMenu.add(categoryNames);

        menuBar.add(editMenu);
        menuBar.add(new KnowledgeMenu(this));

        JMenu tools = new JMenu("Tools");
        menuBar.add(tools);

        tools.add(new HistogramAction(this));
        tools.add(new QQPlotAction(this));
        tools.add(new NormalityTestAction(this));
//        tools.add(new ConditionalIndependenceTestAction(this));
        tools.add(new CalculatorAction(this));

//        JMenuItem nonsingularityCheck = new JMenuItem("Check nonsingularity");
//
//        nonsingularityCheck.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                DataModel dataModel = dataWrapper.getSelectedDataModel();
//
//                if (dataModel instanceof CovarianceMatrix) {
//                    CovarianceMatrix dataSet = (CovarianceMatrix) dataModel;
//
//                    DoubleMatrix2D data = dataSet.getMatrix();
//
//                    System.out.println(data);
//
//                    LUDecomposition decomposition = new LUDecomposition(data);
//
//                    DoubleMatrix2D L = decomposition.getL();
//                    System.out.println(L);
//
////                        for (int i )
//
//                    boolean nonsingular = decomposition.isNonsingular();
//
////                        boolean nonsingular = true;
////
//                    try {
//                        DoubleMatrix2D b = new Algebra().mult(data, data.viewDice());
//                        new Algebra().inverse(b);
//                    } catch (Exception e1) {
//                        System.out.println("Could not invert");
//
////                            nonsingular = false;
//                    }
//
//                    if (nonsingular) {
//                        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                                "This dataset has full rank.");
//                        return;
//                    } else {
//                        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
//                                "This dataset has less than full rank.");
//                        return;
//                    }
//                }
//            }
//        });
//
//        if (getDataWrapper().getSelectedDataModel() instanceof CovarianceMatrix) {
//            tools.add(nonsingularityCheck);
//        }

        int vkBackSpace = KeyEvent.VK_BACK_SPACE;
        int vkDelete = KeyEvent.VK_DELETE;

        KeyStroke backspaceKeystroke = KeyStroke.getKeyStroke(vkBackSpace, 0);
        KeyStroke deleteKeystroke = KeyStroke.getKeyStroke(vkDelete, 0);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(backspaceKeystroke,
                "DELETE");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(deleteKeystroke,
                "DELETE");

        Action deleteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteActionListener.actionPerformed(null);
            }
        };

        getActionMap().put("DELETE", deleteAction);

        return menuBar;
    }

//    private void closeTab() {
//        int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
//                "Closing this tab will remove the data it contains. Continue?",
//                "Confirm", JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.WARNING_MESSAGE);
//
//        if (ret == JOptionPane.OK_OPTION) {
//            DataModel dataModel = getSelectedDataModel();
//
//            int index = tabbedPane.getSelectedIndex();
//            tabbedPane.removeTabAt(index);
//
//            tabbedPane().removeAll();
//            setPreferredSize(new Dimension(600, 400));
//
//            DataModelList dataModelList = getDataWrapper().getDataModelList();
//            dataModelList.remove(dataModel);
//
////            removeAll();
//            tabbedPane.removeAll();
//
//            for (int i = 0; i < dataModelList.size(); i++) {
//                Object _dataModel = dataModelList.get(i);
//                tabbedPane().addTab(tabName(_dataModel, i + 1),
//                        dataDisplay(_dataModel));
//            }
//
//            add(tabbedPane(), BorderLayout.CENTER);
//            add(menuBar(), BorderLayout.NORTH);
//            validate();
//        }
//    }

    private void closeTab(int index) {
        int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
                "Closing this tab will remove the data it contains. Continue?",
                "Confirm", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (ret == JOptionPane.OK_OPTION) {
            DataModel dataModel = getSelectedDataModel();

//            int index = tabbedPane.getSelectedIndex();
            tabbedPane.removeTabAt(index);

            tabbedPane().removeAll();
            setPreferredSize(new Dimension(600, 400));

            DataModelList dataModelList = dataWrapper.getDataModelList();
            dataModelList.remove(dataModel);

//            removeAll();
            tabbedPane.removeAll();

            for (int i = 0; i < dataModelList.size(); i++) {
                Object _dataModel = dataModelList.get(i);
                tabbedPane().addTab(tabName(_dataModel, i + 1),
                        dataDisplay(_dataModel));
            }

            add(tabbedPane(), BorderLayout.CENTER);
            add(menuBar(), BorderLayout.NORTH);
            validate();
        }
    }

    private static String tabName(Object dataModel, int i) {
        String tabName = ((DataModel) dataModel).getName();

        if (tabName == null) {
            tabName = "Data Set " + i;
        }

        return tabName;
    }

    /**
     * Returns the data display for the given model.
     */
    private JComponent dataDisplay(Object model) {
        if (model instanceof DataSet) {
            DataDisplay dataDisplay = new DataDisplay((DataSet) model);
            dataDisplay.addPropertyChangeListener(this);
            return dataDisplay;
        } else if (model instanceof CovarianceMatrix) {
            CovMatrixDisplay covMatrixDisplay = new CovMatrixDisplay((CovarianceMatrix) model);
            covMatrixDisplay.addPropertyChangeListener(this);
            return covMatrixDisplay;
        } else if (model instanceof TimeSeriesData) {
            return new TimeSeriesDataDisplay((TimeSeriesData) model);
        } else {
            throw new IllegalArgumentException("Unrecognized data type.");
        }
    }

    private JTabbedPane tabbedPane() {
        return tabbedPane;
    }
}

