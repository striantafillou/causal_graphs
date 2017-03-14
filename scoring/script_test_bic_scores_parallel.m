clear all;clc;

fprintf('----------Test: Scores-vs nVars--------------\n');
variableSizes =[10 20 50 100];
maxParents= 3;
nSamples = 500;
maxLatent=4;
nIters =20;

all_models=cell(4, length(variableSizes));
%parpool(4);

for inVars = 1:length(variableSizes)
    nVars = variableSizes(inVars);
    clear models*;
    models_gt(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci_cons(nIters,1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_random(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);

    fprintf('\n*----------------*nVars %d*-------------*\n', nVars);
    parfor i =1:nIters
        %fprintf('\no----------------oIter %do-------------o\n', i);
        % generate random gaussian dag
        dag = randomdag(nVars, maxParents);
        type = 'gaussian';
        nLatent = randi(maxLatent);
        isLatent = false(nVars, 1);
        isLatent(randsample(1:nVars, nLatent)) = true;
        observedVars=find(~isLatent);
        nObsVars = length(observedVars);

        % covert to BN and simulate data.
        [nodes, domainCounts] = dag2randBN(dag, type);
        ds  = simulatedata(nodes, nSamples, type, 'isLatent', isLatent);
        covMat= cov(ds.data(:, observedVars));


        % find golden truth mag/pag
        gtmag = dag2mag(dag, isLatent);
        gtmag_ = gtmag(observedVars,observedVars);
        gtpag_ = mag2pag(gtmag_);


        % run FCI to obtain initial pag/initialize mag.
        pag_fci =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
        mag_fci= pag2mag(pag_fci.graph);    
        mag_fci_ = mag_fci(observedVars, observedVars);

        % run FCI to obtain initial pag/initialize mag.
        pag_fci_cons =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
        mag_fci_cons= pag2mag(pag_fci_cons.graph);    
        mag_fci_cons_ = mag_fci_cons(observedVars, observedVars);


        % fit gaussian model to ground truth    
        [~, ~, hatCovMat, ~] = RICF_fit(gtmag_, covMat, 10^-6);
        models_gt(i)= likelihoodgauss(gtmag_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_, covMat, 10^-6);
        models_fci(i)= likelihoodgauss(mag_fci_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI conservative output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_cons_, covMat, 10^-6);
        models_fci_cons(i)= likelihoodgauss(mag_fci_cons_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to random mag
        dag_random= randomdag(nVars, maxParents);
        mag_random = dag2mag(dag_random, isLatent);
        mag_random_ = mag_random(observedVars, observedVars);

        [~, ~, hatCovMat, ~] = RICF_fit(mag_random_, covMat, 10^-6);
       	models_random(i)= likelihoodgauss(mag_random_, covMat, hatCovMat, nObsVars, nSamples);

%        bics = [gtbic(i), bic_fci(i), bic_fci_cons(i), bic_random(i)];
%         [~, inds] = sort(bics);
%         fprintf('\nOrder:\n');
%         for it=1:4
%             fprintf('\t %s %.4f\t', graphs{inds(it)}, bics(inds(it)));
%         end
    end
    [all_models{1, inVars}, all_models{2, inVars}, all_models{3, inVars}, all_models{4, inVars}] =deal(models_gt, models_fci, models_fci_cons, models_random);
end    % end nVars

save('tetrad\results\scores_vs_nVars.mat');
%% Figure with bics, aics
close all;

methods = {'GT', 'FCI', 'FCI cons', 'Random'};
[mean_bics, mean_aics] = deal(nan(length(variableSizes), length(methods)));
for inVars=1:length(variableSizes)
    for iMethod=1:4
    mean_bics(iMethod, inVars)= mean([all_models{iMethod, inVars}.bic]);
    mean_aics(iMethod, inVars) = mean([all_models{iMethod, inVars}.aic_c]);
    end
end
% mean_bics(i, j): mean bics of method i, nVars j

% plot aics
fh_aic =figure; ah(1) = gca;hold all;
% plot aic(fci)- mean(gt) for each variable size
%fci
plot(mean_aics(2, :)-mean_aics(1, :));
% fci cons
plot(mean_aics(3, :)-mean_aics(1, :));
% line at y=0
plot([1, inVars], [0, 0], '--k');
legend(methods{[2, 3]});
ah(1).YLabel.String = 'AIC(FCI)-AIC(GT)';

% plot bics

fh_bic =figure; ah(2)= gca;hold all;
% plot bic(fci)- mean(gt) for each variable size
%fci
plot(mean_bics(2, :)-mean_bics(1, :));
% fci cons
plot(mean_bics(3, :)-mean_bics(1, :));
% line at y=0
plot([1, inVars], [0, 0], '--k');
legend(methods{[2, 3]});
ah(2).YLabel.String = 'BIC(FCI)-BIC(GT)';

% common figure properites
[ah(:).XTick] = deal(1:length(variableSizes));
[ah(:).XTickLabel] = deal(variableSizes);
ah(:).XLabel.String = 'number of variables';

ah(:).Title.String = [num2str(nSamples), ' samples, ' num2str(maxParents) ' max parents']; 

%save
saveas(fh_aic, 'tetrad\plots\ricf_aics_vs_nVars', 'png');
saveas(fh_aic, 'tetrad\ricf_aics_vs_nVars', 'fig');

saveas(fh_bic, 'tetrad\plots\ricf_bics_vs_nVars', 'png');
saveas(fh_bic, 'tetrad\ricf_bics_vs_nVars', 'fig');



%% vs Max Parents

clear all;clc;

fprintf('----------Test: Scores-vs MP--------------\n');
nVars = 20;
maxParentsSizes= [3 5 7];
nSamples = 500;
maxLatent=4;
nIters =50;

all_models=cell(4, length(maxParentsSizes));
%parpool(4);

for iMP = 1:length(maxParentsSizes)
    maxParents = maxParentsSizes(iMP);
    clear models*;
    models_gt(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci_cons(nIters,1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_random(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);

    fprintf('\n*----------------*maxParents %d*-------------*\n', maxParents);
    for i =1:nIters
        %fprintf('\no----------------oIter %do-------------o\n', i);
        % generate random gaussian dag
        dag = randomdag(nVars, maxParents);
        type = 'gaussian';
        nLatent = randi(maxLatent);
        isLatent = false(nVars, 1);
        isLatent(randsample(1:nVars, nLatent)) = true;
        observedVars=find(~isLatent);
        nObsVars = length(observedVars);

        % covert to BN and simulate data.
        [nodes, domainCounts] = dag2randBN(dag, type);
        ds  = simulatedata(nodes, nSamples, type, 'isLatent', isLatent);
        covMat= cov(ds.data(:, observedVars));


        % find golden truth mag/pag
        gtmag = dag2mag(dag, isLatent);
        gtmag_ = gtmag(observedVars,observedVars);
        gtpag_ = mag2pag(gtmag_);


        % run FCI to obtain initial pag/initialize mag.
        pag_fci =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
        mag_fci= pag2mag(pag_fci.graph);    
        mag_fci_ = mag_fci(observedVars, observedVars);

        % run FCI to obtain initial pag/initialize mag.
        pag_fci_cons =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
        mag_fci_cons= pag2mag(pag_fci_cons.graph);    
        mag_fci_cons_ = mag_fci_cons(observedVars, observedVars);


        % fit gaussian model to ground truth    
        [~, ~, hatCovMat, ~] = RICF_fit(gtmag_, covMat, 10^-6);
        models_gt(i)= likelihoodgauss(gtmag_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_, covMat, 10^-6);
        models_fci(i)= likelihoodgauss(mag_fci_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI conservative output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_cons_, covMat, 10^-6);
        models_fci_cons(i)= likelihoodgauss(mag_fci_cons_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to random mag
        dag_random= randomdag(nVars, maxParents);
        mag_random = dag2mag(dag_random, isLatent);
        mag_random_ = mag_random(observedVars, observedVars);

        [~, ~, hatCovMat, ~] = RICF_fit(mag_random_, covMat, 10^-6);
       	models_random(i)= likelihoodgauss(mag_random_, covMat, hatCovMat, nObsVars, nSamples);

%        bics = [gtbic(i), bic_fci(i), bic_fci_cons(i), bic_random(i)];
%         [~, inds] = sort(bics);
%         fprintf('\nOrder:\n');
%         for it=1:4
%             fprintf('\t %s %.4f\t', graphs{inds(it)}, bics(inds(it)));
%         end
    end
    [all_models{1, iMP}, all_models{2, iMP}, all_models{3, iMP}, all_models{4, iMP}] =deal(models_gt, models_fci, models_fci_cons, models_random);
end    % end nVars

save('tetrad\results\scores_vs_MP.mat');
%% Figure with bics, aics
close all;
methods = {'GT', 'FCI', 'FCI cons', 'Random'};
[mean_bics, mean_aics] = deal(nan(length(maxParentsSizes), length(methods)));
for iMP=1:length(maxParentsSizes)
    for iMethod=1:4
    mean_bics(iMethod, iMP)= mean([all_models{iMethod, iMP}.bic]);
    mean_aics(iMethod, iMP) = mean([all_models{iMethod, iMP}.aic_c]);
    end
end
% mean_bics(i, j): mean bics of method i, nVars j

% plot aics
fh_aic =figure; ah(1) = gca;hold all;
% plot aic(fci)- mean(gt) for each variable size
%fci
plot(mean_aics(2, :)-mean_aics(1, :));
% fci cons
plot(mean_aics(3, :)-mean_aics(1, :));
% line at y=0
plot([1, iMP], [0, 0], '--k');
legend(methods{[2, 3]});
ah(1).YLabel.String = 'AIC(FCI)-AIC(GT)';

% plot bics

fh_bic =figure; ah(2)= gca;hold all;
% plot bic(fci)- mean(gt) for each variable size
%fci
plot(mean_bics(2, :)-mean_bics(1, :));
% fci cons
plot(mean_bics(3, :)-mean_bics(1, :));
% line at y=0
plot([1, iMP], [0, 0], '--k');
legend(methods{[2, 3]});
ah(2).YLabel.String = 'BIC(FCI)-BIC(GT)';

% common figure properites
[ah(:).XTick] = deal(1:length(maxParentsSizes));
[ah(:).XTickLabel] = deal(maxParentsSizes);
ah(1).XLabel.String = 'maxParents';
ah(2).XLabel.String = 'maxParents';

ah(1).Title.String = [num2str(nVars) ' variables,   ', num2str(nSamples), ' samples'];
ah(2).Title.String = [num2str(nVars) ' variables,   ', num2str(nSamples), ' samples']; 
%save
saveas(fh_aic, 'tetrad\plots\ricf_aics_vs_MP', 'png');
saveas(fh_aic, 'tetrad\ricf_aics_vs_MP', 'fig');

saveas(fh_bic, 'tetrad\plots\ricf_bics_vs_MP', 'png');
saveas(fh_bic, 'tetrad\ricf_bics_vs_MP', 'fig');



%% vs Sample sizes

clear all;clc;

fprintf('----------Test: Scores-vs MP--------------\n');
nVars = 20;
maxParents = 5;
nSamplesSizes = [100, 500, 1000, 5000];
maxLatent=4;
nIters =50;

all_models=cell(4, length(nSamplesSizes));
%parpool(4);

for iSS = 1:length(nSamplesSizes)
    nSamples = nSamplesSizes(iSS);
    clear models*;
    models_gt(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_fci_cons(nIters,1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);
    models_random(nIters, 1) = struct('fml',[],'sll',[],'bic',[], 'aic_c', [], 'nFreeParams', [], 'dof', []);

    fprintf('\n*----------------*nSamples %d*-------------*\n', nSamples);
    for i =1:nIters
        %fprintf('\no----------------oIter %do-------------o\n', i);
        % generate random gaussian dag
        dag = randomdag(nVars, maxParents);
        type = 'gaussian';
        nLatent = randi(maxLatent);
        isLatent = false(nVars, 1);
        isLatent(randsample(1:nVars, nLatent)) = true;
        observedVars=find(~isLatent);
        nObsVars = length(observedVars);

        % covert to BN and simulate data.
        [nodes, domainCounts] = dag2randBN(dag, type);
        ds  = simulatedata(nodes, nSamples, type, 'isLatent', isLatent);
        covMat= cov(ds.data(:, observedVars));


        % find golden truth mag/pag
        gtmag = dag2mag(dag, isLatent);
        gtmag_ = gtmag(observedVars,observedVars);
        gtpag_ = mag2pag(gtmag_);


        % run FCI to obtain initial pag/initialize mag.
        pag_fci =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
        mag_fci= pag2mag(pag_fci.graph);    
        mag_fci_ = mag_fci(observedVars, observedVars);

        % run FCI to obtain initial pag/initialize mag.
        pag_fci_cons =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
        mag_fci_cons= pag2mag(pag_fci_cons.graph);    
        mag_fci_cons_ = mag_fci_cons(observedVars, observedVars);


        % fit gaussian model to ground truth    
        [~, ~, hatCovMat, ~] = RICF_fit(gtmag_, covMat, 10^-6);
        models_gt(i)= likelihoodgauss(gtmag_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_, covMat, 10^-6);
        models_fci(i)= likelihoodgauss(mag_fci_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to FCI conservative output
        [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_cons_, covMat, 10^-6);
        models_fci_cons(i)= likelihoodgauss(mag_fci_cons_, covMat, hatCovMat, nObsVars, nSamples);

        % fit gaussian model to random mag
        dag_random= randomdag(nVars, maxParents);
        mag_random = dag2mag(dag_random, isLatent);
        mag_random_ = mag_random(observedVars, observedVars);

        [~, ~, hatCovMat, ~] = RICF_fit(mag_random_, covMat, 10^-6);
       	models_random(i)= likelihoodgauss(mag_random_, covMat, hatCovMat, nObsVars, nSamples);

%        bics = [gtbic(i), bic_fci(i), bic_fci_cons(i), bic_random(i)];
%         [~, inds] = sort(bics);
%         fprintf('\nOrder:\n');
%         for it=1:4
%             fprintf('\t %s %.4f\t', graphs{inds(it)}, bics(inds(it)));
%         end
    end
    [all_models{1, iSS}, all_models{2, iSS}, all_models{3, iSS}, all_models{4, iSS}] =deal(models_gt, models_fci, models_fci_cons, models_random);
end    % end nVars

save('tetrad\results\scores_vs_SS.mat');
%% Figure with bics, aics
close all;
methods = {'GT', 'FCI', 'FCI cons', 'Random'};
[mean_bics, mean_aics] = deal(nan(length(nSamplesSizes), length(methods)));
for iSS=1:length(nSamplesSizes)
    for iMethod=1:4
    mean_bics(iMethod, iSS)= mean([all_models{iMethod, iSS}.bic]);
    mean_aics(iMethod, iSS) = mean([all_models{iMethod, iSS}.aic_c]);
    end
end
% mean_bics(i, j): mean bics of method i, nVars j

% plot aics
fh_aic =figure; ah(1) = gca;hold all;
% plot aic(fci)- mean(gt) for each variable size
%fci
plot(mean_aics(2, :)-mean_aics(1, :));
% fci cons
plot(mean_aics(3, :)-mean_aics(1, :));
% line at y=0
plot([1, iSS], [0, 0], '--k');
legend(methods{[2, 3]});
ah(1).YLabel.String = 'AIC(FCI)-AIC(GT)';

% plot bics

fh_bic =figure; ah(2)= gca;hold all;
% plot bic(fci)- mean(gt) for each variable size
%fci
plot(mean_bics(2, :)-mean_bics(1, :));
% fci cons
plot(mean_bics(3, :)-mean_bics(1, :));
% line at y=0
plot([1, iSS], [0, 0], '--k');
legend(methods{[2, 3]});
ah(2).YLabel.String = 'BIC(FCI)-BIC(GT)';

% common figure properites
[ah(:).XTick] = deal(1:length(nSamplesSizes));
[ah(:).XTickLabel] = deal(nSamplesSizes);
ah(1).XLabel.String = 'sample size';
ah(2).XLabel.String = 'sample size';

ah(1).Title.String = [num2str(nVars) ' variables,   ', num2str(maxParents), ' max parents'];
ah(2).Title.String = [num2str(nVars) ' variables,   ', num2str(maxParents), ' max parents']; 
%save
saveas(fh_aic, 'tetrad\plots\ricf_aics_vs_SS', 'png');
saveas(fh_aic, 'tetrad\ricf_aics_vs_SS', 'fig');

saveas(fh_bic, 'tetrad\plots\ricf_bics_vs_SS', 'png');
saveas(fh_bic, 'tetrad\ricf_bics_vs_SS', 'fig');
