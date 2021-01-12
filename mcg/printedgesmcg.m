function []= printedgesmcg(mcg, headers)
% prints on screen edges of a mixed causal graph.
fprintf('---------------------\n')

if nargin>1
    nVars = length(mcg);
    symbolsXY = {'o', '>', '-', 'z'};
    symbolsYX = {'o', '<', '-', 'z'};
    for X =1:nVars
        for Y =X+1:nVars
            if mcg(X, Y) 
                fprintf('%s %s-%s %s\n', headers{X}, symbolsYX{mcg(Y, X)},symbolsXY{mcg(X, Y)}, headers{Y});
            end
        end
    end

else
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

end
