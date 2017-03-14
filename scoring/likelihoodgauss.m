function stats= likelihoodgauss(sem, covMat, hatCovMat, nVars, nSamples)
% function model= likelihoodgauss(sem, covMat, hatCovMat, nVars, nSamples)
% author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% sem                 = the (mixed causal) graph of a structural equation
%                       model
% covMat              = sample covariance matrix
% hatCovMat           = estimated covariance matrix
% nVars               = number of variables
% nSamples            = sample size
% =======================================================================
% Output
% =======================================================================
% stats                = A struct with teh following fields: 
% fml                    = fitted log likelihood of the model (Bollen,
%                           p.107)
% sll                    = log likelihood of the model (Bollen, p.133)
% bic                    = bic score of the model
% aic_c                  = aic_c for the model (aic+ correction for finite
%                           sample size
% nFreeParams            = number of free parameters of the model.
% dof                    = degrees of freedom

S =covMat/hatCovMat;
fml = logdet(hatCovMat) + trace(S) -logdet(covMat)-nVars;

covMatUB = covMat*((nSamples-1)/nSamples);
S =covMatUB/hatCovMat;
sll =-nSamples*nVars/2*log(2*pi)-(nSamples/2)*logdet(hatCovMat)-(nSamples/2)*trace(S);

% free parameters: nEdges+nVars;
nFreeParams = nnz(sem)/2+ nVars;

dof = nVars*(nVars+1)/2 - nFreeParams;

bic =-2*sll+(log(nSamples)*nFreeParams);


aic_c = -2*sll +2*nFreeParams+ (2*nFreeParams*(nFreeParams+1))/(nSamples-nFreeParams-1);

stats.fml = fml;
stats.sll = sll;
stats.bic= bic;
stats.aic_c = aic_c;
stats.nFreeParams = nFreeParams;
stats.dof = dof;