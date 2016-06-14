package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Feb 14, 2004
 * Time: 9:50:33 PM
 * To change this template use Options | File Templates.
 */
public class TestGuessedManipulatedGraph extends junit.framework.TestCase{
    Graph correctGraph;
    ExperimentalSetup experiment;
    GuessedManipulatedGraph guess;

    public TestGuessedManipulatedGraph(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        correctGraph = makeModel("Education", "Income", "Happiness"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        experiment = new ExperimentalSetup("experiment 1", correctGraph); //$NON-NLS-1$
        guess = new GuessedManipulatedGraph(correctGraph);
    }

    /**
     * A randomized variable should break the incoming links and leave the outgoing links in tact
     */
    public void testRandomizedType(){
        guess.setVariableRandomized("Education"); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Education")== VariableManipulation.RANDOMIZED); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Income")== VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Happiness")== VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Latent")== VariableManipulation.NONE); //$NON-NLS-1$
        assertNull(guess.getManipulationFor("foo")); //$NON-NLS-1$

        ManipulatedEdge [] brokenEdges = guess.getBrokenEdges();
        assertTrue(brokenEdges.length == 0);

        guess.setVariableNotManipulated("Education"); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Education")== VariableManipulation.NONE); //$NON-NLS-1$
    }

    /**
     * A locked variable should break the incoming links and freeze the outgoing links in tact
     */
    public void testLockedType(){
        guess.setVariableLocked("Education"); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Education")== VariableManipulation.LOCKED); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Income")== VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Happiness")== VariableManipulation.NONE); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Latent")== VariableManipulation.NONE); //$NON-NLS-1$
        assertNull(guess.getManipulationFor("foo")); //$NON-NLS-1$

        ManipulatedEdge [] brokenEdges = guess.getBrokenEdges();
        assertTrue(brokenEdges.length == 0);

        guess.setVariableNotManipulated("Education"); //$NON-NLS-1$
        assertTrue(guess.getManipulationFor("Education")== VariableManipulation.NONE); //$NON-NLS-1$
    }


    public void testGetAllEdges(){
        ManipulatedEdge [] edges = guess.getAllNonLatentEdges();
        for(int i = 0; i < edges.length; i++){
            System.out.println(this.getClass() + " " + edges[i]); //$NON-NLS-1$
        }
        assertTrue(edges.length == 2);

    }

    public void testEdgeType(){
        guess.setEdgeBroken("Education", "Income"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(guess.getManipulationForEdge("Education", "Income") == EdgeManipulation.BROKEN); //$NON-NLS-1$ //$NON-NLS-2$
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
