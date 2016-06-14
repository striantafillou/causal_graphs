package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Runs GES on the (hopefully small) list of variables, containing the given
 * target, and trims the result to a Markov blanket DAG for the target.
 * <p/>
 * All of the variables must be in the specified data set.
 * <p/>
 * The intended use for this is to filter the results of some other method of
 * estimating the Markov blanket of thet target, which includes the target,
 * hopefully almost all (if not all) of the actual Markov blanket variables, and
 * possibly a few extra variables, to produce a good estimate of the Markov
 * blanket DAG.
 *
 * @author Joseph Ramsey
 */
public class GesMbFilter {

    private DataSet dataSet;
    private Ges search;

    public GesMbFilter(DataSet dataSet) {
        this.dataSet = dataSet;
        search = new Ges(dataSet);
        search.setSamplePrior(10.0);
        search.setStructurePrior(0.01);
    }

    public Graph filter(List<Node> variable, Node target) {
        List<Node> dataVars = new LinkedList<Node>();

        for (Node node1 : variable) {
            dataVars.add(dataSet.getVariable(node1.getName()));
        }

        Graph mbPattern = search.search(new LinkedList<Node>(dataVars));
        Node dataTarget = dataSet.getVariable(target.getName());

        MbUtils.trimToMbNodes(mbPattern, dataTarget, false);
        MbUtils.trimEdgesAmongParents(mbPattern, dataTarget);
        MbUtils.trimEdgesAmongParentsOfChildren(mbPattern, dataTarget);

        return mbPattern;
    }
}
