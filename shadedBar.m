function [ h ] = shadedBar( x, m, stds, color,line)
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here
x=reshape(x, 1, length(x));
m = reshape(m, 1, length(m));
stds = reshape(stds, 1, length(stds));


x1 = [x fliplr(x)];
%x(1)= x(1)+0.005; x(length(x))= x(length(x))-0.005;


y= [m-stds fliplr(m+stds)];
h.fill = fill(x1,y,color, 'linestyle', 'none');
% Choose a number between 0 (invisible) and 1 (opaque) for facealpha.  
if line
    set(h.fill,'facealpha',.1)
    hold on;
    h.plot= plot(x, m, 'color', color, 'linewidth', 2);
end
end

