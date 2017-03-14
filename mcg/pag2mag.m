function [mag, sat] = pag2mag(pag)
% finds a mag from a pag based on theorem 2 of "On the completeness...",
% Zhang, 2008

mag = pag;
%Find circles in o-> edges and turn them into tails.
circles = pag==1&pag'==2;
mag(circles) =3;
% Orient the circle component according to Meek's algorithm for chordal
% graphs. This only works if the graph is chordal. 

% find the circle component.
pag_c = zeros(size(pag));
pag_c(pag==1&pag'==1)=1;

if nnz(pag_c)>0 && ~ischordal(pag_c)
    sat= false;
    %fprintf('The circle component is not chordal. Output may not be a correct MAG\n');
end

while(any(any(mag==1)))
    % pick an edge
    [x, y] = find(pag_c==1, 1);

    mag(x,y) =2; mag(y, x)=3;
    pag_c(x,y)=0; pag_c(y, x)=0;

    % apply orientation rules
    mag =  FCI_rules_apply(mag, false);
end

end


function [G] = FCI_rules_apply(G, screen)
% Applies only rules R1-R3 from FCI.
% This is sound but NOT COMPLETE.
% It is an open problem to devise a complete set of rules to propagate a
% given set of orientations.

flag=1;
while(flag)
    flag=0;
    [G,flag] = R1(G, flag, screen);
    [G,flag] = R2(G, flag, screen);
    [G,flag] = R3(G, flag, screen);
end

end

function [Pag flag] = R1(Pag, flag, screen)
% If a*->bo-*c and a,c not adjacent ==> a*->b->c

[c b] = find(Pag == 1);
len = length(c);
for i = 1:len
    if(Pag(c(i),b(i)) == 1 && any(Pag(:,b(i)) == 2 & Pag(:,c(i)) == 0))
        if(screen)
            fprintf('R1: Orienting %d->%d\n',b(i),c(i));
        end
        Pag(b(i),c(i)) = 2;
        Pag(c(i),b(i)) = 3;
        flag = 1;
    end
end

end

function [Pag flag] = R2(Pag, flag, screen)
% If a->b*->c or a*->b->c, and a*-oc ==> a*->c

[a c] = find(Pag == 1);
len = length(a);
for i = 1:len
    if(Pag(a(i),c(i)) == 1 && any(Pag(a(i),:) == 2 & Pag(:,c(i))' == 2 & (Pag(:,a(i))' == 3 | Pag(c(i),:) == 3)))
        if(screen)
            fprintf('R2: Orienting %d*->%d\n',a(i),c(i));
        end
        Pag(a(i),c(i)) = 2;
        flag = 1;
    end
end

end

function [Pag flag] = R3(Pag, flag, screen)
% If a*->b<-*c, a*-o8o-*c, a,c not adjacent, 8*-ob ==> 8*->b

[th b] = find(Pag == 1);
nedges = length(th);

for i = 1:nedges
    a = find(Pag(:,th(i)) == 1 & Pag(:,b(i)) == 2);
    len = length(a);
    f = false;
    for j = 1:len
        for k = j+1:len
            if(Pag(a(j),a(k)) == 0 && Pag(th(i),b(i)) == 1)
                if(screen)
                    fprintf('R3: Orienting %d*->%d\n',th(i),b(i));
                end
                Pag(th(i),b(i)) = 2;
                flag = 1;
                f = true;
                break;
            end
        end
        if(f)
            break;
        end
    end
end

end

function bool = hasanycycle(closure, pag)

bool = any(diag(closure))|| any(any((closure | closure') & pag == 2 & pag' == 2));
end
