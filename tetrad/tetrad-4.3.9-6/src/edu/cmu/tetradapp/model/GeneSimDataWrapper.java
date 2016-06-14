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
import edu.cmu.tetrad.gene.history.*;
import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Wraps a data model so that a random sample will automatically be drawn on
 * construction from a Measurement Simulator object.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public class GeneSimDataWrapper extends DataWrapper
        implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public GeneSimDataWrapper(BooleanGlassGeneIm glassIm,
            MeasurementSimulatorParams simulator) {
        glassIm.setSimulator(simulator);
        setDataModel(glassIm.simulateData());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        ManualActiveLagGraph lagGraph = new ManualActiveLagGraph();

        lagGraph.addFactor("G1");
        lagGraph.addFactor("G2");
        lagGraph.addFactor("G3");

        // Initialize graph.
        GraphInitializer graphInitializer = new PreviousStepOnly();

        graphInitializer.initialize(lagGraph);
        lagGraph.addEdge("G2", new LaggedFactor("G1", 1));
        lagGraph.addEdge("G3", new LaggedFactor("G1", 1));
        lagGraph.addEdge("G3", new LaggedFactor("G2", 1));

        UpdateFunction updateFunction = new BooleanGlassFunction(lagGraph);
        BasalInitializer historyInitializer =
                new BasalInitializer(updateFunction, 0.0, 1.0);
        GeneHistory history =
                new GeneHistory(historyInitializer, updateFunction);
        //        MeasurementSimulator simulator = new MeasurementSimulator();

        MeasurementSimulatorParams params = new MeasurementSimulatorParams();
        params.setHistory(history);

        BooleanGlassGenePm pm = new BooleanGlassGenePm(lagGraph);
        BooleanGlassGeneIm im = new BooleanGlassGeneIm(pm);

        return new GeneSimDataWrapper(im, params);
    }
}


