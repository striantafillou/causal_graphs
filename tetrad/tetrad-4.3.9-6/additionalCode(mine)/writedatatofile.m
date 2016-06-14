fid=fopen('hugedata100','wt');
for k=1:50000
    for j=1:37
        fprintf(fid,'%s\t ',num2str(hugedata(k,j)));
    end
    fprintf(fid,'\n ');
end
fclose(fid);