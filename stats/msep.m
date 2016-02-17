function [p, foo, exitflag] = msep(x, y, condset, dataset)
% function [p, foo, exitflag] = msep(x, y, condset, dataset)
% Fisher test of independence of variable var1 and variable var2 given
% variables in condvarset.
%
% Author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% var1, var2, condvarset  = indices of var1, var2, [condvarset] in
%                           dataset.data
% dataset                 = structure with domains
%                           .graph the mag/smcm matrix
%                           .isAncestor a nVars x nVars matrix, 
%                           (i, j) = true if i is a descendant of j (j-->i), 
%                           false otherwise
% =======================================================================
% Outputs
% =======================================================================
% p                       = 0 for H_1, 1 for H_0
% foo                     = dummy variable
% exitflag                = dummy variable denoting if computations
%                           completed correctly (1 f true, 0 otherwise)
% =======================================================================
if ismember(y, findmseparations(dataset.data, x, condset, dataset.isAncestor, dataset.isLatent));
    p =1;
else p =0;
end
exitflag =1;foo =1;
end
function Y =findmseparations(mcg, X, conditionToNodes, isAnc, isLatent)
% function Y=findmseparations(graph, X, conditionToNodes, isAnc, isLatent)
% Returns nodes Y that are m-separated with nodes X given nodes
% conditionToNodes in graph mcg.
% The algorithm is a modified version of the algorithm described in Richard E. Neapolitan's, 
% 'Learning Bayesian Networks' p.84 modified for mixed causal graphs.
% =======================================================================
% Inputs
% =======================================================================
% mcg                     = A mixed causal graph.               
% X                       = set of nodes to look for m-separated nodes.
% conditionToNodes        = conditioning set.
% isAnc                   = nVars x nVars matrix, isAnc(i, j) = true
%                           if i is an ancestor of j in mcg. use
%                           findancestors(mcg) to create this matrix.
% isLatent                = nVars x 1 matrix, true for latent nodes.
% =======================================================================
% Outputs
% =======================================================================
% Y                       = the set of nodes that is m-separated with X
%                           given conditionToNodes in mcg.
% =======================================================================


%global isDescendant;
%does not check if(graph not square or A,B not disjoint or A+B>V)...

reachable=[];
edges=zeros(size(mcg));
numOfVariables=size(mcg,1);

inConditionSet=zeros(numOfVariables,1)';
inConditionSet(conditionToNodes)=1;
descendants = sum(isAnc(:, conditionToNodes),2)';
descendants(descendants>1) = 1;
descendants(conditionToNodes) = 1;

newGraph = zeros(size(mcg));
newGraph(~~mcg)=1;



for i=1:size(X,2)
    iedges=find(newGraph(:,X(i)))';
    reachable=[reachable X(i) iedges];
    edges(X(i),iedges)=1;
end

%all this while and fors I think they are not fully optimized...
i=1;
found=1;
while(found)
    found=0;
    [edgesLabeledi_rows,edgesLabeledi_cols]=find(edges==i);
    tmp1=edges(edgesLabeledi_cols,:)+newGraph(edgesLabeledi_cols,:);
    tmp2=zeros(size(mcg,1));
    tmp2(edgesLabeledi_cols,:)=tmp1;
    [unlabeledEdges_rows,unlabeledEdges_cols]=find(tmp2==1);
   % unlabeledEdges = [unlabeledEdges_rows,unlabeledEdges_cols];
    for j=1:size(edgesLabeledi_rows)
        indices_of_unlabeledEdges=find(unlabeledEdges_rows==edgesLabeledi_cols(j));
        for k=1:size(indices_of_unlabeledEdges,1)
            unlabeledEdge=unlabeledEdges_cols(indices_of_unlabeledEdges(k));
            
            U=edgesLabeledi_rows(j);
            V=edgesLabeledi_cols(j);
            W=unlabeledEdge;
           %  fprintf('Proceeding through %s\n', num2str([U V W]));            
            if(edgesLabeledi_rows(j)~=unlabeledEdge)
                %U_V=graph(U,V)
                %W_V=graph(W,V)
                %isDesc=descendents(V)
                %inCon=inConditionSet(V)
                if(((isnoncollider(mcg,U, V, W) && inConditionSet(V)==0)||(iscollider(mcg,U, V, W) && descendants(V)==1)))
                  % fprintf('%d is a non collider and in CS or a collider and a descendant of CS\n', V);
                    reachable=[reachable W];
                    edges(V,W)=i+1;
                    found=1;
                end
            end
        end
    end
    i=i+1;
end
AcR=[conditionToNodes reachable];
a=ones(size(mcg,1),1)';
a(AcR)=0;
Y=find(a==1);
Y = setdiff(Y, find(isLatent));
end



function isCol=iscollider(graph, U, V, W)
    if((graph(U,V)==2||graph(U,V)==4) && (graph(W, V)==2||graph(W, V)==4))
        isCol=1;
    else
        isCol=0;
    end
end


function isnCol=isnoncollider(graph, U, V, W)
    if ~(graph(U,V)==2 && graph(W, V)==2) 
        isnCol=1;
    else
        isnCol=0;
    end
end