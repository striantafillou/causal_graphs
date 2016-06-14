package edu.cmu.tetrad.manip.population;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.manip.experimental_setup.ExperimentalSetup;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;
import edu.cmu.tetrad.manip.manipulated_graph.ManipulatedGraph;

/**
 * Created by IntelliJ IDEA.
 * User: Matthew Easterday
 * Date: Oct 25, 2003
 * Time: 5:07:12 PM
 * To change this template use Options | File Templates.
 *
 * This class describes the Bayes IM graph model of the correct manipulated
 * graph.
 */
public class CorrectManipulatedGraphBayesIm {

    /**
     * @return the manipulated Bayes IM graph given the correct Bayes IM and the
     * experimental setup.
     */
    public static MlBayesIm createIm( BayesIm correctIm, ExperimentalSetup experiment) {
        MlBayesIm IM;
        Dag mGraph = new Dag(new ManipulatedGraph(correctIm.getDag(), experiment));

        if(mGraph == null){
            throw new NullPointerException("Graph has cycles"); //$NON-NLS-1$
        }
        BayesPm pm = new BayesPm(mGraph, correctIm.getBayesPm());
        //this line is not working when you have manipulations
        IM = new MlBayesIm(pm, correctIm, MlBayesIm.MANUAL);
        //change the IM based on locked and randomized variables in the
        //quantitative experimental setup
        String [] variableNames = experiment.getVariableNames();
        VariableManipulation type;
        String variableName;
        for(int i = 0; i < variableNames.length; i++){
            variableName = variableNames[i];
            type = experiment.getVariable(variableName).getManipulation();
            if(type == VariableManipulation.LOCKED){
               setVariableLocked(IM, experiment, variableName);
            }else if (type == VariableManipulation.RANDOMIZED){
               setVariableRandomized(IM, variableName);
            }else if (type == VariableManipulation.NONE){
                //do nothing
            }
        }
        return IM;
    }



    private static void setVariableLocked(BayesIm IM, ExperimentalSetup experimentQN, String variableName) throws IllegalArgumentException{
        Node node = IM.getNode(variableName);
        int nodeIndex = IM.getNodeIndex(node);

        //if this node is  lock, make sure that it has no parents
        int numParents = IM.getNumParents(nodeIndex);
        if(numParents != 0){
            throw new IllegalArgumentException(variableName + " should be locked and have no parents"); //$NON-NLS-1$
        }

        String lockedValue = experimentQN.getVariable(variableName).getLockedAtValue();
        int valueIndex = IM.getBayesPm().getCategoryIndex(node, lockedValue);

        //make all the jointDistributionProbabilities for the given variable 0
        for(int i = 0; i < IM.getBayesPm().getNumCategories(node); i++){
            IM.setProbability(nodeIndex, 0, i, 0);
        }
        //set the locked value to 1.0
        IM.setProbability(nodeIndex, 0, valueIndex, 1.0);
    }

    private static void setVariableRandomized(BayesIm IM, String variableName){
        Node node = IM.getNode(variableName);
        int nodeIndex = IM.getNodeIndex(node);
        int numValues = IM.getNumColumns(nodeIndex);

        for(int i = 0; i < IM.getBayesPm().getNumCategories(node); i++){
            IM.setProbability(nodeIndex, 0, i, 1.0/((double)numValues));
        }
    }

}
