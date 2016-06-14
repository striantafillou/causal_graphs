package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.search.indtest.IndTestDSep;
import java.util.Arrays;
import java.util.List;


/**
 * Example of ION with independence oracle.
 *
 * @author Robert Tillman
 */
public class IonOracleExample {

    public IonOracleExample() {
    }

    public static void main(String args[]) {
        // create 4 nodes, X, Y, W, Z
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");

        // create DAG X -> Y -> Z
        Graph dag1 = new EdgeListGraph(Arrays.asList(X, Y, Z));
        dag1.addDirectedEdge(X, Y);
        dag1.addDirectedEdge(Y, Z);

        // creat DAG X -> W -> Z
        Graph dag2 = new EdgeListGraph(Arrays.asList(X, W, Z));
        dag2.addDirectedEdge(X, W);
        dag2.addDirectedEdge(W, Z);

        // learn PAGs for each DAG using FCI algorithm with independence oracle
        Fci fci1 = new Fci(new IndTestDSep(dag1));
        Graph pag1 = fci1.search();
        Fci fci2 = new Fci(new IndTestDSep(dag2));
        Graph pag2 = fci2.search();

        // perform ION search with resulting PAGs as input
        Ion search = new Ion(Arrays.asList(pag1, (Graph) pag2));
        List<Graph> ionoutput = search.search();

        System.out.println("Initial DAGs: \n" + dag1 + dag2);
        System.out.println("Resulting PAGs: \n" + pag1 + pag2);
        System.out.println("ION Result: \n" + ionoutput);
    }


}
