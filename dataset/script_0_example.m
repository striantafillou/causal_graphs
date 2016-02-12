nVars =10;
inds= find(triu(ones(nVars),1));
allProbInds = [];isNadj = [];
for i =1:200
    maxParents = 3;
    dag = randomdag(nVars, maxParents);
    adj = dag+dag';
    type = 'gaussian';

    [nodes, domainCounts] = dag2randBN(dag, type);
    nSamples = 100;

    ds  = simulatedata(nodes, nSamples, domainCounts, type);
    gds = simulateoracledata(dag);
    gds.isAncestor = transitiveClosureSparse_mex(sparse(dag));
    p = FCI(gds, 'test', 'msep',  'verbose', false);
    d = dag+dag';
    [x, y] = find(d-~~p.graph);
    if ~isempty(x)
        disp(x);
        break;
    end
    
%     [probInd, ahat, pi0hat] = PROPeR(~~p.graph, p.pvalues);
%    % [fh, ah] = plotPvalueFit(p.pvalues, graph+graph', ahat);
%     allProbInds = [allProbInds; probInd(inds)];
%     isNadj =  [isNadj; ~adj(inds)];
% 
end
%% Create the example from Causation, Prediction and Search p. 187 to check pdSepStage.

dag = zeros(9);
dag(9, 1) = 1; dag(9, 2) = 1;
dag(3, 2) = 1; dag(4, 3) = 1;
dag(4, 5) = 1; dag(5, 6) = 1;
dag(8, 6) = 1; dag(8, 7) = 1;
dag(6, 1) = 1; dag(2, 7) = 1;

isLatent = false(1, nVars); isLatent([8, 9]) = true;
gds = simulateoracledata(dag, 'isLatent', isLatent);

gds.isAncestor = transitiveClosureSparse_mex(sparse(dag));
    p = FCI(gds, 'test', 'msep', 'pdsep', true, 'verbose', false);

%%
[n, bin] = histc(allProbInds, 0:0.1:1);
for iBin =1:length(n)-1
    curInds = bin==iBin;
    actualProbInd(iBin) = sum(isNadj(curInds))./sum(curInds);
end

    
%%

nVars =15; maxParents = 5;
dag = generateRandomDag(nVars, maxParents);
type = 'gaussian';
verbose = true;

[nodes, domainCounts] = convertGraphToBN(dag, type);
nSamples = 100;

ds  = simulateData(nodes, nSamples, domainCounts, type);
gds = simulateOracleData(dag);
gds.domainCounts = transitiveClosureSparse_mex(sparse(dag));

maxCondsetSize = 5;
fprintf('%s\n', datestr(now))
[pvalues, tests, latentTests] = allIndependenceTests(ds, 'fisher', 'maxCondsetSize', 4);
fprintf('%s\n', datestr(now))
[tpvalues, ~, ~]= allIndependenceTests(gds, 'msep','maxCondsetSize', 4);
%%

dag = zeros(4); dag([1, 2], 3)=1; dag(3, 4) =1;
type = 'discrete';
headers ={'X', 'Y', 'Z', 'W'};
nSamples =500;
gds = simulateOracleData(dag);
gds.domainCounts = AllPairsDescendants_mex(sparse(dag));
%%

nSamples =1000;

nVars =6; maxParents = 3;
[allProbInd, allActualProbInd, allPvals] = deal([]);
for i=1:100
dag = generateRandomDag(nVars, maxParents);
type = 'gaussian';
verbose = true;
gds = simulateOracleData(dag);
gds.domainCounts = transitiveClosureSparse_mex(sparse(dag));
fprintf('%s\n', datestr(now))
[tpvalues, ~, ~]= allIndependenceTests(gds, 'msep');



type = 'gaussian';
[nodes, domainCounts] = convertGraphToBN(dag, type);
ds  = simulateData(nodes, nSamples, domainCounts, type);

%fprintf('%s\n', datestr(now))
[pvalues, tests, latentTests] = allIndependenceTests(ds, 'fisher');


[probInd, ahat(i), pi0hat(i)] = PROPeR(pvalues);
allPvals=  [allPvals; pvalues];
allProbInd = [allProbInd; probInd];
allActualProbInd = [allActualProbInd;tpvalues];
end

[n, bin] = histc(allProbInd, 0:0.1:1);
for iBin =1:length(n)-1
    curInds = bin==iBin;
    actualProbInd(iBin) = sum(allActualProbInd(curInds))./sum(curInds);
end

