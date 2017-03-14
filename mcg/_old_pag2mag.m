function [MAG, sat] = pag2mag(PAG_in)

PAG.G = PAG_in.graph;
PAG.sepSet = logical(PAG_in.sepSet);
PAG.dnc = PAG_in.dnc;

% for i = 1:size(PAG_in.dnc,1)
%    curdnc = PAG_in.dnc(i,:);
%    PAG.dnc{curdnc(2)} = [PAG.dnc{curdnc(2)}; curdnc([1 3])];
% end

[sat, MAG] = orientEdges(PAG.G, PAG);

end

function [x,y] = getNextEdge(pag)
[x,y] = find(pag == 1,1);
end

function [sat, pag] = orientEdges(pag, PAG)

[X,Y] = getNextEdge(pag);

if(~isempty(X))
    % Test tail
    if(pag(Y,X) ~= 3)
        pag2 = pag;
        pag2(X,Y) = 3;
        pag2(Y,X) = 2;
        pag2 = FCI_rules_apply(pag2,0);
        closure = transitiveClosurePag_mex(pag2);
        if ~hasanycycle(closure, pag2)
            [sat2,pag2] = orientEdges(pag2,PAG);
            if(sat2)
                pag = pag2;
                sat = sat2;
                return;
            end
        end
    end
    
    % Test arrow
    pag1 = pag;
    pag1(X,Y) = 2;
    pag1 = FCI_rules_apply(pag1,0);
    closure = transitiveClosurePag_mex(pag1);
    if ~hasanycycle(closure, pag1);
        [sat, pag] = orientEdges(pag1, PAG);
    else
        sat = false;
    end
else
    closure = transitiveClosurePag_mex(pag);
    sat = ~hasanycycle(closure, pag) && ~dncsViolated(pag, PAG.dnc) && (isempty(PAG.sepSet) ||  ~independenciesViolated(PAG.sepSet, pag, closure));
end


end

function bool = independenciesViolated(sepSet, Mag, closure)
% Checks whether all M-Separations of sepSet hold in Mag

[X Y] = find(Mag == 0);
n = length(X);
bool = false;
for i = 1:n
    if(X(i) ~= Y(i))
        if(~isMSeparated(Mag,X(i),Y(i),squeeze(sepSet(X(i),Y(i),:))',closure))
            bool = true;
            return;
        end
    end
end
end

function bool = isMSeparated(Mag, X, Y, Z, closure)

nnodes = length(Mag);
visited = false(nnodes,nnodes);

Q = zeros(nnodes*nnodes,2);

if(Mag(X,Y))
    bool = false;
    return;
end

U = find(Mag(X,:));
len = length(U);
Q(1:len,1) = X;
Q(1:len,2) = U;
curQ = len;
visited(X,:) = true;

des = false(1,nnodes);
for i = 1:nnodes
    des(i) = Z(i) || any(closure(i,:) & Z);
end

while(curQ)
    v = Q(curQ,1);
    u = Q(curQ,2);
    curQ = curQ-1;

    U = [];
    for i = 1:nnodes
        if(v ~= i && Mag(u,i) && ~visited(u,i))
            cond = Mag(v,u) == 2 && Mag(i,u) == 2;
            if((~cond && ~Z(u)) || (cond && des(u)))
                if(i == Y)
                    bool = false;
                    return;
                end
                U = [U i];
            end
        end
    end
    len = length(U);
    if(len > 0)
        Q(curQ+1:curQ+len,1) = u;
        Q(curQ+1:curQ+len,2) = U;
        curQ = curQ+len;
        visited(u,U) = true;
    end
end

bool = true;
end


function [res] = dncsViolated(pag, dncs)
% Checks whether some definite non-collider constraint is violated in pag.

nnodes = length(pag);
for i = 1:nnodes
    curdncs = dncs(dncs(:, 2)==i, :);
    if(any(pag(curdncs(:,1),i) == 2 & pag(curdncs(:,2),i) == 2))
        res = true;
        return;
    end
end
res = false;
end


function [G] = FCI_rules_apply(G, screen)
% Applies only rules R1-R3 from FCI.
% This is sound but NOT COMPLETE.
% It is an open problem to devise a complete set of rules to propagate a
% given set of orientations.

flag=1;
while(flag)
    flag=0;
    [G,flag] = R1(G, flag, screen);
    [G,flag] = R2(G, flag, screen);
    [G,flag] = R3(G, flag, screen);
end

end

function [Pag flag] = R1(Pag, flag, screen)
% If a*->bo-*c and a,c not adjacent ==> a*->b->c

[c b] = find(Pag == 1);
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

function [Pag flag] = R2(Pag, flag, screen)
% If a->b*->c or a*->b->c, and a*-oc ==> a*->c

[a c] = find(Pag == 1);
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

function [Pag flag] = R3(Pag, flag, screen)
% If a*->b<-*c, a*-o8o-*c, a,c not adjacent, 8*-ob ==> 8*->b

[th b] = find(Pag == 1);
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

function bool = hasanycycle(closure, pag)

bool = any(diag(closure))|| any(any((closure | closure') & pag == 2 & pag' == 2));
end
