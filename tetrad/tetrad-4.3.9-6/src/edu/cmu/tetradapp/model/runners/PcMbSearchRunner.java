package edu.cmu.tetradapp.model.runners;

import edu.cmu.tetrad.search.Pcmb;
import edu.cmu.tetradapp.model.AbstractMBSearchRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MbSearchParams;

/**
 * @author Tyler Gibson
 */
public class PcMbSearchRunner extends AbstractMBSearchRunner {
    static final long serialVersionUID = 23L;


    public PcMbSearchRunner(DataWrapper data, MbSearchParams params) {
        super(data.getSelectedDataModel(), params);
    }


    public void execute() throws Exception {
        Pcmb search = new Pcmb(getIndependenceTest(),
                getParams().getDepth());
        this.setSearchResults(search.findMb(this.getParams().getTargetName()));
        this.setSearchName(search.getAlgorithmName());
    }
}
