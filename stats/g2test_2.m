function [p, G2, exit_flag]=g2test_2(var1, var2, condvarset, dataset)
% FUNCTION [P, R, EXITFLAG] = G2TEST_2(VAR1, VAR2, CONDVARSET, DATASET)
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
% G2                      = value of the g2 statistic
% exit_flag               = 1 when p and G2 can be computed reliably,
%                           0 otherwise(p and G2 are not returned). This 
%                           could be due to inadequate elements per cell 
%                           in the cpt or too many structural zero, 
%                           resulting to negative degrees of freedom
% =======================================================================
data = dataset.data;
domainCounts = dataset.domainCounts;

[k, l] = size(data);

%H1: Consider the following heuristic on the sample size
if k < 5*prod(domainCounts([var1 var2 condvarset]))
   p=NaN; 
   G2=NaN;
   exit_flag=0;
   fprintf('Cannot perform  test Ind(%d, %d|[%s]): Not enought sample \n', var1, var2, num2str(condvarset));
   return;
end

% Proceed with computation of G^2 statistic
var1_var2_cond_indexset = [var1 var2 condvarset];
var1_cond_indexset = [var1 condvarset];
var2_cond_indexset = [var2 condvarset];

prior_var1_var2_cond = prod(domainCounts(var1_var2_cond_indexset));
prior_var1_cond = prod(domainCounts(var1_cond_indexset));
prior_var2_cond = prod(domainCounts(var2_cond_indexset));
prior_cond = prod(domainCounts(condvarset));
prior_var1_var2 = prod(domainCounts([var1 var2]));
prior_var1 = domainCounts(var1);
prior_var2 = domainCounts(var2);


% See pages 17-50 in the Matlab manual for this calculation.
% Try to understand first how hits_var1_var2_cond is calculated
% the rest are the similar.

bases_var1_var2_cond = zeros(1, 1+length(var1_var2_cond_indexset));
bases_var1_var2_cond(2:end) = cumprod(domainCounts(var1_var2_cond_indexset));
bases_var1_var2_cond(1) = 1;
bases_var1_var2_cond(end) = [];

bases_var1_cond = zeros(1, 1+length(var1_cond_indexset));
bases_var1_cond(2:end) = cumprod(domainCounts(var1_cond_indexset));
bases_var1_cond(1) = 1;
bases_var1_cond(end) = [];

bases_var2_cond = zeros(1, 1+length(var2_cond_indexset));
bases_var2_cond(2:end) = cumprod(domainCounts(var2_cond_indexset));
bases_var2_cond(1) = 1;
bases_var2_cond(end) = [];

bases_cond = zeros(1, 1+length(condvarset));
bases_cond(2:end) = cumprod(domainCounts(condvarset));
bases_cond(1) = 1;
bases_cond(end) = [];

hits_var1_var2_cond = data(:, var1_var2_cond_indexset) * bases_var1_var2_cond' + 1;
hits_var1_cond = data(:, var1_cond_indexset) * bases_var1_cond' + 1;
hits_var2_cond = data(:, var2_cond_indexset) * bases_var2_cond' + 1;
hits_cond = data(:, condvarset) * bases_cond' + 1;

var1_var2_cond_counters = histc(hits_var1_var2_cond, [1:prior_var1_var2_cond]);
var1_cond_counters = histc(hits_var1_cond, [1:prior_var1_cond]);
var2_cond_counters = histc(hits_var2_cond, [1:prior_var2_cond]);
cond_counters = histc(hits_cond, [1:prior_cond]);

A=reshape(var1_cond_counters, prior_var1, prior_cond)';
B=reshape(var2_cond_counters, prior_var2, prior_cond)';
df=0;
for i=1:size(A,1)
   df=df+max(prior_var1-1-length(find(~A(i,:))),0)*max(prior_var2-1-length(find(~B(i,:))),0);
end
%fprintf('Degrees of Freedom = %d\n', df);
% G^2 is approximated as follows:
%
%                            N(z) N(x,y,z)      
% sum(x,y,z)=N(x,y,z) * log(-------------) 
%                           N(x,z) N(y,z)
%
% where N(xyz) is the number of instances that match the values for x, y, and z
% (whatever they may be for that term in the some) 

var2_cond_counters = var2_cond_counters';
var2_cond_counters = repmat(var2_cond_counters, domainCounts(var1), 1);
var2_cond_counters = var2_cond_counters(:);

var1_cond_counters = reshape(var1_cond_counters, domainCounts(var1), prod(domainCounts(condvarset)));
var1_cond_counters = repmat(var1_cond_counters, domainCounts(var2), 1);
var1_cond_counters = var1_cond_counters(:);


cond_counters = cond_counters';
cond_counters = repmat(cond_counters, prod(domainCounts([var1 var2])), 1);
cond_counters = cond_counters(:);

res = var1_var2_cond_counters .* ...
   log(eps+(var1_var2_cond_counters .* cond_counters)./...
    (var1_cond_counters .* var2_cond_counters + eps) );

% Compute G^2
G2 =  2*sum(res);

% Compute p-value
if df==0
   p=NaN;
   exit_flag=0;
   fprintf('Cannot perform test Ind(%d, %d|[%s]): Zero dof \n', var1, var2, num2str(condvarset));
else
   p = 1-chi2cdf(G2,df);
   exit_flag=1;
end


