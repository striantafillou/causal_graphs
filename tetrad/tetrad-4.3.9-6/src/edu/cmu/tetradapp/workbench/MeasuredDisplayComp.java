package edu.cmu.tetradapp.workbench;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * The display component for measured nodes--an opaque rounded rectangle.
 *
 * @author Joseph Ramsey
 */
public class MeasuredDisplayComp extends JComponent implements DisplayComp {
    private boolean selected = false;

    public MeasuredDisplayComp(String name) {
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
        return new RoundRectangle2D.Double(0, 0, getPreferredSize().width - 1,
                getPreferredSize().height - 1, 4, 3);
    }

    /**
     * Paints the component.
     *
     * @param g the graphics context.
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        int width = getPreferredSize().width;
        int stringWidth = fm.stringWidth(getName());
        int stringX = (width - stringWidth) / 2;
        int stringY = fm.getAscent() + DisplayNodeUtils.getPixelGap();

        g2.setColor(isSelected() ? DisplayNodeUtils.getNodeSelectedFillColor() :
                DisplayNodeUtils.getNodeFillColor());
        g2.fill(getShape());
        g2.setColor(isSelected() ? DisplayNodeUtils.getNodeSelectedEdgeColor() :
                DisplayNodeUtils.getNodeEdgeColor());
        g2.draw(getShape());
        g2.setColor(DisplayNodeUtils.getNodeTextColor());
        g2.setFont(DisplayNodeUtils.getFont());
        g2.drawString(getName(), stringX, stringY);
    }

    /**
     * Calculates the size of the component based on its name.
     */
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(DisplayNodeUtils.getFont());
        int width = fm.stringWidth(getName()) + fm.getMaxAdvance();
        int height = 2 * DisplayNodeUtils.getPixelGap() + fm.getAscent() + 3;
        width = (width < 60) ? 60 : width;
        return new Dimension(width, height);
    }

    public boolean isSelected() {
        return selected;
    }
}

