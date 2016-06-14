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

package edu.cmu.tetrad.gene.graph;

import edu.cmu.tetrad.gene.history.LaggedFactor;

/**
 * Constructs as a (manual) update graph.
 */
public class ManualActiveLagGraph extends ActiveLagGraph {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS===========================//

    /**
     * Using the given parameters, constructs an BasicLagGraph.
     */
    public ManualActiveLagGraph() {
        addFactors("Gene", 1);
        setMaxLagAllowable(3);

        // Add edges one time step back.
        for (String s : getFactors()) {
            LaggedFactor laggedFactor = new LaggedFactor(s, 1);
            addEdge(s, laggedFactor);
        }
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ActiveLagGraph serializableInstance() {
        return new ManualActiveLagGraph();
    }
}


