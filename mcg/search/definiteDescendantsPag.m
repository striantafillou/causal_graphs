 function definiteDescendants =  definiteDescendantsPag(pag)
 %Finds definited descendants of a PAG (or mag)by creating a DAG with only the
 %directed edges.
 
dag =  zeros(size(pag));
[directedEdgesX, directedEdgesY] =  find(pag ==2 & pag' ==3);
dag(sub2ind(size(dag),directedEdgesX,directedEdgesY))=1;    
    
definiteDescendants = AllPairsDescendants_mex(sparse(dag));
definiteDescendants =  ~~definiteDescendants;
 end
    