function cyto = write_cyto_file(g,file_name);

fid=fopen(file_name,'wt');

graph=g;
[rows, cols] = find(graph==1);
for i=1:length(rows)
    if graph(cols(i),rows(i))==0
        fprintf(fid,'X%s --> X%s\n',num2str(rows(i)),num2str(cols(i)));
        graph(rows(i),cols(i))=0
    elseif graph(cols(i),rows(i))==2  
         fprintf(fid,'X%s o-> X%s\n',num2str(rows(i)),num2str(cols(i)));
          graph(rows(i),cols(i))=0
    end
end

[rows, cols] = find(tril(graph==1));
for i=1:length(rows)
    fprintf(fid,'X%s <-> X%s\n',num2str(rows(i)),num2str(cols(i)));
 
end

[rows, cols] = find(tril(graph==2));
for i=1:length(rows)
    if graph(cols(i),rows(i))==2
        fprintf(fid,'X%s o-o X%s\n',num2str(rows(i)),num2str(cols(i)));
    end
end

[rows, cols] = find(tril(graph==3));
for i=1:length(rows)
    fprintf(fid,'X%s --- X%s\n',num2str(rows(i)),num2str(cols(i)));
 
end



fclose(fid);