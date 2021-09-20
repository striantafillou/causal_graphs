function [mb, pdag, dsj]= tetradFgesMB(y, obsDataset)
    % returns mb(Y)
    nVars = size(obsDataset.data, 2);
    domainCounts = obsDataset.domainCounts;

    list= tetradList(nVars, domainCounts);
    % make tetrad data set
    ds2 = javaObject('edu.cmu.tetrad.data.VerticalIntDataBox',obsDataset.data');
    dsj = javaObject('edu.cmu.tetrad.data.BoxDataSet',ds2, list);
    % make algo
    bd = javaObject('edu.cmu.tetrad.search.BDeuScore', dsj);%bd.setStructurePrior(10);
    ges= javaObject('edu.cmu.tetrad.search.Fges',bd);

    tiers =[zeros(1, nVars)]; tiers(y) =1;
    knowledge = tetradKnowledgeAddTiers(tiers);
    knowledge.setTierForbiddenWithin(0, true);


    %ges.setKnowledge(knowledge);
    ges.setKnowledge(knowledge);


    % run fges
    pdagt = ges.search;
    pdag = tetradGraphtoAdjMat(pdagt, nVars);
    mb = find(pdag(:, y));
end


