package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Graph;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 27, 2006 Time: 10:15:26 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GraphSearch {
    Graph search();
    long getElapsedTime();
}
