package edu.cmu.tetrad.manip.experimental_setup;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.NodeType;


/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Oct 13, 2003
 * Time: 3:41:57 PM
 * To change this template use Options | File Templates.
 * todo document
 */
public class TestExperimentalSetupVariablesStudied extends junit.framework.TestCase{

    private ExperimentalSetup studiedVariables;

    public TestExperimentalSetupVariablesStudied(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Dag graph = new Dag();
        graph.addNode(new GraphNode("education")); //$NON-NLS-1$
        graph.addNode(new GraphNode("income")); //$NON-NLS-1$
        graph.addNode(new GraphNode("happiness")); //$NON-NLS-1$
        GraphNode node = new GraphNode("Latent"); //$NON-NLS-1$
        node.setNodeType(NodeType.LATENT);
        graph.addNode(node);
        graph.addDirectedEdge(graph.getNode("education"), graph.getNode("income")); //$NON-NLS-1$ //$NON-NLS-2$
        graph.addDirectedEdge(graph.getNode("education"), graph.getNode("happiness")); //$NON-NLS-1$ //$NON-NLS-2$
        graph.addDirectedEdge(graph.getNode("Latent"), graph.getNode("education")); //$NON-NLS-1$ //$NON-NLS-2$

        studiedVariables = new ExperimentalSetup("experiment 1", graph);         //$NON-NLS-1$
    }

    public void test(){
        boolean except = false;

        assertTrue(studiedVariables.isVariableStudied("education")); //$NON-NLS-1$
        assertTrue(studiedVariables.isVariableStudied("income")); //$NON-NLS-1$
        assertTrue(studiedVariables.isVariableStudied("happiness")); //$NON-NLS-1$


        studiedVariables.getVariable("education").setStudied(false); //$NON-NLS-1$
        assertTrue(!studiedVariables.isVariableStudied("education")) ; //$NON-NLS-1$
        assertTrue(studiedVariables.getNumVariablesStudied() == 2);

        try{
            studiedVariables.isVariableStudied("foo"); //$NON-NLS-1$
        }catch (IllegalArgumentException e){
            except = true;
        }
        assertTrue(except);

        except = false;
        try{
            studiedVariables.getVariable("foo").setStudied(false); //$NON-NLS-1$
        }catch (IllegalArgumentException e){
            except = true;
        }
        assertTrue(except);
    }

    public void testNames(){
        String [] names;
        studiedVariables.getVariable("education").setStudied(false); //$NON-NLS-1$
        names = studiedVariables.getNamesOfStudiedVariables();
        assertTrue(studiedVariables.getNumVariablesStudied() == 2);
        assertTrue(names.length == 2);
        assertTrue(((names[0]).equals("happiness") && (names[1]).equals("income")) || //$NON-NLS-1$ //$NON-NLS-2$
                    (names[1]).equals("happiness") && (names[0]).equals("income")); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
