function pdset = findpdsepset(pag, X, Y, verbose)
% Finds possible separating sets for two nodes:A variable V is in D-Sep(A,E)
% in G' if and only if V ? A and there is an undirected path between A and V
%on which every vertex except the endpoints is a collider, and each vertex 
%is an ancestor of A or E. 


tmppag=  pag;
tmppag(Y, X) = 0;
tmppag(X, Y)= 0;
%Find nodes V that are adjacent to Node A and Ao->V 
neighborsX =  find(tmppag(X, :));
queue = neighborsX;
pdset = neighborsX;
p = X*ones(1,length(queue));

tmppag(X, neighborsX) = 0; %delete edges to neighbors
tmppag(neighborsX, X) = 0;

while~isempty(queue)
    V =  queue(1);
    queue = queue(2:end);
    U =  p(1);
    tmppag(U, V) = 0;
    tmppag(V, U) = 0;

    
    p = p(2:end);
    curNeighbors = setdiff(find(tmppag(V, :)), Y);
    
    if ~isempty(curNeighbors)
        for W = curNeighbors   
           if verbose; fprintf('Now checking triple %d-%d-%d\n', U, V, W);end
            if ispdseptriple(pag, U, V, W)
                tmppag(V, W) = 0;
                tmppag(W, V) = 0;
                queue = [queue W];       
                p = [p V];
                pdset = [pdset W];   
                 if verbose; fprintf('Triple %d-%d-%d is legal\n Added %d in queue of possible d separating nodes\n', U, V,  W, W);end                
            end %end if
        end%end for
    end%end if
end%end while
        
if isempty(setdiff(pdset, neighborsX))
    pdset =[];
else
    pdset =  unique(pdset);
end
end %end function
  


function isPdSepTriple = ispdseptriple(pag,U, V, W)
   if pag(U, W)~=0
       isPdSepTriple =true;
   else
    isPdSepTriple = pag(U, V) == 2 && pag(W, V) ==2;
   end
end %end function