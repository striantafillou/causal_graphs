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
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetradapp.model.SemEstimatorWrapper;
import edu.cmu.tetradapp.util.WatchedProcess;
import edu.cmu.tetradapp.util.LayoutEditable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Lets the user interact with a SEM estimator.
 *
 * @author Joseph Ramsey
 */
public final class SemEstimatorEditor extends JPanel {
    private static SemEstimatorWrapper wrapper;
    private JPanel editorPanel;
    private JComboBox optimizerCombo;

    public SemEstimatorEditor(SemEstimatorWrapper _wrapper) {
        wrapper = _wrapper;
        editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(editorPanel, BorderLayout.CENTER);

        optimizerCombo = new JComboBox();
        optimizerCombo.addItem("Regression");
        optimizerCombo.addItem("EM");
        optimizerCombo.addItem("CDS");
        optimizerCombo.addItem("Random Search");
//        optimizerCombo.addItem("Powell");
//        optimizerCombo.addItem("Uncmin");

        optimizerCombo.setMaximumSize(new Dimension(200, 25));

        SemOptimizer optimizer = wrapper.getSemOptimizer();
        String selection;

        if (optimizer instanceof SemOptimizerRegression) {
            selection = "Regression";
        }
        else if (optimizer instanceof SemOptimizerEm) {
            selection = "EM";
        }
        else if (optimizer instanceof SemOptimizerScattershot) {
            selection = "Random Search";
        }
        else if (optimizer instanceof SemOptimizerPalCds) {
            selection = "CDS";
        }
        else if (optimizer instanceof SemOptimizerNrPowell) {
            selection = "Powell";
        }
        else if (optimizer instanceof SemOptimizerUncmin) {
            selection = "Uncmin";
        }
        else {
            selection = "CDS";
        }

        optimizerCombo.setSelectedItem(selection);

        JButton estimateButton = new JButton("Estimate Again");

        estimateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 Window owner = (Window) getTopLevelAncestor();

                 new WatchedProcess(owner) {
                    public void watch() {
                        reestimate();
                    }
                 };
            }
        });

        Box lowerBar = Box.createHorizontalBox();
        lowerBar.add(Box.createHorizontalGlue());
        lowerBar.add(new JLabel("Choose Optimizer:  "));
        lowerBar.add(optimizerCombo);
        lowerBar.add(Box.createHorizontalStrut(10));
        lowerBar.add(estimateButton);
        
        add(lowerBar, BorderLayout.SOUTH);

        resetSemImEditor();
    }

    private void reestimate() {
        SemOptimizer optimizer;
        Object type = optimizerCombo.getSelectedItem();

        if ("Regression".equals(type)) {
            optimizer = new SemOptimizerRegression();
        }
        else if ("EM".equals(type)) {
            optimizer = new SemOptimizerEm();
        }
        else if ("CDS".equals(type)) {
            optimizer = new SemOptimizerPalCds();
        }
        else if ("Random Search".equals(type)) {
            optimizer = new SemOptimizerScattershot();
        }
        else if ("Powell".equals(type)) {
            optimizer = new SemOptimizerNrPowell();
        }
        else if ("Uncmin".equals(type)) {
            optimizer = new SemOptimizerUncmin();
        }
        else {
            throw new IllegalArgumentException("Unexpected optimizer " +
                    "type: " + type);
        }

        SemEstimator estimator = wrapper.getSemEstimator();
        SemPm semPm = estimator.getSemPm();

        DataSet dataSet = estimator.getDataSet();
        CovarianceMatrix covMatrix = estimator.getCovMatrix();

        SemEstimator newEstimator;

        if (dataSet != null) {
            newEstimator = new SemEstimator(dataSet, semPm, optimizer);
        }
        else if (covMatrix != null) {
            newEstimator = new SemEstimator(covMatrix, semPm, optimizer);
        }
        else {
            throw new IllegalStateException("Only continuous " +
                    "rectangular data sets or covariance matrices " +
                    "can be processed.");
        }

        

        newEstimator.estimate();
        wrapper.setSemEstimator(newEstimator);
        resetSemImEditor();
    }

    private void resetSemImEditor() {
        SemIm estimatedSem = wrapper.getEstimatedSemIm();
        SemImEditor editor = new SemImEditor(estimatedSem);

        editorPanel.removeAll();
        editorPanel.add(editor, BorderLayout.CENTER);
        editorPanel.revalidate();
        editorPanel.repaint();
    }
}