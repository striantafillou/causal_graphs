function [bool] = hasInducingPath(X,Y,G,L,S,closure)
% Every non-endpoint vertex is in L or a collider
% Every collider is an ancestor of X,Y, or a member of S

if((~isempty(L) && any((G(L,X) & G(L,Y)))) || (~isempty(S) && any(G(X,S) & G(Y,S))))
    bool = true;
    return;
end

if(G(X,Y) || G(Y,X))
    bool = true;
    return;
end

if(isempty(L) && isempty(S))
    bool = false;
    return;
end

nnodes = length(G);
visited = false(nnodes,nnodes);
Q = zeros(nnodes*nnodes,2);

visited(:,X) = true;
visited(Y,:) = true;

% Initialize Q by adding neighbours of X
neighbours = find(G(X,:) | G(:,X)');
len = length(neighbours);

if(len ~= 0)
    visited(X,neighbours) = true;
    Q(1:len,1) = X;
    Q(1:len,2) = neighbours;
    curQ = len;
else
    curQ = 0;
end

L_bin = false(1,nnodes);
L_bin(L) = true;
S_bin = false(1,nnodes);
S_bin(S) = true;
collidercond = ((closure(:,X) | closure(:,Y))' | S_bin);

while(curQ)
    curX = Q(curQ,1);
    curY = Q(curQ,2);
    curQ = curQ - 1;
    
    if(G(curX,curY))
        % if collidercond: all G(i,curY) + all G(curY,j) if L_bin, i ~= j
        % else: all G(curY,j)
        if(~collidercond(curY) && ~L_bin(curY))
            neighbours = false(1,nnodes);
        elseif(~collidercond(curY) && L_bin(curY))
            neighbours = ~visited(curY,:) & G(curY,:);
        elseif(collidercond(curY) && ~L_bin(curY))
            neighbours = ~visited(curY,:) & G(:,curY)';
        else
            neighbours = ~visited(curY,:) & (G(curY,:) | G(:,curY)');
        end
    elseif(L_bin(curY))
        neighbours = ~visited(curY,:) & (G(curY,:) | G(:,curY)');
    else
        neighbours = false(1,nnodes);
    end
    
    neighbours(curX) = false;
    if(neighbours(Y))
        bool = true;
        return;
    end
    
    curneighbours = find(neighbours);
    len = length(curneighbours);
    if(len ~= 0)
        visited(curY,curneighbours) = true;
        Q(curQ+1:curQ+len,1) = curY;
        Q(curQ+1:curQ+len,2) = curneighbours;
        curQ = curQ + len;
    end
end

bool = false;
end

