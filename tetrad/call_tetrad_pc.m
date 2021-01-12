javaaddpath('C:\Users\SOT16\Documents\MATLAB\causal_graphs\tetrad\tetrad-gui-6.4.0-launch.jar')
import edu.cmu.tetrad.* 
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.graph.*


[nSamples,nVars] = size(obsDataset.data);
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)]);
    %ContinuousVariable(['X' num2str(i)]);
    list.add(var);
end
ds = javaObject('edu.cmu.tetrad.data.ColtDataSet', nSamples,list);
for i=0:(nSamples-1)
    for j=0:(nVars-1)
        ds.setInt(i,j,obsDataset.data((i+1),(j+1)));
    end
end

array = javaArray('edu.cmu.tetrad.graph.GraphNode', nVars);

for i=1:nVars
    array(i) =GraphNode(['X', num2str(i)]);
end

list  = Arrays.asList(array);
tGraph = EdgeListGraph(list);   

for x=1:nVars-1
    for y =x+1:nVars
        if dag(x,y) == 1 
            tGraph.addDirectedEdge(list.get(x-1), list.get(y-1));
        elseif dag(y, x)==1
             tGraph.addDirectedEdge(list.get(y-1), list.get(x-1));
        end
    end
end

it = IndTestChiSquare(ds,0.05);
knowledge = javaObject('edu.cmu.tetrad.data.Knowledge');
knowledge.addToTier(0,'X1')
knowledge.addToTier(1,'X2')

pc = javaObject('edu.cmu.tetrad.search.Pc', it);
pdagt = pc.search;
pdag = tetradGraphtoAdjMat(pdagt, nVars);

%%
javaaddpath('C:\Users\SOT16\Documents\MATLAB\causal_graphs\tetrad\tetrad-5.0.0-3.jar')
import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.graph.*


[nSamples,nVars] = size(obsDataset.data);
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)]);
    %ContinuousVariable(['X' num2str(i)]);
    list.add(var);
end
ds = javaObject('edu.cmu.tetrad.data.ColtDataSet', nSamples,list);
for i=0:(nSamples-1)
    for j=0:(nVars-1)
        ds.setInt(i,j,obsDataset.data((i+1),(j+1)));
    end
end


it = IndTestChiSquare(ds,0.05);
knowledge.addToTier(0,'X1')
knowledge.addToTier(1,'X2')

pc = javaObject('edu.cmu.tetrad.search.Pc', it);
%%



ja = javaArray('edu.cmu.tetrad.data.ContinuousVariable',3);

 
ja(1) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'X');
ja(2) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'Y');
ja(3) = javaObject('edu.cmu.tetrad.data.ContinuousVariable', 'W');

varList  = Arrays.asList(ja);

%variables = semPm.getVariableNodes;
covarianceMatrix = javaObject('edu.cmu.tetrad.data.CovarianceMatrix',varList, doubleMatrix, sample_size);

estimator = javaObject('edu.cmu.tetrad.sem.SemEstimator', covarianceMatrix,semPm);
estimator.estimate;

 