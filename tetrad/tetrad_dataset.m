% discrete data
%javaaddpath('C:\Users\sot16.PITT\Documents\MATLAB\causal_graphs\tetrad\tetrad-gui-6.8.0-20191209.204646-45-launch.jar')
javaaddpath('C:\Users\sot16.PITT\Dropbox\MATLAB\causal_graphs\tetrad\tetrad-gui-6.8.0-SNAPSHOT-launch.jar')
import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.graph.*
% 
[N, nVars] = size(data);
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)], obsDatasetOr.domainCounts(i));
    list.add(var);
end

% make dataset
ds2 = javaObject('edu.cmu.tetrad.data.VerticalIntDataBox',data');
ds = javaObject('edu.cmu.tetrad.data.BoxDataSet',ds2);

bd = javaObject('edu.cmu.tetrad.search.BDeuScore', ds);
ges= javaObject('edu.cmu.tetrad.search.Fges',bd);
pdagt = ges.search;
