function isAnc = findancestors(mcg)
% function isDesc = findancestors(mcg) Returns an nVars x nVars matrix
% where isDesc(i, j) is true if i is a descendant of j in graph mcg. 
% Author: striant@csd.uoc.gr
dag =  false(size(mcg));
dag(mcg ==2 & (mcg' ==3|mcg'==4)) =true;
isAnc = transitiveClosureSparse_mex(sparse(dag));
isAnc =  ~~isAnc;
end
