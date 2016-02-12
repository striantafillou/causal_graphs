function [] = writecytomcg(mcg, fileName, headers)
fid = fopen(fileName, 'w');

nVars = length(mcg);
if nargin==2
    headers = strsplit(sprintf('X%d ', 1:nVars), ' ');
    headers = headers(1:nVars);
end

symbolsXY = {'o', '>', '-', 'z'};
symbolsYX = {'o', '<', '-', 'z'};
fprintf(fid, 'Source Interaction Target\n');
for X =1:nVars
    for Y =X+1:nVars
        if mcg(X, Y) 
            fprintf(fid, '%s %s-%s %s\n', headers{X}, symbolsYX{mcg(Y, X)},symbolsXY{mcg(X, Y)}, headers{Y});
        end
    end
end

fclose(fid);
end