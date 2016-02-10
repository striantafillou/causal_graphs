function [pvalues, tests, latentTests] = allIndependenceTests(dataset, test, varargin)
% function [pvalues, tests, latentTests] = ALLINDEPENDENCETESTS(DATAST,
% TEST, VARARGIN). Performs all conditional indepence tests among observed
% variables in a dataset. author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% dataset                 = A stucture containing the data.(see folder
% README) test                    = the preferred test of conditional
% indpendence. maxCondsetSize          = maximum conditioning set size.
% verbose                 = true for screen output.
% =======================================================================
% Outputs
% =======================================================================
% pvalues                = nTests x 1 matrix with the returned pvalues.
% tests                  = nTests x 1 cell, i-th entry containst the
%                           variables in the i-th test [x y condset]. 
% latentTests            = nTests x 1 matrix boolean matrix, true if i-th
%                           test includes some latent variable.
% =======================================================================

[~, nVars] = size(dataset.data);
[maxCondsetSize, verbose] = process_options(varargin, 'maxCondsetSize', nVars-2, 'verbose', false);

% calculate how many tests and preallocate  
if maxCondsetSize ==nVars-2
nTests = nchoosek(nVars, 2)*2^(maxCondsetSize);
else foo =0;
    for i=0:maxCondsetSize
        foo = foo+nchoosek(nVars-2, i);
    end
nTests = nchoosek(nVars, 2)*foo;    
end

fprintf('%d  tests\n', nTests);
pvalues= nan(nTests,1);
tests = cell(nTests, 1);
latentTests = false(nTests, 1);
iTest=0;
for x =1:nVars-1
    for y=x+1:nVars
        for condSetSize = 0:maxCondsetSize
            condsets = nchoosek(setdiff(1:nVars, [x y]), condSetSize);
            for iCondset =1:size(condsets, 1)
                iTest =iTest+1;
                condset = condsets(iCondset,:);
                if verbose 
                    fprintf('Testing %d-%d given %s\n', x, y, num2str(condset));
                end
                    pvalues(iTest) = feval(test,  x, y, condset, dataset.data, dataset.domainCounts);
                    tests{iTest} = [x,y, condset];
                    if any(dataset.isLatent(tests{iTest}))
                        latentTests(iTest) = true;
                    end
                    if verbose
                        fprintf('pvalue %.3f\n',  pvalues(iTest));
                    end
            end
        end
    end
end

end
    