package edu.cmu.tetradapp.model.runners;

import edu.cmu.tetrad.search.GrowShrink;
import edu.cmu.tetradapp.model.AbstractMBSearchRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MbSearchParams;

/**
 * Runner for the Grow Shrink mb search.
 *
 * @author Tyler Gibson
 */
public class GrowShrinkRunner extends AbstractMBSearchRunner {
    static final long serialVersionUID = 23L;



    public GrowShrinkRunner(DataWrapper data, MbSearchParams params){
        super(data.getSelectedDataModel(), params);
    }


    /**
     * Executes the grow shrink algorithm.
     */
    public void execute() throws Exception {
        validate();
        GrowShrink search = new GrowShrink(this.getIndependenceTest());
        setSearchResults(search.findMb(this.getParams().getTargetName()));
        setSearchName(search.getAlgorithmName());
    }
}
