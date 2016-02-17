function isAnc = findancestors(mcg)
% function isAnc = findancestors(mcg) Returns an nVars x nVars matrix
% where isAnc(i, j) is true if i is an ancestor of j in graph mcg. 
% Author: striant@csd.uoc.gr
dag =  false(size(mcg));
dag(mcg ==2 & (mcg' ==3|mcg'==4)) =true;
isAnc = transitiveClosureSparse_mex(sparse(dag));
isAnc =  ~~isAnc;
end
