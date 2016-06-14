javaaddpath('Z:\CODE\not_my_code\colt\lib\colt.jar');
javaaddpath('Z:\CODE\not_my_code\tetrad-4.3.9-6\build\tetrad\classes');


import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.util.*
import edu.cmu.tetrad.search.indtest.*


array.data = rand(100, 3);
array.colheaders =  {'x', 'y', 'z'};

[rows,cols] = size(array.data);
list = LinkedList();
for i=1:cols
x = ContinuousVariable(array.colheaders{1, i});
list.add(x);
end
dataSet = ColtDataSet(rows,list);
for i=0:(rows-1)
for j=0:(cols-1)
dataSet.setDouble(i,j,array.data((i+1),(j+1)));
end
end
a =0.05;
independenceTest = IndTestFisherZ(dataSet,a);

conditioningSet = LinkedList;
conditioningSet.add(list.get(1));

independenceTest.isIndependent(list.get(0), list.get(2), conditioningSet);
pval = independenceTest.getPValue
