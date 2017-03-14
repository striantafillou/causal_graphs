function sem =  semfromsmm(smm)
    
tGraph= tetrad_graph_from_matrix(smm);
semPm = javaObject('edu.cmu.tetrad.sem.SemPm', tGraph);
% estimator = javaObject('edu.cmu.tetrad.sem.SemEstimator', dataSet,semPm);
% estimator.estimate;
% dofs = nVars*(nVars+1)/2-estimator.getEstimatedSem.getFreeParameters.size;
%    
% bic= estimator.getEstimatedSem.getBicScore;
end