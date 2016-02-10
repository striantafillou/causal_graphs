% simulate data from y-structure with random parameters.
graph = zeros(4); graph([1, 2], 3)=1; graph(3, 4) =1;
type = 'discrete';
headers ={'X', 'Y', 'Z', 'W'};
nSamples =100;
[nodes, domainCounts] = convertGraphToBN(graph, type,  'headers', headers);
dataset = simulateData(nodes, nSamples, domainCounts, type);

type = 'gaussian';
[nodes, domainCounts] = convertGraphToBN(graph, type,  'headers', headers);
dataset = simulateData(nodes, nSamples, domainCounts, type);

