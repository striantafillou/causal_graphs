code_path='C:\Users\striant\Documents\GitHub\causal_graphs';
addpath(genpath(code_path));
cd(code_path);
rmpath([code_path filesep 'scoring' filesep '_trash']);

% save results and plots outside git
results_dir = 'C:\Users\striant\Documents\GitHub\_results\uai_2016_scoring';
if ~isdir(results_dir);mkdir(results_dir);end
plots_dir ='C:\Users\striant\Documents\GitHub\_plots\uai_2016_scoring';
if ~isdir(plots_dir);mkdir(plots_dir);end
clc;

maxParents =5;
tol = 10^-6;

%figure; hold all;
nIters=100;
nnSamples=[100 1000 5000];

nnVarsL =[10 20 50];

[shdsCFCI, shdsFCI, shdsGS,  scoresGS, scoresGT, scoresFCI, scoresEmpty, scoresRandom, precisionsCFCI, precisionsFCI, precisionsGS, precisionsEmpty, precisionsRandom, recallsCFCI, recallsFCI, recallsGS, ...
    recallsEmpty, recallsRandom, timesCFCI, timesFCI, timesGS, diffedgesCFCI, diffedgesFCI, diffedgesGS, diffendpointsCFCI, diffendpointsFCI, diffendpointsGS] =deal(nan(length(nnVarsL), length(nnSamples),nIters));    
           
%%
%flag =false;
for iter=1:nIters
    fprintf('Iter %d:\n', iter);
    
%     if flag
%         [shdsCFCI(:, :, nIters), shdsFCI(:, :, nIters), shdsGS(:, :, nIters),  scoresGS(:, :, nIters), scoresGT(:, :, nIters), scoresFCI(:, :, nIters), precisionsCFCI(:, :, nIters), precisionsFCI(:, :, nIters), ...
%             precisionsGS(:, :, nIters), recallsCFCI(:, :, nIters), recallsFCI(:, :, nIters), recallsGS(:, :, nIters), ...
%             timesCFCI(:, :, nIters), timesFCI(:, :, nIters), timesGS(:, :, nIters), diffedgesCFCI(:, :, nIters), diffedgesFCI(:, :, nIters), diffedgesGS(:, :, nIters),...
%             diffendpointsCFCI(:, :, nIters), diffendpointsFCI(:, :, nIters), diffendpointsGS(:, :, nIters)] =deal(nan);    
%            
%     end
    for inVars =1:length(nnVarsL)
        nVarsL = nnVarsL(inVars);        
        fprintf('----------------------nVars: %d----------------\n', nVarsL);

        % generate random DAG.
        dag = randomdag(nVarsL, maxParents);
        bn = dag2randBN(dag, 'gaussian');
        % choose latent variables.
        nLatent = ceil(0.1*nVarsL);
        isLatent = false(1, nVarsL);
        isLatent(randsample(1:nVarsL, nLatent)) = true;

        % create mag
        magL = dag2mag(dag, isLatent);
        magT = magL(~isLatent, ~isLatent);
        pagT = mag2pag(magT);
        nVars = sum(~isLatent);        

       for inSamples=1:length(nnSamples)
            nSamples=nnSamples(inSamples);
            fprintf('----------------------nSamples: %d----------------\n', nSamples);
            % simulate data, estimate cov_mat           
            ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);
            % get covMat
            covMat=corr(ds.data(:, ~isLatent));

            % run CFCI
            tCFCI=tic;
            cfciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', true);
            cfciPag.graph = cfciPag.graph(~isLatent, ~isLatent);
            shdsCFCI(inVars, inSamples, iter)=structuralHammingDistancePAG(cfciPag.graph, pagT);
            [precisionsCFCI(inVars,inSamples, iter), recallsCFCI(inVars, inSamples, iter)] = precisionRecall(cfciPag.graph, pagT);
            [diffedgesCFCI(inVars, inSamples, iter), diffendpointsCFCI(inVars,inSamples, iter)] = diffEdgeEndpoints(cfciPag.graph, pagT);% 
            timesCFCI(inVars, inSamples, iter, 1) = toc(tCFCI);
    % 
            tFCI=tic;
            fciPag = FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', false, 'pdsep', false, 'cons', false);
            fciPag.graph = fciPag.graph(~isLatent, ~isLatent);
            shdsFCI(inVars, inSamples, iter)=structuralHammingDistancePAG(fciPag.graph, pagT);
            magFCI = pag2mag(fciPag.graph);
            [precisionsFCI(inVars, inSamples, iter), recallsFCI(inVars, inSamples, iter)] = precisionRecall(fciPag.graph, pagT);
            [diffedgesFCI(inVars, inSamples, iter), diffendpointsFCI(inVars, inSamples, iter)] = diffEdgeEndpoints(fciPag.graph, pagT);
            timesFCI(inVars, inSamples, iter) = toc(tFCI);

            % run greedy search
            tGS=tic;
            [gsMag, bs, gsIters, gs] = greedySearchMag(covMat, nSamples, tol, false);
            gsPag = mag2pag(gsMag);
            shdsGS(inVars, inSamples, iter)=structuralHammingDistancePAG(gsPag, pagT);
            scoresGS(inVars, inSamples, iter) = bs;
            [precisionsGS(inVars, inSamples, iter), recallsGS(inVars, inSamples, iter)] = precisionRecall(gsPag, pagT);
            [diffedgesGS(inVars, inSamples, iter), diffendpointsGS(inVars, inSamples, iter)] = diffEdgeEndpoints(gsPag, pagT);
            timesGS(inVars, inSamples, iter) = toc(tGS);

            % score FCI, gt mags.
            [beta, omega, hatCovMat, ~] = RICF_fit(magT, covMat, tol);
            stats = likelihoodgauss(magT, covMat, hatCovMat, nVars, nSamples);
            scoresGT(inVars, inSamples, iter) = stats.bic;         

            [~, ~, hatCovMat, ~] = RICF_fit(magFCI, covMat, tol);
            stats = likelihoodgauss(magFCI, covMat, hatCovMat, nVars, nSamples);
            scoresFCI(inVars, inSamples, iter) = stats.bic;  
            
            emptyMag= zeros(nVars);
            emptyPag = emptyMag;
            [~, ~, hatCovMat, ~] = RICF_fit(emptyMag, covMat, tol);
            stats = likelihoodgauss(emptyMag, covMat, hatCovMat, nVars, nSamples);
            scoresEmpty(inVars, inSamples, iter) = stats.bic;         
            shdsEmpty(inVars, inSamples, iter)=structuralHammingDistancePAG(emptyPag, pagT);
            [precisionsEmpty(inVars, inSamples, iter), recallsEmpty(inVars, inSamples, iter)] = precisionRecall(emptyPag, pagT);
            
            randomMag = dag2mag(randomdag(nVarsL, maxParents), isLatent);randomMag = randomMag(~isLatent, ~isLatent);
            randomPag = mag2pag(randomMag);
            [~, ~, hatCovMat, ~] = RICF_fit(randomMag, covMat, tol);
            stats = likelihoodgauss(randomMag, covMat, hatCovMat, nVars, nSamples);
            scoresRandom(inVars, inSamples, iter) = stats.bic; 
            shdsRandom(inVars, inSamples, iter)=structuralHammingDistancePAG(randomPag, pagT);
            [precisionsRandom(inVars, inSamples, iter), recallsRandom(inVars, inSamples, iter)] = precisionRecall(randomPag, pagT);
       end
    end
    save([results_dir filesep 'experiments_maxParents_' num2str(maxParents) '_new']);
end
%%
close all;maxParents=3;
load([results_dir filesep 'experiments_maxParents_' num2str(maxParents)]);
linesize =3;xlabelsize=30;ylabelsize =30; ticklabelsize =20;legendsize=20;
for inVars =1:3    
    figure; hold all;
    
    shds = squeeze(shdsRandom(inVars, :, :));
    nanmeanShds = nanmean(shds, 2);   
    nanstdShds = nanstd(shds, [], 2);
    x= [1.005 2 2.905, 2.905 2 1.005];
    y = [[nanmeanShds-nanstdShds]'  fliplr([nanmeanShds+nanstdShds]')];
    ahp(4)=fill(x, y, [0.9 0.9 0.9], 'linestyle', 'none');
   % ahp(4) =plot(1:3, nanmeanShds, 'LineWidth', linesize, 'LineStyle', '--', 'color', 'k');
    
    
    shds = squeeze(shdsCFCI(inVars, :, :));
    nanmeanShds = nanmean(shds, 2);
    ahp(1) = plot(1:3, nanmeanShds, 'LineWidth', linesize);
    nanstdShds = nanstd(shds, [], 2);
    plot(1:3, nanmeanShds+nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
    plot(1:3, nanmeanShds-nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);

    hold on;
    shds = squeeze(shdsFCI(inVars, :, :));
    nanmeanShds = nanmean(shds, 2);
    ahp(2) =plot(1:3, nanmeanShds, 'LineWidth', linesize);
    nanstdShds = nanstd(shds, [], 2);
    plot(1:3, nanmeanShds+nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
    plot(1:3, nanmeanShds-nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);

    shds = squeeze(shdsGS(inVars, :, :));
    nanmeanShds = nanmean(shds, 2);
    ahp(3) =plot(1:3, nanmeanShds, 'LineWidth', linesize);
    nanstdShds = nanstd(shds, [], 2);
    plot(1:3, nanmeanShds+nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    plot(1:3, nanmeanShds-nanstdShds, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    
    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('shd',  'FontSize', ylabelsize)

    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_shds_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end


%%


close all;
for inVars =1:3   
    figure; hold all;

    precision = squeeze(precisionsCFCI(inVars, :, :));
    nanmeanPrec = nanmean(precision, 2);
    ahp(1) = plot(1:3, nanmeanPrec, 'LineWidth', linesize);
    nanstdPrec = nanstd(precision, [], 2);
    plot(1:3, nanmeanPrec+nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
    plot(1:3, nanmeanPrec-nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);

    precision = squeeze(precisionsFCI(inVars, :, :));
    nanmeanPrec = nanmean(precision, 2);
    ahp(2) =plot(1:3, nanmeanPrec, 'LineWidth', linesize);
    nanstdPrec = nanstd(precision, [], 2);
    plot(1:3, nanmeanPrec+nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
    plot(1:3, nanmeanPrec-nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);


    precision = squeeze(precisionsGS(inVars, :, :));
    nanmeanPrec = nanmean(precision, 2);
    ahp(3) =plot(1:3, nanmeanPrec, 'LineWidth', linesize);
    nanstdPrec = nanstd(precision, [], 2);
    plot(1:3, nanmeanPrec+nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    plot(1:3, nanmeanPrec-nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    
    precision = squeeze(precisionsRandom(inVars, :, :));
    nanmeanPrec = nanmean(precision, 2);
    ahp(4) =plot(1:3, nanmeanPrec, 'LineWidth', linesize, 'LineStyle', '--', 'color', 'k');
    nanstdPrec = nanstd(precision, [], 2);
    plot(1:3, nanmeanPrec+nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
    plot(1:3, nanmeanPrec-nanstdPrec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
    
    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.YLim =[-0.05 1];
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('Precision',  'FontSize', ylabelsize)

    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_precision_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');


       
end


%%

close all;clear ahp;
for inVars =1:3    
    figure; hold all;

    recall = squeeze(recallsCFCI(inVars, :, :));
    nanmeanRec = nanmean(recall, 2);
    ahp(1) = plot(1:3, nanmeanRec, 'LineWidth', linesize);
    nanstdRec = nanstd(recall, [], 2);
    plot(1:3, nanmeanRec+nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
    plot(1:3, nanmeanRec-nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
    
    hold on;
    recall = squeeze(recallsFCI(inVars, :, :));
    nanmeanRec = nanmean(recall, 2);
    ahp(2) =plot(1:3, nanmeanRec, 'LineWidth', linesize);
    nanstdRec = nanstd(recall, [], 2);
    plot(1:3, nanmeanRec+nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
    plot(1:3, nanmeanRec-nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
    
    recall = squeeze(recallsGS(inVars, :, :));
    nanmeanRec = nanmean(recall, 2);
    ahp(3) =plot(1:3, nanmeanRec, 'LineWidth', linesize);
    nanstdRec = nanstd(recall, [], 2);
    plot(1:3, nanmeanRec+nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    plot(1:3, nanmeanRec-nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    
    recall = squeeze(recallsRandom(inVars, :, :));
    nanmeanRec = nanmean(recall, 2);
    ahp(4) =plot(1:3, nanmeanRec, 'LineWidth', linesize, 'LineStyle', '--', 'color', 'k');
    nanstdRec = nanstd(recall, [], 2);
    plot(1:3, nanmeanRec+nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
    plot(1:3, nanmeanRec-nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
    
%     nanstdRec = nanstd(recall, [], 2);
%     plot(1:3, nanmeanRec+nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color','k');
%     plot(1:3, nanmeanRec-nanstdRec, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color','k');
%     recall = squeeze(recallsEmpty(inVars, :, :));
%     nanmeanRec = nanmean(recall, 2);
%     ahp(5) =plot(1:3, nanmeanRec, 'LineWidth', linesize/3, 'LineStyle', '-.', 'color', 'k');
%     
    
    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.YLim =[-0.05 1];

    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('Recall',  'FontSize', ylabelsize)

    figureName = [plots_dir filesep 'mp' num2str(maxParents) '_recall_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');

       
end



%%



close all;clear ahp;
for inVars =1:3   
        figure; hold all;

        times = squeeze(timesCFCI(inVars, :, :));
        nanmeanTimes = nanmean(times, 2);
        ahp(1) = plot(1:3, nanmeanTimes, 'LineWidth', linesize);
        stdTimes = nanstd(times,[], 2);
        plot(1:3, nanmeanTimes+stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
        plot(1:3, nanmeanTimes-stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
        
        hold on;
        times = squeeze(timesFCI(inVars, :, :));
        nanmeanTimes = nanmean(times, 2);
        ahp(2) =plot(1:3, nanmeanTimes, 'LineWidth', linesize);
        stdTimes = nanstd(times,[], 2);
        plot(1:3, nanmeanTimes+stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
        plot(1:3, nanmeanTimes-stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
        
        
        times = squeeze(timesGS(inVars, :, :));
        nanmeanTimes = nanmean(times, 2);
        ahp(3) =plot(1:3, nanmeanTimes, 'LineWidth', linesize);       
        stdTimes = nanstd(times,[], 2);
        plot(1:3, nanmeanTimes+stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
        plot(1:3, nanmeanTimes-stdTimes, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
        
        
        l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');

        xlabel('sample size', 'FontSize', xlabelsize);
        ah= gca;
        ah.XTick = 1:3;
        ymax= ah.YLim(2);
        ah.YLim =[-0.05 ymax];

        ah.XTickLabel ={'100', '1000', '5000'};
        ylabel('Time (sec)',  'FontSize', ylabelsize)

        figureName = [plots_dir filesep 'mp' num2str(maxParents) '_times_' num2str(nnVarsL(inVars))];
        saveas(gcf, figureName, 'png');
       
end


%%


close all;clear ahp;
for inVars =1:3    
    figure; hold all;
    scores = squeeze(scoresFCI(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    nanmeanScores = nanmean(scores,2);
    ahp(1) = plot(1:3, nanmeanScores, 'LineWidth', linesize);
    stdScores = nanstd(scores,[], 2);
    plot(1:3, nanmeanScores+stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
    plot(1:3, nanmeanScores-stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(1).Color);
        

    hold on;
    scores = squeeze(scoresGS(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    nanmeanScores = nanmean(scores,2);
    ahp(2) =plot(1:3, nanmeanScores, 'LineWidth', linesize);
    stdScores = nanstd(scores,[], 2);
    plot(1:3, nanmeanScores+stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
    plot(1:3, nanmeanScores-stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(2).Color);
        

    scores = squeeze(scoresGT(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    nanmeanScores = nanmean(scores,2);
    ahp(3) =plot(1:3, nanmeanScores, 'LineWidth', linesize);
    stdScores = nanstd(scores,[], 2);
    plot(1:3, nanmeanScores+stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
    plot(1:3, nanmeanScores-stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(3).Color);
        
    
    scores = squeeze(scoresRandom(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    nanmeanScores = nanmean(scores,2);
    ahp(4) =plot(1:3, nanmeanScores, 'LineWidth', linesize,'LineStyle', '--', 'color', 'k');
    stdScores = nanstd(scores,[], 2);
    plot(1:3, nanmeanScores+stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
    plot(1:3, nanmeanScores-stdScores, 'LineWidth', linesize/4, 'LineStyle', '-.', 'Color', ahp(4).Color);
  
    l =legend(ahp, {'FCI', 'GS', 'GT', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XTickLabel ={'100', '1000', '5000'};
    set(ah, 'FontSize', ticklabelsize);
    set(l, 'FontSize', legendsize)

    ylabel('Score/# samples','FontSize', ylabelsize)
    figureName = [plots_dir filesep 'mp' num2str(maxParents) '_scores_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');

end

