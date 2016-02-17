function [pag, dnc, col] = R0(pag, sepSets, verbose)
% function [pag, dnc, col] = R0(pag, sepSets, verbose)
% FCI rule  RO: Orient unshielded colliders
% If a*-*c*-*b, a b not adjacent, c not in sepSet(a,b) orient a*->c<-*b
% else mark as definite non collider.

nVars = length(pag);
unshieldedtriples = cell(nVars,1);
unshieldedpairs = zeros(nVars*nVars,2);

for i = 1:nVars
    neighbours = find(pag(i,:));
    nneighbours = length(neighbours);
    curindex = 1;
    for n1 = 1:nneighbours
        curn1 = neighbours(n1);
        for n2 = n1+1:nneighbours
            curn2 = neighbours(n2);
            if(pag(curn1,curn2) == 0)
                unshieldedpairs(curindex,1) = curn1;
                unshieldedpairs(curindex,2) = curn2;
                curindex = curindex + 1;
            end
        end
    end
    unshieldedtriples{i} = unshieldedpairs(1:curindex-1,:);
end


[dnc, col] = deal(nan(nchoosek(nVars, 3), 3));
iCol =0; iDnc =0;
for c = 1:nVars
    curtriples = unshieldedtriples{c};
    ntriples = size(curtriples,1);
    
    for t = 1:ntriples
        a = curtriples(t,1);
        b = curtriples(t,2);  
        if sepSets(a,b,c) % If in sepset ==> dnc
            iDnc = iDnc+1; 
            dnc(iDnc, :) = [a c b];
        else % If not in sepset ==> ~dnc and orient
            if verbose
                fprintf('\tR0: Orienting %d*->%d<-*%d\n',a,c,b);
            end
            iCol = iCol+1; 
            col(iCol, :) = [a c b];
            pag(a,c) = 2;
            pag(b,c) = 2;
        end
    end
    dnc = dnc(1:iDnc, :);
    col = col(1:iCol, :);
end

end