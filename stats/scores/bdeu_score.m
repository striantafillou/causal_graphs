function [score, localscores] = bdeu_score(data, graph, domainCounts, N_prime)
% FUNCTION SCORE = BdEU_SCORE(DATA, GRAPH)
% bgeu score -- code from the BDAGL package
% http://www.cs.ubc.ca/~murphyk/Software/BDAGL/
% =======================================================================
% Inputs
% =======================================================================
% data                    = nSamples x nVars data matrix
% graph                   = nVars x nVars DAG adjacency matrix 
% domainCounts            = nVars x 1 vector # of possible values for each 
%                           variable
% N_prime                 = equivalent sample size, usually 1
% =======================================================================
% Outputs
% =======================================================================
% score                      = the bdeu score of the DAG
% =======================================================================

nnodes = length(graph);
% Initialize local scores
localscores = zeros(1,nnodes);
for i = 1:nnodes
    localscores(i) = bde(data,domainCounts,i,find(graph(:,i)'),N_prime);
end

% Initialize total scores
score = sum(localscores);

end


function [score, prior, likelihood] = bde(D, domainCounts, i, pa_i, N_prime)
%BDEU Summary of this function goes here
%   Detailed explanation goes here

% References:
% [1] David Heckerman, Dan Geiger, and David M. Chickering. Learning
%     bayesian networks: The combination of knowledge and statistical data.
%     In KDD Workshop, pages 85-“96,

% param: N

numParentLevels = prod(domainCounts(pa_i));

N_prime_ijk = N_prime/(numParentLevels*domainCounts(i));
N_prime_ij = N_prime/numParentLevels;

N_ijk_s = reshape(histc((D(:,[pa_i i])) * [1 cumprod(domainCounts(pa_i))]' + 1, 1:numParentLevels * domainCounts(i)), numParentLevels, domainCounts(i)) + N_prime_ijk;
N_ij_s = sum(N_ijk_s, 2);

% compute log prior [1] / # nodes
% delta = length(pa_i); % because the prior network is the empty network
% kappa = 1/(N_prime + 1);
% prior = delta*log(kappa);
prior = 0;

% likelihood2 = numParentLevels * gammaln(N_prime_ij);
% for qi = 1:numParentLevels
%     likelihood2 = likelihood2 - gammaln(N_ij_s(qi));
%     for ri = 1:domainCounts(i)
%         likelihood2 = likelihood2 - gammaln(N_prime_ijk) + gammaln(N_ijk_s(qi,ri));
%     end
% end

likelihood = numParentLevels * gammaln(N_prime_ij) - sum(gammaln(N_ij_s)) + ...
    sum(sum(gammaln(N_ijk_s))) - numParentLevels*domainCounts(i)*gammaln(N_prime_ijk);

% add them
score = prior + likelihood;

end