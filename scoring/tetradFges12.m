function [adag, dsj]= tetradFges12(data, domainCounts, varargin)
% only Y: no edges among pretreatmetn variables, adag(i, 2) is true only if
% i\in Mb(Y).
[pretreat, onlyY] = process_options(varargin, 'pretreat', false, 'onlyY', false);
nVars = size(data, 2);

list= tetradList(nVars, domainCounts);
% make tetrad data set
ds2 = javaObject('edu.cmu.tetrad.data.VerticalIntDataBox',data');
dsj = javaObject('edu.cmu.tetrad.data.BoxDataSet',ds2, list);
% make algo
bd = javaObject('edu.cmu.tetrad.search.BDeuScore', dsj);%bd.setStructurePrior(10);
ges= javaObject('edu.cmu.tetrad.search.Fges',bd);

if pretreat
    % make knowledge
    knowledge = tetradKnowledgeAddTiers([1 2 zeros(1, nVars-2)]);
    ges.setKnowledge(knowledge);
elseif onlyY
    knowledge = tetradKnowledgeAddTiers([0 1 zeros(1, nVars-2)]);
    knowledge.setTierForbiddenWithin(0, true);
else
    knowledge = javaObject('edu.cmu.tetrad.data.Knowledge2');
end

knowledge.setRequired('X1', 'X2');
%ges.setKnowledge(knowledge);
ges.setKnowledge(knowledge);


% run fges
pdagt = ges.search;
pdag = tetradGraphtoAdjMat(pdagt, nVars);
% get dag and make sure it is connected
adag = pdag_to_dag(pdag);
if ~onlyY
adag = makeConnected(adag);
end
end

