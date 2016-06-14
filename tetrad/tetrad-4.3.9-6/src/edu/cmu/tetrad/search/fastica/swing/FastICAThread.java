package edu.cmu.tetrad.search.fastica.swing;

import edu.cmu.tetrad.search.fastica.*;

import javax.swing.*;

/**
 * The <code>FastICAThread</code> class is used by the FastIca Swing application
 * to perform the independent component analysis.
 *
 * @author Michael Lambertz
 */

public class FastICAThread extends Thread {

    private FastICAFrame mainFrame;
    private double[][] inVectors;
    private FastICAConfig config;
    private ContrastFunction conFunction;
    private EigenValueFilter evFilter;
    private ProgressListener listener;
    private FastICA fica;

    public FastICAThread(
            FastICAFrame mainFrame,
            double[][] inVectors,
            FastICAConfig config,
            ContrastFunction conFunction,
            EigenValueFilter evFilter) {
        this.mainFrame = mainFrame;
        this.inVectors = inVectors;
        this.config = config;
        this.conFunction = conFunction;
        this.evFilter = evFilter;
        this.listener = null;
        this.fica = null;
    }

    private void lock() {
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                            mainFrame.setLock(true);
                        }
                    });
        }
        catch (Exception exc) {
            FastICAApp.exceptionDialog(exc);
        }
    }

    private void unlock() {
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                            mainFrame.setLock(false);
                        }
                    });
        }
        catch (Exception exc) {
            FastICAApp.exceptionDialog(exc);
        }
    }

    private void save() {
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                            mainFrame.saveAudioVectors(fica.getICVectors(), "Analysed Collection");
                        }
                    });
        }
        catch (Exception exc) {
            FastICAApp.exceptionDialog(exc);
        }
    }

    private void setProgress(
            int proVal,
            String proStr) {
        final int tmpVal = proVal;
        final String tmpStr = proStr;
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                            mainFrame.setProgress(tmpVal, tmpStr);
                        }
                    });
        }
        catch (Exception exc) {
            FastICAApp.exceptionDialog(exc);
        }
    }

    public void run() {
        // lock main frame
        lock();
        // build the progress listener
        listener =
                new ProgressListener() {
                    public void progressMade(
                            ComputationState state,
                            int component,
                            int iteration,
                            int maxComps) {
                        switch (state) {
                            case WHITENING:
                                setProgress(0, "...Whitening...");
                                break;
                            case SYMMETRIC:
                                setProgress((1024 * iteration) / config.getMaxIterations(), "...Analysing...");
                                break;
                            case DEFLATION:
                                int progress =
                                        (1024 * (iteration + config.getMaxIterations() * component)) /
                                                (config.getMaxIterations() * maxComps);
                                setProgress(progress, "...Analysing...");
                                break;
                            case READY:
                                setProgress(0, "Ready.");
                                break;
                        }
                    }
                };
        try {
            fica = new FastICA(inVectors, config, conFunction, evFilter, listener);
        }
        catch (FastICAException exc) {
            FastICAApp.exceptionDialog(exc);
            unlock();
            return;
        }
        System.gc();
        // copy resulting audio signals
        save();
        // unlock main frame
        unlock();
    }

}
