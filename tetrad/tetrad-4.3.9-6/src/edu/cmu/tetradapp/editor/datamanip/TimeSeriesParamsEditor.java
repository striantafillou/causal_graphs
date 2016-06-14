package edu.cmu.tetradapp.editor.datamanip;

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.editor.ParameterEditor;
import edu.cmu.tetradapp.model.datamanip.TimeSeriesParams;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author Tyler Gibson
 */
public class TimeSeriesParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The params.
     */
    private TimeSeriesParams params;


    /**
     * Empty constructor that does nothing, call <code>setup()</code> to build panel.
     */
    public TimeSeriesParamsEditor() {
        super(new BorderLayout());
    }


    /**
     * Sets the parameters.
     *
     * @param params
     */
    public void setParams(Params params) {
        this.params = (TimeSeriesParams) params;
    }

    /**
     * Does nothing
     *
     * @param parentModels
     */
    public void setParentModels(Object[] parentModels) {

    }

    /**
     * Builds the panel.
     */
    public void setup() {
        SpinnerNumberModel model = new SpinnerNumberModel(this.params.getNumOfTimeLags(),
                0, Integer.MAX_VALUE, 1);
        JSpinner jSpinner = new JSpinner(model);
        jSpinner.setPreferredSize(jSpinner.getPreferredSize());

        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SpinnerNumberModel model = (SpinnerNumberModel) e.getSource();
                params.setNumOfTimeLags(model.getNumber().intValue());
            }
        });

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Number of time lags: "));
        b1.add(Box.createHorizontalGlue());
        b1.add(Box.createHorizontalStrut(15));
        b1.add(jSpinner);
        b1.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(b1, BorderLayout.CENTER);
    }

    /**
     * Returns true.
     *
     * @return true
     */
    public boolean mustBeShown() {
        return true;
    }
}
