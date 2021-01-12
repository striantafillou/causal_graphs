function [possibleDescendants, descendantPaths] = getPossibleDescendingPathsMaxLength(startNode,targetNodes, maxPathLength, pag)

%An algorithm that reurns all the possiblyDescendantPaths from startNode to
%any of the targetNodes, empty cell if no such path exists
%        
%Each path contains startNode and the corresponding endNode member of
%targetNodes
%          
%A directed cycle is persumed impossible, so if the same node is 
%encountered twice in a path, the algorithm stops searching the path 
%
%descendantPaths = findPossibleDescendantPaths(pag,startNode,targetNodes)
%Input:     pag:                the pag
%           startNode:          the node whose possibledescendants we ara
%                               looking for
%           targetNodes:        the set in which we are looking for
%                               possible Descendants
%          
%Output:    descendantPaths     a cell containing all the descendant
%                               paths from the startNode to any node
%                               in targetNodes
%           possibleDescendants a matrix containing the subset of
%                               targetNodes that are possibleDescendants of
%                               startNode
%@sofia

nVars = length(path);
if maxPathLength ==-1;
    maxPathLength =nVars;
end
descendantPaths = zeros(1, size(pag, 1));
possibleDescendants = [];
if isempty(targetNodes)
  return;
end
   
pathCount = 1;
getPossibleChildren(pag,startNode,targetNodes,1,startNode, maxPathLength);


    function null = getPossibleChildren(pag,U,targetNodes,i,curPath, maxPathLength)
        if i>=maxPathLength;
            return;
        end
        null = [];      
%         fprintf('Now entering function getPossible Children for node %d, i = %d\n',U,i);
%         fprintf('-----------------------------------------------------------------\n');   
        %The Children of the current node must fall into current node with
        %a tail or a circle and the current node must fall into them
        %with an arrowhead or a circle
        nodes = pag(:,U);
        candidateChildren = find(nodes==1|nodes==3);
       
        nodes = pag(U,:);
        moreCandidateChildren = find(nodes==2|nodes==1);
        CurrentPossibleChildren = intersect(candidateChildren,moreCandidateChildren);
%         fprintf('Candidate Children of  %d are [%s]\n',U,num2str(CurrentPossibleChildren));
        %if there are Possible Children
        if ~isempty(CurrentPossibleChildren)
            for k = 1:length(CurrentPossibleChildren)
                V=CurrentPossibleChildren(k);
                %that have not been visited again in this path
                if ismember(V, curPath)
                
                %if we have not reached a member of the targetNodes,
                %proceed
                elseif ~ismember(V, targetNodes)
%                 fprintf('The path [%s] is a legal path, adding node %d to reachable nodes\n',num2str([U V]),V);
%                 fprintf('\n');
                
                getPossibleChildren(pag,V,targetNodes,i+1,[curPath V], maxPathLength);
                %else report the path and return
                else
                    descendantPaths(pathCount, 1:length(curPath)+1) = [curPath V];
                    pathCount = pathCount + 1;
                    possibleDescendants = unique([possibleDescendants, V]);
                end
            end
        end
    end

end
 