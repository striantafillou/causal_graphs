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

package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.SearchGraphUtils;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Builds a menu for layout operations on graphs. Interacts with classes that
 * implement the LayoutEditable interface.
 *
 * @author Joseph Ramsey
 */
public class LayoutMenu extends JMenu {
    private LayoutEditable layoutEditable;
    private CopyLayoutAction copyLayoutAction;

    public LayoutMenu(LayoutEditable layoutEditable) {
        super("Layout");
        this.layoutEditable = layoutEditable;

        JMenuItem circleLayout = new JMenuItem("Circle");
        add(circleLayout);

        circleLayout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                for (Node node : new ArrayList<Node>(graph.getNodes())) {
                    if (node.getNodeType() == NodeType.ERROR) {
                        graph.removeNode(node);
                    }
                }

                Rectangle r = getLayoutEditable().getVisibleRect();

                int m = Math.min(r.width, r.height) / 2;
                int radius = m - 50;
                int centerx = r.x + m;
                int centery = r.y + m;

//                GraphUtils.arrangeInCircle(graph, 200, 200, 150);
                GraphUtils.arrangeInCircle(graph, centerx, centery, radius);
                getLayoutEditable().layoutByGraph(graph);

                // Copy the laid out graph to the clipboard.
                getCopyLayoutAction().actionPerformed(null);
            }
        });

        if (getLayoutEditable().getKnowledge() != null) {
            JMenuItem knowledgeTiersLayout = new JMenuItem("Knowledge Tiers");
            add(knowledgeTiersLayout);

            knowledgeTiersLayout.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                        for (Node node : new ArrayList<Node>(graph.getNodes())) {
                            if (node.getNodeType() == NodeType.ERROR) {
                                graph.removeNode(node);
                            }
                        }

                        Knowledge knowledge = getLayoutEditable().getKnowledge();
                        SearchGraphUtils.arrangeByKnowledgeTiers(graph, knowledge);
                        getLayoutEditable().layoutByGraph(graph);

                        // Copy the laid out graph to the clipboard.
                        getCopyLayoutAction().actionPerformed(null);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                                e1.getMessage());
                    }
                }
            });
        }

        if (getLayoutEditable().getSourceGraph() != null) {
            JMenuItem lastResultLayout = new JMenuItem("Source Graph");
            add(lastResultLayout);

            lastResultLayout.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                    for (Node node : new ArrayList<Node>(graph.getNodes())) {
                        if (node.getNodeType() == NodeType.ERROR) {
                            graph.removeNode(node);
                        }
                    }

                    Graph sourceGraph = getLayoutEditable().getSourceGraph();
                    GraphUtils.arrangeBySourceGraph(graph, sourceGraph);
                    getLayoutEditable().layoutByGraph(graph);

                    // Copy the laid out graph to the clipboard.
                    getCopyLayoutAction().actionPerformed(null);
                }
            });
        }

        JMenuItem layeredDrawing = new JMenuItem("Layered Drawing");
        add(layeredDrawing);

        layeredDrawing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                for (Node node : new ArrayList<Node>(graph.getNodes())) {
                    if (node.getNodeType() == NodeType.ERROR) {
                        graph.removeNode(node);
                    }
                }

                GraphUtils.hierarchicalLayout(graph);
                getLayoutEditable().layoutByGraph(graph);

                // Copy the laid out graph to the clipboard.
                getCopyLayoutAction().actionPerformed(null);
            }
        });

//        JMenu forceDirectedDrawing = new JMenu("Force Directed Drawing");
//        add(forceDirectedDrawing);
//
        JMenuItem fruchtermanReingold = new JMenuItem("Fruchterman-Reingold");
        add(fruchtermanReingold);

        fruchtermanReingold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                for (Node node : new ArrayList<Node>(graph.getNodes())) {
                    if (node.getNodeType() == NodeType.ERROR) {
                        graph.removeNode(node);
                    }
                }

                GraphUtils.fruchtermanReingoldLayout(graph);
                getLayoutEditable().layoutByGraph(graph);

                // Copy the laid out graph to the clipboard.
                getCopyLayoutAction().actionPerformed(null);
            }
        });

        JMenuItem kamadaKawai = new JMenuItem("Kamada-Kawai");
        add(kamadaKawai);

        kamadaKawai.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        Graph graph = new EdgeListGraph(getLayoutEditable().getGraph());

                        for (Node node : new ArrayList<Node>(graph.getNodes())) {
                            if (node.getNodeType() == NodeType.ERROR) {
                                graph.removeNode(node);
                            }
                        }

                        GraphEditorUtils.editkamadaKawaiLayoutParams();

                        boolean initializeRandomly = Preferences.userRoot()
                                .getBoolean(
                                        "kamadaKawaiLayoutInitializeRandomly",
                                        false);
                        double naturalEdgeLength = Preferences.userRoot()
                                .getDouble("kamadaKawaiLayoutNaturalEdgeLength",
                                        80.0);
                        double springConstant = Preferences.userRoot()
                                .getDouble("kamadaKawaiLayoutSpringConstant",
                                        0.2);
                        double stopEnergy = Preferences.userRoot().getDouble(
                                "kamadaKawaiLayoutStopEnergy", 1.0);

                        GraphUtils.kamadaKawaiLayout(graph, initializeRandomly,
                                naturalEdgeLength, springConstant, stopEnergy);
                        getLayoutEditable().layoutByGraph(graph);

                        // Copy the laid out graph to the clipboard.
                        getCopyLayoutAction().actionPerformed(null);
                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        addSeparator();

        copyLayoutAction = new CopyLayoutAction(getLayoutEditable());
        add(getCopyLayoutAction());
        add(new PasteLayoutAction(getLayoutEditable()));
    }

    private LayoutEditable getLayoutEditable() {
        return layoutEditable;
    }

    private CopyLayoutAction getCopyLayoutAction() {
        return copyLayoutAction;
    }
}


