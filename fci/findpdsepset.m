function pdset = findpdsepset(pag, source, target, screen)
% Finds possible separating sets for two nodes: A variable V is in Possible
% D-Sep(A,E) in G' if and only if V ? A and there is an undirected path
% between A and V on which every vertex except the endpoints is a collider, 
% and each vertex is an ancestor of A or E.


tmppag=  pag;
tmppag(target, source) = 0;
tmppag(source, target)= 0;
%Find nodes V that are adjacent to Node A and Ao->V 
neighbors =  find(tmppag(source, :));
queue = neighbors;
pdset = neighbors;
p = source*ones(1,length(queue));

tmppag(source, neighbors) = 0; %delete edges to neighbors
tmppag(neighbors, source) = 0;

while~isempty(queue)
    curMiddleNode =  queue(1);
    queue = queue(2:end);
    curFirstNode =  p(1);
    tmppag(curFirstNode, curMiddleNode) = 0;
    tmppag(curMiddleNode, curFirstNode) = 0;

    
    p = p(2:end);
    neighbors = setdiff(find(tmppag(curMiddleNode, :)), target);
    
    if ~isempty(neighbors)
        for curEndNode = neighbors   
           if screen; fprintf('Now checking triple %d-%d-%d\n', curFirstNode, curMiddleNode, curEndNode);end
            if ispdseptriple(pag, curFirstNode, curMiddleNode, curEndNode, source, target, screen)
                tmppag(curMiddleNode, curEndNode) = 0;
                tmppag(curEndNode, curMiddleNode) = 0;
                queue = [queue curEndNode];       
                p = [p curMiddleNode];
                pdset = [pdset curEndNode];   
                 if screen; fprintf('Triple %d-%d-%d is legal\n Added %d in queue of possible d separating nodes\n',...
                     curFirstNode, curMiddleNode,  curEndNode, curEndNode);end                
            end %end if
        end%end for
    end%end if
end%end while
        
pdset =  unique(pdset);
end %end function