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

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetrad.session.SessionModel;

/**
 * Specifies the methods that all algorithm runners must implement. All
 * algorithm runners must know what their parameters are, must know what their
 * source graph is, and must know what their result graph is (if it has been
 * calculated).
 *
 * @author Joseph Ramsey
 */
public interface AlgorithmRunner extends SessionModel, Executable, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * Returns the data used to execute this algorithm. Might possibly be a
     * graph.
     */
    DataModel getDataModel();

    /**
     * Returns the search parameters for this algorithm.
     */
    SearchParams getParams();

    /**
     * Returns the graph from which data was originally generated, if such a
     * graph is available. Otherwise, returns null.
     */
    Graph getSourceGraph();

    /**
     * Returns the graph that results from executing the algorithm, if the
     * algorithm has been successfully executed.
     */
    Graph getResultGraph();

    /**
     * Executes the algorithm.
     */
    void execute();

    /**
     * Returns true if the algorithm supports knowledge.
     */
    boolean supportsKnowledge();

    /**
     * Returns the orientation rules for this search.
     */
    ImpliedOrientation getMeekRules();
}


