function potdir = possibleDescendantsPag(pag)

G = pag ~= 0 & pag ~= 3 & pag' ~= 2;
potdir = transitiveClosureSparse_mex(sparse(G));
for i = 1:length(pag)
    potdir(i,i) = 0;
end

end
