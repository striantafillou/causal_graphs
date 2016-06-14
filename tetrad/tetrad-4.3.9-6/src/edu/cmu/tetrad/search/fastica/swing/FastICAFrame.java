package edu.cmu.tetrad.search.fastica.swing;

import edu.cmu.tetrad.search.fastica.*;
import edu.cmu.tetrad.search.fastica.math.Matrix;
import edu.cmu.tetrad.search.fastica.util.AudioVector;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;

/**
 * The <code>FastICAFrame</code> is the main frame of the FastIca Swing
 * Application.
 *
 * @author Michael Lambertz
 */

public class FastICAFrame extends JFrame {

    private static final long serialVersionUID = 3257285816446694454L;

    private JButton addBt;
    private JButton deleteBt;
    private JButton playBt;
    private JButton mixBt;
    private JButton analyseBt;

    private DefaultMutableTreeNode dataRootNd;
    private JTree dataTr;

    private LinkedList<LinkedList<double[]>> audioVectors;
    private float audioSampleRate = 44100;

    private JProgressBar statusPB;


    public FastICAFrame() {
        audioVectors = new LinkedList<LinkedList<double[]>>();

        setMinimumSize(new Dimension(400, 300));
        setSize(new Dimension(400, 300));
        setTitle("FastIca for JAVA");
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        JPanel mainPn = new JPanel(new BorderLayout(4, 4));
        setContentPane(mainPn);

        dataRootNd = new DefaultMutableTreeNode("root");

        dataTr = new JTree(dataRootNd);
        dataTr.setRootVisible(false);
        dataTr.setEditable(false);
        dataTr.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        JScrollPane dataSP = new JScrollPane(dataTr);
        mainPn.add(dataSP, BorderLayout.CENTER);

        JPanel buttonPn = new JPanel();
        mainPn.add(buttonPn, BorderLayout.NORTH);

        addBt = new JButton("  add  ");
        addBt.setToolTipText("Adds a signal.");
        addBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionAdd();
                    }
                });
        buttonPn.add(addBt);

        deleteBt = new JButton(" delete ");
        deleteBt.setToolTipText("Deletes the selected signal.");
        deleteBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionDelete();
                    }
                });
        buttonPn.add(deleteBt);

        playBt = new JButton("  play  ");
        playBt.setToolTipText("Plays the selected signal.");
        playBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionPlay();
                    }
                });
        buttonPn.add(playBt);

        mixBt = new JButton("  mix  ");
        mixBt.setToolTipText("Mixes the signals.");
        mixBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionMix();
                    }
                });
        buttonPn.add(mixBt);

        analyseBt = new JButton(" analyse ");
        analyseBt.setToolTipText("Starts to analyse the signals.");
        analyseBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionAnalyse();
                    }
                });
        buttonPn.add(analyseBt);

        statusPB = new JProgressBar(0, 1024);
        statusPB.setBorderPainted(false);
        statusPB.setStringPainted(true);
        mainPn.add(statusPB, BorderLayout.SOUTH);
        setProgress(0, "Ready.");

        pack();
    }

    private DefaultMutableTreeNode getSelectedNode() {
        TreePath
                selectionPath = dataTr.getSelectionPath();
        if (selectionPath == null)
            return (null);
        DefaultMutableTreeNode
                selectedNd = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        if (!dataRootNd.isNodeDescendant(selectedNd))
            return (null);
        return (selectedNd);
    }

    private int[] getNodeIndexList(
            DefaultMutableTreeNode node) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        while (!node.isRoot()) {
            list.addFirst(new Integer(node.getParent().getIndex(node)));
            node = (DefaultMutableTreeNode) node.getParent();
        }
        int n = list.size();
        int[] nodeList = new int[n];
        for (int i = 0; i < n; ++i)
            nodeList[i] = list.get(i).intValue();
        return (nodeList);
    }

    private void actionAdd() {
        DefaultMutableTreeNode selNd = getSelectedNode();
        if (selNd == null) {
            audioVectors.add(new LinkedList<double[]>());
            DefaultMutableTreeNode newNd = new DefaultMutableTreeNode("Collection");
            dataRootNd.add(newNd);
            dataTr.scrollPathToVisible(new TreePath(newNd.getPath()));
            dataTr.updateUI();
        }
        else {
            int[] selIdxs = getNodeIndexList(selNd);
            if (selIdxs.length == 1) {
                JFileChooser fileDl = new JFileChooser(FastICAApp.getPreference(FastICAApp.PREF_DIR_ADD));
                if (fileDl.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File selFile = fileDl.getSelectedFile();
                        FastICAApp.setPreference(FastICAApp.PREF_DIR_ADD, selFile.getParentFile().getAbsolutePath());
                        double[] audioVector = AudioVector.readAudioFile(selFile, audioSampleRate);
                        audioVectors.get(selIdxs[0]).add(audioVector);
                        DefaultMutableTreeNode newNd = new DefaultMutableTreeNode(selFile.getName());
                        selNd.add(newNd);
                        dataTr.scrollPathToVisible(new TreePath(newNd.getPath()));
                        dataTr.updateUI();
                    }
                    catch (Exception exc) {
                        FastICAApp.exceptionDialog(exc);
                    }
                }
            }
        }
    }

    private void actionDelete() {
        DefaultMutableTreeNode
                selNd = getSelectedNode();
        if (selNd != null) {
            int[] selIdxs = getNodeIndexList(selNd);
            if (selIdxs.length == 1) {
                audioVectors.remove(selIdxs[0]);
                dataRootNd.remove(selIdxs[0]);
                dataTr.updateUI();
            }
            else {
                ((DefaultMutableTreeNode) dataRootNd.getChildAt(selIdxs[0])).remove(selIdxs[1]);
                audioVectors.get(selIdxs[0]).remove(selIdxs[1]);
                dataTr.updateUI();
            }
        }
    }

    private void actionPlay() {
        DefaultMutableTreeNode
                selNd = getSelectedNode();
        if (selNd != null) {
            int[] selIdxs = getNodeIndexList(selNd);
            if (selIdxs.length == 2) {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(
                            AudioVector.toAudioInputStream(
                                    audioVectors.get(selIdxs[0]).get(selIdxs[1]), audioSampleRate));
                    clip.start();
                    clip.drain();
                    clip.stop();
                    clip.flush();
                    clip.close();
                }
                catch (Exception exc) {
                    FastICAApp.exceptionDialog(exc);
                }
            }
        }
    }

    private void actionMix() {
        DefaultMutableTreeNode
                selNd = getSelectedNode();
        if (selNd != null) {
            int[] selIdxs = getNodeIndexList(selNd);
            if (selIdxs.length == 1) {
                LinkedList<double[]> audioList = audioVectors.get(selIdxs[0]);
                int m = audioList.size();
                if (m > 0) {
                    NumberDialog numberDl =
                            new NumberDialog(this, "Number of output signals...", 1, 1, 32);
                    if (numberDl.open()) {
                        int n = numberDl.getNumber();
                        MatrixDialog matrixDl =
                                new MatrixDialog(this, "Choose the mixing matrix...", Matrix.newMatrix(m, n, 0.0));
                        if (matrixDl.open()) {
                            // compute the new signals
                            double[][] mixingMatrix = Matrix.transpose(matrixDl.getMatrix());
                            double[][] audioMatrix = AudioVector.toMatrix(audioList.toArray(new double[1][]));
                            double[][] resAudioMatrix = Matrix.mult(mixingMatrix, audioMatrix);
                            saveAudioVectors(resAudioMatrix, "Mixed Collection");
                        }
                    }
                }
            }
        }
    }

    private void actionAnalyse() {
        DefaultMutableTreeNode
                selNd = getSelectedNode();
        if (selNd != null) {
            int[] selIdxs = getNodeIndexList(selNd);
            if (selIdxs.length == 1) {
                LinkedList<double[]> audioList = audioVectors.get(selIdxs[0]);
                int m = audioList.size();
                if (m > 0) {
                    NumberDialog numberDl =
                            new NumberDialog(this, "Number of independent components...", 1, 1, 32);
                    if (numberDl.open()) {
                        int ics = numberDl.getNumber();
                        // build the eigenvalue filter
                        CompositeEVFilter filter = new CompositeEVFilter();
                        filter.add(new BelowEVFilter(1.0e-12, false));
                        filter.add(new SortingEVFilter(true, true));
                        filter.add(new DialogEVFilter(this));
                        // build an ICA configuration
                        FastICAConfig config = new FastICAConfig(
                                ics, FastICAConfig.Approach.DEFLATION,
                                1.0, 1.0e-14, 400, null);
                        // perform the independent component analysis
                        double[][] audioMatrix = AudioVector.toMatrix(audioList.toArray(new double[1][]));
                        FastICAThread ficaThread =
                                new FastICAThread(this, audioMatrix, config, new TanhCFunction(1.0), filter);
                        ficaThread.start();
                    }
                }
            }
        }
    }

    private void actionExit() {
        System.exit(0);
    }

    protected void processWindowEvent(
            WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING)
            actionExit();
        super.processWindowEvent(event);
    }

    public void setProgress(
            int progressValue,
            String progressString) {
        statusPB.setValue(progressValue);
        statusPB.setString(progressString);
        statusPB.updateUI();
    }

    public void setLock(
            boolean lock) {
        addBt.setEnabled(!lock);
        deleteBt.setEnabled(!lock);
        playBt.setEnabled(!lock);
        mixBt.setEnabled(!lock);
        analyseBt.setEnabled(!lock);
    }

    public void saveAudioVectors(
            double[][] newAudioVectors,
            String newCollectionName) {
        int n = newAudioVectors.length;
        LinkedList<double[]> newAudioList = new LinkedList<double[]>();
        audioVectors.add(newAudioList);
        DefaultMutableTreeNode newParNd = new DefaultMutableTreeNode(newCollectionName);
        dataRootNd.add(newParNd);
        for (int i = 0; i < n; ++i) {
            newAudioList.add(AudioVector.normalise(newAudioVectors[i]));
            newParNd.add(new DefaultMutableTreeNode(Integer.toString(i)));
        }
        // update the tree
        dataTr.updateUI();
    }

}
