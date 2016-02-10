function pval = tetradLogisticRegression(x, y, condset, dataset)
% Calculates the logistic regression p-value using tetrad code
% variables must be either continuous or binary
% These must be imported for this code to work
% javaaddpath('Z:\CODE\not_my_code\colt\lib\colt.jar');
% javaaddpath('Z:\CODE\not_my_code\tetradlib-4.3.10-7\tetrad\tetrad.jar')
% 
import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.util.*

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
independenceTest = IndTestLogisticRegression(dataSet, 0.05);

conditioningSet = LinkedList;
if ~isempty(condset)
    for iVar =1:length(conditioningSet)    
        conditioningSet.add(list.get(condset(iVar)-1));
    end
end
independenceTest.isIndependent(list.get(x-1), list.get(y-1), conditioningSet);
pval = independenceTest.getPValue;
end



