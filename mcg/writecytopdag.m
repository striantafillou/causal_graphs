function [] = writecytopdag(pdag, fileName, headers)
fid = fopen(fileName, 'w');

nVars = length(pdag);
if nargin==2
    headers = strsplit(sprintf('X%d ', 1:nVars), ' ');
    headers = headers(1:nVars);
end

fprintf(fid, 'Source\t Interaction\t Target\n');
for X =1:nVars
    for Y =X+1:nVars
        if pdag(X, Y)==1 && pdag(Y, X)==1
            fprintf(fid, '%s\t ---\t%s\n', headers{X}, headers{Y});
        elseif pdag(X, Y)==1 
            fprintf(fid, '%s\t -->\t%s\n', headers{X}, headers{Y});
        elseif pdag(Y, X)==1
            fprintf(fid, '%s\t -->\t%s\n', headers{Y}, headers{X});
        end
    end
end

fclose(fid);
end