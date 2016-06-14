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
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.MimBuild;
import edu.cmu.tetrad.search.MimUtils;
import edu.cmu.tetrad.search.indtest.IndTestMimBuild;
import edu.cmu.tetrad.sem.SemPm;

import java.util.prefs.Preferences;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the MIMBuild
 * algorithm.
 *
 * @author Ricardo Silva
 */
public class MimBuildRunner extends AbstractMimRunner implements GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS===========================//

    public MimBuildRunner(DataWrapper dataWrapper, MimBuildParams params) {
        super(dataWrapper, params);
        setClusters(params.getClusters());
    }
                                                    
    public MimBuildRunner(BuildPureClustersRunner pureClustersRunner,
            MimBuildParams params) {
        super(pureClustersRunner, params);
        setClusters(params.getClusters());
    }

    public MimBuildRunner(PurifyRunner runner, MimBuildParams params) {
        super(runner, params);
        setClusters(params.getClusters());
    }

    public MimBuildRunner(MimBuildRunner runner, MimBuildParams params) {
        super(runner, params);
        setClusters(params.getClusters());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MimBuildRunner serializableInstance() {
        DataSet dataSet = DataUtils.discreteSerializableInstance();
        DataWrapper dataWrapper = new DataWrapper(dataSet);
        return new MimBuildRunner(dataWrapper,
                MimBuildParams.serializableInstance());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() throws Exception {
        IndTestMimBuild test = getIndependenceTest();

        test.setAlgorithmType(getParams().getAlgorithmType());
        test.setSignificance(getParams().getAlpha());

        MimBuild mimBuildSearch =
                new MimBuild(test, getParams().getKnowledge());

        Graph searchGraph = mimBuildSearch.search();
        setResultGraph(searchGraph);

        Clusters clusters = MimUtils.convertToClusters(searchGraph);
        setClusters(clusters);

        setStructureGraph(MimUtils.extractStructureGraph(searchGraph));
    }

    //===========================PRIVATE METHODS==========================//

    /**
     * Returns an independence checker appropriate to the given data source.
     */
    private IndTestMimBuild getIndependenceTest() {
        if (getData() instanceof DataSet) {
            DataSet dataContinuous = (DataSet) getData();
            double alpha = Preferences.userRoot().getDouble("alpha", 0.05);
            return new IndTestMimBuild(dataContinuous, alpha,
                    getParams().getClusters());
        }
        else if (getData() instanceof CovarianceMatrix) {
            CovarianceMatrix data = (CovarianceMatrix) getData();
            double alpha = Preferences.userRoot().getDouble("alpha", 0.05);
            return new IndTestMimBuild(data, alpha, getParams().getClusters());
        }
        else {
            throw new IllegalStateException("Data source must be a " +
                    "continuous data set: " + getData().getClass());
        }
    }

    public Graph getGraph() {
        return getResultGraph();
    }

    public SemPm getSemPm() {
        Graph graph = super.getResultGraph();
        return new SemPm(graph);
    }
}


