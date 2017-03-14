function sc = score_contrib(compMag, component, district, compSize, covMat, nSamples,  tol)
if sum(district)==1
    sc = logdet(2*pi*covMat(district, district))+(nSamples-1)/nSamples;
else
    curCovMat = covMat(district, district);
    compMag = compMag(district, district);
    [~, ~, curHatCovMat, ~] = RICF_fit(compMag, curCovMat, tol);
    remParents=district;
    remParents(component)=false;
    parInds = remParents(district);

    if any(remParents)
        l1 = compSize*log(2*pi);
        l2 = logdet(curHatCovMat) - log(prod(diag(curHatCovMat(parInds, parInds))));
        l3 = (nSamples-1)/nSamples*(trace(curHatCovMat\curCovMat)-sum(remParents));
        sc = l1+l2+l3;
    else
        l1 = compSize*log(2*pi);
        l2 = logdet(curHatCovMat);
        l3 = (nSamples-1)/nSamples*trace(curHatCovMat\curCovMat);
        sc = l1+l2+l3;
    end
end
