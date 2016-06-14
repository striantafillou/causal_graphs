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
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Ricardo Silva
 */
final class ConvertToCorrMatixAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public ConvertToCorrMatixAction(DataEditor editor) {
        super("Correlation Matrix");

        if (editor == null) {
            throw new NullPointerException();
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    public void actionPerformed(ActionEvent e) {
        DataModel dataModel = getDataEditor().getSelectedDataModel();

        if (dataModel instanceof DataSet) {
            DataSet dataSet = (DataSet) dataModel;

            if (!(dataSet.isContinuous())) {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Must be a continuous data set " +
                                "or a covariance (or correlation) matrix.");
                return;
            }

            CorrelationMatrix corrMatrix = new CorrelationMatrix(dataSet);

            DataModelList list = new DataModelList();
            list.add(corrMatrix);
            getDataEditor().reset(list);
            getDataEditor().selectLastTab();
        }
        else if (dataModel instanceof CovarianceMatrix) {
            CovarianceMatrix covarianceMatrix = (CovarianceMatrix) dataModel;
            CorrelationMatrix corrMatrix =
                    new CorrelationMatrix(covarianceMatrix);

            DataModelList list = new DataModelList();
            list.add(corrMatrix);
            getDataEditor().reset(list);
            getDataEditor().selectLastTab();
        }
        else {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Must be a continuous data set " +
                            "or a covariance (or correlation) matrix.");
        }
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}


