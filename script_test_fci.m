% test to see if FCI with oracle works correctly
% Create 200 graphs with 40 variables and max. 5 latent, run FCI, compare
% to gold standard.

fprintf('----------Test 1: FCI oracle--------------\n');
nIters=200;
times = nan(nIters, 1);
nVars = 10;
maxParents = 3;
maxLatent=4;

for i =1:nIters
    fprintf('Iter %d\n', i);

    dag = randomdag(nVars, maxParents);
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;
    gds = simulateoracledata(dag, 'isLatent', isLatent);
    gds.isAncestor = transitiveClosureSparse_mex(sparse(dag));
    t=tic;
    p = FCI(gds, 'test', 'msep',  'verbose', false);
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
gt = mag2pag(dag2mag(dag, isLatent));
if any(any(p.graph-gt.graph))
    fprintf('Error in FCI oracle\n')
end
%% Test to check the results of the FCI heuristic.

fprintf('----------Test 3: FCI heuristics--------------\n');
nIters=200;
nVars =100;
maxParents = 3;
nSamples = 1000;
maxLatent=4;

[sPrec, sRec, oPrec, oRec, times, nTests] = deal(zeros(nIters, 3));

for i =1:nIters
    fprintf('Iter %d\n', i);
    dag = randomdag(nVars, maxParents);
    type = 'gaussian';
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;

    [nodes, domainCounts] = dag2randBN(dag, type);

    ds  = simulatedata(nodes, nSamples, domainCounts, type, 'isLatent', isLatent);
    
    t1= tic;
    p1 =FCI(ds, 'test', 'fisher', 'heuristic', 1, 'verbose', false, 'pdsep', false);
    times(i, 1) = toc(t1);
    nTests(i, 1) = p1.nTests;
    
    t2 =tic;
    p2 =FCI(ds, 'test', 'fisher', 'heuristic', 2, 'verbose', false, 'pdsep', false);
    times(i, 2) = toc(t2);
    nTests(i, 2) = p2.nTests;
    
    t3 = tic;
    p3 = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false);  
    times(i, 3) = toc(t3);
    nTests(i, 3) = p3.nTests;


    gt = mag2pag(dag2mag(dag, isLatent));
    
    [sPrec(i, 1), sRec(i, 1), oPrec(i, 1), oRec(i, 1)] = comparepags(p1, gt);
    [sPrec(i, 2), sRec(i, 2), oPrec(i, 2), oRec(i, 2)] = comparepags(p2, gt);
    [sPrec(i, 3), sRec(i, 3), oPrec(i, 3), oRec(i, 3)] = comparepags(p3, gt);
 
end


%% Test to check the results of using conservative FCI

fprintf('----------Test 3: Conservative--------------\n');
nIters=10;
nVars =20;
maxParents = 3;
nSamples = 1000;
maxLatent=4;

[sPrec, sRec, oPrec, oRec, times] = deal(zeros(nIters, 2));

for i =1:nIters
    fprintf('Iter %d\n', i);
    dag = randomdag(nVars, maxParents);
    type = 'gaussian';
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;

    [nodes, domainCounts] = dag2randBN(dag, type);

    ds  = simulatedata(nodes, nSamples, domainCounts, type, 'isLatent', isLatent);
    
    t1= tic;
    p1 =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
    times(i, 1) = toc(t1);
    
    t2 =tic;
    p2 =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
    times(i, 2) = toc(t2);
    


    gt = mag2pag(dag2mag(dag, isLatent));
    
    [sPrec(i, 1), sRec(i, 1), oPrec(i, 1), oRec(i, 1)] = comparepags(p1, gt);
    [sPrec(i, 2), sRec(i, 2), oPrec(i, 2), oRec(i, 2)] = comparepags(p2, gt);
   
end


