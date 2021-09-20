 function [fig1,ax1,dcm_obj] = fig_open()
 set(0,'defaultfigurecolor',[1 1 1]) % white background
 set(0,'defaultaxesfontname','cambria math') % beautify the axes a bit
 scrn_size = get(0,'ScreenSize'); % get size of screen
 shrink_pct = 0.1; % shrink the figure by 10%
 %
 fig1 = figure('DefaultAxesFontSize',20,'Position',...%'Visible','off',
     [scrn_size(1)+(scrn_size(3)*shrink_pct) scrn_size(2)+(scrn_size(4)*shrink_pct)...
     scrn_size(3)-(scrn_size(3)*2*shrink_pct) scrn_size(4)-(scrn_size(4)*2*shrink_pct)]); % shrinking the figure
 %
 ax1 = gca;
 dcm_obj = datacursormode(fig1); % enable control of datatips
 % set(dcm_obj,'UpdateFcn',@myupdatefcn) % this will be used to configure
 % data tips
 set(ax1,'fontsize',22,'Color',[0.8 0.8 0.8],'gridcolor',[1 1 1],'gridalpha',0.9) % set the axis color
 % to compliment the white background
 %
 hold all
 grid on % grid for more contrast in the axes
 end