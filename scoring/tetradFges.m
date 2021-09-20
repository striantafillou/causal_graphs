function [adag]= tetradFges12(obsDataset, varargin)
pretreat = process_options(varargin, 'pretreat', false);
nVars = size(obsDataset.data, 2);
domainCounts = obsDataset.domainCounts;

list= tetradList(nVars, domainCounts);
% make tetrad data set
ds2 = javaObject('edu.cmu.tetrad.data.VerticalIntDataBox',obsDataset.data');
dsj = javaObject('edu.cmu.tetrad.data.BoxDataSet',ds2, list);
% make algo
bd = javaObject('edu.cmu.tetrad.search.BDeuScore', dsj);%bd.setStructurePrior(10);
ges= javaObject('edu.cmu.tetrad.search.Fges',bd);
knowledge.setRequired('X1', 'X2');

if pretreat
    % make knowledge
    knowledge = tetradKnowledgeAddTiers([1 2 zeros(1, nVars-2)]);
    ges.setKnowledge(knowledge);
end

% run fges
pdagt = ges.search;
pdag = tetradGraphtoAdjMat(pdagt, nVars);
% get dag and make sure it is connected
adag = pdag_to_dag(pdag);
adag = makeConnected(adag);
end