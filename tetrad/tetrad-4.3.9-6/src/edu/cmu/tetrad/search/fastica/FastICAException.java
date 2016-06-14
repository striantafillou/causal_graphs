package edu.cmu.tetrad.search.fastica;

/**
 * The <code>FastICAException</code> will be thrown if an error occurs
 * performing the FastIca algorithm.
 *
 * @author Michael Lambertz
 */

public class FastICAException extends Exception {

    private static final long serialVersionUID = 3256727268784290100L;

    public enum Reason {
        NO_MORE_EIGENVALUES
    }

    private Reason reason;


    /**
     * This constructor creates a new <code>FastICAException</code> object.
     */
    public FastICAException(
            Reason reason) {
        this.reason = reason;
    }

    /**
     * Returns the reason for throwing this exception.
     *
     * @return the reason
     */
    public Reason getReason() {
        return (this.reason);
    }

}
