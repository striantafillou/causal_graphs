function [dif] = graph_difference(file1,file2);
graph1=fci_outfile2pag(file1);
graph2=fci_outfile2pag(file2);

dif= graph1-graph2;
