function hasIndPath = hasinducingpathdag(X, Y, dag, isAncestor, isLatent, verbose)
% function bool = hasinducingpathdag(X, Y, mcg, isAncestor, isLatent, verbose)
% returns true if variables X and Y are connected in graph dag by an
% inducing path with respect to variables find(isLatent).
% Author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% X, Y                    = endpoint variables of interest.
% dag                     = dag adjacency matrix
% isAncestor              = nVars x nVars matrix, isAncestor(i, j) = true
%                           if i is an ancestor of j in mcg. Is not 
%                           calculated inside the function to save time if
%                           you call the  function multiple times for the
%                           same graph.
% isLatent                = nVars x 1 matrix, isLatent(i) true if i is a
%                           latent variable.
% verbose                 = true for screen output
% =======================================================================
% Outputs
% =======================================================================
% hasIndPath                    = true if there is an inducing path from X to Y
%                           in mcg wrt to find(isLatent).
% =======================================================================

nnodes = length(dag);
visited = false(nnodes,nnodes);
Q = zeros(nnodes*nnodes,2);

visited(:,X) = true;
visited(Y,:) = true;

% Initialize Q by adding neighbors of X
neighbors = find(dag(X,:) | dag(:,X)');
num_neighbors = length(neighbors);

if(num_neighbors ~= 0)
    visited(X,neighbors) = true;
    Q(1:num_neighbors,1) = X;
    Q(1:num_neighbors,2) = neighbors;
    curQ = num_neighbors;
else
    curQ = 0;
end

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
        if(~dag(curY,i) && ~dag(i,curY))
            continue;
        end
        if verbose
            fprintf('Testing triple %d-%d-%d\n',curX,curY,i);
        end
        % every 
        if (isLatent(curY) && ~isCollider(curX, curY, i, dag)) ||...
                (isCollider(curX, curY, i, dag) && any(isAncestor(curY, [X, Y])))
            if verbose
                fprintf('\t latent or possible colliders, adding %d to neighbors\n', i);
            end
            neighbors = [neighbors i];
            if(i == Y)
                hasIndPath = true;
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

hasIndPath = false;
end



function isCol = isCollider(X, Y, Z, dag)
isCol = 0;
if dag(X, Y)==1 && dag(Z, Y)==1
    isCol =1;
end
end


