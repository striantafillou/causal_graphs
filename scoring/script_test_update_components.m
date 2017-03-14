clc

nVars=50;
emptyMag = zeros(nVars);
[nComps, compSizes, comps, inComponent]= concomp(emptyMag);

pairs = nchoosek(1:nVars, 2);
nPairs= length(pairs);

edges = randsample(1:nPairs, 100);

% try adding edges.
for iEdge=edges;
 
    from=pairs(iEdge, 1); to =pairs(iEdge, 2);
    [nComps, compSizes, comps, inComponent, k, m] = updateConcomp(from, to, nComps, compSizes, comps, inComponent);
    fprintf('Adding edge %d-%d, number of Components:%d \n', from, to, nComps);
 
    for iComp=1:nComps
        if ~isequal(find(inComponent==iComp), comps{iComp})
            fprintf('Something is wrong with component %d\n', iComp); 
            return;
        end
    end
end

%%
clear all
clc 
nVars=50;


pairs = nchoosek(1:nVars, 2);
nPairs= length(pairs);
% 
for iter=1:100
 edges = randsample(1:nPairs, nPairs);

fullMag= 2*ones(nVars);
[nComps, compSizes, comps, inComponent]= concomp(fullMag);
% try adding edges.
for iEdge=edges;
 
    from=pairs(iEdge, 1); to =pairs(iEdge, 2);
    [nComps, compSizes, comps, inComponent, k, m] = updateConcompRem(from, to, nComps, compSizes, comps, inComponent, fullMag);
    fprintf('Removing edge %d-%d, number of Components:%d \n', from, to, nComps);
 
    for iComp=1:nComps
        if ~isequal(find(inComponent==iComp), comps{iComp})
            fprintf('Something is wrong with component %d\n', iComp); 
            return;
        end
    end
    fullMag(from, to)=0; fullMag(to, from)=0;
    [nComps2, compSizes2, comps2, inComponent2] = concomp(fullMag);[nComps2, compSizes2, comps2, inComponent2] = lexSortComponents(nComps2, compSizes2, comps2, inComponent2);
     
    if nComps2~=nComps
        fprintf('Unequal component sizes\n');
        return;
    else
        for iComp=1:nComps
        if ~isequal(comps{iComp}, sort(comps2{iComp}))
         fprintf('Something is wrong\n')
        return;
        end
        end
    end 
end

end
