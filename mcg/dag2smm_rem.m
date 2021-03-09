function [smm, arrows, tails] =  dag2smm_rem(graph, isLatent)
% function [SMM, ARROWS, TAILS] = DAG2SMM_REM(GRAPH, ISLATENT)
% Converts a DAG into a SMM after marginalizing latent variables and removes 
% latent dimensions Author: % striant@csd.uoc.gr remov
% =======================================================================
% Inputs
% =======================================================================
% graph                   = DAG adjacency matrix 
% isLatent                = nVars x 1 boolean vector, isLatent(i)= true if 
%                           $X_i$ is  latent
% =======================================================================
% Outputs
% =======================================================================
% mag                     = MAG matrix(see folder README for details)
% arrows                  = nVars x nVars boolean matrix, arrows(i, j)=
%                           true if  i*->j, false otherwise
% tails                   = nVars x nVars boolean matrix, tails(i, j)=
%                           true if  i*--j, false otherwise
% =======================================================================
dag =  sparse(graph);
%descendants(i, j) = 1 if j is a descendant of i.
nVars=  size(graph, 1);
smm= 2*graph+ 3*graph';
smm(isLatent, :)=0; smm(:, isLatent)=0;
arrows  = smm==2; tails =smm==3;
observedVars = ~isLatent;
latentDag = dag;
latentDag(observedVars, observedVars) = 0;
descendants = transitiveClosureSparse_mex(sparse(latentDag));

outOfLatent = zeros(nVars);
outOfLatent(isLatent, :) = dag(isLatent, :);
% where can you go only through latent variables?
throughLatent = transitiveClosureSparse_mex(sparse(outOfLatent));



for X = 1:nVars
    if isLatent(X)
        continue;
    end
    for Y =  X+1:nVars
        if isLatent(Y)
            continue;
        end
        if any(dag(X, isLatent) &  throughLatent(isLatent, Y)')
        %if descendants(X, Y)           
            arrows(X, Y) = 1;
            tails(Y, X) = 1;
        elseif any(dag(Y, isLatent) &  throughLatent(isLatent, X)')%descendants(Y, X);
            tails(X, Y) = 1;
            arrows(Y, X) = 1;
        end

        if any(sum(throughLatent(isLatent, [X, Y]), 2)==2)
            arrows(Y, X) = 1;
            arrows(X, Y) = 1;
        end
    end
end



smm(arrows&~tails) =2;
smm(tails&~arrows)=3;
smm(tails&arrows)=4;

smm= smm(~isLatent, ~isLatent);
        
end