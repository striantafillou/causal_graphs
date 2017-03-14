clear all;clc;
nVars=3;
dag = zeros(nVars);
dag(1, 2) = 1; 
dag(4, 3) = 1;
dag(

magT = dag2mag(dag, false(1, 3));

nSamples=1000;
tol = 10^-6;

foundDnc = true(1, 100);

for iter =1:100
    bn = dag2randBN(dag, 'gaussian');
    ds = simulatedata(bn, nSamples, 'gaussian');
    covMat=corr(ds.data);

    [mag, curScore, nIters, gs] = greedySearchMag(covMat, nSamples, tol);    
    if mag(1, 2)~=2||mag(3,2)~=2||mag(1,3)~=0
        foundDnc(iter)=false;
    end
    printedgesmcg(mag);
    mags{iter}=mag;   
end


%%
clear;clc;
 
nVarsL = 20;
nSamples=1000;
maxLatent = 2;
maxParents = 3;
tol = 10^-6;


for iter=1:10
% generate random DAG.
dag = randomdag(nVarsL, maxParents);

% choose latent variables.
nLatent = randi(maxLatent);
isLatent = false(1, nVarsL);
isLatent(randsample(1:nVarsL, nLatent)) = true;
% simulate data, estimate cov_mat
bn = dag2randBN(dag, 'gaussian');
ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);

% create mag
magL = dag2mag(dag, isLatent);
magT = magL(~isLatent, ~isLatent);
pagT = mag2pag(magT);
nVars = sum(~isLatent);

% run CFCI
cfciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
cfciPag.graph = cfciPag.graph(~isLatent, ~isLatent);

% run greedy search
covMat=corr(ds.data(:, ~isLatent));
[gsMag, curScore, nIters, gs] = greedySearchMagDoubleCheck(covMat, nSamples, tol);
gsPag = mag2pag(gsMag);


shds(iter,1)=structuralHammingDistancePAG(cfciPag.graph, pagT);
shds(iter,2)=structuralHammingDistancePAG(gsPag, pagT);


covMat = corr(ds.data(:, ~isLatent));
%
end

%%
clear;clc;
 
nVarsL = 50;
nSamples=1000;
maxLatent = 5;
maxParents = 3;
tol = 10^-6;


for iter=1:10
% generate random DAG.
dag = randomdag(nVarsL, maxParents);

% choose latent variables.
nLatent = randi(maxLatent);
isLatent = false(1, nVarsL);
isLatent(randsample(1:nVarsL, nLatent)) = true;
% simulate data, estimate cov_mat
bn = dag2randBN(dag, 'gaussian');
ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);

% create mag
magL = dag2mag(dag, isLatent);
magT = magL(~isLatent, ~isLatent);
pagT = mag2pag(magT);
nVars = sum(~isLatent);

% run CFCI
cfciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
cfciPag.graph = cfciPag.graph(~isLatent, ~isLatent);


fciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
fciPag.graph = fciPag.graph(~isLatent, ~isLatent);

% run greedy search
covMat=corr(ds.data(:, ~isLatent));
[gsMag, curScore, nIters, gs] = greedySearchMag(covMat, nSamples, tol);
gsPag = mag2pag(gsMag);


shds(iter,1)=structuralHammingDistancePAG(cfciPag.graph, pagT);
shds(iter, 2) = structuralHammingDistancePAG(fciPag.graph, pagT);
shds(iter,3)=structuralHammingDistancePAG(gsPag, pagT);


covMat = corr(ds.data(:, ~isLatent));
%
end


%%
clear;clc;
 
nVarsL = 10;
nSamples = 100;
maxLatent = 4;
maxParents = 5;
tol = 10^-6;


for iter=1:10
fprintf('Iter %d:\n', iter);
% generate random DAG.
dag = randomdag(nVarsL, maxParents);

% choose latent variables.
nLatent = randi(maxLatent);
isLatent = false(1, nVarsL);
isLatent(randsample(1:nVarsL, nLatent)) = true;
% simulate data, estimate cov_mat
bn = dag2randBN(dag, 'gaussian');
ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);

% create mag
magL = dag2mag(dag, isLatent);
magT = magL(~isLatent, ~isLatent);
pagT = mag2pag(magT);
nVars = sum(~isLatent);

% run CFCI
cfciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
cfciPag.graph = cfciPag.graph(~isLatent, ~isLatent);


fciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
fciPag.graph = fciPag.graph(~isLatent, ~isLatent);

% run greedy search
covMat=corr(ds.data(:, ~isLatent));
[gsMag, bestScore(iter,1), nIters(iter), gs(iter)] = greedySearchMag(covMat, nSamples, tol);
gsPag = mag2pag(gsMag);


[gsMagTB, bestScore(iter,2), nItersTB, gsTB] = greedySearchMagTABU(covMat, nSamples, tol);
gsPagTB = mag2pag(gsMagTB);

[beta, omega, hatCovMat, ~] = RICF_fit(magT, covMat, tol);
stats = likelihoodgauss(magT, covMat, hatCovMat, nVars, nSamples);
bestScore(iter, 3) = stats.bic; 

shds(iter,1)=structuralHammingDistancePAG(cfciPag.graph, pagT);
shds(iter,2)=structuralHammingDistancePAG(fciPag.graph, pagT);

shds(iter,3)=structuralHammingDistancePAG(gsPag, pagT);
shds(iter,4)=structuralHammingDistancePAG(gsPagTB, pagT);


covMat = corr(ds.data(:, ~isLatent));
%
end