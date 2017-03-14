function [nComps, sizes, comps, inComponent, k, m] = updateConcompRem(from, to, nComps, sizes, comps, inComponent, mag)
% update connected components if you rmove from *-*to.
% from, to must be in ascending order

% k-th component, the component you may split
k = inComponent(from);compK = comps{k};

mag_bidir = mag==2 & mag'==2;
mag_bidir(from, to)=0; mag_bidir(to, from)=0;
mag_bidir_k = mag_bidir(compK, compK);

[nCompsK, sizesK, compsK]= concomp(mag_bidir_k);
if nCompsK==1
    m=nan;
    return;
elseif nCompsK>2
    fprintf('More than 2 connected components???\n');
    return;
else % split components
%fprintf('in\n')
    compK_new = sort(compK( compsK{1}));
    compM = sort(compK(compsK{2}));
    minM =compM(1); % should probably be first anyway
    m = max(inComponent(1:minM-1))+1; % position of the new component
    newC(1:m-1) = comps(1:m-1);
    newC{k} = compK_new;
    newC{m} = compM;
    newC(m+1:nComps+1) = comps(m:nComps);
    comps=newC;
    sizes(m+1:nComps+1)= sizes(m:nComps);
    sizes(m)= sizesK(2);
    tmp = inComponent>m-1;
    inComponent(tmp)= inComponent(tmp)+1;
    inComponent(compM) = m;
    nComps=nComps+1;
end % end if
end % end function

    