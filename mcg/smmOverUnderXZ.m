function smm = smmOverUnderXZ(smm,x, z)
% function Gover = smmOverUnderX(smm,x,z)
% takes as input smm G and outputs smm G_{\overline X\underline Z}

nX = length(x); nZ = length(z);
for iX=1:nX
    intoX = smm(:, x(iX))==2|smm(:, x(iX))==4;
    smm(intoX, x(iX))=0; smm(x(iX), intoX) =0;
end

for iZ=1:nZ
    outofZ = smm(:, z(iZ))==3;
    smm(outofZ, z(iZ))=0; smm(z(iZ), outofZ) =0;
end
