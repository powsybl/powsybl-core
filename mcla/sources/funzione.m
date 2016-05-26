%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function X = funzione(teta,C)

B = ones(size(C));

n = size(B,1);

for jB = 1:size(B,1)
   
       B(jB,1) = cos(teta(jB,1));
       for k = 2:n-1
           B(jB,k) = cos(teta(jB,k));
           for p = 1:k-1
        B(jB,k) = B(jB,k)*sin(teta(jB,p));
           end
       end
       for p = 1:n-1
       B(jB,n) = B(jB,n)* sin(teta(jB,p));
       end
end
% keyboard
rho = B*B';

% X = norm(rho-C,'fro');

X = real(max(max(abs(rho-C))));%,'fro');
