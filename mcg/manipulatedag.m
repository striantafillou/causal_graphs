function manipulatedDag = manipulateDag(dag, isManipulated)
% function manipulatedDag = manipulateDag(dag, isManipulated)
% Returns the manipulated DAG manipulatedDAG resulting from
% dag after manipulating (removing incoming variables) from nodes for which
% isManipulated(node) = true.
% author: striant@csd.uoc.gr.
manipulatedDag = dag;
manipulatedDag(:, isManipulated) = 0;
end