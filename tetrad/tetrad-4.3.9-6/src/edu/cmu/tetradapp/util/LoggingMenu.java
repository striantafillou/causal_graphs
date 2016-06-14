package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A menu that allows the user to configure a logger.
 *
 * @author Tyler Gibson
 */
public class LoggingMenu extends JMenu {


    /**
     * The logger that we are display setup for.
     */
    private TetradLoggerConfig config;


    /**
     * The component you want the menu to center matters on.
     */
    private Component parent;


    /**
     * Constructs the menu given the logger that the menu is to display.
     *
     * @param config
     */
    public LoggingMenu(TetradLoggerConfig config) {
        super("Logging");
        if (config == null) {
            throw new NullPointerException("The given config must not be null");
        }
        this.config = config;
        this.addMenuListener(new LoggingMenuListener());
    }


    /**
     * Constructs the logging menu, given the parent component that should be used
     * to center dialogs on.
     *
     * @param config
     * @param parent
     */
    public LoggingMenu(TetradLoggerConfig config, Component parent){
        this(config);
        this.parent = parent;
    }


    //============================ Private Methods ============================//

    /**
     * Builds the menu
     */
    private void buildMenu() {
        this.removeAll();
        JMenuItem setup = new JMenuItem("Setup Log Events...");
        setup.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showLogSetupDialog();
            }
        });

        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Logging");
        menuItem.setSelected(TetradLogger.getInstance().isLogging());

        this.add(setup);
        this.addSeparator();
        this.add(menuItem);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                TetradLogger.getInstance().setLogging(item.isSelected());
            }
        });
    }


    /**
     * Shows the log display setup dialog.
     */
    private void showLogSetupDialog() {
        JPanel panel = new JPanel();
        List<TetradLoggerConfig.Event> events = config.getSupportedEvents();
        panel.setLayout(new GridLayout(3, events.size() / 3));
        for (TetradLoggerConfig.Event event : events) {
            final String id = event.getId();
            JCheckBox checkBox = new JCheckBox(event.getDescription());
            checkBox.setHorizontalTextPosition(AbstractButton.RIGHT);
            checkBox.setSelected(config.isEventActive(id));
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox box = (JCheckBox) e.getSource();
                    config.setEventActive(id, box.isSelected());
                }
            });

            panel.add(checkBox);
        }

        panel.setBorder(new TitledBorder("Select Events to Log"));

        Component comp = parent == null ? JOptionUtils.centeringComp() : parent;
        JOptionPane.showMessageDialog(comp, panel, "Logging Setup", JOptionPane.PLAIN_MESSAGE);
    }

    //=========================== Inner class ===============================//


    /**
     * Menu listener that builds the menu on the fly when clicked.
     */
    private class LoggingMenuListener implements MenuListener {

        public void menuSelected(MenuEvent e) {
            buildMenu();
        }

        public void menuDeselected(MenuEvent e) {

        }

        public void menuCanceled(MenuEvent e) {

        }
    }

}
