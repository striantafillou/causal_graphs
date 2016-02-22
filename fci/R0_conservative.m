function [graph, dnc, cols, unfaithful, triangles] = R0_conservative(graph, sepSet, test, dataset, alpha, maxK, screen)
%function [graph, definiteNonColliders, colliders, unfaithful, triangles] = R0_conservative(graph, sepSet, test, dataset, alpha, maxK, screen)
% Orients unshielded colliders in pag graph and returns pag and also lists
% dnc and cols with triples that are definite non colliders and colliders, respectively.

numVars =  size(graph, 2);
tmpgraph = graph;
unshieldedTriples = zeros(numVars, 3);
triangles = zeros(numVars,3);
numTotalUnshieldedTriples = 0;
numTotalTriangles = 0;

dnc =  zeros(1,3);
numDefiniteNonColliders =  0;
cols =  zeros(1,3);
numColliders = 0;
unfaithful =  zeros(1,3);
numUnfaithful = 0;
% for each variable
for varX  =  1:numVars
    % find neighbors of X
    neighborsOfX =  find(graph(varX, :));
    % remove the edges so that you don't visit them again
    tmpgraph(varX, neighborsOfX) = 0;
    tmpgraph(neighborsOfX, varX) = 0;

    for varY =  neighborsOfX
        neighborsOfY =  find(tmpgraph(varY, :));
        % look only for ws larger than Xs to avoid having both x-y-w and
        % w-y-x
        neighborsOfY =  neighborsOfY(neighborsOfY>varX);
        %Record Unshielded triples
        varWs =  setdiff(neighborsOfY, neighborsOfX);
        numUnshielededTriplesIter = length(varWs);        
        unshieldedTriples(numTotalUnshieldedTriples+1: numTotalUnshieldedTriples+numUnshielededTriplesIter, :)= ...
            [repmat(varX, numUnshielededTriplesIter,1) repmat(varY, numUnshielededTriplesIter,1) varWs'];
        numTotalUnshieldedTriples =  numTotalUnshieldedTriples+numUnshielededTriplesIter;
        %Record Triangles
        varTs =  intersect(neighborsOfY, neighborsOfX);
        numTrianglesIter = length(varTs);        
        triangles(numTotalTriangles+1: numTotalTriangles+numTrianglesIter, :)= ...
            [repmat(varX, numTrianglesIter,1) repmat(varY, numTrianglesIter,1) varTs'];
        numTotalTriangles =  numTotalTriangles + numTrianglesIter;
    end
    tmpgraph =  graph;
end

if numTotalUnshieldedTriples ==0
    unshieldedTriples = [];
else
unshieldedTriples =  unshieldedTriples(1:numTotalUnshieldedTriples,:);
end
if numTotalTriangles ==0
    triangles = [];
else
triangles = triangles(1:numTotalTriangles,:);
triangles =  unique(sort(triangles,2), 'rows');
end

for iTriple =  1:numTotalUnshieldedTriples
    curTriple =  unshieldedTriples(iTriple,:);
    if screen
        fprintf('Now checking triple %s\n', num2str(curTriple))
    end
    if sepSet(curTriple(1), curTriple(3), curTriple(2)) == 1
        if isConservativeDNC(curTriple, graph,test, dataset, alpha, maxK)
            numDefiniteNonColliders =  numDefiniteNonColliders + 1;
            dnc(numDefiniteNonColliders, :) =  curTriple;
            if screen
                fprintf('Found DNC %d--%d--%d\n', curTriple(1), curTriple(2), curTriple(3))
            end
        else
            numUnfaithful =  numUnfaithful+1;
            unfaithful(numUnfaithful, :) =  curTriple;
            if screen
                fprintf('Found unfaithful triple %d--%d--%d\n', curTriple(1), curTriple(2), curTriple(3));
            end
        end
    elseif  sepSet(curTriple(1), curTriple(3), curTriple(2)) == 0
        if isConservativeCollider(curTriple, graph,test, dataset, alpha, maxK)
            numColliders =  numColliders + 1;
            cols(numColliders, :) =  curTriple;
            graph(curTriple(1), curTriple(2)) = 2;
            graph(curTriple(3), curTriple(2)) = 2;
            if screen
                fprintf('Found collider %d->%d<-%d\n', curTriple(1), curTriple(2), curTriple(3))
            end  
        else
            numUnfaithful =  numUnfaithful+1;
            unfaithful(numUnfaithful, :) =  curTriple;
            if screen
                fprintf('Found unfaithful triple %d--%d--%d\n', curTriple(1), curTriple(2), curTriple(3));
            end
          
        end
    else fprintf('Sth is wrong\n');
    end   
        
end

if numColliders == 0
    cols = [];
end
if numDefiniteNonColliders ==0
    dnc = [];
end
end

function isCollider =  isConservativeCollider(triple, graph,test, dataset, alpha, maxK)
% 0: the triple is unfaithfull
% -1 : the triple is a definite non collider 
% 1: the triple is a collider


isCollider = 1;

for iEndpoint=[1 3]
    a = triple(iEndpoint);
    b = triple(2);
    c = setdiff(triple, [a b]);
    
    neighbors =  find(graph(a,:));
    nofNeighbors = length(neighbors);
    if maxK  == -1 ||maxK>nofNeighbors
        maxCondSetSize =  nofNeighbors;
    else
        maxCondSetSize =  maxK;
    end
    n=0;


while n<maxCondSetSize
    n = n+1;
  
    condSets =  nchoosek(neighbors, n);
    nofCondSets =  size(condSets, 1);
    for iCondSet =  1: nofCondSets
        condSet =  condSets(iCondSet, 1:n);
        % Sets without b should NOT separate a and c if triple is a
        % non-collider
        %[p, ~, exitflag] = g2test_2(a, c, condSet, dataset.data, dataset.domain_counts);
        [p, ~, exitflag] = feval(test, a, c, condSet, dataset);
        if p>alpha      % if conditioning set renders them independent  

           if ismember(b, condSet)              
               isCollider =0; % b cannot be a member of this set AND a collider.
%                if screen 
%                   fprintf('%s is a condSet that d-separates %d and %d\n', num2str(condSet), a,c)
%                   fprintf('therefore %d cannot be a collider\n',b)
%                end     
               return;               
           end
        end
    end% end for iCondSet
end % end while
end % end for iEndpoint 
end

function isDNC =  isConservativeDNC(triple, graph, test, dataset, alpha, maxK)
% 0: the triple is unfaithfull
% 1: the triple is a conservative DNC

%screen = 0;

isDNC = 1;

for iEndpoint=[1 3]
    a = triple(iEndpoint);
    b = triple(2);
    c = setdiff(triple, [a b]);
   
    neighbors =  find(graph(a,:));
    nofNeighbors = length(neighbors);
    if maxK  == -1 ||maxK>nofNeighbors
        maxCondSetSize =  nofNeighbors;
    else
        maxCondSetSize =  maxK;
    end
    n=0;


while n<maxCondSetSize
    n = n+1;
   % fprintf('Now proceeding with conditioning sets of size %d\n',n)
    condSets =  nchoosek(neighbors, n);
    nofCondSets =  size(condSets, 1);
    for iCondSet =  1: nofCondSets
        condSet =  condSets(iCondSet, 1:n);
        % Sets without b should NOT separate a and c if triple is a
        % non-collider
        %[p, ~, exitflag] = g2test_2(a, c, condSet, dataset.data, dataset.domain_counts);
        p= feval(test,a, c, condSet, dataset);
        if p>alpha      % if conditioning set renders them independent  
           if ~ismember(b, condSet)              
               isDNC =0; % b cannot be a member of this set AND a collider.
%                if screen 
%                   fprintf('%s is a condSet that d-separates %d and %d\n', num2str(condSet), a,c)
%                   fprintf('therefore %d cannot be a DNC\n',b)
%                end     
               return;               
           end
        end
    end% end for iCondSet
end % end while
end % end for iEndpoint
end
