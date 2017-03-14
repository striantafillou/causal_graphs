function [precision, recall] = precisionRecall(pcg, gtpcg)
% [PRECISION, RECALL] = PRECISIONRECALL(PCG, GTPCG) Returns precision and
% recall for comparing a  pag to the ground truth pcg(as described in Tillman et al, learning
% equivalence classes...)
% precision = # of edges in pag1(with correct orientation)/#of edges in pag1
% recall = # of edges in pag1(with correct orientation)/# of edges in gtpag
% NOTE: if i understood correctly, they do not take under consideration
% whethet the edge is dashed or solid.

nEdgesPcg = nnz(pcg);
nEdgesGtPcg = nnz(gtpcg);
nCorrectEdges = nnz(((pcg-gtpcg)==0)& ((pcg'-gtpcg')==0) & ~~pcg);
%nCorrectEdges = nnz(~triu(pcg-gtpcg & pcg'-gtpcg')&~~pcg);
precision = nCorrectEdges/nEdgesPcg;
recall = nCorrectEdges/nEdgesGtPcg;
end

