package edu.cmu.tetradapp.workbench;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * The display component for error nodes, which is a transparent ellipse.
 *
 * @author  Joseph Ramsmey
 */
public class ErrorDisplayComp extends JComponent
        implements DisplayComp {
    private boolean selected = false;

    public ErrorDisplayComp(String name) {
        setBackground(DisplayNodeUtils.getNodeFillColor());
        setFont(DisplayNodeUtils.getFont());
        setName(name);
    }

    public void setName(String name) {
        super.setName(name);
        setSize(getPreferredSize());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean contains(int x, int y) {
        return getShape().contains(x, y);
    }

    /**
     * Returns the shape of the component.
     */
    public Shape getShape() {
        return new Rectangle2D.Double(0, 0, getPreferredSize().width - 1,
                getPreferredSize().height - 1);
    }

    /**
     * Paints the component.
     */
    public void paint(Graphics g) {
        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        Dimension size = getPreferredSize();
        int stringWidth = fm.stringWidth(getName());
        int stringX = (size.width - stringWidth) / 2;
        int stringY = fm.getAscent() + (size.height - fm.getHeight()) / 2;

        g.setColor(DisplayNodeUtils.getNodeTextColor());
        g.drawString(getName(), stringX, stringY);
    }


    /**
     * Calculates the size of the component based on its name.
     */
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        int width = fm.stringWidth(getName()) + fm.getMaxAdvance();
        int height = 2 * DisplayNodeUtils.getPixelGap() + fm.getAscent();

        return new Dimension(width, height);
    }

    public boolean isSelected() {
        return selected;
    }
}
