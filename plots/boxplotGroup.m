function h = boxplotGroup(varargin)
% BOXPLOTGROUP groups boxplots together with horizontal space between groups.
%   boxplotGroup(x) receives a 1xm cell array where each element is a matrix with
%   n columns and produced n groups of boxplot boxes with m boxes per group.
%
%   boxplotGroup(x,'interGroupSpace',d) separates groups by d units along the x axis
%   where d is a positive, scalar integer (default = 1)
%
%   boxplotGroup(x,'primaryLabels', c) specifies the x tick label for each boxplot.
%   c is a string array or cell array of characters and must have one element per
%   box or one element per group-member.
%
%   boxplotGroup(x,'secondaryLabels', s) specifies the group labels for the boxplot
%   groups.  s is a string array or cell array of characters and must have one element
%   per group (see 'groupLabelType')
%
%   boxplotGroup(x,'groupLines', true) adds vertical divider lines between groups and
%   labels the lines by group name (requires >=r2018b).
%
%   boxplotGroup(x,'groupLabelType', str) specifies how to label the groups by one of
%   the following options.
%    * 'horizontal': Group labels will be centered under the primary labels using a 2nd 
%       invisible axis underlying the main axis.
%    * 'vertical': Group labels will be vertical, between groups (requires Matlab >=2018b)
%    * 'both': Both methods will be used.
%
%   boxplotGroup(ax,__) specifies the axis handle, otherwise current axis is used. 
%
%   boxplotGroup(..., 'PARAM1', val1, 'PARAM2, val2, ...) sends optional name/value pairs
%   to the boxplot() function. Some properties may not be supported due to how the grouped
%   boxplots are produced within a loop.  Contact me for enhancement requests. 
%
%   h = boxplotGroup(__) outputs a structure of graphic handles.
%
% NOTE: If you're working with a grouping variable 'g', use the syntax boxplot(x,g) along
%   with the "Group Appearance" options described in Matlab's boxplot() documentation. 
%   https://www.mathworks.com/help/stats/boxplot.html#d118e146984
%
% EXAMPLES:
% data = {rand(100,4), rand(20,4)*.8, rand(1000,4)*1.2};
%
% Required inputs
%   boxplotGroup(data)
%
% Set space between groups
%   boxplotGroup(data, 'interGroupSpace', 3)
%
% Specify labels and draw divider line
%   boxplotGroup(data, 'groupLines', true, 'PrimaryLabels', {'a' 'b' 'c'},...
%       'SecondaryLabels', {'Lancaster', 'Cincinnati', 'Sofia', 'Rochester'})
%
% Label groups with vertical lables
%   boxplotGroup(data, 'PrimaryLabels', {'a' 'b' 'c'}, 'SecondaryLabels', ...
%       {'Lancaster', 'Cincinnati', 'Sofia', 'Rochester'}, 'groupLabelType', 'vertical')
%
% Pass additional boxplot properties
%   boxplotGroup(data, 'PrimaryLabels', {'a' 'b' 'c'}, 'SecondaryLabels', ...
%       {'Lancaster', 'Cincinnati', 'Sofia', 'Rochester'}, 'groupLabelType', 'vertical', ...
%       'BoxStyle', 'filled', 'PlotStyle', 'Compact')
%
%
% Contact adam.danz@gmail.com for questions, bugs, suggestions, and high-fives.
% Copyright (c) 2020, Adam Danz  adam.danz@gmail.com
% All rights reserved
% Source: https://www.mathworks.com/matlabcentral/fileexchange/74437-boxplotgroup

% Changes history
% 200306 - v1.0.0 first release.
% 200308 - v1.1.0 Added recommendation to use boxplot() with grouping variable.
%                 Added axis handle as input to boxplot() call. Linkaxes changed
%                 from x to xy. Set axis2.Units to axis.Units.  Using linkprop
%                 to link position etc of main axis and axis2. Added DeleteFcn 
%                 to main axis. Disabled toolbar for axis2. Added listener to 
%                 resize axis2 when main axis is resized. Changes to help section.
% 200309 - v1.2.0 When 2nd axis is added, main axis is set to current axis. 
% 200309 - v1.2.1 Suppress linkprops() and changes to toolbar suppression to work 
%                 with versions prior to r2018b.
% 200309 - v1.2.2 Instead of creating new axis, default axis is gca(). 

%% Check for axis handle in first input
if ~isempty(varargin) && ~isempty(varargin{1}) && isgraphics(varargin{1}(1), 'axes')
    % first input is an axis
    h.axis = varargin{1} ;
    varargin(1) = [];
else
    h.axis = [];
end

%% Parse inputs
p = inputParser();
p.FunctionName = mfilename;
p.KeepUnmatched = true;	%accept additional parameter value inputs (passed to boxplot())
addRequired(p, 'x', @(x)validateattributes(x,{'cell'},{'row','nonempty'}))
addParameter(p, 'interGroupSpace', 1, @(x)validateattributes(x,{'double'},{'scalar','integer'}))
addParameter(p, 'primarylabels', [], @(x)validateattributes(x,{'string','cell'},{'nonempty'}))
addParameter(p, 'secondarylabels', [], @(x)validateattributes(x,{'string','cell'},{'nonempty'}))
addParameter(p, 'groupLines', false, @(x)validateattributes(x,{'logical','double'},{'binary'}))
addParameter(p, 'groupLabelType', 'Horizontal', @(x)ischar(validatestring(lower(x),{'vertical','horizontal','both'})))
parse(p,varargin{:})

% Prepare the unmatched boxplot() parameters.
% If a param is passed that isn't accepted by boxplot(), an error is thrown from boxplot() function.
unmatchNameVal = reshape([fieldnames(p.Unmatched)'; struct2cell(p.Unmatched)'], 1, []);

% Check that each element of x is a matrix
assert(all(cellfun(@ismatrix, p.Results.x)), 'All elements of the cell array ''x'' must be a matrix.')
% Check that each matrix contains the same number of columns.
assert(numel(unique(cellfun(@(m)size(m,2),p.Results.x))) == 1, ...
    ['All elements of the cell array ''x'' must contain the same number of columns. '...
    'Pad the matricies that contain fewer columns with NaN values.']);

%% Compute horizontal spacing
nGroups = size(p.Results.x{1},2);       % number of columns of data / number of groups
nMembers = numel(p.Results.x);          % number of members per group
maxX = ((nMembers + p.Results.interGroupSpace) * nGroups) - p.Results.interGroupSpace;

% Check that labels (if any) are the right size
% PrimaryLabels: either 1 per group-member or 1 for each bar
if ~isempty(p.Results.primarylabels)
    assert(ismember(numel(p.Results.primarylabels),[nMembers, nMembers*nGroups]), ...
        sprintf(['The number of primary labels must equal either the number of bars per group (%d) '...
        'or the number of total bars (%d).'], nMembers, nMembers*nGroups))
end
% SecondaryLabels: 1 per group
if ~isempty(p.Results.secondarylabels)
    assert(isequal(numel(p.Results.secondarylabels),nGroups), ...
        sprintf('The number of secondary labels must equal either the number groups (%d).',nGroups))
end

%% Do plotting
if isempty(h.axis)
    h.axis = gca(); 
    h.figure = h.axis.Parent;
end

originalHoldStatus = ishold(h.axis);
hold(h.axis, 'on')

x = cell(1,nMembers);
for i = 1:nMembers
    x{i} = i : nMembers + p.Results.interGroupSpace : maxX;
    temp = nan(size(p.Results.x{i},1), max(x{i}));
    temp(:,x{i}) = p.Results.x{i};
    boxplot(h.axis, temp, unmatchNameVal{:})
end

% cosmetics
if ~originalHoldStatus
    hold(h.axis, 'off')
end
axis(h.axis, 'tight')
xticks = 1:maxX;
limGap = (p.Results.interGroupSpace+1)/2;
set(h.axis,'XTick',xticks,'xlim',[1-limGap, maxX+limGap])
yl = ylim(h.axis);
ylim(h.axis, yl + [-range(yl)*.05, range(yl)*.05])

% Set primary labels if provided
if ~isempty(p.Results.primarylabels)
    h.axis.XTick = sort([x{:}]);
    h.axis.XTickLabel = p.Results.primarylabels;
end
% Set secondary labels if provided
vertLinesDrawn = false;
if ~isempty(p.Results.secondarylabels)
    if any(strcmpi(p.Results.groupLabelType, {'horizontal','both'}))
        % Compute x position of secondary labels
        secondaryX = (nMembers : nMembers + p.Results.interGroupSpace : maxX) - (nMembers-1)/2;
        h.axis2 = axes('Units',h.axis.Units,'position', h.axis.Position, 'ActivePositionProperty', h.axis.ActivePositionProperty, ...
            'xlim', h.axis.XLim, 'TickLength', [0 0], 'ytick', [], 'Color', 'none', 'XTick', secondaryX, 'XTickLabel', ...
            strcat('\newline',p.Results.secondarylabels));
        linkaxes([h.axis, h.axis2], 'xy')
        linkprop([h.axis, h.axis2],{'Units','Position','ActivePositionProperty','Parent'});
        uistack(h.axis, 'top')
        if isprop(h.axis2, 'Toolbar')
            h.axis2.Toolbar.Visible = 'off'; % ver >= r2018b
        end
        h.axis.DeleteFcn = @(~,~)delete(h.axis2); % Delete axis2 if main axis is deleted
        addlistener(h.axis, 'Position', 'PostSet', @(src,evnt)set(h.axis2,'Position',get(h.axis, 'Position'))); %when axis 1 is resized
        set(h.figure,'CurrentAxes',h.axis)
    end
    if any(strcmpi(p.Results.groupLabelType, {'vertical','both'}))
        spaces = setdiff(1-p.Results.interGroupSpace : maxX, [x{:}]);
        endSpaceIdx = [diff(spaces),2] > 1;
        midSpace = spaces(endSpaceIdx) - (p.Results.interGroupSpace-1)/2;
        h.xline = arrayfun(@(x)xline(h.axis, x),midSpace);
        set(h.xline', {'Label'}, p.Results.secondarylabels')
        vertLinesDrawn = true;
    end
end

% Draw vertical lines if requested and if they don't already exist.
if p.Results.groupLines && ~vertLinesDrawn
    spaces = setdiff(1:maxX+p.Results.interGroupSpace, [x{:}]);
    endSpaceIdx = [diff(spaces),2] > 1;
    midSpace = spaces(endSpaceIdx) - (p.Results.interGroupSpace-1)/2;
    h.xline = arrayfun(@(x)xline(h.axis, x,'-k'),midSpace);
end


