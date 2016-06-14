package edu.cmu.tetradapp.model.calculator;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.calculator.expression.*;
import edu.cmu.tetradapp.model.calculator.parser.ExpressionParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a transformation on some dataset.
 *
 * @author Tyler Gibson
 */
public class Transformation {

    /**
     * Don't instantiate.
     */
    private Transformation() {

    }

    //======================== Public Methods ======================//


    /**
     * Transforms the given data using the given representations of transforming
     * equations.
     *
     * @param data      - The data that is being transformed.
     * @param equations - The equations used to transform the data.
     * @throws ParseException - Throws a parse exception if any of the given equations isn't
     *                        "valid".
     */
    public static void transform(DataSet data, String... equations) throws ParseException {
        if (equations.length == 0) {
            return;
        }
        for (String equation : equations) {
            transformEquation(data, equation);
        }
    }

    //======================== Private Methods ============================//

    /**
     * Transforms the given dataset using the given equation.
     */
    private static void transformEquation(DataSet data, String eq) throws ParseException {
        ExpressionParser parser = new ExpressionParser(data.getVariableNames());
        Equation equation = parser.parseEquation(eq);

        addVariableIfRequired(data, equation.getVariable());
        Expression expression = equation.getExpression();
        Node variable = data.getVariable(equation.getVariable());
        if (variable == null) {
            throw new IllegalStateException("Unknown variable " + variable);
        }
        int column = data.getColumn(variable);
        // build the context pairs.
        List<String> contextVars = getContextVariables(expression);
        // now do the transformation row by row.
        DataBackedContext context = new DataBackedContext(data, contextVars);
        int rows = data.getNumRows();
        for (int row = 0; row < rows; row++) {
            // build the context
            context.setRow(row);
            double newValue = expression.evaluate(context);
            data.setDouble(row, column, newValue);
        }
    }


    /**
     * Adds a column for the given varible if required.
     */
    private static void addVariableIfRequired(DataSet data, String var) {
        List<String> nodes = data.getVariableNames();
        if (!nodes.contains(var)) {
            data.addVariable(new ContinuousVariable(var));
        }
    }


    /**
     * Returns the variables used in the expression.
     */
    private static List<String> getContextVariables(Expression exp) {
        List<String> variables = new ArrayList<String>();

        for (Expression sub : exp.getSubExpressions()) {
            if (sub instanceof VariableExpression) {
                variables.add(((VariableExpression) sub).getVariable());
            } else if (!(sub instanceof ConstantExpression)) {
                variables.addAll(getContextVariables(sub));
            }
        }

        return variables;
    }

    //============================== Inner Class ==============================//

    private static class DataBackedContext implements Context {


        /**
         * The data.
         */
        private DataSet data;


        /**
         * Var -> index mapping.
         */
        private Map<String, Integer> indexes = new HashMap<String, Integer>();


        /**
         * The current row.
         */
        private int row;


        public DataBackedContext(DataSet data, List<String> vars){
            this.data = data;
            for(String v : vars){
                Node n = data.getVariable(v);
                indexes.put(v, data.getColumn(n));
            }
        }

        public void setRow(int row){
            this.row = row;
        }

        public double getDoubleValue(String var) {
            Integer i = indexes.get(var);
            if(i != null){
               return data.getDouble(row, i);
            }
            return Double.NaN;
        }

        public Object getValue(String var) {
            Integer i = indexes.get(var);
            if(i != null){
                return data.getObject(row, i);
            }
            return null;
        }
    }




}
