function sc=skeleton_compare(original_dag,pag)
[r,c]=size(original_dag);
g1=zeros(r,c);
g2=zeros(r,c);
g1(find(original_dag+original_dag'))=1;
g2(find(pag+pag'))=1;

sc = g1-g2;
[r,c]=find(triu(sc)==1);

fprintf('Edges Removed:\n')

for i=1:length(r)
   fprintf('X%s --- X%s\n',num2str(r(i)),num2str(c(i)))
end

[r,c]=find(triu(sc)==-1);
fprintf('Edges Added:\n')
for i=1:length(r)
   fprintf('X%s --- X%s\n',num2str(r(i)),num2str(c(i)))
end
   
return;