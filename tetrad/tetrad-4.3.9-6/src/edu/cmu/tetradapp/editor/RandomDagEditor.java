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

package edu.cmu.tetradapp.editor;

import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Edits the parameters for generating random graphs.
 *
 * @author Joseph Ramsey
 */
public class RandomDagEditor extends JPanel {
    IntTextField numNodesField = new IntTextField(getNumNodes(), 4);
    IntTextField numLatentsField = new IntTextField(getNumLatents(), 4);
    IntTextField maxEdgesField = new IntTextField(getMaxEdges(), 4);
    IntTextField maxIndegreeField = new IntTextField(getMaxIndegree(), 4);
    IntTextField maxOutdegreeField = new IntTextField(getMaxOutdegree(), 4);
    IntTextField maxDegreeField = new IntTextField(getMaxDegree(), 4);
    JRadioButton chooseUniform = new JRadioButton("Draw uniformly from all such DAGs");
    JRadioButton chooseFixed = new JRadioButton("Guarantee maximum number of edges");
    JComboBox connectedBox = new JComboBox(new String[]{"No", "Yes"});


    /**
     * Constructs a dialog to edit the given workbench randomization
     * parameters.
     */
    public RandomDagEditor() {
        ButtonGroup group = new ButtonGroup();
        group.add(chooseUniform);
        group.add(chooseFixed);
        chooseUniform.setSelected(isUniformlySelected());
        chooseFixed.setSelected(!isUniformlySelected());

        // set up text and ties them to the parameters object being edited.
        numNodesField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == numNodesField.getValue()) {
                    return oldValue;
                }

                try {
                    setNumNodes(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                maxEdgesField.setValue(getMaxEdges());
                return value;
            }
        });

        numLatentsField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == numLatentsField.getValue()) {
                    return oldValue;
                }

                try {
                    setNumLatents(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                maxEdgesField.setValue(getMaxEdges());
                return value;
            }
        });

        maxEdgesField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == maxEdgesField.getValue()) {
                    return oldValue;
                }

                try {
                    setMaxEdges(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                return value;
            }
        });

        maxIndegreeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == maxIndegreeField.getValue()) {
                    return oldValue;
                }

                try {
                    setMaxIndegree(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                maxOutdegreeField.setValue(getMaxOutdegree());
                return value;
            }
        });

        maxOutdegreeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == maxOutdegreeField.getValue()) {
                    return oldValue;
                }

                try {
                    setMaxOutdegree(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                maxIndegreeField.setValue(getMaxIndegree());
                maxDegreeField.setValue(getMaxDegree());
                return value;
            }
        });

        maxDegreeField.setFilter(new IntTextField.Filter() {
            public int filter(int value, int oldValue) {
                if (value == maxDegreeField.getValue()) {
                    return oldValue;
                }

                try {
                    setMaxDegree(value);
                }
                catch (Exception e) {
                    // Ignore.
                }

                maxIndegreeField.setValue(getMaxIndegree());
                maxOutdegreeField.setValue(getMaxOutdegree());
                return value;
            }
        });

        if (isConnected()) {
            connectedBox.setSelectedItem("Yes");
        } else {
            connectedBox.setSelectedItem("No");
        }

        if (isUniformlySelected()) {
            maxIndegreeField.setEnabled(true);
            maxOutdegreeField.setEnabled(true);
            maxDegreeField.setEnabled(true);
            connectedBox.setEnabled(true);
        } else {
            maxIndegreeField.setEnabled(false);
            maxOutdegreeField.setEnabled(false);
            maxDegreeField.setEnabled(false);
            connectedBox.setEnabled(false);
        }

        connectedBox.setMaximumSize(connectedBox.getPreferredSize());
        connectedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                if ("Yes".equals(box.getSelectedItem())) {
                    setConnected(true);
                } else if ("No".equals(box.getSelectedItem())) {
                    setConnected(false);
                } else {
                    throw new IllegalArgumentException();
                }

                maxIndegreeField.setValue(getMaxIndegree());
                maxOutdegreeField.setValue(getMaxOutdegree());
                maxDegreeField.setValue(getMaxDegree());
                maxEdgesField.setValue(getMaxEdges());
            }
        });

        chooseUniform.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton button = (JRadioButton) e.getSource();
                button.setSelected(true);
                setUniformlySelected(true);

                maxIndegreeField.setEnabled(true);
                maxOutdegreeField.setEnabled(true);
                maxDegreeField.setEnabled(true);
                connectedBox.setEnabled(true);
            }
        });

        chooseFixed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton button = (JRadioButton) e.getSource();
                button.setSelected(true);
                setUniformlySelected(false);

                maxIndegreeField.setEnabled(false);
                maxOutdegreeField.setEnabled(false);
                maxDegreeField.setEnabled(false);
                connectedBox.setEnabled(false);
            }
        });

        // construct the workbench.
        setLayout(new BorderLayout());

        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Parameters for Random DAG:"));
        b2.add(Box.createHorizontalGlue());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        Box b10 = Box.createHorizontalBox();
        b10.add(new JLabel("Number of nodes:"));
        b10.add(Box.createRigidArea(new Dimension(10, 0)));
        b10.add(Box.createHorizontalGlue());
        b10.add(numNodesField);
        b1.add(b10);

        Box b11 = Box.createHorizontalBox();
        b11.add(new JLabel("Number of latent nodes:"));
        b11.add(Box.createHorizontalStrut(25));
        b11.add(Box.createHorizontalGlue());
        b11.add(numLatentsField);
        b1.add(b11);
        b1.add(Box.createVerticalStrut(5));

        Box b12 = Box.createHorizontalBox();
        b12.add(new JLabel("Maximum number of edges:"));
        b12.add(Box.createHorizontalGlue());
        b12.add(maxEdgesField);
        b1.add(b12);
        b1.add(Box.createVerticalStrut(5));

        Box b14 = Box.createHorizontalBox();
        b14.add(new JLabel("Maximum indegree:"));
        b14.add(Box.createHorizontalGlue());
        b14.add(maxIndegreeField);
        b1.add(b14);

        Box b15 = Box.createHorizontalBox();
        b15.add(new JLabel("Maximum outdegree:"));
        b15.add(Box.createHorizontalGlue());
        b15.add(maxOutdegreeField);
        b1.add(b15);

        Box b13 = Box.createHorizontalBox();
        b13.add(new JLabel("Maximum degree:"));
        b13.add(Box.createHorizontalGlue());
        b13.add(maxDegreeField);
        b1.add(b13);
        b1.add(Box.createVerticalStrut(5));

        Box b16 = Box.createHorizontalBox();
        b16.add(new JLabel("Connected:"));
        b16.add(Box.createHorizontalGlue());
        b16.add(connectedBox);
        b1.add(b16);
        b1.add(Box.createVerticalStrut(5));

        Box b17 = Box.createHorizontalBox();
        b17.add(chooseUniform);
        b17.add(Box.createHorizontalGlue());
        b1.add(b17);

        Box b18 = Box.createHorizontalBox();
        b18.add(chooseFixed);
        b18.add(Box.createHorizontalGlue());
        b1.add(b18);

        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!isUniformlySelected() && enabled) {
            numNodesField.setEnabled(enabled);
            numLatentsField.setEnabled(enabled);
            maxEdgesField.setEnabled(enabled);
            maxIndegreeField.setEnabled(false);
            maxOutdegreeField.setEnabled(false);
            maxDegreeField.setEnabled(false);
            connectedBox.setEnabled(false);
            chooseUniform.setEnabled(enabled);
            chooseFixed.setEnabled(enabled);
        } else {
            numNodesField.setEnabled(enabled);
            numLatentsField.setEnabled(enabled);
            maxEdgesField.setEnabled(enabled);
            maxIndegreeField.setEnabled(enabled);
            maxOutdegreeField.setEnabled(enabled);
            maxDegreeField.setEnabled(enabled);
            connectedBox.setEnabled(enabled);
            chooseUniform.setEnabled(enabled);
            chooseFixed.setEnabled(enabled);
        }
    }

    public boolean isUniformlySelected() {
        return Preferences.userRoot().getBoolean("graphUniformlySelected", true);
    }

    public void setUniformlySelected(boolean uniformlySelected) {
        Preferences.userRoot().putBoolean("graphUniformlySelected", uniformlySelected);
    }

    public int getNumNodes() {
        return Preferences.userRoot().getInt("newGraphNumNodes", 5);
    }

    private void setNumNodes(int numNodes) {
        if (numNodes < 2) {
            throw new IllegalArgumentException("Number of nodes must be >= 2.");
        }

        if (numNodes < getNumLatents()) {
            throw new IllegalArgumentException(
                    "Number of nodes must be >= number " + "of latent nodes.");
        }

        Preferences.userRoot().putInt("newGraphNumNodes", numNodes);

        if (isConnected()) {
            setMaxEdges(numNodes);
        }
    }

    public int getNumLatents() {
        return Preferences.userRoot().getInt("newGraphNumLatents", 0);
    }

    private void setNumLatents(int numLatentNodes) {
        if (numLatentNodes < 0) {
            throw new IllegalArgumentException(
                    "Number of latent nodes must be" + " >= 0: " +
                            numLatentNodes);
        }

        if (numLatentNodes > getNumNodes()) {
            throw new IllegalArgumentException(
                    "Number of latent nodes must be " + "<= number of nodes.");
        }

        Preferences.userRoot().putInt("newGraphNumLatents", numLatentNodes);
    }

    public int getMaxEdges() {
        return Preferences.userRoot().getInt("newGraphNumEdges", 3);
    }


    private void setMaxEdges(int numEdges) {
        if (isConnected() && numEdges < getNumNodes()) {
            throw new IllegalArgumentException("When assuming connectedness, " +
                    "the number of edges must be at least the number of nodes.");
        }

        if (!isConnected() && numEdges < 0) {
            throw new IllegalArgumentException(
                    "Number of edges must be >= 0: " + numEdges);
        }

        int maxNumEdges = getNumNodes() * (getNumNodes() - 1) / 2;

        if (numEdges > maxNumEdges) {
            numEdges = maxNumEdges;
        }

        Preferences.userRoot().putInt("newGraphNumEdges", numEdges);
    }

    public int getMaxDegree() {
        return Preferences.userRoot().getInt("randomGraphMaxDegree", 6);
    }

    private void setMaxDegree(int maxDegree) {
        if (!isConnected() && maxDegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxDegree", 1);
            return;
        }

        if (isConnected() && maxDegree < 3) {
            Preferences.userRoot().putInt("randomGraphMaxDegree", 3);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxDegree", maxDegree);
    }

    public int getMaxIndegree() {
        return Preferences.userRoot().getInt("randomGraphMaxIndegree", 3);
    }

    private void setMaxIndegree(int maxIndegree) {
        if (!isConnected() && maxIndegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxIndegree", 1);
            return;
        }

        if (isConnected() && maxIndegree < 2) {
            Preferences.userRoot().putInt("randomGraphMaxIndegree", 2);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxIndegree", maxIndegree);
    }

    public int getMaxOutdegree() {
        return Preferences.userRoot().getInt("randomGraphMaxOutdegree", 3);
    }

    private void setMaxOutdegree(int maxOutDegree) {
        if (!isConnected() && maxOutDegree < 1) {
            Preferences.userRoot().putInt("randomGraphMaxOutdegree", 1);
            return;
        }

        if (isConnected() && maxOutDegree < 2) {
            Preferences.userRoot().putInt("randomGraphMaxOutdegree", 2);
            return;
        }

        Preferences.userRoot().putInt("randomGraphMaxOutdegree", maxOutDegree);
    }

    private void setConnected(boolean connected) {
        Preferences.userRoot().putBoolean("randomGraphConnected", connected);

        if (connected) {
            if (getMaxIndegree() < 2) {
                setMaxIndegree(2);
            }

            if (getMaxOutdegree() < 2) {
                setMaxOutdegree(2);
            }

            if (getMaxDegree() < 3) {
                setMaxDegree(3);
            }

            if (getMaxEdges() < getNumNodes()) {
                setMaxEdges(getNumNodes());
            }
        }
    }

    public boolean isConnected() {
        return Preferences.userRoot().getBoolean("randomGraphConnected", false);
    }

}


