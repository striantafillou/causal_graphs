function pag = FCI(dataset, varargin)
% FUNCTION PAG = FCI(DATASET, VARARGIN)
% Runs FCI 
% author: striant@csd.uoc.gr
% =======================================================================
% Inputs
% =======================================================================
% dataset                 = a dataset of measured variables (*help 
%                           simulateData for details on the dataset structure)
% =======================================================================
% Output
% =======================================================================
% pag                    = struct describing the output pag, 
%   .graph               = nVars x nVars matrix, graph(i, j) =
%                                                        1 if i*-oj,
%                                                        2 if i*->j,
%                                                        3 if i*--j.
%   .sepSet              = nVars x nVars x nVars matrix, sepSet(i, j, k)=1
%                          if k $\in$ sepSet(i, j)
%   .maxSepSet           = nVars x nVars x nVars matrix maxSepSet(i, j, k)=1
%                          if k $\in$ the set for which the max p-value of
%                          any attempted test of independence for i and j
%                          was achieved.
%   .pvalues             = nVars x nVars matrix, pvalues(i, j) the maximum
%                          p-value for any attempted test of independence
%                          for i and j.
%   .ddnc                = list (ndDnc x 3) of discriminating definite non 
%                          colliders.
%   .dcolliders          = list  of discriminating  colliders.
%   .dnc                 = list of unshielded definite non colliders.
%   .colliders           = list of unshielded colliders
%   .nTests              = number of tests attempted by the algorithm.
%
%   [...] = FCI(...,'PARAM1',VAL1,'PARAM2',VAL2,...) specifies additional
%     parameters and their values.  Valid parameters are the following:
%  
%          Parameter     Value
%           'test'       conditional test of independence used. Default:
%                        'g2test_2' for discrete data, 
%                        'fisher' for continuous data,
%                        'msep' for oracle data.
%           'heuristic'  heuristic to implement during the skeleton searh.
%                        'PC1': Pairs and conditioning sets in
%                        lexicographic order. 
%                        'PC2': Pairs ordered in decreasing dependence,
%                        conditioning sets in lexicographic order.
%                        'PC3' Pairs ordered in decreasing dependence,
%                        conditioning sets ordered in increasing
%                        dependence with either pair variable. (default).
%           'alpha'      rejection region for test of independence. Default 0.05
%           'maxK'       maximum conditioning set size. Default: 4
%           'cons'       true (default) for conservative rules for
%                        colliders(Ramsey et al, UAI 2006)
%           'pdSep'      false (default) for skipping the possible
%                        d-separating step of FCI (see SGS, p.187)
%           'verbose'    true for screen output, false(default) otherwise.
%           'debug'      true for detailed screen output, false(default)
%                        otherwise.
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if isequal(dataset.type , 'discrete') % discrete variables 
  [test,  heuristic, alpha,  maxK, pdSep, cons, verbose] = ...
      process_options(varargin, 'test', 'g2test_2', 'heuristic', 3, 'alpha', 0.05, 'maxK', 4, 'pdSep', false, 'cons', false, 'verbose', false);
elseif isequal(dataset.type , 'continuous')
   [test,  heuristic, alpha,  maxK, pdSep, cons, verbose] =...
       process_options(varargin, 'test', 'fisher', 'heuristic', 3, 'alpha', 0.05, 'maxK', 4, 'pdSep', false, 'cons', false, 'verbose', false);
elseif isequal(dataset.type , 'oracle')
    [test,  heuristic, alpha,  maxK, pdSep, cons, verbose] =...
        process_options(varargin, 'test', 'msep', 'heuristic', 3, 'alpha', 0.05, 'maxK', 4, 'pdSep', true, 'cons', false, 'verbose', true);
else
    errprintf('Unknown dataset type\n')
end


%Step 1. Find pag skeleton.
if verbose
    fprintf('-------------------------------------------------------------------------------\n')
    fprintf('Running FCI, params:\n')
    fprintf('\t test: %s \n\t heuristic: %d \n\t alpha: %.3f\n\t maxK: %d\n\t pdSep: %d \n\t cons: %d\n\t verbose %d\n', test, heuristic, alpha,  maxK, pdSep, cons, verbose)

    fprintf('Step1. Identifying skeleton\n');
end
[graph, sepSet, pag.pvalues, pag.maxSepSet, pag.nTests] = fciskeleton(dataset, test, heuristic, alpha, maxK,  pdSep, verbose);
if verbose
    fprintf('Step2. Orientations\n');
end
if cons
    [graph, dnc, cols, pag.unfaithful] = R0_conservative(graph, sepSet, test, dataset, alpha, maxK, verbose);
else
    [graph, dnc,  cols] = R0(graph, sepSet, verbose);
end
% Iteratively apply rules R1-R4 until none of them applies
flag=1;
ddnc =[]; dcol =[];
while(flag)
    flag=0;
    [graph,flag] = R1(graph, flag, verbose);
    [graph,flag] = R2(graph, flag, verbose);
    [graph,flag] = R3(graph, flag, verbose);
    [graph, ddnc, dcols, flag] = R4(graph, sepSet, ddnc, dcol, flag, verbose);
end    
% Iteratively apply rules R8-R10 until none of them applies
% flag=1;
% while(flag)
%     flag=0;
%     [graph,flag] = R8(graph, flag, verbose);
%     [graph,flag] = R9_R10(graph, dnc, flag, verbose);
% end 

% Apply orientation rules
% Apply R0: Orient unshielded colliders
% [pag, definiteNonColliders,  colliders, triangles] = orientunshieldedcolliders(pag, sepSet, verbose);

% if cons
%     [graph, definiteNonColliders, colliders] = ...
%     R0_conservative(graph, sepSet, test, dataset, alpha, maxK, verbose);
%     [graph, ddnc, dcol, ~] = FCI_rules_magR12R4(graph, sepSet, verbose);
% else 
%     [graph, definiteNonColliders,  colliders] = R0(graph, sepSet, verbose);
%     % Iteratively apply rules R1-R4 until none of them applies
%     [graph, ddnc, dcol, ~] = FCI_rules_magR12R4(graph, sepSet, verbose);
% end

pag.graph = graph;
pag.ddnc = ddnc;
pag.dcolliders = dcols;
pag.colliders = cols;
pag.dnc = dnc;
pag.sepSet = sepSet;
end


function [graph, flag] = R1(graph, flag, verbose)
% If x*->yo-*c and x,z not adjacent ==> x*->y->z

[Xs,Ys] = find(graph == 1);
len = length(Xs);
for i = 1:len
    if(graph(Xs(i),Ys(i)) == 1 && any(graph(:,Ys(i)) == 2 & graph(:,Xs(i)) == 0))
        if(verbose)
            fprintf('\tR1: Orienting %d->%d\n',Ys(i),Xs(i));
        end
        graph(Ys(i),Xs(i)) = 2;
        graph(Xs(i),Ys(i)) = 3;
        flag = 1;
    end
end

end

function [graph, flag] = R2(graph, flag, verbose)
% If x->y*->z or x*->y->z, and x*-oz ==> x*->z

[Xs, Zs] = find(graph == 1);
len = length(Xs);
for i = 1:len
    if(graph(Xs(i),Zs(i)) == 1 && any(graph(Xs(i),:) == 2 & graph(:,Zs(i))' == 2 & (graph(:,Xs(i))' == 3 | graph(Zs(i),:) == 3)))
        if(verbose)
            fprintf('\tR2: Orienting %d*->%d\n',Xs(i),Zs(i));
        end
        graph(Xs(i),Zs(i)) = 2;
        flag = 1;
    end
end

end

function [graph, flag] = R3(graph, flag, verbose)
% If x*->y<-*z, x*-o8o-*z, x,z not adjacent, 8*-oy ==> 8*->y

[Ths, Ys] = find(graph == 1);
nedges = length(Ths);

for i = 1:nedges
    a = find(graph(:,Ths(i)) == 1 & graph(:,Ys(i)) == 2);
    len = length(a);
    f = false;
    for j = 1:len
        for k = j+1:len
            if(graph(a(j),a(k)) == 0 && graph(Ths(i),Ys(i)) == 1)
                if(verbose)
                    fprintf('\tR3: Orienting %d*->%d\n',Ths(i),Ys(i));
                end
                graph(Ths(i),Ys(i)) = 2;
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

function [graph, ddnc, dcol, flag] = R4(graph, sepSet, ddnc, dcol, flag, verbose)

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

nnodes = length(graph);
dir = graph == 2 & graph' == 3;
bidir = graph == 2 & graph' == 2;

for curc = 1:nnodes
    b = find(graph(curc,:) == 1);
    if(isempty(b))
        continue;
    end
    
    th = find(graph(curc,:) == 0);
    if(isempty(th))
        continue;
    end
    
    curdir = dir(:,curc)';
    
    G = zeros(nnodes,nnodes);
    for curth = th
        G(curth,graph(curth,:) == 2 & curdir) = 1;
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
            if(graph(curb,cura) == 2)
                if(any(closure(th,cura) & sepSet(th,curc,curb)))
                    if verbose
                        fprintf('\tR4: Orienting %s ... %d->%d\n', num2str(find(closure(th,cura) & sepSet(th,curc,curb))), curb,curc);
                    end
                    graph(curb, curc) = 2;
                    graph(curc, curb) = 3;
                    ddnc = [ddnc; cura curb curc];
                else
                    if verbose
                        fprintf('\tR4: Orienting %s, ... %d<->%d\n', num2str(find(closure(th,cura) & sepSet(th,curc,curb))), curb,curc);
                    end
                    graph(curb, curc) = 2;
                    graph(curc, curb) = 2;
                    graph(cura, curb) = 2;
                    dcol = [dcol; cura curb curc];
                end
                flag = 1;
                break;
            end
        end
    end
end
end

function [graph,flag] = R8(graph, flag, verbose)

[r,c] = find(graph == 2 & graph' == 1);
nedges = length(r);

for i = 1:nedges
    out = find(graph(:,r(i)) == 3);
    if(any(graph(out,c(i)) == 2 & graph(c(i),out)' == 3))
        if(verbose)
            fprintf('\tR8: Orienting %d->%d\n',r(i),c(i));
        end
        graph(c(i),r(i)) = 3;
        flag = 1;
    end
end
end

function [graph,flag] = R9_R10(graph,dnc,flag,verbose)

[r,c] = find(graph == 2 & graph' == 1);
nedges = length(r);

% R9: Equivalent to orienting X <-o Y as X <-> Y and checking if Y is an
% ancestor of X (i.e. there is an almost directed cycle)
for i = 1:nedges
    tmpgraph = graph;
    tmpgraph(c(i),r(i)) = 2;
    tmpgraph = orientDnc_mex(tmpgraph, c(i), r(i));
    if(isReachablePag_mex(tmpgraph,r(i),c(i)))
        if(verbose)
            fprintf('\tR9: Orienting %d*--%d\n',c(i),r(i));
        end
        graph(c(i),r(i)) = 3;
        flag = 1;
    end
end

% Fast, trivial check if there is a potentially directed path
possibleClosure = transitiveClosureSparse_mex(sparse((graph == 1 & graph' ~= 2) | (graph == 2 & graph' == 3)));

% R10: Equivalent to checking if for some definite non collider V - X - W
% and edge X o-> Y, X->V and X->W both create a directed path to Y after
% oriented
closures = zeros(length(graph),length(graph));
tested = false(1,length(graph));
if isempty(dnc); return;end
for s = 1:length(graph)
    tested(:) = false;
    curDnc = dnc(ismember(dnc(:, 2), s),:);
    %curDnc = dnc{s};
    ndnc = size(curDnc,1);
    
    for t = find(graph(:,s)' == 1 & graph(s,:) == 2)
        for j = 1:ndnc
            a = curDnc(j,1);
            b = curDnc(j,3);
            if(~possibleClosure(a,t) || ~possibleClosure(b,t) || graph(a,s) == 2 || graph(b,s) == 2 || a == t || b == t)
                continue;
            end
            
            if(~tested(a))
                tmpgraph = graph;
                tmpgraph(s,a) = 2;
                tmpgraph(a,s) = 3;
                tmpgraph = orientDnc_mex(tmpgraph,s,a);
                closures(a,:) = findAncestorsPag_mex(tmpgraph',s);
                tested(a) = true;
            end
            if(~closures(a,t))
                continue;
            end
            
            if(~tested(b))
                tmpgraph = graph;
                tmpgraph(s,b) = 2;
                tmpgraph(b,s) = 3;
                tmpgraph = orientDnc_mex(tmpgraph,s,b);
                closures(b,:) = findAncestorsPag_mex(tmpgraph',s);
                tested(b) = true;
            end
            if(~closures(b,t))
                continue;
            end
            
            if(verbose)
                fprintf('\tR10: Orienting %d*--%d\n',t,s);
            end
            graph(t,s) = 3;
            flag = 1;
            break;
        end
        
    end
end

end

