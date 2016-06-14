package edu.cmu.tetrad.search;
// This file lets you run ICA-based LiNG discovery, specifically with the local thresholding algorithm
//
// The following call hierarchy shows the essence of our method.
//
//main() calls testCyclicDiscovery(), which generates simulated data and calls:
//LacerdaSpirtesRamsey2007Search.run(RectangularDataSet)  (edu.cmu.tetrad.search)
//which calls
//LacerdaSpirtesRamsey2007Search.findCandidateModels(List<Node>, DoubleMatrix2D, DoubleMatrix2D, int, boolean)  (edu.cmu.tetrad.search)
//which calls
//LacerdaSpirtesRamsey2007Search.zerolessDiagonalPermutations(DoubleMatrix2D, boolean)  (edu.cmu.tetrad.search)
//which calls
//LacerdaSpirtesRamsey2007Search.nRookRowAssignments(DoubleMatrix2D)  (edu.cmu.tetrad.search)
//
//
//to play around, you may modify the code as follows.
//
// to use a different SEM, create a new SEM, and refer to it instead of graphUaiPaper().
// to modify the distribution, create a new Distribution and use it instead of gp2.
// to modify the number of samples, change the value of 'numSamples'.
//
//Gustavo Lacerda, 28 February 2008

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.fastica.math.Matrix;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.dist.Distribution;
import edu.cmu.tetrad.util.dist.GaussianPower;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class RunCyclicDiscovery {


    public double[][] sampleMatrix() {
//		double[][] a = {{12,1,10},{3,5,7},{17,9,2}};
//		double[][] a = {{12,1,10},{17,9,2},{3,5,7}};
//		double[][] a = {{2,5,10,11},{3,3,5,1},{5,4,8,6},{6,3,5,12}};
        double[][] a = {{2, 5, 10, 11, 7}, {3, 3, 5, 1, 2}, {5, 4, 8, 6, 11}, {6, 3, 5, 12, 10}, {9, 2, 4, 5, 9}};

        return a;
    }


    //creates a list of nodes in simple order
    public static List<Node> makeNodeList(int nVars) {
        List<Node> nodes = new Vector<Node>();

        //taken from UniformGraphGenerator.getDag
        for (int i = 1; i <= nVars; i++) {
            GraphNode node = new GraphNode("X" + i);
            nodes.add(node);
        }

        return nodes;
    }


    //Dag with edge weights
    private static GraphWithParameters makeRandomDagWithParms(Dag dag) {
        GraphWithParameters dwp = new GraphWithParameters(dag);

        List<Edge> edges = dag.getEdges();

        //for each edge, add a weight to the hash
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
//			double w = 0.0;
            double w = 1.0;
            dwp.getWeightHash().put(edge, w);
        }

        return dwp;
    }


    //Dag with a slot for node values, i.e. instantiations
    public static class DagWithParmsAndValues {

        public GraphWithParameters dwp;

        public HashMap<Node, Double> valueHash;

        public DagWithParmsAndValues(GraphWithParameters dwp) {
            this.dwp = dwp;
            valueHash = new HashMap<Node, Double>();
        }
    }

//		testMain();
//		testNRooks();
//		generateCandidateModels();

    public static void main(String[] args) {
        testCyclicDiscovery();
    }


    private static void generateCandidateModels() {

        //there are two ways of specifying the generating model genModel (which is of type PatternWithParameters):

        //FIRST WAY: graph matrix, a.k.a. B matrix
//		DoubleMatrix2D mat = getMatXyzwM1();//getMatXyzCycle();//getMatXyzwM1();//getMatAbcd();
//		System.out.println("original B mat = " + mat);
//		PatternWithParameters genModel = new PatternWithParameters(mat);

        //SECOND WAY: graph and weight

        GraphWithParameters genModel = graph4Cycle();//graphInteractingCycles();//graph4Cycle();//graph2_2Cycles();//graphInteractingCycles();//graph4Cycle();//graphXyzw(); //graph2_2Cycles();//
//		PatternWithParameters genModel = graphWithCycleIndicators();//graphIndepCycles();//graphInteractingCycles();//graph4Cycle();//graph2_2Cycles();//graphInteractingCycles();//graph4Cycle();//graphXyzw(); //graph2_2Cycles();//

        System.out.println("genModel = " + genModel + "\n" + genModel.getGraphMatrix());
        boolean isShrinkingMatrix = LacerdaSpirtesRamsey2007Search.allEigenvaluesAreSmallerThanOneInModulus(genModel.getGraphMatrix().getDoubleData());
        System.out.println("for generating model, isShrinkingMatrix = " + isShrinkingMatrix);

//		showPowersOfB(genModel);

        DoubleMatrix2D reducedForm = reducedForm(genModel);
        System.out.println("reducedForm = " + reducedForm);


        ColtDataSet matrixB = genModel.getGraphMatrix();
        int n = matrixB.getDoubleData().rows();
//		System.out.println("210: matrixB = " + matrixB);

        DoubleMatrix2D matrixW = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n), 1, matrixB.getDoubleData(), -1); //W = I-B
        System.out.println("211: matrixW = " + matrixW);
        LacerdaSpirtesRamsey2007Search.findCandidateModels(genModel.getGraph().getNodes(), matrixW, null, n, false);
    }


    private static void showPowersOfB(GraphWithParameters genModel) {
        ColtDataSet matrixB = genModel.getGraphMatrix();
        Algebra algebra = new Algebra();

//		System.out.println("dec.getD() = " + dec.getD());
//		System.out.println("dec.getV() = " + dec.getV());

//		System.out.println("realEigenvalues  = " + realEigenvalues);
//		System.out.println("imagEigenvalues = " + imagEigenvalues);

//		boolean allSmallerThanOneInAbs = LacerdaSpirtesRamsey2007Search.allEigenvaluesAreSmallerThanOneInModulus(matrixB.getDoubleData());
//
//		System.out.println("allSmallerThanOneInAbs = " + allSmallerThanOneInAbs);


        for (int i = 1; i < 100; i += 10) {
            DoubleMatrix2D bpow = algebra.pow(matrixB.getDoubleData(), i);
            System.out.println("B^" + i + " = " + bpow);
        }


    }

    private static GraphWithParameters graphXyzw() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("X"));
        g.addNode(new GraphNode("Y"));
        g.addNode(new GraphNode("Z"));
        g.addNode(new GraphNode("W"));
        genModel.addEdge("X", "Z", 1);
        genModel.addEdge("Y", "W", 1);
        genModel.addEdge("Z", "W", 1.2);
        genModel.addEdge("W", "Z", 0.7);

        return genModel;
    }

    private static GraphWithParameters graph4Cycle() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
//		g.addNode(new GraphNode("exoB"));
//		g.addNode(new GraphNode("exoC"));
//		g.addNode(new GraphNode("exoD"));
//		g.addNode(new GraphNode("exoE"));
        g.addNode(new GraphNode("B"));
        g.addNode(new GraphNode("C"));
        g.addNode(new GraphNode("D"));
        g.addNode(new GraphNode("E"));

//		genModel.addEdge("exoB", "B", 0.8);
//		genModel.addEdge("exoC", "C", 0.9);
//		genModel.addEdge("exoD", "D", 0.95);
//		genModel.addEdge("exoE", "E", 0.6);
        genModel.addEdge("B", "C", 1);
        genModel.addEdge("C", "D", 0.7);
        genModel.addEdge("D", "E", 0.7);
        genModel.addEdge("E", "B", 1);

        return genModel;
    }


    private static GraphWithParameters graphInteractingCycles() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("A"));
        g.addNode(new GraphNode("B"));
        g.addNode(new GraphNode("C"));
        g.addNode(new GraphNode("D"));
        g.addNode(new GraphNode("E"));
//		g.addNode(new GraphNode("exog"));

        genModel.addEdge("A", "B", 0.5);
        genModel.addEdge("B", "C", 1);
        genModel.addEdge("C", "A", 1);
//		genModel.addEdge("C", "D", -1);
//		genModel.addEdge("A", "C", 0.5);
        genModel.addEdge("A", "D", -1.1);
//		genModel.addEdge("D", "C", .96);
        genModel.addEdge("D", "E", 1);
        genModel.addEdge("E", "C", .96);

        //		genModel.addEdge("D", "A", 1.5);

//		genModel.addEdge("exog", "B", 0.5);


        return genModel;
    }

    private static GraphWithParameters graphWithCycleIndicators() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("A1"));
        g.addNode(new GraphNode("A2"));
        g.addNode(new GraphNode("A3"));
        g.addNode(new GraphNode("A4"));
        g.addNode(new GraphNode("B1"));
        g.addNode(new GraphNode("B2"));
        g.addNode(new GraphNode("B3"));
        g.addNode(new GraphNode("B4"));
        g.addNode(new GraphNode("C1"));
        g.addNode(new GraphNode("C2"));
        g.addNode(new GraphNode("C3"));
        g.addNode(new GraphNode("C4"));

        //top edges
        genModel.addEdge("A1", "B1", 1);
        genModel.addEdge("A2", "B2", 1);
        genModel.addEdge("A3", "B3", 1);
        genModel.addEdge("A4", "B4", 1);

        //bottom edges
        genModel.addEdge("B1", "C1", 1);
        genModel.addEdge("B2", "C2", 1);
        genModel.addEdge("B3", "C3", 1);
        genModel.addEdge("B4", "C4", 1);

        //cycle edges
        genModel.addEdge("B1", "B2", 0.9);
        genModel.addEdge("B2", "B3", 0.9);
        genModel.addEdge("B3", "B4", 0.9);
        genModel.addEdge("B4", "B1", 0.9);

        return genModel;
    }

    private static GraphWithParameters graph2() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("A"));
        g.addNode(new GraphNode("B"));

        //top edges
        genModel.addEdge("A", "B", 0.5);
        genModel.addEdge("B", "A", 0.8);
        return genModel;
    }

    //this is the SEM shown in Example 1. It is used for generating data for the sample run in section 4.4
    private static GraphWithParameters graphUaiPaper() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("X1"));
        g.addNode(new GraphNode("X2"));
        g.addNode(new GraphNode("X3"));
        g.addNode(new GraphNode("X4"));
        g.addNode(new GraphNode("X5"));

        genModel.addEdge("X1", "X2", 1.2);
        genModel.addEdge("X2", "X3", 2);
        genModel.addEdge("X3", "X4", -1);
        genModel.addEdge("X4", "X2", -0.3);
        genModel.addEdge("X2", "X5", 3);
        return genModel;
    }


    private static GraphWithParameters graphIndepCycles() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
        g.addNode(new GraphNode("A"));
        g.addNode(new GraphNode("B"));
        g.addNode(new GraphNode("C"));
        g.addNode(new GraphNode("D"));
        g.addNode(new GraphNode("E"));

        genModel.addEdge("A", "B", 0.5);
        genModel.addEdge("B", "A", 0.5);
        genModel.addEdge("B", "C", 1);
        genModel.addEdge("C", "D", 0.7);
        genModel.addEdge("D", "C", 0.7);
        return genModel;
    }


    private static GraphWithParameters graph2_2Cycles() {
        Graph g = new EdgeListGraph();

        GraphWithParameters genModel = new GraphWithParameters(g);
//		g.addNode(new GraphNode("A"));
//		g.addNode(new GraphNode("B"));
        g.addNode(new GraphNode("K1"));
        g.addNode(new GraphNode("K2"));
        g.addNode(new GraphNode("L1"));
        g.addNode(new GraphNode("L2"));

//		genModel.addEdge("A", "K1", 0.8);
        genModel.addEdge("K1", "K2", 0.9);
        genModel.addEdge("K2", "K1", 0.9);

//		genModel.addEdge("B", "L1", 0.8);
        genModel.addEdge("L1", "L2", 0.9);
        genModel.addEdge("L2", "L1", 0.9);

        return genModel;
    }

    //generates data, and gives it to ICA-based cyclic discovery
    private static void testCyclicDiscovery() {

        int numSamples = 15000;

        //define the "Gaussian-squared" distribution
        Distribution gp2 = new GaussianPower(2);

        //get the graph shown in Example 1
        GraphWithParameters genModel = graphUaiPaper();

        //the coefficients of the error terms  (here, all 1s)
        DoubleMatrix1D errorCoefficients = getErrorCoeffsIdentity(genModel.getGraph().getNumNodes());

        //generate data from the SEM
        DoubleMatrix2D inVectors = simulate_Cyclic(genModel, errorCoefficients, numSamples, gp2); //errorCoefficients;

        //print the generated data
        //		System.out.println("inVectors = " + inVectors.viewDice());

        //reformat it
        ColtDataSet dataSet = ColtDataSet.makeContinuousData(genModel.getGraph().getNodes(), inVectors.viewDice());

        //run ICA-based cyclic discovery with this data
        new LacerdaSpirtesRamsey2007Search().run(dataSet);//,iMinusB);
    }


    private static DoubleMatrix1D getErrorCoeffsSpirtes05Nov() {
        DoubleMatrix1D errorCoefficients = new DenseDoubleMatrix1D(4);
        errorCoefficients.set(0, .9);
        errorCoefficients.set(1, 1.5);
        errorCoefficients.set(2, 1.3);
        errorCoefficients.set(3, .6);
        return errorCoefficients;
    }

    private static DoubleMatrix1D getErrorCoeffsIdentity(int n) {
        DoubleMatrix1D errorCoefficients = new DenseDoubleMatrix1D(n);
        for (int i = 0; i < n; i++) {
            errorCoefficients.set(i, 1);
        }
        return errorCoefficients;
    }

    public static void testNRooks() {
        //create matrix
        // 1 1 0 0
        // 0 1 1 0
        // 0 0 1 1
        // 1 0 0 1
        DoubleMatrix2D mat = getMat17Oct();


        List<List<Integer>> assns =
                LacerdaSpirtesRamsey2007Search.nRookColumnAssignments(mat);

        System.out.println("assns.size() = " + assns.size());

        for (List<Integer> assn : assns) {
            System.out.println(assn);

            DoubleMatrix2D m = LacerdaSpirtesRamsey2007Search.displayNRookAssignment(assn);
            System.out.println(m);
        }
    }


    private static DoubleMatrix2D getMat17Oct() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(4, 4);
        mat.set(0, 0, 1);
        mat.set(0, 1, 1);
        mat.set(1, 1, 1);
        mat.set(1, 2, 1);
        mat.set(2, 2, 1);
        mat.set(2, 3, 1);
        mat.set(3, 3, 1);
        mat.set(3, 0, 1);
        return mat;
    }


    private static DoubleMatrix2D getMatAbcd() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(4, 4);
        mat.set(0, 1, 0.5);
        mat.set(1, 2, 0.5);
        mat.set(2, 3, 0.5);
        mat.set(3, 0, 0.5);
        return mat;
    }

    private static DoubleMatrix2D getMatXyzwM1() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(4, 4);
        mat.set(2, 0, .7);
        mat.set(3, 1, 1.2);
        mat.set(3, 2, 0.8);
        mat.set(2, 3, 0.4);
        return mat;
    }

    private static DoubleMatrix2D getMatXyzCycle() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(3, 3);
        mat.set(1, 0, .5);
        mat.set(2, 1, .5);
        mat.set(1, 2, .5);
        return mat;
    }

    private static DoubleMatrix2D getMatXzyCycle() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(3, 3);
        mat.set(2, 0, .5);
        mat.set(2, 1, .5);
        mat.set(1, 2, .5);
        return mat;
    }


    private static DoubleMatrix2D getMatXyzwM2() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(4, 4);
        mat.set(3, 0, 0.5);
        mat.set(2, 1, 0.5);
        mat.set(3, 2, 0.5);
        mat.set(2, 3, 0.5);
        return mat;
    }


    private static DoubleMatrix2D getMatXyzComplete() {
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(3, 3);
        mat.set(0, 1, 0.5);
        mat.set(0, 2, 0.5);
        mat.set(1, 0, 0.5);
        mat.set(1, 2, 0.5);
        mat.set(2, 0, 0.5);
        mat.set(2, 1, 0.5);
        return mat;
    }

    private static DoubleMatrix2D getMatXyzXExogenous() { // X--->Y<==>Z
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(3, 3);
        mat.set(0, 1, 0.5);
        mat.set(1, 2, 0.5);
        mat.set(2, 1, 0.5);
        return mat;
    }

    private static int argmin(List<Double> l, List<SemLearningMethod> methods) {
        int minIndex = 0;
        double min = l.get(0);
        int i = 0;
        for (double d : l) {
            if (d < min && !methods.get(i).getName().contains("true DAG")) {
                min = d;
                minIndex = i;
            }
            i++;
        }
        return minIndex;
    }


    private static Dag twoNodeDag() {
        Node node1 = new GraphNode("X1");
        Node node2 = new GraphNode("X2");
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node1);
        nodes.add(node2);
        Dag dag = new Dag(nodes);
        Edge edge = new Edge(node1, node2, Endpoint.TAIL, Endpoint.ARROW);
//		Edge edge = new Edge(node2,node1,Endpoint.TAIL,Endpoint.ARROW);
        dag.addEdge(edge);
        return dag;
    }


    /**
     * graph matrix is B
     * mixing matrix (reduced form) is A
     *
     * @param graph
     * @return
     */
    private static DoubleMatrix2D reducedForm(GraphWithParameters graph) {
        int n = graph.getGraph().getNumNodes();
        DoubleMatrix2D graphMatrix = graph.getGraphMatrix().getDoubleData();
//		System.out.println("graphMatrix = " + graphMatrix);
        DoubleMatrix2D identityMinusGraphMatrix = MatrixUtils.linearCombination(MatrixUtils.identityMatrix(n), 1, graphMatrix, -1);
//		System.out.println("identityMinusGraphMatrix = " + identityMinusGraphMatrix);
        DoubleMatrix2D mixingMatrix = MatrixUtils.inverse(identityMinusGraphMatrix);
        return mixingMatrix;
    }

    //check against model in which: A =  ..... / (1 - xyzw)

    private static DoubleMatrix1D simulateReducedForm(DoubleMatrix2D reducedForm, DoubleMatrix1D errorCoefficients, Distribution distr) {
        int n = reducedForm.rows();
        DoubleMatrix1D vector = new DenseDoubleMatrix1D(n);
        DoubleMatrix1D samples = new DenseDoubleMatrix1D(n);

        for (int j = 0; j < n; j++) { //sample from each noise term
            double sample = distr.nextRandom();
            double errorCoefficient = errorCoefficients.get(j);
            samples.set(j, sample * errorCoefficient);
        }

        for (int i = 0; i < n; i++) { //for each observed variable, i.e. dimension
            double sum = 0;
            for (int j = 0; j < n; j++) {
                double coefficient = reducedForm.get(i, j);
                double sample = samples.get(j);
                sum += coefficient * sample;
            }
            vector.set(i, sum);
        }
        return vector;
    }


    public static DoubleMatrix2D simulate_Cyclic(GraphWithParameters dwp, DoubleMatrix1D errorCoefficients, int n, Distribution distribution) {
        DoubleMatrix2D errorCoeffMatrix = null;

        DoubleMatrix2D reducedForm = reducedForm(dwp);
        System.out.println("reducedForm = " + reducedForm);

        DoubleMatrix2D vectors = new DenseDoubleMatrix2D(dwp.getGraph().getNumNodes(), n);
        for (int j = 0; j < n; j++) {
            DoubleMatrix1D vector = simulateReducedForm(reducedForm, errorCoefficients, distribution);//simulate_Cyclic(dwp, errorCoefficients, distribution);
            vectors.viewColumn(j).assign(vector);
        }
        return vectors;
    }

    //do all nodes have a value in weightHash?


    private static DoubleMatrix2D example_line(int n) {

        DoubleMatrix1D e1 = generateUniform(0, 1, n);
        DoubleMatrix1D e2 = generateUniform(0, 1, n);
        DoubleMatrix1D e3 = generateUniform(0, 1, n);

        DoubleMatrix1D x1 = e1;
        DoubleMatrix1D x2 = linearCombination(x1, 1, e2, 0.07);
        DoubleMatrix1D x3 = linearCombination(x2, 1, e3, 0.05);

        DoubleMatrix1D[] observedVars = {x1, x2, x3};

        return combine(observedVars);
    }


    private static DoubleMatrix2D example_Y(int n) {
        DoubleMatrix1D e1 = generateUniform(0, 1, n);
        DoubleMatrix1D e2 = generateUniform(0, 1, n);
        DoubleMatrix1D e3 = generateUniform(0, 1, n);
        DoubleMatrix1D e4 = generateUniform(0, 1, n);
        DoubleMatrix1D e5 = generateUniform(0, 1, n);

        DoubleMatrix1D x1 = e1;
        DoubleMatrix1D x2 = e2;
        DoubleMatrix1D x3 = linearCombination(linearCombination(x1, 1, x2, 2), 1, e3, 1);
        DoubleMatrix1D x4 = linearCombination(x3, 1.5, e4, 1);

        DoubleMatrix1D x5 = linearCombination(x1, 1, e5, 1);

        DoubleMatrix1D[] observedVars = {x1, x2, x3, x4, x5};

        return combine(observedVars);

    }

    private static DoubleMatrix2D example_completeDag(int n) {
        //DoubleMatrix1D[] vars;
        //double[] ws;

        DoubleMatrix1D e1 = generateUniform(0, 1, n);
        DoubleMatrix1D e2 = generateUniform(0, 1, n);
        DoubleMatrix1D e3 = generateUniform(0, 1, n);
        DoubleMatrix1D e4 = generateUniform(0, 1, n);

        DoubleMatrix1D x1 = e1;

        DoubleMatrix1D[] vars2 = {x1, e2};
        double[] ws2 = {2.0, 1.0};
        DoubleMatrix1D x2 = linearCombination(vars2, ws2);

        DoubleMatrix1D[] vars3 = {x1, x2, e3};
        double[] ws3 = {3.0, 0.0, 1.0};
        DoubleMatrix1D x3 = linearCombination(vars3, ws3);

        DoubleMatrix1D[] vars4 = {x1, x2, x3, e4};
        double[] ws4 = {1.0, 1.0, -1.0, 1.0};
        DoubleMatrix1D x4 = linearCombination(vars4, ws4);

        DoubleMatrix1D[] observedVars = {x1, x2, x3, x4};
//		DoubleMatrix1D[] observedVars = {x4,x3,x2,x1};   //


        return combine(observedVars);

    }

    private static DoubleMatrix2D example_completeDagSG(int n) {
        //DoubleMatrix1D[] vars;
        //double[] ws;

        DoubleMatrix1D e1 = generateSquaredGaussian(n);
        DoubleMatrix1D e2 = generateSquaredGaussian(n);
        DoubleMatrix1D e3 = generateSquaredGaussian(n);
        DoubleMatrix1D e4 = generateSquaredGaussian(n);

        DoubleMatrix1D x1 = e1;

        DoubleMatrix1D[] vars2 = {x1, e2};
        double[] ws2 = {2.0, 1.0};
        DoubleMatrix1D x2 = linearCombination(vars2, ws2);

        DoubleMatrix1D[] vars3 = {x1, x2, e3};
        double[] ws3 = {3.0, 0.0, 1.0};
        DoubleMatrix1D x3 = linearCombination(vars3, ws3);

        DoubleMatrix1D[] vars4 = {x1, x2, x3, e4};
        double[] ws4 = {1.0, 1.0, -1.0, 1.0};
        DoubleMatrix1D x4 = linearCombination(vars4, ws4);

        DoubleMatrix1D[] observedVars = {x1, x2, x3, x4};
//		DoubleMatrix1D[] observedVars = {x4,x3,x2,x1};   //


        return combine(observedVars);

    }

    private static DoubleMatrix2D example_Straight(int n) {
        DoubleMatrix1D e1 = generateFourthGaussian(n);
        DoubleMatrix1D e2 = generateAlmostGaussian(n);

        DoubleMatrix1D x1 = e1;
        DoubleMatrix1D[] vars2 = {x1, e2};
        double[] ws2 = {1.0, 1.0};
        DoubleMatrix1D x2 = linearCombination(vars2, ws2);

        DoubleMatrix1D[] observedVars = {x1, x2};
        return combine(observedVars);
    }


    private static DoubleMatrix2D example_Reverse(int n) {
        DoubleMatrix1D e1 = generateAlmostGaussian(n);
        DoubleMatrix1D e2 = generateFourthGaussian(n);

        DoubleMatrix1D x1 = e1;
        DoubleMatrix1D[] vars2 = {x1, e2};
        double[] ws2 = {1.0, 1.0};
        DoubleMatrix1D x2 = linearCombination(vars2, ws2);

        DoubleMatrix1D[] observedVars = {x1, x2};
        return combine(observedVars);
    }

//	private static DoubleMatrix2D combine(DoubleMatrix1D vec1, DoubleMatrix1D vec2) {
//	DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(2, vec1.size());
//	resultMatrix.viewRow(0).assign(vec1);
//	resultMatrix.viewRow(1).assign(vec2);
//	return resultMatrix;
//	}

    private static DoubleMatrix2D combine(DoubleMatrix1D[] vecs) {
        DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(vecs.length, vecs[0].size());
        for (int i = 0; i < vecs.length; i++) { //for each vector
            resultMatrix.viewRow(i).assign(vecs[i]);
        }
        return resultMatrix;
    }

    private static DoubleMatrix1D linearCombination(DoubleMatrix1D a, double aw, DoubleMatrix1D b, double bw) {
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(a.size());
        for (int i = 0; i < a.size(); i++) {
            resultMatrix.set(i, aw * a.get(i) + bw * b.get(i));
        }
        return resultMatrix;
    }

    private static DoubleMatrix1D linearCombination(DoubleMatrix1D[] vecs, double[] weights) {
        //the elements of vecs must be vectors of the same size
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(vecs[0].size());

        for (int i = 0; i < vecs[0].size(); i++) { //each entry
            double sum = 0;
            for (int j = 0; j < vecs.length; j++) { //for each vector
                sum += vecs[j].get(i) * weights[j];
            }
            resultMatrix.set(i, sum);
        }
        return resultMatrix;
    }


    private static DoubleMatrix2D linearCombination(DoubleMatrix2D a, double aw, DoubleMatrix2D b, double bw) {

        for (int i = 0; i < a.rows(); i++) {
            //System.out.println("b.get("+i+","+i+") = " + b.get(i,i));
        }


        DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(a.rows(), a.columns());
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                resultMatrix.set(i, j, aw * a.get(i, j) + bw * b.get(i, j));
                if (i == j) {
//					System.out.println("entry (" + i + ","+ i+ ")   is the sum of ");
//					System.out.println("aw*a.get("+i+","+j+") = " + aw*a.get(i,j) + " and");
//					System.out.println("bw*b.get("+i+","+j+") = " + bw*b.get(i,j));
//					System.out.println(", which is " + (aw*a.get(i,j) + bw*b.get(i,j)));
//					System.out.println("");
//					System.out.println("bw = " + bw);
//					System.out.println("b.get("+i+","+j+") = " + b.get(i,j));
//					System.out.println("");
//					System.out.println("");

                }

            }
        }
        return resultMatrix;
    }


    //reads a matrix written by R
    private static DataSet readData(String filename) {
        DataReader p = new DataReader();

        File inFile = new File(filename);

//		String thisLine;
        try {
            return p.parseTabular(inFile);
        }
//		Vector v0 = new Vector();
//		Vector v1 = new Vector();
//
//			FileInputStream fin =  new FileInputStream(inFile);
//			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
//
//			myInput.readLine(); //throw out first line
//
//			while ((thisLine = myInput.readLine()) != null) {
//				//System.out.println("thisLine = " + thisLine);
//				String[] spl = thisLine.trim().split(" +"); //" +" means "any number of spaces"
//				//System.out.println("spl[0] = " + spl[0] + "     spl[1] = " + spl[1] );
//				v0.add(new Double(spl[1]));
//				v1.add(new Double(spl[2]));
//			}
//		}
        catch (Exception e) {
            e.printStackTrace();
        }

//		//String[] spl = bufferStr.split(" ");
//		////System.out.println("spl = " + spl);
//		//v0.add(spl[0]);
//		//v1.add(spl[1]);
//
//		DoubleMatrix2D dataMatrix = new DenseDoubleMatrix2D(2,v0.size());
//		for (int i=0; i<v0.size(); i++){
//			dataMatrix.set(0, i, (Double) v0.get(i));
//			dataMatrix.set(1, i, (Double) v1.get(i));
//		}
//
//		return dataMatrix;
        return null;
    }


    public static DoubleMatrix2D generateBimodal2DGaussian(int n) {
        DoubleMatrix2D vectors = new DenseDoubleMatrix2D(2, n);
        double x, y;

        for (int i = 0; i < n; i++) {
            if (Math.random() < 0.5) {
                x = Gaussian_invcdf(Math.random()) - 10;
                y = Gaussian_invcdf(Math.random());
            } else {
                x = Gaussian_invcdf(Math.random()) + 10;
                y = Gaussian_invcdf(Math.random());
            }
            vectors.set(0, i, x);
            vectors.set(1, i, y);
        }
        return vectors;
    }


    public static DoubleMatrix2D generate2DGaussian(int n) {
        DoubleMatrix2D vectors = new DenseDoubleMatrix2D(2, n);
        RandomUtil r = RandomUtil.getInstance();
        for (int i = 0; i < n; i++) {
//			double x = Gaussian_invcdf(Math.random());
//			double y = Gaussian_invcdf(Math.random());
//			vectors.set(0,i,x);
//			vectors.set(1,i,y);
            vectors.set(0, i, r.nextNormal(0, 1));
            vectors.set(1, i, r.nextNormal(0, 1));
        }
        return vectors;
    }


    public static DoubleMatrix1D generateAlmostGaussian(int n) {
        DoubleMatrix1D points = new DenseDoubleMatrix1D(n);
        RandomUtil r = RandomUtil.getInstance();

        for (int i = 0; i < n; i++) {
            if (r.nextDouble() < .95)
                points.set(i, r.nextNormal(0, 1));
            else
                points.set(i, r.nextNormal(0, 1) + 2);
        }

        return points;
    }


    public static DoubleMatrix1D generateGaussian(int n) {
        DoubleMatrix1D points = new DenseDoubleMatrix1D(n);
        RandomUtil r = RandomUtil.getInstance();

        for (int i = 0; i < n; i++)
            points.set(i, r.nextNormal(0, 1));

        return points;
    }


    public static DoubleMatrix1D generateSquaredGaussian(int n) {
        DoubleMatrix1D points = new DenseDoubleMatrix1D(n);
        RandomUtil r = RandomUtil.getInstance();

        for (int i = 0; i < n; i++)
            points.set(i, Math.pow(r.nextNormal(0, 1), 2));

        return points;
    }


    public static DoubleMatrix1D generateFourthGaussian(int n) {
        DoubleMatrix1D points = new DenseDoubleMatrix1D(n);
        RandomUtil r = RandomUtil.getInstance();

        for (int i = 0; i < n; i++)
            points.set(i, Math.pow(r.nextNormal(0, 1), 4));

        return points;
    }

    public static double generateUniform(double a, double b) {
        double length = b - a;
        double x = a + length * Math.random();
        return x;
    }


    public static DoubleMatrix1D generateUniform(double a, double b, int n) {
        DoubleMatrix1D points = new DenseDoubleMatrix1D(n);
        double length = b - a;

        for (int i = 0; i < n; i++) {
            double x = a + length * Math.random();
            points.set(i, x);
        }
        return points;
    }


    //d is in the range [0,1]
    private static double Gaussian_invcdf(double x) {

        //at .5398, the fn is at 0.1; at .9987, the fn is at 3.0
        double[] table = {0.0000, 0.0398, 0.0793, 0.1179, 0.1554, 0.1915, 0.2257, 0.2580, 0.2881,
                0.3159, 0.3413, 0.3643, 0.3849, 0.4032, 0.4192, 0.4332, 0.4452, 0.4554, 0.4641,
                0.4713, 0.4772, 0.4821, 0.4861, 0.4893, 0.4918, 0.4938, 0.4953, 0.4965, 0.4974,
                0.4981, 0.4987, 0.5}; //assuming that at 3.1

        //identify where d falls in the table.
        if (x > 0.5) {
            int index = getLastSmallerIndex(x - 0.5, table);

            //returns a value between 0.1*index and 0.1*(index+1)
            double interpol = interpolate(0.5 + table[index], 0.5 + table[index + 1],
                    index * 0.1, (index + 1) * 0.1, x); //weight

            System.out.println("x = " + x + "    interpol = " + interpol);

            return interpol;
        } else {
            double ix = 1 - x;

            int index = getLastSmallerIndex(ix - 0.5, table);

            double interpol = interpolate(0.5 + table[index], 0.5 + table[index + 1],
                    index * 0.1, (index + 1) * 0.1, ix); //weight

            System.out.println("x = " + x + "    -interpol = " + -interpol);
            return -interpol;
        }

    }


    //does a linear interpolation, assuming x1 < x < x2
    //returns the y-value corresponding to 'x'.
    private static double interpolate(double x1, double x2, double y1, double y2, double x) {
        //0.1*index is between d and e
        double dx = x2 - x1;
        double dy = y2 - y1;
        double slope = dy / dx;
        return y1 + slope * (x - x1);
    }


    private static int getLastSmallerIndex(double d, double[] table) {
        for (int i = 0; i < table.length; i++)
            if (table[i] > d)
                return i - 1;
        return -1;
    }


    public static final class Invert implements DoubleFunction {
        public double apply(double d) {
            return 1 / d;
        }
    }

//	private static void testColt(DoubleMatrix2D inVectors) {
//	try{
//	System.out.println("");
//	System.out.println("================================================");
//	System.out.println("================================================");
//	System.out.println("============= testing FastICA Colt  ============");
//	//System.out.println("inVectors = " + inVectors);
//	long sTime = (new Date()).getTime();
//	FastIca fica = new FastIca(inVectors, inVectors.rows());
//	long eTime = (new Date()).getTime();
//	System.out.println("Colt-based ICA took " + (eTime-sTime) + "ms");
//	System.out.println("\nfica.getICVectors(): " + fica.getICVectors());
//	System.out.println("\nWeight matrix: " + fica.getWeightMatrix());
//	System.out.println("\nMixing matrix: " + fica.getMixingMatrix());
//	System.out.println("\nNormalized mixing matrix: " + normalizeDiagonal(fica.getMixingMatrix()));

//	}
//	catch(Exception e){
//	e.printStackTrace();
//	}

//	}


    public static DoubleMatrix2D convertToColt(double[][] vectors) {
        int m = Matrix.getNumOfRows(vectors);
        int n = Matrix.getNumOfColumns(vectors);

        DoubleMatrix2D mat = new DenseDoubleMatrix2D(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                mat.set(i, j, vectors[i][j]);

        return mat;
    }

    public static double[] convert(DoubleMatrix1D vector) {
        int n = vector.size();
        double[] v = new double[n];
        for (int i = 0; i < n; i++)
            v[i] = vector.get(i);
        return v;
    }

    public static double[][] convert(DoubleMatrix2D inVectors) {
        if (inVectors == null) return null;

        int m = inVectors.rows();
        int n = inVectors.columns();

        double[][] inV = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                inV[i][j] = inVectors.get(i, j);

        return inV;
    }


}