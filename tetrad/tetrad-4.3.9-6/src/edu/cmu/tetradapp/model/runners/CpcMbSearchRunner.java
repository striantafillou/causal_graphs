package edu.cmu.tetradapp.model.runners;

import edu.cmu.tetrad.search.CpcMb;
import edu.cmu.tetradapp.model.AbstractMBSearchRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MbSearchParams;

/**
 * @author Tyler Gibson
 */
public class CpcMbSearchRunner extends AbstractMBSearchRunner {
    static final long serialVersionUID = 23L;


    public CpcMbSearchRunner(DataWrapper data, MbSearchParams params) {
        super(data.getSelectedDataModel(), params);
    }


    public void execute() throws Exception {
        CpcMb search = new CpcMb(getIndependenceTest(),
                getParams().getDepth());
        this.setSearchResults(search.findMb(this.getParams().getTargetName()));
        this.setSearchName(search.getAlgorithmName());
    }
}
