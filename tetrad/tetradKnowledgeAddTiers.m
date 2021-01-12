function knowledge = tetradKnowledgeAddTiers(tiers)
% function knowledge =  tetradKnowledgeAddTiers(tiers)
% adds variable i to tier tiers(i)
knowledge = javaObject('edu.cmu.tetrad.data.Knowledge2');
nVars = size(tiers, 2);
for iVar=1:nVars
   knowledge.addToTier(tiers(iVar), ['X' num2str(iVar)]);
end

% for iVar=nVars+1:nVarsTot
%     knowledge.addToTier(1,  ['X' num2str(iVar)]);
% end
end