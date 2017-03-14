function hasC = hasdirectedcycle(mcg)
% hasC = hasdirectedcycle(mcg)
% returns true if graph mcg has an almost cycle.
% Author:  striant@csd.uoc.gr
isDesc= finddescendants(mcg);
hasC = any(diag(isDesc));
end
