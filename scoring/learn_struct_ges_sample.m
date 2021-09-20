function [cpdag, best_score, sample_pdags, sample_scores] = learn_struct_ges_sample(data, nodesizes, seeddag, varargin)
%
% LEARN_STRUCT_GES learns a structure of Bayesian net by Greedy Equivalence Search.
% cpdag = learn_struct_ges(Data, Nodesizes, 'cache', cache, 'scoring_fn', 'bic', 'verbose', 'yes')
%
% cpdag: the final cpdag
% Data : training data, data(i,m) is the m obsevation of node i
% Nodesizes: the size array of different nodes
% cache : data structure used to memorize local score computations
%   (cf. SCORE_INIT_CACHE function)
%
% V1.1 : 28 july 2003 (Ph. Leray - philippe.leray@univ-nantes.fr, O. francois - francois.olivier.c.h@gmail.com)
%
% Ref:
%   Optimal Structure Identification with Greedy Search, Chickering 2002
%

[N ncases] = size(data);

% set default params
scoring_fn = 'bayesian';
verbose  = 0;
cache=[];

% get params
args = varargin;
nargs = length(args);
if length(args) > 0
    if isstr(args{1})
        for i = 1:2:nargs
            switch args{i}
            case 'scoring_fn', scoring_fn = args{i+1};
            case 'verbose',  verbose  = strcmp(args{i+1},'yes');
            case 'cache',  cache=args{i+1} ;
            end;
        end;
    end;
end;

if verbose
    names=cellstr(int2str((1:N)'));
    carre=zeros(N,1);
end

done = 0;



[best_score cache] = score_dags(data,nodesizes, {seeddag},'scoring_fn',scoring_fn,'cache',cache);
cptt=0;
nins =0;
sample_pdags =cell(50,1);
sample_pdags{1}= seeddag;
sample_scores = nan(50, 1);
sample_scores(1)= best_score;
% First step : INSERT
while ~done  
    cptt=cptt+1;
    [pdags,nodes] = mk_nbrs_of_pdag_add(seeddag);
    seedold=seeddag;
    sold=best_score;
    nbrs = length(pdags);
    dags=pdag_to_dag(pdags);
    [scores ] = score_dags(data, nodesizes, dags,'scoring_fn',scoring_fn,'cache',cache);
    max_score = max(scores);
    new = find(scores == max_score );
    if ~isempty(new) && (max_score > best_score)
        p = sample_discrete(normalise(ones(1, length(new))));
        best_score = max_score;
        seeddag = dag_to_cpdag(dags{new(p)});
        sample_pdags{cptt+1} =seeddag;
        sample_dags{cptt+1} = dags;

        sample_scores(cptt+1) = best_score;
        new=new(p);
        if verbose
            figure;
            subplot(1,2,1), [xx yy]=draw_graph(seedold,names,carre);  
            set(gca,'color',[1 1 0]); 
            title(sprintf('current CPDAG (Smax=%5.2f)',sold));
            subplot(1,2,2), draw_graph(seeddag,names,carre,xx,yy);     
            s=sprintf(' %d',nodes{new,3});
            title([sprintf('Best in N+ = INSERT(%d, %d,',nodes{new,1},nodes{new,2}) s ')' sprintf('  S=%5.2f',max_score)]);
            drawnow;
        end

    else
        done = 1;
    end

end

done = 0;
%[best_score cache] = score_dags(data,nodesizes, {seeddag},'scoring_fn',scoring_fn,'cache',cache);
nins=cptt-1;
cptt=0;

if sum(sum(seeddag))==0, done=1;end

% Second step : DELETE
while ~done
    cptt=cptt+1;
    [pdags,nodes] = mk_nbrs_of_pdag_del(seeddag);
    seedold=seeddag; sold=best_score;
    nbrs = length(pdags);
    dags=pdag_to_dag(pdags);
    [scores ] = score_dags(data, nodesizes, dags,'scoring_fn',scoring_fn,'cache',cache);
    max_score = max(scores);
    new = find(scores == max_score );
    if ~isempty(new) & (max_score > best_score)
        p = sample_discrete(normalise(ones(1, length(new))));
        best_score = max_score;
        seeddag = dag_to_cpdag(dags{new(p)});
        sample_pdags{cptt+nins} =seeddag;
        sample_scores(cptt+nins) = best_score;
        new=new(p);
        if verbose
            cpdags=dag_to_cpdag(dags);
            figure; 
            subplot(1,2,1), [xx yy]=draw_graph(seedold,names,carre);  
            set(gca,'color',[1 1 0]); 
            title(sprintf('current CPDAG (Smax=%5.2f)',best_score));
            subplot(1,2,2), draw_graph(seeddag,names,carre,xx,yy);     
            s=sprintf('%d',nodes{new,3});
            title([sprintf('Best in N- = DELETE(%d, %d,',nodes{new,1},nodes{new,2}) s ')' sprintf('  S=%5.2f',max_score)]);
            drawnow;
        end

    else
        done = 1;
    end

end
ndel = cptt-1;
cpdag = seeddag;
if nins+ndel>0
    sample_pdags = sample_pdags(1:nins+ndel);
    sample_scores = sample_scores(1:nins+ndel);
else
    sample_pdags = sample_pdags(1);
    sample_scores = sample_scores(1);
end
