nVars =15;
maxParents=3;
type='gaussian';

dag = randomdag(nVars, maxParents);

bn = dag2randBN(dag, type);

dataset = simulatedata(bn, 10000, [], type);
%%
[pag, sepSet,pvalues, maxSepSet, pdsep] = fciskeleton(dataset, 'fisher', 3, 0.05, 4,  false, true);