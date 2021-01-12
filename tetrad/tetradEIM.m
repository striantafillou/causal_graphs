function [eIM, bPM, list] = tetradEIM(dag, nodes, domainCounts)

nVars = size(dag,1);

import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.bayes.*

% make list
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)], domainCounts(i));
    list.add(var);
end
tGraph=dag2tetrad(dag,list, nVars);
bPM = javaObject('edu.cmu.tetrad.bayes.BayesPm', tGraph);
ds2 = javaObject('edu.cmu.tetrad.data.VerticalIntDataBox',zeros(nVars, 1));
dsj = javaObject('edu.cmu.tetrad.data.BoxDataSet',ds2, list);
%bIM2 = javaObject('edu.cmu.tetrad.bayes.DirichletBayesIm', bPM,
%double(1));no appropriate constructor?
% 
bIM = javaObject('edu.cmu.tetrad.bayes.EmBayesEstimator', bPM, dsj);
eIM = bIM.getEstimatedIm();

for iVar=1:nVars
    curParents = reshape(nodes{iVar}.parents, 1, []);
    if isempty(curParents)
        for iD = 0:domainCounts(iVar)-1
            eIM.setProbability(iVar-1, 0,iD, nodes{iVar}.cpt(iD+1))
        end
    else
        nConfigs = prod(domainCounts(curParents));
        nParents= length(nodes{iVar}.parents);
        configs = allConfigurations(nParents, domainCounts(curParents))-1;
        tmp_cpt = reshape(nodes{iVar}.cpt,domainCounts(iVar), []);
        for iConfig=1:nConfigs
            curConfig =configs(iConfig,:);
            rowIndex =eIM.getRowIndex(iVar-1, curConfig);
            for iD = 0:domainCounts(iVar)-1
                eIM.setProbability(iVar-1, rowIndex,iD, tmp_cpt(iD+1, iConfig))
            end
        end  
    end
end

end
