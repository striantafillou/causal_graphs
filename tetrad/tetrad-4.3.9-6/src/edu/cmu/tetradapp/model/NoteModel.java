package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.session.SessionModel;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 * Provides a simple model for notes that the user may want to add to the
 * session. Notes are stored as styled documents, on the theory that maybe
 * at some point the ability to add styling will be nice. Names are also
 * stored on the theory that maybe someday the name of the node can be
 * displayed in the interface. That day is not this day.
 *
 * @author Joseph Ramsey
 */
public class NoteModel implements SessionModel {
    static final long serialVersionUID = 23L;

    private StyledDocument note = new DefaultStyledDocument();
    private String name;

    public NoteModel() {
        
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static NoteModel serializableInstance() {
        return new NoteModel();
    }

    public StyledDocument getNote() {
        return note;
    }

    public void setNote(StyledDocument note) {
        this.note = note;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
