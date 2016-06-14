package edu.cmu.tetrad.manip.sample;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.manipulated_graph.AbstractManipulatedGraph;
import edu.cmu.tetrad.manip.manipulated_graph.ManipulatedGraph;
import edu.cmu.tetrad.manip.population.Population;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: Matthew Easterday Date: Oct 20, 2003 Time:
 * 3:44:19 PM To change this template use Options | File Templates. todo
 * document
 */
public class TestSample extends junit.framework.TestCase {
    Population pop;
    Graph graph;
    BayesPm pm;
    MlBayesIm im;
    ExperimentalSetup exp;

    ExperimentalSetup expVarStudied;
    AbstractManipulatedGraph manipGraph;
    //SampleCases sampleCases;
    //JointDistribution sampleFrequencies;
    BayesSample sample;
    long seed;

    public TestSample(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        makeModel();
        makePM(graph);
        makeIM(pm);
        makeExperiment(graph);
        makeManipulatedGraph(graph, exp);

        seed = System.currentTimeMillis();
        sample = new BayesSample(im, exp, 10, seed);
        //sampleCases = BayesSample.createSampleCases(im, manipGraph, expQN, expVarStudied, true, 10);
        //sampleFrequencies = BayesSample.createSampleCaseFrequencies(sampleCases);
    }

    /*
    public void testPrint(){
        String [] sampleCase;

        System.out.println("---BayesSample cases---");
        for(int row = 0; row < sample.getNumSampleCases(expVarStudied); row++){
            sampleCase = sample.getSampleCase(row, expVarStudied);
            System.out.print(row + " ");
            for(int col = 0; col < sampleCase.length;  col++){
                System.out.print(sampleCase[col] + " ");
            }
            System.out.println();
        }

        System.out.println();
         System.out.println("---BayesSample frequencies---");
        for(int row = 0; row < sample.getNumSampleCaseFrequencies(expVarStudied); row++){
            sampleCase = sample.getSampleCaseFrequencyCombination(row, expVarStudied);
            System.out.print(row + " ");
            for(int col = 0; col < sampleCase.length;  col++){
                System.out.print(sampleCase[col] + " " );
            }
            System.out.print(sample.getSampleCaseFrequency(row, expVarStudied) + " ");
            System.out.println();
        }
    }
    */

    public void testGetNumSampleCases() {
        assertTrue(sample.getNumSampleCases() == 10);
    }

    public void testGetSampleCase() {
        assertTrue(sample.getSampleCase(0, expVarStudied).length == 4);
        expVarStudied.getVariable("education").setStudied(false); //$NON-NLS-1$
        assertTrue(sample.getSampleCase(0, expVarStudied).length == 3);
    }

    public void testGetSampleCaseColumnNames() {
        String[] headers = sample.getSampleCaseColumnNames(expVarStudied);
        assertTrue(headers.length == 4);
        assertTrue(headers[0].equals("#")); //$NON-NLS-1$
        assertTrue(headers[1].equals("education")); //$NON-NLS-1$
        assertTrue(headers[2].equals("happiness")); //$NON-NLS-1$
        assertTrue(headers[3].equals("income")); //$NON-NLS-1$
    }


    public void testGetNumSampleCaseFrequencies() {
        assertTrue(sample.getNumSampleCaseFrequencies(expVarStudied) == 18);
        expVarStudied.getVariable("education").setStudied(false); //$NON-NLS-1$
        assertTrue(sample.getNumSampleCaseFrequencies(expVarStudied) == 6);
    }

    public void testGetSampleFrequencies() {
        double count1, count2;
        BayesSample aSample = new BayesSample(im, exp, 1000, seed);

        for (int i = 0; i < aSample.getNumSampleCaseFrequencies(expVarStudied); i++)
        {
            count1 = Population.getProbability(im, exp, expVarStudied, i);
            count2 = aSample.getSampleCaseFrequency(i, expVarStudied);
            //System.out.println(i + " " + count1 + " " + count2);
            assertEquals(count1, count2, .05);
        }
    }

    public void testGetSampleFrequenciesColumnNames() {
        String[] headers = sample.getSampleFrequenciesColumnNames(expVarStudied);
        assertTrue(headers.length == 4);
        assertTrue(headers[0].equals("education")); //$NON-NLS-1$
        assertTrue(headers[1].equals("happiness")); //$NON-NLS-1$
        assertTrue(headers[2].equals("income")); //$NON-NLS-1$
        assertTrue(headers[3].equals("%")); //$NON-NLS-1$
    }

    public void rtest45() {

        for (int i = 0; i < 100; i++) {
            Graph graph = GraphUtils.randomDag(10, 0, 10, 4, 4, 4, false);
            SemPm semPm = new SemPm(graph);
            SemIm semIm1 = new SemIm(semPm);
            SemIm semIm2 = new SemIm(semPm);

            DataSet dataSet = semIm1.simulateData(1000, false);
            DoubleMatrix2D sample = new CovarianceMatrix(dataSet).getMatrix();

            semIm2.setDataSet(dataSet);
            DoubleMatrix2D implied = semIm2.getImplCovar();

            double detSample = new Algebra().det(sample);
            double detImplied = new Algebra().det(implied);

            DoubleMatrix2D inverseImplied = new Algebra().inverse(implied);
            DoubleMatrix2D product = new Algebra().mult(sample, inverseImplied);
            double trace = new Algebra().trace(product);

            System.out.println(trace);
        }
    }

    /**
     * Test to make sure that the sample can be recalculated with exactly the
     * same values from a single seed.
     */
    public void testReCalculateFromSeed() {
        BayesSample sampleA = new BayesSample(im, exp, 10, seed);
        BayesSample sampleB = new BayesSample(im, exp, 10, seed);
        String[] caseA;
        String[] caseB;

        for (int i = 0; i < 10; i++) {
            caseA = sampleA.getSampleCase(i, expVarStudied);
            caseB = sampleB.getSampleCase(i, expVarStudied);
            for (int j = 0; j < caseA.length; j++) {
                assertTrue(caseA[j].equals(caseB[j]));
            }
        }
    }

    private void makeModel() {
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education")); //$NON-NLS-1$
        m1.addNode(new GraphNode("happiness")); //$NON-NLS-1$
        m1.addNode(new GraphNode("income")); //$NON-NLS-1$
        tn = new GraphNode("Latent"); //$NON-NLS-1$
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness")); //$NON-NLS-1$ //$NON-NLS-2$
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income")); //$NON-NLS-1$ //$NON-NLS-2$
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education")); //$NON-NLS-1$ //$NON-NLS-2$

        graph = m1;
    }

    private void makePM(Graph aGraph) {
        pm = new BayesPm(new Dag(aGraph));

        ArrayList<String> varVals = new ArrayList<String>();
        varVals.add("college"); //$NON-NLS-1$
        varVals.add("High school"); //$NON-NLS-1$
        varVals.add("none"); //$NON-NLS-1$
        pm.setCategories(pm.getDag().getNode("education"), varVals); //$NON-NLS-1$

        varVals = new ArrayList<String>();
        varVals.add("high"); //$NON-NLS-1$
        varVals.add("medium"); //$NON-NLS-1$
        varVals.add("low"); //$NON-NLS-1$
        pm.setCategories(pm.getDag().getNode("income"), varVals); //$NON-NLS-1$

        varVals = new ArrayList<String>();
        varVals.add("true"); //$NON-NLS-1$
        varVals.add("false"); //$NON-NLS-1$
        pm.setCategories(pm.getDag().getNode("happiness"), varVals); //$NON-NLS-1$

        varVals = new ArrayList<String>();
        varVals.add("true"); //$NON-NLS-1$
        varVals.add("false"); //$NON-NLS-1$
        pm.setCategories(pm.getDag().getNode("Latent"), varVals); //$NON-NLS-1$


    }

    private void makeIM(BayesPm aPm) {
        int i;
        im = new MlBayesIm(aPm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent")); //$NON-NLS-1$
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education")); //$NON-NLS-1$
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);

        //happiness
        i = im.getNodeIndex(im.getNode("happiness")); //$NON-NLS-1$
        im.setProbability(i, 0, 0, 0.5);
        im.setProbability(i, 0, 1, 0.5);
        im.setProbability(i, 1, 0, 0.5);
        im.setProbability(i, 1, 1, 0.5);
        im.setProbability(i, 2, 0, 0.5);
        im.setProbability(i, 2, 1, 0.5);

        //income
        i = im.getNodeIndex(im.getNode("income")); //$NON-NLS-1$
        im.setProbability(i, 0, 0, 0.3);
        im.setProbability(i, 0, 1, 0.3);
        im.setProbability(i, 0, 2, 0.4);
        im.setProbability(i, 1, 0, 0.3);
        im.setProbability(i, 1, 1, 0.3);
        im.setProbability(i, 1, 2, 0.4);
        im.setProbability(i, 2, 0, 0.3);
        im.setProbability(i, 2, 1, 0.3);
        im.setProbability(i, 2, 2, 0.4);

        //System.out.println(im.toString());
    }

    public void test5() {
        double d = 1.0 / 3.0;

        System.out.println(d);

        double d2 = d - (d % 0.01);

        System.out.println(d2);
    }

    private void makeExperiment(Graph aGraph) {
        exp = new ExperimentalSetup("experiment", aGraph); //$NON-NLS-1$
        exp.getVariable("happiness").setLocked("true"); //$NON-NLS-1$ //$NON-NLS-2$
        exp.getVariable("income").setRandomized(); //$NON-NLS-1$
        exp.getVariable("happiness").setLocked("true"); //$NON-NLS-1$ //$NON-NLS-2$
        expVarStudied = exp;
    }

    private void makeManipulatedGraph(Graph aGraph, ExperimentalSetup exp) {
        manipGraph = new ManipulatedGraph(aGraph, exp);
    }

}
