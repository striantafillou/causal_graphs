function [bestDag, bestScore, iter, gs] = greedySearchDagTABU(inDag, obsData, expData, x, y, xval, domainCounts, nVars)
    % begin from the inDag -- inDag should have an edge (from-->to)
    dag  = inDag;
    [isAncestor,stepAncestor] = warshall2(dag);
    whichPair=nan;
    
    %initialize
    [curScore, curLocalScores] = bdeu_score(obsData, dag, domainCounts, 1);
    bestScore = curScore;
    [bestDag, curDag] = deal(dag);
    nEdges=nnz(dag);
    iter =0;

    pairs = nchoosek(1:nVars, 2);
    pairs = [pairs; pairs(:,2) pairs(:, 1)];
    nPairs = size(pairs,1);

    noChange=0;
    % Initialize TABU
    tabu = nan(100,nVars*nVars);
    tabu_cnt = 0;
    tabu(1, :) = reshape(curDag, 1, []);

    while noChange<1
        iter=iter+1;  
        fprintf('Entering iteration %d, curScore: [%.4f], nEdges: %d\n', iter, curScore, nEdges);
        %add = true(nPairs); % add -> remove
        stepScores = nan(nPairs, 2);
        stepLocalScores = nan(nPairs, nVars);
        stepExpScores = nan(nPairs,1);
        
        for iPair=1:nPairs
            from= pairs(iPair, 1); to = pairs(iPair,2);
            if from==x && to==y || from==y && to==x
                continue;
            end
            % if edge is not in the dag
            if curDag(from, to)==0            
                if stepAncestor(to,from)>1 % if there
                    fprintf('%d--->%d in the graph, cannot add %d->%d\n', to, from, from, to)
                    continue;
                end
                [stepScores(iPair,1), stepLocalScores(iPair,:), stepExpScores(iPair)] = addDirectedEdge(from, to, curDag, obsData, domainCounts, curLocalScores, expData, x, y, xval, nVars);
            else
                [stepScores(iPair,2), stepLocalScores(iPair,:), stepExpScores(iPair)] = removeDirectedEdge(from, to, curDag, obsData, domainCounts, curLocalScores, expData, x, y, xval, nVars);
            end
        end
        

        % look for the best scoring mag not in the tabu list
        bestNotTabu=false;
        while ~bestNotTabu       
            % choose best performing action, update descendants, parents
            if all(isnan(stepScores)|isinf(stepScores))
                fprintf('Visited all possible dags\n')
                break;
            end
            [maxScores,actions] = nanmax(stepScores, [],  2);
            [maxScore, whichPair] = nanmax(maxScores);   
            from = pairs(whichPair,1); to= pairs(whichPair,2);
            stepDag = curDag;
            if actions(whichPair)==1 %add edge
                if stepDag(to, from)==1 % if edge exists reversed, remove it first);
                   stepDag(to, from)=0;
                end
               stepDag(from, to)=1; stepDagFlat= reshape(stepDag, 1, []);
            elseif actions(whichPair)==2
                stepDag(from, to)=1; stepDagFlat= reshape(stepDag, 1, []);
            end
            if ismember(stepDagFlat, tabu, 'rows')
            fprintf('Already visited this dag\n');
            stepScores(whichPair, actions(whichPair))=-inf;
            else, bestNotTabu=true;
            end
        end

        %if minScore<curScore
        from = pairs(whichPair,1); to= pairs(whichPair,2);
        % flag= true you have removed a directed edge and you have to
        % update ancestor matrix
        switch actions(whichPair)
        case 1
            fprintf('\t \t Adding edge %d->%d\n', from, to);
            [isAncestor, stepAncestor]= warshall2(stepDag);
        case 2
            fprintf('\t \t Removing edge %d*-*%d\n', to, from);
            [isAncestor,stepAncestor] = warshall2(double(stepDag));            
        end
        % update score, dag
        curDag= stepDag;
        curScore = maxScore;
        curLocalScores = stepLocalScores(whichPair,:);
        curExpScore =  stepExpScores(whichPair);
        % update tabu list
        tabu(mod(tabu_cnt, 100)+1, :)=stepDagFlat;
        tabu_cnt = tabu_cnt+1;

        % keep i-step matrices.
        gs(iter).stepAncestor = stepAncestor;
        gs(iter).isAncestor = isAncestor;
        gs(iter).score = curScore;
        gs(iter).expScore = curExpScore;
        gs(iter).localScores= curLocalScores;
        gs(iter).dag = curDag;

        if maxScore>bestScore
            noChange=0;
            bestScore=maxScore;
            bestDag = curDag;
        else 
            noChange= noChange+1;
            fprintf('Iteration %d: No score improvements\n', iter);
        end
    end % end while
%     figure;
%     plot(1:iter, [gs(:).score]);
end


function [newScore, curLocalScores, curExpScore] =addDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores, expData, x, y, xval,  nVars)
    dag(from, to) = 1;
    pa_to = find(dag(:, to))';
    curLocalScores(to) = bde(obsData, domainCounts, to, pa_to, 1);
    if dag(to, from)==1
        dag(to, from)=0;
        pa_from = find(dag(:, from))';
        curLocalScores(from) = bde(obsData, domainCounts, from, pa_from, 1);
    end
    adjSet= findMinAdjSet(dag, x, y, nVars);
    [~, curExpScore] = probAdjSet(x, xval,  y, adjSet, expData, obsData, domainCounts, 1000);
    newScore=sum([curExpScore curLocalScores]);
end


function [newScore, curLocalScores, curExpScore] =removeDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores, expData, x, y, xval,  nVars)
    dag(from,to) = 0;
    pa_to = find(dag(:, to))';
    curLocalScores(to) = bde(obsData, domainCounts, to, pa_to, 1);
    adjSet= findMinAdjSet(dag, x, y, nVars);
    [~, curExpScore] = probAdjSet(x, xval,  y, adjSet, expData, obsData, domainCounts, 1000);
    newScore=sum([curExpScore curLocalScores]);
end

