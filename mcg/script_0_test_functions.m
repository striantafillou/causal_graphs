%script_example

% create a dag (a small inducing path)
dag = zeros(5);
dag(1, 2)=1;dag(2, 3) =1; dag(3, 4)=1;
dag(5, 2) =1; dag(5, 4)=1;

printedgesdag(dag);

% make confounder latent
fprintf('Variable 5 is hidden\n');
isLatent = [false false false false true];
%
fprintf('SMCM:\n')
smm = dag2smm(dag, isLatent);
printedgesmcg(smm);
writecytomcg(smm, 'smm.txt');

fprintf('MAG:\n')
mag = dag2mag(dag, isLatent);
printedgesmcg(mag);
writecytomcg(mag, 'mag.txt');

fprintf('PAG:\n')
pag = mag2pag(mag);
printedgesmcg(pag);
writecytomcg(pag, 'pag.txt');
% smm has almost cycle
hasalmostcycle(smm)

% mag does not have almost cycle
hasalmostcycle(mag)

% nodes 1,3 are m separated given 2 in mag,smcm
isAncMag = findancestors(mag);
msepnodes = findmseparations(mag, 1,2, isAncMag, isLatent);
fprintf('Nodes %s are m-separated from node 1 given node 2 in mag\n',num2str(msepnodes));

isAncSmm = findancestors(smm);
msepnodes =findmseparations(smm, 1,2, isAncSmm, isLatent);
fprintf('Nodes %s are m-separated from node 1 given node 2 in smm\n', num2str(msepnodes));




