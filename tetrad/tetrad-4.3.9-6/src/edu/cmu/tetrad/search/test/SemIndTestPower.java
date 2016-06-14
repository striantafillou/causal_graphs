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
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.indtest.IndTestCramerT;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.search.indtest.IndTestFisherZ;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is an application which generates various random SEM's (i.e.
 * random graphs, randomly chosen parameters, etc.)  and then tests for
 * independence in generated datasets using two kinds of independence
 * tests--Fisher's Z and whether partial correlations are significantly
 * different from zero.  True negatives, false negatives, true positives and
 * false positives are counted according to whether the result of the test
 * agrees with d-separation facts involving the same pair of variables and the
 * same conditioning set applied to the underlying graph. </p> The issue is
 * which test is more powerful, where the power is 1.0 minus the probability of
 * a type II error.  A type II error consists of accepting a false null
 * hypothesis.
 *
 * @author Frank Wimberly (who doesn't remember writing this but the evidence is
 *         clear that he did.)
 */
public class SemIndTestPower {
    private static CorrelationMatrix corrMatrix;
    private static List variables;
    private static double alpha = 0.05;

    public static void main(String[] args) {

        double[][][] allPowersT = new double[4][5][3];
        double[][][] allSensesT = new double[4][5][3];
        double[][][] allPowersF = new double[4][5][3];
        double[][][] allSensesF = new double[4][5][3];

        int numNodes = 20;
        //int numEdges = 3;
        int numLatents = 0;
        //int sampleSize = 50;
        //alpha = 0.01;
        int[] edges = {20, 30, 50, 100};
        int[] samples = {10, 20, 50, 100, 1000};
        double[] sigs = {0.01, 0.05, 0.10};

        int ncases = 0;
        double sumTtestP = 0.0;
        double sumTtestS = 0.0;
        double sumFisherP = 0.0;
        double sumFisherS = 0.0;

        for (int iedges = 0; iedges < edges.length; iedges++) {
            for (int isamp = 0; isamp < samples.length; isamp++) {
                for (int isig = 0; isig < sigs.length; isig++) {

                    int numEdges = edges[iedges];
                    int sampleSize = samples[isamp];
                    boolean latentDataSaved = false;
                    double alpha = sigs[isig];
                    int[][] corrConfusion = {{0, 0}, {0, 0}};
                    int[][] fishConfusion = {{0, 0}, {0, 0}};

                    for (int k = 0; k < 100; k++) {
                        Graph graph = GraphUtils.randomDagC(numNodes,
                                numLatents, numEdges
                        );
                        SemPm semPm = new SemPm(graph);

                        SemIm sem = new SemIm(semPm);
                        DataSet contDataColumnContinuous =
                                sem.simulateData(sampleSize, latentDataSaved);
                        sem.setDataSet(contDataColumnContinuous);

                        //double[][] covar = sem.getImplCovarMeas();
                        DoubleMatrix2D covar = sem.getSampleCovar();
                        int nvars = covar.rows();

                        String[] varNames = new String[nvars];

                        List nodes = sem.getMeasuredNodes();

                        for (int i = 0; i < nvars; i++) {
                            varNames[i] = ((Node) nodes.get(i)).getName();
                        }

                        CovarianceMatrix covarMatrix = new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), covar, sampleSize);
                        variables = covarMatrix.getVariables();

                        corrMatrix = new CorrelationMatrix(covarMatrix);


                        IndTestCramerT indTestCramerT =
                                new IndTestCramerT(corrMatrix, alpha);

                        IndTestFisherZ indTestF =
                                new IndTestFisherZ(corrMatrix, alpha);

                        IndTestDSep indTestG = new IndTestDSep(graph);

                        List cond = new LinkedList();
                        cond.add(variables.get(2));
                        cond.add(variables.get(3));
                        cond.add(variables.get(4));
                        //cond.add(variables.get(5));
                        //cond.add(variables.get(6));

                        boolean indCorr = indTestCramerT.isIndependent(
                                (Node) variables.get(0),
                                (Node) variables.get(1), cond);

                        boolean indGraph = indTestG.isIndependent(
                                (Node) variables.get(0),
                                (Node) variables.get(1), cond);

                        boolean indFisher = indTestF.isIndependent(
                                (Node) variables.get(0),
                                (Node) variables.get(1), cond);

                        //boolean indFisher = true;
                        //double thresh = cutoffGaussian();
                        //System.out.println("thresh = " + thresh);
                        //if(Math.abs(fishersZ) > 1.96) indFisher = false; //Two sided with alpha = 0.05
                        //if(Math.abs(fishersZ) > thresh) indFisher= false;  //Two sided

                        //System.out.println(indCorr + " " + indGraph + " " + indFisher);

                        if (!indGraph && !indCorr) {
                            corrConfusion[0][0]++;  //True neg
                        }
                        if (indGraph && indCorr) {
                            corrConfusion[1][1]++;    //True pos
                        }
                        if (!indGraph && indCorr) {
                            corrConfusion[0][1]++;   //False pos
                        }
                        if (indGraph && !indCorr) {
                            corrConfusion[1][0]++;   //False neg
                        }

                        if (!indGraph && !indFisher) {
                            fishConfusion[0][0]++;  //True neg
                        }
                        if (indGraph && indFisher) {
                            fishConfusion[1][1]++;    //True pos
                        }
                        if (!indGraph && indFisher) {
                            fishConfusion[0][1]++;   //False pos
                        }
                        if (indGraph && !indFisher) {
                            fishConfusion[1][0]++;   //False neg
                        }

                        //System.out.println(sem.toString());
                    }
                    /*
                    System.out.println("\n\n\n=========================================");
                    System.out.println("For nedges = " + numEdges + " samp size = " + sampleSize +
                                       " alpha = " + alpha);
                    System.out.println("Confusion matrix for t-test:");
                    System.out.println("\tdep\tind");
                    System.out.println("dep\t" + corrConfusion[0][0] + "\t" + corrConfusion[0][1]);
                    System.out.println("ind\t" + corrConfusion[1][0] + "\t" + corrConfusion[1][1]);
                    */
                    double powerCorr = (double) corrConfusion[0][0] / (double) (
                            corrConfusion[0][0] + corrConfusion[0][1]);
                    double sensitivityCorr = (double) corrConfusion[1][1] /
                            (double) (corrConfusion[1][1] +
                                    corrConfusion[1][0]);
                    /*
                    System.out.println("Power = " + powerCorr);
                    System.out.println("Sensitivity = " + sensitivityCorr);

                    System.out.println("\n\nConfusion matrix for Fisher's Z test:");
                    System.out.println("\tdep\tind");
                    System.out.println("dep\t" + fishConfusion[0][0] + "\t" + fishConfusion[0][1]);
                    System.out.println("ind\t" + fishConfusion[1][0] + "\t" + fishConfusion[1][1]);
                    */
                    double powerFish = (double) fishConfusion[0][0] / (double) (
                            fishConfusion[0][0] + fishConfusion[0][1]);
                    double sensitivityFish = (double) fishConfusion[1][1] /
                            (double) (fishConfusion[1][1] +
                                    fishConfusion[1][0]);
                    //System.out.println("Power = " + powerFish);
                    //System.out.println("Sensitivity = " + sensitivityFish);

                    allPowersT[iedges][isamp][isig] = powerCorr;
                    allSensesT[iedges][isamp][isig] = sensitivityCorr;
                    allPowersF[iedges][isamp][isig] = powerFish;
                    allSensesF[iedges][isamp][isig] = sensitivityFish;

                    ncases++;
                    sumTtestP += powerCorr;
                    sumTtestS += sensitivityCorr;
                    sumFisherP += powerFish;
                    sumFisherS += sensitivityFish;

                }
            }
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);


        for (int iedges = 0; iedges < edges.length; iedges++) {
            System.out.println("\n\n\n****For graph with " + numNodes +
                    "variables and " + edges[iedges] +
                    " edges and five conditioning variables:");
            System.out.println("\nPower\n");
            System.out.println("\t\t\t\tFisher's Z\t\t\tT-Test");
            System.out.println(
                    "Alpha:\t\t\t0.01  0.05  0.10\t\t 0.01  0.05  0.10");
            for (int isamp = 0; isamp < samples.length; isamp++) {
                System.out.print(samples[isamp] + "\t\t" +
                        nf.format(allPowersF[iedges][isamp][0]) + "  " +
                        nf.format(allPowersF[iedges][isamp][1]) + "  " +
                        nf.format(allPowersF[iedges][isamp][2]) + "\t");
                System.out.println("\t\t" +
                        nf.format(allPowersT[iedges][isamp][0]) + "  " +
                        nf.format(allPowersT[iedges][isamp][1]) + "  " +
                        nf.format(allPowersT[iedges][isamp][2]));
            }

            System.out.println("\nSensitivity\n");
            System.out.println("\t\t\t\tFisher's Z\t\t\tT-Test");
            System.out.println(
                    "Alpha:\t\t\t0.01  0.05  0.10\t\t 0.01  0.05  0.10");
            for (int isamp = 0; isamp < samples.length; isamp++) {
                System.out.print(samples[isamp] + "\t\t" +
                        nf.format(allSensesF[iedges][isamp][0]) + "  " +
                        nf.format(allSensesF[iedges][isamp][1]) + "  " +
                        nf.format(allSensesF[iedges][isamp][2]) + "\t");
                System.out.println("\t\t" +
                        nf.format(allSensesT[iedges][isamp][0]) + "  " +
                        nf.format(allSensesT[iedges][isamp][1]) + "  " +
                        nf.format(allSensesT[iedges][isamp][2]));
            }
        }

        double avePowerTT = sumTtestP / ncases;
        double aveSenseTT = sumTtestS / ncases;
        System.out.println("\nT-Test ave power = " + nf.format(avePowerTT) +
                " ave sensitivity = " + nf.format(aveSenseTT));

        double avePowerFish = sumFisherP / ncases;
        double aveSenseFish = sumFisherS / ncases;
        System.out.println("Fishers Z ave power = " + nf.format(avePowerFish) +
                " ave sensitivity = " + nf.format(aveSenseFish));
    }

    public static double getAlpha() {
        return alpha;
    }
}


