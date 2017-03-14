function [diffedges, diffendpoints, nExtra, nMissing] = diffEdgeEndpoints(pag, gtpag)

extra = find(triu(pag)&~triu(gtpag));
missing = find(~triu(pag)&triu(gtpag));
nExtra =length(extra);
nMissing = length(missing);
diffedges = nExtra+nMissing;
common =pag & gtpag;
 
diffendpoints = nnz(pag(common)-gtpag(common));