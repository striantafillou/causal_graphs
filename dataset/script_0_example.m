% test to see if FCI with oracle works correctly
% Create 200 graphs with 40 variables and max. 5 latent, run FCI, compare
% to gold standard.

fprintf('----------Test 1: FCI oracle--------------\n');
nIters=200;
times = nan(nIters, 1);
nVars = 10;
maxParents = 3;

for i =1:nIters
    fprintf('Iter %d\n', i);

    dag = randomdag(nVars, maxParents);
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;
    gds = simulateoracledata(dag, 'isLatent', isLatent);
    gds.isAncestor = transitiveClosureSparse_mex(sparse(dag));
    t=tic;
    p = FCI(gds, 'test', 'msep',  'verbose', true);
    times(i) =toc;

    gt = mag2pag(dag2mag(dag, isLatent));
    if any(any(p.graph-gt.graph))
    fprintf('Error in FCI oracle\n')
        break;
    end
end
 

%% Create the example from Causation, Prediction and Search p. 187 to check pdSepStage.


fprintf('----------Test 2: Find possible d-separating set--------------\n');
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

    
%% Test to check the results of the FCI heuristic.

fprintf('----------Test 1: FCI oracle--------------\n');
nIters=200;
times = nan(nIters, 3);
nVars =20;
maxParents = 3;

for i =1%:200
    dag = randomdag(nVars, maxParents);
    type = 'gaussian';
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;

    [nodes, domainCounts] = dag2randBN(dag, type);
    nSamples = 500;

    ds  = simulatedata(nodes, nSamples, domainCounts, type, 'isLatent', isLatent);

    p3 = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false);   
times(i, 
    p2 =FCI(ds, 'test', 'fisher', 'heuristic', 2, 'false', true, 'pdsep', false);
    p1 =FCI(ds, 'test', 'fisher', 'heuristic', 1, 'verbose', true, 'pdsep', false);
    gt = mag2pag(dag2mag(dag, isLatent));
    
    gds = simulateoracledata(dag, 'isLatent', isLatent);
    gds.isAncestor = transitiveClosureSparse_mex(sparse(dag));
    gds.isAncestor(isLatent, :) =0; gds.isAncestor(:, isLatent)=0;
    pgt = FCI(gds, 'test', 'msep',  'verbose', false);
    
    [sPrec3, sRec3, oPrec3, oRec3] = fciprescisionrecall(p3, gt);
    [sPrec1, sRec1, oPrec1, oRec1] = fciprescisionrecall(p1, gt);
    
end

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
