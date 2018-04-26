function mag = ag2mag(ag)
% function [MAG] = ag2MAG(ag)
% Converts an ag  into a MAG over the same variables
% Author:  striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% ag                     = ancestral graph(same representation as MAG)
% ======================================================================
% Outputs
% =======================================================================
% mag                     = MAG matrix(see folder README file for details)
% =======================================================================
nVars = size(ag,1);
mag = zeros(nVars);
dag =  zeros(size(ag));
[directedEdgesX, directedEdgesY] =  find(ag ==2 & ag' ==3);
dag(sub2ind(size(dag),directedEdgesX,directedEdgesY))=1;    
    
isAncestor = transitiveClosureSparse_mex(sparse(dag));
isAncestor =  ~~isAncestor;
isLatent= false(nVars, 1);
for X = 1:nVars-1
    for Y =  X+1:nVars 
        % if the pair are not adjacent in the ag and 3 inducing path given empty set
        if ~ag(X, Y) && hasinducingpathmcg(X, Y, ag, isAncestor, isLatent, false)
            if ~isAncestor(X, Y)&&~isAncestor(Y, X)
            end
        end
    end
end
    
