function [dag, nExtraVars] = smm2dag(smm)
% FUNCTION [DAG, NEXTRAVARS] = SMM2DAG(SMM)
% Converts a SMM into THE 'CANONICAL DAG' (ZHANG 2008)
% Author:  striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% SMM                     = SMM  matrix (see folder README for details)
% isLatent                = nVars x 1 boolean vector, isLatent(i)= true if 
%                           $X_i$ is  latent
% =======================================================================
% Outputs
% =======================================================================
% dag                     = DAG adjacency matrix
% nExtraVars              = number of extra variables representing
%                           confounders
% =======================================================================

nVars = length(smm);
[Xs, Ys] = find(triu((smm==2||smm ==4) & (smm'==2||smm'==4)));
nExtraVars = length(Xs);
dag=zeros(nVars);

dag((smm==3)')=1;

for iEdge=1:length(Xs)
    extraVar = nVars+iEdge;
    X =Xs(iEdge); Y = Ys(iEdge);
    dag(extraVar, X)=1;
    dag(extraVar, Y)=1;
    dag(:, extraVar)= 0;
    dag(:, extraVar)=0;
end
    