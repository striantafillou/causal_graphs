function theta = sample_dirich(alphas)
% SAMPLE_DIRICHLET Sample N vectors from Dir(alpha(i,1), ..., alpha(i, k))
% theta = sample_dirichlet(alpha, N)
% theta(i,j) = i'th sample of theta_j, where theta ~ Dir

% We use the method from p. 482 of "Bayesian Data Analysis", Gelman et al.

[nInst, k] = size(alphas); % numValues
scale = 1; % 
theta = zeros(nInst, k);
for iInst=1:nInst
    theta_ = zeros(1, k);

    for i=1:k
        theta_(:,i) = gamrnd(alphas(iInst, i), scale, 1, 1);
    end
    S = sum(theta_,2);
    theta(iInst, :) = theta_ ./ repmat(S, 1, k);
end

end