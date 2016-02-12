function mConnectingPaths = FindMConnectingPathsBetweenXandY(pag,nodes,conditionToNodes,dnc,Y)

%mConnectingPaths=FindMConnectingPathsBetweenXandY(pag,nodes,conditionToNodes,dnc,Y)
%Input:     pag:                the pag
%           nodes:              beginning nodes
%           conditionToNodes:   the nodes condition to which will look for
%                               m-connections
%           dnc:                the set of definite non colliders for this
%                               pag
%           Y:                  The target node
%Output:    mConnectingPaths    a cell containing all the m-connecting
%                               paths from the initial node to any node
%                               by conditionToNodes




global edges;
global inConditionSet;
global descendents;
global newGraph;
global allEdges;
global pathCount;


edges=zeros(size(pag));
numOfVariables=size(pag,1);

inConditionSet=zeros(numOfVariables,1)';
inConditionSet(conditionToNodes)=1;

descendents=zeros(numOfVariables,1)';
descendents(findancestors(pag),conditionToNodes)=1;

newGraph = zeros(size(pag));
newGraph(find(pag))=1;

nbrs = find(newGraph(nodes,:));
edges(nodes,nbrs)=1;
nbrs = setdiff(nbrs,Y);

reachable = [];
[allEdgesRows,allEdgesCols] = find(newGraph);
allEdges = [allEdgesRows,allEdgesCols] ;


mConnectingPaths = {};
pathCount = 1;

for l =1:length(nbrs)
    null = testMConnections2(pag,nodes,nbrs(l),conditionToNodes,dnc,1,[],[nodes nbrs(l)],Y,nodes);
end

    function null=testMConnections2(pag,U,V,conditionToNodes,dnc,i,visitedTriples,curPath,Y, X)
        null = [];
        %fprintf('Now Procceeding through edge %d-%d for i= %d\n',U,V,i)

        reachable = [V reachable];

        %Find all nodes W s.t U->V , V->W
        currentNbrs = allEdgesCols(find(allEdgesRows==V));
        %For all W
        for k = 1:length(currentNbrs)
            W = currentNbrs(k);
            if W~=U && (isempty(visitedTriples)||~ismember([U V W],visitedTriples,'rows')) && ~ismember(W,curPath) && W~= X
                if ((isnotheadtohead(pag,[U V W],dnc)&&inConditionSet(V)==0)||(isheadtohead(pag,[U V W])&&descendents(V)==1)...
                        || (~isnotheadtohead(pag,[U V W],dnc)&&~isheadtohead(pag,[U V W])))
                    if W~=Y
                     %fprintf('The path [%s] is a legal path, adding node %d to reachable nodes\n',num2str([U V W]),W);
                     visitedTriples = [visitedTriples; U V W];
                     testMConnections2(pag,V,W,conditionToNodes,dnc,i+1,visitedTriples,[curPath W],Y, X);
                    elseif W == Y
                     mConnectingPaths{pathCount} = [curPath Y];
                     pathCount = pathCount + 1;
                    %reachable = [];
                    end
                end

            end
        end
    end

end




function [boolean]=isheadtohead(pag,threenodes)
if(pag(threenodes(1),threenodes(2))==2 && pag(threenodes(3),threenodes(2))==2)
    boolean=1;
else
    boolean=0;
end
end


function [boolean]=isnotheadtohead(pag,threenodes,definite_non_colliders)
if((pag(threenodes(1),threenodes(2))==3||pag(threenodes(3),threenodes(2))==3)||ismember(threenodes,definite_non_colliders,'rows')...
        ||ismember([threenodes(3),threenodes(2),threenodes(1)],definite_non_colliders,'rows'))
    boolean=1;
else
    boolean=0;
end
end