package edu.cmu.tetrad.manip.population;

import edu.cmu.tetrad.graph.DirectedGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;
import edu.cmu.tetrad.manip.manipulated_graph.ManipulatedGraph;
import edu.cmu.tetrad.sem.Parameter;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: May 21, 2004
 * Time: 12:42:43 AM
 * To change this template use File | Settings | File Templates.
 *
 * This class describes the SEM IM graph model of the correct manipulated
 * graph.
 */
public class CorrectManipulatedGraphSemIm {

    /**
     * @return the manipulated SEM IM graph given the correct SEM IM and the
     * experimental setup.
     */
    public static SemIm createIm(SemIm correctSemIm, ExperimentalSetup experiment){
        //need to remove all the error terms...

        SemGraph tempGraph = new SemGraph(correctSemIm.getSemPm().getGraph());
        List <Node> errorNodes = correctSemIm.getSemPm().getErrorNodes();
        if(errorNodes != null){
            tempGraph.removeNodes(errorNodes);
        }

        Graph mGraph = new ManipulatedGraph(new DirectedGraph(tempGraph), experiment);
        if(mGraph == null){ throw new NullPointerException("Graph was null"); } //$NON-NLS-1$
        SemPm pm = new SemPm(mGraph);                     
        SemIm im = new SemIm(pm);
//        Parameter param;
//        double value;
//        Node nodeA, nodeB;

        //copy the original sem values to the new sem (if possible)
        //replace with SEMIM retain values
        //SemIm.retainValues(correctSemIm, mGraph);

//        for (Parameter parameter : im.getSemPm().getParameters()) {
//            Node nodeA = correctSemIm.getSemPm().getGraph().getNode(parameter.getNodeA().getName());
//            Node nodeB = correctSemIm.getSemPm().getGraph().getNode(parameter.getNodeB().getName());
//            double value;
//
//            if ((nodeA != null) && (nodeB != null)) {
//                try {
//                    value = correctSemIm.getParamValue(nodeA, nodeB);
//                    im.setParamValue(parameter, value);
//                } catch (IllegalArgumentException e) {
//                    if (nodeA == nodeB) { // Y->Z  is now Y  Z  so param E_Z, E_Z should become Z Z
//                        Parameter errorParam = correctSemIm.getSemPm().getNodeParameter(correctSemIm.getSemPm().getGraph().getExogenous(nodeA));
//                        value = correctSemIm.getParamValue(errorParam);
//                        im.setParamValue(parameter, value);
//                    }
//                }
//            }
//        }

        // Rewriting the above route so it doesn't use getParamValue(node1, node2).
        for (Parameter p1 : im.getSemPm().getParameters()) {
            Node nodeA = correctSemIm.getSemPm().getGraph().getNode(p1.getNodeA().getName());
            Node nodeB = correctSemIm.getSemPm().getGraph().getNode(p1.getNodeB().getName());

            for (Parameter p2 : correctSemIm.getSemPm().getParameters()) {
                if (p2.getNodeA() == nodeA && p2.getNodeB() == nodeB && p2.getType() == p1.getType()) {
                    im.setParamValue(p1, correctSemIm.getParamValue(p2));
                }
            }
        }

        //alter the sem values based on the experiment
        List nodes = im.getVariableNodes();
        for (Object ob: nodes) {
            Node node = (Node)ob;
            if(experiment.getVariable(node.getName()).getManipulation() == VariableManipulation.RANDOMIZED) {
                double stdDev = experiment.getVariable(node.getName()).getStandardDeviation();
                im.setMean(node, experiment.getVariable(node.getName()).getMean());

                im.setErrCovar(node, stdDev * stdDev);

            }else{
                im.setMean(node, correctSemIm.getMean(node));
            }
        }
        return im;

/*
        //change the IM based on locked and randomized variables in the
        //quantitative experimental setup
        List variableNames = experiment.getVariables();
        ManipulationType type;
        String variableName;
        for(Iterator i = variableNames.iterator(); i.hasNext();){
            variableName = (String) i.next();
            type = experiment.getManipulationFor(variableName).getType();
            if(type == ManipulationType.LOCKED){
               setLocked(im, experiment, variableName);
            }else if (type == ManipulationType.RANDOMIZED){
               setRandomized(im, variableName);
            }
        }
        return im;
 */
    }

    // in interface to lock a value user specifies one term
    // a) constant term for a varialbe
    // then we
    // b) set the the distribution of the error term to (i. dist type= normal, ii. mean, iii. variance=0)
    //
    public static void setVariableLocked(SemIm manipulatedSemIm, ExperimentalSetup experiment, String variableName){
        //set the constant term for the variable
        //set the error term to 0, either by setting variance to 0, or getting rid of the error term (can population calculations handle degenerate sem?)
        //set the coefficients of parents to 0
        System.out.println("CorrectManipulatedGraphSemIm not locking"); //$NON-NLS-1$
    }

    /**
     * in a sem im with X=>Z Y=>Z, the Z = coeff_1 * X + coeff_2 * Y + error_Z.
     * To randomize Z, you need to set coeff_1 and coeff_2 = 0;
     *
     * @param manipulatedSemIm
     * @param variableName
     */
    public static void setVariableRandomized(SemIm manipulatedSemIm, String variableName){
        Node parent, var;
        Graph graph = manipulatedSemIm.getSemPm().getGraph();
        var = graph.getNode(variableName);
       // manipulatedSemIm.setp
        List parents = graph.getParents(graph.getNode(variableName));
        for(Iterator i = parents.iterator(); i.hasNext(); ){
            parent = (Node) i.next();
            //0 out the coeff
            manipulatedSemIm.setParamValue(parent, var, 0.0);
        }
    }

}
