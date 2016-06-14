package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetradapp.model.DataWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tyler was lazy and didn't document this....
 *
 * @author Tyler Gibson
 */
public class SplitCasesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Constructs the wrapper given some data and the params.
     *
     * @param data
     * @param params
     */
    public SplitCasesWrapper(DataWrapper data, SplitCasesParams params) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }
        if (params == null) {
            throw new NullPointerException("The given parameters must not be null");
        }
        DataSet originalData = (DataSet) data.getSelectedDataModel();
        DataModel model = createSplits(originalData, params);
        this.setDataModel(model);
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        SplitCasesParams params = new SplitCasesParams();
        params.setNumSplits(1);
        params.setSpec(new SplitCasesSpec(1, new int[1], Collections.singletonList("1")));
        return new SplitCasesWrapper(DataWrapper.serializableInstance(),
                params);
    }

    //========================= Private Methods ===================================//


    /**
     * Returns the splitNames selected by the editor.
     */
    public static DataModel createSplits(DataSet dataSet, SplitCasesParams params) {
        List<Integer> indices = new ArrayList<Integer>(dataSet.getNumRows());
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            indices.add(i);
        }

        if (params.isDataShuffled()) {
            Collections.shuffle(indices);
        }

        SplitCasesSpec spec = params.getSpec();
        int numSplits = params.getNumSplits();
        int sampleSize = spec.getSampleSize();
        int[] breakpoints = spec.getBreakpoints();
        List<String> splitNames = spec.getSplitNames();

        int[] _breakpoints = new int[breakpoints.length + 2];
        _breakpoints[0] = 0;
        _breakpoints[_breakpoints.length - 1] = sampleSize;
        System.arraycopy(breakpoints, 0, _breakpoints, 1, breakpoints.length);

        DataModelList list = new DataModelList();
        int ncols = dataSet.getNumColumns();
        for (int n = 0; n < numSplits; n++) {
            int _sampleSize = _breakpoints[n + 1] - _breakpoints[n];

            DataSet _data =
                    new ColtDataSet(_sampleSize, dataSet.getVariables());
            _data.setName(splitNames.get(n));

            for (int i = 0; i < _sampleSize; i++) {
                int oldCase = indices.get(i + _breakpoints[n]);

                for (int j = 0; j < ncols; j++) {
                    _data.setObject(i, j, dataSet.getObject(oldCase, j));
                }
            }

            list.add(_data);
        }

        return list;
    }


}
