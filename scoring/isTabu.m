function bool = isTabu(from, to, iAct, mag, tabu)

bool=false;
switch iAct
    case 1
        mag(from, to)=2; mag(to, from)=3;
    case 2
        mag(from, to)=3; mag(to, from)=2;
    case 3
        mag(from, to)=2; mag(to, from)=2;
    case 4
        mag(from, to)=0; mag(to, from)=0;
end


for m=1:size(tabu,1)
    if all(all(mag==tabu{m}))==1
        bool = true;
        break;
    end
end