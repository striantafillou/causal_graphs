function [A, B]= warshall2(A)
n=size(A, 1);
B= zeros(n);
for k = 1:n
    for i = 1:n
        for j = 1:n
            if A(i,k) && A(k,j)
                A(i,j) =true;
                B(i, j)=2;
            end
        end
    end
end
A(1:n+1:end)  = true;
end