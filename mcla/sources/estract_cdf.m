%
% Copyright (c) 2016, Ricerca sul Sistema Energetico – RSE S.p.A. <itesla@rse-web.it>
% This Source Code Form is subject to the terms of the Mozilla Public
% License, v. 2.0. If a copy of the MPL was not distributed with this
% file, You can obtain one at http://mozilla.org/MPL/2.0/.
%

function CDF = estract_cdf(Z,tol)
if nargin < 2
    tol = 1e-8;
end
[y x] = ecdf(Z);
A = [x y];

[dummy1, uniq,dummy3]=unique(A(:,1));

CDF = max(tol,min(1-tol,interp1(A(uniq,1),A(uniq,2),Z,'linear','extrap')));
