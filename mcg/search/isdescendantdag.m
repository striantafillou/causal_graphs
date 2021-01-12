function isAnc =  isancestordag(x,y, dag)
% function isDesc =  isdescendantdag(x,y, dag) Returns true if x is a descendant of y in dag
% sot16@pitt.edu
allAnc = transitiveClosureSparse_mex(sparse(dag));
isAnc =  ~~allAnc(x, y);
end
