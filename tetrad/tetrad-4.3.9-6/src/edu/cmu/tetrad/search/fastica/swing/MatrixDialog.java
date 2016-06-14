package edu.cmu.tetrad.search.fastica.swing;

import edu.cmu.tetrad.search.fastica.math.Matrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * This dialog can be used to input a matrix.
 *
 * @author Michael Lambertz
 */

public class MatrixDialog extends JDialog {

    private static final long serialVersionUID = 3905809656100960049L;

    private enum State {
        PROCESSING,
        OKAY,
        CANCEL
    }

    private State state;
    private JTextField[][] matrixTF;
    private double[][] matrix;


    public MatrixDialog(
            Frame owner,
            String title,
            double[][] initMatrix) {
        // create dialog
        super(owner, title, true);

        // create data
        int m = Matrix.getNumOfRows(initMatrix);
        int n = Matrix.getNumOfColumns(initMatrix);
        matrix = Matrix.newMatrix(m, n);

        // create dialog content
        JPanel mainPn = new JPanel(new BorderLayout(4, 4));
        setContentPane(mainPn);

        JPanel matrixPn = new JPanel(new GridLayout(m, n, 4, 4));
        mainPn.add(matrixPn, BorderLayout.CENTER);

        matrixTF = new JTextField[m][];
        for (int i = 0; i < m; ++i) {
            matrixTF[i] = new JTextField[n];
            for (int j = 0; j < n; ++j) {
                matrixTF[i][j] = new JTextField(Double.toString(initMatrix[i][j]));
                matrixPn.add(matrixTF[i][j]);
            }
        }

        JPanel buttonPn = new JPanel();
        mainPn.add(buttonPn, BorderLayout.SOUTH);

        JButton randomBt = new JButton("Random");
        randomBt.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        actionRandom();
                    }
                });
        buttonPn.add(randomBt);

        buttonPn.add(new JLabel("  "));

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

    public double[][] getMatrix() {
        return (matrix);
    }

    private void actionRandom() {
        int m = Matrix.getNumOfRows(matrix);
        int n = Matrix.getNumOfColumns(matrix);
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j) {
                matrix[i][j] = Math.random();
                matrixTF[i][j].setText(Double.toString(matrix[i][j]));
            }
        repaint();
    }

    private void actionOk() {
        try {
            int m = Matrix.getNumOfRows(matrix);
            int n = Matrix.getNumOfColumns(matrix);
            for (int i = 0; i < m; ++i)
                for (int j = 0; j < n; ++j)
                    matrix[i][j] = Double.parseDouble(matrixTF[i][j].getText());
        }
        catch (NumberFormatException exc) {
            return;
        }
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

