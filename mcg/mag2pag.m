function pag =  mag2pag(mag)
% function PAG = MAG2PAG(MAG)
% Converts a MAG into a PAG 
% Author:  striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% mag                     = MAG matrix(see folder README for details)
% =======================================================================
% Outputs
% =======================================================================
% pag                     = PAG matrix(see folder README for details)
% =======================================================================

pag = mag;

pag(~~pag)=1;
pag =  FCI_rules_mag(pag, mag, false);
end

function [G, dnc, flagcount] = FCI_rules_mag(G, mag, screen)
flagcount =0;
 unshieldedtriples = getUnshieldedTriples(G);
[G, dnc] = R0(G, unshieldedtriples, mag, screen);

flag=1;
while(flag)
    flag=0;
    [G,flag] = R1(G, flag, screen);
    [G,flag] = R2(G, flag, screen);
    [G,flag] = R3(G, flag, screen);
    [G,flag] = R4(G, mag, flag, screen);
    flagcount =  flagcount+flag;
end

flag=1;
while(flag)
    flag=0;
    [G,flag] = R8(G,flag,screen);
    [G,flag] = R9_R10(G,dnc,flag,screen);
    flagcount =  flagcount+flag;
end

end

function [Pag, dnc] = R0(Pag, unshieldedtriples, mag, screen)
%fprintf('orienting unshielded triples\n');

% If a b not adjacent, c not in sepSet(a,b) => a->c<-b
nnodes = length(Pag);
dnc = [];

for c = 1:nnodes
    curtriples = unshieldedtriples{c};
    ntriples = size(curtriples,1);
    
    if(ntriples > 0)
        sep = false(ntriples,1);
        for i = 1:ntriples
            triple= curtriples(i, :);
          %  disp(triple)
          if mag(triple(1), c)~=2 || mag(triple(2), c)~=2
              sep(i)=1;
          end
        end
        dnc{c} = curtriples(sep,:);
        Pag(curtriples(~sep,1),c) = 2;
        Pag(curtriples(~sep,2),c) = 2;
        
        if(screen)
            idx = find(~sep)';
            for i = idx
                fprintf('R0: Orienting %d*->%d<-*%d\n',curtriples(i,1),c,curtriples(i,2));
            end
        end
    else
        dnc{c} = zeros(0,2);
    end
end

end

function [Pag, flag] = R1(Pag, flag, screen)
% If a*->bo-*c and a,c not adjacent ==> a*->b->c

[c, b] = find(Pag == 1);
len = length(c);
for i = 1:len
    if(Pag(c(i),b(i)) == 1 && any(Pag(:,b(i)) == 2 & Pag(:,c(i)) == 0))
        if(screen)
            fprintf('R1: Orienting %d->%d\n',b(i),c(i));
        end
        Pag(b(i),c(i)) = 2;
        Pag(c(i),b(i)) = 3;
        flag = 1;
    end
end

end

function [Pag, flag] = R2(Pag, flag, screen)
% If a->b*->c or a*->b->c, and a*-oc ==> a*->c

[a, c] = find(Pag == 1);
len = length(a);
for i = 1:len
    if(Pag(a(i),c(i)) == 1 && any(Pag(a(i),:) == 2 & Pag(:,c(i))' == 2 & (Pag(:,a(i))' == 3 | Pag(c(i),:) == 3)))
        if(screen)
            fprintf('R2: Orienting %d*->%d\n',a(i),c(i));
        end
        Pag(a(i),c(i)) = 2;
        flag = 1;
    end
end

end

function [Pag, flag] = R3(Pag, flag, screen)
% If a*->b<-*c, a*-o8o-*c, a,c not adjacent, 8*-ob ==> 8*->b

[th, b] = find(Pag == 1);
nedges = length(th);

for i = 1:nedges
    a = find(Pag(:,th(i)) == 1 & Pag(:,b(i)) == 2);
    len = length(a);
    f = false;
    for j = 1:len
        for k = j+1:len
            if(Pag(a(j),a(k)) == 0 && Pag(th(i),b(i)) == 1)
                if(screen)
                    fprintf('R3: Orienting %d*->%d\n',th(i),b(i));
                end
                Pag(th(i),b(i)) = 2;
                flag = 1;
                f = true;
                break;
            end
        end
        if(f)
            break;
        end
    end
end

end

function [Pag, flag] = R4(Pag, mag, flag, screen)

% Start from some node X, for node Y
% Visit all possible nodes X*->V & V->Y
% For every neighbour that is bi-directed and a parent of Y, continue
% For every neighbour that is bi-directed and o-*Y, orient and if
% parent continue
% Total: n*n*(n+m)

% For each node Y, find all orientable neighbours W
% For each node X, non-adjacent to Y, see if there is a path to some
% node in W
% Create graph as follows:
% for X,Y
% edges X*->V & V -> Y --> X -> V
% edges A <-> B & A -> Y --> A -> B
% edges A <-* W & A -> Y --> A->W
% discriminating: if path from X to W

nnodes = length(Pag);
dir = Pag == 2 & Pag' == 3;
bidir = Pag == 2 & Pag' == 2;

for curc = 1:nnodes
    b = find(Pag(curc,:) == 1);
    if(isempty(b))
        continue;
    end
    
    th = find(Pag(curc,:) == 0);
    if(isempty(th))
        continue;
    end
    
    curdir = dir(:,curc)';
    
    G = zeros(nnodes,nnodes);
    for curth = th
        G(curth,Pag(curth,:) == 2 & curdir) = 1;
    end
    for d = find(curdir)
        G(bidir(d,:),d) = 1;
    end
    closure = transitiveClosureSparse_mex(sparse(G));
    
    a = find(any(closure(th,:)));
    if(isempty(a))
        continue;
    end
    
    for curb = b
        for cura = a
            if(Pag(curb,cura) == 2)
                if(any(closure(th,cura) & mag(curb, curc) == 2 & mag(curc, curb) == 3))
                    if(screen)
                        fprintf('R4: Orienting %d->%d\n',curb,curc);
                    end
                    Pag(curb, curc) = 2;
                    Pag(curc, curb) = 3;
                else
                    if(screen)
                        fprintf('R4: Orienting %d<->%d\n',curb,curc);
                    end
                    Pag(curb, curc) = 2;
                    Pag(curc, curb) = 2;
                    Pag(cura, curb) = 2;
                end
                flag = 1;
                break;
            end
        end
    end
end
end

function [G,flag] = R8(G, flag, screen)

[r,c] = find(G == 2 & G' == 1);
nedges = length(r);

for i = 1:nedges
    out = find(G(:,r(i)) == 3);
    if(any(G(out,c(i)) == 2 & G(c(i),out)' == 3))
        if(screen)
            fprintf('R8: Orienting %d->%d\n',r(i),c(i));
        end
        G(c(i),r(i)) = 3;
        flag = 1;
    end
end
end

function [G,flag] = R9_R10(G,dnc,flag,screen)

[r,c] = find(G == 2 & G' == 1);
nedges = length(r);

% R9: Equivalent to orienting X <-o Y as X <-> Y and checking if Y is an
% ancestor of X (i.e. there is an almost directed cycle)
for i = 1:nedges
    % Can it be bidirected? (R9)
    G_ = G;
    G_(c(i),r(i)) = 2;
    G_ = orientDnc_mex(G_, c(i), r(i));
    if(isReachablePag_mex(G_,r(i),c(i)))
        if(screen)
            fprintf('R9: Orienting %d*--%d\n',c(i),r(i));
        end
        G(c(i),r(i)) = 3;
        flag = 1;
    end
end

% Fast, trivial check if there is a potentially directed path
possibleClosure = transitiveClosureSparse_mex(sparse((G == 1 & G' ~= 2) | (G == 2 & G' == 3)));

% R10: Equivalent to checking if for some definite non collider V - X - W
% and edge X o-> Y, X->V and X->W both create a directed path to Y after
% oriented
closures = zeros(length(G),length(G));
tested = false(1,length(G));
for s = 1:length(G)
    tested(:) = false;
    curdnc = dnc{s};
    ndnc = size(curdnc,1);
    
    for t = find(G(:,s)' == 1 & G(s,:) == 2)
        for j = 1:ndnc
            a = curdnc(j,1);
            b = curdnc(j,2);
            if(~possibleClosure(a,t) || ~possibleClosure(b,t) || G(a,s) == 2 || G(b,s) == 2 || a == t || b == t)
                continue;
            end
            
            if(~tested(a))
                G_ = G;
                G_(s,a) = 2;
                G_(a,s) = 3;
                G_ = orientDnc_mex(G_,s,a);
                closures(a,:) = findAncestorsPag_mex(G_',s);
                tested(a) = true;
            end
            if(~closures(a,t))
                continue;
            end
            
            if(~tested(b))
                G_ = G;
                G_(s,b) = 2;
                G_(b,s) = 3;
                G_ = orientDnc_mex(G_,s,b);
                closures(b,:) = findAncestorsPag_mex(G_',s);
                tested(b) = true;
            end
            if(~closures(b,t))
                continue;
            end
            
            if(screen)
                fprintf('R10: Orienting %d*--%d\n',t,s);
            end
            G(t,s) = 3;
            flag = 1;
            break;
        end
        
    end
end

end

function [unshieldedtriples] = getUnshieldedTriples(G)

nnodes = length(G);
unshieldedtriples = cell(nnodes,1);

for i = 1:nnodes
    neighbours = find(G(i,:)');
    [x,y] = find(triu(~G(neighbours,neighbours)) & ~eye(length(neighbours)));
    %[x y] = find(triu(G(neighbours,neighbours)) & ~eye(length(neighbours)));
    unshieldedtriples{i} = [neighbours(x) neighbours(y)];
end

end

