package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.search.indtest.IndTestChiSquare;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import junit.framework.TestCase;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Tests the ResolveSepsets utilities.
 *
 * @author Robert Tillmab
 */
public class TestResolveSepsets extends TestCase {

    private String dir = "/home/rtillman/Desktop/temp/";
    private File correctFile = new File(dir+"correct.dat");
    private File incorrectFile = new File(dir+"incorrect.dat");
    private File accuracy = new File(dir+"resolve.dat");
    private File indFile = new File(dir+"independent.dat");

    private String[] methods = {"fisher", "tippett", "worsleyfriston", "stouffer", "mudholkergeorge",
        "averagetest", "average", "random"};

    private int[] nsizes = {50,100,500,1000,2500};
    
    public TestResolveSepsets(String name) {
        super(name);
    }

    public void testSimulation() {
        int[] d = {10,25,100};
        for (int di : d) {
            for (int k=0; k<100; k++) {
                discreteTest(di);
                System.out.println(di + ", " + (k+1));
            }
        }
    }

    public void discreteTest(int d) {
        Dag graph = GraphUtils.randomDag(d, 0, d, 3, 2, 1, true);
        BayesPm pm = new BayesPm(graph, 4, 4);
        BayesIm im = new MlBayesIm(pm, MlBayesIm.RANDOM);
        List<Set<Node>> subsetsNodes = subsetsFromDag(graph, 2);

        for (int n : nsizes) {
            DataSet dataset = im.simulateData(2*n, false);
            List<DataSet> missingDatasets = missingDatasets(dataset, subsetsNodes);
            List<IndependenceTest> independenceTests = new ArrayList<IndependenceTest>();
            for (DataSet missingDataset : missingDatasets) {
                independenceTests.add(new IndTestChiSquare(missingDataset, .01));
            }
            Map<String, Integer> correct = new HashMap<String, Integer>();
            Map<String, Integer> incorrect = new HashMap<String, Integer>();
            Map<String, Integer> independent = new HashMap<String, Integer>();
            Map<String, Integer> associated = new HashMap<String, Integer>();
            for (String method : methods) {
                correct.put(method, 0);
                incorrect.put(method, 0);
                independent.put(method, 0);
                associated.put(method, 0);
            }
            tryMethods(graph, independenceTests, correct, incorrect, independent, associated);
            try {
                FileWriter correctWr = new FileWriter(correctFile, true);
                PrintWriter pcorrectWr = new PrintWriter(correctWr);
                FileWriter incorrectWr = new FileWriter(incorrectFile, true);
                PrintWriter pincorrectWr = new PrintWriter(incorrectWr);
                FileWriter accuracyWr = new FileWriter(accuracy, true);
                PrintWriter paccuracyWr = new PrintWriter(accuracyWr);
                FileWriter indWr = new FileWriter(indFile, true);
                PrintWriter pindWr = new PrintWriter(indWr);
                for (String method : methods) {
                    int correctnum = correct.get(method);
                    int incorrectnum = incorrect.get(method);
                    pcorrectWr.print(correctnum + ",");
                    pincorrectWr.print(incorrectnum + ",");
                    double acc = 1.0;
                    if (correctnum+incorrectnum>0) {
                        acc = (double)correctnum/(correctnum+incorrectnum);
                    }
                    paccuracyWr.print(acc + ",");
                    int indnum = independent.get(method);
                    int assnum = associated.get(method);
                    if (indnum+assnum>0) {
                        pindWr.print(((double)indnum/(indnum+assnum)) + ",");
                    }
                }
                pcorrectWr.close();
                pincorrectWr.close();
                paccuracyWr.close();
                pindWr.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            FileWriter correctWr = new FileWriter(correctFile, true);
            PrintWriter pcorrectWr = new PrintWriter(correctWr);
            FileWriter incorrectWr = new FileWriter(incorrectFile, true);
            PrintWriter pincorrectWr = new PrintWriter(incorrectWr);
            FileWriter accuracyWr = new FileWriter(accuracy, true);
            PrintWriter paccuracyWr = new PrintWriter(accuracyWr);
            FileWriter indWr = new FileWriter(indFile, true);
            PrintWriter pindWr = new PrintWriter(indWr);
            pcorrectWr.println();
            pincorrectWr.println();
            paccuracyWr.println();
            pindWr.println();
            pcorrectWr.close();
            pincorrectWr.close();
            paccuracyWr.close();
            pindWr.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // gets random subsets nodes in a dag of specified size
    private static List<Set<Node>> subsetsFromDag(Graph graph, int s) {
        int sets = s;
        int n = graph.getNumNodes();
        List<Node> nodes = graph.getNodes();
        List<Node> overlap = new ArrayList<Node>();
        Set<Node> set1 = new HashSet<Node>();
        Set<Node> set2 = new HashSet<Node>();
        RandomUtil generator = RandomUtil.getInstance();
        long overlapsize = Math.round(n*.6);
        while (nodes.size()>n-overlapsize) {
            overlap.add(nodes.remove(generator.nextInt(nodes.size())));
        }
        while (!nodes.isEmpty()) {
            if (generator.nextInt(2)==0) {
                set1.add(nodes.remove(generator.nextInt(nodes.size())));
            }
            else {
                set2.add(nodes.remove(generator.nextInt(nodes.size())));
            }
        }
        set1.addAll(overlap);
        set2.addAll(overlap);
        List<Set<Node>> dds = new ArrayList<Set<Node>>();
        dds.add(set1);
        dds.add(set2);
        sets -= 2;
        nodes = graph.getNodes();
        while (sets>0) {
            List<Node> lastSet = new ArrayList<Node>(dds.get(dds.size()-1));
            Set<Node> newSet = new HashSet<Node>();
            while (newSet.size()<overlapsize) {
                newSet.add(lastSet.remove(generator.nextInt(lastSet.size())));
            }
            int averageSetSize = 0;
            for (Set<Node> set : dds) {
                averageSetSize += set.size();
            }
            averageSetSize /= dds.size();
            for (Node node : nodes) {
                if (!lastSet.contains(node)) {
                    newSet.add(node);
                }
                if (newSet.size()>=averageSetSize) {
                    break;
                }
            }
            dds.add(newSet);
            sets--;
        }
        return dds;
    }

    // generates missing data for ion and structural em
    private static List<DataSet> missingDatasets(DataSet dataset, List<Set<Node>> missingVars) {
        List<DataSet> datasetSet = new ArrayList<DataSet>();
        int datapoint = 0;
        for (Set<Node> missing : missingVars) {
            int[] ints = new int[(dataset.getNumRows()/missingVars.size())];
            for (int i=0;i<(dataset.getNumRows()/missingVars.size());i++) {
                ints[i] = datapoint;
                datapoint++;
            }
            DataSet newDataset = dataset.subsetRows(ints);
            for (Node node : dataset.getVariables()) {
                boolean remove = true;
                for (Node node2 : missing) {
                    if (node.getName().equals(node2.getName())) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    newDataset.removeColumn(node);
                }
            }
            datasetSet.add(newDataset);
        }
        return datasetSet;
    }



    public void tryMethods(Graph graph, List<IndependenceTest> independenceTests,
        Map<String, Integer> correct, Map<String, Integer> incorrect,
        Map<String, Integer> independent, Map<String, Integer> associated) {
        List<SepsetMapDci> sepsets = new ArrayList<SepsetMapDci>();
        Set<Node> allVars = new HashSet<Node>();
        for (IndependenceTest independenceTest : independenceTests) {
            allVars.addAll(independenceTest.getVariables());
        }
        for (IndependenceTest independenceTest : independenceTests) {
            Graph fullGraph = new EdgeListGraph(new ArrayList<Node>(allVars));
            fullGraph.fullyConnect(Endpoint.CIRCLE);
            FasDci adj = new FasDci(new EdgeListGraph(fullGraph), independenceTest);
            adj.setDepth(3);
            sepsets.add(adj.search());
        }
        List<NodePair> allPairs = ResolveSepsets.allNodePairs(new ArrayList<Node>(allVars));
        for (String method : methods) {
            SepsetMapDci resolvedInd = new SepsetMapDci();
            SepsetMapDci resolvedDep = new SepsetMapDci();
            ResolveSepsets.ResolveSepsets(sepsets, independenceTests, method, resolvedInd, resolvedDep);
            for (NodePair pair : allPairs) {
                Node x = graph.getNode(pair.getFirst().getName());
                Node y = graph.getNode(pair.getSecond().getName());
                List<List<Node>> indCondSets = resolvedInd.getSet(pair.getFirst(), pair.getSecond());
                if (indCondSets!=null) {
                    for (List<Node> indCondSet : indCondSets) {
                        List<Node> z = new ArrayList<Node>();
                        for (Node c : indCondSet) {
                            z.add(graph.getNode(c.getName()));
                        }
                        if (graph.isDSeparatedFrom(x, y, z)) {
                            Integer num = correct.get(method) + 1;
                            correct.put(method, num);
                            num = independent.get(method) + 1;
                            independent.put(method, num);
                        }
                        else {
                            Integer num = incorrect.get(method) + 1;
                            incorrect.put(method, num);
                            num = associated.get(method) + 1;
                            associated.put(method, num);
                        }
                    }
                }
                List<List<Node>> depCondSets = resolvedDep.getSet(pair.getFirst(), pair.getSecond());
                if (depCondSets!=null) {
                    for (List<Node> depCondSet : depCondSets) {
                        List<Node> z = new ArrayList<Node>();
                        for (Node c : depCondSet) {
                            z.add(graph.getNode(c.getName()));
                        }
                        if (graph.isDConnectedTo(x, y, z)) {
                            Integer num = correct.get(method) + 1;
                            correct.put(method, num);
                            num = associated.get(method) + 1;
                            associated.put(method, num);
                        }
                        else {
                            Integer num = incorrect.get(method) + 1;
                            incorrect.put(method, num);
                            num = independent.get(method) + 1;
                            independent.put(method, num);
                        }
                    }
                }
            }
        }
    }

}


