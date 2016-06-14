package edu.cmu.tetrad.search.fastica.swing;

import edu.cmu.tetrad.search.fastica.EigenValueFilter;
import edu.cmu.tetrad.search.fastica.math.Matrix;
import edu.cmu.tetrad.search.fastica.math.Vector;

import javax.swing.*;

/**
 * The <code>DialogEVFilter</code> is an eigenvalue filter, which asks the user
 * to choose the requested eigenvalues.
 *
 * @author Michael Lambertz
 */

public class DialogEVFilter implements EigenValueFilter {

    private JFrame owner;
    private double[] eigenValues;
    private double[][] eigenVectors;

    public DialogEVFilter(
            JFrame owner) {
        this.owner = owner;
    }

    public void passEigenValues(double[] eigenValues, double[][] eigenVectors) {
        EigenValueDialog eigenDl =
                new EigenValueDialog(owner, "Please select the eigenvalues...", eigenValues);
        if (eigenDl.open()) {
            boolean[] accept = eigenDl.getAcceptanceList();
            int l = accept.length;
            int n = 0;
            for (int i = 0; i < l; ++i)
                if (accept[i])
                    n++;
            int m = Matrix.getNumOfRows(eigenVectors);
            this.eigenValues = Vector.newVector(n);
            this.eigenVectors = Matrix.newMatrix(m, n);
            int ii = 0;
            for (int i = 0; i < l; ++i) {
                if (accept[i]) {
                    this.eigenValues[ii] = eigenValues[i];
                    for (int j = 0; j < m; ++j)
                        this.eigenVectors[j][ii] = eigenVectors[j][i];
                    ii++;
                }
            }
        }
        else {
            this.eigenValues = eigenValues;
            this.eigenVectors = eigenVectors;
        }
    }

    public double[] getEigenValues() {
        return (eigenValues);
    }

    public double[][] getEigenVectors() {
        return (eigenVectors);
    }

}
