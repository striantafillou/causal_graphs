package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a set of variables, reports the DAG over these variables that maximizes
 * the p value of the estimated SEM model for this DAG as against the estimated
 * SEM model for all alternative DAGs.
 * <p/>
 * Should only be attempted on very small graphs, out of respect for the
 * combinatorial explosion of number of DAGs for graphs as the number of
 * variables increases.
 *
 * @author Joseph Ramsey
 */
public class MaxPValueSearch {
    private DataSet data;
    private List<Node> nodes;
    private double alpha;
    private int maxEdges;
    private Dag trueDag;
    private SemIm trueIm;

    public MaxPValueSearch(DataSet data, double alpha, int maxEdges) {
        if (data == null || !data.isContinuous()) {
            throw new IllegalArgumentException("Please provide a continuous dataset.");
        }

        if (alpha < 0) {
            throw new IllegalArgumentException("Alpha must be >= 0: " + alpha);
        }

        if (maxEdges < 0) {
            throw new IllegalArgumentException("Max edges must be >= 0: " + maxEdges);
        }

        this.data = data;
        this.nodes = data.getVariables();
        this.alpha = 1e-10; //alpha;
        this.maxEdges = maxEdges;
    }

    public void setTrueDag(Dag dag) {
        this.trueDag = dag;
    }

    public void setTrueIm(SemIm trueIm) {
        this.trueIm = trueIm;
    }

    public MaxPValueSearch.Result search() {
        LinkedList<Double> maxPs = new LinkedList<Double>();
        LinkedList<Dag> maxDags = new LinkedList<Dag>();

        if (trueDag != null) {
            double trueFml = printDagEstimate("TRUE DAG ", trueDag);
        }

        for (int numEdges = 0; numEdges <= maxEdges; numEdges++) {
            System.out.println("######## Num Edges = " + numEdges);

            DagIterator3 iterator3 = new DagIterator3(nodes, numEdges, numEdges);
            int i = 0;

            while (iterator3.hasNext()) {
                if ((++i) % 100 == 0) System.out.println(i);

                Dag dag = iterator3.next();

                if (dag.equals(trueDag)) {
                    printDagEstimate("TRUE DAG FOUND BY GENERATOR", dag);
                }

                SemPm pm = new SemPm(dag);
                SemEstimator estimator = new SemEstimator(data, pm);
                estimator.estimate();
                SemIm im = estimator.getEstimatedSem();
                double p = im.getPValue();

//                System.out.println(dag);
//                System.out.println(p);
//                System.out.println("Ratio = " + (im.getFml() / trueFml));

//                if (p < alpha) {
//                    continue;
//                }

                if (p < .01) {
                    continue;
                }

                maxPs.add(p);
                maxDags.add(dag);

            }

            if (!maxDags.isEmpty()) {
//                double maxP = 0.0;
//
//                for (double _p : maxPs) {
//                    if (_p > maxP) maxP = _p;
//                }
//
//                for (int j = maxDags.size() - 1; j >= 0; j--) {
//                    if (maxPs.get(j) < maxP - .01) {
//                        maxPs.remove(j);
//                        maxDags.remove(j);
//                    }
//                }

//                printTopDags(maxDags, maxPs);

                if (trueDag != null) {
                    printDagEstimate("TRUE DAG", trueDag);
                }

                if (!maxDags.contains(trueDag)) {
                    System.out.println("WARNING!!!! TRUE DAG NOT IN THE LIST!!!");
                }

                return new Result(maxDags, maxPs);
            }
        }

        return new Result(new ArrayList<Dag>(), new ArrayList<Double>());
    }

    public class Result {
        private List<Dag> dags;
        private List<Double> pValues;

        public Result(List<Dag> dags, List<Double> pValues) {
            if (dags == null || pValues == null || dags.size() != pValues.size()) {
                throw new IllegalArgumentException();
            }

            this.setDags(dags);
            this.setPValues(pValues);
            sort();
        }

        private void sort() {
            List<Dag> sortedDags = new ArrayList<Dag>();
            List<Double> sortedPValues = new ArrayList<Double>();

            while (!getDags().isEmpty()) {
                double max = 0.0;
                int index = -1;

                for (int i = 0; i < getDags().size(); i++) {
                    if (getPValues().get(i) > max) {
                        max = getPValues().get(i);
                        index = i;
                    }
                }

                sortedDags.add(getDags().get(index));
                sortedPValues.add(getPValues().get(index));
                getDags().remove(index);
                getPValues().remove(index);
            }

            dags = sortedDags;
            pValues = sortedPValues;
        }

        public List<Dag> getDags() {
            return dags;
        }

        public void setDags(List<Dag> dags) {
            this.dags = dags;
        }

        public List<Double> getPValues() {
            return pValues;
        }

        public void setPValues(List<Double> pValues) {
            this.pValues = pValues;
        }
    }

    public List<Dag> search2() {
        LinkedList<Double> maxPs = new LinkedList<Double>();
        LinkedList<Dag> maxDags = new LinkedList<Dag>();

        for (int numEdges = 0; numEdges <= maxEdges; numEdges++) {
            System.out.println("######## Num Edges = " + numEdges);

            DagIterator3 iterator3 = new DagIterator3(nodes, numEdges, numEdges);
            int i = 0;

            while (iterator3.hasNext()) {
                if ((++i) % 100 == 0) System.out.println(i);

                Dag dag = iterator3.next();

                if (trueDag != null && dag.equals(trueDag)) {
                    printDagEstimate("TRUE DAG FOUND BY GENERATOR", dag);
                }

                SemPm pm = new SemPm(dag);
                SemEstimator estimator = new SemEstimator(data, pm);
                estimator.estimate();
                SemIm im = estimator.getEstimatedSem();
                double p = im.getPValue();

                if (p < alpha) {
                    continue;
                }

                maxPs.add(p);
                maxDags.add(dag);

                System.out.println(dag);
                System.out.println("P = " + p);

                if (trueIm != null) {
                    double sum = sumDifferencesFromTrue(im, trueIm);
                    System.out.println("Sum of diffs of sample covar vs. estimated covar elements = " + sum);
                    double sum2 = sumParameterDifferencesFromTrue(im, trueIm);
                    System.out.println("Sum of diffs of true params from corresponding estimated params = " + sum2);
                    System.out.println("FML = " + im.getFml());
                }
            }

        }

        if (!maxDags.isEmpty()) {
            double maxP = 0.0;

            for (double _p : maxPs) {
                if (_p > maxP) maxP = _p;
            }

            for (int j = maxDags.size() - 1; j >= 0; j--) {
                if (maxPs.get(j) < maxP - .01) {
                    maxPs.remove(j);
                    maxDags.remove(j);
                }
            }

            printTopDags(maxDags, maxPs);

            if (trueDag != null) {
                printDagEstimate("TRUE DAG", trueDag);
            }

            if (!maxDags.contains(trueDag)) {
                System.out.println("WARNING!!!! TRUE DAG NOT IN THE LIST!!!");
            }

            return maxDags;
        }

        return new LinkedList<Dag>();
    }

    private double sumDifferencesFromTrue(SemIm im, SemIm trueIm) {
        DoubleMatrix2D implCovar = im.getImplCovar();
        DoubleMatrix2D sampleCovar = im.getSampleCovar();
        double sum = 0.0;

        for (int i = 0; i < implCovar.rows(); i++) {
            for (int j = 0; j < implCovar.columns(); j++) {
                double diff = implCovar.get(i, j) - sampleCovar.get(i, j);
                sum += diff * diff;
            }
        }

        return sum;
    }

    private double sumParameterDifferencesFromTrue(SemIm im, SemIm trueIm) {
        double sum = 0.0;

        SemGraph estGraph = im.getSemPm().getGraph();

        for (Parameter trueParam : trueIm.getFreeParameters()) {
            Node estA = estGraph.getNode(trueParam.getNodeA().toString());
            Node estB = estGraph.getNode(trueParam.getNodeB().toString());

            if (trueParam.getType() == ParamType.COEF) {
                double trueValue = trueIm.getEdgeCoef(trueParam.getNodeA(), trueParam.getNodeB());
                double estValue = im.getEdgeCoef(estA, estB);

                if (Double.isNaN(estValue)) {
                    estValue = 0.0;
                }

                double diff = trueValue - estValue;
                sum += diff * diff;
            }

            if (trueParam.getType() == ParamType.COVAR) {
                double trueValue = trueIm.getErrCovar(trueParam.getNodeA());
                double estValue = im.getErrCovar(estA);
                double diff = trueValue - estValue;
                sum += diff * diff;
            }
        }

        return sum;
    }

    private double printDagEstimate(String label, Dag dag) {
        if (trueDag == null) {
            throw new IllegalArgumentException();
        }

        SemPm pm = new SemPm(dag);
        SemEstimator estimator = new SemEstimator(data, pm);
        estimator.estimate();
        SemIm im = estimator.getEstimatedSem();
        double p = im.getPValue();

        System.out.println(label + dag);
        System.out.println("P = " + p);
        System.out.println();

        return im.getFml();
    }

    private void printTopDags(LinkedList<Dag> maxDags, LinkedList<Double> maxPs) {
        if (maxDags.size() != maxPs.size()) {
            throw new IllegalArgumentException();
        }

        System.out.println("TOP DAGs");

        for (int i = 0; i < maxDags.size(); i++) {
            System.out.println("\n#" + (i + 1) + ": P = " + maxPs.get(i) + "\n" + maxDags.get(i));
        }
    }

    public static Graph convertToPattern(List<Dag> dags) {
        if (dags == null || dags.isEmpty()) {
            return null;
        }

        Graph pattern = new EdgeListGraph(dags.get(0));

        for (int i = 1; i < dags.size(); i++) {
            Dag dag = dags.get(i);

            for (Edge edge : pattern.getEdges()) {
                if (!dag.isAdjacentTo(edge.getNode1(), edge.getNode2())) {

                    System.out.println("Not all DAGs have the same adjacencies");
                    return null;
                }
            }

            for (Edge edge : dag.getEdges()) {
                if (!pattern.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                    System.out.println("Not all DAGs have the same adjacencies");
                    return null;
                }
            }

            for (Edge patternEdge: pattern.getEdges()) {
                if (Edges.isUndirectedEdge(patternEdge)) {
                    continue;
                }

                Node node1 = patternEdge.getNode1();
                Node node2 = patternEdge.getNode2();
                Edge dagEdge = dag.getEdge(node1, node2);

                if (Edges.getDirectedEdgeHead(patternEdge) != Edges.getDirectedEdgeHead(dagEdge)) {
                    pattern.removeEdge(patternEdge);
                    pattern.addUndirectedEdge(node1, node2);
                }
            }
        }

        return pattern;
    }
}