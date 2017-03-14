function estimator= estimate_sem(covariance, sample_size, graph_type)
import edu.cmu.tetrad.*
import java.util.*
%import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.search.*


array = javaArray('edu.cmu.tetrad.graph.GraphNode', 3);

array(1) = GraphNode('X');
array(2) = GraphNode('Y');
array(3) = GraphNode('W');

list  = Arrays.asList(array);
tGraph = EdgeListGraph(list);   

doubleMatrix = javaObject('cern.colt.matrix.impl.DenseDoubleMatrix2D', 3,3);
doubleMatrix.assign(covariance(1:3,1:3));
 
if graph_type == 1
    tGraph.addDirectedEdge(list.get(0), list.get(1));
    tGraph.addDirectedEdge(list.get(1), list.get(2));
elseif graph_type == 2
    tGraph.addDirectedEdge(list.get(1), list.get(0));
    tGraph.addDirectedEdge(list.get(2), list.get(1));
elseif graph_type == 3    
    tGraph.addDirectedEdge(list.get(1), list.get(0));
    tGraph.addDirectedEdge(list.get(1), list.get(2));
end


semPm = javaObject('edu.cmu.tetrad.sem.SemPm', tGraph);

ja = javaArray('edu.cmu.tetrad.data.ContinuousVariable',3);

 
ja(1) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'X');
ja(2) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'Y');
ja(3) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'W');

varList  = Arrays.asList(ja);

%variables = semPm.getVariableNodes;
covarianceMatrix = javaObject('edu.cmu.tetrad.data.CovarianceMatrix',varList, doubleMatrix, sample_size);

estimator = javaObject('edu.cmu.tetrad.sem.SemEstimator', covarianceMatrix,semPm);
estimator.estimate;
end