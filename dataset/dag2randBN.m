
function [nodes, domainCounts, order, rnodes] = dag2randBN(graph, type,  varargin)
% function [nodes, domainCounts] = dag2randBN(graph, type,  varargin)
% Converts a DAG into a BN with random parameters. Author:
% striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% graph                   = The DAG of the BN type                    
% type                    = type of variables, 'discrete' or 'gaussian' 
% minNumStates            = minimum number of states (domain counts) for
%                           discrete variables (default 2)
% maxNumStates            = maximum number of states (domain counts) for
%                           discrete variables (default 5) variable, (empty
%                           matrix for continuous variables)
% miMinValue/miMaxValue   = min/max value for the mean of each gaussian
%                           gaussian variable (default 0/0)
% sMinValue/sMaxValue     = min/max value for the std of each gaussian
%                           gaussian variable (default 1/1)
% betaMinValue/
% betaMaxValue            = min/max value for the correlation
%                           coefficient of a gaussian variable with each
%                           parent (default 0.2/0.9)
% headers                 = 1 x numNodes cell of node names (default x1:xn)
% 
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
nodes = cell(numNodes,1);
domainCounts = nan(1,numNodes);

%topologically ordering the graph
% levels = toposort(graph);
% [~, order] = sortrows([(1:numNodes)' levels'], 2);
% 

order = graphtopoorder(sparse(graph));
if isequal(type, 'discrete')
    [minNumStates, maxNumStates,   headers, alpha] = process_options(varargin, 'minNumStates', 2*ones(1, numNodes), 'maxNumStates', 5*ones(1,numNodes),  'headers', ...
        arrayfun(@(x)sprintf('x%i',x),1:numNodes, 'uniformOutput',false), 'alpha',ones(1, numNodes));
elseif isequal(type, 'linear')
    [miMinValue, miMaxValue, sMinValue, sMaxValue, betaMinValue, betaMaxValue, headers] = process_options(varargin,...
        'miMinValue', -5, 'miMaxValue', 5,  'sMinValue', 1, 'sMaxValue',10, 'betaMinValue', 0.1, 'betaMaxValue', 0.9, 'headers', ...
        arrayfun(@(x)sprintf('x%i',x),1:numNodes,'uniformOutput',false));
elseif isequal(type, 'mixed')
    [numStates, miMinValue, miMaxValue, sMinValue, sMaxValue, betaMinValue, betaMaxValue, logMinValue, logMaxValue,  headers] = process_options(varargin,...
        'numStates', 2*ones(1,numNodes), ...
        'miMinValue', 0, 'miMaxValue', 0,  'sMinValue', 1, 'sMaxValue',1, 'betaMinValue', 0.1, 'betaMaxValue', 3, 'logMinValue', -2, 'logMaxValue', 1,...
        'headers', arrayfun(@(x)sprintf('x%i',x),1:numNodes,'uniformOutput',false));    
elseif isequal(type, 'polynomial')
    [miMinValue, miMaxValue, sMinValue, sMaxValue, betaMinValue, betaMaxValue, pMinOrder, pMaxOrder,  headers] = process_options(varargin,...
        'miMinValue', 0, 'miMaxValue', 0,  'sMinValue', 1, 'sMaxValue',1, 'betaMinValue', 0.1, 'betaMaxValue', 0.9, ...
        'pMinOrder', 1, 'pMaxOrder', 2, 'headers', arrayfun(@(x)sprintf('x%i',x),1:numNodes,'uniformOutput',false));

else
    fprintf('Unknown data type: %s\n', type);
    return;
end

if isequal(type, 'discrete')
    %let's follow the topographical order ;-)
    for i = order
        %info
        %fprintf('Node %d of %d\n', i, length(order))
        %name, parents, number of states and domain counts of the node
        node.name = headers{i};
        node.parents = find(graph(:,i));
        numStates = round(rand()*(maxNumStates(i) - minNumStates(i)) + minNumStates(i));
        domainCounts(i) = numStates;
        rnode=node;
        %let's create the cpt...
        if isempty(node.parents)

            %firstly, let's sample...
            node.parents = [];
           % node.cpt = dirichletsample(0.5*ones(1,numStates),1);
           [theta, theta2] = dirichletsample(alpha(i).*ones(1,numStates),1);
            node.cpt = theta;
            rnode.cpt = theta2;
        else
            [theta, theta2] =dirichletsample(alpha(i).*ones(1,numStates), domainCounts(node.parents));
            node.cpt = theta;
            rnode.cpt = theta2;
        end

        nodes{i} = node;
        rnodes{i}= rnode;

    end
elseif isequal(type, 'linear') % gaussian
    for i=1:numNodes
        clear tempNode options cOptions
        tempNode.name = headers{i};
        tempNode.parents = find(graph(:,i))';
        numParents = length(tempNode.parents);
        signs = (-1).^floor(rand(1, numParents).*2);
        tempNode.beta =[betaMinValue + (betaMaxValue-betaMinValue).*rand(1,numParents)].*signs;

    
        tempNode.mi = (miMaxValue - miMinValue)*rand()+miMinValue;
        tempNode.s = (sMaxValue - sMinValue)*rand()+sMinValue;

        nodes{i} = tempNode; 
    end
    domainCounts =[];

elseif isequal(type, 'polynomial') % polynomial
    for i=1:numNodes
        clear tempNode options cOptions
        tempNode.name = headers{i};
        tempNode.parents = find(graph(:,i))';
        numParents = length(tempNode.parents);
        signs = (-1).^floor(rand(1, numParents).*2);
        tempNode.p = randi([pMinOrder, pMaxOrder],1, numParents);
        tempNode.beta =[betaMinValue + (betaMaxValue-betaMinValue).*rand(1,numParents)].*signs;
    
        tempNode.mi = (miMaxValue - miMinValue)*rand() + miMinValue;
        tempNode.s = (sMaxValue - sMinValue)*rand() + sMinValue;

        nodes{i} = tempNode; 
    
    end
    domainCounts =[];
    
elseif isequal(type, 'mixed') % mixed model with P(Y|Z)conditional Gaussian if Y is discrete, and P(Y|Z) logistic if Y is binary
    domainCounts=numStates;
    for i=1:numNodes
        clear tempNode options cOptions
        tempNode.name = headers{i};
        tempNode.dc = numStates(i);
        %find parents
        tempNode.parents= find(graph(:,i))';numParents = length(tempNode.parents);
        tempNode.isContPar = isnan(numStates(tempNode.parents));nContParents = sum(tempNode.isContPar);
        tempNode.isDiscPar = ~isnan(numStates(tempNode.parents));nDiscParents = sum(tempNode.isDiscPar);

        if isnan(numStates(i)) % if iNode is continuous          
            if any(tempNode.isDiscPar)
                allConfigs = allConfigurations(nDiscParents, numStates(tempNode.parents(tempNode.isDiscPar)));
                nConfigs = size(allConfigs,1);
            else 
                nConfigs=1;
                allConfigs=[];
            end
            tempNode.configs = allConfigs;
            tempNode.beta = nan(nConfigs,nContParents);
            tempNode.mi = nan(nConfigs, 1);
            tempNode.s = nan(nConfigs, 1);
            for iConfig =1:nConfigs
                signs = (-1).^floor(rand(1, nContParents).*2);
                tempNode.beta(iConfig, :) =[betaMinValue + (betaMaxValue-betaMinValue).*rand(1,nContParents)].*signs;
                tempNode.mi(iConfig) = (miMaxValue - miMinValue)*rand()+miMinValue;
                tempNode.s(iConfig) = (sMaxValue - sMinValue)*rand()+sMinValue;
            end
        elseif numStates(i) == 2
            if isempty(tempNode.parents)
               [theta, theta2] = dirichletsample(ones(1,numStates(i)),1);
               tempNode.cpt = theta;
            elseif ~any(tempNode.isContPar)
               [theta, theta2] =dirichletsample(ones(1,numStates(i)), numStates(tempNode.parents(tempNode.isDiscPar)));
                tempNode.cpt = theta;
            else
                signs = (-1).^floor(rand(1, numParents+1).*2);
                tempNode.beta =[logMinValue + (logMaxValue-logMinValue).*rand(1,numParents+1)].*signs;
            end
        else 
            errprintf('DomainCounts %d not supported yet\n', numStates(i));
            
        end  % end if discrete
    nodes{i} = tempNode; 
    
    end
end
end
function [theta, theta2] = dirichletsample(alpha, dims)
% SAMPLE_DIRICHLET Sample N vectors from Dir(alpha(1), ..., alpha(k))
% theta = sample_dirichlet(alpha, N)
% theta(i,j) = i'th sample of theta_j, where theta ~ Dir

% We use the method from p. 482 of "Bayesian Data Analysis", Gelman et al.

k = length(alpha);
N = prod(dims);
theta = zeros(N, k);
scale = 1; % arbitrary
for i=1:k
    theta(:,i) = gamrnd(alpha(i), scale, N, 1);
end
S = sum(theta,2);
theta2 = theta ./ repmat(S, 1, k);
%let's reshape theta following dims...
theta = reshape(theta2', numel(theta), 1);
theta = reshape(theta, [k dims]);
end
