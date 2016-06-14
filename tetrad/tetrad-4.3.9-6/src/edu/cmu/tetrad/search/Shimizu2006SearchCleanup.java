package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.fastica.FastICA;
import edu.cmu.tetrad.util.MatrixUtils;

import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Algorithms implementing Shimizu ???.
 *
 * @author Gustavo Lacerda
 */
public final class Shimizu2006SearchCleanup {

    //===============================PUBLIC METHODS======================//

    public static ColtDataSet lingamDiscoveryBhat(DataSet dataSet) {
        DoubleMatrix2D A = null, Btilde = null, Bhat = null, W, zldW, zldWprime = null;

        DoubleMatrix2D data = dataSet.getDoubleData().viewDice();
        //System.out.println("data = " + data);

        boolean runColt = false;
        boolean runArray = true;
        try {
            if (runArray) {
                double[][] inV = MatrixUtils.convert(data);
                long sTime = (new Date()).getTime();
                FastICA fica = new FastICA(inV, data.rows());

                long eTime = (new Date()).getTime();
//				System.out.println("Array-based ICA took " + (eTime-sTime) + "ms");
//				System.out.println("\nfica.getICVectors(): " + convertToColt(fica.getICVectors()));

                A = MatrixUtils.convertToColt(fica.getMixingMatrix());
            }
            else if (runColt) {
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
            //System.out.println("W = " + W);
            int n = W.rows();
            //System.out.println("n = " + n);

            zldW = permuteZerolessDiagonal(W); //i.e. W~
            //System.out.println("zldW = " + zldW);

            zldWprime = MatrixUtils.normalizeDiagonal(zldW);// W~' = normalizeDiagonal(W~)
            //System.out.println("zldWprime = " + zldWprime);

            for (int i = 0; i < zldWprime.rows(); i++) {
                //System.out.println("zldWprime.get("+i+","+i+") = " + zldWprime.get(i,i));
            }

            Bhat = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n), 1, zldWprime, -1); //B^ = I - W~'
            //System.out.println("Bhat = " + Bhat);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return makeDataSet(Bhat.viewDice(), dataSet.getVariables());
    }

    //and creates a ColtDataSet from the variables and the matrix

    //the matrix must be in column-variable form


    /**
     * This creates a list of ContinuousVariables from 'nodes' and creates a
     * ColtDataSet from the variables and the matrix. The matrix must be in
     * column-variable form. Gustavo 14 May 2007
     */
    public static ColtDataSet makeDataSet(DoubleMatrix2D inVectors, List<Node> nodes) {
        if (inVectors.columns() != nodes.size()) {
//            System.out.println("inVectors.columns() = " + inVectors.columns());
//            System.out.println("nodes.size() = " + nodes.size());
            new Exception("dimensions don't match!").printStackTrace();
        }
        //create new Continuous variables passing the node to the constructor
        Vector<Node> variables = new Vector<Node>();
        for (Node node : nodes)
            variables.add(new ContinuousVariable(node.getName()));
        return ColtDataSet.makeContinuousData(variables, inVectors);
    }

    //if 'mat' can be permuted into a lower-triangular matrix, it returns the permutation
    //otherwise, it returns null

    /**
     * If 'mat' can be permuted into a lower-triangular matrix, it returns the
     * permutation; otherwise, it returns null.
     */
    public static Vector<Integer> dagPermutation(DoubleMatrix2D mat) {
//		System.out.println("entered algorithmB: mat = " + mat);

        Vector<Integer> removedIndices = new Vector<Integer>();

        Vector<Integer> v = new Vector<Integer>();
        while (removedIndices.size() < mat.rows()) {
//			System.out.println("algorithmB: mat = " + mat);
            int allZerosRow = -1;
            //find a row has all zeros
            for (int i = 0; i < mat.rows(); i++) {
                if (removedIndices.contains(i)) //if this index has been removed, ignore the row
                    continue;
                if (containsAllZeros(mat.viewRow(i), removedIndices)) {
                    allZerosRow = i;
                    break;
                }
            }
            if (allZerosRow == -1) {// no such row
//				System.out.println("algorithmB: failed to permute");
                return null;
            }
            //int actualZeroRowsIndex =
            removedIndices.add(allZerosRow);
            v.add(allZerosRow);

            //the problem now is that removing these rows and columns screws up the indices
            //mat = removeNthRowAndColumn(allZerosRow,mat); //remove the ith row and ith column


        }
//		System.out.println("algorithmB: returning v = " + v);
        return v;
    }

    public static class Entry implements Comparable<Shimizu2006SearchCleanup.Entry> {
        int row;
        int column;
        double value;

        public Entry(int row, int col, double val) {
            this.row = row;
            this.column = col;
            this.value = val;
        }

        /* used for sorting. A entry is smaller than another if its absolute value is smaller.
                  * (non-Javadoc)
                  * @see java.lang.Comparable#compareTo(java.lang.Object)
                  */
        public int compareTo(Shimizu2006SearchCleanup.Entry entry) {
            double thisVal = Math.abs(value);
            double entryVal = Math.abs(entry.value);
            return (new Double(thisVal).compareTo(entryVal));
        }

        public String toString() {
            return "[" + row + "," + column + "]:" + value + " ";
        }

    }

    public static Vector<Shimizu2006SearchCleanup.Entry> getEntries(DoubleMatrix2D mat) {
        Vector<Shimizu2006SearchCleanup.Entry> entries = new Vector();
        for (int i = 0; i < mat.rows(); i++)
            for (int j = 0; j < mat.columns(); j++) {
                Shimizu2006SearchCleanup.Entry entry = new Shimizu2006SearchCleanup.Entry(i, j, mat.get(i, j));
                entries.add(entry);
            }
        return entries;
    }

    /**
     * The algorithm works by iteratively setting the smallest (by absolute
     * value) non-zero entry to zero, until swappable by calling
     * 'algorithmB'.
     * <p/>
     * in: the unswapped matrix, as a ColtDataSet
     * <p/>
     * out: the swapped matrix, as a ColtDataSet
     */
    public static ColtDataSet iterativeC(ColtDataSet dataSet) {
        DoubleMatrix2D mat = dataSet.getDoubleData();
        //System.out.println("mat = " + mat);

        int n = mat.rows();

        Vector<Shimizu2006SearchCleanup.Entry> remainingEntries = getEntries(mat);
        java.util.Collections.sort(remainingEntries);

        //System.out.println("remainingEntries = " + remainingEntries);

        //sort the entries of mat by absolute value
        Vector entryQueue = getNFirst(n * (n + 1) / 2, remainingEntries); //cache of entries before they get set to zero.

        //set the smallest m(m+1)/2 to zero
        DoubleMatrix2D tempMat = setEntriesToZero(entryQueue, mat);
        remainingEntries.removeAll(entryQueue);
        entryQueue.removeAllElements();

        Vector<Integer> permutation;

        //test
        while (true) { //while not permutable
//			System.out.println("tempMat = " + tempMat);
            permutation = dagPermutation(tempMat);
            if (permutation != null) //once we have a permutation, we proceed
                break;

            Shimizu2006SearchCleanup.Entry entry = remainingEntries.get(0);

            //add more zeros to tempMat
            tempMat = setEntryToZero(entry, tempMat);
            remainingEntries.remove(0);
        }

        return permute(permutation, dataSet);
    }

    /**
     * Returns the n smallest (by absolute value) members of a list.
     */
    public static Vector<Shimizu2006SearchCleanup.Entry> getNFirst(int n, Vector<Shimizu2006SearchCleanup.Entry> list) {
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

    /**
     * Step 5 of algorithm A from the paper: swaps the rows and columns
     * simultaneously in order to get a lower-triangular matrix must preserve
     * the variable names.
     */
    public static ColtDataSet lingamDiscoveryStep5(ColtDataSet bhat) {
        return iterativeC(bhat);
    }


    /**
     * Turns a lower-triangular matrix into a DAG.
     */
    public static ShimizuResult makeDagWithParms(ColtDataSet ltDataSet) {
        DoubleMatrix2D ltMat = ltDataSet.getDoubleData(); //lower-triangular matrix
        int n = ltMat.rows();

        List<Node> variables = ltDataSet.getVariables();
        Dag dag = new Dag(variables);

        ShimizuResult dwp = new ShimizuResult(dag);

        //adding edges to 'dag', and populating 'weightHash'
        for (int i = 0; i < ltMat.rows(); i++)
            for (int j = 0; j < i; j++) {
                if (ltMat.get(i, j) != 0) {
                    Edge edge = new Edge(variables.get(i), variables.get(j),
                            Endpoint.TAIL, Endpoint.ARROW);
                    dwp.getGraph().addEdge(edge);
                    dwp.setWeight(edge, ltMat.get(i, j));
                }
            }

        return dwp;
    }


    /**
     * DataSet format: columns are variables.
     */
    public static ShimizuResult lingamDiscoveryDag(DataSet dataSet) {
//        System.out.println("");
//        System.out.println("****************************************************");
//        System.out.println("*************** Running Shimizu2006 ****************");
//        System.out.println("****************************************************");

        //normalize data set
//		dataSet = normalizeVariance(dataSet);

        ColtDataSet Bhat = lingamDiscoveryBhat(dataSet);
//		System.out.println("before pruning, Bhat = " + Bhat); //Bhat corresponds to a complete graph

        //do significance testing to decide which edges are zero, i.e. pruning
        int nPieces = new Double(Math.floor(Math.sqrt(dataSet.getDoubleData().rows()) / 2)).intValue();
//        int nPieces = 3;
        pruneEdges(Bhat, dataSet, 0.05, nPieces);
//		System.out.println("after pruning, Bhat = " + Bhat);

        ColtDataSet B = lingamDiscoveryStep5(Bhat); //B is the lower-triangular version of Bhat
//        System.out.println("B = " + B);

        ShimizuResult icaDag = makeDagWithParms(B); //icaDag is the DAG made from Bhat's nonzero entries

        return icaDag;
    }

    //==============================PRIVATE METHODS=======================//

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

        for (int i = 0; i < mat.rows(); i++) {//for each row in mat
            int newRowIndex = assignment[i][1];
            swappedMat.viewRow(newRowIndex).assign(mat.viewRow(i));
        }

        return swappedMat;
    }


    //the method that calls assign() twice could be a problem for the negative coefficients
    private static DoubleMatrix2D permuteZerolessDiagonal(DoubleMatrix2D w) {

        DoubleMatrix2D temp = new DenseDoubleMatrix2D(w.rows(), w.columns());
        temp.assign(w);
        temp.assign(cern.jet.math.Functions.inv);
        temp.assign(cern.jet.math.Functions.abs);

        //this is an n x 2 matrix, i.e. a list of index pairs
        int[][] assignment = Hungarian.hgAlgorithm(MatrixUtils.convert(temp), "min");

        return assignment2Matrix(w, assignment);

//		return bestAssignment(temp).matrix.assign(cern.jet.math.Functions.inv);
    }


    //set the rows, one by one to the arrayth row
    //
    private static ColtDataSet permute(Vector<Integer> permutation, ColtDataSet dataSet) {
        List<Node> resNodes = permute(permutation, dataSet.getVariables());
        DoubleMatrix2D resMatrix = permuteRows(permutation, dataSet.getDoubleData());
        resMatrix = permuteColumns(permutation, resMatrix);

        ColtDataSet resDataSet = makeDataSet(resMatrix, resNodes);

        return resDataSet;
    }

    //permutes the rows of 'mat'
    private static DoubleMatrix2D permuteRows(Vector<Integer> permutation, DoubleMatrix2D mat) {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(), mat.columns());
        for (int i = 0; i < mat.rows(); i++) {
            DoubleMatrix1D row = mat.viewRow(permutation.get(i));
            m.viewRow(i).assign(row);
        }
        return m;
    }

    //permutes the columns of 'mat'
    private static DoubleMatrix2D permuteColumns(Vector<Integer> permutation, DoubleMatrix2D mat) {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(), mat.columns());
        for (int i = 0; i < mat.columns(); i++) {
            DoubleMatrix1D col = mat.viewColumn(permutation.get(i));
            m.viewColumn(i).assign(col);
        }
        return m;
    }


    private static List<Node> permute(Vector<Integer> permutation, List<Node> variables) {
        //System.out.println("permute: variables = " + variables);

        List<Node> nodes = new Vector<Node>(variables.size());
        for (int i = 0; i < variables.size(); i++)
            nodes.add(null);

        //System.out.println("permute: before, nodes = " + nodes);
        for (int i = 0; i < variables.size(); i++)
            nodes.set(i, variables.get(permutation.get(i)));
        //nodes.set(permutation.get(i),variables.get(i));
        //System.out.println("permute: after, nodes = " + nodes);
        return nodes;
    }


    private static DoubleMatrix2D setEntriesToZero(Vector<Shimizu2006SearchCleanup.Entry> entries, DoubleMatrix2D mat) {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(), mat.columns());
        m.assign(mat);
        for (Shimizu2006SearchCleanup.Entry entry : entries)
            m.set(entry.row, entry.column, 0);

        return m;
    }

    private static DoubleMatrix2D setEntryToZero(Shimizu2006SearchCleanup.Entry entry, DoubleMatrix2D mat) {
//		System.out.println("setEntryToZero: before, mat = " + mat);
        DoubleMatrix2D m = new DenseDoubleMatrix2D(mat.rows(), mat.columns());
        m.assign(mat);
        m.set(entry.row, entry.column, 0);
//		System.out.println("setEntryToZero: returning m = " + m);
        return m;
    }


    //this is BROKEN!!!!
    private static DoubleMatrix2D removeNthRowAndColumn(int n, DoubleMatrix2D mat) {
//        System.out.println("removeNthRowAndColumn: mat = " + mat);

        DoubleMatrix2D result;

        result = removeRow(mat, n);
        //System.out.println("removeNthRowAndColumn: after removeRow, result = " + result );

        result = removeColumn(result, n);
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
        if (vec.size() > 20)
            System.exit(0);

        for (int i = 0; i < vec.size(); i++)
            if (vec.get(i) != 0 && !removedIndices.contains(i)) { //if column has been removed, do nothing.
//				System.out.println("containsAllZeros: returning false");
                return false;
            }
//		System.out.println("containsAllZeros: returning true");
        return true;
    }


    private static ColtDataSet pruneEdges(ColtDataSet B, DataSet dataSet, double alpha, int nPieces) {
        return pruneEdgesBySampling(B, dataSet, alpha, nPieces);
    }


    private static ColtDataSet pruneEdgesBySampling(ColtDataSet B, DataSet dataSet, double alpha, int nPieces) {

        //we have one B-hat for each piece of the data
        ColtDataSet[] bHats = new ColtDataSet[nPieces];

        //divide the data set into nPieces pieces
        for (int i = 0; i < nPieces; i++) {
            DataSet currentDataSet = getSubsetOfDataSet(dataSet, nPieces, i);
            bHats[i] = lingamDiscoveryBhat(currentDataSet);
//			System.out.println("bHats["+i+"] = " + bHats[i]);
        }

        //for each entries in Bhat, record its mean and s.d.

        //mean
        DoubleMatrix2D meanMat = new DenseDoubleMatrix2D(B.getDoubleData().rows(), B.getDoubleData().columns());

        for (int i = 0; i < B.getDoubleData().rows(); i++)
            for (int j = 0; j < B.getDoubleData().columns(); j++) {
                double sum = 0;
                for (int z = 0; z < nPieces; z++)
                    sum += bHats[z].getDouble(i, j);
                meanMat.set(i, j, sum / nPieces);
            }
//		System.out.println("meanMat = " + meanMat);

        //s.d.
        DoubleMatrix2D sdMat = new DenseDoubleMatrix2D(B.getDoubleData().rows(), B.getDoubleData().columns());
        for (int i = 0; i < B.getDoubleData().rows(); i++)
            for (int j = 0; j < B.getDoubleData().columns(); j++) {
                double varianceSum = 0;
                for (int z = 0; z < nPieces; z++)
                    varianceSum += Math.pow((bHats[z].getDouble(i, j) - meanMat.get(i, j)), 2);
                double variance = varianceSum / nPieces;
                sdMat.set(i, j, Math.pow(variance, 0.5));
            }

//		System.out.println("sdMat = " + sdMat);

        double pruneFactor = 1;

        for (int i = 0; i < B.getDoubleData().rows(); i++)
            for (int j = 0; j < B.getDoubleData().columns(); j++) {
                // if can't reject the zero hypothesis...
                if (Math.abs(meanMat.get(i, j)) < sdMat.get(i, j) * pruneFactor) {
//					System.out.println("pruning edge ("+i+","+j+").  ");
//					System.out.println("setting "+ B.getDoubleData().get(i, j) + " to zero.");
//					System.out.println("mean = " + meanMat.get(i, j) + "   sd = " + sdMat.get(i, j));
//					String printo="";
//					for (int z=0; z<nPieces; z++)
//						printo += " " + bHats[z].getDouble(i,j);
//					System.out.println("entries: "+printo);

                    B.getDoubleData().set(i, j, 0); //set it to zero
                }
            }

        // TODO Auto-generated method stub
        return null;
    }


    /**
     * returns a subset of the data points
     */
    private static DataSet getSubsetOfDataSet(DataSet dataSet, int pieces, int pieceIndex) {
        DoubleMatrix2D mat = dataSet.getDoubleData();

        //System.out.println("mat = " + mat);
        int pieceSize = mat.rows() / pieces;

        DoubleMatrix2D res = mat.viewPart(pieceSize * pieceIndex, 0, pieceSize, mat.columns());
        //System.out.println("res = " + res);

        return makeDataSet(res, dataSet.getVariables());
    }

    /**
     * format: columns are variables
     */
    private static DataSet normalizeVariance(DataSet dataSet) {
        DoubleMatrix2D mat = dataSet.getDoubleData();
        DoubleMatrix2D res = new DenseDoubleMatrix2D(mat.rows(), mat.columns());

        for (int j = 0; j < mat.columns(); j++) { //for each variable

            //first compute the mean
            double sum = 0;
            for (int i = 0; i < mat.rows(); i++) {
                sum += mat.get(i, j);
            }
            double mean = sum / mat.rows();

            double sumDistSq = 0;
            for (int i = 0; i < mat.rows(); i++) {
                sumDistSq += Math.pow((mat.get(i, j) - mean), 2);
            }
            double variance = sumDistSq / mat.rows();
            double sd = Math.pow(variance, 0.5);

            //normalize
            for (int i = 0; i < mat.rows(); i++) {
                res.set(i, j, mat.get(i, j) / sd);
            }
        }

        return makeDataSet(res, dataSet.getVariables());
    }

}
