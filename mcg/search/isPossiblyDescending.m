function bool =  isPossiblyDescending(path, pag, isManipulated)

bool =1;
len =  length(path);
fromendpoints = path(1:len-1);
toendpoints =  path(2:len);

if any(isManipulated(path(2:end)))
    bool=0;
    return;
end
if any(ismember([0, 3], pag(sub2ind(size(pag), fromendpoints, toendpoints))))
    bool =0;
    return;
elseif any(ismember([0, 2], pag(sub2ind(size(pag), toendpoints, fromendpoints))))
    bool =  0;
    return;
end
end
