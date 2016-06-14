package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.util.TetradLogger;

import javax.swing.*;
import java.io.OutputStream;

/**
 * An output stream to pipe stuff written to it directly to a JTextArea.
 *
 * @author  Joseph Ramsey
 */
public class TextAreaOutputStream extends OutputStream implements TetradLogger.LogDisplayOutputStream{

    /**
     * The text area written to.
     */
    private JTextArea textArea;

    /**
     * A string bugger used to buffer lines.
     */
    private StringBuffer buf = new StringBuffer();

    /**
     * The length of string written to the text area.
     */
    private int lengthWritten = 0;

    /**
     * Creates a text area output stream, for writing text to the given
     * text area. It is assumed that the text area is blank to begin with
     * and that nothing else writes to it. The reason is that the length
     * of the text in the text area is tracked separately so that the text
     * area can quickly be scrolled to the end of the text stored. (The
     * internal mechanism for keeping track of text area length is slow.)
     *
     * @param textArea The text area written to.
     */
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;        
        lengthWritten = textArea.getText().length();
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public synchronized void write(int b) {
        buf.append((char) b);

        if ((char) b == '\n') {
            if (lengthWritten > 10000) {
                String text = textArea.getText();
                StringBuffer buf1 = new StringBuffer(text.substring(5000));
                buf1.append(buf);
                textArea.setText(buf.toString());
                lengthWritten = buf.length();
                buf.setLength(0);
                moveToEnd();
            }
            else {
                textArea.append(buf.toString());
//            textArea.setText(buf.toString());
                lengthWritten = lengthWritten + buf.length();
                buf.setLength(0);
                moveToEnd();
            }
        }
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the platform's default character encoding.
     *
     * @return String translated from the buffer's contents.
     */
    public String toString() {
        return textArea.toString();
    }


    public void reset(){
        this.textArea.setText("");
        this.lengthWritten = 0;
    }


    /**
     * The total string length written to the text area.
     * @return The total string length written to the text area.
     */
    public int getLengthWritten() {
        return lengthWritten;
    }

    public void moveToEnd() {
        this.textArea.setCaretPosition(this.lengthWritten);
    }
}
