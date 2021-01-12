

dag = zeros(4);
conf=1; x=2; t =3; o =4;

dag(x, [t, o])=1;
dag(1, [x, t, o]) =1;
dag(t, o)=1;
printedgesdag(dag);

nodes = dag2randBN(dag,'linear');
numCases =1000;

confs= normrnd(2,5, numCases, 1);
x = -2 + (3 * confs) + normrnd(0,1,numCases,1);
thres=20;
t =x>thres;
o =  (5 + 2*confs+1.8*x+ normrnd(0,1,numCases,1)).*t+(-3 + -2*confs+6*x+ normrnd(0,1,numCases,1)).*~t;