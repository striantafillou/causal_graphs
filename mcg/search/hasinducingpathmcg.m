function hasIndPath = hasinducingpathmcg(X, Y, mcg, isAncestor, isLatent, verbose)
% function bool = hasInducingPathMcg(X, Y, mcg, isAncestor, isLatent, verbose)
% returns true if variables X and Y are connected in graph mcg by an
% inducing path with respect to variables find(isLatent).
% Author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% X, Y                    = endpoint variables of interest.
% mcg                     = mixed causal model(smm, mag, pag)
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
% bool                    = true if there is an inducing path from X to Y
%                           in mcg wrt to find(isLatent).
% =======================================================================

nnodes = length(mcg);
visited = false(nnodes,nnodes);
Q = zeros(nnodes*nnodes,2);

visited(:,X) = true;
visited(Y,:) = true;

% Initialize Q by adding neighbors of X
neighbors = find(mcg(X,:) | mcg(:,X)');
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
            if(~mcg(curY,i) && ~mcg(i,curY))
                continue;
            end
            if verbose
                fprintf('Testing edge %d-%d-%d\n',curX,curY,i);
            end
%             if (isLatent(curY) && ~isCollider(curX, curY, i, mcg)==0) ||...
%                     (~isNonCollider(curX, curY, i, mcg) && any(isAncestor(curY, [X, Y])))
            if (isLatent(curY) && ~isCollider(curX, curY, i, mcg)) ||...
                     (isCollider(curX, curY, i, mcg) && any(isAncestor(curY, [X, Y])))
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

function isNCol = isNonCollider(X, Y, Z, pag)
isNCol = 0;
if pag(X, Y)==3|| pag(Z, Y)==3
    isNCol =1;
end
end


function isCol = isCollider(X, Y, Z, mcg)
isCol = 0;
if (mcg(X, Y)==2||mcg(X, Y)==4)&&(mcg(Z, Y)==2|| mcg(Z, Y)==4)
    isCol =1;
end
end


