function knowledge = tetradKnowledgeSelectionEdges(isSelected, nVars)
% function knowledge = requiredSelectionEdges(isSelected)
% adds edges X->Sx
knowledge = javaObject('edu.cmu.tetrad.data.Knowledge2');
nVarsTot = nVars+sum(isSelected);
import edu.cmu.tetrad.search.*
selVar = nVars;
for iVar=1:nVars
%   knowledge.addToTier(0, ['X' num2str(iVar)]);
    if isSelected(iVar)
        selVar = selVar+1;
        knowledge.setRequired(['X' num2str(iVar)], ['X' num2str(selVar)]);

        for jVar=1:nVarsTot
            if iVar==jVar
                continue;
            else
                knowledge.setForbidden(['X' num2str(jVar)], ['X' num2str(selVar)]);
                knowledge.setForbidden(['X' num2str(selVar)], ['X' num2str(jVar)]);
            end
        end
    end
end

% for iVar=nVars+1:nVarsTot
%     knowledge.addToTier(1,  ['X' num2str(iVar)]);
% end
end