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

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * This scrollHandler enhances the operation of the MultiGraphWorkbench by
 * providing automatic operations tied to the scrollbar. Currently the only
 * operation that is done is switching between time modes. If a majority of the
 * editor is showing nodes from the past, then past time mode (where all edges
 * go into current time) is used. If a majority of future nodes is shown, the
 * future time mode (where all edges are sourced from current time) is used.
 *
 * @author gmli
 */
public class MultiGraphScrollHandler
        implements AdjustmentListener { //, PropertyChangeListener {

    MultiGraphWorkbench workbench;
    //JScrollBar defaultSb;
    boolean timeExpand_enabled;

    /*
	public void setDefaultScrollBar(JScrollBar defaultSb) {
		this.defaultSb = defaultSb;
	}
	 */

    /*
	 * Sets the editor that this scrollHandler enhances
	 */
    public void setWorkbench(MultiGraphWorkbench workbench) {
        /*
		if (editor==null) {
			if (this.editor != null)
				this.editor.removePropertyChangeListener(this);
		} else
			editor.addPropertyChangeListener(this);
		 */

        this.workbench = workbench;
    }

    /*
	public void propertyChange(PropertyChangeEvent evt) {
		if ("edgeDisplayMode".equals(evt.getPropertyName())) {
			timeExpand_enabled = (((Integer) evt.getNewValue()).getInt() == MultiGraphWorkbench.EDGE_LAG);
			evaluateTimeMode(defaultSb);
		}
	}
	 */

    /*
	 * Evaluates the scrollbar and updates the editor accordingly. Will switch
	 * automatically back and forth between past and future time modes
	 */
    protected void evaluateTimeMode(JScrollBar sb) {
        // NOTE: there is a lot of code commented out here.. it works, except was
        // mostly unnecessary. What it did was to automatically increase the number
        // of timesteps shown in the editor, based on how far forward/back the
        // scrollbar was, basically so that there were always nodes shown where
        // the user had scrolled to. However, it is much simpler just to have
        // more nodes shown than will fit on the editor, which is what is done
        // now

        // set the time mode to show edges into/from current time by comparing
        // center of viewport area to center of entire display
        int midway = (sb.getMaximum() >> 1);

        // adjust number of past/future timesteps shown to match visible area by
        // seeing how far into the future/past the display goes, and comparing
        // the number of timesteps that could be shown with the number that are
        // shown
        if (sb.getValue() + sb.getVisibleAmount() / 2 > midway) {
            // looking at future
            workbench.setTimeMode(MultiGraphWorkbench.TIME_SRC);

            /*
                                    if (timeExpand_enabled) {
                                            // get bottom edge of adjustment window-
                                            int visible = sb.getLabel() + sb.getVisibleAmount() - midway; // pixel amount of future shown
                                            visible /= editor.getVGap();
                                            visible++; // this so there is always just one more timestep beyond what is visible

                                            //System.out.println("F " + visible);
                                            // only try to mess with it if the user is scrolling way into the future
                                            if (visible >= editor.getModelGraph().getMaxLagAllowable())
                                                    editor.setFutureSteps(visible);
                                    }
                                     */
        }
        else {
            // looking at past
            workbench.setTimeMode(MultiGraphWorkbench.TIME_DEST);
            /*
                                    if (timeExpand_enabled) {
                                            // get bottom edge of adjustment window-
                                            int visible = midway - sb.getLabel(); // pixel amount of future shown
                                            visible /= editor.getVGap();
                                            visible++; // this so there is always just one more timestep beyond what is visible

                                            //System.out.println("P " + visible);
                                            // only try to mess with it if the user is scrolling way into the future
                                            if (visible >= editor.getModelGraph().getMaxLagAllowable())
                                                    editor.setPastSteps(visible);
                                    }
                                     */

        }
    }

    /*
	 * method called by scrollbar when it is changed
	 */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar sb = (JScrollBar) e.getSource();
        evaluateTimeMode(sb);

    }
}


