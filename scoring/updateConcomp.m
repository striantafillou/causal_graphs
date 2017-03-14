function [nComps, sizes, comps, inComponent, k, m] = updateConcomp(from, to, nComps, sizes, comps, inComponent)
% update connected components if you add from <-> to.
% from, to must be in ascending order

% k-th component
k = inComponent(from);
% m-th component
m = inComponent(to);
if k==m
  %  fprintf('%d and %d already in the same component\n', from, to)
return;
end
if k>m
    tmp = k;
    k = m;m=tmp;
end

keepC =true(1, nComps);
nComps= nComps-1;
C_new = sort([comps{k} comps{m}]);

comps{k} = C_new;
sizes(k)= sizes(k)+sizes(m);


keepC(m)= false;

sizes = sizes(keepC);
comps = comps(keepC);

tmp = inComponent>m;
inComponent(tmp)= inComponent(tmp)-1;
inComponent(C_new) = k;

end
    


