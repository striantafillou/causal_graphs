function isDesc = finddescendants(mcg)
% function isDesc = finddescendants(mcg) Returns an nVars x nVars matrix
% where isDesc(i, j) is true if i is a descendant of j in graph mcg. 
% Author: striant@csd.uoc.gr
dag =  false(size(mcg));
dag((mcg ==3|mcg==4) & mcg' ==2) =true;
isDesc = transitiveClosureSparse_mex(sparse(dag));
isDesc =  ~~isDesc;
end
