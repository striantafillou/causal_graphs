package edu.cmu.tetrad.manip.experimental_setup;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;


/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Sep 24, 2003
 * Time: 6:52:22 PM
 * To change this template use Options | File Templates.
 * todo document
 */
public class TestExperimentalSetup extends junit.framework.TestCase {

    private ExperimentalSetup experiment;

    public TestExperimentalSetup(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Dag graph = new Dag();
        graph.addNode(new GraphNode("A")); //$NON-NLS-1$
        graph.addNode(new GraphNode("B")); //$NON-NLS-1$
        graph.addNode(new GraphNode("C")); //$NON-NLS-1$
        GraphNode node = new GraphNode("Latent"); //$NON-NLS-1$
        node.setNodeType(NodeType.LATENT);
        graph.addNode(node);
        graph.addDirectedEdge(graph.getNode("A"), graph.getNode("B")); //$NON-NLS-1$ //$NON-NLS-2$
        graph.addDirectedEdge(graph.getNode("A"), graph.getNode("C")); //$NON-NLS-1$ //$NON-NLS-2$
        graph.addDirectedEdge(graph.getNode("Latent"), graph.getNode("A")); //$NON-NLS-1$ //$NON-NLS-2$

        experiment = new ExperimentalSetup("experiment 1", graph); //$NON-NLS-1$
    }

    /**
     * Make sure that the default manipulation is set to NONE
     */
    public void testDefaultsSetCorrectly(){
        assertTrue(experiment.getNumVariables() == 3);
        assertTrue(experiment.getVariable("A").getManipulation() == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(experiment.getVariable("B").getManipulation() == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(experiment.getVariable("C").getManipulation() == VariableManipulation.NONE); //$NON-NLS-1$
        //assertTrue(experiment.getManipulationFor("Latent") == ManipulationType.LATENT);

    }

    /**
     * Make sure that if user asks for a variable not in model that it returns null
     */
    public void testVarNotInExperiment(){
        boolean error = false;
        try{
            experiment.getVariable("W").getManipulation(); //$NON-NLS-1$
        } catch (Exception e){
            error = true;
        }
        assertTrue(error);
    }

    /**
     * Make sure that when you set a manipulation, it gets set
     */
    public void testSetManipulator(){
        experiment.getVariable("A").setRandomized(); //$NON-NLS-1$
        assertTrue(experiment.getVariable("A").getManipulation() == VariableManipulation.RANDOMIZED);
        experiment.getVariable("A").setUnmanipulated(); //$NON-NLS-1$
    }

    /**
     * Make sure that an excpetion is thrown
     */
    public void testSetManipulatorOnAbsentVariable(){
        boolean except = false;

        try{
            experiment.getVariable("W").setRandomized(); //$NON-NLS-1$
        } catch (IllegalArgumentException e){
            except = true;
        }
        assertTrue(except);
    }

    /**
     * Make sure you can get all the variables in the model
     */
    public void testGetVariables(){
        String [] varNames = experiment.getVariableNames();

        assertTrue(varNames[0].equals("A")); //$NON-NLS-1$
        assertTrue(varNames[1].equals("B")); //$NON-NLS-1$
        assertTrue(varNames[2].equals("C")); //$NON-NLS-1$
        //assertTrue(((String) i.next()).equals("Latent"));
        assertTrue(varNames.length == 3);
    }

    public void testIsValidVariableName(){
        assertTrue(experiment.isValidVariableName("A")); //$NON-NLS-1$
        assertTrue(!experiment.isValidVariableName("foo")); //$NON-NLS-1$
    }

    public void testSetName(){
        experiment.setName("Experiment foo"); //$NON-NLS-1$
        assertTrue(experiment.getName().equals("Experiment foo")); //$NON-NLS-1$
    }
}
