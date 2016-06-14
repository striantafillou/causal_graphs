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
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.search.Bpc;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.BuildPureClustersParams;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MimParams;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class should access the getMappings mapped to it from the mapping to the
 * search classes. </p> This class is the parameter editor currently for
 * BuildPureClusters parameters
 *
 * @author Ricardo Silva rbas@cs.cmu.edu
 */

public class BuildPureClustersParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameter wrapper being viewed.
     */
    private BuildPureClustersParams params;
    private Object[] parentModels;

    /**
     * Opens up an editor to let the user view the given
     * BuildPureClustersRunner.
     */
    public BuildPureClustersParamsEditor() {
    }

    public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (BuildPureClustersParams) params;
    }

    public void setParentModels(Object[] parentModels) {
        if (parentModels == null) {
            throw new NullPointerException();
        }

        this.parentModels = parentModels;
    }

    public void setup() {
        DoubleTextField alphaField = new DoubleTextField(
                params.getAlpha(), 4, NumberFormatUtil.getInstance().getNumberFormat());
        alphaField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                try {
                    getParams().setAlpha(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        final String[] descriptions = Bpc.getTestDescriptions();
        JComboBox testSelector = new JComboBox(descriptions);
        testSelector.setSelectedIndex(getParams().getTetradTestType());

        testSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                int index = combo.getSelectedIndex();
                getParams().setTetradTestType(index);
            }
        });

        final String[] purifyDescriptions =
                Bpc.getPurifyTestDescriptions();
        JComboBox purifySelector = new JComboBox(purifyDescriptions);
        purifySelector.setSelectedIndex(getParams().getPurifyTestType());

        purifySelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                int index = combo.getSelectedIndex();
                getParams().setPurifyTestType(index);
            }
        });

        //Where is it setting the appropriate knowledge for the search?
        DataModel dataModel = null;

        for (Object parentModel : this.parentModels) {
            if (parentModel instanceof DataWrapper) {
                DataWrapper dataWrapper = (DataWrapper) parentModel;
                dataModel = dataWrapper.getSelectedDataModel();
            }
        }

        if (dataModel == null) {
            throw new IllegalStateException("Null data model.");
        }

        List<String> varNames =
                new ArrayList<String>(dataModel.getVariableNames());

        boolean isDiscreteModel;
        if (dataModel instanceof CovarianceMatrix) {
            isDiscreteModel = false;
        }
        else {
            DataSet dataSet = (DataSet) dataModel;
            isDiscreteModel = dataSet.isDiscrete();

        //            try {
        //                new DataSet((DataSet) dataModel);
        //                isDiscreteModel = true;
        //            }
        //            catch (IllegalArgumentException e) {
        //                isDiscreteModel = false;
        //            }
        }

        this.params.setVarNames(varNames);
        alphaField.setValue(this.params.getAlpha());

        Box b = Box.createVerticalBox();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Alpha:"));
        b1.add(Box.createHorizontalGlue());
        b1.add(alphaField);
        b.add(b1);

        if (!isDiscreteModel) {
            Box b2 = Box.createHorizontalBox();
            b2.add(new JLabel("Statistical Test:"));
            b2.add(Box.createHorizontalGlue());
            b2.add(testSelector);
            b.add(b2);

            Box b3 = Box.createHorizontalBox();
            b3.add(new JLabel("Purify Test:"));
            b3.add(Box.createHorizontalGlue());
            b3.add(purifySelector);
            b.add(b3);
        }
        else {
            this.params.setPurifyTestType(
                    Bpc.PURIFY_TEST_DISCRETE_LRT);
            this.params.setTetradTestType(Bpc.TEST_DISCRETE);
        }

        setLayout(new BorderLayout());
        add(b, BorderLayout.CENTER);
    }

    public boolean mustBeShown() {
        return false;
    }

    private MimParams getParams() {
        return params;
    }
}


