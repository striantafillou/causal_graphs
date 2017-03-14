clear all;clc;

fprintf('----------Test: Scores-vs nVars--------------\n');
nVars = 10;
maxParents= 5;
nSamples = 1000;
maxLatent=3;

%parpool(4);
repeat= false;

%fprintf('\no----------------oIter %do-------------o\n', i);
% generate random gaussian dag
figure;
for iDag=1:10;
    t=tic;
    fprintf('---------%d---------------\n', iDag)
    if ~repeat
     dag = randomdag(nVars, maxParents);
    end
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

    %
    % find golden truth mag/pag
    mag_gt = dag2mag(dag, isLatent);
    mag_gt_ = mag_gt(observedVars,observedVars);
    pag_gt_ = mag2pag(mag_gt_);

    % run FCI conservative to obtain initial pag/initialize mag.
    pag_fci_cons =FCI(ds, 'test', 'fisher', 'heuristic', 3, 'verbose', fakse, 'pdsep', false, 'cons', true);
    mag_fci_cons= pag2mag(pag_fci_cons.graph);    
    mag_fci_cons_ = mag_fci_cons(observedVars, observedVars);
    pag_fci_cons_ = pag_fci_cons.graph(observedVars, observedVars);

    % fit gaussian model to ground truth    
    [~, ~, hatCovMat, ~] = RICF_fit(mag_gt_, covMat, 10^-6);
    models_gt= likelihoodgauss(mag_gt_, covMat, hatCovMat, nObsVars, nSamples);


    % fit gaussian model to FCI conservative output
    [~, ~, hatCovMat, ~] = RICF_fit(mag_fci_cons_, covMat, 10^-6);
    models_fci_cons= likelihoodgauss(mag_fci_cons_, covMat, hatCovMat, nObsVars, nSamples);


    [fpEdges, fnEdges] = falseEdges(mag_fci_cons_, mag_gt_);

    cur_mag = mag_fci_cons_;
    cur_stats = models_fci_cons;


    %[xs, ys]=find(triu(cur_mag==0, 1));
    %pairs=[xs, ys];
    %

    pairs = nchoosek(1:nObsVars, 2);
    nPairs= size(pairs, 1);
    criterion ='bic';
    [precision_init, recall_init] = precisionRecall(mag2pag(cur_mag), pag_gt_);

    % needed for printing
    symbolsXY = {' ', 'o', '>', '-', 'z'};
    symbolsYX = {' ', 'o', '<', '-', 'z'};

    fprintf('Entering algo, cur %s: %.5f, Precision: %.3f, Recall %.3f\n',criterion,models_fci_cons.(criterion), precision_init, recall_init);

    nIters = 100;
    [mags, stats, actions] = deal(cell(nIters, 1));
    gs = struct('mag', cur_mag', 'stats', cur_stats, 'from', '', 'to', '', 'action', '', 'score', cur_stats.(criterion), 'shd', structuralHammingDistancePAG(mag_fci_cons_, mag_gt_));

    clear search_struct
    search_struct(1)=gs;
    search_struct(nIters+1).from = [];
    for iter =1:nIters
        round_scores = nan(nPairs, 1);
        [round_mags, round_stats, round_actions] = deal(cell(nPairs, 1));
        parfor iPair = 1:nPairs
            from = pairs(iPair,1); to = pairs(iPair, 2);
            % if edge is absent
            [round_mags{iPair}, round_stats{iPair}, round_actions{iPair} ]= pick_action(cur_mag, cur_stats, from, to, covMat, nObsVars, nSamples, 'criterion', criterion);
            round_scores(iPair)= round_stats{iPair}.(criterion);
        end
        [best_score, best_ind] = min(round_scores);

        from=pairs(best_ind, 1); to =pairs(best_ind, 2);
        best_action = round_actions{best_ind};
        if isequal(best_action, 'none');
            break;
        end
        %fprintf('Pair %d-%d, action %s, new_score %.3f\n', pairs(best_ind, 1), pairs(best_ind, 2), best_action, cur_stats.(criterion));
        %fprintf('\t is: %d %s-%s %d // %d %s-%s %d \n', from, symbolsYX{mag_gt_(to, from)+1},symbolsXY{mag_gt_(from, to)+1}, to, from, symbolsYX{pag_gt_(to, from)+1},symbolsXY{pag_gt_(from, to)+1}, to)
        cur_pag = mag2pag(cur_mag);
        %fprintf('\t was: %d %s-%s %d // %d %s-%s %d \n', from, symbolsYX{cur_mag(to, from)+1},symbolsXY{cur_mag(from, to)+1}, to, from, symbolsYX{cur_pag(to, from)+1},symbolsXY{cur_pag(from, to)+1}, to)
    %     
        cur_mag = round_mags{best_ind};
        cur_stats = round_stats{best_ind};
        cur_pag=mag2pag(cur_mag);
        %fprintf('\t now: %d %s-%s %d // %d %s-%s %d \n', from, symbolsYX{cur_mag(to, from)+1},symbolsXY{cur_mag(from, to)+1}, to, from, symbolsYX{cur_pag(to, from)+1},symbolsXY{cur_pag(from, to)+1}, to)
    % 
    % 
        gs = struct('mag', cur_mag, 'stats', cur_stats, 'from', from, 'to', to, 'action', '', 'score', cur_stats.(criterion),'shd', structuralHammingDistancePAG(cur_mag, mag_gt_));
        search_struct(iter+1) = gs;
        fprintf('\t\t New shd %d\n', search_struct(iter+1).shd); 

    end
plot([search_struct(:).shd]); hold on;
fprintf('\t\t time elapsed %.3fd\n', toc(t)); 

end


%%
    
    

for iEdge =1:size(fnEdges,1)
   from = fnEdges(iEdge, 1);
   to = fnEdges(iEdge, 2);
   x_=observedVars(from);
   y_=observedVars(to);
   fprintf('Edge %d-%d, sepSet [%s], cur aic_c: %.5f\n', from, to, num2str(find(squeeze(pag_fci_cons.sepSet(x_, y_, :)))), models_fci_cons.aic_c);

   % add edge X->Y
   mag_tmp = mag_fci_cons_;
   mag_tmp(from, to) = 3;
   mag_tmp(to, from) = 2;
   
   % score
   [~, ~, hatCovMat, ~] = RICF_fit(mag_tmp, covMat, 10^-6);
   models_tmp= likelihoodgauss(mag_tmp, covMat, hatCovMat, nObsVars, nSamples);
       
   fprintf('\t new aic_c: %.5f\n', models_tmp.aic_c);
  
   % add Y->X
   mag_tmp = mag_fci_cons_;
   mag_tmp(from, to) = 2;
   mag_tmp(to, from) = 3;
   
   % score
   [~, ~, hatCovMat, ~] = RICF_fit(mag_tmp, covMat, 10^-6);
   models_tmp= likelihoodgauss(mag_tmp, covMat, hatCovMat, nObsVars, nSamples);
       
   fprintf('\t new aic_c: %.5f\n', models_tmp.aic_c);
end
    



       

       

