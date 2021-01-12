function dataset = dag2dataset(dag, isLatent)
% makes pseudodataset from dag
if nargin==1
    isLatent= false(1, size(dag,1));
end
isObs =~isLatent;
isAnc = transitiveClosureSparse_mex(sparse(dag));
isAnc =  ~~isAnc(isObs, isObs);
dataset.isAncestor = isAnc;
dataset.mag = dag2mag(dag, isLatent);
end