function[newScore, curLocalScores] =removeDirectedEdge(from, to, dag, obsData, domainCounts, curLocalScores)
    dag(from,to) = 0;
    pa_to = find(dag(:, to))';
    curLocalScores(to) = bde(obsData, domainCounts, to, pa_to, 1);
    newScore = sum(curLocalScores);
end
