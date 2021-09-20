function pdag =tetradGraphtoAdjMat(pdagt, nVars)

nodes = pdagt.getNodes;
pdag = zeros(nVars);
for iNode = 1:nVars
    adjNodes = pdagt.getAdjacentNodes(nodes.get(iNode-1)).toString.toCharArray';
    adjNodes = strrep(adjNodes, 'X', '');
    adjNodes = str2double(strsplit(adjNodes(2:end-1), ','));
    adjNodes = adjNodes(adjNodes>iNode);


    parents = pdagt.getParents(nodes.get(iNode-1)).toString.toCharArray';
    parents = strrep(parents, 'X', '');
    parents = str2double(strsplit(parents(2:end-1),','));
    if isnan(parents); parents=[]; end

    pdag(iNode, adjNodes)=1; pdag(adjNodes,iNode)=1;
    pdag(iNode, parents)=0;
end

