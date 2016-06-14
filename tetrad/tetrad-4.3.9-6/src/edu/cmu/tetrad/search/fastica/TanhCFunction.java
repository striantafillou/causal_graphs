package edu.cmu.tetrad.search.fastica;

/**
 * The tanh(a * x) function is a good general purpose contrast function.
 *
 * @author Michael Lambertz
 */

public class TanhCFunction implements ContrastFunction {

    private double a;

    public TanhCFunction(double a) {
        this.a = a;
    }

    public double function(double x) {
        return (Math.tanh(a * x));
    }

    public double derivative(double x) {
        double tanha1x = Math.tanh(a * x);
        return (a * (1 - tanha1x * tanha1x));
    }

}
