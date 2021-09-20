function [score] = score_dags_mod(data, ns, dags, varargin)
% SCORE_DAGS Compute the bdeu score of one or more DAGs
% score = score_dags(data, ns, dags, varargin)
NG = length(dags);
score = zeros(1, NG);

for g=1:NG   
    score(g) = bdeu_score(data, dags{g}, ns, 1);
end
