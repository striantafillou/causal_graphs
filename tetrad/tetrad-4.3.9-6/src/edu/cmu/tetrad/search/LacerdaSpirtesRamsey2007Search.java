package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.fastica.FastICA;
import edu.cmu.tetrad.util.MatrixUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class LacerdaSpirtesRamsey2007Search implements CyclicDiscoveryMethod {

	public String getName() {
		return "LacerdaSpirtesRamsey2007";
	}
	//nth moment of X is defined as E(X^n)
	public double estimateNthMoment(int n, DoubleMatrix1D vec){
		double sum=0;
		for (int i=0; i<vec.size(); i++){
			sum+=java.lang.Math.pow(vec.get(i), n);
		}
		return sum/vec.size();
	}

	HashMap<Integer, List<Double>> moments;

	/* returns the Nth moment of X_i, given the Nth moments of the error terms, and their
	 * mixing proportions.
	 * given top moment n, cache the 1st to nth moments for each error-term
	 */
	public static double impliedNthMoment(DoubleMatrix1D mixingCoeffs, DoubleMatrix1D errorTermsNthMoments, int n){
		double sum=0;

		//this is hard coded for the 4th moment, and for 2 error terms
		//E((a E1 + b E2)^4) = a^4 E(E1^4) + b^4 E(E2^4) + 3 E(X^3Y) + 3 E(XY^3) + 6 E(X^2 Y^2)
		//
		//if they are independent and zero-centered, terms with odd powers go to zero...
		// = a^4 E(E1^4) + b^4 E(E2^4) + 6 E(X^2 Y^2)
		for (int j=0; j<mixingCoeffs.size(); j++){
			double coeff = mixingCoeffs.get(j);
			sum += java.lang.Math.pow(coeff, n) * errorTermsNthMoments.get(j);
		}

//		sum += impliedSecondMoment();
		//
		//this is incorrectly ignoring the terms with even powers
		//nth (a E1 + b E2) = a^n nth(E1) + b^n nth(E2)

		return sum;
	}

	public static double impliedFourthMoment(DoubleMatrix1D mixingCoeffs, DoubleMatrix1D errorTermsNthMoments){
		return impliedNthMoment(mixingCoeffs, errorTermsNthMoments, 4);
	}


	/**
	 * first, generate all permutations compatible with the output of ICA
	 * each one corresponds to a model
	 * then estimate the fit for each model
//	 * @param iMinusB
	 */
	public GraphWithParameters run(DataSet dataSet//, DoubleMatrix2D iMinusB
			) {
		DoubleMatrix2D ica_A = null, Btilde = null, Bhat = null, ica_W, normalizedZldW = null;
		List<PermutationMatrixPair> zldPerms;

		DoubleMatrix2D data = dataSet.getDoubleData().viewDice();
		//System.out.println("data = " + data);

		GraphWithParameters bestGraph = null;

//		boolean runColt=false;
//		boolean runArray=true;
		try{
//			if (runArray){
				double[][] inV = MatrixUtils.convert(data);
				long sTime = (new Date()).getTime();
				FastICA fica = new FastICA(inV, data.rows());

				long eTime = (new Date()).getTime();
//				System.out.println("Array-based ICA took " + (eTime-sTime) + "ms");
//				System.out.println("\nfica.getICVectors(): " + convertToColt(fica.getICVectors()));

				ica_A = MatrixUtils.convertToColt(fica.getMixingMatrix());
//			}
//			else if(runColt){
//			}
	//		System.out.println("\nA: " + A);

			ica_W = MatrixUtils.inverse(ica_A);
			int n = ica_W.rows();
			//System.out.println("n = " + n);

			System.out.println("W = " + ica_W);

			//if W is not square or does not have enough dimensions, throw exception
			if (ica_W.rows()!=ica_W.columns()){
				new Exception("W is not square!").printStackTrace();
			}

			if (ica_W.rows()!=dataSet.getNumColumns())
				new Exception("W does not have the right number of dimensions!").printStackTrace();


//			DoubleMatrix2D covarianceMatrix = new CovarianceMatrix(dataSet).getMatrix();
//			DoubleMatrix2D normalizedInvCovMatrix = MatrixUtils.normalizeDiagonal(MatrixUtils.inverse(covarianceMatrix));
//			System.out.println("covarianceMatrix = " + covarianceMatrix);

            //this is the heart of our method:
            findCandidateModels(dataSet.getVariables(), ica_W, data, n, true);

		}
		catch (Exception e){
			e.printStackTrace();
		}

		return bestGraph;
	}

	public static boolean allEigenvaluesAreSmallerThanOneInModulus(DoubleMatrix2D mat) {

		EigenvalueDecomposition dec = new EigenvalueDecomposition(mat);
		DoubleMatrix1D realEigenvalues = dec.getRealEigenvalues();
		DoubleMatrix1D imagEigenvalues = dec.getImagEigenvalues();

		boolean allEigenvaluesSmallerThanOneInModulus=true;
		for (int i=0; i<realEigenvalues.size(); i++){
			double realEigenvalue = realEigenvalues.get(i);
			double imagEigenvalue = imagEigenvalues.get(i);
			System.out.println("eigenvalue #"+i+" = " + realEigenvalue + "+" + imagEigenvalue + "i");
			double argument = arg(realEigenvalue, imagEigenvalue);
			System.out.println("eigenvalue #"+i+" has argument = " + argument);

			double modulus = java.lang.Math.sqrt(java.lang.Math.pow(realEigenvalue, 2) +
					java.lang.Math.pow(imagEigenvalue, 2));
			System.out.println("eigenvalue #"+i+" has modulus = " + modulus);

			double modulusCubed = java.lang.Math.pow(modulus, 3);
			System.out.println("eigenvalue #"+i+" has modulus^3 = " + modulusCubed);

			if (modulus>=1){
				allEigenvaluesSmallerThanOneInModulus = false;
//				break;
			}
		}
		return allEigenvaluesSmallerThanOneInModulus;
	}


	private static double arg(double realPart, double imagPart) {
		return java.lang.Math.atan(imagPart / realPart);
	}

//given the W matrix, outputs the list of SEMs consistent with the observed distribution.
    public static void findCandidateModels(List<Node> variables, DoubleMatrix2D matrixW, DoubleMatrix2D data, int n, boolean approximateZeros) {

		System.out.println("variables = " + variables);

		DoubleMatrix2D normalizedZldW;
		List<PermutationMatrixPair> zldPerms;

		//returns <permutations, matrix> pairs
		zldPerms = zerolessDiagonalPermutations(matrixW, approximateZeros);

//		DoubleMatrix2D impliedErrorTerms = matrixW.zMult(data, null);
//
//		for (int row=0; row<data.rows(); row++){ //for each observed
//			double fourthMoment = estimateNthMoment(data.viewRow(row),4);
//			System.out.println("4th moment of observed term #" + row + " = " + fourthMoment);
//		}

		List<Double> losses = new Vector();
		int i=0;

		//for each W~, compute a candidate B, and score it

		for (PermutationMatrixPair zldPerm : zldPerms){

			System.out.println();
			System.out.println("------------------------");
			System.out.println("---- candidate #"+(i+1)+" ------");
			System.out.println();

			//DoubleMatrix2D zldWmatrix = zldPerm.matrixW;
			List<Integer> zldWpermutation = zldPerm.permutation;
			normalizedZldW = MatrixUtils.normalizeDiagonal(zldPerm.matrixW);

//			System.out.println("zldPerm.matrixW = " + zldPerm.matrixW);

//			zldPerm.matrixA = MatrixUtils.inverse(zldPerm.matrixW);
//			System.out.println("zldPerm.matrixA = " + zldPerm.matrixA);

			if (data!=null){

//				//use this A to reconstruct the X
//				//then look at the 4th moments
//				DoubleMatrix2D impliedX = zldPerm.matrixA.zMult(impliedErrorTerms, null);
//
//				for (int row=0; row<data.rows(); row++){ //for each observed
//					double fourthMoment = estimateNthMoment(impliedX.viewRow(row),4);
//					System.out.println("implied 4th moment of observed term #" + row + " = " + fourthMoment);
//				}
//
//				for (int row=0; row<data.rows(); row++){
//					double diff = difference(impliedX.viewRow(row), data.viewRow(row));
//					System.out.println("diff = " + diff);
//				}

				//compute error-terms 4th moments
//				DoubleMatrix1D errorTerms4thMoments = new DenseDoubleMatrix1D(n);
//				for (int e=0; e<n; e++){
//					double fourthMoment = estimateNthMoment(impliedErrorTerms.viewRow(e),4);
//					errorTerms4thMoments.set(e, fourthMoment);
//					System.out.println("4th moment of error term #" + e + " = " + fourthMoment);
//				}
//				for (int row=0; row<zldPerm.matrixA.rows(); row++){	//for each row of A
//					double fourthMoment = impliedFourthMoment(zldPerm.matrixA.viewRow(row),
//							errorTerms4thMoments);
//					System.out.println("4th moment of row " + row + " of A = " + fourthMoment);
//				}
////				RectangularDataSet matrixBhat
			}

			zldPerm.matrixBhat = computeBhatMatrix(normalizedZldW, n, variables); //B~ = I - W~
			System.out.println("matrixBhat = " + zldPerm.matrixBhat);
			boolean isShrinkingMatrix = allEigenvaluesAreSmallerThanOneInModulus(zldPerm.matrixBhat.getDoubleData());
			System.out.println("isShrinkingMatrix = " + isShrinkingMatrix);


			//			System.out.println("determinant = " + new Algebra().det(zldPerm.matrixBhat.getDoubleData()));

			GraphWithParameters graph = new GraphWithParameters(zldPerm.matrixBhat);

						System.out.println("graph:\n" + graph);

			//				System.out.println("zldWpermutation = " + zldWpermutation);

			//A~ = invert W~
//			zldPerm.matrixA = MatrixUtils.inverse(zldPerm.matrixW);
//
//			System.out.println("COV = " + zldPerm.matrixA.zMult(zldPerm.matrixA.viewDice(), null));
//				//try every permutation ica_A
//
//				//instead of taking the inverse for every permutation,
//				//a more efficient way is to permute the rows of A
//				//... not sure how yet
//				DoubleMatrix2D rMatrix = iMinusB.zMult(permuted_ica_A, null);
//				System.out.println("rMatrix = " + rMatrix);
//

//							System.out.println("normalizedInvCovMatrix  = " + normalizedInvCovMatrix );
//			System.out.println("normalizedZldW = " + normalizedZldW);

//				System.out.println("impliedErrorTerms = " + impliedErrorTerms);
			//ContrastFunction conFunction;
//			double loss = estimateNthMoment(impliedErrorTerms,4); //conFunction.function(impliedErrorTerms);
//
//
//			//nonGaussianity(impliedErrorTerms);
//				//sumSquaredDifferences(normalizedInvCovMatrix, normalizedZldW); //sum of differences squares
//
//			System.out.println("loss = " + loss);

			//
//				losses.add(loss);
				i++;
		}
		System.out.println("------------------------");

//			int smallestLossIndex = argmin(losses);
//			PermutationMatrixPair bestPermMatrixPair = zldPerms.get(smallestLossIndex);
//
//			System.out.println("best candidate B = " + bestPermMatrixPair.matrixBhat);

		//			RectangularDataSet bestBmatrix = computeBmatrix(bestPermMatrixPair.matrixW,n, dataSet.getVariables());
		//pick the one with smallest score
//			bestGraph =
//				new PatternWithParameters(bestBmatrix);

		System.out.println("There are " + zldPerms.size() + " candidates.");


	}

	private static double difference(DoubleMatrix1D data1, DoubleMatrix1D data2) {
		double sum=0;
		for (int i=0; i<data1.size(); i++){
			double eltDiff = data1.get(i) - data2.get(i);
			sum+=java.lang.Math.pow(eltDiff, 2);
		}
		return sum;
	}


	private double estimateNthMoment(DoubleMatrix2D mat, int n) {
		double sum=0;
		for (int i=0; i<mat.rows(); i++){
			double variableNthMoment = estimateNthMoment(mat.viewRow(i), n);
//			System.out.println(" variableNthMoment for row "+i+" = " + variableNthMoment);
			sum+=variableNthMoment;
		}
		return sum;
	}

	//control for variance?
	private static double estimateNthMoment(DoubleMatrix1D vec, int n) {
		double sum=0;
		for (int j=0; j<vec.size(); j++){
			sum+= java.lang.Math.pow(vec.get(j), n);
		}
		return sum;
	}

	//	B^ = I - W~'
	private static DataSet computeBhatMatrix(DoubleMatrix2D normalizedZldW, int n, List<Node> nodes){//, List<Integer> perm) {
		DoubleMatrix2D mat = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n),1,normalizedZldW,-1);
//		List<Node> nodes = makeNodes(perm);
//		return mat;
		return ColtDataSet.makeContinuousData(nodes, mat);
	}

	private DoubleMatrix2D permuteRows(int[] zldWpermutation, DoubleMatrix2D ica_A) {
		for (int i=0; i<zldWpermutation.length; i++){

		}
		return null;
	}

	private int argmin(List<Double> scores) {
		int minIndex=0;
		double min=scores.get(0);
		for (int i=0; i<scores.size(); i++){
			double value = scores.get(i);
			if (value<min){
				minIndex = i;
				min = value;
			}
		}
		return minIndex;
	}

	private double sumSquaredDifferences(DoubleMatrix2D m1, DoubleMatrix2D m2) {
		int n = m1.rows();
		int m = m1.columns();
//		System.out.println("m = " + m + "  n = " + n);
		double sum=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				double diff = m1.get(i, j) - m2.get(i, j);
//				System.out.println("diff("+i+","+j+ ") = " + diff);
				sum+=java.lang.Math.pow(diff,2);
			}
		}
		return sum;
	}

	private static List<PermutationMatrixPair> zerolessDiagonalPermutations(DoubleMatrix2D ica_W, boolean approximateZeros) {

		List<PermutationMatrixPair> permutations = new Vector<PermutationMatrixPair>();

		if (approximateZeros){
			System.out.println("before pruning, ica_W = " + ica_W);
            setInsignificantEntriesToZero(ica_W, 0.05);
            System.out.println("after pruning, ica_W = " + ica_W);
		}

//		sortMostNonZeroFirst(ica_W);

		//find assignments
		List<List<Integer>> nRookAssignments = nRookRowAssignments(ica_W);

//		System.out.println("the zeroless-diagonal permutations of ica_W are  " + nRookAssignments);


		//for each assignment, add the corresponding permutation to 'permutations'
		for (List<Integer> permutation : nRookAssignments){
			PermutationMatrixPair permMatrixPair = new PermutationMatrixPair();
			permMatrixPair.permutation = permutation;
			permMatrixPair.matrixW = permuteRows(ica_W, permutation);
//			System.out.println("adding to permutations: " + permMatrixPair.matrixW);
			permutations.add(permMatrixPair);
		}

		return permutations;
	}

	private List<Integer> inverse(List<Integer> permutation) {
		System.out.println("inverting a permutation: " + permutation);
		int[] inv = new int[permutation.size()];
		for (int i=0; i<permutation.size(); i++){
			inv[permutation.get(i)] = i;
		}
		return makeVector(inv);
	}

	private List<Integer> makeVector(int[] array) {
		List<Integer> v = new Vector<Integer>();
		for (int i=0; i<array.length; i++){
			v.add(array[i]);
		}
		return v;
	}

	/**
	 * uses the thresholding criterion
	 * @param mat
	 */
	private static void setInsignificantEntriesToZero(DoubleMatrix2D mat, double threshold) {
		int n = mat.rows();
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (java.lang.Math.abs(mat.get(i, j))<threshold)
					mat.set(i, j, 0);
			}
		}
	}


	private DoubleMatrix2D permuteRowsAndColumns(DoubleMatrix2D mat, List<Integer> permutation) {
		return permuteRows(permuteRows(mat, permutation), permutation);
	}

	private DoubleMatrix2D permuteColumns(DoubleMatrix2D mat, List<Integer> permutation) {
//		System.out.println("permuteColumns: mat = " + mat);
		int n = mat.columns();

		DoubleMatrix2D permutedMat = new DenseDoubleMatrix2D(n,n);
		for (int j=0; j<n; j++){
			DoubleMatrix1D fromColumn = mat.viewColumn(j);
//			System.out.println("fromColumn = " + fromColumn);
			int toColumnIndex = permutation.get(j);
			DoubleMatrix1D toColumn = permutedMat.viewColumn(toColumnIndex);
//			System.out.println("toColumn = " + toColumn);
			toColumn.assign(fromColumn);

		}
		return permutedMat;
	}

	private static DoubleMatrix2D permuteRows(DoubleMatrix2D mat, List<Integer> permutation) {
		int n = mat.columns();

		DoubleMatrix2D permutedMat = new DenseDoubleMatrix2D(n,n);
		for (int j=0; j<n; j++){
			DoubleMatrix1D row = mat.viewRow(j);
			permutedMat.viewRow(permutation.get(j)).assign(row);
		}
		return permutedMat;
	}




	/**
	 * returns all zeroless-diagonal column-permutations
	 *
	 * for each nonzero in the first column, we call 'nRookAssignments'
	 * recursively.
	 *
	 * @param mat
	 * @return
	 *
	 * x 0 x
	 * x x 0
	 * 0 x x
	 */
	public static List<List<Integer>> nRookColumnAssignments(DoubleMatrix2D mat) {
		int n = mat.rows();
		List<Integer> allRows = makeAllRows(n);
		return nRookColumnAssignments(mat, allRows);
	}

    //solve the n-rooks problem, by depth-first search
    public static List<List<Integer>> nRookRowAssignments(DoubleMatrix2D mat) {
		return nRookColumnAssignments(mat.viewDice());
	}



	private static List<Integer> makeAllRows(int n) {
		List l = new Vector();
		for (int i=0; i<n; i++){
			l.add(i);
		}
		return l;
	}

	/** [0,2,1] ->
	 *
	 *  1 0 0
	 *  0 0 1
	 *  0 1 0
	 *
	 * @param perm
	 * @return
	 */
	public static DoubleMatrix2D displayNRookAssignment(List<Integer> perm){
		int n = perm.size();
		DoubleMatrix2D mat = new DenseDoubleMatrix2D(n,n);
		for (int j=0; j<n; j++){
			mat.set(perm.get(j), j, 1);
		}
		return mat;
	}

	public static List<List<Integer>> nRookColumnAssignments(DoubleMatrix2D mat,
			List<Integer> availableRows) {
//		System.out.println(" mat = " + mat);
//		System.out.println(" availableRows = " + availableRows);
		List concats = new Vector();

		int n = availableRows.size();

		if (mat.columns()>1){

			for (int i=0; i<n; i++){
				int currentRowIndex = availableRows.get(i);
				if (mat.get(currentRowIndex, 0)!=0){ //what to do in the last column?
					Vector<Integer> newAvailableRows = (new Vector<Integer>(availableRows));
					newAvailableRows.removeElement(currentRowIndex);
					DoubleMatrix2D subMat = mat.viewPart(0, 1, mat.rows(), mat.columns()-1);
					List<List<Integer>> allLater = nRookColumnAssignments(subMat, newAvailableRows);
					for (List<Integer> laterPerm : allLater) {
						laterPerm.add(0,currentRowIndex);
						concats.add(laterPerm);
					}
				}
			}

		}
		else{ //1 column left
			for (int i=0; i<n; i++){
				int currentRowIndex = availableRows.get(i);
				if (mat.get(currentRowIndex, 0)!=0){
					List<Integer> l = new Vector();
					l.add(currentRowIndex);
					concats.add(l);
				}
			}

		}
//		System.out.println("for mat "+ mat +" and availableRows = "+ availableRows +", returning concats = " + concats );
		return concats;
	}

}
