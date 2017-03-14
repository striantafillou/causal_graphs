
clear all 
clc;
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

close all;maxParents=3;
load([results_dir filesep 'experiments_maxParents_' num2str(maxParents)]);
linesize =2;xlabelsize=30;ylabelsize =30; ticklabelsize =20;legendsize=20;for inVars =1:3    
    figure; hold all;
    
    shds = squeeze(shdsRandom(inVars, :, :));
    meanShds = mean(shds, 2);   
    stdShds = std(shds, [], 2);  
    ahp(4)=shadedBar([0.86 2 3.14], meanShds, stdShds, [0.9 0.9 0.9], false);
  
    
    shds = squeeze(shdsCFCI(inVars, :, :));
    meanShds = mean(shds, 2);
    stdShds = std(shds, [], 2);
    ahp(1) = errorbar([1:3]-.1, meanShds, stdShds, 'LineWidth', linesize);

    shds = squeeze(shdsFCI(inVars, :, :));
    meanShds = mean(shds, 2);
    stdShds = std(shds, [], 2);
    ahp(2) = errorbar([1:3], meanShds, stdShds, 'LineWidth', linesize);

    shds = squeeze(shdsGS(inVars, :, :));
    meanShds = mean(shds, 2);
    stdShds = std(shds, [], 2);
    ahp(3) = errorbar([1:3]+0.1, meanShds, stdShds, 'LineWidth', linesize);

    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XLim =[0.85 3.15];
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('shd',  'FontSize', ylabelsize)

    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_shds_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end

%save colors;

colorFCI = ahp(2).Color;
colorCFCI = ahp(1).Color;
colorGS = ahp(3).Color;

for inVars =1:3    
    figure; hold all;
    
    precisions = squeeze(precisionsRandom(inVars, :, :));
    meanprecisions = mean(precisions, 2);   
    stdprecisions = std(precisions, [], 2);  
    ahp(4)=shadedBar([0.86 2 3.14], meanprecisions, stdprecisions, [0.9 0.9 0.9], false);
  
    
    precisions = squeeze(precisionsCFCI(inVars, :, :));
    meanprecisions = mean(precisions, 2);
    stdprecisions = std(precisions, [], 2);
    ahp(1) = errorbar([1:3]-.1, meanprecisions, stdprecisions, 'LineWidth', linesize);

    precisions = squeeze(precisionsFCI(inVars, :, :));
    meanprecisions = mean(precisions, 2);
    stdprecisions = std(precisions, [], 2);
    ahp(2) = errorbar([1:3], meanprecisions, stdprecisions, 'LineWidth', linesize);

    precisions = squeeze(precisionsGS(inVars, :, :));
    meanprecisions = mean(precisions, 2);
    stdprecisions = std(precisions, [], 2);
    ahp(3) = errorbar([1:3]+0.1, meanprecisions, stdprecisions, 'LineWidth', linesize);

    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XLim =[0.85 3.15];
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('Precision',  'FontSize', ylabelsize)
    ah.YLim =[-0.05 1];

    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_precisions_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end


for inVars =1:3    
    figure; hold all;
    
    recalls = squeeze(recallsRandom(inVars, :, :));
    meanrecalls = mean(recalls, 2);   
    stdrecalls = std(recalls, [], 2);  
    ahp(4)=shadedBar([0.86 2 3.14], meanrecalls, stdrecalls, [0.9 0.9 0.9], false);
  
    
    recalls = squeeze(recallsCFCI(inVars, :, :));
    meanrecalls = mean(recalls, 2);
    stdrecalls = std(recalls, [], 2);
    ahp(1) = errorbar([1:3]-.1, meanrecalls, stdrecalls, 'LineWidth', linesize);

    recalls = squeeze(recallsFCI(inVars, :, :));
    meanrecalls = mean(recalls, 2);
    stdrecalls = std(recalls, [], 2);
    ahp(2) = errorbar([1:3], meanrecalls, stdrecalls, 'LineWidth', linesize);

    recalls = squeeze(recallsGS(inVars, :, :));
    meanrecalls = mean(recalls, 2);
    stdrecalls = std(recalls, [], 2);
    ahp(3) = errorbar([1:3]+0.1, meanrecalls, stdrecalls, 'LineWidth', linesize);

    l =legend(ahp, {'cFCI', 'FCI', 'GS', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XLim =[0.85 3.15];
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('Recall',  'FontSize', ylabelsize)
    ah.YLim =[-0.05 1];

    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_recalls_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end

%
for inVars =1:3    
    figure; hold all;
    clear ahp
   
    times = squeeze(timesCFCI(inVars, :, :));
    meantimes = mean(times, 2);
    stdtimes = std(times, [], 2);
    ahp(1) = errorbar([1:3]-.1, meantimes, stdtimes, 'LineWidth', linesize, 'color', colorCFCI);
    

    times = squeeze(timesFCI(inVars, :, :));
    meantimes = mean(times, 2);
    stdtimes = std(times, [], 2);
    ahp(2) = errorbar([1:3], meantimes, stdtimes, 'LineWidth', linesize, 'color', colorFCI);
    

    times = squeeze(timesGS(inVars, :, :));
    meantimes = mean(times, 2);
    stdtimes = std(times, [], 2);
    ahp(3) = errorbar([1:3]+0.1, meantimes, stdtimes, 'LineWidth', linesize, 'color', colorGS);

    l =legend(ahp, {'cFCI', 'FCI', 'GS'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XLim =[0.85 3.15];
    ah.XTickLabel ={'100', '1000', '5000'};
    ylabel('Time',  'FontSize', ylabelsize)
    ah.YLim =[-0.05 ah.YLim(2)];
    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_times_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end
%

for inVars =1:3    
    figure; hold all;
    clear ahp;
    scores = squeeze(scoresRandom(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    meanscores = mean(scores, 2);   
    stdscores = std(scores, [], 2);  
    ahp(5)=shadedBar([0.86 2 3.14], meanscores, stdscores, [0.9 0.9 0.9], false);
  
%
    scores = squeeze(scoresFCI(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    meanscores = mean(scores, 2);
    stdscores = std(scores, [], 2);
    ahp(2) = errorbar([1:3]-0.1, meanscores, stdscores, 'LineWidth', linesize, 'color', colorFCI);

    scores = squeeze(scoresGS(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    meanscores = mean(scores, 2);
    stdscores = std(scores, [], 2);
    ahp(3) = errorbar([1:3], meanscores, stdscores, 'LineWidth', linesize, 'color', colorGS);

    scores = squeeze(scoresGT(inVars, :, :))./repmat(nnSamples, nIters, 1)';
    meanscores = mean(scores, 2);
    stdscores = std(scores, [], 2);
    ahp(4) = errorbar([1:3]+0.1, meanscores, stdscores, 'LineWidth', linesize, 'color', 'k');

    
    l =legend(ahp(2:5), {'FCI', 'GS','GT', 'Random'}, 'FontSize', legendsize, 'Orientation', 'horizontal', 'Location', 'northoutside');
    xlabel('sample size', 'FontSize', xlabelsize);
    ah= gca;
    ah.XTick = 1:3;
    ah.XLim =[0.85 3.15];
    ah.XTickLabel ={'100', '1000', '5000'};
 
    ylabel('Score/# samples','FontSize', ylabelsize)
    figureName = [plots_dir filesep 'mp' num2str(maxParents)  '_scores_' num2str(nnVarsL(inVars))];
    saveas(gcf, figureName, 'png');
       
end