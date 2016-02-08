function hasAC = hasalmostcycle(mcg)
% hasAC = hasalmostcycle(mcg)
% returns true if graph mcg has an almost cycle.
% Author:  striant@csd.uoc.gr
isDesc= finddescendants(mcg);
hasAC = any(any((isDesc | isDesc') & (mcg == 2|mcg==4) & (mcg' == 2|mcg' == 4)));
end
