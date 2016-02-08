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
           % fprintf('Proceeding through %s\n', num2str([U V W]));
            
            if(edgesLabeledi_rows(j)~=unlabeledEdge)
                %U_V=graph(U,V)
                %W_V=graph(W,V)
                %isDesc=descendents(V)
                %inCon=inConditionSet(V)
                collider=iscollider(mcg,[U V W]);
                if(((~collider && inConditionSet(V)==0)||(collider && descendants(V)==1)))
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



function isCol=iscollider(graph,threenodes)
    if(graph(threenodes(1),threenodes(2))==2 && graph(threenodes(3),threenodes(2))==2)
        isCol=1;
    else
        isCol=0;
    end
end