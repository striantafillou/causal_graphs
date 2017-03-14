function [compMag, district, parents] = componentMag(component, nVars, mag, isParent)
% creates the component mag for scoring Maximal Ancestral Graphs, which
% consists of : 1) the bi-directed component and 2) edges into the
% bidirected component.

compMag = zeros(nVars);
compMag(component, component) = mag(component, component);
[district, parents]= deal(false(1, nVars));
district(component) = true;
for iVar = component
    iParents = isParent(:, iVar);
    compMag(iParents, iVar)=2;
    compMag(iVar,iParents)=3;
    district(iParents) = true;
    parents(iParents)=true;
end
        