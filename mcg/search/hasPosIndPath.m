function [bool] = hasPosIndPath(X, Y, pag, dnc, possibleDescendants,isLatent,  screen)
% Every non-endpoint vertex is in L or a collider
% Every collider is an ancestor of X,Y, or a member of S

% if graph(X, Y) && confounded(X, Y)~=-1
%     bool =true;
%     return;
% end


nnodes = length(pag);
visited = false(nnodes,nnodes);
Q = zeros(nnodes*nnodes,2);

visited(:,X) = true;
visited(Y,:) = true;

% Initialize Q by adding neighbors of X
neighbors = find(pag(X,:) | pag(:,X)');
num_neighbors = length(neighbors);

if(num_neighbors ~= 0)
    visited(X,neighbors) = true;
    Q(1:num_neighbors,1) = X;
    Q(1:num_neighbors,2) = neighbors;
    curQ = num_neighbors;
else
    curQ = 0;
end

%collidercond = ((descendants(:,X) | descendants(:,Y))' );

while(curQ)
    curX = Q(curQ,1);
    curY = Q(curQ,2);
    curQ = curQ - 1;
%    disp([curX curY])
%     if(curY == Y)
%         bool = true;
%         return;
%     else
        neighbors = [];
        for i = 1:nnodes
            if(curX == i)
                continue;
            end
            
            % If visited
            if(visited(curY,i))
                continue;
            end
            
            % If no edge
            if(~pag(curY,i) && ~pag(i,curY))
                continue;
            end
            if screen
                fprintf('Testing edge %d-%d-%d\n',curX,curY,i);
            end
            if (isLatent(curY) && ~isCollider(curX, curY, i, pag,  dnc)==0) ||...
                    (~isNonCollider(curX, curY, i, pag, dnc) && any(possibleDescendants(curY, [X, Y])))
                if screen
                    fprintf('\t latent or possible colliders, adding %d to neighbors\n', i);
                end
                neighbors = [neighbors i];
                if(i == Y)
                    bool = true;
                    return;
                end               
                continue;
            end
            
        end
        
        num_neighbors = length(neighbors);
        if(num_neighbors ~= 0)
            visited(curY,neighbors) = true;
            Q(curQ+1:curQ+num_neighbors,1) = curY;
            Q(curQ+1:curQ+num_neighbors,2) = neighbors;
            curQ = curQ + num_neighbors;
        end
 %   end
end % end while

bool = false;
end

function bool = isNonCollider(X, Y, Z, pag, dnc)
bool = 0;
if ismember([X Y Z], dnc, 'rows') || ismember([Z Y X], dnc, 'rows')
    bool = 1;
    return;
end
if pag(X, Y)==3|| pag(Z, Y)==3
    bool =1;
end
end


function bool = isCollider(X, Y, Z, pag, dnc)
bool = 0;
if ismember([X Y Z], dnc, 'rows') || ismember([Z Y X], dnc, 'rows')
    bool = 0;
    return;
end
if pag(X, Y)==2|| pag(Z, Y)==2
    bool =1;
end
end


