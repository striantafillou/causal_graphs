package edu.cmu.tetradapp.app;

import edu.cmu.tetradapp.util.ImageUtils;
import edu.cmu.tetradapp.workbench.DisplayNodeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Appearance of session nodes for standard nodes.
 *
 * @author Joseph Ramsey
 */
public class StdDisplayComp extends JComponent implements SessionDisplayComp {

    /**
     * The color of this node when there is no underlying model that it
     * represents.
     */
    private static final Color NO_MODEL_COLOR = new Color(214, 214, 214);

    /**
     * The color of this node when there is an underlying model that it
     * represents.
     */
    private static final Color HAS_MODEL_COLOR = DisplayNodeUtils.getNodeFillColor();

    /**
     * The color this node when unselected; depends on whether there is a model
     * or not.
     */
    private Color unselectedColor = NO_MODEL_COLOR;

    /**
     * Font used to render text.
     */
    private static final Font SMALL_FONT = new Font("Dialog", Font.BOLD, 10);

    /**
     * A label displaying the name of this node--for instance, "Graph1".
     */
    private JLabel nameLabel;

    /**
     * A label displaying the acronym for this node--for instance, "Lag Graph".
     */
    private JLabel acronymLabel;

    /**
     * Whether the node is selected.
     */
    private boolean selected;

    /**
     * The image that's displayed in the node.
     */
    private Image image;


    public StdDisplayComp(String imagePath) {
        nameLabel = new JLabel(" ");
        acronymLabel = new JLabel("No model");
        image = ImageUtils.getImage(this, imagePath);
        layoutComponents();
    }

    /**
     * Sets the selection status of the node.
     *
     * @param selected the selection status of the node (true or false).
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        setBorder(null);
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setName(String name) {
        super.setName(name);
        nameLabel.setText(name);
    }

    public void setAcronym(String acronym) {
        acronymLabel.setText(acronym);
        layoutComponents();
    }

    public Shape getShape() {
        return new Rectangle2D.Double(0, 0, getSize().width - 1,
                    getSize().height - 1);
    }

    public boolean contains(int x, int y) {
        return getShape().contains(x, y);
    }

    public void setHasModel(boolean hasModel) {
        if (hasModel) {
            this.unselectedColor = HAS_MODEL_COLOR;
        }
        else {
            this.unselectedColor = NO_MODEL_COLOR;
        }
    }



    /**
     * Paints the background of the component (since it has to be a
     * JComponent).
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(isSelected() ? DisplayNodeUtils.getNodeSelectedFillColor() : unselectedColor);
        g2.fill(getShape());
        g2.setColor(isSelected() ? DisplayNodeUtils.getNodeSelectedEdgeColor() : DisplayNodeUtils.getNodeEdgeColor());
        g2.draw(getShape());

        super.paint(g);
    }

    private void layoutComponents() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(DisplayNodeUtils.getNodeFillColor());
        setFont(DisplayNodeUtils.getFont());

        Box b = Box.createVerticalBox();

        // Add icon to name label.
        Box b1 = Box.createHorizontalBox();
        b1.add(Box.createHorizontalGlue());
        b1.add(new JLabel(new ImageIcon(image)));
        b1.add(Box.createHorizontalGlue());
        b.add(b1);

        // Construct name label.
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalGlue());
        b2.add(Box.createHorizontalStrut(5));
        b2.add(nameLabel);
        b2.add(Box.createHorizontalStrut(5));
        b2.add(Box.createHorizontalGlue());
        b.add(b2);

        // Construct acronym label.
        Box b3 = Box.createHorizontalBox();
        b3.add(Box.createHorizontalGlue());
        b3.add(Box.createHorizontalStrut(5));
        acronymLabel.setFont(SMALL_FONT);
        b3.add(acronymLabel);
        b3.add(Box.createHorizontalStrut(5));
        b3.add(Box.createHorizontalGlue());
        b.add(b3);

        b.add(Box.createRigidArea(new Dimension(60, 0)));

        add(b, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

}
