function [pdag] = fci_outfile2pag(fname);



fid = fopen(fname, 'r');

if fid==-1
    pdag==-1;
    return;
end

while 1
    
    l = fgetl(fid);
    
    if ~ischar(l), break, end
    
    if strcmp(l, ''), continue, end

        if ~isempty(strfind(l, '-'))
           
            
            if ~isempty(strfind(l, '-->'))
                start_num1 = strfind(l,'X')
                end_num1 = strfind(l,'-')
                num1 = str2num(l(start_num1(1)+1:end_num1-1));
                num2 = str2num(l(start_num1(2)+1:end));
                pdag(num1, num2) = 1;
            elseif ~isempty(strfind(l, '---'))
                start_num1 = strfind(l,'X')
                end_num1 = strfind(l,'-')
                num1 = str2num(l(start_num1(1)+1:end_num1-1));
                num2 = str2num(l(start_num1(2)+1:end));
                pdag(num1, num2) = 3;
                pdag(num2, num1) = 3;
            elseif ~isempty(strfind(l, 'o->'))
                start_num1 = strfind(l,'X')
                end_num1 = strfind(l,'o')
                num1 = str2num(l(start_num1(1)+1:end_num1-1));
                num2 = str2num(l(start_num1(2)+1:end));
                pdag(num1, num2) = 1;
                pdag(num2, num1) = 2;
            elseif ~isempty(strfind(l, 'o-o'))
                start_num1 = strfind(l,'X')
                end_num1 = strfind(l,'o')
                num1 = str2num(l(start_num1(1)+1:end_num1-1));
                num2 = str2num(l(start_num1(2)+1:end));
                pdag(num1, num2) = 2;
                pdag(num2, num1) = 2;        
            end
            
        end
end


fclose(fid);