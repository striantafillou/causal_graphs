package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.DataSet;

import java.util.List;

public class PermutationMatrixPair {

	public DoubleMatrix2D matrixW;
	public DataSet matrixBhat;
	public DoubleMatrix2D matrixA;
	
	public List<Integer> permutation;
	
}
