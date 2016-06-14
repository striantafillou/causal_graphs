package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetradapp.util.ImageUtils;
import edu.cmu.tetradapp.util.TextAreaOutputStream;
import edu.cmu.tetradapp.workbench.DisplayNodeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The area used to display log output.
 *
 * @author Tyler Gibson
 */
class TetradLogArea extends JPanel {


    /**
     * Where the actual logs are displayed.
     */
    private JTextArea textArea;


    /**
     * The output stream that is used to log to.
     */
    private TextAreaOutputStream stream;


    /**
     * The desktop
     */
    private TetradDesktop desktop;


    /**
     * Constructs the log area.
     */
    public TetradLogArea(TetradDesktop tetradDesktop) {
        super(new BorderLayout());
        if (tetradDesktop == null) {
            throw new NullPointerException("The given desktop must not be null");
        }
        this.desktop = tetradDesktop;

        // build buttons.
        Box box = Box.createVerticalBox();

        JButton clear = new JButton(new ImageIcon(ImageUtils.getImage(this, "clear.png")));
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = "<html>Doing this will clear all of Tetrad's logs.<br>" +
                        "Are you sure you want to continue?<html>";
                int response = JOptionPane.showConfirmDialog(JOptionUtils.centeringComp(), new JLabel(message),
                        "Clear Logs", JOptionPane.YES_NO_OPTION);
                if(response == JOptionPane.YES_OPTION && stream != null){
                    stream.reset();
                }                                                
            }
        });


        JButton save = new JButton(new ImageIcon(ImageUtils.getImage(this, "save.png")));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setCurrentDirectory(new File(TetradLogger.getInstance().getLoggingDirectory()));

                int ret = chooser.showOpenDialog(desktop);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    writeLogToFile(selected);
                }
            }
        });


        JButton close = new JButton(new ImageIcon(ImageUtils.getImage(this, "close.png")));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desktop.setDisplayLogging(false);
                TetradLogger.getInstance().removeOutputStream(stream);
            }
        });

        box.add(clear);
        box.add(save);
        box.add(close);
        box.add(Box.createVerticalGlue());

        // build the text area.
        this.textArea = new JTextArea();
        if (TetradLogger.getInstance().isDisplayLogEnabled()) {
            this.stream = new TextAreaOutputStream(this.textArea);
            TetradLogger.getInstance().addOutputStream(this.stream);
        }
        JScrollPane pane = new JScrollPane(this.textArea);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // finally add the components to the panel.
        add(createHeader(), BorderLayout.NORTH);
        add(box, BorderLayout.WEST);
        add(pane, BorderLayout.CENTER);
    }

    //================================= Public methods =============================//


    /**
     * Returns the output stream that is being used to log messages to the log area.
     *
     * @return
     */
    public OutputStream getOutputStream() {
        return this.stream;
    }

    //============================== Private Methods ============================//

    /**
     * Creates the header of the log display.
     */
    private static JComponent createHeader() {
        JLabel label = new JLabel("  Log output");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setBackground(DisplayNodeUtils.getNodeFillColor());
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(1, 1, 1, 1));

        return label;
    }


    /**
     * Writes whatever is in the log display to the given file. Will display error messages if
     * any exceptions are thrown.
     *
     * @param file
     */
    private void writeLogToFile(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(this.textArea.getText());
            writer.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(desktop, "Error while trying to write to the selected file.");
        }
    }


}
