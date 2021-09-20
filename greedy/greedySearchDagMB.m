function [bestDag, bestScore, iter, gs] = greedySearchDagMB(obsData, y, domainCounts, nVars)
    % begin from the inDag -- inDag should have an edge (from-->to)
    % finds MB for Y =2.
    dag  = zeros(nVars);
    [isAncestor,stepAncestor] = warshall2(dag);
    whichPair=nan;
    
    %initialize
    [curScore, curLocalScores] = bdeu_score(obsData, dag, domainCounts, 1);
    bestScore = curScore;
    [bestDag, curDag] = deal(dag);
    nEdges=nnz(dag);
    iter =0;

    pairs = [[1 3:nVars]; 2*ones(1, nVars-1)]';
    nPairs = size(pairs,1);

    found=false;
  
    while ~found
        iter=iter+1;  
        fprintf('Entering iteration %d, curScore: [%.4f], nEdges: %d\n', iter, curScore, nEdges);
        %add = true(nPairs); % add -> remove
        stepScores = nan(nPairs, 2);
        stepLocalScores = nan(nPairs, nVars);
        stepExpScores = nan(nPairs,1);
        
        for iPair=1:nPairs
            from= pairs(iPair, 1); to = pairs(iPair,2);
            % if edge is not in the dag
            if curDag(from, to)==0            
                if stepAncestor(to,from)>1 % if there
                    fprintf('%d--->%d in the graph, cannot add %d->%d\n', to, from, from, to)
                    continue;
                end
                [stepScores(iPair,1), stepLocalScores(iPair,:), stepExpScores(iPair)] = addDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores);
            else
                [stepScores(iPair,2), stepLocalScores(iPair,:), stepExpScores(iPair)] = removeDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores);
            end
        end
        
        [maxScores,actions] = nanmax(stepScores, [],  2);
        [maxScore, whichPair] = nanmax(maxScores);   
        if maxScore>=bestScore %if score is better
            bestScore=maxScore;
            from = pairs(whichPair,1); to= pairs(whichPair,2);
            % updateDag;
            bestDag = curDag;
            if actions(whichPair)==1 %add edge
                if bestDag(to, from)==1 % if edge exists reversed, remove it first);
                   bestDag(to, from)=0;
                end
               bestDag(from, to)=1; %stepDagFlat= reshape(stepDag, 1, []);
            elseif actions(whichPair)==2
                bestDag(from, to)=1; %stepDagFlat= reshape(stepDag, 1, []);
            end
            curDag=bestDag;

             % update score.
            curScore = maxScore;
            curLocalScores = stepLocalScores(whichPair,:);
            % update tabu list

            % keep i-step matrices.
            gs(iter).stepAncestor = stepAncestor;
            gs(iter).isAncestor = isAncestor;
            gs(iter).score = curScore;
            gs(iter).localScores= curLocalScores;
            gs(iter).dag = curDag;



            % flag= true you have removed a directed edge and you have to
            % update ancestor matrix
            switch actions(whichPair)
            case 1
                fprintf('\t \t Adding edge %d->%d\n', from, to);
                %if curDag(to, from)
                [isAncestor, stepAncestor]= warshall2(curDag);
            case 2
                fprintf('\t \t Removing edge %d*-*%d\n', to, from);
                [isAncestor,stepAncestor] = warshall2(double(curDag));
            end
                bestDag = curDag;
            else 
            found= true;
            fprintf('Iteration %d: No score improvements\n', iter);
        end % end if maxscore is better
        
    end % end while

end % end function

function[newScore, curLocalScores] =removeDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores)
    dag(from,to) = 0;
    pa_to = find(dag(:, to))';
    curLocalScores(to) = bde(obsData, domainCounts, to, pa_to, 1);
    newScore = sum(curLocalScores);
end


function[newScore, curLocalScores] =addDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores)
    dag(from,to) = 1;
    pa_to = find(dag(:, to))';
    curLocalScores(to) = bde(obsData, domainCounts, to, pa_to, 1);
    newScore = sum(curLocalScores);
end