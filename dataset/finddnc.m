function dnc = finddnc(dag)
dnc = [];
nVars = length(dag);

for X=1:nVars
    for Y =X+1:nVars
        if dag(X, Y)~=1 && dag(Y, X)~=1 
            continue;
        end
        for Z =1:nVars
            if (dag(Z, Y)~=1 && dag(Y, Z)~=1) || (dag(X, Z)==1 || dag(Z, X)==1)
                continue;
            end
            if X==Z;continue; end
            fprintf('Testing %d -%d-%d\n', X, Y, Z);
            if (dag(X, Y)~=1 || dag(Z, Y)~=1) && dag(X, Z) ==0 && dag(Z, X)==0
                dnc = [dnc; X Y Z];
            end
        end
    end
end
end

