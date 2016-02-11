function []= printedgesdag(dag)
% FUNCTION [] = PRINTEDGSDAG(DAG)
% Prints DAG edges on screen.
% striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% dag                     = DAG adjacency matrix
% =======================================================================
fprintf('---------------------\n')

nVars = length(dag);
symbolsXY = {'-', '>'};
symbolsYX = {'-', '<'};
for X =1:nVars
    for Y =X+1:nVars
        if dag(X, Y)||dag(Y, X)
            fprintf('%d %s-%s %d\n', X, symbolsYX{dag(Y, X)+1},symbolsXY{dag(X, Y)+1}, Y);
        end
    end
end
end