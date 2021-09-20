function meanp = dirichlet_posterior_expectation(counts, priors)

if nargin==1
    priors = zeros(size(counts));
end
meanp = (priors+counts+1)./sum(priors+counts+1);
end