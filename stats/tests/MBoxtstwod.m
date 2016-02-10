function [p_value, stat] =MBoxtstwod(X,n,alpha)
% Multivariate Statistical Testing for the Homogeneity of Covariance Matrices Without Data by
% the Box's M. 
%
%   Syntax: function MBoxtstwod(X,n,alpha) 
%      
%   Inputs:
%        X - covariances matrix (covariances can be input in a rows or columns
%            arrangement.)
%        n - vector of groups-size.
%    alpha - significance level (default = 0.05). 
%   Outputs:
%          - MBox - the Box's M statistic.
%          - Chi-sqr. or F - the approximation statistic test.
%          - g - number of groups.
%          - p - number of variables.
%          - df's - degrees' of freedom of the approximation statistic test.
%          - P-value.
%  
%   If the groups sample-size is at least 20 (sufficiently large), Box's M test
%   takes a Chi-square approximation; otherwise it takes an F approximation.
%
%   Example: For a two groups (g = 2) with three independent variables (p = 3), we 
%            are interested to test the homogeneity of covariances matrices without
%            data with a significance level = 0.05. The two groups have the same 
%            sample-size n1 = n2 = 5. The variance-covariance matrix (S) for each
%            group are:
%
%                             S1                        S2
%                    --------------------      --------------------
%                     5909   7573   1474        5474   8228   2619
%                     7573  10401   1717        8228  19205   6204
%                     1474   1717    412        2619   6204   3486
%                    --------------------      --------------------
%
%  Data matrix must be:
%  X = [5909 7573 1474 5474 8228 2619;7573 10401 1717 8228 19205 6204;
%  1474 1717 412 2619 6204 3486];
%
%  n = [5 5];
%
%  Calling on Matlab the function: 
%  mboxtstwod(X,n)
%
%  Answer is:
%
%  Box's M test for homogeneity of covariance matrices without data.
%  --------------------------------------------------------------------------------
%       MBox         F         g         p           df1          df2          P
%  --------------------------------------------------------------------------------
%     27.4339     2.6556       2         3            6           463       0.0153
%  --------------------------------------------------------------------------------
%  With a given significance level of: 0.05
%  Covariance matrices are significantly different.
%
%  Created by A. Trujillo-Ortiz, R. Hernandez-Walls and A. Castro-Perez
%             Facultad de Ciencias Marinas
%             Universidad Autonoma de Baja California
%             Apdo. Postal 453
%             Ensenada, Baja California
%             Mexico.
%             atrujo@uabc.mx
%             And the special collaboration of the post-graduate students of the 2004:2
%             Multivariate Statistics Course: Laura Rodriguez-Cardoso, Rene Garcia-Sanchez,
%             Norma A. Ramos-Delgado.
%  Copyright. December 8, 2004.
%
%  To cite this file, this would be an appropriate format:
%  Trujillo-Ortiz, A. R. Hernandez-Walls and A. Castro-Perez (2204). MBoxtstwod: 
%    Multivariate Statistical Testing for the Homogeneity of Covariance Matrices
%    Without Data by the Box's M. A MATLAB file. [WWW document]. URL http://
%    www.mathworks.com/matlabcentral/fileexchange/loadFile.do?objectId=6548
%
%  References:
% 
%  Stevens, J. (1992), Applied Multivariate Statistics for Social Sciences. 2nd. ed.
%              New-Jersey:Lawrance Erlbaum Associates Publishers. pp. 260-269.
%
  
if nargin < 2, 
    error('Requires at least two input arguments.'); 
end;

if nargin < 3, 
    alpha = 0.05; %(default)
end; 

if (alpha <= 0 | alpha >= 1)
   fprintf('Warning: significance level must be between 0 and 1\n');
   return;
end;

[r c] = size(X);

if c > r;
    X = X';
else
    X = X;
end;

p = size(X,2); %Number of variables.
ks = size(X,1);
g = ks/p; %Number of groups.

if g ~= fix(g);
    error('Some of the covariance matrix has different size.');
end;

r = 1; 
r1 = n(1);
bandera = 2;
for k = 1:g
   if n(k) >= 20;
      bandera = 1;
   end;
end;

ma = 'S';
rr = 1;
for k = 1:g
    b = X(rr:rr+p-1,:);
    eval([ma num2str(k) '=b;']);
    rr = rr+p;
end;

deno = sum(n)-g;
suma = zeros(size(S1));
% 
% tl = [];tu = [];
% for k = 1:g
%     eval(['l =tril(S' num2str(k) ');']);
%     eval(['u =triu(S' num2str(k) ');']);
%     tl = [tl;l'];tu = [tu;u];
%     d = tl(:)-tu(:);
%     if any(d ~= 0);
%         disp('Warning:Some of the covariance matrix it is not symmetric.');
%         return;
%     end;
% end;

for k = 1:g
   eval(['suma =suma + (n(k)-1)*S' num2str(k) ';']);
end;

Sp = suma/deno;  %Pooled covariance matrix.
Falta = 0;

for k = 1:g
   eval(['Falta =Falta + ((n(k)-1)*log(det(S' num2str(k) ')));']);
end;

MB = (sum(n)-g)*log(det(Sp))-Falta;  %Box's M statistic.
suma1 = sum(1./(n(1:g)-1));
suma2 = sum(1./((n(1:g)-1).^2));
C = (((2*p^2)+(3*p)-1)/(6*(p+1)*(g-1)))*(suma1-(1/deno));  %Computing of correction factor.

if bandera == 1
    X2 = MB*(1-C);  %Chi-square approximation.
    v = (p*(p+1)*(g-1))/2;  %Degrees of freedom.
    P = 1-chi2cdf(X2,v);  %Significance value associated to the observed Chi-square statistic.
    stat = X2;
    p_value =P;
    disp(' ')
    ;
    disp('Box''s M test for homogeneity of covariance matrices without data.')
    fprintf('--------------------------------------------------------------\n');
    disp('     MBox     Chi-sqr.      g       p         df          P')
    fprintf('--------------------------------------------------------------\n');
    fprintf('%10.4f%11.4f%8.i%8.i%11.i%13.4f\n',MB,X2,g,p,v,P);
    fprintf('--------------------------------------------------------------\n');
    fprintf('With a given significance level of:% 3.2f\n', alpha );
    if P >= alpha
        disp('Covariance matrices are not significantly different.');
    else
        disp('Covariance matrices are significantly different.');
    end;
else
    %To obtain the F approximation we first define Co, which combined to the before C value
    %are used to estimate the denominator degrees of freedom (v2); resulting two possible cases. 
    Co = (((p-1)*(p+2))/(6*(g-1)))*(suma2-(1/(deno^2)));
    if Co-(C^2) >= 0;
        v1 = (p*(p+1)*(g-1))/2;  %Numerator degrees of freedom.
        v21 = fix((v1+2)/(Co-(C^2)));  %Denominator degrees of freedom.
        F1 = MB*((1-C-(v1/v21))/v1);  %F approximation.
        P1 = 1-fcdf(F1,v1,v21);  %Significance value associated to the observed F statistic.
        p_value =P1;
        stat =  F1;

        disp(' ')
        ;
        disp('Box''s M test for homogeneity of covariance matrices without data.')
        fprintf('--------------------------------------------------------------------------------\n');
        disp('     MBox         F         g         p           df1          df2          P')
        fprintf('--------------------------------------------------------------------------------\n');
        fprintf('%10.4f%11.4f%8.i%10.i%13.i%14.i%13.4f\n',MB,F1,g,p,v1,v21,P1);
        fprintf('--------------------------------------------------------------------------------\n');   
        fprintf('With a given significance level of:% 3.2f\n', alpha );
        if P1 >= alpha
            disp('Covariance matrices are not significantly different.');
        else
            disp('Covariance matrices are significantly different.');
        end;       
    else 
        v1 = (p*(p+1)*(g-1))/2;  %Numerator degrees of freedom.
        v22 = fix((v1+2)/((C^2)-Co));  %Denominator degrees of freedom.
        b = v22/(1-C-(2/v22));
        F2 = (v22*MB)/(v1*(b-MB));  %F approximation.
        P2 = 1-fcdf(F2,v1,v22);  %Significance value associated to the observed F statistic.
        p_value =P2;
        stat =  F2;
        disp(' ') 
        ;
        disp('Box''s M test for homogeneity of covariance matrices without data.')
        fprintf('--------------------------------------------------------------------------------\n');
        disp('     MBox         F         g         p           df1          df2          P')
        fprintf('--------------------------------------------------------------------------------\n');
        fprintf('%10.4f%11.4f%8.i%10.i%13.i%14.i%13.4f\n',MB,F2,g,p,v1,v22,P2);
        fprintf('--------------------------------------------------------------------------------\n');   
        fprintf('With a given significance level of:% 3.2f\n', alpha );
        if P1 >= alpha
            disp('Covariance matrices are not significantly different.');
        else
            disp('Covariance matrices are significantly different.');
        end;       
    end;
end;
