function dataset=simulatedata(nodes, numCases, domainCounts, type, varargin)
% function dataset=simulatedata(nodes, numCases, domainCounts, type, varargin)
% Author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% nodes                   = nVars x 1 cell describing the BN. Node
%                           parametrizations P(Node|Parents(Node)) are
%                           stored in field cpt. To create a random
%                           parametrization from a DAG, use 
%                           graph2randBN(graph, params).
% numCases                = Number of samples desired
% domainCounts            = nVars x 1 vector # of possible values for each 
%                           variable, (empty matrix for continuous
%                           variables) 
% type                    = type of nodes: discrete or gaussian
%                           future versions could be extended to
%                           include mixtures of distributions,then type
%                           should be changed to be a node attribute.
% =======================================================================
% Outputs
% =======================================================================
% dataset                 = struct describing the data, 
%    .data                   nSamples x nVars matrix containing the data
%    .domain_counts       = nVars x 1 vector # of possible values for each 
%                           variable, (empty matrix for continuous
%                           variables)
%    .isLatent            = nVars x 1 boolean vector, true for latent
%                           variables
%    .isManipulated       = nVars x 1 boolean vector, true for manipulated
%                           variables  
% =======================================================================
numNodes=length(nodes);
[isLatent, isManipulated, verbose] = process_options(varargin, 'isLatent',  false(1, numNodes),'isManipulated', false(1, numNodes),  'verbose', false);
dataset.isLatent = isLatent;
dataset.isManipulated = isManipulated;
headers = cell(1, numNodes);
if isequal(type, 'discrete')
    
    edges=0;
    for i=1:numNodes
        headers{i} = nodes{i}.name;
        edges=edges+length(nodes{i}.parents);
    end
    graph=spalloc(numNodes,numNodes,edges);
    for i=1:numNodes
        graph(nodes{i}.parents,i) = 1;
    end
    ord=graphtopoorder(sparse(graph));

    data=zeros(numCases, length(nodes));
    node_values = zeros(1,numNodes);
    for case_cnt=1:numCases
        % Loop over all nodes to be simulated
        for node=ord
            node_values(node)=randomSampleDiscrete(nodes{node}.cpt, node_values(nodes{node}.parents));                            
        end 
        data(case_cnt,:)=node_values;    
    end 
    data=data-1;
    dataset.data = data;
    dataset.domainCounts = domainCounts;
elseif isequal(type, 'gaussian')
%first step: creating the dataset
    edges=0;
    for i=1:numNodes
        headers{i} = nodes{i}.name;
        edges=edges+length(nodes{i}.parents);
    end
    graph=spalloc(numNodes,numNodes,edges);
    for i=1:numNodes
        parents=nodes{i}.parents;
        children=i*ones(size(parents));
    end
    ord=graphtopoorder(sparse(graph));
  
    data=zeros(numCases, length(nodes));
    for case_cnt=1:numCases
        % Reset nodes values
        node_values=-1*ones(1,numNodes);

        % Loop over all nodes to be simulated
        for node=ord
            % Sample
            node_values(node)=randomSampleGaussian(nodes{node}, node_values(nodes{node}.parents));                            
        end

        data(case_cnt,:)=node_values;

    end 
    dataset.data = data;
    dataset.domainCounts =[];
else % type is unknown
    errprintf('Unknown data type:%s\n', type);
    dataset = nan;
end
datset.headers = headers;
end

function value = randomSampleDiscrete(cpt, instance)
% value = RANDOMSAMPLEDISCRETE(CPT, INSTANCE)
% Returns a value for a discrete variable using the conditional probability table
% cpt, for parent instanciation instance.

if(isempty(instance))
    x = 1;
else
    s = size(cpt);
    x = mdSub2Ind_mex(s(2:end), instance);
end

cumprobs = cumsum(cpt(:,x));
value = [];
while isempty(value)
      value = find(cumprobs - rand > 0, 1 );
end
end


function value = randomSampleGaussian(node, instance)
% value = RANDOMSAMPLEGAUSSIAN(NODE, INSTANCE) Returns a value y = beta
% *instance+e for a conditional variable with parent instanciation instance

%calculate the normal distribution mean conditioned by parents value
if ~isempty(instance)
    distrMean = node.mi + (node.beta * instance');
else
    distrMean = node.mi;
end

%normal distribution standard deviation
distrS = node.s;

%calculate the node value
value = normrnd(distrMean, distrS);
end