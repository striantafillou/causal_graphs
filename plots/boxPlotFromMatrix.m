%prepareForBoxlplot
function fh = boxPlotFromMatrix(data, t, x, y, domainCounts, labels)
% plots grouped boxplot, treatment and control (t) group outcomes (y) for different
% levels of variable x. 
c = cell(1, 2);
nX = domainCounts(x);
outcome = data(:, y);
xData = data(:,x);
treat = data(:, t);
nTreat = sum(data(:, t)==1); nCtrl = sum(data(:, t)==0);
% create generic matrix;
if nTreat>=nCtrl
    c_i = nan(nTreat, nX);
    for iX =1:nX    
        c_ = c_i;
        c_(1:sum(treat==0 & xData==iX-1), 1) = outcome(treat==0 & xData==iX-1);
        c_(1:sum(treat==1 & xData==iX-1), 2) = outcome(treat==1 & xData==iX-1);
        c{iX}=c_;
    end
else
    c_i = nan(nCtrl, nX);
    for iX =1:nX    
        c_ = c_i;
        c_(1:sum(treat==0 & xData==iX-1), 1) = outcome(treat==0 & xData==iX-1);
        c_(1:sum(treat==1 & xData==iX-1), 2) = outcome(treat==1 & xData==iX-1);
        c{iX}=c_;

    end
end
fh = boxplotGroup(c, 'PrimaryLabels', {'Ctrl', 'Treat'}, 'SecondaryLabels', {'No', 'Yes'});
hold on;
xlabel(labels{x}, 'FontSize', 14);
ylabel(labels{y}, 'FontSize', 14);