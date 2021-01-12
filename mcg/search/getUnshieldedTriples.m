function [unshieldedtriples] = getUnshieldedTriples(G)

nnodes = length(G);
unshieldedtriples = cell(nnodes,1);

for i = 1:nnodes
    neighbours = find(G(i,:)');
    [x y] = find(triu(~G(neighbours,neighbours)) & ~eye(length(neighbours)));
    %[x y] = find(triu(G(neighbours,neighbours)) & ~eye(length(neighbours)));
    unshieldedtriples{i} = [neighbours(x) neighbours(y)];
end

end
