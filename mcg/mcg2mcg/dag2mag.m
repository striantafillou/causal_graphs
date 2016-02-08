function [mag, arrows, tails] =  dag2mag(dag, isLatent)
% function [MAG, ARROWS, TAILS] = DAG2MAG(GRAPH, ISLATENT)
% Converts a DAG into a MAG after marginalizing latent variables Author: %
% striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% dag                   = DAG adjacency matrix 
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

dag =  sparse(dag);
%descendants(i, j) = 1 if j is a descendant of i.
isAncestor = transitiveClosureSparse_mex(sparse(dag));
[nVars, ~] =  size(dag);
mag =  zeros(nVars);
mag(isLatent, :) = 0;
mag(:, isLatent) = 0;
confounded = zeros(nVars);
for X = 1:nVars
    if isLatent(X)
        continue;
    end
    for Y =  X+1:nVars
        if isLatent(Y)
                continue;
        end
        if dag(X, Y) == 1;
            mag(X, Y) = 2;
            mag(Y, X) = 3;
        elseif dag(Y, X) == 1;
            mag(Y, X) = 2;
            mag(X, Y) =3;
        elseif hasinducingpathdag(X, Y, dag, isAncestor, isLatent, false)
            if isAncestor(X, Y)==1
                mag(X, Y) = 2;
                mag(Y, X) = 3;
            elseif isAncestor(Y, X)==1
                mag(Y, X) = 2;
                mag(X, Y) = 3;
            else
                mag(X, Y) = 2;
                mag(Y, X) = 2;                
            end
        end
        if any(sum(dag(isLatent, [X, Y]), 2)==2)
            confounded([X, Y], [X, Y]) =[0 1; 1 0];
        end
            
    end
end

tails = isAncestor';
arrows = isAncestor|confounded;
end
        
