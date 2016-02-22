function [sPrec, sRec, oPrec, oRec] = comparepags(pag, GT)
% function [sPrec, sRec, oPrec, oRec] = comparepags(pag, GT)
% returns structural/ orientation precision recall  of pag against ground
% truth pag:

if ~isstruct(pag)
    fprintf('Warning: nan pag\n');
     [sPrec, sRec, oPrec, oRec] = deal(nan);
     return;
end
predEdges = ~~pag.graph;
actualEdges = ~~GT;

sPrec = (nnz(predEdges & actualEdges))/nnz(predEdges);
sRec = (nnz(predEdges & actualEdges))/nnz(actualEdges);


predArrows = pag.graph ==2;
predTails = pag.graph ==3;
actualArrows = GT ==2;
actualTails = GT==3;


oPrec = (nnz((predArrows & actualArrows)| (predTails & actualTails)))/(nnz(pag.graph>1));
oRec = (nnz((predArrows & actualArrows)| (predTails & actualTails)))/(nnz(actualArrows|actualTails));
end