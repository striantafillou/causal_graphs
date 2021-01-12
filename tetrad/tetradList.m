function list= tetradList(nVars, domainCounts)
% function  list= getTetradList(nVars, domainCounts)
% get tetrad list with variables with domainCounts
import java.util.*
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)], domainCounts(i));
    list.add(var);
end
end