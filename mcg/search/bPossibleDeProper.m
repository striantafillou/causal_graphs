function bpossDes = bPossibleDeProper(graph,x,y)
%I will use depth first search
%q denotes unvisited nodes/ nodes in queue
%v denotes visited nodes
if nargin==2
   y =[];
end

[v, previous] =deal(zeros(length(graph), 1)); 
i =1;
k =1;
if(length(x)>1)
fprintf("Need to do this node by node~\n")
return;
end
q  = sort(x);           
tmp = graph;
% previous will remember the previous node
% on the path, so we can check for definite status
previous(1) = q(1);

while(k<=i && q(k)~=0)
    t = q(k)
    %mark t as visited
    v(k) = t;       
    k = k+1
    % in this for cycle adds all children of t and all nodes j  
    % such that t-j is in the pdag and  <previous(k-1),t,j> is of def. status
    %%i'm using the amat.cpdag encoding: amat(i,j) = 0, amat(j,i)=1 iff i -> j
    for j  = 1:length(tmp) 
      if (tmp(j,t) ~= 0  && tmp(t,j) ~= 2 )%cat(previous(k-1),t,j,"\n")
        if ((tmp(j,t) ==2 && tmp(t,j) == 3) || (previous(k-1)==t) || (tmp(j,previous(k-1)) ==0 && tmp(previous(k-1),j) ==0))
          %only add nodes that haven't been added
          if (~ismember(j, q) && ~ismember(j, y))           
            i = i+1;
            previous(i) = t;
            q(i) = j;
          end
        end
      end
    end
end
% remove all leftover zeros from initialization
bpossDes =setdiff(v,0);   
       
  
end
