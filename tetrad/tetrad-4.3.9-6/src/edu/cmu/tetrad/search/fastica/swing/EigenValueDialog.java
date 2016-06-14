package edu.cmu.tetrad.search.fastica.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * This dialog is used to choose eigenvalues.
 *
 * @author Michael Lambertz
 */

public class EigenValueDialog extends JDialog {

    private static final long serialVersionUID = 3906082369361621299L;

    private enum State {
        PROCESSING,
        OKAY,
        CANCEL
    }

    private State state;
    private boolean[] acceptanceList;
    private JCheckBox[] eigenValueCB;

    public EigenValueDialog(
            JFrame owner,
            String title,
            double[] eigenValues) {
        // create dialog
        super(owner, title, true);

        // create data
        int m = eigenValues.length;
        acceptanceList = new boolean[m];
        for (int i = 0; i < m; ++i)
            acceptanceList[i] = true;

        // create dialog content
        JPanel mainPn = new JPanel(new BorderLayout(4, 4));
        setContentPane(mainPn);

        JPanel eigenValuePn = new JPanel(new GridLayout(m, 1, 4, 4));
        mainPn.add(eigenValuePn, BorderLayout.CENTER);

        eigenValueCB = new JCheckBox[m];
        for (int i = 0; i < m; ++i) {
            eigenValueCB[i] = new JCheckBox(Double.toString(eigenValues[i]), true);
            eigenValuePn.add(eigenValueCB[i]);
        }

        JPanel buttonPn = new JPanel();
        mainPn.add(buttonPn, BorderLayout.SOUTH);

        JButton okBt = new JButton("  OK  ");
        okBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionOk();
                    }
                });
        buttonPn.add(okBt);

        JButton cancelBt = new JButton("Cancel");
        cancelBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionCancel();
                    }
                });
        buttonPn.add(cancelBt);

        mainPn.add(new JLabel("  "), BorderLayout.NORTH);
        mainPn.add(new JLabel("  "), BorderLayout.WEST);
        mainPn.add(new JLabel("  "), BorderLayout.EAST);

        // set frame's size and position
        pack();
        setLocation(owner.getLocation().x + 16, owner.getLocation().y + 16);
        setSize((int) (getSize().width * 1.1), (int) (getSize().height * 1.05));
    }

    public boolean[] getAcceptanceList() {
        return (acceptanceList);
    }

    public boolean open() {
        state = State.PROCESSING;
        setVisible(true);
        while (state == State.PROCESSING) ;
        return (state == State.OKAY);
    }

    private void actionOk() {
        int m = acceptanceList.length;
        for (int i = 0; i < m; ++i)
            acceptanceList[i] = eigenValueCB[i].isSelected();
        state = State.OKAY;
        setVisible(false);
    }

    private void actionCancel() {
        state = State.CANCEL;
        setVisible(false);
    }

    protected void processWindowEvent(
            WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING) {
            actionCancel();
        }
        super.processWindowEvent(event);
    }

}
