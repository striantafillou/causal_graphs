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

import edu.cmu.tetrad.gene.graph.ActiveLagGraph;
import edu.cmu.tetrad.gene.history.SimpleRandomizer;
import edu.cmu.tetrad.session.SessionModel;

/**
 * Constructs as a randomized update workbench.
 *
 * @author Joseph Ramsey
 */
public class RandomActiveLagGraph extends ActiveLagGraph
        implements SessionModel {
    static final long serialVersionUID = 23L;

    private String name;

    //===========================CONSTRUCTORS===========================//

    /**
     * Using the given parameters, constructs an BasicLagGraph that is
     * randomized upon construction.
     *
     * @param params an LagGraphParams object.
     */
    public RandomActiveLagGraph(LagGraphParams params) {

        addFactors("Gene", params.getVarsPerInd());

        int indegreeType;

        switch (params.getIndegreeType()) {
            case LagGraphParams.CONSTANT:
                indegreeType = SimpleRandomizer.CONSTANT;
                break;

            case LagGraphParams.MAX:
                indegreeType = SimpleRandomizer.MAX;
                break;

            case LagGraphParams.MEAN:
                indegreeType = SimpleRandomizer.MEAN;
                break;

            default :
                throw new IllegalArgumentException();
        }

        setMaxLagAllowable(params.getMlag());

        SimpleRandomizer randomizer = new SimpleRandomizer(params.getIndegree(),
                indegreeType, params.getMlag(), params.getPercentUnregulated());

        randomizer.initialize(this);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ActiveLagGraph serializableInstance() {
        return new RandomActiveLagGraph(LagGraphParams.serializableInstance());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


