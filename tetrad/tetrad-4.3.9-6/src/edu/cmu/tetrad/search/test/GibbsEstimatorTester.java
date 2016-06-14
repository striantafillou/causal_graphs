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

package edu.cmu.tetrad.search.test;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class GibbsEstimatorTester {

    public static void main(String[] args) {
        Graph leadGraph = new EdgeListGraph();

        Node LE = new GraphNode("LE");
        Node le = new GraphNode("le");
        Node iq = new GraphNode("iq");
        //Node leError = new GraphNode("leError");
        //Node iqError = new GraphNode("iqError");

        leadGraph.addNode(LE);
        leadGraph.addNode(le);
        leadGraph.addNode(iq);

        LE.setNodeType(NodeType.LATENT);
        le.setNodeType(NodeType.MEASURED);
        iq.setNodeType(NodeType.MEASURED);

        leadGraph.addDirectedEdge(LE, le);
        leadGraph.addDirectedEdge(LE, iq);
        //leadGraph.addBidirectedEdge(le, iq);    //Test of error cov

        SemPm semPm = new SemPm(leadGraph);
        SemGraph leadSemGraph = semPm.getGraph();

        Node leError = leadSemGraph.getErrorNode(le);
        Node iqError = leadSemGraph.getErrorNode(iq);

        System.out.println("Graph as retrieved from SemPm");
        System.out.println(semPm.getGraph());

        double[][] covariances = {{2.0, -0.2}, {-0.2, 1.04}};
        double[][] sampleCovars1 = {{1.9470, -0.2620}, {-0.2620, 0.8619}};
        DoubleMatrix2D sampleCovars = new DenseDoubleMatrix2D(sampleCovars1);
        String[] varNames = {"le", "iq"};

        SemIm semIm = new SemIm(semPm);

        //semIm.setParamValue(LE, le, 1.0);
        //semIm.setParamValue(LE, iq, -0.5);

        //Debug...  Test of error covariance parameter
        //Parameter ptest = semPm.getParameter(leError, iqError);
        //System.out.println("ptest = " + ptest);
        //semIm.setParamValue(leError, iqError, 0.333);

        Parameter varLE = semPm.getVarianceParameter(LE);
        varLE.setFixed(true);
        semIm.setParamValue(varLE, 1.0);

        Parameter varleError = semPm.getVarianceParameter(leError);
        varleError.setFixed(false);
        semIm.setParamValue(varleError, 1.0);

        Parameter variqError = semPm.getVarianceParameter(iqError);
        variqError.setFixed(false);
        semIm.setParamValue(variqError, 1.0);

        Parameter coefLEle = semPm.getCoefficientParameter(LE, le);
        coefLEle.setFixed(false);
        semIm.setParamValue(coefLEle, 1.0);

        Parameter coefLEiq = semPm.getCoefficientParameter(LE, iq);
        coefLEiq.setFixed(false);
        semIm.setParamValue(coefLEiq, -0.5);

        //DEBUG...  Test of error cov parameter.
        //Parameter errCoviqle = semPm.getParameter(leError, iqError);
        //errCoviqle.setFixed(false);

        int sampleSize = 100;
        CovarianceMatrix covMatrix =
                new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), sampleCovars, sampleSize);
        semIm.setCovMatrix(covMatrix);

        //RectangularDataSet dataSet = semIm.simulateData(sampleSize);

        //int nrows = dataSet.getDoubleData().rows();
        //int ncols = dataSet.getDoubleData().columns();
        //System.out.println("nrows = " + nrows + " " + "ncols = " + ncols);

        //DEBUG Print
        // for(int i = 0; i < nrows; i++) {
        //    System.out.println(dataSet.getDouble(i, 0) + " " + dataSet.getDouble(i, 1));
        //}

        //Parameters
        boolean flatPrior = true;
        double stretch = 2.0;
        int numIterations = 2000000;
        long seed = -3186053;

        /*
        System.out.println("Parameters of SEM in order:");
        List paramsList = semPm.getParameters();
        for(int i = 0; i < paramsList.size(); i++) {
            System.out.println((Parameter) paramsList.get(i));
        }
        */

        SemEstimatorGibbsParams params = new SemEstimatorGibbsParams(semIm,
                flatPrior, stretch, numIterations);

//        SemEstimatorGibbs estimator = new SemEstimatorGibbs(semPm, params);
//        estimator.estimate();
    }
}

