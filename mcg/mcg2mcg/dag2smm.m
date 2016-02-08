function [smm, arrows, tails] =  dag2smm(graph, isLatent)
% function [SMM, ARROWS, TAILS] = DAG2SMM(GRAPH, ISLATENT)
% Converts a DAG into a SMM after marginalizing latent variables Author: %
% striant@csd.uoc.gr
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
[nVars, ~] =  size(graph);
smm= 2*graph+ 3*graph';
smm(isLatent, :)=0; smm(:, isLatent)=0;
arrows  = smm==2; tails =smm==3;
observedVars = ~isLatent;
latentDag = dag;
latentDag(observedVars, observedVars) = 0;
descendants = AllPairsDescendants_mex(sparse(latentDag));



for X = 1:nVars
    if isLatent(X)
        continue;
    end
    for Y =  X+1:nVars
        if isLatent(Y)
            continue;
        end
        if descendants(X, Y)           
            arrows(X, Y) = 1;
            tails(Y, X) = 1;
        elseif descendants(Y, X);
            tails(X, Y) = 1;
            arrows(Y, X) = 1;
        end
        if any(sum(descendants(isLatent, [X, Y]), 2)==2)
            arrows(Y, X) = 1;
            arrows(X, Y) = 1;
        end
    end
end



smm(arrows&~tails) =2;
smm(tails&~arrows)=3;
smm(tails&arrows)=4;
        
end