function [bestMag, bestScore, iter, gs] = greedySearchMagTABU(covMat, nSamples, tol)
    nVars=size(covMat,1);
    % begin from the empty mag
    isAncestor = false(nVars);
    stepAncestor = zeros(nVars);
    isParent= false(nVars);
    isBidir = false(nVars);
    whichPair=nan;

    % initially each node is a component
    mag =zeros(nVars); bestMag = mag;
    [nComps, sizes, comps, inComponent]= concomp(mag);
    nsf = -nSamples/2;
    scores = zeros(1,nComps);
    for iComp =1:nComps
        [compMag, district] = componentMag(comps{iComp}, nVars, mag, isParent);
        % scores(iComp) = logdet(2*pi*covMat(district, district))+(nSamples-1)/nSamples;
        scores(iComp) = score_contrib(compMag, comps{iComp}, district, sizes(iComp), covMat,nSamples, tol);
    end
    nEdges=0;
    tmpSll = nsf*sum(scores);
  
    curScore = -2*tmpSll+log(nSamples)*(nVars+nEdges);
    bestScore=curScore;
    iter =0;

    pairs = nchoosek(1:nVars, 2);
    nPairs = length(pairs);

    noChange=0;
    % Initialize TABU
    tabu = cell(100,1);
    tabu_cnt = 0;
    [tabu{:}]=deal(mag);

    while noChange<50
        %fprintf('Entering iteration %d, curScore: [%.4f], nComponents: %d, nEdges: %d\n', iter, curScore, nComps, nEdges);
        iter=iter+1;  
        allowed = true(nPairs, 4);
        stepScores = nan(nPairs, 4);
        scoreContribs = cell(nPairs, 4);
        for iPair =1:nPairs
            if iPair==whichPair % if you just changed this edge, continue
                continue;
            end
            from = pairs(iPair, 1); to = pairs(iPair, 2);
            %%fprintf('\n-----pair %d -  %d ------\n',from ,to);

            % do you create a cycle?
            if stepAncestor(from, to)>1
                %%fprintf('%d--->%d in the graph\n', from, to)
                allowed(iPair, 1:3) = [true false false];
            elseif stepAncestor(to, from)>1
                %%fprintf('%d--->%d in the graph\n', to, from)
                allowed(iPair, 1:3) = [false true false];
            end

            % can you create a cycle to--->bdFrom<->bdTo---->from
            [bdFrom, bdTo] = find(isBidir);
            for iBDedge=1:length(bdFrom)
                if isAncestor(from, bdFrom(iBDedge)) && isAncestor(bdTo(iBDedge), to)
                    allowed(iPair, 1)=false;
                elseif isAncestor(to, bdFrom(iBDedge)) && isAncestor(bdTo(iBDedge), to)
                    allowed(iPair, 2)=false;
                end
            end
            % is the edge already in the graph?
            if mag(from, to)==0 
                allowed(iPair, 4) = false;
            else
                if isParent(from, to)
                %%fprintf('%d->%d already in the graph\n', from, to)
                allowed(iPair, 1)=false;
                elseif isParent(to, from)
                %%fprintf('%d->%d already in the graph\n', to, from)
                allowed(iPair, 2)=false;
                elseif mag(from, to)==2 && mag(to, from)==2
                %%fprintf('%d<->%d already in the graph\n', from, to)
                allowed(iPair, 3)=false;
                end
            end
            % add edge from->to
            % you do not need to update the connected components, only rescore
            % component (from);
           % %fprintf(' \t 1 \t')
            if allowed(iPair, 1)
                 [stepScores(iPair, 1), scoreContribs{iPair, 1}] = ...
                     addDirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
            %else %fprintf('\t %d->%d not allowed\n',  from, to);
            end
            % add edge to->from
            %%fprintf(' \t 2 \t')
            if allowed(iPair, 2)
                  [stepScores(iPair, 2), scoreContribs{iPair, 2}] = ...
                      addDirectedEdge(to, from, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
            %else %fprintf('\t %d->%d not allowed\n', to, from);
            end
            % add edge from<->to        
            %%fprintf(' \t 3 \t')
            if allowed(iPair, 3)
                % update components
              [stepScores(iPair, 3), scoreContribs{iPair, 3}] = ...
                      addBidirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
            end
               %else %fprintf('\t %d<->%d not allowed\n', from, to);
            %%fprintf(' \t 4 \n')
            if allowed(iPair, 4)
                % if the edge is bidirected
                if mag(from,to)==2 && mag(to, from)==2
                    [stepScores(iPair, 4), scoreContribs{iPair, 4}] = ...
                        removeBidirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
                else
                    if mag(from, to)==2
                         [stepScores(iPair, 4), scoreContribs{iPair, 4}] = removeDirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
                    elseif mag(to, from)==2
                        [stepScores(iPair, 4), scoreContribs{iPair, 4}] = removeDirectedEdge(to, from, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
                    end
                end
                %else %fprintf('\t %d %d not allowed\n', from, to);
            end
        end % end for iPair
        
        
        % look for the best scoring mag not in the tabu list
        bestNotTabu=false;
        while ~bestNotTabu       
            % choose best performing action, update descendants, parents
            [minScores,actions] = nanmin(stepScores, [],  2);
            [minScore, whichPair] = nanmin(minScores);   
            from = pairs(whichPair,1); to= pairs(whichPair,2);
            if isTabu(from, to, actions(whichPair), mag, tabu)
               % %fprintf('Already visited this mag\n');
                stepScores(whichPair, actions(whichPair))=inf;
                continue;
            else bestNotTabu=true;
            end
        end

        %if minScore<curScore
        from = pairs(whichPair,1); to= pairs(whichPair,2);
        % flag= true you have removed a directed edge and you have to
        % update ancestor matrix
        flag=false;
        switch actions(whichPair)
        case 1
            %fprintf('\t \t Adding edge %d->%d\n', from, to);
            if mag(from, to)==3;
                flag=true; nEdges= nEdges-1;
            end
            mag(from, to)=2; mag(to, from)=3; nEdges= nEdges+1;
            [isParent, isAncestor, stepAncestor] = updateAncestors(from, to, mag, isParent, isAncestor, stepAncestor, flag);
        case 2
            %fprintf('\t \t Adding edge %d->%d\n', to, from);
            if mag(to, from)==3;
                flag=true; nEdges= nEdges-1;
            end
            mag(from, to)=3; mag(to, from)=2; nEdges= nEdges+1;
            [isParent, isAncestor, stepAncestor] = updateAncestors(to, from, mag, isParent, isAncestor, stepAncestor, flag);
        case 3
            %fprintf('\t \t Adding edge %d<->%d\n', to, from);
            [nComps, sizes, comps, inComponent] = updateConcomp(from, to, nComps, sizes, comps, inComponent);
            isBidir(from,to)=true;isBidir(to, from)=true;
            if mag(to, from)==3;
                isParent(from, to)=false;
                mag(to, from)=2;  nEdges= nEdges-1;
                isAncestor = findancestors(mag);
            elseif mag(from, to)==3
                isParent(to, from)=false;
                mag(from, to)=2; nEdges= nEdges-1;
                isAncestor=findancestors(mag);
            else
                mag(from, to)=2; mag(to, from)=2;  
            end
            nEdges= nEdges+1;
        case 4
            %fprintf('\t \t Removing edge %d*-*%d\n', to, from);
            [nComps, sizes, comps, inComponent] = updateConcompRem(from, to, nComps, sizes, comps, inComponent, mag);
             if mag(to, from)==3;
                mag(to, from)=0;mag(from, to)=0;
                isParent(from,to)=false;
                [isAncestor,stepAncestor] = warshall2(double(isParent));
            elseif mag(from, to)==3
                isParent(to, from)=false;
                mag(to, from)=0;mag(from, to)=0; 
                [isAncestor,stepAncestor] = warshall2(double(isParent));
             else
                mag(from, to)=0; mag(to, from)=0;  
                isBidir(from,to)=false;isBidir(to, from)=false;
             end
            nEdges= nEdges-1;
            isParent(from, to)= false;isParent(to, from)=false;
        end
        % update score.
        curScore = minScore;
        scores = scoreContribs{whichPair,actions(whichPair)};
        % update tabu list
        tabu{mod(tabu_cnt, 100)+1} = mag;
        tabu_cnt = tabu_cnt+1;

        % keep i-step matrices.
        gs(iter).stepAncestor = stepAncestor;
        gs(iter).isAncestor = isAncestor;
        gs(iter).score=curScore;
        gs(iter).mag=mag;

        if minScore<bestScore
            noChange=0;
            bestScore=minScore;
            bestMag = mag;
        else 
            noChange= noChange+1;
            %fprintf('Iteration %d: No score improvements, exiting greedy search\n', iter);
        end
    end % end while
%     figure;
%     plot(1:iter, [gs(:).score]);
end


function [newScore, scoreContribs, newNComps, newSizes, newComps, newInComponent, newMag] = addDirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol)
    
    % if the edge is bidirected you first have to update the components.
    if mag(to, from)==2 && mag(from, to)==2
       [~, scores, newNComps, newSizes, newComps, newInComponent, newMag] = removeBidirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol);
    else
        [newNComps, newSizes, newComps, newInComponent, newMag] = deal(nComps, sizes, comps, inComponent, mag);        
    end
    % change mag
    newMag(from, to)=2;newMag(to, from)=3;
    tmpIsParent = isParent;
    tmpIsParent(from, to)=true;
    % if the edge was directed in reverse, you must also update
    % inComponent(from);
    scoreContribs = scores;
    if mag(from, to)==3;
        tmpIsParent(to, from)=false;
        component= comps{inComponent(from)};
        [compMag, district] = componentMag(component, nVars, newMag, tmpIsParent);
        scoreContribs(inComponent(from)) = score_contrib(compMag, component, district, sizes(inComponent(from)), covMat, nSamples, tol);
    end 
    % update district
    component = comps{inComponent(to)}; 
    [compMag, district] = componentMag(component, nVars, newMag, tmpIsParent);
    % get new score
    scoreContribs(inComponent(to)) = score_contrib(compMag, component, district, sizes(inComponent(to)), covMat, nSamples, tol);
       
   
    tmpSll = (-nSamples/2)*sum(scoreContribs);
    if mag(from, to)==0
        nEdges = nEdges+1;
    end
    newScore =-2*tmpSll+log(nSamples)*(nVars+nEdges);
%     % test-remove
%     [~, ~, hatCovMat, ~] = RICF_fit(newMag, covMat, tol); 
%     stats = likelihoodgauss(newMag, covMat, hatCovMat, nVars, nSamples);
%     if tmpSll-stats.sll>tol
%         error('Something is wrong with scores in addDirectedEdge\n')
%     end
end

function [newScore, scoreContribs, newNComps, newSizes, newComps, newInComponent, newMag] = addBidirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol)

    % if edge was directed, update isParent
    if mag(to, from)==3 
        isParent(from, to)=false;
    elseif mag(from, to)==3
        isParent(to, from)=false;
    end
    [newNComps, newSizes, newComps, newInComponent, k, m] = updateConcomp(from, to, nComps, sizes, comps, inComponent);
    % add edge
    newMag = mag;
    newMag(to, from)=2;newMag(from, to)=2;
    component= newComps{k};
    [compMag, district] = componentMag(component, nVars, newMag, isParent);
    % keep old scores
    keepScores =[1:m-1, m+1:nComps];
    if k<m
        newScores = scores(keepScores);
    else
        newScores= scores;
    end
    newScores(k)  = score_contrib(compMag, component, district, newSizes(k), covMat, nSamples,  tol);
    scoreContribs = newScores;
    
    tmpSll = (-nSamples/2)*sum(newScores);
    tmp_nEdges = nEdges+1;
    newScore =-2*tmpSll+log(nSamples)*(nVars+tmp_nEdges);
    
%     % REMOVE
%     [~, ~, hatCovMat, ~] = RICF_fit(newMag, covMat, tol); 
%     stats = likelihoodgauss(newMag, covMat, hatCovMat, nVars, nSamples);
%     % REMOVE
%     if tmpSll-stats.sll>tol
%         error('Something is wrong with scores in 3\n')
%     end

end


function [newScore, scoreContribs, nComps, sizes, comps, inComponent, newMag] = removeDirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol)
    newMag = mag;
    newMag(from, to)=0;newMag(to, from)=0;
    % update district
    tmpIsParent = isParent;
    tmpIsParent(from, to)=false;
    component = comps{inComponent(to)}; 
    [compMag, district] = componentMag(component, nVars, newMag, tmpIsParent);
    % get new score
    scoreContribs = scores;
    scoreContribs(inComponent(to)) = score_contrib(compMag, component, district, sizes(inComponent(to)), covMat, nSamples, tol);
    
    tmpSll = (-nSamples/2)*sum(scoreContribs);
    tmp_nEdges = nEdges+1;
    newScore =-2*tmpSll+log(nSamples)*(nVars+tmp_nEdges);
%     % test-remove
%     [~, ~, hatCovMat, ~] = RICF_fit(newMag, covMat, tol); 
%     stats = likelihoodgauss(newMag, covMat, hatCovMat, nVars, nSamples);
%     % test-remove
%     if tmpSll-stats.sll>tol
%         error('Something is wrong with scores in removeDirectedEdge\n')
%     end
end


function [newScore, scoreContribs, newNComps, newNsizes, newComps, newInComponent, newMag] = removeBidirectedEdge(from, to, nEdges, nComps, sizes, comps, inComponent, mag, covMat, isParent, scores, nSamples, nVars,  tol)
    % remove edge
    newMag = mag;
    newMag(to, from)=0;newMag(from, to)=0;
    % update components 
    [newNComps, newNsizes, newComps, newInComponent, k, m] = updateConcompRem(from, to, nComps, sizes, comps, inComponent, mag);
    % keep old scores
    if isnan(m) % if you have not split the component.
        tmp_scores=scores;
    else
        keepScores =[1:k-1, k+1:m-1, m+1:newNComps];
        tmp_scores = nan(1, newNComps);
        tmp_scores(keepScores) = scores([1:k-1,k+1:nComps]);
        % update m-score
        component= newComps{m};
        [compMag, district] = componentMag(component, nVars, newMag, isParent);
        tmp_scores(m)  = score_contrib(compMag, component, district, newNsizes(m), covMat, nSamples,  tol);
    end
    %update k-score
    component = newComps{k};
    [compMag, district] = componentMag(component, nVars, newMag, isParent);
    tmp_scores(k)  = score_contrib(compMag, component, district, newNsizes(k), covMat, nSamples,  tol);

    scoreContribs = tmp_scores;
    
    % calculate new score
    tmpSll = (-nSamples/2)*sum(tmp_scores);
    tmp_nEdges = nEdges-1;
    newScore =-2*tmpSll+log(nSamples)*(nVars+tmp_nEdges);
    
%     % check if new score is correct - REMOVE 
%     [~, ~, hatCovMat, ~] = RICF_fit(newMag, covMat, tol); 
%     stats = likelihoodgauss(newMag, covMat, hatCovMat, nVars, nSamples);
%     if tmpSll-stats.sll>tol
%         error('Something is wrong with scores in 4\n')
%         return;
%     end
end

function [isParent, isAncestor, stepAncestor] = updateAncestors(from, to, mag, isParent, isAncestor, stepAncestor, flag)

    % flag=true : You have removed a directed edge and you have to recompute all ancestors
    if flag
        isParent(to, from)=false;
        isParent(from, to)= true;
        [isAncestor, stepAncestor]= warshall2(isParent);
    else
        isParent(from, to)= true;
        Anc_from = isAncestor(:, from);
        Desc_to = isAncestor(to, :);

        stepAncestor(Anc_from, to) = 2;
        stepAncestor(Anc_from, Desc_to) = 2;
        stepAncestor(from, Desc_to)= 2;

        Anc_from(from)= true;
        Desc_to(to)= true;
        isAncestor(Anc_from, Desc_to)= true;
    end
    %     isAncestor_tmp= findancestors(mag);
    % if ~isequal(isAncestor, isAncestor_tmp)
    %     error('Something is wrong with updating ancestors\n')
    % end
end