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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.indtest.*;

/**
 * Chooses an independence test for a particular data source.
 *
 * @author Joseph Ramsey
 */
public final class IndTestFactory {
    public IndependenceTest getTest(Object dataSource, SearchParams params) {
        return getTest(dataSource, params, IndTestType.DEFAULT);
    }

    /**
     * Returns an independence checker appropriate to the given data source.
     * Also sets the IndTestParams on the params to an appropriate type object
     * (using the existing one if it's of the right type).
     */
    public IndependenceTest getTest(Object dataSource, SearchParams params,
            IndTestType testType) {
        if (dataSource == null) {
            throw new NullPointerException();
        }
        if (params == null) {
            throw new NullPointerException();
        }

        IndTestParams indTestParams = params.getIndTestParams();
        if (indTestParams == null) {
            indTestParams = new BasicIndTestParams();
            params.setIndTestParams2(indTestParams);
        }

        if (dataSource instanceof DataSet) {
            DataSet dataSet = (DataSet) dataSource;

            if (dataSet.isContinuous() || dataSet.getNumColumns() == 0) {
                DataSet dataContinuous =
                        (DataSet) dataSource;

                if (dataContinuous.isMulipliersCollapsed()) {
                    dataContinuous = new CaseExpander().filter(dataSet);
                }

                return getContinuousTest(dataContinuous, params, testType);
            }
            if (dataSet.isDiscrete()) {
                DataSet dataDiscrete =
                        (DataSet) dataSource;

                if (dataDiscrete.isMulipliersCollapsed()) {
                    dataDiscrete = new CaseExpander().filter(dataSet);
                }

                return getDiscreteTest(dataDiscrete, params, testType);
            }
        }
        if (dataSource instanceof Graph) {
            return getGraphTest((Graph) dataSource, params,
                    IndTestType.D_SEPARATION);
        }
        if (dataSource instanceof CovarianceMatrix) {
            return getCovMatrixTest((CovarianceMatrix) dataSource, params,
                    testType);
        }
//        if (dataSource instanceof CorrelationMatrix) {
//            return getCorrMatrixTest((CovarianceMatrix) dataSource, params,
//                    testType);
//        }

        if (dataSource instanceof TimeSeriesData) {
            return timeSeriesTest((TimeSeriesData) dataSource, params);
        }

        throw new IllegalStateException(
                "Unrecognized data source type: " + dataSource.getClass());
    }

    private IndependenceTest getContinuousTest(DataSet dataSet,
            SearchParams params, IndTestType testType) {
        IndTestParams indTestParams = params.getIndTestParams();

        if (IndTestType.CORRELATION_T == testType) {
            return new IndTestCramerT(dataSet, indTestParams.getAlpha());
        }
        if (IndTestType.FISHER_Z == testType) {
            return new IndTestFisherZ(dataSet, indTestParams.getAlpha());
        }
        if (IndTestType.FISHER_ZD == testType) {
            return new IndTestFisherZD(dataSet, indTestParams.getAlpha());
        }
        if (IndTestType.FISHER_Z_BOOTSTRAP == testType) {
            return new IndTestFisherZBootstrap(dataSet, indTestParams.getAlpha(), 15, dataSet.getNumRows() / 2);
        }
        if (IndTestType.LINEAR_REGRESSION == testType) {
            return new IndTestRegression(dataSet,
                    indTestParams.getAlpha());
        }
        else {
            params.setIndTestType(IndTestType.CORRELATION_T);
            return new IndTestCramerT(dataSet, indTestParams.getAlpha());
        }
    }

    private IndependenceTest getDiscreteTest(DataSet dataDiscrete,
            SearchParams params, IndTestType testType) {
        IndTestParams indTestParams = params.getIndTestParams();

        if (IndTestType.G_SQUARE == testType) {
            return new IndTestGSquare(dataDiscrete, indTestParams.getAlpha());
        }
        if (IndTestType.CHI_SQUARE == testType) {
            return new IndTestChiSquare(dataDiscrete, indTestParams.getAlpha());
        }
        else {
            params.setIndTestType(IndTestType.CHI_SQUARE);
            return new IndTestChiSquare(dataDiscrete, indTestParams.getAlpha());
        }
    }

    private IndependenceTest getGraphTest(Graph data, SearchParams params,
            IndTestType testType) {
        if (IndTestType.D_SEPARATION == testType) {
            return new IndTestDSep(data);
        }
        else {
            params.setIndTestType(IndTestType.D_SEPARATION);
            return new IndTestDSep(data);
        }
    }

    private IndependenceTest getCovMatrixTest(CovarianceMatrix covMatrix,
            SearchParams params, IndTestType testType) {
        if (IndTestType.CORRELATION_T == testType) {
            return new IndTestCramerT(covMatrix,
                    params.getIndTestParams().getAlpha());
        }
        if (IndTestType.FISHER_Z == testType) {
            return new IndTestFisherZ(covMatrix,
                    params.getIndTestParams().getAlpha());
        }
        else {
            params.setIndTestType(IndTestType.CORRELATION_T);
            return new IndTestCramerT(covMatrix,
                    params.getIndTestParams().getAlpha());
        }
    }

//    private IndependenceTest getCorrMatrixTest(CovarianceMatrix covMatrix,
//            SearchParams params, IndTestType testType) {
//        if (IndTestType.CORRELATION_T == testType) {
//            return new IndTestCramerT(covMatrix,
//                    params.getIndTestParams().getAlpha());
//        }
//        if (IndTestType.FISHER_Z == testType) {
//            return new IndTestFisherZ(covMatrix,
//                    params.getIndTestParams().getAlpha());
//        }
//        else {
//            params.setIndTestType(IndTestType.CORRELATION_T);
//            return new IndTestCramerT(covMatrix,
//                    params.getIndTestParams().getAlpha());
//        }
//    }

    private IndependenceTest timeSeriesTest(TimeSeriesData data,
            SearchParams params) {
        IndTestParams indTestParams = params.getIndTestParams();
        if (!(indTestParams instanceof LagIndTestParams) || !(
                getOldNumTimePoints(indTestParams) == data.getNumTimePoints()))
        {
            indTestParams = new LagIndTestParams();
            ((LagIndTestParams) indTestParams).setNumTimePoints(
                    data.getData().rows());
            params.setIndTestParams2(indTestParams);
        }
        IndTestTimeSeries test =
                new IndTestTimeSeries(data.getData(), data.getVariables());
        test.setAlpha(indTestParams.getAlpha());
        test.setNumLags(((LagIndTestParams) indTestParams).getNumLags());
        return test;
    }

    /**
     * Returns an independence checker appropriate to the given data source.
     * Also sets the IndTestParams on the params to an appropriate type
     * dataSource (using the existing one if it's of the right type).
     */
    public void adjustIndTestParams(Object dataSource, SearchParams params) {
        if (dataSource instanceof DataSet) {
            DataSet dataSet = (DataSet) dataSource;

            if (dataSet.isContinuous()) {
                IndTestParams indTestParams = params.getIndTestParams();
                if (indTestParams == null) {
                    indTestParams = new BasicIndTestParams();
                    params.setIndTestParams2(indTestParams);
                }
                return;
            }
            else if (dataSet.isDiscrete()) {
                IndTestParams indTestParams = params.getIndTestParams();
                if (indTestParams == null) {
                    indTestParams = new BasicIndTestParams();
                    params.setIndTestParams2(indTestParams);
                }
                return;
            }
            else {
                throw new IllegalStateException("Tabular data must be either " +
                        "continuous or discrete.");
            }
        }

        if (dataSource instanceof CorrelationMatrix) {
            IndTestParams indTestParams = params.getIndTestParams();
            if (indTestParams == null) {
                indTestParams = new BasicIndTestParams();
                params.setIndTestParams2(indTestParams);
            }
            return;
        }

        if (dataSource instanceof CovarianceMatrix) {
            IndTestParams indTestParams = params.getIndTestParams();
            if (indTestParams == null) {
                indTestParams = new BasicIndTestParams();
                params.setIndTestParams2(indTestParams);
            }
            return;
        }

        if (dataSource instanceof Graph) {
            IndTestParams indTestParams = params.getIndTestParams();
            if (indTestParams == null) {
                indTestParams = new GraphIndTestParams();
                params.setIndTestParams2(indTestParams);
            }
            return;
        }

        if (dataSource instanceof TimeSeriesData) {
            TimeSeriesData data = (TimeSeriesData) dataSource;
            IndTestParams indTestParams = params.getIndTestParams();
            if (indTestParams == null ||
                    !(indTestParams instanceof BasicIndTestParams) || !(
                    getOldNumTimePoints(indTestParams) ==
                            data.getNumTimePoints())) {
                indTestParams = new BasicIndTestParams();
                params.setIndTestParams2(indTestParams);
            }
            return;
        }

        throw new IllegalStateException("Unrecognized data type.");
    }


    private int getOldNumTimePoints(IndTestParams indTestParams) {
        return ((LagIndTestParams) indTestParams).getNumTimePoints();
    }
}


