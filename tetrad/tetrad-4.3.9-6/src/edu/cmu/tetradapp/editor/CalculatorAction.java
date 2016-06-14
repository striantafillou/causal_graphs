package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.calculator.Transformation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Action that lauches the calculator editor.
 *
 * @author Tyler Gibson
 */
public class CalculatorAction extends AbstractAction {


    /**
     * The data the calculator is working on.
     */
    private DataWrapper wrapper;


    /**
     * The data editor, may be null.
     */
    private DataEditor editor;

    /**
     * Constructs the calculator action given the data wrapper to operate on.
     */
    public CalculatorAction(DataWrapper wrapper) {
        super("Calculator ...");
        if(wrapper == null){
            throw new NullPointerException("DataWrapper was null.");
        }
        this.wrapper = wrapper;
    }


    /**
     * Constructs the calculator given the data editor its attached to.
     */
    public CalculatorAction(DataEditor editor){
        this(editor.getDataWrapper());
        this.editor = editor;

    }


    /**
     * Launches the calculator editoir.
     */
    public void actionPerformed(ActionEvent e) {
        CalculatorEditor editor = new CalculatorEditor();
        editor.setParentModels(new Object[]{wrapper});
        editor.setup();

        if(editor.finalizeEdit()) {
            List<String> equations = new ArrayList<String>();
            String _displayEquations = Preferences.userRoot().get("calculator_equations", "");
            String[] displayEquations = _displayEquations.split(",");

            for (String equation : displayEquations) {
                if (equation.contains("$")) {
                    for (Node node : editor.getDataSet().getVariables()) {
                        equations.add(equation.replace("$", node.getName()));
                    }
                }
                else {
                    equations.add(equation);
                }
            }

            String[] eqs = equations.toArray(new String[0]);
            try {
                Transformation.transform(editor.getDataSet(), eqs);
            } catch (ParseException e1) {
                throw new IllegalStateException("Parse error while applying equations to dataset.");
            }
            if(this.editor != null){
                this.editor.repaint();
            }
        }
    }
}
