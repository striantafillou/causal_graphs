function inducingPaths = getPossibleInducingPathsMaxLength(nodes,Y, smm,  dnc,  isLatent, maxPathLength, screen)
% fuction INDUCINGPATHS = GETPOSSIBLEMINDUCINGPATHS(NODES, Y, SMM, DNC,
%                            SCREEN)
% The algorithm is described in Richard E. Neapolitan's, 'Learning Bayesian
% Networks' p.84
%
% inducingPaths =
% FindPossibleInducingPathsBetweenXandY(smm,nodes,latentNodes,Y, screen)
%Input:     smm:                the smm
%           nodes:              beginning node
%           conditionToNodes:   the nodes condition to which will look for
%                               m-connections
%           Y:                  The target node
%           screen:             1 for screen output, 0 otherwise. 
%Output:    inducingPaths    a cell containing all the m-connecting
%                               paths from the initial node to any node
%                               by conditionToNodes

global pathCount;


% Edge we are trying to substitute cannot be a part of the search
nVars = size(smm,1);
if maxPathLength ==-1
    maxPathLength = nVars-2;
end
inducingPaths = zeros(1, nVars-2);
pathCount = 1;
if smm(nodes, Y)~=0
    inducingPaths(1, 1:2) = [nodes Y];
    pathCount = pathCount+1;
end

smm([nodes, Y], [nodes, Y]) = 0;
edges=zeros(size(smm));
numOfVariables=size(smm,1);

newGraph = zeros(size(smm));
newGraph(find(smm))=1;

nbrs = find(newGraph(nodes,:));
edges(nodes,nbrs)=1;
nbrs = setdiff(nbrs,Y);

reachable = [];
[allEdgesRows,allEdgesCols] = find(newGraph);
allEdges = [allEdgesRows,allEdgesCols] ;

potdir =  possibleDescendantsPag(smm);


for l =1:length(nbrs)
    null = testInductions2(smm,nodes,nbrs(l),1,[],[nodes nbrs(l)],Y,nodes, isLatent);
end

function null=testInductions2(smm,U,V,i,visitedTriples,curPath,Y, X, isLatent)
    null = [];
    if length(curPath)>maxPathLength
        return;        
    end
    if screen
        fprintf('Now Procceeding through edge %d-%d for i= %d\n',U,V,i)
    end
    reachable = [V reachable];

    %Find all nodes W s.t U->V , V->W
    currentNbrs = allEdgesCols(find(allEdgesRows==V));
    %For all W
    for k = 1:length(currentNbrs)
        W = currentNbrs(k);
        if W~=U && (isempty(visitedTriples) || ~ismember([U V W],visitedTriples,'rows')) && ~ismember(W,curPath) && W~= X
            if (isLatent(V) && ~iscollider(U, V, W, smm))||(~isnoncollider(smm, U, V, W, dnc) && any(potdir(V, [X, Y])))
                if screen
                    fprintf('%s is a possible extended collider and %d is a possible ancestor of edge endpoints %s\n',...
                    num2str([U V W]), V, num2str([X, Y]));
                end
                if W~=Y
                    if screen
                        fprintf('The path [%s] is a legal path, adding node %d to reachable nodes\n',num2str([U V W]),W);
                    end
                 visitedTriples = [visitedTriples; U V W];
                 testInductions2(smm,V,W,i+1,visitedTriples,[curPath W],Y, X, isLatent);
                elseif W == Y
                 inducingPaths(pathCount, 1:length([curPath Y]))= [curPath Y];
                 pathCount = pathCount + 1;
                end % end if W~=Y
            end % end if ~isnoncollider
        end % end if  W~=U && ~ismember([U V W],visitedTriples,'rows') && ~ismember(W,curPath) && W~= X
    end %end for k
end % end test inductions2
end



function bool =  isnoncollider(smm, X, Y, Z, dnc)

bool = 0;

if ~isempty(dnc) && (ismember([X Y Z], dnc, 'rows') || ismember([Z Y X], dnc, 'rows'))
    bool = 1;
    return;
end
if smm(X, Y)==3 || smm(Z, Y)==3
    bool =1;
end
end

function bool = iscollider(X, Y,Z, smm)
bool =0;
if smm(X, Y)==2 && smm(Z, Y)==2
    bool =1;
end
end


