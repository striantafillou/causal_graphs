package edu.cmu.tetrad.search.fastica.swing;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.prefs.Preferences;

/**
 * The main class for the FastIca Swing application.
 *
 * @author Michael Lambertz
 */

public class FastICAApp {

    //preference keys and defaults
    public static final int PREF_DIR_ADD = 0;
    private static final String[] PREF_KEYS = {
            "DIR_ADD"};
    private static final String[] PREF_DEFAULTS = {
            System.getProperty("user.home")};

    public FastICAApp() {
        // initialize main frame
        FastICAFrame frame = new FastICAFrame();

        // center the frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    public static void main(String[] args)
            throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new FastICAApp();
    }

    public static void exceptionDialog(Exception exc) {
        StringWriter sw = new StringWriter();
        exc.printStackTrace(new PrintWriter(sw));
        String str = sw.toString();
        JOptionPane.showMessageDialog(
                null,
                "Error: " + str,
                exc.getClass().toString(),
                JOptionPane.ERROR_MESSAGE);
        System.err.println(str);
    }

    public static String getPreference(int pref) {
        Preferences
                prefs = Preferences.userNodeForPackage(FastICAApp.class);
        return (prefs.get(PREF_KEYS[pref], PREF_DEFAULTS[pref]));
    }

    public static void setPreference(int pref, String value) {
        Preferences
                prefs = Preferences.userNodeForPackage(FastICAApp.class);
        prefs.put(PREF_KEYS[pref], value);
    }

}

