package edu.cmu.tetrad.manip.population;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.manipulated_graph.AbstractManipulatedGraph;
import edu.cmu.tetrad.manip.manipulated_graph.ManipulatedGraph;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Oct 5, 2003
 * Time: 6:52:34 PM
 * To change this template use Options | File Templates.
 * todo document
 */
public class TestPopulation extends junit.framework.TestCase {
    Graph graph;
    BayesPm pm;
    MlBayesIm im;
    //ExperimentalSetup expQL;
    ExperimentalSetup exp;
    ExperimentalSetup expVarStudied;
    AbstractManipulatedGraph manipGraph;

    public TestPopulation(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        makeModel();
        makePM(graph);
        makeIM(pm);
        makeExperiment(graph);
        makeManipulatedGraph(graph, exp);
    }

    public void testGetRows(){
        assertTrue(Population.getNumCombinationRows(im, expVarStudied)==  18);
    }

    public void testGetColumns(){
        assertTrue(Population.getNumColumns(im) == 4);
    }

    public void testGetHeaders(){
        assertTrue(Population.getColumnHeaders(im, expVarStudied)[0].equals("education"));
        assertTrue(Population.getColumnHeaders(im, expVarStudied)[1].equals("happiness"));
        assertTrue(Population.getColumnHeaders(im, expVarStudied)[2].equals("income"));
        assertTrue(Population.getColumnHeaders(im, expVarStudied)[3].equals("%"));
    }



    public void testCombinations(){
        int i, j;
        /*
        for(i=0; i< 36; i++){
            System.out.println(i + " " +
                    fullPopulationDistribution.getValueAt(i, 0) + " " +
                    fullPopulationDistribution.getValueAt(i, 1) + " " +
                    fullPopulationDistribution.getValueAt(i, 2) + " " +
                    fullPopulationDistribution.getValueAt(i, 3) + " " +
                    fullPopulationDistribution.getValueAt(i, 4)
            );
        }
        */


        //check the education column

        for(i=0;  i <6; i++){
            assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("college")); //$NON-NLS-1$
        }
        for(i=6;  i<12; i++){
            assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("High school")); //$NON-NLS-1$
        }
        for(i=12; i<18; i++){
            assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("none")); //$NON-NLS-1$
        }


        for(i=0; i<18; ){
            for(j=0; j < 3; j++, i++){
                assertTrue(Population.getCase(im, exp, expVarStudied, i)[1].equals("true")); //$NON-NLS-1$
            }
            for(j=0; j < 3; j++, i++){
                assertTrue(Population.getCase(im, exp, expVarStudied, i)[1].equals("false")); //$NON-NLS-1$
            }
        }

        for(i=0; i < 18; ){
            assertTrue( Population.getCase(im, exp, expVarStudied, i++)[2].equals("high")); //$NON-NLS-1$
            assertTrue( Population.getCase(im, exp, expVarStudied, i++)[2].equals("medium")); //$NON-NLS-1$
            assertTrue( Population.getCase(im, exp, expVarStudied, i++)[2].equals("low")); //$NON-NLS-1$
        }
    }


    public void testProbabilities(){

        //row 0: Latent = true, Education == college, Happiness = true, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 0), .1, .00001);
        //row 1: Latent = true, Education == college, Happiness = true, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 1), .1, .00001);
        //row 2: Latent = true, Education == college, Happiness = true, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 2), .1, .00001);

        //row 3: Latent = true, Education == college, Happiness = false, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 3), .0, .00001);
        //row 4: Latent = true, Education == college, Happiness = false, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 4), .0, .00001);
        //row 5: Latent = true, Education == college, Happiness = false, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 5), .0, .00001);

        //row 6: Latent = true, Education == High school, Happiness = true, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 6), .1, .00001);
        //row 7: Latent = true, Education == High school, Happiness = true, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 7), .1, .00001);
        //row 8: Latent = true, Education == High school, Happiness = true, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 8), .1, .00001);

        //row 9: Latent = true, Education == High school, Happiness = false, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 9), .0, .00001);
        //row 10: Latent = true, Education == High school, Happiness = false, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 10), .0, .00001);
        //row 11: Latent = true, Education == High school, Happiness = false, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 11), .0, .00001);

        //row 12: Latent = true, Education == none, Happiness = true, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 12), 4.0/30.0, .00001);
        //row 13: Latent = true, Education == none, Happiness = true, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 13), 4.0/30.0, .00001);
        //row 14: Latent = true, Education == none, Happiness = true, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 14), 4.0/30.0, .00001);

        //row 15: Latent = true, Education == none, Happiness = false, Income == High
        assertEquals(Population.getProbability(im, exp, expVarStudied, 15), .0, .00001);
        //row 16: Latent = true, Education == none, Happiness = false, Income == medium
        assertEquals(Population.getProbability(im, exp, expVarStudied, 16), .0, .00001);
        //row 17: Latent = true, Education == none, Happiness = false, Income == low
        assertEquals(Population.getProbability(im, exp, expVarStudied, 17), .0, .00001);
    }

    public void testCreateViewedDistribution(){
        expVarStudied.getVariable("income").setStudied(false); //$NON-NLS-1$
        int i, j;


        //check the education column
        for(j = 0; j < 6;){
            for(i=0;  i <2; i++, j++){
                assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("college")); //$NON-NLS-1$
            }
            for(i=2;  i<4; i++, j++){
                assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("High school")); //$NON-NLS-1$
            }
            for(i=4; i<6; i++, j++){
                assertTrue(Population.getCase(im, exp, expVarStudied, i)[0].equals("none")); //$NON-NLS-1$
            }
        }

        for(i=0; i<6; ){
            assertTrue( (Population.getCase(im, exp, expVarStudied, i++)[1]).equals("true")); //$NON-NLS-1$
            assertTrue( (Population.getCase(im, exp, expVarStudied, i++)[1]).equals("false")); //$NON-NLS-1$
        }

        //row 0: Education == college, Happiness = true
        assertEquals(Population.getProbability(im, exp, expVarStudied, 0), .3, .00001);
        //row 1:  Education == college, Happiness = false
        assertEquals(Population.getProbability(im, exp, expVarStudied, 1), .0, .00001);
        //row 2:  Education == High School, Happiness = true
        assertEquals(Population.getProbability(im, exp, expVarStudied, 2), .3, .00001);
        //row 3:  Education == High School, Happiness = false
        assertEquals(Population.getProbability(im, exp, expVarStudied, 3), .0, .00001);
        //row 4:  Education == none, Happiness = true
        assertEquals(Population.getProbability(im, exp, expVarStudied, 4), .4, .00001);
        //row 5:  Education == none, Happiness = false
        assertEquals(Population.getProbability(im, exp, expVarStudied, 5), .0, .00001);

    }

    /**
     * This test checks that the frequencies are still generated even when one of the variables
     * is locked or randomized.
     */
    public void testManipulations(){

       // Population.getProbability(im, manipGraph, exp, expVarStudied, 0)
    }


    public void testPrint(){
       //fullPopulationDistribution.printTables();
    }


    private void makeModel(){
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode("education"));
        m1.addNode(new GraphNode("happiness"));
        m1.addNode(new GraphNode("income"));
        tn = new GraphNode("Latent");
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("happiness"));
        m1.addDirectedEdge(m1.getNode("education"), m1.getNode("income"));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode("education"));

        graph = m1;
    }

    private void makePM(Graph aGraph){
        pm = new BayesPm(new Dag(aGraph));
        pm.setNumCategories(pm.getDag().getNode("education"), 3 );

        ArrayList <String> varVals = new ArrayList<String> ();
        varVals.add("college");
        varVals.add("High school");
        varVals.add("none");
        pm.setCategories(pm.getDag().getNode("education"), varVals);

        varVals = new ArrayList<String> ();
        varVals.add("high");
        varVals.add("medium");
        varVals.add("low");
        pm.setCategories(pm.getDag().getNode("income"), varVals);

        varVals = new ArrayList<String> ();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("happiness"), varVals);

        varVals = new ArrayList<String> ();
        varVals.add("true");
        varVals.add("false");
        pm.setCategories(pm.getDag().getNode("Latent"), varVals);


    }

    private void makeIM(BayesPm aPm){
        int i;
        im = new MlBayesIm(aPm);
        //Latent
        i = im.getNodeIndex(im.getNode("Latent"));
        im.setProbability(i,0,0, 0.5);
        im.setProbability(i,0,1, 0.5);

        //education
        i = im.getNodeIndex(im.getNode("education"));
        im.setProbability(i,0,0, 0.3);
        im.setProbability(i,0,1, 0.3);
        im.setProbability(i,0,2, 0.4);
        im.setProbability(i,1,0, 0.3);
        im.setProbability(i,1,1, 0.3);
        im.setProbability(i,1,2, 0.4);

        //happiness
        i = im.getNodeIndex(im.getNode("happiness"));
        im.setProbability(i,0,0, 0.5);
        im.setProbability(i,0,1, 0.5);
        im.setProbability(i,1,0, 0.5);
        im.setProbability(i,1,1, 0.5);
        im.setProbability(i,2,0, 0.5);
        im.setProbability(i,2,1, 0.5);

        //income
        i = im.getNodeIndex(im.getNode("income"));
        im.setProbability(i,0,0, 0.3);
        im.setProbability(i,0,1, 0.3);
        im.setProbability(i,0,2, 0.4);
        im.setProbability(i,1,0, 0.3);
        im.setProbability(i,1,1, 0.3);
        im.setProbability(i,1,2, 0.4);
        im.setProbability(i,2,0, 0.3);
        im.setProbability(i,2,1, 0.3);
        im.setProbability(i,2,2, 0.4);

        //System.out.println(im.toString());
    }

    private void makeExperiment(Graph aGraph){
        exp = new ExperimentalSetup("experiment", aGraph);
        exp.getVariable("income").setRandomized();
        exp.getVariable("happiness").setLocked("true");
        expVarStudied = exp;
    }

    private void makeManipulatedGraph(Graph aGraph, ExperimentalSetup exp){
        manipGraph = new ManipulatedGraph(aGraph, exp);
    }
}
