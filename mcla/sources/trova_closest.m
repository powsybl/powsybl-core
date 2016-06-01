%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function U2 = trova_closest(U1,F,T,var_miss,var_av)

pdfx = copulapdf('gaussian',U1,T);

Z = rand(5000,length(var_miss));

UX = zeros(1,length(var_miss)+length(var_av));
UX(var_av) = F;

for iZ = 1:size(Z,1)
    UX(var_miss) = Z(iZ,:);
   pdfZ(iZ) =  copulapdf('gaussian',UX,T);
end

idxZ = find(abs(pdfZ-pdfx)==min(abs(pdfZ-pdfx)));
% keyboard
UX(var_miss) = Z(idxZ(1),:);

U2 = UX;
