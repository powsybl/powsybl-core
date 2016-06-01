%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function COR1 = closest_corr(COR)
[ V D] = eig(COR,'nobalance');
Dp = D;

for jd = 1:size(D,1)
    if D(jd,jd) <= 1e-4
        Dp(jd,jd) = 1e-4;
    end
end
T = zeros(size(D));

eigenv_p = diag(Dp);

for i = 1:size(T,1)
  T(i,i)= 1/((V(i,:).^2)*eigenv_p); 
%      T(i,i) = dummy;
end

Bp = V * sqrt(Dp);

Bf = sqrt(T)*Bp;

COR1 = Bf*Bf';

for jd = 1:size(D,1)
COR1(jd,jd)=1;
end
