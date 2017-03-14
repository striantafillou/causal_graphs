function mag = performAction(mag, from, to, edgeType, actionID)

% if is directed

if edgeType==1% is directed from->to
    switch actionID
        case 1
            mag(to, from) =0;mag(from, to) =0;
        case 2
            mag(to, from) =3;mag(from, to) =2;
        case 3
            mag(to, from) =2;
    end
elseif edgeType==2% is directed to->from
    switch actionID
        case 1
            mag(to, from) =0;mag(from, to) =0;
        case 2
            mag(to, from) =2;mag(from, to) =3;
        case 3
            mag(from, to) =2;
    end

elseif edgeType==3 % is bidirected
    switch actionID
        case 1
            mag(to, from) =0;mag(from, to) =0;
        case 2
            mag(to, from) =3;mag(from, to) =2;
        case 3
            mag(from, to) =3;
    end

elseif edgeType==4 % is absent
    switch actionID
         case 1
            mag(to, from) =3;mag(from, to) =2;
        case 2
            mag(to, from) =2;mag(from, to) =3;
        case 3
            mag(to, from) =2;mag(from, to) =3;
    end
end
end
        