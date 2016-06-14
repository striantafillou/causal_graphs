package edu.cmu.tetrad.search.fastica;

/**
 * The contrast functions used to estimate negentropy. One of this functions has
 * to be passed to the FastIca algorithm.
 *
 * @author Michael Lambertz
 */

public interface ContrastFunction {

    /**
     * Computes the function value at position <code>x</code>.
     *
     * @param x the desired position
     * @return the function value
     */
    public double function(double x);

    /**
     * Computes the value of the function's derivative at position <code>x</code>.
     *
     * @param x the desired position
     * @return the function value
     */
    public double derivative(double x);

}
