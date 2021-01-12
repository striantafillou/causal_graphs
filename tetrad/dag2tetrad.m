function tGraph=dag2tetrad(dag,list, nVars)
import edu.cmu.tetrad.*
import java.util.*
%import edu.cmu.tetrad.search.indtest.IndTestDSep;
import edu.cmu.tetrad.graph.*
import edu.cmu.tetrad.search.*
 tGraph = EdgeListGraph(list);   


for x=1:nVars-1
    for y =x+1:nVars
        if dag(x, y)==1
            %fprintf('Adding %d->%d\n', x, y)
            tGraph.addDirectedEdge(list.get(x-1), list.get(y-1));
        elseif dag(y, x)==1
            %fprintf('Adding %d->%d\n', y, x)
            tGraph.addDirectedEdge(list.get(y-1), list.get(x-1));
        end
    end
end

end