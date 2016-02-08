function []= printedgesmcg(mcg)
% prints on screen edges of a mixed causal graph.
fprintf('---------------------\n')

nVars = length(mcg);
symbolsXY = {'o', '>', '-', 'z'};
symbolsYX = {'o', '<', '-', 'z'};
for X =1:nVars
    for Y =X+1:nVars
        if mcg(X, Y) 
            fprintf('%d %s-%s %d\n', X, symbolsYX{mcg(Y, X)},symbolsXY{mcg(X, Y)}, Y);
        end
    end
end
