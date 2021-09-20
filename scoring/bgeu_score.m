function ll = bgeu_score(data, graph)
nVars = length(graph);
bgeus = zeros(1, nVars);
[mu0, T0, am, aw] = computeGaussHyperParams(nVars);
for iVar = 1:nVars
    Pa = find(graph(:, iVar))';
   % fprintf('Processing node %d with parents %s\n', iVar, num2str(Pa));
    bgeus(iVar) = logMargLikGaussFamily(data, Pa, iVar, T0, mu0,  am, aw, []);
end
ll = sum(bgeus);
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
