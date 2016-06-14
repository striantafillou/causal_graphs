///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.indtest.IndependenceTest;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * This class provides the datastructures and methods for carrying out the
 * Cyclic Causal Discovery algorithm (CCD) described by Thomas Richardson and
 * Peter Spirtes in Chapter 7 of Computation, Causation, & Discovery by Glymour
 * and Cooper eds.  The comments that appear below are keyed to the algorithm
 * specification on pp. 269-271. </p> The search method returns an instance of a
 * Graph but it also constructs two lists of node triples which represent the
 * underlines and dotted underlines that the algorithm discovers.
 *
 * @author Frank C. Wimberly
 */
public final class Ccd {

    private IndependenceTest test;
    private int depth = -1;
    private Knowledge knowledge;
    private List<Node> nodes;
    private Set<Triple> underLineTriples;
    private Set<Triple> dottedUnderLineTriples;

    /**
     * The arguments of the constructor are an oracle which answers conditional
     * independence questions.  In the case of a continuous dataset it will most
     * likely be an instance of the IndTestCramerT class.  The second argument
     * is not used at this time.  The author (Wimberly) asked Thomas Richardson
     * about how to use background knowledge and his answer was that it should
     * be applied after steps A-F had been executed.  Any implementation of the
     * use of background knowledge will be done later.
     *
     * @param knowledge Background knowledge. Not used yet--can be null.
     */
    public Ccd(IndependenceTest test, Knowledge knowledge) {
        this.knowledge = knowledge;
        this.test = test;
        this.nodes = new LinkedList<Node>(test.getVariables());
        this.underLineTriples = new HashSet<Triple>();
        this.dottedUnderLineTriples = new HashSet<Triple>();
    }

    /**
     * The arguments of the constructor are an oracle which answers conditional
     * independence questions.  In the case of a continuous dataset it will most
     * likely be an instance of the IndTestCramerT class.  The second argument
     * is not used at this time.  The author (Wimberly) asked Thomas Richardson
     * about how to use background knowledge and his answer was that it should
     * be applied after steps A-F had been executed.  Any implementation of the
     * use of background knowledge will be done later.
     */
    public Ccd(IndependenceTest test) {
        this(test, new Knowledge());
    }

    /**
     * The search method assumes that the IndependenceTest provided to the
     * constructor is a conditional independence oracle for the SEM (or Bayes
     * network) which describes the causal structure of the population. The
     * method returns a PAG instantiated as a Tetrad GaSearchGraph which
     * represents the equivalence class of digraphs which are d-separation
     * equivalent to the digraph of the underlying model (SEM or BN). </p>
     * Although they are not returned by the search method it also computes two
     * lists of triples which, respectively store the underlines and dotted
     * underlines of the PAG.
     */
    public Graph search() {

        TetradLogger.getInstance().info("Starting CCD algorithm.");
        TetradLogger.getInstance().info("Independence test = " + test);
        TetradLogger.getInstance().info("Depth = " + depth);

        Graph phi = new EdgeListGraph(new LinkedList<Node>(nodes));
        phi.fullyConnect(Endpoint.CIRCLE);
//        LogUtils.getInstance().fine("Initial PAG in CCD search");
//        LogUtils.getInstance().fine(phi.toString());

        SepsetMap sepset = new SepsetMap();

        List[][][] supsepset =
                new LinkedList[nodes.size()][nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                for (int k = 0; k < nodes.size(); k++) {
                    supsepset[i][j][k] = null;
                }
            }
        }

        //Step A
        TetradLogger.getInstance().info("\nStep A");

        // ... Switching to the implementation of Step A from FCI since it has
        // background knowledge already incorporated. jdramsey
        int _depth = depth;

        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }

        for (int d = 0; d <= _depth; d++) {
            if (!searchAtDepth(phi, test, getKnowledge(), sepset, d)) {
                break;
            }
        }

        //Step B

        // ...Using collider orientation step from FCI since it doesn't check
        // both x--b--y and y--b--x.  jdramsey

        //For each triple of vertices A,B and C
        TetradLogger.getInstance().info("\nStep B");

        for (Node bnode : nodes) {

            //Set of nodes adjacent to B
            List<Node> adjB = phi.getAdjacentNodes(bnode);

            if (adjB.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjB.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node anode = adjB.get(choice[0]);
                Node cnode = adjB.get(choice[1]);

                if (!phi.isAdjacentTo(bnode, anode)) {
                    continue;
                }

                if (!phi.isAdjacentTo(bnode, cnode)) {
                    continue;
                }

                if (phi.isAdjacentTo(anode, cnode)) {
                    continue;
                }

                //Orient A*--*B*--*C as A->B<-C iff B not in sepset<A,C>
                if (!sepset.get(anode, cnode).contains(bnode)) {
                    if (!isDirEdgeAllowed(anode, bnode) ||
                            !isDirEdgeAllowed(cnode, bnode)) {
                        continue;
                    }

                    phi.setEndpoint(anode, bnode, Endpoint.ARROW);
                    phi.setEndpoint(bnode, anode, Endpoint.TAIL);
                    phi.setEndpoint(cnode, bnode, Endpoint.ARROW);
                    phi.setEndpoint(bnode, cnode, Endpoint.TAIL);
                    TetradLogger.getInstance().log("colliderOriented", "Orienting collider " + anode + "-->" +
                            bnode + "<--" + cnode);
                } else {
                    if (knowledge.edgeRequired(anode.getName(), bnode.getName()) &&
                            knowledge.edgeRequired(cnode.getName(), bnode.getName())) {
                        continue;
                    }

                    Triple ul = new Triple(anode, bnode, cnode);
                    underLineTriples.add(ul);
                    TetradLogger.getInstance().details("Adding underline " + ul);
                }
            }
        }

        //Step C
        TetradLogger.getInstance().info("\nStep C");

        for (int x = 0; x < nodes.size(); x++) {
            Node xnode = nodes.get(x);
            for (int y = 0; y < nodes.size(); y++) {

                // X,Y distinct
                if (x == y) {
                    continue;
                }

                Node ynode = nodes.get(y);

                // ...X and Y are adjacent...
                if (!phi.isAdjacentTo(xnode, ynode)) {
                    continue;
                }

                // Check each A
                for (int a = 0; a < nodes.size(); a++) {
                    if (a == x || a == y) {
                        continue;  //distinctness
                    }
                    Node anode = nodes.get(a);

                    //...A is not adjacent to X and A is not adjacent to Y...
                    if (phi.isAdjacentTo(xnode, anode) ||
                            phi.isAdjacentTo(ynode, anode)) {
                        continue;
                    }
                    //...X is not in sepset<A, Y>...
                    if ((sepset.get(nodes.get(a), nodes.get(y)).contains(
                            xnode))) {
                        continue;
                    }

                    //If A and X are d-connected given SepSet<A, Y>
                    //then orient Xo-oY or Xo-Y as X<-Y.
                    if (!test.isIndependent(anode, xnode,
                            sepset.get(nodes.get(a), nodes.get(y)))) {
                        if (!isDirEdgeAllowed(ynode, xnode)) {
                            continue;
                        }

                        // Needed precaution? jdramsey (Don't want to orient
                        // --- as <--, e.g. Maybe unnecessary.)
                        if (phi.getEndpoint(ynode, xnode) != Endpoint.CIRCLE) {
                            continue;
                        }

                        if (phi.getEndpoint(xnode, ynode) == Endpoint.ARROW) {
                            continue;
                        }

                        phi.setEndpoint(ynode, xnode, Endpoint.ARROW);
                        phi.setEndpoint(xnode, ynode, Endpoint.TAIL);

                        TetradLogger.getInstance().edgeOriented("Orienting " + ynode + "-->" + xnode);
                    }
                }
            }
        }

        //Step D
        TetradLogger.getInstance().info("\nStep D");

        //Construct Local(phi, V) for each node V in phi
        List[] local = new ArrayList[nodes.size()];
        for (int v = 0; v < nodes.size(); v++) {
            Node vnode = nodes.get(v);
            local[v] = new ArrayList<Node>();

            //Is X p-adjacent to V in phi?
            for (int x = 0; x < nodes.size(); x++) {
                if (x == v) {
                    continue;  //TEST
                }
                Node xnode = nodes.get(x);
                if (phi.isAdjacentTo(vnode, xnode)) {
                    local[v].add(xnode);
                }

                //or is there a collider between X and V in phi?
                for (int y = 0; y < nodes.size(); y++) {
                    if (y == v || y == x) {
                        continue; //TEST
                    }
                    Node ynode = nodes.get(y);
                    if (phi.getEndpoint(xnode, ynode) == Endpoint.ARROW &&
                            phi.getEndpoint(ynode, xnode) == Endpoint.TAIL &&
                            phi.getEndpoint(vnode, ynode) == Endpoint.ARROW &&
                            phi.getEndpoint(ynode, vnode) == Endpoint.TAIL) {
                        local[v].add(xnode);
                    }
                }
            } //End x loop
        } //End v loop--Local(phi, V) now exists

        int m = 1;

        //maxCountLocalMinusSep is the largest cardinality of all sets of the
        //form Loacl(phi,A)\(SepSet<A,C> union {B,C})
        while (maxCountLocalMinusSep(phi, sepset, local, underLineTriples) >= m) {
            //Select A
            for (int a = 0; a < nodes.size(); a++) {
                Node anode = nodes.get(a);

                //Then C distinct from A
                for (int c = 0; c < nodes.size(); c++) {
                    if (c == a) {
                        continue;
                    }
                    Node cnode = nodes.get(c);
                    if (phi.isAdjacentTo(anode, cnode)) {
                        continue;
                    }

                    //Then B distinct from A and C
                    for (int b = 0; b < nodes.size(); b++) {
                        if (b == c || b == a) {
                            continue;
                        }
                        if (supsepset[a][b][c] != null) {
                            continue; //supsepset of this triple
                        }
                        //not previously computed
                        Node bnode = nodes.get(b);

                        //Is <A,B,C> is underlined?  If so skip to next triple.
                        if (inTriplesList(anode, bnode, cnode,
                                underLineTriples)) {
                            continue;
                        }

                        //Is B a collider between A and C?  If not, next triple.
                        if (!isCollider(phi, anode, bnode, cnode)) {
                            continue;
                        }

                        //Compute the number of elements (count)
                        //in Local(phi,A)\(sepset<A,C> union {B,C})
                        Set<Node> localMinusSep = countLocalMinusSep(phi,
                                sepset, local, anode, bnode, cnode);
                        int count = localMinusSep.size();

                        if (count < m) {
                            continue; //If not >= m skip to next triple.
                        }

                        //Compute the set T (setT) with m elements which is a subset of
                        //Local(phi,A)\(sepset<A,C> union {B,C})
                        Object[] v = new Object[count];
                        for (int i = 0; i < count; i++) {
                            v[i] = (localMinusSep.toArray())[i];
                        }

                        ChoiceGenerator generator = new ChoiceGenerator(count, m);
                        int[] elts;

                        while ((elts = generator.next()) != null) {
                            List<Node> setT = new LinkedList<Node>();
                            for (int i = 0; i < m; i++) {
                                int j = elts[i];
                                setT.add((Node) v[j]);
                            }

                            setT.add(bnode);
                            for (Object o : sepset
                                    .get(nodes.get(a), nodes.get(c))) {
                                setT.add((Node) o);
                            }

                            //Note:  B is a collider between A and C (see above).
                            //If anode and cnode are d-separated given T union
                            //sep[a][c] union {bnode} create a dotted underline triple
                            //<A,B,C> and record T union sepset<A,C> union {B} in
                            //supsepset<A,B,C> and in supsepset<C,B,A>

                            boolean dsepACT;
                            try {
                                dsepACT = test.isIndependent(anode, cnode,
                                        setT);
                            }
                            catch (Exception e) {
                                dsepACT = false;
                            }

                            if (dsepACT) {
                                supsepset[a][b][c] = setT;
                                supsepset[c][b][a] = setT;

                                Triple ul = new Triple(anode, bnode, cnode);
                                dottedUnderLineTriples.add(ul);

                                TetradLogger.getInstance().details("Adding dotted underline: " + ul);
                            }
                        }
                    }
                }
            }

            m++;
        }

//        LogUtils.getInstance().fine("Underline triples:  ");
//        for (Triple underLineTriple2 : underLineTriples) {
//            LogUtils.getInstance().fine((underLineTriple2).toString());
//        }
//
//        LogUtils.getInstance().fine("Dotted underline triples:  ");
//        for (Triple dottedUnderLineTriple : dottedUnderLineTriples) {
//            LogUtils.getInstance().fine((dottedUnderLineTriple).toString());
//        }

        //Step E
        TetradLogger.getInstance().info("\nStep E");

        if (nodes.size() < 4) {
            System.out.println(phi);

            Graph pagPhi = new EdgeListGraph(phi);
            pagPhi.setUnderLineTriples(underLineTriples);
            pagPhi.setDottedUnderLineTriples(dottedUnderLineTriples);

            return pagPhi;  //Steps E and F require at least 4 vertices
        }

        //If there is a quadruple <A,B,C,D> of distinct vertices such that...
        for (int a = 0; a < nodes.size(); a++) {
            Node nodeA = nodes.get(a);

            for (int b = 0; b < nodes.size(); b++) {
                if (b == a) {
                    continue; //Distinct
                }
                Node nodeB = nodes.get(b);

                for (int c = 0; c < nodes.size(); c++) {
                    if (c == a || c == b) {
                        continue; //Distinct
                    }
                    Node nodeC = nodes.get(c);

                    //...A, B, and C are an dotted underline triple...
                    if (!inTriplesList(nodeA, nodeB, nodeC,
                            dottedUnderLineTriples)) {
                        continue;
                    }

                    for (int d = 0; d < nodes.size(); d++) {
                        if (d == a || d == b || d == c) {
                            continue; //Distinct
                        }
                        Node nodeD = nodes.get(d);

                        //...B is a collider in phi between A and C...
                        //...D is a collider betwen A and C in phi...
                        //...B and D are adjacent in phi...
                        //...A, D and C are not an underline triple...
                        if (isCollider(phi, nodeA, nodeB, nodeC) &&
                                inTriplesList(nodeA, nodeB, nodeC,
                                        dottedUnderLineTriples) &&
                                isCollider(phi, nodeA, nodeD, nodeC) &&
                                !inTriplesList(nodeA, nodeD, nodeC,
                                        underLineTriples) &&
                                phi.isAdjacentTo(nodeB, nodeD)) {

                            if (supsepset[a][b][c].contains(nodeD)) {

                                // Orient B*-oD as B*-D
                                phi.setEndpoint(nodeB, nodeD, Endpoint.TAIL);
                                TetradLogger.getInstance().edgeOriented("Orienting " + nodeB + "*--" + nodeD);
                            } else {
                                if (!isDirEdgeAllowed(nodeB, nodeD)) {
                                    continue;
                                }

                                if (phi.getEndpoint(nodeD, nodeB) == Endpoint.ARROW) {
                                    continue;
                                }

                                if (phi.getEndpoint(nodeB, nodeD) != Endpoint.CIRCLE) {
                                    continue;
                                }

                                // Or orient Bo-oD or B-oD as B->D...
                                TetradLogger.getInstance().edgeOriented("Orienting " + nodeB + "->" + nodeD);
                                phi.setEndpoint(nodeB, nodeD, Endpoint.ARROW);
                            }
                        }
                    }
                }
            }
        }

//        LogUtils.getInstance().fine("After Step E");
//        LogUtils.getInstance().fine(phi.toString());

//        LogUtils.getInstance().fine("Underline triples");
//        for (Triple underLineTriple3 : underLineTriples) {
//            LogUtils.getInstance().fine((underLineTriple3).toString());
//        }
//
//        LogUtils.getInstance().fine("Dotted underline triples");
//        for (Triple dottedUnderLineTriple1 : dottedUnderLineTriples) {
//            LogUtils.getInstance().fine((dottedUnderLineTriple1).toString());
//        }

        //Step F
        TetradLogger.getInstance().info("\nStep F");

        //For each quadruple <A,B,C,D> of distinct vertices...
        for (int a = 0; a < nodes.size(); a++) {
            Node nodeA = nodes.get(a);

            for (int b = 0; b < nodes.size(); b++) {
                if (b == a) {
                    continue;  //Distinct
                }
                Node nodeB = nodes.get(b);

                for (int c = 0; c < nodes.size(); c++) {
                    if (c == a || c == b) {
                        continue;  //Distinct
                    }
                    Node nodeC = nodes.get(c);

                    //...if A, B, C aren't a dotted underline triple get next triple...
                    if (!inTriplesList(nodeA, nodeB, nodeC,
                            dottedUnderLineTriples)) {
                        continue;
                    }
                    for (int d = 0; d < nodes.size(); d++) {
                        if (d == a || d == b || d == c) {
                            continue;  //Distinct
                        }
                        Node nodeD = nodes.get(d);

                        //...and D is not adjacent to both A and C in phi...
                        if (phi.isAdjacentTo(nodeA, nodeD) &&
                                phi.isAdjacentTo(nodeC, nodeD)) {
                            continue;
                        }
                        //...and B and D are adjacent...
                        if (!phi.isAdjacentTo(nodeB, nodeD)) {
                            continue;  //D was C
                        }

                        //Construct supSepUnionD = SupSepset<A, B, C> union {D}
                        List<Node> supSepUnionD = new LinkedList<Node>();
                        supSepUnionD.add(nodeD);
                        for (Node node : (Iterable<Node>) supsepset[a][b][c]) {
                            supSepUnionD.add(node);
                        }

                        //If A and C are a pair of vertices d-connected given
                        //SupSepset<A,B,C> union {D} then orient Bo-oD or B-oD
                        //as B->D in phi.

                        boolean dsepACD;
                        try {
                            dsepACD = test.isIndependent(nodeA, nodeC,
                                    supSepUnionD);
                        }
                        catch (Exception e) {
                            dsepACD = false;
                        }

                        if (!dsepACD) {
                            if (!isDirEdgeAllowed(nodeB, nodeD)) {
                                continue;
                            }

                            phi.setEndpoint(nodeB, nodeD, Endpoint.ARROW);
                            phi.setEndpoint(nodeD, nodeB, Endpoint.TAIL);
                            TetradLogger.getInstance().edgeOriented("Orienting " + nodeB + "->" + nodeD);
                        }
                    }
                }
            }
        }

//        LogUtils.getInstance().fine("After Step F");
//        LogUtils.getInstance().fine(phi.toString());

        TetradLogger.getInstance().log("graph", "\nFinal Graph:");
        TetradLogger.getInstance().log("graph", phi.toString());

        TetradLogger.getInstance().log("triples", "\nUnderline triples:");

        for (Triple underLineTriple4 : underLineTriples) {
            TetradLogger.getInstance().log("triples", underLineTriple4.toString());
        }

        TetradLogger.getInstance().log("triples", "\nDotted underline triples:");

        for (Triple dottedUnderLineTriple2 : dottedUnderLineTriples) {
            TetradLogger.getInstance().log("triples", dottedUnderLineTriple2.toString());
        }

        //return phi;

        Graph pagPhi = new EdgeListGraph(phi);
        pagPhi.setUnderLineTriples(underLineTriples);
        pagPhi.setDottedUnderLineTriples(dottedUnderLineTriples);

        return pagPhi;
    }

    /**
     * Helper Method: Checks if a directed edge is allowed by background
     * knowledge.
     */
    private boolean isDirEdgeAllowed(Node from, Node to) {
        return !knowledge.edgeRequired(to.getName(), from.getName()) &&
                !knowledge.edgeForbidden(from.getName(), to.getName());
    }

    /**
     * Returns the list of underline triples for the PAG computed by the search
     * method.
     */
    public Set<Triple> getUnderLineTriples() {
        return underLineTriples;
    }

    /**
     * Returns the list of dotted underline triples for the PAG computed by the
     * search method.
     */
    public Set<Triple> getDottedUnderLineTriples() {
        return dottedUnderLineTriples;
    }

    /**
     * Returns a boolean which indicates whether a given ordered triple,
     * represented by a, b and c are in the list of triples specified by the
     * third argument.
     */
    private static boolean inTriplesList(Node a, Node b, Node c,
                                         Set<Triple> triples) {
        boolean e1 = false;

        for (Triple ul : triples) {
            if (a == ul.getX() && b == ul.getY() && c == ul.getZ()) {
                e1 = true;
                break;
            }
        }

        return e1;
    }

    /**
     * Returns true if b is a collider between a and c in the GaSearchGraph phi;
     * returns false otherwise.
     */
    private static boolean isCollider(Graph phi, Node a, Node b, Node c) {
        return !(phi.getEndpoint(a, b) != Endpoint.ARROW ||
                phi.getEndpoint(b, a) != Endpoint.TAIL ||
                phi.getEndpoint(c, b) != Endpoint.ARROW ||
                phi.getEndpoint(b, c) != Endpoint.TAIL);

    }

    /**
     * For a given GaSearchGraph phi and for a given set of sepsets, each of
     * which is associated with a pair of vertices A and C, computes and returns
     * the set Local(phi,A)\(SepSet<A,C> union {B,C}).
     */
    private static Set<Node> countLocalMinusSep(Graph phi, SepsetMap sepset,
                                                List<Node>[] loc, Node anode,
                                                Node bnode, Node cnode) {
        List<Node> nodes = phi.getNodes();
        int a = nodes.indexOf(anode);
        Set<Node> localMinusSep = new HashSet<Node>();

        if (loc[a] != null) {
            for (Node node : loc[a]) {
                localMinusSep.add(node);
            }
        }

        if (sepset.get(anode, cnode) != null) {
            for (Node node : sepset.get(anode, cnode)) {
                localMinusSep.remove(node);
            }
        }

        localMinusSep.add(bnode);
        localMinusSep.add(cnode);

        return localMinusSep;
    }

    /**
     * Computes and returns the size (cardinality) of the largest set of the
     * form Local(phi,A)\(SepSet<A,C> union {B,C}) where B is a collider between
     * A and C and where A and C are not adjacent.  A, B and C should not be a
     * dotted underline triple.
     */
    private static int maxCountLocalMinusSep(Graph phi, SepsetMap sep,
                                             List<Node>[] loc,
                                             Set<Triple> triples) {
        List<Node> nodes = phi.getNodes();
        int num = nodes.size();

        int maxCount = -1;

        for (int a = 0; a < num; a++) {
            Node anode = nodes.get(a);

            for (int c = 0; c < num; c++) {
                if (c == a) {
                    continue;
                }
                Node cnode = nodes.get(c);
                if (phi.isAdjacentTo(anode, cnode)) {
                    continue;
                }

                for (int b = 0; b < num; b++) {
                    if (b == a || b == c) {
                        continue;
                    }
                    Node bnode = nodes.get(b);
                    //Want B to be a collider between A and C but not for
                    //A, B, and C to be an underline triple.
                    if (inTriplesList(anode, bnode, cnode, triples)) {
                        continue;
                    }
                    //Is B a collider between A and C?
                    if (!isCollider(phi, anode, bnode, cnode)) {
                        continue;
                    }

                    //int count =
                    //        countLocalMinusSep(phi, sep, loc, anode, bnode, cnode);
                    //
                    Set<Node> localMinusSep = countLocalMinusSep(phi, sep, loc,
                            anode, bnode, cnode);
                    int count = localMinusSep.size();

                    if (count > maxCount) {
                        maxCount = count;
                    }
                }
            }
        }

        return maxCount;
    }

    private boolean searchAtDepth(Graph graph, IndependenceTest test,
                                  Knowledge knowledge, SepsetMap sepset,
                                  int depth) {
        boolean more = false;
        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        for (Node x : nodes) {
            List<Node> b = new LinkedList<Node>(graph.getAdjacentNodes(x));

            nextEdge:
            for (Node y : b) {

                // This is the standard algorithm, without the v1 bias.
                List<Node> adjx = graph.getAdjacentNodes(x);
                adjx.remove(y);
                List<Node> ppx = possibleParents(x, y, adjx, knowledge);

                System.out.println("Possible parents for removing " + x +
                        " --> " + y + " are " + ppx);

                boolean noEdgeRequired =
                        knowledge.noEdgeRequired(x.getName(), y.getName());

                if (ppx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(ppx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet =
                                SearchGraphUtils.asList(choice, ppx);
                        boolean independent = test.isIndependent(x, y, condSet);

                        if (independent && noEdgeRequired) {
                            graph.removeEdge(x, y);
                            sepset.set(x, y, condSet);
                            continue nextEdge;
                        }
                    }
                }
            }

            if (graph.getAdjacentNodes(x).size() - 1 > depth) {
                more = true;
            }
        }

        return more;
    }

    private List<Node> possibleParents(Node x, Node y, List<Node> nodes,
                                       Knowledge knowledge) {
        List<Node> possibleParents = new LinkedList<Node>();
        String _x = x.getName();
        String _y = y.getName();

        for (Node z : nodes) {
            String _z = z.getName();

            if (possibleParentOf(_z, _x, _y, knowledge)) {
                possibleParents.add(z);
            }
        }

        return possibleParents;
    }

    /**
     * Returns true just in case z is a possible parent of both x and y, in the
     * sense that edges are not forbidden from z to either x or y, and edges are
     * not required from either x or y to z, according to background knowledge.
     */
    private static boolean possibleParentOf(String z, String x, String y,
                                            Knowledge knowledge) {
        if (knowledge.edgeForbidden(z, x)) {
            return false;
        }

        if (knowledge.edgeForbidden(z, y)) {
            return false;
        }

        if (knowledge.edgeRequired(x, z)) {
            return false;
        }

        return !knowledge.edgeRequired(y, z);
    }


    public Knowledge getKnowledge() {
        return knowledge;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}



