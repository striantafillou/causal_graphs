function MG = moralize(M)

M_dir = M == 2 & M' == 3;
M_bidir = M == 2 & M' == 2;

MG = ~~(M);

comps = concomp(M_bidir);
nComps=max(comps);
for iComp =1:nComps
    curComp = comps==iComp;
    compParents = M_dir(:, curComp);
    MG(compParents, compParents)=true;
end
MG = MG-diag(diag(MG)); % remove diagonal elements
end
    


