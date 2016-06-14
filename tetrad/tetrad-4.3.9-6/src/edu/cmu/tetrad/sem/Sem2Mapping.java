package edu.cmu.tetrad.sem;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Apr 19, 2007 Time: 5:37:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Sem2Mapping {
    double getValue();

    void setValue(double value);

    Parameter getParameter();
}
