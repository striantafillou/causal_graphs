%% simulateData from stim-> from -> to
clear all
colors = {'r', 'g', 'b', 'c', 'm', 'y'};
graph = zeros(2); graph(2, 1)=1;

%2. create bn
[nodes, dc] = dag2randBN(graph, 'discrete', 'maxNumStates', 2, 'headers', {'YF', 'Smoking'});

%3. set cpt parameters;
nodes{2}.cpt = [0.7; 0.3];

nodes{1}.cpt = [0.9 0.1; 0.1 0.9]';

% simulate data
sample_size = 1000;

dataset= simulatedata(nodes, sample_size, dc, 'discrete');
yf = dataset.data(:, 1); sm = dataset.data(:, 2);
nStim =sum(sm==1); nRef = sum(sm==0);

toRefData= 1.9 + 0.7*randn(nRef, 1);
toStimData = 0.8 * sm(sm==1)+ rand(nStim, 1)+2;

toData= zeros(nRef+nStim, 1);
toData(sm==0) = toRefData;
toData(sm==1)  = toStimData;

a = 0.1; b=-0.1;
r = a+(b-a)*rand(sample_size, 1);
%%        
close all;
figure('units','normalized','outerposition',[0 0 0.6 0.6])
ph1 = subplot(1, 2,1);hold on; ph2 =  subplot(1,2, 2);hold on;

   

scatter(ph2, yf(sm==0)+r(sm==0), toData(sm ==0), 'b.');
scatter(ph2, yf(sm==1)+r(sm==1), toData(sm==1), 'r.');
hL =legend(ph2, {'Non Smokers','Smokers'}, 'Location', 'SouthEast');
plot(ph2,[0.8 1.2], [mean(toData(sm ==0 & yf==1)) mean(toData(sm ==0 & yf==1))], 'b', 'LineWidth', 2);
plot(ph2, [-0.2 0.2], [mean(toData(sm ==0 & yf==0)) mean(toData(sm ==0 & yf==0))], 'b', 'LineWidth', 2);
plot(ph2, [0.8 1.2], [mean(toData(sm ==1 & yf==1)) mean(toData(sm ==1 & yf==1))], 'r', 'LineWidth', 2);
plot(ph2, [-0.2 0.2], [mean(toData(sm ==1 & yf==0)) mean(toData(sm ==1 & yf==0))], 'r', 'LineWidth', 2);

hXL(2) = xlabel(ph2, 'Yellow Teeth');
hYL(2) = ylabel(ph2, 'Nicotine Levels');

scatter(ph1, yf+r, toData, 'k.');
plot(ph1,[0.8 1.2], [mean(toData(yf==1)) mean(toData(yf==1))], 'k', 'LineWidth', 2);
plot(ph1, [-0.2 0.2], [mean(toData(yf==0)) mean(toData( yf==0))], 'k', 'LineWidth', 2);
hXL(1) = xlabel(ph1, 'Yellow Teeth');
hYL(1) = ylabel(ph1, 'Nicotine Levels');


set([hYL, hXL, hL], ...
    'FontName'   , 'AvantGarde', ...
    'FontSize'   , 12 );

    set([ph1, ph2], ...
    'Box'         , 'on'     , ...
    'XColor'      , [.3 .3 .3], ...
    'YColor'      , [.3 .3 .3], ...  
    'XLim'        , [-0.5 1.5], ...
    'XTick'       , [0 1],.... 
    'XTickLabel'  , {'No', 'Yes'});
set(gcf, 'Color', 'white')


export_fig('conditional_independence_figure.png');

