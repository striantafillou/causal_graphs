function dataset=simulateDoDataSel(nodes, doVar, doVal, selVars, numCases,  type, varargin)
% function dataset=simulateDoData(nodes, doVar, doVal, numCases,  type, varargin)
% =======================================================================
% Inputs
% =======================================================================
% nodes                   = nVars x 1 cell describing the BN. Node
%                           parametrizations P(Node|Parents(Node)) are
%                           stored in field cpt. To create a random
%                           parametrization from a DAG, use 
%                           graph2randBN(graph, params).
% numCases                = Number of samples desired
%                           variable, (empty matrix for continuous
%                           variables) 
% type                    = type of nodes: discrete or gaussian
%                           in future versions this should be decided a
%                           field of th network.
% doVar                   = indices for manipulated variables
% doVal                   = values for manipulated variables (nm
%   [...] = simulatedata(...,'PARAM1',VAL1,'PARAM2',VAL2,...) specifies additional
%     parameters and their values.  Valid parameters are the following:
%  
%          Parameter     Value
%       'domainCounts'   1 x nVars vector # of possible values for each
%                         variable. Default: all variables are binary
%
%       'isLatent'       1 x nVars vector true for latent variables.
%       'isManipulated'  1 x nVars vector true for manipulated variables.
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
%    .isManipulated       = nVars x 1 boolean vector, true for manipulatedn
%                           variables  
%   .type                 = type of data set (discrete, gaussian)
% =======================================================================
numNodes=length(nodes);
[domainCounts, isLatent, isManipulated, verbose] = process_options(varargin, 'domainCounts', [], 'isLatent',  false(1, numNodes),'isManipulated', false(1, numNodes),  'verbose', false);
dataset.isLatent = isLatent;
dataset.isManipulated = isManipulated;
headers = cell(1, numNodes);

doVal = doVal+1;
if size(doVal, 1)==1
    doVal = repmat(doVal, numCases, 1);
end
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
    case_cnt=1;
    while case_cnt<numCases+1
        % Loop over all nodes to be simulated
        for node=ord
            if ismember(node, doVar)
                node_values(node) = doVal(case_cnt, ismember(doVar, node));
            else
                node_values(node)=randomSampleDiscrete(nodes{node}.cpt, node_values(nodes{node}.parents));
            end
        end 
        data(case_cnt,:)=node_values ;
        if all(data(case_cnt, selVars)==2)
            case_cnt = case_cnt+1;
        end
    end 
    data=data-1;
    dataset.data = data;
    dataset.domainCounts = domainCounts;
    dataset.type = 'discrete';

else % type is unknown
    errprintf('Unknown data type:%s\n', type);
    dataset = nan;
end
dataset.headers = headers;
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


function value = randomSampleGaussian(node, instance, numCases)
% value = RANDOMSAMPLEGAUSSIAN(NODE, INSTANCE) Returns a value y = beta
% *instance+e for a conditional variable with parent instanciation instance

%calculate the normal distribution mean conditioned by parents value
if ~isempty(instance)
    distrMean = [node.mi + (node.beta * instance')]';
else
    distrMean = node.mi;
end

%normal distribution standard deviation
distrS = node.s*ones(numCases, 1);

%calculate the node value
value = normrnd(distrMean, distrS, numCases,1);
end

function value = randomSamplePolynomial(node, instance, numCases)
% value = RANDOMSAMPLEPOLYNOMIAL(NODE, INSTANCE) Returns a value y = beta
% *instance.^p+e for a conditional variable with parent instanciation instance

%calculate the normal distribution mean conditioned by parents value
if ~isempty(instance)
    distrMean = [node.mi + (node.beta * [instance.^node.p]')]';
else
    distrMean = node.mi;
end

%normal distribution standard deviation
distrS = node.s*ones(numCases, 1);

%calculate the node value
value = normrnd(distrMean, distrS, numCases,1);
end