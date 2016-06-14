package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.util.NumberFormatUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A panel that is responsible for drawing a histogram.
 *
 * @author Tyler Gibson
 */
public class HistogramDisplayPanel extends JPanel {


    /**
     * The line color around the histogram.
     */
    private static Color LINE_COLOR = Color.GRAY.darker();

    /**
     * Bar colors for the histogram (ripped from causality lab)
     */
    private static Color BAR_COLORS[] = {
            new Color(153, 102, 102), new Color(102, 102, 153), new Color(102, 153, 102), new Color(153, 102, 153), new Color(153, 153, 102), new Color(102, 153, 153), new Color(204, 153, 153), new Color(153, 153, 204), new Color(153, 204, 153), new Color(204, 153, 204),
            new Color(204, 204, 153), new Color(153, 204, 204), new Color(255, 204, 204), new Color(204, 204, 255), new Color(204, 255, 204)
    };


    /**
     * Variables that control the size of the drawing area.
     */
    private final static int PADDING = 40;
    private final static int HEIGHT = 250 + PADDING;
    private final static int DISPLAYED_HEIGHT = (int) ((HEIGHT - PADDING) - HEIGHT * .10);
    private final static int WIDTH = 290 + PADDING;
    private final static int SPACE = 2;
    private final static int DASH = 10;


    /**
     * The default size of the component.
     */
    private Dimension size = new Dimension(WIDTH + 2 * SPACE, HEIGHT);

    /**
     * Format for continuous data.
     */
    private NumberFormat format = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The histogram we are displayling.
     */
    private Histogram histogram;


    /**
     * A cached string displaying what is being viewed in the histogram.
     */
    private String displayString;


    /**
     * A cache value that stores the top frequency.
     */
    private int topFreq = -1;

    /**
     * A map from the rectangles that define the bars, to the number of units in the bar.
     */
    private Map<Rectangle, Integer> rectMap = new ConcurrentHashMap<Rectangle, Integer>();


    /**
     * Constructs the histogram dipslay panel given the initial histogram to display.
     *
     * @param histogram
     */
    public HistogramDisplayPanel(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("Given histogram must be null");
        }
        this.histogram = histogram;

        this.addMouseMotionListener(new MouseMovementListener());
        this.setToolTipText(" ");
    }

    //============================ PUblic Methods =============================//

    /**
     * Updates the histogram that is dispalyed to the given one.
     *
     * @param histogram
     */
    public synchronized void updateHistogram(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("The given histogram must not be null");
        }
        this.displayString = null;
        this.histogram = histogram;
        this.topFreq = -1;
        this.repaint();
    }


    public String getToolTipText(MouseEvent evt) {
        Point point = evt.getPoint();
        for (Rectangle rect : rectMap.keySet()) {
            if (rect.contains(point)) {
                Integer i = rectMap.get(rect);
                if (i != null) {
                    return i.toString();
                }
                break;
            }
        }
        return null;
    }


    /**
     * Paints the histogram and related items.
     *
     * @param graphics
     */
    public void paintComponent(Graphics graphics) {
        // set up variables.
        this.rectMap.clear();
        Graphics2D g2d = (Graphics2D) graphics;
        Histogram histogram = this.histogram;
        int[] freqs = histogram.getFrequencies();
        int categories = freqs.length;
        int barWidth = Math.max((WIDTH - PADDING) / categories, 12) - SPACE;
        int height = HEIGHT - PADDING;
        int max = getMax(freqs);
        double scale = DISPLAYED_HEIGHT / (double) max;
        FontMetrics fontMetrics = g2d.getFontMetrics();
        // draw background/surrounding box.
        g2d.setColor(this.getBackground());
        g2d.fillRect(0, 0, WIDTH + 2 * SPACE, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(PADDING, 0, (WIDTH + SPACE) - PADDING, height);
        // draw the histogram
        for (int i = 0; i < categories; i++) {
            int freq = freqs[i];
            int y = (int) Math.ceil(scale * freq);
            int x = SPACE * (i + 1) + barWidth * i + PADDING;
            g2d.setColor(getBarColor(i));
            Rectangle rect = new Rectangle(x, (height - y), barWidth, y);
            g2d.fill(rect);
            rectMap.put(rect, freq);
        }
        //border
        g2d.setColor(LINE_COLOR);
        g2d.drawRect(PADDING, 0, (WIDTH + SPACE) - PADDING, height);
        // graw the buttom line
        g2d.setColor(LINE_COLOR);
        g2d.drawString(format.format(histogram.getMinValue()), PADDING + 5, height + 15);
        g2d.drawLine(PADDING, height + DASH, PADDING, height);
        String maxStr = format.format(histogram.getMaxValue());
        g2d.drawString(maxStr, WIDTH - fontMetrics.stringWidth(maxStr), height + 15);
        g2d.drawLine(WIDTH + SPACE, height + DASH, WIDTH + SPACE, height);
        int size = (WIDTH - PADDING) / 4;
        for (int i = 1; i < 4; i++) {
            int width = size * i;
            g2d.drawLine(width + PADDING, height + DASH, width + PADDING, height);
        }
        // draw the side line
        g2d.setColor(LINE_COLOR);
        int topFreq = getTopFreq();
        int topY = height - (int) Math.ceil(scale * topFreq);
        String top = String.valueOf(topFreq);
        g2d.drawString(top, PADDING - fontMetrics.stringWidth(top), topY - 2);
        g2d.drawLine(PADDING - DASH, topY, PADDING, topY);
        g2d.drawString("0", PADDING - fontMetrics.stringWidth("0"), height - 2);
        g2d.drawLine(PADDING - DASH, height, PADDING, height);
        int hSize = (height - topY) / 4;
        for (int i = 1; i < 4; i++) {
            int topHeight = height - hSize * i;
            g2d.drawLine(PADDING - DASH, topHeight, PADDING, topHeight);
        }
        // draw the display string.
        g2d.setColor(LINE_COLOR);
        g2d.drawString(getDisplayString(), PADDING, HEIGHT - 5);
    }


    public Dimension getPreferredSize() {
        return this.size;
    }


    public Dimension getMaximumSize() {
        return this.size;
    }


    public Dimension getMinimumSize() {
        return this.size;
    }

    //========================== private methods ==========================//


    private static Color getBarColor(int i) {
        return BAR_COLORS[i % BAR_COLORS.length];
    }


    private int getTopFreq() {
        if (topFreq == -1) {
            int[] freqs = this.histogram.getFrequencies();
            int top = freqs[0];
            for (int freq : freqs) {
                if (top < freq) {
                    top = freq;
                }
            }
            this.topFreq = top;
        }
        return topFreq;
    }


    private String getDisplayString() {
        if (this.displayString == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Showing: ");
            builder.append(histogram.getSelectedVariable().getName());
            java.util.List<Histogram.ConditionalDependency> conditions = histogram.getConditionalDependencies();
            if (!conditions.isEmpty()) {
                builder.append(" | ");
                for (int k = 0; k < conditions.size(); k++) {
                    Histogram.ConditionalDependency condition = conditions.get(k);
                    builder.append(condition.getNode().getName());
                    builder.append("=");
                    java.util.List<String> values = condition.getValues();
                    for (int i = 0; i < values.size(); i++) {
                        builder.append(values.get(i));
                        if (i < values.size() - 1) {
                            builder.append(" v ");
                        } else if (k < conditions.size() - 1) {
                            builder.append(", ");
                        }
                    }
                }
            }
            this.displayString = builder.toString();
        }
        return this.displayString;
    }


    private static int getMax(int[] freqs) {
        int max = freqs[0];
        for (int i = 1; i < freqs.length; i++) {
            int current = freqs[i];
            if (max < current) {
                max = current;
            }
        }
        return max;
    }

    //============================ Inner class =====================================//

    private class MouseMovementListener implements MouseMotionListener {

        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {
            Point point = e.getPoint();
            for (Rectangle rect : rectMap.keySet()) {
                if (rect.contains(point)) {
                  //  System.out.println(rectMap.get(rect));
                    break;
                }
            }
        }
    }

}
