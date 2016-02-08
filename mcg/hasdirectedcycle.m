function hasC = hasDirectedCycle(mcg)
% hasAC = hasDirected(mcg, isDescendant)
% returns true if graph mcg has an almost cycle.
% Author:  striant@csd.uoc.gr
isDesc= isDescendant(mcg);
hasC = any(diag(isDesc));
end
