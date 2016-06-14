function manipulatedSMM = manipulatesmm(smm, isManipulated)
% function manipulatedSMM = manipulatesmm(smm, isManipulated)
% Returns the manipulated Semi Markov model manipulatedSMM resulting from
% smm after manipulating (removing incoming variables) from nodes for which
% isManipulated(node) = true.
% author: striant@csd.uoc.gr.

manipulatedVars = find(isManipulated);
manipulatedSMM = smm;
% if screen
%     fprintf('ManipulatedVars: %s\n', num2str(manipulatedVars))
% end

[x, y] =  find(manipulatedSMM(:, manipulatedVars)==2);
y =  manipulatedVars(y);
y = reshape(y, length(y), 1);
if ~isempty(x)
    manipulatedSMM(sub2ind(size(smm), x, y))= 0;
    manipulatedSMM(sub2ind(size(smm), y, x)) = 0;
%     if screen
%         fprintf('Removing edges into manipulatedVars: %s\n', num2str(x'));
%     end
end

end