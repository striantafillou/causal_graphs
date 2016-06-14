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

import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.prefs.Preferences;

/**
 * Stores parameter values for generating random tetrad-style graphs.
 */
public class GraphParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * The initialization mode in which probability values in tables are
     * retained where possible and otherwise filled in manually.
     */
    public static final int MANUAL = 0;

    /**
     * The initialization mode in which probability values in tables are
     * retained where possible and otherwise filled in manually.
     */
    public static final int RANDOM = 1;

    /**
     * The initialization mode, either MANUAL or AUTOMATIC.
     *
     * @serial MANUAL or AUTOMATIC.
     */
    private int initializationMode = MANUAL;

    //==============================CONSTRUCTOR=========================//

    /**
     * Blank constructor--no parents are permitted.
     */
    public GraphParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GraphParams serializableInstance() {
        return new GraphParams();
    }

    //===============================PUBLIC METHODS=====================//

    public int getNumNodes() {
        return Preferences.userRoot().getInt("newGraphNumNodes", 5);
    }

    public void setNumNodes(int numNodes) {
        if (numNodes < 1) {
            throw new IllegalArgumentException("Number of nodes must be >= 1.");
        }

        if (numNodes < getNumLatents()) {
            throw new IllegalArgumentException(
                    "Number of nodes must be >= number " + "of latent nodes.");
        }

        Preferences.userRoot().putInt("newGraphNumNodes", numNodes);

        setMaxEdges(getMaxEdges());

        if (isConnected()) {
            setMaxEdges(numNodes);
        }
    }

    public int getNumLatents() {
        return Preferences.userRoot().getInt("newGraphNumLatents", 0);
    }

    public void setNumLatents(int numLatentNodes) {
        if (numLatentNodes < 0) {
            throw new IllegalArgumentException(
                    "Number of latent nodes must be" + " >= 0: " +
                            numLatentNodes);
        }

        if (numLatentNodes > getNumNodes()) {
            throw new IllegalArgumentException(
                    "Number of latent nodes must be " + "<= number of nodes.");
        }

        Preferences.userRoot().putInt("newGraphNumLatents", numLatentNodes);
    }

    public int getMaxEdges() {
        return Preferences.userRoot().getInt("newGraphNumEdges", 3);
    }


    public void setMaxEdges(int numEdges) {
        if (isConnected() && numEdges < getNumNodes()) {
            throw new IllegalArgumentException("When assuming connectedness, " +
                    "the number of edges must be at least the number of nodes.");
        }

        if (!isConnected() && numEdges < 0) {
            throw new IllegalArgumentException(
                    "Number of edges must be >= 0: " + numEdges);
        }

        int maxNumEdges = getNumNodes() * (getNumNodes() - 1) / 2;

        if (numEdges > maxNumEdges) {
            numEdges = maxNumEdges;
        }

        Preferences.userRoot().putInt("newGraphNumEdges", numEdges);
    }

    public int getInitializationMode() {
        return initializationMode;
    }

    public void setInitializationMode(int initializationMode) {
        switch (initializationMode) {
            case MANUAL:
                // Falls through.
            case RANDOM:
                break;
            default:
                throw new IllegalStateException(
                        "Illegal initialization mode: " + initializationMode);
        }

        this.initializationMode = initializationMode;
    }

    public int getMaxDegree() {
        return Preferences.userRoot().getInt("randomGraphMaxDegree", 6);
    }

    public void setMaxDegree(int maxDegree) {
        if (!isConnected() && maxDegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxDegree", 1);
            return;
        }

        if (isConnected() && maxDegree < 3) {
            Preferences.userRoot().putInt("randomGraphMaxDegree", 3);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxDegree", maxDegree);
    }

    public int getMaxIndegree() {
        return Preferences.userRoot().getInt("randomGraphMaxIndegree", 3);
    }

    public void setMaxIndegree(int maxIndegree) {
        if (!isConnected() && maxIndegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxIndegree", 1);
            return;
        }

        if (isConnected() && maxIndegree < 2) {
            Preferences.userRoot().putInt("randomGraphMaxIndegree", 2);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxIndegree", maxIndegree);
    }

    public int getMaxOutdegree() {
        return Preferences.userRoot().getInt("randomGraphMaxOutdegree", 3);
    }

    public void setMaxOutdegree(int maxOutDegree) {
        if (!isConnected() && maxOutDegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxOutdegree", 1);
            return;
        }

        if (isConnected() && maxOutDegree < 2) {
            Preferences.userRoot().putInt("randomGraphMaxOutdegree", 2);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxOutdegree", maxOutDegree);
    }

    public void setConnected(boolean connected) {
        Preferences.userRoot().putBoolean("randomGraphConnected", connected);

        if (connected) {
            if (getMaxIndegree() < 2) {
                setMaxIndegree(2);
            }

            if (getMaxOutdegree() < 2) {
                setMaxOutdegree(2);
            }

            if (getMaxDegree() < 3) {
                setMaxDegree(3);
            }

            if (getMaxEdges() < getNumNodes()) {
                setMaxEdges(getNumNodes());
            }
        }
    }

    public boolean isConnected() {
        return Preferences.userRoot().getBoolean("randomGraphConnected", false);
    }

    public boolean isUniformlySelected() {
        return Preferences.userRoot().getBoolean("graphUniformlySelected", true);
    }

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

        switch (initializationMode) {
            case MANUAL:
                // Falls through.
            case RANDOM:
                break;
            default:
                throw new IllegalStateException(
                        "Illegal initialization mode: " + initializationMode);
        }
    }
}


