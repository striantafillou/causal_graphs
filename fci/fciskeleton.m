function [pag, sepSet,pvalues, maxSepSet, nTests] = fciskeleton(dataset, test, heuristic, alpha, maxK,  pdSep, verbose)
% [pag, sepSet,pvalues, maxSepSet, nTests] = fciskeleton(dataset, test, alpha, maxK,  pdSep, verbose)
%Runs FCI skeleton with maxK  and alpha 
nVars =  size(dataset.data, 2);
isLatent = dataset.isLatent;
% allpvalues= nan(numVars*numVars, 1);
% allpvalueCounter =0;
%Step 0 Initialize variables.
pag = ones(nVars, nVars);
pag(isLatent, :)=0;
pag(:, isLatent)=0;
% set diagonal equal to zero;
pag(1:nVars+1:nVars^2) = 0; 
[sepSet, maxSepSet] =  deal(zeros(nVars, nVars, nVars));

nTests = 0;
pvalues = -ones(nVars, nVars);
unistats = -ones(nVars, nVars);
% dummy variable to denote whether we have checked an edge. Initially has
% zeros everywhere except for the main diagonal.
flagPag =  ones(size(pag));
flagPag =  ~(tril(flagPag,-1)+ triu(flagPag, 1));

%Step 1. skeleton search
%Step 1a. find unconditional independencies
for X  =  1:nVars;
    if isLatent(X)
        continue;
    end
    for Y =  X+1:nVars;
        if isLatent(Y)
            continue;
        end       
        [p, s, exitflag] = feval(test, X, Y, [], dataset); 
        if p>alpha
            pag(X, Y) = 0;
            pag(Y, X) = 0;
            flagPag(X, Y) = 1;
            flagPag(Y, X) = 1;            
            if verbose 
                fprintf('\t Independence accepted: %d _||_ %d , %s\n', X, Y, num2str(p))
            end
        end
        pvalues(X, Y)=p;
        pvalues(Y, X)=p;
        unistats(X, Y)= s;
        unistats(Y, X)=s;
    end
end


if maxK  == -1
    maxCondSetSize =  nVars -2;
else
    maxCondSetSize =  maxK;
end

unips = pvalues;
n=0;
while n<maxCondSetSize 
    n = n+1;
    if verbose
        fprintf('\t-------------k=%d---------\n', n);
    end
    % find edges in lexicographic order
    [Ys,Xs]=find(tril(pag));
    % for heuristic 2/3 sort edges by increasing order of association,
    % which means: higher p-values first, lower statistics first.
    if heuristic==3||heuristic==2
        edgepvals = unips(sub2ind([nVars nVars], Xs, Ys));
        edgestats = unistats(sub2ind([nVars nVars], Xs, Ys));
        [~, ord] = sortrows([-edgepvals edgestats]);
        Ys = Ys(ord); Xs= Xs(ord);
    else ord=1:length(Xs);

    end
    for iEdge =1:length(ord);
        X=Xs(iEdge); Y = Ys(iEdge);
        if verbose
            fprintf('\t Checking %d-%d, p-value %.3f\n', X, Y, unips(X, Y));
        end
        tmpPag = pag;
        tmpPag(X, Y) = 0; tmpPag(Y, X)=0;
        condsetsX =[]; condsetsY =[]; strengthX =[]; strengthY =[]; 
        neighborsX =  find(tmpPag(X, :));
        if length(neighborsX)>=n
            condsetsX = nchoosek(neighborsX, n);
            if heuristic==3
                strengthX = [unips(condsetsX(:, 1), X), -unistats(condsetsX(:, 1), X)];
            end
        end

        neighborsY  = find(tmpPag(Y, :));
        if length(neighborsY)>=n
            condsetsY = nchoosek(neighborsY, n);
            if heuristic==3
                strengthY = [unips(condsetsY(:, 1), Y), -unistats(condsetsY(:, 1), Y)];
            end
        end
        condsets = [condsetsX; condsetsY];

        % if heuristic 3, sort conditioning set in deacreasing order of
        % association, which means: lower p-values first, larger statistics
        % first.
        if heuristic==3
            strengthCondsets = [strengthX; strengthY];
            [strengthCondsets, csOrd] = sortrows(strengthCondsets);
            condsets = condsets(csOrd, :);
        end
       [condsets, uniqueOrd]= unique(condsets, 'rows', 'stable');

        for iCS = 1:size(condsets, 1)
            condset = condsets(iCS, :);
            if verbose
                if heuristic==3
                     fprintf('\t \t with set %s, strength %.3f\n', num2str(condset), strengthCondsets(uniqueOrd(iCS), 1));
                else 
                    fprintf('\t \t with set %s\n', num2str(condset));
                end 
            end
            [p, ~, exitflag] = feval(test, X, Y, condset, dataset);
            nTests =nTests+1;
            if p>alpha              
                pag(X, Y) = 0; pag(Y, X) = 0;
                flagPag(X, Y) = 1; flagPag(Y, X) = 1;  
                % report sepset
                sepSet(X, Y, condset)=1; sepSet(Y, X, condset)=1; 
                maxSepSet(X, Y, :)=0; maxSepSet(Y, X, :)=0;maxSepSet(X, Y, condset)=1; maxSepSet(Y, X, condset)=1;
                pvalues(X, Y) = p;
                pvalues(Y, X) = p;
                if verbose 
                    fprintf('\t\t\t Accepted, p-value %s\n', num2str(p))
                end
                break;
            else
%                 if debug
%                     fprintf('\t\t\t Independence NOT accepted, p-value %s\n', num2str(p))
%                 end
                % TODO here maybe it is better to compare stats in case of
                % ties.
                [pvalues(X, Y), ind] = max([pvalues(X, Y), pvalues(Y, X), p]); pvalues(Y, X) = pvalues(X, Y);
                if ind ==3
                    maxSepSet(X, Y, :) = 0; maxSepSet(Y, X, :) = 0; maxSepSet(X, Y, condset) =1; maxSepSet(Y, X, condset)=1;
                end
            end % end if p
        end % end for iCS
    end % end for iEdge
end % end while n<maxK
    
    % Implement possibly d-separating set
if pdSep==1 
    if verbose
        fprintf('\t-------------pdSep Stage---------\n');
    end
    [pag, ~,  ~] = R0(pag, sepSet, verbose);
    % you need to look edges both ways.
    [Ys, Xs] = find(pag);
    for iEdge=1:length(Xs)
        X =Xs(iEdge); Y = Ys(iEdge);
        % if in the meantime you found the independence
        if flagPag(X, Y) == 1
            continue;
        end
        if verbose
            fprintf('\t Checking %d-%d, p-value %.3f\n', X, Y, pvalues(X, Y));
        end
        pdSepSet =  findpdsepset(pag, X, Y, false);
        if verbose
            fprintf('\t Pdseparating set %s\n', num2str(pdSepSet));
        end
        if isempty(pdSepSet)
            continue;
        end
        for n =2:maxCondSetSize
           pdSepSets = nchoosek(pdSepSet, n);
            for iPDS =1:size(pdSepSets, 1)
                condset = pdSepSets(iPDS, :);
                % if set constitutes only of neighbors it has been
                % checked again, so skip
                if all(pag(X, condset))  
                    continue;
                else
                    if verbose
                    fprintf('\t \t with  set %s\n', num2str(condset));
                    end
                    [p, ~, exitflag] = feval(test,X, Y, condset, dataset);
                    nTests =nTests+1;
                    if p>alpha 
                        pag(X, Y) = 0; pag(Y, X) = 0;
                        flagPag(X, Y) = 1; flagPag(Y, X) = 1;  
                        % report sepset
                        sepSet(X, Y, condset)=1; sepSet(Y, X, condset)=1; 
                        maxSepSet(X, Y, :)=0; maxSepSet(Y, X, :)=0;maxSepSet(X, Y, condset)=1; maxSepSet(Y, X, condset)=1;
                        pvalues(X, Y) = p;
                        pvalues(Y, X) = p;
                        if verbose 
                            fprintf('\t\t Independence accepted, p-value %s\n', num2str(p))
                        end
                    else
%                         if verbose
%                         fprintf('\t\t Independence NOT accepted, p-value %s\n', num2str(p))
%                         end
                        [pvalues(X, Y), ind] = max([pvalues(X, Y), pvalues(Y, X), p]); pvalues(Y, X) = pvalues(Y, X);
                        if ind ==3
                            maxSepSet(X, Y, :) = 0; maxSepSet(Y, X, :) = 0; maxSepSet(X, Y, condset) =1; maxSepSet(Y, X, condset)=1;
                        end
                    end % end if p
                end % end if all
            end % end for iPDS
        end % end for
    end % end for iEdge
end % end if pdSep

end % end  function


% pvalues = max(pvalues, pvalues');

            
