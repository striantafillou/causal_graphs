function mag = smm2mag(smm)
% function [MAG] = SMM2MAG(SMM)
% Converts an SMM  into a MAG over the same variables
% Author:  striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% smm                     = SMCM matrix(see folder README file for details)
% =======================================================================
% Outputs
% =======================================================================
% mag                     = MAG matrix(see folder README file for details)
% =======================================================================
nVars = size(smm,1);
mag = zeros(nVars);
dag =  zeros(size(smm));
[directedEdgesX, directedEdgesY] =  find(smm ==2 & smm' ~=2);
dag(sub2ind(size(dag),directedEdgesX,directedEdgesY))=1;    
    
isAncestor = transitiveClosureSparse_mex(sparse(dag));
isAncestor =  ~~isAncestor;
isLatent= false(nVars, 1);
for X = 1:nVars-1
    for Y =  X+1:nVars 
        % if the pair are adjacent in the smm or 3 inducing path given empty set
        if smm(X, Y) || hasinducingpathmcg(X, Y, smm, isAncestor, isLatent, false);
            if isAncestor(X, Y);
                mag(X, Y) = 2;
                mag(Y, X) = 3;
            elseif isAncestor(Y, X);
                mag(Y, X) = 2;
                mag(X, Y) = 3;
            else 
                mag(X, Y) =2; mag(Y, X) =2;
            end
        end
    end
end
    
