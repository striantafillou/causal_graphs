function [p, r, exitflag] = fisher_2(x, y, condvars)
% FUNCTION [P, R, EXITFLAG] = FISHER(x, y, CONDVARSET, DATASET)
% Fisher test of independence of variable var1 and variable var2 given
% variables in condvarset.
%
% Author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% var1, var2, condvarset  = indices of var1, var2, [condvarset] in
%                           dataset.data
% dataset                 = nSamples x nVars data set including variables
%                           var1, var2, condvarset
% =======================================================================
% Outputs
% =======================================================================
% p                       = the p-value, i.e. P(data|H_0)
% r                       = conditional correlation of var1 and var2 given
%                           condvarset
% exitflag                = dummy variable denoting if computations
%                           completed correctly (1 f true, 0 otherwise)
% =======================================================================


% Compute partial correlation coefficient
S=corrcoef([x y condvars]);

condvarset=3:size(condvars, 2)+2;
vars = [1 2];
S2 = S(vars,vars) - S(vars,condvarset)*inv(S(condvarset,condvarset))*S(condvarset,vars);
r = abs(S2(1,2) / sqrt(S2(1,1) * S2(2,2)));

% Sample size
N=size(x,1);

% Compute Z and P-value
z = 0.5*log( (1+r)/(1-r));
df=N - length(condvarset) - 3;
W = sqrt(df)*z; 
p = 2*tcdf(-abs(W),df);

exitflag=1; 
end


