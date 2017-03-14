clear;clc;
 
nVarsL =50;
nSamples = 100;
maxLatent = 4;
maxParents = 5;
tol = 10^-6;

%figure; hold all;
nIters=20;
nnSamples=[100 1000 5000];

nnVarsL =[10 20 50];

[shds, precisions, recalls, times] = deal(zeros(length(nnVars), length(nnSamples), nIters, 3));
scores = zeros(length(nnSamples), nIters, 3);


for inVars =1:length(nnVarsL)
    nVars = nnVars(inVarsL);        
    fprintf('----------------------nVars: %d----------------\n', nVars);
    nLatent = ceil(0.1*nVars);
    for inSamples=1:length(nnSamples)
        nSamples=nnSamples(inSamples);
        fprintf('----------------------nSamples: %d----------------\n', nSamples);
        [shdsCFCI, shdsFCI, shdsGS,  scoreGS, scoreGT, scoreFCI, precisionCFCI, precisionFCI, precisionGS, recallCFCI, recallFCI, recallGS, ...
            timesCFCI, timesFCI, timesGS, diffedgesCFCI, diffedgesFCI, diffedgesGS, diffendpointsCFCI, diffendpointsFCI, diffendpointsGS] =deal(nan(nIters,1));    
        parfor iter=1:nIters
            fprintf('Iter %d:\n', iter);
            % generate random DAG.
            dag = randomdag(nVarsL, maxParents);

            % choose latent variables.
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

            % get covMat
            covMat=corr(ds.data(:, ~isLatent));

            % run CFCI
            tCFCI=tic;
            cfciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
            cfciPag.graph = cfciPag.graph(~isLatent, ~isLatent);
            shdsCFCI(iter)=structuralHammingDistancePAG(cfciPag.graph, pagT);
            [precisionCFCI(iter), recallCFCI(iter)] = precisionRecall(cfciPag.graph, pagT);
            [diffedgesCFCI(iter), diffendpointsCFCI(iter)] = diffEdgeEndpoints(cfciPag.graph, pagT);

            timesCFCI(iter) = toc(tCFCI);

            tFCI=tic;
            fciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
            fciPag.graph = fciPag.graph(~isLatent, ~isLatent);
            shdsFCI(iter)=structuralHammingDistancePAG(fciPag.graph, pagT);
            magFCI = pag2mag(fciPag.graph);
            [precisionFCI(iter), recallFCI(iter)] = precisionRecall(fciPag.graph, pagT);
            [diffedgesFCI(iter), diffendpointsFCI(iter)] = diffEdgeEndpoints(fciPag.graph, pagT);

            timesFCI(iter) = toc(tFCI);



            % run greedy search
            tGS=tic;
            [gsMag, bs, gsIters, gs] = greedySearchMag(covMat, nSamples, tol, false);
            gsPag = mag2pag(gsMag);
            shdsGS(iter)=structuralHammingDistancePAG(gsPag, pagT);
            scoreGS(iter) = bs;
            [precisionGS(iter), recallGS(iter)] = precisionRecall(gsPag, pagT);
            [diffedgesGS(iter), diffendpointsGS(iter)] = diffEdgeEndpoints(gsPag, pagT);

            timesGS(iter) = toc(tGS);

            % score FCI, gt mags.

            [beta, omega, hatCovMat, ~] = RICF_fit(magT, covMat, tol);
            stats = likelihoodgauss(magT, covMat, hatCovMat, nVars, nSamples);
            scoreGT(iter) = stats.bic;         

            [~, ~, hatCovMat, ~] = RICF_fit(magFCI, covMat, tol);
            stats = likelihoodgauss(magFCI, covMat, hatCovMat, nVars, nSamples);
            scoreFCI(iter) = stats.bic;         
        end
    shds(inSamples, :, :) = [shdsCFCI shdsFCI shdsGS ];
    scores(inSamples, :, :) = [scoreFCI scoreGS  scoreGT];
    times(inSamples, :,:) =[timesCFCI timesFCI timesGS];
    precisions(inSamples, :,:) =[precisionCFCI precisionFCI precisionGS];
    recalls(inSamples, :,:) =[recallCFCI recallFCI recallGS];
    diffedges(inSamples, :,:) =[diffedgesCFCI diffedgesFCI diffedgesGS];
    diffendpoints(inSamples, :,:) =[diffendpointsCFCI diffendpointsFCI diffendpointsGS];

end
%%
%close all;
figure; hold all;
meanSHDs = squeeze(mean(shds, 2));
%stdSHDDs = squeeze(stc(shds, 2))
for i=1:3
    ahPerf(i) =plot(1:length(nnSamples),meanSHDs( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('structural hamming distance')


figure; hold all;
meanTimes = squeeze(mean(times, 2));
%stdSHDDs = squeeze(stc(shds, 2))
for i=1:3
    ahTimes(i) =plot(1:length(nnSamples),meanTimes( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('running time')


figure; hold all;
meanprecisions = squeeze(mean(precisions, 2));
%stdSHDDs = squeeze(stc(precisions, 2))
for i=1:3
    ahPrec(i) =plot(1:length(nnSamples),meanprecisions( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('precision')

figure; hold all;
meanrecalls = squeeze(mean(recalls, 2));
%stdSHDDs = squeeze(stc(recalls, 2))
for i=1:3
    ahRec(i) =plot(1:length(nnSamples),meanrecalls( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('recall');

figure; hold all;
meandiffedges = squeeze(mean(diffedges, 2));
%stdSHDDs = squeeze(stc(diffedges, 2))
for i=1:3
    ahPerf(i) =plot(1:length(nnSamples),meandiffedges( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('# different edges')

figure; hold all;
meandiffendpoints = squeeze(mean(diffendpoints, 2));
%stdSHDDs = squeeze(stc(diffendpoints, 2))
for i=1:3
    ahPerf(i) =plot(1:length(nnSamples),meandiffendpoints( :, i));
    %ahEB(i)= errorbar(
end
legend({'cFCI', 'FCI', 'GS'})
xlabel('sample size');
ah= gca;
ah.XTick = 1:4;
ah.XTickLabel ={'100', '500', '1000', '5000'};
ylabel('# different endpoints')
