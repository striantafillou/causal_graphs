% test if decomposed likelihood is always the same as the full one
clear;
clc;
nVars = 20;
nSamples=1000;
maxLatent = 2;
maxParents = 4;
tol = 10^-6;


for iter=1:100
    fprintf('Iteration %d\n', iter)
    % generate random DAG.
    dag = randomdag(nVars, maxParents);

    % choose latent variables.
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;
    % simulate data, estimate cov_mat
    bn = dag2randBN(dag, 'gaussian');
    ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);

    % create mag
    mag = dag2mag(dag, isLatent);
    mag_marg = mag(~isLatent, ~isLatent);
    nVars_marg = sum(~isLatent);

    % run RICF
    covMat_marg = cov(ds.data(:, ~isLatent));
    [beta_marg, omega_marg, hatCovMat_marg, ricf_marg] = RICF_fit(mag_marg, covMat_marg, tol);
    stats = likelihoodgauss(mag_marg, covMat_marg, hatCovMat_marg, nVars_marg, nSamples);

    % decompose and run RICF
   
    mag_bidir = mag_marg==2 & mag_marg'==2;
    if ~any(any(mag_bidir))
        fprintf('no bidirected components\n')
    end
    [nComps, sizes, comps]= concomp(mag_bidir);
    isParent= mag_marg==2 & mag_marg'==3;
    sll_contribs = nan(nComps, 1);
    for iComp=1:nComps
        [curComp, curDistrict, curParents] = deal(false(1, nVars_marg));
        curMag = zeros(nVars_marg);

        curComp(comps{iComp}) = true;
        curDistrict(curComp)=true;

        curMag(curComp, curComp) = mag_marg(curComp, curComp);

        for iVar = comps{iComp}
            iParents = isParent(:, iVar);
            curMag(iParents, iVar)=2;
            curMag(iVar,iParents)=3;
            curDistrict(iParents) = true;
            curParents(iParents)=true;
        end
        %printedgesmcg(curMag);    
        curCovMat = covMat_marg(curDistrict, curDistrict);

        [curBeta, curOmega, curHatCovMat, curRicf] = RICF_fit(curMag(curDistrict, curDistrict), curCovMat, tol);
        inds =curComp(curDistrict);
        
        % you must remove marginal likelihood of parents that are not in
        % the district (because you will add them in another component)
        remParents=curParents;
        remParents(curComp)=false;
        compInds = curComp(curDistrict);
        parInds = remParents(curDistrict);
        if any(curParents)
            l1 = sum(curComp)*log(2*pi);
            l2 = logdet(curHatCovMat) - log(prod(diag(curHatCovMat(parInds, parInds))));
            l3 = (nSamples-1)/nSamples*(trace(curHatCovMat\curCovMat)-sum(remParents));
            sll_contribs(iComp) = l1+l2+l3;
        else
            l1 = sum(curComp)*log(2*pi);
            l2 = logdet(curHatCovMat);
            l3 = (nSamples-1)/nSamples*trace(curHatCovMat\curCovMat);
            sll_contribs(iComp) = l1+l2+l3;
        end
    end
    % compare scores

    sll_dec = sum(sll_contribs)*(-nSamples/2);
    if sll_dec-stats.sll>10^-10
        fprintf('Different scores: %.5f vs %.5f\n', sll_dec, stats.sll);
        
    end

end


%%
% test if decomposed likelihood is always the same as the full one
clear;
clc;
nVars = 20;
nSamples=1000;
maxLatent = 2;
maxParents = 4;
tol = 10^-6;


for iter=1:100
    fprintf('Iteration %d\n', iter)
    % generate random DAG.
    dag = randomdag(nVars, maxParents);

    % choose latent variables.
    nLatent = randi(maxLatent);
    isLatent = false(nVars, 1);
    isLatent(randsample(1:nVars, nLatent)) = true;
    % simulate data, estimate cov_mat
    bn = dag2randBN(dag, 'gaussian');
    ds = simulatedata(bn, nSamples, 'gaussian', 'isLatent', isLatent);

    % create mag
    mag = dag2mag(dag, isLatent);
    mag_marg = mag(~isLatent, ~isLatent);
    nVars_marg = sum(~isLatent);

    % run RICF
    covMat_marg = cov(ds.data(:, ~isLatent));
    [beta_marg, omega_marg, hatCovMat_marg, ricf_marg] = RICF_fit(mag_marg, covMat_marg, tol);
    stats = likelihoodgauss(mag_marg, covMat_marg, hatCovMat_marg, nVars_marg, nSamples);

    % decompose and run RICF
   
    mag_bidir = mag_marg==2 & mag_marg'==2;
    if ~any(any(mag_bidir))
        fprintf('no bidirected components\n')
    end
    [nComps, sizes, comps]= concomp(mag_bidir);
    isParent= mag_marg==2 & mag_marg'==3;
    sll_contribs = nan(nComps, 1);
    for iComp=1:nComps
        component = comps{iComp};
        [compMag, district] = componentMag(component, nVars_marg, mag_marg, isParent);
        sll_contribs(iComp) = score_contrib(compMag, component, district, sizes(iComp), covMat_marg, nSamples, tol);
    end

    % compare scores

    sll_dec = sum(sll_contribs)*(-nSamples/2);
    if sll_dec-stats.sll>10^-10
        fprintf('Different scores: %.5f vs %.5f\n', sll_dec, stats.sll);
        
    end

end


