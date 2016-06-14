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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.editor.DataDisplay;
import edu.cmu.tetradapp.editor.TimeSeriesDataDisplay;
import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.model.GeneSimDataWrapper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * <p>Displays data objects in the tetrad.data package and allows users to edit
 * these objects, load and save these objects, and perform statistical analyses
 * on these objects.</p> </p> <p>Currently supported objects:</p> </p> <ul> <li>
 * DataSet <li> CovarianceMatrix. </ul> </p> <p>Multiple data models can be
 * displayed.</p>
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @see CovarianceMatrix
 */
public class GeneDataEditor extends JPanel implements PropertyChangeListener {

    /**
     * The data model being displayed.
     */
    private GeneSimDataWrapper dataWrapper;

    /**
     * A tabbed pane containing displays for all data models and displaying
     * 'dataModel' currently.
     */
    private JTabbedPane tabbedPane;

    private Map<Component, Object> compsToDataModels =
            new HashMap<Component, Object>();

    //==========================CONSTUCTORS===============================//

    /**
     * Constructs the data editor with an empty list of data displays.
     */
    public GeneDataEditor() {

    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Constructs a new DataEditor for the given Boolean Glass simulator.
     */
    public GeneDataEditor(GeneSimDataWrapper dataWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException("Data wrapper must not be null.");
        }

        this.dataWrapper = dataWrapper;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400));
        setName(getTitle());

        this.tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                getDataModelList().setSelectedModel(getSelectedDataSet());
            }
        });

        reset();
    }

    /**
     * Retrieves the data wrapper for this editor (read-only).
     */
    public GeneSimDataWrapper getDataWrapper() {
        return this.dataWrapper;
    }

    /**
     * Sets this editor to display contents of the given data model wrapper.
     */
    public final void reset() {
        constructTabbedPane();
        constructEditor();
    }

    private void constructTabbedPane() {
        clearTabbedPane();

        for (int index = 0; index < getDataModelList().size(); index++) {
            DataModel dataModel = (DataModel) getDataModelList().get(index);

            if (dataModel instanceof DataSet) {
                DataSet dataSet = (DataSet) dataModel;
                JComponent dataDisplay = new DataDisplay(dataSet);
                constructTab(dataModel, index, dataDisplay);

            }
            else if (dataModel instanceof TimeSeriesData) {
                TimeSeriesData timeSeriesData = (TimeSeriesData) dataModel;
                JComponent dataDisplay =
                        new TimeSeriesDataDisplay(timeSeriesData);
                constructTab(dataModel, index, dataDisplay);
            }
        }
    }

    private void constructEditor() {
        removeAll();
        add(tabbedPane, BorderLayout.CENTER);
        add(getMenuBar(), BorderLayout.NORTH);
        validate();
    }

    private void clearTabbedPane() {
        tabbedPane.removeAll();
    }

    private JMenuBar getMenuBar() {
        return new GeneDataEditorMenuBar(this);
    }

    private void constructTab(Object dataModel, int i, JComponent dataDisplay) {
        String tabName = ((DataModel) dataModel).getName();
        tabName = (tabName == null) ? "Data Set " + i : tabName;
        JScrollPane component = new JScrollPane(dataDisplay);
        compsToDataModels.put(component, dataModel);
        tabbedPane.addTab(tabName, component);
    }

    /**
     * Reacts to property change events.
     */
    public void propertyChange(PropertyChangeEvent e) {

        if ("closeFrame".equals(e.getPropertyName())) {
            firePropertyChange("closeFrame", null, null);
        }
        else if ("dataModel".equals(e.getPropertyName())) {
            firePropertyChange("dataModel", e.getOldValue(), e.getNewValue());
        }
    }

    /**
     * Sets the name of the editor.
     */
    public void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    /**
     * Returns the title of the data editor.
     */
    private String getTitle() {
        return "Gene Data Editor";
    }

    /**
     * Returns the display in the tabbed pane that's currently selected.
     */
    public Component getSelectedDisplay() {
        return tabbedPane.getSelectedComponent();
    }

    /**
     * Returns the data sets that's currently in front.
     */
    public DataModel getSelectedDataSet() {
        Component comp = tabbedPane.getSelectedComponent();
        return (DataModel) compsToDataModels.get(comp);
    }

    //==========================PRIVATE METHODS===========================//

    private DataModelList getDataModelList() {
        DataModel dataModel = dataWrapper.getDataModelList();

        if (dataModel instanceof DataModelList) {
            return (DataModelList) dataModel;
        }
        else {
            DataModelList dataModelList = new DataModelList();
            dataModelList.add(dataModel);
            return dataModelList;
        }
    }
}

/**
 * This is the menu for the editor editor.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
class GeneDataEditorMenuBar extends JMenuBar {

    /**
     * The DataEditor that this menuBar controls.
     */
    private GeneDataEditor editor;

    /**
     * Constructs a new data display menu.
     */
    public GeneDataEditorMenuBar(GeneDataEditor editor) {
        if (editor == null) {
            throw new NullPointerException("Data editor cannot be null.");
        }

        this.editor = editor;

        add(createFileMenu());
        add(createEditMenu());
    }

    /**
     * Creates the "file" menu and returns it.
     */
    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem saveDataItem = new JMenuItem("Save Data...");

        saveDataItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        menu.add(saveDataItem);

        return menu;
    }

    /**
     * Creates the "edit" menu and returns it.
     */
    private JMenu createEditMenu() {

        JMenu menu = new JMenu("Edit");
        JMenuItem copy = new JMenuItem("Copy (Tab Delimited)");
        JMenuItem copyAsJavaArray = new JMenuItem("Copy (Java Array)");
        JMenuItem removeDiscrete = new JMenuItem("Remove Discrete Columns");

        menu.add(copy);
        menu.add(copyAsJavaArray);
        menu.add(removeDiscrete);

        copy.addActionListener(new ActionListener() {

            /**
             * Performs the action of copying the data to the system clipboard.
             */
            public void actionPerformed(ActionEvent e) {
                doCopy();
            }
        });
        copyAsJavaArray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCopyAsJavaArray();
            }
        });
        removeDiscrete.addActionListener(new ActionListener() {

            /**
             * Produce a new data set in which the discrete columns have been
             * removed and add it as a new tabbed pane.
             *
             * @param e the action event.
             */
            public void actionPerformed(ActionEvent e) {
                removeDiscrete();
            }
        });

        return menu;
    }

    private void saveData() {
        Object dataSet = editor.getSelectedDataSet();

        if (dataSet instanceof DataSet) {
            saveDataSetData();
        }
        else if (dataSet instanceof TimeSeriesData) {
            saveTimeSeriesData();
        }
        else {
            throw new RuntimeException();
        }
    }

    // TODO!!! Refactor the data classes so that an arbitrary data set
    // can be saved out using a uniform method!!!! Note here that gene
    // data sets can time series data sets have to be saved out using
    // different methods. jdramsey 2/21/03

    /**
     * Method saveData
     */
    private void saveDataSetData() {
        File file = EditorUtils.getSaveFile("data", "dat", editor, false, "Save");

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(
                    "Output file could not be opened: " + file);
        }

        DataSet dataSet =
                (DataSet) editor.getSelectedDataSet();
        int numVars = dataSet.getNumColumns();
        int numData = dataSet.getNumRows();
        List<Node> variables = new ArrayList<Node>();

        for (int i = 0; i < numVars; i++) {
            Node v = dataSet.getVariable(i);

            variables.add(v);
            out.print(v.getName());

            if (i < numVars - 1) {
                out.print("\t");
            }
        }

        out.println();

        for (int j = 0; j < numData; j++) {
            for (int i = 0; i < numVars; i++) {
                if (variables.get(i) instanceof ContinuousVariable) {
                    out.print(nf.format(dataSet.getObject(j, i)));
                }
                else {
                    out.print(dataSet.getObject(j, i));
                }

                if (i < numVars - 1) {
                    out.print("\t");
                }
            }

            out.println();
        }

        out.close();
    }

    /**
     * Method saveData
     */
    private void saveTimeSeriesData() {
        File file = EditorUtils.getSaveFile("data", "dat", editor, false, "Save");

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(
                    "Output file could not be opened: " + file);
        }

        System.out.println(editor.getSelectedDataSet().getClass());

        TimeSeriesData dataSet = (TimeSeriesData) editor.getSelectedDataSet();
        int numVars = dataSet.getNumVars();
        int numData = dataSet.getNumTimePoints();
        List vars = dataSet.getVariableNames();

        for (int i = 0; i < numVars; i++) {
            out.print(vars.get(i));

            if (i < numVars - 1) {
                out.print("\t");
            }
        }

        out.println();

        for (int j = 0; j < numData; j++) {
            for (int i = 0; i < numVars; i++) {
                out.print(nf.format(dataSet.getDatum(j, i)));

                if (i < numVars - 1) {
                    out.print("\t");
                }
            }

            out.println();
        }

        out.close();
    }

    private void doCopy() {

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuffer sb = new StringBuffer();
        TimeSeriesData dataSet =
                (TimeSeriesData) this.editor.getSelectedDataSet();
        int numVars = dataSet.getNumVars();
        int numData = dataSet.getNumTimePoints();
        List vars = dataSet.getVariableNames();

        for (int i = 0; i < numVars; i++) {
            sb.append(vars.get(i));

            if (i < numVars - 1) {
                sb.append("\t");
            }
        }

        sb.append("\n");

        for (int j = 0; j < numData; j++) {
            for (int i = 0; i < numVars; i++) {
                sb.append(nf.format(dataSet.getDatum(j, i)));

                if (i < numVars - 1) {
                    sb.append("\t");
                }
            }

            sb.append("\n");
        }

        StringSelection strSel = new StringSelection(sb.toString());

        clip.setContents(strSel, strSel);

    }

    private void doCopyAsJavaArray() {

        NumberFormat nf = new DecimalFormat("0.0000;-0.0000");

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuffer sb = new StringBuffer();
        TimeSeriesData dataSet = (TimeSeriesData) editor.getSelectedDataSet();
        int numVars = dataSet.getNumVars();
        int numData = dataSet.getNumTimePoints();
        List vars = dataSet.getVariableNames();

        sb.append("\nString[] vars = {");
        for (int i = 0; i < numVars; i++) {
            sb.append("\"");
            sb.append(vars.get(i));
            sb.append("\"");

            if (i < numVars - 1) {
                sb.append(", ");
            }
        }
        sb.append("};");

        sb.append("\ndouble[][] data = {");
        for (int j = 0; j < numData; j++) {
            sb.append("\n\t{");
            for (int i = 0; i < numVars; i++) {
                sb.append(nf.format(dataSet.getDatum(j, i)));

                if (i < numVars - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");

            if (j < numData - 1) {
                sb.append(",");
            }
        }

        sb.append("\n};\n");

        StringSelection strSel = new StringSelection(sb.toString());

        clip.setContents(strSel, strSel);

    }

    private void removeDiscrete() {
        DataSet dataSet =
                (DataSet) editor.getSelectedDataSet();
        List<Node> variables = new LinkedList<Node>(dataSet.getVariables());

        for (Node variable1 : variables) {
            if (variable1 instanceof DiscreteVariable) {
                dataSet.removeColumn(variable1);
            }
        }

        this.editor.getDataWrapper().setDataModel(dataSet);
        this.editor.reset();
    }

}


