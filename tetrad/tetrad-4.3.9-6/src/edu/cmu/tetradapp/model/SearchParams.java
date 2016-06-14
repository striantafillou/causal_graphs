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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeTransferable;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.search.IndTestType;

import java.util.List;

/**
 * Stores the parameters needed for (a variety of) search algorithms.
 *
 * @author Raul Salinas
 */
public interface SearchParams extends Params, KnowledgeTransferable {

    /**
     * Returns a copy of the knowledge for these params.
     */
    Knowledge getKnowledge();

    /**
     * Sets knowledge to a copy of the given object.
     */
    void setKnowledge(Knowledge knowledge);

    /**
     * Returns the independence test parameters for this search.
     */
    IndTestParams getIndTestParams();


    void setIndTestParams2(IndTestParams params);

    /**
     * Returns the list of variable names.
     */
    List<String> getVarNames();

    /**
     * Sets the list of variable names.
     */
    void setVarNames(List<String> varNames);

    /**
     * Returns a copy of the latest workbench graph.
     */
    Graph getSourceGraph();

    /**
     * Sets the latest workbench graph.
     */
    void setSourceGraph(Graph graph);

    /**
     * Sets the independence test type.
     */
    void setIndTestType(IndTestType testType);

    /**
     * Gets the independence test type.
     */
    IndTestType getIndTestType();
}


