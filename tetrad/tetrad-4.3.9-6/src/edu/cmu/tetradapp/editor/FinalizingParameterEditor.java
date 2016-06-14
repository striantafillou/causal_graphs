package edu.cmu.tetradapp.editor;

/**
 * A <code>FinalizingParameterEditor</code> (for the lack of a better name) is a parameter editor that deals
 * with editing material that can't easily update itself while the user edits matters.
 * The editor should be either a JComponent or a JDialog, if the form then <code>finalizeEdit()</code>
 * will be called once the user has indicate that they are finished editing
 * so that the editor can collect and commit all the edits the user made. If <code>finalizeEdit</code>
 * returns false then the edit is aborted, as if they canceled it. And the other hand if the
 * editor is a Dialog then <code>finalizeEdit()</code> will be called right after <code>setup()</code>
 * assuming that the Dialog is modal and is handling matters on the users behalf.
 *
 *
 * @author Tyler Gibson
 */
public interface FinalizingParameterEditor extends ParameterEditor {

    /**
     * Tells the editor to commit any final details before it is closed (only called when the
     * user selects "Ok" or something of that nature). If false is returned the edit is considered
     * invalid and it will be treated as if the user selected "cancel".
     *
     * @return - true iff the edit was committed.
     */
    boolean finalizeEdit();




}
