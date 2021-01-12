function pag =tetradPagtoMcg(pagt, nVars)

nodes = pagt.getNodes;
pag = zeros(nVars);

edges = pagt.getEdges;
edges = edges.toArray;

nEdges = size(edges, 1);
for iEdge =1:nEdges
    curEdge= char(edges(iEdge).toString);
    curEdge= strsplit(curEdge, ' ' );
    from  = curEdge{1};x = str2double(from(2:end));
    to  = curEdge{3};y = str2double(to(2:end));
    edge = curEdge{2};
    switch edge(1)
        case 'o'
            pag(y, x)=1;
        case '-'
            pag(y, x)=3;
        case '<'
            pag(y, x)=2;
    end
    switch edge(3)
            case 'o'
                pag(x, y)=1;
            case '-'
                pag(x, y)=3;
            case '>'
                pag(x, y)=2;
    end
end
printedgesmcg(pag);
end