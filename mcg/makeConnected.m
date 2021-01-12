function dag = makeConnected(dag)
%function dag = makeConnected(dag)
% Adds edges to make sure the dag is connected

[nComponents, ~, members] = concomp(dag);
if nComponents>1
    for iComponent=1:nComponents-1
        inA = members{iComponent}(1);
        inB = members{iComponent+1}(1);
        dag(inA, inB) = 1;
    end
end
end