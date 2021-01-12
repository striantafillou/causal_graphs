function bool = isPossiblyInducing(path, isLatent, isManipulated, smm, dnc,  possibleDescendants)
bool=1;
if isempty(path)
    bool=0;
    return;
end
if any([smm(path(2), path(1))==2*isManipulated(path(1)) smm(path(end-1), path(end))==2*isManipulated(path(end))...
        isManipulated(path(2:end-1))' isManipulated(path(1))*isManipulated(path(end))])
    bool =0;
    return;
end
nTriples = length(path)-2;
for iTriple = 1:nTriples
    X =  path(iTriple);Y =  path(iTriple+1); Z = path(iTriple+2);
    if isLatent(Y)
        if iscollider(X, Y, Z, smm) && ~possibleDescendants(Y, path(1)) &&...
         ~possibleDescendants(Y, path(end-1))
            bool = 0;
            return;
        end
    else
        if isManipulated(Y)||smm(X, Y)==0 || smm(Y, Z)==0 || isnoncollider(smm, X, Y, Z, dnc) ...
            ||~(possibleDescendants(Y, path(1)) || possibleDescendants(Y, path(end)))
        bool =0;
        return;
        end
    end
end
end

function bool =  isnoncollider(smm, X, Y, Z, dnc)

bool = 0;

if ~isempty(dnc) && (ismember([X Y Z], dnc, 'rows') || ismember([Z Y X], dnc, 'rows'))
    bool = 1;
    return;
end
if smm(X, Y)==3 || smm(Z, Y)==3
    bool =1;
end
end

function bool = iscollider(X, Y,Z, smm)
bool =0;
if smm(X, Y)==2 && smm(Z, Y)==2
    bool =1;
end
end