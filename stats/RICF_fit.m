function [beta, omega, hatCovMat, ricf] = RICF_fit(smm, covMat, tol)

if any(eig(covMat)<= 0);
    errprintf('Covariance matrix is not positive definite\n')
end
if any(smm==4)
    errprintg('Graph includes bows\n');
end


nVars = size(covMat,1);
dg = smm ==2 & smm'==3;
bg = smm==2 &  smm'==2;
% 
% dg = (smm ==2 & smm'==3) | (smm==2 & smm'==4);
% bg = (smm ==2 & smm'==4) | (smm==2 & smm'==2) | (smm==4 & smm'==2);

% starting values
omega =diag(diag(covMat),0);
beta = eye(nVars);

% list of parents, spouses etc
[par, sp] = deal(false(nVars));
for iVar=1:nVars
    par(iVar, dg(:, iVar)) = true;
    sp(iVar, bg(:, iVar)) = true;
end
iter=0;

while true
    iter = iter+1;
    ricf(iter).omega = omega;
    ricf(iter).beta = beta;
% for each variable
    for iVar =1:nVars

       vcomp = [1:iVar-1, iVar+1:nVars];
       iPar= find(par(iVar,:));
       iSp = find(sp(iVar, :));

       if isempty(iSp)
           if ~isempty(iPar)
               if iter==1;
                   beta(iVar, iPar) = -covMat(iVar, iPar)*inv(covMat(iPar, iPar));
                   omega(iVar, iVar) = covMat(iVar, iVar) + beta(iVar, iPar)*covMat(iPar, iVar);
               end
           end
       elseif ~isempty(iPar) % spouses and parents
           oInv = zeros(nVars, nVars);
           oInv(vcomp, vcomp) = inv(omega(vcomp, vcomp)); %\Omega_{-i, -i}^-1
           Z = oInv(iSp, vcomp)*beta(vcomp, :);
           nPar = length(iPar);
           nSp = length(iSp);
           range1 = 1:nPar; 
           range2= nPar+1:nPar+nSp;
           % XX
           XX = nan(nPar+nSp);
           % Upper left quadrant
           XX(range1, range1) = covMat(iPar, iPar);
           % Upper right quadrant
           XX(range1, range2) = covMat(iPar, :)*Z';
           % Lower left quadrant
           XX(range2, range1) =  XX(1:nPar, nPar+1:nPar+nSp)';
           % Lower right quadrant
           XX(range2, range2) = Z*covMat*Z';
           % YX <- c(S[v,parv], S[v,]%*%t(Z))
           % temp <- YX %*% solve(XX)
           YX = [covMat(iVar, iPar) covMat(iVar, :)*Z']';
           temp =YX'/XX;
           % update beta, omega
           beta(iVar, iPar)=-temp(range1);
           omega(iVar, iSp) = temp(range2);
           omega(iSp, iVar) = omega(iVar, iSp);

           tempVar = covMat(iVar, iVar) - temp*YX;
           omega(iVar, iVar) = tempVar + omega(iVar, iSp)/omega(iSp, iSp)*omega(iSp, iVar);
       else
           oInv = zeros(nVars, nVars);
           oInv(vcomp, vcomp) = inv(omega(vcomp, vcomp)); %\Omega_{-i, -i}^-1
           Z = oInv(iSp, vcomp)* beta(vcomp, :);
           XX = Z*covMat*Z';
           YX = [covMat(iVar, :)*Z']';
           omega(iVar, iSp) = YX'/XX;
           omega(iSp, iVar) = omega(iVar, iSp);
           tempVar = covMat(iVar, iVar)-omega(iVar,iSp)*YX;
           omega(iVar, iVar) = tempVar+ omega(iVar, iSp)*oInv(iSp, iSp)*omega(iSp, iVar);
       end
%        fprintf('iteration %d, variable %d------------\n',iter, iVar);
%        fprintf('Changed:\n');
% 
%        [x, y]= find(ricf(iter).omega-omega); [w,z] = find(ricf(iter).beta-beta);
%        for iX =1:length(x)
%            fprintf('%d-%d, %.3f --> %.3f\n', x(iX), y(iX), ricf(iter).omega(x(iX), y(iX)), omega(x(iX), y(iX)));
%        end
%       for iX =1:length(w)
%            fprintf('%d-%d, %.3f-->%.3f\n', w(iX), z(iX), ricf(iter).beta(w(iX), z(iX)), beta(w(iX), z(iX)));
%        end
    end
   if (sum(sum(abs(ricf(iter).omega-omega))) + sum(sum(abs(ricf(iter).beta-beta))) < tol)
       break;
   end
end
hatCovMat = inv(beta)*omega*inv(beta');
end


