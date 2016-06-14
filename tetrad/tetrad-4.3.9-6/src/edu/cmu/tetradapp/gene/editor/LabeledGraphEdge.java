///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.gene.editor;

import edu.cmu.tetradapp.workbench.DisplayEdge;
import edu.cmu.tetradapp.workbench.DisplayNode;
import edu.cmu.tetradapp.workbench.PointPair;

import javax.swing.*;
import java.awt.*;

/**
 * @author ejt
 */
public final class LabeledGraphEdge extends DisplayEdge
        implements WeightedDisplayEdgeInterface {
    private String label;
    private boolean showLabel;
    private boolean showMulti;
    private double offset;
    private int alpha;

    private static final Stroke UNANCHORED_STROKE = new BasicStroke(1.0f);
    private static final Stroke NORMAL_STROKE = new BasicStroke(1.0f);
    private static final double CIRCULAR_RADIUS = 40;
    private static final int PADDING = 10;

    public LabeledGraphEdge(DisplayNode node1, DisplayNode node2,
            String label) {
        super(node1, node2, DIRECTED);
        setLabel(label); // default to being fully visible
        setAlpha(255);
        setShowMulti(false);
        resetBounds();
    }

    private void setLabel(String label) {
        this.label = label;
        setShowLabel(label != null);
    }

    public String getLabel() {
        return label;
    }

    public int getWeight() {
        return Integer.parseInt(label);
    }

    public void setWeight(int weight) {
        label = String.valueOf(weight);
    }

    public boolean getShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        repaint();
    }

    public boolean getShowMulti() {
        return showMulti;
    }

    public void setShowMulti(boolean showMulti) {
        this.showMulti = showMulti;
        repaint();
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
        resetBounds();
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    /**
     * If this is a circular reference, draw a circle
     *
     * @param g the graphics2d context passed from regular paint()
     */
    private void paintCircle(Graphics2D g) {
        Point c = new Point((int) CIRCULAR_RADIUS, (int) CIRCULAR_RADIUS);
        double rad_off = calcRadFromOffset(offset);
        double start = normalize(searchCircleBoundaryIntersection(true, c));
        double end = normalize(searchCircleBoundaryIntersection(false, c));
        int astart = (int) (start / Math.PI * 180);
        int da = (int) (normalize(end - start) / Math.PI * 180);
        int inner_dia = (int) (2 * CIRCULAR_RADIUS) - 2 * PADDING;
        if (showLabel) {
            int gap =
                    (int) ((10 / (CIRCULAR_RADIUS - PADDING)) / Math.PI * 180);
            int da1 = (int) (normalize(rad_off - start) / Math.PI * 180);
            int da2 = (int) (normalize(end - rad_off) / Math.PI * 180);
            g.drawArc(PADDING, PADDING, inner_dia, inner_dia, astart,
                    da1 - gap);
            g.drawArc(PADDING, PADDING, inner_dia, inner_dia,
                    astart + da1 + gap, da2 - gap);
        }
        else {
            g.drawArc(PADDING, PADDING, inner_dia, inner_dia, astart, da);
        }
        if (showMulti) {
            int w = Integer.parseInt(label);
            double dr = ((double) da) / w / 180 * Math.PI;
            for (int i = 0; i < w; i++) {
                Point intersect = calcCircularPointFromAngle(end - dr * i, c);
                Point endpoint = calcCircularPointFromAngle(
                        end - dr * i - 12 / (CIRCULAR_RADIUS - PADDING), c);
                drawArrowEndpoint(endpoint, intersect, g);
            }
        }
        else {
            Point intersect = calcCircularPointFromAngle(end, c);
            Point endpoint = calcCircularPointFromAngle(
                    end - 12 / (CIRCULAR_RADIUS - PADDING), c);
            drawArrowEndpoint(endpoint, intersect, g);
        }
        if (showLabel) {
            int width = g.getFontMetrics().stringWidth(label);
            Point labelloc = calcCircularPointFromAngle(rad_off, c);
            g.setColor(Color.black);
            g.drawString(label, labelloc.x - width / 2, labelloc.y + 5);
        }
    }

    private double normalize(double a) {
        while (a < 0) {
            a += Math.PI * 2;
        }
        while (a >= Math.PI * 2) {
            a -= Math.PI * 2;
        }
        return a;
    }

    /**
     * Converts the usual offset in pixels [-20..20] to spread over 2*PI
     * radians. Offsets multiplied by 2 an extra time because the links get
     * double counted (potential bug?)
     *
     * @param offset the offset to convert
     */
    private double calcRadFromOffset(double offset) {
        return offset / 40 * 2 * Math.PI * 2;
    }

    /**
     * Calculates the center of the circle that would be used if it's a circular
     * link.
     */
    private Point calcCircleCenter() {
        Rectangle r = getNode1().getBounds();
        Point loc = new Point(r.x + r.width / 2, r.y + r.height / 2);
        int x = (int) (CIRCULAR_RADIUS * Math.cos(calcRadFromOffset(offset)));
        int y = (int) (CIRCULAR_RADIUS * Math.sin(calcRadFromOffset(offset)));
        return new Point(x + loc.x, -y + loc.y);
    }

    private Point calcCircularPointFromAngle(double a, Point center) {
        int x = (int) ((CIRCULAR_RADIUS - PADDING) * Math.cos(a));
        int y = (int) ((CIRCULAR_RADIUS - PADDING) * Math.sin(a));
        return new Point(x + center.x, -y + center.y);
    }

    /**
     * Overrides the parent's contains() method using the click region, so that
     * points not in the click region are passed through to components lying
     * beneath this one in the z-order. (Equates the effective shape of this
     * edge to its click region.)
     *
     * @param x the x value of the point to be tested.
     * @param y the y value of the point to be tested.
     * @return true of (x, y) is in the click region, false if not.
     */
    public boolean contains(int x, int y) {
        if (getNode1() == getNode2()) {
            if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                return false;
            }
            Point c = new Point((int) CIRCULAR_RADIUS, (int) CIRCULAR_RADIUS);
            int dx = (int) (x - CIRCULAR_RADIUS);
            int dy = (int) (CIRCULAR_RADIUS - y);
            double dist = Math.sqrt(dx * dx + dy * dy);
            double a = normalize(Math.atan2(dy, dx));
            double start = normalize(searchCircleBoundaryIntersection(true, c));
            double end = normalize(searchCircleBoundaryIntersection(false, c));
            return ((a > start || a < end) &&
                    dist > CIRCULAR_RADIUS - PADDING - 5 &&
                    dist < CIRCULAR_RADIUS - PADDING + 5);
        }
        else {
            return super.contains(x, y);
        }
    }

    /**
     * searches around the circle to find the intersection with the edge of
     * node1
     */
    private double searchCircleBoundaryIntersection(boolean clockwise,
            Point c) {
        double from = calcRadFromOffset(offset) + Math.PI;
        double to = (clockwise ? from + Math.PI : from - Math.PI);
        Point node1loc = getNode1().getLocation();
        Point node1offset = new Point(getLocation().x - node1loc.x,
                getLocation().y - node1loc.y);
        double mid = (from + to) / 2;

        while (Math.abs(from - to) * CIRCULAR_RADIUS > 2.0) {
            Point pMid = calcCircularPointFromAngle(mid, c);
            if (getNode1().contains(pMid.x + node1offset.x,
                    pMid.y + node1offset.y)) {
                from = mid;
            }
            else {
                to = mid;
            }
            mid = (from + to) / 2;
        }

        return mid;
    }

    protected void resetBounds() {
        if (getNode1() != getNode2()) {
            super.resetBounds();
        }
        else {
            Point c = calcCircleCenter();
            setBounds(c.x - (int) CIRCULAR_RADIUS, c.y - (int) CIRCULAR_RADIUS,
                    (int) (2 * CIRCULAR_RADIUS), (int) (2 * CIRCULAR_RADIUS));
        }
    }

    /**
     * This method paints the component.
     *
     * @param g the graphics context.
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // NOTE:  For this component, the resetBounds() methods should ALWAYS
        // be called before repaint().
        //int c1x, c1y, c2x, c2y;
        Stroke origStroke = g2.getStroke();

        switch (getMode()) {
            case HALF_ANCHORED:
                g.setColor(getLineColor());
                g2.setStroke(UNANCHORED_STROKE);
                break;
            case ANCHORED_UNSELECTED:
                g.setColor(getLineColor());
                g2.setStroke(NORMAL_STROKE);
                break;
            case ANCHORED_SELECTED:
                g.setColor(getSelectedColor());
                g2.setStroke(NORMAL_STROKE);
                break;
            default :
                throw new IllegalStateException();
        }
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));

        if (getNode1() == getNode2()) {
            paintCircle(g2);
            g2.setStroke(origStroke);
        }

        PointPair pp;

        if (getMode() == HALF_ANCHORED) {
            pp = calculateEdge(getNode1(), getRelativeMouseTrackPoint());
        }
        else {
            pp = calculateEdge(getNode1(), getNode2());
        }

        if (pp != null) {
            pp.getFrom().translate(-getLocation().x, -getLocation().y);
            pp.getTo().translate(-getLocation().x, -getLocation().y);

            setClickRegion(null);

            double rad = 10;
            double cx = (pp.getFrom().x + pp.getTo().x) / 2;
            double cy = (pp.getFrom().y + pp.getTo().y) / 2;
            Point p1, p2;
            if (showLabel) {
                if (pp.getFrom().x == pp.getTo().x) { //vertical line
                    if (pp.getFrom().y < pp.getTo().y) {
                        rad = -rad;
                    }
                    p1 = new Point((int) cx, (int) (cy + rad));
                    p2 = new Point((int) cx, (int) (cy - rad));
                }
                else {
                    if (pp.getFrom().x >= pp.getTo().x) {
                        rad = -rad;
                    }
                    double slope = (pp.getFrom().y - pp.getTo().y) /
                            (double) (pp.getFrom().x - pp.getTo().x);
                    double a = rad / Math.sqrt(slope * slope + 1);
                    double b = slope * a;
                    p1 = new Point((int) (cx - a), (int) (cy - b));
                    p2 = new Point((int) (cx + a), (int) (cy + b));
                }
                g.drawLine(pp.getFrom().x, pp.getFrom().y, p1.x, p1.y);
                g.drawLine(p2.x, p2.y, pp.getTo().x, pp.getTo().y);
            }
            else {
                g.drawLine(pp.getFrom().x, pp.getFrom().y, pp.getTo().x,
                        pp.getTo().y);
            }
            g2.setStroke(origStroke);
            if (showMulti) {
                int w = Integer.parseInt(label);
                if (pp.getFrom().x == pp.getTo().x) { //vertical line
                    double dy = (pp.getFrom().y - pp.getTo().y) / (double) w;
                    for (int i = 1; i <= w; i++) {
                        Point p = new Point(pp.getFrom().x,
                                pp.getFrom().y - (int) (dy * i));
                        drawArrowEndpoint(pp.getFrom(), p, g);
                    }
                }
                else {
                    double dx = (pp.getFrom().x - pp.getTo().x) / (double) w;
                    double dy = (pp.getFrom().y - pp.getTo().y) / (double) w;
                    for (int i = 1; i <= w; i++) {
                        Point p = new Point(pp.getFrom().x - (int) (dx * i),
                                pp.getFrom().y - (int) (dy * i));
                        drawArrowEndpoint(pp.getFrom(), p, g);
                    }
                }
            }
            else {
                drawEndpoints(pp, g);
            }
            if (showLabel) {
                int width = g.getFontMetrics().stringWidth(label);
                g.setColor(Color.black);
                g.drawString(label, (int) (cx - width / 2),
                        (int) (cy + Math.abs(rad) / 2));
            }
            setConnectedPoints(pp);
            firePropertyChange("newPointPair", null, pp);
        }
    }

    /**
     * Draws an arrowhead at the 'to' end of the edge.
     *
     * @param from
     * @param to
     * @param g    the graphics context.
     */
    private void drawArrowEndpoint(Point from, Point to, Graphics g) {
        double a = to.x - from.x;
        double b = from.y - to.y;
        double theta = Math.atan2(b, a);
        int itheta = (int) ((theta * 360.0) / (2.0 * Math.PI) + 180);

        g.fillArc(to.x - 18, to.y - 18, 36, 36, itheta - 15, 30);
    }

    public void launchAssociatedEditor() {
        String old = label;
        label = JOptionPane.showInputDialog("Time lag of edge:");
        if (label == null || Integer.parseInt(label) <= 0) {
            label = old;
        }
        else {
            firePropertyChange("edgeLaunch", old, this);
        }
    }

    private PointPair calculateEdge(JComponent comp1, JComponent comp2) {

        Rectangle r1 = comp1.getBounds();
        Rectangle r2 = comp2.getBounds();
        Point c1 = new Point((int) (r1.x + r1.width / 2.0),
                (int) (r1.y + r1.height / 2.0));
        Point c2 = new Point((int) (r2.x + r2.width / 2.0),
                (int) (r2.y + r2.height / 2.0));

        double angle = Math.atan2(c1.y - c2.y, c1.x - c2.x);
        angle += Math.PI / 2;
        Point d = new Point((int) (offset * Math.cos(angle)),
                (int) (offset * Math.sin(angle)));
        c1.translate(d.x, d.y);
        c2.translate(d.x, d.y);

        Point p1 = getBoundaryIntersection(comp1, c1, c2);
        Point p2 = getBoundaryIntersection(comp2, c2, c1);

        if ((p1 == null) || (p2 == null)) {
            return null;
        }
        else {
            return new PointPair(p1, p2);
        }
    }

    private Point getBoundaryIntersection(JComponent comp, Point pIn,
            Point pOut) {

        Point loc = comp.getLocation();

        if (!comp.contains(pIn.x - loc.x, pIn.y - loc.y)) {
            return null;
        }

        if (comp.contains(pOut.x - loc.x, pOut.y - loc.y)) {
            return null;
        }

        // Set up from, to, mid for a binary search (result = boundary
        // intersection).  In testing, this binary search method was
        // comparable to analytic methods for finding boundary
        // intersections for rectangular nodes but has the advantage
        // of flexibility, since this same algorithm applies without
        // modification to any convexly shaped nodes.  (The 'contains'
        // method for that node will of course need to be different.)
        Point pFrom = new Point(pOut);
        Point pTo = new Point(pIn);
        Point pMid = null;

        while (distance(pFrom, pTo) > 2.0) {
            pMid = new Point((pFrom.x + pTo.x) / 2, (pFrom.y + pTo.y) / 2);

            if (comp.contains(pMid.x - loc.x, pMid.y - loc.y)) {
                pTo = pMid;
            }
            else {
                pFrom = pMid;
            }
        }

        return pMid;
    }
}


