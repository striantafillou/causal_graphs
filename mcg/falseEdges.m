function [fpEdges, fnEdges] = falseEdges(mcg, gtmcg)
%[fpEdges, fnEdges] = falseEdges(mcg, gtmcg)
% false positive, false negative edges of mcg compared to ground truth
% gtmcg

if isstruct(mcg)
    mcg = mcg.graph;
end
if isstruct(gtmcg)
    gtmcg= gtmcg.graph;
end
% keep only structure
mcg =~~mcg;gtmcg=~~gtmcg;

% false positive edges: edges in mcg and not gt
[x, y]= find(triu(mcg&~gtmcg));
fpEdges =[x, y];

% false negative edges: edges in gt and not mcg
[x, y]= find(triu(~mcg&gtmcg));
fnEdges =[x, y];


end