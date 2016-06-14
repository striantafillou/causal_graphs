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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.Bpc;
import edu.cmu.tetrad.search.MimUtils;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the
 * BuildPureClusters algorithm.
 *
 * @author Ricardo Silva
 */
public class BuildPureClustersRunner extends AbstractMimRunner
        implements GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper.
     */

    public BuildPureClustersRunner(DataWrapper dataWrapper,
            BuildPureClustersParams pureClustersParams) {
        super(dataWrapper, pureClustersParams);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BuildPureClustersRunner serializableInstance() {
        return new BuildPureClustersRunner(DataWrapper.serializableInstance(),
                BuildPureClustersParams.serializableInstance());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    public void execute() {
        Bpc bpc;
        Object source = getData();

        if (source instanceof CovarianceMatrix) {
            bpc = new Bpc((CovarianceMatrix) source,
                    getParams().getAlpha(), getParams().getTetradTestType(),
                    getParams().getPurifyTestType());
        }
        else if (source instanceof DataSet) {
            bpc = new Bpc(
                    (DataSet) source, getParams().getAlpha(),
                    getParams().getTetradTestType(),
                    getParams().getPurifyTestType());
        }
        else {
            bpc = new Bpc(
                    (DataSet) source, getParams().getAlpha(),
                    getParams().getTetradTestType(),
                    getParams().getPurifyTestType());
        }

        Graph searchGraph = bpc.search();

        if (searchGraph == null) {

            // The whole lotta hoopla below had to be done so that a message
            // could be displayed to the user without blocking the thread.
            // Please don't change this back to a JOptionPane, even if you
            // really want to. jdramsey 8/15/2005
            JFrame jFrame = (JFrame) SwingUtilities.getAncestorOfClass(
                    JFrame.class, JOptionUtils.centeringComp());
            final JDialog dialog = new JDialog(jFrame);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            panel.add(new JLabel("No model found."), BorderLayout.CENTER);

            JButton button = new JButton("OK");

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            panel.add(button, BorderLayout.SOUTH);
            dialog.getContentPane().add(panel, BorderLayout.CENTER);

            dialog.setLocationRelativeTo(JOptionUtils.centeringComp());
            dialog.pack();
            dialog.setVisible(true);

            setResultGraph(new EdgeListGraph());
            setClusters(new Clusters());
            return;
        }

        setResultGraph(searchGraph);
        GraphUtils.arrangeClustersInLine(getResultGraph(), true);

        setClusters(MimUtils.convertToClusters(searchGraph));
    }

    public Graph getGraph() {
        return getResultGraph();
    }
}


