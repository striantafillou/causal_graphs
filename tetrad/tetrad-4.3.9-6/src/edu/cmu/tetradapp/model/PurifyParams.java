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
import edu.cmu.tetrad.search.Purify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Stores the parameters needed for the Purify search and wizard.
 *
 * @author Ricardo Silva rbas@cs.cmu.edu
 */

public final class PurifyParams implements MimParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Cannot be null.
     */
    private Clusters clusters = new Clusters();

    /**
     * @serial Cannot be null.
     */
    private PurifyIndTestParams indTestParams;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //============================CONSTRUCTORS==========================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */

    public PurifyParams() {
        indTestParams = new PurifyIndTestParams(0.05, 1,
                Purify.TEST_GAUSSIAN_SCORE, this);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PurifyParams serializableInstance() {
        return new PurifyParams();
    }

    //============================PUBLIC METHODS=========================//

    /**
     * indTestParams is not in use yet
     */
    public MimIndTestParams getMimIndTestParams() {
        return indTestParams;
    }

    public void setIndTestParams(IndTestParams indTestParams) {
        if (!(indTestParams instanceof PurifyIndTestParams)) {
            throw new IllegalArgumentException(
                    "Illegal IndTestParams in PurifyParams");
        }
        this.indTestParams = (PurifyIndTestParams) indTestParams;
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

    /**
     * Sets a new knowledge for the algorithm. Not in use yet.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

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

    /**
     * Gets the significance level for the search.
     */
    public double getAlpha() {
        return indTestParams.getAlpha();
    }

    /**
     * Sets the significance level for the search.
     */
    public void setAlpha(double alpha) {
        indTestParams.setAlpha(alpha);
    }

    /**
     * Gets the type of significance test.
     */
    public int getTetradTestType() {
        return indTestParams.getTetradTestType();
    }

    /**
     * Sets the type of significance test.
     */
    public void setTetradTestType(int tetradTestType) {
        indTestParams.setTetradTestType(tetradTestType);
    }

    public int getPurifyTestType() {
        throw new UnsupportedOperationException();
    }

    public void setPurifyTestType(int purifyTestType) {
        throw new UnsupportedOperationException();
    }

    public int getAlgorithmType() {
        throw new UnsupportedOperationException();
    }

    public void setAlgorithmType(int tt) {
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

        if (knowledge == null) {
            throw new NullPointerException();
        }

        if (clusters == null) {
            throw new NullPointerException();
        }

        if (indTestParams == null) {
            throw new NullPointerException();
        }
    }
}


