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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizer;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a SemEstimator for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 */
public class SemEstimatorWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private SemEstimator semEstimator;

    //==============================CONSTRUCTORS==========================//

    /**
     * Private constructor for serialization only. Problem is, for the real
     * constructors, I'd like to call the degrees of freedom check, which
     * pops up a dialog. This is irritating when running unit tests.
     * jdramsey 8/29/07
     */
    private SemEstimatorWrapper(DataSet dataSet, SemPm semPm) {
        this.semEstimator = new SemEstimator(dataSet, semPm);
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemPmWrapper semPmWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException("Data wrapper must not be null.");
        }

        if (semPmWrapper == null) {
            throw new NullPointerException(
                    "OldSem PM Wrapper must not be null.");
        }

        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("Starting estimation!");
        }

        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (dataModel instanceof DataSet) {
            //            checkVarNameEquality(dataModel, semPmWrapper.getSemPm());
            DataSet dataSet =
                    (DataSet) dataWrapper.getSelectedDataModel();
            SemPm semPm = semPmWrapper.getSemPm();
            this.semEstimator = new SemEstimator(dataSet, semPm);
            if (!degreesOfFreedomCheck(semPm)) return;
            this.semEstimator.estimate();
        } else if (dataModel instanceof CovarianceMatrix) {
            //            checkVarNameEquality(dataModel, semPmWrapper.getSemPm());
            CovarianceMatrix covMatrix = (CovarianceMatrix) dataModel;
            SemPm semPm = semPmWrapper.getSemPm();
            this.semEstimator = new SemEstimator(covMatrix, semPm);
            if (!degreesOfFreedomCheck(semPm)) return;
            this.semEstimator.estimate();
        } else {
            throw new IllegalArgumentException("Data wrapper must be " +
                    "either a DataSet or a CovarianceMatrix.");
        }

        if (config != null) {
            TetradLogger.getInstance().reset();
        }
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemImWrapper semImWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (semImWrapper == null) {
            throw new NullPointerException();
        }

        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("Starting estimation!");
        }

        
        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        SemPm semPm = semImWrapper.getSemIm().getSemPm();

        this.semEstimator = new SemEstimator(dataSet, semPm);
        if (!degreesOfFreedomCheck(semPm)) return;
        this.semEstimator.estimate();

        if (config != null) {
            TetradLogger.getInstance().reset();
        }

    }

    private boolean degreesOfFreedomCheck(SemPm semPm) {
        if (semPm.getDof() < 1) {
            int ret = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(),
                    "This model has nonpositive degrees of freedom (DOF = " +
                            semPm.getDof() + "). " +
                            "\nEstimation will be uninformative. Are you sure you want to proceed?",
                    "Please confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ret != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return true;
    }

    public SemEstimatorWrapper(DataWrapper dataWrapper,
                               SemPmWrapper semPmWrapper,
                               SemImWrapper semImWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (semPmWrapper == null) {
            throw new NullPointerException();
        }

        if (semImWrapper == null) {
            throw new NullPointerException();
        }

        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("Starting estimation!");
        }

        DataSet dataSet =
                (DataSet) dataWrapper.getSelectedDataModel();
        SemPm semPm = semPmWrapper.getSemPm();
        SemIm semIm = semImWrapper.getSemIm();

        this.semEstimator = new SemEstimator(dataSet, semPm);
        if (!degreesOfFreedomCheck(semPm)) return;
        this.semEstimator.setTrueSemIm(semIm);
        this.semEstimator.estimate();

        if (config != null) {
            TetradLogger.getInstance().reset();
        }
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemEstimatorWrapper serializableInstance() {
        DataSet dataSet = DataUtils.continuousSerializableInstance();
        return new SemEstimatorWrapper(dataSet, SemPm.serializableInstance());
    }

    //============================PUBLIC METHODS=========================//

    public SemEstimator getSemEstimator() {
        return this.semEstimator;
    }

    public void setSemEstimator(SemEstimator semEstimator) {
        this.semEstimator = semEstimator;
    }

    public SemIm getEstimatedSemIm() {
        return semEstimator.getEstimatedSem();
    }

    public SemOptimizer getSemOptimizer() {
        return semEstimator.getSemOptimizer();
    }

    public Graph getGraph() {
        return semEstimator.getEstimatedSem().getSemPm().getGraph();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //=============================== Private methods =======================//


    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (semEstimator == null) {
            throw new NullPointerException();
        }
    }
}


