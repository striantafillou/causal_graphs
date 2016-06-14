package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.fastica.FastICA;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.StatUtils;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Shimizu2006Search implements SemLearningMethod {

	private double alpha;
	public double getAlpha(){
		return alpha;
	}

	static boolean isGraphUsed = false;
	public static boolean isGraphUsed(){
		return isGraphUsed;
	}
	public static void setIsGraphUsed(boolean value){
		isGraphUsed = value;
	}

	//used for caching the Shimizu graph, in case another SemLearningMethod wants to re-use it
	static GraphWithParameters lastShimizuGraph = null;

	//the alpha that was used for creating lastShimizuGraph
	public static Double lastAlpha = null;
	public double getLastAlpha(){
		return lastAlpha;
	}


	Shimizu2006Search(double alpha){
		this.alpha = alpha;
	}

	public String getName() {
		return "Shimizu(alpha="+alpha+")";
	}


	public GraphWithParameters run(DataSet dataSet) {
		lastShimizuGraph = lingamDiscovery_DAG(dataSet, this.alpha);
		setIsGraphUsed(false);
		return lastShimizuGraph;
	}


		//estimates a graph


//	//runs hungarian algorithm to get
//	//translate this matrix problem into a problem about matchings
//
//	//in: the matrix with x|->abs(inv(x)) applied to each entry
//	//out: the row-swapped matrix that has the best assignment, i.e. whose
//	//diagonal is zeroless
//	private static MatCost hungarianBestAssignment(DoubleMatrix2D mat) {
//
//		//this is an n x 2 matrix, i.e. a list of index pairs
//		int[][] assignment = HungarianAlgorithm.hgAlgorithm(convert(mat), "max");
//
//		DoubleMatrix2D matrix = assignment2Matrix(assignment);
//
//		MatCost matCost = new MatCost(0, matrix);
//		return matCost;
//	}

	//in: original matrix, the best assignment
	//out: the swapped matrix

	private static DoubleMatrix2D assignment2Matrix(DoubleMatrix2D mat, int[][] assignment) {

		//assignment[i][1] contains the proper place of row i

		//We are going to sort the rows of mat in such a way that row i ends up in row j
		//iff we have (i, j) in assignment
//		e.g. if we have
//		array(1,3) = 0.86
//		array(2,1) = 0.44
//		array(3,5) = 0.98
//		array(4,2) = 0.93
//		array(5,4) = 0.98
		// then 1st row moves to 3rd row, 2nd row moves to 1st row, 3rd row moves to 5th row, etc.

		DoubleMatrix2D swappedMat = new DenseDoubleMatrix2D(mat.rows(), mat.columns());

		for (int i=0; i<mat.rows(); i++){//for each row in mat
			int newRowIndex = assignment[i][1];
			swappedMat.viewRow(newRowIndex).assign(mat.viewRow(i));
		}

		return swappedMat;
	}


	//the method that calls assign() twice could be a problem for the negative coefficients
	private static DoubleMatrix2D permuteZerolessDiagonal(DoubleMatrix2D w) {

		DoubleMatrix2D temp = new DenseDoubleMatrix2D(w.rows(),w.columns());
		temp.assign(w);
		temp.assign(cern.jet.math.Functions.inv);
		temp.assign(cern.jet.math.Functions.abs);

		//this is an n x 2 matrix, i.e. a list of index pairs
		int[][] assignment = Hungarian.hgAlgorithm(MatrixUtils.convert(temp), "min");

		return assignment2Matrix(w, assignment);

//		return bestAssignment(temp).matrix.assign(cern.jet.math.Functions.inv);
	}




/**
 *
 * @param dataSet
 * @return the unpermuted coefficient matrix, B-hat
 */
	public static ColtDataSet lingamDiscovery_Bhat(DataSet dataSet) {
		DoubleMatrix2D A = null, Btilde = null, Bhat = null, W, zldW, zldWprime = null;

		DoubleMatrix2D data = dataSet.getDoubleData().viewDice();
		//System.out.println("data = " + data);

		boolean runColt=false;
		boolean runArray=true;
		try{
			if (runArray){
				double[][] inV = MatrixUtils.convert(data);
				long sTime = (new Date()).getTime();
				FastICA fica = new FastICA(inV, data.rows());

				long eTime = (new Date()).getTime();
//				System.out.println("Array-based ICA took " + (eTime-sTime) + "ms");
//				System.out.println("\nfica.getICVectors(): " + convertToColt(fica.getICVectors()));

				A = MatrixUtils.convertToColt(fica.getMixingMatrix());
			}
			else if(runColt){
//				long sTime = (new Date()).getTime();
//				FastIca fica = new FastIca(inVectors, inVectors.rows());
//
//				long eTime = (new Date()).getTime();
//				System.out.println("Colt-based ICA took " + (eTime-sTime) + "ms");
//				System.out.println("\nfica.getICVectors(): " + fica.getICVectors());
//
//				A = fica.getMixingMatrix();
			}
	//		System.out.println("\nA: " + A);

			W = MatrixUtils.inverse(A);
			int n = W.rows();
			//System.out.println("n = " + n);

			//if W is not square or does not have enough dimension, throw exception
			if (W.rows()!=W.columns()){
				System.out.println("W = " + W);
				new Exception("W is not square!").printStackTrace();
			}

			if (W.rows()!=dataSet.getNumColumns())
				new Exception("W does not have the right number of dimensions!").printStackTrace();


			zldW = permuteZerolessDiagonal(W); //i.e. W~
			//System.out.println("zldW = " + zldW);

			zldWprime = MatrixUtils.normalizeDiagonal(zldW);// W~' = normalizeDiagonal(W~)
			//System.out.println("zldWprime = " + zldWprime);

			for (int i=0; i<zldWprime.rows(); i++){
				//System.out.println("zldWprime.get("+i+","+i+") = " + zldWprime.get(i,i));
			}

			Bhat = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n),1,zldWprime,-1); //B^ = I - W~'
			//System.out.println("Bhat = " + Bhat);

		}
		catch (Exception e){
			e.printStackTrace();
		}

		if (Bhat==null)
			return null;

		DoubleMatrix2D BhatT = Bhat.viewDice();
		return ColtDataSet.makeContinuousData(dataSet.getVariables(), BhatT);
	}



	//if 'mat' can be permuted into a lower-triangular matrix, it returns the permutation
	//otherwise, it returns null
	public static Vector<Integer> dagPermutation(DoubleMatrix2D mat){
//		System.out.println("entered dagPermutation: mat = " + mat);

		Vector<Integer> removedIndices = new Vector<Integer>();

		Vector<Integer> v = new Vector<Integer>();
		while (removedIndices.size()<mat.rows()){
//			System.out.println("dagPermutation: mat = " + mat);
			int allZerosRow = -1;
			//find a row has all zeros
			for (int i=0; i<mat.rows(); i++){
				if (removedIndices.contains(i)) //if this index has been removed, ignore the row
					continue;
				if (containsAllZeros(mat.viewRow(i),removedIndices)){
					allZerosRow = i;
					break;
				}
			}
			if (allZerosRow==-1) {// no such row
//				System.out.println("dagPermutation: failed to permute");
				return null;
			}
			//int actualZeroRowsIndex =
			removedIndices.add(allZerosRow);
			v.add(allZerosRow);

			//the problem now is that removing these rows and columns screws up the indices
			//mat = removeNthRowAndColumn(allZerosRow,mat); //remove the ith row and ith column


		}
//		System.out.println("dagPermutation: returning v = " + v);
		return v;
	}

	public static class Entry implements Comparable<Entry> {
		int row;
		int column;
		double value;

		public Entry(int row, int col, double val){
			this.row = row;
			this.column = col;
			this.value = val;
		}

		/* used for sorting. A entry is smaller than another if its absolute value is smaller.
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry entry) {
			double thisVal = Math.abs(value);
			double entryVal = Math.abs(entry.value);
			return (new Double(thisVal).compareTo(entryVal));
		}

		public String toString(){
			return "[" + row +","+ column + "]:" + value + " ";
		}

	}

	public static Vector<Entry> getEntries(DoubleMatrix2D mat){
		Vector<Entry> entries = new Vector();
		for (int i=0; i<mat.rows(); i++)
			for (int j=0; j<mat.columns(); j++){
				Entry entry = new Entry(i,j,mat.get(i, j));
				entries.add(entry);
			}
		return entries;
	}


	/* the algorithm works by iteratively setting the smallest (by absolute value) non-zero entry to zero,
	 * until swappable by calling 'dagPermutation'
	 */
	//in: the unswapped matrix, as a ColtDataSet
	//out: the swapped matrix, as a ColtDataSet
	public static ColtDataSet iterativeC(ColtDataSet dataSet){
		DoubleMatrix2D mat = dataSet.getDoubleData();
		//System.out.println("mat = " + mat);

		int n = mat.rows();

		Vector<Entry> remainingEntries = getEntries(mat);
		java.util.Collections.sort(remainingEntries);

		//System.out.println("remainingEntries = " + remainingEntries);

		//sort the entries of mat by absolute value
		Vector entryQueue = getNFirst(n*(n+1)/2, remainingEntries); //cache of entries before they get set to zero.

		//set the smallest m(m+1)/2 to zero
		DoubleMatrix2D tempMat = setEntriesToZero(entryQueue, mat);
		remainingEntries.removeAll(entryQueue);
		entryQueue.removeAllElements();

		Vector<Integer> permutation;

		//test
		while (true){ //while not permutable
//			System.out.println("tempMat = " + tempMat);
			permutation = dagPermutation(tempMat);
			if (permutation!=null) //once we have a permutation, we proceed
				break;

			Entry entry = remainingEntries.get(0);

			//add more zeros to tempMat
			tempMat = setEntryToZero(entry,tempMat);
			remainingEntries.remove(0);
		}

		return permute(permutation,dataSet);
	}

	//set the rows, one by one to the arrayth row
	//
	private static ColtDataSet permute(Vector<Integer> permutation, ColtDataSet dataSet) {
		List<Node> resNodes = permute(permutation, dataSet.getVariables());
		DoubleMatrix2D resMatrix = permuteRows(permutation, dataSet.getDoubleData());
		resMatrix = permuteColumns(permutation, resMatrix);

		ColtDataSet resDataSet = ColtDataSet.makeContinuousData(resNodes, resMatrix);

		return resDataSet;
	}

	//permutes the rows of 'mat'
	private static DoubleMatrix2D permuteRows(Vector<Integer> permutation, DoubleMatrix2D mat) {
		DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(),mat.columns());
		for (int i=0; i<mat.rows(); i++){
			DoubleMatrix1D row = mat.viewRow(permutation.get(i));
			m.viewRow(i).assign(row);
		}
		return m;
	}

	//permutes the columns of 'mat'
	private static DoubleMatrix2D permuteColumns(Vector<Integer> permutation, DoubleMatrix2D mat) {
		DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(),mat.columns());
		for (int i=0; i<mat.columns(); i++){
			DoubleMatrix1D col = mat.viewColumn(permutation.get(i));
			m.viewColumn(i).assign(col);
		}
		return m;
	}



	private static List<Node> permute(Vector<Integer> permutation, List<Node> variables) {
		//System.out.println("permute: variables = " + variables);

		List<Node> nodes = new Vector<Node>(variables.size());
		for (int i=0; i<variables.size(); i++)
			nodes.add(null);

		//System.out.println("permute: before, nodes = " + nodes);
		for (int i=0; i<variables.size(); i++)
			nodes.set(i,variables.get(permutation.get(i)));
			//nodes.set(permutation.get(i),variables.get(i));
		//System.out.println("permute: after, nodes = " + nodes);
		return nodes;
	}


	private static DoubleMatrix2D setEntriesToZero(Vector<Entry> entries, DoubleMatrix2D mat) {
		DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(),mat.columns());
		m.assign(mat);
		for (Entry entry : entries)
			m.set(entry.row, entry.column, 0);

		return m;
	}

	private static DoubleMatrix2D setEntryToZero(Entry entry, DoubleMatrix2D mat) {
//		System.out.println("setEntryToZero: before, mat = " + mat);
		DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(),mat.columns());
		m.assign(mat);
		m.set(entry.row, entry.column, 0);
//		System.out.println("setEntryToZero: returning m = " + m);
		return m;
	}



	//returns the n smallest (by absolute value) members of a list
	public static Vector<Entry> getNFirst(int n, Vector<Entry> list) {
		return new Vector(list.subList(0, n));
	}


    public static DoubleMatrix2D removeColumn(DoubleMatrix2D mat, int index) {

        int[] rows = new int[mat.rows()];

        for (int i = 0; i < mat.rows(); i++) {
            rows[i] = i;
        }

        int[] cols = new int[mat.columns() - 1];

        int m = -1;

        for (int i = 0; i < mat.columns(); i++) {
            if (i != index) {
                cols[++m] = i;
            }
        }

        return mat.viewSelection(rows, cols).copy();
    }


    public static DoubleMatrix2D removeRow(DoubleMatrix2D mat, int index) {

        int[] cols = new int[mat.columns()];

        for (int i = 0; i < mat.columns(); i++) {
            cols[i] = i;
        }

        int[] rows = new int[mat.columns() - 1];

        int m = -1;

        for (int i = 0; i < mat.columns(); i++) {
            if (i != index) {
                rows[++m] = i;
            }
        }

        return mat.viewSelection(rows, cols).copy();
    }




	//this is BROKEN!!!!
	private static DoubleMatrix2D removeNthRowAndColumn(int n, DoubleMatrix2D mat) {
		System.out.println("removeNthRowAndColumn: mat = " + mat);

		DoubleMatrix2D result;

		result = removeRow(mat,n);
		//System.out.println("removeNthRowAndColumn: after removeRow, result = " + result );

		result = removeColumn(result,n);
		//System.out.println("removeNthRowAndColumn: after removeColumn, result = " + result );

		return result;

//		int[] rowSel = new int[mat.size()-1];
//
//
//		for (int i=0; i<allZerosRow;i++)
//			rowSel[i]=i;
//		for (int i=allZerosRow; i<mat.rows()-1;i++)
//			rowSel[i]=i+1;
//
//
//		int[] colSel = new int[mat.size()];
//		for (int i=0; i<mat.rows(); i++)
//			colSel[i]=i;
//
//
//		String s="";
//		for (int i=0; i<mat.rows()-1; i++)
//			s += " "+ rowSel[i];
//		System.out.println("rowSel = " + s);
//
//		s="";
//		for (int i=0; i<mat.rows(); i++)
//			s += " "+ colSel[i];
//		System.out.println("colSel = " + s);
//
//
//		System.out.println("removeNthRowAndColumn: returning " + mat.viewSelection(colSel, colSel));
//
//		return mat.viewSelection(rowSel,colSel);

	}


	private static boolean containsAllZeros(DoubleMatrix1D vec, Vector<Integer> removedIndices) {
//		System.out.println("containsAllZeros: vec = " + vec);
		if (vec.size()>20)
			java.lang.System.exit(0);

		for (int i=0; i<vec.size(); i++)
			if (vec.get(i)!=0&&!removedIndices.contains(i)){ //if column has been removed, do nothing.
//				System.out.println("containsAllZeros: returning false");
				return false;
			}
//		System.out.println("containsAllZeros: returning true");
		return true;
	}


	//step 5 of algorithm A from the paper: swaps the rows and columns simultaneously in order to
	//get a lower-triangular matrix
	//must preserve the variable names
	public static ColtDataSet lingamDiscovery_step5(ColtDataSet bhat) {
		return iterativeC(bhat);
	}




	//turns a lower-triangular matrix into a DAG
    public static GraphWithParameters makeDagWithParms(ColtDataSet ltDataSet) {

    	DoubleMatrix2D ltMat = ltDataSet.getDoubleData(); //lower-triangular matrix
    	int n = ltMat.rows();

    	List<Node> variables = ltDataSet.getVariables();
    	Dag dag = new Dag(variables);

    	GraphWithParameters dwp = new GraphWithParameters(dag);

    	//adding edges to 'dag', and populating 'weightHash'
    	for (int i=0; i<ltMat.rows(); i++)
    		for(int j=0; j<i; j++){
    			if (ltMat.get(i, j)!=0){
    				Edge edge = new Edge(variables.get(i), variables.get(j),
    						Endpoint.TAIL, Endpoint.ARROW);
    				dwp.getGraph().addEdge(edge);

    				dwp.getWeightHash().put(edge,ltMat.get(i, j));
    			}
    		}

		return dwp;
	}

	public GraphWithParameters lingamDiscovery_DAG(DataSet dataSet) {
		return lingamDiscovery_DAG(dataSet, this.alpha);
	}



/*
 * dataSet format: columns are variables
 */
	public static GraphWithParameters lingamDiscovery_DAG(DataSet dataSet, double alpha) {

		//normalize data set
//		dataSet = normalizeVariance(dataSet);

		ColtDataSet Bhat = Shimizu2006SearchOld.lingamDiscovery_Bhat(dataSet);
		System.out.println("before pruning, Bhat = " + Bhat); //Bhat corresponds to a complete graph

		if (Bhat==null) {
			System.out.println("ICA throws an exception!");
			return null;
		}

		//do significance testing to decide which edges are zero, i.e. pruning
		//int nSamples = new Double(java.lang.Math.floor(java.lang.Math.sqrt(dataSet.getDoubleData().rows())/2)).intValue();

//		pruneEdges(Bhat,dataSet,0.05,nSamples);
//		System.out.println("after pruning, Bhat = " + Bhat);

		ColtDataSet B = Shimizu2006SearchOld.lingamDiscovery_step5(Bhat); //B is the lower-triangular version of Bhat
		System.out.println("before pruning, B = " + B);

		//do we need to swap the dataset as well?
		int nSamples = 15;
		pruneEdges(B,dataSet,alpha,nSamples);

		System.out.println("after pruning, B = " + B);

		GraphWithParameters icaGraph = makeDagWithParms(B); //icaDag is the DAG made from Bhat's nonzero entries

		lastShimizuGraph = icaGraph;
		isGraphUsed = false;

		return icaGraph;
	}


	private static void pruneEdges(ColtDataSet B, DataSet dataSet, double alpha, int nSamples) {
		pruneEdgesBySampling(B, dataSet, alpha, nSamples, true);
	}

	/* Given a data set, this method prunes edges that don't pass the significance test.
	 *
	 * supports two methods:
	 * * partitioning the data
	 * * bootstrap sampling
	 *
	 * This method assumes that 'B' and 'dataSet' have variables in the same order.
	 */
	private static void pruneEdgesBySampling(ColtDataSet B, DataSet dataSet,
			double alpha, int nSamples, boolean isBootstrapSampling) {

		//we have one B-hat for each piece of the data
		ColtDataSet[] bHats = new ColtDataSet[nSamples];

		//get nSample samples from the data
		for (int i=0; i<nSamples; i++){
			System.out.print("i = ");
			System.out.print( "" + i + " ");


			ColtDataSet bHat = null;
			while(bHat==null){
				DataSet currentBootstrapSample = isBootstrapSampling ? getBootstrapSample(dataSet) : //randomly draw from the dataSet, with replacement
					getSubsetOfDataSet(dataSet, nSamples, i); //divide the data set into nSamples pieces
				//System.out.println(" currentBootstrapSample = " + currentBootstrapSample);
				//					System.out.println("i="+i);

				bHat = lingamDiscovery_Bhat(currentBootstrapSample);
			}
			bHats[i] = bHat;
			//			System.out.println("bHats["+i+"] = " + bHats[i]);
		}

		System.out.println("");

		//for each entries in Bhat, record its mean and s.d.

		//mean
		DoubleMatrix2D meanMat = new DenseDoubleMatrix2D(B.getDoubleData().rows(),B.getDoubleData().columns());

		for (int i=0; i<B.getDoubleData().rows(); i++)
			for (int j=0; j<B.getDoubleData().columns(); j++){
				double sum=0;
				for (int z=0; z<nSamples; z++)
					sum+=bHats[z].getDouble(i, j);
				meanMat.set(i, j, sum/nSamples);
			}
		System.out.println("meanMat = " + meanMat);



		//standard error of each coefficient
		DoubleMatrix2D sdMat = new DenseDoubleMatrix2D(B.getDoubleData().rows(),B.getDoubleData().columns());
		for (int i=0; i<B.getDoubleData().rows(); i++)
			for (int j=0; j<B.getDoubleData().columns(); j++){
				double varianceSum=0;
//				System.out.println("i = " + i + ", j = " + j);
				for (int z=0; z<nSamples; z++){
//					System.out.println("estimate from this sample = " + bHats[z].getDouble(i, j));
					varianceSum+=java.lang.Math.pow((bHats[z].getDouble(i, j)-meanMat.get(i, j)),2);
				}
				double variance = varianceSum/nSamples;
				sdMat.set(i, j, java.lang.Math.pow(variance, 0.5));
			}

		System.out.println("sdMat = " + sdMat);

		GaussianConfidenceInterval gci = new GaussianConfidenceInterval(alpha);
//		double pruneFactor = 1;

		for (int i=0; i<B.getDoubleData().rows(); i++)
			for (int j=0; j<B.getDoubleData().columns(); j++){

//				System.out.println("thinking about pruning edge ("+i+","+j+"): mean = " +
//						meanMat.get(i, j) + ", stdev = " + sdMat.get(i, j));
				// if don't reject the zero hypothesis...
				if (!gci.test(0.0, meanMat.get(i, j), sdMat.get(i, j), nSamples)){
//					System.out.println("pruning: within interval");

//					System.out.println("Can't reject the zero hypothesis: pruning the " + B.getDoubleData().get(i, j));
					//here, .get(0,0) refers to the coefficient of X1-->X1

				//if (java.lang.Math.abs(meanMat.get(i, j))<sdMat.get(i, j)*pruneFactor){
//					System.out.println("setting "+ B.getDoubleData().get(i, j) + " to zero.");
//					System.out.println("mean = " + meanMat.get(i, j) + "   sd = " + sdMat.get(i, j));
//					String printo="";
//					for (int z=0; z<nSamples; z++)
//						printo += " " + bHats[z].getDouble(i,j);
//					System.out.println("entries: "+printo);

					List<Node> swappedVariables = B.getVariables();
					List<Node> originalVariables = dataSet.getVariables();
					int xi = findColumn(i,swappedVariables,originalVariables); //gives us the location of the node named "X_i+1"
					int xj = findColumn(j,swappedVariables,originalVariables);

//					System.out.println("pruning: setting ("+xi+","+xj+") to 0.   Value was: " + B.getDoubleData().get(xi, xj));

					B.setDouble(xi, xj, 0); //set it to zero
				}
				else{
//					System.out.println("not pruning: outside interval");

				}

			}
	}

	/*
	 * returns the number of distinct rows of a bootstrapped sample
	 */
	private static int diversity(DataSet currentDataSet) {
		int n=0;
		DoubleMatrix2D mat = currentDataSet.getDoubleData();
		for (int i=0; i<mat.rows(); i++){
			if (!isContained(mat.viewRow(i), mat.viewPart(0, 0, i, mat.columns()))) //if new point
				n++;
		}
		return n;
	}


	private static boolean isContained(DoubleMatrix1D vec, DoubleMatrix2D mat) {
		for (int i=0; i<mat.rows(); i++){
			if (mat.viewRow(i).equals(vec))
				return true;
		}
		return false;
	}


/**
 * iterates through the columns of variables to find the one that matches the originalVariables.get(index)
 * @param variables
 * @return
 */
	private static int findColumn(int index, List<Node> variables, List<Node> originalVariables) {
		int i=0;
		for (Node node : variables){
			if (node.getName().equals(originalVariables.get(index).getName())) //"X"+(index+1)))
				return i;
			i++;
		}
		System.out.println(" index = " + index + " variables = " + variables + " originalVariables = " + originalVariables);
		new Exception("not found").printStackTrace();
		return -1;
	}


	/**
	 * returns a subset of the data points
	 */
	private static DataSet getSubsetOfDataSet(DataSet dataSet, int pieces, int pieceIndex) {
		DoubleMatrix2D mat = dataSet.getDoubleData();

		//System.out.println("mat = " + mat);
		int pieceSize = mat.rows() / pieces;

		DoubleMatrix2D res = mat.viewPart(pieceSize*pieceIndex, 0, pieceSize, mat.columns());
		//System.out.println("res = " + res);

		return ColtDataSet.makeContinuousData(dataSet.getVariables(), res);
	}

	private static DataSet getBootstrapSample(DataSet dataSet) {
		DataSet sample = null;
		int diversity = 0;
		while (diversity<=dataSet.getNumColumns()){
			sample = makeBootstrapSample(dataSet);
			diversity = diversity(sample);
//			System.out.println("diversity = " + diversity);
		}
		return sample;
	}



	/*
	 * creates a "bootstrap sample" of size 'sampleSize' from a dataSet.
	 */
	private static DataSet makeBootstrapSample(DataSet dataSet) {
		DoubleMatrix2D mat = dataSet.getDoubleData();
		DoubleMatrix2D bootstrapSample = new DenseDoubleMatrix2D(mat.rows(), mat.columns());

		int dataSize = mat.rows();

		int sampleSize = dataSize; //by default

		//make a sample, with replacement
		for(int i=0; i<sampleSize; i++){
			DoubleMatrix1D point = mat.viewRow(StatUtils.dieToss(dataSize));
			bootstrapSample.viewRow(i).assign(point);
		}
//		System.out.println("bootstrapSample = " + bootstrapSample);
		return ColtDataSet.makeContinuousData(dataSet.getVariables(), bootstrapSample);
	}



	/**
	 * format: columns are variables
	 *
	 */
	private static DataSet normalizeVariance(DataSet dataSet) {
		DoubleMatrix2D mat = dataSet.getDoubleData();
		DoubleMatrix2D res = new DenseDoubleMatrix2D(mat.rows(),mat.columns());

		for (int j=0; j<mat.columns(); j++){ //for each variable

			//first compute the mean
			double sum=0;
			for (int i=0; i<mat.rows(); i++){
				sum+=mat.get(i,j);
			}
			double mean = sum/mat.rows();

			double sumDistSq=0;
			for (int i=0; i<mat.rows(); i++){
				sumDistSq+=java.lang.Math.pow((mat.get(i,j)-mean),2);
			}
			double variance = sumDistSq/mat.rows();
			double sd = java.lang.Math.pow(variance,0.5);

			//normalize
			for (int i=0; i<mat.rows(); i++){
				res.set(i, j, mat.get(i, j)/sd);
			}
		}

		return ColtDataSet.makeContinuousData(dataSet.getVariables(), res);
	}

	/**
	 * doesn't do anything to edgesToEvaluateCoeffs
	 */
//	public PatternWithParameters run(RectangularDataSet dataSet, List<Edge> edgesToEvaluateCoeffs) {
//		return run(dataSet);
//	}

	public GraphWithParameters run(DataSet dataSet, boolean estimateCoefficients, TestFastIca.PwpPlusGeneratingParameters standardPwpPlusParms) {
		return run(dataSet);
	}

}
