function bool = ischordal(amat)

%%needs checking
bool= true;
% find nodes with an undirected edge
[rows, ~]  = find(amat==1&amat'==1);
rows= unique(rows);

% no edges, trivially chordal
if isempty(rows)
    bool = false;
    true;
end

% initialize partitions
S = rows(2:end)';
MA = rows(1);
while ~isempty(S)
    dim = zeros(1, length(S));
    for i=1:length(S)
        dim(i) = sum(amat(S(i), MA)==1);
    end
    % pick next node
    [~, ind] = max(dim);
    s = S(ind);
    ns = find(amat(s, MA)==1);
    if min(amat(ns, ns)+eye(length(ns)))==0
        bool=false;
        return;
    end
     MA = [MA, s];
     S = setdiff(S, s);
end
end

