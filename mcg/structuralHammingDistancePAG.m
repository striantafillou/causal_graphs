function shd = structuralHammingDistancePAG(PAG1, PAG2)


%G1 = PAG1.G;
%G2 = PAG2.G;

G1 = PAG1;
G2 = PAG2;

nnodes = length(G1);
shd = 0;
for i = 1:nnodes
    for j = i+1:nnodes
        % o-o
        if(G1(i,j) == 1 && G1(j,i) == 1)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 0;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 2;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 1;
            end
        end
        % o->
        if(G1(i,j) == 2 && G1(j,i) == 1)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 0;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 1;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 2;
            end
        end
        % <-o
        if(G1(i,j) == 1 && G1(j,i) == 2)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 0;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 2;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 2;
            end
        end
        % <->
        if(G1(i,j) == 2 && G1(j,i) == 2)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 0;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 1;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 3;
            end
        end
        % ->
        if(G1(i,j) == 2 && G1(j,i) == 3)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 0;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 3;
            end
        end
        % <-
        if(G1(i,j) == 3 && G1(j,i) == 2)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 1;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 2;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 0;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 3;
            end
        end
        % 'empty'
        if(G1(i,j) == 0 && G1(j,i) == 0)
            % o-o
            if(G2(i,j) == 1 && G2(j,i) == 1)
                shd = shd + 1;
            end
            % o->
            if(G2(i,j) == 2 && G2(j,i) == 1)
                shd = shd + 2;
            end
            % <-o
            if(G2(i,j) == 1 && G2(j,i) == 2)
                shd = shd + 2;
            end
            % <->
            if(G2(i,j) == 2 && G2(j,i) == 2)
                shd = shd + 3;
            end
            % ->
            if(G2(i,j) == 2 && G2(j,i) == 3)
                shd = shd + 3;
            end
            % <-
            if(G2(i,j) == 3 && G2(j,i) == 2)
                shd = shd + 3;
            end
            % 'empty'
            if(G2(i,j) == 0 && G2(j,i) == 0)
                shd = shd + 0;
            end
        end
    end
end
end