% ricf examples
clear
dag = zeros(5);

dag(1,3)=1; 
dag(1,2)=1;
dag(2,3)=1;
dag(3,4)=1;
dag(5,4)=1;
dag(5,2)=1;

type = 'gaussian';
isLatent=[false false false false true];
nSamples =1000;

bn = dag2randBN(dag, type);
ds  = simulatedata(bn, nSamples, type, 'isLatent', isLatent);


%%
tol = 10^-6;
covMat = cov(ds.data(:, ~isLatent));
mag = dag2mag(dag, isLatent);
mag_marg = mag(~isLatent, ~isLatent);
nVars = sum(~isLatent);
smm = dag2smm(dag, isLatent);
smm_marg =smm(~isLatent, ~isLatent);

[beta, omega, hatCovMat1, ricf] = RICF_fit(mag_marg, covMat, tol);
statsMag = likelihoodgauss(mag_marg, covMat, hatCovMat1, nVars, nSamples);

[beta, omega, hatCovMat2, ricf] = RICF_fit(smm_marg, covMat, tol);
statsSmm = likelihoodgauss(smm_marg, covMat, hatCovMat2, nVars, nSamples);
%%
mag_bidir = mag_marg==2 & mag_marg'==2;
[nComps, sizes, comps]= concomp(mag_bidir);
isParent= mag_marg==2 & mag_marg'==3;
nVars = size(mag_marg,1);

for iComp=1:nComps
    [curComp, curDistrict, curParents] = deal(false(1, nVars));
    curMag = zeros(nVars);
    
    curComp(comps{iComp}) = true;
    curDistrict(curComp)=true;
    
    curMag(curComp, curComp) = 2;
    curMag(1:nVars+1:nVars^2) = 0;
    
    for iVar = comps{iComp}
        iParents = isParent(:, iVar);
        curMag(iParents, iVar)=2;
        curMag(iVar,iParents)=3;
        curDistrict(iParents) = true;
        curParents(iParents)=true;
    end
    printedgesmcg(curMag);    
    curCovMat = covMat(curDistrict, curDistrict);
    
    [curBeta, curOmega, curHatCovMat, curRicf] = RICF_fit(curMag(curDistrict, curDistrict), curCovMat, tol);
    inds =curComp(curDistrict);

    compInds = curComp(curDistrict);
    parInds = curParents(curDistrict);
    sll_tmp(iComp) =logdet(2*pi*curHatCovMat)+(nSamples-1)/nSamples*trace(curHatCovMat\curCovMat);
    if any(curParents)
        l1 = sum(curComp)*log(2*pi);
        l2 = logdet(curHatCovMat) - log(prod(diag(curHatCovMat(parInds, parInds))));
        l3 = (nSamples-1)/nSamples*(trace(curHatCovMat\curCovMat)-sum(curParents));
        sll_contribs(iComp) = l1+l2+l3;
    else
        l1 = sum(curComp)*log(2*pi);
        l2 = logdet(curHatCovMat);
        l3 = (nSamples-1)/nSamples*trace(curHatCovMat\curCovMat);
        sll_contribs(iComp) = l1+l2+l3;
    end

  sll_dec = sum(sll_contribs)*(-nSamples)/2
    
end