function mConnectingPaths = findmconpathsbetweenxandy(mcg,nodes,conditionToNodes,Y)

 %mConnectingPaths = findmconnectingpathsbetweenxandy(pag,nodes,conditionToNodes,dnc,Y)
%Input:     mcg:                the mcg
%           nodes:              beginning nodes
%           conditionToNodes:   the nodes condition to which will look for
%                               m-connections
%           dnc:                the set of definite non colliders for this
%                               pag
%           Y:                  The target node
%Output:    mConnectingPaths    a cell containing all the m-connecting
%                               paths from the initial node to any node
%                               by conditionToNodes




%global edges;
global inConditionSet;
global descendents;
global newGraph;
global allEdges;
global pathCount;


%edges=zeros(size(pag));
nVars=size(mcg,1);

inConditionSet=zeros(nVars,1)';
inConditionSet(conditionToNodes)=1;

descendents=zeros(nVars,1)';
descendents(findancestors(mcg),conditionToNodes)=1;

newGraph = zeros(size(mcg));
newGraph(~~mcg)=1;

nbrs = find(newGraph(nodes,:));
%edges(nodes,nbrs)=1;
nbrs = setdiff(nbrs,Y);

reachable = [];
[allEdgesRows,allEdgesCols] = find(newGraph);
allEdges = [allEdgesRows,allEdgesCols] ;


mConnectingPaths = {};
mConnectingPaths = zeros(nVars^2, nVars);
pathCount = 0;

for l =1:length(nbrs)
    null = testMConnections2(mcg,nodes,nbrs(l),conditionToNodes,1,[],[nodes nbrs(l)],Y,nodes);
end

    function null=testMConnections2(pag,U,V,conditionToNodes,i,visitedTriples,curPath,Y, X)
        null = [];
        %fprintf('Now Procceeding through edge %d-%d for i= %d\n',U,V,i)

        reachable = [V reachable];

        %Find all nodes W s.t U->V , V->W
        currentNbrs = allEdgesCols(allEdgesRows==V);
        %For all W
        for k = 1:length(currentNbrs)
            W = currentNbrs(k);
            if W~=U && (isempty(visitedTriples)||~ismember([U V W],visitedTriples,'rows')) && ~ismember(W,curPath) && W~= X
                 if (~iscollider(mcg, U, V,W) && ~inConditionSet(V)) || iscollider(mcg, U, V, X) && descendents(V);
                    if W~=Y
                     %fprintf('The path [%s] is a legal path, adding node %d to reachable nodes\n',num2str([U V W]),W);
                     visitedTriples = [visitedTriples; U V W];
                     testMConnections2(pag,V,W,conditionToNodes,i+1,visitedTriples,[curPath W],Y, X);
                    elseif W == Y
                        pathCount = pathCount + 1;
                        mConnectingPaths(pathCount, 1:length(curPath)) = curPath;%deal(1:length(curPath));%{pathCount} = [curPath Y];
                        disp(mConnectingPaths(pathCount, :))
                    %reachable = [];
                    end
                end

            end
        end
    end
 mConnectingPaths = mConnectingPaths(1:pathCount, :);
end



function isCol=iscollider(mcg, U, V, W)
    if((mcg(U,V)==2||mcg(U,V)==4) && (mcg(W, V)==2||mcg(W, V)==4))
        isCol=1;
    else
        isCol=0;
    end
end
