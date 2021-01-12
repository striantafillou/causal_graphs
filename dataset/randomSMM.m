function smm = randomSMM(nVars,edgeProb)
% function smm = randomSMM(nVars,maxAdjDeg)
% add every edge with probability edgeprob

smm = zeros(nVars);
[isAncestor, directed, bidirected] = deal(false(nVars));
for x=1:nVars
    for y =x+1:nVars
        % directed edge?
        if rand<=edgeProb
            if rand<0.5 && ~isAncestor(y,x)
                directed(x,y)= true;
                isAncestor = transitiveClosureSparse_mex(sparse(directed));
            elseif ~isAncestor(x,y)
                directed(y,x)=true;
                isAncestor = transitiveClosureSparse_mex(sparse(directed));
            end
        end
        if rand<=edgeProb
            bidirected(x,y)=true; bidirected(y, x)=true;
        end
    end
end

smm(bidirected) = 2;
smm(directed')= 3;smm(directed)=2;
smm(directed' & bidirected) = 4;
end


