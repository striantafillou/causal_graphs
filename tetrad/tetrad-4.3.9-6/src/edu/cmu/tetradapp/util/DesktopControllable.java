package edu.cmu.tetradapp.util;

/**
 * Interface for desktop controller methods, to allow app components to control
 * the desktop without a package cycle. See TetradDesktop for meaning of
 * methods.
 *
 * @author Joseph Ramsey
 * @see edu.cmu.tetradapp.app.TetradDesktop
 */
public interface DesktopControllable {
    void newSessionEditor();

    SessionEditorIndirectRef getFrontmostSessionEditor();

    void exitProgram();

    boolean existsSessionByName(String name);

    void addSessionEditor(SessionEditorIndirectRef editor);

    void closeEmptySessions();

    void putMetadata(SessionWrapperIndirectRef sessionWrapper,
            TetradMetadataIndirectRef metadata);

    TetradMetadataIndirectRef getTetradMetadata(
            SessionWrapperIndirectRef sessionWrapper);

    void addEditorWindow(EditorWindowIndirectRef editorWindow);


    void closeFrontmostSession();

    boolean closeAllSessions();
}
