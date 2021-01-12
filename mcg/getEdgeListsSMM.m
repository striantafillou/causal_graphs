function [directed, bidirected] = getEdgeListsSMM(smm)
% returns nVarsxnVars boolean matrices with directed and bidirected edges.
directed = smm==2&smm'==3;
bidirected = (smm==2|smm==4) &(smm'==2|smm'==4);
end