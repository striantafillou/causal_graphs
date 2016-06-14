package edu.cmu.tetrad.search;

import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.search.indtest.IndTestChiSquare;
import edu.cmu.tetrad.graph.NodePair;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.*;

/**
 * Utilities for resolving inconsistencies that arise between sepsets learned for
 * overlapping datasets. This occurs frequently when using the DCI and ION algorithms.
 *                                  f
 * @author Robert Tillman
 */
public final class ResolveSepsets {

    /**
     * Resolves all inconsistencies between sepsets using a paricular method. Returns
     * a sepsetMapDci with the resolved separations and associations. resolvedIndependent
     * and resolvedDependent keep up with the number resolved to check later against
     * the truth
     *
     * @param sepsets
     * @param independenceTests
     * @param method
     * @param resolvedIndependent
     * @param resolvedDependent
     * @return
     */
    public static SepsetMapDci ResolveSepsets(List<SepsetMapDci> sepsets, List<IndependenceTest> independenceTests, String method, SepsetMapDci resolvedIndependent, SepsetMapDci resolvedDependent) {
        SepsetMapDci resolvedSepset = new SepsetMapDci();
        // get all variables
        Set<Node> allVars = new HashSet<Node>();
        for (IndependenceTest independenceTest : independenceTests) {
            allVars.addAll(independenceTest.getVariables());
        }
        // checks each pair of nodes for inconsistencies across independenceTests
        for (NodePair pair : allNodePairs(new ArrayList<Node>(allVars))) {
            // gets independenceTests and sepsets for every dataset with the pair
            List<List<List<Node>>> pairSepsets = new ArrayList<List<List<Node>>>();
            List<IndependenceTest> testsWithPair = new ArrayList<IndependenceTest>();
            for (int k=0; k<independenceTests.size(); k++) {
                IndependenceTest independenceTest = independenceTests.get(k);
                if (independenceTest.getVariables().containsAll(Arrays.asList(pair.getFirst(), pair.getSecond()))) {
                    pairSepsets.add(sepsets.get(k).getSet(pair.getFirst(), pair.getSecond()));
                    testsWithPair.add(independenceTest);
                }
            }
            // only check if pair is included in more than one dataset
            if (testsWithPair.size()<2) {
                // if pair only in one dataset then add all to resolvedSepset
                if (testsWithPair.size()==1) {
                    if (pairSepsets.get(0)==null) {
                        continue;
                    }
                    for (List<Node> sepset : pairSepsets.get(0)) {
                        resolvedSepset.set(pair.getFirst(), pair.getSecond(), sepset);
                    }
                }
                continue;
            }
            // check each conditioning set from a dataset
            List<List<Node>> allConditioningSets = new ArrayList<List<Node>>();
            for (List<List<Node>> conditioningSet : pairSepsets) {
                if (conditioningSet==null) {
                    continue;
                }
                allConditioningSets.addAll(conditioningSet);
            }
            for (List<Node> conditioningSet : allConditioningSets) {
                List<IndependenceTest> testsWithSet = new ArrayList<IndependenceTest>();
                for (IndependenceTest independenceTest : testsWithPair) {
                    if (independenceTest.getVariables().containsAll(conditioningSet)||conditioningSet.isEmpty()) {
                        testsWithSet.add(independenceTest);
                    }
                }
                // only check if more than one dataset have test
                if (testsWithSet.size()<2) {
                    // if conditioning set only in one dataset then add to resolvedSepset
                    if (testsWithPair.size()==1) {
                        resolvedSepset.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                    }
                    continue;
                }
                boolean separated = false;
                boolean inconsistent = false;
                for (int k=0; k<testsWithSet.size(); k++) {
                    IndependenceTest testWithSet = testsWithSet.get(k);
                    if (k==0) {
                        separated = testWithSet.isIndependent(pair.getFirst(), pair.getSecond(), conditioningSet);
                        continue;
                    }
                    // checks to see if inconsistent
                    if (separated!=testWithSet.isIndependent(pair.getFirst(), pair.getSecond(), conditioningSet)) {
                        inconsistent = true;
                        break;
                    }
                }
                // if inconsistent then use pooling method
                if (inconsistent) {
                    // if using Fisher pooling
                    if (method.equals("fisher")) {
                        if (isIndependentPooledFisher(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("tippett")) {
                        if (isIndependentPooledTippett(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("worsleyfriston")) {
                        if (isIndependentPooledWorsleyFriston(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("stouffer")) {
                        if (isIndependentPooledStouffer(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("mudholkergeorge")) {
                        if (isIndependentPooledMudholkerGeorge(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("averagetest")) {
                        if (isIndependentPooledAverageTest(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("average")) {
                        if (isIndependentPooledAverage(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else if (method.equals("random")) {
                        if (isIndependentPooledRandom(testsWithSet, pair.getFirst(), pair.getSecond(), conditioningSet)) {
                            resolvedSepset.set(pair.getFirst(), pair.getFirst(), conditioningSet);
                            resolvedIndependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                        else {
                            resolvedDependent.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                        }
                    }
                    else {
                        throw new RuntimeException("Invalid Test");
                    }

                }
                else {
                    resolvedSepset.set(pair.getFirst(), pair.getSecond(), conditioningSet);
                }
            }
        }
        return resolvedSepset;
    }

    /**
     * Tests for independence using one of the pooled methods
     * 
     * @param method
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooled(String method, List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        if (method.equals("fisher")) {
            return isIndependentPooledFisher(independenceTests, x, y, condSet);
        }
        else if (method.equals("tippett")) {
            return isIndependentPooledTippett(independenceTests, x, y, condSet);
        }
        else if (method.equals("worsleyfriston")) {
            return isIndependentPooledWorsleyFriston(independenceTests, x, y, condSet);
        }
        else if (method.equals("stouffer")) {
            return isIndependentPooledStouffer(independenceTests, x, y, condSet);
        }
        else if (method.equals("mudholkergeorge")) {
            return isIndependentPooledMudholkerGeorge(independenceTests, x, y, condSet);
        }
        else if (method.equals("averagetest")) {
            return isIndependentPooledAverageTest(independenceTests, x, y, condSet);
        }
        else if (method.equals("average")) {
            return isIndependentPooledAverage(independenceTests, x, y, condSet);
        }
        else if (method.equals("random")) {
            return isIndependentPooledRandom(independenceTests, x, y, condSet);
        }
        else {
            throw new RuntimeException("Invalid Test");
        }
    }

    /**
     * Checks independence from pooled samples using Fisher's method.
     *
     * See R. A. Fisher. Statistical Methods for Research Workers. Oliver and
     * Boyd, 11th edition, 1950.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledFisher(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double tf = 0.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            tf += -2.0*Math.log(independenceTest.getPValue());
        }
        double p = 1.0 - ProbUtils.chisqCdf(tf, 2*independenceTests.size());
        return (p > alpha);
    }

    /**
     * Checks independence from pooled samples using Tippett's method
     *
     * See L. H. C. Tippett. The Method of Statistics. Williams and Norgate,
     * 1st edition, 1950.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledTippett(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double p = -1.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            if (p==-1.0) {
                p = independenceTest.getPValue();
                continue;
            }
            double newp = independenceTest.getPValue();
            if (newp<p) {
                p = newp;
            }
        }
        return (p > (1-Math.pow(1-alpha,(1/(double)independenceTests.size()))));
    }

    /**
     * Checks independence from pooled samples using Wilkinson's method
     *
     * I don't have a reference for this but its basically in between Tippett
     * and Worsley and Friston.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @param r
     * @return
     */
    public static boolean isIndependentPooledWilkinson(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet, int r) {
        double alpha = independenceTests.get(0).getAlpha();
        double p[] = new double[independenceTests.size()];
        int k = 0;
        for (IndependenceTest independenceTest : independenceTests) {
            p[k] = independenceTest.getPValue();
            k++;
        }
        java.util.Arrays.sort(p);
        return (p[r] > (1-Math.pow(1-Math.pow(alpha,1.0/(double)r),(r/(double)independenceTests.size()))));
    }

    /**
     * Checks independence from pooled samples using Worsley and Friston's method
     *
     * See K. J. Worsely and K. J. Friston. A test for conjunction. Statistics and
     * Probability Letters, 47:135–140, 2000.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledWorsleyFriston(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double p = -1.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            if (p==-1.0) {
                p = independenceTest.getPValue();
                continue;
            }
            double newp = independenceTest.getPValue();
            if (newp>p) {
                p = newp;
            }
        }
        return (p > Math.pow(alpha,(1/(double)independenceTests.size())));
    }

    /**
     * Checks independence from pooled samples using Stouffer et al.'s method
     *
     * See S. A. Stouffer, E. A. Suchman, L. C. Devinney, S. A. Star, and R. M.
     * Williams. The American Soldier: Vol. 1. Adjustment During Army Life.
     * Princeton University Press, 1949.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledStouffer(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double ts = 0.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            ts += ProbUtils.normalQuantile(independenceTest.getPValue())/Math.sqrt(independenceTests.size());
        }
        double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(ts)));
        return (p > alpha);
    }

    /**
     * Checks independence from pooled samples using Mudholker and George's method
     *
     * See G. S. Mudholkar and E. O. George. The logit method for combining
     * probabilities. In J. Rustagi, editor, Symposium on Optimizing Methods in
     * Statisics, pages 345–366. Academic Press, 1979.
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledMudholkerGeorge(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double c = Math.sqrt(3*(5*independenceTests.size()+4)/(double)(independenceTests.size()*Math.pow(Math.PI,2)*(5*independenceTests.size()+2)));
        double tm = 0.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            double pk = independenceTest.getPValue();
            tm += -c*Math.log(pk/(1-pk));
        }
        double p = 2.0 * (1.0 - ProbUtils.tCdf(Math.abs(tm), 5*independenceTests.size()+4));
        return (p > alpha);
    }

    /**
     * Checks independence from pooled samples by taking the average p value
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledAverage(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double p = 0.0;
        for (IndependenceTest independenceTest : independenceTests) {
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            p += independenceTest.getPValue()/independenceTests.size();
        }
        return (p > alpha);
    }

    /**
     * Checks independence from pooled samples by taking the average test statistic
     * CURRENTLY ONLY WORKS FOR CHISQUARE TEST
     *
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledAverageTest(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        double ts = 0.0;
        int df = 0;
        for (IndependenceTest independenceTest : independenceTests) {
            if (!(independenceTest instanceof IndTestChiSquare)) {
                throw new RuntimeException("Must be ChiSquare Test");
            }
            List<Node> localCondSet = new ArrayList<Node>();
            for (Node node : condSet) {
                localCondSet.add(independenceTest.getVariable(node.getName()));
            }
            independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
            ts += ((IndTestChiSquare)independenceTest).getXSquare()/independenceTests.size();
            df += ((IndTestChiSquare)independenceTest).getDf();
        }
        df = df/independenceTests.size();
        double p = 1.0 - ProbUtils.chisqCdf(ts, df);
        return (p > alpha);
    }

    /**
     * Checks independence from pooled samples by randomly selecting a p value
     * 
     * @param independenceTests
     * @param x
     * @param y
     * @param condSet
     * @return
     */
    public static boolean isIndependentPooledRandom(List<IndependenceTest> independenceTests, Node x, Node y, List<Node> condSet) {
        double alpha = independenceTests.get(0).getAlpha();
        int r = RandomUtil.getInstance().nextInt(independenceTests.size());
        IndependenceTest independenceTest = independenceTests.get(r);
        List<Node> localCondSet = new ArrayList<Node>();
        for (Node node : condSet) {
            localCondSet.add(independenceTest.getVariable(node.getName()));
        }
        independenceTest.isIndependent(independenceTest.getVariable(x.getName()), independenceTest.getVariable(y.getName()), localCondSet);
        double p = independenceTest.getPValue();
        return (p > alpha);
    }

    /**
     * Generates NodePairs of all possible pairs of nodes from given
     * list of nodes.
     */
    public static List<NodePair> allNodePairs(List<Node> nodes) {
        List<NodePair> nodePairs = new ArrayList<NodePair>();
        for (int j = 0; j < nodes.size() - 1; j++) {
            for (int k = j + 1; k < nodes.size(); k++) {
                nodePairs.add(new NodePair(nodes.get(j), nodes.get(k)));
            }
        }
        return nodePairs;
    }
}
