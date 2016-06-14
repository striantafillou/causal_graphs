package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;

/**
 * methods to learn SEMs, e.g. PC, Shimizu, etc
 *  
 * @author Gustavo
 *
 */
public interface SemLearningMethod {

	String getName();	//name of the SEM learning method
	
//	PatternWithParameters run(RectangularDataSet dataSet); //estimates a graph
//
//	//sets edgesToEvaluateCoeffs
//	PatternWithParameters run(RectangularDataSet dataSet, PatternWithParameters generatingDag);

	GraphWithParameters run(DataSet dataSet, boolean estimateCoefficients, TestFastIca.PwpPlusGeneratingParameters standardPwpPlusParms);

}
