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

import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.indtest.IndTestMimBuild;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Stores the parameters needed for the MimBuild search and wizard.
 *
 * @author Ricardo Silva
 */
public final class MimBuildParams implements MimParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private MimBuildIndTestParams indTestParams;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Cannot be null.
     */
    private Clusters clusters = new Clusters();

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //===========================CONSTRUCTORS=============================//

    public MimBuildParams() {
        indTestParams = new MimBuildIndTestParams(0.05, 1,
                IndTestMimBuild.MIMBUILD_MLE, this);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MimBuildParams serializableInstance() {
        return new MimBuildParams();
    }

    //===========================PUBLIC METHODS==========================//

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public Clusters getClusters() {
        return this.clusters;
    }

    public void setClusters(Clusters clusters) {
        if (clusters == null) {
            throw new NullPointerException();
        }
        this.clusters = clusters;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

    public MimIndTestParams getMimIndTestParams() {
        return this.indTestParams;
    }

    public List getVarNames() {
        return this.varNames;
    }

    public void setVarNames(List varNames) {
        this.varNames = varNames;
    }

    public Graph getSourceGraph() {
        return this.sourceGraph;
    }

    public void setSourceGraph(Graph graph) {
        this.sourceGraph = graph;
    }

    public double getAlpha() {
        return indTestParams.getAlpha();
    }

    public void setAlpha(double alpha) {
        indTestParams.setAlpha(alpha);
    }

    public int getAlgorithmType() {
        return indTestParams.getAlgorithmType();
    }

    public void setAlgorithmType(int tt) {
        if (tt == IndTestMimBuild.MIMBUILD_GES_ABIC ||
                tt == IndTestMimBuild.MIMBUILD_GES_SBIC ||
                tt == IndTestMimBuild.MIMBUILD_PC) {
            indTestParams.setAlgorithmType(tt);
        }
    }

    public int getTetradTestType() {
        throw new UnsupportedOperationException();
    }

    public void setTetradTestType(int tetradTestType) {
        throw new UnsupportedOperationException();
    }

    public int getPurifyTestType() {
        throw new UnsupportedOperationException();
    }

    public void setPurifyTestType(int purifyTestType) {
        throw new UnsupportedOperationException();
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

        if (indTestParams == null) {
            throw new NullPointerException();
        }

        if (knowledge == null) {
            throw new NullPointerException();
        }

        if (clusters == null) {
            throw new NullPointerException();
        }
    }
}


