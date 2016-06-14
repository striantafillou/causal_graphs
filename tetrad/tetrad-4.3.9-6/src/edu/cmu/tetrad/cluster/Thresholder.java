package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Creates thresholded datasets from given dataset.
 *
 * @author Joseph Ramsey
 */
public class Thresholder {

    /**
     * The data that is being thresholded.
     */
    private DoubleMatrix2D data;

    /**
     * The scores for each of the data points/rows (in order).
     */
    private List<Double> scores;

    /**
     * Constructs a new thresholder for the given tdata.
     */
    public Thresholder(DoubleMatrix2D data) {
        this.data = data;
        this.scores = new ArrayList<Double>(data.rows());

        for (int i = 0; i < data.rows(); i++) {
            this.scores.add(Double.NaN);
        }
    }

    /**
     * Sets the score for the ith row/point to the given score.
     */
    public void setScore(int i, double score) {
        this.scores.set(i, score);
    }

    /**
     * Returns the score for the ith point/row.
     */
    public double getScore(int i) {
        return this.scores.get(i);
    }

    /**
     * Creates a datasets restricted to rows/points whose score is >= the given
     * cutoff, restricted to the specified columns.
     */
    public DoubleMatrix2D getThresholdedData(double cutoff) {
        List<Integer> includedRows = new ArrayList<Integer>();

        for (int i = 0; i < data.rows(); i++) {
            double score = getScore(i);
            if (score > cutoff) {
                includedRows.add(i);
            }
        }

        List<Integer> includedColumns = new ArrayList<Integer>();

        for (int j = 0; j < data.columns(); j++) {
            includedColumns.add(j);
        }

        int[] rows = new int[includedRows.size()];

        for (int i = 0; i < includedRows.size(); i++) {
            rows[i] = includedRows.get(i);
        }

        int[] columns = new int[includedColumns.size()];

        for (int j = 0; j < includedColumns.size(); j++) {
            columns[j] = includedColumns.get(j);
        }

        return data.viewSelection(rows, columns).copy();
    }

    /**
     * Creates a dataset which includes the indicated top fraction of points--
     * that is, it sorts the points low to high according <code>getScore</code>
     * and returns the top <code>topFraction</code> of them.
     */
    public DoubleMatrix2D getTopFractionPoints(double topFraction) {
        List<Integer> _points = new ArrayList<Integer>();
        final Map<Integer, Double> _values = new HashMap<Integer, Double>();

        List<Integer> includedColumns = new ArrayList<Integer>();

        for (int j = 0; j < data.columns(); j++) {
            includedColumns.add(j);
        }

        for (int i = 0; i < data.rows(); i++) {
            DoubleMatrix1D vector = new DenseDoubleMatrix1D(includedColumns.size());

            for (int j = 0; j < includedColumns.size(); j++) {
                vector.set(j, includedColumns.get(j));
            }

            _points.add(i);
            _values.put(i, getScore(i));
        }

        Collections.sort(_points, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                double v1 = _values.get(o1);
                double v2 = _values.get(o2);
                return v1 < v2 ? -1 : (v1 == v2 ? 0 : 1);
            }
        });

        List<Integer> points = new ArrayList<Integer>();

        for (int i = (int) ((1.0 - topFraction) * _points.size()); i < _points.size(); i++) {
            points.add(_points.get(i));
        }

        try {
            PrintWriter out = new PrintWriter(new File("/home/jdramsey/singlecolumn.txt"));

            for (int i = 0; i < _points.size(); i++) {
                out.println(_values.get(_points.get(i)));
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DoubleMatrix2D thresholdedData = new DenseDoubleMatrix2D(points.size(), data.columns());

        for (int i = 0; i < points.size(); i++) {
            thresholdedData.viewRow(i).assign(data.viewRow(points.get(i)));
        }

        return thresholdedData;

    }
}
