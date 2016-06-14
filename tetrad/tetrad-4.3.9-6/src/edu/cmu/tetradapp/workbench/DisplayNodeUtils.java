package edu.cmu.tetradapp.workbench;

import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Apr 1, 2006 Time: 5:19:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayNodeUtils {
    // Note that this component must be a JComponent, since non-rectangular
// shapes are used for some extensions.
    private static final Color NODE_FILL_COLOR = new Color(148, 198, 226);
    private static final Color NODE_EDGE_COLOR = new Color(146, 154, 166);
    private static final Color NODE_SELECTED_FILL_COLOR =
            new Color(244, 219, 110);
    private static final Color NODE_SELECTED_EDGE_COLOR =
            new Color(215, 193, 97);
    private static final Color NODE_TEXT_COLOR = new Color(0, 1, 53);
    private static final Font FONT = new Font("Dialog", Font.BOLD, 12);
    private static final int PIXEL_GAP = 7;

    public static Color getNodeFillColor() {
        return NODE_FILL_COLOR;
    }

    public static Color getNodeEdgeColor() {
        return NODE_EDGE_COLOR;
    }

    public static Color getNodeSelectedFillColor() {
        return NODE_SELECTED_FILL_COLOR;
    }

    public static Color getNodeSelectedEdgeColor() {
        return NODE_SELECTED_EDGE_COLOR;
    }

    public static Color getNodeTextColor() {
        return NODE_TEXT_COLOR;
    }

    public static Font getFont() {
        return FONT;
    }

    public static int getPixelGap() {
        return PIXEL_GAP;
    }
}
