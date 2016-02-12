inds= find(triu(ones(nVars),1));
allProbInds = [];isNadj = [];
for i =1:200
    nVars =20; maxParents = 3;
    graph = generateRandomDag(nVars, maxParents);
    adj = graph+graph';
    type = 'gaussian';
    verbose = true;

    [nodes, domainCounts] = convertGraphToBN(graph, type);
    nSamples = 100;

    ds  = simulateData(nodes, nSamples, domainCounts, type);
    gds = simulateOracleData(graph);
    gds.domainCounts = AllPairsDescendants_mex(sparse(graph));
    p =FCI(ds, 'fisher', 0.1, 5, false, false, false);
    [probInd, ahat, pi0hat] = PROPeR(~~p.graph, p.pvalues);
   % [fh, ah] = plotPvalueFit(p.pvalues, graph+graph', ahat);
    allProbInds = [allProbInds; probInd(inds)];
    isNadj =  [isNadj; ~adj(inds)];

end


[n, bin] = histc(allProbInds, 0:0.1:1);
for iBin =1:length(n)-1
    curInds = bin==iBin;
    actualProbInd(iBin) = sum(isNadj(curInds))./sum(curInds);
end

    
%%

nVars =15; maxParents = 5;
graph = generateRandomDag(nVars, maxParents);
type = 'gaussian';
verbose = true;

[nodes, domainCounts] = convertGraphToBN(graph, type);
nSamples = 100;

ds  = simulateData(nodes, nSamples, domainCounts, type);
gds = simulateOracleData(graph);
gds.domainCounts = AllPairsDescendants_mex(sparse(graph));

maxCondsetSize = 5;
fprintf('%s\n', datestr(now))
[pvalues, tests, latentTests] = allIndependenceTests(ds, 'fisher', 'maxCondsetSize', 4);
fprintf('%s\n', datestr(now))
[tpvalues, ~, ~]= allIndependenceTests(gds, 'msep','maxCondsetSize', 4);
%%

graph = zeros(4); graph([1, 2], 3)=1; graph(3, 4) =1;
type = 'discrete';
headers ={'X', 'Y', 'Z', 'W'};
nSamples =500;
gds = simulateOracleData(graph);
gds.domainCounts = AllPairsDescendants_mex(sparse(graph));
%%

nSamples =1000;

nVars =6; maxParents = 3;
[allProbInd, allActualProbInd, allPvals] = deal([]);
for i=1:100
graph = generateRandomDag(nVars, maxParents);
type = 'gaussian';
verbose = true;
gds = simulateOracleData(graph);
gds.domainCounts = AllPairsDescendants_mex(sparse(graph));
fprintf('%s\n', datestr(now))
[tpvalues, ~, ~]= allIndependenceTests(gds, 'msep');



type = 'gaussian';
[nodes, domainCounts] = convertGraphToBN(graph, type);
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

