function [nodes,  rnodes] = dag2BN(graph, dataset)
% function [nodes, domainCounts] = dag2randBN(graph,dataset)
% Converts a DAG into a BN with  parameters estimated from dataset. Author:
% sot16@pitt.edu
% =======================================================================
% Inputs
% =======================================================================
% graph                   = The DAG of the BN type                    
% dataset                 = struct with dataset.data
% =======================================================================
% Outputs
% =======================================================================
% nodes                   = nVars x 1 cell, cell iVar contains a struct with
%                           domains:
%    .name                = the name of variable iVar
%    .parents             = the parents of variable iVar
%    .cpt                 = conditional probability table 
%                           P(iVar|Parents(iVar))
% domainCounts            = nVars x 1 array with the number of possible
%                           values for each variable
% =======================================================================
numNodes = size(graph,1);
[nodes, rnodes] = deal(cell(numNodes,1));

%topologically ordering the graph
% levels = toposort(graph);
% [~, order] = sortrows([(1:numNodes)' levels'], 2);
% 

order = graphtopoorder(sparse(graph));
headers = dataset.headers;
 
if isequal(dataset.type, 'discrete')
    %let's follow the topographical order ;-)
    for i = order

        %info
        %fprintf('Node %d of %d\n', i, length(order))

        %name, parents, number of states and domain counts of the node
        node.name = headers{i};
        node.parents = find(graph(:,i));
        rnode=node;
        %let's create the cpt...
        if isempty(node.parents)

            %firstly, let's sample...
            node.parents = [];
           % node.cpt = dirichletsample(0.5*ones(1,numStates),1);
           [node.cpt, rnode.cpt] = cond_prob_mult(i, node.parents', dataset);
        else
           [node.cpt, rnode.cpt] = cond_prob_mult(i, node.parents', dataset);
        end

        nodes{i} = node;
        rnodes{i}= rnode;

    end
end
end
