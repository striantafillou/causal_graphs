package edu.cmu.tetradapp.util;

import javax.swing.*;

/**
 * Add description
 *
 * @author tyler
 */
public class TetradButton extends JButton {


    /**
     * Creates a button with no set text or icon.
     */
    public TetradButton() {
        this(null, null);
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon the Icon image to display on the button
     */
    public TetradButton(Icon icon) {
        this(null, icon);
    }

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public TetradButton(String text) {
        this(text, null);
    }

    /**
     * Creates a button where properties are taken from the
     * <code>Action</code> supplied.
     *
     * @param a the <code>Action</code> used to specify the new button
     * @since 1.3
     */
    public TetradButton(Action a) {
        this();
        setAction(a);
        this.setUI(TetradButtonUI.createUI());
    }

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public TetradButton(String text, Icon icon) {
        super(text, icon);
        this.setUI(TetradButtonUI.createUI());
    }

    /**
     * Does nothing but reset the UI to the tetrad button UI.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI(TetradButtonUI.createUI(this));
    }


}
