package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Sep 28, 2003
 * Time: 11:38:10 AM
 * To change this template use Options | File Templates.
 * todo document
 */
public class TestManipulatedGraph extends junit.framework.TestCase {

    Graph correctGraph;
    ExperimentalSetup experiment;


    public TestManipulatedGraph(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        correctGraph = makeModel("Education", "Income", "Happiness"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        experiment = new ExperimentalSetup("experiment 1", correctGraph); //$NON-NLS-1$
    }

    /**
     * A randomized variable should break the incoming links and leave the outgoing links in tact
     */
    public void testRandomizedType(){
        experiment.getVariable("Education").setRandomized(); //$NON-NLS-1$
        AbstractManipulatedGraph manipulatedGraph;
        manipulatedGraph = new ManipulatedGraph(correctGraph, experiment);

        assertTrue(manipulatedGraph.getManipulationFor("Education") == VariableManipulation .RANDOMIZED); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Income")    == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Happiness") == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Latent")    == VariableManipulation.NONE); //$NON-NLS-1$
        assertNull(manipulatedGraph.getManipulationFor("foo"));

        ManipulatedEdge [] brokenEdges = manipulatedGraph.getBrokenEdges();
        assertTrue(brokenEdges.length == 1);
        assertTrue(brokenEdges[0].toString().equals("Latent --> Education")); //$NON-NLS-1$
        assertTrue(brokenEdges[0].getType() == EdgeManipulation.BROKEN);
    }


    /**
     * A locked variable should break the incoming links and freeze the outgoing links in tact
     */
    public void testLockedType(){
        experiment.getVariable("Education").setLocked("fake value"); //$NON-NLS-1$ //$NON-NLS-2$
        AbstractManipulatedGraph manipulatedGraph;
        manipulatedGraph = new ManipulatedGraph(correctGraph, experiment);

        assertTrue(manipulatedGraph.getManipulationFor("Education") == VariableManipulation.LOCKED); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Income")    == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Happiness") == VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(manipulatedGraph.getManipulationFor("Latent")    == VariableManipulation.NONE); //$NON-NLS-1$
        assertNull(manipulatedGraph.getManipulationFor("foo"));

        ManipulatedEdge [] brokenEdges = manipulatedGraph.getBrokenEdges();
        ManipulatedEdge [] frozenEdges = manipulatedGraph.getFrozenEdges();

        assertTrue(brokenEdges.length == 1);
        assertTrue(frozenEdges.length == 2);


        assertTrue((brokenEdges[0].toString().equals("Latent --> Education") && //$NON-NLS-1$
                brokenEdges[0].getType() == EdgeManipulation.BROKEN));
        assertTrue((frozenEdges[0].toString().equals("Education --> Income") && //$NON-NLS-1$
                frozenEdges[0].getType() == EdgeManipulation.FROZEN));
        assertTrue((frozenEdges[1].toString().equals("Education --> Happiness") && //$NON-NLS-1$
                frozenEdges[1].getType() == EdgeManipulation.FROZEN));

    }


    private Graph makeModel(String a, String b, String c){
        GraphNode tn;
        Graph m1 = new Dag();

        m1.addNode(new GraphNode(a));
        m1.addNode(new GraphNode(b));
        m1.addNode(new GraphNode(c));
        tn = new GraphNode("Latent"); //$NON-NLS-1$
        tn.setNodeType(NodeType.LATENT);
        m1.addNode(tn);

        m1.addDirectedEdge(m1.getNode(a), m1.getNode(b));
        m1.addDirectedEdge(m1.getNode(a), m1.getNode(c));
        m1.addDirectedEdge(m1.getNode("Latent"), m1.getNode(a)); //$NON-NLS-1$

        return m1;
    }
}
