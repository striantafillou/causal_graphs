package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;

public interface CyclicDiscoveryMethod {

	String getName();	//name of the cyclic discovery method	
	GraphWithParameters run(DataSet dataSet);
}
