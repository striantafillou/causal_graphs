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

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

/**
 * Extends DisplayEdge to support the different edge display modes
 *
 * @author gmli
 */
public class MultiGraphEdge extends DisplayEdge {

    protected int visibility;
    protected boolean isSelfEdge =
            false; // is an edge to self with time lag>1 => arced edge

    // controls how much self-edges should "bow" out.
    // this should, at some point be a function of the size of the nodes.. but
    // there's no easy formula for calculating this. However, in general, for
    // repeating graphs, this works out just fine
    private static final float ARC_HEIGHT = 120.0f;
    private static final int SELECTION_FAT = 5;
    public static final Stroke UNANCHORED_STROKE = new BasicStroke(1.0f);
    // changed it to 1.0f stroke to make calculations for arced edges easier
    public static final Stroke NORMAL_STROKE = new BasicStroke(1.0f);

    protected Point controlPoint;
    private Polygon clickRegion = null;

    /**
     * Creates new MultiGraphEdge
     */
    public MultiGraphEdge(DisplayNode node1, DisplayNode node2) {
        super(node1, node2, DIRECTED);
        setVisibility(255); // default to being fully visible

        if (node1 instanceof MultiGraphNode && node2 instanceof MultiGraphNode)
        {
            MultiGraphNode mNode1 = (MultiGraphNode) node1;
            MultiGraphNode mNode2 = (MultiGraphNode) node2;
            String name1 = mNode1.getFactorGroup().getName();
            int lag = mNode2.getTimestep() - mNode1.getTimestep();
            isSelfEdge = name1.equals(mNode2.getFactorGroup().getName()) &&
                    (lag > 1);
            if (isSelfEdge) {
                resetBounds(); // need to recall this, because it would have been called
                // by super with isSelfEdge == false
                //clickRegion = null;
            }
        }
    }

    /**
     * Sets the transparency of the edge
     */
    public void setVisibility(int visibility) {
        if (visibility < 0 || visibility > 255) {
            throw new IllegalArgumentException("visibility must be 0-255");
        }
        this.visibility = visibility;
    }

    /**
     * Returns the visibility of the edge
     */
    public int getVisibility() {
        return visibility;
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
        PointPair pp = null;

        switch (getMode()) {

            case HALF_ANCHORED:
                g.setColor(getLineColor());
                g2.setStroke(UNANCHORED_STROKE);
                pp = calculateEdge(getNode1(), getRelativeMouseTrackPoint());
                break;

            case ANCHORED_UNSELECTED:
                Color c = new Color(getLineColor().getRed(), getLineColor().getGreen(),
                        getLineColor().getBlue(), getVisibility());
                g.setColor(c);
                g2.setStroke(NORMAL_STROKE);
                pp = calculateEdge(getNode1(), getNode2());
                break;

            case ANCHORED_SELECTED:
                g.setColor(getSelectedColor());
                g2.setStroke(NORMAL_STROKE);
                pp = calculateEdge(getNode1(), getNode2());
                break;

            default :
                throw new IllegalStateException();
        }

        if (pp != null) {
            Point loc = getLocation();
            pp.getFrom().translate(-loc.x, -loc.y);
            pp.getTo().translate(-loc.x, -loc.y);

            setClickRegion(null);

            if (isSelfEdge) {
                //float dy = pp.to.y-pp.from.y;
                //float dx = pp.to.x-pp.from.x;
                //float len = (float) pp	.from.distance(pp.to);
                //float scale = (float) ARC_HEIGHT / len;
                //Point2D.Float control = new Point2D.Float(pp.from.x + dx/2 + dy*scale, pp.from.y + dy/2 + dx*scale);
                //controlPoint = new Point((int) control.x, (int) control.y);
                Point tempCPoint = new Point(controlPoint);
                //tempCPoint.translate(-loc.x, -loc.y);
                //System.out.println("Before: " + pp.from.toString() + " " + pp.to.toString() + " " + tempCPoint.toString());
                //Polygon selArea = getClickRegion();
                //selArea.translate(-loc.x, -loc.y);
                //g2.fill(selArea);
                //System.out.println("After: " + pp.from.toString() + " " + pp.to.toString() + " " + tempCPoint.toString());
                QuadCurve2D arc = new QuadCurve2D.Float((float) pp.getFrom().x,
                        (float) pp.getFrom().y, (float) tempCPoint.x,
                        (float) tempCPoint.y, (float) pp.getTo().x,
                        (float) pp.getTo().y);
                g2.draw(arc);
                // have to kind of hack this to make the endpoint draw in the right direction..
                PointPair pp2 = new PointPair(tempCPoint, pp.getTo());
                drawEndpoints(pp2, g);
            }
            else {
                g.drawLine(pp.getFrom().x, pp.getFrom().y, pp.getTo().x,
                        pp.getTo().y);
                drawEndpoints(pp, g);
            }
            g2.setStroke(origStroke);
            setConnectedPoints(pp);
            firePropertyChange("newPointPair", null, pp);
        }
    }

    /**
     * This method resets the bounds of the edge component to the union of the
     * bounds of the two components which the edge connects.  It also calculates
     * the bounds of these two components relative to this new union.
     */
    protected void resetBounds() {

        if (getMode() == HALF_ANCHORED || !isSelfEdge) {
            super.resetBounds();
        }
        else {
            // recalculate the control point
            PointPair endP = calculateEdge(getNode1(), getNode2());

            // calculate a coordinate system base for controlPoint- needed
            // to be consistent with coordinate system used by pp
            Rectangle bounds1 = getNode1().getBounds();
            Rectangle bounds2 = getNode2().getBounds();
            Rectangle baseBounds = bounds1.union(bounds2);
            if (endP == null) {
                // ?? when would this happen (that is, why would pp be null?)
                setBounds(baseBounds);
            }
            else {
                //pp.from.translate(-baseBounds.x, -baseBounds.y);
                //pp.to.translate(-baseBounds.x, -baseBounds.y);

                float dy = endP.getTo().y - endP.getFrom().y;
                float dx = endP.getTo().x - endP.getFrom().x;
                float len = (float) endP.getFrom().distance(endP.getTo());
                float scale = ARC_HEIGHT / len;
                float x1 = endP.getFrom().x + (dx / 2) + (dy * scale);
                float y1 = endP.getFrom().y + (dy / 2) + (dx * scale);
                Point2D.Float control = new Point2D.Float(x1, y1);
                controlPoint = new Point((int) control.x, (int) control.y);

                // calculate the bounds
                Rectangle b = new Rectangle(controlPoint);
                b = b.union(baseBounds);
                setBounds(b);

                // controlPoint is used in other places...  but in other places
                // it should be using relative coordinates
                controlPoint.translate(-getLocation().x, -getLocation().y);
                //setBounds(baseBounds);
            }

            //node1RelBounds = new Rectangle(node1.getBounds());
            //node2RelBounds = new Rectangle(node2.getBounds());

            //node1RelBounds.translate(-getLocation().x, -getLocation().y);
            //node2RelBounds.translate(-getLocation().x, -getLocation().y);
        }
    }

    /**
     * Method declaration
     *
     * @return the midpoint of the edge.
     */
    public Point getMidPoint() {
        PointPair pp = getConnectedPoints();

        if (getMode() == HALF_ANCHORED || !isSelfEdge)
        //PointPair pp = getPointPair();
        {
            return new Point((pp.getFrom().x + pp.getTo().x) / 2,
                    (pp.getFrom().y + pp.getTo().y) / 2);
        }
        else {
            //PointPair pp = getPointPair();
            Point virtualMid = midPoint(pp.getFrom(), pp.getTo());
            return midPoint(virtualMid, controlPoint);
        }
    }

    // calculates the midpoint of two points
    private Point midPoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    /**
     * Retrieves the current region where mouse clicks are responded to (as
     * opposed to passed on). </p> Creates an approximate polygon around curved
     * edges
     *
     * @return the click region.
     */
    public Polygon getClickRegion() {
        PointPair pp = getConnectedPoints();

        if (getMode() == HALF_ANCHORED || !isSelfEdge) {
            clickRegion = super.getClickRegion();
        }
        else {
            if (clickRegion == null && pp != null) {
                // approximates the click region for an arc
                Point tempCPoint = new Point(controlPoint);

                //tempCPoint.translate(-loc.x, -loc.y);
                Point arc1 = midPoint(pp.getFrom(), tempCPoint);
                Point arc2 = midPoint(tempCPoint, pp.getTo());

                Point mid = getMidPoint();

                // create a unit vector to add some "fat" around the midpoint portion to
                // make it easier to select
                float bisect_len = (float) tempCPoint.distance(mid);
                int bisect_x =
                        (int) (((float) (tempCPoint.x - mid.x)) / bisect_len);
                int bisect_y =
                        (int) (((float) (tempCPoint.y - mid.y)) / bisect_len);
                bisect_x *= SELECTION_FAT;
                bisect_y *= SELECTION_FAT;
                mid.translate(-bisect_x, -bisect_y);
                arc1.translate(bisect_x, bisect_y);
                arc2.translate(bisect_x, bisect_y);

                //System.out.println("A: " + " " + pp.from.toString() + " " + pp.to.toString() + " " + tempCPoint.toString());
                int xPoints[] =
                        {pp.getFrom().x, arc1.x, arc2.x, pp.getTo().x, mid.x};
                int yPoints[] =
                        {pp.getFrom().y, arc1.y, arc2.y, pp.getTo().y, mid.y};

                clickRegion = new Polygon(xPoints, yPoints, 5);
                setClickRegion(clickRegion);
            }
        }
        return clickRegion;
    }
}


