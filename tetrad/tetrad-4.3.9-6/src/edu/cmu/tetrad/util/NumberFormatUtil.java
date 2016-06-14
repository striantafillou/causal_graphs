package edu.cmu.tetrad.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

/**
 * Provides an application-wide "memory" of the number format to be used.
 *
 * @author Joseph Ramsey
 */
public class NumberFormatUtil {
    private static final NumberFormatUtil INSTANCE = new NumberFormatUtil();
    private NumberFormat nf = new DecimalFormat(Preferences.userRoot()
            .get("numberFormat", "0.0000"));

    private NumberFormatUtil() {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return Ibid.
     */
    public static NumberFormatUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the number format, <code>nf</code>.
     *
     * @param nf Ibid.
     * @throws NullPointerException if nf is null.
     */
    public void setNumberFormat(NumberFormat nf) {
        if (nf == null) {
            throw new NullPointerException();
        }

        this.nf = nf;
    }

    /**
     * Returns the stored number format.
     *
     * @return Ibid.
     */
    public NumberFormat getNumberFormat() {
        return nf;
    }
}
