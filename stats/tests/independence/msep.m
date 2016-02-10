function [p, foo, exitflag] = msep(x, y, condset, dataset)
% FUNCTION [P, R, EXITFLAG] = FISHER(VAR1, VAR2, CONDVARSET, DATASET)
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
%                           .isDescendant a nVars x nVars matrix, 
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
graph  = dataset.data;
isDescendant = dataset.domainCounts;
if ismember(y, findmseparations(graph, x, condset, isDescendant));
    p =1;
else p =0;
end
exitflag =1;foo =1;
end

function [mSeparateNodes]=findmseparations(graph, nodes, conditionToNodes, isDescendant)
% The algorithm is described in Richard E. Neapolitan's, 'Learning Bayesian
% Networks' p.84
% 
%dSeparateNodes=findDSeparations(graph,nodes,conditionToNodes)
%Input:     graph:              the graph
%           nodes:              the nodes you want to find to which they are 
%                               m-separated
%           conditionToNodes:   the nodes condition to which will look for 
%                               m-separations
%Output:    mSeparateNodes:     the set of all d-separate nodes of the graph 
%                               by conditionToNodes 


%global isDescendant;
%does not check if(graph not square or A,B not disjoint or A+B>V)...

reachable=[];
edges=zeros(size(graph));
numOfVariables=size(graph,1);

inConditionSet=zeros(numOfVariables,1)';
inConditionSet(conditionToNodes)=1;
descendents=zeros(numOfVariables,1)';
descendents = sum(isDescendant(:, conditionToNodes),2)';
descendents(descendents>1) = 1;
descendents(conditionToNodes) = 1;

newGraph = zeros(size(graph));
newGraph(find(graph))=1;



for i=1:size(nodes,2)
    iedges=find(newGraph(:,nodes(i)))';
    reachable=[reachable nodes(i) iedges];
    edges(nodes(i),iedges)=1;
end

%all this while and fors I think they are not fully optimized...
i=1;
found=1;
while(found)
    found=0;
    [edgesLabeledi_rows,edgesLabeledi_cols]=find(edges==i);
    tmp1=edges(edgesLabeledi_cols,:)+newGraph(edgesLabeledi_cols,:);
    tmp2=zeros(size(graph,1));
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
         %   fprintf('Proceeding through %s\n', num2str([U V W]));
            
            if(edgesLabeledi_rows(j)~=unlabeledEdge)
                %U_V=graph(U,V)
                %W_V=graph(W,V)
                %isDesc=descendents(V)
                %inCon=inConditionSet(V)
                headToHead=isheadtohead(graph,[U V W]);
                if(((~headToHead && inConditionSet(V)==0)||(headToHead && descendents(V)==1)))
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
a=ones(size(graph,1),1)';
a(AcR)=0;
mSeparateNodes=find(a==1);
end



function [boolean]=isheadtohead(graph,threenodes)
    if(graph(threenodes(1),threenodes(2))==2 && graph(threenodes(3),threenodes(2))==2)
        boolean=1;
    else
        boolean=0;
    end
end