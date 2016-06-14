package edu.cmu.tetrad.regression;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.StatUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests the new regression classes. There is a tabular linear regression
 * model as well as a correlation linear regression model. (Space for more
 * in the future.)
 *
 * @author Joseph Ramsey
 */
public class TestRegression extends TestCase {
    public TestRegression(String name) {
        super(name);
    }

    public void testTabular() {
        DataSet data = loadCarsFile();

        Node accel = data.getVariable("accel");
        Node hp = data.getVariable("hp");
        Node torque = data.getVariable("torque");
        Node weight = data.getVariable("weight");
        Node ratio = data.getVariable("ratio");
        Node disp = data.getVariable("disp");

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(hp);
        regressors.add(torque);
        regressors.add(weight);
        regressors.add(ratio);
        regressors.add(disp);

        Regression regression = new RegressionDataset(data);
        RegressionResult result = regression.regress(accel, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertEquals(6.5, coeffs[0], 0.05);
        assertEquals(-0.02, coeffs[1], 0.005);
        assertEquals(0.007, coeffs[2], 0.001);
        assertEquals(0.0014, coeffs[3], 0.0002);
        assertEquals(-0.318, coeffs[4], 0.001);
        assertEquals(0.0002, coeffs[5], 0.00005);
    }


    public void testCovariance() {
        DataSet data = loadCarsFile();
        CovarianceMatrix covariance = new CovarianceMatrix(data);

        Node accel = data.getVariable("accel");
        Node hp = data.getVariable("hp");
        Node torque = data.getVariable("torque");
        Node weight = data.getVariable("weight");
        Node ratio = data.getVariable("ratio");
        Node disp = data.getVariable("disp");

        Node target = accel;

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(hp);
        regressors.add(torque);
        regressors.add(weight);
        regressors.add(ratio);
        regressors.add(disp);

        DoubleMatrix1D means = calculateMeans(target, data);

        Regression regression = new RegressionCovariance(covariance, means);
        RegressionResult result = regression.regress(accel, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertEquals(6.5, coeffs[0], 0.05);
        assertEquals(-0.02, coeffs[1], 0.005);
        assertEquals(0.007, coeffs[2], 0.001);
        assertEquals(0.0014, coeffs[3], 0.0002);
        assertEquals(-0.318, coeffs[4], 0.001);
        assertEquals(0.0002, coeffs[5], 0.00005);
    }

    /**
     * Without the means.
     */
    public void testCovarianceb() {
        DataSet data = loadCarsFile();
        CovarianceMatrix covariance = new CovarianceMatrix(data);

        Node accel = data.getVariable("accel");
        Node hp = data.getVariable("hp");
        Node torque = data.getVariable("torque");
        Node weight = data.getVariable("weight");
        Node ratio = data.getVariable("ratio");
        Node disp = data.getVariable("disp");

        Node target = accel;

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(hp);
        regressors.add(torque);
        regressors.add(weight);
        regressors.add(ratio);
        regressors.add(disp);

        DoubleMatrix1D means = null; //calculateMeans(target, regressors, data);

        Regression regression = new RegressionCovariance(covariance, means);
        RegressionResult result = regression.regress(accel, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertTrue(Double.isNaN(coeffs[0]));
        assertEquals(-0.02, coeffs[1], 0.005);
        assertEquals(0.007, coeffs[2], 0.001);
        assertEquals(0.0014, coeffs[3], 0.0002);
        assertEquals(-0.318, coeffs[4], 0.001);
        assertEquals(0.0002, coeffs[5], 0.00005);
    }

    public void testCovariancec() {
        DataSet data = loadCarsFile();
        CovarianceMatrix covariance = new CovarianceMatrix(data);

        Node accel = data.getVariable("accel");
        Node hp = data.getVariable("hp");
        Node torque = data.getVariable("torque");
        Node weight = data.getVariable("weight");
        Node ratio = data.getVariable("ratio");
        Node disp = data.getVariable("disp");

        Node target = torque;

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(hp);
        regressors.add(ratio);
        regressors.add(weight);

        DoubleMatrix1D means = null; //calculateMeans(target, regressors, data);

//        RegressionNew regression = new RegressionTabular(data);
        Regression regression = new RegressionCovariance(covariance, means);
        RegressionResult result = regression.regress(target, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertTrue(Double.isNaN(coeffs[0]));
        assertEquals(.8316, coeffs[1], 0.001);
        assertEquals(-54.83, coeffs[2], 0.001);
        assertEquals(0.009949, coeffs[3], 0.001);
    }


    public void testTabular2() {
        DataSet data = loadRegressionDataFile();

        Node x1 = data.getVariable("X1");
        Node x2 = data.getVariable("X2");
        Node x3 = data.getVariable("X3");
        Node x4 = data.getVariable("X4");
        Node x5 = data.getVariable("X5");
        Node x6 = data.getVariable("X6");

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(x2);
        regressors.add(x3);
        regressors.add(x4);
        regressors.add(x5);
        regressors.add(x6);

        Regression regression = new RegressionDataset(data);
        RegressionResult result = regression.regress(x1, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertEquals(-.0524, coeffs[0], 0.05);
        assertEquals(-0.0336, coeffs[1], 0.005);
        assertEquals(0.0889, coeffs[2], 0.001);
        assertEquals(0.0044, coeffs[3], 0.001);
        assertEquals(0.0128, coeffs[4], 0.001);
        assertEquals(-1.4011, coeffs[5], 0.001);

        double[] se = result.getSe();
        assertEquals(.0476, se[0], 0.05);
        assertEquals(0.0294, se[1], 0.005);
        assertEquals(0.0491, se[2], 0.001);
        assertEquals(0.0447, se[3], 0.001);
        assertEquals(0.0326, se[4], 0.001);
        assertEquals(0.0647, se[5], 0.001);
    }


    public void testCovariance2() {
        DataSet data = loadRegressionDataFile();
        CovarianceMatrix covariance = new CovarianceMatrix(data);

        Node x1 = data.getVariable("X1");
        Node x2 = data.getVariable("X2");
        Node x3 = data.getVariable("X3");
        Node x4 = data.getVariable("X4");
        Node x5 = data.getVariable("X5");
        Node x6 = data.getVariable("X6");

        Node target = x1;

        List<Node> regressors = new LinkedList<Node>();
        regressors.add(x2);
        regressors.add(x3);
        regressors.add(x4);
        regressors.add(x5);
        regressors.add(x6);

        DoubleMatrix1D means = calculateMeans(target, data);

        Regression regression = new RegressionCovariance(covariance, means);
        RegressionResult result = regression.regress(x1, regressors);

        System.out.println(result);

        double[] coeffs = result.getCoef();
        assertEquals(-.05, coeffs[0], 0.05);
        assertEquals(-0.03, coeffs[1], 0.005);
        assertEquals(0.0889, coeffs[2], 0.001);
        assertEquals(0.0044, coeffs[3], 0.0002);
        assertEquals(0.0128, coeffs[4], 0.001);
        assertEquals(-1.4011, coeffs[5], 0.00005);

        double[] se = result.getSe();
        assertTrue(Double.isNaN(se[0]));
        assertEquals(0.0294, se[1], 0.005);
        assertEquals(0.0491, se[2], 0.001);
        assertEquals(0.0447, se[3], 0.001);
        assertEquals(0.0326, se[4], 0.001);
        assertEquals(0.0647, se[5], 0.001);
    }

    private DoubleMatrix1D calculateMeans(Node target, DataSet data) {
        DoubleMatrix1D means = new DenseDoubleMatrix1D(6);

        for (int i = 0; i < data.getNumColumns(); i++) {
            means.set(i, StatUtils.mean(getArray(data, data.getVariable(i))));
        }

        return means;
    }

    private double[] getArray(DataSet data, Node node) {
        return data.getDoubleData().viewColumn(data.getColumn(node)).toArray();
    }

    private char[] fileToCharArray(File file) {
        try {
            FileReader reader = new FileReader(file);
            CharArrayWriter writer = new CharArrayWriter();
            int c;

            while ((c = reader.read()) != -1) {
                writer.write(c);
            }

            return writer.toCharArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSet loadCarsFile() {
        File file = new File("test_data/cars.dat");
        char[] chars = fileToCharArray(file);

        DataReader reader = new DataReader();
        reader.setDelimiter(DelimiterType.WHITESPACE);

        DataSet data = reader.parseTabular(chars);
        return data;
    }

    private DataSet loadRegressionDataFile() {
        File file = new File("test_data/regressiondata.dat");
        char[] chars = fileToCharArray(file);

        DataReader reader = new DataReader();
        reader.setDelimiter(DelimiterType.WHITESPACE);

        DataSet data = reader.parseTabular(chars);
        return data;
    }

    public static Test suite() {
        return new TestSuite(TestRegression.class);
    }
}
