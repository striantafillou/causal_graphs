function score = bgeu_score(data, graph)
% FUNCTION LL = BGEU_SCORE(DATA, GRAPH)
% bgeu score -- code from the BDAGL package
% http://www.cs.ubc.ca/~murphyk/Software/BDAGL/
% =======================================================================
% Inputs
% =======================================================================
% data                    = nSamples x nVars data matrix
% graph                   = nVars x nVars DAG adjacency matrix 
% isLatent                = nVars x 1 boolean vector, isLatent(i)= true if 
%                           $X_i$ is  latent
% =======================================================================
% Outputs
% =======================================================================
% score                   = the bgeu score of the DAG
% =======================================================================
nVars = length(graph);
bgeus = zeros(1, nVars);
[mu0, T0, am, aw] = computeGaussHyperParams(nVars);
for iVar = 1:nVars;
    Pa = find(graph(:, iVar))';
   % fprintf('Processing node %d with parents %s\n', iVar, num2str(Pa));
    bgeus(iVar) = logMargLikGaussFamily(data, Pa, iVar, T0, mu0,  am, aw, []);
end
score = sum(bgeus);
end


function  [mu0, T0, am, aw] = computeGaussHyperParams(n)

% Geiger and Heckerman, Annals, p1426
mu0 = zeros(n,1);
aw = n+1+0.1; % small sample sizes -> weak prior
am = 0.1;

gamma = aw-n+1;
Sigma = eye(n); % rand_psd(n);
TTinv = (gamma-2)/gamma*Sigma; % eqn 20 : let TT = T', TTinv = inv(T')
TT = inv(TTinv);
Tinv = (am+1)/(am*gamma)*TT; % eqn 19
T0 = inv(Tinv);
end

function logp = logMargLikGaussFamily(data, Pa, i, T0, mu0,  am, aw)
% Computes L = log p(x(child) | x(parents))
%
% We mostly follow the notation of Geiger and Heckerman,
% Annals of Stats, 2002, 30(5), p1425
%   X ~ N(mu, W) W = precision matrix
%   mu | W ~ N(mu0, am W)
%   W ~ Wi(aw, T0)
%
% data(all cases, all nodes)
% intervention.clamped(m, i) if node i is clamped in case m
% Prior params: am = alpha_mu, aw = alphaW, mu0 = nu, T0 = T, TN = R

% Update global parameters using all data except cases where i was clamped.
% We cannot just compute TN once in case
% each node has a different effective sample size (due to clamping)

% unclamped = find(intervention.clamped(:,i)==0);
% D = data(:, unclamped );

D=data;
[muN, TN, amN, awN] = gaussWishartPosterior(D, mu0, T0, am, aw);
T0inv = (T0);
TNinv = (TN);

logpFam = logMargLikGaussSubset(D, [Pa i], T0inv, TNinv, am, aw);
logpPa = logMargLikGaussSubset(D, Pa, T0inv, TNinv, am, aw);
logp = logpFam - logpPa; % eqn 9
end

%%%%%%%%%%
function  logp = logMargLikGaussSubset(D, Y, T0inv, TNinv, am, aw)

L = length(Y);
[N n] = size(D); % N = num cases, n = num variables
aw2 = aw- n + L; % alpha_w'
TY = (T0inv(Y,Y));
RY = (TNinv(Y,Y));

% Eqn 18
logp = -L*N/2*log(2*pi) + (L/2)*log(am/(am+N)) + logc(L,aw2) ...
       - logc(L, aw2 + N) + (aw2/2)*logdet(TY) - (aw2+N)/2*logdet(RY);
end


%%%%%%%

function c = logc(n, alpha)

% eqn 15
us = alpha + 1 - (1:n);
c = alpha*(n/2) * log(2) + (n*(n-1)/4)*log(pi) +  sum(gammaln(us./2));
c = -c;
end


%%%%%%%

function [muN, TN, amN, awN] = gaussWishartPosterior(D, mu0, T0, am, aw)
% mu0 = nu, am = alpha mu, aw = alpga W, T0 = T
% muN = nu', TN = R

xbar = mean(D)';
[N n] = size(D);
SN = cov(D)*(N-1);

muN = (am*mu0 + N*xbar)/(am + N); % eqn 16
TN = T0 + SN + (am*N)/(am+N)*(mu0-xbar)*(mu0-xbar)'; % eqn 17
amN = am + N;
awN = aw + N;
end
