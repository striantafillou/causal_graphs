function pval = tetradFisherZ(x, y, condset, dataset)
% Calculates the fisher p-value using tetrad code
% These must be imported for this code to work
% javaaddpath('Z:\CODE\not_my_code\colt\lib\colt.jar');
% javaaddpath('Z:\CODE\not_my_code\tetrad-4.3.9-6\build\tetrad\classes');
% 
% 
import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.util.*
import edu.cmu.tetrad.search.indtest.*

[rows,cols] = size(dataset.data);
list = LinkedList();
for i=1:cols
var = ContinuousVariable(dataset.headers{1, i});
list.add(var);
end
dataSet = ColtDataSet(rows,list);
for i=0:(rows-1)
    for j=0:(cols-1)
    dataSet.setDouble(i,j,dataset.data((i+1),(j+1)));
    end
end
independenceTest = IndTestFisherZ(dataSet, 0.05);

conditioningSet = LinkedList;
for iVar =1:length(conditioningSet)    
    conditioningSet.add(list.get(condset(iVar)-1));
end

independenceTest.isIndependent(list.get(x-1), list.get(y-1), conditioningSet);
pval = independenceTest.getPValue;
end