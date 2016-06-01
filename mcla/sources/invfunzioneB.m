%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function teta = invfunzioneB(B)

n = size(B,1);

for iB = 1:size(B,1)
   teta(iB,1) = acos(min(0.999,B(iB,1)));D=1;
   
   for k = 2:n-1
       
       D = D*sin(teta(iB,k-1));
       if abs(D)<1e-5
          D = 1e-5; 
       end
       teta(iB,k) = acos(B(iB,k)/D);
   end
    teta(iB,n-1) = asin(B(iB,n)/D);
end
