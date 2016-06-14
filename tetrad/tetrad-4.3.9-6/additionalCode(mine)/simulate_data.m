function [data, graph]=simulate_data(nodes, num_cases)
% function [data, graph]=simulate_data(nodes, num_cases)
%
% Data and Adjacency Matrix Generator for 
% Bayesian Network Tiling Tool, public domain version 1.0a
%
% Copyright (C) 2001-2003, Discovery Systems Laboratory (DSL),
%                          Department of Biomedical Informatics,
%                          Vanderbilt University,
%                          2209 Garland Avenue
%                          Nashville, TN 37232,
%                          USA
%
% Authors: A. Statnikov (alexander.statnikov@vanderbilt.edu)
%          I. Tsamardinos (ioannis.tsamardinos@vanderbilt.edu)
%          C. Aliferis (constantin.aliferis@vanderbilt.edu)
%
% =======================================================================
% Inputs
% =======================================================================
% nodes                    = Network (see description of format in the 
%                            documentation in file bn_tiling.m)
% num_cases                = Number of samples desired
%
% =======================================================================
% Outputs
% =======================================================================
% data                     = Data array (see description of format in the 
%                            documentation in file bn_tiling.m)
% graph                    = Adjacency matrix. If the element in the ith
%                            row and jth column of adjacency matrix is 
%                            equal to 1, this means that variable i is a
%                            parent of variable j.
% =======================================================================
% Example
% =======================================================================
% load data\nodes.mat
% [data, graph]=simulate_data(nodes, 1000);
%
% =======================================================================

num_nodes=length(nodes);
edges=0;
for i=1:num_nodes
    edges=edges+length(nodes{i}.parents);
end
graph=spalloc(num_nodes,num_nodes,edges);
for i=1:num_nodes
    parents=nodes{i}.parents;
    children=i*ones(size(parents));
    graph(parents, children)=1;
end
ord=toposort(graph);
[tmp, ord]=sort(ord);

data=zeros(num_cases, length(nodes));
for case_cnt=1:num_cases
    if mod(case_cnt,1000)==1
        fprintf('Generating case %d out of %d\n', case_cnt, num_cases);
    end
    % Reset nodes values
    for cnt=1:num_nodes
        node_values=-1*ones(1,num_nodes);
    end     
    % Loop over all nodes to be simulated
    for node=ord
        % Construct domain counts array for node and parents to be
        % instantiated 
        parents=nodes{node}.parents;
        sizes=size(nodes{node}.cpt);
        if isempty(parents);
           domain_counts=sizes(1);
        else
           domain_counts=sizes;
        end
        % Construct parents values array
        parents_values=node_values(parents);
        % Sample
        node_values(node)=random_sample(nodes{node}.cpt, parents_values, domain_counts);                            
    end 
    data(case_cnt,:)=node_values;    
%    if mod(case_cnt, 10)==0
%       save tmp data
%    end
end 
data=data-1;