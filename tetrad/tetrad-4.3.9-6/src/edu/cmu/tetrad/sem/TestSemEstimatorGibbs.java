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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;

public class TestSemEstimatorGibbs {

    public static void main(String[] args) {
        Graph leadGraph = new EdgeListGraph();

//        Node LE = new GraphNode("LE");
//        Node le = new GraphNode("le");
//        Node iq = new GraphNode("iq");
//
//        LE.setNodeType(NodeType.LATENT);
//        le.setNodeType(NodeType.MEASURED);
//        iq.setNodeType(NodeType.MEASURED);
//
//        leadGraph.addNode(LE);
//        leadGraph.addNode(le);
//        leadGraph.addNode(iq);
//
//        leadGraph.addDirectedEdge(LE, le);
//        leadGraph.addDirectedEdge(LE, iq);
//
//        SemPm semPm = new SemPm(leadGraph);

//        System.out.println("Graph as retrieved from SemPm");
//        System.out.println(semPm.getGraph());

        int sampleSize = 1000;
        Graph graph = GraphUtils.randomDag(5, 0, 3, 3, 3, 3, false);
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        DataSet data = semIm.simulateData(1000, false);
        CovarianceMatrix covMatrix = new CovarianceMatrix(data);
        double[][] sampleCovars1 = covMatrix.getMatrix().toArray();

//        double[][] sampleCovars1 = {{1.9470, -0.2620}, {-0.2620, 0.8619}};
//        DoubleMatrix2D sampleCovars = new DenseDoubleMatrix2D(sampleCovars1);
//        String[] varNames = {"le", "iq"};
//
//        SemIm semIm = new SemIm(semPm);
//
//        Parameter varLE = semPm.getVarianceParameter(LE);
//        varLE.setFixed(true);
//        semIm.setParamValue(varLE, 1.0);
//
//        Parameter coefLEle = semPm.getCoefficientParameter(LE, le);
//        coefLEle.setFixed(false);
//        semIm.setParamValue(coefLEle, 1.0);
//
//        Parameter coefLEiq = semPm.getCoefficientParameter(LE, iq);
//        coefLEiq.setFixed(false);
//        semIm.setParamValue(coefLEiq, -0.2);

//        int sampleSize = 100;
//        CovarianceMatrix covMatrix =
//                new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), sampleCovars, sampleSize);
//        semIm.setCovMatrix(covMatrix);

		//Parameters
        boolean flatPrior = true;
        double stretch = 2.0;
        int numIterations = 1000;

//		SemEstimatorGibbsParams params = new SemEstimatorGibbsParams(semIm,
//			flatPrior, stretch, numIterations);

        SemEstimatorGibbs gibbsEstimator = new SemEstimatorGibbs(semPm, semIm, sampleCovars1, flatPrior, stretch, numIterations);

        gibbsEstimator.estimate();

        System.out.println("\n\nTest using standard sem estimation.");
//        Graph graph = constructGraph1();
        SemPm semPm2 = new SemPm(leadGraph);
 //       CovarianceMatrix covMatrix = constructCovMatrix1();
        SemEstimator estimator = new SemEstimator(covMatrix, semPm2);
        System.out.println();
        System.out.println("... Before:");
        System.out.println(estimator);
        estimator.estimate();
		System.out.println();
        System.out.println("... After:");
        System.out.println(estimator);

		System.out.println("Calculating Richard's Score");
		System.out.println();

		DoubleMatrix2D coef1 = estimator.getEstimatedSem().getEdgeCoef();
		DoubleMatrix2D coef2 = gibbsEstimator.getEstimatedSem().getEdgeCoef();
		double score = 0.0;
		for (int i = 0; i < coef1.size(); i++){
			double c1 = coef1.get(i%coef1.rows(), (int) i/coef1.rows());
			double c2 = coef2.get(i%coef2.rows(), (int) i/coef2.rows());
			double val = (c1-c2);
			score += val*val;
		}

		System.out.println(coef1);
		System.out.println();
		System.out.println(coef2);
		System.out.println();
		System.out.println("score: "+score);

	}

}
