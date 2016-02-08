dag:
------------------------------
nVars x nVars adjacency matrix dag(i, j) =1 if i-->j in the graph

smm: (semi-Markov model)
------------------------------
nVars x nVars matrix:
smm(i,j) =2 if i*->j
smm(i,j) =3 if i*--J
smm(i, j) =4 if i*->j AND i*--J (j causes i AND are confounded).

mag: (Maximal Ancestral Graph)
-------------------------------
nVars x nVars matrix:
mag(i,j) =2 if i*->j
mag(i,j) =3 if i*--J

pag: (Partial Ancestral Graph)
-------------------------------
nVars x nVars matrix:
pag(i,j) = 1 if i*-oj
pag(i,j) =2 if i*->j
pag(i,j) =3 if i*--J
