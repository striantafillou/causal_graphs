 function [beta, omega, hatCovMat, ricf] = RICF(smm, ds, tol)
 
 
isLatent = ds.isLatent;
smm = smm(~isLatent, ~isLatent);
covMat = cov(ds.data(:, ~isLatent));

[nSamples, nVars] = size(ds.data, 2);

[beta, omega, hatCovMat] = deal(zeros(nVars));
[beta_tmp, omega_tmp, hatCovMat_tmp, ricf] = RICF_fit(smm, covMat, tol);

[ricf.fmls, ricf.sll, ricf.bic, ricf.nFreeParams, ricf.dof]= likelihoodgauss(sem, covMat, hatCovMat, nVars, nSamples);


% reshape to include latent variables.
beta(~isLatent, ~isLatent) = beta_tmp;
omega(~isLatent, ~isLatent)=  omega_tmp;
hatCovMat(~isLatent, ~isLatent) = hatCovMat_tmp;

end
