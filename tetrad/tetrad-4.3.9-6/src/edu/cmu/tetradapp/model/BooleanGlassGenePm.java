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

import edu.cmu.tetrad.gene.graph.ManualActiveLagGraph;
import edu.cmu.tetrad.session.SessionModel;

/**
 * Implements a parametric model for Boolean Glass gene PM's, which in this case
 * just presents the underlying workbench. There are no additional parameters to
 * the PM.
 *
 * @author Joseph Ramsey
 */
public class BooleanGlassGenePm extends GenePm implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.n
     */
    private String name;

    //============================CONSTRUCTORS===============================//

    public BooleanGlassGenePm(ManualActiveLagGraph lagGraph) {
        super(lagGraph);
    }

    public BooleanGlassGenePm(RandomActiveLagGraph lagGraph) {
        super(lagGraph);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BooleanGlassGenePm serializableInstance() {
        return new BooleanGlassGenePm(
                (ManualActiveLagGraph) ManualActiveLagGraph.serializableInstance());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


