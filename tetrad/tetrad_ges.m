% script_run_fges.m

% check calibration of P(Do|Hw)
clear;
%add causal_graphs to simulate data
addpath(genpath('C:\Users\sot16.PITT\Documents\MATLAB\causal_graphs\'))

%java imports
javaaddpath('C:\Users\sot16.PITT\Documents\MATLAB\causal_graphs\tetrad\tetrad-5.0.0-3.jar') %jar path
import edu.cmu.tetrad.*
import java.util.*
import java.lang.*
import edu.cmu.tetrad.data.*
import edu.cmu.tetrad.search.*
import edu.cmu.tetrad.graph.*

% control panel
nVars =10;
N=1000;
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.DiscreteVariable',['X' num2str(i)]);
    list.add(var);
end
%simulate dag
dag = randomdag(nVars, 4);
[nodes, domainCounts] = dag2randBN(dag, 'discrete', 'maxNumStates', 3*ones(nVars, 1), 'minNumStates', 3*ones(nVars, 1));
obsDataset = simulatedata(nodes, N, 'discrete', 'domainCounts', domainCounts);
ds = javaObject('edu.cmu.tetrad.data.ColtDataSet', N,list);
for i=0:(N-1)
    for j=0:(nVars-1)
        ds.setInt(i,j,obsDataset.data((i+1),(j+1)));
    end
end 

% Run GES
ges= javaObject('edu.cmu.tetrad.search.Ges',ds);
ges.setAggressivelyPreventCycles(true);
pdagt = ges.search;
pdag = tetradGraphtoAdjMat(pdagt, nVars);
