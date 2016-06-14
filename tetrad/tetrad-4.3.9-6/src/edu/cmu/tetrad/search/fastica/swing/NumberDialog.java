package edu.cmu.tetrad.search.fastica.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;


/**
 * This dialog can be used to input a number.
 *
 * @author Michael Lambertz
 */

public class NumberDialog extends JDialog {

    private static final long serialVersionUID = 3257562910472550704L;

    private enum State {
        PROCESSING,
        OKAY,
        CANCEL
    }

    private State state;

    private int number;
    private JTextField numberTF;
    private int min;
    private int max;

    public NumberDialog(
            Frame owner,
            String title,
            int number,
            int min,
            int max) {
        // create dialog
        super(owner, title, true);

        // create data
        this.number = number;
        this.min = min;
        this.max = max;

        // create dialog content
        JPanel mainPn = new JPanel(new BorderLayout(4, 4));
        setContentPane(mainPn);

        numberTF = new JTextField(Integer.toBinaryString(number));
        mainPn.add(numberTF, BorderLayout.CENTER);

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

    public boolean open() {
        state = State.PROCESSING;
        setVisible(true);
        while (state == State.PROCESSING) ;
        return (state == State.OKAY);
    }

    public int getNumber() {
        return (number);
    }

    private void actionOk() {
        int temp;
        try {
            temp = Integer.parseInt(numberTF.getText());
        }
        catch (NumberFormatException exc) {
            return;
        }
        if (temp > max)
            return;
        if (temp < min)
            return;
        number = temp;
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
