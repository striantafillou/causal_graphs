function []= printedgespdag(pdag, headers)
% FUNCTION [] = PRINTEDGSDAG(DAG)
% Prints DAG edges on screen.
% striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% dag                     = DAG adjacency matrix
% =======================================================================
fprintf('---------------------\n')
if nargin>1
    nVars = length(pdag);
%     symbolsXY = {'-', '>'};
%     symbolsYX = {'-', '<'};
    for X =1:nVars
        for Y =X+1:nVars
            if pdag(X, Y)&& pdag(Y, X)
                fprintf('%s---%s\n', headers{X},headers{Y});
            elseif pdag(X, Y)
                fprintf('%s-->%s\n', headers{X},headers{Y});
            elseif pdag(Y, X)
                fprintf('%s-->%s\n', headers{Y},headers{X});
            end
        end
    end
else
    nVars = length(pdag);
%     symbolsXY = {'-', '>'};
%     symbolsYX = {'-', '<'};
    for X =1:nVars
        for Y =X+1:nVars
            if pdag(X, Y)&& pdag(Y, X)
                fprintf('%d---%d\n', X, Y);
            elseif pdag(X, Y)
                fprintf('%d-->%d\n', X, Y);
            elseif pdag(Y, X)
                fprintf('%d-->%d\n', Y, X);
            end
        end
    end
end

end