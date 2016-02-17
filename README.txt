INTRODUCTION
Causal graphs includes some basic functions for mixed causal graphs, as well as implementations of the PC and FCI algorithms.
The package was created in MATLAB R2013a.

CONTENTS
This software provides basic applications for the following types of mixed causal graphs: 

dag:
------------------------------
nVars x nVars adjacency matrix dag(i, j) =1 if i-->j in the graph

smm: (semi-Markov Causal model)
------------------------------
nVars x nVars matrix:
smm(i,j) =2 if i*->j
smm(i,j) =3 if i*--J
smm(i, j) =4 if i*->j AND i*--J (j causes i AND are confounded).

Maximal Ancestral Graph
-------------------------------
nVars x nVars matrix:
mag(i,j) =2 if i*->j
mag(i,j) =3 if i*--J

pag: (Partial Ancestral Graph)
-------------------------------
nVars x nVars matrix:
pag(i,j) = 1 if i*-oj
pag(i,j) =2 if i*->j
pag(i,j) =3 if i*--j

To construct a BN you need a dag and a parametrization P(X|Pa(X)) for each variable.

List of folders:

dataset: Includes functions for creating graphs and simulating datasets.
The dataset is a struct describing the data, with the following fields: 
   .data                   nSamples x nVars matrix containing the data
   .domain_counts       = nVars x 1 vector # of possible values for each 
                          variable, (empty matrix for continuous
                          variables)
   .isLatent            = nVars x 1 boolean vector, true for latent
                          variables
   .isManipulated       = nVars x 1 boolean vector, true for manipulated
                          variables  

dag2randBN.m
	converts a dag (adjacency matrix) to a Bayes net with random parametrization (discrete of gaussian).
simulatedata.m
	simulates a data from a BN (discrete or gaussian).
simulateoracle data.m
	creates pseudo dataset to run the algorithms with an oracle of conditional independence, the struct fields are isLatent, isManipulated as above and:
    .data                 = the semi-Markov causal model of the dag after
                            marginalizing out the variables isLatent
    .isAncestor           = nVars x nVars ancestor matrix: 
                           isAncestor(i,j) =1 if i is an ancestor of j
                           in graph, 0 otherwise
fci: Includes implementations of the PC and FCI algorithms.
FCI: FCI algorithm, without the rules for selection bias. calling FCI with rules R0-R4 and pdsep = false is essentially the PC algorithm.
FCI returns a pag structure with the following fields:
  .graph               = nVars x nVars matrix, graph(i, j) =
                                                       1 if i*-oj,
                                                       2 if i*->j,
                                                       3 if i*--j.
  .sepSet              = nVars x nVars x nVars matrix, sepSet(i, j, k)=1
                         if k $\in$ sepSet(i, j)
  .maxSepSet           = nVars x nVars x nVars matrix maxSepSet(i, j, k)=1
                         if k $\in$ the set for which the max p-value of
                         any attempted test of independence for i and j
                         was achieved.
  .pvalues             = nVars x nVars matrix, pvalues(i, j) the maximum
                         p-value for any attempted test of independence
                         for i and j.
  .ddnc                = list (ndDnc x 3) of discriminating definite non 
                         colliders.
  .dcolliders          = list  of discriminating  colliders.
  .dnc                 = list of unshielded definite non colliders.
  .colliders           = list of unshielded colliders
  .nTests              = number of tests attempted by the algorithm.

fciskeleton: The skeleton search step of FCI algorithm. Called internally by fci.

findpdsepset: Finds possible d-separating set between two variables, after the basic skeleton search and orientation of the colliders.

finddncdag: Finds the definite non colliders of a dag.


INSTALLATION
Before running the code, go to \util\mex and run make.m. Most c code by borbudak.


LICENSE 
This software is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This software is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%