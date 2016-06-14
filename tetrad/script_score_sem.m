clear java;
javaaddpath('tetrad\tetrad-4.3.10-0.jar');

jarPath ='tetrad\tetrad-4.3.9-6\lib\';
jarFiles = dir(jarPath);
for i=3:length(jarFiles)
javaaddpath([jarPath jarFiles(i).name]);
end

nVars =5;
dag = randomdag(nVars,3);
bn = dag2randBN(dag,'gaussian');

isLatent= false(1,nVars);
dataset = simulatedata(bn, 1000, [], 'gaussian', 'isLatent', isLatent);


ds = tetrad_dataset(dataset);
smm = dag2smm(dag,isLatent);
tGraph = tetrad_graph_from_matrix(smm);
sem =sem_from_smm(smm);

% pick optimizer. If sem is not a DAG, you need a general-purpose optimizer
% of ricf.
opt = javaObject('edu.cmu.tetrad.sem.SemOptimizerNrPowell');


estimator = javaObject('edu.cmu.tetrad.sem.SemEstimator', ds, sem, opt);
estimator.estimate;

edgeCoeffs = double(estimator.getEstimatedSem.getEdgeCoef.toArray);
erroCov = double(estimator.getEstimatedSem.getErrCovar.toArray);
dof =  nVars*(nVars+1)/2-estimator.getEstimatedSem.getFreeParameters.size;
bic = double(estimator.getEstimatedSem.getBicScore);