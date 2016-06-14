function ds = tetrad_dataset(dataset)
import edu.cmu.tetrad.*
import java.util.*
%import java.lang.*
import edu.cmu.tetrad.data.*

[nSamples,nVars] = size(dataset.data);
list = LinkedList();
for i=1:nVars
    var = javaObject('edu.cmu.tetrad.data.ContinuousVariable',['X' num2str(i)]);
    %ContinuousVariable(['X' num2str(i)]);
    list.add(var);
end
ds = ColtDataSet(nSamples,list);
for i=0:(nSamples-1)
    for j=0:(nVars-1)
        ds.setDouble(i,j,dataset.data((i+1),(j+1)));
    end
end

end